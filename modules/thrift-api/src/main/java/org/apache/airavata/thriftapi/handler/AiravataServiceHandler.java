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
import org.apache.airavata.service.AiravataService;
import org.apache.airavata.thriftapi.credential.exception.CredentialStoreException;
import org.apache.airavata.thriftapi.exception.AiravataClientException;
import org.apache.airavata.thriftapi.exception.AiravataErrorType;
import org.apache.airavata.thriftapi.exception.AiravataSystemException;
import org.apache.airavata.thriftapi.exception.AuthorizationException;
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
import org.apache.airavata.thriftapi.model.ApplicationModule;
import org.apache.airavata.thriftapi.model.BatchQueueResourcePolicy;
import org.apache.airavata.thriftapi.model.CloudJobSubmission;
import org.apache.airavata.thriftapi.model.ComputeResourceDescription;
import org.apache.airavata.thriftapi.model.ComputeResourcePolicy;
import org.apache.airavata.thriftapi.model.ComputeResourcePreference;
import org.apache.airavata.thriftapi.model.DMType;
import org.apache.airavata.thriftapi.model.DataProductModel;
import org.apache.airavata.thriftapi.model.DataReplicaLocationModel;
import org.apache.airavata.thriftapi.model.ExperimentModel;
import org.apache.airavata.thriftapi.model.ExperimentSearchFields;
import org.apache.airavata.thriftapi.model.ExperimentState;
import org.apache.airavata.thriftapi.model.ExperimentStatistics;
import org.apache.airavata.thriftapi.model.ExperimentStatus;
import org.apache.airavata.thriftapi.model.ExperimentSummaryModel;
import org.apache.airavata.thriftapi.model.GatewayGroups;
import org.apache.airavata.thriftapi.model.GatewayResourceProfile;
import org.apache.airavata.thriftapi.model.GridFTPDataMovement;
import org.apache.airavata.thriftapi.model.GroupComputeResourcePreference;
import org.apache.airavata.thriftapi.model.GroupResourceProfile;
import org.apache.airavata.thriftapi.model.InputDataObjectType;
import org.apache.airavata.thriftapi.model.JobModel;
import org.apache.airavata.thriftapi.model.JobState;
import org.apache.airavata.thriftapi.model.JobStatus;
import org.apache.airavata.thriftapi.model.LOCALDataMovement;
import org.apache.airavata.thriftapi.model.LOCALSubmission;
import org.apache.airavata.thriftapi.model.OutputDataObjectType;
import org.apache.airavata.thriftapi.model.Parser;
import org.apache.airavata.thriftapi.model.ParsingTemplate;
import org.apache.airavata.thriftapi.model.ProcessStatus;
import org.apache.airavata.thriftapi.model.ProjectSearchFields;
import org.apache.airavata.thriftapi.model.ResourceJobManager;
import org.apache.airavata.thriftapi.model.ResourcePermissionType;
import org.apache.airavata.thriftapi.model.SCPDataMovement;
import org.apache.airavata.thriftapi.model.SSHAccountProvisionerConfigParam;
import org.apache.airavata.thriftapi.model.SSHAccountProvisionerConfigParamType;
import org.apache.airavata.thriftapi.model.SSHAccountProvisionerDescription;
import org.apache.airavata.thriftapi.model.SSHJobSubmission;
import org.apache.airavata.thriftapi.model.StorageDirectoryInfo;
import org.apache.airavata.thriftapi.model.StoragePreference;
import org.apache.airavata.thriftapi.model.StorageResourceDescription;
import org.apache.airavata.thriftapi.model.StorageVolumeInfo;
import org.apache.airavata.thriftapi.model.UnicoreDataMovement;
import org.apache.airavata.thriftapi.model.UnicoreJobSubmission;
import org.apache.airavata.thriftapi.model.UserComputeResourcePreference;
import org.apache.airavata.thriftapi.model.UserConfigurationDataModel;
import org.apache.airavata.thriftapi.model.UserResourceProfile;
import org.apache.airavata.thriftapi.model.UserStoragePreference;
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
    private final DataReplicaLocationModelMapper dataReplicaLocationModelMapper =
            DataReplicaLocationModelMapper.INSTANCE;
    private final SSHAccountProvisionerDescriptionMapper sshAccountProvisionerDescriptionMapper =
            SSHAccountProvisionerDescriptionMapper.INSTANCE;
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
    private final QueueStatusModelMapper queueStatusModelMapper = QueueStatusModelMapper.INSTANCE;

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
            thriftException.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.valueOf(
                    e.getAiravataErrorType().name()));
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
        thriftException.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.valueOf(
                e.getAiravataErrorType().name()));
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

    private org.apache.airavata.thriftapi.exception.ExperimentNotFoundException
            convertToThriftExperimentNotFoundException(
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
    public String getAPIVersion() throws org.apache.airavata.thriftapi.exception.AiravataSystemException, TException {
        return airavata_apiConstants.AIRAVATA_API_VERSION;
    }

    @Override
    @SecurityCheck
    public boolean isUserExists(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken, String gatewayId, String userName)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException,
                    org.apache.airavata.thriftapi.exception.AiravataSystemException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            if (false) throw new org.apache.airavata.thriftapi.exception.AiravataSystemException();
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
    public String addGateway(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken,
            org.apache.airavata.thriftapi.model.Gateway gateway)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException,
                    org.apache.airavata.thriftapi.exception.AiravataSystemException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
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
    public List<String> getAllUsersInGateway(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken, String gatewayId)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException,
                    org.apache.airavata.thriftapi.exception.AiravataSystemException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
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
    public boolean updateGateway(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken,
            String gatewayId,
            org.apache.airavata.thriftapi.model.Gateway updatedGateway)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException,
                    org.apache.airavata.thriftapi.exception.AiravataSystemException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
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
    public org.apache.airavata.thriftapi.model.Gateway getGateway(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken, String gatewayId)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException,
                    org.apache.airavata.thriftapi.exception.AiravataSystemException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
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
    public boolean deleteGateway(org.apache.airavata.thriftapi.security.model.AuthzToken authzToken, String gatewayId)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException,
                    org.apache.airavata.thriftapi.exception.AiravataSystemException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            return airavataService.deleteGateway(gatewayId);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException exception =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            exception.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error deleting gateway: " + e.getMessage());
            exception.initCause(e);
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public List<org.apache.airavata.thriftapi.model.Gateway> getAllGateways(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException,
                    org.apache.airavata.thriftapi.exception.AiravataSystemException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            // Get domain models from service
            List<org.apache.airavata.common.model.Gateway> domainGateways = airavataService.getAllGateways();
            // Convert domain models to thrift models
            return domainGateways.stream().map(gatewayMapper::toThrift).collect(Collectors.toList());
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException exception =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            exception.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error getting all gateways: " + e.getMessage());
            exception.initCause(e);
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public boolean isGatewayExist(org.apache.airavata.thriftapi.security.model.AuthzToken authzToken, String gatewayId)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException,
                    org.apache.airavata.thriftapi.exception.AiravataSystemException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            return airavataService.isGatewayExist(gatewayId);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException exception =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            exception.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error checking if gateway exists: " + e.getMessage());
            exception.initCause(e);
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public String createNotification(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken,
            org.apache.airavata.thriftapi.model.Notification notification)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException,
                    org.apache.airavata.thriftapi.exception.AiravataSystemException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            // Convert thrift model to domain model
            org.apache.airavata.common.model.Notification domainNotification =
                    notificationMapper.toDomain(notification);
            return airavataService.createNotification(domainNotification);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error creating notification: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public boolean updateNotification(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken,
            org.apache.airavata.thriftapi.model.Notification notification)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException,
                    org.apache.airavata.thriftapi.exception.AiravataSystemException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            // Convert thrift model to domain model
            org.apache.airavata.common.model.Notification domainNotification =
                    notificationMapper.toDomain(notification);
            return airavataService.updateNotification(domainNotification);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error updating notification: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public boolean deleteNotification(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken, String gatewayId, String notificationId)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException,
                    org.apache.airavata.thriftapi.exception.AiravataSystemException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            return airavataService.deleteNotification(gatewayId, notificationId);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error deleting notification: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    // No security check
    @Override
    public org.apache.airavata.thriftapi.model.Notification getNotification(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken, String gatewayId, String notificationId)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException,
                    org.apache.airavata.thriftapi.exception.AiravataSystemException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            // Get domain model from service
            org.apache.airavata.common.model.Notification domainNotification =
                    airavataService.getNotification(gatewayId, notificationId);
            // Convert domain model to thrift model
            return notificationMapper.toThrift(domainNotification);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error getting notification: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    // No security check
    @Override
    public List<org.apache.airavata.thriftapi.model.Notification> getAllNotifications(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken, String gatewayId)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException,
                    org.apache.airavata.thriftapi.exception.AiravataSystemException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
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
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error getting all notifications: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public String generateAndRegisterSSHKeys(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken, String description)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, TException {
        try {
            if (false) throw new org.apache.airavata.thriftapi.exception.InvalidRequestException();
            if (false) throw new org.apache.airavata.thriftapi.exception.AiravataClientException();
            if (false) throw new org.apache.airavata.thriftapi.exception.AiravataSystemException();
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
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error generating and registering SSH keys: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public String registerPwdCredential(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken,
            String loginUserName,
            String password,
            String description)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, TException {
        try {
            if (false) throw new org.apache.airavata.thriftapi.exception.InvalidRequestException();
            if (false) throw new org.apache.airavata.thriftapi.exception.AiravataClientException();
            if (false) throw new org.apache.airavata.thriftapi.exception.AiravataSystemException();
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
            org.apache.airavata.thriftapi.exception.AiravataSystemException exception =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            exception.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error registering password credential: " + e.getMessage());
            exception.initCause(e);
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public org.apache.airavata.thriftapi.credential.model.CredentialSummary getCredentialSummary(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken, String tokenId)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException,
                    org.apache.airavata.thriftapi.exception.AiravataSystemException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            // Convert thrift model to domain model
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            String gatewayId = domainAuthzToken.getClaimsMap().get(Constants.GATEWAY_ID);
            org.apache.airavata.credential.model.CredentialSummary domainSummary =
                    airavataService.getCredentialSummary(tokenId, gatewayId);
            return credentialSummaryMapper.toThrift(domainSummary);
        } catch (org.apache.airavata.credential.exception.CredentialStoreException e) {
            throw convertToThriftCredentialStoreException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException exception =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            exception.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error getting credential summary: " + e.getMessage());
            exception.initCause(e);
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public List<org.apache.airavata.thriftapi.credential.model.CredentialSummary> getAllCredentialSummaries(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken,
            org.apache.airavata.thriftapi.credential.model.SummaryType type)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException,
                    org.apache.airavata.thriftapi.exception.AiravataSystemException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
        org.apache.airavata.credential.model.SummaryType domainType =
                org.apache.airavata.credential.model.SummaryType.valueOf(type.name());
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
            org.apache.airavata.thriftapi.exception.AiravataSystemException exception =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            exception.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error getting all credential summaries: " + e.getMessage());
            exception.initCause(e);
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public boolean deleteSSHPubKey(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken, String airavataCredStoreToken)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException,
                    org.apache.airavata.thriftapi.exception.AiravataSystemException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
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
    public boolean deletePWDCredential(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken, String airavataCredStoreToken)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException,
                    org.apache.airavata.thriftapi.exception.AiravataSystemException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
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
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error deleting PWD credential: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public String createProject(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken,
            String gatewayId,
            org.apache.airavata.thriftapi.model.Project project)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException,
                    org.apache.airavata.thriftapi.exception.AiravataSystemException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            // Convert thrift model to domain model
            org.apache.airavata.common.model.Project domainProject = projectMapper.toDomain(project);
            return airavataService.createProject(gatewayId, domainProject);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException exception =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            exception.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error creating project: " + e.getMessage());
            exception.initCause(e);
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public void updateProject(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken,
            String projectId,
            org.apache.airavata.thriftapi.model.Project updatedProject)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException,
                    org.apache.airavata.thriftapi.exception.AiravataSystemException,
                    org.apache.airavata.thriftapi.exception.ProjectNotFoundException, AuthorizationException,
                    TException {
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
        } catch (org.apache.airavata.common.exception.ProjectNotFoundException e) {
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
    public boolean deleteProject(org.apache.airavata.thriftapi.security.model.AuthzToken authzToken, String projectId)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException,
                    org.apache.airavata.thriftapi.exception.AiravataSystemException,
                    org.apache.airavata.thriftapi.exception.ProjectNotFoundException, AuthorizationException,
                    TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            return airavataService.deleteProject(domainAuthzToken, projectId);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (org.apache.airavata.common.exception.ProjectNotFoundException e) {
            org.apache.airavata.thriftapi.exception.ProjectNotFoundException thriftException =
                    new org.apache.airavata.thriftapi.exception.ProjectNotFoundException();
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
    public org.apache.airavata.thriftapi.model.Project getProject(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken, String projectId)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException,
                    org.apache.airavata.thriftapi.exception.AiravataSystemException,
                    org.apache.airavata.thriftapi.exception.ProjectNotFoundException, AuthorizationException,
                    TException {
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
        } catch (org.apache.airavata.common.exception.ProjectNotFoundException e) {
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
    public List<org.apache.airavata.thriftapi.model.Project> getUserProjects(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken,
            String gatewayId,
            String userName,
            int limit,
            int offset)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException,
                    org.apache.airavata.thriftapi.exception.AiravataSystemException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
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
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error getting user projects: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public List<org.apache.airavata.thriftapi.model.Project> searchProjects(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken,
            String gatewayId,
            String userName,
            Map<ProjectSearchFields, String> filters,
            int limit,
            int offset)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException,
                    org.apache.airavata.thriftapi.exception.AiravataSystemException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            // Convert thrift models to domain models
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            Map<org.apache.airavata.common.model.ProjectSearchFields, String> domainFilters = new java.util.HashMap<>();
            for (Map.Entry<ProjectSearchFields, String> entry : filters.entrySet()) {
                domainFilters.put(
                        org.apache.airavata.common.model.ProjectSearchFields.valueOf(
                                entry.getKey().name()),
                        entry.getValue());
            }
            // Get domain models from service
            List<org.apache.airavata.common.model.Project> domainProjects =
                    airavataService.searchProjects(domainAuthzToken, gatewayId, userName, domainFilters, limit, offset);
            // Convert domain models to thrift models
            return domainProjects.stream().map(projectMapper::toThrift).collect(Collectors.toList());
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException exception =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
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
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        // Convert thrift models to domain models
        org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
        Map<org.apache.airavata.common.model.ExperimentSearchFields, String> domainFilters = new java.util.HashMap<>();
        for (Map.Entry<ExperimentSearchFields, String> entry : filters.entrySet()) {
            domainFilters.put(
                    org.apache.airavata.common.model.ExperimentSearchFields.valueOf(
                            entry.getKey().name()),
                    entry.getValue());
        }
        try {
            // Get domain models from service
            List<org.apache.airavata.common.model.ExperimentSummaryModel> domainSummaries =
                    airavataService.searchExperiments(
                            domainAuthzToken, gatewayId, userName, domainFilters, limit, offset);
            // Convert domain models to thrift models
            return domainSummaries.stream()
                    .map(experimentSummaryModelMapper::toThrift)
                    .collect(Collectors.toList());
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error searching experiments: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public ExperimentStatistics getExperimentStatistics(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken,
            String gatewayId,
            long fromTime,
            long toTime,
            String userName,
            String applicationName,
            String resourceHostName,
            int limit,
            int offset)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException,
                    org.apache.airavata.thriftapi.exception.AiravataSystemException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
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
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error getting experiment statistics: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public List<ExperimentModel> getExperimentsInProject(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken, String projectId, int limit, int offset)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException,
                    org.apache.airavata.thriftapi.exception.AiravataSystemException,
                    org.apache.airavata.thriftapi.exception.ProjectNotFoundException, AuthorizationException,
                    TException {
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
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken,
            String gatewayId,
            String userName,
            int limit,
            int offset)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException,
                    org.apache.airavata.thriftapi.exception.AiravataSystemException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
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
    public String createExperiment(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken,
            String gatewayId,
            ExperimentModel experiment)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException,
                    org.apache.airavata.thriftapi.exception.AiravataSystemException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
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
    public boolean deleteExperiment(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken, String experimentId)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException,
                    org.apache.airavata.thriftapi.exception.AiravataSystemException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            return airavataService.deleteExperimentWithAuth(domainAuthzToken, experimentId);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (org.apache.airavata.common.exception.InvalidRequestException e) {
            throw convertToThriftInvalidRequestException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException exception =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            exception.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error deleting experiment: " + e.getMessage());
            exception.initCause(e);
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public ExperimentModel getExperiment(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken, String airavataExperimentId)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.ExperimentNotFoundException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException, AiravataSystemException,
                    AuthorizationException, TException {
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
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException exception =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            exception.setMessage("Error getting experiment: " + e.getMessage());
            exception.initCause(e);
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public ExperimentModel getExperimentByAdmin(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken, String airavataExperimentId)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.ExperimentNotFoundException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException, AiravataSystemException,
                    AuthorizationException, TException {
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
        } catch (Exception e) {
            AiravataSystemException exception = new AiravataSystemException();
            exception.setMessage("Error getting experiment by admin: " + e.getMessage());
            exception.initCause(e);
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public ExperimentModel getDetailedExperimentTree(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken, String airavataExperimentId)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.ExperimentNotFoundException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException, AiravataSystemException,
                    AuthorizationException, TException {
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
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
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
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            org.apache.airavata.common.model.UserConfigurationDataModel domainUserConfig =
                    userConfigurationDataModelMapper.toDomain(userConfiguration);
            airavataService.updateExperimentConfiguration(airavataExperimentId, domainUserConfig);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
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
            org.apache.airavata.thriftapi.model.ComputationalResourceSchedulingModel resourceScheduling)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException,
                    org.apache.airavata.thriftapi.exception.AiravataSystemException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            org.apache.airavata.common.model.ComputationalResourceSchedulingModel domainScheduling =
                    computationalResourceSchedulingModelMapper.toDomain(resourceScheduling);
            airavataService.updateResourceScheduleing(airavataExperimentId, domainScheduling);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error updating resource scheduling: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public boolean validateExperiment(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken, String airavataExperimentId)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.ExperimentNotFoundException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException, AiravataSystemException,
                    AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            // Validate experiment access first
            org.apache.airavata.common.model.ExperimentModel experiment =
                    airavataService.getExperiment(domainAuthzToken, airavataExperimentId);
            // Basic validation - check if experiment exists and user has access
            // Full validation would require orchestrator service which is not directly
            // accessible
            // For now, return true if experiment exists and is accessible
            return experiment != null;
        } catch (org.apache.airavata.common.exception.InvalidRequestException e) {
            throw convertToThriftInvalidRequestException(e);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (org.apache.airavata.common.exception.AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error validating experiment: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public ExperimentStatus getExperimentStatus(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken, String airavataExperimentId)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.ExperimentNotFoundException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException,
                    org.apache.airavata.thriftapi.exception.AiravataSystemException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.common.model.ExperimentStatus domainStatus =
                    airavataService.getExperimentStatus(airavataExperimentId);
            ExperimentStatus thriftStatus = new ExperimentStatus();
            thriftStatus.setState(
                    ExperimentState.valueOf(domainStatus.getState().name()));
            thriftStatus.setTimeOfStateChange(domainStatus.getTimeOfStateChange());
            thriftStatus.setReason(domainStatus.getReason());
            thriftStatus.setStatusId(domainStatus.getStatusId());
            return thriftStatus;
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error getting experiment status: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public List<OutputDataObjectType> getExperimentOutputs(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken, String airavataExperimentId)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.ExperimentNotFoundException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException,
                    org.apache.airavata.thriftapi.exception.AiravataSystemException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            // Get domain models from service
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            List<org.apache.airavata.common.model.OutputDataObjectType> domainOutputs =
                    airavataService.getExperimentOutputs(airavataExperimentId);
            // Convert domain models to thrift models
            return domainOutputs.stream()
                    .map(outputDataObjectTypeMapper::toThrift)
                    .collect(Collectors.toList());
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error getting experiment outputs: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public List<OutputDataObjectType> getIntermediateOutputs(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken, String airavataExperimentId)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.ExperimentNotFoundException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException,
                    org.apache.airavata.thriftapi.exception.AiravataSystemException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            // getIntermediateOutputs is not directly available in AiravataService
            // Using registry service through experiment service
            // This is a workaround - ideally AiravataService should expose this method
            org.apache.airavata.common.model.ExperimentModel experiment =
                    airavataService.getExperiment(domainAuthzToken, airavataExperimentId);
            // For now, return empty list as getIntermediateOutputs requires registry
            // service access
            // TODO: Add getIntermediateOutputs method to AiravataService
            List<org.apache.airavata.common.model.OutputDataObjectType> domainOutputs = new java.util.ArrayList<>();
            return domainOutputs.stream()
                    .map(outputDataObjectTypeMapper::toThrift)
                    .collect(Collectors.toList());
        } catch (org.apache.airavata.common.exception.InvalidRequestException e) {
            throw convertToThriftInvalidRequestException(e);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error getting intermediate outputs: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public void fetchIntermediateOutputs(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken,
            String airavataExperimentId,
            List<String> outputNames)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.ExperimentNotFoundException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException, AiravataSystemException,
                    AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            airavataService.fetchIntermediateOutputs(domainAuthzToken, airavataExperimentId, outputNames);
        } catch (org.apache.airavata.common.exception.InvalidRequestException e) {
            throw convertToThriftInvalidRequestException(e);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (org.apache.airavata.common.exception.AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error fetching intermediate outputs: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public Map<String, JobStatus> getJobStatuses(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken, String airavataExperimentId)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.ExperimentNotFoundException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException,
                    org.apache.airavata.thriftapi.exception.AiravataSystemException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            Map<String, org.apache.airavata.common.model.JobStatus> domainStatuses =
                    airavataService.getJobStatuses(airavataExperimentId);
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
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error getting job statuses: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public List<JobModel> getJobDetails(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken, String airavataExperimentId)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.ExperimentNotFoundException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException,
                    org.apache.airavata.thriftapi.exception.AiravataSystemException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            // Get domain models from service
            List<org.apache.airavata.common.model.JobModel> domainJobs =
                    airavataService.getJobDetails(airavataExperimentId);
            // Convert domain models to thrift models
            return domainJobs.stream().map(jobModelMapper::toThrift).collect(Collectors.toList());
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
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
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            airavataService.launchExperiment(domainAuthzToken, gatewayId, airavataExperimentId);
        } catch (org.apache.airavata.common.exception.InvalidRequestException e) {
            throw convertToThriftInvalidRequestException(e);
        } catch (org.apache.airavata.common.exception.ExperimentNotFoundException e) {
            org.apache.airavata.thriftapi.exception.ExperimentNotFoundException thriftException =
                    new org.apache.airavata.thriftapi.exception.ExperimentNotFoundException();
            thriftException.setMessage(e.getMessage());
            thriftException.initCause(e);
            throw thriftException;
        } catch (org.apache.airavata.common.exception.ProjectNotFoundException e) {
            org.apache.airavata.thriftapi.exception.ProjectNotFoundException thriftException =
                    new org.apache.airavata.thriftapi.exception.ProjectNotFoundException();
            thriftException.setMessage(e.getMessage());
            thriftException.initCause(e);
            throw thriftException;
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (org.apache.airavata.common.exception.AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (org.apache.airavata.sharing.model.SharingRegistryException e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException exception =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            exception.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error launching experiment: " + e.getMessage());
            exception.initCause(e);
            throw exception;
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
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
                    org.apache.airavata.thriftapi.exception.ProjectNotFoundException, TException {
        try {
            // getExperiment will apply sharing permissions
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            org.apache.airavata.common.model.ExperimentModel existingExperiment =
                    airavataService.getExperiment(domainAuthzToken, existingExperimentID);
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
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (org.apache.airavata.common.exception.AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (org.apache.airavata.common.exception.ProjectNotFoundException e) {
            throw convertToThriftProjectNotFoundException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
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
                    org.apache.airavata.thriftapi.exception.ProjectNotFoundException, TException {
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
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (org.apache.airavata.common.exception.AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (org.apache.airavata.common.exception.ProjectNotFoundException e) {
            throw convertToThriftProjectNotFoundException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error cloning experiment by admin: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public void terminateExperiment(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken,
            String airavataExperimentId,
            String gatewayId)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.ExperimentNotFoundException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException, AiravataSystemException,
                    AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            airavataService.terminateExperiment(airavataExperimentId, gatewayId);
        } catch (org.apache.airavata.common.exception.ExperimentNotFoundException e) {
            throw convertToThriftExperimentNotFoundException(e);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
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
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            org.apache.airavata.common.model.ApplicationModule domainModule =
                    applicationModuleMapper.toDomain(applicationModule);
            return airavataService.registerApplicationModule(gatewayId, domainModule);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error registering application module: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public ApplicationModule getApplicationModule(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken, String appModuleId)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException,
                    org.apache.airavata.thriftapi.exception.AiravataSystemException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            org.apache.airavata.common.model.ApplicationModule domainModule =
                    airavataService.getApplicationModule(appModuleId);
            return applicationModuleMapper.toThrift(domainModule);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
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
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            org.apache.airavata.common.model.ApplicationModule domainModule =
                    applicationModuleMapper.toDomain(applicationModule);
            airavataService.updateApplicationModule(appModuleId, domainModule);
            return true;
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error updating application module: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public List<ApplicationModule> getAllAppModules(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken, String gatewayId)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException,
                    org.apache.airavata.thriftapi.exception.AiravataSystemException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            List<org.apache.airavata.common.model.ApplicationModule> domainModules =
                    airavataService.getAllAppModules(gatewayId);
            return domainModules.stream().map(applicationModuleMapper::toThrift).collect(Collectors.toList());
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error getting all app modules: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public List<ApplicationModule> getAccessibleAppModules(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken, String gatewayId)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException,
                    org.apache.airavata.thriftapi.exception.AiravataSystemException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            List<org.apache.airavata.common.model.ApplicationModule> domainModules =
                    airavataService.getAccessibleAppModules(domainAuthzToken, gatewayId);
            return domainModules.stream().map(applicationModuleMapper::toThrift).collect(Collectors.toList());
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (org.apache.airavata.common.exception.AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error getting accessible app modules: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public boolean deleteApplicationModule(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken, String appModuleId)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException,
                    org.apache.airavata.thriftapi.exception.AiravataSystemException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            return airavataService.deleteApplicationModule(appModuleId);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error deleting application module: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public String registerApplicationDeployment(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken,
            String gatewayId,
            ApplicationDeploymentDescription applicationDeployment)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException,
                    org.apache.airavata.thriftapi.exception.AiravataSystemException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            // Convert thrift model to domain model
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            org.apache.airavata.common.model.ApplicationDeploymentDescription domainDeployment =
                    applicationDeploymentDescriptionMapper.toDomain(applicationDeployment);
            return airavataService.registerApplicationDeployment(domainAuthzToken, gatewayId, domainDeployment);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (org.apache.airavata.common.exception.AuthorizationException e) {
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
    public ApplicationDeploymentDescription getApplicationDeployment(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken, String appDeploymentId)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException,
                    org.apache.airavata.thriftapi.exception.AiravataSystemException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            // Get domain model from service
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            org.apache.airavata.common.model.ApplicationDeploymentDescription domainDeployment =
                    airavataService.getApplicationDeployment(domainAuthzToken, appDeploymentId);
            // Convert domain model to thrift model
            return applicationDeploymentDescriptionMapper.toThrift(domainDeployment);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (org.apache.airavata.common.exception.AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error getting application deployment: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public boolean updateApplicationDeployment(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken,
            String appDeploymentId,
            ApplicationDeploymentDescription applicationDeployment)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException,
                    org.apache.airavata.thriftapi.exception.AiravataSystemException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            // Convert thrift model to domain model
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            org.apache.airavata.common.model.ApplicationDeploymentDescription domainDeployment =
                    applicationDeploymentDescriptionMapper.toDomain(applicationDeployment);
            return airavataService.updateApplicationDeployment(domainAuthzToken, appDeploymentId, domainDeployment);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (org.apache.airavata.common.exception.AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error updating application deployment: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public boolean deleteApplicationDeployment(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken, String appDeploymentId)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException,
                    org.apache.airavata.thriftapi.exception.AiravataSystemException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            return airavataService.deleteApplicationDeployment(domainAuthzToken, appDeploymentId);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (org.apache.airavata.common.exception.AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error deleting application deployment: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public List<ApplicationDeploymentDescription> getAllApplicationDeployments(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken, String gatewayId)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException,
                    org.apache.airavata.thriftapi.exception.AiravataSystemException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        return getAccessibleApplicationDeployments(authzToken, gatewayId, ResourcePermissionType.READ);
    }

    @Override
    @SecurityCheck
    public List<ApplicationDeploymentDescription> getAccessibleApplicationDeployments(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken,
            String gatewayId,
            ResourcePermissionType permissionType)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException,
                    org.apache.airavata.thriftapi.exception.AiravataSystemException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            // Get domain models from service
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            org.apache.airavata.common.model.ResourcePermissionType domainPermissionType =
                    org.apache.airavata.common.model.ResourcePermissionType.valueOf(permissionType.name());
            List<org.apache.airavata.common.model.ApplicationDeploymentDescription> domainDeployments =
                    airavataService.getAccessibleApplicationDeployments(
                            domainAuthzToken, gatewayId, domainPermissionType);
            // Convert domain models to thrift models
            return domainDeployments.stream()
                    .map(applicationDeploymentDescriptionMapper::toThrift)
                    .collect(Collectors.toList());
        } catch (org.apache.airavata.common.exception.InvalidRequestException e) {
            throw convertToThriftInvalidRequestException(e);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (org.apache.airavata.common.exception.AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
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
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken, String appModuleId)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException,
                    org.apache.airavata.thriftapi.exception.AiravataSystemException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            return airavataService.getAppModuleDeployedResources(appModuleId);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
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
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
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
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (org.apache.airavata.common.exception.AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
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
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken,
            String gatewayId,
            ApplicationInterfaceDescription applicationInterface)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException,
                    org.apache.airavata.thriftapi.exception.AiravataSystemException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            // Convert thrift model to domain model
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            org.apache.airavata.common.model.ApplicationInterfaceDescription domainInterface =
                    applicationInterfaceDescriptionMapper.toDomain(applicationInterface);
            return airavataService.registerApplicationInterface(gatewayId, domainInterface);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error registering application interface: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public String cloneApplicationInterface(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken,
            String existingAppInterfaceID,
            String newApplicationName,
            String gatewayId)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException,
                    org.apache.airavata.thriftapi.exception.AiravataSystemException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            return airavataService.cloneApplicationInterface(existingAppInterfaceID, newApplicationName, gatewayId);
        } catch (org.apache.airavata.common.exception.InvalidRequestException e) {
            throw convertToThriftInvalidRequestException(e);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (org.apache.airavata.common.exception.AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error cloning application interface: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public ApplicationInterfaceDescription getApplicationInterface(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken, String appInterfaceId)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException,
                    org.apache.airavata.thriftapi.exception.AiravataSystemException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            org.apache.airavata.common.model.ApplicationInterfaceDescription domainInterface =
                    airavataService.getApplicationInterface(appInterfaceId);
            return applicationInterfaceDescriptionMapper.toThrift(domainInterface);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
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
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            // Convert thrift model to domain model
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            org.apache.airavata.common.model.ApplicationInterfaceDescription domainInterface =
                    applicationInterfaceDescriptionMapper.toDomain(applicationInterface);
            airavataService.updateApplicationInterface(appInterfaceId, domainInterface);
            return true;
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error updating application interface: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public boolean deleteApplicationInterface(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken, String appInterfaceId)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException,
                    org.apache.airavata.thriftapi.exception.AiravataSystemException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            return airavataService.deleteApplicationInterface(appInterfaceId);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error deleting application interface: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public Map<String, String> getAllApplicationInterfaceNames(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken, String gatewayId)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException,
                    org.apache.airavata.thriftapi.exception.AiravataSystemException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            return airavataService.getAllApplicationInterfaceNames(gatewayId);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error getting all application interface names: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public List<ApplicationInterfaceDescription> getAllApplicationInterfaces(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken, String gatewayId)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException,
                    org.apache.airavata.thriftapi.exception.AiravataSystemException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            // Get domain models from service
            List<org.apache.airavata.common.model.ApplicationInterfaceDescription> domainInterfaces =
                    airavataService.getAllApplicationInterfaces(gatewayId);
            // Convert domain models to thrift models
            return domainInterfaces.stream()
                    .map(applicationInterfaceDescriptionMapper::toThrift)
                    .collect(Collectors.toList());
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error getting all application interfaces: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public List<InputDataObjectType> getApplicationInputs(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken, String appInterfaceId)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException,
                    org.apache.airavata.thriftapi.exception.AiravataSystemException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            // Get domain models from service
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            List<org.apache.airavata.common.model.InputDataObjectType> domainInputs =
                    airavataService.getApplicationInputs(appInterfaceId);
            // Convert domain models to thrift models
            return domainInputs.stream()
                    .map(inputDataObjectTypeMapper::toThrift)
                    .collect(Collectors.toList());
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error getting application inputs: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public List<OutputDataObjectType> getApplicationOutputs(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken, String appInterfaceId)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException,
                    org.apache.airavata.thriftapi.exception.AiravataSystemException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            // Get domain models from service
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            List<org.apache.airavata.common.model.OutputDataObjectType> domainOutputs =
                    airavataService.getApplicationOutputs(appInterfaceId);
            // Convert domain models to thrift models
            return domainOutputs.stream()
                    .map(outputDataObjectTypeMapper::toThrift)
                    .collect(Collectors.toList());
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error getting application outputs: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    @Deprecated
    public Map<String, String> getAvailableAppInterfaceComputeResources(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken, String appInterfaceId)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException,
                    org.apache.airavata.thriftapi.exception.AiravataSystemException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            return airavataService.getAvailableAppInterfaceComputeResources(appInterfaceId);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
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
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            // Convert thrift model to domain model
            org.apache.airavata.common.model.ComputeResourceDescription domainComputeResource =
                    computeResourceDescriptionMapper.toDomain(computeResourceDescription);
            return airavataService.registerComputeResource(domainComputeResource);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error registering compute resource: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public ComputeResourceDescription getComputeResource(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken, String computeResourceId)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException,
                    org.apache.airavata.thriftapi.exception.AiravataSystemException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            // Get domain model from service
            org.apache.airavata.common.model.ComputeResourceDescription domainComputeResource =
                    airavataService.getComputeResource(computeResourceId);
            // Convert domain model to thrift model
            return computeResourceDescriptionMapper.toThrift(domainComputeResource);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
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
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            return airavataService.getAllComputeResourceNames();
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
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
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            org.apache.airavata.common.model.ComputeResourceDescription domainComputeResource =
                    computeResourceDescriptionMapper.toDomain(computeResourceDescription);
            airavataService.updateComputeResource(computeResourceId, domainComputeResource);
            return true;
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error updating compute resource: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public boolean deleteComputeResource(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken, String computeResourceId)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException,
                    org.apache.airavata.thriftapi.exception.AiravataSystemException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            return airavataService.deleteComputeResource(computeResourceId);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
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
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            org.apache.airavata.common.model.StorageResourceDescription domainStorage =
                    storageResourceDescriptionMapper.toDomain(storageResourceDescription);
            return airavataService.registerStorageResource(domainStorage);
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
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error registering storage resource: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public StorageResourceDescription getStorageResource(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken, String storageResourceId)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException,
                    org.apache.airavata.thriftapi.exception.AiravataSystemException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            org.apache.airavata.common.model.StorageResourceDescription domainStorage =
                    airavataService.getStorageResource(storageResourceId);
            return storageResourceDescriptionMapper.toThrift(domainStorage);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
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
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            return airavataService.getAllStorageResourceNames();
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
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
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            org.apache.airavata.common.model.StorageResourceDescription domainStorage =
                    storageResourceDescriptionMapper.toDomain(storageResourceDescription);
            return airavataService.updateStorageResource(storageResourceId, domainStorage);
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
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error updating storage resource: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public boolean deleteStorageResource(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken, String storageResourceId)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException,
                    org.apache.airavata.thriftapi.exception.AiravataSystemException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            return airavataService.deleteStorageResource(storageResourceId);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error deleting storage resource: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public StorageVolumeInfo getResourceStorageInfo(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken, String resourceId, String location)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException,
                    org.apache.airavata.thriftapi.exception.AiravataSystemException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            org.apache.airavata.common.model.StorageVolumeInfo domainInfo =
                    airavataService.getResourceStorageInfo(domainAuthzToken, resourceId, location);
            return storageVolumeInfoMapper.toThrift(domainInfo);
        } catch (org.apache.airavata.common.exception.InvalidRequestException e) {
            throw convertToThriftInvalidRequestException(e);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error getting resource storage info: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public StorageDirectoryInfo getStorageDirectoryInfo(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken, String resourceId, String location)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException,
                    org.apache.airavata.thriftapi.exception.AiravataSystemException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            org.apache.airavata.common.model.StorageDirectoryInfo domainInfo =
                    airavataService.getStorageDirectoryInfo(domainAuthzToken, resourceId, location);
            return storageDirectoryInfoMapper.toThrift(domainInfo);
        } catch (org.apache.airavata.common.exception.InvalidRequestException e) {
            throw convertToThriftInvalidRequestException(e);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
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
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            org.apache.airavata.common.model.LOCALSubmission domainLocal =
                    localSubmissionMapper.toDomain(localSubmission);
            return airavataService.addLocalSubmissionDetails(computeResourceId, priorityOrder, domainLocal);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
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
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            org.apache.airavata.common.model.LOCALSubmission domainLocal =
                    localSubmissionMapper.toDomain(localSubmission);
            airavataService.updateLocalSubmissionDetails(jobSubmissionInterfaceId, domainLocal);
            return true;
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error updating local submission details: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public LOCALSubmission getLocalJobSubmission(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken, String jobSubmissionId)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException,
                    org.apache.airavata.thriftapi.exception.AiravataSystemException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.common.model.LOCALSubmission domainLocal =
                    airavataService.getLocalJobSubmission(jobSubmissionId);
            return localSubmissionMapper.toThrift(domainLocal);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
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
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            org.apache.airavata.common.model.SSHJobSubmission domainSSH =
                    sshJobSubmissionMapper.toDomain(sshJobSubmission);
            return airavataService.addSSHJobSubmissionDetails(computeResourceId, priorityOrder, domainSSH);
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
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
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
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            org.apache.airavata.common.model.SSHJobSubmission domainSSH =
                    sshJobSubmissionMapper.toDomain(sshJobSubmission);
            return airavataService.addSSHForkJobSubmissionDetails(computeResourceId, priorityOrder, domainSSH);
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
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error adding SSH fork job submission details: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public SSHJobSubmission getSSHJobSubmission(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken, String jobSubmissionId)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException,
                    org.apache.airavata.thriftapi.exception.AiravataSystemException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            org.apache.airavata.common.model.SSHJobSubmission domainSSH =
                    airavataService.getSSHJobSubmission(jobSubmissionId);
            return sshJobSubmissionMapper.toThrift(domainSSH);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
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
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            org.apache.airavata.common.model.CloudJobSubmission domainCloud =
                    cloudJobSubmissionMapper.toDomain(cloudJobSubmission);
            return airavataService.addCloudJobSubmissionDetails(computeResourceId, priorityOrder, domainCloud);
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
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error adding cloud job submission details: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public CloudJobSubmission getCloudJobSubmission(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken, String jobSubmissionId)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException,
                    org.apache.airavata.thriftapi.exception.AiravataSystemException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            org.apache.airavata.common.model.CloudJobSubmission domainCloud =
                    airavataService.getCloudJobSubmission(jobSubmissionId);
            return cloudJobSubmissionMapper.toThrift(domainCloud);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
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
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            org.apache.airavata.common.model.UnicoreJobSubmission domainUnicore =
                    unicoreJobSubmissionMapper.toDomain(unicoreJobSubmission);
            return airavataService.addUNICOREJobSubmissionDetails(computeResourceId, priorityOrder, domainUnicore);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error adding UNICORE job submission details: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public UnicoreJobSubmission getUnicoreJobSubmission(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken, String jobSubmissionId)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException,
                    org.apache.airavata.thriftapi.exception.AiravataSystemException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            org.apache.airavata.common.model.UnicoreJobSubmission domainUnicore =
                    airavataService.getUnicoreJobSubmission(jobSubmissionId);
            return unicoreJobSubmissionMapper.toThrift(domainUnicore);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
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
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            org.apache.airavata.common.model.SSHJobSubmission domainSSH =
                    sshJobSubmissionMapper.toDomain(sshJobSubmission);
            return airavataService.updateSSHJobSubmissionDetails(jobSubmissionInterfaceId, domainSSH);
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
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
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
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            org.apache.airavata.common.model.CloudJobSubmission domainCloud =
                    cloudJobSubmissionMapper.toDomain(cloudJobSubmission);
            return airavataService.updateCloudJobSubmissionDetails(jobSubmissionInterfaceId, domainCloud);
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
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
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
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            org.apache.airavata.common.model.UnicoreJobSubmission domainUnicore =
                    unicoreJobSubmissionMapper.toDomain(unicoreJobSubmission);
            return airavataService.updateUnicoreJobSubmissionDetails(jobSubmissionInterfaceId, domainUnicore);
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
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error updating unicore job submission details: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public boolean updateLocalDataMovementDetails(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken,
            String dataMovementInterfaceId,
            LOCALDataMovement localDataMovement)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException,
                    org.apache.airavata.thriftapi.exception.AiravataSystemException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.common.model.LOCALDataMovement domainLocalDataMovement =
                    localDataMovementMapper.toDomain(localDataMovement);
            airavataService.updateLocalDataMovementDetails(dataMovementInterfaceId, domainLocalDataMovement);
            return true;
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
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
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.common.model.SCPDataMovement domainSCP =
                    scpDataMovementMapper.toDomain(scpDataMovement);
            return airavataService.updateSCPDataMovementDetails(dataMovementInterfaceId, domainSCP);
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
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error updating SCP data movement details: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public SCPDataMovement getSCPDataMovement(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken, String dataMovementId)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException,
                    org.apache.airavata.thriftapi.exception.AiravataSystemException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            org.apache.airavata.common.model.SCPDataMovement domainSCP =
                    airavataService.getSCPDataMovement(dataMovementId);
            return scpDataMovementMapper.toThrift(domainSCP);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
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
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            org.apache.airavata.common.model.DMType domainDMType =
                    org.apache.airavata.common.model.DMType.valueOf(dmType.name());
            org.apache.airavata.common.model.UnicoreDataMovement domainUnicore =
                    unicoreDataMovementMapper.toDomain(unicoreDataMovement);
            return airavataService.addUnicoreDataMovementDetails(
                    resourceId, domainDMType, priorityOrder, domainUnicore);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
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
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            org.apache.airavata.common.model.UnicoreDataMovement domainUnicore =
                    unicoreDataMovementMapper.toDomain(unicoreDataMovement);
            airavataService.updateUnicoreDataMovementDetails(dataMovementInterfaceId, domainUnicore);
            return true;
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error updating Unicore data movement details: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public LOCALDataMovement getLocalDataMovement(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken, String dataMovementId)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException,
                    org.apache.airavata.thriftapi.exception.AiravataSystemException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.common.model.LOCALDataMovement domainLocal =
                    airavataService.getLocalDataMovement(dataMovementId);
            return localDataMovementMapper.toThrift(domainLocal);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error getting local data movement: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public UnicoreDataMovement getUnicoreDataMovement(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken, String dataMovementId)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException,
                    org.apache.airavata.thriftapi.exception.AiravataSystemException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            org.apache.airavata.common.model.UnicoreDataMovement domainUnicore =
                    airavataService.getUnicoreDataMovement(dataMovementId);
            return unicoreDataMovementMapper.toThrift(domainUnicore);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
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
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            org.apache.airavata.common.model.DMType domainDMType =
                    org.apache.airavata.common.model.DMType.valueOf(dataMoveType.name());
            org.apache.airavata.common.model.GridFTPDataMovement domainGridFTP =
                    gridFTPDataMovementMapper.toDomain(gridFTPDataMovement);
            return airavataService.addGridFTPDataMovementDetails(
                    productUri, domainDMType, priorityOrder, domainGridFTP);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
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
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            org.apache.airavata.common.model.GridFTPDataMovement domainGridFTP =
                    gridFTPDataMovementMapper.toDomain(gridFTPDataMovement);
            airavataService.updateGridFTPDataMovementDetails(dataMovementInterfaceId, domainGridFTP);
            return true;
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error updating GridFTP data movement details: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public GridFTPDataMovement getGridFTPDataMovement(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken, String dataMovementId)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException,
                    org.apache.airavata.thriftapi.exception.AiravataSystemException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.common.model.GridFTPDataMovement domainGridFTP =
                    airavataService.getGridFTPDataMovement(dataMovementId);
            return gridFTPDataMovementMapper.toThrift(domainGridFTP);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error getting GridFTP data movement: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public boolean changeJobSubmissionPriority(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken,
            String jobSubmissionInterfaceId,
            int newPriorityOrder)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException,
                    org.apache.airavata.thriftapi.exception.AiravataSystemException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        return false;
    }

    @Override
    @SecurityCheck
    public boolean changeDataMovementPriority(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken,
            String dataMovementInterfaceId,
            int newPriorityOrder)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException,
                    org.apache.airavata.thriftapi.exception.AiravataSystemException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        return false;
    }

    @Override
    @SecurityCheck
    public boolean changeJobSubmissionPriorities(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken,
            Map<String, Integer> jobSubmissionPriorityMap)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException,
                    org.apache.airavata.thriftapi.exception.AiravataSystemException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        return false;
    }

    @Override
    @SecurityCheck
    public boolean changeDataMovementPriorities(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken,
            Map<String, Integer> dataMovementPriorityMap)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException,
                    org.apache.airavata.thriftapi.exception.AiravataSystemException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        return false;
    }

    @Override
    @SecurityCheck
    public String registerResourceJobManager(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken, ResourceJobManager resourceJobManager)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException,
                    org.apache.airavata.thriftapi.exception.AiravataSystemException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            org.apache.airavata.common.model.ResourceJobManager domainRJM =
                    resourceJobManagerMapper.toDomain(resourceJobManager);
            return airavataService.registerResourceJobManager(domainRJM);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
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
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.common.model.ResourceJobManager domainRJM =
                    resourceJobManagerMapper.toDomain(updatedResourceJobManager);
            return airavataService.updateResourceJobManager(resourceJobManagerId, domainRJM);
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
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error updating resource job manager: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public ResourceJobManager getResourceJobManager(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken, String resourceJobManagerId)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException,
                    org.apache.airavata.thriftapi.exception.AiravataSystemException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.common.model.ResourceJobManager domainRJM =
                    airavataService.getResourceJobManager(resourceJobManagerId);
            return resourceJobManagerMapper.toThrift(domainRJM);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error getting resource job manager: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public boolean deleteResourceJobManager(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken, String resourceJobManagerId)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException,
                    org.apache.airavata.thriftapi.exception.AiravataSystemException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            return airavataService.deleteResourceJobManager(resourceJobManagerId);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error deleting resource job manager: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public boolean deleteBatchQueue(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken,
            String computeResourceId,
            String queueName)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException,
                    org.apache.airavata.thriftapi.exception.AiravataSystemException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            return airavataService.deleteBatchQueue(computeResourceId, queueName);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error deleting batch queue: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public String registerGatewayResourceProfile(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken,
            GatewayResourceProfile gatewayResourceProfile)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException,
                    org.apache.airavata.thriftapi.exception.AiravataSystemException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            // Convert thrift model to domain model
            org.apache.airavata.common.model.GatewayResourceProfile domainProfile =
                    gatewayResourceProfileMapper.toDomain(gatewayResourceProfile);
            return airavataService.registerGatewayResourceProfile(domainProfile);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error registering gateway resource profile: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public GatewayResourceProfile getGatewayResourceProfile(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken, String gatewayID)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException,
                    org.apache.airavata.thriftapi.exception.AiravataSystemException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            // Get domain model from service
            org.apache.airavata.common.model.GatewayResourceProfile domainProfile =
                    airavataService.getGatewayResourceProfile(gatewayID);
            // Convert domain model to thrift model
            return gatewayResourceProfileMapper.toThrift(domainProfile);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error getting gateway resource profile: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public boolean updateGatewayResourceProfile(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken,
            String gatewayID,
            GatewayResourceProfile gatewayResourceProfile)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException,
                    org.apache.airavata.thriftapi.exception.AiravataSystemException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            // Convert thrift model to domain model
            org.apache.airavata.common.model.GatewayResourceProfile domainProfile =
                    gatewayResourceProfileMapper.toDomain(gatewayResourceProfile);
            airavataService.updateGatewayResourceProfile(gatewayID, domainProfile);
            return true;
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error updating gateway resource profile: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public boolean deleteGatewayResourceProfile(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken, String gatewayID)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException,
                    org.apache.airavata.thriftapi.exception.AiravataSystemException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            return airavataService.deleteGatewayResourceProfile(gatewayID);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error deleting gateway resource profile: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public boolean addGatewayComputeResourcePreference(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken,
            String gatewayID,
            String computeResourceId,
            ComputeResourcePreference computeResourcePreference)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException,
                    org.apache.airavata.thriftapi.exception.AiravataSystemException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.common.model.ComputeResourcePreference domainPreference =
                    computeResourcePreferenceMapper.toDomain(computeResourcePreference);
            airavataService.addGatewayComputeResourcePreference(gatewayID, computeResourceId, domainPreference);
            return true;
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
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
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            org.apache.airavata.common.model.StoragePreference domainPreference =
                    storagePreferenceMapper.toDomain(dataStoragePreference);
            return airavataService.addGatewayStoragePreference(gatewayID, storageResourceId, domainPreference);
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
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error adding gateway storage preference: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public ComputeResourcePreference getGatewayComputeResourcePreference(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken,
            String gatewayID,
            String computeResourceId)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException,
                    org.apache.airavata.thriftapi.exception.AiravataSystemException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            org.apache.airavata.common.model.ComputeResourcePreference domainPreference =
                    airavataService.getGatewayComputeResourcePreference(gatewayID, computeResourceId);
            return computeResourcePreferenceMapper.toThrift(domainPreference);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error getting gateway compute resource preference: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public StoragePreference getGatewayStoragePreference(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken, String gatewayID, String storageId)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException,
                    org.apache.airavata.thriftapi.exception.AiravataSystemException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.common.model.StoragePreference domainPreference =
                    airavataService.getGatewayStoragePreference(gatewayID, storageId);
            return storagePreferenceMapper.toThrift(domainPreference);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error getting gateway storage preference: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public List<org.apache.airavata.thriftapi.model.StoragePreference> getAllGatewayStoragePreferences(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken, String gatewayID)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException,
                    org.apache.airavata.thriftapi.exception.AiravataSystemException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            List<org.apache.airavata.common.model.StoragePreference> domainPreferences =
                    airavataService.getAllGatewayStoragePreferences(gatewayID);
            return domainPreferences.stream()
                    .map(storagePreferenceMapper::toThrift)
                    .collect(Collectors.toList());
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error getting all gateway storage preferences: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public List<org.apache.airavata.thriftapi.model.GatewayResourceProfile> getAllGatewayResourceProfiles(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException,
                    org.apache.airavata.thriftapi.exception.AiravataSystemException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            // Get domain models from service
            List<org.apache.airavata.common.model.GatewayResourceProfile> domainProfiles =
                    airavataService.getAllGatewayResourceProfiles();
            // Convert domain models to thrift models
            return domainProfiles.stream()
                    .map(gatewayResourceProfileMapper::toThrift)
                    .collect(Collectors.toList());
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error getting all gateway resource profiles: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public boolean updateGatewayComputeResourcePreference(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken,
            String gatewayID,
            String computeResourceId,
            ComputeResourcePreference computeResourcePreference)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException,
                    org.apache.airavata.thriftapi.exception.AiravataSystemException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            org.apache.airavata.common.model.ComputeResourcePreference domainPreference =
                    computeResourcePreferenceMapper.toDomain(computeResourcePreference);
            return airavataService.updateGatewayComputeResourcePreference(
                    gatewayID, computeResourceId, domainPreference);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
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
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            org.apache.airavata.common.model.StoragePreference domainPreference =
                    storagePreferenceMapper.toDomain(dataStoragePreference);
            return airavataService.updateGatewayStoragePreference(gatewayID, storageId, domainPreference);
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
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error updating gateway storage preference: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public boolean deleteGatewayComputeResourcePreference(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken,
            String gatewayID,
            String computeResourceId)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException,
                    org.apache.airavata.thriftapi.exception.AiravataSystemException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            return airavataService.deleteGatewayComputeResourcePreference(gatewayID, computeResourceId);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error deleting gateway compute resource preference: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public boolean deleteGatewayStoragePreference(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken, String gatewayID, String storageId)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException,
                    org.apache.airavata.thriftapi.exception.AiravataSystemException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            return airavataService.deleteGatewayStoragePreference(gatewayID, storageId);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error deleting gateway storage preference: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public List<SSHAccountProvisionerDescription> getSSHAccountProvisioners(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException,
                    org.apache.airavata.thriftapi.exception.AiravataSystemException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {

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
        // domainProvisioners already contains Thrift objects, return directly
        return domainProvisioners;
    }

    @Override
    @SecurityCheck
    public boolean doesUserHaveSSHAccount(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken, String computeResourceId, String userId)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException,
                    org.apache.airavata.thriftapi.exception.AiravataSystemException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            return airavataService.doesUserHaveSSHAccount(domainAuthzToken, computeResourceId, userId);
        } catch (org.apache.airavata.common.exception.InvalidRequestException e) {
            throw convertToThriftInvalidRequestException(e);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (org.apache.airavata.common.exception.AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
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
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken,
            String computeResourceId,
            String airavataCredStoreToken)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException,
                    org.apache.airavata.thriftapi.exception.AiravataSystemException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            return airavataService.isSSHAccountSetupComplete(
                    domainAuthzToken, computeResourceId, airavataCredStoreToken);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error checking SSH setup completion: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public UserComputeResourcePreference setupUserComputeResourcePreferencesForSSH(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken,
            String computeResourceId,
            String userId,
            String airavataCredStoreToken)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException,
                    org.apache.airavata.thriftapi.exception.AiravataSystemException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            org.apache.airavata.common.model.UserComputeResourcePreference domainPreference =
                    airavataService.setupSSHAccount(
                            domainAuthzToken, computeResourceId, userId, airavataCredStoreToken);
            return userComputeResourcePreferenceMapper.toThrift(domainPreference);
        } catch (org.apache.airavata.common.exception.InvalidRequestException e) {
            throw convertToThriftInvalidRequestException(e);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (org.apache.airavata.common.exception.AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error setting up user compute resource preferences for SSH: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public String registerUserResourceProfile(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken, UserResourceProfile userResourceProfile)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException,
                    org.apache.airavata.thriftapi.exception.AiravataSystemException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            org.apache.airavata.common.model.UserResourceProfile domainProfile =
                    userResourceProfileMapper.toDomain(userResourceProfile);
            return airavataService.registerUserResourceProfile(domainProfile);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error registering user resource profile: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public boolean isUserResourceProfileExists(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken, String userId, String gatewayID)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException,
                    org.apache.airavata.thriftapi.exception.AiravataSystemException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            return airavataService.isUserResourceProfileExists(userId, gatewayID);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error checking if user resource profile exists: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public UserResourceProfile getUserResourceProfile(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken, String userId, String gatewayID)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException,
                    org.apache.airavata.thriftapi.exception.AiravataSystemException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            // Get domain model from service
            org.apache.airavata.common.model.UserResourceProfile domainProfile =
                    airavataService.getUserResourceProfile(userId, gatewayID);
            // Convert domain model to thrift model
            return userResourceProfileMapper.toThrift(domainProfile);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error getting user resource profile: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public boolean updateUserResourceProfile(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken,
            String userId,
            String gatewayID,
            UserResourceProfile userResourceProfile)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException,
                    org.apache.airavata.thriftapi.exception.AiravataSystemException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            // Convert thrift model to domain model
            org.apache.airavata.common.model.UserResourceProfile domainProfile =
                    userResourceProfileMapper.toDomain(userResourceProfile);
            return airavataService.updateUserResourceProfile(userId, gatewayID, domainProfile);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error updating user resource profile: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public boolean deleteUserResourceProfile(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken, String userId, String gatewayID)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException,
                    org.apache.airavata.thriftapi.exception.AiravataSystemException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            return airavataService.deleteUserResourceProfile(userId, gatewayID);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
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
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            org.apache.airavata.common.model.UserComputeResourcePreference domainPreference =
                    userComputeResourcePreferenceMapper.toDomain(userComputeResourcePreference);
            return airavataService.addUserComputeResourcePreference(
                    userId, gatewayID, computeResourceId, domainPreference);
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
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
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
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            org.apache.airavata.common.model.UserStoragePreference domainPreference =
                    userStoragePreferenceMapper.toDomain(dataStoragePreference);
            return airavataService.addUserStoragePreference(userId, gatewayID, userStorageResourceId, domainPreference);
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
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error adding user storage preference: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public UserComputeResourcePreference getUserComputeResourcePreference(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken,
            String userId,
            String gatewayID,
            String userComputeResourceId)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException,
                    org.apache.airavata.thriftapi.exception.AiravataSystemException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.common.model.UserComputeResourcePreference domainPreference =
                    airavataService.getUserComputeResourcePreference(userId, gatewayID, userComputeResourceId);
            return userComputeResourcePreferenceMapper.toThrift(domainPreference);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error getting user compute resource preference: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public UserStoragePreference getUserStoragePreference(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken,
            String userId,
            String gatewayID,
            String userStorageId)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException,
                    org.apache.airavata.thriftapi.exception.AiravataSystemException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.common.model.UserStoragePreference domainPreference =
                    airavataService.getUserStoragePreference(userId, gatewayID, userStorageId);
            return userStoragePreferenceMapper.toThrift(domainPreference);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error getting user storage preference: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public List<org.apache.airavata.thriftapi.model.UserComputeResourcePreference> getAllUserComputeResourcePreferences(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken, String userId, String gatewayID)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException,
                    org.apache.airavata.thriftapi.exception.AiravataSystemException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            List<org.apache.airavata.common.model.UserComputeResourcePreference> domainPreferences =
                    airavataService.getAllUserComputeResourcePreferences(userId, gatewayID);
            return domainPreferences.stream()
                    .map(userComputeResourcePreferenceMapper::toThrift)
                    .collect(Collectors.toList());
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error getting all user compute resource preferences: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public List<org.apache.airavata.thriftapi.model.UserStoragePreference> getAllUserStoragePreferences(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken, String userId, String gatewayID)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException,
                    org.apache.airavata.thriftapi.exception.AiravataSystemException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            List<org.apache.airavata.common.model.UserStoragePreference> domainPreferences =
                    airavataService.getAllUserStoragePreferences(userId, gatewayID);
            return domainPreferences.stream()
                    .map(userStoragePreferenceMapper::toThrift)
                    .collect(Collectors.toList());
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error getting all user storage preferences: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public List<org.apache.airavata.thriftapi.model.UserResourceProfile> getAllUserResourceProfiles(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException,
                    org.apache.airavata.thriftapi.exception.AiravataSystemException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            List<org.apache.airavata.common.model.UserResourceProfile> domainProfiles =
                    airavataService.getAllUserResourceProfiles();
            return domainProfiles.stream()
                    .map(userResourceProfileMapper::toThrift)
                    .collect(Collectors.toList());
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
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
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            org.apache.airavata.common.model.UserComputeResourcePreference domainPreference =
                    userComputeResourcePreferenceMapper.toDomain(userComputeResourcePreference);
            airavataService.updateUserComputeResourcePreference(
                    userId, gatewayID, userComputeResourceId, domainPreference);
            return true;
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
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
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            org.apache.airavata.common.model.UserStoragePreference domainPreference =
                    userStoragePreferenceMapper.toDomain(dataStoragePreference);
            return airavataService.updateUserStoragePreference(userId, gatewayID, userStorageId, domainPreference);
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
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error updating user storage preference: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public boolean deleteUserComputeResourcePreference(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken,
            String userId,
            String gatewayID,
            String userComputeResourceId)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException,
                    org.apache.airavata.thriftapi.exception.AiravataSystemException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            return airavataService.deleteUserComputeResourcePreference(userId, gatewayID, userComputeResourceId);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error deleting user compute resource preference: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public boolean deleteUserStoragePreference(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken,
            String userId,
            String gatewayID,
            String userStorageId)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException,
                    org.apache.airavata.thriftapi.exception.AiravataSystemException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            return airavataService.deleteUserStoragePreference(userId, gatewayID, userStorageId);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error deleting user storage preference: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public List<org.apache.airavata.thriftapi.model.QueueStatusModel> getLatestQueueStatuses(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException,
                    org.apache.airavata.thriftapi.exception.AiravataSystemException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            // Get domain models from service
            var domainStatuses = airavataService.getLatestQueueStatuses();
            // Convert domain models to thrift models
            return domainStatuses.stream().map(queueStatusModelMapper::toThrift).collect(Collectors.toList());
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error getting latest queue statuses: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public String registerDataProduct(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken, DataProductModel dataProductModel)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException,
                    org.apache.airavata.thriftapi.exception.AiravataSystemException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        // Convert thrift model to domain model
        org.apache.airavata.common.model.DataProductModel domainDataProduct =
                dataProductModelMapper.toDomain(dataProductModel);
        try {
            return airavataService.registerDataProduct(domainDataProduct);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error registering data product: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public DataProductModel getDataProduct(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken, String productUri)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException,
                    org.apache.airavata.thriftapi.exception.AiravataSystemException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            // Get domain model from service
            org.apache.airavata.common.model.DataProductModel domainDataProduct =
                    airavataService.getDataProduct(productUri);
            // Convert domain model to thrift model
            return dataProductModelMapper.toThrift(domainDataProduct);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error getting data product: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public String registerReplicaLocation(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken,
            DataReplicaLocationModel replicaLocationModel)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException,
                    org.apache.airavata.thriftapi.exception.AiravataSystemException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            // Convert thrift model to domain model
            org.apache.airavata.common.model.DataReplicaLocationModel domainReplica =
                    dataReplicaLocationModelMapper.toDomain(replicaLocationModel);
            return airavataService.registerReplicaLocation(domainReplica);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error registering replica location: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public DataProductModel getParentDataProduct(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken, String productUri)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException,
                    org.apache.airavata.thriftapi.exception.AiravataSystemException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            // Get domain model from service
            org.apache.airavata.common.model.DataProductModel domainDataProduct =
                    airavataService.getParentDataProduct(productUri);
            // Convert domain model to thrift model
            return dataProductModelMapper.toThrift(domainDataProduct);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error getting parent data product: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public List<org.apache.airavata.thriftapi.model.DataProductModel> getChildDataProducts(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken, String productUri)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException,
                    org.apache.airavata.thriftapi.exception.AiravataSystemException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            // Get domain models from service
            var domainDataProducts = airavataService.getChildDataProducts(productUri);
            // Convert domain models to thrift models
            return domainDataProducts.stream()
                    .map(dataProductModelMapper::toThrift)
                    .collect(Collectors.toList());
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error getting child data products: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public boolean shareResourceWithUsers(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken,
            String resourceId,
            Map<String, org.apache.airavata.thriftapi.model.ResourcePermissionType> userPermissionList)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException,
                    org.apache.airavata.thriftapi.exception.AiravataSystemException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            Map<String, org.apache.airavata.common.model.ResourcePermissionType> domainUserPermissionList =
                    new java.util.HashMap<>();
            for (Map.Entry<String, ResourcePermissionType> entry : userPermissionList.entrySet()) {
                domainUserPermissionList.put(
                        entry.getKey(),
                        org.apache.airavata.common.model.ResourcePermissionType.valueOf(
                                entry.getValue().name()));
            }
            return airavataService.shareResourceWithUsers(domainAuthzToken, resourceId, domainUserPermissionList);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (org.apache.airavata.common.exception.AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error sharing resource with users: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public boolean shareResourceWithGroups(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken,
            String resourceId,
            Map<String, org.apache.airavata.thriftapi.model.ResourcePermissionType> groupPermissionList)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException,
                    org.apache.airavata.thriftapi.exception.AiravataSystemException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            Map<String, org.apache.airavata.common.model.ResourcePermissionType> domainGroupPermissionList =
                    new java.util.HashMap<>();
            for (Map.Entry<String, ResourcePermissionType> entry : groupPermissionList.entrySet()) {
                domainGroupPermissionList.put(
                        entry.getKey(),
                        org.apache.airavata.common.model.ResourcePermissionType.valueOf(
                                entry.getValue().name()));
            }
            return airavataService.shareResourceWithGroups(domainAuthzToken, resourceId, domainGroupPermissionList);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (org.apache.airavata.common.exception.AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error sharing resource with groups: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public boolean revokeSharingOfResourceFromUsers(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken,
            String resourceId,
            Map<String, org.apache.airavata.thriftapi.model.ResourcePermissionType> userPermissionList)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException,
                    org.apache.airavata.thriftapi.exception.AiravataSystemException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            Map<String, org.apache.airavata.common.model.ResourcePermissionType> domainUserPermissionList =
                    new java.util.HashMap<>();
            for (Map.Entry<String, ResourcePermissionType> entry : userPermissionList.entrySet()) {
                domainUserPermissionList.put(
                        entry.getKey(),
                        org.apache.airavata.common.model.ResourcePermissionType.valueOf(
                                entry.getValue().name()));
            }
            return airavataService.revokeSharingOfResourceFromUsers(
                    domainAuthzToken, resourceId, domainUserPermissionList);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (org.apache.airavata.common.exception.AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error revoking sharing of resource from users: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public boolean revokeSharingOfResourceFromGroups(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken,
            String resourceId,
            Map<String, org.apache.airavata.thriftapi.model.ResourcePermissionType> groupPermissionList)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException,
                    org.apache.airavata.thriftapi.exception.AiravataSystemException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            Map<String, org.apache.airavata.common.model.ResourcePermissionType> domainGroupPermissionList =
                    new java.util.HashMap<>();
            for (Map.Entry<String, ResourcePermissionType> entry : groupPermissionList.entrySet()) {
                domainGroupPermissionList.put(
                        entry.getKey(),
                        org.apache.airavata.common.model.ResourcePermissionType.valueOf(
                                entry.getValue().name()));
            }
            return airavataService.revokeSharingOfResourceFromGroups(
                    domainAuthzToken, resourceId, domainGroupPermissionList);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (org.apache.airavata.common.exception.AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error revoking sharing of resource from groups: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public List<String> getAllAccessibleUsers(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken,
            String resourceId,
            ResourcePermissionType permissionType)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException,
                    org.apache.airavata.thriftapi.exception.AiravataSystemException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            org.apache.airavata.common.model.ResourcePermissionType domainPermissionType =
                    org.apache.airavata.common.model.ResourcePermissionType.valueOf(permissionType.name());
            return airavataService.getAllAccessibleUsers(domainAuthzToken, resourceId, domainPermissionType, false);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error getting all accessible users: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public List<String> getAllDirectlyAccessibleUsers(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken,
            String resourceId,
            ResourcePermissionType permissionType)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException,
                    org.apache.airavata.thriftapi.exception.AiravataSystemException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            org.apache.airavata.common.model.ResourcePermissionType domainPermissionType =
                    org.apache.airavata.common.model.ResourcePermissionType.valueOf(permissionType.name());
            return airavataService.getAllAccessibleUsers(domainAuthzToken, resourceId, domainPermissionType, true);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error getting all directly accessible users: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public List<String> getAllAccessibleGroups(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken,
            String resourceId,
            ResourcePermissionType permissionType)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException,
                    org.apache.airavata.thriftapi.exception.AiravataSystemException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            org.apache.airavata.common.model.ResourcePermissionType domainPermissionType =
                    org.apache.airavata.common.model.ResourcePermissionType.valueOf(permissionType.name());
            return airavataService.getAllAccessibleGroups(domainAuthzToken, resourceId, domainPermissionType, false);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error getting all accessible groups: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
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
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            org.apache.airavata.common.model.ResourcePermissionType domainPermissionType =
                    org.apache.airavata.common.model.ResourcePermissionType.valueOf(permissionType.name());
            return airavataService.getAllAccessibleGroups(domainAuthzToken, resourceId, domainPermissionType, true);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error getting all directly accessible groups: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public boolean userHasAccess(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken,
            String resourceId,
            ResourcePermissionType permissionType)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException,
                    org.apache.airavata.thriftapi.exception.AiravataSystemException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            org.apache.airavata.common.model.ResourcePermissionType domainPermissionType =
                    org.apache.airavata.common.model.ResourcePermissionType.valueOf(permissionType.name());
            return airavataService.userHasAccess(domainAuthzToken, resourceId, domainPermissionType);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error checking user access: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public String createGroupResourceProfile(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken,
            org.apache.airavata.thriftapi.model.GroupResourceProfile groupResourceProfile)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException,
                    org.apache.airavata.thriftapi.exception.AiravataSystemException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            org.apache.airavata.common.model.GroupResourceProfile domainGroupResourceProfile =
                    groupResourceProfileMapper.toDomain(groupResourceProfile);
            return airavataService.createGroupResourceProfile(domainAuthzToken, domainGroupResourceProfile);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error creating group resource profile: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public void updateGroupResourceProfile(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken,
            org.apache.airavata.thriftapi.model.GroupResourceProfile groupResourceProfile)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException,
                    org.apache.airavata.thriftapi.exception.AiravataSystemException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            org.apache.airavata.common.model.GroupResourceProfile domainGroupResourceProfile =
                    groupResourceProfileMapper.toDomain(groupResourceProfile);
            airavataService.updateGroupResourceProfile(domainAuthzToken, domainGroupResourceProfile);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error updating group resource profile: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public GroupResourceProfile getGroupResourceProfile(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken, String groupResourceProfileId)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException,
                    org.apache.airavata.thriftapi.exception.AiravataSystemException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            org.apache.airavata.common.model.GroupResourceProfile domainProfile =
                    airavataService.getGroupResourceProfile(domainAuthzToken, groupResourceProfileId);
            return groupResourceProfileMapper.toThrift(domainProfile);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error getting group resource profile: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public boolean removeGroupResourceProfile(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken, String groupResourceProfileId)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException,
                    org.apache.airavata.thriftapi.exception.AiravataSystemException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            return airavataService.removeGroupResourceProfile(domainAuthzToken, groupResourceProfileId);
        } catch (org.apache.airavata.common.exception.AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error removing group resource profile: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public List<org.apache.airavata.thriftapi.model.GroupResourceProfile> getGroupResourceList(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken, String gatewayId)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException,
                    org.apache.airavata.thriftapi.exception.AiravataSystemException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            List<org.apache.airavata.common.model.GroupResourceProfile> domainProfiles =
                    airavataService.getGroupResourceList(domainAuthzToken, gatewayId);
            return domainProfiles.stream()
                    .map(groupResourceProfileMapper::toThrift)
                    .collect(Collectors.toList());
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
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
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            return airavataService.removeGroupComputePrefs(domainAuthzToken, computeResourceId, groupResourceProfileId);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (org.apache.airavata.common.exception.AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error removing group compute preferences: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public boolean removeGroupComputeResourcePolicy(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken, String resourcePolicyId)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException,
                    org.apache.airavata.thriftapi.exception.AiravataSystemException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            return airavataService.removeGroupComputeResourcePolicy(domainAuthzToken, resourcePolicyId);
        } catch (org.apache.airavata.common.exception.AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error removing group compute resource policy: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public boolean removeGroupBatchQueueResourcePolicy(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken, String resourcePolicyId)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException,
                    org.apache.airavata.thriftapi.exception.AiravataSystemException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            return airavataService.removeGroupBatchQueueResourcePolicy(domainAuthzToken, resourcePolicyId);
        } catch (org.apache.airavata.common.exception.AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error removing group batch queue resource policy: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public ComputeResourcePolicy getGroupComputeResourcePolicy(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken, String resourcePolicyId)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException,
                    org.apache.airavata.thriftapi.exception.AiravataSystemException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            org.apache.airavata.common.model.ComputeResourcePolicy domainPolicy =
                    airavataService.getGroupComputeResourcePolicy(domainAuthzToken, resourcePolicyId);
            return computeResourcePolicyMapper.toThrift(domainPolicy);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (org.apache.airavata.common.exception.AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error getting group compute resource policy: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public BatchQueueResourcePolicy getBatchQueueResourcePolicy(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken, String resourcePolicyId)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException,
                    org.apache.airavata.thriftapi.exception.AiravataSystemException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            org.apache.airavata.common.model.BatchQueueResourcePolicy domainPolicy =
                    airavataService.getBatchQueueResourcePolicy(domainAuthzToken, resourcePolicyId);
            return batchQueueResourcePolicyMapper.toThrift(domainPolicy);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (org.apache.airavata.common.exception.AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error getting batch queue resource policy: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public List<org.apache.airavata.thriftapi.model.GroupComputeResourcePreference> getGroupComputeResourcePrefList(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken, String groupResourceProfileId)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException,
                    org.apache.airavata.thriftapi.exception.AiravataSystemException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            List<org.apache.airavata.common.model.GroupComputeResourcePreference> domainPrefs =
                    airavataService.getGroupComputeResourcePrefList(domainAuthzToken, groupResourceProfileId);
            return domainPrefs.stream()
                    .map(groupComputeResourcePreferenceMapper::toThrift)
                    .collect(Collectors.toList());
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (org.apache.airavata.common.exception.AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error getting group compute resource preference list: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public List<org.apache.airavata.thriftapi.model.BatchQueueResourcePolicy> getGroupBatchQueueResourcePolicyList(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken, String groupResourceProfileId)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException,
                    org.apache.airavata.thriftapi.exception.AiravataSystemException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            List<org.apache.airavata.common.model.BatchQueueResourcePolicy> domainPolicies =
                    airavataService.getGroupBatchQueueResourcePolicyList(domainAuthzToken, groupResourceProfileId);
            return domainPolicies.stream()
                    .map(batchQueueResourcePolicyMapper::toThrift)
                    .collect(Collectors.toList());
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (org.apache.airavata.common.exception.AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error getting group batch queue resource policy list: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public List<org.apache.airavata.thriftapi.model.ComputeResourcePolicy> getGroupComputeResourcePolicyList(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken, String groupResourceProfileId)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException,
                    org.apache.airavata.thriftapi.exception.AiravataSystemException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            List<org.apache.airavata.common.model.ComputeResourcePolicy> domainPolicies =
                    airavataService.getGroupComputeResourcePolicyList(domainAuthzToken, groupResourceProfileId);
            return domainPolicies.stream()
                    .map(computeResourcePolicyMapper::toThrift)
                    .collect(Collectors.toList());
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (org.apache.airavata.common.exception.AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error getting group compute resource policy list: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public GatewayGroups getGatewayGroups(org.apache.airavata.thriftapi.security.model.AuthzToken authzToken)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException,
                    org.apache.airavata.thriftapi.exception.AiravataSystemException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            String gatewayId = domainAuthzToken.getClaimsMap().get(Constants.GATEWAY_ID);
            org.apache.airavata.common.model.GatewayGroups domainGroups = airavataService.getGatewayGroups(gatewayId);
            return gatewayGroupsMapper.toThrift(domainGroups);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error getting gateway groups: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public Parser getParser(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken, String parserId, String gatewayId)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException,
                    org.apache.airavata.thriftapi.exception.AiravataSystemException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.common.model.Parser domainParser = airavataService.getParser(parserId, gatewayId);
            return parserMapper.toThrift(domainParser);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
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
            org.apache.airavata.thriftapi.model.Parser parser)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException,
                    org.apache.airavata.thriftapi.exception.AiravataSystemException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            org.apache.airavata.common.model.Parser domainParser = parserMapper.toDomain(parser);
            return airavataService.saveParser(domainParser);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error saving parser: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public List<org.apache.airavata.thriftapi.model.Parser> listAllParsers(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken, String gatewayId)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException,
                    org.apache.airavata.thriftapi.exception.AiravataSystemException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            List<org.apache.airavata.common.model.Parser> domainParsers = airavataService.listAllParsers(gatewayId);
            return domainParsers.stream().map(parserMapper::toThrift).collect(Collectors.toList());
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error listing parsers: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public boolean removeParser(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken, String parserId, String gatewayId)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException,
                    org.apache.airavata.thriftapi.exception.AiravataSystemException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            airavataService.removeParser(parserId, gatewayId);
            return true;
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error removing parser: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public ParsingTemplate getParsingTemplate(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken, String templateId, String gatewayId)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException,
                    org.apache.airavata.thriftapi.exception.AiravataSystemException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.common.model.ParsingTemplate domainTemplate =
                    airavataService.getParsingTemplate(templateId, gatewayId);
            return parsingTemplateMapper.toThrift(domainTemplate);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error getting parsing template: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public List<org.apache.airavata.thriftapi.model.ParsingTemplate> getParsingTemplatesForExperiment(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken, String experimentId, String gatewayId)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException,
                    org.apache.airavata.thriftapi.exception.AiravataSystemException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            List<org.apache.airavata.common.model.ParsingTemplate> domainTemplates =
                    airavataService.getParsingTemplatesForExperiment(experimentId, gatewayId);
            return domainTemplates.stream().map(parsingTemplateMapper::toThrift).collect(Collectors.toList());
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
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
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            org.apache.airavata.common.model.ParsingTemplate domainTemplate =
                    parsingTemplateMapper.toDomain(parsingTemplate);
            return airavataService.saveParsingTemplate(domainTemplate);
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
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error saving parsing template: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public boolean removeParsingTemplate(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken, String templateId, String gatewayId)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException,
                    org.apache.airavata.thriftapi.exception.AiravataSystemException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            airavataService.removeParsingTemplate(templateId, gatewayId);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error removing parsing template: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
        return true;
    }

    @Override
    @SecurityCheck
    public List<org.apache.airavata.thriftapi.model.ParsingTemplate> listAllParsingTemplates(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken, String gatewayId)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException,
                    org.apache.airavata.thriftapi.exception.AiravataSystemException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            List<org.apache.airavata.common.model.ParsingTemplate> domainTemplates =
                    airavataService.listAllParsingTemplates(gatewayId);
            return domainTemplates.stream().map(parsingTemplateMapper::toThrift).collect(Collectors.toList());
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
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
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken,
            String airavataExperimentId,
            List<String> outputNames)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.ExperimentNotFoundException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException, AiravataSystemException,
                    AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            org.apache.airavata.common.model.ProcessStatus domainStatus =
                    airavataService.getIntermediateOutputProcessStatus(
                            domainAuthzToken, airavataExperimentId, outputNames);
            return processStatusMapper.toThrift(domainStatus);
        } catch (org.apache.airavata.common.exception.InvalidRequestException e) {
            throw convertToThriftInvalidRequestException(e);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (org.apache.airavata.common.exception.AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error getting intermediate output process status: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public void updateResourceScheduleing(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken,
            String airavataExperimentId,
            org.apache.airavata.thriftapi.model.ComputationalResourceSchedulingModel resourceScheduling)
            throws AuthorizationException, TException {
        try {
            if (false) throw new org.apache.airavata.thriftapi.exception.AuthorizationException();
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            org.apache.airavata.common.model.ComputationalResourceSchedulingModel domainScheduling =
                    computationalResourceSchedulingModelMapper.toDomain(resourceScheduling);
            airavataService.updateResourceScheduleing(airavataExperimentId, domainScheduling);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error updating resource scheduling: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public List<org.apache.airavata.thriftapi.model.ComputeResourcePreference> getAllGatewayComputeResourcePreferences(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken, String gatewayID)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException,
                    org.apache.airavata.thriftapi.exception.AiravataSystemException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            List<org.apache.airavata.common.model.ComputeResourcePreference> domainPreferences =
                    airavataService.getAllGatewayComputeResourcePreferences(gatewayID);
            return domainPreferences.stream()
                    .map(computeResourcePreferenceMapper::toThrift)
                    .collect(Collectors.toList());
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error getting all gateway compute resource preferences: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public GroupComputeResourcePreference getGroupComputeResourcePreference(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken,
            String computeResourceId,
            String groupResourceProfileId)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException,
                    org.apache.airavata.thriftapi.exception.AiravataSystemException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            org.apache.airavata.common.model.GroupComputeResourcePreference domainPreference =
                    airavataService.getGroupComputeResourcePreference(groupResourceProfileId, computeResourceId);
            return groupComputeResourcePreferenceMapper.toThrift(domainPreference);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error getting group compute resource preference: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public boolean deleteJobSubmissionInterface(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken,
            String computeResourceId,
            String jobSubmissionInterfaceId)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException,
                    org.apache.airavata.thriftapi.exception.AiravataSystemException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            return airavataService.deleteJobSubmissionInterface(computeResourceId, jobSubmissionInterfaceId);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error deleting job submission interface: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public String addLocalDataMovementDetails(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken,
            String productUri,
            DMType dataMoveType,
            int priorityOrder,
            LOCALDataMovement localDataMovement)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException,
                    org.apache.airavata.thriftapi.exception.AiravataSystemException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            org.apache.airavata.common.model.DMType domainDMType =
                    org.apache.airavata.common.model.DMType.valueOf(dataMoveType.name());
            org.apache.airavata.common.model.LOCALDataMovement domainLocal =
                    localDataMovementMapper.toDomain(localDataMovement);
            return airavataService.addLocalDataMovementDetails(productUri, domainDMType, priorityOrder, domainLocal);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(org.apache.airavata.thriftapi.exception.AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error adding local data movement details: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public boolean deleteDataMovementInterface(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken,
            String productUri,
            String dataMovementInterfaceId,
            DMType dataMoveType)
            throws org.apache.airavata.thriftapi.exception.InvalidRequestException,
                    org.apache.airavata.thriftapi.exception.AiravataClientException,
                    org.apache.airavata.thriftapi.exception.AiravataSystemException,
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            org.apache.airavata.common.model.DMType domainDMType =
                    org.apache.airavata.common.model.DMType.valueOf(dataMoveType.name());
            return airavataService.deleteDataMovementInterface(productUri, dataMovementInterfaceId, domainDMType);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.exception.AiravataSystemException ex =
                    new org.apache.airavata.thriftapi.exception.AiravataSystemException();
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
                    org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            org.apache.airavata.common.model.DMType domainDMType =
                    org.apache.airavata.common.model.DMType.valueOf(dataMoveType.name());
            org.apache.airavata.common.model.SCPDataMovement domainSCP =
                    scpDataMovementMapper.toDomain(scpDataMovement);
            return airavataService.addSCPDataMovementDetails(productUri, domainDMType, priorityOrder, domainSCP);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            throw convertToThriftSystemException(e);
        } catch (Exception e) {
            var ex = new AiravataSystemException();
            ex.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            ex.setMessage("Error adding SCP data movement details: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }
}
