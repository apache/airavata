package org.apache.airavata.apis.db.repository.application;

import org.apache.airavata.apis.db.entity.application.ApplicationEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApplicationRepository extends CrudRepository<ApplicationEntity, String> {
}
