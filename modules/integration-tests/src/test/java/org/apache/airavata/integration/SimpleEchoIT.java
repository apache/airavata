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

package org.apache.airavata.integration;

import java.util.ArrayList;
import java.util.List;

import org.apache.airavata.client.tools.DocumentCreatorNew;
import org.apache.airavata.model.util.ExperimentModelUtil;
import org.apache.airavata.model.util.ProjectModelUtil;
import org.apache.airavata.model.workspace.Project;
import org.apache.airavata.model.workspace.experiment.ComputationalResourceScheduling;
import org.apache.airavata.model.workspace.experiment.DataObjectType;
import org.apache.airavata.model.workspace.experiment.DataType;
import org.apache.airavata.model.workspace.experiment.Experiment;
import org.apache.airavata.model.workspace.experiment.UserConfigurationData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class SimpleEchoIT extends SingleAppIntegrationTestBase {
    private final static Logger log = LoggerFactory.getLogger(SimpleEchoIT.class);

    public SimpleEchoIT() {
        //super();
    }

    @BeforeTest
    public void setUp() throws Exception {
//        Thread.sleep(20000);
        init();
    }

    @Test
    public void testSimpleLocalhostEchoService() throws Exception {
        log.info("Running job in localhost");
        log.info("========================");
        log.info("Adding applications...");
        DocumentCreatorNew documentCreatorNew = new DocumentCreatorNew(getClient());
        String hostAndappId = documentCreatorNew.createLocalHostDocs();
        String appId = hostAndappId.split(",")[1];
        
        List<DataObjectType> exInputs = new ArrayList<DataObjectType>();
        DataObjectType input = new DataObjectType();
        input.setKey("echo_input");
        input.setType(DataType.STRING);
        input.setValue("echo_output=Hello World");
        exInputs.add(input);

        List<DataObjectType> exOut = new ArrayList<DataObjectType>();
        DataObjectType output = new DataObjectType();
        output.setKey("echo_output");
        output.setType(DataType.STRING);
        output.setValue("");
        exOut.add(output);

        Project project = ProjectModelUtil.createProject("project1", "admin", "test project");
        String projectId = getClient().createProject(project);

        Experiment simpleExperiment =
                ExperimentModelUtil.createSimpleExperiment(projectId, "admin", "echoExperiment", appId, appId, exInputs);
        simpleExperiment.setExperimentOutputs(exOut);

        ComputationalResourceScheduling scheduling = ExperimentModelUtil.createComputationResourceScheduling("localhost", 1, 1, 1, "normal", 0, 0, 1, "sds128");
        scheduling.setResourceHostId("localhost");
        UserConfigurationData userConfigurationData = new UserConfigurationData();
        userConfigurationData.setAiravataAutoSchedule(false);
        userConfigurationData.setOverrideManualScheduledParams(false);
        userConfigurationData.setComputationalResourceScheduling(scheduling);
        simpleExperiment.setUserConfigurationData(userConfigurationData);

        log.info("Creating experiment...");

        final String expId = createExperiment(simpleExperiment);
        log.info("Experiment Id returned : " + expId);

        launchExperiment(expId);

        Thread.sleep(3000);
	log.info("Experiment launched successfully\n");
        log.info("Monitoring job in localhost");
        log.info("===========================");
        monitorJob(expId);
    }

}
