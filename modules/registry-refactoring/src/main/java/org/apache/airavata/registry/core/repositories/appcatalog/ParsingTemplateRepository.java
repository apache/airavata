package org.apache.airavata.registry.core.repositories.appcatalog;

import org.apache.airavata.model.appcatalog.parser.ParsingTemplate;
import org.apache.airavata.registry.core.entities.appcatalog.ParsingTemplateEntity;

public class ParsingTemplateRepository extends AppCatAbstractRepository<ParsingTemplate, ParsingTemplateEntity, String> {

    public ParsingTemplateRepository() {
        super(ParsingTemplate.class, ParsingTemplateEntity.class);
    }
}
