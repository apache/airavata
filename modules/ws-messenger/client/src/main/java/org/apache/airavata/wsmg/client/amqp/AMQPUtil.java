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

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.net.URL;

/**
 *  AMQPUtil provides common utilities required for the AMQP transport implementation.
 */
public class AMQPUtil {
    
    public static final String CONFIG_AMQP_ENABLE = "amqp.notification.enable";

    public static final String CONFIG_AMQP_PROVIDER_HOST = "amqp.broker.host";
    public static final String CONFIG_AMQP_PROVIDER_PORT = "amqp.broker.port";
    public static final String CONFIG_AMQP_PROVIDER_USERNAME = "amqp.broker.username";
    public static final String CONFIG_AMQP_PROVIDER_PASSWORD = "amqp.broker.password";

    public static final String CONFIG_AMQP_SENDER = "amqp.sender";
    public static final String CONFIG_AMQP_TOPIC_SENDER = "amqp.topic.sender";
    public static final String CONFIG_AMQP_BROADCAST_SENDER = "amqp.broadcast.sender";

    public static final String EXCHANGE_NAME_DIRECT = "ws-messenger-direct";
    public static final String EXCHANGE_TYPE_DIRECT = "direct";
    public static final String EXCHANGE_NAME_TOPIC = "ws-messenger-topic";
    public static final String EXCHANGE_TYPE_TOPIC = "topic";
    public static final String EXCHANGE_NAME_FANOUT = "ws-messenger-fanout";
    public static final String EXCHANGE_TYPE_FANOUT = "fanout";

    private static final String ROUTING_KEY_FILENAME = "amqp-routing-keys.xml";

    /**
     * Load routing keys from configuration file.
     *
     * @return Root element of routing key object model.
     * @throws AMQPException on error.
     */
    public static Element loadRoutingKeys() throws AMQPException {
        try {
            URL resource = AMQPUtil.class.getClassLoader().getResource(ROUTING_KEY_FILENAME);
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            Document document = docBuilder.parse(new File(resource.getPath()));

            return document.getDocumentElement();
        } catch (Exception e) {
            throw new AMQPException(e);
        }
    }
}
