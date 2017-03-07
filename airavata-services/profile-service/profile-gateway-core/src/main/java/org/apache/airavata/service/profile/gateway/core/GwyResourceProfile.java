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

package org.apache.airavata.service.profile.gateway.core;

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
    String addGatewayResourceProfile(GatewayResourceProfile gatewayProfile) throws Exception;

    /**
     * This method will update a gateway profile
     * @param gatewayId unique gateway id
     * @param updatedProfile updated profile
     */
    void updateGatewayResourceProfile(String gatewayId, GatewayResourceProfile updatedProfile) throws Exception;

    /**
     *
     * @param gatewayId
     * @return
     */
   GatewayResourceProfile getGatewayProfile(String gatewayId) throws Exception;

    /**
     * This method will remove a gateway profile
     * @param gatewayId unique gateway id
     * @return true or false
     */
    boolean removeGatewayResourceProfile(String gatewayId) throws Exception;

    /**
     * This method will check whether gateway profile exists
     * @param gatewayId unique gateway id
     * @return true or false
     */
    boolean isGatewayResourceProfileExists(String gatewayId) throws Exception;

    /**
     * This method will get gateway profile ids
     * @param gatewayName
     * @return
     * @throws Exception
     */
    List<String> getGatewayProfileIds(String gatewayName) throws Exception;

    /**
     * This method will return all gateway profiles
     * @return
     * @throws Exception
     */
    List<GatewayResourceProfile> getAllGatewayProfiles() throws Exception;
}
