package org.apache.airavata.registry.core.repositories.appcatalog;

import org.apache.airavata.model.appcatalog.parser.ParserDagElement;
import org.apache.airavata.registry.core.entities.appcatalog.ParserDagElementEntity;

public class ParserDagElementRepository extends AppCatAbstractRepository<ParserDagElement, ParserDagElementEntity, String> {

    public ParserDagElementRepository() {
        super(ParserDagElement.class, ParserDagElementEntity.class);
    }
}
