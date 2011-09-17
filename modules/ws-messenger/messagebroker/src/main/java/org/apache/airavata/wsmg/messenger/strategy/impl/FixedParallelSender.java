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
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.airavata.wsmg.broker.ConsumerInfo;
import org.apache.airavata.wsmg.commons.CommonRoutines;
import org.apache.airavata.wsmg.commons.OutGoingMessage;
import org.apache.airavata.wsmg.commons.WsmgCommonConstants;
import org.apache.airavata.wsmg.commons.config.ConfigurationManager;
import org.apache.airavata.wsmg.config.WSMGParameter;
import org.apache.airavata.wsmg.messenger.ConsumerUrlManager;
import org.apache.airavata.wsmg.messenger.SenderUtils;
import org.apache.airavata.wsmg.messenger.strategy.SendingStrategy;
import org.apache.axiom.om.OMElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FixedParallelSender extends Thread implements SendingStrategy {

    private static final Logger log = LoggerFactory.getLogger(FixedParallelSender.class);

    private ConcurrentHashMap<String, ConsumerHandler> activeConsumerHanders = new ConcurrentHashMap<String, ConsumerHandler>();

    private ThreadCrew threadCrew = null;

    private ConsumerUrlManager urlManager = null;
    private ConfigurationManager configManager = null;

    private long consumerHandlerIdCounter;

    private boolean stopFlag = false;

    public FixedParallelSender(ConfigurationManager config, ConsumerUrlManager urlMan) {

        int poolSize = config.getConfig(WsmgCommonConstants.CONFIG_SENDING_THREAD_POOL_SIZE,
                WsmgCommonConstants.DEFAULT_SENDING_THREAD_POOL_SIZE);

        threadCrew = new ThreadCrew(poolSize);
        urlManager = urlMan;
        configManager = config;
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
                log.error("Unexpected_exception:", e);
            }

            dequeuedMessageCounter++;
        }

        threadCrew.stop();

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

        if (handler == null) {
            handler = new ConsumerHandler(getNextConsumerHandlerId(), consumerUrl, configManager, urlManager);
            activeConsumerHanders.put(consumerUrl, handler);
            handler.submitMessage(lwm); // import to submit before execute.
            threadCrew.submitTask(handler);
            // (to remove a possible race
            // condition)
        } else {
            handler.submitMessage(lwm);
        }

        return handler;

    }

    private long getNextConsumerHandlerId() {
        return ++consumerHandlerIdCounter;
    }

    class ConsumerHandler implements RunnableEx {

        LinkedBlockingQueue<LightweightMsg> queue = new LinkedBlockingQueue<LightweightMsg>();

        final long id;
        int batchSize;

        ThreadLocal<SenderUtils> threadlocalSender = new ThreadLocal<SenderUtils>();

        // SenderUtils sender = null;
        String consumerUrl;

        ConfigurationManager configMan;
        ConsumerUrlManager consumerURLManager;

        public ConsumerHandler(long handlerId, String url, ConfigurationManager config, ConsumerUrlManager urlMan) {

            configMan = config;
            consumerURLManager = urlMan;
            // sender = new SenderUtils(urlMan, config, true);
            id = handlerId;
            consumerUrl = url;

            batchSize = config.getConfig(WsmgCommonConstants.CONFIG_SENDING_BATCH_SIZE,
                    WsmgCommonConstants.DEFAULT_SENDING_BATCH_SIZE);
        }

        public long getId() {
            return id;
        }

        public String getConsumerUrl() {
            return consumerUrl;
        }

        private SenderUtils getSender() {

            SenderUtils s = threadlocalSender.get();

            if (s == null) {
                s = new SenderUtils(consumerURLManager, configMan);
                threadlocalSender.set(s);
            }

            return s;
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

            queue.drainTo(localList, batchSize);

            send(localList);
            localList.clear();

            if (log.isDebugEnabled())
                log.debug(String.format("calling on completion from : %d,", getId()));

        }

        private void send(LinkedList<LightweightMsg> list) {

            SenderUtils s = getSender();

            while (!list.isEmpty()) {

                LightweightMsg m = list.removeFirst();

                try {
                    OMElement messgae2Send = CommonRoutines.reader2OMElement(new StringReader(m.getPayLoad()));

                    s.send(m.getConsumerInfo(), messgae2Send, m.getHeader());

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }

            }

        }
    }

}
