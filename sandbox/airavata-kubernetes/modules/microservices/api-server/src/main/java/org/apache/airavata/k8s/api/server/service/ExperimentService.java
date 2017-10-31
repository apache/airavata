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
package org.apache.airavata.k8s.api.server.service;

import org.apache.airavata.k8s.api.server.ServerRuntimeException;
import org.apache.airavata.k8s.api.server.model.experiment.Experiment;
import org.apache.airavata.k8s.api.server.model.experiment.ExperimentInputData;
import org.apache.airavata.k8s.api.server.model.experiment.ExperimentOutputData;
import org.apache.airavata.k8s.api.server.model.experiment.ExperimentStatus;
import org.apache.airavata.k8s.api.server.repository.*;
import org.apache.airavata.k8s.api.resources.experiment.ExperimentResource;
import org.apache.airavata.k8s.api.server.service.messaging.MessagingService;
import org.apache.airavata.k8s.api.server.service.util.ToResourceUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * TODO: Class level comments please
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
@Service
@Transactional
public class ExperimentService {

    private ExperimentRepository experimentRepository;
    private ApplicationDeploymentRepository appDepRepository;
    private ApplicationIfaceRepository appIfaceRepository;
    private ExperimentInputDataRepository inputDataRepository;
    private ExperimentOutputDataRepository outputDataRepository;
    private ExperimentStatusRepository experimentStatusRepository;

    private MessagingService messagingService;

    @Value("${launch.topic.name}")
    private String launchTopic;

    @Autowired
    public ExperimentService(ExperimentRepository experimentRepository,
                             ApplicationDeploymentRepository appDepRepository,
                             ApplicationIfaceRepository appIfaceRepository,
                             ExperimentInputDataRepository inputDataRepository,
                             ExperimentOutputDataRepository outputDataRepository,
                             ExperimentStatusRepository experimentStatusRepository,
                             MessagingService messagingService) {

        this.experimentRepository = experimentRepository;
        this.appDepRepository = appDepRepository;
        this.appIfaceRepository = appIfaceRepository;
        this.inputDataRepository = inputDataRepository;
        this.outputDataRepository = outputDataRepository;
        this.experimentStatusRepository = experimentStatusRepository;
        this.messagingService = messagingService;
    }

    public long create(ExperimentResource resource) {
        Experiment experiment = new Experiment();
        experiment.setExperimentName(resource.getExperimentName());

        experiment.setApplicationDeployment(appDepRepository.findById(resource.getApplicationDeploymentId())
                .orElseThrow(() -> new ServerRuntimeException("Can not find app deployment with id " +
                        resource.getApplicationDeploymentId())));

        experiment.setApplicationInterface(appIfaceRepository.findById(resource.getApplicationInterfaceId())
                .orElseThrow(() -> new ServerRuntimeException("Can not find app inerface with id " +
                        resource.getApplicationInterfaceId())));

        Optional.ofNullable(resource.getExperimentOutputs()).ifPresent(ops -> {
            ops.forEach(op -> {
                ExperimentOutputData outputData = new ExperimentOutputData();
                outputData.setName(op.getName());
                outputData.setValue(op.getValue());
                outputData.setType(ExperimentOutputData.DataType.valueOf(op.getType()));
                ExperimentOutputData saved = outputDataRepository.save(outputData);
                experiment.getExperimentOutputs().add(saved);
            });
        });

        Optional.ofNullable(resource.getExperimentInputs()).ifPresent(ips -> {
            ips.forEach(ip -> {
                ExperimentInputData inputData = new ExperimentInputData();
                inputData.setName(ip.getName());
                inputData.setValue(ip.getValue());
                inputData.setType(ExperimentInputData.DataType.valueOf(ip.getType()));
                inputData.setArguments(ip.getArguments());
                ExperimentInputData saved = inputDataRepository.save(inputData);
                experiment.getExperimentInputs().add(saved);
            });
        });

        Experiment saved = experimentRepository.save(experiment);
        return saved.getId();
    }

    public Optional<ExperimentResource> findById(long id) {
        return ToResourceUtil.toResource(findEntityById(id).get());
    }

    public Optional<Experiment> findEntityById(long id) {
        return this.experimentRepository.findById(id);
    }

    public long launchExperiment(long id) {
        Experiment experiment = this.experimentRepository.findById(id).orElseThrow(() -> new ServerRuntimeException("Experiment with id " +
                id + "can not be found"));
        // TODO validate status and get a lock

        ExperimentStatus experimentStatus = this.experimentStatusRepository.save(new ExperimentStatus()
                .setState(ExperimentStatus.ExperimentState.LAUNCHED)
                .setTimeOfStateChange(System.currentTimeMillis()));

        experiment.getExperimentStatus().add(experimentStatus);

        this.messagingService.send(this.launchTopic, "exp-" + id);
        return 0;
    }

    public List<ExperimentResource> getAll() {
        List<ExperimentResource> experimentList = new ArrayList<>();
        Optional.ofNullable(experimentRepository.findAll())
                .ifPresent(experiments ->
                        experiments.forEach(experiment -> experimentList.add(ToResourceUtil.toResource(experiment).get())));
        return experimentList;
    }
}
