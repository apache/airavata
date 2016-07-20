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

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.model.appcatalog.appinterface.ApplicationInterfaceDescription;
import org.apache.airavata.model.appcatalog.computeresource.*;
import org.apache.airavata.model.appcatalog.computeresource.JobSubmissionInterface;
import org.apache.airavata.model.appcatalog.computeresource.UnicoreJobSubmission;
import org.apache.airavata.model.appcatalog.gatewayprofile.ComputeResourcePreference;
import org.apache.airavata.model.appcatalog.gatewayprofile.StoragePreference;
import org.apache.airavata.model.data.movement.DataMovementInterface;
import org.apache.airavata.model.data.movement.DataMovementProtocol;
import org.apache.airavata.model.data.movement.SCPDataMovement;
import org.apache.airavata.model.data.movement.SecurityProtocol;
import org.apache.airavata.model.process.ProcessModel;
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

    public static String OrchestratorStringConstant(OrchestratorConstants constant)
    {
      return constant.getOrchestratorStringConstant();
    }

    public static int OrchestratorIntegerConstant(OrchestratorConstants constant)
    {
      return constant.getOrchestratorIntegerConstant();
    }

    public static OrchestratorConfiguration loadOrchestratorConfiguration() throws OrchestratorException, IOException, NumberFormatException, ApplicationSettingsException {
        OrchestratorConfiguration orchestratorConfiguration = new OrchestratorConfiguration();
        orchestratorConfiguration.setSubmitterInterval(Integer.parseInt((String) ServerSettings.getSetting(OrchestratorStringConstant(OrchestratorConstants.SUBMIT_INTERVAL))));
        orchestratorConfiguration.setThreadPoolSize(Integer.parseInt((String) ServerSettings.getSetting(OrchestratorStringConstant(OrchestratorConstants.THREAD_POOL_SIZE))));
        orchestratorConfiguration.setStartSubmitter(Boolean.valueOf(ServerSettings.getSetting(OrchestratorStringConstant(OrchestratorConstants.START_SUBMITTER))));
        orchestratorConfiguration.setEmbeddedMode(Boolean.valueOf(ServerSettings.getSetting(OrchestratorStringConstant(OrchestratorConstants.EMBEDDED_MODE))));
        orchestratorConfiguration.setEnableValidation(Boolean.valueOf(ServerSettings.getSetting(OrchestratorStringConstant(OrchestratorConstants.ENABLE_VALIDATION))));
        if (orchestratorConfiguration.isEnableValidation()) {
            orchestratorConfiguration.setValidatorClasses(Arrays.asList(ServerSettings.getSetting(OrchestratorStringConstant(OrchestratorConstants.JOB_VALIDATOR)).split(",")));
        }
        return orchestratorConfiguration;
    }

    public static JobSubmissionProtocol getPreferredJobSubmissionProtocol(OrchestratorContext context, ProcessModel model, String gatewayId) throws RegistryException {
        try {
            GwyResourceProfile gatewayProfile = context.getRegistry().getAppCatalog().getGatewayProfile();
            String resourceHostId = model.getComputeResourceId();
            ComputeResourcePreference preference = gatewayProfile.getComputeResourcePreference(gatewayId
                    , resourceHostId);
            return preference.getPreferredJobSubmissionProtocol();
        } catch (AppCatalogException e) {
            logger.error("Error occurred while initializing app catalog", e);
            throw new RegistryException("Error occurred while initializing app catalog", e);
        }
    }

    public static String getApplicationInterfaceName(OrchestratorContext context, ProcessModel model) throws RegistryException {
        try {
            ApplicationInterface applicationInterface = context.getRegistry().getAppCatalog().getApplicationInterface();
            ApplicationInterfaceDescription appInterface = applicationInterface.getApplicationInterface(model.getApplicationInterfaceId());
            return appInterface.getApplicationName();
        } catch (AppCatalogException e) {
            throw new RegistryException("Error while retrieving application interface", e);
        }
    }

    public static DataMovementProtocol getPreferredDataMovementProtocol(OrchestratorContext context, ProcessModel model, String gatewayId) throws RegistryException {
        try {
            GwyResourceProfile gatewayProfile = context.getRegistry().getAppCatalog().getGatewayProfile();
            String resourceHostId = model.getComputeResourceId();
            ComputeResourcePreference preference = gatewayProfile.getComputeResourcePreference(gatewayId
                    , resourceHostId);
            return preference.getPreferredDataMovementProtocol();
        } catch (AppCatalogException e) {
            logger.error("Error occurred while initializing app catalog", e);
            throw new RegistryException("Error occurred while initializing app catalog", e);
        }
    }

    public static ComputeResourcePreference getComputeResourcePreference(OrchestratorContext context, ProcessModel processModel, String gatewayId) throws RegistryException {
        try {
            GwyResourceProfile gatewayProfile = context.getRegistry().getAppCatalog().getGatewayProfile();
            String resourceHostId = processModel.getComputeResourceId();
            return gatewayProfile.getComputeResourcePreference(gatewayId, resourceHostId);
        } catch (AppCatalogException e) {
            logger.error("Error occurred while initializing app catalog", e);
            throw new RegistryException("Error occurred while initializing app catalog", e);
        }
    }

    public static StoragePreference getStoragePreference(OrchestratorContext context, ProcessModel processModel, String gatewayId) throws RegistryException {
        try {
            GwyResourceProfile gatewayProfile = context.getRegistry().getAppCatalog().getGatewayProfile();
            String resourceHostId = processModel.getComputeResourceId();
            return gatewayProfile.getStoragePreference(gatewayId, resourceHostId);
        } catch (AppCatalogException e) {
            logger.error("Error occurred while initializing app catalog", e);
            throw new RegistryException("Error occurred while initializing app catalog", e);
        }
    }

    public static String getLoginUserName(OrchestratorContext context, ProcessModel processModel, String gatewayId) throws RegistryException {
        try {
            String loginUserName = null;
            String overrideLoginUserName = processModel.getResourceSchedule().getOverrideLoginUserName();
            if (overrideLoginUserName != null && !overrideLoginUserName.equals("")) {
                loginUserName = overrideLoginUserName;
            } else {
                GwyResourceProfile gatewayProfile = context.getRegistry().getAppCatalog().getGatewayProfile();
                loginUserName = gatewayProfile.getComputeResourcePreference(gatewayId, processModel.getComputeResourceId()).getLoginUserName();
            }
            return loginUserName;
        } catch (AppCatalogException e) {
            logger.error("Error occurred while initializing app catalog to fetch login username", e);
            throw new RegistryException("Error occurred while initializing app catalog to fetch login username", e);
        }
    }

    public static String getScratchLocation(OrchestratorContext context, ProcessModel processModel, String gatewayId) throws RegistryException {
        try {
            String scratchLocation = null;
            String overrideScratchLocation = processModel.getResourceSchedule().getOverrideScratchLocation();
            if (overrideScratchLocation != null && !overrideScratchLocation.equals("")) {
                scratchLocation = overrideScratchLocation;
            } else {
                GwyResourceProfile gatewayProfile = context.getRegistry().getAppCatalog().getGatewayProfile();
                scratchLocation = gatewayProfile.getComputeResourcePreference(gatewayId, processModel.getComputeResourceId()).getScratchLocation();
            }
            return scratchLocation;
        } catch (AppCatalogException e) {
            logger.error("Error occurred while initializing app catalog to fetch scratch location", e);
            throw new RegistryException("Error occurred while initializing app catalog to fetch scratch location", e);
        }
    }

    public static JobSubmissionInterface getPreferredJobSubmissionInterface(OrchestratorContext context, ProcessModel processModel, String gatewayId) throws RegistryException {
        try {
            String resourceHostId = processModel.getComputeResourceId();
            ComputeResourcePreference resourcePreference = getComputeResourcePreference(context, processModel, gatewayId);
            JobSubmissionProtocol preferredJobSubmissionProtocol = resourcePreference.getPreferredJobSubmissionProtocol();
            ComputeResourceDescription resourceDescription = context.getRegistry().getAppCatalog().getComputeResource().getComputeResource(resourceHostId);
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
                        Collections.sort(jobSubmissionInterfaces, new Comparator<JobSubmissionInterface>() {
                            @Override
                            public int compare(JobSubmissionInterface jobSubmissionInterface, JobSubmissionInterface jobSubmissionInterface2) {
                                return jobSubmissionInterface.getPriorityOrder() - jobSubmissionInterface2.getPriorityOrder();
                            }
                        });
                    }
                }
                interfaces = orderedInterfaces.get(preferredJobSubmissionProtocol);
                Collections.sort(interfaces, new Comparator<JobSubmissionInterface>() {
                    @Override
                    public int compare(JobSubmissionInterface jobSubmissionInterface, JobSubmissionInterface jobSubmissionInterface2) {
                        return jobSubmissionInterface.getPriorityOrder() - jobSubmissionInterface2.getPriorityOrder();
                    }
                });
            } else {
                throw new RegistryException("Compute resource should have at least one job submission interface defined...");
            }
            return interfaces.get(0);
        } catch (AppCatalogException e) {
            throw new RegistryException("Error occurred while retrieving data from app catalog", e);
        }
    }

    public static DataMovementInterface getPrefferredDataMovementInterface(OrchestratorContext context, ProcessModel processModel, String gatewayId) throws RegistryException {
        try {
            String resourceHostId = processModel.getComputeResourceId();
            ComputeResourcePreference resourcePreference = getComputeResourcePreference(context, processModel, gatewayId);
            DataMovementProtocol preferredDataMovementProtocol = resourcePreference.getPreferredDataMovementProtocol();
            ComputeResourceDescription resourceDescription = context.getRegistry().getAppCatalog().getComputeResource().getComputeResource(resourceHostId);
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

    public static int getDataMovementPort(OrchestratorContext context, ProcessModel processModel, String gatewayId) throws RegistryException{
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


    public static SecurityProtocol getSecurityProtocol(OrchestratorContext context, ProcessModel processModel, String gatewayId) throws RegistryException{
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
            }
        } catch (RegistryException e) {
            logger.error("Error occurred while retrieving security protocol", e);
        }
        return null;
    }

    public static LOCALSubmission getLocalJobSubmission(OrchestratorContext context, String submissionId) throws RegistryException {
        try {
            AppCatalog appCatalog = context.getRegistry().getAppCatalog();
            return appCatalog.getComputeResource().getLocalJobSubmission(submissionId);
        } catch (Exception e) {
            String errorMsg = "Error while retrieving local job submission with submission id : " + submissionId;
            logger.error(errorMsg, e);
            throw new RegistryException(errorMsg, e);
        }
    }

    public static UnicoreJobSubmission getUnicoreJobSubmission(OrchestratorContext context, String submissionId) throws RegistryException {
        try {
            AppCatalog appCatalog = context.getRegistry().getAppCatalog();
            return appCatalog.getComputeResource().getUNICOREJobSubmission(submissionId);
        } catch (Exception e) {
            String errorMsg = "Error while retrieving UNICORE job submission with submission id : " + submissionId;
            logger.error(errorMsg, e);
            throw new RegistryException(errorMsg, e);
        }
    }

    public static SSHJobSubmission getSSHJobSubmission(OrchestratorContext context, String submissionId) throws RegistryException {
        try {
            AppCatalog appCatalog = context.getRegistry().getAppCatalog();
            return appCatalog.getComputeResource().getSSHJobSubmission(submissionId);
        } catch (Exception e) {
            String errorMsg = "Error while retrieving SSH job submission with submission id : " + submissionId;
            logger.error(errorMsg, e);
            throw new RegistryException(errorMsg, e);
        }
    }

    public static SCPDataMovement getSCPDataMovement(OrchestratorContext context, String dataMoveId) throws RegistryException {
        try {
            AppCatalog appCatalog = context.getRegistry().getAppCatalog();
            return appCatalog.getComputeResource().getSCPDataMovement(dataMoveId);
        } catch (Exception e) {
            String errorMsg = "Error while retrieving SCP Data movement with submission id : " + dataMoveId;
            logger.error(errorMsg, e);
            throw new RegistryException(errorMsg, e);
        }
    }
}
