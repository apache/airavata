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

package org.apache.airavata.xbaya.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.security.GeneralSecurityException;
import java.util.List;

import junit.framework.TestSuite;

import org.apache.airavata.xbaya.XBayaException;
import org.apache.airavata.xbaya.component.ComponentException;
import org.apache.airavata.xbaya.component.ws.WSComponentPort;
import org.apache.airavata.xbaya.file.XBayaPathConstants;
import org.apache.airavata.xbaya.gpel.DSCUtil;
import org.apache.airavata.xbaya.lead.LEADWorkflowInvoker;
import org.apache.airavata.xbaya.lead.LeadContextHeaderHelper;
import org.apache.airavata.xbaya.test.service.adder.Adder;
import org.apache.airavata.xbaya.test.service.adder.AdderService;
import org.apache.airavata.xbaya.util.WSDLUtil;
import org.apache.airavata.xbaya.util.XMLUtil;
import org.globus.gsi.CertUtil;
import org.ietf.jgss.GSSCredential;
import org.xmlpull.v1.builder.XmlContainer;
import org.xmlpull.v1.builder.XmlDocument;
import org.xmlpull.v1.builder.XmlElement;

import xsul.invoker.gsi.GsiInvoker;
import xsul.invoker.puretls.PuretlsInvoker;
import xsul.lead.LeadContextHeader;
import xsul.wsif.WSIFMessage;
import xsul.wsif.WSIFOperation;
import xsul.wsif.WSIFPort;
import xsul.wsif.WSIFService;
import xsul.wsif.WSIFServiceFactory;
import xsul.xwsif_runtime.WSIFClient;
import xsul.xwsif_runtime.WSIFRuntime;
import xsul5.MLogger;
import xsul5.wsdl.WsdlDefinitions;
import xsul5.wsdl.WsdlException;
import xsul5.wsdl.WsdlResolver;

public class LEADWorkflowInvokerTestCase extends XBayaTestCase {

    private static final String SAMPLE_AWSDL = XBayaPathConstants.WSDL_DIRECTORY + File.separator + Adder.WSDL_PATH;

    private static final MLogger logger = MLogger.getLogger();

    /**
     * @param args
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(new TestSuite(LEADWorkflowInvokerTestCase.class));
    }

    /**
     * 
     */
    public void estWSIFINvoker() {
        WsdlDefinitions definitions = WsdlResolver.getInstance().loadWsdl(new File(SAMPLE_AWSDL).toURI());

        WSIFServiceFactory factory = WSIFServiceFactory.newInstance();
        WSIFService service = factory.getService(WSDLUtil.wsdlDefinitions5ToWsdlDefintions3(definitions));

        WSIFClient client = WSIFRuntime.getDefault().newClientFor(service, null);

        // null selects the first port in the first service.

        WSIFPort port = client.getPort();
        WSIFOperation operation = port.createOperation("add");
        WSIFMessage inputMessage = operation.createInputMessage();
        WSIFMessage outputMessage = operation.createOutputMessage();
        WSIFMessage faultMessage = operation.createFaultMessage();

        inputMessage.setObjectPart("x", "2");
        inputMessage.setObjectPart("y", "3");

        // not needed in XSUL 2.9+
        // XmlElementAdapter inputWrapper = (XmlElementAdapter) inputMessage;
        // XmlDocument doc =
        // Soap11Util.getInstance().wrapBodyContent(inputWrapper.getTarget());
        // inputWrapper.setParent(inputWrapper.getTarget().getParent()); // make
        // sure wrapper has right parent

        {
            XmlElement xmlEl = (XmlElement) inputMessage;
            XmlContainer top = xmlEl.getRoot();
            String xmlAsStr = XMLUtil.BUILDER3.serializeToString(top);
            System.out.println(xmlAsStr);
            // Object top2 = inputWrapper.getRoot();
            // System.out.println(XMLUtil.BUILDER3.serializeToString(top2));
        }

        { // required for my addder ...
            XmlDocument doc = (XmlDocument) (((XmlElement) inputMessage).getRoot());
            XmlElement env = doc.getDocumentElement();
            XmlElement header = env.element(env.getNamespace(), "Header");
            if (header == null) {
                header = env.newElement(env.getNamespace(), "Header");
                env.addElement(0, header);
            }
            header.addElement(LeadContextHeader.NS, LeadContextHeader.TYPE.getLocalPart());
        }

        boolean success = operation.executeRequestResponseOperation(inputMessage, outputMessage, faultMessage);

        {
            XmlElement xmlEl = (XmlElement) inputMessage;
            XmlContainer top = xmlEl.getRoot();
            String xmlAsStr = XMLUtil.BUILDER3.serializeToString(top);
            System.out.println(xmlAsStr);
            // Object top2 = inputWrapper.getRoot();
            // System.out.println(XMLUtil.BUILDER3.serializeToString(top2));
        }

        if (success) {
            logger.info("outputMessage: " + XMLUtil.xmlElementToString((XmlElement) outputMessage));
        } else {
            logger.info("faultMessage: " + XMLUtil.xmlElementToString((XmlElement) faultMessage));
        }
    }

    /**
     * @throws WsdlException
     * @throws XBayaException
     */
    public void test() throws WsdlException, XBayaException {
        // WsdlDefinitions definitions = WsdlResolver.getInstance().loadWsdl(
        // new File(SAMPLE_AWSDL).toURI());

        AdderService service = new AdderService();
        service.run();
        WsdlDefinitions definitions = WSDLUtil.wsdlDefinitions3ToWsdlDefintions5(service.getWsdl());

        if (WSDLUtil.isAWSDL(definitions)) {
            DSCUtil.convertToCWSDL(definitions, this.configuration.getDSCURL());
        }

        logger.info(definitions.xmlStringPretty());

        // Create lead context.
        LeadContextHeaderHelper leadContextHelper = new LeadContextHeaderHelper();
        leadContextHelper.setXBayaConfiguration(this.configuration);
        LeadContextHeader leadContext = leadContextHelper.getLeadContextHeader();

        URI messageBoxURL = null;
        if (this.configuration.isPullMode()) {
            messageBoxURL = this.configuration.getMessageBoxURL();
        }

        LEADWorkflowInvoker invoker = new LEADWorkflowInvoker(definitions, leadContext, messageBoxURL);

        List<WSComponentPort> inputs = invoker.getInputs();

        for (WSComponentPort input : inputs) {
            String name = input.getName();
            logger.info("name: " + name);
            input.setValue("200");

            org.xmlpull.infoset.XmlElement appinfo = input.getAppinfo();
            logger.info("appinfo: " + XMLUtil.xmlElementToString(appinfo));
        }

        invoker.setInputs(inputs);

        boolean success = invoker.invoke();
        logger.info("success: " + success);

        if (success) {
            List<WSComponentPort> outputs = invoker.getOutputs();
            for (WSComponentPort output : outputs) {
                String name = output.getName();
                logger.info("name: " + name);
                Object value = output.getValue();
                logger.info("value: " + value);
            }
        } else {
            WSIFMessage fault = invoker.getFault();
            logger.info("fault: " + fault);
        }

        service.shutdownServer();
    }

    /**
     * This is a sample. It doesn't work now.
     * 
     * @throws FileNotFoundException
     * @throws ComponentException
     * @throws GeneralSecurityException
     * @throws IOException
     */
    public void estWithUserCredential() throws ComponentException, IOException, GeneralSecurityException {
        boolean userCred = true;
        GSSCredential proxy = null;
        WsdlDefinitions wsdl = null; // Set WSDL of the workflow.
        LeadContextHeader leadContext = null; // Set the LEAD Context.
        URI messageBoxURL = null; // This one can be null;

        String trustedcerts = System.getProperty("trustedcerts");
        String certskey = System.getProperty("certskey");

        PuretlsInvoker secureInvoker;
        if (userCred) {
            // Using user credential
            secureInvoker = new GsiInvoker(proxy, CertUtil.loadCertificates(trustedcerts));
        } else {
            // Using service certificate.
            secureInvoker = new PuretlsInvoker(certskey, "", trustedcerts);
        }
        LEADWorkflowInvoker workflowInvoker = new LEADWorkflowInvoker(wsdl, leadContext, messageBoxURL, secureInvoker);
        assertNotNull(workflowInvoker);
    }
}