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
package org.apache.airavata.storage.mapper;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import org.apache.airavata.common.CommonMapperConversions;
import org.apache.airavata.model.data.replica.proto.DataProductModel;
import org.apache.airavata.model.data.replica.proto.DataReplicaLocationModel;
import org.apache.airavata.storage.model.DataProductEntity;
import org.apache.airavata.storage.model.DataReplicaLocationEntity;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DataMapper extends CommonMapperConversions {

    DataMapper INSTANCE = Mappers.getMapper(DataMapper.class);

    // --- DataProductModel ---
    // Hand-written rather than MapStruct-generated: a protobuf Builder's map getter
    // returns an immutable view, so the generated
    // `builder.getProductMetadata().putAll(..)`
    // throws UnsupportedOperationException. Use the builder's putAll* accessor
    // instead,
    // and map the repeated replicaLocations (which MapStruct silently drops). Same
    // proto
    // hand-mapping pattern as appDeploymentToModel above.
    default DataProductModel dataProductToModel(DataProductEntity entity) {
        if (entity == null)
            return null;
        DataProductModel.Builder builder = DataProductModel.newBuilder();
        if (entity.getProductUri() != null)
            builder.setProductUri(entity.getProductUri());
        if (entity.getGatewayId() != null)
            builder.setGatewayId(entity.getGatewayId());
        if (entity.getParentProductUri() != null)
            builder.setParentProductUri(entity.getParentProductUri());
        if (entity.getProductName() != null)
            builder.setProductName(entity.getProductName());
        if (entity.getProductDescription() != null)
            builder.setProductDescription(entity.getProductDescription());
        if (entity.getOwnerName() != null)
            builder.setOwnerName(entity.getOwnerName());
        if (entity.getDataProductType() != null)
            builder.setDataProductType(entity.getDataProductType());
        builder.setProductSize(entity.getProductSize());
        builder.setCreationTime(timestampToLong(entity.getCreationTime()));
        builder.setLastModifiedTime(timestampToLong(entity.getLastModifiedTime()));
        if (entity.getProductMetadata() != null) {
            builder.putAllProductMetadata(entity.getProductMetadata());
        }
        if (entity.getReplicaLocations() != null) {
            for (DataReplicaLocationEntity replica : entity.getReplicaLocations()) {
                builder.addReplicaLocations(dataReplicaToModel(replica));
            }
        }
        return builder.build();
    }

    // Hand-written so the nested replicaLocations are mapped: MapStruct silently
    // drops the proto repeated -> entity collection mapping, so a registered data
    // product would persist with no replica (losing its file path).
    default DataProductEntity dataProductToEntity(DataProductModel model) {
        if (model == null)
            return null;
        DataProductEntity entity = new DataProductEntity();
        entity.setProductUri(model.getProductUri());
        entity.setGatewayId(model.getGatewayId());
        entity.setProductName(model.getProductName());
        entity.setProductDescription(model.getProductDescription());
        entity.setOwnerName(model.getOwnerName());
        entity.setParentProductUri(model.getParentProductUri());
        entity.setProductSize(model.getProductSize());
        entity.setCreationTime(longToTimestamp(model.getCreationTime()));
        entity.setLastModifiedTime(longToTimestamp(model.getLastModifiedTime()));
        entity.setDataProductType(model.getDataProductType());
        if (model.getProductMetadataMap() != null) {
            entity.setProductMetadata(new LinkedHashMap<>(model.getProductMetadataMap()));
        }
        List<DataReplicaLocationEntity> replicas = new ArrayList<>();
        for (DataReplicaLocationModel replica : model.getReplicaLocationsList()) {
            replicas.add(dataReplicaToEntity(replica));
        }
        entity.setReplicaLocations(replicas);
        return entity;
    }

    // --- DataReplicaLocationModel ---
    // Hand-written for the same proto immutable-map reason as dataProductToModel.
    default DataReplicaLocationModel dataReplicaToModel(DataReplicaLocationEntity entity) {
        if (entity == null)
            return null;
        DataReplicaLocationModel.Builder builder = DataReplicaLocationModel.newBuilder();
        if (entity.getReplicaId() != null)
            builder.setReplicaId(entity.getReplicaId());
        if (entity.getProductUri() != null)
            builder.setProductUri(entity.getProductUri());
        if (entity.getReplicaName() != null)
            builder.setReplicaName(entity.getReplicaName());
        if (entity.getReplicaDescription() != null)
            builder.setReplicaDescription(entity.getReplicaDescription());
        builder.setCreationTime(timestampToLong(entity.getCreationTime()));
        builder.setLastModifiedTime(timestampToLong(entity.getLastModifiedTime()));
        builder.setValidUntilTime(timestampToLong(entity.getValidUntilTime()));
        if (entity.getReplicaLocationCategory() != null)
            builder.setReplicaLocationCategory(entity.getReplicaLocationCategory());
        if (entity.getReplicaPersistentType() != null)
            builder.setReplicaPersistentType(entity.getReplicaPersistentType());
        if (entity.getStorageResourceId() != null)
            builder.setStorageResourceId(entity.getStorageResourceId());
        if (entity.getFilePath() != null)
            builder.setFilePath(entity.getFilePath());
        if (entity.getReplicaMetadata() != null) {
            builder.putAllReplicaMetadata(entity.getReplicaMetadata());
        }
        return builder.build();
    }

    DataReplicaLocationEntity dataReplicaToEntity(DataReplicaLocationModel model);
}
