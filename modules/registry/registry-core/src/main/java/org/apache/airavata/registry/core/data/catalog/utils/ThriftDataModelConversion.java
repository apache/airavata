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

package org.apache.airavata.registry.core.data.catalog.utils;

import org.apache.airavata.model.data.movement.DataMovementProtocol;
import org.apache.airavata.model.data.resource.*;
import org.apache.airavata.registry.core.data.catalog.model.DataReplicaLocation;
import org.apache.airavata.registry.core.data.catalog.model.DataReplicaMetaData;
import org.apache.airavata.registry.core.data.catalog.model.DataResource;
import org.apache.airavata.registry.core.data.catalog.model.DataResourceMetaData;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ThriftDataModelConversion {

    private final static Logger logger = LoggerFactory.getLogger(ThriftDataModelConversion.class);

    public static DataResourceModel getDataResourceModel(DataResource dataResource){
        if (dataResource != null) {
            DataResourceModel dataResourceModel = new DataResourceModel();
            dataResourceModel.setResourceId(dataResource.getResourceId());
            dataResourceModel.setGatewayId(dataResource.getGatewayId());
            dataResourceModel.setParentResourceId(dataResource.getParentResourceId());
            dataResourceModel.setResourceName(dataResource.getResourceName());
            if(dataResource.getDataResourceType() != null)
                dataResourceModel.setDataResourceType(DataResourceType.valueOf(dataResource.getDataResourceType()));
            else
                dataResourceModel.setDataResourceType(DataResourceType.FILE);
            dataResourceModel.setResourceDescription(dataResource.getResourceDescription());
            dataResourceModel.setOwnerName(dataResource.getOwnerName());
            dataResourceModel.setResourceSize(dataResource.getResourceSize());
            dataResourceModel.setCreationTime(dataResource.getCreationTime().getTime());
            dataResourceModel.setLastModifiedTime(dataResource.getLastModifiedTime().getTime());
            dataResourceModel.setResourceMetadata(getResourceMetaData(dataResource.getDataResourceMetaData()));
            if(dataResource.getDataReplicaLocations() != null){
                ArrayList<DataReplicaLocationModel> dataReplicaLocationModels = new ArrayList<>();
                dataResource.getDataReplicaLocations().stream().forEach(r->dataReplicaLocationModels
                        .add(getDataReplicaLocationModel(r)));
                dataResourceModel.setReplicaLocations(dataReplicaLocationModels);
            }
            if(dataResourceModel.getDataResourceType().equals(DataResourceType.COLLECTION) && dataResource.getChildDataResources() != null){
                ArrayList<DataResourceModel> childDataResources = new ArrayList<>();
                dataResource.getChildDataResources().stream().forEach(r->childDataResources.add(getDataResourceModel(r)));
                dataResourceModel.setChildResources(childDataResources);
            }
            return dataResourceModel;
        }
        return null;
    }

    public static DataResource getDataResource(DataResourceModel dataResourceModel){
        if(dataResourceModel != null){
            DataResource dataResource = new DataResource();
            return getUpdatedDataResource(dataResourceModel, dataResource);
        }
        return null;
    }

    public static DataResource getUpdatedDataResource(DataResourceModel dataResourceModel, DataResource dataResource){
        dataResource.setResourceId(dataResourceModel.getResourceId());
        dataResource.setGatewayId(dataResourceModel.getGatewayId());
        dataResource.setResourceName(dataResourceModel.getResourceName());
        dataResource.setParentResourceId(dataResourceModel.getParentResourceId());
        if(dataResourceModel.getDataResourceType() != null)
            dataResource.setDataResourceType(dataResourceModel.getDataResourceType().toString());
        else
            dataResource.setDataResourceType(DataResourceType.FILE.toString());
        dataResource.setResourceDescription(dataResourceModel.getResourceDescription());
        dataResource.setOwnerName(dataResourceModel.getOwnerName());
        dataResource.setResourceSize(dataResourceModel.getResourceSize());
        dataResource.setCreationTime(new Timestamp(dataResourceModel.getCreationTime()));
        dataResource.setLastModifiedTime(new Timestamp(dataResourceModel.getLastModifiedTime()));
        ArrayList<DataResourceMetaData> dataResourceMetaData = new ArrayList<>();
        if(dataResourceModel.getResourceMetadata() != null) {
            dataResourceModel.getResourceMetadata().keySet().stream().forEach(k -> {
                String v = dataResourceModel.getResourceMetadata().get(k);
                DataResourceMetaData temp = new DataResourceMetaData();
                temp.setResourceId(dataResource.getResourceId());
                temp.setKey(k);
                temp.setValue(v);
                dataResourceMetaData.add(temp);
            });
            dataResource.setDataResourceMetaData(dataResourceMetaData);
        }
        if(dataResourceModel.getReplicaLocations() != null){
            ArrayList<DataReplicaLocation> dataReplicaLocations = new ArrayList<>();
            dataResourceModel.getReplicaLocations().stream().forEach(r->{
                DataReplicaLocation dataReplicaLocationModel = getDataReplicaLocation(r);
                dataReplicaLocationModel.setResourceId(dataResourceModel.getResourceId());
                dataReplicaLocations.add(dataReplicaLocationModel);
            });
            dataResource.setDataReplicaLocations(dataReplicaLocations);
        }
        if(dataResourceModel.getDataResourceType() == DataResourceType.COLLECTION && dataResourceModel.getChildResources() != null){
            ArrayList<DataResource> childDataResources = new ArrayList<>();
            dataResourceModel.getChildResources().stream().forEach(r->childDataResources.add(getDataResource(r)));
            dataResource.setChildDataResources(childDataResources);
        }
        return dataResource;
    }

    public static DataReplicaLocationModel getDataReplicaLocationModel(DataReplicaLocation replicaLocation){
        if (replicaLocation != null) {
            DataReplicaLocationModel replicaLocationModel = new DataReplicaLocationModel();
            replicaLocationModel.setReplicaId(replicaLocation.getReplicaId());
            replicaLocationModel.setResourceId(replicaLocation.getResourceId());
            replicaLocationModel.setReplicaName(replicaLocation.getReplicaName());
            replicaLocationModel.setReplicaDescription(replicaLocation.getReplicaDescription());
            replicaLocationModel.setStorageResourceId(replicaLocation.getStorageResourceId());
            replicaLocationModel.setStorageResourceHostName(replicaLocation.getStorageResourceHostName());
            if(replicaLocation.getStorageResourceDataTransferProtocol() != null)
                replicaLocationModel.setDataMovementProtocol(DataMovementProtocol.valueOf(replicaLocation.getStorageResourceDataTransferProtocol()));
            replicaLocationModel.setHostPort(replicaLocation.getStorageResourcePort());
            replicaLocationModel.setFilePath(replicaLocation.getFilePath());
            replicaLocationModel.setCreationTime(replicaLocation.getCreationTime().getTime());
            replicaLocationModel.setLastModifiedTime(replicaLocation.getLastModifiedTime().getTime());
            if(replicaLocation.getReplicaLocationCategory() != null)
                replicaLocationModel.setReplicaLocationCategory(ReplicaLocationCategory.valueOf(replicaLocation
                    .getReplicaLocationCategory().toString()));
            if(replicaLocation.getReplicaPersistentType() != null)
                replicaLocationModel.setReplicaPersistentType(ReplicaPersistentType.valueOf(replicaLocation
                    .getReplicaPersistentType().toString()));
            replicaLocationModel.setReplicaUrl(replicaLocation.getReplicaUrl());
            replicaLocationModel.setReplicaMetadata(getReplicaMetaData(replicaLocation.getDataReplicaMetaData()));
            return replicaLocationModel;
        }
        return null;
    }

    public static DataReplicaLocation getDataReplicaLocation(DataReplicaLocationModel dataReplicaLocationModel){
        if(dataReplicaLocationModel != null){
            DataReplicaLocation dataReplicaLocation = new DataReplicaLocation();
            return getUpdatedDataReplicaLocation(dataReplicaLocationModel, dataReplicaLocation);
        }
        return null;
    }

    public static DataReplicaLocation getUpdatedDataReplicaLocation(DataReplicaLocationModel dataReplicaLocationModel,
                                                                    DataReplicaLocation dataReplicaLocation){
        dataReplicaLocation.setReplicaId(dataReplicaLocationModel.getReplicaId());
        dataReplicaLocation.setResourceId(dataReplicaLocationModel.getResourceId());
        dataReplicaLocation.setReplicaName(dataReplicaLocationModel.getReplicaName());
        dataReplicaLocation.setReplicaDescription(dataReplicaLocationModel.getReplicaDescription());
        dataReplicaLocation.setStorageResourceId(dataReplicaLocationModel.getStorageResourceId());
        dataReplicaLocation.setStorageResourceHostName(dataReplicaLocationModel.getStorageResourceHostName());
        if(dataReplicaLocationModel.getDataMovementProtocol() != null)
            dataReplicaLocation.setStorageResourceDataTransferProtocol(dataReplicaLocationModel.getDataMovementProtocol().toString());
        dataReplicaLocation.setStorageResourcePort((int)dataReplicaLocationModel.getHostPort());
        dataReplicaLocation.setFilePath(dataReplicaLocationModel.getFilePath());
        dataReplicaLocation.setReplicaUrl(dataReplicaLocation.getReplicaUrl());
        dataReplicaLocation.setCreationTime(new Timestamp(dataReplicaLocationModel.getCreationTime()));
        dataReplicaLocation.setLastModifiedTime(new Timestamp(dataReplicaLocationModel.getLastModifiedTime()));
        if(dataReplicaLocationModel.getReplicaLocationCategory() != null)
            dataReplicaLocation.setReplicaLocationCategory(dataReplicaLocationModel.getReplicaLocationCategory().toString());
        if(dataReplicaLocationModel.getReplicaPersistentType() != null)
            dataReplicaLocation.setReplicaPersistentType(dataReplicaLocationModel.getReplicaPersistentType().toString());
        ArrayList<DataReplicaMetaData> dataReplicaMetadata = new ArrayList<>();
        if(dataReplicaLocation.getDataReplicaMetaData() != null){
            dataReplicaLocationModel.getReplicaMetadata().keySet().stream().forEach(k -> {
                String v = dataReplicaLocationModel.getReplicaMetadata().get(k);
                DataReplicaMetaData temp = new DataReplicaMetaData();
                temp.setReplicaId(dataReplicaLocationModel.getResourceId());
                temp.setKey(k);
                temp.setValue(v);
                dataReplicaMetadata.add(temp);
            });
            dataReplicaLocation.setDataReplicaMetaData(dataReplicaMetadata);
        }
        return dataReplicaLocation;
    }

    public static Map<String, String> getResourceMetaData(Collection<DataResourceMetaData> dataResourceMetaData){
        HashMap<String, String> metadata = new HashMap<>();
        if(dataResourceMetaData!=null && !dataResourceMetaData.isEmpty()) {
            dataResourceMetaData.stream().forEach(m -> metadata.put(m.getKey(),m.getValue()));
        }
        return metadata;
    }

    public static Map<String, String> getReplicaMetaData(Collection<DataReplicaMetaData> dataReplicaMetaData){
        HashMap<String, String> metadata = new HashMap<>();
        if(dataReplicaMetaData!=null && !dataReplicaMetaData.isEmpty()) {
            dataReplicaMetaData.stream().forEach(m -> metadata.put(m.getKey(),m.getValue()));
        }
        return metadata;
    }
}