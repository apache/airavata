/*
 * Copyright (c) 2007 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the license.
 *
 * $Id: AllServices.java,v 1.4 2008/04/01 21:44:28 echintha Exp $
 */
package org.apache.airavata.xbaya.test.service;

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
import java.io.IOException;

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
import org.apache.airavata.xbaya.util.XMLUtil;

import xsul.lead.LeadContextHeader;
import xsul.wsdl.WsdlDefinitions;
import xsul.xhandler_soap_sticky_header.StickySoapHeaderHandler;
import xsul.xservo.XService;
import xsul.xservo_soap.XSoapDocLiteralService;
import xsul.xservo_soap_http.HttpBasedServices;

/**
 * @author Satoshi Shirasuna
 */
public class AllServices {

    // private static final MLogger logger = MLogger.getLogger();

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
