/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
 package org.apache.airavata.registry.core.repositories.appcatalog;

import org.apache.airavata.model.appcatalog.computeresource.GroovyTemplate;
import org.apache.airavata.registry.core.entities.appcatalog.GroovyTemplateEntity;
import org.apache.airavata.registry.core.utils.DBConstants;
import org.apache.airavata.registry.core.utils.ObjectMapperSingleton;
import org.apache.airavata.registry.core.utils.QueryConstants;
import org.apache.airavata.registry.cpi.AppCatalogException;
import org.dozer.Mapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroovyTemplateRepository extends AppCatAbstractRepository<GroovyTemplate, GroovyTemplateEntity, String> {
    public GroovyTemplateRepository() {
        super(GroovyTemplate.class, GroovyTemplateEntity.class);
    }

    private String saveGroovyTemplate(GroovyTemplate template) {
        Mapper mapper = ObjectMapperSingleton.getInstance();
        GroovyTemplateEntity entity = mapper.map(template, GroovyTemplateEntity.class);
        GroovyTemplateEntity savedEntity = execute(entityManager -> entityManager.merge(entity));
        return savedEntity.getTemplateId();
    }

    public String addGroovyTemplate(GroovyTemplate template) throws AppCatalogException {
        return saveGroovyTemplate(template);
    }

    public String updateGroovyTemplate(GroovyTemplate template) throws AppCatalogException {
        if (! isExists(template.getTemplateId())) {
           throw new AppCatalogException("Groovy template with id " + template.getTemplateId() + " does not exist");
        }

        return saveGroovyTemplate(template);
    }

    public void removeGroovyTemplate(String templateId) throws AppCatalogException {
        if (! isExists(templateId)) {
            throw new AppCatalogException("Groovy template with id " + templateId + " does not exist");
        }

        delete(templateId);
    }

    public GroovyTemplate getGroovyTemplate(String templateId) throws AppCatalogException {
        if (! isExists(templateId)) {
            throw new AppCatalogException("Groovy template with id " + templateId + " does not exist");
        }

        return get(templateId);
    }

    public List<GroovyTemplate> getGroovyTemplatesForResourceJobManager(String resourceJobManagerType) {
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put(DBConstants.GroovyTemplate.RESOURCE_JOB_MANAGER_TYPE, resourceJobManagerType);
        return select(QueryConstants.FIND_ALL_GROOVY_TEMPLATES_FOR_RESOURCE_JOB_MANAGER, -1, 0, queryParameters);
    }
}
