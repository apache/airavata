package org.apache.airavata.registry.core.repositories.appcatalog;

import org.apache.airavata.model.appcatalog.parser.Parser;
import org.apache.airavata.registry.core.entities.appcatalog.ParserEntity;

public class ParserRepository extends AppCatAbstractRepository<Parser, ParserEntity, String> {

    public ParserRepository() {
        super(Parser.class, ParserEntity.class);
    }
}
