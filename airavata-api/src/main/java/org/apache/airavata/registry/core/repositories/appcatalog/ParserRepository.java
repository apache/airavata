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

import com.github.dozermapper.core.Mapper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.airavata.model.appcatalog.parser.Parser;
import org.apache.airavata.registry.core.entities.appcatalog.ParserEntity;
import org.apache.airavata.registry.core.utils.DBConstants;
import org.apache.airavata.registry.core.utils.ObjectMapperSingleton;
import org.apache.airavata.registry.core.utils.QueryConstants;
import org.apache.airavata.registry.cpi.AppCatalogException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

@Repository
public class ParserRepository extends AppCatAbstractRepository<Parser, ParserEntity, String> {

    private static final Logger logger = LoggerFactory.getLogger(ParserRepository.class);

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
