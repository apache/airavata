package org.apache.airavata.k8s.api.server.repository;

import org.apache.airavata.k8s.api.server.model.experiment.Experiment;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

/**
 * TODO: Class level comments please
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
public interface ExperimentRepository extends CrudRepository<Experiment, Long> {
    public Optional<Experiment> findById(long id);
}
