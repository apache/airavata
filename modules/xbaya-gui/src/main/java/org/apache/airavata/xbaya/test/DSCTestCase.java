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
import java.net.URI;

import junit.framework.TestSuite;

import org.apache.airavata.common.utils.XMLUtil;
import org.apache.airavata.xbaya.file.XBayaPathConstants;
import org.apache.airavata.xbaya.gpel.DSCUtil;
import org.apache.airavata.xbaya.lead.LeadContextHeaderHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmlpull.v1.builder.XmlElement;

import xsul.lead.LeadContextHeader;
import xsul.util.XsulUtil;
import xsul.wsif.WSIFMessage;
import xsul.wsif.WSIFOperation;
import xsul.wsif.WSIFPort;
import xsul.xhandler_soap_sticky_header.StickySoapHeaderHandler;
import xsul.xwsif_runtime.WSIFClient;
import xsul.xwsif_runtime.WSIFRuntime;
import xsul.xwsif_runtime_async.WSIFAsyncResponsesCorrelator;
import xsul.xwsif_runtime_async_http.XsulSoapHttpWsaResponsesCorrelator;
import xsul5.wsdl.WsdlDefinitions;
import xsul5.wsdl.WsdlException;
import xsul5.wsdl.WsdlResolver;

public class DSCTestCase extends XBayaTestCase {

    private static final String SAMPLE_AWSDL = XBayaPathConstants.WSDL_DIRECTORY + "/test/TestCMD_Example1_AWSDL.xml";

    private static final Logger logger = LoggerFactory.getLogger(DSCTestCase.class);

    /**
     * @param args
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(new TestSuite(DSCTestCase.class));
    }

    /**
     * @see org.apache.airavata.xbaya.test.XBayaTestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    /**
     * @throws WsdlException
     */
    public void test() throws WsdlException {
        WsdlDefinitions definitions = WsdlResolver.getInstance().loadWsdl(new File(".").toURI(),
                new File(SAMPLE_AWSDL).toURI());
        URI dscURL = this.configuration.getDSCURL();
        logger.info("dscURL: " + dscURL);
        DSCUtil.convertToCWSDL(definitions, dscURL);

        logger.info(definitions.xmlStringPretty());

        // client
        int clientPort = 0;
        WSIFAsyncResponsesCorrelator correlator = new XsulSoapHttpWsaResponsesCorrelator(clientPort);
        String serverLoc = ((XsulSoapHttpWsaResponsesCorrelator) correlator).getServerLocation();
        logger.info("client is waiting at " + serverLoc);

        // LEAD Context Header
        // Create lead context.
        LeadContextHeaderHelper leadContextHelper = new LeadContextHeaderHelper();
        leadContextHelper.setXBayaConfiguration(this.configuration);
        LeadContextHeader leadContext = leadContextHelper.getLeadContextHeader();
        leadContext.setWorkflowId(URI.create("http://host/2005/11/09/workflowinstace"));
        leadContext.setNodeId("decoder1");
        leadContext.setTimeStep("5");
        leadContext.setServiceInstanceId(URI.create("decoder-instance-10"));

        XmlElement element3 = XMLUtil.xmlElement5ToXmlElement3(definitions.xml());
        xsul.wsdl.WsdlDefinitions definitions3 = new xsul.wsdl.WsdlDefinitions(element3);

        WSIFClient wclient = WSIFRuntime.getDefault().newClientFor(definitions3, "TestCMD_Example1SoapPort");
        StickySoapHeaderHandler handler = new StickySoapHeaderHandler("use-lead-header", leadContext);

        wclient.addHandler(handler);
        wclient.useAsyncMessaging(correlator);
        wclient.setAsyncResponseTimeoutInMs(33000L);

        WSIFPort port = wclient.getPort();
        WSIFOperation operation = port.createOperation("Run");
        WSIFMessage inputMessage = operation.createInputMessage();
        WSIFMessage outputMessage = operation.createOutputMessage();
        WSIFMessage faultMessage = operation.createFaultMessage();

        // inputMessage.setObjectPart("InputParam1", "Hello");
        inputMessage.setObjectPart("InputParam1", "100");

        logger.info("inputMessage: " + XsulUtil.safeXmlToString((XmlElement) inputMessage));
        boolean success = operation.executeRequestResponseOperation(inputMessage, outputMessage, faultMessage);

        XmlElement result;
        if (success) {
            result = (XmlElement) outputMessage;
        } else {
            result = (XmlElement) faultMessage;
        }
        logger.info("result:\n" + XsulUtil.safeXmlToString(result));

    }
}