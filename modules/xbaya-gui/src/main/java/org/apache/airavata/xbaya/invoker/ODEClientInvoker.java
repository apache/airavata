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

package org.apache.airavata.xbaya.invoker;

import java.net.URI;
import java.util.List;

import org.apache.airavata.workflow.model.component.ws.WSComponentPort;
import org.apache.airavata.workflow.model.exceptions.WorkflowException;
import org.apache.airavata.workflow.model.exceptions.WorkflowRuntimeException;
import org.apache.airavata.workflow.model.wf.Workflow;
import org.apache.airavata.xbaya.lead.LEADWorkflowInvoker;
import org.apache.airavata.xbaya.security.XBayaSecurity;
import org.ietf.jgss.GSSCredential;

import xsul.XmlConstants;
import xsul.invoker.gsi.GsiInvoker;
import xsul.lead.LeadContextHeader;
import xsul5.wsdl.WsdlDefinitions;

public class ODEClientInvoker {

    public static final String STREAM_SOURCE_NS = "http://extreme.indiana.edu/streaming/source";

    /**
     * 
     * @param workflow
     * @param credentials
     * @param dscUrl
     * @param odeEprEndingWithPort
     *            https://pagodatree.cs.indiana.edu:17443
     * @param experimentID
     * @param leadContext
     * @param inputs
     */
    public void invoke(Workflow workflow, List<WSComponentPort> inputs, GSSCredential credentials, URI dscUrl,
            String odeEprEndingWithPort, LeadContextHeader leadContext) {
        try {
            WsdlDefinitions wsdl = workflow.getOdeInvokableWSDL(dscUrl, odeEprEndingWithPort);
            for (WSComponentPort componentPort : inputs) {
                if (null == componentPort.getValue()) {
                    if (null != componentPort.getDefaultValue()) {
                        componentPort.setValue(componentPort.getDefaultValue());
                    } else {
                        throw new WorkflowRuntimeException("Workflow input cannot be null :" + componentPort.getName());
                    }
                }
                // This is a check that we do to make sure if the user didnt bother
                // to parse the input to a type like a xmlElement or an array we would
                // do it ourselves
                if (componentPort.getValue() instanceof String) {
                    componentPort.setValue(ODEClientUtil.parseValue(componentPort, (String) componentPort.getValue()));
                }

            }

            GsiInvoker secureInvoker = null;
            secureInvoker = new GsiInvoker(credentials, XBayaSecurity.getTrustedCertificates());

            LEADWorkflowInvoker invoker = new LEADWorkflowInvoker(wsdl, leadContext, null, secureInvoker);
            invoker.setInputs(inputs);
            final LEADWorkflowInvoker finalInvoker = invoker;

            new Thread() {
                public synchronized void run() {
                    boolean success;
                    try {
                        success = finalInvoker.invoke();

                        if (success) {
                            XmlConstants.BUILDER.serializeToString(finalInvoker.getOutputMessage());
                        } else {
                            XmlConstants.BUILDER.serializeToString(finalInvoker.getFaultMessage());
                        }
                    } catch (WorkflowException e) {
                        ODEClientInvoker.this.throwException(e);
                    }
                }
            }.start();

        } catch (Exception e) {
            throw new WorkflowRuntimeException(e);
        }

    }

    private void throwException(Exception e) {
        throw new WorkflowRuntimeException(e);
    }

 }