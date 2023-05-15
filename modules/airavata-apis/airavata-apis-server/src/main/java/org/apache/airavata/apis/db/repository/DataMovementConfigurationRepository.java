package org.apache.airavata.apis.db.repository;

import org.apache.airavata.apis.db.entity.DataMovementConfigurationEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DataMovementConfigurationRepository extends CrudRepository<DataMovementConfigurationEntity, String> {
}
