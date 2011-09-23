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
import org.apache.airavata.wsmg.util.Counter;
import org.apache.airavata.wsmg.util.TimerThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OutGoingQueue {

    private static final Logger logger = LoggerFactory.getLogger(OutGoingQueue.class);

    private Counter storeToOutQueueCounter;

    public OutGoingQueue() {
        if (WSMGParameter.measureMessageRate) {
            storeToOutQueueCounter = new Counter();
            TimerThread timerThread = new TimerThread(storeToOutQueueCounter, " StoreToOutQueueCounter");
            new Thread(timerThread).start();
        }
    }

    // need synchronized???
    public void storeNotification(OutGoingMessage outGoingMessage, long messageId) {

        boolean loop = false;
        do {

            // this outgoing Queue is created inside the messenger which is
            // intended to send the notification message to the consumer.
            WSMGParameter.OUT_GOING_QUEUE.enqueue(outGoingMessage, outGoingMessage.getAdditionalMessageContent()
                    .getTrackId());

            if (WSMGParameter.measureMessageRate) {
                storeToOutQueueCounter.addCounter();
            }

            if (WSMGParameter.testOutGoingQueueMaxiumLength && storeToOutQueueCounter.getCounterValue() < 1000000) {
                loop = true;
            }
        } while (loop);

    }
}
