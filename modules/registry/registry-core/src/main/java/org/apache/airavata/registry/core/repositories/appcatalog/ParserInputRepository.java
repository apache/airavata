package org.apache.airavata.registry.core.repositories.appcatalog;

import org.apache.airavata.model.appcatalog.parser.ParserInput;
import org.apache.airavata.registry.core.entities.appcatalog.ParserInputEntity;
import org.apache.airavata.registry.cpi.AppCatalogException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ParserInputRepository extends AppCatAbstractRepository<ParserInput, ParserInputEntity, String> {

    private final static Logger logger = LoggerFactory.getLogger(ParserInputRepository.class);

    public ParserInputRepository() {
        super(ParserInput.class, ParserInputEntity.class);
    }

    public ParserInput getParserInput(String inputId) throws AppCatalogException {
        try {
            return super.get(inputId);
        } catch (Exception e) {
            logger.error("Failed to fetch parser input with id " + inputId, e);
            throw new AppCatalogException("Failed to fetch parser input with id " + inputId, e);
        }
    }
}
