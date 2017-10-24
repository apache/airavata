package org.apache.airavata.k8s.api.server.service;

import org.apache.airavata.k8s.api.server.ServerRuntimeException;
import org.apache.airavata.k8s.api.server.model.experiment.Experiment;
import org.apache.airavata.k8s.api.server.model.process.ProcessModel;
import org.apache.airavata.k8s.api.server.repository.ExperimentRepository;
import org.apache.airavata.k8s.api.server.repository.ProcessRepository;
import org.apache.airavata.k8s.api.server.resources.process.ProcessResource;
import org.apache.airavata.k8s.api.server.service.util.ToResourceUtil;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * TODO: Class level comments please
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
@Service
public class ProcessService {

    private ExperimentRepository experimentRepository;
    private ProcessRepository processRepository;

    public ProcessService(ExperimentRepository experimentRepository, ProcessRepository processRepository) {
        this.experimentRepository = experimentRepository;
        this.processRepository = processRepository;
    }

    public long create(ProcessResource resource) {

        ProcessModel processModel = new ProcessModel();
        processModel.setId(resource.getId());
        processModel.setCreationTime(resource.getCreationTime());
        processModel.setLastUpdateTime(resource.getLastUpdateTime());
        processModel.setExperiment(experimentRepository.findById(resource.getExperimentId())
                .orElseThrow(() -> new ServerRuntimeException("Can not find experiment with id " +
                        resource.getExperimentId())));
        processModel.setExperimentDataDir(resource.getExperimentDataDir());
        ProcessModel saved = processRepository.save(processModel);
        return saved.getId();
    }

    public Optional<ProcessResource> findById(long id) {
        return ToResourceUtil.toResource(processRepository.findById(id).get());
    }
}
