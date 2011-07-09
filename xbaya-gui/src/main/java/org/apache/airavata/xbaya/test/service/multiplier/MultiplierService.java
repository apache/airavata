/*
 * Copyright (c) 2005-2006 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the license.
 *
 * $Id: MultiplierService.java,v 1.5 2008/04/01 21:44:28 echintha Exp $
 */

package org.apache.airavata.xbaya.test.service.multiplier;

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

import org.apache.airavata.xbaya.util.XMLUtil;

import xsul.lead.LeadContextHeader;
import xsul.wsdl.WsdlDefinitions;
import xsul.xhandler_soap_sticky_header.StickySoapHeaderHandler;
import xsul.xservo.XService;
import xsul.xservo_soap.XSoapDocLiteralService;
import xsul.xservo_soap_http.HttpBasedServices;
import xsul5.MLogger;

/**
 */
public class MultiplierService {

    private final static String SERVICE_NAME = "MultiplierService";

    private final static String BASE_WSDL_LOCATION = "wsdls/math/multiplier-wsdl.xml";

    private final static String OUTPUT_WSDL_LOCATION = "wsdls/sample/multiplier-wsdl.xml";

    private final static MLogger logger = MLogger.getLogger();

    private HttpBasedServices httpServices;

    private XService xservice;

    private int port;

    /**
     * Constructs a MultiplierService.
     * 
     */
    public MultiplierService() {
        this(0);
    }

    /**
     * Constructs a MultiplierService.
     * 
     * @param port
     */
    public MultiplierService(int port) {
        this.port = port;
    }

    /**
     * Runs the service.
     */
    public void run() {
        this.httpServices = new HttpBasedServices(this.port);
        logger.info("Server started on " + this.httpServices.getServerPort());

        logger.info("Using WSDL for service description from " + BASE_WSDL_LOCATION);
        this.xservice = this.httpServices.addService(new XSoapDocLiteralService(SERVICE_NAME, BASE_WSDL_LOCATION,
                new MultiplierImpl()));
        this.xservice.addHandler(new StickySoapHeaderHandler("retrieve-lead-header", LeadContextHeader.TYPE));
        this.xservice.startService();
        logger.info("Service started");
        logger.info("Service WSDL available at " + getServiceWsdlLocation());
    }

    /**
     * Returns the location of the WSDL.
     * 
     * @return The location of the WSDL
     */
    public String getServiceWsdlLocation() {
        return this.httpServices.getServer().getLocation() + "/" + SERVICE_NAME + "?wsdl";
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
            MultiplierService service = new MultiplierService(port);
            service.run();
            WsdlDefinitions wsdl = service.getWsdl();
            File wsdlFile = new File(OUTPUT_WSDL_LOCATION);
            XMLUtil.saveXML(wsdl, wsdlFile);
        } catch (Exception e) {
            logger.caught(e);
        }
    }

}

/*
 * Indiana University Extreme! Lab Software License, Version 1.2
 * 
 * Copyright (c) 2005-2006 The Trustees of Indiana University. All rights reserved.
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

