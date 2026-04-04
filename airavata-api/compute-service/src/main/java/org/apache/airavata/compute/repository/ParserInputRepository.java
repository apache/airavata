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
package org.apache.airavata.compute.repository;

import org.apache.airavata.compute.mapper.ComputeMapper;
import org.apache.airavata.compute.model.ParserInputEntity;
import org.apache.airavata.db.AbstractRepository;
import org.apache.airavata.interfaces.AppCatalogException;
import org.apache.airavata.model.appcatalog.parser.proto.ParserInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ParserInputRepository extends AbstractRepository<ParserInput, ParserInputEntity, String> {

    private static final Logger logger = LoggerFactory.getLogger(ParserInputRepository.class);

    public ParserInputRepository() {
        super(ParserInput.class, ParserInputEntity.class);
    }

    @Override
    protected ParserInput toModel(ParserInputEntity entity) {
        return ComputeMapper.INSTANCE.parserInputToModel(entity);
    }

    @Override
    protected ParserInputEntity toEntity(ParserInput model) {
        return ComputeMapper.INSTANCE.parserInputToEntity(model);
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
