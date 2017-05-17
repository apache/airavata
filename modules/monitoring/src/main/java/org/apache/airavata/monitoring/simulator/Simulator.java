/**
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
 */
package org.apache.airavata.monitoring.simulator;

import org.apache.airavata.monitoring.consumer.StatusReceiver;

public class Simulator {
    private static final String EXCHANGE_NAME = "monitor";
    private static final String QUEUE_NAME1 = "q1";
    private static final String QUEUE_NAME2 = "q2";
    private static final String BROKER_URI = "amqp://localhost:5672";

    public static void main(String args[]) {
        try {
            //Consumer 1
            StatusReceiver statusReceiver1 = new StatusReceiver(EXCHANGE_NAME, QUEUE_NAME1, BROKER_URI);
            //Consumer 2
            StatusReceiver statusReceiver2 = new StatusReceiver(EXCHANGE_NAME, QUEUE_NAME2, BROKER_URI);
            statusReceiver1.startThread();
            statusReceiver2.startThread();
            //publisher
            FetchPublish.fetchEmailAndPublish();
            Thread.sleep(60000);
            //shutdown after a minute, for demo purposes
            statusReceiver1.shutdown();
            statusReceiver2.shutdown();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
