/**
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
 */
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

package org.apache.airavata.registry.core.replica.catalog.utils;

import org.apache.airavata.model.data.replica.*;
import org.apache.airavata.registry.core.replica.catalog.model.DataProduct;
import org.apache.airavata.registry.core.replica.catalog.model.DataProductMetaData;
import org.apache.airavata.registry.core.replica.catalog.model.DataReplicaLocation;
import org.apache.airavata.registry.core.replica.catalog.model.DataReplicaMetaData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ThriftDataModelConversion {

    private final static Logger logger = LoggerFactory.getLogger(ThriftDataModelConversion.class);

    public static DataProductModel getDataProductModel(DataProduct dataProduct){
        if (dataProduct != null) {
            DataProductModel dataProductModel = new DataProductModel();
            dataProductModel.setProductUri(dataProduct.getProductUri());
            dataProductModel.setGatewayId(dataProduct.getGatewayId());
            dataProductModel.setParentProductUri(dataProduct.getParentProductUri());
            dataProductModel.setProductName(dataProduct.getProductName());
            if(dataProduct.getDataProductType() != null)
                dataProductModel.setDataProductType(DataProductType.valueOf(dataProduct.getDataProductType()));
            else
                dataProductModel.setDataProductType(DataProductType.FILE);
            dataProductModel.setProductDescription(dataProduct.getProductDescription());
            dataProductModel.setOwnerName(dataProduct.getOwnerName());
            dataProductModel.setProductSize(dataProduct.getProductSize());
            if(dataProduct.getCreationTime() != null)
                dataProductModel.setCreationTime(dataProduct.getCreationTime().getTime());
            if(dataProduct.getLastModifiedTime() != null)
                dataProductModel.setLastModifiedTime(dataProduct.getLastModifiedTime().getTime());
            dataProductModel.setProductMetadata(getResourceMetaData(dataProduct.getDataProductMetaData()));
            if(dataProduct.getDataReplicaLocations() != null){
                ArrayList<DataReplicaLocationModel> dataReplicaLocationModels = new ArrayList<>();
                dataProduct.getDataReplicaLocations().stream().forEach(r->dataReplicaLocationModels
                        .add(getDataReplicaLocationModel(r)));
                dataProductModel.setReplicaLocations(dataReplicaLocationModels);
            }
            return dataProductModel;
        }
        return null;
    }

    public static DataProduct getDataProduct(DataProductModel dataProductModel){
        if(dataProductModel != null){
            DataProduct dataProduct = new DataProduct();
            return getUpdatedDataProduct(dataProductModel, dataProduct);
        }
        return null;
    }

    public static DataProduct getUpdatedDataProduct(DataProductModel dataProductModel, DataProduct dataProduct){
        dataProduct.setProductUri(dataProductModel.getProductUri());
        dataProduct.setGatewayId(dataProductModel.getGatewayId());
        dataProduct.setProductName(dataProductModel.getProductName());
        dataProduct.setParentProductUri(dataProductModel.getParentProductUri());
        if(dataProductModel.getDataProductType() != null)
            dataProduct.setDataProductType(dataProductModel.getDataProductType().toString());
        else
            dataProduct.setDataProductType(DataProductType.FILE.toString());
        dataProduct.setProductDescription(dataProductModel.getProductDescription());
        dataProduct.setOwnerName(dataProductModel.getOwnerName());
        dataProduct.setProductSize(dataProductModel.getProductSize());
        if(dataProductModel.getCreationTime() > 0)
            dataProduct.setCreationTime(new Timestamp(dataProductModel.getCreationTime()));
        if(dataProductModel.getLastModifiedTime() > 0)
            dataProduct.setLastModifiedTime(new Timestamp(dataProductModel.getLastModifiedTime()));
        ArrayList<DataProductMetaData> dataProductMetaData = new ArrayList<>();
        if(dataProductModel.getProductMetadata() != null) {
            dataProductModel.getProductMetadata().keySet().stream().forEach(k -> {
                String v = dataProductModel.getProductMetadata().get(k);
                DataProductMetaData temp = new DataProductMetaData();
                temp.setProductUri(dataProduct.getProductUri());
                temp.setKey(k);
                temp.setValue(v);
                dataProductMetaData.add(temp);
            });
            dataProduct.setDataProductMetaData(dataProductMetaData);
        }
        if(dataProductModel.getReplicaLocations() != null){
            ArrayList<DataReplicaLocation> dataReplicaLocations = new ArrayList<>();
            dataProductModel.getReplicaLocations().stream().forEach(r->{
                DataReplicaLocation dataReplicaLocationModel = getDataReplicaLocation(r);
                dataReplicaLocationModel.setProductUri(dataProductModel.getProductUri());
                dataReplicaLocations.add(dataReplicaLocationModel);
            });
            dataProduct.setDataReplicaLocations(dataReplicaLocations);
        }
        return dataProduct;
    }

    public static DataReplicaLocationModel getDataReplicaLocationModel(DataReplicaLocation replicaLocation){
        if (replicaLocation != null) {
            DataReplicaLocationModel replicaLocationModel = new DataReplicaLocationModel();
            replicaLocationModel.setReplicaId(replicaLocation.getReplicaId());
            replicaLocationModel.setProductUri(replicaLocation.getProductUri());
            replicaLocationModel.setReplicaName(replicaLocation.getReplicaName());
            replicaLocationModel.setReplicaDescription(replicaLocation.getReplicaDescription());
            replicaLocationModel.setStorageResourceId(replicaLocation.getStorageResourceId());
            if(replicaLocation.getValidUntilTime() != null)
                replicaLocationModel.setValidUntilTime(replicaLocation.getValidUntilTime().getTime());
            replicaLocationModel.setFilePath(replicaLocation.getFilePath());
            if(replicaLocation.getCreationTime() != null)
                replicaLocationModel.setCreationTime(replicaLocation.getCreationTime().getTime());
            if(replicaLocation.getLastModifiedTime() != null)
                replicaLocationModel.setLastModifiedTime(replicaLocation.getLastModifiedTime().getTime());
            if(replicaLocation.getReplicaLocationCategory() != null)
                replicaLocationModel.setReplicaLocationCategory(ReplicaLocationCategory.valueOf(replicaLocation
                        .getReplicaLocationCategory().toString()));
            if(replicaLocation.getReplicaPersistentType() != null)
                replicaLocationModel.setReplicaPersistentType(ReplicaPersistentType.valueOf(replicaLocation
                        .getReplicaPersistentType().toString()));
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
        dataReplicaLocation.setProductUri(dataReplicaLocationModel.getProductUri());
        dataReplicaLocation.setReplicaName(dataReplicaLocationModel.getReplicaName());
        dataReplicaLocation.setReplicaDescription(dataReplicaLocationModel.getReplicaDescription());
        dataReplicaLocation.setStorageResourceId(dataReplicaLocationModel.getStorageResourceId());
        dataReplicaLocation.setFilePath(dataReplicaLocationModel.getFilePath());
        if(dataReplicaLocationModel.getValidUntilTime() > 0)
            dataReplicaLocation.setValidUntilTime(new Timestamp(dataReplicaLocationModel.getValidUntilTime()));
        if(dataReplicaLocationModel.getCreationTime() > 0)
            dataReplicaLocation.setCreationTime(new Timestamp(dataReplicaLocationModel.getCreationTime()));
        if(dataReplicaLocationModel.getLastModifiedTime() > 0)
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
                temp.setReplicaId(dataReplicaLocationModel.getProductUri());
                temp.setKey(k);
                temp.setValue(v);
                dataReplicaMetadata.add(temp);
            });
            dataReplicaLocation.setDataReplicaMetaData(dataReplicaMetadata);
        }
        return dataReplicaLocation;
    }

    public static Map<String, String> getResourceMetaData(Collection<DataProductMetaData> dataProductMetaData){
        HashMap<String, String> metadata = new HashMap<>();
        if(dataProductMetaData!=null && !dataProductMetaData.isEmpty()) {
            dataProductMetaData.stream().forEach(m -> metadata.put(m.getKey(),m.getValue()));
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