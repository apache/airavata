package org.apache.airavata.registry.core.repositories.appcatalog;

import org.apache.airavata.model.appcatalog.parser.ParserDagInputOutputMapping;
import org.apache.airavata.registry.core.entities.appcatalog.ParserDagInputOutputMappingEntity;

public class ParserDagInputOutputMappingRepository extends AppCatAbstractRepository<ParserDagInputOutputMapping, ParserDagInputOutputMappingEntity, String> {

    public ParserDagInputOutputMappingRepository() {
        super(ParserDagInputOutputMapping.class, ParserDagInputOutputMappingEntity.class);
    }
}
