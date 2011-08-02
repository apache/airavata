/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.apache.airavata.wsmg.messenger.strategy.impl;

import java.io.StringReader;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.airavata.wsmg.broker.ConsumerInfo;
import org.apache.airavata.wsmg.commons.CommonRoutines;
import org.apache.airavata.wsmg.commons.OutGoingMessage;
import org.apache.airavata.wsmg.config.ConfigurationManager;
import org.apache.airavata.wsmg.config.WSMGParameter;
import org.apache.airavata.wsmg.messenger.ConsumerUrlManager;
import org.apache.airavata.wsmg.messenger.SenderUtils;
import org.apache.airavata.wsmg.messenger.strategy.SendingStrategy;
import org.apache.axiom.om.OMElement;
import org.apache.log4j.Logger;

public class ParallelSender extends Thread implements SendingStrategy {

    private Logger log = Logger.getLogger(ParallelSender.class);

    private ConcurrentHashMap<String, ConsumerHandler> activeConsumerHanders = new ConcurrentHashMap<String, ConsumerHandler>();

    private final ExecutorService threadPool;
    private long consumerHandlerIdCounter = 0L;
    private boolean stopFlag = false;

    private ConsumerUrlManager urlManager = null;
    private ConfigurationManager configManager = null;

    private ConsumerHandlerCompletionCallback consumerCallback = new ConsumerHandlerCompletionCallback() {

        public void onCompletion(ConsumerHandler h) {

            if (!activeConsumerHanders.remove(h.getConsumerUrl(), h)) {

                if (log.isDebugEnabled())
                    log.debug(String.format("inactive consumer handler " + "is already removed: id %d, url : %s",
                            h.getId(), h.getConsumerUrl()));
            }

        }
    };

    public ParallelSender(ConfigurationManager config, ConsumerUrlManager urlMan) {
        urlManager = urlMan;
        configManager = config;

        threadPool = Executors.newCachedThreadPool();

    }

    public void shutdown() {
        stopFlag = true;
    }

    public void run() {
        int dequeuedMessageCounter = 0;

        while (!stopFlag) {

            try {

                if (log.isDebugEnabled())
                    log.debug("before dequeue -  delivery thread");

                OutGoingMessage outGoingMessage = (OutGoingMessage) WSMGParameter.OUT_GOING_QUEUE.blockingDequeue();

                if (WSMGParameter.showTrackId)
                    log.debug(outGoingMessage.getAdditionalMessageContent().getTrackId()
                            + ": dequeued from outgoing queue");

                distributeOverConsumerQueues(outGoingMessage);

            } catch (Exception e) {

                log.fatal("Unexpected_exception:", e);
            }

            dequeuedMessageCounter++;
        }

        threadPool.shutdown();

    }

    public void distributeOverConsumerQueues(OutGoingMessage message) {
        List<ConsumerInfo> consumerInfoList = message.getConsumerInfoList();

        for (ConsumerInfo consumer : consumerInfoList) {

            sendToConsumerHandler(consumer, message);

        }

    }

    private ConsumerHandler sendToConsumerHandler(ConsumerInfo consumer, OutGoingMessage message) {

        String consumerUrl = consumer.getConsumerEprStr();

        LightweightMsg lwm = new LightweightMsg(consumer, message.getTextMessage(),
                message.getAdditionalMessageContent());

        ConsumerHandler handler = activeConsumerHanders.get(consumerUrl);

        if (handler == null || (!handler.isActive())) {
            handler = new ConsumerHandler(getNextConsumerHandlerId(), consumerUrl, consumerCallback, configManager,
                    urlManager);
            activeConsumerHanders.put(consumerUrl, handler);
            handler.submitMessage(lwm); // import to submit before execute.
            // (to remove a possible race
            // condition)
            threadPool.execute(handler);
        } else {
            handler.submitMessage(lwm);
        }

        return handler;
    }

    private long getNextConsumerHandlerId() {
        return ++consumerHandlerIdCounter;
    }

    interface ConsumerHandlerCompletionCallback {

        public void onCompletion(ConsumerHandler h);

    }

    class ConsumerHandler implements Runnable {

        LinkedBlockingQueue<LightweightMsg> queue = new LinkedBlockingQueue<LightweightMsg>();

        ReadWriteLock activeLock = new ReentrantReadWriteLock();

        final long id;
        final int MAX_UNSUCCESSFULL_DRAINS = 3;
        final int SLEEP_TIME_SECONDS = 1;
        int numberOfUnsuccessfullDrainAttempts = 0;

        boolean active = true;

        ConsumerHandlerCompletionCallback callback = null;
        SenderUtils sender = null;
        String consumerUrl;

        public ConsumerHandler(long handlerId, String url, ConsumerHandlerCompletionCallback c,
                ConfigurationManager config, ConsumerUrlManager urlMan) {
            callback = c;
            sender = new SenderUtils(urlMan, config);
            id = handlerId;
            consumerUrl = url;
        }

        public long getId() {
            return id;
        }

        public String getConsumerUrl() {
            return consumerUrl;
        }

        public boolean isActive() {

            boolean ret = false;

            activeLock.readLock().lock();
            try {
                ret = active;
            } finally {
                activeLock.readLock().unlock();
            }

            return ret;
        }

        @Override
        public boolean equals(Object o) {

            if (o instanceof ConsumerHandler) {
                ConsumerHandler h = (ConsumerHandler) o;
                return h.getId() == id && h.getConsumerUrl().equals(this.getConsumerUrl());
            }

            return false;
        }

        public void submitMessage(LightweightMsg msg) {
            queue.add(msg);
        }

        public void run() {

            if (log.isDebugEnabled())
                log.debug(String.format("starting consumer handler: id :%d, url : %s", getId(), getConsumerUrl()));

            LinkedList<LightweightMsg> localList = new LinkedList<LightweightMsg>();

            while (active) {

                int drainedMsgs = 0;
                try {
                    activeLock.writeLock().lock();

                    drainedMsgs = queue.drainTo(localList);

                    if (drainedMsgs <= 0) {
                        numberOfUnsuccessfullDrainAttempts++;
                    } else {
                        numberOfUnsuccessfullDrainAttempts = 0;
                    }

                    if (numberOfUnsuccessfullDrainAttempts >= MAX_UNSUCCESSFULL_DRAINS) {

                        log.debug(String.format("inactivating, %d", getId()));

                        active = false;
                        numberOfUnsuccessfullDrainAttempts = 0;
                    }

                } finally {
                    activeLock.writeLock().unlock();
                }

                send(localList);
                localList.clear();

                if (numberOfUnsuccessfullDrainAttempts > 0) {
                    waitForMessages();
                }

            }

            if (log.isDebugEnabled())
                log.debug(String.format("calling on completion from : %d,", getId()));

            callback.onCompletion(this);

        }

        private void send(LinkedList<LightweightMsg> list) {

            while (!list.isEmpty()) {

                LightweightMsg m = list.removeFirst();

                try {
                    OMElement messgae2Send = CommonRoutines.reader2OMElement(new StringReader(m.getPayLoad()));

                    sender.send(m.getConsumerInfo(), messgae2Send, m.getHeader());

                } catch (Exception e) {
                    log.fatal(e);
                }

            }

        }

        private void waitForMessages() {
            try {

                TimeUnit.SECONDS.sleep(SLEEP_TIME_SECONDS);
                log.debug("finished - waiting for messages");
            } catch (InterruptedException e) {
                log.error("interrupted while waiting for messages", e);
            }
        }
    }
}
