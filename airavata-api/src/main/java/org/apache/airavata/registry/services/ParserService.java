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

import com.github.dozermapper.core.Mapper;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.airavata.model.appcatalog.parser.Parser;
import org.apache.airavata.registry.entities.appcatalog.ParserEntity;
import org.apache.airavata.registry.exceptions.AppCatalogException;
import org.apache.airavata.registry.exceptions.RegistryException;
import org.apache.airavata.registry.repositories.appcatalog.ParserRepository;
import org.apache.airavata.registry.utils.ObjectMapperSingleton;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ParserService {
    @Autowired
    private ParserRepository parserRepository;

    public boolean isExists(String parserId) throws RegistryException {
        return parserRepository.existsById(parserId);
    }

    public Parser get(String parserId) throws RegistryException {
        ParserEntity entity = parserRepository.findById(parserId).orElse(null);
        if (entity == null) return null;
        Mapper mapper = ObjectMapperSingleton.getInstance();
        return mapper.map(entity, Parser.class);
    }

    public Parser saveParser(Parser parser) throws AppCatalogException {
        Mapper mapper = ObjectMapperSingleton.getInstance();
        ParserEntity entity = mapper.map(parser, ParserEntity.class);
        ParserEntity saved = parserRepository.save(entity);
        return mapper.map(saved, Parser.class);
    }

    public List<Parser> getAllParsers(String gatewayId) throws RegistryException {
        List<ParserEntity> entities = parserRepository.findByGatewayId(gatewayId);
        Mapper mapper = ObjectMapperSingleton.getInstance();
        return entities.stream().map(e -> mapper.map(e, Parser.class)).collect(Collectors.toList());
    }

    public void delete(String parserId) throws RegistryException {
        parserRepository.deleteById(parserId);
    }
}
