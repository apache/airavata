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
package org.apache.airavata.orchestrator.sample;

//import org.apache.airavata.client.AiravataAPIFactory;
//import org.apache.airavata.client.api.AiravataAPI;
//import org.apache.airavata.client.api.exception.AiravataAPIInvocationException;
//import org.apache.airavata.client.tools.DocumentCreator;

import org.apache.airavata.model.application.io.DataType;
import org.apache.airavata.model.application.io.InputDataObjectType;
import org.apache.airavata.model.application.io.OutputDataObjectType;
import org.apache.airavata.model.experiment.ExperimentModel;
import org.apache.airavata.model.experiment.UserConfigurationDataModel;
import org.apache.airavata.model.scheduling.ComputationalResourceSchedulingModel;
import org.apache.airavata.model.util.ExperimentModelUtil;
import org.apache.airavata.orchestrator.cpi.OrchestratorService;
import org.apache.thrift.TException;

import java.util.ArrayList;
import java.util.List;

public class OrchestratorClientSample {
//    private static DocumentCreator documentCreator;
    private static OrchestratorService.Client orchestratorClient;
//    private static Registry registry;
    private static int NUM_CONCURRENT_REQUESTS = 1;
    private static final String DEFAULT_USER = "default.registry.user";
    private static final String DEFAULT_USER_PASSWORD = "default.registry.password";
    private static final String DEFAULT_GATEWAY = "default.registry.gateway";
    private static String sysUser;
    private static String sysUserPwd;
    private static String gateway;
/*

    public static void main(String[] args) {
        try {
            AiravataUtils.setExecutionAsClient();
            sysUser = ClientSettings.getSetting(DEFAULT_USER);
            sysUserPwd = ClientSettings.getSetting(DEFAULT_USER_PASSWORD);
            gateway = ClientSettings.getSetting(DEFAULT_GATEWAY);
            orchestratorClient = OrchestratorClientFactory.createOrchestratorClient("localhost", 8940);
            registry = RegistryFactory.getRegistry(gateway, sysUser, sysUserPwd);
            documentCreator = new DocumentCreator(getAiravataAPI());
            documentCreator.createLocalHostDocs();
            documentCreator.createGramDocs();
            documentCreator.createPBSDocsForOGCE();
            storeExperimentDetail();
        } catch (ApplicationSettingsException e) {
            e.printStackTrace();
        } catch (RegistryException e) {
            e.printStackTrace();
        }

    }

    private static AiravataAPI getAiravataAPI() {
        AiravataAPI airavataAPI = null;
            try {
                airavataAPI = AiravataAPIFactory.getAPI(gateway, sysUser);
            } catch (AiravataAPIInvocationException e) {
                e.printStackTrace();
            }
        return airavataAPI;
    }
*/

    public static void storeExperimentDetail() {
        for (int i = 0; i < NUM_CONCURRENT_REQUESTS; i++) {
            Thread thread = new Thread() {
                public void run() {
                    List<InputDataObjectType> exInputs = new ArrayList<InputDataObjectType>();
                    InputDataObjectType input = new InputDataObjectType();
                    input.setName("echo_input");
                    input.setType(DataType.STRING);
                    input.setValue("echo_output=Hello World");
                    exInputs.add(input);


                    List<OutputDataObjectType> exOut = new ArrayList<OutputDataObjectType>();
                    OutputDataObjectType output = new OutputDataObjectType();
                    output.setName("echo_output");
                    output.setType(DataType.STRING);
                    output.setValue("");
                    exOut.add(output);

                    ExperimentModel simpleExperiment = ExperimentModelUtil.createSimpleExperiment(DEFAULT_GATEWAY,"default", "admin", "echoExperiment", "SimpleEcho2", "SimpleEcho2", exInputs);
                    simpleExperiment.setExperimentOutputs(exOut);

                    ComputationalResourceSchedulingModel scheduling = ExperimentModelUtil.createComputationResourceScheduling("trestles.sdsc.edu", 1, 1, 1, "normal", 0, 0);
                    scheduling.setResourceHostId("gsissh-trestles");
                    UserConfigurationDataModel userConfigurationDataModel = new UserConfigurationDataModel();
                    userConfigurationDataModel.setComputationalResourceScheduling(scheduling);
                    simpleExperiment.setUserConfigurationData(userConfigurationDataModel);
                    String expId = null;
                    try {
//                        expId = (String) registry.add(ParentDataType.EXPERIMENT, simpleExperiment);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    try {
                        orchestratorClient.launchExperiment(expId, "airavataToken");
                    } catch (TException e) {
                        throw new RuntimeException("Error while storing experiment details", e);
                    }
                }
            };
            thread.start();
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
