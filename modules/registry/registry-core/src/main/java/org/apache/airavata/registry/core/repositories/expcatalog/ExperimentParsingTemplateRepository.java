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
 *
 */
 package org.apache.airavata.registry.core.repositories.expcatalog;

import org.apache.airavata.model.experiment.ExperimentParsingTemplate;
import org.apache.airavata.registry.core.entities.expcatalog.ExperimentParsingTemplateEntity;
import org.apache.airavata.registry.core.entities.expcatalog.ExperimentParsingTemplatePK;
import org.apache.airavata.registry.core.utils.DBConstants;
import org.apache.airavata.registry.core.utils.ObjectMapperSingleton;
import org.apache.airavata.registry.core.utils.QueryConstants;
import org.dozer.Mapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ExperimentParsingTemplateRepository extends
                ExpCatAbstractRepository<ExperimentParsingTemplate, ExperimentParsingTemplateEntity, ExperimentParsingTemplatePK> {

    public ExperimentParsingTemplateRepository() {
        super(ExperimentParsingTemplate.class, ExperimentParsingTemplateEntity.class);
    }

    public List<ExperimentParsingTemplate> getAllParsingTemplatesForExperiment(String experimentId) {
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put(DBConstants.ExperimentParsingTemplate.EXPERIMENT_ID, experimentId);
        return select(QueryConstants.GET_ALL_PARSING_TEMPLATES_FOR_EXPERIMENT, -1, 0, queryParameters);
    }

    public void addParsingTemplatesForExperiment(List<String> templates, String experimentId) {
        List<ExperimentParsingTemplate> oldEntries = getAllParsingTemplatesForExperiment(experimentId);
        for (ExperimentParsingTemplate oldEntry: oldEntries) {
            ExperimentParsingTemplatePK pk = new ExperimentParsingTemplatePK();
            pk.setExperimentId(experimentId);
            pk.setParsingTemplateId(oldEntry.getParsingTemplateId());
            delete(pk);
        }

        List<ExperimentParsingTemplate> parsingTemplates = templates.stream().map(id -> {
            ExperimentParsingTemplate ept = new ExperimentParsingTemplate();
            ept.setExperimentId(experimentId);
            ept.setParsingTemplateId(id);
            return ept;
        }).collect(Collectors.toList());

        for (ExperimentParsingTemplate template: parsingTemplates) {

            Mapper mapper = ObjectMapperSingleton.getInstance();
            ExperimentParsingTemplateEntity ptEntity = mapper.map(template, ExperimentParsingTemplateEntity.class);
            execute(entityManager -> entityManager.merge(ptEntity));
        }
    }
}
