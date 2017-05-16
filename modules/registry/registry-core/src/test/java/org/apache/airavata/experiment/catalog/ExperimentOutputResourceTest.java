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
import org.apache.airavata.registry.core.experiment.catalog.resources.ExperimentOutputResource;
import org.apache.airavata.registry.core.experiment.catalog.resources.ExperimentResource;
import org.junit.After;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertTrue;


public class ExperimentOutputResourceTest extends AbstractResourceTest  {
    private ExperimentResource experimentResource;
    private String experimentID = "testExpID1";
    ExperimentOutputResource outputResource;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        experimentResource = (ExperimentResource) getGatewayResource().create(ResourceType.EXPERIMENT);
        experimentResource.setExperimentId(experimentID);
        experimentResource.setUserName(getWorkerResource().getUser());
        experimentResource.setProjectId(getProjectResource().getId());
        experimentResource.setCreationTime(getCurrentTimestamp());
        experimentResource.setExecutionId("1.0");
        experimentResource.setDescription("Test Application");
        experimentResource.setExperimentName("TestExperiment");
        experimentResource.save();

        outputResource = (ExperimentOutputResource)experimentResource.create(ResourceType.EXPERIMENT_OUTPUT);
        outputResource.setExperimentId(experimentResource.getExperimentId());
        outputResource.setOutputName("testKey");
        outputResource.setOutputValue("testValue");
        outputResource.setDataType("string");
        outputResource.save();
    }

    @Test
    public void testSave() throws Exception {
        assertTrue("Experiment output saved successfully", experimentResource.isExists(ResourceType.EXPERIMENT_OUTPUT, "testKey"));
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testGet () throws Exception {
        List<ExperimentOutputResource> outputs = experimentResource.getExperimentOutputs();
        System.out.println("output counts : " + outputs.size());
        assertTrue("Experiment output retrieved successfully...", outputs.size() > 0);
    }
}
