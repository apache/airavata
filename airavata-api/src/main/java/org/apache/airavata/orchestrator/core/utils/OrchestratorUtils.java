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
package org.apache.airavata.orchestrator.core.utils;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.apache.airavata.common.exception.AiravataException;
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.DBUtil;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.credential.store.CredentialReader;
import org.apache.airavata.credential.store.impl.CredentialReaderImpl;
import org.apache.airavata.model.appcatalog.appinterface.ApplicationInterfaceDescription;
import org.apache.airavata.model.appcatalog.computeresource.CloudJobSubmission;
import org.apache.airavata.model.appcatalog.computeresource.ComputeResourceDescription;
import org.apache.airavata.model.appcatalog.computeresource.JobSubmissionInterface;
import org.apache.airavata.model.appcatalog.computeresource.JobSubmissionProtocol;
import org.apache.airavata.model.appcatalog.computeresource.LOCALSubmission;
import org.apache.airavata.model.appcatalog.computeresource.SSHJobSubmission;
import org.apache.airavata.model.appcatalog.computeresource.UnicoreJobSubmission;
import org.apache.airavata.model.appcatalog.gatewayprofile.StoragePreference;
import org.apache.airavata.model.appcatalog.groupresourceprofile.GroupComputeResourcePreference;
import org.apache.airavata.model.appcatalog.userresourceprofile.UserComputeResourcePreference;
import org.apache.airavata.model.data.movement.DataMovementInterface;
import org.apache.airavata.model.data.movement.DataMovementProtocol;
import org.apache.airavata.model.data.movement.SCPDataMovement;
import org.apache.airavata.model.data.movement.SecurityProtocol;
import org.apache.airavata.model.process.ProcessModel;
import org.apache.airavata.model.scheduling.ComputationalResourceSchedulingModel;
import org.apache.airavata.orchestrator.core.OrchestratorConfiguration;
import org.apache.airavata.orchestrator.core.exception.OrchestratorException;
import org.apache.airavata.registry.api.exception.RegistryServiceException;
import org.apache.airavata.service.RegistryService;
import org.apache.airavata.service.ServiceFactory;
import org.apache.airavata.service.ServiceFactoryException;
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

    public static JobSubmissionProtocol getPreferredJobSubmissionProtocol(ProcessModel model, String gatewayId)
            throws RegistryServiceException, OrchestratorException, ServiceFactoryException {
        return getPreferredJobSubmissionInterface(model, gatewayId).getJobSubmissionProtocol();
    }

    public static GroupComputeResourcePreference getGroupComputeResourcePreference(ProcessModel model)
            throws RegistryServiceException, ServiceFactoryException {
        final RegistryService registryService = ServiceFactory.getInstance().getRegistryService();
        return registryService.getGroupComputeResourcePreference(
                model.getComputeResourceId(), model.getGroupResourceProfileId());
    }

    public static String getApplicationInterfaceName(ProcessModel model)
            throws RegistryServiceException, OrchestratorException, ServiceFactoryException {
        final RegistryService registryService = ServiceFactory.getInstance().getRegistryService();
        ApplicationInterfaceDescription appInterface =
                registryService.getApplicationInterface(model.getApplicationInterfaceId());
        return appInterface.getApplicationName();
    }

    public static DataMovementProtocol getPreferredDataMovementProtocol(ProcessModel model, String gatewayId)
            throws RegistryServiceException, OrchestratorException, ServiceFactoryException {
        return getPreferredDataMovementInterface(model, gatewayId).getDataMovementProtocol();
    }

    public static StoragePreference getStoragePreference(ProcessModel processModel, String gatewayId)
            throws OrchestratorException, ServiceFactoryException {
        final RegistryService registryService = ServiceFactory.getInstance().getRegistryService();
        try {
            String resourceHostId = processModel.getComputeResourceId();
            return registryService.getGatewayStoragePreference(gatewayId, resourceHostId);
        } catch (Exception e) {
            logger.error("Error occurred while retrieving StoragePreference", e);
            throw new OrchestratorException("Error occurred while retrieving StoragePreference", e);
        }
    }

    public static String getLoginUserName(ProcessModel processModel, String gatewayId)
            throws AiravataException, RegistryServiceException, ServiceFactoryException {
        final RegistryService registryService = ServiceFactory.getInstance().getRegistryService();
        GroupComputeResourcePreference computeResourcePreference = getGroupComputeResourcePreference(processModel);
        ComputationalResourceSchedulingModel processResourceSchedule = processModel.getProcessResourceSchedule();
        if (processModel.isUseUserCRPref()) {
            UserComputeResourcePreference userComputeResourcePreference =
                    registryService.getUserComputeResourcePreference(
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
    }

    public static String getScratchLocation(ProcessModel processModel, String gatewayId)
            throws AiravataException, RegistryServiceException, ServiceFactoryException {
        final RegistryService registryService = ServiceFactory.getInstance().getRegistryService();
        GroupComputeResourcePreference computeResourcePreference = getGroupComputeResourcePreference(processModel);
        ComputationalResourceSchedulingModel processResourceSchedule = processModel.getProcessResourceSchedule();
        String scratchLocation = computeResourcePreference.getScratchLocation();

        if (processModel.isUseUserCRPref()) {
            UserComputeResourcePreference userComputeResourcePreference =
                    registryService.getUserComputeResourcePreference(
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
    }

    public static JobSubmissionInterface getPreferredJobSubmissionInterface(ProcessModel processModel, String gatewayId)
            throws OrchestratorException, ServiceFactoryException {
        final RegistryService registryService = ServiceFactory.getInstance().getRegistryService();
        try {
            String resourceHostId = processModel.getComputeResourceId();
            ComputeResourceDescription resourceDescription = registryService.getComputeResource(resourceHostId);
            List<JobSubmissionInterface> jobSubmissionInterfaces = resourceDescription.getJobSubmissionInterfaces();
            if (jobSubmissionInterfaces != null && !jobSubmissionInterfaces.isEmpty()) {
                Collections.sort(
                        jobSubmissionInterfaces, Comparator.comparingInt(JobSubmissionInterface::getPriorityOrder));
            } else {
                throw new OrchestratorException(
                        "Compute resource should have at least one job submission interface defined...");
            }
            return jobSubmissionInterfaces.get(0);
        } catch (Exception e) {
            throw new OrchestratorException("Error occurred while retrieving data from app catalog", e);
        }
    }

    public static DataMovementInterface getPreferredDataMovementInterface(ProcessModel processModel, String gatewayId)
            throws OrchestratorException, ServiceFactoryException {
        final RegistryService registryService = ServiceFactory.getInstance().getRegistryService();
        try {
            String resourceHostId = processModel.getComputeResourceId();
            ComputeResourceDescription resourceDescription = registryService.getComputeResource(resourceHostId);
            List<DataMovementInterface> dataMovementInterfaces = resourceDescription.getDataMovementInterfaces();
            if (dataMovementInterfaces != null && !dataMovementInterfaces.isEmpty()) {
                Collections.sort(
                        dataMovementInterfaces, Comparator.comparingInt(DataMovementInterface::getPriorityOrder));
            } else {
                throw new OrchestratorException(
                        "Compute resource should have at least one data movement interface defined...");
            }
            return dataMovementInterfaces.get(0);
        } catch (Exception e) {
            throw new OrchestratorException("Error occurred while retrieving data from app catalog", e);
        }
    }

    public static int getDataMovementPort(ProcessModel processModel, String gatewayId)
            throws RegistryServiceException, ApplicationSettingsException, OrchestratorException {
        try {
            DataMovementProtocol protocol = getPreferredDataMovementProtocol(processModel, gatewayId);
            DataMovementInterface dataMovementInterface = getPreferredDataMovementInterface(processModel, gatewayId);
            if (protocol == DataMovementProtocol.SCP) {
                SCPDataMovement scpDataMovement =
                        getSCPDataMovement(dataMovementInterface.getDataMovementInterfaceId());
                if (scpDataMovement != null) {
                    return scpDataMovement.getSshPort();
                }
            }
        } catch (Exception e) {
            logger.error("Error occurred while retrieving security protocol", e);
        }
        return 0;
    }

    public static SecurityProtocol getSecurityProtocol(ProcessModel processModel, String gatewayId)
            throws RegistryServiceException, ApplicationSettingsException, OrchestratorException,
                    ServiceFactoryException {
        try {
            JobSubmissionProtocol submissionProtocol = getPreferredJobSubmissionProtocol(processModel, gatewayId);
            JobSubmissionInterface jobSubmissionInterface = getPreferredJobSubmissionInterface(processModel, gatewayId);
            if (submissionProtocol == JobSubmissionProtocol.SSH) {
                SSHJobSubmission sshJobSubmission =
                        getSSHJobSubmission(jobSubmissionInterface.getJobSubmissionInterfaceId());
                if (sshJobSubmission != null) {
                    return sshJobSubmission.getSecurityProtocol();
                }
            } else if (submissionProtocol == JobSubmissionProtocol.LOCAL) {
                LOCALSubmission localJobSubmission =
                        getLocalJobSubmission(jobSubmissionInterface.getJobSubmissionInterfaceId());
                if (localJobSubmission != null) {
                    return localJobSubmission.getSecurityProtocol();
                }
            } else if (submissionProtocol == JobSubmissionProtocol.SSH_FORK) {
                SSHJobSubmission sshJobSubmission =
                        getSSHJobSubmission(jobSubmissionInterface.getJobSubmissionInterfaceId());
                if (sshJobSubmission != null) {
                    return sshJobSubmission.getSecurityProtocol();
                }
            } else if (submissionProtocol == JobSubmissionProtocol.CLOUD) {
                CloudJobSubmission cloudJobSubmission =
                        getCloudJobSubmission(jobSubmissionInterface.getJobSubmissionInterfaceId());
                if (cloudJobSubmission != null) {
                    return cloudJobSubmission.getSecurityProtocol();
                }
            }
        } catch (OrchestratorException e) {
            logger.error("Error occurred while retrieving security protocol", e);
        }
        return null;
    }

    public static LOCALSubmission getLocalJobSubmission(String submissionId)
            throws OrchestratorException, ServiceFactoryException {
        final RegistryService registryService = ServiceFactory.getInstance().getRegistryService();
        try {
            return registryService.getLocalJobSubmission(submissionId);
        } catch (Exception e) {
            String errorMsg = "Error while retrieving local job submission with submission id : " + submissionId;
            logger.error(errorMsg, e);
            throw new OrchestratorException(errorMsg, e);
        }
    }

    public static UnicoreJobSubmission getUnicoreJobSubmission(String submissionId)
            throws OrchestratorException, ServiceFactoryException {
        final RegistryService registryService = ServiceFactory.getInstance().getRegistryService();
        try {
            return registryService.getUnicoreJobSubmission(submissionId);
        } catch (Exception e) {
            String errorMsg = "Error while retrieving UNICORE job submission with submission id : " + submissionId;
            logger.error(errorMsg, e);
            throw new OrchestratorException(errorMsg, e);
        }
    }

    public static SSHJobSubmission getSSHJobSubmission(String submissionId)
            throws OrchestratorException, ServiceFactoryException {
        final RegistryService registryService = ServiceFactory.getInstance().getRegistryService();
        try {
            return registryService.getSSHJobSubmission(submissionId);
        } catch (Exception e) {
            String errorMsg = "Error while retrieving SSH job submission with submission id : " + submissionId;
            logger.error(errorMsg, e);
            throw new OrchestratorException(errorMsg, e);
        }
    }

    public static CloudJobSubmission getCloudJobSubmission(String submissionId)
            throws OrchestratorException, ServiceFactoryException {
        final RegistryService registryService = ServiceFactory.getInstance().getRegistryService();
        try {
            return registryService.getCloudJobSubmission(submissionId);
        } catch (Exception e) {
            String errorMsg = "Error while retrieving SSH job submission with submission id : " + submissionId;
            logger.error(errorMsg, e);
            throw new OrchestratorException(errorMsg, e);
        }
    }

    public static SCPDataMovement getSCPDataMovement(String dataMoveId)
            throws OrchestratorException, ServiceFactoryException {
        final RegistryService registryService = ServiceFactory.getInstance().getRegistryService();
        try {
            return registryService.getSCPDataMovement(dataMoveId);
        } catch (Exception e) {
            String errorMsg = "Error while retrieving SCP Data movement with submission id : " + dataMoveId;
            logger.error(errorMsg, e);
            throw new OrchestratorException(errorMsg, e);
        }
    }

    private static boolean isValid(String str) {
        return (str != null && !str.trim().isEmpty());
    }

    public static CredentialReader getCredentialReader() {
        try {
            String jdbcUrl = ServerSettings.getCredentialStoreDBURL();
            String jdbcUsr = ServerSettings.getCredentialStoreDBUser();
            String jdbcPass = ServerSettings.getCredentialStoreDBPassword();
            String driver = ServerSettings.getCredentialStoreDBDriver();
            return new CredentialReaderImpl(new DBUtil(jdbcUrl, jdbcUsr, jdbcPass, driver));
        } catch (ApplicationSettingsException e) {
            logger.error("Error while getting credential reader", e);
            return null;
        }
    }
}
