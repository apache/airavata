/*
 * Copyright (c) 2009 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the license.
 *
 * $Id: $
 */
package org.apache.airavata.xbaya.streaming;

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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.airavata.xbaya.XBaya;
import org.apache.airavata.xbaya.XBayaConfiguration;
import org.apache.airavata.xbaya.XBayaConstants;
import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.XBayaException;
import org.apache.airavata.xbaya.lead.LEADWorkflowInvoker;
import org.apache.airavata.xbaya.lead.LeadContextHeaderHelper;
import org.apache.airavata.xbaya.monitor.MonitorConfiguration;
import org.apache.airavata.xbaya.myproxy.MyProxyClient;
import org.apache.airavata.xbaya.myproxy.gui.MyProxyChecker;
import org.apache.airavata.xbaya.security.XBayaSecurity;
import org.xmlpull.infoset.XmlBuilderException;
import org.xmlpull.infoset.XmlElement;

import xsul.invoker.gsi.GsiInvoker;
import xsul.lead.LeadContextHeader;
import xsul5.XmlConstants;

/**
 * @author Chathura Herath
 */
public class StreamPublisher {

    public static void main(String[] args) throws XmlBuilderException, FileNotFoundException, URISyntaxException,
            XBayaException {

        XmlElement wsdlElement = XmlConstants.BUILDER.parseFragmentFromInputStream(

        new FileInputStream(new File("echo.xml")));
        xsul5.wsdl.WsdlDefinitions wsdlDefinitions = new xsul5.wsdl.WsdlDefinitions(wsdlElement);
        XBaya xbaya = new XBaya(new String[0]);
        XBayaEngine engine = xbaya.getEngine();

        LeadContextHeaderHelper leadContextHelper = new LeadContextHeaderHelper();
        XBayaConfiguration configuration = engine.getConfiguration();
        leadContextHelper.setXBayaConfiguration(configuration);
        leadContextHelper.setMyLeadConfiguration(engine.getMyLead().getConfiguration());
        leadContextHelper.setWorkflowInstanceID(engine.getWorkflow().getGPELInstanceID());
        leadContextHelper.setWorkflowTemplateID(engine.getWorkflow()

        .getUniqueWorkflowName());

        MonitorConfiguration monitorConfig = engine.getMonitor().getConfiguration();
        String TOPIC = "d25edf16-499c-4bb6-832f-b2b21d50fabe";
        monitorConfig.setTopic(TOPIC);
        leadContextHelper.setMonitorConfiguration(monitorConfig);

        LeadContextHeader leadContext = leadContextHelper.getLeadContextHeader();

        leadContext.setExperimentId(TOPIC);

        leadContext.setTimeStep("1");
        leadContext.setXRegistryUrl(new URI(XBayaConstants.DEFAULT_XREGISTRY_URL.toString() + "?wsdl"));
        MyProxyChecker myProxyChecker = new MyProxyChecker(engine);
        myProxyChecker.loadIfNecessary();
        GsiInvoker secureInvoker = null;
        if (engine.getWorkflowClient().isSecure()) {
            MyProxyClient myProxyClient = engine.getMyProxyClient();
            secureInvoker = new GsiInvoker(myProxyClient.getProxy(), XBayaSecurity.getTrustedCertificates());

            leadContext.setScmsUrl(URI.create("https://tyr12.cs.indiana.edu:60443/SCMS?wsdl"));

        }
        LEADWorkflowInvoker invoker = new LEADWorkflowInvoker(wsdlDefinitions, leadContext, null, secureInvoker);

        invoker.getInputMessage().setObjectPart("input", "chathura");

        invoker.invoke();

    }
}

/*
 * Indiana University Extreme! Lab Software License, Version 1.2
 * 
 * Copyright (c) 2009 The Trustees of Indiana University. All rights reserved.
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
