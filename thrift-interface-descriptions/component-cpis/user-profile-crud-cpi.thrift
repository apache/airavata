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


namespace java org.apache.airavata.userprofile.crude.cpi

const string CS_CPI_VERSION = "0.16.0"

service UserProfileCrudeService {

  string addUserProfile (1: required user_profile_model.UserProfile userProfile)
                        throws (1:registry_api_errors.RegistryServiceException registryException);

  string updateUserProfile (1: required user_profile_model.UserProfile userProfile)
                          throws (1:registry_api_errors.RegistryServiceException registryException);

  user_profile_model.UserProfile getUserProfile(1: required string userId)
                                                throws (1:registry_api_errors.RegistryServiceException registryException);

  bool deleteUserProfile(1: required string userId)
                                                  throws (1:registry_api_errors.RegistryServiceException registryException);

}