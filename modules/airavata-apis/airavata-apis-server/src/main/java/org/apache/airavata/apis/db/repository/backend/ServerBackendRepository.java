package org.apache.airavata.apis.db.repository.backend;

import org.apache.airavata.apis.db.entity.backend.ServerBackendEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServerBackendRepository extends CrudRepository<ServerBackendEntity, String> {
}
