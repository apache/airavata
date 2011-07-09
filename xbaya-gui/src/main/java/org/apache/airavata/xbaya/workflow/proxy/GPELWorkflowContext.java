package org.apache.airavata.xbaya.workflow.proxy;

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
import java.net.URISyntaxException;

import org.apache.airavata.xbaya.XBayaConstants;
import org.apache.airavata.xbaya.security.SecurityUtil;
import org.apache.airavata.xbaya.wf.Workflow;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;

import wsmg.WseClientAPI;
import xsul.lead.LeadContextHeader;
import xsul.ws_addressing.WsaEndpointReference;

public class GPELWorkflowContext implements WorkflowContext {

    private static GSSCredential proxy;
    private String topic;
    private LeadContextHeader header;

    public GPELWorkflowContext(String topic, String userName, String password) {
        this.proxy = SecurityUtil.getGSSCredential(userName, password, XBayaConstants.DEFAULT_MYPROXY_SERVER);
        this.topic = topic;

    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.indiana.vortex2.gpel.WorkflowContext#prepare(edu.indiana.vortex2.WorkflowClient)
     */
    public void prepare(WorkflowClient client, Workflow workflow) throws GSSException, URISyntaxException {
        LeadContextHeader leadContextHeader = new LeadContextHeader(this.topic, proxy.getName().toString());

        leadContextHeader.setXRegistryUrl(new URI(XBayaConstants.DEFAULT_XREGISTRY_URL.toString() + "?wsdl"));
        leadContextHeader.setGfacUrl(new URI(XBayaConstants.DEFAULT_GFAC_URL.toString() + "?wsdl"));
        leadContextHeader.setDscUrl(XBayaConstants.DEFAULT_DSC_URL);
        // The followings might overwrite some variables.
        leadContextHeader.setWorkflowTemplateId(workflow.getGPELTemplateID());
        leadContextHeader.setWorkflowInstanceId(new URI(client.getInstanceID()));
        leadContextHeader.setUserDn(proxy.getName().toString());
        leadContextHeader.setMyleadAgentUrl(new URI(XBayaConstants.DEFAULT_MYLEAD_AGENT_URL.toString() + "?wsdl"));
        WsaEndpointReference eventSink = WseClientAPI.createEndpointReference(
                XBayaConstants.DEFAULT_BROKER_URL.toString(), topic);
        leadContextHeader.setEventSink(eventSink);
        leadContextHeader.setExperimentId("urn:uuid:" + topic);
        this.header = leadContextHeader;
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.indiana.vortex2.gpel.WorkflowContext#getHeader()
     */
    public LeadContextHeader getHeader() {
        if (null == this.header) {
            throw new RuntimeException("The Context not 'prepare' ed :" + this);
        }
        return this.header;
    }

    public GSSCredential getCredentials() {
        return this.proxy;
    }

    public String getTopic() {
        return this.topic;
    }

}
