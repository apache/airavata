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
package org.apache.airavata.compute.mapper;

import java.sql.Timestamp;
import org.apache.airavata.compute.model.*;
import org.apache.airavata.model.appcatalog.appdeployment.ApplicationDeploymentDescription;
import org.apache.airavata.model.appcatalog.appdeployment.ApplicationModule;
import org.apache.airavata.model.appcatalog.appinterface.ApplicationInterfaceDescription;
import org.apache.airavata.model.appcatalog.computeresource.BatchQueue;
import org.apache.airavata.model.appcatalog.computeresource.CloudJobSubmission;
import org.apache.airavata.model.appcatalog.computeresource.ComputeResourceDescription;
import org.apache.airavata.model.appcatalog.computeresource.JobSubmissionInterface;
import org.apache.airavata.model.appcatalog.computeresource.LOCALSubmission;
import org.apache.airavata.model.appcatalog.computeresource.ResourceJobManager;
import org.apache.airavata.model.appcatalog.computeresource.SSHJobSubmission;
import org.apache.airavata.model.appcatalog.computeresource.UnicoreJobSubmission;
import org.apache.airavata.model.appcatalog.gatewaygroups.GatewayGroups;
import org.apache.airavata.model.appcatalog.gatewayprofile.ComputeResourcePreference;
import org.apache.airavata.model.appcatalog.gatewayprofile.GatewayResourceProfile;
import org.apache.airavata.model.appcatalog.groupresourceprofile.BatchQueueResourcePolicy;
import org.apache.airavata.model.appcatalog.groupresourceprofile.ComputeResourcePolicy;
import org.apache.airavata.model.appcatalog.groupresourceprofile.GroupComputeResourcePreference;
import org.apache.airavata.model.appcatalog.groupresourceprofile.GroupResourceProfile;
import org.apache.airavata.model.appcatalog.parser.Parser;
import org.apache.airavata.model.appcatalog.parser.ParserInput;
import org.apache.airavata.model.appcatalog.parser.ParserOutput;
import org.apache.airavata.model.appcatalog.parser.ParsingTemplate;
import org.apache.airavata.model.appcatalog.userresourceprofile.UserComputeResourcePreference;
import org.apache.airavata.model.appcatalog.userresourceprofile.UserResourceProfile;
import org.apache.airavata.model.application.io.InputDataObjectType;
import org.apache.airavata.model.application.io.OutputDataObjectType;
import org.apache.airavata.model.data.movement.GridFTPDataMovement;
import org.apache.airavata.model.data.movement.LOCALDataMovement;
import org.apache.airavata.model.data.movement.SCPDataMovement;
import org.apache.airavata.model.data.movement.UnicoreDataMovement;
import org.apache.airavata.storage.model.GridftpDataMovementEntity;
import org.apache.airavata.storage.model.LocalDataMovementEntity;
import org.apache.airavata.storage.model.ScpDataMovementEntity;
import org.apache.airavata.storage.model.UnicoreDatamovementEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ComputeMapper {

    ComputeMapper INSTANCE = Mappers.getMapper(ComputeMapper.class);

    // --- ComputeResourceDescription ---
    ComputeResourceDescription computeResourceToModel(ComputeResourceEntity entity);

    ComputeResourceEntity computeResourceToEntity(ComputeResourceDescription model);

    // --- ApplicationDeploymentDescription ---
    @Mapping(source = "defaultWallTime", target = "defaultWalltime")
    ApplicationDeploymentDescription appDeploymentToModel(ApplicationDeploymentEntity entity);

    @Mapping(source = "defaultWalltime", target = "defaultWallTime")
    ApplicationDeploymentEntity appDeploymentToEntity(ApplicationDeploymentDescription model);

    // --- ApplicationInterfaceDescription ---
    ApplicationInterfaceDescription appInterfaceToModel(ApplicationInterfaceEntity entity);

    ApplicationInterfaceEntity appInterfaceToEntity(ApplicationInterfaceDescription model);

    // --- ApplicationModule ---
    ApplicationModule appModuleToModel(ApplicationModuleEntity entity);

    ApplicationModuleEntity appModuleToEntity(ApplicationModule model);

    // --- ApplicationInput ---
    InputDataObjectType appInputToModel(ApplicationInputEntity entity);

    ApplicationInputEntity appInputToEntity(InputDataObjectType model);

    // --- ApplicationOutput ---
    OutputDataObjectType appOutputToModel(ApplicationOutputEntity entity);

    ApplicationOutputEntity appOutputToEntity(OutputDataObjectType model);

    // --- BatchQueue ---
    @Mapping(source = "maxRuntime", target = "maxRunTime")
    BatchQueue batchQueueToModel(BatchQueueEntity entity);

    @Mapping(source = "maxRunTime", target = "maxRuntime")
    BatchQueueEntity batchQueueToEntity(BatchQueue model);

    // --- ResourceJobManager ---
    @Mapping(target = "jobManagerCommands", ignore = true)
    @Mapping(target = "parallelismPrefix", ignore = true)
    ResourceJobManager resourceJobManagerToModel(ResourceJobManagerEntity entity);

    @Mapping(target = "jobManagerCommands", ignore = true)
    @Mapping(target = "parallelismCommands", ignore = true)
    ResourceJobManagerEntity resourceJobManagerToEntity(ResourceJobManager model);

    // --- JobSubmissionInterface ---
    JobSubmissionInterface jobSubmissionInterfaceToModel(JobSubmissionInterfaceEntity entity);

    JobSubmissionInterfaceEntity jobSubmissionInterfaceToEntity(JobSubmissionInterface model);

    // --- GatewayResourceProfile / GatewayProfileEntity ---
    @Mapping(source = "gatewayId", target = "gatewayID")
    GatewayResourceProfile gatewayProfileToModel(GatewayProfileEntity entity);

    @Mapping(source = "gatewayID", target = "gatewayId")
    GatewayProfileEntity gatewayProfileToEntity(GatewayResourceProfile model);

    // --- ComputeResourcePreference ---
    ComputeResourcePreference computeResourcePrefToModel(ComputeResourcePreferenceEntity entity);

    ComputeResourcePreferenceEntity computeResourcePrefToEntity(ComputeResourcePreference model);

    // --- GroupResourceProfile ---
    GroupResourceProfile groupResourceProfileToModel(GroupResourceProfileEntity entity);

    GroupResourceProfileEntity groupResourceProfileToEntity(GroupResourceProfile model);

    // --- GroupComputeResourcePreference ---
    GroupComputeResourcePreference groupComputePrefToModel(GroupComputeResourcePrefEntity entity);
    // Uses SlurmGroupComputeResourcePrefEntity as the default concrete type
    SlurmGroupComputeResourcePrefEntity groupComputePrefToEntity(GroupComputeResourcePreference model);

    // --- ComputeResourcePolicy ---
    ComputeResourcePolicy computeResourcePolicyToModel(ComputeResourcePolicyEntity entity);

    ComputeResourcePolicyEntity computeResourcePolicyToEntity(ComputeResourcePolicy model);

    // --- BatchQueueResourcePolicy ---
    BatchQueueResourcePolicy batchQueuePolicyToModel(BatchQueueResourcePolicyEntity entity);

    BatchQueueResourcePolicyEntity batchQueuePolicyToEntity(BatchQueueResourcePolicy model);

    // --- UserResourceProfile ---
    @Mapping(source = "gatewayId", target = "gatewayID")
    UserResourceProfile userResourceProfileToModel(UserResourceProfileEntity entity);

    @Mapping(source = "gatewayID", target = "gatewayId")
    UserResourceProfileEntity userResourceProfileToEntity(UserResourceProfile model);

    // --- UserComputeResourcePreference ---
    UserComputeResourcePreference userComputeResourcePrefToModel(UserComputeResourcePreferenceEntity entity);

    UserComputeResourcePreferenceEntity userComputeResourcePrefToEntity(UserComputeResourcePreference model);

    // --- GatewayGroups ---
    GatewayGroups gatewayGroupsToModel(GatewayGroupsEntity entity);

    GatewayGroupsEntity gatewayGroupsToEntity(GatewayGroups model);

    // --- Parser ---
    Parser parserToModel(ParserEntity entity);

    ParserEntity parserToEntity(Parser model);

    // --- ParserInput ---
    ParserInput parserInputToModel(ParserInputEntity entity);

    ParserInputEntity parserInputToEntity(ParserInput model);

    // --- ParserOutput ---
    ParserOutput parserOutputToModel(ParserOutputEntity entity);

    ParserOutputEntity parserOutputToEntity(ParserOutput model);

    // --- ParsingTemplate ---
    ParsingTemplate parsingTemplateToModel(ParsingTemplateEntity entity);

    ParsingTemplateEntity parsingTemplateToEntity(ParsingTemplate model);

    // --- Extra types used directly in ComputeResourceRepository ---

    // SSHJobSubmission
    SSHJobSubmission sshJobSubmissionToModel(SshJobSubmissionEntity entity);

    SshJobSubmissionEntity sshJobSubmissionToEntity(SSHJobSubmission model);

    // CloudJobSubmission
    CloudJobSubmission cloudJobSubmissionToModel(CloudJobSubmissionEntity entity);

    CloudJobSubmissionEntity cloudJobSubmissionToEntity(CloudJobSubmission model);

    // LOCALSubmission
    LOCALSubmission localSubmissionToModel(LocalSubmissionEntity entity);

    LocalSubmissionEntity localSubmissionToEntity(LOCALSubmission model);

    // UnicoreJobSubmission
    UnicoreJobSubmission unicoreSubmissionToModel(UnicoreSubmissionEntity entity);

    UnicoreSubmissionEntity unicoreSubmissionToEntity(UnicoreJobSubmission model);

    // LOCALDataMovement
    LOCALDataMovement localDataMovementToModel(LocalDataMovementEntity entity);

    LocalDataMovementEntity localDataMovementToEntity(LOCALDataMovement model);

    // SCPDataMovement
    SCPDataMovement scpDataMovementToModel(ScpDataMovementEntity entity);

    ScpDataMovementEntity scpDataMovementToEntity(SCPDataMovement model);

    // UnicoreDataMovement
    @Mapping(source = "unicoreEndpointUrl", target = "unicoreEndPointURL")
    UnicoreDataMovement unicoreDataMovementToModel(UnicoreDatamovementEntity entity);

    @Mapping(source = "unicoreEndPointURL", target = "unicoreEndpointUrl")
    UnicoreDatamovementEntity unicoreDataMovementToEntity(UnicoreDataMovement model);

    // GridFTPDataMovement
    GridFTPDataMovement gridFtpDataMovementToModel(GridftpDataMovementEntity entity);

    GridftpDataMovementEntity gridFtpDataMovementToEntity(GridFTPDataMovement model);

    // --- Custom converters ---

    default Timestamp longToTimestamp(long millis) {
        return millis == 0 ? null : new Timestamp(millis);
    }

    default long timestampToLong(Timestamp ts) {
        return ts == null ? 0 : ts.getTime();
    }

    /** short to boolean (Thrift boolean fields stored as short in some entities) */
    default boolean shortToBoolean(short value) {
        return value != 0;
    }

    /** boolean to short */
    default short booleanToShort(boolean value) {
        return (short) (value ? 1 : 0);
    }
}
