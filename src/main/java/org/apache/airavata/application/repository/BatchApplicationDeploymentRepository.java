package org.apache.airavata.application.repository;

import java.util.List;
import org.apache.airavata.application.model.deployment.BatchApplicationDeploymentEntity;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BatchApplicationDeploymentRepository
        extends ListCrudRepository<BatchApplicationDeploymentEntity, String> {

    List<BatchApplicationDeploymentEntity> findByApplicationTemplate_TemplateId(String templateId);

    boolean existsByApplicationTemplate_TemplateId(String templateId);
}
