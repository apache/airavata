///*
// *
// * Licensed to the Apache Software Foundation (ASF) under one
// * or more contributor license agreements.  See the NOTICE file
// * distributed with this work for additional information
// * regarding copyright ownership.  The ASF licenses this file
// * to you under the Apache License, Version 2.0 (the
// * "License"); you may not use this file except in compliance
// * with the License.  You may obtain a copy of the License at
// *
// *   http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing,
// * software distributed under the License is distributed on an
// * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// * KIND, either express or implied.  See the License for the
// * specific language governing permissions and limitations
// * under the License.
// *
// */
//package org.apache.airavata.persistance.registry.jpa;
//
//import java.util.UUID;
//
//import org.apache.airavata.common.utils.AiravataJobState;
//import org.apache.airavata.persistance.registry.jpa.resources.*;
//
//public class OrchestratorDataResourceTest extends AbstractResourceTest{
//	private OrchestratorDataResource dataResource;
//    private ExperimentMetadataResource experimentResource;
//    private WorkerResource workerResource;
////	private String experimentID = UUID.randomUUID().toString();
//	private String applicationName = "echo_test";
//    private GatewayResource gatewayResource;
//	
//	 @Override
//	    public void setUp() throws Exception {
//         super.setUp();
//         gatewayResource = super.getGatewayResource();
//         workerResource = super.getWorkerResource();
//
//         experimentResource = (ExperimentMetadataResource) gatewayResource.create(ResourceType.EXPERIMENT_METADATA);
//         experimentResource.setExpID("testExpID");
//         experimentResource.setExperimentName("testExpID");
//         experimentResource.setExecutionUser(workerResource.getUser());
//         experimentResource.setProject(new ProjectResource(workerResource, gatewayResource, "testProject"));
//         experimentResource.save();
//
//         dataResource = (OrchestratorDataResource) gatewayResource.create(ResourceType.ORCHESTRATOR);
//
//     }
//
//	    public void testSave() throws Exception {
//	        dataResource.setExperimentID("testExpID");
//	        dataResource.setStatus(AiravataJobState.State.CREATED.toString());
//	        dataResource.setApplicationName(applicationName);
//	        dataResource.save();
//	        assertNotNull("Orchestrator data resource created successfully", dataResource);
//	        // Get saved data
//	        assertNotNull("Orchestrator data resource get successfully", gatewayResource.get(ResourceType.ORCHESTRATOR, "testExpID"));
//	    }
//
//	    @Override
//	    public void tearDown() throws Exception {
//	        super.tearDown();
//	    }
//
//
//}
