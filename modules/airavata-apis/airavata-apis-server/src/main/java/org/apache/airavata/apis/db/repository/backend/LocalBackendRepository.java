package org.apache.airavata.apis.db.repository.backend;

import org.apache.airavata.apis.db.entity.backend.LocalBackendEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LocalBackendRepository extends CrudRepository<LocalBackendEntity, String> {
}
