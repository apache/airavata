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
package org.apache.airavata.orchestrator.core.validator.impl;

import org.apache.airavata.model.error.ValidatorResult;
import org.apache.airavata.model.workspace.experiment.Experiment;
import org.apache.airavata.model.workspace.experiment.TaskDetails;
import org.apache.airavata.model.workspace.experiment.WorkflowNodeDetails;
import org.apache.airavata.orchestrator.core.exception.OrchestratorException;
import org.apache.airavata.orchestrator.core.validator.JobMetadataValidator;
import org.apache.airavata.persistance.registry.jpa.impl.RegistryFactory;
import org.apache.airavata.registry.cpi.Registry;
import org.apache.airavata.registry.cpi.RegistryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleAppDataValidator implements JobMetadataValidator {
    private final static Logger logger = LoggerFactory.getLogger(SimpleAppDataValidator.class);

    private Registry registry;

    public SimpleAppDataValidator() {
        try {
            this.registry = RegistryFactory.getDefaultRegistry();
        } catch (RegistryException e) {
            logger.error("Unable to initialize registry", e);
        }
    }

    public ValidatorResult validate(Experiment experiment, WorkflowNodeDetails workflowNodeDetail, TaskDetails taskID) {
        boolean result = false;
        if (experiment.getUserConfigurationData().isAiravataAutoSchedule()) {
            logger.error("We dont' support auto scheduling at this point, We will simply use user data as it is");
        }
        return new ValidatorResult(true);
    }
}
