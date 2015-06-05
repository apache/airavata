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

package org.apache.airavata.registry.core.experiment.catalog;

import org.apache.airavata.registry.core.experiment.catalog.ResourceType;
import org.apache.airavata.registry.core.experiment.catalog.resources.ComputationSchedulingExperimentCatResource;
import org.apache.airavata.registry.core.experiment.catalog.resources.ExperimentExperimentCatResource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.Timestamp;
import java.util.Date;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ComputationalSchedulingTest extends AbstractResourceTest {
    private ExperimentExperimentCatResource experimentResource;
    private ComputationSchedulingExperimentCatResource schedulingResource;
    private String experimentID = "testExpID";

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        experimentResource = (ExperimentExperimentCatResource) getGatewayResource().create(ResourceType.EXPERIMENT);
        experimentResource.setExpID(experimentID);
        experimentResource.setExecutionUser(getWorkerResource().getUser());
        experimentResource.setProjectId(getProjectResource().getId());
        Timestamp currentDate = new Timestamp(new Date().getTime());
        experimentResource.setCreationTime(currentDate);
        experimentResource.setApplicationId("testApplication");
        experimentResource.setApplicationVersion("1.0");
        experimentResource.setDescription("Test Application");
        experimentResource.setExpName("TestExperiment");
        experimentResource.save();

        schedulingResource = (ComputationSchedulingExperimentCatResource)experimentResource.create(ResourceType.COMPUTATIONAL_RESOURCE_SCHEDULING);
        schedulingResource.setResourceHostId("testResource");
        schedulingResource.setCpuCount(10);
        schedulingResource.setNodeCount(5);
        schedulingResource.setPhysicalMemory(1000);
        schedulingResource.setProjectName("project1");
        schedulingResource.setQueueName("testQueue");
        schedulingResource.save();
        System.out.println("scheduling id : " + schedulingResource.getSchedulingId());
    }


    @Test
    public void testSave() throws Exception {
        assertTrue("Computational schedule successfully", experimentResource.isExists(ResourceType.COMPUTATIONAL_RESOURCE_SCHEDULING, experimentID));
    }

    @Test
    public void testRemove() throws Exception {
        experimentResource.remove(ResourceType.COMPUTATIONAL_RESOURCE_SCHEDULING, experimentID);
        assertFalse("Computational schedule removed successfully", experimentResource.isExists(ResourceType.COMPUTATIONAL_RESOURCE_SCHEDULING, experimentID));
    }

    @After
    public void tearDown() throws Exception {
    }
}
