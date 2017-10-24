package org.apache.airavata.k8s.api.server.service;

import org.apache.airavata.k8s.api.server.ServerRuntimeException;
import org.apache.airavata.k8s.api.server.model.experiment.Experiment;
import org.apache.airavata.k8s.api.server.model.experiment.ExperimentInputData;
import org.apache.airavata.k8s.api.server.model.experiment.ExperimentOutputData;
import org.apache.airavata.k8s.api.server.repository.*;
import org.apache.airavata.k8s.api.server.resources.experiment.ExperimentResource;
import org.apache.airavata.k8s.api.server.service.util.ToResourceUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Autowired
    public ExperimentService(ExperimentRepository experimentRepository,
                             ApplicationDeploymentRepository appDepRepository,
                             ApplicationIfaceRepository appIfaceRepository,
                             ExperimentInputDataRepository inputDataRepository,
                             ExperimentOutputDataRepository outputDataRepository) {

        this.experimentRepository = experimentRepository;
        this.appDepRepository = appDepRepository;
        this.appIfaceRepository = appIfaceRepository;
        this.inputDataRepository = inputDataRepository;
        this.outputDataRepository = outputDataRepository;
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
        return ToResourceUtil.toResource(experimentRepository.findById(id).get());
    }
}
