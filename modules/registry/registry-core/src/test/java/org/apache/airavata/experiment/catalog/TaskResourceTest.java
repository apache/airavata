/**
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
 */
package org.apache.airavata.experiment.catalog;

import org.apache.airavata.model.task.DataStageType;
import org.apache.airavata.model.task.DataStagingTaskModel;
import org.apache.airavata.model.task.TaskTypes;
import org.apache.airavata.registry.core.experiment.catalog.ResourceType;
import org.apache.airavata.registry.core.experiment.catalog.resources.ExperimentResource;
import org.apache.airavata.registry.core.experiment.catalog.resources.ProcessResource;
import org.apache.airavata.registry.core.experiment.catalog.resources.TaskResource;
import org.junit.Before;
import org.junit.Test;

import java.sql.Timestamp;
import java.util.Date;

import static org.junit.Assert.*;

public class TaskResourceTest extends AbstractResourceTest{
	
	   private ExperimentResource experimentResource;
	   private ProcessResource processResource;
	   private TaskResource taskResource;
       private String experimentID = "testExpID6";
	   private String processId = "processID";
       private String taskId = "taskID";

	
	@Before
	public void setUp() throws Exception {
		super.setUp();
	    Timestamp creationTime = new Timestamp(new Date().getTime());
	    
	    experimentResource = (ExperimentResource) getGatewayResource().create(ResourceType.EXPERIMENT);
        experimentResource.setExperimentId(experimentID);
        experimentResource.setExperimentName(experimentID);
        experimentResource.setUserName(getWorkerResource().getUser());
        experimentResource.setProjectId(getProjectResource().getId());
        experimentResource.setCreationTime(creationTime);
        experimentResource.save();

        processResource = (ProcessResource)experimentResource.create(ResourceType.PROCESS);
        processResource.setProcessId(processId);
        processResource.setExperimentId(experimentID);
        processResource.setCreationTime(creationTime);
        processResource.save();

        taskResource = (TaskResource)processResource.create(ResourceType.TASK);
        taskResource.setTaskId(taskId);
        taskResource.setParentProcessId(processId);
        taskResource.setTaskType(TaskTypes.DATA_STAGING.toString());
        taskResource.setTaskDetail("task detail");
        taskResource.setSubTaskModel(new DataStagingTaskModel("source","destination", DataStageType.INPUT).toString().getBytes());
        taskResource.save();
    }
	

	@Test
    public void testCreate() throws Exception {
    	assertNotNull("task data resource has being created ", taskResource);
    }
    
    @Test
    public void testSave() throws Exception {
        assertTrue("task save successfully", processResource.isExists(ResourceType.TASK, taskId));
    }
    
    @Test
    public void testGet() throws Exception {
        assertNotNull("task data retrieved successfully", processResource.get(ResourceType.TASK, taskId));
    }

    @Test
    public void testRemove() throws Exception {
    	processResource.remove(ResourceType.TASK, taskId);
    	assertFalse("task data removed successfully", processResource.isExists(ResourceType.TASK, taskId));
    }
}
