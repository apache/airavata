package org.apache.airavata.persistance.registry.jpa;

import java.util.UUID;

import org.apache.airavata.common.utils.AiravataJobState;
import org.apache.airavata.persistance.registry.jpa.resources.GatewayResource;
import org.apache.airavata.persistance.registry.jpa.resources.OrchestratorDataResource;

public class OrchestratorDataResourceTest extends AbstractResourceTest{
	 private OrchestratorDataResource dataResource;
	 private String experimentID = UUID.randomUUID().toString();
	
	 @Override
	    public void setUp() throws Exception {
	        super.setUp();
	        GatewayResource gatewayResource = super.getGatewayResource();
		    dataResource = (OrchestratorDataResource) gatewayResource.create(ResourceType.ORCHESTRATOR_DATA);
	      
	   }

	    public void testSave() throws Exception {
	        dataResource.setExperimentID(experimentID);
	        dataResource.setStatus(AiravataJobState.State.CREATED.toString());
	        dataResource.save();
	        assertNotNull("Orchestrator data resource created successfully", dataResource);
	    }

	    public void testGet() throws Exception {
//	        dataResource.get(ResourceType.ORCHESTRATOR_DATA, experimentID);
//	        assertNotNull("Orchestrator data resource created successfully", dataResource);
	    }
	    @Override
	    public void tearDown() throws Exception {
	        super.tearDown();
	    }


}
