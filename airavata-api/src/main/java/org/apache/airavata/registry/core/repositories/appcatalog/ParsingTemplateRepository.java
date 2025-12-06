/**
*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.apache.airavata.registry.core.repositories.appcatalog;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.airavata.model.appcatalog.parser.ParsingTemplate;
import org.apache.airavata.registry.core.entities.appcatalog.ParsingTemplateEntity;
import org.apache.airavata.registry.core.utils.DBConstants;
import org.apache.airavata.registry.core.utils.QueryConstants;

@Repository
public class ParsingTemplateRepository
        extends AppCatAbstractRepository<ParsingTemplate, ParsingTemplateEntity, String> {

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
