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
package org.apache.airavata.orchestration.util;

import java.io.IOException;
import java.util.Arrays;
import org.apache.airavata.config.ServerSettings;
import org.apache.airavata.exception.AiravataException;
import org.apache.airavata.exception.ApplicationSettingsException;
import org.apache.airavata.interfaces.RegistryHandler;
import org.apache.airavata.model.appcatalog.appinterface.proto.ApplicationInterfaceDescription;
import org.apache.airavata.model.appcatalog.gatewayprofile.proto.StoragePreference;
import org.apache.airavata.model.appcatalog.groupresourceprofile.proto.GroupComputeResourcePreference;
import org.apache.airavata.model.appcatalog.userresourceprofile.proto.UserComputeResourcePreference;
import org.apache.airavata.model.process.proto.ProcessModel;
import org.apache.airavata.model.scheduling.proto.ComputationalResourceSchedulingModel;
import org.apache.airavata.orchestration.service.OrchestratorConfiguration;
import org.apache.airavata.orchestration.service.OrchestratorException;
import org.apache.airavata.task.SchedulerUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This contains orchestrator specific utilities
 */
public class OrchestratorUtils {
    private static final Logger logger = LoggerFactory.getLogger(OrchestratorUtils.class);

    public static OrchestratorConfiguration loadOrchestratorConfiguration()
            throws OrchestratorException, IOException, NumberFormatException, ApplicationSettingsException {

        OrchestratorConfiguration orchestratorConfiguration = new OrchestratorConfiguration();
        orchestratorConfiguration.setEnableValidation(
                Boolean.parseBoolean(ServerSettings.getSetting(OrchestratorConstants.ENABLE_VALIDATION)));
        if (orchestratorConfiguration.isEnableValidation()) {
            orchestratorConfiguration.setValidatorClasses(
                    Arrays.asList(ServerSettings.getSetting(OrchestratorConstants.JOB_VALIDATOR)
                            .split(",")));
        }
        return orchestratorConfiguration;
    }

    public static GroupComputeResourcePreference getGroupComputeResourcePreference(ProcessModel model)
            throws Exception {
        return getRegistryHandler()
                .getGroupComputeResourcePreference(model.getComputeResourceId(), model.getGroupResourceProfileId());
    }

    public static String getApplicationInterfaceName(ProcessModel model) throws Exception, OrchestratorException {
        try {
            ApplicationInterfaceDescription appInterface =
                    getRegistryHandler().getApplicationInterface(model.getApplicationInterfaceId());
            return appInterface.getApplicationName();
        } catch (Exception e) {
            throw new OrchestratorException("Error while retrieving application interface", e);
        }
    }

    public static StoragePreference getStoragePreference(ProcessModel processModel, String gatewayId)
            throws OrchestratorException {
        try {
            // Storage preferences are keyed by STORAGE resource id (the experiment's output/input
            // storage), not the compute resource id.
            String storageResourceId = processModel.getOutputStorageResourceId();
            if (storageResourceId == null || storageResourceId.isEmpty()) {
                storageResourceId = processModel.getInputStorageResourceId();
            }
            return getRegistryHandler().getGatewayStoragePreference(gatewayId, storageResourceId);
        } catch (Exception e) {
            logger.error("Error occurred while retrieving StoragePreference", e);
            throw new OrchestratorException("Error occurred while retrieving StoragePreference", e);
        }
    }

    public static String getLoginUserName(ProcessModel processModel, String gatewayId)
            throws AiravataException, Exception {
        try {
            RegistryHandler registryHandler = getRegistryHandler();
            GroupComputeResourcePreference computeResourcePreference = getGroupComputeResourcePreference(processModel);
            ComputationalResourceSchedulingModel processResourceSchedule = processModel.getProcessResourceSchedule();
            if (processModel.getUseUserCrPref()) {
                UserComputeResourcePreference userComputeResourcePreference =
                        registryHandler.getUserComputeResourcePreference(
                                processModel.getUserName(), gatewayId, processModel.getComputeResourceId());
                if (isValid(userComputeResourcePreference.getLoginUserName())) {
                    return userComputeResourcePreference.getLoginUserName();
                } else if (isValid(processResourceSchedule.getOverrideLoginUserName())) {
                    logger.warn("User computer resource preference doesn't have valid user login name, using computer "
                            + "resource scheduling login name " + processResourceSchedule.getOverrideLoginUserName());
                    return processResourceSchedule.getOverrideLoginUserName();
                } else if (isValid(computeResourcePreference.getLoginUserName())) {
                    logger.warn("Either User computer resource preference or computer resource scheduling "
                            + "doesn't have valid user login name, using  group resource profile computer resource preference login name "
                            + computeResourcePreference.getLoginUserName());
                    return computeResourcePreference.getLoginUserName();
                } else {
                    throw new AiravataException("Login name is not found");
                }
            } else {
                if (isValid(processResourceSchedule.getOverrideLoginUserName())) {
                    return processResourceSchedule.getOverrideLoginUserName();
                } else if (isValid(computeResourcePreference.getLoginUserName())) {
                    logger.warn("Process compute resource scheduling doesn't have valid user login name, "
                            + "using  gateway computer resource preference login name "
                            + computeResourcePreference.getLoginUserName());
                    return computeResourcePreference.getLoginUserName();
                } else {
                    throw new AiravataException("Login name is not found");
                }
            }
        } catch (ApplicationSettingsException e) {
            logger.error("Error occurred while initializing app catalog to fetch login username", e);
            throw new ApplicationSettingsException(
                    "Error occurred while initializing app catalog to fetch login username", e);
        }
    }

    public static String getScratchLocation(ProcessModel processModel, String gatewayId)
            throws AiravataException, Exception {
        try {
            RegistryHandler registryHandler = getRegistryHandler();
            GroupComputeResourcePreference computeResourcePreference = getGroupComputeResourcePreference(processModel);
            ComputationalResourceSchedulingModel processResourceSchedule = processModel.getProcessResourceSchedule();
            String scratchLocation = computeResourcePreference.getScratchLocation();

            if (processModel.getUseUserCrPref()) {
                UserComputeResourcePreference userComputeResourcePreference =
                        registryHandler.getUserComputeResourcePreference(
                                processModel.getUserName(), gatewayId, processModel.getComputeResourceId());
                if (isValid(userComputeResourcePreference.getScratchLocation())) {
                    return userComputeResourcePreference.getScratchLocation();
                } else if (isValid(processResourceSchedule.getOverrideScratchLocation())) {
                    logger.warn("User computer resource preference doesn't have valid scratch location, using computer "
                            + "resource scheduling scratch location "
                            + processResourceSchedule.getOverrideScratchLocation());
                    return processResourceSchedule.getOverrideScratchLocation();
                } else if (isValid(scratchLocation)) {
                    logger.warn("Either User computer resource preference or computer resource scheduling doesn't have "
                            + "valid scratch location, using  gateway computer resource preference scratch location "
                            + scratchLocation);
                    return scratchLocation;
                } else {
                    throw new AiravataException("Scratch location is not found");
                }
            } else {
                if (isValid(processResourceSchedule.getOverrideScratchLocation())) {
                    return processResourceSchedule.getOverrideScratchLocation();
                } else if (isValid(scratchLocation)) {
                    logger.warn("Process compute resource scheduling doesn't have valid scratch location, "
                            + "using  gateway computer resource preference scratch location "
                            + scratchLocation);
                    return scratchLocation;
                } else {
                    throw new AiravataException("Scratch location is not found");
                }
            }
        } catch (AiravataException e) {
            logger.error("Error occurred while initializing app catalog to fetch scratch location", e);
            throw new AiravataException("Error occurred while initializing app catalog to fetch scratch location", e);
        }
    }

    private static boolean isValid(String str) {
        return (str != null && !str.trim().isEmpty());
    }

    private static RegistryHandler getRegistryHandler() {
        return SchedulerUtils.getRegistryHandler();
    }
}
