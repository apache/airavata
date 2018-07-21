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
 */
package org.apache.airavata.registry.core.repositories.workflowcatalog;

import org.apache.airavata.model.WorkflowModel;
import org.apache.airavata.model.application.io.OutputDataObjectType;
import org.apache.airavata.model.workflow.AiravataWorkflow;
import org.apache.airavata.registry.core.repositories.workflowcatalog.util.Initialize;
import org.apache.airavata.registry.cpi.WorkflowCatalogException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

public class WorkflowRepositoryTest {

    private static Initialize initialize;
    private WorkflowRepository workflowRepository;
    private String gatewayId = "testGateway";
    private static final Logger logger = LoggerFactory.getLogger(WorkflowRepositoryTest.class);

    @Before
    public void setUp() {
        try {
            initialize = new Initialize("airavataworkflowcatalog-derby.sql");
            initialize.initializeDB();
            workflowRepository = new WorkflowRepository();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    @After
    public void tearDown() throws Exception {
        System.out.println("********** TEAR DOWN ************");
        initialize.stopDerbyServer();
    }

    @Test
    public void WorkflowRepositoryTest() throws WorkflowCatalogException {

        AiravataWorkflow workflowModel1 = new AiravataWorkflow();
        workflowModel1.setName("workflow1");
        workflowModel1.setGatewayId(gatewayId);
        workflowModel1.setUserName("user1");
        workflowModel1.setStorageResourceId("storage1");
        workflowModel1.setDescription("description1");
        workflowModel1.setEnableEmailNotification(true);

        ArrayList<String> emails = new ArrayList<>();
        emails.add("example1@example.com");
        emails.add("example2@example.com");

        workflowModel1.setNotificationEmails(emails);

        workflowRepository.registerWorkflow(workflowModel1, gatewayId);

        List<String> workflows = workflowRepository.getAllWorkflows(gatewayId);
        assertEquals(1, workflows.size());

//        OutputDataObjectType outputDataObjectType1 = new OutputDataObjectType();
//        outputDataObjectType1.setName("outputKey1");
//        OutputDataObjectType outputDataObjectType2 = new OutputDataObjectType();
//        outputDataObjectType2.setName("outputKey2");
//        List<OutputDataObjectType> outputDataObjectTypeList = new ArrayList<>();
//        outputDataObjectTypeList.add(outputDataObjectType1);
//        outputDataObjectTypeList.add(outputDataObjectType2);
//        workflowRepository.updateWorkflowOutputs(templateId1, outputDataObjectTypeList);
//
//        WorkflowModel retrievedWorkflowModel = workflowRepository.getWorkflow(templateId1);
//        assertEquals(gatewayId, retrievedWorkflowModel.getGatewayId());
//        assertEquals(workflowModel1.getCreatedUser(), retrievedWorkflowModel.getCreatedUser());
//        assertTrue(retrievedWorkflowModel.getWorkflowOutputs().size() == 2);
//
//        WorkflowModel workflowModel2 = new WorkflowModel();
//        workflowModel2.setName("workflow2");
//        String templateId2 = workflowRepository.registerWorkflow(workflowModel2, gatewayId);
//        assertTrue(templateId2 != null);
//
//        List<String> workflows = workflowRepository.getAllWorkflows(gatewayId);
//        assertTrue(workflows.size() == 2);
//
//        String retrievedTemplateId = workflowRepository.getWorkflowId(workflowModel1.getName());
//        assertEquals(templateId1, retrievedTemplateId);
//
//        assertTrue(workflowRepository.isWorkflowExistWithName(workflowModel2.getName()));
//
//        workflowRepository.deleteWorkflow(templateId1);
//        assertFalse(workflowRepository.isWorkflowExistWithName(workflowModel1.getName()));
//
//        workflowRepository.deleteWorkflow(templateId2);
    }

}
