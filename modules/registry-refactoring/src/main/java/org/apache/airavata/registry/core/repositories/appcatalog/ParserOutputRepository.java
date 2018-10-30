package org.apache.airavata.registry.core.repositories.appcatalog;

import org.apache.airavata.model.appcatalog.parser.ParserOutput;
import org.apache.airavata.registry.core.entities.appcatalog.ParserOutputEntity;

public class ParserOutputRepository extends AppCatAbstractRepository<ParserOutput, ParserOutputEntity, String> {

    public ParserOutputRepository() {
        super(ParserOutput.class, ParserOutputEntity.class);
    }
}
