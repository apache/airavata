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

package org.apache.airavata.xbaya.monitor;

import java.io.IOException;
import java.net.URI;
import java.net.URL;

import org.apache.airavata.commons.WorkFlowUtils;
import org.apache.airavata.wsmg.client.ConsumerNotificationHandler;
import org.apache.airavata.wsmg.client.MsgBrokerClientException;
import org.apache.airavata.wsmg.client.NotificationHandler;
import org.apache.airavata.wsmg.client.WseMsgBrokerClient;
import org.apache.airavata.wsmg.client.msgbox.MessagePuller;
import org.apache.airavata.xbaya.util.XMLUtil;
import org.apache.axiom.soap.SOAPEnvelope;
import org.xmlpull.infoset.XmlElement;

import xsul5.MLogger;

import javax.xml.stream.XMLStreamException;

public class WsmgClient implements ConsumerNotificationHandler, NotificationHandler{

    private static final MLogger logger = MLogger.getLogger();

    private Monitor monitor;

    private URI brokerURL;

    private String topic;

    private boolean pullMode;

    private URI messageBoxURL;

    private WseMsgBrokerClient wseClient;

    private String subscriptionID;

    private MessagePuller messagePuller;


    /**
     * Constructs a WsmgClient.
     * 
     * @param monitor
     */
    public WsmgClient(Monitor monitor) {
        this.monitor = monitor;

        MonitorConfiguration configuration = monitor.getConfiguration();
        // We need to copy these because the configuration might change.
        this.brokerURL = configuration.getBrokerURL();
        this.topic = configuration.getTopic();
        this.pullMode = configuration.isPullMode();
        this.messageBoxURL = configuration.getMessageBoxURL();

        this.wseClient = new WseMsgBrokerClient();
        this.wseClient.init(this.brokerURL.toString());
    }

    /**
     * Subscribes to the notification.
     * 
     * @throws MonitorException
     */
    public synchronized void subscribe() throws MonitorException {
        try {
            if (this.pullMode) {
                org.apache.axis2.addressing.EndpointReference messageBoxEPR = this.wseClient.createPullMsgBox(this.messageBoxURL.toString());
                this.subscriptionID = this.wseClient.subscribe(this.brokerURL.toString(), messageBoxEPR.toString(),
                        this.topic);
                this.messagePuller = this.wseClient.startPullingEventsFromMsgBox(messageBoxEPR, this, 1000L,20000L);
            } else {
                String[] endpoints = this.wseClient.startConsumerService(2222,this);
                URL consumerUrl = new URL(endpoints[0]);
                this.subscriptionID = this.wseClient.subscribe(this.brokerURL.toString(), consumerUrl.getHost() + ":"
                        + consumerUrl.getPort(), this.topic);
            }
        } catch (IOException e) {
            throw new MonitorException("Failed to subscribe.", e);
        } catch (RuntimeException e) {
            throw new MonitorException("Failed to subscribe.", e);
        }
    }

    /**
     * Unsubscribes from the notification.
     * 
     * @throws MonitorException
     */
    public synchronized void unsubscribe() throws MonitorException {
        // This method needs to be synchronized along with subscribe() because
        // unsubscribe() might be called while subscribe() is being executed.
        if (this.subscriptionID == null) {
            throw new IllegalStateException();
        }
        try {
            if (this.pullMode) {
                this.messagePuller.stopPulling();
            } else {
                this.wseClient.shutdownConsumerService();
            }
            this.wseClient.unSubscribe(this.subscriptionID);
        } catch (MsgBrokerClientException e) {
            throw new MonitorException("Failed to unsubscribe.", e);
        }

    }

    /**
     * @see org.apache.airavata.wsmg.client.NotificationHandler#handleNotification(java.lang.String)
     */
    public void handleNotification(SOAPEnvelope message) {
        String soapBody = message.getBody().toString();
        this.handleNotification(soapBody);
    }

    /**
     *
     * @param message
     */
    public void handleNotification(String message) {
        try {
            String soapBody = WorkFlowUtils.getSoapBodyContent(message);
            XmlElement event = XMLUtil.stringToXmlElement(soapBody);
            this.monitor.handleNotification(event);
        } catch (XMLStreamException e) {
            // Just log them because they can be unrelated messages sent to
            // this topic by accident.
            logger.warning("Could not parse received notification: " + message, e);
        } catch (RuntimeException e) {
            logger.warning("Failed to process notification: " + message, e);
        }
    }

}