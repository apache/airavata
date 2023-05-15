package org.apache.airavata.apis.db.repository.data;

import org.apache.airavata.apis.db.entity.data.InDataMovementEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InDataMovementRepository extends CrudRepository<InDataMovementEntity, String> {
}
