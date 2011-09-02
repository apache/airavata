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

package org.apache.airavata.xbaya;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.airavata.xbaya.component.ComponentException;
import org.apache.airavata.xbaya.file.XBayaPathConstants;
import org.apache.airavata.xbaya.graph.GraphException;
import org.apache.airavata.xbaya.lead.LeadContextHeaderHelper;
import org.apache.airavata.xbaya.util.XMLUtil;
import org.apache.airavata.xbaya.wf.Workflow;
import org.apache.airavata.xbaya.workflow.WorkflowEngineException;
import org.apache.airavata.xbaya.workflow.proxy.WorkflowProxyClient;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.xmlpull.v1.builder.XmlElement;
import org.xmlpull.v1.builder.XmlInfosetBuilder;

import xsul.lead.LeadContextHeader;
import xsul.wsif.WSIFMessage;
import xsul.wsif.WSIFOperation;
import xsul.wsif.WSIFPort;
import xsul.wsif.impl.WSIFMessageElement;
import xsul.xhandler_soap_sticky_header.StickySoapHeaderHandler;
import xsul.xwsif_runtime.WSIFClient;
import xsul.xwsif_runtime.WSIFRuntime;
import xsul.xwsif_runtime_async.WSIFAsyncResponsesCorrelator;
import edu.indiana.extreme.weps.DeploymentDocumentsType;
import edu.indiana.extreme.weps.DeploymentInformationDocument;
import edu.indiana.extreme.weps.XMLFile;

public class ProxyServiceTester {

    private String wsdlLoc = "http://localhost:8081/axis2/services/WEPSService?wsdl";

    private void runClient() {
        // logger.info("Invoking operation deploy using WSDL from " + wsdlLoc);
        try {
            WSIFAsyncResponsesCorrelator correlator;
            correlator = null;

            // pass some headers
            LeadContextHeaderHelper helper = new LeadContextHeaderHelper();
            helper.setXBayaConfiguration(new XBayaConfiguration());
            LeadContextHeader leadContext = helper.getLeadContextHeader();
            leadContext.setWorkflowId(URI.create("http://host/2005/11/09/workflowinstace"));
            leadContext.setNodeId("decoder1");
            leadContext.setTimeStep("5");
            leadContext.setServiceId("decoder-instance-10");

            WSIFClient wclient = WSIFRuntime.newClient(wsdlLoc)
                    .addHandler(new StickySoapHeaderHandler("use-lead-header", leadContext))
                    .useAsyncMessaging(correlator).setAsyncResponseTimeoutInMs(33000L); // to simplify testing set to
                                                                                        // just few
            // seconds

            WSIFPort port = wclient.getPort();
            WSIFOperation operation = port.createOperation("deploy");

            WSIFMessage outputMessage = operation.createOutputMessage();
            WSIFMessage faultMessage = operation.createFaultMessage();

            XmlElement inputMessageElement = xmlObjectToEl(getDeployPayload());

            WSIFMessage inputMessage = new WSIFMessageElement(inputMessageElement);

            System.out.println("Sending a message:\n" + XMLUtil.xmlElementToString((XmlElement) inputMessage));
            boolean success = operation.executeRequestResponseOperation(inputMessage, outputMessage, faultMessage);

            XmlElement result;
            if (success) {
                result = (XmlElement) outputMessage;
            } else {
                result = (XmlElement) faultMessage;
            }
            System.out.println("Received message:\n" + XMLUtil.xmlElementToString(result));
        } catch (IOException e) {
            e.printStackTrace();

        } catch (XmlException e) {
            e.printStackTrace();

        }
    }

    private XmlObject getDeployPayload() throws XmlException {
        DeploymentInformationDocument.DeploymentInformation deploymentInformation = DeploymentInformationDocument.DeploymentInformation.Factory
                .newInstance();
        DeploymentDocumentsType documentsType = DeploymentDocumentsType.Factory.newInstance();

        deploymentInformation.setProcessName("DummyProcess");

        // I do not have a bpel document around to test this. So sending my build.xml as the bpel document ;)
        documentsType.setBPEL(XmlObject.Factory.parse("<BPELFile>BPEL File will come here</BPELFile>"));
        documentsType.setDeploymentDescriptor(XmlObject.Factory.parse("<DD>BPEL File will come here</DD>"));

        XMLFile processWSDL = XMLFile.Factory.newInstance();
        processWSDL.setFileName("Process WSDL");
        processWSDL.setContent(XmlObject.Factory.parse("<ProcessWSDL>BPEL File will come here</ProcessWSDL>"));
        documentsType.setProcessWSDL(processWSDL);

        XMLFile serviceWSDL = XMLFile.Factory.newInstance();
        serviceWSDL.setFileName("Service WSDL");
        serviceWSDL.setContent(XmlObject.Factory.parse("<ServiceWSDL>BPEL File will come here</ServiceWSDL>"));
        documentsType.setServiceWSDLsArray(new XMLFile[] { serviceWSDL });

        deploymentInformation.setDeploymentDocuments(documentsType);

        return deploymentInformation;
    }

    private XmlElement xmlObjectToEl(XmlObject outgoingXmlObj) throws IOException {
        String outgoingXmlAsString = outgoingXmlObj.xmlText();
        return XmlInfosetBuilder.newInstance().parseFragmentFromReader(new StringReader(outgoingXmlAsString));
    }

    public void testXBayaProxyClientDeploy() {
        try {
            File file = new File(XBayaPathConstants.WORKFLOW_DIRECTORY, "complex-math.xwf");
            org.xmlpull.infoset.XmlElement workflowXML = XMLUtil.loadXML(file);

            // Parse the workflow
            Workflow workflow = new Workflow(workflowXML);
            workflow.setName("some-number-will-come-here");
            WorkflowProxyClient.createScript(workflow);

            WorkflowProxyClient workflowProxyClient = new WorkflowProxyClient();
            workflowProxyClient.setEngineURL(new URI(wsdlLoc));

            workflowProxyClient.deploy(workflow, false);

        } catch (IOException e) {
            e.printStackTrace();

        } catch (GraphException e) {
            e.printStackTrace();

        } catch (ComponentException e) {
            e.printStackTrace();

        } catch (WorkflowEngineException e) {
            e.printStackTrace();

        } catch (URISyntaxException e) {
            e.printStackTrace();

        }

    }

    public void testXBayaProxyClientCreateInstance() {
        try {

            Workflow workflow = new Workflow();
            workflow.setName("uuid:some-number-will-come-here");
            WorkflowProxyClient workflowProxyClient = new WorkflowProxyClient();
            workflowProxyClient.setEngineURL(new URI(wsdlLoc));

            workflowProxyClient.instantiate(workflow, new URI(wsdlLoc));

        } catch (GraphException e) {
            e.printStackTrace();
        } catch (WorkflowEngineException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (ComponentException e) {
            e.printStackTrace();

        }
    }

    public void testXRegistry() {

        // try {
        // String xregistryURL = "https://129.79.240.87:6666/xregistry?wsdl";
        //
        // MyProxyClient myProxyClient = this.engine.getMyProxyClient();
        // GSSCredential proxy = myProxyClient.getProxy();
        //
        // GlobalContext context = new GlobalContext(true);
        // context.setCredential(proxy);
        //
        // //For Generel Client
        // DocumentRegistryClient client = new DocumentRegistryClient(context, xregistryURL);
        //
        //
        // String resourceValue = "These are some information I want to keep in xregistry";
        // QName resourceQName = new QName("TestResourceFromEran");
        // client.registerResource(resourceQName, resourceValue);
        //
        // String resource = client.getResource(resourceQName);
        // System.out.println("resource = " + resource);
        //
        //
        // } catch (XregistryException e) {
        // e.printStackTrace();
        //
        // }

    }

    public static void main(String[] args) {
        new ProxyServiceTester();

        try {
            URI myUri = new URI("https://129.79.240.87:6666/xregistry?wsdl");
            System.out.println(myUri.getRawFragment());
        } catch (URISyntaxException e) {
            e.printStackTrace();

        }
    }
}
