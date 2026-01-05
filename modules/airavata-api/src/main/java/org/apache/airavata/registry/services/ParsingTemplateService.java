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
import org.apache.airavata.common.model.ParsingTemplate;
import org.apache.airavata.registry.entities.appcatalog.ParsingTemplateEntity;
import org.apache.airavata.registry.exception.RegistryException;
import org.apache.airavata.registry.mappers.ParsingTemplateMapper;
import org.apache.airavata.registry.repositories.appcatalog.ParsingTemplateRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ParsingTemplateService {
    private final ParsingTemplateRepository parsingTemplateRepository;
    private final ParsingTemplateMapper parsingTemplateMapper;

    public ParsingTemplateService(
            ParsingTemplateRepository parsingTemplateRepository, ParsingTemplateMapper parsingTemplateMapper) {
        this.parsingTemplateRepository = parsingTemplateRepository;
        this.parsingTemplateMapper = parsingTemplateMapper;
    }

    public boolean isExists(String templateId) throws RegistryException {
        return parsingTemplateRepository.existsById(templateId);
    }

    public ParsingTemplate get(String templateId) throws RegistryException {
        ParsingTemplateEntity entity =
                parsingTemplateRepository.findById(templateId).orElse(null);
        if (entity == null) return null;
        return parsingTemplateMapper.toModel(entity);
    }

    public ParsingTemplate create(ParsingTemplate parsingTemplate) throws RegistryException {
        ParsingTemplateEntity entity = parsingTemplateMapper.toEntity(parsingTemplate);
        ParsingTemplateEntity saved = parsingTemplateRepository.save(entity);
        return parsingTemplateMapper.toModel(saved);
    }

    public List<ParsingTemplate> getParsingTemplatesForApplication(String applicationInterfaceId)
            throws RegistryException {
        List<ParsingTemplateEntity> entities =
                parsingTemplateRepository.findByApplicationInterfaceId(applicationInterfaceId);
        return parsingTemplateMapper.toModelList(entities);
    }

    public List<ParsingTemplate> getAllParsingTemplates(String gatewayId) throws RegistryException {
        List<ParsingTemplateEntity> entities = parsingTemplateRepository.findByGatewayId(gatewayId);
        return parsingTemplateMapper.toModelList(entities);
    }

    public void delete(String templateId) throws RegistryException {
        parsingTemplateRepository.deleteById(templateId);
    }
}
