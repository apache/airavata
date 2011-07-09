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

import java.rmi.RemoteException;

import javax.xml.namespace.QName;

import org.apache.airavata.wsmg.client.msgbox.MessagePuller;
import org.apache.airavata.wsmg.client.msgbox.MsgboxHandler;
import org.apache.airavata.wsmg.commons.WsmgCommonConstants;
import org.apache.airavata.wsmg.commons.WsmgNameSpaceConstants;
import org.apache.airavata.wsmg.commons.WsmgVersion;
import org.apache.airavata.wsmg.util.WsmgUtil;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.util.UUIDGenerator;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPHeaderBlock;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.log4j.Logger;

public abstract class CommonClientProcessing {

    protected final static OMFactory factory = OMAbstractFactory.getOMFactory();
    private final static SOAPFactory soapfactory = OMAbstractFactory.getSOAP11Factory();

    private final static org.apache.log4j.Logger logger = Logger.getLogger(CommonClientProcessing.class);
    protected ConsumerServer xs;
    protected MsgboxHandler msgboxHandler = new MsgboxHandler();

    private long socketTimeout = 2000L;

    public CommonClientProcessing(long timeout) {
        socketTimeout = timeout;
        WsmgVersion.requireVersionOrExit(WsmgVersion.getSpecVersion());
    }

    // ------------------------Consumer service user API
    // code------------------------//
    public String[] startConsumerService(int port, ConsumerNotificationHandler handler) throws AxisFault {
        xs = new ConsumerServer(port, handler);
        xs.start();
        logger.info("Consumer server started on port :" + port);
        return getConsumerServiceEndpointReference();
    }

    public String[] getConsumerServiceEndpointReference() {
        if (xs == null) {
            throw new RuntimeException("Consumer server is not started yet");
        }
        return xs.getConsumerServiceEPRs();
    }

    public void shutdownConsumerService() {
        if (xs != null) {
            System.out.println("Consumer Service shutdown requested");
            logger.info("Consumer server stopped");
            xs.stop();
        }
    }

    public void setTimeOutInMilliSeconds(long timeout) {
        socketTimeout = timeout;
    }

    public CommonClientProcessing() {
        WsmgVersion.requireVersionOrExit(WsmgVersion.getSpecVersion());

    }

    public long getTimeOutInMilliSeconds() {
        return socketTimeout;
    }

    // ------------------------Subscribe and Un-subscribe
    // API------------------------//

    public String subscribe(String brokerLocation, String eventSinkLocation, String topic) throws AxisFault {
        return subscribe(brokerLocation, eventSinkLocation, topic, null, null, null);
    }

    public String subscribe(String brokerLocation, String eventSinkLocation, String topicExpression,
            String xpathExpression) throws AxisFault {
        return subscribe(brokerLocation, eventSinkLocation, topicExpression, xpathExpression, null, null);
    }

    public abstract String subscribe(String brokerLocation, String eventSinkLocation, String topic,
            String xpathExpression, String eventSinkEndpointReferenceNS, String eventSinkEndpointReference)
            throws AxisFault;

    public String subscribe(String brokerLocation, String eventSinkLocation, String topic,
            String eventSinkEndpointReferenceNS, String eventSinkEndpointReference) throws AxisFault {
        return subscribe(brokerLocation, eventSinkLocation, topic, null, eventSinkEndpointReferenceNS,
                eventSinkEndpointReference);
    }

    // -----------------------------//

    public String subscribeMsgBox(String brokerService, EndpointReference msgBoxEpr, String topic, String xpath)
            throws AxisFault {

        String msgBoxId = null;
        String msgBoxUrl = msgBoxEpr.getAddress();
        int biginIndex = msgBoxUrl.indexOf("clientid");
        msgBoxId = msgBoxUrl.substring(biginIndex + "clientid".length() + 1);

        if (msgBoxId == null)
            throw new RuntimeException("Invalid Message Box EPR, message box ID is missing");

        return subscribe(brokerService, msgBoxEpr.getAddress(), topic, xpath, null, null);
    }

    public String subscribeMsgBox(String brokerService, EndpointReference msgBoxEpr, String topic,
            String xpathExpression, String eventSinkEndpointReferenceNS, String eventSinkEndpointReference)
            throws AxisFault {
        String msgBoxEventSink = msgBoxEpr.getAddress();
        if (msgBoxEpr.getAllReferenceParameters() == null)
            throw new RuntimeException("Invalid Message Box EPR, no reference parameters found");
        String msgBoxId = msgBoxEpr.getAllReferenceParameters()
                .get(new QName("http://www.extreme.indiana.edu/xgws/msgbox/2004/", "MsgBoxAddr")).getText();
        if (msgBoxId == null)
            throw new RuntimeException("Invalid Message Box EPR, reference parameter MsgBoxAddr is missing");
        String format = msgBoxEventSink.endsWith("/") ? "%sclient/%s" : "%s/clientid/%s";
        String formattedEventSink = String.format(format, msgBoxEventSink, msgBoxId);

        return subscribe(brokerService, formattedEventSink, topic, xpathExpression, eventSinkEndpointReferenceNS,
                eventSinkEndpointReference);
    };

    public int unSubscribe(String subscriptionManagerLocation, String subId, OMElement message, String replyTo)
            throws AxisFault {
        String subscriptionManagerLoc = WsmgUtil.formatURLString(subscriptionManagerLocation);
        EndpointReference subscriptionManagerEPR = null;
        subscriptionManagerEPR = new EndpointReference(subscriptionManagerLoc);
        OMElement identifierEl = factory.createOMElement("Identifier", message.getNamespace());
        identifierEl.setText(subId);
        ServiceClient client = new ServiceClient();
        if (client.getAxisConfiguration().getModule(WsmgCommonConstants.AXIS_MODULE_NAME_ADDRESSING) != null) {
            subscriptionManagerEPR.addReferenceParameter(identifierEl);
            configureServiceClient(client, message, subscriptionManagerEPR);
        } else
            configureServiceClient(client, message, subscriptionManagerEPR, identifierEl);
        client.sendReceive(message);
        client.cleanupTransport();
        return 0;
    }

    // ------------------------service client------------------------//

    private ServiceClient configureServiceClient(ServiceClient client, OMElement message,
            EndpointReference subscriptionManagerEPR) throws AxisFault {

        String uuid = UUIDGenerator.getUUID();

        if (client.getAxisConfiguration().getModule(WsmgCommonConstants.AXIS_MODULE_NAME_ADDRESSING) != null) {

            client.engageModule(WsmgCommonConstants.AXIS_MODULE_NAME_ADDRESSING);
        } else {
            SOAPHeaderBlock msgId = soapfactory.createSOAPHeaderBlock("MessageID", WsmgNameSpaceConstants.WSA_NS);
            msgId.setText(uuid);

            SOAPHeaderBlock to = soapfactory.createSOAPHeaderBlock("To", WsmgNameSpaceConstants.WSA_NS);
            to.setText(subscriptionManagerEPR.getAddress());

            SOAPHeaderBlock action = soapfactory.createSOAPHeaderBlock("Action", WsmgNameSpaceConstants.WSA_NS);
            action.setText(message.getNamespace().getNamespaceURI() + "/" + message.getLocalName());

            client.addHeader(action);
            client.addHeader(msgId);
            client.addHeader(to);
        }
        Options opts = new Options();
        opts.setProperty(org.apache.axis2.transport.http.HTTPConstants.CHUNKED, Boolean.FALSE);
        opts.setMessageId(uuid);
        opts.setTo(subscriptionManagerEPR);
        opts.setAction(message.getNamespace().getNamespaceURI() + "/" + message.getLocalName());
        opts.setTimeOutInMilliSeconds(getTimeOutInMilliSeconds());
        client.setOptions(opts);
        return client;
    }

    private ServiceClient configureServiceClient(ServiceClient client, OMElement message,
            EndpointReference subscriptionManagerEPR, OMElement identifierEl) throws AxisFault {
        String uuid = UUIDGenerator.getUUID();

        if (client.getAxisConfiguration().getModule(WsmgCommonConstants.AXIS_MODULE_NAME_ADDRESSING) != null) {

            client.engageModule(WsmgCommonConstants.AXIS_MODULE_NAME_ADDRESSING);
        } else {

            // OMElement msgId = factory.createOMElement("MessageID",
            // WsmgNameSpaceConstants.WSA_NS);
            // msgId.setText(uuid);
            //
            // OMElement to = factory.createOMElement("To",
            // WsmgNameSpaceConstants.WSA_NS);
            // to.setText(subscriptionManagerEPR.getAddress());
            //
            // OMElement action = factory.createOMElement("Action",
            // WsmgNameSpaceConstants.WSA_NS);
            // action.setText(message.getNamespace().getNamespaceURI() + "/"
            // + message.getLocalName());

            SOAPHeaderBlock msgId = soapfactory.createSOAPHeaderBlock("MessageID", WsmgNameSpaceConstants.WSA_NS);
            msgId.setText(uuid);

            SOAPHeaderBlock to = soapfactory.createSOAPHeaderBlock("To", WsmgNameSpaceConstants.WSA_NS);
            to.setText(subscriptionManagerEPR.getAddress());

            SOAPHeaderBlock action = soapfactory.createSOAPHeaderBlock("Action", WsmgNameSpaceConstants.WSA_NS);
            action.setText(message.getNamespace().getNamespaceURI() + "/" + message.getLocalName());

            client.addHeader(action);
            client.addHeader(msgId);
            client.addHeader(to);
            try {
                client.addHeader(org.apache.axiom.om.util.ElementHelper.toSOAPHeaderBlock(identifierEl, soapfactory));
            } catch (Exception e) {
                throw AxisFault.makeFault(e);
            }
        }
        Options opts = new Options();
        opts.setProperty(org.apache.axis2.transport.http.HTTPConstants.CHUNKED, Boolean.FALSE);
        opts.setMessageId(uuid);
        opts.setTo(subscriptionManagerEPR);
        opts.setAction(message.getNamespace().getNamespaceURI() + "/" + message.getLocalName());
        opts.setTimeOutInMilliSeconds(getTimeOutInMilliSeconds());
        client.setOptions(opts);
        return client;
    }

    // ------------------------Message box user
    // API----------------------------//

    public EndpointReference createPullMsgBox(String msgBoxLocation, long timeout) throws RemoteException {

        EndpointReference ret = null;
        try {
            ret = msgboxHandler.createPullMsgBox(msgBoxLocation, timeout);
        } catch (MsgBrokerClientException e) {
            throw AxisFault.makeFault(e);
        }

        return ret;
    }

    public EndpointReference createPullMsgBox(String msgBoxServerLoc) throws RemoteException {
        EndpointReference ret = null;
        try {
            ret = msgboxHandler.createPullMsgBox(msgBoxServerLoc);
        } catch (MsgBrokerClientException e) {
            throw AxisFault.makeFault(e);
        }

        return ret;
    }

    public MessagePuller startPullingEventsFromMsgBox(EndpointReference msgBoxEpr, NotificationHandler handler,
            long interval, long timeout) throws RemoteException {

        MessagePuller ret = null;

        try {
            ret = msgboxHandler.startPullingEventsFromMsgBox(msgBoxEpr, handler, interval, timeout);
        } catch (MsgBrokerClientException e) {
            throw AxisFault.makeFault(e);
        }
        return ret;
    }

    public MessagePuller startPullingFromExistingMsgBox(EndpointReference msgBoxAddr, NotificationHandler handler,
            long interval, long timeout) throws AxisFault {

        MessagePuller ret = null;

        try {
            ret = msgboxHandler.startPullingFromExistingMsgBox(msgBoxAddr, handler, interval, timeout);
        } catch (MsgBrokerClientException e) {
            throw AxisFault.makeFault(e);
        }

        return ret;
    }

    public String deleteMsgBox(EndpointReference msgBoxEpr, long timeout) throws RemoteException {
        String ret = null;
        try {
            ret = msgboxHandler.deleteMsgBox(msgBoxEpr, timeout);
        } catch (MsgBrokerClientException e) {
            throw AxisFault.makeFault(e);
        }
        return ret;
    }

    public void stopPullingEventsFromMsgBox(MessagePuller msgPuller) {
        msgboxHandler.stopPullingEventsFromMsgBox(msgPuller);
    }
}
