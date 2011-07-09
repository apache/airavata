/*
 * Copyright (c) 2005-2007 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the license.
 *
 * $Id: MyLeadTestCase.java,v 1.31 2009/01/10 06:48:18 cherath Exp $
 */

package org.apache.airavata.xbaya.test;

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
import java.util.List;
import java.util.UUID;

import org.apache.airavata.xbaya.XBayaConstants;
import org.apache.airavata.xbaya.component.ComponentException;
import org.apache.airavata.xbaya.component.registry.ComponentRegistryException;
import org.apache.airavata.xbaya.graph.GraphException;
import org.apache.airavata.xbaya.mylead.MyLead;
import org.apache.airavata.xbaya.mylead.MyLeadConfiguration;
import org.apache.airavata.xbaya.mylead.MyLeadException;
import org.apache.airavata.xbaya.mylead.gui.MyleadWorkflowMetadata;
import org.apache.airavata.xbaya.myproxy.MyProxyClient;
import org.apache.airavata.xbaya.security.UserX509Credential;
import org.apache.airavata.xbaya.security.XBayaSecurity;
import org.apache.airavata.xbaya.test.util.WorkflowCreator;
import org.apache.airavata.xbaya.wf.Workflow;
import org.apache.airavata.xbaya.workflow.WorkflowClient;
import org.apache.airavata.xbaya.workflow.WorkflowEngineException;
import org.apache.airavata.xbaya.workflow.WorkflowEngineManager;
import org.apache.xmlbeans.XmlException;
import org.globus.myproxy.MyProxyException;
import org.ietf.jgss.GSSCredential;

import xsul.xwsif_runtime.WSIFClient;
import xsul.xwsif_runtime.XmlBeansWSIFRuntime;
import xsul5.MLogger;
import edu.indiana.dde.mylead.agent.AgentPortType;
import edu.indiana.extreme.lead.metadata.LEADResourceType;
import edu.indiana.extreme.lead.metadata.LEADresourceDocument;
import edu.indiana.extreme.lead.metadata.util.MinimalLEADMetadata;

/**
 * @author Satoshi Shirasuna
 */
public class MyLeadTestCase extends XBayaTestCase {

    private static final MLogger logger = MLogger.getLogger();

    private MyProxyClient myProxyClient;

    /**
     * @see org.apache.airavata.xbaya.test.XBayaTestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();

        this.myProxyClient = new MyProxyClient(this.configuration.getMyProxyServer(),
                this.configuration.getMyProxyPort(), this.configuration.getMyProxyUsername(),
                this.configuration.getMyProxyPassphrase(), this.configuration.getMyProxyLifetime());
    }

    /**
     * Creates a metadata, converts it to a string, and read it back.
     * 
     * @throws GraphException
     * @throws ComponentException
     * @throws XmlException
     * @throws ComponentRegistryException
     */
    public void testMetadata() throws ComponentException, GraphException, XmlException, ComponentRegistryException {
        WorkflowCreator workflowCreator = new WorkflowCreator();
        MyLeadConfiguration myleadConfig = new MyLeadConfiguration(this.configuration.getMyLeadAgentURL(),
                this.configuration.getMyLeadUser(), this.configuration.getMyLeadProject());

        Workflow workflow = workflowCreator.createGFacWorkflow();
        String templateID = "http://test";

        String uid = myleadConfig.getUser();

        String name = workflow.getName();
        String description = workflow.getDescription();

        MinimalLEADMetadata metadata = new MinimalLEADMetadata(uid, name, description);
        metadata.setResourceId(templateID);

        LEADresourceDocument leadResourceDocument = LEADresourceDocument.Factory.parse(metadata.toString());

        leadResourceDocument.getLEADresource();

        String leadResourceString = leadResourceDocument.toString();

        logger.info("leadResourceString: " + leadResourceString);

        LEADresourceDocument leadResourceDocument2 = LEADresourceDocument.Factory.parse(leadResourceString);
        leadResourceDocument2.validate();
        LEADResourceType leadResource2 = leadResourceDocument2.getLEADresource();
        String templateID2 = leadResource2.getResourceID();
        // Compare if the read templateID is same as the original one.
        assertEquals(templateID, templateID2);
    }

    /**
     * Test MyLEADClient which XBaya doesn't use at this point.
     */
    public void testMyLeadAgent() {

        String wsdlLoc = this.configuration.getMyLeadAgentURL().toString() + "?wsdl";

        System.err.println("invoking operation MyLEADClient using WSDL from " + wsdlLoc);

        WSIFClient wcl = XmlBeansWSIFRuntime.newClient(wsdlLoc);
        wcl.generateDynamicStub(AgentPortType.class);

        // This doesn't return anything, but prints out an error message to
        // stdout.
        // TODO Fix this and uncomment
        // MyLEADClient.runDoesUserHaveMyLeadAccount(stub, "leesangm");
        //
        // String user = this.configuration.getMyLeadUser();
        //
        // // This doesn't print out anything, so successful.
        // MyLEADClient.runDoesUserHaveMyLeadAccount(stub, user);
        //
        // MinimalLEADMetadata projectMetadata = new MinimalLEADMetadata(user,
        // "project", "description");
        // projectMetadata.construct();
        // String project = MyLEADClient.runCreateProject(stub, user,
        // projectMetadata.toString(), true);
        // System.out.println("new project " + project + " is added.");
        //
        // MinimalLEADMetadata workflowsMetadata = new MinimalLEADMetadata(user,
        // "project", "description");
        // String workflows = MyLEADClient.runCreateCollection(stub, user,
        // workflowsMetadata.toString(), project, true);
        // System.out.println("new collection " + workflows + " is added.");
        //
        // MinimalLEADMetadata workflowMetadata = new MinimalLEADMetadata(user,
        // "project", "description");
        // String workflow = MyLEADClient.runCreateCollection(stub, user,
        // workflowMetadata.toString(), workflows, true);
        // System.out.println("new collection " + workflow + " is added.");
    }

    /**
     * @throws ComponentException
     * @throws GraphException
     * @throws MyLeadException
     * @throws ComponentRegistryException
     * @throws MyProxyException
     */
    public void testSaveAndLoad() throws ComponentException, GraphException, MyLeadException,
            ComponentRegistryException, MyProxyException {
        // Get MyProxy
        this.myProxyClient.load();
        GSSCredential proxy = this.myProxyClient.getProxy();

        MyLeadConfiguration myleadConfig = new MyLeadConfiguration(this.configuration.getMyLeadAgentURL(),
                this.configuration.getMyLeadUser(), this.configuration.getMyLeadProject());
        MyLead myLead = new MyLead(myleadConfig, proxy);
        WorkflowCreator workflowCreator = new WorkflowCreator();

        Workflow workflow = workflowCreator.createSimpleMathWorkflow();

        UUID uuid = UUID.randomUUID();
        workflow.setName(uuid.toString());

        // First deploy
        String resourceID = myLead.save(workflow, false);
        // Load
        URI templateID2 = myLead.load(resourceID);
        assertEquals(workflow.getUniqueWorkflowName(), templateID2);

        // Redeploy
        String resourceID2 = myLead.save(workflow, true);
        // Load
        URI templateID3 = myLead.load(resourceID2);
        assertEquals(workflow.getUniqueWorkflowName(), templateID3);
    }

    /**
     * @throws MyLeadException
     * @throws MyProxyException
     */
    public void testList() throws MyLeadException, MyProxyException {
        // Get MyProxy
        this.myProxyClient.load();
        GSSCredential proxy = this.myProxyClient.getProxy();

        MyLeadConfiguration myleadConfig = new MyLeadConfiguration(this.configuration.getMyLeadAgentURL(),
                this.configuration.getMyLeadUser(), this.configuration.getMyLeadProject());
        MyLead myLead = new MyLead(myleadConfig, proxy);
        List<MyleadWorkflowMetadata> workflowTemplates = myLead.list();
        for (MyleadWorkflowMetadata template : workflowTemplates) {
            String resouceID = template.getId();
            String title = template.getName();
            logger.info("resouceID: " + resouceID);
            logger.info("title: " + title);
        }
    }

    /**
     * @throws ComponentException
     * @throws GraphException
     * @throws MyLeadException
     * @throws org.apache.airavata.xbaya.workflow.WorkflowEngineException
     * @throws ComponentRegistryException
     * @throws MyProxyException
     */
    public void testSaveAndLoadWithGPEL() throws ComponentException, GraphException, MyLeadException,
            WorkflowEngineException, ComponentRegistryException, MyProxyException {
        // Get MyProxy
        this.myProxyClient.load();
        GSSCredential proxy = this.myProxyClient.getProxy();

        MyLeadConfiguration myleadConfig = new MyLeadConfiguration(this.configuration.getMyLeadAgentURL(),
                this.configuration.getMyLeadUser(), this.configuration.getMyLeadProject());
        MyLead myLead = new MyLead(myleadConfig, proxy);

        UserX509Credential gpelUserCredential = new UserX509Credential(proxy, XBayaSecurity.getTrustedCertificates());
        WorkflowClient gpelClient = WorkflowEngineManager.getWorkflowClient(XBayaConstants.DEFAULT_GPEL_ENGINE_URL,
                gpelUserCredential);
        gpelClient.setEngineURL(XBayaConstants.DEFAULT_GPEL_ENGINE_URL);

        WorkflowCreator workflowCreator = new WorkflowCreator();
        Workflow workflow = workflowCreator.createSimpleMathWorkflow();

        gpelClient.createScriptAndDeploy(workflow, false);

        logger.info("template ID: " + workflow.getUniqueWorkflowName());

        String resourceID = myLead.save(workflow, false);

        logger.info("resourceID: " + resourceID);

        URI templateID2 = myLead.load(resourceID);

        Workflow workflow2 = gpelClient.load(templateID2);

        logger.info("workflow name: " + workflow2.getName());
        assertEquals(workflow.getName(), workflow2.getName());
    }
}

/*
 * Indiana University Extreme! Lab Software License, Version 1.2
 * 
 * Copyright (c) 2005-2007 The Trustees of Indiana University. All rights reserved.
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
