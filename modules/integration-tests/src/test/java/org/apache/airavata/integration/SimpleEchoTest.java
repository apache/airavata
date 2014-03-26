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

import org.apache.airavata.api.Airavata;
import org.apache.airavata.api.client.AiravataClientFactory;
import org.apache.airavata.api.error.AiravataClientException;
import org.apache.airavata.api.error.AiravataSystemException;
import org.apache.airavata.api.error.InvalidRequestException;
import org.apache.airavata.client.AiravataAPIFactory;
import org.apache.airavata.client.api.AiravataAPI;
import org.apache.airavata.client.api.exception.AiravataAPIInvocationException;
import org.apache.airavata.client.tools.DocumentCreator;
import org.apache.airavata.model.util.ExperimentModelUtil;
import org.apache.airavata.model.workspace.experiment.*;
import org.apache.airavata.schemas.gfac.DataType;
import org.apache.airavata.server.ServerMain;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SimpleEchoTest extends BaseCaseIT {
    public static final String THRIFT_SERVER_HOST = "localhost";
    public static final int THRIFT_SERVER_PORT = 8930;
    private final static Logger logger = LoggerFactory.getLogger(SimpleEchoTest.class);
    private static final String DEFAULT_USER = "defauly.registry.user";
    private static final String DEFAULT_GATEWAY = "default.registry.gateway";

    public SimpleEchoTest() throws Exception {
        //super();
    }
    public AiravataAPI getAiravataAPI() throws AiravataAPIInvocationException {
        if (airavataAPI == null){
            airavataAPI = AiravataAPIFactory.getAPI("default", "admin");
        }
        return airavataAPI;
    }

    @BeforeTest
    public void setUp() throws Exception {
        new Thread() {
            public void run() {
                try {
                    ServerMain.main(new String[]{});
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
        Thread.sleep(10000);
        this.airavataAPI = getAiravataAPI();
        this.client = AiravataClientFactory.createAiravataClient(THRIFT_SERVER_HOST, THRIFT_SERVER_PORT);

    }

    @Test
    public void testSimpleLocalhostEchoService() throws Exception {
        log.info("Running job in localhost...");
        DocumentCreator documentCreator = new DocumentCreator(airavataAPI);
        documentCreator.createLocalHostDocs();

        List<DataObjectType> exInputs = new ArrayList<DataObjectType>();
        DataObjectType input = new DataObjectType();
        input.setKey("echo_input");
        input.setType(DataType.STRING.toString());
        input.setValue("echo_output=Hello World");
        exInputs.add(input);

        List<DataObjectType> exOut = new ArrayList<DataObjectType>();
        DataObjectType output = new DataObjectType();
        output.setKey("echo_output");
        output.setType(DataType.STRING.toString());
        output.setValue("");
        exOut.add(output);

        Experiment simpleExperiment =
                ExperimentModelUtil.createSimpleExperiment("project1", "admin", "echoExperiment", "SimpleEcho0", "SimpleEcho0", exInputs);
        simpleExperiment.setExperimentOutputs(exOut);

        ComputationalResourceScheduling scheduling = ExperimentModelUtil.createComputationResourceScheduling("localhost", 1, 1, 1, "normal", 0, 0, 1, "sds128");
        scheduling.setResourceHostId("localhost");
        UserConfigurationData userConfigurationData = new UserConfigurationData();
        userConfigurationData.setAiravataAutoSchedule(false);
        userConfigurationData.setOverrideManualScheduledParams(false);
        userConfigurationData.setComputationalResourceScheduling(scheduling);
        simpleExperiment.setUserConfigurationData(userConfigurationData);


        final String expId = createExperiment(simpleExperiment);
        System.out.println("Experiment Id returned : " + expId);

        log.info("Experiment Id returned : " + expId);

        launchExperiment(expId);

        System.out.println("Launched successfully");

        Thread monitor = (new Thread() {
            public void run() {
                Map<String, JobStatus> jobStatuses = null;
                while (true) {
                    try {
                        jobStatuses = client.getJobStatuses(expId);
                        Set<String> strings = jobStatuses.keySet();
                        for (String key : strings) {
                            JobStatus jobStatus = jobStatuses.get(key);
                            if (jobStatus == null) {
                                return;
                            } else {
                                if (JobState.COMPLETE.equals(jobStatus.getJobState())) {
                                    log.info("Job completed Job ID: " + key);
                                    return;
                                } else {
                                    log.info("Job ID:" + key + "  Job Status : " + jobStatuses.get(key).getJobState().toString());
                                }
                            }
                        }
                        Thread.sleep(5000);
                    } catch (Exception e) {
                        log.error("Thread interrupted", e.getMessage());
                    }
                }
            }
        });
        monitor.start();
        try {
            monitor.join();
        } catch (InterruptedException e) {
            log.error("Thread interrupted..", e.getMessage());
        }
    }
}
