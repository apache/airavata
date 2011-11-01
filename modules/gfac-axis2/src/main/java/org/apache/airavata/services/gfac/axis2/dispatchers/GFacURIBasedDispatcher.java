package org.apache.airavata.services.gfac.axis2.dispatchers;

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

import javax.xml.namespace.QName;

import org.apache.airavata.services.gfac.axis2.util.GFacServiceOperations;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.HandlerDescription;
import org.apache.axis2.dispatchers.AbstractServiceDispatcher;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.util.LoggingControl;
import org.apache.axis2.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GFacURIBasedDispatcher extends AbstractServiceDispatcher {

    public static final String NAME = "GFacURIBasedDispatcher";
    private static final Logger log = LoggerFactory.getLogger(GFacURIBasedDispatcher.class);

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.axis2.engine.AbstractDispatcher#findService(org.apache.axis2.context.MessageContext)
     */
    public AxisService findService(MessageContext messageContext) throws AxisFault {
        EndpointReference toEPR = messageContext.getTo();
        if (toEPR != null) {
            if (LoggingControl.debugLoggingAllowed && log.isDebugEnabled()) {
                log.debug(messageContext.getLogIDString() + " Checking for Service using target endpoint address : "
                        + toEPR.getAddress());
            }
            String filePart = toEPR.getAddress();
//            if(!filePart.endsWith("/invoke")){
//                filePart = filePart + "/invoke";
//            }
            ConfigurationContext configurationContext = messageContext.getConfigurationContext();
            String[] values = Utils.parseRequestURLForServiceAndOperation(filePart, messageContext
                    .getConfigurationContext().getServiceContextPath());
            AxisConfiguration registry = configurationContext.getAxisConfiguration();

            if ((values.length >= 1) && (values[0] != null)) {
                AxisService service = registry.getService(values[0]);
                if (service == null) {
                    service = registry.getService("GFacService");
                    if (service != null) {
                        messageContext.setAxisService(service);
                        if (GFacServiceOperations.INVOKE_SOAP_ACTION.toString().equals(messageContext.getSoapAction())) {
                            messageContext.setAxisOperation(service.getOperation(new QName(GFacServiceOperations.INVOKE
                                    .toString())));
                            messageContext.setTo(new EndpointReference(filePart + "/invoke"));
                        } else if (GFacServiceOperations.GETWSDL.toString().equals(values[1])) {
                            messageContext.setAxisOperation(service.getOperation(new QName(
                                    GFacServiceOperations.GETWSDL.toString())));
                        } else if (GFacServiceOperations.GETABSTRACTWSDL.toString().equals(values[1])) {
                            messageContext.setAxisOperation(service.getOperation(new QName(
                                    GFacServiceOperations.GETABSTRACTWSDL.toString())));
                        } else {
                            log.error("Wrong Service Name :" + values[0]);
                        }
                    } else {
                        log.error("GFacService is not deployed");
                    }
                }
                return service;
            }
        }
        return null;
    }

    public void initDispatcher() {
        init(new HandlerDescription(NAME));
    }
}
