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
import java.util.List;

public class GFacJobStatusResourceTest extends AbstractResourceTest {
    private GFacJobDataResource gFacJobDataResource;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        GatewayResource gatewayResource = super.getGatewayResource();
        WorkerResource workerResource = super.getWorkerResource();

        ExperimentResource experimentResource = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
        experimentResource.setExpID("testExpID");
        experimentResource.setWorker(workerResource);
        experimentResource.setProject(new ProjectResource(workerResource, gatewayResource, "testProject"));
        experimentResource.save();

        ExperimentDataResource experimentDataResource = (ExperimentDataResource) experimentResource.create(ResourceType.EXPERIMENT_DATA);
        experimentDataResource.setExpName("testExpID");
        experimentDataResource.setUserName(workerResource.getUser());
        experimentDataResource.save();

        WorkflowDataResource workflowDataResource = (WorkflowDataResource) experimentDataResource.create(ResourceType.WORKFLOW_DATA);
        workflowDataResource.setWorkflowInstanceID("testWFInstance");
        workflowDataResource.setTemplateName("testTemplate");
        workflowDataResource.setExperimentID("testExpID");
        Calendar calender = Calendar.getInstance();
        java.util.Date d = calender.getTime();
        Timestamp timestamp = new Timestamp(d.getTime());
        workflowDataResource.setLastUpdatedTime(timestamp);
        workflowDataResource.save();

        gFacJobDataResource = (GFacJobDataResource) workflowDataResource.create(ResourceType.GFAC_JOB_DATA);
        gFacJobDataResource.setLocalJobID("testJobID");
        gFacJobDataResource.setApplicationDescID("testApplication");
        gFacJobDataResource.setExperimentDataResource(experimentDataResource);
        gFacJobDataResource.setNodeID("testNode");
        gFacJobDataResource.setHostDescID("testHost");
        gFacJobDataResource.setServiceDescID("testService");
        gFacJobDataResource.setStatus("testStatus");
        gFacJobDataResource.setJobData("testJobData");
        gFacJobDataResource.save();

    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testSave() throws Exception {
        GFacJobStatusResource resource = (GFacJobStatusResource)gFacJobDataResource.create(ResourceType.GFAC_JOB_STATUS);
        resource.setStatus("testStatus");
        resource.setgFacJobDataResource(gFacJobDataResource);
        Calendar calender = Calendar.getInstance();
        java.util.Date d = calender.getTime();
        Timestamp timestamp = new Timestamp(d.getTime());
        resource.setStatusUpdateTime(timestamp);
        resource.save();
        List<Resource> resources = gFacJobDataResource.get(ResourceType.GFAC_JOB_STATUS);
        assertTrue("GFac job status saved successfully", resources.size() != 0);
    }
}
