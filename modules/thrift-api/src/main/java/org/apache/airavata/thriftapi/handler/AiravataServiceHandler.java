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
import org.apache.airavata.accountprovisioning.SSHAccountProvisionerFactory;
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.Constants;
import org.apache.airavata.security.interceptor.SecurityCheck;
import org.apache.airavata.service.AiravataService;
import org.apache.airavata.thriftapi.exception.AiravataClientException;
import org.apache.airavata.thriftapi.exception.AiravataErrorType;
import org.apache.airavata.thriftapi.exception.AiravataSystemException;
import org.apache.airavata.thriftapi.exception.AuthorizationException;
import org.apache.airavata.thriftapi.exception.InvalidRequestException;
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
import org.apache.airavata.thriftapi.mapper.JobStatusMapper;
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
    private final JobStatusMapper jobStatusMapper = JobStatusMapper.INSTANCE;
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

    public AiravataServiceHandler(AiravataService airavataService) {
        this.airavataService = airavataService;
        // AiravataService.init() is called automatically via @PostConstruct by Spring
    }

    /**
     * Converts any thrown exception from Airavata Services layer to appropriate Thrift API exception type.
     * The mapping strategy is:
     *  - If the cause is already a Thrift exception, return as is.
     *  - If the cause is a known domain exception, map accordingly.
     *  - If unknown, wrap in a generic AiravataSystemException.
     *
     * This merges all domain-to-thrift exception conversions into a single convenient method.
     */
    private TException wrapException(Throwable e) {
        if (e instanceof TException te) return te;
        TException thriftException = null;
        if (e instanceof ApplicationSettingsException
                || e instanceof org.apache.airavata.common.exception.AiravataSystemException) {
            var ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            if (e != null) {
                ex.setMessage(e.getMessage());
                ex.initCause(e);
            }
            thriftException = ex;
        } else if (e instanceof org.apache.airavata.common.exception.AuthorizationException) {
            var ex = new org.apache.airavata.thriftapi.exception.AuthorizationException();
            if (e != null) {
                ex.setMessage(e.getMessage());
                ex.initCause(e);
            }
            thriftException = ex;
        } else if (e instanceof org.apache.airavata.credential.exception.CredentialStoreException) {
            var ex = new org.apache.airavata.thriftapi.credential.exception.CredentialStoreException();
            if (e != null) {
                ex.setMessage(e.getMessage());
                ex.initCause(e);
            }
            thriftException = ex;
        } else if (e instanceof org.apache.airavata.common.exception.InvalidRequestException) {
            var ex = new org.apache.airavata.thriftapi.exception.InvalidRequestException();
            if (e != null) {
                ex.setMessage(e.getMessage());
                ex.initCause(e);
            }
            thriftException = ex;
        } else if (e instanceof org.apache.airavata.common.exception.ExperimentNotFoundException) {
            var ex = new org.apache.airavata.thriftapi.exception.ExperimentNotFoundException();
            if (e != null) {
                ex.setMessage(e.getMessage());
                ex.initCause(e);
            }
            thriftException = ex;
        } else if (e instanceof org.apache.airavata.common.exception.ProjectNotFoundException) {
            var ex = new org.apache.airavata.thriftapi.exception.ProjectNotFoundException();
            if (e != null) {
                ex.setMessage(e.getMessage());
                ex.initCause(e);
            }
            thriftException = ex;
        } else if (e instanceof org.apache.airavata.common.exception.AiravataClientException) {
            var ex = new org.apache.airavata.thriftapi.exception.AiravataClientException();
            if (e != null) {
                ex.setParameter(e.getMessage());
                ex.initCause(e);
            }
            thriftException = ex;
        }
        if (thriftException == null) {
            var ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            if (e != null) {
                ex.setMessage(e.getMessage());
                ex.initCause(e);
            }
            thriftException = ex;
        }
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
            return airavataService.isUserExists(gatewayId, userName);
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainGateway = gatewayMapper.toDomain(gateway);
            return airavataService.addGateway(domainGateway);
        } catch (Throwable e) {
            throw wrapException(e);
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
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainGateway = gatewayMapper.toDomain(updatedGateway);
            return airavataService.updateGateway(gatewayId, domainGateway);
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainGateway = airavataService.getGateway(gatewayId);
            return gatewayMapper.toThrift(domainGateway);
        } catch (Throwable e) {
            throw wrapException(e);
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
        } catch (Throwable e) {
            throw wrapException(e);
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
            var gateways = airavataService.getAllGateways();
            return gateways.stream().map(gatewayMapper::toThrift).collect(Collectors.toList());
        } catch (Throwable e) {
            throw wrapException(e);
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
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainNotification = notificationMapper.toDomain(notification);
            return airavataService.createNotification(domainNotification);
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainNotification = notificationMapper.toDomain(notification);
            return airavataService.updateNotification(domainNotification);
        } catch (Throwable e) {
            throw wrapException(e);
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
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainNotification = airavataService.getNotification(gatewayId, notificationId);
            return notificationMapper.toThrift(domainNotification);
        } catch (Throwable e) {
            throw wrapException(e);
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
            var notifications = airavataService.getAllNotifications(gatewayId);
            return notifications.stream().map(notificationMapper::toThrift).collect(Collectors.toList());
        } catch (Throwable e) {
            throw wrapException(e);
        }
    }

    @Override
    @SecurityCheck
    public String generateAndRegisterSSHKeys(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken, String description)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, TException {
        try {
            var domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            var gatewayId = domainAuthzToken.getClaimsMap().get(Constants.GATEWAY_ID);
            var userName = domainAuthzToken.getClaimsMap().get(Constants.USER_NAME);
            return airavataService.generateAndRegisterSSHKeys(gatewayId, userName, description);
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            var gatewayId = domainAuthzToken.getClaimsMap().get(Constants.GATEWAY_ID);
            var userName = domainAuthzToken.getClaimsMap().get(Constants.USER_NAME);
            return airavataService.registerPwdCredential(gatewayId, userName, loginUserName, password, description);
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            var gatewayId = domainAuthzToken.getClaimsMap().get(Constants.GATEWAY_ID);
            var domainSummary = airavataService.getCredentialSummary(tokenId, gatewayId);
            return credentialSummaryMapper.toThrift(domainSummary);
        } catch (Throwable e) {
            throw wrapException(e);
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
        var domainAuthzToken = authzTokenMapper.toDomain(authzToken);
        var domainType = org.apache.airavata.credential.model.SummaryType.valueOf(type.name());
        var gatewayId = domainAuthzToken.getClaimsMap().get(Constants.GATEWAY_ID);
        var userName = domainAuthzToken.getClaimsMap().get(Constants.USER_NAME);
        try {
            var domainSummaries =
                    airavataService.getAllCredentialSummaries(domainAuthzToken, domainType, gatewayId, userName);
            return domainSummaries.stream()
                    .map(credentialSummaryMapper::toThrift)
                    .collect(Collectors.toList());
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            var gatewayId = domainAuthzToken.getClaimsMap().get(Constants.GATEWAY_ID);
            return airavataService.deleteSSHCredential(airavataCredStoreToken, gatewayId);
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            var gatewayId = domainAuthzToken.getClaimsMap().get(Constants.GATEWAY_ID);
            return airavataService.deletePWDCredential(domainAuthzToken, airavataCredStoreToken, gatewayId);
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainProject = projectMapper.toDomain(project);
            return airavataService.createProject(gatewayId, domainProject);
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            var domainProject = projectMapper.toDomain(updatedProject);
            airavataService.updateProject(domainAuthzToken, projectId, domainProject);
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            return airavataService.deleteProject(domainAuthzToken, projectId);
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            var domainProject = airavataService.getProject(domainAuthzToken, projectId);
            return projectMapper.toThrift(domainProject);
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            var domainProjects = airavataService.getUserProjects(domainAuthzToken, gatewayId, userName, limit, offset);
            return domainProjects.stream().map(projectMapper::toThrift).collect(Collectors.toList());
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            var domainFilters = new java.util.HashMap<org.apache.airavata.common.model.ProjectSearchFields, String>();
            for (Map.Entry<ProjectSearchFields, String> entry : filters.entrySet()) {
                domainFilters.put(
                        org.apache.airavata.common.model.ProjectSearchFields.valueOf(
                                entry.getKey().name()),
                        entry.getValue());
            }
            var domainProjects =
                    airavataService.searchProjects(domainAuthzToken, gatewayId, userName, domainFilters, limit, offset);
            return domainProjects.stream().map(projectMapper::toThrift).collect(Collectors.toList());
        } catch (Throwable e) {
            throw wrapException(e);
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
        var domainAuthzToken = authzTokenMapper.toDomain(authzToken);
        var domainFilters = new java.util.HashMap<org.apache.airavata.common.model.ExperimentSearchFields, String>();
        for (Map.Entry<ExperimentSearchFields, String> entry : filters.entrySet()) {
            domainFilters.put(
                    org.apache.airavata.common.model.ExperimentSearchFields.valueOf(
                            entry.getKey().name()),
                    entry.getValue());
        }
        try {
            var domainSummaries = airavataService.searchExperiments(
                    domainAuthzToken, gatewayId, userName, domainFilters, limit, offset);
            return domainSummaries.stream()
                    .map(experimentSummaryModelMapper::toThrift)
                    .collect(Collectors.toList());
        } catch (Throwable e) {
            throw wrapException(e);
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
            var accessibleExpIds = new ArrayList<String>();
            var domainStats = airavataService.getExperimentStatistics(
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
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            var gatewayId = domainAuthzToken.getClaimsMap().get(Constants.GATEWAY_ID);
            var domainExperiments = airavataService.getExperimentsInProject(gatewayId, projectId, limit, offset);
            return domainExperiments.stream()
                    .map(experimentModelMapper::toThrift)
                    .collect(Collectors.toList());
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainExperiments = airavataService.getUserExperiments(gatewayId, userName, limit, offset);
            return domainExperiments.stream()
                    .map(experimentModelMapper::toThrift)
                    .collect(Collectors.toList());
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainExperiment = experimentModelMapper.toDomain(experiment);
            return airavataService.createExperiment(gatewayId, domainExperiment);
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            return airavataService.deleteExperimentWithAuth(domainAuthzToken, experimentId);
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            var domainExperiment = airavataService.getExperiment(domainAuthzToken, airavataExperimentId);
            return experimentModelMapper.toThrift(domainExperiment);
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            var domainExperiment = airavataService.getExperimentByAdmin(domainAuthzToken, airavataExperimentId);
            return experimentModelMapper.toThrift(domainExperiment);
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainExperiment = airavataService.getDetailedExperimentTree(airavataExperimentId);
            return experimentModelMapper.toThrift(domainExperiment);
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            var domainExperiment = experimentModelMapper.toDomain(experiment);
            airavataService.updateExperiment(domainAuthzToken, airavataExperimentId, domainExperiment);
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainUserConfig = userConfigurationDataModelMapper.toDomain(userConfiguration);
            airavataService.updateExperimentConfiguration(airavataExperimentId, domainUserConfig);
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainScheduling = computationalResourceSchedulingModelMapper.toDomain(resourceScheduling);
            airavataService.updateResourceScheduleing(airavataExperimentId, domainScheduling);
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            var domainExperiment = airavataService.getExperiment(domainAuthzToken, airavataExperimentId);
            return domainExperiment != null;
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainStatus = airavataService.getExperimentStatus(airavataExperimentId);
            var thriftStatus = new ExperimentStatus();
            thriftStatus.setState(
                    ExperimentState.valueOf(domainStatus.getState().name()));
            thriftStatus.setTimeOfStateChange(domainStatus.getTimeOfStateChange());
            thriftStatus.setReason(domainStatus.getReason());
            thriftStatus.setStatusId(domainStatus.getStatusId());
            return thriftStatus;
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainOutputs = airavataService.getExperimentOutputs(airavataExperimentId);
            return domainOutputs.stream()
                    .map(outputDataObjectTypeMapper::toThrift)
                    .collect(Collectors.toList());
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            var experiment = airavataService.getExperiment(domainAuthzToken, airavataExperimentId);
            return experiment.getExperimentOutputs().stream()
                    .map(outputDataObjectTypeMapper::toThrift)
                    .collect(Collectors.toList());
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            airavataService.fetchIntermediateOutputs(domainAuthzToken, airavataExperimentId, outputNames);
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainStatuses = airavataService.getJobStatuses(airavataExperimentId);
            return domainStatuses.entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, entry -> jobStatusMapper.toThrift(entry.getValue())));
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainJobs = airavataService.getJobDetails(airavataExperimentId);
            return domainJobs.stream().map(jobModelMapper::toThrift).collect(Collectors.toList());
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            airavataService.launchExperiment(domainAuthzToken, gatewayId, airavataExperimentId);
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            var existingExperiment = airavataService.getExperiment(domainAuthzToken, existingExperimentID);
            return airavataService.cloneExperiment(
                    domainAuthzToken,
                    existingExperimentID,
                    newExperimentName,
                    newExperimentProjectId,
                    existingExperiment);
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            var existingExperiment = airavataService.getExperimentByAdmin(domainAuthzToken, existingExperimentID);
            return airavataService.cloneExperiment(
                    domainAuthzToken,
                    existingExperimentID,
                    newExperimentName,
                    newExperimentProjectId,
                    existingExperiment);
        } catch (Throwable e) {
            throw wrapException(e);
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
            airavataService.terminateExperiment(airavataExperimentId, gatewayId);
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainModule = applicationModuleMapper.toDomain(applicationModule);
            applicationModuleMapper.toDomain(applicationModule);
            return airavataService.registerApplicationModule(gatewayId, domainModule);
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainModule = airavataService.getApplicationModule(appModuleId);
            return applicationModuleMapper.toThrift(domainModule);
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainModule = applicationModuleMapper.toDomain(applicationModule);
            airavataService.updateApplicationModule(appModuleId, domainModule);
            return true;
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainModules = airavataService.getAllAppModules(gatewayId);
            return domainModules.stream().map(applicationModuleMapper::toThrift).collect(Collectors.toList());
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            var domainModules = airavataService.getAccessibleAppModules(domainAuthzToken, gatewayId);
            return domainModules.stream().map(applicationModuleMapper::toThrift).collect(Collectors.toList());
        } catch (Throwable e) {
            throw wrapException(e);
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
            return airavataService.deleteApplicationModule(appModuleId);
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            var domainDeployment = applicationDeploymentDescriptionMapper.toDomain(applicationDeployment);
            return airavataService.registerApplicationDeployment(domainAuthzToken, gatewayId, domainDeployment);
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            var domainDeployment = airavataService.getApplicationDeployment(domainAuthzToken, appDeploymentId);
            return applicationDeploymentDescriptionMapper.toThrift(domainDeployment);
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            var domainDeployment = applicationDeploymentDescriptionMapper.toDomain(applicationDeployment);
            return airavataService.updateApplicationDeployment(domainAuthzToken, appDeploymentId, domainDeployment);
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            return airavataService.deleteApplicationDeployment(domainAuthzToken, appDeploymentId);
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            var domainPermissionType =
                    org.apache.airavata.common.model.ResourcePermissionType.valueOf(permissionType.name());
            var domainDeployments = airavataService.getAccessibleApplicationDeployments(
                    domainAuthzToken, gatewayId, domainPermissionType);
            return domainDeployments.stream()
                    .map(applicationDeploymentDescriptionMapper::toThrift)
                    .collect(Collectors.toList());
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            var domainDeployments = airavataService.getApplicationDeploymentsForAppModuleAndGroupResourceProfile(
                    domainAuthzToken, appModuleId, groupResourceProfileId);
            return domainDeployments.stream()
                    .map(applicationDeploymentDescriptionMapper::toThrift)
                    .collect(Collectors.toList());
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainInterface = applicationInterfaceDescriptionMapper.toDomain(applicationInterface);
            return airavataService.registerApplicationInterface(gatewayId, domainInterface);
        } catch (Throwable e) {
            throw wrapException(e);
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
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainInterface = airavataService.getApplicationInterface(appInterfaceId);
            return applicationInterfaceDescriptionMapper.toThrift(domainInterface);
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainInterface = applicationInterfaceDescriptionMapper.toDomain(applicationInterface);
            airavataService.updateApplicationInterface(appInterfaceId, domainInterface);
            return true;
        } catch (Throwable e) {
            throw wrapException(e);
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
            return airavataService.deleteApplicationInterface(appInterfaceId);
        } catch (Throwable e) {
            throw wrapException(e);
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
            return airavataService.getAllApplicationInterfaceNames(gatewayId);
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainInterfaces = airavataService.getAllApplicationInterfaces(gatewayId);
            return domainInterfaces.stream()
                    .map(applicationInterfaceDescriptionMapper::toThrift)
                    .collect(Collectors.toList());
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainInputs = airavataService.getApplicationInputs(appInterfaceId);
            return domainInputs.stream()
                    .map(inputDataObjectTypeMapper::toThrift)
                    .collect(Collectors.toList());
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainOutputs = airavataService.getApplicationOutputs(appInterfaceId);
            return domainOutputs.stream()
                    .map(outputDataObjectTypeMapper::toThrift)
                    .collect(Collectors.toList());
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainComputeResource = computeResourceDescriptionMapper.toDomain(computeResourceDescription);
            return airavataService.registerComputeResource(domainComputeResource);
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainComputeResource = airavataService.getComputeResource(computeResourceId);
            return computeResourceDescriptionMapper.toThrift(domainComputeResource);
        } catch (Throwable e) {
            throw wrapException(e);
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
            return airavataService.getAllComputeResourceNames();
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainComputeResource = computeResourceDescriptionMapper.toDomain(computeResourceDescription);
            airavataService.updateComputeResource(computeResourceId, domainComputeResource);
            return true;
        } catch (Throwable e) {
            throw wrapException(e);
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
            return airavataService.deleteComputeResource(computeResourceId);
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainStorage = storageResourceDescriptionMapper.toDomain(storageResourceDescription);
            return airavataService.registerStorageResource(domainStorage);
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainStorage = airavataService.getStorageResource(storageResourceId);
            return storageResourceDescriptionMapper.toThrift(domainStorage);
        } catch (Throwable e) {
            throw wrapException(e);
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
            return airavataService.getAllStorageResourceNames();
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainStorage = storageResourceDescriptionMapper.toDomain(storageResourceDescription);
            return airavataService.updateStorageResource(storageResourceId, domainStorage);
        } catch (Throwable e) {
            throw wrapException(e);
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
            return airavataService.deleteStorageResource(storageResourceId);
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            var domainInfo = airavataService.getResourceStorageInfo(domainAuthzToken, resourceId, location);
            return storageVolumeInfoMapper.toThrift(domainInfo);
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            var domainInfo = airavataService.getStorageDirectoryInfo(domainAuthzToken, resourceId, location);
            return storageDirectoryInfoMapper.toThrift(domainInfo);
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainLocal = localSubmissionMapper.toDomain(localSubmission);
            return airavataService.addLocalSubmissionDetails(computeResourceId, priorityOrder, domainLocal);
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainLocal = localSubmissionMapper.toDomain(localSubmission);
            airavataService.updateLocalSubmissionDetails(jobSubmissionInterfaceId, domainLocal);
            return true;
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainLocal = airavataService.getLocalJobSubmission(jobSubmissionId);
            return localSubmissionMapper.toThrift(domainLocal);
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainSSH = sshJobSubmissionMapper.toDomain(sshJobSubmission);
            return airavataService.addSSHJobSubmissionDetails(computeResourceId, priorityOrder, domainSSH);
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainSSH = sshJobSubmissionMapper.toDomain(sshJobSubmission);
            return airavataService.addSSHForkJobSubmissionDetails(computeResourceId, priorityOrder, domainSSH);
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainSSH = airavataService.getSSHJobSubmission(jobSubmissionId);
            return sshJobSubmissionMapper.toThrift(domainSSH);
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainCloud = cloudJobSubmissionMapper.toDomain(cloudJobSubmission);
            return airavataService.addCloudJobSubmissionDetails(computeResourceId, priorityOrder, domainCloud);
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainCloud = airavataService.getCloudJobSubmission(jobSubmissionId);
            return cloudJobSubmissionMapper.toThrift(domainCloud);
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainUnicore = unicoreJobSubmissionMapper.toDomain(unicoreJobSubmission);
            return airavataService.addUNICOREJobSubmissionDetails(computeResourceId, priorityOrder, domainUnicore);
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainUnicore = airavataService.getUnicoreJobSubmission(jobSubmissionId);
            return unicoreJobSubmissionMapper.toThrift(domainUnicore);
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainSSH = sshJobSubmissionMapper.toDomain(sshJobSubmission);
            return airavataService.updateSSHJobSubmissionDetails(jobSubmissionInterfaceId, domainSSH);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            var thriftException = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            thriftException.setMessage(e.getMessage());
            if (e.getAiravataErrorType() != null) {
                thriftException.setAiravataErrorType(
                        AiravataErrorType.valueOf(e.getAiravataErrorType().name()));
            }
            thriftException.initCause(e);
            throw thriftException;
        } catch (Exception e) {
            var ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
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
            var domainCloud = cloudJobSubmissionMapper.toDomain(cloudJobSubmission);
            return airavataService.updateCloudJobSubmissionDetails(jobSubmissionInterfaceId, domainCloud);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            var thriftException = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            thriftException.setMessage(e.getMessage());
            if (e.getAiravataErrorType() != null) {
                thriftException.setAiravataErrorType(
                        AiravataErrorType.valueOf(e.getAiravataErrorType().name()));
            }
            thriftException.initCause(e);
            throw thriftException;
        } catch (Exception e) {
            var ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
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
            var domainUnicore = unicoreJobSubmissionMapper.toDomain(unicoreJobSubmission);
            return airavataService.updateUnicoreJobSubmissionDetails(jobSubmissionInterfaceId, domainUnicore);
        } catch (org.apache.airavata.common.exception.AiravataSystemException e) {
            var thriftException = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            thriftException.setMessage(e.getMessage());
            if (e.getAiravataErrorType() != null) {
                thriftException.setAiravataErrorType(
                        AiravataErrorType.valueOf(e.getAiravataErrorType().name()));
            }
            thriftException.initCause(e);
            throw thriftException;
        } catch (Exception e) {
            var ex = new org.apache.airavata.thriftapi.exception.AiravataSystemException();
            ex.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
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
            var domainLocalDataMovement = localDataMovementMapper.toDomain(localDataMovement);
            airavataService.updateLocalDataMovementDetails(dataMovementInterfaceId, domainLocalDataMovement);
            return true;
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainSCP = scpDataMovementMapper.toDomain(scpDataMovement);
            return airavataService.updateSCPDataMovementDetails(dataMovementInterfaceId, domainSCP);
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainSCP = airavataService.getSCPDataMovement(dataMovementId);
            return scpDataMovementMapper.toThrift(domainSCP);
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainDMType = org.apache.airavata.common.model.DMType.valueOf(dmType.name());
            var domainUnicore = unicoreDataMovementMapper.toDomain(unicoreDataMovement);
            return airavataService.addUnicoreDataMovementDetails(
                    resourceId, domainDMType, priorityOrder, domainUnicore);
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainUnicore = unicoreDataMovementMapper.toDomain(unicoreDataMovement);
            airavataService.updateUnicoreDataMovementDetails(dataMovementInterfaceId, domainUnicore);
            return true;
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainLocal = airavataService.getLocalDataMovement(dataMovementId);
            return localDataMovementMapper.toThrift(domainLocal);
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainUnicore = airavataService.getUnicoreDataMovement(dataMovementId);
            return unicoreDataMovementMapper.toThrift(domainUnicore);
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainDMType = org.apache.airavata.common.model.DMType.valueOf(dataMoveType.name());
            var domainGridFTP = gridFTPDataMovementMapper.toDomain(gridFTPDataMovement);
            return airavataService.addGridFTPDataMovementDetails(
                    productUri, domainDMType, priorityOrder, domainGridFTP);
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainGridFTP = gridFTPDataMovementMapper.toDomain(gridFTPDataMovement);
            airavataService.updateGridFTPDataMovementDetails(dataMovementInterfaceId, domainGridFTP);
            return true;
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainGridFTP = airavataService.getGridFTPDataMovement(dataMovementId);
            return gridFTPDataMovementMapper.toThrift(domainGridFTP);
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainRJM = resourceJobManagerMapper.toDomain(resourceJobManager);
            return airavataService.registerResourceJobManager(domainRJM);
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainRJM = resourceJobManagerMapper.toDomain(updatedResourceJobManager);
            return airavataService.updateResourceJobManager(resourceJobManagerId, domainRJM);
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainRJM = airavataService.getResourceJobManager(resourceJobManagerId);
            return resourceJobManagerMapper.toThrift(domainRJM);
        } catch (Throwable e) {
            throw wrapException(e);
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
            return airavataService.deleteResourceJobManager(resourceJobManagerId);
        } catch (Throwable e) {
            throw wrapException(e);
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
            return airavataService.deleteBatchQueue(computeResourceId, queueName);
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainProfile = gatewayResourceProfileMapper.toDomain(gatewayResourceProfile);
            return airavataService.registerGatewayResourceProfile(domainProfile);
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainProfile = airavataService.getGatewayResourceProfile(gatewayID);
            return gatewayResourceProfileMapper.toThrift(domainProfile);
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainProfile = gatewayResourceProfileMapper.toDomain(gatewayResourceProfile);
            airavataService.updateGatewayResourceProfile(gatewayID, domainProfile);
            return true;
        } catch (Throwable e) {
            throw wrapException(e);
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
            return airavataService.deleteGatewayResourceProfile(gatewayID);
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainPreference = computeResourcePreferenceMapper.toDomain(computeResourcePreference);
            airavataService.addGatewayComputeResourcePreference(gatewayID, computeResourceId, domainPreference);
            return true;
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainPreference = storagePreferenceMapper.toDomain(dataStoragePreference);
            return airavataService.addGatewayStoragePreference(gatewayID, storageResourceId, domainPreference);
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainPreference = airavataService.getGatewayComputeResourcePreference(gatewayID, computeResourceId);
            return computeResourcePreferenceMapper.toThrift(domainPreference);
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainPreference = airavataService.getGatewayStoragePreference(gatewayID, storageId);
            return storagePreferenceMapper.toThrift(domainPreference);
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainPreferences = airavataService.getAllGatewayStoragePreferences(gatewayID);
            return domainPreferences.stream()
                    .map(storagePreferenceMapper::toThrift)
                    .collect(Collectors.toList());
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainProfiles = airavataService.getAllGatewayResourceProfiles();
            return domainProfiles.stream()
                    .map(gatewayResourceProfileMapper::toThrift)
                    .collect(Collectors.toList());
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainPreference = computeResourcePreferenceMapper.toDomain(computeResourcePreference);
            return airavataService.updateGatewayComputeResourcePreference(
                    gatewayID, computeResourceId, domainPreference);
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainPreference = storagePreferenceMapper.toDomain(dataStoragePreference);
            return airavataService.updateGatewayStoragePreference(gatewayID, storageId, domainPreference);
        } catch (Throwable e) {
            throw wrapException(e);
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
            return airavataService.deleteGatewayComputeResourcePreference(gatewayID, computeResourceId);
        } catch (Throwable e) {
            throw wrapException(e);
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
            return airavataService.deleteGatewayStoragePreference(gatewayID, storageId);
        } catch (Throwable e) {
            throw wrapException(e);
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
        try {
            var domainProvisioners = new ArrayList<SSHAccountProvisionerDescription>();
            var sshAccountProvisionerProviders = SSHAccountProvisionerFactory.getSSHAccountProvisionerProviders();
            for (var provider : sshAccountProvisionerProviders) {
                var pr = new SSHAccountProvisionerDescription();
                pr.setCanCreateAccount(provider.canCreateAccount());
                pr.setCanInstallSSHKey(provider.canInstallSSHKey());
                pr.setName(provider.getName());
                var cp = new ArrayList<SSHAccountProvisionerConfigParam>();
                for (var configParam : provider.getConfigParams()) {
                    var param = new SSHAccountProvisionerConfigParam();
                    param.setName(configParam.getName());
                    param.setDescription(configParam.getDescription());
                    param.setIsOptional(configParam.isOptional());
                    switch (configParam.getType()) {
                        case STRING:
                            param.setType(SSHAccountProvisionerConfigParamType.STRING);
                            break;
                        case CRED_STORE_PASSWORD_TOKEN:
                            param.setType(SSHAccountProvisionerConfigParamType.CRED_STORE_PASSWORD_TOKEN);
                            break;
                    }
                    cp.add(param);
                }
                pr.setConfigParams(cp);
                domainProvisioners.add(pr);
            }
            return domainProvisioners;
        } catch (Throwable e) {
            throw wrapException(e);
        }
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
            var domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            return airavataService.doesUserHaveSSHAccount(domainAuthzToken, computeResourceId, userId);
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            return airavataService.isSSHAccountSetupComplete(
                    domainAuthzToken, computeResourceId, airavataCredStoreToken);
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            var domainPreference = airavataService.setupSSHAccount(
                    domainAuthzToken, computeResourceId, userId, airavataCredStoreToken);
            return userComputeResourcePreferenceMapper.toThrift(domainPreference);
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainProfile = userResourceProfileMapper.toDomain(userResourceProfile);
            return airavataService.registerUserResourceProfile(domainProfile);
        } catch (Throwable e) {
            throw wrapException(e);
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
            return airavataService.isUserResourceProfileExists(userId, gatewayID);
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainProfile = airavataService.getUserResourceProfile(userId, gatewayID);
            return userResourceProfileMapper.toThrift(domainProfile);
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainProfile = userResourceProfileMapper.toDomain(userResourceProfile);
            return airavataService.updateUserResourceProfile(userId, gatewayID, domainProfile);
        } catch (Throwable e) {
            throw wrapException(e);
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
            return airavataService.deleteUserResourceProfile(userId, gatewayID);
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainPreference = userComputeResourcePreferenceMapper.toDomain(userComputeResourcePreference);
            return airavataService.addUserComputeResourcePreference(
                    userId, gatewayID, computeResourceId, domainPreference);
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainPreference = userStoragePreferenceMapper.toDomain(dataStoragePreference);
            return airavataService.addUserStoragePreference(userId, gatewayID, userStorageResourceId, domainPreference);
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainPreference =
                    airavataService.getUserComputeResourcePreference(userId, gatewayID, userComputeResourceId);
            return userComputeResourcePreferenceMapper.toThrift(domainPreference);
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainPreference = airavataService.getUserStoragePreference(userId, gatewayID, userStorageId);
            return userStoragePreferenceMapper.toThrift(domainPreference);
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainPreferences = airavataService.getAllUserComputeResourcePreferences(userId, gatewayID);
            return domainPreferences.stream()
                    .map(userComputeResourcePreferenceMapper::toThrift)
                    .collect(Collectors.toList());
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainPreferences = airavataService.getAllUserStoragePreferences(userId, gatewayID);
            return domainPreferences.stream()
                    .map(userStoragePreferenceMapper::toThrift)
                    .collect(Collectors.toList());
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainProfiles = airavataService.getAllUserResourceProfiles();
            return domainProfiles.stream()
                    .map(userResourceProfileMapper::toThrift)
                    .collect(Collectors.toList());
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainPreference = userComputeResourcePreferenceMapper.toDomain(userComputeResourcePreference);
            airavataService.updateUserComputeResourcePreference(
                    userId, gatewayID, userComputeResourceId, domainPreference);
            return true;
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainPreference = userStoragePreferenceMapper.toDomain(dataStoragePreference);
            return airavataService.updateUserStoragePreference(userId, gatewayID, userStorageId, domainPreference);
        } catch (Throwable e) {
            throw wrapException(e);
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
            return airavataService.deleteUserComputeResourcePreference(userId, gatewayID, userComputeResourceId);
        } catch (Throwable e) {
            throw wrapException(e);
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
            return airavataService.deleteUserStoragePreference(userId, gatewayID, userStorageId);
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainStatuses = airavataService.getLatestQueueStatuses();
            return domainStatuses.stream().map(queueStatusModelMapper::toThrift).collect(Collectors.toList());
        } catch (Throwable e) {
            throw wrapException(e);
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
        try {
            var domainDataProduct = dataProductModelMapper.toDomain(dataProductModel);
            return airavataService.registerDataProduct(domainDataProduct);
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainDataProduct = airavataService.getDataProduct(productUri);
            return dataProductModelMapper.toThrift(domainDataProduct);
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainReplica = dataReplicaLocationModelMapper.toDomain(replicaLocationModel);
            return airavataService.registerReplicaLocation(domainReplica);
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainDataProduct = airavataService.getParentDataProduct(productUri);
            return dataProductModelMapper.toThrift(domainDataProduct);
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainDataProducts = airavataService.getChildDataProducts(productUri);
            return domainDataProducts.stream()
                    .map(dataProductModelMapper::toThrift)
                    .collect(Collectors.toList());
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            var domainUserPermissionList = userPermissionList.entrySet().stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            entry -> org.apache.airavata.common.model.ResourcePermissionType.valueOf(
                                    entry.getValue().name())));
            return airavataService.shareResourceWithUsers(domainAuthzToken, resourceId, domainUserPermissionList);
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            var domainGroupPermissionList = groupPermissionList.entrySet().stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            entry -> org.apache.airavata.common.model.ResourcePermissionType.valueOf(
                                    entry.getValue().name())));
            return airavataService.shareResourceWithGroups(domainAuthzToken, resourceId, domainGroupPermissionList);
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            var domainUserPermissionList = userPermissionList.entrySet().stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            entry -> org.apache.airavata.common.model.ResourcePermissionType.valueOf(
                                    entry.getValue().name())));
            return airavataService.revokeSharingOfResourceFromUsers(
                    domainAuthzToken, resourceId, domainUserPermissionList);
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            var domainGroupPermissionList = groupPermissionList.entrySet().stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            entry -> org.apache.airavata.common.model.ResourcePermissionType.valueOf(
                                    entry.getValue().name())));
            return airavataService.revokeSharingOfResourceFromGroups(
                    domainAuthzToken, resourceId, domainGroupPermissionList);
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            var domainPermissionType =
                    org.apache.airavata.common.model.ResourcePermissionType.valueOf(permissionType.name());
            return airavataService.getAllAccessibleUsers(domainAuthzToken, resourceId, domainPermissionType, false);
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            var domainPermissionType =
                    org.apache.airavata.common.model.ResourcePermissionType.valueOf(permissionType.name());
            return airavataService.getAllAccessibleUsers(domainAuthzToken, resourceId, domainPermissionType, true);
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            var domainPermissionType =
                    org.apache.airavata.common.model.ResourcePermissionType.valueOf(permissionType.name());
            return airavataService.getAllAccessibleGroups(domainAuthzToken, resourceId, domainPermissionType, false);
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            var domainPermissionType =
                    org.apache.airavata.common.model.ResourcePermissionType.valueOf(permissionType.name());
            return airavataService.getAllAccessibleGroups(domainAuthzToken, resourceId, domainPermissionType, true);
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            var domainPermissionType =
                    org.apache.airavata.common.model.ResourcePermissionType.valueOf(permissionType.name());
            return airavataService.userHasAccess(domainAuthzToken, resourceId, domainPermissionType);
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            var domainGroupResourceProfile = groupResourceProfileMapper.toDomain(groupResourceProfile);
            return airavataService.createGroupResourceProfile(domainAuthzToken, domainGroupResourceProfile);
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            var domainGroupResourceProfile = groupResourceProfileMapper.toDomain(groupResourceProfile);
            airavataService.updateGroupResourceProfile(domainAuthzToken, domainGroupResourceProfile);
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            var domainProfile = airavataService.getGroupResourceProfile(domainAuthzToken, groupResourceProfileId);
            return groupResourceProfileMapper.toThrift(domainProfile);
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            return airavataService.removeGroupResourceProfile(domainAuthzToken, groupResourceProfileId);
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            var domainProfiles = airavataService.getGroupResourceList(domainAuthzToken, gatewayId);
            return domainProfiles.stream()
                    .map(groupResourceProfileMapper::toThrift)
                    .collect(Collectors.toList());
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            return airavataService.removeGroupComputePrefs(domainAuthzToken, computeResourceId, groupResourceProfileId);
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            return airavataService.removeGroupComputeResourcePolicy(domainAuthzToken, resourcePolicyId);
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            return airavataService.removeGroupBatchQueueResourcePolicy(domainAuthzToken, resourcePolicyId);
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            var domainPolicy = airavataService.getGroupComputeResourcePolicy(domainAuthzToken, resourcePolicyId);
            return computeResourcePolicyMapper.toThrift(domainPolicy);
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            var domainPolicy = airavataService.getBatchQueueResourcePolicy(domainAuthzToken, resourcePolicyId);
            return batchQueueResourcePolicyMapper.toThrift(domainPolicy);
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            var domainPrefs = airavataService.getGroupComputeResourcePrefList(domainAuthzToken, groupResourceProfileId);
            return domainPrefs.stream()
                    .map(groupComputeResourcePreferenceMapper::toThrift)
                    .collect(Collectors.toList());
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            var domainPolicies =
                    airavataService.getGroupBatchQueueResourcePolicyList(domainAuthzToken, groupResourceProfileId);
            return domainPolicies.stream()
                    .map(batchQueueResourcePolicyMapper::toThrift)
                    .collect(Collectors.toList());
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            var domainPolicies =
                    airavataService.getGroupComputeResourcePolicyList(domainAuthzToken, groupResourceProfileId);
            return domainPolicies.stream()
                    .map(computeResourcePolicyMapper::toThrift)
                    .collect(Collectors.toList());
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            String gatewayId = domainAuthzToken.getClaimsMap().get(Constants.GATEWAY_ID);
            var domainGroups = airavataService.getGatewayGroups(gatewayId);
            return gatewayGroupsMapper.toThrift(domainGroups);
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainParser = airavataService.getParser(parserId, gatewayId);
            return parserMapper.toThrift(domainParser);
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainParser = parserMapper.toDomain(parser);
            return airavataService.saveParser(domainParser);
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainParsers = airavataService.listAllParsers(gatewayId);
            return domainParsers.stream().map(parserMapper::toThrift).collect(Collectors.toList());
        } catch (Throwable e) {
            throw wrapException(e);
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
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainTemplate = airavataService.getParsingTemplate(templateId, gatewayId);
            return parsingTemplateMapper.toThrift(domainTemplate);
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainTemplates = airavataService.getParsingTemplatesForExperiment(experimentId, gatewayId);
            return domainTemplates.stream().map(parsingTemplateMapper::toThrift).collect(Collectors.toList());
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainTemplate = parsingTemplateMapper.toDomain(parsingTemplate);
            return airavataService.saveParsingTemplate(domainTemplate);
        } catch (Throwable e) {
            throw wrapException(e);
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
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainTemplates = airavataService.listAllParsingTemplates(gatewayId);
            return domainTemplates.stream().map(parsingTemplateMapper::toThrift).collect(Collectors.toList());
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainAuthzToken = authzTokenMapper.toDomain(authzToken);
            var domainStatus = airavataService.getIntermediateOutputProcessStatus(
                    domainAuthzToken, airavataExperimentId, outputNames);
            return processStatusMapper.toThrift(domainStatus);
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainScheduling = computationalResourceSchedulingModelMapper.toDomain(resourceScheduling);
            airavataService.updateResourceScheduleing(airavataExperimentId, domainScheduling);
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainPreferences = airavataService.getAllGatewayComputeResourcePreferences(gatewayID);
            return domainPreferences.stream()
                    .map(computeResourcePreferenceMapper::toThrift)
                    .collect(Collectors.toList());
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainPreference =
                    airavataService.getGroupComputeResourcePreference(groupResourceProfileId, computeResourceId);
            return groupComputeResourcePreferenceMapper.toThrift(domainPreference);
        } catch (Throwable e) {
            throw wrapException(e);
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
            return airavataService.deleteJobSubmissionInterface(computeResourceId, jobSubmissionInterfaceId);
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainDMType = org.apache.airavata.common.model.DMType.valueOf(dataMoveType.name());
            var domainLocal = localDataMovementMapper.toDomain(localDataMovement);
            return airavataService.addLocalDataMovementDetails(productUri, domainDMType, priorityOrder, domainLocal);
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainDMType = org.apache.airavata.common.model.DMType.valueOf(dataMoveType.name());
            return airavataService.deleteDataMovementInterface(productUri, dataMovementInterfaceId, domainDMType);
        } catch (Throwable e) {
            throw wrapException(e);
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
            var domainDMType = org.apache.airavata.common.model.DMType.valueOf(dataMoveType.name());
            var domainSCP = scpDataMovementMapper.toDomain(scpDataMovement);
            return airavataService.addSCPDataMovementDetails(productUri, domainDMType, priorityOrder, domainSCP);
        } catch (Throwable e) {
            throw wrapException(e);
        }
    }
}
