package org.apache.airavata.apis.db.repository.data;

import org.apache.airavata.apis.db.entity.data.OutDataMovementEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OutDataMovementRepository extends CrudRepository<OutDataMovementEntity, String> {
}
