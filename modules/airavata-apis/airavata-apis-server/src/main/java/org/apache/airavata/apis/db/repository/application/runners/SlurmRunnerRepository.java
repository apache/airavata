package org.apache.airavata.apis.db.repository.application.runners;

import org.apache.airavata.apis.db.entity.application.runners.SlurmRunnerEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SlurmRunnerRepository extends CrudRepository<SlurmRunnerEntity, String> {
}
