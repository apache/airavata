package org.apache.airavata.compute.repository;

import java.util.List;
import org.apache.airavata.compute.model.SlurmClusterCredentialEntity;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SlurmClusterCredentialRepository extends ListCrudRepository<SlurmClusterCredentialEntity, String> {

    List<SlurmClusterCredentialEntity> findBySlurmCluster_ClusterId(String clusterId);

    List<SlurmClusterCredentialEntity> findByUser_UserId(String userId);

    List<SlurmClusterCredentialEntity> findByUser_UserIdAndSlurmCluster_ClusterId(String userId, String clusterId);
}
