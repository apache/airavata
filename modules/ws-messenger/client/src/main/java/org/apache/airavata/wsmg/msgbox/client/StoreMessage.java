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

import org.apache.airavata.wsmg.commons.NameSpaceConstants;
import org.apache.airavata.wsmg.commons.WsmgCommonConstants;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.UUIDGenerator;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class can be used to invoke destroyMsgBox operation of msgBoxService and returns the resultant operation status
 * as EndpointReference
 */
public class StoreMessage {
    private static final Log logger = LogFactory.getLog(StoreMessage.class);

    protected EndpointReference msgBoxEndPointReference;
    protected long timeoutInMilliSeconds;

    public StoreMessage(EndpointReference msgBoxEpr, long timeout) throws AxisFault {
        this.msgBoxEndPointReference = msgBoxEpr;
        this.timeoutInMilliSeconds = timeout;
        String address = msgBoxEpr.getAddress();

        /*
         * Validate
         */
        int biginIndex = address.indexOf("clientid");
        if (biginIndex == -1) {
            throw new AxisFault("Invalid Message Box EPR cannot find message box ID");
        }
    }

    public long getTimeoutInMilliSeconds() {
        return timeoutInMilliSeconds;
    }

    public void setTimeoutInMilliSeconds(long timeout) {
        timeoutInMilliSeconds = timeout;
    }

    public String execute(OMElement messageIn) throws AxisFault {
        ServiceClient serviceClient = createServiceClient();

        OMElement responseMessage = null;

        try {
            responseMessage = serviceClient.sendReceive(messageIn);
        } finally {
            serviceClient.cleanup();
            serviceClient.cleanupTransport();
        }

        if (responseMessage == null) {
            throw AxisFault.makeFault(new RuntimeException("no response recieved for subscription message"));
        }
        return responseMessage.getFirstElement().getText();
    }

    private ServiceClient createServiceClient() throws AxisFault {
        String uuid = UUIDGenerator.getUUID();
        Options opts = new Options();
        opts.setProperty(org.apache.axis2.transport.http.HTTPConstants.CHUNKED, Boolean.FALSE);
        opts.setTo(msgBoxEndPointReference);
        opts.setMessageId(uuid);
        opts.setAction(NameSpaceConstants.MSG_BOX.getNamespaceURI() + "/" + "storeMessages");

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
