package org.apache.airavata.application.repository;

import java.util.List;
import org.apache.airavata.application.model.deployment.SlurmApplicationDeploymentEntity;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SlurmApplicationDeploymentRepository
        extends ListCrudRepository<SlurmApplicationDeploymentEntity, String> {

    List<SlurmApplicationDeploymentEntity> findByApplicationTemplate_TemplateId(String templateId);

    boolean existsByApplicationTemplate_TemplateId(String templateId);
}
