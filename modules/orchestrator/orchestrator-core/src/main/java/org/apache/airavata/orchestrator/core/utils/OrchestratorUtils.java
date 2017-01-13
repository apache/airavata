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
package org.apache.airavata.orchestrator.core.utils;

import java.io.IOException;
import java.util.*;

import org.apache.airavata.common.exception.AiravataException;
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.model.appcatalog.appinterface.ApplicationInterfaceDescription;
import org.apache.airavata.model.appcatalog.computeresource.*;
import org.apache.airavata.model.appcatalog.computeresource.JobSubmissionInterface;
import org.apache.airavata.model.appcatalog.computeresource.UnicoreJobSubmission;
import org.apache.airavata.model.appcatalog.gatewayprofile.ComputeResourcePreference;
import org.apache.airavata.model.appcatalog.gatewayprofile.StoragePreference;
import org.apache.airavata.model.appcatalog.userresourceprofile.UserComputeResourcePreference;
import org.apache.airavata.model.data.movement.DataMovementInterface;
import org.apache.airavata.model.data.movement.DataMovementProtocol;
import org.apache.airavata.model.data.movement.SCPDataMovement;
import org.apache.airavata.model.data.movement.SecurityProtocol;
import org.apache.airavata.model.process.ProcessModel;
import org.apache.airavata.model.scheduling.ComputationalResourceSchedulingModel;
import org.apache.airavata.orchestrator.core.OrchestratorConfiguration;
import org.apache.airavata.orchestrator.core.context.OrchestratorContext;
import org.apache.airavata.orchestrator.core.exception.OrchestratorException;
import org.apache.airavata.registry.cpi.*;
import org.apache.airavata.registry.cpi.ApplicationInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    public static JobSubmissionProtocol getPreferredJobSubmissionProtocol(OrchestratorContext context,
                                                                          ProcessModel model,
                                                                          String gatewayId) throws RegistryException {
        try {
            String resourceHostId = model.getComputeResourceId();
            return getComputeResourcePreference(context, gatewayId, resourceHostId).getPreferredJobSubmissionProtocol();
        } catch (AppCatalogException e) {
            logger.error("Error occurred while initializing app catalog", e);
            throw new RegistryException("Error occurred while initializing app catalog", e);
        }
    }

    public static ComputeResourcePreference getComputeResourcePreference(OrchestratorContext context,
                                                                         String gatewayId,
                                                                         String resourceHostId)
            throws AppCatalogException, RegistryException {

        GwyResourceProfile gatewayProfile = getGatewayProfile(context);
        return gatewayProfile.getComputeResourcePreference(gatewayId
                , resourceHostId);
    }

    public static GwyResourceProfile getGatewayProfile(OrchestratorContext context)
            throws AppCatalogException, RegistryException {
        return context.getRegistry().getAppCatalog().getGatewayProfile();
    }

    public static UsrResourceProfile getUserResourceProfile(OrchestratorContext context)
            throws RegistryException, AppCatalogException {
        return context.getRegistry().getAppCatalog().getUserResourceProfile();
    }

    public static String getApplicationInterfaceName(OrchestratorContext context, ProcessModel model)
            throws RegistryException {
        try {
            ApplicationInterface applicationInterface = context.getRegistry().getAppCatalog().getApplicationInterface();
            ApplicationInterfaceDescription appInterface =
                    applicationInterface.getApplicationInterface(model.getApplicationInterfaceId());
            return appInterface.getApplicationName();
        } catch (AppCatalogException e) {
            throw new RegistryException("Error while retrieving application interface", e);
        }
    }

    public static DataMovementProtocol getPreferredDataMovementProtocol(OrchestratorContext context,
                                                                        ProcessModel model,
                                                                        String gatewayId) throws RegistryException {
        try {
            String resourceHostId = model.getComputeResourceId();
            return getComputeResourcePreference(context, gatewayId, resourceHostId).getPreferredDataMovementProtocol();
        } catch (AppCatalogException e) {
            logger.error("Error occurred while initializing app catalog", e);
            throw new RegistryException("Error occurred while initializing app catalog", e);
        }
    }

    public static ComputeResourcePreference getComputeResourcePreference(OrchestratorContext context,
                                                                         ProcessModel processModel,
                                                                         String gatewayId) throws RegistryException {
        try {
            return getComputeResourcePreference(context, gatewayId, processModel.getComputeResourceId());
        } catch (AppCatalogException e) {
            logger.error("Error occurred while initializing app catalog", e);
            throw new RegistryException("Error occurred while initializing app catalog", e);
        }
    }

    public static StoragePreference getStoragePreference(OrchestratorContext context,
                                                         ProcessModel processModel,
                                                         String gatewayId) throws RegistryException {
        try {
            GwyResourceProfile gatewayProfile = getGatewayProfile(context);
            String resourceHostId = processModel.getComputeResourceId();
            return gatewayProfile.getStoragePreference(gatewayId, resourceHostId);
        } catch (AppCatalogException e) {
            logger.error("Error occurred while initializing app catalog", e);
            throw new RegistryException("Error occurred while initializing app catalog", e);
        }
    }

    public static String getLoginUserName(OrchestratorContext context,
                                          ProcessModel processModel,
                                          String gatewayId) throws RegistryException, AiravataException {
        try {
            ComputeResourcePreference computeResourcePreference = getComputeResourcePreference(context, gatewayId,
                    processModel.getComputeResourceId());
            ComputationalResourceSchedulingModel processResourceSchedule = processModel.getProcessResourceSchedule();
            if (processModel.isUseUserCRPref()) {
                UsrResourceProfile userResourceProfile = getUserResourceProfile(context);
                UserComputeResourcePreference userComputeResourcePreference = userResourceProfile
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
                            "doesn't have valid user login name, using  gateway computer resource preference login name "
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
        } catch (AppCatalogException e) {
            logger.error("Error occurred while initializing app catalog to fetch login username", e);
            throw new RegistryException("Error occurred while initializing app catalog to fetch login username", e);
        }
    }

    public static String getScratchLocation(OrchestratorContext context,
                                            ProcessModel processModel,
                                            String gatewayId) throws RegistryException, AiravataException {
        try {
            ComputeResourcePreference computeResourcePreference = getComputeResourcePreference(context, gatewayId,
                    processModel.getComputeResourceId());
            ComputationalResourceSchedulingModel processResourceSchedule = processModel.getProcessResourceSchedule();
            if (processModel.isUseUserCRPref()) {
                UsrResourceProfile userResourceProfile = getUserResourceProfile(context);
                UserComputeResourcePreference userComputeResourcePreference = userResourceProfile
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
        } catch (AppCatalogException e) {
            logger.error("Error occurred while initializing app catalog to fetch scratch location", e);
            throw new RegistryException("Error occurred while initializing app catalog to fetch scratch location", e);
        }
    }

    public static JobSubmissionInterface getPreferredJobSubmissionInterface(OrchestratorContext context,
                                                                            ProcessModel processModel,
                                                                            String gatewayId) throws RegistryException {
        try {
            String resourceHostId = processModel.getComputeResourceId();
            ComputeResourcePreference resourcePreference = getComputeResourcePreference(context, processModel, gatewayId);
            JobSubmissionProtocol preferredJobSubmissionProtocol = resourcePreference.getPreferredJobSubmissionProtocol();
            ComputeResourceDescription resourceDescription =
                    context.getRegistry().getAppCatalog().getComputeResource().getComputeResource(resourceHostId);
            List<JobSubmissionInterface> jobSubmissionInterfaces = resourceDescription.getJobSubmissionInterfaces();
            Map<JobSubmissionProtocol, List<JobSubmissionInterface>> orderedInterfaces = new HashMap<>();
            List<JobSubmissionInterface> interfaces = new ArrayList<>();
            if (jobSubmissionInterfaces != null && !jobSubmissionInterfaces.isEmpty()) {
                for (JobSubmissionInterface submissionInterface : jobSubmissionInterfaces){

                    if (preferredJobSubmissionProtocol != null){
                        if (preferredJobSubmissionProtocol.toString().equals(submissionInterface.getJobSubmissionProtocol().toString())){
                            if (orderedInterfaces.containsKey(submissionInterface.getJobSubmissionProtocol())){
                                List<JobSubmissionInterface> interfaceList = orderedInterfaces.get(submissionInterface.getJobSubmissionProtocol());
                                interfaceList.add(submissionInterface);
                            }else {
                                interfaces.add(submissionInterface);
                                orderedInterfaces.put(submissionInterface.getJobSubmissionProtocol(), interfaces);
                            }
                        }
                    }else {
                        Collections.sort(jobSubmissionInterfaces,
                                (jobSubmissionInterface, jobSubmissionInterface2) ->
                                        jobSubmissionInterface.getPriorityOrder() - jobSubmissionInterface2.getPriorityOrder());
                    }
                }
                interfaces = orderedInterfaces.get(preferredJobSubmissionProtocol);
                Collections.sort(interfaces, (jobSubmissionInterface, jobSubmissionInterface2) ->
                        jobSubmissionInterface.getPriorityOrder() - jobSubmissionInterface2.getPriorityOrder());
            } else {
                throw new RegistryException("Compute resource should have at least one job submission interface defined...");
            }
            return interfaces.get(0);
        } catch (AppCatalogException e) {
            throw new RegistryException("Error occurred while retrieving data from app catalog", e);
        }
    }

    public static DataMovementInterface getPrefferredDataMovementInterface(OrchestratorContext context,
                                                                           ProcessModel processModel,
                                                                           String gatewayId) throws RegistryException {
        try {
            String resourceHostId = processModel.getComputeResourceId();
            ComputeResourcePreference resourcePreference = getComputeResourcePreference(context, processModel, gatewayId);
            DataMovementProtocol preferredDataMovementProtocol = resourcePreference.getPreferredDataMovementProtocol();
            ComputeResourceDescription resourceDescription =
                    context.getRegistry().getAppCatalog().getComputeResource().getComputeResource(resourceHostId);
            List<DataMovementInterface> dataMovementInterfaces = resourceDescription.getDataMovementInterfaces();
            if (dataMovementInterfaces != null && !dataMovementInterfaces.isEmpty()) {
                for (DataMovementInterface dataMovementInterface : dataMovementInterfaces){
                    if (preferredDataMovementProtocol != null){
                        if (preferredDataMovementProtocol.toString().equals(dataMovementInterface.getDataMovementProtocol().toString())){
                            return dataMovementInterface;
                        }
                    }
                }
            } else {
                throw new RegistryException("Compute resource should have at least one data movement interface defined...");
            }
        } catch (AppCatalogException e) {
            throw new RegistryException("Error occurred while retrieving data from app catalog", e);
        }
        return null;
    }

    public static int getDataMovementPort(OrchestratorContext context,
                                          ProcessModel processModel,
                                          String gatewayId) throws RegistryException{
        try {
            DataMovementProtocol protocol = getPreferredDataMovementProtocol(context, processModel, gatewayId);
            DataMovementInterface dataMovementInterface = getPrefferredDataMovementInterface(context, processModel, gatewayId);
            if (protocol == DataMovementProtocol.SCP ) {
                SCPDataMovement scpDataMovement = getSCPDataMovement(context, dataMovementInterface.getDataMovementInterfaceId());
                if (scpDataMovement != null) {
                    return scpDataMovement.getSshPort();
                }
            }
        } catch (RegistryException e) {
            logger.error("Error occurred while retrieving security protocol", e);
        }
        return 0;
    }


    public static SecurityProtocol getSecurityProtocol(OrchestratorContext context,
                                                       ProcessModel processModel,
                                                       String gatewayId) throws RegistryException{
        try {
            JobSubmissionProtocol submissionProtocol = getPreferredJobSubmissionProtocol(context, processModel, gatewayId);
            JobSubmissionInterface jobSubmissionInterface = getPreferredJobSubmissionInterface(context, processModel, gatewayId);
            if (submissionProtocol == JobSubmissionProtocol.SSH ) {
                SSHJobSubmission sshJobSubmission = getSSHJobSubmission(context, jobSubmissionInterface.getJobSubmissionInterfaceId());
                if (sshJobSubmission != null) {
                    return sshJobSubmission.getSecurityProtocol();
                }
            } else if (submissionProtocol == JobSubmissionProtocol.LOCAL) {
                LOCALSubmission localJobSubmission = getLocalJobSubmission(context, jobSubmissionInterface.getJobSubmissionInterfaceId());
                if (localJobSubmission != null) {
                    return localJobSubmission.getSecurityProtocol();
                }
            } else if (submissionProtocol == JobSubmissionProtocol.SSH_FORK){
                SSHJobSubmission sshJobSubmission = getSSHJobSubmission(context, jobSubmissionInterface.getJobSubmissionInterfaceId());
                if (sshJobSubmission != null) {
                    return sshJobSubmission.getSecurityProtocol();
                }
            } else if (submissionProtocol == JobSubmissionProtocol.CLOUD) {
                CloudJobSubmission cloudJobSubmission = getCloudJobSubmission(context, jobSubmissionInterface.getJobSubmissionInterfaceId());
                if (cloudJobSubmission != null) {
                    return cloudJobSubmission.getSecurityProtocol();
                }
            }
        } catch (RegistryException e) {
            logger.error("Error occurred while retrieving security protocol", e);
        }
        return null;
    }

    public static LOCALSubmission getLocalJobSubmission(OrchestratorContext context,
                                                        String submissionId) throws RegistryException {
        try {
            AppCatalog appCatalog = context.getRegistry().getAppCatalog();
            return appCatalog.getComputeResource().getLocalJobSubmission(submissionId);
        } catch (Exception e) {
            String errorMsg = "Error while retrieving local job submission with submission id : " + submissionId;
            logger.error(errorMsg, e);
            throw new RegistryException(errorMsg, e);
        }
    }

    public static UnicoreJobSubmission getUnicoreJobSubmission(OrchestratorContext context,
                                                               String submissionId) throws RegistryException {
        try {
            AppCatalog appCatalog = context.getRegistry().getAppCatalog();
            return appCatalog.getComputeResource().getUNICOREJobSubmission(submissionId);
        } catch (Exception e) {
            String errorMsg = "Error while retrieving UNICORE job submission with submission id : " + submissionId;
            logger.error(errorMsg, e);
            throw new RegistryException(errorMsg, e);
        }
    }

    public static SSHJobSubmission getSSHJobSubmission(OrchestratorContext context,
                                                       String submissionId) throws RegistryException {
        try {
            AppCatalog appCatalog = context.getRegistry().getAppCatalog();
            return appCatalog.getComputeResource().getSSHJobSubmission(submissionId);
        } catch (Exception e) {
            String errorMsg = "Error while retrieving SSH job submission with submission id : " + submissionId;
            logger.error(errorMsg, e);
            throw new RegistryException(errorMsg, e);
        }
    }

    public static CloudJobSubmission getCloudJobSubmission(OrchestratorContext context,
                                                           String submissionId) throws RegistryException {
        try {
            AppCatalog appCatalog = context.getRegistry().getAppCatalog();
            return appCatalog.getComputeResource().getCloudJobSubmission(submissionId);
        } catch (Exception e) {
            String errorMsg = "Error while retrieving SSH job submission with submission id : " + submissionId;
            logger.error(errorMsg, e);
            throw new RegistryException(errorMsg, e);
        }
    }

    public static SCPDataMovement getSCPDataMovement(OrchestratorContext context,
                                                     String dataMoveId) throws RegistryException {
        try {
            AppCatalog appCatalog = context.getRegistry().getAppCatalog();
            return appCatalog.getComputeResource().getSCPDataMovement(dataMoveId);
        } catch (Exception e) {
            String errorMsg = "Error while retrieving SCP Data movement with submission id : " + dataMoveId;
            logger.error(errorMsg, e);
            throw new RegistryException(errorMsg, e);
        }
    }

    private static boolean isValid(String str) {
        return (str != null && !str.trim().isEmpty());
    }
}
