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

package org.apache.airavata.wsmg.broker.amqp;

import org.apache.airavata.common.utils.ApplicationSettings;
import org.apache.airavata.wsmg.client.amqp.*;
import org.apache.airavata.wsmg.commons.NameSpaceConstants;
import org.apache.airavata.wsmg.broker.context.ProcessingContext;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMException;
import org.apache.axiom.om.OMNamespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;

/**
 * AMQPNotificationProcessor handles AMQP-specific notification processing.
 */
public class AMQPNotificationProcessor {

    private static final Logger logger = LoggerFactory.getLogger(AMQPNotificationProcessor.class);
    
    private boolean amqpEnabled = false;
    private AMQPSender amqpSender = null;
    private AMQPTopicSender amqpTopicSender = null;
    private AMQPBroadcastSender amqpBroadcastSender = null;

    public void init() {
        String amqpEnabledAppSetting = ApplicationSettings.getSetting(AMQPUtil.CONFIG_AMQP_ENABLE, "");
        if (!amqpEnabledAppSetting.isEmpty() && (1 == Integer.parseInt(amqpEnabledAppSetting))) {
            try {
                String host = ApplicationSettings.getSetting(AMQPUtil.CONFIG_AMQP_PROVIDER_HOST, "localhost");
                String port = ApplicationSettings.getSetting(AMQPUtil.CONFIG_AMQP_PROVIDER_PORT, "5672");
                String username = ApplicationSettings.getSetting(AMQPUtil.CONFIG_AMQP_PROVIDER_USERNAME, "guest");
                String password = ApplicationSettings.getSetting(AMQPUtil.CONFIG_AMQP_PROVIDER_PASSWORD, "guest");

                Properties properties = new Properties();
                properties.setProperty(AMQPUtil.CONFIG_AMQP_PROVIDER_HOST, host);
                properties.setProperty(AMQPUtil.CONFIG_AMQP_PROVIDER_PORT, port);
                properties.setProperty(AMQPUtil.CONFIG_AMQP_PROVIDER_USERNAME, username);
                properties.setProperty(AMQPUtil.CONFIG_AMQP_PROVIDER_PASSWORD, password);

                String className = ApplicationSettings.getSetting(AMQPUtil.CONFIG_AMQP_SENDER, "");
                Class clazz = Class.forName(className);
                amqpSender = (AMQPSender)clazz.getDeclaredConstructor(Properties.class).newInstance(properties);

                className = ApplicationSettings.getSetting(AMQPUtil.CONFIG_AMQP_TOPIC_SENDER, "");
                clazz = Class.forName(className);
                amqpTopicSender = (AMQPTopicSender)clazz.getDeclaredConstructor(Properties.class).newInstance(properties);

                className = ApplicationSettings.getSetting(AMQPUtil.CONFIG_AMQP_BROADCAST_SENDER, "");
                clazz = Class.forName(className);
                amqpBroadcastSender = (AMQPBroadcastSender)clazz.getDeclaredConstructor(Properties.class).newInstance(properties);

                Element routingKeys = AMQPUtil.loadRoutingKeys();
                if (routingKeys != null) {
                    ((AMQPRoutingAwareClient)amqpSender).init(routingKeys);
                    ((AMQPRoutingAwareClient)amqpTopicSender).init(routingKeys);
                    ((AMQPRoutingAwareClient)amqpBroadcastSender).init(routingKeys);
                }

                amqpEnabled = true;
            } catch (Exception ex) {
                logger.error(ex.getMessage());
            }
        }
    }

    public void notify(ProcessingContext ctx, OMNamespace protocolNs) throws OMException {
        if (amqpEnabled) {
            // Extract messages
            List<OMElement> messages = new ArrayList<OMElement>();
            if (NameSpaceConstants.WSNT_NS.equals(protocolNs)) {
                // WSNT
                OMElement messageElements = ctx.getSoapBody().getFirstElement();
                for (Iterator<OMElement> ite = messageElements.getChildrenWithLocalName("NotificationMessage"); ite.hasNext(); ) {
                    try {
                        OMElement messageElement = ite.next();
                        OMElement message = messageElement.getFirstChildWithName(
                                new QName(NameSpaceConstants.WSNT_NS.getNamespaceURI(), "Message")).getFirstElement();
                        messages.add(message);
                    } catch (NullPointerException e) {
                        throw new OMException(e);
                    }
                }
            } else {
                // WSE
                OMElement message = ctx.getSoapBody().getFirstElement();
                if (message != null) {
                    messages.add(message);
                }
            }

            // Dispatch messages
            try {
                for (OMElement message : messages) {
                    amqpBroadcastSender.Send(message);
                    amqpTopicSender.Send(message);
                    amqpSender.Send(message);
                }
            } catch (AMQPException e) {
                logger.warn("Failed to send AMQP notification.[Reason=" + e.getMessage() + "]");
            }
        }
    }
}
