/**
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
 */

package org.apache.airavata.registry.core.experiment.catalog;

import static org.junit.Assert.*;

import java.sql.Timestamp;
import java.util.Date;

import org.apache.airavata.registry.core.experiment.catalog.ResourceType;
import org.apache.airavata.registry.core.experiment.catalog.resources.ExperimentExperimentCatResource;
import org.apache.airavata.registry.core.experiment.catalog.resources.TaskDetailExperimentCatResource;
import org.apache.airavata.registry.core.experiment.catalog.resources.WorkflowNodeDetailExperimentCatResource;
import org.junit.Before;
import org.junit.Test;

public class TaskDetailExperimentCatResourceTest extends AbstractResourceTest{
	
	   private ExperimentExperimentCatResource experimentResource;
	   private TaskDetailExperimentCatResource taskDetailResource;
	   private WorkflowNodeDetailExperimentCatResource nodeDetailResource;
	   private String experimentID = "testExpID";
	   private String applicationID = "testAppID"; 
	   private String taskID = "testTask";
	   private String nodeID = "testNode";

	
	@Before
	public void setUp() throws Exception {
		super.setUp();
	    Timestamp creationTime = new Timestamp(new Date().getTime());
	    
	    experimentResource = (ExperimentExperimentCatResource) getGatewayResource().create(ResourceType.EXPERIMENT);
        experimentResource.setExpID(experimentID);
        experimentResource.setExecutionUser(getWorkerResource().getUser());
        experimentResource.setProjectId(getProjectResource().getId());
        experimentResource.setCreationTime(creationTime);
        experimentResource.save();
        
        nodeDetailResource = (WorkflowNodeDetailExperimentCatResource) experimentResource.create(ResourceType.WORKFLOW_NODE_DETAIL);
        nodeDetailResource.setExperimentId(experimentResource.getExpID());
        nodeDetailResource.setNodeInstanceId(nodeID);
        nodeDetailResource.setNodeName(nodeID);
        nodeDetailResource.setCreationTime(creationTime);
        nodeDetailResource.save();
        
        taskDetailResource = (TaskDetailExperimentCatResource)nodeDetailResource.create(ResourceType.TASK_DETAIL);
        taskDetailResource.setNodeId(nodeDetailResource.getNodeInstanceId());
        taskDetailResource.setTaskId(taskID);
        taskDetailResource.setApplicationId(applicationID);
        taskDetailResource.setApplicationVersion("1.0");
        taskDetailResource.setCreationTime(creationTime);
        taskDetailResource.save();
    }
	

	@Test
    public void testCreate() throws Exception {
    	assertNotNull("task data resource has being created ", taskDetailResource);
    }
    
    @Test
    public void testSave() throws Exception {
        assertTrue("task save successfully", nodeDetailResource.isExists(ResourceType.TASK_DETAIL, taskID));
    }
    
    @Test
    public void testGet() throws Exception {
        assertNotNull("task data retrieved successfully", nodeDetailResource.get(ResourceType.TASK_DETAIL, taskID));
    }

    @Test
    public void testRemove() throws Exception {
    	nodeDetailResource.remove(ResourceType.TASK_DETAIL, taskID);
    	assertFalse("task data removed successfully", nodeDetailResource.isExists(ResourceType.TASK_DETAIL, taskID));        
    }
}
