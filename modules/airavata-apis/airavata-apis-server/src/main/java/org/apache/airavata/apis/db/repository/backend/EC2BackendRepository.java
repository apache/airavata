package org.apache.airavata.apis.db.repository.backend;

import org.apache.airavata.apis.db.entity.backend.EC2BackendEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EC2BackendRepository extends CrudRepository<EC2BackendEntity, String> {
}
