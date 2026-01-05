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
package org.apache.airavata.registry.services;

import java.util.List;
import org.apache.airavata.common.model.Parser;
import org.apache.airavata.registry.entities.appcatalog.ParserEntity;
import org.apache.airavata.registry.exception.AppCatalogException;
import org.apache.airavata.registry.exception.RegistryException;
import org.apache.airavata.registry.mappers.ParserMapper;
import org.apache.airavata.registry.repositories.appcatalog.ParserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ParserService {
    private final ParserRepository parserRepository;
    private final ParserMapper parserMapper;

    public ParserService(ParserRepository parserRepository, ParserMapper parserMapper) {
        this.parserRepository = parserRepository;
        this.parserMapper = parserMapper;
    }

    public boolean isExists(String parserId) throws RegistryException {
        return parserRepository.existsById(parserId);
    }

    public Parser get(String parserId) throws RegistryException {
        ParserEntity entity = parserRepository.findById(parserId).orElse(null);
        if (entity == null) return null;
        return parserMapper.toModel(entity);
    }

    public Parser saveParser(Parser parser) throws AppCatalogException {
        ParserEntity entity = parserMapper.toEntity(parser);
        ParserEntity saved = parserRepository.save(entity);
        return parserMapper.toModel(saved);
    }

    public List<Parser> getAllParsers(String gatewayId) throws RegistryException {
        List<ParserEntity> entities = parserRepository.findByGatewayId(gatewayId);
        return parserMapper.toModelList(entities);
    }

    public void delete(String parserId) throws RegistryException {
        parserRepository.deleteById(parserId);
    }
}
