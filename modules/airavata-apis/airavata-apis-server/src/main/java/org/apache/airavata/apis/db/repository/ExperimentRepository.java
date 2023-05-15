package org.apache.airavata.apis.db.repository;

import org.apache.airavata.apis.db.entity.ExperimentEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExperimentRepository extends CrudRepository<ExperimentEntity, String> {
}
