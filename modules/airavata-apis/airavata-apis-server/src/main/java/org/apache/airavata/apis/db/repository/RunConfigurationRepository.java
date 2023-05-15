package org.apache.airavata.apis.db.repository;

import org.apache.airavata.apis.db.entity.RunConfigurationEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RunConfigurationRepository extends CrudRepository<RunConfigurationEntity, String> {
}
