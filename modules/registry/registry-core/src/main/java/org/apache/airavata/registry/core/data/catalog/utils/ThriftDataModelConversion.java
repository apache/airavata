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

import org.apache.airavata.model.data.resource.DataReplicaLocationModel;
import org.apache.airavata.model.data.resource.DataResourceModel;
import org.apache.airavata.registry.core.data.catalog.model.DataReplicaLocation;
import org.apache.airavata.registry.core.data.catalog.model.DataResource;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;

public class ThriftDataModelConversion {

    private final static Logger logger = LoggerFactory.getLogger(ThriftDataModelConversion.class);

    public static DataResourceModel getDataResourceModel(DataResource dataResource){
        if (dataResource != null) {
            DataResourceModel dataResourceModel = new DataResourceModel();
            dataResourceModel.setResourceId(dataResource.getResourceId());
            dataResourceModel.setResourceName(dataResource.getResourceName());
            dataResourceModel.setResourceDescription(dataResource.getResourceDescription());
            dataResourceModel.setOwnerName(dataResource.getOwnerName());
            dataResourceModel.setResourceSize(dataResource.getResourceSize());
            dataResourceModel.setCreationTime(dataResource.getCreationTime().getTime());
            dataResourceModel.setLastModifiedTime(dataResource.getLastModifiedTime().getTime());
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
        dataResource.setResourceName(dataResourceModel.getResourceName());
        dataResource.setResourceDescription(dataResourceModel.getResourceDescription());
        dataResource.setOwnerName(dataResourceModel.getOwnerName());
        dataResource.setResourceSize(dataResourceModel.getResourceSize());
        dataResource.setCreationTime(new Timestamp(dataResourceModel.getCreationTime()));
        dataResource.setLastModifiedTime(new Timestamp(dataResourceModel.getLastModifiedTime()));
        return dataResource;
    }

    public static DataReplicaLocationModel getDataReplicaLocationModel(DataReplicaLocation replicaLocation){
        if (replicaLocation != null) {
            DataReplicaLocationModel replicaLocationModel = new DataReplicaLocationModel();
            replicaLocationModel.setReplicaId(replicaLocation.getReplicaId());
            replicaLocationModel.setResourceId(replicaLocation.getResourceId());
            replicaLocationModel.setReplicaName(replicaLocation.getReplicaName());
            replicaLocationModel.setReplicaDescription(replicaLocation.getReplicaDescription());
            replicaLocationModel.setCreationTime(replicaLocation.getCreationTime().getTime());
            replicaLocationModel.setLastModifiedTime(replicaLocation.getLastModifiedTime().getTime());
            if(replicaLocation.getDataLocations()!=null && !replicaLocation.getDataLocations().isEmpty()) {
                String[] dataLocations = replicaLocation.getDataLocations().split(",");
                for(String dataLocation : dataLocations){
                    replicaLocationModel.addToDataLocations(dataLocation);
                }
            }
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
        dataReplicaLocation.setDataLocations(StringUtils.join(dataReplicaLocationModel.getDataLocations(), ','));
        dataReplicaLocation.setCreationTime(new Timestamp(dataReplicaLocationModel.getCreationTime()));
        dataReplicaLocation.setLastModifiedTime(new Timestamp(dataReplicaLocationModel.getLastModifiedTime()));
        return dataReplicaLocation;
    }
}