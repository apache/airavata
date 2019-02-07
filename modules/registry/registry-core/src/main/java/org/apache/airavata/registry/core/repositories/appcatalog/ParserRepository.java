package org.apache.airavata.registry.core.repositories.appcatalog;

import org.apache.airavata.model.appcatalog.parser.Parser;
import org.apache.airavata.registry.core.entities.appcatalog.ParserEntity;
import org.apache.airavata.registry.core.utils.DBConstants;
import org.apache.airavata.registry.core.utils.ObjectMapperSingleton;
import org.apache.airavata.registry.core.utils.QueryConstants;
import org.apache.airavata.registry.cpi.AppCatalogException;
import org.dozer.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParserRepository extends AppCatAbstractRepository<Parser, ParserEntity, String> {

    private final static Logger logger = LoggerFactory.getLogger(ParserRepository.class);

    public ParserRepository() {
        super(Parser.class, ParserEntity.class);
    }

    public Parser saveParser(Parser parser) throws AppCatalogException {

        try {
            Mapper mapper = ObjectMapperSingleton.getInstance();
            ParserEntity parserEntity = mapper.map(parser, ParserEntity.class);

            if (parser.getInputFiles() != null) {
                parserEntity.getInputFiles().forEach(input -> {
                    input.setParser(parserEntity);
                    input.setParserId(parserEntity.getId());
                });
            }

            if (parser.getOutputFiles() != null) {
                parserEntity.getOutputFiles().forEach(output -> {
                    output.setParser(parserEntity);
                    output.setParserId(parserEntity.getId());
                });
            }

            ParserEntity savedParserEntity = execute(entityManager -> entityManager.merge(parserEntity));
            return mapper.map(savedParserEntity, Parser.class);
        } catch (Exception e) {
            logger.error("Failed to save parser with id " + parser.getId(), e);
            throw new AppCatalogException("Failed to save parser with id " + parser.getId(), e);
        }
    }

    public List<Parser> getAllParsers(String gatewayId) {
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put(DBConstants.Parser.GATEWAY_ID, gatewayId);
        return select(QueryConstants.FIND_ALL_PARSERS_FOR_GATEWAY_ID, -1, 0, queryParameters);
    }
}
