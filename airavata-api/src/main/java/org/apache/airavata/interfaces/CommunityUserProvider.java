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

/**
 * SPI for community user persistence, decoupling credential-service from iam-service.
 * Implementations are discovered at runtime via Spring dependency injection.
 */
public interface CommunityUserProvider {

    /**
     * Saves or replaces a community user record for the given gateway and token.
     *
     * @param gatewayId the gateway name/ID
     * @param tokenId   the credential token
     * @param userName  the community username
     * @param userEmail the community user email
     */
    void saveCommunityUser(String gatewayId, String tokenId, String userName, String userEmail);
}
