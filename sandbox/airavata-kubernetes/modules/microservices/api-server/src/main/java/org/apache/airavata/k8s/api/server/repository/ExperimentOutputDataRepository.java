package org.apache.airavata.k8s.api.server.repository;

import org.apache.airavata.k8s.api.server.model.experiment.ExperimentOutputData;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

/**
 * TODO: Class level comments please
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
public interface ExperimentOutputDataRepository extends CrudRepository<ExperimentOutputData, Long> {
    public Optional<ExperimentOutputData> findById(long id);
}
