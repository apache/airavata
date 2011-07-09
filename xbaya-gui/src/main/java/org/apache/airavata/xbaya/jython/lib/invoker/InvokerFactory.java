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

package org.apache.airavata.xbaya.jython.lib.invoker;

import javax.xml.namespace.QName;

import org.apache.airavata.xbaya.XBayaException;
import org.apache.airavata.xbaya.util.WSDLUtil;

import xsul.lead.LeadContextHeader;
import xsul.wsdl.WsdlDefinitions;

public class InvokerFactory {

    // private final static MLogger logger = MLogger.getLogger();

    /**
     * @param portTypeQName
     * @param definitions
     * @param gfacURL
     * @param messageBoxURL
     * @return The invoker
     * @throws XBayaException
     */
    public static Invoker createInvoker(QName portTypeQName, WsdlDefinitions definitions, String gfacURL,
            String messageBoxURL, LeadContextHeader leadContext) throws XBayaException {
        Invoker invoker = null;

        if (definitions != null && definitions.getServices().iterator().hasNext()) {
            // The WSDL has a service information. Assume that the service is
            // running.

            // check if this web service supports asynchronous invocation
            if (WSDLUtil.isAsynchronousSupported(WSDLUtil.wsdlDefinitions3ToWsdlDefintions5(definitions))) {
                invoker = new AsynchronousInvoker(definitions, messageBoxURL);
            } else {
                invoker = new SimpleInvoker(definitions);
            }
        } else if (gfacURL != null && gfacURL.length() != 0) {
            // Use the gfacURL set by user first because currently AWSDLs
            // created by GFac has wrong GFac URL.
            invoker = new GFacInvoker(portTypeQName, gfacURL, messageBoxURL, leadContext);
            // } else if (definitions != null) {
            // // TODO Remove this case because AWSDL created by GFac won't
            // include
            // // the URL of GFac anymore.
            // XmlElement factoryServicesElement = definitions.element(null,
            // "factoryServices");
            // if (factoryServicesElement != null) {
            // invoker = new GFacInvoker(definitions, messageBoxURL);
            // }
        }

        if (invoker == null) {
            String message = "Cannot find an appropriate way to invoke the service";
            throw new XBayaException(message);
        }
        return invoker;
    }
}