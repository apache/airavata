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
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class can be used to invoke destroyMsgBox operation of msgBoxService and returns the resultant operation status
 * as EndpointReference
 */
public class StoreMessage {
    private static final Logger logger = LoggerFactory.getLogger(StoreMessage.class);

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
        ServiceClient _serviceClient = new ServiceClient();

        MultiThreadedHttpConnectionManager httpConnectionManager = new MultiThreadedHttpConnectionManager();
        httpConnectionManager.getParams().setMaxTotalConnections(10000);
        httpConnectionManager.getParams().setMaxConnectionsPerHost(HostConfiguration.ANY_HOST_CONFIGURATION, 100);
        httpConnectionManager.getParams().setDefaultMaxConnectionsPerHost(200);
        HttpClient httpClient = new HttpClient(httpConnectionManager);
        ConfigurationContext configurationContext = _serviceClient.getServiceContext().getConfigurationContext();

        configurationContext.setProperty(HTTPConstants.REUSE_HTTP_CLIENT, true);
        configurationContext.setProperty(HTTPConstants.CACHED_HTTP_CLIENT, httpClient);
        configurationContext.setProperty(HTTPConstants.AUTO_RELEASE_CONNECTION, true);
        _serviceClient.getOptions().setTo(new org.apache.axis2.addressing.EndpointReference(msgBoxEndPointReference.getAddress()));
        try {
            _serviceClient.engageModule(WsmgCommonConstants.AXIS_MODULE_NAME_ADDRESSING);
            if (logger.isDebugEnabled())
                logger.debug("Addressing module engaged");
        } catch (AxisFault e) {
            if (logger.isDebugEnabled())
                logger.debug("Addressing module not engaged :" + e);
        }

        _serviceClient.setOptions(opts);
        return _serviceClient;
    }
}
