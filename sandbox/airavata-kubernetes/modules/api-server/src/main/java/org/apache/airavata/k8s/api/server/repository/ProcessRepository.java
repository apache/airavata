package org.apache.airavata.k8s.api.server.repository;

import org.apache.airavata.k8s.api.server.model.process.ProcessModel;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

/**
 * TODO: Class level comments please
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
public interface ProcessRepository extends CrudRepository<ProcessModel, Long> {
    Optional<ProcessModel> findById(long id);
}
