package org.apache.airavata.process.repository;

import java.util.List;
import org.apache.airavata.process.model.BatchJobProcess;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BatchJobProcessRepository extends ListCrudRepository<BatchJobProcess, String> {

    List<BatchJobProcess> findByBatchApplicationDeployment_DeploymentId(String deploymentId);

    List<BatchJobProcess> findByUser_UserId(String userId);
}
