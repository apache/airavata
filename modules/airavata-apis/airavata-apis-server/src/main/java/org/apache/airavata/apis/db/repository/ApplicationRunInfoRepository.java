package org.apache.airavata.apis.db.repository;

import org.apache.airavata.apis.db.entity.ApplicationRunInfoEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApplicationRunInfoRepository extends CrudRepository<ApplicationRunInfoEntity, String> {
}
