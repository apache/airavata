package org.apache.airavata.apis.db.repository.application.output;

import org.apache.airavata.apis.db.entity.application.output.StandardErrorEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StandardErrorRepository extends CrudRepository<StandardErrorEntity, String> {
}
