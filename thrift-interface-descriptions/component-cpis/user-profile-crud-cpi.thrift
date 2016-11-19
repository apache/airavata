/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

/*
 * Component Programming Interface definition for Apache Airavata GFac Service.
 *
*/

include "../data-models/user-group-models/user_profile_model.thrift"
include "registry_api_errors.thrift"


namespace java org.apache.airavata.userprofile.crude.cpi

const string USER_PROFILE_CPI_VERSION = "0.16.0"

service UserProfileCrudeService {

  string addUserProfile (1: required user_profile_model.UserProfile userProfile)
                        throws (1:registry_api_errors.RegistryServiceException registryException);

  bool updateUserProfile (1: required user_profile_model.UserProfile userProfile)
                          throws (1:registry_api_errors.RegistryServiceException registryException);

  user_profile_model.UserProfile getUserProfileById(1: required string userId, 2: required string gatewayId)
                                                throws (1:registry_api_errors.RegistryServiceException registryException);

  bool deleteUserProfile(1: required string userId)
                                                  throws (1:registry_api_errors.RegistryServiceException registryException);

  list<user_profile_model.UserProfile> getAllUserProfilesInGateway (1: required string gatewayId, 2: required i32 offset, 3: required i32 limit)
                          throws (1:registry_api_errors.RegistryServiceException registryException);

 user_profile_model.UserProfile getUserProfileByName(1: required string userName, 2: required string gatewayId)
                                                throws (1:registry_api_errors.RegistryServiceException registryException);

   bool doesUserExist(1: required string userName, 2: required string gatewayId)
                                                   throws (1:registry_api_errors.RegistryServiceException registryException);


}