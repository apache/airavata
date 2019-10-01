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
 * Component Programming Interface definition for Apache Airavata Tenant profile Service.
 *
*/

include "../../../airavata-apis/airavata_errors.thrift"
include "../../../airavata-apis/security_model.thrift"
include "../../../data-models/experiment-catalog-models/workspace_model.thrift"
include "../../../data-models/user-tenant-group-models/user_profile_model.thrift"
include "../../../data-models/credential-store-models/credential_store_data_models.thrift"
include "iam_admin_services_cpi_errors.thrift"
include "../../../base-api/base_api.thrift"

namespace java org.apache.airavata.service.profile.iam.admin.services.cpi
namespace php Airavata.Service.Iam.Admin.Services.CPI
namespace py airavata.service.profile.iam.admin.services.cpi

const string IAM_ADMIN_SERVICES_CPI_VERSION = "0.18.0"
const string IAM_ADMIN_SERVICES_CPI_NAME = "IamAdminServices"

service IamAdminServices extends base_api.BaseAPI {

    workspace_model.Gateway setUpGateway (1: required security_model.AuthzToken authzToken,
                       2: required workspace_model.Gateway gateway)
                    throws (1: iam_admin_services_cpi_errors.IamAdminServicesException Idse,
                            2: airavata_errors.AuthorizationException ae)

    bool isUsernameAvailable(1: required security_model.AuthzToken authzToken,
                             2: required string username)
                        throws (1: iam_admin_services_cpi_errors.IamAdminServicesException Idse,
                                                    2: airavata_errors.AuthorizationException ae)

    bool registerUser(1: required security_model.AuthzToken authzToken,
                        2: required string username,
                        3: required string emailAddress,
                        4: required string firstName,
                        5: required string lastName,
                        6: required string newPassword)
                        throws (1: iam_admin_services_cpi_errors.IamAdminServicesException Idse,
                                                    2: airavata_errors.AuthorizationException ae)

    bool enableUser(1: required security_model.AuthzToken authzToken,
                        2: required string username)
                            throws (1: iam_admin_services_cpi_errors.IamAdminServicesException Idse,
                                                        2: airavata_errors.AuthorizationException ae)

    bool isUserEnabled(1: required security_model.AuthzToken authzToken,
                        2: required string username)
                            throws (1: iam_admin_services_cpi_errors.IamAdminServicesException Idse,
                                                        2: airavata_errors.AuthorizationException ae)

    bool isUserExist(1: required security_model.AuthzToken authzToken,
                     2: required string username)
                            throws (1: iam_admin_services_cpi_errors.IamAdminServicesException Idse,
                                                        2: airavata_errors.AuthorizationException ae)

    user_profile_model.UserProfile getUser(1: required security_model.AuthzToken authzToken,
                                           2: required string username)
                            throws (1: iam_admin_services_cpi_errors.IamAdminServicesException Idse,
                                                        2: airavata_errors.AuthorizationException ae)

    list<user_profile_model.UserProfile> getUsers(1: required security_model.AuthzToken authzToken,
                                                  2: required i32 offset,
                                                  3: required i32 limit,
                                                  4: optional string search)
                            throws (1: iam_admin_services_cpi_errors.IamAdminServicesException Idse,
                                                        2: airavata_errors.AuthorizationException ae)

    bool resetUserPassword(1: required security_model.AuthzToken authzToken,
                            2: required string username,
                            3: required string newPassword)
                            throws (1: iam_admin_services_cpi_errors.IamAdminServicesException Idse,
                                                        2: airavata_errors.AuthorizationException ae)

    /*userId can be null*/
    list<user_profile_model.UserProfile> findUsers(1: required security_model.AuthzToken authzToken,
                                2: required string email,
                                3: required string userId)
                                throws (1: iam_admin_services_cpi_errors.IamAdminServicesException Idse,
                                                            2: airavata_errors.AuthorizationException ae)

    void updateUserProfile(1: required security_model.AuthzToken authzToken,
                           2: required user_profile_model.UserProfile userDetails)
        throws (1: iam_admin_services_cpi_errors.IamAdminServicesException Idse,
                2: airavata_errors.AuthorizationException ae)

    bool deleteUser(1: required security_model.AuthzToken authzToken,
                                2: required string username)
                                throws (1: iam_admin_services_cpi_errors.IamAdminServicesException Idse,
                                                            2: airavata_errors.AuthorizationException ae)

    bool addRoleToUser(1: required security_model.AuthzToken authzToken,
                        2: required string username,
                        3: required string roleName)
        throws (1: iam_admin_services_cpi_errors.IamAdminServicesException Idse,
                2: airavata_errors.AuthorizationException ae)

    bool removeRoleFromUser(1: required security_model.AuthzToken authzToken,
                        2: required string username,
                        3: required string roleName)
        throws (1: iam_admin_services_cpi_errors.IamAdminServicesException Idse,
                2: airavata_errors.AuthorizationException ae)

    list<user_profile_model.UserProfile> getUsersWithRole(1: required security_model.AuthzToken authzToken,
                                2: required string roleName)
                                throws (1: iam_admin_services_cpi_errors.IamAdminServicesException Idse,
                                                            2: airavata_errors.AuthorizationException ae)
}
