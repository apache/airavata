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
import org.apache.airavata.registry.core.experiment.catalog.resources.ExperimentExperimentCatResource;
import org.apache.airavata.registry.core.experiment.catalog.resources.ExperimentOutputExperimentCatResource;
import org.junit.After;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertTrue;


public class ExperimentOutputExperimentCatResourceTest extends AbstractResourceTest  {
    private ExperimentExperimentCatResource experimentResource;
    private String experimentID = "testExpID";
    ExperimentOutputExperimentCatResource outputResource;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        experimentResource = (ExperimentExperimentCatResource) getGatewayResource().create(ResourceType.EXPERIMENT);
        experimentResource.setExpID(experimentID);
        experimentResource.setExecutionUser(getWorkerResource().getUser());
        experimentResource.setProjectId(getProjectResource().getId());
        experimentResource.setCreationTime(getCurrentTimestamp());
        experimentResource.setApplicationId("testApplication");
        experimentResource.setApplicationVersion("1.0");
        experimentResource.setDescription("Test Application");
        experimentResource.setExpName("TestExperiment");
        experimentResource.save();

        outputResource = (ExperimentOutputExperimentCatResource)experimentResource.create(ResourceType.EXPERIMENT_OUTPUT);
        outputResource.setExperimentId(experimentResource.getExpID());
        outputResource.setExperimentKey("testKey");
        outputResource.setValue("testValue");
        outputResource.setDataType("string");
        outputResource.save();
    }

    @Test
    public void testSave() throws Exception {
        assertTrue("Experiment output saved successfully", experimentResource.isExists(ResourceType.EXPERIMENT_OUTPUT, experimentID));
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testGet () throws Exception {
        List<ExperimentOutputExperimentCatResource> outputs = experimentResource.getExperimentOutputs();
        System.out.println("output counts : " + outputs.size());
        assertTrue("Experiment output retrieved successfully...", outputs.size() > 0);
    }
}
