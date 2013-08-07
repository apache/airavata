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

package org.apache.airavata.persistance.registry.jpa;

import org.apache.airavata.persistance.registry.jpa.resources.*;

import java.sql.Timestamp;
import java.util.Calendar;

public class ExperimentDataResourceTest extends AbstractResourceTest {
    private ExperimentDataResource experimentDataResource;
    private ExperimentResource experimentResource;
    private WorkflowDataResource workflowDataResource;
    private ExperimentMetadataResource experimentMetadataResource;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        GatewayResource gatewayResource = super.getGatewayResource();
        WorkerResource workerResource = super.getWorkerResource();

        experimentResource = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
        experimentResource.setExpID("testExpID");
        experimentResource.setWorker(workerResource);
        experimentResource.setProject(new ProjectResource(workerResource, gatewayResource, "testProject"));
        experimentResource.save();

        experimentDataResource = (ExperimentDataResource) experimentResource.create(ResourceType.EXPERIMENT_DATA);
        experimentDataResource.setExpName("testExpID");
        experimentDataResource.setUserName(workerResource.getUser());
        experimentDataResource.save();

        experimentMetadataResource = experimentDataResource.createExperimentMetadata();
        workflowDataResource = experimentDataResource.createWorkflowInstanceResource("testWorkflowInstance");

        experimentMetadataResource.setExpID("testExpID");
        experimentMetadataResource.setMetadata("testMetadata");
        experimentMetadataResource.save();

        workflowDataResource.setExperimentID("testExpID");
        workflowDataResource.setStatus("testStatus");
        workflowDataResource.setTemplateName("testWorkflowInstance");

        Calendar calender = Calendar.getInstance();
        java.util.Date d = calender.getTime();
        Timestamp currentTime = new Timestamp(d.getTime());

        workflowDataResource.setLastUpdatedTime(currentTime);
        workflowDataResource.setStartTime(currentTime);
        workflowDataResource.save();
    }

    public void testCreate() throws Exception {
        assertNotNull("workflowdata resource created", workflowDataResource);
        assertNotNull("experimemt metadata resource created", experimentMetadataResource);
    }

    public void testGet() throws Exception {
        assertNotNull("workflow data retrieved successfully", experimentDataResource.getWorkflowInstance("testWorkflowInstance"));
        assertNotNull("experiment meta data retrieved successfully", experimentDataResource.getExperimentMetadata());
    }

    public void testGetList() throws Exception {
        assertNotNull("workflow data retrieved successfully", experimentDataResource.get(ResourceType.WORKFLOW_DATA));
        assertNotNull("experiment meta data retrieved successfully", experimentDataResource.get(ResourceType.EXPERIMENT_METADATA));
    }

    public void testSave() throws Exception {
        experimentDataResource.save();
        assertTrue("experiment data saved successfully", experimentResource.isExists(ResourceType.EXPERIMENT_DATA, "testExpID"));
    }

    public void testRemove() throws Exception {
        experimentDataResource.remove(ResourceType.WORKFLOW_DATA, "testWFInstanceID");
        assertTrue("workflow data resource removed successfully", !experimentResource.isExists(ResourceType.EXPERIMENT_DATA, "testWFInstanceID"));

        experimentDataResource.remove(ResourceType.EXPERIMENT_METADATA, "testExpID");
        assertTrue("experiment meta data resource removed successfully", !experimentDataResource.isExists(ResourceType.EXPERIMENT_METADATA, "testExpID"));

    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }


}
