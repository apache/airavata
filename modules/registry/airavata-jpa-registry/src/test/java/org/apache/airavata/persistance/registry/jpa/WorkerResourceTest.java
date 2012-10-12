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

import java.sql.Date;
import java.util.Calendar;

public class WorkerResourceTest extends AbstractResourceTest {
    private GatewayResource gatewayResource;
    private WorkerResource workerResource;
    private UserResource userResource;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        gatewayResource = super.getGatewayResource();
        workerResource = super.getWorkerResource();
        userResource = super.getUserResource();
    }

    public void testCreate() throws Exception {
        ProjectResource testProject = workerResource.createProject("testProject");
        UserWorkflowResource userWorkflowResource = workerResource.createWorkflowTemplate("workflow1");
        ExperimentResource experimentResource = (ExperimentResource) workerResource.create(ResourceType.EXPERIMENT);

        testProject.setGateway(gatewayResource);
        testProject.save();

        userWorkflowResource.setGateway(gatewayResource);
        userWorkflowResource.setContent("testContent");
        userWorkflowResource.save();

        experimentResource.setGateway(gatewayResource);
        experimentResource.setExpID("testExpID");
        experimentResource.setProject(testProject);
        Calendar calender = Calendar.getInstance();
        java.util.Date d = calender.getTime();
        Date currentTime = new Date(d.getTime());
        experimentResource.setSubmittedDate(currentTime);
        experimentResource.save();

        assertNotNull("project resource created successfully", testProject);
        assertNotNull("user workflow created successfully", userWorkflowResource);
    }

    public void testGet() throws Exception {
        assertNotNull("project resource retrieved successfully", workerResource.get(ResourceType.PROJECT, "testProject"));
        assertNotNull("user workflow retrieved successfully", workerResource.get(ResourceType.USER_WORKFLOW, "workflow1"));
        assertNotNull("experiment retrieved successfully", workerResource.get(ResourceType.EXPERIMENT, "testExpID"));
    }

    public void testGetList() throws Exception {
        assertNotNull("project resources retrieved successfully", workerResource.get(ResourceType.PROJECT));
        assertNotNull("user workflows retrieved successfully", workerResource.get(ResourceType.USER_WORKFLOW));
        assertNotNull("experiments retrieved successfully", workerResource.get(ResourceType.EXPERIMENT));

    }

    public void testSave() throws Exception {
        workerResource.save();
        if (gatewayResource.isExists(ResourceType.USER, "testUser")) {
            assertTrue("worker resource saved successfully", true);
        }
        //remove
//        ResourceUtils.removeGatewayWorker(gatewayResource, userResource);
//        gatewayResource.remove(ResourceType.USER, "testUser");
    }

    public void testRemove() throws Exception {
        workerResource.removeProject("testProject");
        workerResource.removeWorkflowTemplate("workflow1");
        workerResource.removeExperiment("testExpID");

        if (!workerResource.isProjectExists("testProject")) {
            assertTrue("project has been removed successfully", true);
        }

        if (!workerResource.isExperimentExists("testExpID")) {
            assertTrue("experiment has been removed successfully", true);
        }

        if (!workerResource.isWorkflowTemplateExists("workflow1")) {
            assertTrue("user workflow has been removed successfully", true);
        }
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }


}
