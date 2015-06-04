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

package org.apache.airavata.experiment.catalog;

import static org.junit.Assert.*;

import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.experiment.catalog.resources.*;
import org.junit.After;
import org.junit.Test;

import java.sql.Timestamp;
import java.util.Date;


public class GatewayResourceTest extends AbstractResourceTest {
    private GatewayResource gatewayResource;
    private ProjectResource projectResource;
    private UserResource userResource;
    private WorkerResource workerResource;
    private ExperimentResource experimentResource;


    @Override
    public void setUp() throws Exception {
        super.setUp();
        Timestamp currentDate = new Timestamp(new Date().getTime());
        
        gatewayResource = super.getGatewayResource();
        workerResource = super.getWorkerResource();
        userResource = super.getUserResource();
        if (gatewayResource == null) {
            gatewayResource = (GatewayResource) ResourceUtils.getGateway(ServerSettings.getDefaultUserGateway());
        }
        projectResource = (ProjectResource) gatewayResource.create(ResourceType.PROJECT);
        projectResource.setId("testProject");
        projectResource.setName("testProject");
        projectResource.setWorker(workerResource);
        projectResource.save();

        experimentResource = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);

        experimentResource.setExpID("testExpID");
        experimentResource.setExecutionUser(getWorkerResource().getUser());
        experimentResource.setProjectId(getProjectResource().getId());
        experimentResource.setCreationTime(currentDate);
        experimentResource.setApplicationId("testApplication");
        experimentResource.setApplicationVersion("1.0");
        experimentResource.setDescription("Test Application");
        experimentResource.setExpName("TestExperiment");
        experimentResource.save();
    }
    @Test
    public void testSave() throws Exception {
        gatewayResource.setDomain("owner1");
        gatewayResource.save();

        boolean gatewayExist = ResourceUtils.isGatewayExist(ServerSettings.getDefaultUserGateway());
        assertTrue("The gateway exisits", gatewayExist);

    }
 
    @Test
    public void testCreate() throws Exception {
        assertNotNull("project resource cannot be null", projectResource);
        assertNotNull("user resource cannot be null", userResource);
        assertNotNull("worker resource cannot be null", workerResource);
        assertNotNull("experiment resource cannot be null", experimentResource);
    }
    
    @Test
    public void testIsExists() throws Exception {
        assertTrue(gatewayResource.isExists(ResourceType.GATEWAY_WORKER, ServerSettings.getDefaultUser()));
        assertTrue(gatewayResource.isExists(ResourceType.EXPERIMENT, "testExpID"));
    }

    @Test
    public void testGet() throws Exception {
        assertNotNull(gatewayResource.get(ResourceType.GATEWAY_WORKER, ServerSettings.getDefaultUser()));
        assertNotNull(gatewayResource.get(ResourceType.EXPERIMENT, "testExpID"));
    }

    @Test
    public void testGetList() throws Exception {
        assertNotNull(gatewayResource.get(ResourceType.GATEWAY_WORKER));
        assertNotNull(gatewayResource.get(ResourceType.PROJECT));
        assertNotNull(gatewayResource.get(ResourceType.EXPERIMENT));
    }
    
    @Test
    public void testRemove() throws Exception {

        gatewayResource.remove(ResourceType.EXPERIMENT, "testExpID");
        assertFalse(gatewayResource.isExists(ResourceType.EXPERIMENT, "testExpID"));

    }

    @After
    public void tearDown() throws Exception {
    }
}
