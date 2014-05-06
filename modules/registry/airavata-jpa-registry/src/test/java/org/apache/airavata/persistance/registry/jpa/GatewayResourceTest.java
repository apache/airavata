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

import static org.junit.Assert.*;

import org.apache.airavata.persistance.registry.jpa.resources.*;
import org.junit.After;
import org.junit.Test;

import java.sql.Timestamp;
import java.util.Date;


public class GatewayResourceTest extends AbstractResourceTest {
    private GatewayResource gatewayResource;
    private ProjectResource projectResource;
    private UserResource userResource;
    private WorkerResource workerResource;
    private PublishWorkflowResource publishWorkflowResource;
    private UserWorkflowResource userWorkflowResource;
    private HostDescriptorResource hostDescriptorResource;
    private ServiceDescriptorResource serviceDescriptorResource;
    private ApplicationDescriptorResource applicationDescriptorResource;
    private ExperimentResource experimentResource;


    @Override
    public void setUp() throws Exception {
        super.setUp();
        Timestamp currentDate = new Timestamp(new Date().getTime());
        
        gatewayResource = super.getGatewayResource();
        workerResource = super.getWorkerResource();
        userResource = super.getUserResource();
        if (gatewayResource == null) {
            gatewayResource = (GatewayResource) ResourceUtils.getGateway("default");
        }
        projectResource = (ProjectResource) gatewayResource.create(ResourceType.PROJECT);
        projectResource.setId("testProject");
        projectResource.setName("testProject");
        projectResource.setWorker(workerResource);
        projectResource.save();

        publishWorkflowResource = (PublishWorkflowResource) gatewayResource.create(ResourceType.PUBLISHED_WORKFLOW);
        userWorkflowResource = (UserWorkflowResource) gatewayResource.create(ResourceType.USER_WORKFLOW);
        hostDescriptorResource = (HostDescriptorResource) gatewayResource.create(ResourceType.HOST_DESCRIPTOR);
        serviceDescriptorResource = (ServiceDescriptorResource) gatewayResource.create(ResourceType.SERVICE_DESCRIPTOR);
        applicationDescriptorResource = (ApplicationDescriptorResource) gatewayResource.create(ResourceType.APPLICATION_DESCRIPTOR);
        experimentResource = (ExperimentResource) gatewayResource.create(ResourceType.EXPERIMENT);

        hostDescriptorResource.setUserName(workerResource.getUser());
        hostDescriptorResource.setHostDescName("testHostDesc");
        hostDescriptorResource.setContent("testContent");
        hostDescriptorResource.save();

        serviceDescriptorResource.setUserName(workerResource.getUser());
        serviceDescriptorResource.setServiceDescName("testServiceDesc");
        serviceDescriptorResource.setContent("testContent");
        serviceDescriptorResource.save();

        applicationDescriptorResource.setHostDescName(hostDescriptorResource.getHostDescName());
        applicationDescriptorResource.setServiceDescName(serviceDescriptorResource.getServiceDescName());
        applicationDescriptorResource.setUpdatedUser(workerResource.getUser());
        applicationDescriptorResource.setName("testAppDesc");
        applicationDescriptorResource.setContent("testContent");
        applicationDescriptorResource.save();

        userWorkflowResource.setName("workflow1");
        userWorkflowResource.setLastUpdateDate(currentDate);
        userWorkflowResource.setWorker(workerResource);
        userWorkflowResource.setContent("testContent");
        userWorkflowResource.save();

        publishWorkflowResource.setName("pubworkflow1");
        publishWorkflowResource.setCreatedUser("admin");
        publishWorkflowResource.setContent("testContent");
        publishWorkflowResource.setPublishedDate(currentDate);
        publishWorkflowResource.save();

        experimentResource.setExpID("testExpID");
        experimentResource.setExecutionUser(getWorkerResource().getUser());
        experimentResource.setProject(getProjectResource());
        experimentResource.setCreationTime(currentDate);
        experimentResource.setApplicationId("testApplication");
        experimentResource.setApplicationVersion("1.0");
        experimentResource.setDescription("Test Application");
        experimentResource.setExpName("TestExperiment");
        experimentResource.save();
    }
    @Test
    public void testSave() throws Exception {
        gatewayResource.setOwner("owner1");
        gatewayResource.save();

        boolean gatewayExist = ResourceUtils.isGatewayExist("default");
        assertTrue("The gateway exisits", gatewayExist);

    }
 
    @Test
    public void testCreate() throws Exception {
        assertNotNull("project resource cannot be null", projectResource);
        assertNotNull("user resource cannot be null", userResource);
        assertNotNull("worker resource cannot be null", workerResource);
        assertNotNull("publish workflow resource cannot be null", publishWorkflowResource);
        assertNotNull("user workflow resource cannot be null", userWorkflowResource);
        assertNotNull("host descriptor resource cannot be null", hostDescriptorResource);
        assertNotNull("service descriptor resource cannot be null", serviceDescriptorResource);
        assertNotNull("application descriptor resource cannot be null", applicationDescriptorResource);
        assertNotNull("experiment resource cannot be null", experimentResource);
    }
    
    @Test
    public void testIsExists() throws Exception {
        assertTrue(gatewayResource.isExists(ResourceType.USER, "admin"));
        assertTrue(gatewayResource.isExists(ResourceType.PUBLISHED_WORKFLOW, "pubworkflow1"));
        assertTrue(gatewayResource.isExists(ResourceType.HOST_DESCRIPTOR, "testHostDesc"));
        assertTrue(gatewayResource.isExists(ResourceType.SERVICE_DESCRIPTOR, "testServiceDesc"));
        assertTrue(gatewayResource.isExists(ResourceType.APPLICATION_DESCRIPTOR, "testAppDesc"));
        assertTrue(gatewayResource.isExists(ResourceType.EXPERIMENT, "testExpID"));
    }

    @Test
    public void testGet() throws Exception {
        assertNotNull(gatewayResource.get(ResourceType.USER, "admin"));
        assertNotNull(gatewayResource.get(ResourceType.PUBLISHED_WORKFLOW, "pubworkflow1"));
        assertNotNull(gatewayResource.get(ResourceType.HOST_DESCRIPTOR, "testHostDesc"));
        assertNotNull(gatewayResource.get(ResourceType.SERVICE_DESCRIPTOR, "testServiceDesc"));
        assertNotNull(gatewayResource.get(ResourceType.APPLICATION_DESCRIPTOR, "testAppDesc"));
        assertNotNull(gatewayResource.get(ResourceType.EXPERIMENT, "testExpID"));
    }

    @Test
    public void testGetList() throws Exception {
        assertNotNull(gatewayResource.get(ResourceType.GATEWAY_WORKER));
        assertNotNull(gatewayResource.get(ResourceType.PROJECT));
        assertNotNull(gatewayResource.get(ResourceType.PUBLISHED_WORKFLOW));
        assertNotNull(gatewayResource.get(ResourceType.HOST_DESCRIPTOR));
        assertNotNull(gatewayResource.get(ResourceType.SERVICE_DESCRIPTOR));
        assertNotNull(gatewayResource.get(ResourceType.APPLICATION_DESCRIPTOR));
        assertNotNull(gatewayResource.get(ResourceType.EXPERIMENT));
    }
    
    @Test
    public void testRemove() throws Exception {

        gatewayResource.remove(ResourceType.PUBLISHED_WORKFLOW, "pubworkflow1");
        assertFalse(gatewayResource.isExists(ResourceType.PUBLISHED_WORKFLOW, "pubworkflow1"));

        gatewayResource.remove(ResourceType.HOST_DESCRIPTOR, "testHostDesc");
        assertFalse(gatewayResource.isExists(ResourceType.HOST_DESCRIPTOR, "testHostDesc"));

        gatewayResource.remove(ResourceType.SERVICE_DESCRIPTOR, "testServiceDesc");
        assertFalse(gatewayResource.isExists(ResourceType.SERVICE_DESCRIPTOR, "testServiceDesc"));

        gatewayResource.remove(ResourceType.EXPERIMENT, "testExpID");
        assertFalse(gatewayResource.isExists(ResourceType.EXPERIMENT, "testExpID"));

        gatewayResource.remove(ResourceType.APPLICATION_DESCRIPTOR, "testAppDesc");
        assertFalse(gatewayResource.isExists(ResourceType.APPLICATION_DESCRIPTOR, "testAppDesc"));

    }

    @After
    public void tearDown() throws Exception {
    }
}
