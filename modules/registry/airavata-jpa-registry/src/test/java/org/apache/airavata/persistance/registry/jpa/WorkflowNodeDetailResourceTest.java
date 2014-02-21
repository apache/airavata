package org.apache.airavata.persistance.registry.jpa;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.sql.Timestamp;
import java.util.Date;

import org.apache.airavata.persistance.registry.jpa.resources.ExperimentResource;
import org.apache.airavata.persistance.registry.jpa.resources.WorkflowNodeDetailResource;
import org.junit.Before;
import org.junit.Test;

public class WorkflowNodeDetailResourceTest extends AbstractResourceTest {

	private ExperimentResource experimentResource;
	private WorkflowNodeDetailResource nodeDetailResource;
	private String experimentID = "testExpID";
	private String applicationID = "testAppID";
	private String nodeID = "testNode";

	@Before
	public void setUp() throws Exception {
		super.setUp();
		Timestamp creationTime = new Timestamp(new Date().getTime());

		experimentResource = (ExperimentResource) getGatewayResource().create(ResourceType.EXPERIMENT);
		experimentResource.setExpID(experimentID);
		experimentResource.setWorker(getWorkerResource());
		experimentResource.setProject(getProjectResource());
		experimentResource.setCreationTime(creationTime);
		experimentResource.setApplicationId(applicationID);
		experimentResource.save();

		nodeDetailResource = (WorkflowNodeDetailResource) experimentResource.create(ResourceType.WORKFLOW_NODE_DETAIL);
		nodeDetailResource.setExperimentResource(experimentResource);
		nodeDetailResource.setNodeInstanceId(nodeID);
		nodeDetailResource.setNodeName(nodeID);
		nodeDetailResource.setCreationTime(creationTime);
		nodeDetailResource.save();

	}

	@Test
	public void testCreate() throws Exception {
		assertNotNull("task data resource has being created ", nodeDetailResource);
	}

	@Test
	public void testSave() throws Exception {
		assertTrue("task save successfully", experimentResource.isExists(ResourceType.WORKFLOW_NODE_DETAIL, nodeID));
	}

	@Test
	public void testGet() throws Exception {
		assertNotNull("task data retrieved successfully", experimentResource.get(ResourceType.WORKFLOW_NODE_DETAIL, nodeID));
	}

	@Test
	public void testRemove() throws Exception {
		experimentResource.remove(ResourceType.WORKFLOW_NODE_DETAIL, nodeID);
		assertFalse("task data removed successfully", experimentResource.isExists(ResourceType.WORKFLOW_NODE_DETAIL, nodeID));
	}

}
