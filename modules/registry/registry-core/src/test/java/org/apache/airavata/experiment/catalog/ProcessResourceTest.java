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

import org.apache.airavata.registry.core.experiment.catalog.ResourceType;
import org.apache.airavata.registry.core.experiment.catalog.resources.ExperimentResource;
import org.apache.airavata.registry.core.experiment.catalog.resources.ProcessResource;
import org.junit.Before;
import org.junit.Test;

import java.sql.Timestamp;
import java.util.Date;

import static org.junit.Assert.*;

public class ProcessResourceTest extends AbstractResourceTest{
	
	   private ExperimentResource experimentResource;
	   private ProcessResource processResource;
	   private String experimentID = "testExpID5";
	   private String processId = "processID";

	
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
    }
	

	@Test
    public void testCreate() throws Exception {
    	assertNotNull("process data resource has being created ", processResource);
    }
    
    @Test
    public void testSave() throws Exception {
        assertTrue("process save successfully", experimentResource.isExists(ResourceType.PROCESS, processId));
    }
    
    @Test
    public void testGet() throws Exception {
        assertNotNull("process data retrieved successfully", experimentResource.get(ResourceType.PROCESS, processId));
    }

    @Test
    public void testRemove() throws Exception {
    	experimentResource.remove(ResourceType.PROCESS, processId);
    	assertFalse("process data removed successfully", experimentResource.isExists(ResourceType.PROCESS, processId));
    }
}
