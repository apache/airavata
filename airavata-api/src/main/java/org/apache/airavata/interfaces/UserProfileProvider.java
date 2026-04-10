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

import org.apache.airavata.model.user.proto.UserProfile;

/**
 * Abstraction for looking up user profiles, allowing the task framework
 * (in root airavata-api) to retrieve user data without depending on iam-service.
 */
public interface UserProfileProvider {

    /**
     * Retrieve a user profile by user ID and gateway ID.
     *
     * @param userId    the user identifier
     * @param gatewayId the gateway identifier
     * @return the user profile, or null if not found
     */
    UserProfile getUserProfileByIdAndGateWay(String userId, String gatewayId);
}
