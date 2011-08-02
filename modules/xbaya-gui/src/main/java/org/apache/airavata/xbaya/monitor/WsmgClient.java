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

import org.apache.airavata.xbaya.util.XMLUtil;
import org.xmlpull.infoset.XmlBuilderException;
import org.xmlpull.infoset.XmlElement;

import wsmg.NotificationHandler;
import wsmg.WseClientAPI;
import wsmg.XmlConsumer;
import wsmg.pull.MessagePuller;
import wsmg.util.WsmgUtil;
import xsul.ws_addressing.WsaEndpointReference;
import xsul5.MLogger;

public class WsmgClient implements NotificationHandler {

    private static final MLogger logger = MLogger.getLogger();

    private Monitor monitor;

    private URI brokerURL;

    private String topic;

    private boolean pullMode;

    private URI messageBoxURL;

    private WseClientAPI wseClient;

    private String subscriptionID;

    private MessagePuller messagePuller;

    private XmlConsumer xmlConsumer;

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

        this.wseClient = new WseClientAPI();
    }

    /**
     * Subscribes to the notification.
     * 
     * @throws MonitorException
     */
    public synchronized void subscribe() throws MonitorException {
        if (this.subscriptionID != null) {
            throw new IllegalStateException();
        }
        try {
            if (this.pullMode) {
                WsaEndpointReference messageBoxEPR = this.wseClient.createPullMsgBox(this.messageBoxURL.toString());
                URI address = messageBoxEPR.getAddress();
                this.subscriptionID = this.wseClient.subscribe(this.brokerURL.toString(), address.toString(),
                        this.topic);
                this.messagePuller = this.wseClient.startPullingEventsFromMsgBox(messageBoxEPR, this, 1000L);
            } else {
                this.xmlConsumer = new XmlConsumer(0, this);
                this.xmlConsumer.start();
                URL consumerUrl = new URL(this.xmlConsumer.getServer().getLocation());
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
                this.xmlConsumer.shutdown();
            }
            this.wseClient.unSubscribe(this.brokerURL.toString(), this.subscriptionID);
        } catch (RuntimeException e) {
            throw new MonitorException("Failed to unsubscribe.", e);
        }

    }

    /**
     * @see wsmg.NotificationHandler#handleNotification(java.lang.String)
     */
    public void handleNotification(String message) {
        try {
            String soapBody = WsmgUtil.getSoapBodyContent(message);
            XmlElement event = XMLUtil.stringToXmlElement(soapBody);
            this.monitor.handleNotification(event);
        } catch (XmlBuilderException e) {
            // Just log them because they can be unrelated messages sent to
            // this topic by accident.
            logger.warning("Could not parse received notification: " + message, e);
        } catch (RuntimeException e) {
            logger.warning("Failed to process notification: " + message, e);
        }
    }

}