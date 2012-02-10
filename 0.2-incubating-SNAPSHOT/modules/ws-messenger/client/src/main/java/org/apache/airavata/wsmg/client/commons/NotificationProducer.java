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

package org.apache.airavata.wsmg.client.commons;

import org.apache.airavata.wsmg.commons.WsmgCommonConstants;
import org.apache.airavata.wsmg.commons.NameSpaceConstants;
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

//import org.apache.airavata.wsmg.WsmgConstants;

//import org.apache.airavata.wsmg.WsmgConstants;

public class NotificationProducer {

    private final OMFactory factory = OMAbstractFactory.getOMFactory();
    private final SOAPFactory soapfactory = OMAbstractFactory.getSOAP11Factory();

    public NotificationProducer() {

    }

    public synchronized OMElement deliverMessage(OMElement notificationMessage, String type,
            EndpointReference brokerLocationEPR, long timeout) throws AxisFault {

        ServiceClient client = createServiceClient(type, notificationMessage, brokerLocationEPR, timeout, null);

        OMElement ret = client.sendReceive(notificationMessage);
        client.cleanupTransport();
        return ret;

    }

    public synchronized OMElement deliverMessage(OMElement notificationMessage, String type,
            EndpointReference brokerLocationEPR, long timeout, OMElement topicExpressionEl) throws AxisFault {

        ServiceClient client = createServiceClient(type, notificationMessage, brokerLocationEPR, timeout,
                topicExpressionEl);

        OMElement ret = client.sendReceive(notificationMessage);
        client.cleanupTransport();
        return ret;

    }

    private ServiceClient createServiceClient(String type, OMElement notificationMessage,
            EndpointReference brokerLocationEPR, long timeout, OMElement topicExpressionEl) throws AxisFault {

        ServiceClient client = new ServiceClient();

        if (client.getAxisConfiguration().getModule(WsmgCommonConstants.AXIS_MODULE_NAME_ADDRESSING) != null) {
            brokerLocationEPR.addReferenceParameter(topicExpressionEl);
            client.engageModule(WsmgCommonConstants.AXIS_MODULE_NAME_ADDRESSING);
        } else {
            SOAPHeaderBlock msgId = soapfactory.createSOAPHeaderBlock("MessageID", NameSpaceConstants.WSA_NS);
            msgId.setText(UUIDGenerator.getUUID());

            SOAPHeaderBlock to = soapfactory.createSOAPHeaderBlock("To", NameSpaceConstants.WSA_NS);
            to.setText(brokerLocationEPR.getAddress());

            SOAPHeaderBlock action = soapfactory.createSOAPHeaderBlock("Action", NameSpaceConstants.WSA_NS);
            action.setText("wsnt".equals(type) ? NameSpaceConstants.WSNT_NS.getNamespaceURI() + "/Notify"
                    : WsmgCommonConstants.WSMG_PUBLISH_SOAP_ACTION);
            if (topicExpressionEl != null) {
                try {
                    client.addHeader(org.apache.axiom.om.util.ElementHelper.toSOAPHeaderBlock(topicExpressionEl,
                            soapfactory));
                } catch (Exception e) {
                    throw AxisFault.makeFault(e);
                }
            }
            client.addHeader(action);
            client.addHeader(msgId);
            client.addHeader(to);

        }

        Options opts = new Options();

        opts.setAction("wsnt".equals(type) ? NameSpaceConstants.WSNT_NS.getNamespaceURI() + "/Notify"
                : WsmgCommonConstants.WSMG_PUBLISH_SOAP_ACTION);

        opts.setTo(brokerLocationEPR);
        opts.setTimeOutInMilliSeconds(timeout);

        client.setOptions(opts);

        return client;

    }

}
