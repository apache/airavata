package org.apache.airavata.k8s.api.server.repository;

import org.apache.airavata.k8s.api.server.model.task.TaskStatus;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

/**
 * TODO: Class level comments please
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
public interface TaskStatusRepository extends CrudRepository<TaskStatus, Long> {

    public Optional<TaskStatus> findById(long id);
}
