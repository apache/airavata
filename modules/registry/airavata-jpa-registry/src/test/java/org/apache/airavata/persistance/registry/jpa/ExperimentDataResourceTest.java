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
import org.apache.airavata.persistance.registry.jpa.util.Initialize;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.Calendar;

public class ExperimentDataResourceTest extends TestCase {
    private ExperimentDataResource experimentDataResource;
    private GatewayResource gatewayResource;
    private ExperimentResource experimentResource;
    private WorkerResource workerResource;
    private WorkflowDataResource workflowDataResource;
    private ExperimentMetadataResource experimentMetadataResource;

    private Initialize initialize ;
    @Override
    public void setUp() throws Exception {
        initialize = new Initialize();
        initialize.initializeDB();
        gatewayResource = (GatewayResource)ResourceUtils.getGateway("gateway1");
        workerResource = (WorkerResource)ResourceUtils.getWorker(gatewayResource.getGatewayName(), "testUser");

        experimentResource = (ExperimentResource)gatewayResource.create(ResourceType.EXPERIMENT);
        experimentResource.setExpID("testExpID");
        experimentResource.setWorker(workerResource);
        experimentResource.setProject(new ProjectResource(workerResource, gatewayResource, "testProject"));
        experimentResource.save();

        experimentDataResource = (ExperimentDataResource)experimentResource.create(ResourceType.EXPERIMENT_DATA);
        experimentDataResource.setExpName("exp1");
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
        java.util.Date d =  calender.getTime();
        Timestamp currentTime = new Timestamp(d.getTime());

        workflowDataResource.setLastUpdatedTime(currentTime);
        workflowDataResource.setStartTime(currentTime);
        workflowDataResource.save();

//        super.setUp();
    }

    public void testCreate() throws Exception {
        WorkflowDataResource resource = (WorkflowDataResource)experimentDataResource.create(ResourceType.WORKFLOW_DATA);
//        resource.setTemplateName("testTemplate");
//        resource.setWorkflowInstanceID("testWFInstanceID");
//        Calendar calender = Calendar.getInstance();
//        java.util.Date d =  calender.getTime();
//        Timestamp currentTime = new Timestamp(d.getTime());
//        resource.setLastUpdatedTime(currentTime);
//        resource.save();

        ExperimentMetadataResource metadataResource = (ExperimentMetadataResource)experimentDataResource.create(ResourceType.EXPERIMENT_METADATA);
//        metadataResource.setMetadata("testmetadata");
//        metadataResource.save();

        assertNotNull("workflowdata resource created", resource);
        assertNotNull("experimemt metadata resource created" , metadataResource);
    }

    public void testGet() throws Exception {
        WorkflowDataResource workflowDataResource = experimentDataResource.getWorkflowInstance("testWorkflowInstance");
        assertNotNull("workflow data retrieved successfully", workflowDataResource);

        ExperimentMetadataResource experimentMetadataResource = experimentDataResource.getExperimentMetadata();
        assertNotNull("experiment meta data retrieved successfully", experimentMetadataResource);
    }

    public void testGetList() throws Exception {
        assertNotNull("workflow data retrieved successfully" ,experimentDataResource.get(ResourceType.WORKFLOW_DATA));
        assertNotNull("experiment meta data retrieved successfully", experimentDataResource.get(ResourceType.EXPERIMENT_METADATA));
    }

    public void testSave() throws Exception {
        experimentDataResource.save();

        if (experimentResource.isExists(ResourceType.EXPERIMENT_DATA, "testExpID")){
            assertTrue("experiment data saved successfully", true);
        }
        //remove experiment data
        experimentResource.remove(ResourceType.EXPERIMENT_DATA, "testExpID");
    }

    public void testRemove() throws Exception {
        experimentDataResource.remove(ResourceType.WORKFLOW_DATA, "testWFInstanceID");
        if(!experimentResource.isExists(ResourceType.EXPERIMENT_DATA, "testWFInstanceID")) {
            assertTrue("workflow data resource removed successfully", true);
        }

        experimentDataResource.remove(ResourceType.EXPERIMENT_METADATA, "testExpID");
        if (! experimentDataResource.isExists(ResourceType.EXPERIMENT_METADATA, "testExpID")) {
            assertTrue("experiment meta data resource removed successfully", true);
        }
    }

    @Override
    protected void tearDown() throws Exception {
        initialize.stopDerbyServer();
//        super.tearDown();    //To change body of overridden methods use File | Settings | File Templates.
    }



}
