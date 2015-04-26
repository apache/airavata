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

import org.airavata.appcatalog.cpi.AppCatalog;
import org.airavata.appcatalog.cpi.AppCatalogException;
import org.apache.aiaravata.application.catalog.data.impl.AppCatalogFactory;
import org.apache.airavata.model.appcatalog.computeresource.BatchQueue;
import org.apache.airavata.model.appcatalog.computeresource.ComputeResourceDescription;
import org.apache.airavata.model.error.ValidationResults;
import org.apache.airavata.model.error.ValidatorResult;
import org.apache.airavata.model.workspace.experiment.*;
import org.apache.airavata.orchestrator.core.validator.JobMetadataValidator;
import org.apache.airavata.persistance.registry.jpa.impl.RegistryFactory;
import org.apache.airavata.registry.cpi.Registry;
import org.apache.airavata.registry.cpi.RegistryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class SimpleAppDataValidator implements JobMetadataValidator {
    private final static Logger logger = LoggerFactory.getLogger(SimpleAppDataValidator.class);

    private Registry registry;
    private AppCatalog appCatalog;

    public SimpleAppDataValidator() {
        try {
            this.registry = RegistryFactory.getDefaultRegistry();
            this.appCatalog = AppCatalogFactory.getAppCatalog();
        } catch (RegistryException e) {
            logger.error("Unable to initialize registry", e);
        } catch (AppCatalogException e) {
            logger.error("Unable to initialize Application Catalog", e);
        }
    }

    public ValidationResults validate(Experiment experiment, WorkflowNodeDetails workflowNodeDetail, TaskDetails taskID) {
        ValidationResults validationResults = new ValidationResults();
        validationResults.setValidationState(true);
        try {
            List<ValidatorResult> validatorResultList = validateUserConfiguration(experiment, taskID);
            for (ValidatorResult result : validatorResultList){
                if (!result.isResult()){
                    validationResults.setValidationState(false);
                    break;
                }
            }
            validationResults.setValidationResultList(validatorResultList);
        } catch (AppCatalogException e) {
            logger.error("Error while validating user configuration", e);
        }
        return validationResults;
    }

    private List<ValidatorResult> validateUserConfiguration (Experiment experiment, TaskDetails taskDetail) throws AppCatalogException{
        List<ValidatorResult> validatorResultList = new ArrayList<ValidatorResult>();
        try {
            UserConfigurationData userConfigurationData = experiment.getUserConfigurationData();
            ComputationalResourceScheduling computationalResourceScheduling = userConfigurationData.getComputationalResourceScheduling();
            if (userConfigurationData.isAiravataAutoSchedule()) {
                logger.error("We dont' support auto scheduling at this point, We will simply use user data as it is");
                ValidatorResult validatorResult = new ValidatorResult();
                validatorResult.setResult(false);
                validatorResultList.add(validatorResult);
            }
            ComputeResourceDescription computeResource = appCatalog.getComputeResource().getComputeResource(taskDetail.getTaskScheduling().getResourceHostId());
            List<BatchQueue> batchQueues = computeResource.getBatchQueues();

            if (batchQueues != null && !batchQueues.isEmpty()){
                if (computationalResourceScheduling != null){
                    String queueName = computationalResourceScheduling.getQueueName();
                    int wallTimeLimit = computationalResourceScheduling.getWallTimeLimit();
                    ValidatorResult queueNameResult = new ValidatorResult();

                    queueNameResult.setResult(false);
                    queueNameResult.setErrorDetails("Unable to find queue name from appcatalog configured queues.. Either you " +
                            "specified a wrong queue name or you did not configure app catalog correctly...");
                    for (BatchQueue queue : batchQueues){
                        String configuredQname = queue.getQueueName();
                        int maxRunTime = queue.getMaxRunTime();
                        if (configuredQname != null && configuredQname.equals(queueName)){
                            ValidatorResult wallTimeResult = new ValidatorResult();
                            wallTimeResult.setResult(false);
                            wallTimeResult.setErrorDetails("Provided walltime period exceeds max walltime configured..");
                            queueNameResult.setResult(true);
                            queueNameResult.setErrorDetails("");
                            if (maxRunTime != 0 && maxRunTime > wallTimeLimit){
                                wallTimeResult.setResult(true);
                                wallTimeResult.setErrorDetails("");
                            }
                            validatorResultList.add(wallTimeResult);
                        }
                    }
                    validatorResultList.add(queueNameResult);

                }
            } else {
                // for some compute resources, you dnt need to specify queue names
                ValidatorResult result = new ValidatorResult();
                logger.info("There are not queues defined under the compute resource. Airavata assumes this experiment " +
                        "does not need a queue name...");
                result.setResult(true);
                validatorResultList.add(result);
            }
        } catch (AppCatalogException e) {
            logger.error("Error while getting information from App catalog", e);
            throw new AppCatalogException("Error while getting information from App catalog", e);
        }
        return validatorResultList;
    }
}
