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
import java.util.LinkedList;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.airavata.common.utils.XMLUtil;
import org.apache.airavata.xbaya.XBayaConstants;
import org.apache.airavata.xbaya.XBayaException;
import org.apache.airavata.xbaya.XBayaRuntimeException;
import org.apache.airavata.xbaya.component.ComponentException;
import org.apache.airavata.xbaya.component.ws.WSComponentPort;
import org.apache.airavata.xbaya.gpel.script.BPELScript;
import org.apache.airavata.xbaya.gpel.script.BPELScriptType;
import org.apache.airavata.xbaya.graph.GraphException;
import org.apache.airavata.xbaya.graph.impl.NodeImpl;
import org.apache.airavata.xbaya.graph.system.InputNode;
import org.apache.airavata.xbaya.graph.system.OutputNode;
import org.apache.airavata.xbaya.lead.LEADTypes;
import org.apache.airavata.xbaya.lead.LEADWorkflowInvoker;
import org.apache.airavata.xbaya.security.XBayaSecurity;
import org.apache.airavata.xbaya.wf.Workflow;
import org.ietf.jgss.GSSCredential;

import xsul.XmlConstants;
import xsul.invoker.gsi.GsiInvoker;
import xsul.lead.LeadContextHeader;
import xsul5.wsdl.WsdlDefinitions;

public class ODEClient {

    public static final String STREAM_SOURCE_NS = "http://extreme.indiana.edu/streaming/source";

    public ODEClient() {

    }

    public List<InputNode> getInputNodes(Workflow workflow) {
        LinkedList<InputNode> ret = new LinkedList<InputNode>();
        List<NodeImpl> nodes = workflow.getGraph().getNodes();
        for (NodeImpl nodeImpl : nodes) {
            if (nodeImpl instanceof InputNode) {
                ret.add((InputNode) nodeImpl);
            }

        }
        return ret;
    }

    /**
     * Returns workflow inputs and can be used to get workflow input metadata
     * 
     * @param workflow
     * @return
     */
    public List<WSComponentPort> getInputs(Workflow workflow) {
        List<WSComponentPort> inputs;
        try {
            if (workflow.getWorkflowWSDL() == null) {
                BPELScript script = new BPELScript(workflow);
                script.create(BPELScriptType.BPEL2);
                workflow.setWorkflowWSDL(script.getWorkflowWSDL().getWsdlDefinitions());
                workflow.setGpelProcess(script.getGpelProcess());

            }
            inputs = workflow.getInputs();
            return inputs;
        } catch (GraphException e) {
            throw new XBayaRuntimeException(e);
        } catch (ComponentException e) {
            throw new XBayaRuntimeException(e);
        }

    }

    /**
     * Returns a WSDL that is ready to be used as the WSIF invokable WSDL to initiate the process
     *
     * @param xRegistryURI
     * @param gssCredential
     * @param qname
     *            Qname of the workflow that was used to store in XRegistry
     * @param dscUrl
     * @param odeEprEndingWithPort
     *            Something that look like https://kili.extreme.indiana.edu:17433
     * @return
     */
    // public WsdlDefinitions getWorkflowWSDL(URI xRegistryURI, GSSCredential gssCredential, QName qname,
    // String odeEprEndingWithPort) {
    // try {
    // XRegistryAccesser xregistryAccesser = new XRegistryAccesser(gssCredential, xRegistryURI);
    // Workflow workflow = xregistryAccesser.getWorkflow(qname);
    // // The REsulting WSDL is not affected by the DSCURL this is
    // // necessary to create the other scripts
    // // So send in some dummy URI for DSC
    // URI dscUrl = XBayaConstants.DEFAULT_DSC_URL;
    // return workflow.getOdeInvokableWSDL(dscUrl, odeEprEndingWithPort);
    // } catch (Exception e) {
    // throw new XBayaRuntimeException(e);
    // }
    // }

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
                        throw new XBayaRuntimeException("Workflow input cannot be null :" + componentPort.getName());
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
                    } catch (XBayaException e) {
                        ODEClient.this.throwException(e);
                    }
                }
            }.start();

        } catch (Exception e) {
            throw new XBayaRuntimeException(e);
        }

    }

    private void throwException(Exception e) {
        throw new XBayaRuntimeException(e);
    }

    // public ResourceData[] getStreamResources(XRegistryClient client) throws XRegistryClientException {
    // ResourceData[] result = client.findResource(STREAM_SOURCE_NS);
    // return result;
    // }
    //
    // public void deployStreamSource(XRegistryClient client, String name, String wsdlURL) throws
    // XRegistryClientException {
    // QName qname = new QName(STREAM_SOURCE_NS, name);
    // if (isResourceExist(client, qname)) {
    // throw new XRegistryClientException("Resource Already exist by qname:" + qname);
    // }
    // client.registerResource(qname, wsdlURL);
    // }

    /**
     * @param client
     * @param qname
     * @return
     * @throws XregistryException
     */
    // private boolean isResourceExist(XRegistryClient client, QName qname) throws XRegistryClientException {
    // String resource = client.getResource(qname);
    // if (resource != null && !"".equals(resource)) {
    // return true;
    // }
    // return false;
    // }

    /**
     * @param workflow
     * @return
     */
    public LinkedList<OutputNode> getoutNodes(Workflow workflow) {
        List<NodeImpl> nodes = workflow.getGraph().getNodes();
        LinkedList<OutputNode> ret = new LinkedList<OutputNode>();
        for (NodeImpl nodeImpl : nodes) {
            if (nodeImpl instanceof OutputNode) {
                ret.add((OutputNode) nodeImpl);
            }
        }
        return ret;

    }

}