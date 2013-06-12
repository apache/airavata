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

import org.apache.airavata.persistance.registry.jpa.resources.ExperimentResource;
import org.apache.airavata.persistance.registry.jpa.resources.GatewayResource;
import org.apache.airavata.persistance.registry.jpa.resources.ProjectResource;
import org.apache.airavata.persistance.registry.jpa.resources.WorkerResource;

import java.sql.Timestamp;
import java.util.Calendar;

public class ProjectResourceTest extends AbstractResourceTest {
    private GatewayResource gatewayResource;
    private WorkerResource workerResource;
    private ProjectResource projectResource;
    private ExperimentResource experimentResource;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        gatewayResource = super.getGatewayResource();
        workerResource = super.getWorkerResource();
        projectResource = workerResource.createProject("testProject");
        projectResource.setGateway(gatewayResource);
        projectResource.setWorker(workerResource);
        projectResource.save();

        experimentResource = projectResource.createExperiment("testExpID");
        experimentResource.setGateway(gatewayResource);
        experimentResource.setWorker(workerResource);
        Calendar calender = Calendar.getInstance();
        java.util.Date d = calender.getTime();
        Timestamp currentTime = new Timestamp(d.getTime());
        experimentResource.setSubmittedDate(currentTime);
        experimentResource.setProject(projectResource);
        experimentResource.save();
    }

    public void testCreate() throws Exception {
        assertNotNull("experiment resource created successfully", experimentResource);
    }

    public void testGet() throws Exception {
        ExperimentResource experiment = projectResource.getExperiment("testExpID");
        assertNotNull("experiment resource retrieved successfully", experiment);
    }

    public void testGetList() throws Exception {
        assertNotNull("experiment resources retrieved successfully", projectResource.getExperiments());
    }

    public void testSave() throws Exception {
        projectResource.save();
        assertTrue("Project saved successfully", workerResource.isProjectExists("testProject"));
        //remove project
        workerResource.removeProject("testProject");
    }


    public void testRemove() throws Exception {
        projectResource.removeExperiment("testExpID");
        assertFalse("experiment removed successfully", projectResource.isExperimentExists("testExpID"));

        experimentResource = projectResource.createExperiment("testExpID");
        experimentResource.setGateway(gatewayResource);
        experimentResource.setWorker(workerResource);
        Calendar calender = Calendar.getInstance();
        java.util.Date d = calender.getTime();
        Timestamp currentTime = new Timestamp(d.getTime());
        experimentResource.setSubmittedDate(currentTime);
        experimentResource.setProject(projectResource);
        experimentResource.save();
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }
}
