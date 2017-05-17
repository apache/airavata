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

package org.apache.airavata.experiment.catalog;

import org.apache.airavata.registry.core.experiment.catalog.ResourceType;
import org.apache.airavata.registry.core.experiment.catalog.resources.ExperimentResource;
import org.junit.After;
import org.junit.Test;

import java.sql.Timestamp;
import java.util.Date;

import static org.junit.Assert.*;

public class ExperimentResourceTest extends AbstractResourceTest {
    private ExperimentResource experimentResource;
    private String experimentID = "testExpID";

    @Override
    public void setUp() throws Exception {
    	super.setUp();
        experimentResource = (ExperimentResource) getGatewayResource().create(ResourceType.EXPERIMENT);
        experimentResource.setExperimentId(experimentID);
        experimentResource.setUserName(getWorkerResource().getUser());
        experimentResource.setProjectId(getProjectResource().getId());
        Timestamp currentDate = new Timestamp(new Date().getTime());
        experimentResource.setCreationTime(currentDate);
        experimentResource.setExecutionId("1.0");
        experimentResource.setDescription("Test Application");
        experimentResource.setExperimentName("TestExperiment");
    	experimentResource.save();
    }
    
    @Test
    public void testCreate() throws Exception {
    	assertNotNull("experiment data resource has being created ", experimentResource);
    }
    
    @Test
    public void testSave() throws Exception {
        assertTrue("experiment save successfully", getGatewayResource().isExists(ResourceType.EXPERIMENT, experimentID));
    }
    
    @Test
    public void testGet() throws Exception {
        assertNotNull("experiment data retrieved successfully", getGatewayResource().get(ResourceType.EXPERIMENT, experimentID));
    }

    @Test
    public void testRemove() throws Exception {
    	getGatewayResource().remove(ResourceType.EXPERIMENT, experimentID);
    	assertFalse("experiment data removed successfully", getGatewayResource().isExists(ResourceType.EXPERIMENT, experimentID));        
    }

    @After
    public void tearDown() throws Exception {
    }
}
