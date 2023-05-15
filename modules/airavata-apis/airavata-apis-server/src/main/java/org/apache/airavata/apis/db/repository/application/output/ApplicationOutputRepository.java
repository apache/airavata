package org.apache.airavata.apis.db.repository.application.output;

import org.apache.airavata.apis.db.entity.application.output.ApplicationOutputEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApplicationOutputRepository extends CrudRepository<ApplicationOutputEntity, String> {
}
