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

public class WorkflowDataResourceTest extends AbstractResourceTest {
    private ExperimentDataResource experimentDataResource;
    private WorkflowDataResource workflowDataResource;
    private NodeDataResource nodeDataResource;
    private GramDataResource gramDataResource;

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

        experimentDataResource = (ExperimentDataResource) experimentResource.create(ResourceType.EXPERIMENT_DATA);
        experimentDataResource.setExpName("testExpID");
        experimentDataResource.setUserName(workerResource.getUser());
        experimentDataResource.save();

        workflowDataResource = (WorkflowDataResource) experimentDataResource.create(ResourceType.WORKFLOW_DATA);
        workflowDataResource.setWorkflowInstanceID("testWFInstance");
        workflowDataResource.setTemplateName("testTemplate");
        workflowDataResource.setExperimentID("testExpID");
        Calendar calender = Calendar.getInstance();
        java.util.Date d = calender.getTime();
        Timestamp timestamp = new Timestamp(d.getTime());
        workflowDataResource.setLastUpdatedTime(timestamp);
        workflowDataResource.save();

        nodeDataResource = workflowDataResource.createNodeData("testNodeID");
        gramDataResource = workflowDataResource.createGramData("testNodeID");

        nodeDataResource.setWorkflowDataResource(workflowDataResource);
        nodeDataResource.setInputs("testInput");
        nodeDataResource.setOutputs("testOutput");
        nodeDataResource.setStatus("testStatus");
        nodeDataResource.save();

        gramDataResource.setRsl("testRSL");
        gramDataResource.setWorkflowDataResource(workflowDataResource);
        gramDataResource.save();
    }

    public void testCreate() throws Exception {
        assertNotNull("node data resource created successfully", nodeDataResource);
        assertNotNull("gram data resource created successfully", gramDataResource);
    }

    public void testGet() throws Exception {
        assertNotNull("Node data retrieved successfully", workflowDataResource.getNodeData("testNodeID"));
        assertNotNull("Gram data retrieved successfully", workflowDataResource.getGramData("testNodeID"));
    }

    public void testGetList() throws Exception {
        assertNotNull("Node data retrieved successfully", workflowDataResource.getNodeData());
        assertNotNull("Gram data retrieved successfully", workflowDataResource.getGramData());
    }

    public void testRemove() throws Exception {
        workflowDataResource.removeNodeData("testNodeID");
        workflowDataResource.removeGramData("testNodeID");

        assertTrue("node date removed successfully", !workflowDataResource.isNodeExists("testNodeID"));
        assertTrue("gram date removed successfully", !workflowDataResource.isGramDataExists("testNodeID"));

    }

    public void testSave() throws Exception {
        assertTrue("workflow data saved successfully", experimentDataResource.isWorkflowInstancePresent("testWFInstance"));
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }


}
