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

package org.apache.airavata.xbaya.test.service.arrayadder;

import java.net.URI;

import org.apache.airavata.common.utils.XMLUtil;
import org.apache.airavata.xbaya.XBayaConfiguration;
import org.apache.airavata.xbaya.lead.LeadContextHeaderHelper;
import org.xmlpull.v1.builder.XmlElement;

import xsul.XmlConstants;
import xsul.lead.LeadContextHeader;
import xsul.wsif.WSIFMessage;
import xsul.wsif.WSIFOperation;
import xsul.wsif.WSIFPort;
import xsul.xhandler_soap_sticky_header.StickySoapHeaderHandler;
import xsul.xwsif_runtime.WSIFClient;
import xsul.xwsif_runtime.WSIFRuntime;
import xsul.xwsif_runtime_async.WSIFAsyncResponsesCorrelator;
import xsul5.MLogger;

public class ArrayAdderClient {

    private final static MLogger logger = MLogger.getLogger();

    private ArrayAdderService service;

    /**
     * 
     */
    public void run() {
        String wsdlLoc = startServer();
        runClient(wsdlLoc);
        shutdownServer();
    }

    private String startServer() {
        this.service = new ArrayAdderService();
        this.service.run();
        return this.service.getServiceWsdlLocation();
    }

    private void shutdownServer() {
        this.service.shutdownServer();
    }

    private void runClient(String wsdlLoc) {
        logger.info("Invoking operation add using WSDL from " + wsdlLoc);

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
        WSIFOperation operation = port.createOperation("add");
        WSIFMessage inputMessage = operation.createInputMessage();
        WSIFMessage outputMessage = operation.createOutputMessage();
        WSIFMessage faultMessage = operation.createFaultMessage();

        // Input
        XmlElement arrayElement = XmlConstants.BUILDER.newFragment("input");
        for (int i = 0; i < 5; i++) {
            XmlElement valueElement = arrayElement.addElement("value");
            valueElement.addChild("" + i);
        }

        inputMessage.setObjectPart("input", arrayElement);

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
        (new ArrayAdderClient()).run();
    }
}