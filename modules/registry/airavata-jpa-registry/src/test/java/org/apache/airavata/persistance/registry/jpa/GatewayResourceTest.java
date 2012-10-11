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

import junit.framework.TestCase;
import org.apache.airavata.persistance.registry.jpa.resources.*;

import java.sql.Date;
import java.util.Calendar;


public class GatewayResourceTest extends TestCase {
    private GatewayResource gatewayResource;
    private ProjectResource projectResource;
    private UserResource userResource;
    private WorkerResource workerResource;

    @Override
    public void setUp() throws Exception {
        gatewayResource = (GatewayResource)ResourceUtils.createGateway("gateway1");
        if(gatewayResource == null){
            gatewayResource = (GatewayResource)ResourceUtils.getGateway("gateway1");
        }
        projectResource = (ProjectResource)gatewayResource.create(ResourceType.PROJECT);
        userResource = (UserResource)gatewayResource.create(ResourceType.USER);
        workerResource = (WorkerResource)gatewayResource.create(ResourceType.GATEWAY_WORKER);

        userResource.setUserName("testUser");
        userResource.setPassword("testUser");
        userResource.save();

        workerResource.setUser("testUser");
        workerResource.save();

        projectResource.setName("testProject");
        projectResource.setWorker(workerResource);
        projectResource.save();
    }

     @org.junit.Test
    public void testSave() throws Exception {
        gatewayResource.setOwner("owner1");
        gatewayResource.save();

        boolean gatewayExist = ResourceUtils.isGatewayExist("gateway1");
        if(gatewayExist){
            assertTrue("The gateway exisits", gatewayExist);
        }

    }

    @org.junit.Test
    public void testCreate() throws Exception {

        PublishWorkflowResource publishWorkflowResource = (PublishWorkflowResource)gatewayResource.create(ResourceType.PUBLISHED_WORKFLOW);
        UserWorkflowResource userWorkflowResource = (UserWorkflowResource)gatewayResource.create(ResourceType.USER_WORKFLOW);
        HostDescriptorResource hostDescriptorResource = (HostDescriptorResource)gatewayResource.create(ResourceType.HOST_DESCRIPTOR);
        ServiceDescriptorResource serviceDescriptorResource = (ServiceDescriptorResource)gatewayResource.create(ResourceType.SERVICE_DESCRIPTOR);
        ApplicationDescriptorResource applicationDescriptorResource = (ApplicationDescriptorResource)gatewayResource.create(ResourceType.APPLICATION_DESCRIPTOR);
        ExperimentResource experimentResource = (ExperimentResource)gatewayResource.create(ResourceType.EXPERIMENT);

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

        Calendar calender = Calendar.getInstance();
        java.util.Date d =  calender.getTime();
        Date currentTime = new Date(d.getTime());
        userWorkflowResource.setName("workflow1");
        userWorkflowResource.setLastUpdateDate(currentTime);
        userWorkflowResource.setWorker(workerResource);
        userWorkflowResource.setContent("testContent");
        userWorkflowResource.save();

        publishWorkflowResource.setName("pubworkflow1");
        publishWorkflowResource.setCreatedUser("testUser");
        publishWorkflowResource.setContent("testContent");
        Calendar c = Calendar.getInstance();
        java.util.Date da =  c.getTime();
        Date time = new Date(da.getTime());
        publishWorkflowResource.setPublishedDate(time);
        publishWorkflowResource.save();

        experimentResource.setExpID("testExpID");
        experimentResource.setProject(projectResource);
        experimentResource.setWorker(workerResource);
        experimentResource.setSubmittedDate(currentTime);
        experimentResource.save();

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

    @org.junit.Test
    public void testIsExists() throws Exception {
        assertTrue(gatewayResource.isExists(ResourceType.USER, "testUser"));
        assertTrue(gatewayResource.isExists(ResourceType.PUBLISHED_WORKFLOW, "pubworkflow1"));
        assertTrue(gatewayResource.isExists(ResourceType.HOST_DESCRIPTOR, "testHostDesc"));
        assertTrue(gatewayResource.isExists(ResourceType.SERVICE_DESCRIPTOR, "testServiceDesc"));
        assertTrue(gatewayResource.isExists(ResourceType.APPLICATION_DESCRIPTOR, "testAppDesc"));
        assertTrue(gatewayResource.isExists(ResourceType.EXPERIMENT, "testExpID"));
    }

    @org.junit.Test
    public void testGet() throws Exception {
        assertNotNull(gatewayResource.get(ResourceType.USER, "testUser"));
        assertNotNull(gatewayResource.get(ResourceType.PUBLISHED_WORKFLOW, "pubworkflow1"));
        assertNotNull(gatewayResource.get(ResourceType.HOST_DESCRIPTOR, "testHostDesc"));
        assertNotNull(gatewayResource.get(ResourceType.SERVICE_DESCRIPTOR, "testServiceDesc"));
        assertNotNull(gatewayResource.get(ResourceType.APPLICATION_DESCRIPTOR, "testAppDesc"));
        assertNotNull(gatewayResource.get(ResourceType.EXPERIMENT, "testExpID"));
    }

    public void testGetList() throws Exception {
        assertNotNull(gatewayResource.get(ResourceType.GATEWAY_WORKER));
        assertNotNull(gatewayResource.get(ResourceType.PUBLISHED_WORKFLOW));
        assertNotNull(gatewayResource.get(ResourceType.HOST_DESCRIPTOR));
        assertNotNull(gatewayResource.get(ResourceType.SERVICE_DESCRIPTOR));
        assertNotNull(gatewayResource.get(ResourceType.APPLICATION_DESCRIPTOR));
        assertNotNull(gatewayResource.get(ResourceType.EXPERIMENT));
        assertNotNull(gatewayResource.get(ResourceType.PROJECT));
        assertNotNull(gatewayResource.get(ResourceType.GATEWAY_WORKER));
    }

    @org.junit.Test
    public void testRemove() throws Exception {

        gatewayResource.remove(ResourceType.PUBLISHED_WORKFLOW, "pubworkflow1");
        boolean exists = gatewayResource.isExists(ResourceType.PUBLISHED_WORKFLOW, "pubworkflow1");
        assertFalse(exists);

        gatewayResource.remove(ResourceType.HOST_DESCRIPTOR, "testHostDesc");
        assertFalse(gatewayResource.isExists(ResourceType.HOST_DESCRIPTOR, "testHostDesc"));

        gatewayResource.remove(ResourceType.SERVICE_DESCRIPTOR, "testServiceDesc");
        assertFalse(gatewayResource.isExists(ResourceType.SERVICE_DESCRIPTOR, "testServiceDesc"));

        gatewayResource.remove(ResourceType.EXPERIMENT, "testExpID");
        assertFalse(gatewayResource.isExists(ResourceType.EXPERIMENT, "testExpID"));

        gatewayResource.remove(ResourceType.APPLICATION_DESCRIPTOR, "testAppDesc");
        assertFalse(gatewayResource.isExists(ResourceType.APPLICATION_DESCRIPTOR, "testAppDesc"));

        PublishWorkflowResource publishWorkflowResource = (PublishWorkflowResource)gatewayResource.create(ResourceType.PUBLISHED_WORKFLOW);
        HostDescriptorResource hostDescriptorResource = (HostDescriptorResource)gatewayResource.create(ResourceType.HOST_DESCRIPTOR);
        ServiceDescriptorResource serviceDescriptorResource = (ServiceDescriptorResource)gatewayResource.create(ResourceType.SERVICE_DESCRIPTOR);
        ApplicationDescriptorResource applicationDescriptorResource = (ApplicationDescriptorResource)gatewayResource.create(ResourceType.APPLICATION_DESCRIPTOR);
        ExperimentResource experimentResource = (ExperimentResource)gatewayResource.create(ResourceType.EXPERIMENT);

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

        Calendar calender = Calendar.getInstance();
        java.util.Date d =  calender.getTime();
        Date currentTime = new Date(d.getTime());

        publishWorkflowResource.setName("pubworkflow1");
        publishWorkflowResource.setCreatedUser("testUser");
        publishWorkflowResource.setContent("testContent");
        Calendar c = Calendar.getInstance();
        java.util.Date da =  c.getTime();
        Date time = new Date(da.getTime());
        publishWorkflowResource.setPublishedDate(time);
        publishWorkflowResource.save();

        experimentResource.setExpID("testExpID");
        experimentResource.setProject(projectResource);
        experimentResource.setWorker(workerResource);
        experimentResource.setSubmittedDate(currentTime);
        experimentResource.save();

    }
}
