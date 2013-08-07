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

public class ExperimentResourceTest extends AbstractResourceTest {
    private GatewayResource gatewayResource;
    private ExperimentResource experimentResource;
    private WorkerResource workerResource;
    private ExperimentDataResource experimentDataResource;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        gatewayResource = super.getGatewayResource();
        workerResource = super.getWorkerResource();

        experimentResource = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);
        experimentResource.setExpID("testExpID");
        experimentResource.setWorker(workerResource);
        experimentResource.setProject(new ProjectResource(workerResource, gatewayResource, "testProject"));
        Calendar calender = Calendar.getInstance();
        java.util.Date d = calender.getTime();
        Timestamp currentDate = new Timestamp(d.getTime());
        experimentResource.setSubmittedDate(currentDate);
        experimentResource.save();

        experimentDataResource = (ExperimentDataResource) experimentResource.create(ResourceType.EXPERIMENT_DATA);
        experimentDataResource.setExpName("testExpID");
        experimentDataResource.setUserName(workerResource.getUser());
        experimentDataResource.save();
    }

    public void testCreate() throws Exception {
        assertNotNull("experiment data resource has being created ", experimentDataResource);
    }

    public void testGet() throws Exception {
        assertNotNull("experiment data retrieved successfully", experimentResource.get(ResourceType.EXPERIMENT_DATA, "testExpID"));
    }

    public void testSave() throws Exception {
        experimentResource.save();
        assertTrue("experiment save successfully", gatewayResource.isExists(ResourceType.EXPERIMENT, "testExpID"));
    }

    public void testRemove() throws Exception {
        if (!experimentDataResource.isWorkflowInstancePresent("testWFInstance")){
            experimentResource.remove(ResourceType.EXPERIMENT_DATA, "testExpID");
            assertTrue("experiment data removed successfully", !experimentResource.isExists(ResourceType.EXPERIMENT_DATA, "testExpID"));
        }
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }
}
