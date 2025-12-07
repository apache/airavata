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
import org.apache.airavata.model.appcatalog.parser.ParsingTemplate;
import org.apache.airavata.registry.entities.appcatalog.ParsingTemplateEntity;
import org.apache.airavata.registry.exceptions.RegistryException;
import org.apache.airavata.registry.repositories.appcatalog.ParsingTemplateRepository;
import org.apache.airavata.registry.utils.ObjectMapperSingleton;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ParsingTemplateService {
    @Autowired
    private ParsingTemplateRepository parsingTemplateRepository;

    public boolean isExists(String templateId) throws RegistryException {
        return parsingTemplateRepository.existsById(templateId);
    }

    public ParsingTemplate get(String templateId) throws RegistryException {
        ParsingTemplateEntity entity =
                parsingTemplateRepository.findById(templateId).orElse(null);
        if (entity == null) return null;
        Mapper mapper = ObjectMapperSingleton.getInstance();
        return mapper.map(entity, ParsingTemplate.class);
    }

    public ParsingTemplate create(ParsingTemplate parsingTemplate) throws RegistryException {
        Mapper mapper = ObjectMapperSingleton.getInstance();
        ParsingTemplateEntity entity = mapper.map(parsingTemplate, ParsingTemplateEntity.class);
        ParsingTemplateEntity saved = parsingTemplateRepository.save(entity);
        return mapper.map(saved, ParsingTemplate.class);
    }

    public List<ParsingTemplate> getParsingTemplatesForApplication(String applicationInterfaceId)
            throws RegistryException {
        List<ParsingTemplateEntity> entities =
                parsingTemplateRepository.findByApplicationInterfaceId(applicationInterfaceId);
        Mapper mapper = ObjectMapperSingleton.getInstance();
        return entities.stream().map(e -> mapper.map(e, ParsingTemplate.class)).collect(Collectors.toList());
    }

    public List<ParsingTemplate> getAllParsingTemplates(String gatewayId) throws RegistryException {
        List<ParsingTemplateEntity> entities = parsingTemplateRepository.findByGatewayId(gatewayId);
        Mapper mapper = ObjectMapperSingleton.getInstance();
        return entities.stream().map(e -> mapper.map(e, ParsingTemplate.class)).collect(Collectors.toList());
    }
}
