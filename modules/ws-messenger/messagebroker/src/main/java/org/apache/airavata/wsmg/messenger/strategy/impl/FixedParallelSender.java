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
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.airavata.wsmg.broker.ConsumerInfo;
import org.apache.airavata.wsmg.commons.OutGoingMessage;
import org.apache.airavata.wsmg.messenger.Deliverable;
import org.apache.airavata.wsmg.messenger.strategy.SendingStrategy;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class FixedParallelSender implements SendingStrategy {

    private static final Log log = LogFactory.getLog(FixedParallelSender.class);

    private static final long TIME_TO_WAIT_FOR_SHUTDOWN_SECOND = 30;

    private HashMap<String, ConsumerHandler> activeConsumerHandlers = new HashMap<String, ConsumerHandler>();
    private HashMap<String, Boolean> submittedConsumerHandlers = new HashMap<String, Boolean>();

    private int batchSize;

    private ExecutorService threadPool;

    private boolean stop;

    private Thread t;

    public FixedParallelSender(int poolsize, int batchsize) {
        this.threadPool = Executors.newFixedThreadPool(poolsize);
        this.batchSize = batchsize;
    }

    public void init() {
        this.t = new Thread(new ChooseHandlerToSubmit());
        this.t.start();
    }

    public void addMessageToSend(OutGoingMessage outMessage, Deliverable deliverable) {
        List<ConsumerInfo> consumerInfoList = outMessage.getConsumerInfoList();
        for (ConsumerInfo consumer : consumerInfoList) {
            sendToConsumerHandler(consumer, outMessage, deliverable);
        }
    }

    public void shutdown() {
        log.debug("Shutting down");
        this.stop = true;

        try {
            this.t.join();
        } catch (InterruptedException ie) {
            log.error("Wait for ChooseHandlerToSubmit thread to finish (join) is interrupted");
        }

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
                handler = new FixedParallelConsumerHandler(consumerUrl, deliverable);
                activeConsumerHandlers.put(consumerUrl, handler);
                submittedConsumerHandlers.put(consumerUrl, Boolean.FALSE);
            }
            handler.submitMessage(lwm);
        }
    }

    class ChooseHandlerToSubmit implements Runnable {
        private static final int SLEEP_TIME_SECONDS = 1;

        public void run() {
            /*
             * If stop is true, we will not get any message to send from addMessageToSend() method. So,
             * activeConsumerHandlers size will not increase but decrease only. When shutdown() is invoked, we will have
             * to send out all messages in our queue.
             */
            while (!stop || activeConsumerHandlers.size() > 0) {

                synchronized (activeConsumerHandlers) {
                    Iterator<String> it = activeConsumerHandlers.keySet().iterator();
                    while (it.hasNext()) {
                        String key = it.next();
                        boolean submitted = submittedConsumerHandlers.get(key);

                        /*
                         * If consumer handlers is not scheduled to run, submit it to thread pool.
                         */
                        if (!submitted) {
                            threadPool.submit(activeConsumerHandlers.get(key));
                            submittedConsumerHandlers.put(key, Boolean.TRUE);
                        }
                    }
                }

                try {
                    TimeUnit.SECONDS.sleep(SLEEP_TIME_SECONDS);
                } catch (InterruptedException e) {
                    log.error("interrupted while waiting", e);
                }
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

            /*
             * Remove handler if and only if there is no message
             */
            synchronized (activeConsumerHandlers) {

                /*
                 * all message is sent or not, we will set it as not submitted. So, it can be put back to thread pool.
                 */
                submittedConsumerHandlers.put(getConsumerUrl(), Boolean.FALSE);

                if (queue.size() == 0) {
                    submittedConsumerHandlers.remove(getConsumerUrl());
                    activeConsumerHandlers.remove(getConsumerUrl());

                    log.debug(String.format("Consumer handler is already removed: %s", getConsumerUrl()));
                }
            }

            log.debug(String.format("FixedParallelConsumerHandler done: %s,", getConsumerUrl()));
        }
    }
}
