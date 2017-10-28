package org.apache.airavata.k8s.api.server.repository;

import org.apache.airavata.k8s.api.server.model.experiment.ExperimentStatus;
import org.springframework.data.repository.CrudRepository;

/**
 * TODO: Class level comments please
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
public interface ExperimentStatusRepository extends CrudRepository<ExperimentStatus, Long> {
}
