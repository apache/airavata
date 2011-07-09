/*
 * Copyright (c) 2006-2007 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the license.
 *
 * $Id: MyLeadAgentStubTestCase.java,v 1.12 2008/11/13 23:59:38 smarru Exp $
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

import java.util.ArrayList;

import org.apache.airavata.xbaya.mylead.MyLead;
import org.apache.airavata.xbaya.mylead.MyLeadAgentStub;
import org.apache.airavata.xbaya.mylead.MyLeadException;
import org.apache.airavata.xbaya.mylead.MyLeadQueryResultItem;
import org.apache.airavata.xbaya.myproxy.MyProxyClient;
import org.apache.xmlbeans.XmlException;

import xsul5.MLogger;
import edu.indiana.dde.metadata.catalog.types.ContentFilterType;
import edu.indiana.dde.metadata.catalog.types.QueryTargetDocument;
import edu.indiana.dde.mylead.agent.AgentPortType;
import edu.indiana.dde.mylead.agent.xmlbeans.ContextQueryRequestDocument;
import edu.indiana.dde.mylead.agent.xmlbeans.ContextQueryRequestType;
import edu.indiana.dde.mylead.agent.xmlbeans.QueryResponseDocument;
import edu.indiana.dde.mylead.agent.xmlbeans.QueryResultConfigurationType;
import edu.indiana.extreme.lead.metadata.LEADresourceDocument;
import edu.indiana.extreme.lead.metadata.util.MinimalLEADMetadata;

/**
 * @author Satoshi Shirasuna
 */
public class MyLeadAgentStubTestCase extends XBayaTestCase {

    private static final MLogger logger = MLogger.getLogger();

    private static final String FAKE_USER_ID = "/C=US/O=National Center for Supercomputing Applications/CN=Fake User";

    private MyProxyClient myProxyClient;

    private static final String quryTargetXml = "<queryTarget xmlns='http://www.cs.indiana.edu/dde/namespaces/2008/02/catalog/types'>"
            + "<aggrType>EXPERIMENT</aggrType>"
            + "<createDateStart>2007-01-01T00:00:01</createDateStart>"
            + "<createDateEnd>2009-01-01T00:00:01</createDateEnd>"
            + "<queryPropertyOrSet>"
            + "<queryProperty>"
            + "<name>citation</name>"
            + "<source>LEAD</source>"
            + "<queryElement><name>title</name><source>LEAD</source><queryTextElement>"
            + "<value>NAM</value><compare>CONTAINS</compare></queryTextElement>"
            + "</queryElement></queryProperty><queryProperty><name>grid</name>"
            + "<source>LEAD</source><queryElement><name>ctrlat</name><source>LEAD"
            + "</source><queryValueElement><value>25</value><comparison>GT</comparison>"
            + "</queryValueElement></queryElement><queryElement><name>dx</name><"
            + "source>LEAD</source><queryValueElement><value>10000</value>"
            + "<comparison>LT</comparison></queryValueElement></queryElement>"
            + "</queryProperty></queryPropertyOrSet></queryTarget>";

    /**
     * @see org.apache.airavata.xbaya.test.XBayaTestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();

        this.myProxyClient = new MyProxyClient(this.configuration.getMyProxyServer(),
                this.configuration.getMyProxyPort(), this.configuration.getMyProxyUsername(),
                this.configuration.getMyProxyPassphrase(), this.configuration.getMyProxyLifetime());
        this.myProxyClient.load();
    }

    /**
     * @throws MyLeadException
     */
    public void testDoesUserHaveMyLeadAccount() throws MyLeadException {
        MyLeadAgentStub stub = new MyLeadAgentStub(this.configuration.getMyLeadAgentURL(),
                this.myProxyClient.getProxy());
        boolean result = stub.doesUserHaveMyLeadAccount(this.configuration.getMyLeadUser());
        assertEquals(true, result);

        boolean result2 = stub.doesUserHaveMyLeadAccount(FAKE_USER_ID);
        assertEquals(false, result2);
    }

    /**
     * @throws MyLeadException
     */
    public void estAddNewUser() throws MyLeadException {
        MyLeadAgentStub stub = new MyLeadAgentStub(this.configuration.getMyLeadAgentURL(),
                this.myProxyClient.getProxy());
        String result = stub.addNewUser(FAKE_USER_ID, FAKE_USER_ID);
        logger.info("result: " + result);
    }

    /**
     * @throws MyLeadException
     */
    public void estCreateProject() throws MyLeadException {
        MyLeadAgentStub stub = new MyLeadAgentStub(this.configuration.getMyLeadAgentURL(),
                this.myProxyClient.getProxy());
        try {
            MinimalLEADMetadata projectMetadata = new MinimalLEADMetadata(FAKE_USER_ID, "Project1",
                    "Project description");
            projectMetadata.setResourceId("Unknown"); // this is neccesary.

            String projectID = stub.createProject(FAKE_USER_ID, projectMetadata.toString(), true);

            MinimalLEADMetadata workflowTemplatesMetadata = new MinimalLEADMetadata(FAKE_USER_ID,
                    MyLead.WORKFLOW_TEMPLATES_COLLECTION, "Workflow templates collection description");
            workflowTemplatesMetadata.setResourceId("Unknown");
            LEADresourceDocument leadResourceDoc;

            leadResourceDoc = LEADresourceDocument.Factory.parse(workflowTemplatesMetadata.toString());

            String workflowTemplatesCollectionID = stub.createCollection(FAKE_USER_ID,
                    leadResourceDoc.getLEADresource(), projectID, true);
            logger.info("workflowTemplatesCollectionID: " + workflowTemplatesCollectionID);

        } catch (XmlException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * @throws MyLeadException
     * 
     */
    public void testListProjectsInFullFormat() throws MyLeadException, XmlException {
        MyLeadAgentStub stub = new MyLeadAgentStub(this.configuration.getMyLeadAgentURL(),
                this.myProxyClient.getProxy());
        String user = this.configuration.getMyLeadUser();

        int limit = 0;

        ContentFilterType.Enum cFilter = ContentFilterType.FULL_SCHEMA;

        ContextQueryRequestDocument reqDoc = ContextQueryRequestDocument.Factory.newInstance();
        ContextQueryRequestType req = reqDoc.addNewContextQueryRequest();
        QueryResultConfigurationType resultConfig = req.addNewQueryResultConfiguration();
        resultConfig.setContentFilter(cFilter);
        resultConfig.setHierarchyFilter(edu.indiana.dde.metadata.catalog.types.HierarchyFilterType.TARGET);
        resultConfig.setCount(limit);
        resultConfig.setOffset(0);
        req.setQueryTarget(QueryTargetDocument.Factory.parse(quryTargetXml).getQueryTarget());

        AgentPortType agentStub = stub.createStub(user);

        QueryResponseDocument response = agentStub.queryWithContext(reqDoc);
        logger.finest("resultElement: " + response.toString());
    }

    /**
     * @throws MyLeadException
     * 
     */
    public void testListProjects() throws MyLeadException {
        MyLeadAgentStub stub = new MyLeadAgentStub(this.configuration.getMyLeadAgentURL(),
                this.myProxyClient.getProxy());

        ArrayList<MyLeadQueryResultItem> projects = stub.listProjects(this.configuration.getMyLeadUser());
        for (MyLeadQueryResultItem project : projects) {
            String projectID = project.getResouceID();
            logger.info("projectID: " + projectID);
            String title = project.getTitle();
            logger.info("title: " + title);
        }
    }

    /**
     * @throws MyLeadException
     */
    public void testProjectNameToID() throws MyLeadException {
        MyLeadAgentStub stub = new MyLeadAgentStub(this.configuration.getMyLeadAgentURL(),
                this.myProxyClient.getProxy());

        String user = this.configuration.getMyLeadUser();
        String projectName = this.configuration.getMyLeadProject();
        String projectID = stub.projectNameToID(user, projectName);
        logger.info("projectID: " + projectID);
    }

    /**
     * @throws MyLeadException
     */
    // public void testQueryById() throws MyLeadException {
    // MyLeadAgentStub stub = new MyLeadAgentStub(this.configuration
    // .getMyLeadAgentURL(), this.myProxyClient.getProxy());
    // String user = this.configuration.getMyLeadUser();
    // String projectName = this.configuration.getMyLeadProject();
    //
    // String projectID = stub.projectNameToID(user, projectName);
    // logger.info("projectID: " + projectID);
    //
    // String templateCollectionID = stub.collectionNameToID(user, projectID,
    // MyLead.WORKFLOW_TEMPLATES_COLLECTION);
    // logger.info("templateCollectionID: " + templateCollectionID);
    //
    // List<MyLeadQueryResultItem> templates = stub.listCollections(user,
    // templateCollectionID);
    // for (MyLeadQueryResultItem template : templates) {
    // String templateTitle = template.getTitle();
    // logger.info("templateTitle: " + templateTitle);
    // String templateResourceID = template.getResouceID();
    //
    // LEADresourceDocument leadMetadata = stub.getCollection(user,
    // templateResourceID);
    // logger.info(leadMetadata.toString());
    // }
    // }
}

/*
 * Indiana University Extreme! Lab Software License, Version 1.2
 * 
 * Copyright (c) 2006-2007 The Trustees of Indiana University. All rights reserved.
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
