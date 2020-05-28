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

import org.apache.airavata.model.application.io.OutputDataValueObjectType;
import org.apache.airavata.registry.core.entities.expcatalog.ExperimentOutputValueEntity;
import org.apache.airavata.registry.core.entities.expcatalog.ExperimentOutputValuePK;
import org.apache.airavata.registry.core.utils.DBConstants;
import org.apache.airavata.registry.core.utils.ObjectMapperSingleton;
import org.apache.airavata.registry.core.utils.QueryConstants;
import org.apache.airavata.registry.cpi.RegistryException;
import org.dozer.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExperimentOutputValueRepository extends ExpCatAbstractRepository<OutputDataValueObjectType, ExperimentOutputValueEntity, ExperimentOutputValuePK> {
    private final static Logger logger = LoggerFactory.getLogger(ExperimentOutputValueRepository.class);

    public ExperimentOutputValueRepository() {
        super(OutputDataValueObjectType.class, ExperimentOutputValueEntity.class);
    }

    protected void saveExperimentOutputValues(List<OutputDataValueObjectType> experimentOutputValues, String experimentId) throws RegistryException {

        for (OutputDataValueObjectType output : experimentOutputValues) {
            Mapper mapper = ObjectMapperSingleton.getInstance();
            ExperimentOutputValueEntity experimentOutputValueEntity = mapper.map(output, ExperimentOutputValueEntity.class);

            if (experimentOutputValueEntity.getExperimentId() == null) {
                logger.debug("Setting the ExperimentOutputValueEntity's ExperimentId");
                experimentOutputValueEntity.setExperimentId(experimentId);
            }
            execute(entityManager -> entityManager.merge(experimentOutputValueEntity));
        }
    }

    public String addExperimentOutputValues(List<OutputDataValueObjectType> experimentOutputValues, String experimentId) throws RegistryException {
        saveExperimentOutputValues(experimentOutputValues, experimentId);
        return experimentId;
    }

    public void updateExperimentOutputValues(List<OutputDataValueObjectType> updatedExperimentOutputValues, String experimentId) throws RegistryException {
        saveExperimentOutputValues(updatedExperimentOutputValues, experimentId);
    }

    public List<OutputDataValueObjectType> getExperimentOutputValues(String experimentId, String name) throws RegistryException {
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put(DBConstants.ExperimentOutputValue.EXPERIMENT_ID, experimentId);
        queryParameters.put(DBConstants.ExperimentOutputValue.NAME, name);
        return select(QueryConstants.FIND_EXPERIMENT_OUTPUT_VALUES, -1, 0, queryParameters);
    }
}
