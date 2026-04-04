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
import java.util.Map;
import org.apache.airavata.model.appcatalog.gatewayprofile.proto.StoragePreference;
import org.apache.airavata.model.appcatalog.storageresource.proto.StorageResourceDescription;
import org.apache.airavata.model.data.movement.proto.DataMovementInterface;
import org.apache.airavata.model.data.replica.proto.DataProductModel;
import org.apache.airavata.model.data.replica.proto.DataReplicaLocationModel;

/**
 * SPI contract for storage operations required by the execution engine.
 *
 * <p>This interface decouples the execution module from the storage module's repository
 * implementations. Implementations are expected to be provided by the storage module
 * and injected into execution components.
 */
public interface StorageProvider {

    /**
     * Retrieve a storage resource description by its identifier.
     *
     * @param storageResourceId the unique identifier of the storage resource
     * @return the storage resource description
     * @throws Exception if the resource cannot be found or a data access error occurs
     */
    StorageResourceDescription getStorageResource(String storageResourceId) throws Exception;

    /**
     * Retrieve all registered storage resource names.
     *
     * @return a map of storage resource id to name
     * @throws Exception if a data access error occurs
     */
    Map<String, String> getAllStorageResourceNames() throws Exception;

    /**
     * Register a new data product in the replica catalog.
     *
     * @param dataProductModel the data product to register
     * @return the product URI of the registered data product
     * @throws Exception if a data access error occurs
     */
    String registerDataProduct(DataProductModel dataProductModel) throws Exception;

    /**
     * Retrieve a data product by its product URI.
     *
     * @param productUri the product URI
     * @return the data product model
     * @throws Exception if not found or a data access error occurs
     */
    DataProductModel getDataProduct(String productUri) throws Exception;

    /**
     * Retrieve the parent data product for a given child product URI.
     *
     * @param productUri the child product URI
     * @return the parent data product model
     * @throws Exception if not found or a data access error occurs
     */
    DataProductModel getParentDataProduct(String productUri) throws Exception;

    /**
     * Retrieve all child data products for a given parent product URI.
     *
     * @param productUri the parent product URI
     * @return list of child data product models
     * @throws Exception if a data access error occurs
     */
    List<DataProductModel> getChildDataProducts(String productUri) throws Exception;

    /**
     * Retrieve the gateway storage preference for a specific storage resource.
     *
     * @param gatewayId the gateway identifier
     * @param storageId the storage resource identifier
     * @return the storage preference
     * @throws Exception if not found or a data access error occurs
     */
    StoragePreference getGatewayStoragePreference(String gatewayId, String storageId) throws Exception;

    /**
     * Retrieve all storage preferences for a given gateway.
     *
     * @param gatewayId the gateway identifier
     * @return list of storage preferences
     * @throws Exception if a data access error occurs
     */
    List<StoragePreference> getAllGatewayStoragePreferences(String gatewayId) throws Exception;

    // --- Storage resource management ---

    /**
     * Register a new storage resource.
     *
     * @param description the storage resource description
     * @return the generated storage resource id
     * @throws Exception if a data access error occurs
     */
    String addStorageResource(StorageResourceDescription description) throws Exception;

    /**
     * Update an existing storage resource.
     *
     * @param storageResourceId the storage resource id
     * @param description the updated description
     * @throws Exception if a data access error occurs
     */
    void updateStorageResource(String storageResourceId, StorageResourceDescription description) throws Exception;

    /**
     * Remove a storage resource.
     *
     * @param storageResourceId the storage resource id
     * @throws Exception if a data access error occurs
     */
    void removeStorageResource(String storageResourceId) throws Exception;

    /**
     * Add a data movement interface to a storage resource.
     *
     * @param dataMovementInterface the data movement interface
     * @return the data movement interface id
     * @throws Exception if a data access error occurs
     */
    String addDataMovementInterface(DataMovementInterface dataMovementInterface) throws Exception;

    /**
     * Remove a data movement interface from a storage resource.
     *
     * @param storageResourceId the storage resource id
     * @param dataMovementInterfaceId the data movement interface id
     * @throws Exception if a data access error occurs
     */
    void removeDataMovementInterface(String storageResourceId, String dataMovementInterfaceId) throws Exception;

    // --- Data product operations ---

    /**
     * Search data products by name.
     *
     * @param gatewayId the gateway id
     * @param userId the owner user id
     * @param productName the product name pattern
     * @param limit max results
     * @param offset result offset
     * @return list of matching data products
     * @throws Exception if a data access error occurs
     */
    List<DataProductModel> searchDataProductsByName(
            String gatewayId, String userId, String productName, int limit, int offset) throws Exception;

    /**
     * Register a replica location.
     *
     * @param replicaLocationModel the replica location model
     * @return the replica id
     * @throws Exception if a data access error occurs
     */
    String registerReplicaLocation(DataReplicaLocationModel replicaLocationModel) throws Exception;
}
