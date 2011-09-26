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
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.airavata.wsmg.broker.ConsumerInfo;
import org.apache.airavata.wsmg.commons.OutGoingMessage;
import org.apache.airavata.wsmg.messenger.Deliverable;
import org.apache.airavata.wsmg.messenger.strategy.SendingStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FixedParallelSender implements SendingStrategy {

    private static final Logger log = LoggerFactory.getLogger(FixedParallelSender.class);

    private HashMap<String, ConsumerHandler> activeConsumerHanders = new HashMap<String, ConsumerHandler>();

    private int batchSize;

    private ExecutorService threadPool;

    public FixedParallelSender(int poolsize, int batchsize) {
        this.threadPool = Executors.newFixedThreadPool(poolsize);
        this.batchSize = batchsize;
    }

    public void init() {

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

        synchronized (activeConsumerHanders) {
            ConsumerHandler handler = activeConsumerHanders.get(consumerUrl);
            if (handler == null) {
                handler = new FixedParallelConsumerHandler(consumerUrl, deliverable);
                activeConsumerHanders.put(consumerUrl, handler);
                handler.submitMessage(lwm);
                threadPool.submit(handler);
            } else {
                handler.submitMessage(lwm);
            }
        }
    }

    public void removeFromList(ConsumerHandler h) {
        synchronized (activeConsumerHanders) {
            if (activeConsumerHanders.remove(h.getConsumerUrl()) != null) {
                log.debug(String.format("inactive consumer handler is already removed: url : %s", h.getConsumerUrl()));
            }
        }
    }

    class FixedParallelConsumerHandler extends ConsumerHandler {

        public FixedParallelConsumerHandler(String url, Deliverable deliverable) {
            super(url, deliverable);
        }

        public void run() {

            log.debug(String.format("FixedParallelConsumerHandler starting: %s", getConsumerUrl()));

            ArrayList<LightweightMsg> localList = new ArrayList<LightweightMsg>();

            queue.drainTo(localList, batchSize);

            send(localList);
            localList.clear();

            log.debug(String.format("FixedParallelConsumerHandler done: %s,", getConsumerUrl()));

            /*
             * Remove handler if there is no message
             */
            if (queue.size() == 0) {
                removeFromList(this);
            }
        }
    }
}
