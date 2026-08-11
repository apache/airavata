package org.apache.airavata.compute.repository;

import org.apache.airavata.compute.model.ClusterEntity;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClusterRepository extends ListCrudRepository<ClusterEntity, String> {

    boolean existsByClusterName(String clusterName);
}
