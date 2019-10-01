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
 * Component Programming Interface definition for Apache Airavata User profile Service.
 *
*/

include "../../../airavata-apis/airavata_errors.thrift"
include "../../../airavata-apis/security_model.thrift"
include "../../../data-models/user-tenant-group-models/user_profile_model.thrift"
include "profile_user_cpi_errors.thrift"
include "../../../base-api/base_api.thrift"

namespace java org.apache.airavata.service.profile.user.cpi
namespace php Airavata.Service.Profile.User.CPI
namespace py airavata.service.profile.user.cpi

const string USER_PROFILE_CPI_VERSION = "0.18.0"
const string USER_PROFILE_CPI_NAME = "UserProfileService"

service UserProfileService extends base_api.BaseAPI {

  string getAPIVersion ()
                       throws (1: profile_user_cpi_errors.UserProfileServiceException upe)

  /**
   * Create an initial UserProfile based on information in the IAM service for this user.
   */
  string initializeUserProfile (1: required security_model.AuthzToken authzToken)
                            throws (1: profile_user_cpi_errors.UserProfileServiceException upe,
                                    2: airavata_errors.AuthorizationException ae);
  /**
   * This method is deprecated and will be removed in future versions of Airavata. Please use
   * the initializeUserProfile method which fetches the information about the user profile from
   * IAM service - @Deprecated
   */
  string addUserProfile (1: required security_model.AuthzToken authzToken,
                         2: required user_profile_model.UserProfile userProfile)
                      throws (1: profile_user_cpi_errors.UserProfileServiceException upe,
                              2: airavata_errors.AuthorizationException ae);

  bool updateUserProfile (1: required security_model.AuthzToken authzToken,
                          2: required user_profile_model.UserProfile userProfile)
                       throws (1: profile_user_cpi_errors.UserProfileServiceException upe,
                               2: airavata_errors.AuthorizationException ae);

  user_profile_model.UserProfile getUserProfileById (1: required security_model.AuthzToken authzToken,
                                                     2: required string userId,
                                                     3: required string gatewayId)
                                                  throws (1: profile_user_cpi_errors.UserProfileServiceException upe,
                                                          2: airavata_errors.AuthorizationException ae);

  bool deleteUserProfile (1: required security_model.AuthzToken authzToken,
                          2: required string userId,
                          3: required string gatewayId)
                       throws (1: profile_user_cpi_errors.UserProfileServiceException upe,
                               2: airavata_errors.AuthorizationException ae);

  list<user_profile_model.UserProfile> getAllUserProfilesInGateway (1: required security_model.AuthzToken authzToken,
                                                                    2: required string gatewayId,
                                                                    3: required i32 offset,
                                                                    4: required i32 limit)
                                                                 throws (1: profile_user_cpi_errors.UserProfileServiceException upe,
                                                                         2: airavata_errors.AuthorizationException ae);

 bool doesUserExist (1: required security_model.AuthzToken authzToken,
                     2: required string userId,
                     3: required string gatewayId)
                  throws (1: profile_user_cpi_errors.UserProfileServiceException upe,
                          2: airavata_errors.AuthorizationException ae);

}