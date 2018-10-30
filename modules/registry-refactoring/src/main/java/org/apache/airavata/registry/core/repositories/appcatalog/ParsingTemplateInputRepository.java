package org.apache.airavata.registry.core.repositories.appcatalog;

import org.apache.airavata.model.appcatalog.parser.ParsingTemplateInput;
import org.apache.airavata.registry.core.entities.appcatalog.ParsingTemplateInputEntity;

public class ParsingTemplateInputRepository extends AppCatAbstractRepository<ParsingTemplateInput, ParsingTemplateInputEntity, String> {

    public ParsingTemplateInputRepository() {
        super(ParsingTemplateInput.class, ParsingTemplateInputEntity.class);
    }
}
