package org.apache.airavata.credentials.repository;

import org.apache.airavata.credentials.model.SSHKeyEntity;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SSHKeyRepository extends ListCrudRepository<SSHKeyEntity, String> {

    boolean existsBySshKeyName(String sshKeyName);
}
