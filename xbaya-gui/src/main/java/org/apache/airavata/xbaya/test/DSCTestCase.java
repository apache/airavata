/*
 * Copyright (c) 2006-2007 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the license.
 *
 * $Id: DSCTestCase.java,v 1.18 2008/04/01 21:44:24 echintha Exp $
 */
package org.apache.airavata.xbaya.test;

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

import java.io.File;
import java.net.URI;

import junit.framework.TestSuite;

import org.apache.airavata.xbaya.file.XBayaPathConstants;
import org.apache.airavata.xbaya.gpel.DSCUtil;
import org.apache.airavata.xbaya.lead.LeadContextHeaderHelper;
import org.apache.airavata.xbaya.util.XMLUtil;
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
import xsul5.MLogger;
import xsul5.wsdl.WsdlDefinitions;
import xsul5.wsdl.WsdlException;
import xsul5.wsdl.WsdlResolver;

/**
 * @author Satoshi Shirasuna
 */
public class DSCTestCase extends XBayaTestCase {

    private static final String SAMPLE_AWSDL = XBayaPathConstants.WSDL_DIRECTORY + "/test/TestCMD_Example1_AWSDL.xml";

    private static final MLogger logger = MLogger.getLogger();

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
        logger.finest("client is waiting at " + serverLoc);

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

/*
 * Indiana University Extreme! Lab Software License, Version 1.2
 * 
 * Copyright (c) 2006-2007 The Trustees of Indiana University. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 * 
 * 1) All redistributions of source code must retain the above copyright notice, the list of authors in the original
 * source code, this list of conditions and the disclaimer listed in this license;
 * 
 * 2) All redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 * disclaimer listed in this license in the documentation and/or other materials provided with the distribution;
 * 
 * 3) Any documentation included with all redistributions must include the following acknowledgement:
 * 
 * "This product includes software developed by the Indiana University Extreme! Lab. For further information please
 * visit http://www.extreme.indiana.edu/"
 * 
 * Alternatively, this acknowledgment may appear in the software itself, and wherever such third-party acknowledgments
 * normally appear.
 * 
 * 4) The name "Indiana University" or "Indiana University Extreme! Lab" shall not be used to endorse or promote
 * products derived from this software without prior written permission from Indiana University. For written permission,
 * please contact http://www.extreme.indiana.edu/.
 * 
 * 5) Products derived from this software may not use "Indiana University" name nor may "Indiana University" appear in
 * their name, without prior written permission of the Indiana University.
 * 
 * Indiana University provides no reassurances that the source code provided does not infringe the patent or any other
 * intellectual property rights of any other entity. Indiana University disclaims any liability to any recipient for
 * claims brought by any other entity based on infringement of intellectual property rights or otherwise.
 * 
 * LICENSEE UNDERSTANDS THAT SOFTWARE IS PROVIDED "AS IS" FOR WHICH NO WARRANTIES AS TO CAPABILITIES OR ACCURACY ARE
 * MADE. INDIANA UNIVERSITY GIVES NO WARRANTIES AND MAKES NO REPRESENTATION THAT SOFTWARE IS FREE OF INFRINGEMENT OF
 * THIRD PARTY PATENT, COPYRIGHT, OR OTHER PROPRIETARY RIGHTS. INDIANA UNIVERSITY MAKES NO WARRANTIES THAT SOFTWARE IS
 * FREE FROM "BUGS", "VIRUSES", "TROJAN HORSES", "TRAP DOORS", "WORMS", OR OTHER HARMFUL CODE. LICENSEE ASSUMES THE
 * ENTIRE RISK AS TO THE PERFORMANCE OF SOFTWARE AND/OR ASSOCIATED MATERIALS, AND TO THE PERFORMANCE AND VALIDITY OF
 * INFORMATION GENERATED USING SOFTWARE.
 */
