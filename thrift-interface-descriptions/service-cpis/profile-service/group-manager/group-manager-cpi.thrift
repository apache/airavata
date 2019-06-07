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
 * Component Programming Interface definition for Apache Airavata Group Manager Service.
 *
*/

include "../../../airavata-apis/airavata_errors.thrift"
include "../../../airavata-apis/security_model.thrift"
include "../../../data-models/user-tenant-group-models/group_manager_model.thrift"
include "group_manager_cpi_errors.thrift"
include "../../../base-api/base_api.thrift"

namespace java org.apache.airavata.service.profile.groupmanager.cpi
namespace php Airavata.Service.Profile.Groupmanager.CPI
namespace py airavata.service.profile.groupmanager.cpi

const string GROUP_MANAGER_CPI_VERSION = "0.18.0"
const string GROUP_MANAGER_CPI_NAME = "GroupManagerService"

service GroupManagerService  extends base_api.BaseAPI {

    string getAPIVersion ()
                       throws (1: group_manager_cpi_errors.GroupManagerServiceException gse)

    string createGroup(1: required security_model.AuthzToken authzToken, 2: required group_manager_model.GroupModel groupModel)
                    throws (1: group_manager_cpi_errors.GroupManagerServiceException gse,
                            2: airavata_errors.AuthorizationException ae);

    bool updateGroup(1: required security_model.AuthzToken authzToken, 2: required group_manager_model.GroupModel groupModel)
                     throws (1: group_manager_cpi_errors.GroupManagerServiceException gse,
                             2: airavata_errors.AuthorizationException ae);

    bool deleteGroup(1: required security_model.AuthzToken authzToken, 2: required string groupId, 3: required string ownerId)
                     throws (1: group_manager_cpi_errors.GroupManagerServiceException gse,
                             2: airavata_errors.AuthorizationException ae);

    group_manager_model.GroupModel getGroup(1: required security_model.AuthzToken authzToken, 2: required string groupId)
                      throws (1: group_manager_cpi_errors.GroupManagerServiceException gse,
                              2: airavata_errors.AuthorizationException ae);

    list<group_manager_model.GroupModel> getGroups(1: required security_model.AuthzToken authzToken)
                      throws (1: group_manager_cpi_errors.GroupManagerServiceException gse,
                              2: airavata_errors.AuthorizationException ae);

    list<group_manager_model.GroupModel> getAllGroupsUserBelongs(1: required security_model.AuthzToken authzToken, 2: required string userName)
                       throws (1: group_manager_cpi_errors.GroupManagerServiceException gse,
                               2: airavata_errors.AuthorizationException ae);

    bool addUsersToGroup(1: required security_model.AuthzToken authzToken, 2: required list<string> userIds, 3: required string groupId)
                        throws (1: group_manager_cpi_errors.GroupManagerServiceException gse,
                                2: airavata_errors.AuthorizationException ae);

    bool removeUsersFromGroup(1: required security_model.AuthzToken authzToken, 2: required list<string> userIds, 3: required string groupId)
                        throws (1: group_manager_cpi_errors.GroupManagerServiceException gse,
                                2: airavata_errors.AuthorizationException ae);

    bool transferGroupOwnership(1: required security_model.AuthzToken authzToken, 2: required string groupId, 3: required string newOwnerId)
                    throws (1: group_manager_cpi_errors.GroupManagerServiceException gse,
                            2: airavata_errors.AuthorizationException ae);

    bool addGroupAdmins(1: required security_model.AuthzToken authzToken, 2: required string groupId, 3: required list<string> adminIds)
                    throws (1: group_manager_cpi_errors.GroupManagerServiceException gse,
                            2: airavata_errors.AuthorizationException ae);

    bool removeGroupAdmins(1: required security_model.AuthzToken authzToken, 2: required string groupId, 3: required list<string> adminIds)
                    throws (1: group_manager_cpi_errors.GroupManagerServiceException gse,
                            2: airavata_errors.AuthorizationException ae);

    bool hasAdminAccess(1: required security_model.AuthzToken authzToken, 2: required string groupId, 3: required string adminId)
                    throws (1: group_manager_cpi_errors.GroupManagerServiceException gse,
                            2: airavata_errors.AuthorizationException ae);

    bool hasOwnerAccess(1: required security_model.AuthzToken authzToken, 2: required string groupId, 3: required string ownerId)
                    throws (1: group_manager_cpi_errors.GroupManagerServiceException gse,
                            2: airavata_errors.AuthorizationException ae);

}



