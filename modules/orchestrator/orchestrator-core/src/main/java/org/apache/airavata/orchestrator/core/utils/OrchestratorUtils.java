/**
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
package org.apache.airavata.orchestrator.core.utils;

import org.apache.airavata.common.exception.AiravataException;
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.DBUtil;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.common.utils.ThriftUtils;
import org.apache.airavata.credential.store.store.CredentialReader;
import org.apache.airavata.credential.store.store.impl.CredentialReaderImpl;
import org.apache.airavata.model.appcatalog.appinterface.ApplicationInterfaceDescription;
import org.apache.airavata.model.appcatalog.computeresource.*;
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
import org.apache.airavata.registry.api.RegistryService;
import org.apache.airavata.registry.api.client.RegistryServiceClientFactory;
import org.apache.airavata.registry.api.exception.RegistryServiceException;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

/**
 * This contains orchestrator specific utilities
 */
public class OrchestratorUtils {
    private final static Logger logger = LoggerFactory.getLogger(OrchestratorUtils.class);

    public static OrchestratorConfiguration loadOrchestratorConfiguration()
            throws OrchestratorException, IOException, NumberFormatException, ApplicationSettingsException {

        OrchestratorConfiguration orchestratorConfiguration = new OrchestratorConfiguration();
        orchestratorConfiguration.setSubmitterInterval(
                Integer.parseInt(ServerSettings.getSetting(OrchestratorConstants.SUBMIT_INTERVAL)));
        orchestratorConfiguration.setThreadPoolSize(
                Integer.parseInt(ServerSettings.getSetting(OrchestratorConstants.THREAD_POOL_SIZE)));
        orchestratorConfiguration.setStartSubmitter(
                Boolean.valueOf(ServerSettings.getSetting(OrchestratorConstants.START_SUBMITTER)));
        orchestratorConfiguration.setEmbeddedMode(
                Boolean.valueOf(ServerSettings.getSetting(OrchestratorConstants.EMBEDDED_MODE)));
        orchestratorConfiguration.setEnableValidation(
                Boolean.valueOf(ServerSettings.getSetting(OrchestratorConstants.ENABLE_VALIDATION)));
        if (orchestratorConfiguration.isEnableValidation()) {
            orchestratorConfiguration.setValidatorClasses(
                    Arrays.asList(ServerSettings.getSetting(OrchestratorConstants.JOB_VALIDATOR).split(",")));
        }
        return orchestratorConfiguration;
    }

    public static JobSubmissionProtocol getPreferredJobSubmissionProtocol(ProcessModel model,
                                                                          String gatewayId) throws TException, OrchestratorException {
        return getPreferredJobSubmissionInterface(model, gatewayId).getJobSubmissionProtocol();
    }

    public static GroupComputeResourcePreference getGroupComputeResourcePreference(ProcessModel model) throws TException {
        final RegistryService.Client registryClient = getRegistryServiceClient();
        try {
            return registryClient.getGroupComputeResourcePreference(model.getComputeResourceId(), model.getGroupResourceProfileId());
        } finally {
            if (registryClient != null) {
                ThriftUtils.close(registryClient);
            }
        }
    }

    public static String getApplicationInterfaceName(ProcessModel model)
            throws TException, OrchestratorException {
        final RegistryService.Client registryClient = getRegistryServiceClient();
        try {
            ApplicationInterfaceDescription appInterface = registryClient.getApplicationInterface(model.getApplicationInterfaceId());
            return appInterface.getApplicationName();
        } catch (Exception e) {
            throw new OrchestratorException("Error while retrieving application interface", e);
        } finally {
            if (registryClient != null) {
                ThriftUtils.close(registryClient);
            }
        }
    }

    public static DataMovementProtocol getPreferredDataMovementProtocol(ProcessModel model,
                                                                        String gatewayId) throws TException, OrchestratorException {
        return getPreferredDataMovementInterface(model, gatewayId).getDataMovementProtocol();
    }

    public static StoragePreference getStoragePreference(ProcessModel processModel,
                                                         String gatewayId) throws OrchestratorException {
        final RegistryService.Client registryClient = getRegistryServiceClient();
        try {
            String resourceHostId = processModel.getComputeResourceId();
            return registryClient.getGatewayStoragePreference(gatewayId, resourceHostId);
        } catch (Exception e) {
            logger.error("Error occurred while retrieving StoragePreference", e);
            throw new OrchestratorException("Error occurred while retrieving StoragePreference", e);
        } finally {
            if (registryClient != null) {
                ThriftUtils.close(registryClient);
            }
        }
    }

    public static String getLoginUserName(ProcessModel processModel,
                                          String gatewayId) throws AiravataException, TException {
        final RegistryService.Client registryClient = getRegistryServiceClient();
        try {
            GroupComputeResourcePreference computeResourcePreference = getGroupComputeResourcePreference(processModel);
            ComputationalResourceSchedulingModel processResourceSchedule = processModel.getProcessResourceSchedule();
            if (processModel.isUseUserCRPref()) {
                UserComputeResourcePreference userComputeResourcePreference = registryClient
                        .getUserComputeResourcePreference(processModel.getUserName(), gatewayId,
                                processModel.getComputeResourceId());
                if (isValid(userComputeResourcePreference.getLoginUserName())) {
                    return userComputeResourcePreference.getLoginUserName();
                } else if (isValid(processResourceSchedule.getOverrideLoginUserName())) {
                    logger.warn("User computer resource preference doesn't have valid user login name, using computer " +
                            "resource scheduling login name " +  processResourceSchedule.getOverrideLoginUserName());
                    return processResourceSchedule.getOverrideLoginUserName();
                } else if (isValid(computeResourcePreference.getLoginUserName())) {
                    logger.warn("Either User computer resource preference or computer resource scheduling " +
                            "doesn't have valid user login name, using  group resource profile computer resource preference login name "
                            +  computeResourcePreference.getLoginUserName());
                    return computeResourcePreference.getLoginUserName();
                }else {
                    throw new AiravataException("Login name is not found");
                }
            }else {
                if (isValid(processResourceSchedule.getOverrideLoginUserName())) {
                    return processResourceSchedule.getOverrideLoginUserName();
                } else if (isValid(computeResourcePreference.getLoginUserName())) {
                    logger.warn("Process compute resource scheduling doesn't have valid user login name, " +
                            "using  gateway computer resource preference login name "
                            + computeResourcePreference.getLoginUserName());
                    return computeResourcePreference.getLoginUserName();
                }else {
                    throw new AiravataException("Login name is not found");
                }
            }
        } catch (ApplicationSettingsException e) {
            logger.error("Error occurred while initializing app catalog to fetch login username", e);
            throw new ApplicationSettingsException("Error occurred while initializing app catalog to fetch login username", e);
        } finally {
            if (registryClient != null) {
                ThriftUtils.close(registryClient);
            }
        }
    }

    public static String getScratchLocation(ProcessModel processModel,
                                            String gatewayId) throws  AiravataException, TException {
        final RegistryService.Client registryClient = getRegistryServiceClient();
        try {
            GroupComputeResourcePreference computeResourcePreference = getGroupComputeResourcePreference(processModel);
            ComputationalResourceSchedulingModel processResourceSchedule = processModel.getProcessResourceSchedule();
            if (processModel.isUseUserCRPref()) {
                UserComputeResourcePreference userComputeResourcePreference = registryClient
                        .getUserComputeResourcePreference(processModel.getUserName(), gatewayId,
                                processModel.getComputeResourceId());
                if (isValid(userComputeResourcePreference.getScratchLocation())) {
                    return userComputeResourcePreference.getScratchLocation();
                } else if (isValid(processResourceSchedule.getOverrideScratchLocation())) {
                    logger.warn("User computer resource preference doesn't have valid scratch location, using computer " +
                            "resource scheduling scratch location " +  processResourceSchedule.getOverrideScratchLocation());
                    return processResourceSchedule.getOverrideScratchLocation();
                } else if (isValid(computeResourcePreference.getScratchLocation())) {
                    logger.warn("Either User computer resource preference or computer resource scheduling doesn't have " +
                            "valid scratch location, using  gateway computer resource preference scratch location"
                            +  computeResourcePreference.getScratchLocation());
                    return computeResourcePreference.getScratchLocation();
                }else {
                    throw new AiravataException("Scratch location is not found");
                }
            }else {
                if (isValid(processResourceSchedule.getOverrideScratchLocation())) {
                    return processResourceSchedule.getOverrideScratchLocation();
                } else if (isValid(computeResourcePreference.getScratchLocation())) {
                    logger.warn("Process compute resource scheduling doesn't have valid scratch location, " +
                            "using  gateway computer resource preference scratch location"
                            + computeResourcePreference.getScratchLocation());
                    return computeResourcePreference.getScratchLocation();
                }else {
                    throw new AiravataException("Scratch location is not found");
                }
            }
        } catch (AiravataException e) {
            logger.error("Error occurred while initializing app catalog to fetch scratch location", e);
            throw new AiravataException("Error occurred while initializing app catalog to fetch scratch location", e);
        } finally {
            if (registryClient != null) {
                ThriftUtils.close(registryClient);
            }
        }
    }

    public static JobSubmissionInterface getPreferredJobSubmissionInterface(ProcessModel processModel,
                                                                            String gatewayId) throws OrchestratorException {
        final RegistryService.Client registryClient = getRegistryServiceClient();
        try {
            String resourceHostId = processModel.getComputeResourceId();
            ComputeResourceDescription resourceDescription = registryClient.getComputeResource(resourceHostId);
            List<JobSubmissionInterface> jobSubmissionInterfaces = resourceDescription.getJobSubmissionInterfaces();
            if (jobSubmissionInterfaces != null && !jobSubmissionInterfaces.isEmpty()) {
                Collections.sort(jobSubmissionInterfaces, Comparator.comparingInt(JobSubmissionInterface::getPriorityOrder));
            } else {
                throw new OrchestratorException("Compute resource should have at least one job submission interface defined...");
            }
            return jobSubmissionInterfaces.get(0);
        } catch (Exception e) {
            throw new OrchestratorException("Error occurred while retrieving data from app catalog", e);
        } finally {
            if (registryClient != null) {
                ThriftUtils.close(registryClient);
            }
        }
    }

    public static DataMovementInterface getPreferredDataMovementInterface(ProcessModel processModel,
                                                                          String gatewayId) throws OrchestratorException {
        final RegistryService.Client registryClient = getRegistryServiceClient();
        try {
            String resourceHostId = processModel.getComputeResourceId();
            ComputeResourceDescription resourceDescription = registryClient.getComputeResource(resourceHostId);
            List<DataMovementInterface> dataMovementInterfaces = resourceDescription.getDataMovementInterfaces();
            if (dataMovementInterfaces != null && !dataMovementInterfaces.isEmpty()) {
                Collections.sort(dataMovementInterfaces, Comparator.comparingInt(DataMovementInterface::getPriorityOrder));
            } else {
                throw new OrchestratorException("Compute resource should have at least one data movement interface defined...");
            }
            return dataMovementInterfaces.get(0);
        } catch (Exception e) {
            throw new OrchestratorException("Error occurred while retrieving data from app catalog", e);
        } finally {
            if (registryClient != null) {
                ThriftUtils.close(registryClient);
            }
        }
    }

    public static int getDataMovementPort(ProcessModel processModel,
                                          String gatewayId) throws TException, ApplicationSettingsException, OrchestratorException {
        try {
            DataMovementProtocol protocol = getPreferredDataMovementProtocol(processModel, gatewayId);
            DataMovementInterface dataMovementInterface = getPreferredDataMovementInterface(processModel, gatewayId);
            if (protocol == DataMovementProtocol.SCP ) {
                SCPDataMovement scpDataMovement = getSCPDataMovement(dataMovementInterface.getDataMovementInterfaceId());
                if (scpDataMovement != null) {
                    return scpDataMovement.getSshPort();
                }
            }
        } catch (Exception e) {
            logger.error("Error occurred while retrieving security protocol", e);
        }
        return 0;
    }


    public static SecurityProtocol getSecurityProtocol(ProcessModel processModel,
                                                       String gatewayId) throws TException, ApplicationSettingsException, OrchestratorException {
        try {
            JobSubmissionProtocol submissionProtocol = getPreferredJobSubmissionProtocol(processModel, gatewayId);
            JobSubmissionInterface jobSubmissionInterface = getPreferredJobSubmissionInterface(processModel, gatewayId);
            if (submissionProtocol == JobSubmissionProtocol.SSH ) {
                SSHJobSubmission sshJobSubmission = getSSHJobSubmission(jobSubmissionInterface.getJobSubmissionInterfaceId());
                if (sshJobSubmission != null) {
                    return sshJobSubmission.getSecurityProtocol();
                }
            } else if (submissionProtocol == JobSubmissionProtocol.LOCAL) {
                LOCALSubmission localJobSubmission = getLocalJobSubmission(jobSubmissionInterface.getJobSubmissionInterfaceId());
                if (localJobSubmission != null) {
                    return localJobSubmission.getSecurityProtocol();
                }
            } else if (submissionProtocol == JobSubmissionProtocol.SSH_FORK){
                SSHJobSubmission sshJobSubmission = getSSHJobSubmission(jobSubmissionInterface.getJobSubmissionInterfaceId());
                if (sshJobSubmission != null) {
                    return sshJobSubmission.getSecurityProtocol();
                }
            } else if (submissionProtocol == JobSubmissionProtocol.CLOUD) {
                CloudJobSubmission cloudJobSubmission = getCloudJobSubmission(jobSubmissionInterface.getJobSubmissionInterfaceId());
                if (cloudJobSubmission != null) {
                    return cloudJobSubmission.getSecurityProtocol();
                }
            }
        } catch (OrchestratorException e) {
            logger.error("Error occurred while retrieving security protocol", e);
        }
        return null;
    }

    public static LOCALSubmission getLocalJobSubmission(String submissionId) throws OrchestratorException {
        final RegistryService.Client registryClient = getRegistryServiceClient();
        try {
            return registryClient.getLocalJobSubmission(submissionId);
        } catch (Exception e) {
            String errorMsg = "Error while retrieving local job submission with submission id : " + submissionId;
            logger.error(errorMsg, e);
            throw new OrchestratorException(errorMsg, e);
        } finally {
            if (registryClient != null) {
                ThriftUtils.close(registryClient);
            }
        }
    }

    public static UnicoreJobSubmission getUnicoreJobSubmission(String submissionId) throws OrchestratorException {
        final RegistryService.Client registryClient = getRegistryServiceClient();
        try {
            return registryClient.getUnicoreJobSubmission(submissionId);
        } catch (Exception e) {
            String errorMsg = "Error while retrieving UNICORE job submission with submission id : " + submissionId;
            logger.error(errorMsg, e);
            throw new OrchestratorException(errorMsg, e);
        } finally {
            if (registryClient != null) {
                ThriftUtils.close(registryClient);
            }
        }
    }

    public static SSHJobSubmission getSSHJobSubmission(String submissionId) throws OrchestratorException {
        final RegistryService.Client registryClient = getRegistryServiceClient();
        try {
            return registryClient.getSSHJobSubmission(submissionId);
        } catch (Exception e) {
            String errorMsg = "Error while retrieving SSH job submission with submission id : " + submissionId;
            logger.error(errorMsg, e);
            throw new OrchestratorException(errorMsg, e);
        } finally {
            if (registryClient != null) {
                ThriftUtils.close(registryClient);
            }
        }
    }

    public static CloudJobSubmission getCloudJobSubmission(String submissionId) throws OrchestratorException {
        final RegistryService.Client registryClient = getRegistryServiceClient();
        try {
            return registryClient.getCloudJobSubmission(submissionId);
        } catch (Exception e) {
            String errorMsg = "Error while retrieving SSH job submission with submission id : " + submissionId;
            logger.error(errorMsg, e);
            throw new OrchestratorException(errorMsg, e);
        } finally {
            if (registryClient != null) {
                ThriftUtils.close(registryClient);
            }
        }
    }

    public static SCPDataMovement getSCPDataMovement(String dataMoveId) throws OrchestratorException {
        final RegistryService.Client registryClient = getRegistryServiceClient();
        try {
            return registryClient.getSCPDataMovement(dataMoveId);
        } catch (Exception e) {
            String errorMsg = "Error while retrieving SCP Data movement with submission id : " + dataMoveId;
            logger.error(errorMsg, e);
            throw new OrchestratorException(errorMsg, e);
        } finally {
            if (registryClient != null) {
                ThriftUtils.close(registryClient);
            }
        }
    }

    private static boolean isValid(String str) {
        return (str != null && !str.trim().isEmpty());
    }

    private static RegistryService.Client getRegistryServiceClient() {
        try {
            final int serverPort = Integer.parseInt(ServerSettings.getRegistryServerPort());
            final String serverHost = ServerSettings.getRegistryServerHost();
            return RegistryServiceClientFactory.createRegistryClient(serverHost, serverPort);
        } catch (RegistryServiceException|ApplicationSettingsException e) {
            throw new RuntimeException("Unable to create registry client...", e);
        }
    }

    public static CredentialReader getCredentialReader()
            throws ApplicationSettingsException, IllegalAccessException,
            InstantiationException {
        try {
            String jdbcUrl = ServerSettings.getCredentialStoreDBURL();
            String jdbcUsr = ServerSettings.getCredentialStoreDBUser();
            String jdbcPass = ServerSettings.getCredentialStoreDBPassword();
            String driver = ServerSettings.getCredentialStoreDBDriver();
            return new CredentialReaderImpl(new DBUtil(jdbcUrl, jdbcUsr, jdbcPass,
                    driver));
        } catch (ClassNotFoundException e) {
            logger.error("Not able to find driver: " + e.getLocalizedMessage());
            return null;
        }
    }
}
