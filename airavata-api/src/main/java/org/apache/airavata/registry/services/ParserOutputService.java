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
import org.apache.airavata.model.appcatalog.parser.ParserOutput;
import org.apache.airavata.registry.entities.appcatalog.ParserOutputEntity;
import org.apache.airavata.registry.exceptions.RegistryException;
import org.apache.airavata.registry.repositories.appcatalog.ParserOutputRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ParserOutputService {
    private final ParserOutputRepository parserOutputRepository;
    private final Mapper mapper;

    public ParserOutputService(ParserOutputRepository parserOutputRepository, Mapper mapper) {
        this.parserOutputRepository = parserOutputRepository;
        this.mapper = mapper;
    }

    public boolean isExists(String parserOutputId) throws RegistryException {
        return parserOutputRepository.existsById(parserOutputId);
    }

    public ParserOutput get(String parserOutputId) throws RegistryException {
        ParserOutputEntity entity =
                parserOutputRepository.findById(parserOutputId).orElse(null);
        if (entity == null) return null;
        return mapper.map(entity, ParserOutput.class);
    }

    public ParserOutput create(ParserOutput parserOutput) throws RegistryException {
        ParserOutputEntity entity = mapper.map(parserOutput, ParserOutputEntity.class);
        ParserOutputEntity saved = parserOutputRepository.save(entity);
        return mapper.map(saved, ParserOutput.class);
    }

    public void delete(String parserOutputId) throws RegistryException {
        parserOutputRepository.deleteById(parserOutputId);
    }
}
