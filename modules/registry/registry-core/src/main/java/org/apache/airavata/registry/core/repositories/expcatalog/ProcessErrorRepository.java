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

import org.apache.airavata.registry.core.entities.expcatalog.ProcessErrorEntity;
import org.apache.airavata.registry.core.entities.expcatalog.ProcessErrorPK;
import org.apache.airavata.model.commons.ErrorModel;
import org.apache.airavata.model.process.ProcessModel;
import org.apache.airavata.registry.core.utils.ExpCatalogUtils;
import org.apache.airavata.registry.core.utils.ObjectMapperSingleton;
import org.apache.airavata.registry.cpi.RegistryException;
import org.dozer.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class ProcessErrorRepository extends ExpCatAbstractRepository<ErrorModel, ProcessErrorEntity, ProcessErrorPK> {
    private final static Logger logger = LoggerFactory.getLogger(ProcessErrorRepository.class);

    public ProcessErrorRepository() { super(ErrorModel.class, ProcessErrorEntity.class); }

    protected String saveProcessError(ErrorModel error, String processId) throws RegistryException {
        Mapper mapper = ObjectMapperSingleton.getInstance();
        ProcessErrorEntity processErrorEntity = mapper.map(error, ProcessErrorEntity.class);

        if (processErrorEntity.getProcessId() == null) {
            logger.debug("Setting the ProcessErrorEntity's ProcessId");
            processErrorEntity.setProcessId(processId);
        }

        execute(entityManager -> entityManager.merge(processErrorEntity));
        return processErrorEntity.getErrorId();
    }

    public String addProcessError(ErrorModel processError, String processId) throws RegistryException {

        if (processError.getErrorId() == null) {
            logger.debug("Setting the ProcessError's ErrorId");
            processError.setErrorId(ExpCatalogUtils.getID("ERROR"));
        }

        return saveProcessError(processError, processId);
    }

    public String updateProcessError(ErrorModel updatedProcessError, String processId) throws RegistryException {
        return saveProcessError(updatedProcessError, processId);
    }

    public List<ErrorModel> getProcessError(String processId) throws RegistryException {
        ProcessRepository processRepository = new ProcessRepository();
        ProcessModel processModel = processRepository.getProcess(processId);
        return processModel.getProcessErrors();
    }

}
