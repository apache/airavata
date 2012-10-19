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

package org.apache.airavata.workflow.tracking.impl.subscription;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import org.apache.airavata.workflow.tracking.client.Callback;
import org.apache.airavata.workflow.tracking.client.NotificationType;
import org.apache.airavata.workflow.tracking.client.Subscription;
import org.apache.airavata.workflow.tracking.util.MessageUtil;
import org.apache.airavata.wsmg.client.ConsumerNotificationHandler;
import org.apache.airavata.wsmg.client.ConsumerServer;
import org.apache.airavata.wsmg.client.WseMsgBrokerClient;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPHeader;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Utility for clients to subscribe and receive Lead notifications using new message schema. The agent implements the
 * LeadNotificationHandler.Callback interface and starts the notification handler with the broker location, the topic,
 * and an option to pull the messages (to get around firewalls) by providing a message box url. The deliverMessage
 * method in the Callback interface in invoked when a message with the nes LEAD message type arrives. If a
 * LeadEvent/NCSAEvent arrives, it is silently dropped after being logged. Check the main() method for sample usage.
 */
public class LeadNotificationHandler implements ConsumerNotificationHandler {

    private static final Log logger = LogFactory.getLog(LeadNotificationHandler.class);

    private String topic;

    private String brokerLoc;

    private Callback callback;

    private int consumerServerPort;

    public LeadNotificationHandler(String brokerLoc, String topic, Callback callback, int port) {
        if (port == 0)
            this.consumerServerPort = 2222;
        else
            this.consumerServerPort = port;
        this.brokerLoc = brokerLoc;
        this.topic = topic;
        this.callback = callback;

    }

    /**
     * NON API Method. Use LeadNotificationManager.CreateSubscription() method to create a subscription
     * 
     * @param topic
     * @param callback
     * @return
     * @throws Exception
     */
    public Subscription createSubscription() throws Exception {
        WseMsgBrokerClient wseClient = new WseMsgBrokerClient();
        wseClient.init(brokerLoc);
        logger.debug("Starting Subscription for topic [" + topic + "]at the broker location:" + brokerLoc);
        ConsumerServer xs = new ConsumerServer(consumerServerPort, this);
        xs.start();
        String subscriptionId = wseClient.subscribe(xs.getConsumerServiceEPRs()[0], topic, null);
        logger.info("The consumer server started on EPR" + xs.getConsumerServiceEPRs()[0]);
        Subscription subscription = new Subscription(xs, subscriptionId, topic, callback, brokerLoc);
        return subscription;
    }

    /**
     * NONAPI method Method handleNotification. Called by the message broker when a message arrives at the subscribed
     * topic. Should NOT be called locally. This method will call the Callback interface's deliverMessage when a valid
     * Lead Message is received.
     * 
     * @param messageBody
     *            the soap message body containing the notification message
     * 
     */

    public void handleNotification(SOAPEnvelope envelope) {
        OMElement messageContent = envelope.getBody().getFirstElement();
        SOAPHeader soapHeader = envelope.getHeader();
        OMElement topicEl = soapHeader.getFirstChildWithName(new QName(null, "Topic"));
        XmlObject messageObj = null;

        if (topicEl != null) {
            if (topicEl.getChildElements().hasNext()) {
                OMElement widgetTopicOMEl = (OMElement) topicEl.getChildElements().next();
                String widgetTopicString = null;
                try {
                    widgetTopicString = widgetTopicOMEl.toStringWithConsume();
                } catch (XMLStreamException e) {
                    // TODO add with throws
                    e.printStackTrace();
                }
                String[] topicSubstrings = widgetTopicString.split(":");
                if (topicSubstrings.length > 1) {
                    topic = topicSubstrings[1];
                }
            }
        }

        if (topic != null) {
            try {
                try {
                    messageObj = XmlObject.Factory.parse(messageContent.toStringWithConsume());
                } catch (XMLStreamException e) {
                    // TODO add with throws
                    e.printStackTrace();
                }
                XmlCursor xc = messageObj.newCursor();
                xc.toNextToken();

                xc.dispose();
            } catch (XmlException e) {
                logger.error("error parsing message content: " + messageContent, e);
                e.printStackTrace();
            }
            NotificationType type = MessageUtil.getType(messageObj);
            this.callback.deliverMessage(topic, type, messageObj);

        } else {
            logger.info("Notification came without a Notification Topic:" + envelope);
        }
    }

}
