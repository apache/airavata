package org.apache.airavata.credentials.repository;

import java.util.List;
import org.apache.airavata.credentials.model.SSHUserCredential;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SSHUserCredentialRepository extends ListCrudRepository<SSHUserCredential, String> {

    List<SSHUserCredential> findBySshKey_SshKeyId(String sshKeyId);

    boolean existsBySshKey_SshKeyId(String sshKeyId);
}
