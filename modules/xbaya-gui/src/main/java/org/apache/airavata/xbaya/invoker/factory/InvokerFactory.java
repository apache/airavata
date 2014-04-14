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

package org.apache.airavata.xbaya.invoker.factory;

import javax.xml.namespace.QName;

import org.apache.airavata.common.workflow.execution.context.WorkflowContextHeaderBuilder;
import org.apache.airavata.common.utils.WSDLUtil;
import org.apache.airavata.workflow.model.exceptions.WorkflowException;
import org.apache.airavata.xbaya.invoker.AsynchronousInvoker;
import org.apache.airavata.xbaya.invoker.GFacInvoker;
import org.apache.airavata.xbaya.invoker.Invoker;
import org.apache.airavata.xbaya.invoker.SimpleInvoker;

import xsul.lead.LeadContextHeader;
import xsul.wsdl.WsdlDefinitions;

public class InvokerFactory {

    /**
     * @param portTypeQName
     * @param definitions
     * @param gfacURL
     * @param messageBoxURL
     * @return The invoker
     * @throws WorkflowException
     */
    public static Invoker createInvoker(QName portTypeQName, WsdlDefinitions definitions, String gfacURL,
            String messageBoxURL, LeadContextHeader leadContext) throws WorkflowException {
        Invoker invoker = null;

        if (definitions != null && definitions.getServices().iterator().hasNext()) {
            // check if this web service supports asynchronous invocation
            if (WSDLUtil.isAsynchronousSupported(WSDLUtil.wsdlDefinitions3ToWsdlDefintions5(definitions))) {
                invoker = new AsynchronousInvoker(definitions, messageBoxURL);
            } else {
                invoker = new SimpleInvoker(definitions);
            }
        } else if (gfacURL != null && gfacURL.length() != 0) {
            invoker = new GFacInvoker(portTypeQName, gfacURL, messageBoxURL, leadContext);
        }

        if (invoker == null) {
            String message = "Cannot find an appropriate way to invoke the service";
            throw new WorkflowException(message);
        }
        return invoker;
    }

    public static Invoker createInvoker(QName portTypeQName, WsdlDefinitions definitions, String gfacURL,
            String messageBoxURL, WorkflowContextHeaderBuilder builder, boolean differ) throws WorkflowException {
        Invoker invoker = null;

        if (definitions != null && definitions.getServices().iterator().hasNext()) {
            // check if this web service supports asynchronous invocation
            if (WSDLUtil.isAsynchronousSupported(WSDLUtil.wsdlDefinitions3ToWsdlDefintions5(definitions))) {
                invoker = new AsynchronousInvoker(definitions, messageBoxURL);
            } else {
                invoker = new SimpleInvoker(definitions);
            }
        } else if (gfacURL != null && gfacURL.length() != 0) {
            invoker = new GFacInvoker(portTypeQName, gfacURL, messageBoxURL, builder);
        }

        if (invoker == null) {
            String message = "Cannot find an appropriate way to invoke the service";
            throw new WorkflowException(message);
        }
        return invoker;
    }
}