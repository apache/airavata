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

package org.apache.airavata.wsmg.msgbox.client;

import javax.xml.namespace.QName;

import org.apache.airavata.wsmg.client.util.ClientUtil;
import org.apache.airavata.wsmg.commons.MsgBoxQNameConstants;
import org.apache.airavata.wsmg.commons.NameSpaceConstants;
import org.apache.airavata.wsmg.commons.WsmgCommonConstants;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.util.UUIDGenerator;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class can be used to invoke createMsgBox operation of msgBoxService and returns the resultant messageBoxId as
 * EndpointReference
 */
public class CreateMsgBox {
    private static final Logger logger = LoggerFactory.getLogger(CreateMsgBox.class);
    private final OMFactory factory = OMAbstractFactory.getOMFactory();
    protected String msgBoxEndPointReference;
    protected long timeoutInMilliSeconds;
    private String msgBoxId;
    private OMElement responseEl;

    public CreateMsgBox(String msgBoxLocation, long timeout) {
        this.msgBoxEndPointReference = msgBoxLocation;
        responseEl = OMAbstractFactory.getOMFactory().createOMElement(MsgBoxQNameConstants.MSG_BOXID_QNAME);

        timeoutInMilliSeconds = timeout;
    }

    public long getTimeoutInMilliSeconds() {
        return timeoutInMilliSeconds;
    }

    public void setTimeoutInMilliSeconds(long timeout) {
        timeoutInMilliSeconds = timeout;
    }

    public EndpointReference execute() throws AxisFault {
        ServiceClient serviceClient = createServiceClient();
        OMElement responseMessage = null;
        try {
            responseMessage = serviceClient.sendReceive(createMessageEl());
        } finally {
            serviceClient.cleanupTransport();
        }

        if (responseMessage == null) {
            throw AxisFault.makeFault(new RuntimeException("no response recieved for subscription message"));
        }
        String response = responseMessage.getFirstElement().getText();
        this.responseEl.setText(response);
        this.msgBoxEndPointReference = ClientUtil.formatMessageBoxUrl(this.msgBoxEndPointReference, response);
        return new EndpointReference(this.msgBoxEndPointReference);
    }

    private OMElement createMessageEl() throws AxisFault {
        OMElement message = factory.createOMElement("createMsgBox", NameSpaceConstants.MSG_BOX);
        OMElement msgBoxId = factory.createOMElement("MsgBoxId", NameSpaceConstants.MSG_BOX);
        msgBoxId.setText("Create message box");
        message.addChild(msgBoxId);
        message.declareNamespace(NameSpaceConstants.MSG_BOX);
        return message;
    }

    private ServiceClient createServiceClient() throws AxisFault {
        String uuid = UUIDGenerator.getUUID();
        Options opts = new Options();
        opts.setProperty(org.apache.axis2.transport.http.HTTPConstants.CHUNKED, Boolean.FALSE);
        opts.setTo(new EndpointReference(this.msgBoxEndPointReference));
        opts.setMessageId(uuid);
        opts.setAction(NameSpaceConstants.MSG_BOX.getNamespaceURI() + "/" + "createMsgBox");

        opts.setTimeOutInMilliSeconds(getTimeoutInMilliSeconds());
        ServiceClient client = new ServiceClient();
        try {
            client.engageModule(WsmgCommonConstants.AXIS_MODULE_NAME_ADDRESSING);
            if (logger.isDebugEnabled())
                logger.debug("Addressing module engaged");
        } catch (AxisFault e) {
            if (logger.isDebugEnabled())
                logger.debug("Addressing module not engaged :" + e);
        }
        client.setOptions(opts);
        return client;
    }

}
