package org.apache.airavata.apis.db.repository.application.input;

import org.apache.airavata.apis.db.entity.application.input.EnvironmentInputEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EnvironmentInputRepository extends CrudRepository<EnvironmentInputEntity, String> {
}
