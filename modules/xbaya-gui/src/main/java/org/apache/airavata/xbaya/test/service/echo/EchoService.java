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

package org.apache.airavata.xbaya.test.service.echo;

import java.io.File;

import org.apache.airavata.common.utils.XMLUtil;
import org.apache.airavata.xbaya.test.service.Service;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import xsul.lead.LeadContextHeader;
import xsul.wsdl.WsdlDefinitions;
import xsul.xhandler_soap_sticky_header.StickySoapHeaderHandler;
import xsul.xservo.XService;
import xsul.xservo_soap.XSoapDocLiteralService;
import xsul.xservo_soap_http.HttpBasedServices;

public class EchoService implements Service {

    private static final Log logger = LogFactory.getLog(EchoService.class);

    private HttpBasedServices httpServices;

    private XService xservice;

    private int port;

    /**
     * Constructs an EchoService.
     * 
     * @param port
     */
    public EchoService(int port) {
        this.port = port;
    }

    /**
     * Runs the service.
     */
    public void run() {
        this.httpServices = new HttpBasedServices(this.port);
        this.xservice = this.httpServices.addService(new XSoapDocLiteralService(Echo.SERVICE_NAME,
                Service.MATH_DIRECTORY_NAME + File.separator + Echo.WSDL_PATH, new EchoImpl()));
        this.xservice.addHandler(new StickySoapHeaderHandler("retrieve-lead-header", LeadContextHeader.TYPE));
        this.xservice.startService();
    }

    /**
     * Returns the location of the WSDL of the service.
     * 
     * @return The location of the WSDL of the service.
     */
    public String getServiceWsdlLocation() {
        return this.httpServices.getServer().getLocation() + "/" + Echo.SERVICE_NAME + "?wsdl";
    }

    /**
     * Returns the WSDL of the service.
     * 
     * @return The WSDL of the service.
     */
    public WsdlDefinitions getWsdl() {
        return this.xservice.getWsdl();
    }

    /**
     * Shutdowns the service.
     */
    public void shutdownServer() {
        this.httpServices.getServer().shutdownServer();
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        try {
            int port = 0;
            if (args.length == 2) {
                if ("-port".equalsIgnoreCase(args[0])) {
                    port = Integer.parseInt(args[1]);
                }
            }
            EchoService service = new EchoService(port);
            service.run();
            WsdlDefinitions wsdl = service.getWsdl();
            File wsdlFile = new File(SAMPLE_WSDL_DIRECTORY, Echo.WSDL_NAME);
            XMLUtil.saveXML(wsdl, wsdlFile);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
}