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
package org.apache.airavata.iam.mapper;

import org.apache.airavata.iam.model.GatewayEntity;
import org.apache.airavata.iam.model.GatewayUsageReportingCommandEntity;
import org.apache.airavata.iam.model.QueueStatusEntity;
import org.apache.airavata.mapper.CommonMapperConversions;
import org.apache.airavata.model.status.proto.QueueStatusModel;
import org.apache.airavata.model.workspace.proto.Gateway;
import org.apache.airavata.model.workspace.proto.GatewayUsageReportingCommand;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

/**
 * MapStruct mapper for gateway and queue status entities that live in root airavata-api.
 */
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface GatewayEntityMapper extends CommonMapperConversions {

    GatewayEntityMapper INSTANCE = Mappers.getMapper(GatewayEntityMapper.class);

    // --- Gateway ---
    Gateway gatewayToModel(GatewayEntity entity);

    GatewayEntity gatewayToEntity(Gateway model);

    // --- QueueStatus ---
    QueueStatusModel queueStatusToModel(QueueStatusEntity entity);

    QueueStatusEntity queueStatusToEntity(QueueStatusModel model);

    // --- GatewayUsageReportingCommand ---
    GatewayUsageReportingCommand gatewayUsageReportingCommandToModel(GatewayUsageReportingCommandEntity entity);

    GatewayUsageReportingCommandEntity gatewayUsageReportingCommandToEntity(GatewayUsageReportingCommand model);
}
