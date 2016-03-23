/**
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


import org.apache.airavata.model.data.product.DataProductModel;
import org.apache.airavata.model.data.product.DataReplicaLocationModel;

import java.util.List;

public interface DataCatalog {
    String schema = "airavata-dp";

    String registerDataProduct(DataProductModel product) throws DataCatalogException;

    boolean removeDataProduct(String productUri) throws DataCatalogException;

    boolean updateDataProduct(DataProductModel product) throws DataCatalogException;

    DataProductModel getDataProduct(String productUri) throws DataCatalogException;

    String registerReplicaLocation(DataReplicaLocationModel dataReplicaLocationModel) throws DataCatalogException;

    boolean removeReplicaLocation(String replicaId) throws DataCatalogException;

    boolean updateReplicaLocation(DataReplicaLocationModel dataReplicaLocationModel) throws DataCatalogException;

    DataReplicaLocationModel getReplicaLocation(String replicaId) throws DataCatalogException;

    List<DataReplicaLocationModel> getAllReplicaLocations(String productUri) throws DataCatalogException;
}
