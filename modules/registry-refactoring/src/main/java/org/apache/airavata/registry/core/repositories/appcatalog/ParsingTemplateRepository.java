package org.apache.airavata.registry.core.repositories.appcatalog;

import org.apache.airavata.model.appcatalog.parser.ParsingTemplate;
import org.apache.airavata.registry.core.entities.appcatalog.ParsingTemplateEntity;
import org.apache.airavata.registry.core.utils.DBConstants;
import org.apache.airavata.registry.core.utils.QueryConstants;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParsingTemplateRepository extends AppCatAbstractRepository<ParsingTemplate, ParsingTemplateEntity, String> {

    public ParsingTemplateRepository() {
        super(ParsingTemplate.class, ParsingTemplateEntity.class);
    }

    public List<ParsingTemplate> getParsingTemplatesForApplication(String applicationInterfaceId) {
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put(DBConstants.ParsingTemplate.APPLICATION_INTERFACE_ID, applicationInterfaceId);
        return select(QueryConstants.FIND_PARSING_TEMPLATES_FOR_APPLICATION_INTERFACE_ID, -1, 0, queryParameters);
    }

    public List<ParsingTemplate> getAllParsingTemplates(String gatewayId) {
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put(DBConstants.ParsingTemplate.GATEWAY_ID, gatewayId);
        return select(QueryConstants.FIND_ALL_PARSING_TEMPLATES_FOR_GATEWAY_ID, -1, 0, queryParameters);
    }
}
