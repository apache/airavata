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

package org.apache.airavata.wsmg.client.amqp;

import org.apache.airavata.common.utils.ApplicationSettings;
import org.apache.airavata.wsmg.client.amqp.rabbitmq.AMQPBroadcastReceiverImpl;
import java.util.Properties;

public class BroadcastSubscriber {
    public static void main(String args[]) throws AMQPException {
        String host = ApplicationSettings.getSetting(AMQPUtil.CONFIG_AMQP_PROVIDER_HOST, "localhost");
        String port = ApplicationSettings.getSetting(AMQPUtil.CONFIG_AMQP_PROVIDER_PORT, "5672");
        String username = ApplicationSettings.getSetting(AMQPUtil.CONFIG_AMQP_PROVIDER_USERNAME, "guest");
        String password = ApplicationSettings.getSetting(AMQPUtil.CONFIG_AMQP_PROVIDER_PASSWORD, "guest");

        Properties properties = new Properties();
        properties.setProperty(AMQPUtil.CONFIG_AMQP_PROVIDER_HOST, host);
        properties.setProperty(AMQPUtil.CONFIG_AMQP_PROVIDER_PORT, port);
        properties.setProperty(AMQPUtil.CONFIG_AMQP_PROVIDER_USERNAME, username);
        properties.setProperty(AMQPUtil.CONFIG_AMQP_PROVIDER_PASSWORD, password);

        MessageConsumer consumer = new MessageConsumer();
        AMQPBroadcastReceiver receiver = new AMQPBroadcastReceiverImpl(properties, consumer);
        System.out.println("Waiting for broadcast messages : \n");

        receiver.Subscribe();
    }

    public static class MessageConsumer implements AMQPCallback {
        public void onMessage(String message) {
            System.out.println("Received : " + message);
        }
    }
}
