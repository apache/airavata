package org.apache.airavata.apis.service.impl;

import org.apache.airavata.api.execution.stubs.Experiment;
import org.apache.airavata.apis.db.entity.ExperimentEntity;
import org.apache.airavata.apis.db.repository.ExperimentRepository;
import org.apache.airavata.apis.db.repository.RunConfigurationRepository;
import org.apache.airavata.apis.exception.EntityNotFoundException;
import org.apache.airavata.apis.mapper.ExperimentMapper;
import org.apache.airavata.apis.service.ExecutionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

import java.util.Optional;

// See https://github.com/LogNet/grpc-spring-boot-starter#9-grpc-response-observer--and-spring-transactional-caveats
@Service
@Transactional
public class ExecutionServiceImpl implements ExecutionService {

    @Autowired
    ExperimentRepository experimentRepository;

    @Autowired
    RunConfigurationRepository runConfigurationRepository;

    @Autowired
    ExperimentMapper experimentMapper;

    @Override
    public Experiment createExperiment(Experiment experiment) {

        ExperimentEntity experimentEntity = experimentMapper.mapModelToEntity(experiment);
        ExperimentEntity savedExperimentEntity = experimentRepository.save(experimentEntity);
        return experimentMapper.mapEntityToModel(savedExperimentEntity);
    }

    @Override
    public Experiment updateExperiment(Experiment experiment) {
        Optional<ExperimentEntity> maybeExperimentEntity = experimentRepository.findById(experiment.getExperimentId());
        if (maybeExperimentEntity.isEmpty()) {
            throw new EntityNotFoundException("No experiment exists with id " + experiment.getExperimentId());
        }
        ExperimentEntity experimentEntity = maybeExperimentEntity.get();
        // First delete any existing run configs
        if (experimentEntity.getRunConfigs() != null && !experimentEntity.getRunConfigs().isEmpty()) {
            runConfigurationRepository.deleteAll(experimentEntity.getRunConfigs());
            experimentEntity.setRunConfigs(null);
        }
        experimentMapper.mapModelToEntity(experiment, experimentEntity);
        experimentRepository.save(experimentEntity);
        return experimentMapper.mapEntityToModel(experimentEntity);

    }

}
