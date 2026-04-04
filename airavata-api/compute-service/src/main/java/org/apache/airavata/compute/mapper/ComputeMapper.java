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

import org.apache.airavata.compute.model.*;
import org.apache.airavata.mapper.CommonMapperConversions;
import org.apache.airavata.model.appcatalog.appdeployment.proto.ApplicationDeploymentDescription;
import org.apache.airavata.model.appcatalog.appdeployment.proto.ApplicationModule;
import org.apache.airavata.model.appcatalog.appinterface.proto.ApplicationInterfaceDescription;
import org.apache.airavata.model.appcatalog.computeresource.proto.BatchQueue;
import org.apache.airavata.model.appcatalog.computeresource.proto.CloudJobSubmission;
import org.apache.airavata.model.appcatalog.computeresource.proto.ComputeResourceDescription;
import org.apache.airavata.model.appcatalog.computeresource.proto.JobSubmissionInterface;
import org.apache.airavata.model.appcatalog.computeresource.proto.LOCALSubmission;
import org.apache.airavata.model.appcatalog.computeresource.proto.ResourceJobManager;
import org.apache.airavata.model.appcatalog.computeresource.proto.SSHJobSubmission;
import org.apache.airavata.model.appcatalog.computeresource.proto.UnicoreJobSubmission;
import org.apache.airavata.model.appcatalog.gatewayprofile.proto.ComputeResourcePreference;
import org.apache.airavata.model.appcatalog.gatewayprofile.proto.GatewayResourceProfile;
import org.apache.airavata.model.appcatalog.groupresourceprofile.proto.BatchQueueResourcePolicy;
import org.apache.airavata.model.appcatalog.groupresourceprofile.proto.ComputeResourcePolicy;
import org.apache.airavata.model.appcatalog.groupresourceprofile.proto.GroupComputeResourcePreference;
import org.apache.airavata.model.appcatalog.groupresourceprofile.proto.GroupResourceProfile;
import org.apache.airavata.model.appcatalog.parser.proto.Parser;
import org.apache.airavata.model.appcatalog.parser.proto.ParserInput;
import org.apache.airavata.model.appcatalog.parser.proto.ParserOutput;
import org.apache.airavata.model.appcatalog.parser.proto.ParsingTemplate;
import org.apache.airavata.model.appcatalog.userresourceprofile.proto.UserComputeResourcePreference;
import org.apache.airavata.model.appcatalog.userresourceprofile.proto.UserResourceProfile;
import org.apache.airavata.model.application.io.proto.InputDataObjectType;
import org.apache.airavata.model.application.io.proto.OutputDataObjectType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ComputeMapper extends CommonMapperConversions {

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
    GatewayResourceProfile gatewayProfileToModel(GatewayProfileEntity entity);

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
    UserResourceProfile userResourceProfileToModel(UserResourceProfileEntity entity);

    UserResourceProfileEntity userResourceProfileToEntity(UserResourceProfile model);

    // --- UserComputeResourcePreference ---
    UserComputeResourcePreference userComputeResourcePrefToModel(UserComputeResourcePreferenceEntity entity);

    UserComputeResourcePreferenceEntity userComputeResourcePrefToEntity(UserComputeResourcePreference model);

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
}
