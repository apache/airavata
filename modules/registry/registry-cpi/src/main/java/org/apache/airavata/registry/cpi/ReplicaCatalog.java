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
package org.apache.airavata.registry.cpi;


/*
Included for backwards compatibility
TODO: Remove interface once registry refactoring is complete
*/
public interface ReplicaCatalog extends DataProductInterface, DataReplicaLocationInterface {
    /*String schema = "airavata-dp";

    String registerDataProduct(DataProductModel product) throws ReplicaCatalogException;

    boolean removeDataProduct(String productUri) throws ReplicaCatalogException;

    boolean updateDataProduct(DataProductModel product) throws ReplicaCatalogException;

    DataProductModel getDataProduct(String productUri) throws ReplicaCatalogException;

    boolean isDataProductExists(String productUri) throws ReplicaCatalogException;

    String registerReplicaLocation(DataReplicaLocationModel dataReplicaLocationModel) throws ReplicaCatalogException;

    boolean removeReplicaLocation(String replicaId) throws ReplicaCatalogException;

    boolean updateReplicaLocation(DataReplicaLocationModel dataReplicaLocationModel) throws ReplicaCatalogException;

    DataReplicaLocationModel getReplicaLocation(String replicaId) throws ReplicaCatalogException;

    List<DataReplicaLocationModel> getAllReplicaLocations(String productUri) throws ReplicaCatalogException;

    DataProductModel getParentDataProduct(String productUri) throws ReplicaCatalogException;

    List<DataProductModel> getChildDataProducts(String productUri) throws ReplicaCatalogException;

    List<DataProductModel> searchDataProductsByName(String gatewayId, String userId, String productName,
                                                    int limit, int offset) throws ReplicaCatalogException;*/
}
