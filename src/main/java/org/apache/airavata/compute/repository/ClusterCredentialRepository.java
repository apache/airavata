package org.apache.airavata.compute.repository;

import java.util.List;
import org.apache.airavata.compute.model.ClusterCredentialEntity;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClusterCredentialRepository extends ListCrudRepository<ClusterCredentialEntity, String> {

    List<ClusterCredentialEntity> findBySlurmCluster_ClusterId(String clusterId);

    List<ClusterCredentialEntity> findByUser_UserId(String userId);

    List<ClusterCredentialEntity> findByUser_UserIdAndSlurmCluster_ClusterId(String userId, String clusterId);
}
