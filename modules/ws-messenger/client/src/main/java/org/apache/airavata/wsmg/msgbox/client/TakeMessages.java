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

import java.util.Iterator;

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
 * This class can be used to invoke takeMessages operation of msgBoxService and returns the resultant operation status
 * 
 */
public class TakeMessages {
    private static final Logger logger = LoggerFactory.getLogger(TakeMessages.class);
    private final OMFactory factory = OMAbstractFactory.getOMFactory();
    protected EndpointReference msgBoxEndPointReference;
    protected long timeoutInMilliSeconds;
    private String msgBoxId;

    public TakeMessages(EndpointReference msgBoxEpr, long timeout) throws AxisFault {
        this.msgBoxEndPointReference = msgBoxEpr;
        this.timeoutInMilliSeconds = timeout;
        String address = msgBoxEpr.getAddress();
        int biginIndex = address.indexOf("clientid");
        if (biginIndex != -1) {
            msgBoxId = address.substring(biginIndex + "clientid".length() + 1);
        } else {
            throw new AxisFault("Invalid Message Box EPR cannot find message box ID");
        }
    }

    public long getTimeoutInMilliSeconds() {
        return timeoutInMilliSeconds;
    }

    public void setTimeoutInMilliSeconds(long timeout) {
        timeoutInMilliSeconds = timeout;
    }

    public Iterator<OMElement> execute() throws AxisFault {
        OMElement message = createMessageEl(this.msgBoxId);
        ServiceClient serviceClient = createServiceClient(message);

        OMElement responseMessage = null;
        try {
            responseMessage = serviceClient.sendReceive(message);

            if (responseMessage == null) {
                throw AxisFault.makeFault(new RuntimeException("no response recieved for subscription message"));
            }

            responseMessage.build(); // free the input stream.

        } finally {
            serviceClient.cleanup();
        }

        return (Iterator<OMElement>) responseMessage.getFirstElement().getChildren();
    }

    private OMElement createMessageEl(String msgboxid) throws AxisFault {
        OMElement message = factory.createOMElement("takeMessages", NameSpaceConstants.MSG_BOX);
        OMElement msgBoxId = factory.createOMElement("MsgBoxId", NameSpaceConstants.MSG_BOX);
        msgBoxId.setText(msgboxid);
        message.addChild(msgBoxId);
        message.declareNamespace(NameSpaceConstants.MSG_BOX);
        return message;
    }

    private ServiceClient createServiceClient(OMElement message) throws AxisFault {
        String uuid = UUIDGenerator.getUUID();
        Options opts = new Options();
        opts.setProperty(org.apache.axis2.transport.http.HTTPConstants.CHUNKED, Boolean.FALSE);
        opts.setTo(msgBoxEndPointReference);
        opts.setMessageId(uuid);
        opts.setAction(message.getNamespace().getNamespaceURI() + "/" + message.getLocalName());

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
