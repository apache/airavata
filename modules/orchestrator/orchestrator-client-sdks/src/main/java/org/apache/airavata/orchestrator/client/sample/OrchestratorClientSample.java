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

package org.apache.airavata.orchestrator.client.sample;

import org.apache.airavata.client.AiravataAPIFactory;
import org.apache.airavata.client.api.AiravataAPI;
import org.apache.airavata.client.api.exception.AiravataAPIInvocationException;
import org.apache.airavata.client.tools.DocumentCreator;
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.common.utils.ClientSettings;
import org.apache.airavata.model.util.ExperimentModelUtil;
import org.apache.airavata.model.workspace.experiment.ComputationalResourceScheduling;
import org.apache.airavata.model.workspace.experiment.DataObjectType;
import org.apache.airavata.model.workspace.experiment.Experiment;
import org.apache.airavata.model.workspace.experiment.UserConfigurationData;
import org.apache.airavata.orchestrator.client.OrchestratorClientFactory;
import org.apache.airavata.orchestrator.cpi.OrchestratorService;
import org.apache.airavata.persistance.registry.jpa.impl.RegistryFactory;
import org.apache.airavata.registry.cpi.ParentDataType;
import org.apache.airavata.registry.cpi.Registry;
import org.apache.airavata.schemas.gfac.DataType;
import org.apache.thrift.TException;

import java.util.ArrayList;
import java.util.List;

public class OrchestratorClientSample {
    private static DocumentCreator documentCreator;
    private static OrchestratorService.Client orchestratorClient;
    private static Registry registry;
    private static int NUM_CONCURRENT_REQUESTS = 1;
    private static final String DEFAULT_USER = "defauly.registry.user";
    private static final String DEFAULT_USER_PASSWORD = "default.registry.password";
    private static final String DEFAULT_GATEWAY = "default.registry.gateway";
    private static String sysUser;
    private static String sysUserPwd;
    private static String gateway;

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
//            documentCreator.createGramDocs();
//            documentCreator.createGSISSHDocs();
            storeExperimentDetail();
        } catch (ApplicationSettingsException e) {
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

    public static void storeExperimentDetail() {
        for (int i = 0; i < NUM_CONCURRENT_REQUESTS; i++) {
            Thread thread = new Thread() {
                public void run() {
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

                    Experiment simpleExperiment = ExperimentModelUtil.createSimpleExperiment("project1", "admin", "echoExperiment", "SimpleEcho2", "SimpleEcho2", exInputs);
                    simpleExperiment.setExperimentOutputs(exOut);

                    ComputationalResourceScheduling scheduling = ExperimentModelUtil.createComputationResourceScheduling("trestles.sdsc.edu", 1, 1, 1, "normal", 0, 0, 1, "sds128");
                    scheduling.setResourceHostId("gsissh-trestles");
                    UserConfigurationData userConfigurationData = new UserConfigurationData();
                    userConfigurationData.setComputationalResourceScheduling(scheduling);
                    simpleExperiment.setUserConfigurationData(userConfigurationData);
                    String expId = null;
                    try {
                        expId = (String) registry.add(ParentDataType.EXPERIMENT, simpleExperiment);
                    } catch (Exception e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }

                    try {
                        orchestratorClient.launchExperiment(expId);
                    } catch (TException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                }
            };
            thread.start();
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
    }
}
