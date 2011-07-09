/*
 * Copyright (c) 2007 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the license.
 *
 * $Id: ArrayGeneratorClient.java,v 1.4 2008/04/01 21:44:35 echintha Exp $
 */

package org.apache.airavata.xbaya.test.service.arraygen;

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

import java.net.URI;

import org.apache.airavata.xbaya.XBayaConfiguration;
import org.apache.airavata.xbaya.lead.LeadContextHeaderHelper;
import org.apache.airavata.xbaya.util.XMLUtil;
import org.xmlpull.v1.builder.XmlElement;

import xsul.lead.LeadContextHeader;
import xsul.wsif.WSIFMessage;
import xsul.wsif.WSIFOperation;
import xsul.wsif.WSIFPort;
import xsul.xhandler_soap_sticky_header.StickySoapHeaderHandler;
import xsul.xwsif_runtime.WSIFClient;
import xsul.xwsif_runtime.WSIFRuntime;
import xsul.xwsif_runtime_async.WSIFAsyncResponsesCorrelator;
import xsul5.MLogger;

/**
 */
public class ArrayGeneratorClient {

    private final static MLogger logger = MLogger.getLogger();

    private ArrayGeneratorService service;

    /**
     * 
     */
    public void run() {
        String wsdlLoc = startServer();
        runClient(wsdlLoc);
        shutdownServer();
    }

    private String startServer() {
        this.service = new ArrayGeneratorService();
        this.service.run();
        return this.service.getServiceWsdlLocation();
    }

    private void shutdownServer() {
        this.service.shutdownServer();
    }

    private void runClient(String wsdlLoc) {
        logger.info("Invoking operation echoString using WSDL from " + wsdlLoc);

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
                .addHandler(new StickySoapHeaderHandler("use-lead-header", leadContext)).useAsyncMessaging(correlator)
                .setAsyncResponseTimeoutInMs(33000L); // to simplify testing set to just few
        // seconds

        WSIFPort port = wclient.getPort();
        WSIFOperation operation = port.createOperation("generate");
        WSIFMessage inputMessage = operation.createInputMessage();
        WSIFMessage outputMessage = operation.createOutputMessage();
        WSIFMessage faultMessage = operation.createFaultMessage();

        inputMessage.setObjectPart("n", "3");

        logger.info("Sending a message:\n" + XMLUtil.xmlElementToString((XmlElement) inputMessage));
        boolean success = operation.executeRequestResponseOperation(inputMessage, outputMessage, faultMessage);

        XmlElement result;
        if (success) {
            result = (XmlElement) outputMessage;
        } else {
            result = (XmlElement) faultMessage;
        }
        logger.info("Received message:\n" + XMLUtil.xmlElementToString(result));
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        (new ArrayGeneratorClient()).run();
    }
}

/*
 * Indiana University Extreme! Lab Software License, Version 1.2
 * 
 * Copyright (c) 2007 The Trustees of Indiana University. All rights reserved.
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

