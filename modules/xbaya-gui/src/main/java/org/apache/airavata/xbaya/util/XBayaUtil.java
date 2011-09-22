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

package org.apache.airavata.xbaya.util;

import java.io.InputStream;
import java.net.*;

import org.apache.airavata.xbaya.XBayaConfiguration;
import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.lead.LeadContextHeaderHelper;
import org.apache.airavata.xbaya.monitor.MonitorConfiguration;
import org.apache.airavata.xbaya.wf.Workflow;

import xsul.lead.LeadContextHeader;
import xsul.lead.LeadResourceMapping;
import xsul5.MLogger;

public class XBayaUtil {

    private static final MLogger logger = MLogger.getLogger();

    public static LeadContextHeader buildLeadContextHeader(final XBayaEngine engine,
            MonitorConfiguration monitorConfiguration, String nodeId, LeadResourceMapping resourceMapping)
            throws URISyntaxException {

        XBayaConfiguration configuration = engine.getConfiguration();
        Workflow workflow = engine.getWorkflow();

        LeadContextHeader leadContext = buildLeadContextHeader(workflow, configuration,
                monitorConfiguration, nodeId, resourceMapping);

        return leadContext;

    }

    /**
     *
     * @param workflow
     * @param configuration
     * @param monitorConfiguration
     * @param nodeId
     * @param resourceMapping
     * @return
     * @throws URISyntaxException
     */
    public static LeadContextHeader buildLeadContextHeader(Workflow workflow, XBayaConfiguration configuration,
            MonitorConfiguration monitorConfiguration, String nodeId, LeadResourceMapping resourceMapping)
            throws URISyntaxException {
        LeadContextHeaderHelper leadContextHelper = new LeadContextHeaderHelper();
        leadContextHelper.setXBayaConfiguration(configuration);

        leadContextHelper.setWorkflowInstanceID(workflow.getGPELInstanceID());
        leadContextHelper.setWorkflowTemplateID(workflow.getUniqueWorkflowName());

        leadContextHelper.setMonitorConfiguration(monitorConfiguration);

        LeadContextHeader leadContext = leadContextHelper.getLeadContextHeader();

        leadContext.setNodeId(nodeId);

        leadContext.setTimeStep("1");
        leadContext.setXRegistryUrl(new URI(configuration.getXRegistryURL().toString() + "?wsdl"));

        if (resourceMapping != null) {
            leadContext.setResourceMapping(resourceMapping);
        }
        return leadContext;


    }

    public static boolean isURLExists(String URLName) {
        try {
            if (!URLName.toUpperCase().contains("HTTP"))
                URLName = "http://" + URLName;
            URL url = new URL(URLName);
            System.setProperty("java.net.useSystemProxies", "true");
            HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
            urlConn.setConnectTimeout(9000);
            urlConn.setReadTimeout(9000);
            urlConn.connect();
            if (HttpURLConnection.HTTP_OK == urlConn.getResponseCode())
                return true;
            else
                return false;
        } catch (SocketTimeoutException e) {
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}