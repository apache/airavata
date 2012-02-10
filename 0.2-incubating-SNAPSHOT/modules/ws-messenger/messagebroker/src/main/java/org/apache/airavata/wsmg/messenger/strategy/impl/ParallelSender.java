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
import java.util.concurrent.TimeUnit;

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

    private static final long TIME_TO_WAIT_FOR_SHUTDOWN_SECOND = 30;

    private HashMap<String, ConsumerHandler> activeConsumerHandlers = new HashMap<String, ConsumerHandler>();

    private ExecutorService threadPool;

    public void init() {
        this.threadPool = Executors.newCachedThreadPool();
    }

    public void addMessageToSend(OutGoingMessage outMessage, Deliverable deliverable) {
        List<ConsumerInfo> consumerInfoList = outMessage.getConsumerInfoList();
        for (ConsumerInfo consumer : consumerInfoList) {
            sendToConsumerHandler(consumer, outMessage, deliverable);
        }
    }

    public void shutdown() {
        log.debug("Shutting down");

        threadPool.shutdown();
        try {
            threadPool.awaitTermination(TIME_TO_WAIT_FOR_SHUTDOWN_SECOND, TimeUnit.SECONDS);
        } catch (InterruptedException ie) {
            log.error("Interrupted while waiting thread pool to shutdown");
        }
        log.debug("Shut down");
    }

    private void sendToConsumerHandler(ConsumerInfo consumer, OutGoingMessage message, Deliverable deliverable) {
        String consumerUrl = consumer.getConsumerEprStr();

        LightweightMsg lwm = new LightweightMsg(consumer, message.getTextMessage(),
                message.getAdditionalMessageContent());

        synchronized (activeConsumerHandlers) {
            ConsumerHandler handler = activeConsumerHandlers.get(consumerUrl);
            if (handler == null) {
                handler = new ParallelConsumerHandler(consumerUrl, deliverable);
                activeConsumerHandlers.put(consumerUrl, handler);
                handler.submitMessage(lwm);
                threadPool.submit(handler);
            } else {
                handler.submitMessage(lwm);
            }
        }
    }

    class ParallelConsumerHandler extends ConsumerHandler {

        private static final int MAX_UNSUCCESSFUL_DRAINS = 3;
        private static final int SLEEP_TIME_SECONDS = 1;
        private int numberOfUnsuccessfulDrain = 0;

        public ParallelConsumerHandler(String url, Deliverable deliverable) {
            super(url, deliverable);
        }

        public void run() {
            log.debug(String.format("ParallelConsumerHandler starting: %s", getConsumerUrl()));

            ArrayList<LightweightMsg> localList = new ArrayList<LightweightMsg>();
            while (true) {

                /*
                 * Try to find more message to send out
                 */
                if (queue.drainTo(localList) <= 0) {
                    numberOfUnsuccessfulDrain++;
                } else {
                    numberOfUnsuccessfulDrain = 0;
                }

                /*
                 * No new message for sometimes
                 */
                if (numberOfUnsuccessfulDrain >= MAX_UNSUCCESSFUL_DRAINS) {
                    /*
                     * Stop this thread if and only if there is no message
                     */
                    synchronized (activeConsumerHandlers) {
                        if (queue.size() == 0) {
                            if (activeConsumerHandlers.remove(getConsumerUrl()) != null) {
                                log.debug(String.format("Consumer handler is already removed: %s", getConsumerUrl()));
                            }
                            log.debug(String.format("ParallelConsumerHandler done: %s,", getConsumerUrl()));
                            break;
                        }
                    }
                }

                send(localList);
                localList.clear();

                if (numberOfUnsuccessfulDrain > 0) {
                    waitForMessages();
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
