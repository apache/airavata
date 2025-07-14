/**
*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.apache.airavata.tools.load;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.*;
import org.apache.airavata.agents.api.AgentException;
import org.apache.airavata.api.Airavata;
import org.apache.airavata.api.client.AiravataClientFactory;
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.model.application.io.DataType;
import org.apache.airavata.model.application.io.InputDataObjectType;
import org.apache.airavata.model.experiment.ExperimentModel;
import org.apache.airavata.model.experiment.ExperimentType;
import org.apache.airavata.model.experiment.UserConfigurationDataModel;
import org.apache.airavata.model.scheduling.ComputationalResourceSchedulingModel;
import org.apache.airavata.model.security.AuthzToken;
import org.apache.thrift.TException;

public class UnitLoad {

    private String apiHost;
    private int apiPort;
    private StorageResourceManager storageResourceManager;
    private AuthzToken authzToken;

    public UnitLoad(
            String apiHost,
            int apiPort,
            StorageResourceManager storageResourceManager,
            AuthzToken authzToken) {
        this.apiHost = apiHost;
        this.apiPort = apiPort;
        this.storageResourceManager = storageResourceManager;
        this.authzToken = authzToken;
    }

    public CompletionService<List<String>> execute(Configuration config) {
        String randomUUID = UUID.randomUUID().toString();
        ExecutorService executorService = Executors.newFixedThreadPool(config.getConcurrentUsers());
        CompletionService<List<String>> completionService = new ExecutorCompletionService<>(executorService);

        for (int i = 0; i < config.getConcurrentUsers(); i++) {
            completionService.submit(new Worker(
                    config,
                    randomUUID + "-" + i,
                    config.getIterationsPerUser(),
                    config.getRandomMSDelayWithinSubmissions()));
        }
        return completionService;
    }

    public class Worker implements Callable<List<String>> {

        private final String id;
        private final int iterations;
        private final int delay;
        private final Configuration config;

        public Worker(Configuration config, String id, int iterations, int delay) {
            this.id = id;
            this.iterations = iterations;
            this.delay = delay;
            this.config = config;
        }

        @Override
        public List<String> call() {
            List<String> experiments = new ArrayList<>();
            for (int i = 0; i < iterations; i++) {
                try {
                    double randomDouble = Math.random();
                    randomDouble = randomDouble * delay + 1;
                    long randomLong = (long) randomDouble;
                    Thread.sleep(randomLong);
                    experiments.add(submitExperiment(config, id + "-" + i));
                } catch (TException | ApplicationSettingsException | AgentException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return experiments;
        }
    }

    private String submitExperiment(Configuration config, String suffix) throws TException, AgentException, ApplicationSettingsException {

        String experimentName = config.getExperimentBaseName() + suffix;

        ExperimentModel experimentModel = new ExperimentModel();
        experimentModel.setExperimentName(experimentName);
        experimentModel.setProjectId(config.getProjectId());
        experimentModel.setUserName(config.getUserId());
        experimentModel.setGatewayId(config.getGatewayId());
        experimentModel.setExecutionId(config.getApplicationInterfaceId());

        ComputationalResourceSchedulingModel computationalResourceSchedulingModel =
                new ComputationalResourceSchedulingModel();
        computationalResourceSchedulingModel.setQueueName(config.getQueue());
        computationalResourceSchedulingModel.setNodeCount(config.getNodeCount());
        computationalResourceSchedulingModel.setTotalCPUCount(config.getCpuCount());
        computationalResourceSchedulingModel.setWallTimeLimit(config.getWallTime());
        computationalResourceSchedulingModel.setTotalPhysicalMemory(config.getPhysicalMemory());
        computationalResourceSchedulingModel.setResourceHostId(config.getComputeResourceId());

        UserConfigurationDataModel userConfigurationDataModel = new UserConfigurationDataModel();
        userConfigurationDataModel.setComputationalResourceScheduling(computationalResourceSchedulingModel);
        userConfigurationDataModel.setAiravataAutoSchedule(false);
        userConfigurationDataModel.setOverrideManualScheduledParams(false);
        userConfigurationDataModel.setStorageId(config.getStorageResourceId());
        userConfigurationDataModel.setExperimentDataDir(config.getUserId()
                .concat(File.separator)
                .concat(config.getProjectId())
                .concat(File.separator)
                .concat(experimentName));

        experimentModel.setUserConfigurationData(userConfigurationDataModel);

        Airavata.Client airavataClient = AiravataClientFactory.createAiravataClient(apiHost, apiPort, ServerSettings.isTLSEnabled());

        List<InputDataObjectType> applicationInputs =
                airavataClient.getApplicationInputs(authzToken, config.getApplicationInterfaceId());
        List<InputDataObjectType> experimentInputs = new ArrayList<>();

        storageResourceManager.createExperimentDirectory(config.getUserId(), config.getProjectId(), experimentName);

        for (InputDataObjectType inputDataObjectType : applicationInputs) {

            Optional<Configuration.Input> input = config.getInputs().stream()
                    .filter(inp -> inp.getName().equals(inputDataObjectType.getName()))
                    .findFirst();

            if (input.isPresent()) {
                if (inputDataObjectType.getType() == DataType.URI) {
                    String localFilePath = input.get().getValue();
                    String uploadedPath = storageResourceManager.uploadInputFile(
                            airavataClient,
                            localFilePath,
                            config.getUserId(),
                            config.getProjectId(),
                            experimentName,
                            config.getGatewayId());
                    inputDataObjectType.setValue(uploadedPath);

                } else if (inputDataObjectType.getType() == DataType.STRING) {
                    inputDataObjectType.setValue(input.get().getValue());
                }
            }
            experimentInputs.add(inputDataObjectType);
        }

        experimentModel.setExperimentInputs(experimentInputs);
        experimentModel.setExperimentOutputs(
                airavataClient.getApplicationOutputs(authzToken, config.getApplicationInterfaceId()));
        experimentModel.setExperimentType(ExperimentType.SINGLE_APPLICATION);

        String experimentId = airavataClient.createExperiment(authzToken, config.getGatewayId(), experimentModel);

        airavataClient.launchExperiment(authzToken, experimentId, config.getGatewayId());
        System.out.println(experimentId);

        ExperimentModel experiment = airavataClient.getExperiment(authzToken, experimentId);
        return experimentId;
    }
}
