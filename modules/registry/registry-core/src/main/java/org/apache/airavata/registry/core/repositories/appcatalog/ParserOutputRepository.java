package org.apache.airavata.registry.core.repositories.appcatalog;

import org.apache.airavata.model.appcatalog.parser.ParserOutput;
import org.apache.airavata.registry.core.entities.appcatalog.ParserOutputEntity;
import org.apache.airavata.registry.cpi.AppCatalogException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ParserOutputRepository extends AppCatAbstractRepository<ParserOutput, ParserOutputEntity, String> {

    private final static Logger logger = LoggerFactory.getLogger(ParserInputRepository.class);

    public ParserOutputRepository() {
        super(ParserOutput.class, ParserOutputEntity.class);
    }

    public ParserOutput getParserOutput(String outputId) throws AppCatalogException {
        try {
            return super.get(outputId);
        } catch (Exception e) {
            logger.error("Failed to fetch parser output with id " + outputId, e);
            throw new AppCatalogException("Failed to fetch parser output with id " + outputId, e);
        }
    }
}