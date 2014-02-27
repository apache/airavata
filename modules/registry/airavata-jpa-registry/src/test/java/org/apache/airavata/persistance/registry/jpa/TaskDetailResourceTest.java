package org.apache.airavata.persistance.registry.jpa;

import static org.junit.Assert.*;

import java.sql.Timestamp;
import java.util.Date;

import org.apache.airavata.persistance.registry.jpa.resources.ExperimentResource;
import org.apache.airavata.persistance.registry.jpa.resources.TaskDetailResource;
import org.apache.airavata.persistance.registry.jpa.resources.WorkflowNodeDetailResource;
import org.junit.Before;
import org.junit.Test;

public class TaskDetailResourceTest extends AbstractResourceTest{
	
	   private ExperimentResource experimentResource;
	   private TaskDetailResource  taskDetailResource;
	   private WorkflowNodeDetailResource nodeDetailResource;
	   private String experimentID = "testExpID";
	   private String applicationID = "testAppID"; 
	   private String taskID = "testTask";
	   private String nodeID = "testNode";

	
	@Before
	public void setUp() throws Exception {
		super.setUp();
	    Timestamp creationTime = new Timestamp(new Date().getTime());
	    
	    experimentResource = (ExperimentResource) getGatewayResource().create(ResourceType.EXPERIMENT);
        experimentResource.setExpID(experimentID);
        experimentResource.setExecutionUser(getWorkerResource().getUser());
        experimentResource.setProject(getProjectResource());
        experimentResource.setCreationTime(creationTime);
        experimentResource.save();
        
        nodeDetailResource = (WorkflowNodeDetailResource) experimentResource.create(ResourceType.WORKFLOW_NODE_DETAIL);
        nodeDetailResource.setExperimentResource(experimentResource);
        nodeDetailResource.setNodeInstanceId(nodeID);
        nodeDetailResource.setNodeName(nodeID);
        nodeDetailResource.setCreationTime(creationTime);
        nodeDetailResource.save();
        
        taskDetailResource = (TaskDetailResource)nodeDetailResource.create(ResourceType.TASK_DETAIL);
        taskDetailResource.setWorkflowNodeDetailResource(nodeDetailResource);
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
