package org.apache.airavata.registry.core.repositories.appcatalog;

import org.apache.airavata.model.appcatalog.parser.ParserInput;
import org.apache.airavata.registry.core.entities.appcatalog.ParserInputEntity;

public class ParserInputRepository extends AppCatAbstractRepository<ParserInput, ParserInputEntity, String> {

    public ParserInputRepository() {
        super(ParserInput.class, ParserInputEntity.class);
    }
}
