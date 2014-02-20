///*
//*
//* Licensed to the Apache Software Foundation (ASF) under one
//* or more contributor license agreements.  See the NOTICE file
//* distributed with this work for additional information
//* regarding copyright ownership.  The ASF licenses this file
//* to you under the Apache License, Version 2.0 (the
//* "License"); you may not use this file except in compliance
//* with the License.  You may obtain a copy of the License at
//*
//*   http://www.apache.org/licenses/LICENSE-2.0
//*
//* Unless required by applicable law or agreed to in writing,
//* software distributed under the License is distributed on an
//* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
//* KIND, either express or implied.  See the License for the
//* specific language governing permissions and limitations
//* under the License.
//*
//*/
//
//package org.apache.airavata.persistance.registry.jpa;
//
//
//import org.apache.airavata.persistance.registry.jpa.resources.*;
//
//import java.sql.Date;
//import java.sql.Timestamp;
//import java.util.Calendar;
//
//public class ExperimentMetadataResourceTest extends AbstractResourceTest {
//
//    private GatewayResource gatewayResource;
//    private WorkflowDataResource workflowDataResource;
//    private ExperimentMetadataResource experimentResource;
//
//    @Override
//    public void setUp() throws Exception {
//        super.setUp();
//        gatewayResource = super.getGatewayResource();
//        WorkerResource workerResource = super.getWorkerResource();
//        experimentResource = (ExperimentMetadataResource) gatewayResource.create(ResourceType.EXPERIMENT_METADATA);
//        experimentResource.setExpID("testExpID");
//        experimentResource.setExecutionUser("admin");
//        experimentResource.setProject(new ProjectResource(workerResource, gatewayResource, "testProject"));
//        experimentResource.setDescription("testDescription");
//        experimentResource.setExperimentName("textExpID");
//        experimentResource.setSubmittedDate(getCurrentTimestamp());
//        experimentResource.setShareExp(true);
//        experimentResource.save();
//
//        ExperimentConfigDataResource exConfig = (ExperimentConfigDataResource)experimentResource.create(ResourceType.EXPERIMENT_CONFIG_DATA);
//        exConfig.setExpID("testExpID");
//        exConfig.setNodeCount(5);
//        exConfig.setCpuCount(10);
//        exConfig.setApplicationID("testApp");
//        exConfig.setApplicationVersion("testAppVersion");
//        exConfig.save();
//
//        workflowDataResource = experimentResource.createWorkflowInstanceResource("testWFInstance");
//        workflowDataResource.setExperimentID("testExpID");
//        workflowDataResource.setStatus("testStatus");
//        workflowDataResource.setTemplateName("testWFInstance");
//        workflowDataResource.setLastUpdatedTime(getCurrentTimestamp());
//        workflowDataResource.setStartTime(getCurrentTimestamp());
//        workflowDataResource.save();
//    }
//
//    public void testSave() throws Exception {
//        assertTrue("experiment meta data saved successfully", gatewayResource.isExists(ResourceType.EXPERIMENT_METADATA, "testExpID"));
//
//    }
//
//    public void testRemove() throws Exception {
//        experimentResource.remove(ResourceType.WORKFLOW_DATA, "testWFInstance");
//        assertTrue("workflow data resource removed successfully", !experimentResource.isExists(ResourceType.WORKFLOW_DATA, "testWFInstance"));
//    }
//
//    public void testGet() throws Exception {
//        assertNotNull("experiment configuration retrieved successfully...", experimentResource.get(ResourceType.EXPERIMENT_CONFIG_DATA, "testExpID"));
//    }
//
//    @Override
//    public void tearDown() throws Exception {
//        super.tearDown();
//    }
//}
