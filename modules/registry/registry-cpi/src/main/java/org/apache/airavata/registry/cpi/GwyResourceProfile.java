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

import org.apache.airavata.model.appcatalog.gatewayprofile.ComputeResourcePreference;
import org.apache.airavata.model.appcatalog.gatewayprofile.GatewayResourceProfile;
import org.apache.airavata.model.appcatalog.gatewayprofile.StoragePreference;

import java.util.List;

public interface GwyResourceProfile {
    /**
     * This method will add a gateway profile
     * @param gatewayProfile gateway profile
     * @return gateway id
     */
    String addGatewayResourceProfile(GatewayResourceProfile gatewayProfile) throws AppCatalogException;

    /**
     * This method will update a gateway profile
     * @param gatewayId unique gateway id
     * @param updatedProfile updated profile
     */
    void updateGatewayResourceProfile(String gatewayId, GatewayResourceProfile updatedProfile) throws AppCatalogException;

    /**
     *
     * @param gatewayId
     * @return
     */
   GatewayResourceProfile getGatewayProfile (String gatewayId) throws AppCatalogException;

    /**
     * This method will remove a gateway profile
     * @param gatewayId unique gateway id
     * @return true or false
     */
    boolean removeGatewayResourceProfile(String gatewayId) throws AppCatalogException;
    boolean removeComputeResourcePreferenceFromGateway(String gatewayId, String preferenceId) throws AppCatalogException;
    boolean removeDataStoragePreferenceFromGateway(String gatewayId, String preferenceId) throws AppCatalogException;

    /**
     * This method will check whether gateway profile exists
     * @param gatewayId unique gateway id
     * @return true or false
     */
    boolean isGatewayResourceProfileExists(String gatewayId) throws AppCatalogException;

    /**
     *
     * @param gatewayId
     * @param hostId
     * @return ComputeResourcePreference
     */
    ComputeResourcePreference getComputeResourcePreference (String gatewayId, String hostId) throws AppCatalogException;
    StoragePreference getStoragePreference(String gatewayId, String storageId) throws AppCatalogException;

    /**
     *
     * @param gatewayId
     * @return
     */
    List<ComputeResourcePreference> getAllComputeResourcePreferences (String gatewayId) throws AppCatalogException;
    List<StoragePreference> getAllStoragePreferences(String gatewayId) throws AppCatalogException;

    List<String> getGatewayProfileIds (String gatewayName) throws AppCatalogException;
    List<GatewayResourceProfile> getAllGatewayProfiles () throws AppCatalogException;
}
