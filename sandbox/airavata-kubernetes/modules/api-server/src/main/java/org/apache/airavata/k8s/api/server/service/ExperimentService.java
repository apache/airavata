package org.apache.airavata.k8s.api.server.service;

import org.apache.airavata.k8s.api.server.repository.ExperimentRepository;
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

    @Autowired
    public ExperimentService(ExperimentRepository experimentRepository) {
        this.experimentRepository = experimentRepository;
    }

    public Optional<ExperimentResource> findById(long id) {
        return ToResourceUtil.toResource(experimentRepository.findById(id).get());
    }
}
