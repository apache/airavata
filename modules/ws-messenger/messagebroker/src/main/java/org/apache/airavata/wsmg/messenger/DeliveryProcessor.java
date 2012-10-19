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
package org.apache.airavata.wsmg.messenger;

import org.apache.airavata.wsmg.commons.OutGoingMessage;
import org.apache.airavata.wsmg.config.WSMGParameter;
import org.apache.airavata.wsmg.messenger.strategy.SendingStrategy;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DeliveryProcessor {

    private static final Log logger = LogFactory.getLog(DeliveryProcessor.class);

    private SendingStrategy strategy;
    private Deliverable deliverable;

    private boolean running;
    private Thread t;

    public DeliveryProcessor(Deliverable deliverable, SendingStrategy strategy) {
        this.strategy = strategy;
        this.deliverable = deliverable;
    }

    public void start() {
        this.running = true;
        this.t = new Thread(new CheckingAndSending());
        this.t.start();
    }

    public void stop() {
        this.running = false;

        if (this.t != null) {
            this.t.interrupt();

            try {
                this.t.join();
            } catch (InterruptedException ie) {
                logger.error("Wait for sending thread to finish (join) is interrupted");
            }
        }

        WSMGParameter.OUT_GOING_QUEUE.dispose();
    }

    private class CheckingAndSending implements Runnable {

        public void run() {
            strategy.init();
            while (running) {
                logger.debug("run - delivery thread");
                try {

                    OutGoingMessage outGoingMessage = (OutGoingMessage) WSMGParameter.OUT_GOING_QUEUE.blockingDequeue();

                    if (WSMGParameter.showTrackId)
                        logger.debug(outGoingMessage.getAdditionalMessageContent().getTrackId()
                                + ": dequeued from outgoing queue");

                    strategy.addMessageToSend(outGoingMessage, deliverable);

                } catch (Exception e) {
                    logger.warn("Unexpected_exception:");
                }
            }
            logger.debug("Shutdown Strategy");
            strategy.shutdown();
        }
    }
}
