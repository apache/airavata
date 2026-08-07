package org.apache.airavata.compute.repository;

import java.util.List;
import java.util.Optional;
import org.apache.airavata.compute.model.SlurmPartitionEntity;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SlurmPartitionRepository extends ListCrudRepository<SlurmPartitionEntity, String> {

    List<SlurmPartitionEntity> findBySlurmCluster_ClusterId(String clusterId);

    /**
     * Scopes a partition lookup to its cluster so a partition id from one cluster cannot
     * be read or mutated through another cluster's sub-resource path.
     */
    Optional<SlurmPartitionEntity> findByPartitionIdAndSlurmCluster_ClusterId(String partitionId, String clusterId);
}
