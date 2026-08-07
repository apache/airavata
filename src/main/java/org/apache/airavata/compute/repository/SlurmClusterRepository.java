package org.apache.airavata.compute.repository;

import org.apache.airavata.compute.model.SlurmClusterEntity;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SlurmClusterRepository extends ListCrudRepository<SlurmClusterEntity, String> {

    boolean existsByClusterName(String clusterName);
}
