package org.apache.airavata.k8s.api.server.repository;

import org.apache.airavata.k8s.api.server.model.application.ApplicationInterface;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

/**
 * TODO: Class level comments please
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
public interface ApplicationIfaceRepository extends CrudRepository<ApplicationInterface, Long> {

    public Optional<ApplicationInterface> findById(long id);
}
