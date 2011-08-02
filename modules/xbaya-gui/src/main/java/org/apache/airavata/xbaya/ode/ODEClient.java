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

package org.apache.airavata.xbaya.ode;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;

import javax.xml.namespace.QName;

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
import org.apache.airavata.xbaya.lead.LeadContextHeaderHelper;
import org.apache.airavata.xbaya.monitor.MonitorConfiguration;
import org.apache.airavata.xbaya.security.SecurityUtil;
import org.apache.airavata.xbaya.security.XBayaSecurity;
import org.apache.airavata.xbaya.util.XMLUtil;
import org.apache.airavata.xbaya.wf.Workflow;
import org.apache.airavata.xbaya.xregistry.XRegistryAccesser;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.ogce.xregistry.client.XRegistryClient;
import org.ogce.xregistry.utils.XRegistryClientException;

import xregistry.generated.ResourceData;
import xsul.XmlConstants;
import xsul.invoker.gsi.GsiInvoker;
import xsul.lead.LeadContextHeader;
import xsul.lead.LeadResourceMapping;
import xsul.xhandler_soap_sticky_header.StickySoapHeaderHandler;
import xsul.xwsif_runtime.WSIFClient;
import xsul.xwsif_runtime.XmlBeansWSIFRuntime;
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
    public WsdlDefinitions getWorkflowWSDL(URI xRegistryURI, GSSCredential gssCredential, QName qname,
            String odeEprEndingWithPort) {
        try {
            XRegistryAccesser xregistryAccesser = new XRegistryAccesser(gssCredential, xRegistryURI);
            Workflow workflow = xregistryAccesser.getWorkflow(qname);
            // The REsulting WSDL is not affected by the DSCURL this is
            // necessary to create the other scripts
            // So send in some dummy URI for DSC
            URI dscUrl = XBayaConstants.DEFAULT_DSC_URL;
            return workflow.getOdeInvokableWSDL(dscUrl, odeEprEndingWithPort);
        } catch (Exception e) {
            throw new XBayaRuntimeException(e);
        }
    }

    public Object parseValue(WSComponentPort input, String valueString) {
        String name = input.getName();
        if (false) {
            // Some user wants to pass empty strings, so this check is disabled.
            if (valueString.length() == 0) {
                throw new XBayaRuntimeException("Input parameter, " + name + ", cannot be empty");
            }
        }
        QName type = input.getType();
        Object value;
        if (LEADTypes.isKnownType(type)) {
            // TODO check the type.
            value = valueString;
        } else {
            try {
                value = XMLUtil.stringToXmlElement3(valueString);
            } catch (RuntimeException e) {
                throw new XBayaRuntimeException("Input parameter, " + name + ", is not valid XML", e);
            }
        }
        return value;
    }

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
                    componentPort.setValue(parseValue(componentPort, (String) componentPort.getValue()));
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

    /**
     * 
     * @param workflow
     *            workflow object
     * @param topic
     *            notification topic
     * @param usedDN
     *            eg
     *            "/O=LEAD Project/OU=portal.leadproject.org/OU=cs.indiana.edu/CN=sshirasu/EMAIL=sshirasu@cs.indiana.edu"
     * @param instanceID
     *            workflowinstance id
     * @param resourceMapping
     *            resource mapping
     * @param scmsURL
     *            eg https://tyr12.cs.indiana.edu:60443/SCMS?wsdl
     * @return
     */
    public LeadContextHeader getDefaultLeadHeader(Workflow workflow, String topic, String usedDN, URI instanceID,
            LeadResourceMapping resourceMapping, URI scmsURL) {
        LeadContextHeaderHelper leadContextHelper = new LeadContextHeaderHelper();

        leadContextHelper.setXRegistryURL(XBayaConstants.DEFAULT_XREGISTRY_URL);
        leadContextHelper.setGFacURL(XBayaConstants.DEFAULT_GFAC_URL);

        leadContextHelper.setUser(usedDN);
        leadContextHelper.setWorkflowInstanceID(instanceID);
        leadContextHelper.setWorkflowTemplateID(workflow.getUniqueWorkflowName());

        MonitorConfiguration monitorConfiguration = new MonitorConfiguration(XBayaConstants.DEFAULT_BROKER_URL, topic,
                true, XBayaConstants.DEFAULT_MESSAGE_BOX_URL);
        leadContextHelper.setMonitorConfiguration(monitorConfiguration);

        LeadContextHeader leadContext = leadContextHelper.getLeadContextHeader();

        leadContext.setNodeId(workflow.getName());

        leadContext.setTimeStep("1");
        try {
            leadContext.setXRegistryUrl(new URI(XBayaConstants.DEFAULT_XREGISTRY_URL.toString() + "?wsdl"));
        } catch (URISyntaxException e) {
            throw new XBayaRuntimeException(e);
        }

        if (resourceMapping != null) {
            leadContext.setResourceMapping(resourceMapping);
        }
        if (scmsURL != null) {
            leadContext.setScmsUrl(scmsURL);
        }

        return leadContext;

    }

    // public static void main(String[] args) throws GSSException {
    // ODEClient client = new ODEClient();
    // GSSCredential credential = client.getGSSCredential("chathura",
    // "changeme", "montblanc.extreme.indiana.edu");
    // Workflow workflow = client.getWorkflow(
    // XBayaConstants.DEFAULT_XREGISTRY_URL, credential,
    // "NAM_Workflow101");
    // String instanceID = java.util.UUID.randomUUID().toString();
    // String userDN = credential.getName().toString();
    // new ODEClient().invoke(workflow, credential,
    // XBayaConstants.DEFAULT_DSC_URL, XBayaConstants.DEFAULT_ODE_URL,
    // new ODEClient().getDefaultLeadHeader(workflow, "topic", userDN,
    // URI.create(instanceID), null, null));
    // System.out.println("pppppppppppppppp");
    //
    // // new experiment
    //
    // String wsdlLoc = WSDLUtil
    // .appendWSDLQuary(XBayaConstants.DEFAULT_MYLEAD_AGENT_URL
    // .toString());
    // // OperationResponseDocument res = createExperiment(instanceID,
    // //
    // // "templateid",
    // //
    // // userDN,
    // // wsdlLoc, "Experiment name- preferably unique",
    // // "Experiment name- preferably unique");
    //
    // // System.out.println(res.getOperationResponse().getResourceIDArray(0));
    //
    // // "urn:uuid:f573e6df-6886-4198-8efd-7dde9277b73e"
    // }

    public static void main(String[] args) throws XRegistryClientException, URISyntaxException, GSSException {
        // ODEClient client = new ODEClient();
        // DocumentRegistryClient registryClient =
        // client.getXRegistryClient(client.getGSSCredential("chathura",
        // "changeme", "montblanc.extreme.indiana.edu"),
        // XBayaConstants.DEFAULT_XREGISTRY_URL);
        // // http://hunk.extreme.indiana.edu:8081/axis2/services/LdmStream?wsdl
        // // client.deployStreamSource(registryClient, "Ldm",
        // "http://hunk.extreme.indiana.edu:8081/axis2/services/LDMStream?wsdl");
        // // client.deployStreamSource(registryClient, "Mesonet",
        // "http://hunk.extreme.indiana.edu:8081/axis2/services/StreamSourceAPI?wsdl");
        // DocData[] streamResources =
        // client.getStreamResources(registryClient);
        // for (DocData docData : streamResources) {
        // String resource = registryClient.getResource(docData.name);
        // // registryClient.removeResource(docData.name);
        // System.out.println(resource);
        // }

        // XBayaSecurity.getTrustedCertificates();
        // WSIFProviderManager.getInstance().addProvider(
        // new xsul.wsif_xsul_soap_http.Provider());

        // GSSCredential proxy = new ODEClient().getGSSCredential("chathura", "changeme",
        // XBayaConstants.DEFAULT_MYPROXY_SERVER);
        // WsdlResolver instance = WsdlResolver.getInstance();
        // PuretlsInvoker secureInvoker = new GsiInvoker(proxy, XBayaSecurity
        // .getTrustedCertificates());
        //
        // instance.setSecureInvoker(secureInvoker );
        // xsul.wsdl.WsdlDefinitions definitions = instance.loadWsdl(new URI("https://tyr09.cs.indiana.edu:23443/?wsdl")
        // );

        String userName = "chathura";
        String password = "changeme";
        new ODEClient();
        SecurityUtil.getGSSCredential(userName, password, XBayaConstants.DEFAULT_MYPROXY_SERVER);

        // Workflow workflow1 = client.getWorkflowFromOGCE(new URI(
        // "https://ogceportal.iu.teragrid.org:19443/xregistry"),
        // credential, new QName("Public_NAM_Initialized_WRF_Forecastc55d6223-7f79-4c07-824c-804c6b12782d"));

        // try {
        // BufferedWriter out = new BufferedWriter(new
        // FileWriter("/nfs/mneme/home/users/cherath/projects/test/extremeWorkspace/xbaya-gui/workflows/t-new.xwf"));
        // out.write(workflow1.toXMLText());
        // out.close();
        // } catch (IOException e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }

        // List<WSComponentPort> inputNodes = client.getInputs(workflow1);
        // for (WSComponentPort port : inputNodes) {
        // if (port.getName().equals("CrossCuttingConfigurations")) {
        // // Object val = client.parseValue(port,
        //
        // port.setValue("<CrossCuttingConfigurations n2:leadType='LeadCrosscutParameters' xmlns:n2='http://www.extreme.indiana.edu/namespaces/2004/01/gFac'>"
        // +"<lcp:nx xmlns:lcp='http://lead.extreme.indiana.edu/namespaces/2006/lead-crosscut-parameters/'>203</lcp:nx>"
        // +"<lcp:ny xmlns:lcp='http://lead.extreme.indiana.edu/namespaces/2006/lead-crosscut-parameters/'>203</lcp:ny>"
        // +"<lcp:dx xmlns:lcp='http://lead.extreme.indiana.edu/namespaces/2006/lead-crosscut-parameters/'>5000</lcp:dx>"
        // +"<lcp:dy xmlns:lcp='http://lead.extreme.indiana.edu/namespaces/2006/lead-crosscut-parameters/'>5000</lcp:dy>"
        // +"<lcp:ctrlat xmlns:lcp='http://lead.extreme.indiana.edu/namespaces/2006/lead-crosscut-parameters/'>44.85</lcp:ctrlat>"
        // +"<lcp:ctrlon xmlns:lcp='http://lead.extreme.indiana.edu/namespaces/2006/lead-crosscut-parameters/'>-86.25</lcp:ctrlon>"
        // +"<lcp:fcst_time xmlns:lcp='http://lead.extreme.indiana.edu/namespaces/2006/lead-crosscut-parameters/'>36</lcp:fcst_time>"
        // +"<lcp:start_date xmlns:lcp='http://lead.extreme.indiana.edu/namespaces/2006/lead-crosscut-parameters/'>7/30/2010</lcp:start_date>"
        // +"<lcp:start_hour xmlns:lcp='http://lead.extreme.indiana.edu/namespaces/2006/lead-crosscut-parameters/'>9</lcp:start_hour>"
        // +"<lcp:westbc xmlns:lcp='http://lead.extreme.indiana.edu/namespaces/2006/lead-crosscut-parameters/'>-90.74158</lcp:westbc>"
        // +"<lcp:eastbc xmlns:lcp='http://lead.extreme.indiana.edu/namespaces/2006/lead-crosscut-parameters/'>-81.75842</lcp:eastbc>"
        // +"<lcp:northbc xmlns:lcp='http://lead.extreme.indiana.edu/namespaces/2006/lead-crosscut-parameters/'>47.95601</lcp:northbc>"
        // +"<lcp:southbc xmlns:lcp='http://lead.extreme.indiana.edu/namespaces/2006/lead-crosscut-parameters/'>41.56573</lcp:southbc>"
        // +"</CrossCuttingConfigurations>");
        // // port.setValue(val);
        //
        //
        //
        //
        // } else if (port.getName().equals("NAMInitialData")) {
        // // Object val = client.parseValue(port,
        // port.setValue("gsiftp://gridftp.bigred.iu.teragrid.org:2812//N/dc/projects/lead/ldm/pub/native/grid/NCEP/LEADNAM/2010073006/nam40grb2.2010073006f03");
        // // port.setValue(val);
        // } else if (port.getName().equals("NAMLateralBoundaryData")) {
        // // Object val = client.parseValue(port,
        // port.setValue("gsiftp://gridftp.bigred.iu.teragrid.org:2812//N/dc/projects/lead/ldm/pub/native/grid/NCEP/LEADNAM/2010073006/nam40grb2.2010073006f06 gsiftp://gridftp.bigred.iu.teragrid.org:2812//N/dc/projects/lead/ldm/pub/native/grid/NCEP/LEADNAM/2010073006/nam40grb2.2010073006f09 gsiftp://gridftp.bigred.iu.teragrid.org:2812//N/dc/projects/lead/ldm/pub/native/grid/NCEP/LEADNAM/2010073006/nam40grb2.2010073006f12 gsiftp://gridftp.bigred.iu.teragrid.org:2812//N/dc/projects/lead/ldm/pub/native/grid/NCEP/LEADNAM/2010073006/nam40grb2.2010073006f15 gsiftp://gridftp.bigred.iu.teragrid.org:2812//N/dc/projects/lead/ldm/pub/native/grid/NCEP/LEADNAM/2010073006/nam40grb2.2010073006f18 gsiftp://gridftp.bigred.iu.teragrid.org:2812//N/dc/projects/lead/ldm/pub/native/grid/NCEP/LEADNAM/2010073006/nam40grb2.2010073006f21 gsiftp://gridftp.bigred.iu.teragrid.org:2812//N/dc/projects/lead/ldm/pub/native/grid/NCEP/LEADNAM/2010073006/nam40grb2.2010073006f24 gsiftp://gridftp.bigred.iu.teragrid.org:2812//N/dc/projects/lead/ldm/pub/native/grid/NCEP/LEADNAM/2010073006/nam40grb2.2010073006f27 gsiftp://gridftp.bigred.iu.teragrid.org:2812//N/dc/projects/lead/ldm/pub/native/grid/NCEP/LEADNAM/2010073006/nam40grb2.2010073006f30 gsiftp://gridftp.bigred.iu.teragrid.org:2812//N/dc/projects/lead/ldm/pub/native/grid/NCEP/LEADNAM/2010073006/nam40grb2.2010073006f33 gsiftp://gridftp.bigred.iu.teragrid.org:2812//N/dc/projects/lead/ldm/pub/native/grid/NCEP/LEADNAM/2010073006/nam40grb2.2010073006f36 gsiftp://gridftp.bigred.iu.teragrid.org:2812//N/dc/projects/lead/ldm/pub/native/grid/NCEP/LEADNAM/2010073006/nam40grb2.2010073006f39");
        // // port.setValue(val);
        // }
        //
        // }
        //
        // String instanceID = java.util.UUID.randomUUID().toString();
        //
        // String userDN = credential.getName().toString();
        // client.invoke(
        // workflow1, inputNodes,
        // credential,
        // XBayaConstants.DEFAULT_DSC_URL,
        // XBayaConstants.DEFAULT_ODE_URL,
        // client.getDefaultLeadHeader(workflow1, "topic", userDN,
        // URI.create(instanceID), null, null));

    }

    public ResourceData[] getStreamResources(XRegistryClient client) throws XRegistryClientException {
        ResourceData[] result = client.findResource(STREAM_SOURCE_NS);
        return result;
    }

    public void deployStreamSource(XRegistryClient client, String name, String wsdlURL) throws XRegistryClientException {
        QName qname = new QName(STREAM_SOURCE_NS, name);
        if (isResourceExist(client, qname)) {
            throw new XRegistryClientException("Resource Already exist by qname:" + qname);
        }
        client.registerResource(qname, wsdlURL);
    }

    /**
     * @param client
     * @param qname
     * @return
     * @throws XregistryException
     */
    private boolean isResourceExist(XRegistryClient client, QName qname) throws XRegistryClientException {
        String resource = client.getResource(qname);
        if (resource != null && !"".equals(resource)) {
            return true;
        }
        return false;
    }

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