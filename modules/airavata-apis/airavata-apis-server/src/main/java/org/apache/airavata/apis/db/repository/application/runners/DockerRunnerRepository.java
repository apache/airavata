package org.apache.airavata.apis.db.repository.application.runners;

import org.apache.airavata.apis.db.entity.application.runners.DockerRunnerEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DockerRunnerRepository extends CrudRepository<DockerRunnerEntity, String> {
}
