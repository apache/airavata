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
package org.apache.airavata.wsmg.client;

import org.apache.airavata.wsmg.client.msgbox.MessagePuller;
import org.apache.airavata.wsmg.client.msgbox.MsgboxHandler;
import org.apache.airavata.wsmg.commons.MsgBoxQNameConstants;
import org.apache.airavata.wsmg.commons.WsmgVersion;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

abstract class CommonMsgBrokerClient implements MessageBrokerClient {

    protected final static OMFactory factory = OMAbstractFactory.getOMFactory();
    private final static SOAPFactory soapfactory = OMAbstractFactory.getSOAP11Factory();

    private static final Log logger = LogFactory.getLog(CommonMsgBrokerClient.class);
    protected ConsumerServer xs;
    private long socketTimeout = 200000L;

    protected MsgboxHandler msgboxHandler = new MsgboxHandler();


    public CommonMsgBrokerClient(long timeout) {
        socketTimeout = timeout;
        WsmgVersion.requireVersionOrExit(WsmgVersion.getVersion());
    }

    public String[] getConsumerServiceEndpointReference() {
        if (xs == null) {
            throw new RuntimeException("Consumer server is not started yet");
        }
        return xs.getConsumerServiceEPRs();
    }

    public void setTimeOutInMilliSeconds(long timeout) {
        socketTimeout = timeout;
    }

    public CommonMsgBrokerClient() {
        WsmgVersion.requireVersionOrExit(WsmgVersion.getVersion());

    }

    public long getTimeOutInMilliSeconds() {
        return socketTimeout;
    }

    public String subscribeMsgBox(String brokerService, EndpointReference msgBoxEpr, String topic, String xpath)
            throws MsgBrokerClientException {

        String msgBoxId = null;
        String msgBoxUrl = msgBoxEpr.getAddress();
        int biginIndex = msgBoxUrl.indexOf("clientid");
        msgBoxId = msgBoxUrl.substring(biginIndex + "clientid".length() + 1);

        if (msgBoxId == null)
            throw new RuntimeException("Invalid Message Box EPR, message box ID is missing");

        return subscribe(msgBoxEpr.getAddress(), topic, xpath);
    }

    public String subscribeMsgBox(EndpointReference msgBoxEpr, String topicExpression, String xpathExpression,
            long expireTime) throws MsgBrokerClientException {

        String msgBoxEventSink = msgBoxEpr.getAddress();

        String formattedEventSink = null;

        if (msgBoxEpr.getAddress().contains("clientid")) {
            formattedEventSink = msgBoxEventSink;
        } else {
            if (msgBoxEpr.getAllReferenceParameters() == null)
                throw new MsgBrokerClientException("Invalid Message Box EPR, no reference parameters found");
            String msgBoxId = msgBoxEpr.getAllReferenceParameters().get(MsgBoxQNameConstants.MSG_BOXID_QNAME).getText();
            if (msgBoxId == null)
                throw new MsgBrokerClientException("Invalid Message Box EPR, reference parameter MsgBoxAddr is missing");
            String format = msgBoxEventSink.endsWith("/") ? "%sclientid/%s" : "%s/clientid/%s";

            formattedEventSink = String.format(format, msgBoxEventSink, msgBoxId);

        }

        return subscribe(new EndpointReference(formattedEventSink), topicExpression, xpathExpression, expireTime);

    }

    // ------------------------Message box user
    // API----------------------------//

    public EndpointReference createPullMsgBox(String msgBoxLocation, long timeout) throws MsgBrokerClientException {

        EndpointReference ret = null;
        try {
            ret = msgboxHandler.createPullMsgBox(msgBoxLocation, timeout);
        } catch (MsgBrokerClientException e) {
            throw e;
        }

        return ret;
    }

    public EndpointReference createPullMsgBox(String msgBoxServerLoc) throws MsgBrokerClientException {
        EndpointReference ret = null;
        ret = msgboxHandler.createPullMsgBox(msgBoxServerLoc);
        return ret;
    }


    public MessagePuller startPullingEventsFromMsgBox(EndpointReference msgBoxEpr, NotificationHandler handler,
            long interval, long timeout) throws MsgBrokerClientException {

        MessagePuller ret = null;
        ret = msgboxHandler.startPullingEventsFromMsgBox(msgBoxEpr, handler, interval, timeout);
        return ret;
    }

    public MessagePuller startPullingFromExistingMsgBox(EndpointReference msgBoxAddr, NotificationHandler handler,
            long interval, long timeout) throws MsgBrokerClientException {

        MessagePuller ret = null;
        ret = msgboxHandler.startPullingFromExistingMsgBox(msgBoxAddr, handler, interval, timeout);
        return ret;
    }

    public String deleteMsgBox(EndpointReference msgBoxEpr, long timeout) throws MsgBrokerClientException {
        String ret = null;
        ret = msgboxHandler.deleteMsgBox(msgBoxEpr, timeout);
        return ret;
    }

    public void stopPullingEventsFromMsgBox(MessagePuller msgPuller) {
        msgboxHandler.stopPullingEventsFromMsgBox(msgPuller);
    }
}
