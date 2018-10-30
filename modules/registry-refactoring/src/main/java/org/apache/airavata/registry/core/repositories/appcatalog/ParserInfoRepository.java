package org.apache.airavata.registry.core.repositories.appcatalog;

import org.apache.airavata.model.appcatalog.parser.ParserInfo;
import org.apache.airavata.registry.core.entities.appcatalog.ParserInfoEntity;

public class ParserInfoRepository extends AppCatAbstractRepository<ParserInfo, ParserInfoEntity, String> {

    public ParserInfoRepository() {
        super(ParserInfo.class, ParserInfoEntity.class);
    }
}
