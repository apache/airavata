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

import java.sql.Timestamp;
import java.util.Calendar;

public class WorkflowDataResourceTest extends TestCase {
    private GatewayResource gatewayResource;
    private ExperimentResource experimentResource;
    private WorkerResource workerResource;
    private ExperimentDataResource experimentDataResource;
    private WorkflowDataResource workflowDataResource;

    @Override
    public void setUp() throws Exception {
        gatewayResource = (GatewayResource)ResourceUtils.getGateway("gateway1");
        workerResource = (WorkerResource)ResourceUtils.getWorker(gatewayResource.getGatewayName(), "testUser");

        experimentResource = (ExperimentResource)gatewayResource.create(ResourceType.EXPERIMENT);
        experimentResource.setExpID("testExpID");
        experimentResource.setWorker(workerResource);

        experimentDataResource = (ExperimentDataResource)experimentResource.create(ResourceType.EXPERIMENT_DATA);
        experimentDataResource.setExpName("testExp");
        experimentDataResource.setUserName(workerResource.getUser());
        experimentDataResource.save();

        workflowDataResource = (WorkflowDataResource)experimentDataResource.create(ResourceType.WORKFLOW_DATA);
        workflowDataResource.setWorkflowInstanceID("testWFInstance");
        workflowDataResource.setTemplateName("testTemplate");
        workflowDataResource.setExperimentID("testExpID");
        Calendar calender = Calendar.getInstance();
        java.util.Date d =  calender.getTime();
        Timestamp timestamp = new Timestamp(d.getTime());
        workflowDataResource.setLastUpdatedTime(timestamp);

        super.setUp();
    }

    public void testCreate() throws Exception {
        NodeDataResource nodeDataResource = workflowDataResource.createNodeData("testNodeID");
        GramDataResource gramDataResource = workflowDataResource.createGramData("testNodeID");

        nodeDataResource.setWorkflowDataResource(workflowDataResource);
        nodeDataResource.setInputs("testInput");
        nodeDataResource.setOutputs("testOutput");
        nodeDataResource.setStatus("testStatus");
        nodeDataResource.save();

        gramDataResource.setRsl("testRSL");
        gramDataResource.setWorkflowDataResource(workflowDataResource);
        gramDataResource.save();

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
        if(!workflowDataResource.isNodeExists("testNodeID")){
           assertTrue("node date removed successfully", true);
        }
        if(!workflowDataResource.isGramDataExists("testNodeID")){
            assertTrue("gram date removed successfully", true);
        }
    }

    public void testSave() throws Exception {
        workflowDataResource.save();
        if(experimentDataResource.isWorkflowInstancePresent("testWFInstance")){
            assertTrue("workflow data saved successfully", true);
        }
    }




}
