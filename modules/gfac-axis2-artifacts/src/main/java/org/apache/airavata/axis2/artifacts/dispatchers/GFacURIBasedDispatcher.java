package org.apache.airavata.axis2.artifacts.dispatchers;
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

import com.sun.corba.se.spi.activation.Server;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.HandlerDescription;
import org.apache.axis2.description.WSDL2Constants;
import org.apache.axis2.dispatchers.AbstractServiceDispatcher;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.Handler.InvocationResponse;
import org.apache.axis2.engine.Handler;
import org.apache.axis2.util.LoggingControl;
import org.apache.axis2.util.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;

public class GFacURIBasedDispatcher extends AbstractServiceDispatcher {

    public static final String NAME = "GFacURIBasedDispatcher";
    private static final Log log = LogFactory.getLog(GFacURIBasedDispatcher.class);

    /*
     *  (non-Javadoc)
     * @see org.apache.axis2.engine.AbstractDispatcher#findService(org.apache.axis2.context.MessageContext)
     */
    public AxisService findService(MessageContext messageContext) throws AxisFault {
        EndpointReference toEPR = messageContext.getTo();
        if (toEPR != null) {
            if (LoggingControl.debugLoggingAllowed && log.isDebugEnabled()) {
                log.debug(messageContext.getLogIDString() +
                       " Checking for Service using target endpoint address : " +
                        toEPR.getAddress());
            }
            String filePart = toEPR.getAddress();
            ConfigurationContext configurationContext = messageContext.getConfigurationContext();
            String[] values = Utils.parseRequestURLForServiceAndOperation(filePart,
                                  messageContext.getConfigurationContext().getServiceContextPath());
            AxisConfiguration registry =
                                configurationContext.getAxisConfiguration();

            if ((values.length >= 1) && (values[0] != null)) {
                AxisService service = registry.getService(values[0]);
                if (service == null) {
                    service = registry.getService("GFacService");
                    if(service != null){
                    if ("getWSDL".equals(values[1]) || "invoke".equals(values[1])) {
                        if (service != null) {
                            //todo get the wsdl from registry and add the endpoints to messagecontext
                            messageContext.setAxisService(service);
                            messageContext.setAxisOperation(
                                    service.getOperation(new QName("getWSDL")));
                        } else {
                            log.error("GFacService is not deployed, Please deploy the GFac service or double check the service name");
                        }
                        return service;
                    }}else{
                        log.error("GfacService is not deployed");
                    }
                }
            }
        }
        return null;
    }

     public void initDispatcher() {
        init(new HandlerDescription(NAME));
    }
}
