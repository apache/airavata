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

package org.apache.airavata.xbaya.streaming;

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