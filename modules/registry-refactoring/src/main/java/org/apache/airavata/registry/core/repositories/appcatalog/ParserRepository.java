package org.apache.airavata.registry.core.repositories.appcatalog;

import org.apache.airavata.model.appcatalog.parser.Parser;
import org.apache.airavata.model.appcatalog.parser.ParsingTemplate;
import org.apache.airavata.registry.core.entities.appcatalog.ParserEntity;
import org.apache.airavata.registry.core.utils.DBConstants;
import org.apache.airavata.registry.core.utils.QueryConstants;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParserRepository extends AppCatAbstractRepository<Parser, ParserEntity, String> {

    public ParserRepository() {
        super(Parser.class, ParserEntity.class);
    }

    public List<Parser> getAllParsers(String gatewayId) {
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put(DBConstants.Parser.GATEWAY_ID, gatewayId);
        return select(QueryConstants.FIND_ALL_PARSERS_FOR_GATEWAY_ID, -1, 0, queryParameters);
    }
}
