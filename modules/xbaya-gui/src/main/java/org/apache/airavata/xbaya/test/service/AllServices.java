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

package org.apache.airavata.xbaya.test.service;

import java.io.File;
import java.io.IOException;

import org.apache.airavata.common.utils.XMLUtil;
import org.apache.airavata.xbaya.file.XBayaPathConstants;
import org.apache.airavata.xbaya.test.service.adder.Adder;
import org.apache.airavata.xbaya.test.service.adder.AdderImpl;
import org.apache.airavata.xbaya.test.service.approver.Approver;
import org.apache.airavata.xbaya.test.service.approver.ApproverImpl;
import org.apache.airavata.xbaya.test.service.arrayadder.ArrayAdder;
import org.apache.airavata.xbaya.test.service.arrayadder.ArrayAdderImpl;
import org.apache.airavata.xbaya.test.service.arraygen.ArrayGenerator;
import org.apache.airavata.xbaya.test.service.arraygen.ArrayGeneratorImpl;
import org.apache.airavata.xbaya.test.service.echo.Echo;
import org.apache.airavata.xbaya.test.service.echo.EchoImpl;
import org.apache.airavata.xbaya.test.service.multiplier.Multiplier;
import org.apache.airavata.xbaya.test.service.multiplier.MultiplierImpl;

import xsul.lead.LeadContextHeader;
import xsul.wsdl.WsdlDefinitions;
import xsul.xhandler_soap_sticky_header.StickySoapHeaderHandler;
import xsul.xservo.XService;
import xsul.xservo_soap.XSoapDocLiteralService;
import xsul.xservo_soap_http.HttpBasedServices;

public class AllServices {

    // private static final Logger logger = LoggerFactory.getLogger();

    private int port;

    private HttpBasedServices httpServices;

    /**
     * Constructs an AllService.
     * 
     * @param port
     */
    public AllServices(int port) {
        this.port = port;
    }

    /**
     * @throws IOException
     */
    public void startAll() throws IOException {
        this.httpServices = new HttpBasedServices(this.port);

        start(Adder.SERVICE_NAME, Adder.WSDL_NAME, Adder.WSDL_PATH, new AdderImpl());
        start(Multiplier.SERVICE_NAME, Multiplier.WSDL_NAME, Multiplier.WSDL_PATH, new MultiplierImpl());
        start(ArrayGenerator.SERVICE_NAME, ArrayGenerator.WSDL_NAME, ArrayGenerator.WSDL_PATH, new ArrayGeneratorImpl());
        start(ArrayAdder.SERVICE_NAME, ArrayAdder.WSDL_NAME, ArrayAdder.WSDL_PATH, new ArrayAdderImpl());
        start(Echo.SERVICE_NAME, Echo.WSDL_NAME, Echo.WSDL_PATH, new EchoImpl());

        start(Approver.SERVICE_NAME, Approver.WSDL_NAME, Approver.WSDL_PATH, new ApproverImpl());
    }

    /**
     * @param serviceName
     * @param wsdlName
     * @param wsdlPath
     * @param serviceImpl
     * @throws IOException
     */
    private void start(String serviceName, String wsdlName, String wsdlPath, Object serviceImpl) throws IOException {
        String wsdlLocation = XBayaPathConstants.WSDL_DIRECTORY + File.separator + wsdlPath;
        XService xservice = this.httpServices.addService(new XSoapDocLiteralService(serviceName, wsdlLocation,
                serviceImpl));
        xservice.addHandler(new StickySoapHeaderHandler("retrieve-lead-header", LeadContextHeader.TYPE));
        xservice.startService();

        WsdlDefinitions wsdl = xservice.getWsdl();

        File wsdlFile = new File(Service.SAMPLE_WSDL_DIRECTORY, wsdlName);
        XMLUtil.saveXML(wsdl, wsdlFile);
    }

    /**
     * @param args
     * @throws IOException
     */
    public static void main(String args[]) throws IOException {
        int port = 0;
        if (args.length == 2) {
            if ("-port".equalsIgnoreCase(args[0])) {
                port = Integer.parseInt(args[1]);
            }
        }
        AllServices service = new AllServices(port);
        service.startAll();
    }
}