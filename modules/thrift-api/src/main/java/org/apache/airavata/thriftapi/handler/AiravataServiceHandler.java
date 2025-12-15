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
package org.apache.airavata.thriftapi.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.airavata.accountprovisioning.ConfigParam;
import org.apache.airavata.accountprovisioning.SSHAccountProvisionerFactory;
import org.apache.airavata.accountprovisioning.SSHAccountProvisionerProvider;
import org.apache.airavata.common.exception.AiravataException;
import org.apache.airavata.common.utils.Constants;
import org.apache.airavata.security.interceptor.SecurityCheck;
import org.apache.airavata.thriftapi.security.model.AuthzToken;
import org.apache.airavata.service.AiravataService;
import org.apache.airavata.thriftapi.credential.exception.CredentialStoreException;
import org.apache.airavata.thriftapi.credential.model.CredentialSummary;
import org.apache.airavata.thriftapi.credential.model.SummaryType;
import org.apache.airavata.thriftapi.exception.AiravataClientException;
import org.apache.airavata.thriftapi.exception.AiravataErrorType;
import org.apache.airavata.thriftapi.exception.AiravataSystemException;
import org.apache.airavata.thriftapi.exception.AuthorizationException;
import org.apache.airavata.thriftapi.exception.ExperimentNotFoundException;
import org.apache.airavata.thriftapi.exception.InvalidRequestException;
import org.apache.airavata.thriftapi.exception.ProjectNotFoundException;
import org.apache.airavata.thriftapi.mapper.ApplicationDeploymentDescriptionMapper;
import org.apache.airavata.thriftapi.mapper.ApplicationInterfaceDescriptionMapper;
import org.apache.airavata.thriftapi.mapper.ApplicationModuleMapper;
import org.apache.airavata.thriftapi.mapper.AuthzTokenMapper;
import org.apache.airavata.thriftapi.mapper.BatchQueueResourcePolicyMapper;
import org.apache.airavata.thriftapi.mapper.CloudJobSubmissionMapper;
import org.apache.airavata.thriftapi.mapper.ComputationalResourceSchedulingModelMapper;
import org.apache.airavata.thriftapi.mapper.ComputeResourceDescriptionMapper;
import org.apache.airavata.thriftapi.mapper.ComputeResourcePolicyMapper;
import org.apache.airavata.thriftapi.mapper.ComputeResourcePreferenceMapper;
import org.apache.airavata.thriftapi.mapper.CredentialSummaryMapper;
import org.apache.airavata.thriftapi.mapper.DataProductModelMapper;
import org.apache.airavata.thriftapi.mapper.DataReplicaLocationModelMapper;
import org.apache.airavata.thriftapi.mapper.ExperimentModelMapper;
import org.apache.airavata.thriftapi.mapper.ExperimentStatisticsMapper;
import org.apache.airavata.thriftapi.mapper.ExperimentSummaryModelMapper;
import org.apache.airavata.thriftapi.mapper.GatewayGroupsMapper;
import org.apache.airavata.thriftapi.mapper.GatewayMapper;
import org.apache.airavata.thriftapi.mapper.GatewayResourceProfileMapper;
import org.apache.airavata.thriftapi.mapper.GridFTPDataMovementMapper;
import org.apache.airavata.thriftapi.mapper.GroupComputeResourcePreferenceMapper;
import org.apache.airavata.thriftapi.mapper.GroupResourceProfileMapper;
import org.apache.airavata.thriftapi.mapper.InputDataObjectTypeMapper;
import org.apache.airavata.thriftapi.mapper.JobModelMapper;
import org.apache.airavata.thriftapi.mapper.LOCALDataMovementMapper;
import org.apache.airavata.thriftapi.mapper.LOCALSubmissionMapper;
import org.apache.airavata.thriftapi.mapper.NotificationMapper;
import org.apache.airavata.thriftapi.mapper.OutputDataObjectTypeMapper;
import org.apache.airavata.thriftapi.mapper.ParserMapper;
import org.apache.airavata.thriftapi.mapper.ParsingTemplateMapper;
import org.apache.airavata.thriftapi.mapper.ProcessStatusMapper;
import org.apache.airavata.thriftapi.mapper.ProjectMapper;
import org.apache.airavata.thriftapi.mapper.QueueStatusModelMapper;
import org.apache.airavata.thriftapi.mapper.ResourceJobManagerMapper;
import org.apache.airavata.thriftapi.mapper.SCPDataMovementMapper;
import org.apache.airavata.thriftapi.mapper.SSHAccountProvisionerDescriptionMapper;
import org.apache.airavata.thriftapi.mapper.SSHJobSubmissionMapper;
import org.apache.airavata.thriftapi.mapper.StorageDirectoryInfoMapper;
import org.apache.airavata.thriftapi.mapper.StoragePreferenceMapper;
import org.apache.airavata.thriftapi.mapper.StorageResourceDescriptionMapper;
import org.apache.airavata.thriftapi.mapper.StorageVolumeInfoMapper;
import org.apache.airavata.thriftapi.mapper.UnicoreDataMovementMapper;
import org.apache.airavata.thriftapi.mapper.UnicoreJobSubmissionMapper;
import org.apache.airavata.thriftapi.mapper.UserComputeResourcePreferenceMapper;
import org.apache.airavata.thriftapi.mapper.UserConfigurationDataModelMapper;
import org.apache.airavata.thriftapi.mapper.UserResourceProfileMapper;
import org.apache.airavata.thriftapi.mapper.UserStoragePreferenceMapper;
import org.apache.airavata.thriftapi.model.ApplicationDeploymentDescription;
import org.apache.airavata.thriftapi.model.ApplicationInterfaceDescription;
import org.apache.airavata.thriftapi.model.BatchQueueResourcePolicy;
import org.apache.airavata.thriftapi.model.ComputationalResourceSchedulingModel;
import org.apache.airavata.thriftapi.model.ComputeResourceDescription;
import org.apache.airavata.thriftapi.model.ComputeResourcePreference;
import org.apache.airavata.thriftapi.model.DMType;
import org.apache.airavata.thriftapi.model.DataProductModel;
import org.apache.airavata.thriftapi.model.DataReplicaLocationModel;
import org.apache.airavata.thriftapi.model.ExperimentModel;
import org.apache.airavata.thriftapi.model.ExperimentSearchFields;
import org.apache.airavata.thriftapi.model.ExperimentStatistics;
import org.apache.airavata.thriftapi.model.ExperimentSummaryModel;
import org.apache.airavata.thriftapi.model.Gateway;
import org.apache.airavata.thriftapi.model.GatewayResourceProfile;
import org.apache.airavata.thriftapi.model.GroupComputeResourcePreference;
import org.apache.airavata.thriftapi.model.GroupResourceProfile;
import org.apache.airavata.thriftapi.model.InputDataObjectType;
import org.apache.airavata.thriftapi.model.LOCALDataMovement;
import org.apache.airavata.thriftapi.model.Notification;
import org.apache.airavata.thriftapi.model.OutputDataObjectType;
import org.apache.airavata.thriftapi.model.Project;
import org.apache.airavata.thriftapi.model.ProjectSearchFields;
import org.apache.airavata.thriftapi.model.QueueStatusModel;
import org.apache.airavata.thriftapi.model.ResourcePermissionType;
import org.apache.airavata.thriftapi.model.SSHAccountProvisionerConfigParam;
import org.apache.airavata.thriftapi.model.SSHAccountProvisionerConfigParamType;
import org.apache.airavata.thriftapi.model.StorageDirectoryInfo;
import org.apache.airavata.thriftapi.model.StorageVolumeInfo;
import org.apache.airavata.thriftapi.model.UserConfigurationDataModel;
import org.apache.airavata.thriftapi.model.UserResourceProfile;
import org.apache.airavata.thriftapi.model.ExperimentStatus;
import org.apache.airavata.thriftapi.model.ExperimentState;
import org.apache.airavata.thriftapi.model.JobStatus;
import org.apache.airavata.thriftapi.model.JobState;
import org.apache.airavata.thriftapi.model.JobModel;
import org.apache.airavata.thriftapi.model.ApplicationModule;
import org.apache.airavata.thriftapi.model.StorageResourceDescription;
import org.apache.airavata.thriftapi.model.LOCALSubmission;
import org.apache.airavata.thriftapi.model.SSHJobSubmission;
import org.apache.airavata.thriftapi.model.CloudJobSubmission;
import org.apache.airavata.thriftapi.model.UnicoreJobSubmission;
import org.apache.airavata.thriftapi.model.SCPDataMovement;
import org.apache.airavata.thriftapi.model.UnicoreDataMovement;
import org.apache.airavata.thriftapi.model.UserComputeResourcePreference;
import org.apache.airavata.thriftapi.model.ParsingTemplate;
import org.apache.airavata.thriftapi.model.Parser;
import org.apache.airavata.thriftapi.model.GridFTPDataMovement;
import org.apache.airavata.thriftapi.model.UserStoragePreference;
import org.apache.airavata.thriftapi.model.StoragePreference;
import org.apache.airavata.thriftapi.model.ResourceJobManager;
import org.apache.airavata.thriftapi.model.ComputeResourcePolicy;
import org.apache.airavata.thriftapi.model.GatewayGroups;
import org.apache.airavata.thriftapi.model.ProcessStatus;
import org.apache.airavata.thriftapi.model.SSHAccountProvisionerDescription;
import org.apache.airavata.thriftapi.service.airavata_apiConstants;
import org.apache.thrift.TException;
import org.springframework.stereotype.Component;

@Component
public class AiravataServiceHandler implements org.apache.airavata.thriftapi.service.Airavata.Iface {

    private final AiravataService airavataService;
    private final GatewayMapper gatewayMapper = GatewayMapper.INSTANCE;
    private final ExperimentModelMapper experimentModelMapper = ExperimentModelMapper.INSTANCE;
    private final ProjectMapper projectMapper = ProjectMapper.INSTANCE;
    private final NotificationMapper notificationMapper = NotificationMapper.INSTANCE;
    private final ExperimentSummaryModelMapper experimentSummaryModelMapper = ExperimentSummaryModelMapper.INSTANCE;
    private final ExperimentStatisticsMapper experimentStatisticsMapper = ExperimentStatisticsMapper.INSTANCE;
    private final ApplicationInterfaceDescriptionMapper applicationInterfaceDescriptionMapper =
            ApplicationInterfaceDescriptionMapper.INSTANCE;
    private final ApplicationDeploymentDescriptionMapper applicationDeploymentDescriptionMapper =
            ApplicationDeploymentDescriptionMapper.INSTANCE;
    private final ComputeResourceDescriptionMapper computeResourceDescriptionMapper =
            ComputeResourceDescriptionMapper.INSTANCE;
    private final GatewayResourceProfileMapper gatewayResourceProfileMapper = GatewayResourceProfileMapper.INSTANCE;
    private final UserResourceProfileMapper userResourceProfileMapper = UserResourceProfileMapper.INSTANCE;
    private final OutputDataObjectTypeMapper outputDataObjectTypeMapper = OutputDataObjectTypeMapper.INSTANCE;
    private final InputDataObjectTypeMapper inputDataObjectTypeMapper = InputDataObjectTypeMapper.INSTANCE;
    private final JobModelMapper jobModelMapper = JobModelMapper.INSTANCE;
    private final DataProductModelMapper dataProductModelMapper = DataProductModelMapper.INSTANCE;
    private final CredentialSummaryMapper credentialSummaryMapper = CredentialSummaryMapper.INSTANCE;
    private final AuthzTokenMapper authzTokenMapper = AuthzTokenMapper.INSTANCE;
    private final StorageDirectoryInfoMapper storageDirectoryInfoMapper = StorageDirectoryInfoMapper.INSTANCE;
    private final StorageVolumeInfoMapper storageVolumeInfoMapper = StorageVolumeInfoMapper.INSTANCE;
    private final GroupResourceProfileMapper groupResourceProfileMapper = GroupResourceProfileMapper.INSTANCE;
    private final UserConfigurationDataModelMapper userConfigurationDataModelMapper =
            UserConfigurationDataModelMapper.INSTANCE;
    private final GridFTPDataMovementMapper gridFTPDataMovementMapper = GridFTPDataMovementMapper.INSTANCE;
    private final CloudJobSubmissionMapper cloudJobSubmissionMapper = CloudJobSubmissionMapper.INSTANCE;
    private final LOCALSubmissionMapper localSubmissionMapper = LOCALSubmissionMapper.INSTANCE;
    private final ResourceJobManagerMapper resourceJobManagerMapper = ResourceJobManagerMapper.INSTANCE;
    private final LOCALDataMovementMapper localDataMovementMapper = LOCALDataMovementMapper.INSTANCE;
    private final SCPDataMovementMapper scpDataMovementMapper = SCPDataMovementMapper.INSTANCE;
    private final UnicoreDataMovementMapper unicoreDataMovementMapper = UnicoreDataMovementMapper.INSTANCE;
    private final UnicoreJobSubmissionMapper unicoreJobSubmissionMapper = UnicoreJobSubmissionMapper.INSTANCE;
    private final SSHJobSubmissionMapper sshJobSubmissionMapper = SSHJobSubmissionMapper.INSTANCE;
    private final StorageResourceDescriptionMapper storageResourceDescriptionMapper =
            StorageResourceDescriptionMapper.INSTANCE;
    private final StoragePreferenceMapper storagePreferenceMapper = StoragePreferenceMapper.INSTANCE;
    private final ComputeResourcePreferenceMapper computeResourcePreferenceMapper =
            ComputeResourcePreferenceMapper.INSTANCE;
    private final UserComputeResourcePreferenceMapper userComputeResourcePreferenceMapper =
            UserComputeResourcePreferenceMapper.INSTANCE;
    private final UserStoragePreferenceMapper userStoragePreferenceMapper = UserStoragePreferenceMapper.INSTANCE;
    private final ApplicationModuleMapper applicationModuleMapper = ApplicationModuleMapper.INSTANCE;
    private final ParserMapper parserMapper = ParserMapper.INSTANCE;
    private final ParsingTemplateMapper parsingTemplateMapper = ParsingTemplateMapper.INSTANCE;
    private final ComputationalResourceSchedulingModelMapper computationalResourceSchedulingModelMapper =
            ComputationalResourceSchedulingModelMapper.INSTANCE;
    private final GroupComputeResourcePreferenceMapper groupComputeResourcePreferenceMapper =
            GroupComputeResourcePreferenceMapper.INSTANCE;
    private final ComputeResourcePolicyMapper computeResourcePolicyMapper = ComputeResourcePolicyMapper.INSTANCE;
    private final BatchQueueResourcePolicyMapper batchQueueResourcePolicyMapper =
            BatchQueueResourcePolicyMapper.INSTANCE;
    private final GatewayGroupsMapper gatewayGroupsMapper = GatewayGroupsMapper.INSTANCE;
    private final ProcessStatusMapper processStatusMapper = ProcessStatusMapper.INSTANCE;

    public AiravataServiceHandler(AiravataService airavataService) throws AiravataException {
        this.airavataService = airavataService;
        this.airavataService.init();
    }

    // Helper method to convert domain exceptions to thrift exceptions
    private org.apache.airavata.thriftapi.exception.AiravataSystemException convertToThriftSystemException(
            org.apache.airavata.common.exception.AiravataSystemException e) {
        var thriftException = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
        thriftException.setMessage(e.getMessage());
        if (e.getAiravataErrorType() != null) {
            thriftException.setAiravataErrorType(
                    org.apache.airavata.thriftapi.exception.AiravataErrorType.valueOf(e.getAiravataErrorType().name()));
        }
        thriftException.initCause(e);
        return thriftException;
    }

    private org.apache.airavata.thriftapi.exception.InvalidRequestException convertToThriftInvalidRequestException(
            org.apache.airavata.common.exception.InvalidRequestException e) {
        var thriftException = new org.apache.airavata.thriftapi.exception.InvalidRequestException();
        thriftException.setMessage(e.getMessage());
        thriftException.initCause(e);
        return thriftException;
    }

    private org.apache.airavata.thriftapi.exception.AiravataClientException convertToThriftAiravataClientException(
            org.apache.airavata.common.exception.AiravataClientException e) {
        var thriftException = new org.apache.airavata.thriftapi.exception.AiravataClientException();
        thriftException.setAiravataErrorType(
                org.apache.airavata.thriftapi.exception.AiravataErrorType.valueOf(e.getAiravataErrorType().name()));
        if (e.getMessage() != null) {
            thriftException.setParameter(e.getMessage());
        }
        thriftException.initCause(e);
        return thriftException;
    }

    private org.apache.airavata.thriftapi.exception.AuthorizationException convertToThriftAuthorizationException(
            org.apache.airavata.common.exception.AuthorizationException e) {
        var thriftException = new org.apache.airavata.thriftapi.exception.AuthorizationException();
        thriftException.setMessage(e.getMessage());
        thriftException.initCause(e);
        return thriftException;
    }

    private CredentialStoreException convertToThriftCredentialStoreException(
            org.apache.airavata.credential.exception.CredentialStoreException e) {
        var thriftException = new CredentialStoreException();
        thriftException.setMessage(e.getMessage());
        thriftException.initCause(e);
        return thriftException;
    }

    private org.apache.airavata.thriftapi.exception.ExperimentNotFoundException convertToThriftExperimentNotFoundException(
            org.apache.airavata.common.exception.ExperimentNotFoundException e) {
        var thriftException = new org.apache.airavata.thriftapi.exception.ExperimentNotFoundException();
        thriftException.setMessage(e.getMessage());
        thriftException.initCause(e);
        return thriftException;
    }

    private org.apache.airavata.thriftapi.exception.ProjectNotFoundException convertToThriftProjectNotFoundException(
            org.apache.airavata.common.exception.ProjectNotFoundException e) {
        var thriftException = new org.apache.airavata.thriftapi.exception.ProjectNotFoundException();
        thriftException.setMessage(e.getMessage());
        thriftException.initCause(e);
        return thriftException;
    }

    @Override
    public String getAPIVersion() throws AiravataSystemException, TException {
        return airavata_apiConstants.AIRAVATA_API_VERSION;
    }

    @Override
    @SecurityCheck
    public boolean isUserExists(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken,
            String gatewayId,
            String userName)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
            org.apache.airavata.thriftapi.exception.AiravataClientException,
            org.apache.airavata.thriftapi.exception.AiravataSystemException,
            org.apache.airavata.thriftapi.exception.AuthorizationException,
            TException {
        try {
        // Convert thrift model to domain model
        org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
        return airavataService.isUserExists(gatewayId, userName);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            var exception = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            exception.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error checking if user exists: " + e.getMessage());
            exception.initCause(e);
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public String addGateway(AuthzToken authzToken, Gateway gateway)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        try {
        // Convert thrift model to domain model
            var domainGateway = gatewayMapper.toDomain(gateway);
        return airavataService.addGateway(domainGateway);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            var exception = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            exception.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error adding gateway: " + e.getMessage());
            exception.initCause(e);
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public List<String> getAllUsersInGateway(AuthzToken authzToken, String gatewayId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        try {
        return airavataService.getAllUsersInGateway(gatewayId);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            var exception = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            exception.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error getting all users in gateway: " + e.getMessage());
            exception.initCause(e);
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public boolean updateGateway(AuthzToken authzToken, String gatewayId, Gateway updatedGateway)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        try {
        // Convert thrift model to domain model
            org.apache.airavata.common.model.Gateway domainGateway = gatewayMapper.toDomain(updatedGateway);
        return airavataService.updateGateway(gatewayId, domainGateway);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            AiravataSystemException thriftException = new AiravataSystemException();
            thriftException.setMessage(e.getMessage());
            if (e.getAiravataErrorType() != null) {
                thriftException.setAiravataErrorType(
                        AiravataErrorType.valueOf(e.getAiravataErrorType().name()));
            }
            thriftException.initCause(e);
            throw thriftException;
        } catch (Exception e) {
            AiravataSystemException exception = new AiravataSystemException();
            exception.setMessage("Error updating gateway: " + e.getMessage());
            exception.initCause(e);
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public Gateway getGateway(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken,
            String gatewayId)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
            org.apache.airavata.thriftapi.exception.AiravataClientException,
            org.apache.airavata.thriftapi.exception.AiravataSystemException,
            org.apache.airavata.thriftapi.exception.AuthorizationException,
            TException {
        try {
        // Get domain model from service
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            org.apache.airavata.common.model.Gateway domainGateway = airavataService.getGateway(gatewayId);
        // Convert domain model to thrift model
        return gatewayMapper.toThrift(domainGateway);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            AiravataSystemException exception = new AiravataSystemException();
            exception.setMessage("Error getting gateway: " + e.getMessage());
            exception.initCause(e);
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public boolean deleteGateway(AuthzToken authzToken, String gatewayId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        try {
        return airavataService.deleteGateway(gatewayId);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException exception = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            exception.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error deleting gateway: " + e.getMessage());
            exception.initCause(e);
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public List<Gateway> getAllGateways(AuthzToken authzToken)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        try {
        // Get domain models from service
            List<org.apache.airavata.common.model.Gateway> domainGateways = airavataService.getAllGateways();
        // Convert domain models to thrift models
        return domainGateways.stream().map(gatewayMapper::toThrift).collect(Collectors.toList());
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException exception = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            exception.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error getting all gateways: " + e.getMessage());
            exception.initCause(e);
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public boolean isGatewayExist(AuthzToken authzToken, String gatewayId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        try {
        return airavataService.isGatewayExist(gatewayId);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException exception = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            exception.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error checking if gateway exists: " + e.getMessage());
            exception.initCause(e);
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public String createNotification(AuthzToken authzToken, Notification notification)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        try {
        // Convert thrift model to domain model
            org.apache.airavata.common.model.Notification domainNotification =
                    notificationMapper.toDomain(notification);
        return airavataService.createNotification(domainNotification);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error creating notification: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public boolean updateNotification(AuthzToken authzToken, Notification notification)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        try {
        // Convert thrift model to domain model
            org.apache.airavata.common.model.Notification domainNotification =
                    notificationMapper.toDomain(notification);
        return airavataService.updateNotification(domainNotification);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error updating notification: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public boolean deleteNotification(AuthzToken authzToken, String gatewayId, String notificationId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        try {
        return airavataService.deleteNotification(gatewayId, notificationId);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error deleting notification: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    // No security check
    @Override
    public Notification getNotification(AuthzToken authzToken, String gatewayId, String notificationId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        try {
        // Get domain model from service
            org.apache.airavata.common.model.Notification domainNotification =
                    airavataService.getNotification(gatewayId, notificationId);
        // Convert domain model to thrift model
        return notificationMapper.toThrift(domainNotification);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error getting notification: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    // No security check
    @Override
    public List<Notification> getAllNotifications(AuthzToken authzToken, String gatewayId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        try {
        // Get domain models from service
            List<org.apache.airavata.common.model.Notification> domainNotifications =
                    airavataService.getAllNotifications(gatewayId);
        // Convert domain models to thrift models
            return domainNotifications.stream()
                    .map(notificationMapper::toThrift)
                    .collect(Collectors.toList());
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error getting all notifications: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public String generateAndRegisterSSHKeys(AuthzToken authzToken, String description)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, TException {
        try {
        // Convert thrift model to domain model
        org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
        String gatewayId = domainAuthzToken.getClaimsMap().get(Constants.GATEWAY_ID);
        String userName = domainAuthzToken.getClaimsMap().get(Constants.USER_NAME);
        return airavataService.generateAndRegisterSSHKeys(gatewayId, userName, description);
        } catch (org.apache.airavata.common.exception.InvalidRequestException e) {
            throw convertToThriftInvalidRequestException(e);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error generating and registering SSH keys: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public String registerPwdCredential(
            AuthzToken authzToken, String loginUserName, String password, String description)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, TException {
        try {
        // Convert thrift model to domain model
        org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
        String gatewayId = domainAuthzToken.getClaimsMap().get(Constants.GATEWAY_ID);
        String userName = domainAuthzToken.getClaimsMap().get(Constants.USER_NAME);
        return airavataService.registerPwdCredential(gatewayId, userName, loginUserName, password, description);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (org.apache.airavata.common.exception.InvalidRequestException e) {
            throw convertToThriftInvalidRequestException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException exception = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            exception.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error registering password credential: " + e.getMessage());
            exception.initCause(e);
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public org.apache.airavata.thriftapi.credential.model.CredentialSummary getCredentialSummary(
            AuthzToken authzToken, String tokenId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        try {
        // Convert thrift model to domain model
        org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
        String gatewayId = domainAuthzToken.getClaimsMap().get(Constants.GATEWAY_ID);
            org.apache.airavata.credential.model.CredentialSummary domainSummary = airavataService.getCredentialSummary(tokenId, gatewayId);
            return credentialSummaryMapper.toThrift(domainSummary);
        } catch (org.apache.airavata.credential.exception.CredentialStoreException e) {
            throw convertToThriftCredentialStoreException(e);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException exception = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            exception.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error getting credential summary: " + e.getMessage());
            exception.initCause(e);
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public List<org.apache.airavata.thriftapi.credential.model.CredentialSummary> getAllCredentialSummaries(
            AuthzToken authzToken, org.apache.airavata.thriftapi.credential.model.SummaryType type)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
        org.apache.airavata.credential.model.SummaryType domainType = org.apache.airavata.credential.model.SummaryType.valueOf(type.name());
        String gatewayId = domainAuthzToken.getClaimsMap().get(Constants.GATEWAY_ID);
        String userName = domainAuthzToken.getClaimsMap().get(Constants.USER_NAME);
        try {
            List<org.apache.airavata.credential.model.CredentialSummary> domainSummaries =
                    airavataService.getAllCredentialSummaries(domainAuthzToken, domainType, gatewayId, userName);
            return domainSummaries.stream()
                    .map(credentialSummaryMapper::toThrift)
                    .collect(Collectors.toList());
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (org.apache.airavata.common.exception.InvalidRequestException e) {
            throw convertToThriftInvalidRequestException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException exception = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            exception.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error getting all credential summaries: " + e.getMessage());
            exception.initCause(e);
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public boolean deleteSSHPubKey(AuthzToken authzToken, String airavataCredStoreToken)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        try {
        // Convert thrift model to domain model
        org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
        String gatewayId = domainAuthzToken.getClaimsMap().get(Constants.GATEWAY_ID);
            return airavataService.deleteSSHCredential(airavataCredStoreToken, gatewayId);
        } catch (org.apache.airavata.credential.exception.CredentialStoreException e) {
            throw convertToThriftCredentialStoreException(e);
        } catch (Exception e) {
            AiravataSystemException exception = new AiravataSystemException();
            exception.setMessage("Error deleting SSH credential: " + e.getMessage());
            exception.initCause(e);
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public boolean deletePWDCredential(AuthzToken authzToken, String airavataCredStoreToken)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        try {
        // Convert thrift model to domain model
        org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
        String gatewayId = domainAuthzToken.getClaimsMap().get(Constants.GATEWAY_ID);
            return airavataService.deletePWDCredential(domainAuthzToken, airavataCredStoreToken, gatewayId);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (org.apache.airavata.common.exception.AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error deleting PWD credential: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public String createProject(AuthzToken authzToken, String gatewayId, Project project)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        try {
        // Convert thrift model to domain model
            org.apache.airavata.common.model.Project domainProject = projectMapper.toDomain(project);
        return airavataService.createProject(gatewayId, domainProject);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (org.apache.airavata.common.exception.InvalidRequestException e) {
            throw convertToThriftInvalidRequestException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException exception = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            exception.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error creating project: " + e.getMessage());
            exception.initCause(e);
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public void updateProject(AuthzToken authzToken, String projectId, Project updatedProject)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, ProjectNotFoundException,
                    AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
        // Convert thrift model to domain model
            org.apache.airavata.common.model.Project domainProject = projectMapper.toDomain(updatedProject);
            airavataService.updateProject(domainAuthzToken, projectId, domainProject);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            AiravataSystemException thriftException = new AiravataSystemException();
            thriftException.setMessage(e.getMessage());
            if (e.getAiravataErrorType() != null) {
                thriftException.setAiravataErrorType(
                        AiravataErrorType.valueOf(e.getAiravataErrorType().name()));
            }
            thriftException.initCause(e);
            throw thriftException;
        } catch (org.apache.airavata.common.exception.InvalidRequestException e) {
            InvalidRequestException thriftException = new InvalidRequestException();
            thriftException.setMessage(e.getMessage());
            thriftException.initCause(e);
            throw thriftException;
        } catch (ProjectNotFoundException e) {
            ProjectNotFoundException thriftException = new ProjectNotFoundException();
            thriftException.setMessage(e.getMessage());
            thriftException.initCause(e);
            throw thriftException;
        } catch (Exception e) {
            AiravataSystemException exception = new AiravataSystemException();
            exception.setMessage("Error updating project: " + e.getMessage());
            exception.initCause(e);
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public boolean deleteProject(AuthzToken authzToken, String projectId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, ProjectNotFoundException,
                    AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            return airavataService.deleteProject(domainAuthzToken, projectId);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (org.apache.airavata.common.exception.InvalidRequestException e) {
            throw convertToThriftInvalidRequestException(e);
        } catch (org.apache.airavata.common.exception.ProjectNotFoundException e) {
            org.apache.airavata.thriftapi.exception.ProjectNotFoundException thriftException = new org.apache.airavata.thriftapi.exception.ProjectNotFoundException();
            thriftException.setMessage(e.getMessage());
            thriftException.initCause(e);
            throw thriftException;
        } catch (Exception e) {
            AiravataSystemException exception = new AiravataSystemException();
            exception.setMessage("Error deleting project: " + e.getMessage());
            exception.initCause(e);
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public Project getProject(AuthzToken authzToken, String projectId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, ProjectNotFoundException,
                    AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
        // Get domain model from service
            org.apache.airavata.common.model.Project domainProject =
                    airavataService.getProject(domainAuthzToken, projectId);
        // Convert domain model to thrift model
        return projectMapper.toThrift(domainProject);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            AiravataSystemException thriftException = new AiravataSystemException();
            thriftException.setMessage(e.getMessage());
            if (e.getAiravataErrorType() != null) {
                thriftException.setAiravataErrorType(
                        AiravataErrorType.valueOf(e.getAiravataErrorType().name()));
            }
            thriftException.initCause(e);
            throw thriftException;
        } catch (org.apache.airavata.common.exception.InvalidRequestException e) {
            InvalidRequestException thriftException = new InvalidRequestException();
            thriftException.setMessage(e.getMessage());
            thriftException.initCause(e);
            throw thriftException;
        } catch (ProjectNotFoundException e) {
            ProjectNotFoundException thriftException = new ProjectNotFoundException();
            thriftException.setMessage(e.getMessage());
            thriftException.initCause(e);
            throw thriftException;
        } catch (Exception e) {
            AiravataSystemException exception = new AiravataSystemException();
            exception.setMessage("Error getting project: " + e.getMessage());
            exception.initCause(e);
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public List<Project> getUserProjects(
            AuthzToken authzToken, String gatewayId, String userName, int limit, int offset)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        try {
        // Get domain models from service
        org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            List<org.apache.airavata.common.model.Project> domainProjects =
                    airavataService.getUserProjects(domainAuthzToken, gatewayId, userName, limit, offset);
        // Convert domain models to thrift models
        return domainProjects.stream().map(projectMapper::toThrift).collect(Collectors.toList());
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error getting user projects: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public List<Project> searchProjects(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken,
            String gatewayId,
            String userName,
            Map<ProjectSearchFields, String> filters,
            int limit,
            int offset)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
            org.apache.airavata.thriftapi.exception.AiravataClientException,
            org.apache.airavata.thriftapi.exception.AiravataSystemException,
            org.apache.airavata.thriftapi.exception.AuthorizationException,
            TException {
        try {
            // Convert thrift models to domain models
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            Map<org.apache.airavata.common.model.ProjectSearchFields, String> domainFilters = new java.util.HashMap<>();
            for (Map.Entry<ProjectSearchFields, String> entry : filters.entrySet()) {
                domainFilters.put(org.apache.airavata.common.model.ProjectSearchFields.valueOf(entry.getKey().name()), entry.getValue());
            }
            // Get domain models from service
            List<org.apache.airavata.common.model.Project> domainProjects =
                    airavataService.searchProjects(domainAuthzToken, gatewayId, userName, domainFilters, limit, offset);
            // Convert domain models to thrift models
            return domainProjects.stream().map(projectMapper::toThrift).collect(Collectors.toList());
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException exception = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            exception.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error searching projects: " + e.getMessage());
            exception.initCause(e);
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public List<ExperimentSummaryModel> searchExperiments(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken,
            String gatewayId,
            String userName,
            Map<ExperimentSearchFields, String> filters,
            int limit,
            int offset)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
            org.apache.airavata.thriftapi.exception.AiravataClientException,
            org.apache.airavata.thriftapi.exception.AiravataSystemException,
            org.apache.airavata.thriftapi.exception.AuthorizationException,
            TException {
        // Convert thrift models to domain models
        org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
        Map<org.apache.airavata.common.model.ExperimentSearchFields, String> domainFilters = new java.util.HashMap<>();
        for (Map.Entry<ExperimentSearchFields, String> entry : filters.entrySet()) {
            domainFilters.put(org.apache.airavata.common.model.ExperimentSearchFields.valueOf(entry.getKey().name()), entry.getValue());
        }
        try {
        // Get domain models from service
            List<org.apache.airavata.common.model.ExperimentSummaryModel> domainSummaries = airavataService.searchExperiments(
                    domainAuthzToken, gatewayId, userName, domainFilters, limit, offset);
        // Convert domain models to thrift models
        return domainSummaries.stream()
                .map(experimentSummaryModelMapper::toThrift)
                .collect(Collectors.toList());
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error searching experiments: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public ExperimentStatistics getExperimentStatistics(
            AuthzToken authzToken,
            String gatewayId,
            long fromTime,
            long toTime,
            String userName,
            String applicationName,
            String resourceHostName,
            int limit,
            int offset)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        try {
        org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
        List<String> accessibleExpIds = null;
            org.apache.airavata.common.model.ExperimentStatistics domainStats = airavataService.getExperimentStatistics(
                gatewayId,
                fromTime,
                toTime,
                userName,
                applicationName,
                resourceHostName,
                accessibleExpIds,
                limit,
                offset);
        return experimentStatisticsMapper.toThrift(domainStats);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException thriftException =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            thriftException.setMessage(e.getMessage());
            if (e.getAiravataErrorType() != null) {
                thriftException.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.valueOf(
                        e.getAiravataErrorType().name()));
            }
            thriftException.initCause(e);
            throw thriftException;
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error getting experiment statistics: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public List<ExperimentModel> getExperimentsInProject(AuthzToken authzToken, String projectId, int limit, int offset)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, ProjectNotFoundException,
                    AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            String gatewayId = domainAuthzToken.getClaimsMap().get(Constants.GATEWAY_ID);
        // Get domain models from service
            List<org.apache.airavata.common.model.ExperimentModel> domainExperiments =
                    airavataService.getExperimentsInProject(gatewayId, projectId, limit, offset);
        // Convert domain models to thrift models
            return domainExperiments.stream()
                    .map(experimentModelMapper::toThrift)
                    .collect(Collectors.toList());
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException thriftException =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            thriftException.setMessage(e.getMessage());
            if (e.getAiravataErrorType() != null) {
                thriftException.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.valueOf(
                        e.getAiravataErrorType().name()));
            }
            thriftException.initCause(e);
            throw thriftException;
        } catch (ProjectNotFoundException e) {
            ProjectNotFoundException thriftException = new ProjectNotFoundException();
            thriftException.setMessage(e.getMessage());
            thriftException.initCause(e);
            throw thriftException;
        } catch (Exception e) {
            AiravataSystemException exception = new AiravataSystemException();
            exception.setMessage("Error getting experiments in project: " + e.getMessage());
            exception.initCause(e);
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public List<ExperimentModel> getUserExperiments(
            AuthzToken authzToken, String gatewayId, String userName, int limit, int offset)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        try {
        // Get domain models from service
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            List<org.apache.airavata.common.model.ExperimentModel> domainExperiments =
                airavataService.getUserExperiments(gatewayId, userName, limit, offset);
        // Convert domain models to thrift models
            return domainExperiments.stream()
                    .map(experimentModelMapper::toThrift)
                    .collect(Collectors.toList());
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException thriftException =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            thriftException.setMessage(e.getMessage());
            if (e.getAiravataErrorType() != null) {
                thriftException.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.valueOf(
                        e.getAiravataErrorType().name()));
            }
            thriftException.initCause(e);
            throw thriftException;
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException exception =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            exception.setMessage("Error getting user experiments: " + e.getMessage());
            exception.initCause(e);
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public String createExperiment(AuthzToken authzToken, String gatewayId, ExperimentModel experiment)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        try {
            // Convert thrift model to domain model
            org.apache.airavata.common.model.ExperimentModel domainExperiment =
                    experimentModelMapper.toDomain(experiment);
            return airavataService.createExperiment(gatewayId, domainExperiment);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException exception =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            exception.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error creating experiment: " + e.getMessage());
            exception.initCause(e);
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public boolean deleteExperiment(AuthzToken authzToken, String experimentId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            return airavataService.deleteExperimentWithAuth(domainAuthzToken, experimentId);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (org.apache.airavata.common.exception.InvalidRequestException e) {
            throw convertToThriftInvalidRequestException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException exception = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            exception.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error deleting experiment: " + e.getMessage());
            exception.initCause(e);
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public ExperimentModel getExperiment(AuthzToken authzToken, String airavataExperimentId)
            throws InvalidRequestException, ExperimentNotFoundException, AiravataClientException,
                    AiravataSystemException, AuthorizationException, TException {
        try {
        // Get domain model from service
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            org.apache.airavata.common.model.ExperimentModel domainExperiment =
                    airavataService.getExperiment(domainAuthzToken, airavataExperimentId);
        // Convert domain model to thrift model
        return experimentModelMapper.toThrift(domainExperiment);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException thriftException =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            thriftException.setMessage(e.getMessage());
            if (e.getAiravataErrorType() != null) {
                thriftException.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.valueOf(
                        e.getAiravataErrorType().name()));
            }
            thriftException.initCause(e);
            throw thriftException;
        } catch (org.apache.airavata.common.exception.InvalidRequestException e) {
            throw convertToThriftInvalidRequestException(e);
        } catch (org.apache.airavata.common.exception.ExperimentNotFoundException e) {
            org.apache.airavata.thriftapi.exception.ExperimentNotFoundException thriftException = new org.apache.airavata.thriftapi.exception.ExperimentNotFoundException();
            thriftException.setMessage(e.getMessage());
            thriftException.initCause(e);
            throw thriftException;
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException exception = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            exception.setMessage("Error getting experiment: " + e.getMessage());
            exception.initCause(e);
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public ExperimentModel getExperimentByAdmin(AuthzToken authzToken, String airavataExperimentId)
            throws InvalidRequestException, ExperimentNotFoundException, AiravataClientException,
                    AiravataSystemException, AuthorizationException, TException {
        try {
        // Get domain model from service
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            org.apache.airavata.common.model.ExperimentModel domainExperiment =
                    airavataService.getExperimentByAdmin(domainAuthzToken, airavataExperimentId);
        // Convert domain model to thrift model
        return experimentModelMapper.toThrift(domainExperiment);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException thriftException =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            thriftException.setMessage(e.getMessage());
            if (e.getAiravataErrorType() != null) {
                thriftException.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.valueOf(
                        e.getAiravataErrorType().name()));
            }
            thriftException.initCause(e);
            throw thriftException;
        } catch (InvalidRequestException e) {
            throw convertToThriftInvalidRequestException(e);
        } catch (ExperimentNotFoundException e) {
            ExperimentNotFoundException thriftException = new ExperimentNotFoundException();
            thriftException.setMessage(e.getMessage());
            thriftException.initCause(e);
            throw thriftException;
        } catch (Exception e) {
            AiravataSystemException exception = new AiravataSystemException();
            exception.setMessage("Error getting experiment by admin: " + e.getMessage());
            exception.initCause(e);
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public ExperimentModel getDetailedExperimentTree(AuthzToken authzToken, String airavataExperimentId)
            throws InvalidRequestException, ExperimentNotFoundException, AiravataClientException,
                    AiravataSystemException, AuthorizationException, TException {
        try {
        // Get domain model from service
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            org.apache.airavata.common.model.ExperimentModel domainExperiment =
                    airavataService.getDetailedExperimentTree(airavataExperimentId);
        // Convert domain model to thrift model
        return experimentModelMapper.toThrift(domainExperiment);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException thriftException =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            thriftException.setMessage(e.getMessage());
            if (e.getAiravataErrorType() != null) {
                thriftException.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.valueOf(
                        e.getAiravataErrorType().name()));
            }
            thriftException.initCause(e);
            throw thriftException;
        } catch (InvalidRequestException e) {
            throw convertToThriftInvalidRequestException(e);
        } catch (ExperimentNotFoundException e) {
            ExperimentNotFoundException thriftException = new ExperimentNotFoundException();
            thriftException.setMessage(e.getMessage());
            thriftException.initCause(e);
            throw thriftException;
        } catch (Exception e) {
            AiravataSystemException exception = new AiravataSystemException();
            exception.setMessage("Error getting detailed experiment tree: " + e.getMessage());
            exception.initCause(e);
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public void updateExperiment(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken,
            String airavataExperimentId,
            ExperimentModel experiment)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
            org.apache.airavata.thriftapi.exception.ExperimentNotFoundException,
            org.apache.airavata.thriftapi.exception.AiravataClientException,
            org.apache.airavata.thriftapi.exception.AiravataSystemException,
            org.apache.airavata.thriftapi.exception.AuthorizationException,
            TException {
        try {
            // Convert thrift model to domain model
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            org.apache.airavata.common.model.ExperimentModel domainExperiment =
                    experimentModelMapper.toDomain(experiment);
            airavataService.updateExperiment(domainAuthzToken, airavataExperimentId, domainExperiment);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException exception =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            exception.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error updating experiment: " + e.getMessage());
            exception.initCause(e);
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public void updateExperimentConfiguration(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken,
            String airavataExperimentId,
            UserConfigurationDataModel userConfiguration)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
            org.apache.airavata.thriftapi.exception.AiravataClientException,
            org.apache.airavata.thriftapi.exception.AiravataSystemException,
            org.apache.airavata.thriftapi.exception.AuthorizationException,
            TException {
        try {
        org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
        org.apache.airavata.common.model.UserConfigurationDataModel domainUserConfig = userConfigurationDataModelMapper.toDomain(userConfiguration);
        airavataService.updateExperimentConfiguration(airavataExperimentId, domainUserConfig);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error updating experiment configuration: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @SecurityCheck
    public void updateResourceScheduling(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken,
            String airavataExperimentId,
            ComputationalResourceSchedulingModel resourceScheduling)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
            org.apache.airavata.thriftapi.exception.AiravataClientException,
            org.apache.airavata.thriftapi.exception.AiravataSystemException,
            org.apache.airavata.thriftapi.exception.AuthorizationException,
            TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            org.apache.airavata.common.model.ComputationalResourceSchedulingModel domainScheduling =
                    computationalResourceSchedulingModelMapper.toDomain(resourceScheduling);
            airavataService.updateResourceScheduleing(airavataExperimentId, domainScheduling);
        } catch (org.apache.airavata.common.exception.InvalidRequestException e) {
            throw convertToThriftInvalidRequestException(e);
        } catch (org.apache.airavata.common.exception.AiravataClientException e) {
            throw convertToThriftAiravataClientException(e);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (org.apache.airavata.common.exception.AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error updating resource scheduling: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public boolean validateExperiment(AuthzToken authzToken, String airavataExperimentId)
            throws InvalidRequestException, ExperimentNotFoundException, AiravataClientException,
                    AiravataSystemException, AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            // Validate experiment access first
            org.apache.airavata.common.model.ExperimentModel experiment = airavataService.getExperiment(domainAuthzToken, airavataExperimentId);
            // Basic validation - check if experiment exists and user has access
            // Full validation would require orchestrator service which is not directly
            // accessible
            // For now, return true if experiment exists and is accessible
            return experiment != null;
        } catch (org.apache.airavata.common.exception.InvalidRequestException e) {
            throw convertToThriftInvalidRequestException(e);
        } catch (org.apache.airavata.common.exception.ExperimentNotFoundException e) {
            org.apache.airavata.thriftapi.exception.ExperimentNotFoundException thriftException = new org.apache.airavata.thriftapi.exception.ExperimentNotFoundException();
            thriftException.setMessage(e.getMessage());
            thriftException.initCause(e);
            throw thriftException;
        } catch (org.apache.airavata.common.exception.AiravataClientException e) {
            throw convertToThriftAiravataClientException(e);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (org.apache.airavata.common.exception.AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error validating experiment: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public ExperimentStatus getExperimentStatus(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken,
            String airavataExperimentId)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
            org.apache.airavata.thriftapi.exception.ExperimentNotFoundException,
            org.apache.airavata.thriftapi.exception.AiravataClientException,
            org.apache.airavata.thriftapi.exception.AiravataSystemException,
            org.apache.airavata.thriftapi.exception.AuthorizationException,
            TException {
        try {
            org.apache.airavata.common.model.ExperimentStatus domainStatus = airavataService.getExperimentStatus(airavataExperimentId);
            ExperimentStatus thriftStatus = new ExperimentStatus();
            thriftStatus.setState(
                    ExperimentState.valueOf(domainStatus.getState().name()));
            thriftStatus.setTimeOfStateChange(domainStatus.getTimeOfStateChange());
            thriftStatus.setReason(domainStatus.getReason());
            thriftStatus.setStatusId(domainStatus.getStatusId());
            return thriftStatus;
        } catch (org.apache.airavata.common.exception.InvalidRequestException e) {
            throw convertToThriftInvalidRequestException(e);
        } catch (org.apache.airavata.common.exception.ExperimentNotFoundException e) {
            org.apache.airavata.thriftapi.exception.ExperimentNotFoundException thriftException = new org.apache.airavata.thriftapi.exception.ExperimentNotFoundException();
            thriftException.setMessage(e.getMessage());
            thriftException.initCause(e);
            throw thriftException;
        } catch (org.apache.airavata.common.exception.AiravataClientException e) {
            throw convertToThriftAiravataClientException(e);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (org.apache.airavata.common.exception.AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error getting experiment status: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public List<OutputDataObjectType> getExperimentOutputs(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken,
            String airavataExperimentId)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
            org.apache.airavata.thriftapi.exception.ExperimentNotFoundException,
            org.apache.airavata.thriftapi.exception.AiravataClientException,
            org.apache.airavata.thriftapi.exception.AiravataSystemException,
            org.apache.airavata.thriftapi.exception.AuthorizationException,
            TException {
        try {
        // Get domain models from service
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            List<org.apache.airavata.common.model.OutputDataObjectType> domainOutputs = airavataService.getExperimentOutputs(airavataExperimentId);
        // Convert domain models to thrift models
            return domainOutputs.stream()
                    .map(outputDataObjectTypeMapper::toThrift)
                    .collect(Collectors.toList());
        } catch (org.apache.airavata.common.exception.InvalidRequestException e) {
            throw convertToThriftInvalidRequestException(e);
        } catch (org.apache.airavata.common.exception.ExperimentNotFoundException e) {
            org.apache.airavata.thriftapi.exception.ExperimentNotFoundException thriftException = new org.apache.airavata.thriftapi.exception.ExperimentNotFoundException();
            thriftException.setMessage(e.getMessage());
            thriftException.initCause(e);
            throw thriftException;
        } catch (org.apache.airavata.common.exception.AiravataClientException e) {
            throw convertToThriftAiravataClientException(e);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (org.apache.airavata.common.exception.AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error getting experiment outputs: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public List<OutputDataObjectType> getIntermediateOutputs(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken,
            String airavataExperimentId)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
            org.apache.airavata.thriftapi.exception.ExperimentNotFoundException,
            org.apache.airavata.thriftapi.exception.AiravataClientException,
            org.apache.airavata.thriftapi.exception.AiravataSystemException,
            org.apache.airavata.thriftapi.exception.AuthorizationException,
            TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            // getIntermediateOutputs is not directly available in AiravataService
            // Using registry service through experiment service
            // This is a workaround - ideally AiravataService should expose this method
            org.apache.airavata.common.model.ExperimentModel experiment = airavataService.getExperiment(domainAuthzToken, airavataExperimentId);
            // For now, return empty list as getIntermediateOutputs requires registry
            // service access
            // TODO: Add getIntermediateOutputs method to AiravataService
            List<org.apache.airavata.common.model.OutputDataObjectType> domainOutputs = new java.util.ArrayList<>();
            return domainOutputs.stream()
                    .map(outputDataObjectTypeMapper::toThrift)
                    .collect(Collectors.toList());
        } catch (org.apache.airavata.common.exception.InvalidRequestException e) {
            throw convertToThriftInvalidRequestException(e);
        } catch (org.apache.airavata.common.exception.ExperimentNotFoundException e) {
            org.apache.airavata.thriftapi.exception.ExperimentNotFoundException thriftException = new org.apache.airavata.thriftapi.exception.ExperimentNotFoundException();
            thriftException.setMessage(e.getMessage());
            thriftException.initCause(e);
            throw thriftException;
        } catch (org.apache.airavata.common.exception.AiravataClientException e) {
            throw convertToThriftAiravataClientException(e);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (org.apache.airavata.common.exception.AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error getting intermediate outputs: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public void fetchIntermediateOutputs(AuthzToken authzToken, String airavataExperimentId, List<String> outputNames)
            throws InvalidRequestException, ExperimentNotFoundException, AiravataClientException,
                    AiravataSystemException, AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            airavataService.fetchIntermediateOutputs(domainAuthzToken, airavataExperimentId, outputNames);
        } catch (org.apache.airavata.common.exception.InvalidRequestException e) {
            throw convertToThriftInvalidRequestException(e);
        } catch (org.apache.airavata.common.exception.ExperimentNotFoundException e) {
            org.apache.airavata.thriftapi.exception.ExperimentNotFoundException thriftException = new org.apache.airavata.thriftapi.exception.ExperimentNotFoundException();
            thriftException.setMessage(e.getMessage());
            thriftException.initCause(e);
            throw thriftException;
        } catch (org.apache.airavata.common.exception.AiravataClientException e) {
            throw convertToThriftAiravataClientException(e);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (org.apache.airavata.common.exception.AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error fetching intermediate outputs: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public Map<String, JobStatus> getJobStatuses(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken,
            String airavataExperimentId)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
            org.apache.airavata.thriftapi.exception.ExperimentNotFoundException,
            org.apache.airavata.thriftapi.exception.AiravataClientException,
            org.apache.airavata.thriftapi.exception.AiravataSystemException,
            org.apache.airavata.thriftapi.exception.AuthorizationException,
            TException {
        try {
            Map<String, org.apache.airavata.common.model.JobStatus> domainStatuses = airavataService.getJobStatuses(airavataExperimentId);
            Map<String, JobStatus> thriftStatuses = new java.util.HashMap<>();
            for (Map.Entry<String, org.apache.airavata.common.model.JobStatus> entry : domainStatuses.entrySet()) {
                JobStatus thriftStatus = new JobStatus();
                thriftStatus.setJobState(
                        JobState.valueOf(entry.getValue().getJobState().name()));
                thriftStatus.setTimeOfStateChange(entry.getValue().getTimeOfStateChange());
                thriftStatus.setReason(entry.getValue().getReason());
                thriftStatus.setStatusId(entry.getValue().getStatusId());
                thriftStatuses.put(entry.getKey(), thriftStatus);
            }
            return thriftStatuses;
        } catch (org.apache.airavata.common.exception.InvalidRequestException e) {
            throw convertToThriftInvalidRequestException(e);
        } catch (org.apache.airavata.common.exception.ExperimentNotFoundException e) {
            org.apache.airavata.thriftapi.exception.ExperimentNotFoundException thriftException = new org.apache.airavata.thriftapi.exception.ExperimentNotFoundException();
            thriftException.setMessage(e.getMessage());
            thriftException.initCause(e);
            throw thriftException;
        } catch (org.apache.airavata.common.exception.AiravataClientException e) {
            throw convertToThriftAiravataClientException(e);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (org.apache.airavata.common.exception.AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error getting job statuses: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public List<JobModel> getJobDetails(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken,
            String airavataExperimentId)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
            org.apache.airavata.thriftapi.exception.ExperimentNotFoundException,
            org.apache.airavata.thriftapi.exception.AiravataClientException,
            org.apache.airavata.thriftapi.exception.AiravataSystemException,
            org.apache.airavata.thriftapi.exception.AuthorizationException,
            TException {
        try {
        // Get domain models from service
            List<org.apache.airavata.common.model.JobModel> domainJobs = airavataService.getJobDetails(airavataExperimentId);
        // Convert domain models to thrift models
        return domainJobs.stream().map(jobModelMapper::toThrift).collect(Collectors.toList());
        } catch (org.apache.airavata.common.exception.InvalidRequestException e) {
            throw convertToThriftInvalidRequestException(e);
        } catch (org.apache.airavata.common.exception.ExperimentNotFoundException e) {
            org.apache.airavata.thriftapi.exception.ExperimentNotFoundException thriftException = new org.apache.airavata.thriftapi.exception.ExperimentNotFoundException();
            thriftException.setMessage(e.getMessage());
            thriftException.initCause(e);
            throw thriftException;
        } catch (org.apache.airavata.common.exception.AiravataClientException e) {
            throw convertToThriftAiravataClientException(e);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (org.apache.airavata.common.exception.AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error getting job details: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public void launchExperiment(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken,
            final String airavataExperimentId,
            String gatewayId)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
            org.apache.airavata.thriftapi.exception.ExperimentNotFoundException,
            org.apache.airavata.thriftapi.exception.AiravataClientException,
            org.apache.airavata.thriftapi.exception.AiravataSystemException,
            org.apache.airavata.thriftapi.exception.AuthorizationException,
            TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            airavataService.launchExperiment(domainAuthzToken, gatewayId, airavataExperimentId);
        } catch (org.apache.airavata.common.exception.InvalidRequestException e) {
            throw convertToThriftInvalidRequestException(e);
        } catch (org.apache.airavata.common.exception.ExperimentNotFoundException e) {
            org.apache.airavata.thriftapi.exception.ExperimentNotFoundException thriftException = new org.apache.airavata.thriftapi.exception.ExperimentNotFoundException();
            thriftException.setMessage(e.getMessage());
            thriftException.initCause(e);
            throw thriftException;
        } catch (org.apache.airavata.common.exception.ProjectNotFoundException e) {
            org.apache.airavata.thriftapi.exception.ProjectNotFoundException thriftException = new org.apache.airavata.thriftapi.exception.ProjectNotFoundException();
            thriftException.setMessage(e.getMessage());
            thriftException.initCause(e);
            throw thriftException;
        } catch (org.apache.airavata.common.exception.AiravataClientException e) {
            throw convertToThriftAiravataClientException(e);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (org.apache.airavata.common.exception.AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (org.apache.airavata.sharing.model.SharingRegistryException e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException exception = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            exception.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error launching experiment: " + e.getMessage());
            exception.initCause(e);
            throw exception;
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error launching experiment: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public String cloneExperiment(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken,
            String existingExperimentID,
            String newExperimentName,
            String newExperimentProjectId)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
            org.apache.airavata.thriftapi.exception.ExperimentNotFoundException,
            org.apache.airavata.thriftapi.exception.AiravataClientException,
            org.apache.airavata.thriftapi.exception.AiravataSystemException,
            org.apache.airavata.thriftapi.exception.AuthorizationException,
            org.apache.airavata.thriftapi.exception.ProjectNotFoundException,
            TException {
        try {
        // getExperiment will apply sharing permissions
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            org.apache.airavata.common.model.ExperimentModel existingExperiment = airavataService.getExperiment(domainAuthzToken, existingExperimentID);
        return airavataService.cloneExperiment(
                    domainAuthzToken,
                    existingExperimentID,
                    newExperimentName,
                    newExperimentProjectId,
                    existingExperiment);
        } catch (org.apache.airavata.common.exception.InvalidRequestException e) {
            throw convertToThriftInvalidRequestException(e);
        } catch (org.apache.airavata.common.exception.ExperimentNotFoundException e) {
            throw convertToThriftExperimentNotFoundException(e);
        } catch (org.apache.airavata.common.exception.AiravataClientException e) {
            throw convertToThriftAiravataClientException(e);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (org.apache.airavata.common.exception.AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (org.apache.airavata.common.exception.ProjectNotFoundException e) {
            throw convertToThriftProjectNotFoundException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error cloning experiment: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public String cloneExperimentByAdmin(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken,
            String existingExperimentID,
            String newExperimentName,
            String newExperimentProjectId)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
            org.apache.airavata.thriftapi.exception.ExperimentNotFoundException,
            org.apache.airavata.thriftapi.exception.AiravataClientException,
            org.apache.airavata.thriftapi.exception.AiravataSystemException,
            org.apache.airavata.thriftapi.exception.AuthorizationException,
            org.apache.airavata.thriftapi.exception.ProjectNotFoundException,
            TException {
        try {
            // get existing experiment by bypassing normal sharing permissions for the admin
            // user
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            org.apache.airavata.common.model.ExperimentModel existingExperiment =
                    airavataService.getExperimentByAdmin(domainAuthzToken, existingExperimentID);
        return airavataService.cloneExperiment(
                    domainAuthzToken,
                    existingExperimentID,
                    newExperimentName,
                    newExperimentProjectId,
                    existingExperiment);
        } catch (org.apache.airavata.common.exception.InvalidRequestException e) {
            throw convertToThriftInvalidRequestException(e);
        } catch (org.apache.airavata.common.exception.ExperimentNotFoundException e) {
            throw convertToThriftExperimentNotFoundException(e);
        } catch (org.apache.airavata.common.exception.AiravataClientException e) {
            throw convertToThriftAiravataClientException(e);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (org.apache.airavata.common.exception.AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (org.apache.airavata.common.exception.ProjectNotFoundException e) {
            throw convertToThriftProjectNotFoundException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error cloning experiment by admin: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public void terminateExperiment(AuthzToken authzToken, String airavataExperimentId, String gatewayId)
            throws InvalidRequestException, ExperimentNotFoundException, AiravataClientException,
                    AiravataSystemException, AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
        airavataService.terminateExperiment(airavataExperimentId, gatewayId);
        } catch (InvalidRequestException e) {
            throw convertToThriftInvalidRequestException(e);
        } catch (ExperimentNotFoundException e) {
            throw convertToThriftExperimentNotFoundException(e);
        } catch (AiravataClientException e) {
            throw convertToThriftAiravataClientException(e);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (org.apache.airavata.common.exception.AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error terminating experiment: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public String registerApplicationModule(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken,
            String gatewayId,
            ApplicationModule applicationModule)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
            org.apache.airavata.thriftapi.exception.AiravataClientException,
            org.apache.airavata.thriftapi.exception.AiravataSystemException,
            org.apache.airavata.thriftapi.exception.AuthorizationException,
            TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            org.apache.airavata.common.model.ApplicationModule domainModule = applicationModuleMapper.toDomain(applicationModule);
            return airavataService.registerApplicationModule(gatewayId, domainModule);
        } catch (org.apache.airavata.common.exception.InvalidRequestException e) {
            throw convertToThriftInvalidRequestException(e);
        } catch (org.apache.airavata.common.exception.AiravataClientException e) {
            throw convertToThriftAiravataClientException(e);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (org.apache.airavata.common.exception.AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error registering application module: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public ApplicationModule getApplicationModule(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken,
            String appModuleId)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
            org.apache.airavata.thriftapi.exception.AiravataClientException,
            org.apache.airavata.thriftapi.exception.AiravataSystemException,
            org.apache.airavata.thriftapi.exception.AuthorizationException,
            TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            org.apache.airavata.common.model.ApplicationModule domainModule = airavataService.getApplicationModule(appModuleId);
            return applicationModuleMapper.toThrift(domainModule);
        } catch (org.apache.airavata.common.exception.InvalidRequestException e) {
            throw convertToThriftInvalidRequestException(e);
        } catch (org.apache.airavata.common.exception.AiravataClientException e) {
            throw convertToThriftAiravataClientException(e);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (org.apache.airavata.common.exception.AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error getting application module: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public boolean updateApplicationModule(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken,
            String appModuleId,
            ApplicationModule applicationModule)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
            org.apache.airavata.thriftapi.exception.AiravataClientException,
            org.apache.airavata.thriftapi.exception.AiravataSystemException,
            org.apache.airavata.thriftapi.exception.AuthorizationException,
            TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            org.apache.airavata.common.model.ApplicationModule domainModule = applicationModuleMapper.toDomain(applicationModule);
            airavataService.updateApplicationModule(appModuleId, domainModule);
            return true;
        } catch (org.apache.airavata.common.exception.InvalidRequestException e) {
            throw convertToThriftInvalidRequestException(e);
        } catch (org.apache.airavata.common.exception.AiravataClientException e) {
            throw convertToThriftAiravataClientException(e);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (org.apache.airavata.common.exception.AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error updating application module: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public List<ApplicationModule> getAllAppModules(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken,
            String gatewayId)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
            org.apache.airavata.thriftapi.exception.AiravataClientException,
            org.apache.airavata.thriftapi.exception.AiravataSystemException,
            org.apache.airavata.thriftapi.exception.AuthorizationException,
            TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            List<org.apache.airavata.common.model.ApplicationModule> domainModules = airavataService.getAllAppModules(gatewayId);
            return domainModules.stream().map(applicationModuleMapper::toThrift).collect(Collectors.toList());
        } catch (org.apache.airavata.common.exception.InvalidRequestException e) {
            throw convertToThriftInvalidRequestException(e);
        } catch (org.apache.airavata.common.exception.AiravataClientException e) {
            throw convertToThriftAiravataClientException(e);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (org.apache.airavata.common.exception.AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error getting all app modules: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public List<ApplicationModule> getAccessibleAppModules(AuthzToken authzToken, String gatewayId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            List<ApplicationModule> domainModules =
                    airavataService.getAccessibleAppModules(domainAuthzToken, gatewayId);
            return domainModules.stream().map(applicationModuleMapper::toThrift).collect(Collectors.toList());
        } catch (InvalidRequestException e) {
            throw convertToThriftInvalidRequestException(e);
        } catch (AiravataClientException e) {
            throw convertToThriftAiravataClientException(e);
        } catch (AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error getting accessible app modules: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public boolean deleteApplicationModule(AuthzToken authzToken, String appModuleId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
        return airavataService.deleteApplicationModule(appModuleId);
        } catch (InvalidRequestException e) {
            throw convertToThriftInvalidRequestException(e);
        } catch (AiravataClientException e) {
            throw convertToThriftAiravataClientException(e);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (org.apache.airavata.common.exception.AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error deleting application module: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public String registerApplicationDeployment(
            AuthzToken authzToken, String gatewayId, ApplicationDeploymentDescription applicationDeployment)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        try {
            // Convert thrift model to domain model
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            ApplicationDeploymentDescription domainDeployment =
                    applicationDeploymentDescriptionMapper.toDomain(applicationDeployment);
            return airavataService.registerApplicationDeployment(domainAuthzToken, gatewayId, domainDeployment);
        } catch (InvalidRequestException e) {
            throw convertToThriftInvalidRequestException(e);
        } catch (AiravataClientException e) {
            throw convertToThriftAiravataClientException(e);
        } catch (AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error registering application deployment: " + e.getMessage());
            exception.initCause(e);
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public ApplicationDeploymentDescription getApplicationDeployment(AuthzToken authzToken, String appDeploymentId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        try {
        // Get domain model from service
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
        ApplicationDeploymentDescription domainDeployment =
                    airavataService.getApplicationDeployment(domainAuthzToken, appDeploymentId);
        // Convert domain model to thrift model
        return applicationDeploymentDescriptionMapper.toThrift(domainDeployment);
        } catch (InvalidRequestException e) {
            throw convertToThriftInvalidRequestException(e);
        } catch (AiravataClientException e) {
            throw convertToThriftAiravataClientException(e);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (org.apache.airavata.common.exception.AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error getting application deployment: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public boolean updateApplicationDeployment(
            AuthzToken authzToken, String appDeploymentId, ApplicationDeploymentDescription applicationDeployment)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        try {
        // Convert thrift model to domain model
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
        ApplicationDeploymentDescription domainDeployment =
                applicationDeploymentDescriptionMapper.toDomain(applicationDeployment);
            return airavataService.updateApplicationDeployment(domainAuthzToken, appDeploymentId, domainDeployment);
        } catch (InvalidRequestException e) {
            throw convertToThriftInvalidRequestException(e);
        } catch (AiravataClientException e) {
            throw convertToThriftAiravataClientException(e);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (org.apache.airavata.common.exception.AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error updating application deployment: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public boolean deleteApplicationDeployment(AuthzToken authzToken, String appDeploymentId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            return airavataService.deleteApplicationDeployment(domainAuthzToken, appDeploymentId);
        } catch (InvalidRequestException e) {
            throw convertToThriftInvalidRequestException(e);
        } catch (AiravataClientException e) {
            throw convertToThriftAiravataClientException(e);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (org.apache.airavata.common.exception.AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error deleting application deployment: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public List<ApplicationDeploymentDescription> getAllApplicationDeployments(AuthzToken authzToken, String gatewayId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return getAccessibleApplicationDeployments(authzToken, gatewayId, ResourcePermissionType.READ);
    }

    @Override
    @SecurityCheck
    public List<ApplicationDeploymentDescription> getAccessibleApplicationDeployments(
                    AuthzToken authzToken, String gatewayId, ResourcePermissionType permissionType)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        try {
        // Get domain models from service
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            ResourcePermissionType domainPermissionType = ResourcePermissionType.valueOf(permissionType.name());
        List<ApplicationDeploymentDescription> domainDeployments =
                    airavataService.getAccessibleApplicationDeployments(
                            domainAuthzToken, gatewayId, domainPermissionType);
        // Convert domain models to thrift models
        return domainDeployments.stream()
                .map(applicationDeploymentDescriptionMapper::toThrift)
                .collect(Collectors.toList());
        } catch (InvalidRequestException e) {
            throw convertToThriftInvalidRequestException(e);
        } catch (AiravataClientException e) {
            throw convertToThriftAiravataClientException(e);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (org.apache.airavata.common.exception.AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error getting accessible application deployments: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    @Deprecated
    public List<String> getAppModuleDeployedResources(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken,
            String appModuleId)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
            org.apache.airavata.thriftapi.exception.AiravataClientException,
            org.apache.airavata.thriftapi.exception.AiravataSystemException,
            org.apache.airavata.thriftapi.exception.AuthorizationException,
            TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
        return airavataService.getAppModuleDeployedResources(appModuleId);
        } catch (org.apache.airavata.common.exception.InvalidRequestException e) {
            throw convertToThriftInvalidRequestException(e);
        } catch (org.apache.airavata.common.exception.AiravataClientException e) {
            throw convertToThriftAiravataClientException(e);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (org.apache.airavata.common.exception.AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error getting app module deployed resources: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public List<ApplicationDeploymentDescription> getApplicationDeploymentsForAppModuleAndGroupResourceProfile(
                    org.apache.airavata.thriftapi.security.model.AuthzToken authzToken,
                    String appModuleId,
                    String groupResourceProfileId)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
            org.apache.airavata.thriftapi.exception.AiravataClientException,
            org.apache.airavata.thriftapi.exception.AiravataSystemException,
            org.apache.airavata.thriftapi.exception.AuthorizationException,
            TException {
        try {
        // Get domain models from service
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
        List<org.apache.airavata.common.model.ApplicationDeploymentDescription> domainDeployments =
                airavataService.getApplicationDeploymentsForAppModuleAndGroupResourceProfile(
                            domainAuthzToken, appModuleId, groupResourceProfileId);
        // Convert domain models to thrift models
        return domainDeployments.stream()
                .map(applicationDeploymentDescriptionMapper::toThrift)
                .collect(Collectors.toList());
        } catch (org.apache.airavata.common.exception.InvalidRequestException e) {
            throw convertToThriftInvalidRequestException(e);
        } catch (org.apache.airavata.common.exception.AiravataClientException e) {
            throw convertToThriftAiravataClientException(e);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (org.apache.airavata.common.exception.AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error getting application deployments for app module and group resource profile: "
                    + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public String registerApplicationInterface(
            AuthzToken authzToken, String gatewayId, ApplicationInterfaceDescription applicationInterface)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        try {
        // Convert thrift model to domain model
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
        ApplicationInterfaceDescription domainInterface =
                applicationInterfaceDescriptionMapper.toDomain(applicationInterface);
        return airavataService.registerApplicationInterface(gatewayId, domainInterface);
        } catch (InvalidRequestException e) {
            throw convertToThriftInvalidRequestException(e);
        } catch (AiravataClientException e) {
            throw convertToThriftAiravataClientException(e);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (org.apache.airavata.common.exception.AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error registering application interface: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public String cloneApplicationInterface(
            AuthzToken authzToken, String existingAppInterfaceID, String newApplicationName, String gatewayId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        try {
        return airavataService.cloneApplicationInterface(existingAppInterfaceID, newApplicationName, gatewayId);
        } catch (InvalidRequestException e) {
            throw convertToThriftInvalidRequestException(e);
        } catch (AiravataClientException e) {
            throw convertToThriftAiravataClientException(e);
        } catch (AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error cloning application interface: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public ApplicationInterfaceDescription getApplicationInterface(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken,
            String appInterfaceId)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
            org.apache.airavata.thriftapi.exception.AiravataClientException,
            org.apache.airavata.thriftapi.exception.AiravataSystemException,
            org.apache.airavata.thriftapi.exception.AuthorizationException,
            TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            org.apache.airavata.common.model.ApplicationInterfaceDescription domainInterface = airavataService.getApplicationInterface(appInterfaceId);
            return applicationInterfaceDescriptionMapper.toThrift(domainInterface);
        } catch (org.apache.airavata.common.exception.InvalidRequestException e) {
            throw convertToThriftInvalidRequestException(e);
        } catch (org.apache.airavata.common.exception.AiravataClientException e) {
            throw convertToThriftAiravataClientException(e);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (org.apache.airavata.common.exception.AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error getting application interface: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public boolean updateApplicationInterface(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken,
            String appInterfaceId,
            ApplicationInterfaceDescription applicationInterface)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
            org.apache.airavata.thriftapi.exception.AiravataClientException,
            org.apache.airavata.thriftapi.exception.AiravataSystemException,
            org.apache.airavata.thriftapi.exception.AuthorizationException,
            TException {
        try {
        // Convert thrift model to domain model
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            org.apache.airavata.common.model.ApplicationInterfaceDescription domainInterface =
                applicationInterfaceDescriptionMapper.toDomain(applicationInterface);
            airavataService.updateApplicationInterface(appInterfaceId, domainInterface);
            return true;
        } catch (org.apache.airavata.common.exception.InvalidRequestException e) {
            throw convertToThriftInvalidRequestException(e);
        } catch (org.apache.airavata.common.exception.AiravataClientException e) {
            throw convertToThriftAiravataClientException(e);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (org.apache.airavata.common.exception.AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error updating application interface: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public boolean deleteApplicationInterface(AuthzToken authzToken, String appInterfaceId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
        return airavataService.deleteApplicationInterface(appInterfaceId);
        } catch (InvalidRequestException e) {
            throw convertToThriftInvalidRequestException(e);
        } catch (AiravataClientException e) {
            throw convertToThriftAiravataClientException(e);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (org.apache.airavata.common.exception.AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error deleting application interface: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public Map<String, String> getAllApplicationInterfaceNames(AuthzToken authzToken, String gatewayId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
        return airavataService.getAllApplicationInterfaceNames(gatewayId);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error getting all application interface names: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public List<ApplicationInterfaceDescription> getAllApplicationInterfaces(AuthzToken authzToken, String gatewayId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        try {
        // Get domain models from service
            List<ApplicationInterfaceDescription> domainInterfaces =
                    airavataService.getAllApplicationInterfaces(gatewayId);
        // Convert domain models to thrift models
        return domainInterfaces.stream()
                .map(applicationInterfaceDescriptionMapper::toThrift)
                .collect(Collectors.toList());
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error getting all application interfaces: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public List<InputDataObjectType> getApplicationInputs(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken,
            String appInterfaceId)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
            org.apache.airavata.thriftapi.exception.AiravataClientException,
            org.apache.airavata.thriftapi.exception.AiravataSystemException,
            org.apache.airavata.thriftapi.exception.AuthorizationException,
            TException {
        try {
        // Get domain models from service
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
        List<org.apache.airavata.common.model.InputDataObjectType> domainInputs = airavataService.getApplicationInputs(appInterfaceId);
        // Convert domain models to thrift models
            return domainInputs.stream()
                    .map(inputDataObjectTypeMapper::toThrift)
                    .collect(Collectors.toList());
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error getting application inputs: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public List<OutputDataObjectType> getApplicationOutputs(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken,
            String appInterfaceId)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
            org.apache.airavata.thriftapi.exception.AiravataClientException,
            org.apache.airavata.thriftapi.exception.AiravataSystemException,
            org.apache.airavata.thriftapi.exception.AuthorizationException,
            TException {
        try {
        // Get domain models from service
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
        List<org.apache.airavata.common.model.OutputDataObjectType> domainOutputs = airavataService.getApplicationOutputs(appInterfaceId);
        // Convert domain models to thrift models
            return domainOutputs.stream()
                    .map(outputDataObjectTypeMapper::toThrift)
                    .collect(Collectors.toList());
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error getting application outputs: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    @Deprecated
    public Map<String, String> getAvailableAppInterfaceComputeResources(AuthzToken authzToken, String appInterfaceId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
        return airavataService.getAvailableAppInterfaceComputeResources(appInterfaceId);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error getting available app interface compute resources: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public String registerComputeResource(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken,
            ComputeResourceDescription computeResourceDescription)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
            org.apache.airavata.thriftapi.exception.AiravataClientException,
            org.apache.airavata.thriftapi.exception.AiravataSystemException,
            org.apache.airavata.thriftapi.exception.AuthorizationException,
            TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
        // Convert thrift model to domain model
        org.apache.airavata.common.model.ComputeResourceDescription domainComputeResource =
                computeResourceDescriptionMapper.toDomain(computeResourceDescription);
        return airavataService.registerComputeResource(domainComputeResource);
        } catch (org.apache.airavata.common.exception.InvalidRequestException e) {
            throw convertToThriftInvalidRequestException(e);
        } catch (org.apache.airavata.common.exception.AiravataClientException e) {
            throw convertToThriftAiravataClientException(e);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (org.apache.airavata.common.exception.AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error registering compute resource: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public ComputeResourceDescription getComputeResource(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken,
            String computeResourceId)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
            org.apache.airavata.thriftapi.exception.AiravataClientException,
            org.apache.airavata.thriftapi.exception.AiravataSystemException,
            org.apache.airavata.thriftapi.exception.AuthorizationException,
            TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
        // Get domain model from service
        org.apache.airavata.common.model.ComputeResourceDescription domainComputeResource = airavataService.getComputeResource(computeResourceId);
        // Convert domain model to thrift model
        return computeResourceDescriptionMapper.toThrift(domainComputeResource);
        } catch (InvalidRequestException e) {
            throw convertToThriftInvalidRequestException(e);
        } catch (AiravataClientException e) {
            throw convertToThriftAiravataClientException(e);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (org.apache.airavata.common.exception.AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error getting compute resource: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public Map<String, String> getAllComputeResourceNames(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
            org.apache.airavata.thriftapi.exception.AiravataClientException,
            org.apache.airavata.thriftapi.exception.AiravataSystemException,
            org.apache.airavata.thriftapi.exception.AuthorizationException,
            TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
        return airavataService.getAllComputeResourceNames();
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error getting all compute resource names: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public boolean updateComputeResource(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken,
            String computeResourceId,
            ComputeResourceDescription computeResourceDescription)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
            org.apache.airavata.thriftapi.exception.AiravataClientException,
            org.apache.airavata.thriftapi.exception.AiravataSystemException,
            org.apache.airavata.thriftapi.exception.AuthorizationException,
            TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            org.apache.airavata.common.model.ComputeResourceDescription domainComputeResource =
                    computeResourceDescriptionMapper.toDomain(computeResourceDescription);
            airavataService.updateComputeResource(computeResourceId, domainComputeResource);
            return true;
        } catch (org.apache.airavata.common.exception.InvalidRequestException e) {
            throw convertToThriftInvalidRequestException(e);
        } catch (org.apache.airavata.common.exception.AiravataClientException e) {
            throw convertToThriftAiravataClientException(e);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (org.apache.airavata.common.exception.AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error updating compute resource: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public boolean deleteComputeResource(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken,
            String computeResourceId)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
            org.apache.airavata.thriftapi.exception.AiravataClientException,
            org.apache.airavata.thriftapi.exception.AiravataSystemException,
            org.apache.airavata.thriftapi.exception.AuthorizationException,
            TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
        return airavataService.deleteComputeResource(computeResourceId);
        } catch (org.apache.airavata.common.exception.InvalidRequestException e) {
            throw convertToThriftInvalidRequestException(e);
        } catch (org.apache.airavata.common.exception.AiravataClientException e) {
            throw convertToThriftAiravataClientException(e);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (org.apache.airavata.common.exception.AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error deleting compute resource: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public String registerStorageResource(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken,
            org.apache.airavata.thriftapi.model.StorageResourceDescription storageResourceDescription)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
            org.apache.airavata.thriftapi.exception.AiravataClientException,
            org.apache.airavata.thriftapi.exception.AiravataSystemException,
            org.apache.airavata.thriftapi.exception.AuthorizationException,
            TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            org.apache.airavata.common.model.StorageResourceDescription domainStorage =
                    storageResourceDescriptionMapper.toDomain(storageResourceDescription);
            return airavataService.registerStorageResource(domainStorage);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException thriftException = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            thriftException.setMessage(e.getMessage());
            if (e.getAiravataErrorType() != null) {
                thriftException.setAiravataErrorType(
                        org.apache.airavata.thriftapi.exception.AiravataErrorType.valueOf(e.getAiravataErrorType().name()));
            }
            thriftException.initCause(e);
            throw thriftException;
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error registering storage resource: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public StorageResourceDescription getStorageResource(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken,
            String storageResourceId)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
            org.apache.airavata.thriftapi.exception.AiravataClientException,
            org.apache.airavata.thriftapi.exception.AiravataSystemException,
            org.apache.airavata.thriftapi.exception.AuthorizationException,
            TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            org.apache.airavata.common.model.StorageResourceDescription domainStorage = airavataService.getStorageResource(storageResourceId);
            return storageResourceDescriptionMapper.toThrift(domainStorage);
        } catch (org.apache.airavata.common.exception.InvalidRequestException e) {
            throw convertToThriftInvalidRequestException(e);
        } catch (org.apache.airavata.common.exception.AiravataClientException e) {
            throw convertToThriftAiravataClientException(e);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (org.apache.airavata.common.exception.AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error getting storage resource: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public Map<String, String> getAllStorageResourceNames(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
            org.apache.airavata.thriftapi.exception.AiravataClientException,
            org.apache.airavata.thriftapi.exception.AiravataSystemException,
            org.apache.airavata.thriftapi.exception.AuthorizationException,
            TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
        return airavataService.getAllStorageResourceNames();
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error getting all storage resource names: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public boolean updateStorageResource(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken,
            String storageResourceId,
            org.apache.airavata.thriftapi.model.StorageResourceDescription storageResourceDescription)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
            org.apache.airavata.thriftapi.exception.AiravataClientException,
            org.apache.airavata.thriftapi.exception.AiravataSystemException,
            org.apache.airavata.thriftapi.exception.AuthorizationException,
            TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            org.apache.airavata.common.model.StorageResourceDescription domainStorage =
                    storageResourceDescriptionMapper.toDomain(storageResourceDescription);
            return airavataService.updateStorageResource(storageResourceId, domainStorage);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException thriftException = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            thriftException.setMessage(e.getMessage());
            if (e.getAiravataErrorType() != null) {
                thriftException.setAiravataErrorType(
                        org.apache.airavata.thriftapi.exception.AiravataErrorType.valueOf(e.getAiravataErrorType().name()));
            }
            thriftException.initCause(e);
            throw thriftException;
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error updating storage resource: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public boolean deleteStorageResource(AuthzToken authzToken, String storageResourceId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
        return airavataService.deleteStorageResource(storageResourceId);
        } catch (InvalidRequestException e) {
            throw convertToThriftInvalidRequestException(e);
        } catch (AiravataClientException e) {
            throw convertToThriftAiravataClientException(e);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (org.apache.airavata.common.exception.AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error deleting storage resource: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public StorageVolumeInfo getResourceStorageInfo(AuthzToken authzToken, String resourceId, String location)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        try {
        org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            StorageVolumeInfo domainInfo =
                    airavataService.getResourceStorageInfo(domainAuthzToken, resourceId, location);
        return storageVolumeInfoMapper.toThrift(domainInfo);
        } catch (org.apache.airavata.common.exception.InvalidRequestException e) {
            throw convertToThriftInvalidRequestException(e);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error getting resource storage info: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public StorageDirectoryInfo getStorageDirectoryInfo(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken,
            String resourceId,
            String location)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
            org.apache.airavata.thriftapi.exception.AiravataClientException,
            org.apache.airavata.thriftapi.exception.AiravataSystemException,
            org.apache.airavata.thriftapi.exception.AuthorizationException,
            TException {
        try {
        org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            StorageDirectoryInfo domainInfo =
                    airavataService.getStorageDirectoryInfo(domainAuthzToken, resourceId, location);
        return storageDirectoryInfoMapper.toThrift(domainInfo);
        } catch (org.apache.airavata.common.exception.InvalidRequestException e) {
            throw convertToThriftInvalidRequestException(e);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error getting storage directory info: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public String addLocalSubmissionDetails(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken,
            String computeResourceId,
            int priorityOrder,
            LOCALSubmission localSubmission)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
            org.apache.airavata.thriftapi.exception.AiravataClientException,
            org.apache.airavata.thriftapi.exception.AiravataSystemException,
            org.apache.airavata.thriftapi.exception.AuthorizationException,
            TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            org.apache.airavata.common.model.LOCALSubmission domainLocal = localSubmissionMapper.toDomain(localSubmission);
            return airavataService.addLocalSubmissionDetails(computeResourceId, priorityOrder, domainLocal);
        } catch (org.apache.airavata.common.exception.InvalidRequestException e) {
            throw convertToThriftInvalidRequestException(e);
        } catch (org.apache.airavata.common.exception.AiravataClientException e) {
            throw convertToThriftAiravataClientException(e);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (org.apache.airavata.common.exception.AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error adding local submission details: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public boolean updateLocalSubmissionDetails(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken,
            String jobSubmissionInterfaceId,
            LOCALSubmission localSubmission)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
            org.apache.airavata.thriftapi.exception.AiravataClientException,
            org.apache.airavata.thriftapi.exception.AiravataSystemException,
            org.apache.airavata.thriftapi.exception.AuthorizationException,
            TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            org.apache.airavata.common.model.LOCALSubmission domainLocal = localSubmissionMapper.toDomain(localSubmission);
            airavataService.updateLocalSubmissionDetails(jobSubmissionInterfaceId, domainLocal);
            return true;
        } catch (org.apache.airavata.common.exception.InvalidRequestException e) {
            throw convertToThriftInvalidRequestException(e);
        } catch (org.apache.airavata.common.exception.AiravataClientException e) {
            throw convertToThriftAiravataClientException(e);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (org.apache.airavata.common.exception.AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error updating local submission details: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public LOCALSubmission getLocalJobSubmission(AuthzToken authzToken, String jobSubmissionId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        try {
            LOCALSubmission domainLocal = airavataService.getLocalJobSubmission(jobSubmissionId);
            return localSubmissionMapper.toThrift(domainLocal);
        } catch (InvalidRequestException e) {
            throw convertToThriftInvalidRequestException(e);
        } catch (AiravataClientException e) {
            throw convertToThriftAiravataClientException(e);
        } catch (AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error getting local job submission: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public String addSSHJobSubmissionDetails(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken,
            String computeResourceId,
            int priorityOrder,
            org.apache.airavata.thriftapi.model.SSHJobSubmission sshJobSubmission)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
            org.apache.airavata.thriftapi.exception.AiravataClientException,
            org.apache.airavata.thriftapi.exception.AiravataSystemException,
            org.apache.airavata.thriftapi.exception.AuthorizationException,
            TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            org.apache.airavata.common.model.SSHJobSubmission domainSSH = sshJobSubmissionMapper.toDomain(sshJobSubmission);
            return airavataService.addSSHJobSubmissionDetails(computeResourceId, priorityOrder, domainSSH);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException thriftException = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            thriftException.setMessage(e.getMessage());
            if (e.getAiravataErrorType() != null) {
                thriftException.setAiravataErrorType(
                        org.apache.airavata.thriftapi.exception.AiravataErrorType.valueOf(e.getAiravataErrorType().name()));
            }
            thriftException.initCause(e);
            throw thriftException;
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error adding SSH job submission details: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public String addSSHForkJobSubmissionDetails(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken,
            String computeResourceId,
            int priorityOrder,
            org.apache.airavata.thriftapi.model.SSHJobSubmission sshJobSubmission)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
            org.apache.airavata.thriftapi.exception.AiravataClientException,
            org.apache.airavata.thriftapi.exception.AiravataSystemException,
            org.apache.airavata.thriftapi.exception.AuthorizationException,
            TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            org.apache.airavata.common.model.SSHJobSubmission domainSSH = sshJobSubmissionMapper.toDomain(sshJobSubmission);
            return airavataService.addSSHForkJobSubmissionDetails(computeResourceId, priorityOrder, domainSSH);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException thriftException = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            thriftException.setMessage(e.getMessage());
            if (e.getAiravataErrorType() != null) {
                thriftException.setAiravataErrorType(
                        org.apache.airavata.thriftapi.exception.AiravataErrorType.valueOf(e.getAiravataErrorType().name()));
            }
            thriftException.initCause(e);
            throw thriftException;
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error adding SSH fork job submission details: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public SSHJobSubmission getSSHJobSubmission(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken,
            String jobSubmissionId)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
            org.apache.airavata.thriftapi.exception.AiravataClientException,
            org.apache.airavata.thriftapi.exception.AiravataSystemException,
            org.apache.airavata.thriftapi.exception.AuthorizationException,
            TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            org.apache.airavata.common.model.SSHJobSubmission domainSSH = airavataService.getSSHJobSubmission(jobSubmissionId);
            return sshJobSubmissionMapper.toThrift(domainSSH);
        } catch (org.apache.airavata.common.exception.InvalidRequestException e) {
            throw convertToThriftInvalidRequestException(e);
        } catch (org.apache.airavata.common.exception.AiravataClientException e) {
            throw convertToThriftAiravataClientException(e);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (org.apache.airavata.common.exception.AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error getting SSH job submission: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public String addCloudJobSubmissionDetails(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken,
            String computeResourceId,
            int priorityOrder,
            org.apache.airavata.thriftapi.model.CloudJobSubmission cloudJobSubmission)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
            org.apache.airavata.thriftapi.exception.AiravataClientException,
            org.apache.airavata.thriftapi.exception.AiravataSystemException,
            org.apache.airavata.thriftapi.exception.AuthorizationException,
            TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            org.apache.airavata.common.model.CloudJobSubmission domainCloud = cloudJobSubmissionMapper.toDomain(cloudJobSubmission);
            return airavataService.addCloudJobSubmissionDetails(computeResourceId, priorityOrder, domainCloud);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException thriftException = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            thriftException.setMessage(e.getMessage());
            if (e.getAiravataErrorType() != null) {
                thriftException.setAiravataErrorType(
                        org.apache.airavata.thriftapi.exception.AiravataErrorType.valueOf(e.getAiravataErrorType().name()));
            }
            thriftException.initCause(e);
            throw thriftException;
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error adding cloud job submission details: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public CloudJobSubmission getCloudJobSubmission(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken,
            String jobSubmissionId)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
            org.apache.airavata.thriftapi.exception.AiravataClientException,
            org.apache.airavata.thriftapi.exception.AiravataSystemException,
            org.apache.airavata.thriftapi.exception.AuthorizationException,
            TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            org.apache.airavata.common.model.CloudJobSubmission domainCloud = airavataService.getCloudJobSubmission(jobSubmissionId);
            return cloudJobSubmissionMapper.toThrift(domainCloud);
        } catch (org.apache.airavata.common.exception.InvalidRequestException e) {
            throw convertToThriftInvalidRequestException(e);
        } catch (org.apache.airavata.common.exception.AiravataClientException e) {
            throw convertToThriftAiravataClientException(e);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (org.apache.airavata.common.exception.AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error getting cloud job submission: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public String addUNICOREJobSubmissionDetails(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken,
            String computeResourceId,
            int priorityOrder,
            UnicoreJobSubmission unicoreJobSubmission)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
            org.apache.airavata.thriftapi.exception.AiravataClientException,
            org.apache.airavata.thriftapi.exception.AiravataSystemException,
            org.apache.airavata.thriftapi.exception.AuthorizationException,
            TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            org.apache.airavata.common.model.UnicoreJobSubmission domainUnicore = unicoreJobSubmissionMapper.toDomain(unicoreJobSubmission);
            return airavataService.addUNICOREJobSubmissionDetails(computeResourceId, priorityOrder, domainUnicore);
        } catch (org.apache.airavata.common.exception.InvalidRequestException e) {
            throw convertToThriftInvalidRequestException(e);
        } catch (org.apache.airavata.common.exception.AiravataClientException e) {
            throw convertToThriftAiravataClientException(e);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (org.apache.airavata.common.exception.AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error adding UNICORE job submission details: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public UnicoreJobSubmission getUnicoreJobSubmission(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken,
            String jobSubmissionId)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
            org.apache.airavata.thriftapi.exception.AiravataClientException,
            org.apache.airavata.thriftapi.exception.AiravataSystemException,
            org.apache.airavata.thriftapi.exception.AuthorizationException,
            TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            org.apache.airavata.common.model.UnicoreJobSubmission domainUnicore = airavataService.getUnicoreJobSubmission(jobSubmissionId);
            return unicoreJobSubmissionMapper.toThrift(domainUnicore);
        } catch (org.apache.airavata.common.exception.InvalidRequestException e) {
            throw convertToThriftInvalidRequestException(e);
        } catch (org.apache.airavata.common.exception.AiravataClientException e) {
            throw convertToThriftAiravataClientException(e);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (org.apache.airavata.common.exception.AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error getting Unicore job submission: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public boolean updateSSHJobSubmissionDetails(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken,
            String jobSubmissionInterfaceId,
            org.apache.airavata.thriftapi.model.SSHJobSubmission sshJobSubmission)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
            org.apache.airavata.thriftapi.exception.AiravataClientException,
            org.apache.airavata.thriftapi.exception.AiravataSystemException,
            org.apache.airavata.thriftapi.exception.AuthorizationException,
            TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            org.apache.airavata.common.model.SSHJobSubmission domainSSH = sshJobSubmissionMapper.toDomain(sshJobSubmission);
            return airavataService.updateSSHJobSubmissionDetails(jobSubmissionInterfaceId, domainSSH);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException thriftException = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            thriftException.setMessage(e.getMessage());
            if (e.getAiravataErrorType() != null) {
                thriftException.setAiravataErrorType(
                        org.apache.airavata.thriftapi.exception.AiravataErrorType.valueOf(e.getAiravataErrorType().name()));
            }
            thriftException.initCause(e);
            throw thriftException;
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error updating SSH job submission details: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public boolean updateCloudJobSubmissionDetails(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken,
            String jobSubmissionInterfaceId,
            org.apache.airavata.thriftapi.model.CloudJobSubmission cloudJobSubmission)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
            org.apache.airavata.thriftapi.exception.AiravataClientException,
            org.apache.airavata.thriftapi.exception.AiravataSystemException,
            org.apache.airavata.thriftapi.exception.AuthorizationException,
            TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            org.apache.airavata.common.model.CloudJobSubmission domainCloud = cloudJobSubmissionMapper.toDomain(cloudJobSubmission);
            return airavataService.updateCloudJobSubmissionDetails(jobSubmissionInterfaceId, domainCloud);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException thriftException = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            thriftException.setMessage(e.getMessage());
            if (e.getAiravataErrorType() != null) {
                thriftException.setAiravataErrorType(
                        org.apache.airavata.thriftapi.exception.AiravataErrorType.valueOf(e.getAiravataErrorType().name()));
            }
            thriftException.initCause(e);
            throw thriftException;
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error updating cloud job submission details: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public boolean updateUnicoreJobSubmissionDetails(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken,
            String jobSubmissionInterfaceId,
            org.apache.airavata.thriftapi.model.UnicoreJobSubmission unicoreJobSubmission)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
            org.apache.airavata.thriftapi.exception.AiravataClientException,
            org.apache.airavata.thriftapi.exception.AiravataSystemException,
            org.apache.airavata.thriftapi.exception.AuthorizationException,
            TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            org.apache.airavata.common.model.UnicoreJobSubmission domainUnicore = unicoreJobSubmissionMapper.toDomain(unicoreJobSubmission);
            return airavataService.updateUnicoreJobSubmissionDetails(jobSubmissionInterfaceId, domainUnicore);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException thriftException = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            thriftException.setMessage(e.getMessage());
            if (e.getAiravataErrorType() != null) {
                thriftException.setAiravataErrorType(
                        org.apache.airavata.thriftapi.exception.AiravataErrorType.valueOf(e.getAiravataErrorType().name()));
            }
            thriftException.initCause(e);
            throw thriftException;
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error updating unicore job submission details: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public boolean updateLocalDataMovementDetails(
            AuthzToken authzToken, String dataMovementInterfaceId, LOCALDataMovement localDataMovement)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        try {
            LOCALDataMovement domainLocalDataMovement = localDataMovementMapper.toDomain(localDataMovement);
            airavataService.updateLocalDataMovementDetails(dataMovementInterfaceId, domainLocalDataMovement);
            return true;
        } catch (InvalidRequestException e) {
            throw convertToThriftInvalidRequestException(e);
        } catch (AiravataClientException e) {
            throw convertToThriftAiravataClientException(e);
        } catch (AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error updating local data movement details: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public boolean updateSCPDataMovementDetails(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken,
            String dataMovementInterfaceId,
            org.apache.airavata.thriftapi.model.SCPDataMovement scpDataMovement)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
            org.apache.airavata.thriftapi.exception.AiravataClientException,
            org.apache.airavata.thriftapi.exception.AiravataSystemException,
            org.apache.airavata.thriftapi.exception.AuthorizationException,
            TException {
        try {
            org.apache.airavata.common.model.SCPDataMovement domainSCP = scpDataMovementMapper.toDomain(scpDataMovement);
            return airavataService.updateSCPDataMovementDetails(dataMovementInterfaceId, domainSCP);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException thriftException = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            thriftException.setMessage(e.getMessage());
            if (e.getAiravataErrorType() != null) {
                thriftException.setAiravataErrorType(
                        org.apache.airavata.thriftapi.exception.AiravataErrorType.valueOf(e.getAiravataErrorType().name()));
            }
            thriftException.initCause(e);
            throw thriftException;
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error updating SCP data movement details: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public SCPDataMovement getSCPDataMovement(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken,
            String dataMovementId)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
            org.apache.airavata.thriftapi.exception.AiravataClientException,
            org.apache.airavata.thriftapi.exception.AiravataSystemException,
            org.apache.airavata.thriftapi.exception.AuthorizationException,
            TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            org.apache.airavata.common.model.SCPDataMovement domainSCP = airavataService.getSCPDataMovement(dataMovementId);
            return scpDataMovementMapper.toThrift(domainSCP);
        } catch (org.apache.airavata.common.exception.AiravataClientException e) {
            throw convertToThriftAiravataClientException(e);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (org.apache.airavata.common.exception.AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error getting SCP data movement: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public String addUnicoreDataMovementDetails(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken,
            String resourceId,
            DMType dmType,
            int priorityOrder,
            UnicoreDataMovement unicoreDataMovement)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
            org.apache.airavata.thriftapi.exception.AiravataClientException,
            org.apache.airavata.thriftapi.exception.AiravataSystemException,
            org.apache.airavata.thriftapi.exception.AuthorizationException,
            TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            org.apache.airavata.common.model.DMType domainDMType = org.apache.airavata.common.model.DMType.valueOf(dmType.name());
            org.apache.airavata.common.model.UnicoreDataMovement domainUnicore = unicoreDataMovementMapper.toDomain(unicoreDataMovement);
            return airavataService.addUnicoreDataMovementDetails(
                    resourceId, domainDMType, priorityOrder, domainUnicore);
        } catch (org.apache.airavata.common.exception.InvalidRequestException e) {
            throw convertToThriftInvalidRequestException(e);
        } catch (org.apache.airavata.common.exception.AiravataClientException e) {
            throw convertToThriftAiravataClientException(e);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (org.apache.airavata.common.exception.AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error adding Unicore data movement details: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public boolean updateUnicoreDataMovementDetails(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken,
            String dataMovementInterfaceId,
            UnicoreDataMovement unicoreDataMovement)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
            org.apache.airavata.thriftapi.exception.AiravataClientException,
            org.apache.airavata.thriftapi.exception.AiravataSystemException,
            org.apache.airavata.thriftapi.exception.AuthorizationException,
            TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            org.apache.airavata.common.model.UnicoreDataMovement domainUnicore = unicoreDataMovementMapper.toDomain(unicoreDataMovement);
            airavataService.updateUnicoreDataMovementDetails(dataMovementInterfaceId, domainUnicore);
            return true;
        } catch (org.apache.airavata.common.exception.InvalidRequestException e) {
            throw convertToThriftInvalidRequestException(e);
        } catch (org.apache.airavata.common.exception.AiravataClientException e) {
            throw convertToThriftAiravataClientException(e);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (org.apache.airavata.common.exception.AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error updating Unicore data movement details: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public LOCALDataMovement getLocalDataMovement(AuthzToken authzToken, String dataMovementId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        try {
            LOCALDataMovement domainLocal = airavataService.getLocalDataMovement(dataMovementId);
            return localDataMovementMapper.toThrift(domainLocal);
        } catch (InvalidRequestException e) {
            throw convertToThriftInvalidRequestException(e);
        } catch (AiravataClientException e) {
            throw convertToThriftAiravataClientException(e);
        } catch (AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error getting local data movement: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public UnicoreDataMovement getUnicoreDataMovement(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken,
            String dataMovementId)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
            org.apache.airavata.thriftapi.exception.AiravataClientException,
            org.apache.airavata.thriftapi.exception.AiravataSystemException,
            org.apache.airavata.thriftapi.exception.AuthorizationException,
            TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            org.apache.airavata.common.model.UnicoreDataMovement domainUnicore = airavataService.getUnicoreDataMovement(dataMovementId);
            return unicoreDataMovementMapper.toThrift(domainUnicore);
        } catch (org.apache.airavata.common.exception.InvalidRequestException e) {
            throw convertToThriftInvalidRequestException(e);
        } catch (org.apache.airavata.common.exception.AiravataClientException e) {
            throw convertToThriftAiravataClientException(e);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (org.apache.airavata.common.exception.AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error getting Unicore data movement: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public String addGridFTPDataMovementDetails(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken,
            String productUri,
            DMType dataMoveType,
            int priorityOrder,
            GridFTPDataMovement gridFTPDataMovement)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
            org.apache.airavata.thriftapi.exception.AiravataClientException,
            org.apache.airavata.thriftapi.exception.AiravataSystemException,
            org.apache.airavata.thriftapi.exception.AuthorizationException,
            TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            org.apache.airavata.common.model.DMType domainDMType = org.apache.airavata.common.model.DMType.valueOf(dataMoveType.name());
            org.apache.airavata.common.model.GridFTPDataMovement domainGridFTP = gridFTPDataMovementMapper.toDomain(gridFTPDataMovement);
            return airavataService.addGridFTPDataMovementDetails(
                    productUri, domainDMType, priorityOrder, domainGridFTP);
        } catch (org.apache.airavata.common.exception.InvalidRequestException e) {
            throw convertToThriftInvalidRequestException(e);
        } catch (org.apache.airavata.common.exception.AiravataClientException e) {
            throw convertToThriftAiravataClientException(e);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (org.apache.airavata.common.exception.AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error adding GridFTP data movement details: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public boolean updateGridFTPDataMovementDetails(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken,
            String dataMovementInterfaceId,
            GridFTPDataMovement gridFTPDataMovement)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
            org.apache.airavata.thriftapi.exception.AiravataClientException,
            org.apache.airavata.thriftapi.exception.AiravataSystemException,
            org.apache.airavata.thriftapi.exception.AuthorizationException,
            TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            org.apache.airavata.common.model.GridFTPDataMovement domainGridFTP = gridFTPDataMovementMapper.toDomain(gridFTPDataMovement);
            airavataService.updateGridFTPDataMovementDetails(dataMovementInterfaceId, domainGridFTP);
            return true;
        } catch (org.apache.airavata.common.exception.InvalidRequestException e) {
            throw convertToThriftInvalidRequestException(e);
        } catch (org.apache.airavata.common.exception.AiravataClientException e) {
            throw convertToThriftAiravataClientException(e);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (org.apache.airavata.common.exception.AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error updating GridFTP data movement details: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public GridFTPDataMovement getGridFTPDataMovement(AuthzToken authzToken, String dataMovementId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        try {
            GridFTPDataMovement domainGridFTP = airavataService.getGridFTPDataMovement(dataMovementId);
            return gridFTPDataMovementMapper.toThrift(domainGridFTP);
        } catch (InvalidRequestException e) {
            throw convertToThriftInvalidRequestException(e);
        } catch (AiravataClientException e) {
            throw convertToThriftAiravataClientException(e);
        } catch (AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error getting GridFTP data movement: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public boolean changeJobSubmissionPriority(
            AuthzToken authzToken, String jobSubmissionInterfaceId, int newPriorityOrder)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return false;
    }

    @Override
    @SecurityCheck
    public boolean changeDataMovementPriority(
            AuthzToken authzToken, String dataMovementInterfaceId, int newPriorityOrder)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return false;
    }

    @Override
    @SecurityCheck
    public boolean changeJobSubmissionPriorities(AuthzToken authzToken, Map<String, Integer> jobSubmissionPriorityMap)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return false;
    }

    @Override
    @SecurityCheck
    public boolean changeDataMovementPriorities(AuthzToken authzToken, Map<String, Integer> dataMovementPriorityMap)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        return false;
    }

    @Override
    @SecurityCheck
    public String registerResourceJobManager(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken,
            ResourceJobManager resourceJobManager)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
            org.apache.airavata.thriftapi.exception.AiravataClientException,
            org.apache.airavata.thriftapi.exception.AiravataSystemException,
            org.apache.airavata.thriftapi.exception.AuthorizationException,
            TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            org.apache.airavata.common.model.ResourceJobManager domainRJM = resourceJobManagerMapper.toDomain(resourceJobManager);
            return airavataService.registerResourceJobManager(domainRJM);
        } catch (org.apache.airavata.common.exception.InvalidRequestException e) {
            throw convertToThriftInvalidRequestException(e);
        } catch (org.apache.airavata.common.exception.AiravataClientException e) {
            throw convertToThriftAiravataClientException(e);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (org.apache.airavata.common.exception.AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error registering resource job manager: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public boolean updateResourceJobManager(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken,
            String resourceJobManagerId,
            org.apache.airavata.thriftapi.model.ResourceJobManager updatedResourceJobManager)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
            org.apache.airavata.thriftapi.exception.AiravataClientException,
            org.apache.airavata.thriftapi.exception.AiravataSystemException,
            org.apache.airavata.thriftapi.exception.AuthorizationException,
            TException {
        try {
            org.apache.airavata.common.model.ResourceJobManager domainRJM = resourceJobManagerMapper.toDomain(updatedResourceJobManager);
            return airavataService.updateResourceJobManager(resourceJobManagerId, domainRJM);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException thriftException = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            thriftException.setMessage(e.getMessage());
            if (e.getAiravataErrorType() != null) {
                thriftException.setAiravataErrorType(
                        org.apache.airavata.thriftapi.exception.AiravataErrorType.valueOf(e.getAiravataErrorType().name()));
            }
            thriftException.initCause(e);
            throw thriftException;
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error updating resource job manager: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public ResourceJobManager getResourceJobManager(AuthzToken authzToken, String resourceJobManagerId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        try {
            ResourceJobManager domainRJM = airavataService.getResourceJobManager(resourceJobManagerId);
            return resourceJobManagerMapper.toThrift(domainRJM);
        } catch (InvalidRequestException e) {
            throw convertToThriftInvalidRequestException(e);
        } catch (AiravataClientException e) {
            throw convertToThriftAiravataClientException(e);
        } catch (AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error getting resource job manager: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public boolean deleteResourceJobManager(AuthzToken authzToken, String resourceJobManagerId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
        return airavataService.deleteResourceJobManager(resourceJobManagerId);
        } catch (InvalidRequestException e) {
            throw convertToThriftInvalidRequestException(e);
        } catch (AiravataClientException e) {
            throw convertToThriftAiravataClientException(e);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (org.apache.airavata.common.exception.AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error deleting resource job manager: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public boolean deleteBatchQueue(AuthzToken authzToken, String computeResourceId, String queueName)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
        return airavataService.deleteBatchQueue(computeResourceId, queueName);
        } catch (InvalidRequestException e) {
            throw convertToThriftInvalidRequestException(e);
        } catch (AiravataClientException e) {
            throw convertToThriftAiravataClientException(e);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (org.apache.airavata.common.exception.AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error deleting batch queue: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public String registerGatewayResourceProfile(AuthzToken authzToken, GatewayResourceProfile gatewayResourceProfile)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        try {
        // Convert thrift model to domain model
            GatewayResourceProfile domainProfile = gatewayResourceProfileMapper.toDomain(gatewayResourceProfile);
        return airavataService.registerGatewayResourceProfile(domainProfile);
        } catch (InvalidRequestException e) {
            throw convertToThriftInvalidRequestException(e);
        } catch (AiravataClientException e) {
            throw convertToThriftAiravataClientException(e);
        } catch (AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error registering gateway resource profile: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public GatewayResourceProfile getGatewayResourceProfile(AuthzToken authzToken, String gatewayID)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        try {
        // Get domain model from service
            GatewayResourceProfile domainProfile = airavataService.getGatewayResourceProfile(gatewayID);
        // Convert domain model to thrift model
        return gatewayResourceProfileMapper.toThrift(domainProfile);
        } catch (InvalidRequestException e) {
            throw convertToThriftInvalidRequestException(e);
        } catch (AiravataClientException e) {
            throw convertToThriftAiravataClientException(e);
        } catch (AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error getting gateway resource profile: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public boolean updateGatewayResourceProfile(
            AuthzToken authzToken, String gatewayID, GatewayResourceProfile gatewayResourceProfile)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        try {
        // Convert thrift model to domain model
            GatewayResourceProfile domainProfile = gatewayResourceProfileMapper.toDomain(gatewayResourceProfile);
            airavataService.updateGatewayResourceProfile(gatewayID, domainProfile);
            return true;
        } catch (InvalidRequestException e) {
            throw convertToThriftInvalidRequestException(e);
        } catch (AiravataClientException e) {
            throw convertToThriftAiravataClientException(e);
        } catch (AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error updating gateway resource profile: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public boolean deleteGatewayResourceProfile(AuthzToken authzToken, String gatewayID)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
        return airavataService.deleteGatewayResourceProfile(gatewayID);
        } catch (InvalidRequestException e) {
            throw convertToThriftInvalidRequestException(e);
        } catch (AiravataClientException e) {
            throw convertToThriftAiravataClientException(e);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (org.apache.airavata.common.exception.AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error deleting gateway resource profile: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public boolean addGatewayComputeResourcePreference(
            AuthzToken authzToken,
            String gatewayID,
            String computeResourceId,
            ComputeResourcePreference computeResourcePreference)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        try {
            ComputeResourcePreference domainPreference =
                    computeResourcePreferenceMapper.toDomain(computeResourcePreference);
            airavataService.addGatewayComputeResourcePreference(gatewayID, computeResourceId, domainPreference);
            return true;
        } catch (InvalidRequestException e) {
            throw convertToThriftInvalidRequestException(e);
        } catch (AiravataClientException e) {
            throw convertToThriftAiravataClientException(e);
        } catch (AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error adding gateway compute resource preference: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public boolean addGatewayStoragePreference(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken,
            String gatewayID,
            String storageResourceId,
            org.apache.airavata.thriftapi.model.StoragePreference dataStoragePreference)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
            org.apache.airavata.thriftapi.exception.AiravataClientException,
            org.apache.airavata.thriftapi.exception.AiravataSystemException,
            org.apache.airavata.thriftapi.exception.AuthorizationException,
            TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            org.apache.airavata.common.model.StoragePreference domainPreference = storagePreferenceMapper.toDomain(dataStoragePreference);
            return airavataService.addGatewayStoragePreference(gatewayID, storageResourceId, domainPreference);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException thriftException = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            thriftException.setMessage(e.getMessage());
            if (e.getAiravataErrorType() != null) {
                thriftException.setAiravataErrorType(
                        org.apache.airavata.thriftapi.exception.AiravataErrorType.valueOf(e.getAiravataErrorType().name()));
            }
            thriftException.initCause(e);
            throw thriftException;
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error adding gateway storage preference: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public ComputeResourcePreference getGatewayComputeResourcePreference(
            AuthzToken authzToken, String gatewayID, String computeResourceId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            ComputeResourcePreference domainPreference =
                    airavataService.getGatewayComputeResourcePreference(gatewayID, computeResourceId);
            return computeResourcePreferenceMapper.toThrift(domainPreference);
        } catch (InvalidRequestException e) {
            throw convertToThriftInvalidRequestException(e);
        } catch (AiravataClientException e) {
            throw convertToThriftAiravataClientException(e);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (org.apache.airavata.common.exception.AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error getting gateway compute resource preference: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public StoragePreference getGatewayStoragePreference(AuthzToken authzToken, String gatewayID, String storageId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        try {
            StoragePreference domainPreference = airavataService.getGatewayStoragePreference(gatewayID, storageId);
            return storagePreferenceMapper.toThrift(domainPreference);
        } catch (InvalidRequestException e) {
            throw convertToThriftInvalidRequestException(e);
        } catch (AiravataClientException e) {
            throw convertToThriftAiravataClientException(e);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (org.apache.airavata.common.exception.AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error getting gateway storage preference: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public List<StoragePreference> getAllGatewayStoragePreferences(AuthzToken authzToken, String gatewayID)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        try {
            List<StoragePreference> domainPreferences = airavataService.getAllGatewayStoragePreferences(gatewayID);
            return domainPreferences.stream()
                    .map(storagePreferenceMapper::toThrift)
                    .collect(Collectors.toList());
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error getting all gateway storage preferences: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public List<GatewayResourceProfile> getAllGatewayResourceProfiles(AuthzToken authzToken)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
        // Get domain models from service
        List<GatewayResourceProfile> domainProfiles = airavataService.getAllGatewayResourceProfiles();
        // Convert domain models to thrift models
        return domainProfiles.stream()
                .map(gatewayResourceProfileMapper::toThrift)
                .collect(Collectors.toList());
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error getting all gateway resource profiles: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public boolean updateGatewayComputeResourcePreference(
            AuthzToken authzToken,
            String gatewayID,
            String computeResourceId,
            ComputeResourcePreference computeResourcePreference)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            ComputeResourcePreference domainPreference =
                    computeResourcePreferenceMapper.toDomain(computeResourcePreference);
        return airavataService.updateGatewayComputeResourcePreference(
                    gatewayID, computeResourceId, domainPreference);
        } catch (InvalidRequestException e) {
            throw convertToThriftInvalidRequestException(e);
        } catch (AiravataClientException e) {
            throw convertToThriftAiravataClientException(e);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (org.apache.airavata.common.exception.AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error updating gateway compute resource preference: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public boolean updateGatewayStoragePreference(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken,
            String gatewayID,
            String storageId,
            org.apache.airavata.thriftapi.model.StoragePreference dataStoragePreference)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
            org.apache.airavata.thriftapi.exception.AiravataClientException,
            org.apache.airavata.thriftapi.exception.AiravataSystemException,
            org.apache.airavata.thriftapi.exception.AuthorizationException,
            TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            org.apache.airavata.common.model.StoragePreference domainPreference = storagePreferenceMapper.toDomain(dataStoragePreference);
            return airavataService.updateGatewayStoragePreference(gatewayID, storageId, domainPreference);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException thriftException = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            thriftException.setMessage(e.getMessage());
            if (e.getAiravataErrorType() != null) {
                thriftException.setAiravataErrorType(
                        org.apache.airavata.thriftapi.exception.AiravataErrorType.valueOf(e.getAiravataErrorType().name()));
            }
            thriftException.initCause(e);
            throw thriftException;
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error updating gateway storage preference: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public boolean deleteGatewayComputeResourcePreference(
            AuthzToken authzToken, String gatewayID, String computeResourceId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
        return airavataService.deleteGatewayComputeResourcePreference(gatewayID, computeResourceId);
        } catch (InvalidRequestException e) {
            throw convertToThriftInvalidRequestException(e);
        } catch (AiravataClientException e) {
            throw convertToThriftAiravataClientException(e);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (org.apache.airavata.common.exception.AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error deleting gateway compute resource preference: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public boolean deleteGatewayStoragePreference(AuthzToken authzToken, String gatewayID, String storageId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
        return airavataService.deleteGatewayStoragePreference(gatewayID, storageId);
        } catch (InvalidRequestException e) {
            throw convertToThriftInvalidRequestException(e);
        } catch (AiravataClientException e) {
            throw convertToThriftAiravataClientException(e);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (org.apache.airavata.common.exception.AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error deleting gateway storage preference: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public List<SSHAccountProvisionerDescription> getSSHAccountProvisioners(AuthzToken authzToken)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {

        List<SSHAccountProvisionerDescription> domainProvisioners = new ArrayList<>();
        List<SSHAccountProvisionerProvider> sshAccountProvisionerProviders =
                SSHAccountProvisionerFactory.getSSHAccountProvisionerProviders();
        for (SSHAccountProvisionerProvider provider : sshAccountProvisionerProviders) {
            SSHAccountProvisionerDescription sshAccountProvisionerStruct = new SSHAccountProvisionerDescription();
            sshAccountProvisionerStruct.setCanCreateAccount(provider.canCreateAccount());
            sshAccountProvisionerStruct.setCanInstallSSHKey(provider.canInstallSSHKey());
            sshAccountProvisionerStruct.setName(provider.getName());
            List<SSHAccountProvisionerConfigParam> sshAccountProvisionerConfigParams = new ArrayList<>();
            for (ConfigParam configParam : provider.getConfigParams()) {
                SSHAccountProvisionerConfigParam sshAccountProvisionerConfigParam =
                        new SSHAccountProvisionerConfigParam();
                sshAccountProvisionerConfigParam.setName(configParam.getName());
                sshAccountProvisionerConfigParam.setDescription(configParam.getDescription());
                sshAccountProvisionerConfigParam.setIsOptional(configParam.isOptional());
                switch (configParam.getType()) {
                    case STRING:
                        sshAccountProvisionerConfigParam.setType(SSHAccountProvisionerConfigParamType.STRING);
                        break;
                    case CRED_STORE_PASSWORD_TOKEN:
                        sshAccountProvisionerConfigParam.setType(
                                SSHAccountProvisionerConfigParamType.CRED_STORE_PASSWORD_TOKEN);
                        break;
                }
                sshAccountProvisionerConfigParams.add(sshAccountProvisionerConfigParam);
            }
            sshAccountProvisionerStruct.setConfigParams(sshAccountProvisionerConfigParams);
            domainProvisioners.add(sshAccountProvisionerStruct);
        }
        // Convert domain models to thrift models
        return domainProvisioners.stream()
                .map(SSHAccountProvisionerDescriptionMapper.INSTANCE::toThrift)
                .collect(Collectors.toList());
    }

    @Override
    @SecurityCheck
    public boolean doesUserHaveSSHAccount(AuthzToken authzToken, String computeResourceId, String userId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            return airavataService.doesUserHaveSSHAccount(domainAuthzToken, computeResourceId, userId);
        } catch (org.apache.airavata.common.exception.InvalidRequestException e) {
            throw convertToThriftInvalidRequestException(e);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error checking if user has SSH account: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public boolean isSSHSetupCompleteForUserComputeResourcePreference(
            AuthzToken authzToken, String computeResourceId, String airavataCredStoreToken)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            return airavataService.isSSHAccountSetupComplete(
                    domainAuthzToken, computeResourceId, airavataCredStoreToken);
        } catch (InvalidRequestException e) {
            throw convertToThriftInvalidRequestException(e);
        } catch (AiravataClientException e) {
            throw convertToThriftAiravataClientException(e);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (org.apache.airavata.common.exception.AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error checking SSH setup completion: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public UserComputeResourcePreference setupUserComputeResourcePreferencesForSSH(
            AuthzToken authzToken, String computeResourceId, String userId, String airavataCredStoreToken)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            UserComputeResourcePreference domainPreference = airavataService.setupSSHAccount(
                    domainAuthzToken, computeResourceId, userId, airavataCredStoreToken);
            return userComputeResourcePreferenceMapper.toThrift(domainPreference);
        } catch (InvalidRequestException e) {
            throw convertToThriftInvalidRequestException(e);
        } catch (AiravataClientException e) {
            throw convertToThriftAiravataClientException(e);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (org.apache.airavata.common.exception.AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error setting up user compute resource preferences for SSH: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public String registerUserResourceProfile(AuthzToken authzToken, UserResourceProfile userResourceProfile)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            UserResourceProfile domainProfile = userResourceProfileMapper.toDomain(userResourceProfile);
            return airavataService.registerUserResourceProfile(domainProfile);
        } catch (InvalidRequestException e) {
            throw convertToThriftInvalidRequestException(e);
        } catch (AiravataClientException e) {
            throw convertToThriftAiravataClientException(e);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (org.apache.airavata.common.exception.AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error registering user resource profile: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public boolean isUserResourceProfileExists(AuthzToken authzToken, String userId, String gatewayID)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
        return airavataService.isUserResourceProfileExists(userId, gatewayID);
        } catch (InvalidRequestException e) {
            throw convertToThriftInvalidRequestException(e);
        } catch (AiravataClientException e) {
            throw convertToThriftAiravataClientException(e);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (org.apache.airavata.common.exception.AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error checking if user resource profile exists: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public UserResourceProfile getUserResourceProfile(AuthzToken authzToken, String userId, String gatewayID)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
        // Get domain model from service
        UserResourceProfile domainProfile = airavataService.getUserResourceProfile(userId, gatewayID);
        // Convert domain model to thrift model
        return userResourceProfileMapper.toThrift(domainProfile);
        } catch (InvalidRequestException e) {
            throw convertToThriftInvalidRequestException(e);
        } catch (org.apache.airavata.common.exception.AiravataClientException e) {
            throw convertToThriftAiravataClientException(e);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (org.apache.airavata.common.exception.AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error getting user resource profile: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public boolean updateUserResourceProfile(
            AuthzToken authzToken, String userId, String gatewayID, UserResourceProfile userResourceProfile)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
        // Convert thrift model to domain model
        UserResourceProfile domainProfile = userResourceProfileMapper.toDomain(userResourceProfile);
        return airavataService.updateUserResourceProfile(userId, gatewayID, domainProfile);
        } catch (InvalidRequestException e) {
            throw convertToThriftInvalidRequestException(e);
        } catch (AiravataClientException e) {
            throw convertToThriftAiravataClientException(e);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (org.apache.airavata.common.exception.AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error updating user resource profile: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public boolean deleteUserResourceProfile(AuthzToken authzToken, String userId, String gatewayID)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
        return airavataService.deleteUserResourceProfile(userId, gatewayID);
        } catch (InvalidRequestException e) {
            throw convertToThriftInvalidRequestException(e);
        } catch (AiravataClientException e) {
            throw convertToThriftAiravataClientException(e);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (org.apache.airavata.common.exception.AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error deleting user resource profile: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public boolean addUserComputeResourcePreference(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken,
            String userId,
            String gatewayID,
            String computeResourceId,
            org.apache.airavata.thriftapi.model.UserComputeResourcePreference userComputeResourcePreference)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
            org.apache.airavata.thriftapi.exception.AiravataClientException,
            org.apache.airavata.thriftapi.exception.AiravataSystemException,
            org.apache.airavata.thriftapi.exception.AuthorizationException,
            TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            org.apache.airavata.common.model.UserComputeResourcePreference domainPreference =
                    userComputeResourcePreferenceMapper.toDomain(userComputeResourcePreference);
            return airavataService.addUserComputeResourcePreference(
                    userId, gatewayID, computeResourceId, domainPreference);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException thriftException = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            thriftException.setMessage(e.getMessage());
            if (e.getAiravataErrorType() != null) {
                thriftException.setAiravataErrorType(
                        org.apache.airavata.thriftapi.exception.AiravataErrorType.valueOf(e.getAiravataErrorType().name()));
            }
            thriftException.initCause(e);
            throw thriftException;
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error adding user compute resource preference: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public boolean addUserStoragePreference(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken,
            String userId,
            String gatewayID,
            String userStorageResourceId,
            org.apache.airavata.thriftapi.model.UserStoragePreference dataStoragePreference)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
            org.apache.airavata.thriftapi.exception.AiravataClientException,
            org.apache.airavata.thriftapi.exception.AiravataSystemException,
            org.apache.airavata.thriftapi.exception.AuthorizationException,
            TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            org.apache.airavata.common.model.UserStoragePreference domainPreference = userStoragePreferenceMapper.toDomain(dataStoragePreference);
            return airavataService.addUserStoragePreference(userId, gatewayID, userStorageResourceId, domainPreference);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException thriftException = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            thriftException.setMessage(e.getMessage());
            if (e.getAiravataErrorType() != null) {
                thriftException.setAiravataErrorType(
                        org.apache.airavata.thriftapi.exception.AiravataErrorType.valueOf(e.getAiravataErrorType().name()));
            }
            thriftException.initCause(e);
            throw thriftException;
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error adding user storage preference: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public UserComputeResourcePreference getUserComputeResourcePreference(
            AuthzToken authzToken, String userId, String gatewayID, String userComputeResourceId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        try {
            UserComputeResourcePreference domainPreference =
                    airavataService.getUserComputeResourcePreference(userId, gatewayID, userComputeResourceId);
            return userComputeResourcePreferenceMapper.toThrift(domainPreference);
        } catch (InvalidRequestException e) {
            throw convertToThriftInvalidRequestException(e);
        } catch (AiravataClientException e) {
            throw convertToThriftAiravataClientException(e);
        } catch (AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error getting user compute resource preference: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public UserStoragePreference getUserStoragePreference(
            AuthzToken authzToken, String userId, String gatewayID, String userStorageId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        try {
            UserStoragePreference domainPreference =
                    airavataService.getUserStoragePreference(userId, gatewayID, userStorageId);
            return userStoragePreferenceMapper.toThrift(domainPreference);
        } catch (InvalidRequestException e) {
            throw convertToThriftInvalidRequestException(e);
        } catch (AiravataClientException e) {
            throw convertToThriftAiravataClientException(e);
        } catch (AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error getting user storage preference: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public List<UserComputeResourcePreference> getAllUserComputeResourcePreferences(
            AuthzToken authzToken, String userId, String gatewayID)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        try {
            List<UserComputeResourcePreference> domainPreferences =
                    airavataService.getAllUserComputeResourcePreferences(userId, gatewayID);
            return domainPreferences.stream()
                    .map(userComputeResourcePreferenceMapper::toThrift)
                    .collect(Collectors.toList());
        } catch (InvalidRequestException e) {
            throw convertToThriftInvalidRequestException(e);
        } catch (AiravataClientException e) {
            throw convertToThriftAiravataClientException(e);
        } catch (AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error getting all user compute resource preferences: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public List<UserStoragePreference> getAllUserStoragePreferences(
            AuthzToken authzToken, String userId, String gatewayID)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        try {
            List<UserStoragePreference> domainPreferences =
                    airavataService.getAllUserStoragePreferences(userId, gatewayID);
            return domainPreferences.stream()
                    .map(userStoragePreferenceMapper::toThrift)
                    .collect(Collectors.toList());
        } catch (InvalidRequestException e) {
            throw convertToThriftInvalidRequestException(e);
        } catch (AiravataClientException e) {
            throw convertToThriftAiravataClientException(e);
        } catch (AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error getting all user storage preferences: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public List<UserResourceProfile> getAllUserResourceProfiles(AuthzToken authzToken)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        try {
            List<UserResourceProfile> domainProfiles = airavataService.getAllUserResourceProfiles();
            return domainProfiles.stream()
                    .map(userResourceProfileMapper::toThrift)
                    .collect(Collectors.toList());
        } catch (InvalidRequestException e) {
            throw convertToThriftInvalidRequestException(e);
        } catch (AiravataClientException e) {
            throw convertToThriftAiravataClientException(e);
        } catch (AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error getting all user resource profiles: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public boolean updateUserComputeResourcePreference(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken,
            String userId,
            String gatewayID,
            String userComputeResourceId,
            UserComputeResourcePreference userComputeResourcePreference)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
            org.apache.airavata.thriftapi.exception.AiravataClientException,
            org.apache.airavata.thriftapi.exception.AiravataSystemException,
            org.apache.airavata.thriftapi.exception.AuthorizationException,
            TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            org.apache.airavata.common.model.UserComputeResourcePreference domainPreference =
                    userComputeResourcePreferenceMapper.toDomain(userComputeResourcePreference);
            airavataService.updateUserComputeResourcePreference(
                    userId, gatewayID, userComputeResourceId, domainPreference);
            return true;
        } catch (org.apache.airavata.common.exception.InvalidRequestException e) {
            throw convertToThriftInvalidRequestException(e);
        } catch (org.apache.airavata.common.exception.AiravataClientException e) {
            throw convertToThriftAiravataClientException(e);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error updating user compute resource preference: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public boolean updateUserStoragePreference(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken,
            String userId,
            String gatewayID,
            String userStorageId,
            org.apache.airavata.thriftapi.model.UserStoragePreference dataStoragePreference)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
            org.apache.airavata.thriftapi.exception.AiravataClientException,
            org.apache.airavata.thriftapi.exception.AiravataSystemException,
            org.apache.airavata.thriftapi.exception.AuthorizationException,
            TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            org.apache.airavata.common.model.UserStoragePreference domainPreference = userStoragePreferenceMapper.toDomain(dataStoragePreference);
            return airavataService.updateUserStoragePreference(userId, gatewayID, userStorageId, domainPreference);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException thriftException = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            thriftException.setMessage(e.getMessage());
            if (e.getAiravataErrorType() != null) {
                thriftException.setAiravataErrorType(
                        org.apache.airavata.thriftapi.exception.AiravataErrorType.valueOf(e.getAiravataErrorType().name()));
            }
            thriftException.initCause(e);
            throw thriftException;
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error updating user storage preference: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public boolean deleteUserComputeResourcePreference(
            AuthzToken authzToken, String userId, String gatewayID, String userComputeResourceId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
        return airavataService.deleteUserComputeResourcePreference(userId, gatewayID, userComputeResourceId);
        } catch (InvalidRequestException e) {
            throw convertToThriftInvalidRequestException(e);
        } catch (AiravataClientException e) {
            throw convertToThriftAiravataClientException(e);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (org.apache.airavata.common.exception.AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error deleting user compute resource preference: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public boolean deleteUserStoragePreference(
            AuthzToken authzToken, String userId, String gatewayID, String userStorageId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
        return airavataService.deleteUserStoragePreference(userId, gatewayID, userStorageId);
        } catch (InvalidRequestException e) {
            throw convertToThriftInvalidRequestException(e);
        } catch (AiravataClientException e) {
            throw convertToThriftAiravataClientException(e);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (org.apache.airavata.common.exception.AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error deleting user storage preference: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public List<QueueStatusModel> getLatestQueueStatuses(AuthzToken authzToken)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        // Get domain models from service
        List<QueueStatusModel> domainStatuses = airavataService.getLatestQueueStatuses();
        // Convert domain models to thrift models
        return domainStatuses.stream()
                .map(QueueStatusModelMapper.INSTANCE::toThrift)
                .collect(Collectors.toList());
    }

    @Override
    @SecurityCheck
    public String registerDataProduct(AuthzToken authzToken, DataProductModel dataProductModel)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        // Convert thrift model to domain model
        DataProductModel domainDataProduct = dataProductModelMapper.toDomain(dataProductModel);
        return airavataService.registerDataProduct(domainDataProduct);
    }

    @Override
    @SecurityCheck
    public DataProductModel getDataProduct(AuthzToken authzToken, String productUri)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        // Get domain model from service
        DataProductModel domainDataProduct = airavataService.getDataProduct(productUri);
        // Convert domain model to thrift model
        return dataProductModelMapper.toThrift(domainDataProduct);
    }

    @Override
    @SecurityCheck
    public String registerReplicaLocation(AuthzToken authzToken, DataReplicaLocationModel replicaLocationModel)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        // Convert thrift model to domain model
        DataReplicaLocationModel domainReplica = DataReplicaLocationModelMapper.INSTANCE.toDomain(replicaLocationModel);
        return airavataService.registerReplicaLocation(domainReplica);
    }

    @Override
    @SecurityCheck
    public DataProductModel getParentDataProduct(AuthzToken authzToken, String productUri)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        // Get domain model from service
        DataProductModel domainDataProduct = airavataService.getParentDataProduct(productUri);
        // Convert domain model to thrift model
        return dataProductModelMapper.toThrift(domainDataProduct);
    }

    @Override
    @SecurityCheck
    public List<DataProductModel> getChildDataProducts(AuthzToken authzToken, String productUri)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        // Get domain models from service
        List<DataProductModel> domainDataProducts = airavataService.getChildDataProducts(productUri);
        // Convert domain models to thrift models
        return domainDataProducts.stream().map(dataProductModelMapper::toThrift).collect(Collectors.toList());
    }

    @Override
    @SecurityCheck
    public boolean shareResourceWithUsers(
            AuthzToken authzToken, String resourceId, Map<String, ResourcePermissionType> userPermissionList)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            Map<String, ResourcePermissionType> domainUserPermissionList = new java.util.HashMap<>();
            for (Map.Entry<String, ResourcePermissionType> entry : userPermissionList.entrySet()) {
                domainUserPermissionList.put(
                        entry.getKey(),
                        ResourcePermissionType.valueOf(entry.getValue().name()));
            }
            return airavataService.shareResourceWithUsers(domainAuthzToken, resourceId, domainUserPermissionList);
        } catch (InvalidRequestException e) {
            throw convertToThriftInvalidRequestException(e);
        } catch (AiravataClientException e) {
            throw convertToThriftAiravataClientException(e);
        } catch (AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error sharing resource with users: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public boolean shareResourceWithGroups(
            AuthzToken authzToken, String resourceId, Map<String, ResourcePermissionType> groupPermissionList)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            Map<String, ResourcePermissionType> domainGroupPermissionList = new java.util.HashMap<>();
            for (Map.Entry<String, ResourcePermissionType> entry : groupPermissionList.entrySet()) {
                domainGroupPermissionList.put(
                        entry.getKey(),
                        ResourcePermissionType.valueOf(entry.getValue().name()));
            }
            return airavataService.shareResourceWithGroups(domainAuthzToken, resourceId, domainGroupPermissionList);
        } catch (InvalidRequestException e) {
            throw convertToThriftInvalidRequestException(e);
        } catch (AiravataClientException e) {
            throw convertToThriftAiravataClientException(e);
        } catch (AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error sharing resource with groups: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public boolean revokeSharingOfResourceFromUsers(
            AuthzToken authzToken, String resourceId, Map<String, ResourcePermissionType> userPermissionList)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            Map<String, ResourcePermissionType> domainUserPermissionList = new java.util.HashMap<>();
            for (Map.Entry<String, ResourcePermissionType> entry : userPermissionList.entrySet()) {
                domainUserPermissionList.put(
                        entry.getKey(),
                        ResourcePermissionType.valueOf(entry.getValue().name()));
            }
            return airavataService.revokeSharingOfResourceFromUsers(
                    domainAuthzToken, resourceId, domainUserPermissionList);
        } catch (InvalidRequestException e) {
            throw convertToThriftInvalidRequestException(e);
        } catch (AiravataClientException e) {
            throw convertToThriftAiravataClientException(e);
        } catch (AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error revoking sharing of resource from users: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public boolean revokeSharingOfResourceFromGroups(
            AuthzToken authzToken, String resourceId, Map<String, ResourcePermissionType> groupPermissionList)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            Map<String, ResourcePermissionType> domainGroupPermissionList = new java.util.HashMap<>();
            for (Map.Entry<String, ResourcePermissionType> entry : groupPermissionList.entrySet()) {
                domainGroupPermissionList.put(
                        entry.getKey(),
                        ResourcePermissionType.valueOf(entry.getValue().name()));
            }
            return airavataService.revokeSharingOfResourceFromGroups(
                    domainAuthzToken, resourceId, domainGroupPermissionList);
        } catch (InvalidRequestException e) {
            throw convertToThriftInvalidRequestException(e);
        } catch (AiravataClientException e) {
            throw convertToThriftAiravataClientException(e);
        } catch (AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error revoking sharing of resource from groups: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public List<String> getAllAccessibleUsers(
            AuthzToken authzToken, String resourceId, ResourcePermissionType permissionType)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
        ResourcePermissionType domainPermissionType = ResourcePermissionType.valueOf(permissionType.name());
        return airavataService.getAllAccessibleUsers(domainAuthzToken, resourceId, domainPermissionType, false);
    }

    @Override
    @SecurityCheck
    public List<String> getAllDirectlyAccessibleUsers(
            AuthzToken authzToken, String resourceId, ResourcePermissionType permissionType)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
        ResourcePermissionType domainPermissionType = ResourcePermissionType.valueOf(permissionType.name());
        return airavataService.getAllAccessibleUsers(domainAuthzToken, resourceId, domainPermissionType, true);
    }

    @Override
    @SecurityCheck
    public List<String> getAllAccessibleGroups(
            AuthzToken authzToken, String resourceId, ResourcePermissionType permissionType)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
        ResourcePermissionType domainPermissionType = ResourcePermissionType.valueOf(permissionType.name());
        return airavataService.getAllAccessibleGroups(domainAuthzToken, resourceId, domainPermissionType, false);
    }

    @Override
    @SecurityCheck
    public List<String> getAllDirectlyAccessibleGroups(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken,
            String resourceId,
            ResourcePermissionType permissionType)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
            org.apache.airavata.thriftapi.exception.AiravataClientException,
            org.apache.airavata.thriftapi.exception.AiravataSystemException,
            org.apache.airavata.thriftapi.exception.AuthorizationException,
            TException {
        org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
        org.apache.airavata.common.model.ResourcePermissionType domainPermissionType = org.apache.airavata.common.model.ResourcePermissionType.valueOf(permissionType.name());
        return airavataService.getAllAccessibleGroups(domainAuthzToken, resourceId, domainPermissionType, true);
    }

    @Override
    @SecurityCheck
    public boolean userHasAccess(AuthzToken authzToken, String resourceId, ResourcePermissionType permissionType)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
        ResourcePermissionType domainPermissionType = ResourcePermissionType.valueOf(permissionType.name());
        return airavataService.userHasAccess(domainAuthzToken, resourceId, domainPermissionType);
    }

    @Override
    @SecurityCheck
    public String createGroupResourceProfile(AuthzToken authzToken, GroupResourceProfile groupResourceProfile)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
        GroupResourceProfile domainGroupResourceProfile = groupResourceProfileMapper.toDomain(groupResourceProfile);
        return airavataService.createGroupResourceProfile(domainAuthzToken, domainGroupResourceProfile);
    }

    @Override
    @SecurityCheck
    public void updateGroupResourceProfile(AuthzToken authzToken, GroupResourceProfile groupResourceProfile)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
        GroupResourceProfile domainGroupResourceProfile = groupResourceProfileMapper.toDomain(groupResourceProfile);
        airavataService.updateGroupResourceProfile(domainAuthzToken, domainGroupResourceProfile);
    }

    @Override
    @SecurityCheck
    public GroupResourceProfile getGroupResourceProfile(AuthzToken authzToken, String groupResourceProfileId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            GroupResourceProfile domainProfile =
                    airavataService.getGroupResourceProfile(domainAuthzToken, groupResourceProfileId);
            return groupResourceProfileMapper.toThrift(domainProfile);
        } catch (InvalidRequestException e) {
            throw convertToThriftInvalidRequestException(e);
        } catch (AiravataClientException e) {
            throw convertToThriftAiravataClientException(e);
        } catch (AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error getting group resource profile: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public boolean removeGroupResourceProfile(AuthzToken authzToken, String groupResourceProfileId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            return airavataService.removeGroupResourceProfile(domainAuthzToken, groupResourceProfileId);
        } catch (AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error removing group resource profile: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public List<GroupResourceProfile> getGroupResourceList(AuthzToken authzToken, String gatewayId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            List<GroupResourceProfile> domainProfiles =
                    airavataService.getGroupResourceList(domainAuthzToken, gatewayId);
            return domainProfiles.stream()
                    .map(groupResourceProfileMapper::toThrift)
                    .collect(Collectors.toList());
        } catch (InvalidRequestException e) {
            throw convertToThriftInvalidRequestException(e);
        } catch (AiravataClientException e) {
            throw convertToThriftAiravataClientException(e);
        } catch (AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error getting group resource list: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public boolean removeGroupComputePrefs(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken,
            String computeResourceId,
            String groupResourceProfileId)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
            org.apache.airavata.thriftapi.exception.AiravataClientException,
            org.apache.airavata.thriftapi.exception.AiravataSystemException,
            org.apache.airavata.thriftapi.exception.AuthorizationException,
            TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            return airavataService.removeGroupComputePrefs(domainAuthzToken, computeResourceId, groupResourceProfileId);
        } catch (org.apache.airavata.common.exception.InvalidRequestException e) {
            throw convertToThriftInvalidRequestException(e);
        } catch (AiravataClientException e) {
            throw convertToThriftAiravataClientException(e);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (org.apache.airavata.common.exception.AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error removing group compute preferences: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public boolean removeGroupComputeResourcePolicy(AuthzToken authzToken, String resourcePolicyId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            return airavataService.removeGroupComputeResourcePolicy(domainAuthzToken, resourcePolicyId);
        } catch (org.apache.airavata.common.exception.AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error removing group compute resource policy: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public boolean removeGroupBatchQueueResourcePolicy(AuthzToken authzToken, String resourcePolicyId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            return airavataService.removeGroupBatchQueueResourcePolicy(domainAuthzToken, resourcePolicyId);
        } catch (AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error removing group batch queue resource policy: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public ComputeResourcePolicy getGroupComputeResourcePolicy(AuthzToken authzToken, String resourcePolicyId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            ComputeResourcePolicy domainPolicy =
                    airavataService.getGroupComputeResourcePolicy(domainAuthzToken, resourcePolicyId);
            return computeResourcePolicyMapper.toThrift(domainPolicy);
        } catch (InvalidRequestException e) {
            throw convertToThriftInvalidRequestException(e);
        } catch (AiravataClientException e) {
            throw convertToThriftAiravataClientException(e);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (org.apache.airavata.common.exception.AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error getting group compute resource policy: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public BatchQueueResourcePolicy getBatchQueueResourcePolicy(AuthzToken authzToken, String resourcePolicyId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            BatchQueueResourcePolicy domainPolicy =
                    airavataService.getBatchQueueResourcePolicy(domainAuthzToken, resourcePolicyId);
            return batchQueueResourcePolicyMapper.toThrift(domainPolicy);
        } catch (InvalidRequestException e) {
            throw convertToThriftInvalidRequestException(e);
        } catch (AiravataClientException e) {
            throw convertToThriftAiravataClientException(e);
        } catch (AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (org.apache.airavata.common.exception.AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error getting batch queue resource policy: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public List<GroupComputeResourcePreference> getGroupComputeResourcePrefList(
            AuthzToken authzToken, String groupResourceProfileId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            List<GroupComputeResourcePreference> domainPrefs =
                    airavataService.getGroupComputeResourcePrefList(domainAuthzToken, groupResourceProfileId);
            return domainPrefs.stream()
                    .map(groupComputeResourcePreferenceMapper::toThrift)
                    .collect(Collectors.toList());
        } catch (InvalidRequestException e) {
            throw convertToThriftInvalidRequestException(e);
        } catch (AiravataClientException e) {
            throw convertToThriftAiravataClientException(e);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (org.apache.airavata.common.exception.AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error getting group compute resource preference list: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public List<BatchQueueResourcePolicy> getGroupBatchQueueResourcePolicyList(
            AuthzToken authzToken, String groupResourceProfileId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            List<BatchQueueResourcePolicy> domainPolicies =
                    airavataService.getGroupBatchQueueResourcePolicyList(domainAuthzToken, groupResourceProfileId);
            return domainPolicies.stream()
                    .map(batchQueueResourcePolicyMapper::toThrift)
                    .collect(Collectors.toList());
        } catch (InvalidRequestException e) {
            throw convertToThriftInvalidRequestException(e);
        } catch (AiravataClientException e) {
            throw convertToThriftAiravataClientException(e);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (org.apache.airavata.common.exception.AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error getting group batch queue resource policy list: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public List<ComputeResourcePolicy> getGroupComputeResourcePolicyList(
            AuthzToken authzToken, String groupResourceProfileId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            List<ComputeResourcePolicy> domainPolicies =
                    airavataService.getGroupComputeResourcePolicyList(domainAuthzToken, groupResourceProfileId);
            return domainPolicies.stream()
                    .map(computeResourcePolicyMapper::toThrift)
                    .collect(Collectors.toList());
        } catch (InvalidRequestException e) {
            throw convertToThriftInvalidRequestException(e);
        } catch (AiravataClientException e) {
            throw convertToThriftAiravataClientException(e);
        } catch (AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error getting group compute resource policy list: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public GatewayGroups getGatewayGroups(AuthzToken authzToken)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            String gatewayId = domainAuthzToken.getClaimsMap().get(Constants.GATEWAY_ID);
            GatewayGroups domainGroups = airavataService.getGatewayGroups(gatewayId);
            return gatewayGroupsMapper.toThrift(domainGroups);
        } catch (InvalidRequestException e) {
            throw convertToThriftInvalidRequestException(e);
        } catch (AiravataClientException e) {
            throw convertToThriftAiravataClientException(e);
        } catch (AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error getting gateway groups: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public Parser getParser(AuthzToken authzToken, String parserId, String gatewayId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        try {
            Parser domainParser = airavataService.getParser(parserId, gatewayId);
            return parserMapper.toThrift(domainParser);
        } catch (InvalidRequestException e) {
            throw convertToThriftInvalidRequestException(e);
        } catch (AiravataClientException e) {
            throw convertToThriftAiravataClientException(e);
        } catch (AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error getting parser: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public String saveParser(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken,
            Parser parser)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
            org.apache.airavata.thriftapi.exception.AiravataClientException,
            org.apache.airavata.thriftapi.exception.AiravataSystemException,
            org.apache.airavata.thriftapi.exception.AuthorizationException,
            TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            org.apache.airavata.common.model.Parser domainParser = parserMapper.toDomain(parser);
            return airavataService.saveParser(domainParser);
        } catch (org.apache.airavata.common.exception.InvalidRequestException e) {
            throw convertToThriftInvalidRequestException(e);
        } catch (org.apache.airavata.common.exception.AiravataClientException e) {
            throw convertToThriftAiravataClientException(e);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (org.apache.airavata.common.exception.AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error saving parser: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public List<Parser> listAllParsers(AuthzToken authzToken, String gatewayId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        try {
            List<Parser> domainParsers = airavataService.listAllParsers(gatewayId);
            return domainParsers.stream().map(parserMapper::toThrift).collect(Collectors.toList());
        } catch (InvalidRequestException e) {
            throw convertToThriftInvalidRequestException(e);
        } catch (AiravataClientException e) {
            throw convertToThriftAiravataClientException(e);
        } catch (AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error listing parsers: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public boolean removeParser(AuthzToken authzToken, String parserId, String gatewayId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        try {
        airavataService.removeParser(parserId, gatewayId);
        return true;
        } catch (InvalidRequestException e) {
            throw convertToThriftInvalidRequestException(e);
        } catch (AiravataClientException e) {
            throw convertToThriftAiravataClientException(e);
        } catch (AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error removing parser: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public ParsingTemplate getParsingTemplate(AuthzToken authzToken, String templateId, String gatewayId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        try {
            ParsingTemplate domainTemplate = airavataService.getParsingTemplate(templateId, gatewayId);
            return parsingTemplateMapper.toThrift(domainTemplate);
        } catch (InvalidRequestException e) {
            throw convertToThriftInvalidRequestException(e);
        } catch (AiravataClientException e) {
            throw convertToThriftAiravataClientException(e);
        } catch (AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error getting parsing template: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public List<ParsingTemplate> getParsingTemplatesForExperiment(
            AuthzToken authzToken, String experimentId, String gatewayId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        try {
            List<ParsingTemplate> domainTemplates =
                    airavataService.getParsingTemplatesForExperiment(experimentId, gatewayId);
            return domainTemplates.stream().map(parsingTemplateMapper::toThrift).collect(Collectors.toList());
        } catch (InvalidRequestException e) {
            throw convertToThriftInvalidRequestException(e);
        } catch (AiravataClientException e) {
            throw convertToThriftAiravataClientException(e);
        } catch (AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error getting parsing templates for experiment: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public String saveParsingTemplate(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken,
            org.apache.airavata.thriftapi.model.ParsingTemplate parsingTemplate)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
            org.apache.airavata.thriftapi.exception.AiravataClientException,
            org.apache.airavata.thriftapi.exception.AiravataSystemException,
            org.apache.airavata.thriftapi.exception.AuthorizationException,
            TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            org.apache.airavata.common.model.ParsingTemplate domainTemplate = parsingTemplateMapper.toDomain(parsingTemplate);
            return airavataService.saveParsingTemplate(domainTemplate);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException thriftException = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            thriftException.setMessage(e.getMessage());
            if (e.getAiravataErrorType() != null) {
                thriftException.setAiravataErrorType(
                        org.apache.airavata.thriftapi.exception.AiravataErrorType.valueOf(e.getAiravataErrorType().name()));
            }
            thriftException.initCause(e);
            throw thriftException;
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error saving parsing template: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public boolean removeParsingTemplate(AuthzToken authzToken, String templateId, String gatewayId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        airavataService.removeParsingTemplate(templateId, gatewayId);
        return true;
    }

    @Override
    @SecurityCheck
    public List<ParsingTemplate> listAllParsingTemplates(AuthzToken authzToken, String gatewayId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        try {
            List<ParsingTemplate> domainTemplates = airavataService.listAllParsingTemplates(gatewayId);
            return domainTemplates.stream().map(parsingTemplateMapper::toThrift).collect(Collectors.toList());
        } catch (InvalidRequestException e) {
            throw convertToThriftInvalidRequestException(e);
        } catch (AiravataClientException e) {
            throw convertToThriftAiravataClientException(e);
        } catch (AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error listing parsing templates: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    /**
     * To hold storage info context (login username, credential token, and adaptor)
     */
    @Override
    @SecurityCheck
    public ProcessStatus getIntermediateOutputProcessStatus(
            AuthzToken authzToken, String airavataExperimentId, List<String> outputNames)
            throws InvalidRequestException, ExperimentNotFoundException, AiravataClientException,
                    AiravataSystemException, AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            ProcessStatus domainStatus = airavataService.getIntermediateOutputProcessStatus(
                    domainAuthzToken, airavataExperimentId, outputNames);
            return processStatusMapper.toThrift(domainStatus);
        } catch (InvalidRequestException e) {
            throw convertToThriftInvalidRequestException(e);
        } catch (ExperimentNotFoundException e) {
            throw convertToThriftExperimentNotFoundException(e);
        } catch (AiravataClientException e) {
            throw convertToThriftAiravataClientException(e);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (org.apache.airavata.common.exception.AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error getting intermediate output process status: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public void updateResourceScheduleing(
            AuthzToken authzToken, String airavataExperimentId, ComputationalResourceSchedulingModel resourceScheduling)
            throws AuthorizationException, TException {
        try {
        org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            ComputationalResourceSchedulingModel domainScheduling =
                    computationalResourceSchedulingModelMapper.toDomain(resourceScheduling);
            airavataService.updateResourceScheduleing(airavataExperimentId, domainScheduling);
        } catch (AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error updating resource scheduling: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public List<ComputeResourcePreference> getAllGatewayComputeResourcePreferences(
            AuthzToken authzToken, String gatewayID)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
        List<ComputeResourcePreference> domainPreferences =
                airavataService.getAllGatewayComputeResourcePreferences(gatewayID);
        return domainPreferences.stream()
                .map(computeResourcePreferenceMapper::toThrift)
                .collect(Collectors.toList());
    }

    @Override
    @SecurityCheck
    public GroupComputeResourcePreference getGroupComputeResourcePreference(
            AuthzToken authzToken, String computeResourceId, String groupResourceProfileId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
        GroupComputeResourcePreference domainPreference =
                airavataService.getGroupComputeResourcePreference(groupResourceProfileId, computeResourceId);
        return groupComputeResourcePreferenceMapper.toThrift(domainPreference);
    }

    @Override
    @SecurityCheck
    public boolean deleteJobSubmissionInterface(
            AuthzToken authzToken, String computeResourceId, String jobSubmissionInterfaceId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
        return airavataService.deleteJobSubmissionInterface(computeResourceId, jobSubmissionInterfaceId);
    }

    @Override
    @SecurityCheck
    public String addLocalDataMovementDetails(
            AuthzToken authzToken,
            String productUri,
            DMType dataMoveType,
            int priorityOrder,
            LOCALDataMovement localDataMovement)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
        DMType domainDMType = DMType.valueOf(dataMoveType.name());
        LOCALDataMovement domainLocal = localDataMovementMapper.toDomain(localDataMovement);
        return airavataService.addLocalDataMovementDetails(productUri, domainDMType, priorityOrder, domainLocal);
    }

    @Override
    @SecurityCheck
    public boolean deleteDataMovementInterface(
            AuthzToken authzToken, String productUri, String dataMovementInterfaceId, DMType dataMoveType)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        try {
        org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            DMType domainDMType = DMType.valueOf(dataMoveType.name());
            return airavataService.deleteDataMovementInterface(productUri, dataMovementInterfaceId, domainDMType);
        } catch (InvalidRequestException e) {
            throw convertToThriftInvalidRequestException(e);
        } catch (AiravataClientException e) {
            throw convertToThriftAiravataClientException(e);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (org.apache.airavata.common.exception.AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error deleting data movement interface: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public String addSCPDataMovementDetails(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken,
            String productUri,
            DMType dataMoveType,
            int priorityOrder,
            SCPDataMovement scpDataMovement)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
            org.apache.airavata.thriftapi.exception.AiravataClientException,
            org.apache.airavata.thriftapi.exception.AiravataSystemException,
            org.apache.airavata.thriftapi.exception.AuthorizationException,
            TException {
        try {
        org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
        org.apache.airavata.common.model.DMType domainDMType = org.apache.airavata.common.model.DMType.valueOf(dataMoveType.name());
            org.apache.airavata.common.model.SCPDataMovement domainSCP = scpDataMovementMapper.toDomain(scpDataMovement);
            return airavataService.addSCPDataMovementDetails(productUri, domainDMType, priorityOrder, domainSCP);
        } catch (org.apache.airavata.common.exception.InvalidRequestException e) {
            throw convertToThriftInvalidRequestException(e);
        } catch (org.apache.airavata.common.exception.AiravataClientException e) {
            throw convertToThriftAiravataClientException(e);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (org.apache.airavata.common.exception.AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            var ex = new AiravataSystemException();
            ex.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error adding SCP data movement details: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }
}
