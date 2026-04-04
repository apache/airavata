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
package org.apache.airavata.interfaces;

import java.util.List;
import org.apache.airavata.model.appcatalog.storageresource.proto.StorageResourceDescription;
import org.apache.airavata.model.data.movement.proto.SCPDataMovement;
import org.apache.airavata.model.data.replica.proto.DataProductModel;
import org.apache.airavata.model.data.replica.proto.DataReplicaLocationModel;

/**
 * Registry operations for storage resources, data movement, and data products.
 */
public interface StorageRegistry {

    // --- Storage resource operations ---
    StorageResourceDescription getStorageResource(String storageResourceId) throws Exception;

    // --- Data movement operations ---
    SCPDataMovement getSCPDataMovement(String dataMoveId) throws Exception;

    // --- Data product operations ---
    String registerDataProduct(DataProductModel dataProductModel) throws Exception;

    DataProductModel getDataProduct(String productUri) throws Exception;

    DataProductModel getParentDataProduct(String productUri) throws Exception;

    List<DataProductModel> getChildDataProducts(String productUri) throws Exception;

    String registerReplicaLocation(DataReplicaLocationModel replicaLocationModel) throws Exception;

    List<DataProductModel> searchDataProductsByName(
            String gatewayId, String userId, String productName, int limit, int offset) throws Exception;
}
