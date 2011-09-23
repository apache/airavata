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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.airavata.wsmg.broker.ConsumerInfo;
import org.apache.airavata.wsmg.commons.OutGoingMessage;
import org.apache.airavata.wsmg.messenger.Deliverable;
import org.apache.airavata.wsmg.messenger.strategy.SendingStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Each subscriber (URL Endpoint) will have its own thread to send a message to
 * 
 */
public class ParallelSender implements SendingStrategy {

    private static final Logger log = LoggerFactory.getLogger(ParallelSender.class);

    private ConcurrentHashMap<String, ParallelConsumerHandler> activeConsumerHanders = new ConcurrentHashMap<String, ParallelConsumerHandler>();

    private ExecutorService threadPool;
    private long consumerHandlerIdCounter;

    public void init() {
        this.threadPool = Executors.newCachedThreadPool();
    }

    public void addMessageToSend(OutGoingMessage outMessage, Deliverable deliverable) {
        distributeOverConsumerQueues(outMessage, deliverable);
    }

    public void shutdown() {
        threadPool.shutdown();
    }

    public void distributeOverConsumerQueues(OutGoingMessage message, Deliverable deliverable) {
        List<ConsumerInfo> consumerInfoList = message.getConsumerInfoList();
        for (ConsumerInfo consumer : consumerInfoList) {
            sendToConsumerHandler(consumer, message, deliverable);
        }
    }

    private void sendToConsumerHandler(ConsumerInfo consumer, OutGoingMessage message, Deliverable deliverable) {
        String consumerUrl = consumer.getConsumerEprStr();

        LightweightMsg lwm = new LightweightMsg(consumer, message.getTextMessage(),
                message.getAdditionalMessageContent());

        ParallelConsumerHandler handler = activeConsumerHanders.get(consumerUrl);
        if (handler == null || !handler.isActive()) {
            handler = new ParallelConsumerHandler(consumerHandlerIdCounter++, consumerUrl, deliverable);
            activeConsumerHanders.put(consumerUrl, handler);
            handler.submitMessage(lwm);
            threadPool.submit(handler);
        } else {
            handler.submitMessage(lwm);
        }
    }

    public void removeFromList(ConsumerHandler h) {
        if (!activeConsumerHanders.remove(h.getConsumerUrl(), h)) {
            log.debug(String.format("inactive consumer handler " + "is already removed: id %d, url : %s", h.getId(),
                    h.getConsumerUrl()));
        }
    }

    class ParallelConsumerHandler extends ConsumerHandler {

        private ReadWriteLock activeLock = new ReentrantReadWriteLock();

        private static final int MAX_UNSUCCESSFULL_DRAINS = 3;
        private static final int SLEEP_TIME_SECONDS = 1;
        private int numberOfUnsuccessfullDrainAttempts = 0;

        private boolean active;

        public ParallelConsumerHandler(long handlerId, String url, Deliverable deliverable) {
            super(handlerId, url, deliverable);
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

        public void run() {
            this.active = true;

            log.debug(String.format("starting consumer handler: id :%d, url : %s", getId(), getConsumerUrl()));

            ArrayList<LightweightMsg> localList = new ArrayList<LightweightMsg>();
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
                        break;
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

            log.debug(String.format("calling on completion from : %d,", getId()));

            removeFromList(this);

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
