package org.apache.airavata.application.repository;

import org.apache.airavata.application.model.template.ApplicationTemplateEntity;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApplicationTemplateRepository extends ListCrudRepository<ApplicationTemplateEntity, String> {

    boolean existsByTemplateName(String templateName);
}
