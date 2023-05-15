package org.apache.airavata.apis.db.repository.backend.iface;

import org.apache.airavata.apis.db.entity.backend.iface.SSHInterfaceEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SSHInterfaceRepository extends CrudRepository<SSHInterfaceEntity, String> {
}
