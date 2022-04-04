#  Licensed to the Apache Software Foundation (ASF) under one or more
#  contributor license agreements.  See the NOTICE file distributed with
#  this work for additional information regarding copyright ownership.
#  The ASF licenses this file to You under the Apache License, Version 2.0
#  (the "License"); you may not use this file except in compliance with
#  the License.  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.
#

import logging
import configparser

from airavata_sdk.transport.settings import SharingAPIClientSettings
from airavata_sdk.transport import utils

from airavata.api.error.ttypes import TException

logger = logging.getLogger(__name__)
logger.setLevel(logging.DEBUG)
# create console handler with a higher log level
handler = logging.StreamHandler()
handler.setLevel(logging.DEBUG)
# create formatter and add it to the handler
formatter = logging.Formatter('%(asctime)s - %(name)s - %(levelname)s - %(message)s')
handler.setFormatter(formatter)
# add the handler to the logger
logger.addHandler(handler)


class SharingRegistryClient(object):

    def __init__(self, configuration_file_location=None):
        self.sharing_registry_client_settings = SharingAPIClientSettings(configuration_file_location)
        self._load_settings(configuration_file_location)
        self.sharing_registry_client_pool = utils.initialize_sharing_registry_client(
            self.sharing_registry_client_settings.SHARING_API_HOST,
            self.sharing_registry_client_settings.SHARING_API_PORT,
            self.sharing_registry_client_settings.SHARING_API_SECURE)

    def create_domain(self, domain):
        """
        <p>API method to create a new domain</p>

        Parameters:
         - domain
        """
        try:
            return self.sharing_registry_client_pool.createDomain(domain)
        except TException:
            logger.exception("Error occurred in create_domain, ", )
            raise

    def update_domain(self, domain):
        """
        <p>API method to update a domain</p>

        Parameters:
         - domain
        """
        try:
            return self.sharing_registry_client_pool.updateDomain(domain)
        except TException:
            logger.exception("Error occurred in update_domain, ", )
            raise

    def is_domain_exists(self, domain_id):
        """
        <p>API method to check Domain Exists</p>

        Parameters:
         - domainId
        """
        try:
            return self.sharing_registry_client_pool.isDomainExists(domain_id)
        except TException:
            logger.exception("Error occurred in is_domain_exists, ", )
            raise

    def delete_domain(self, domain_id):
        """
        <p>API method to delete domain</p>

        Parameters:
         - domainId
        """
        try:
            return self.sharing_registry_client_pool.deleteDomain(domain_id)
        except TException:
            logger.exception("Error occurred in delete_domain, ", )
            raise

    def get_domain(self, domain_id):
        """
        <p>API method to retrieve a domain</p>

        Parameters:
         - domainId
        """
        try:
            return self.sharing_registry_client_pool.getDomain(domain_id)
        except TException:
            logger.exception("Error occurred in get_domain, ", )
            raise

    def get_domains(self, offset, limit):
        """
        <p>API method to get all domain.</p>

        Parameters:
         - offset
         - limit
        """
        try:
            return self.sharing_registry_client_pool.getDomains(offset, limit)
        except TException:
            logger.exception("Error occurred in get_domains, ", )
            raise

    def create_user(self, user):
        """
        <p>API method to register a user in the system</p>

        Parameters:
         - user
        """
        try:
            return self.sharing_registry_client_pool.createUser(user)
        except TException:
            logger.exception("Error occurred in create_user, ", )
            raise

    def updated_user(self, user):
        """
        <p>API method to update existing user</p>

        Parameters:
         - user
        """
        try:
            return self.sharing_registry_client_pool.updatedUser(user)
        except TException:
            logger.exception("Error occurred in updated_user, ", )
            raise

    def is_user_exists(self, domain_id, user_id):
        """
        <p>API method to check User Exists</p>

        Parameters:
         - domainId
         - userId
        """
        try:
            return self.sharing_registry_client_pool.isUserExists(domain_id, user_id)
        except TException:
            logger.exception("Error occurred in is_user_exists, ", )
            raise

    def delete_user(self, domain_id, user_id):
        """
        <p>API method to delete user</p>

        Parameters:
         - domainId
         - userId
        """
        try:
            return self.sharing_registry_client_pool.deleteUser(domain_id, user_id)
        except TException:
            logger.exception("Error occurred in delete_user, ", )
            raise

    def get_user(self, domain_id, user_id):
        """
        <p>API method to get a user</p>

        Parameters:
         - domainId
         - userId
        """
        try:
            return self.sharing_registry_client_pool.getUser(domain_id, user_id)
        except TException:
            logger.exception("Error occurred in get_user, ", )
            raise

    def get_users(self, domain_id, offset, limit):
        """
        <p>API method to get a list of users in a specific domain.</p>
        <li>domainId : Domain id</li>
        <li>offset : Starting result number</li>
        <li>limit : Number of max results to be sent</li>

        Parameters:
         - domainId
         - offset
         - limit
        """
        try:
            return self.sharing_registry_client_pool.getUsers(domain_id, offset, limit)
        except TException:
            logger.exception("Error occurred in get_users, ", )
            raise

    def create_group(self, group):
        """
        <p>API method to create a new group</p>

        Parameters:
         - group
        """
        try:
            return self.sharing_registry_client_pool.createGroup(group)
        except TException:
            logger.exception("Error occurred in create_group, ", )
            raise

    def update_group(self, group):
        """
        <p>API method to update a group</p>

        Parameters:
         - group
        """
        try:
            return self.sharing_registry_client_pool.updateGroup(group)
        except TException:
            logger.exception("Error occurred in update_group, ", )
            raise

    def is_group_exists(self, domain_id, group_id):
        """
        <p>API method to check Group Exists</p>

        Parameters:
         - domainId
         - groupId
        """
        try:
            return self.sharing_registry_client_pool.isGroupExists(domain_id, group_id)
        except TException:
            logger.exception("Error occurred in is_group_exists, ", )
            raise

    def delete_group(self, domain_id, group_id):
        """
        <p>API method to delete a group</p>

        Parameters:
         - domainId
         - groupId
        """
        try:
            return self.sharing_registry_client_pool.deleteGroup(domain_id, group_id)
        except TException:
            logger.exception("Error occurred in delete_group, ", )
            raise

    def get_group(self, domain_id, group_id):
        """
        <p>API method to get a group</p>

        Parameters:
         - domainId
         - groupId
        """
        try:
            return self.sharing_registry_client_pool.getGroup(domain_id, group_id)
        except TException:
            logger.exception("Error occurred in get_group, ", )
            raise

    def get_groups(self, domain_id, offset, limit):
        """
        <p>API method to get groups in a domainId.</p>

        Parameters:
         - domainId
         - offset
         - limit
        """
        try:
            return self.sharing_registry_client_pool.getGroups(domain_id, offset, limit)
        except TException:
            logger.exception("Error occurred in get_groups, ", )
            raise

    def add_users_to_group(self, domain_id, user_ids, group_id):
        """
        <p>API method to add list of users to a group</p>

        Parameters:
         - domainId
         - userIds
         - groupId
        """
        try:
            return self.sharing_registry_client_pool.addUsersToGroup(domain_id, user_ids, group_id)
        except TException:
            logger.exception("Error occurred in add_users_to_group, ", )
            raise

    def remove_users_from_group(self, domain_id, user_ids, group_id):
        """
        <p>API method to remove users from a group</p>

        Parameters:
         - domainId
         - userIds
         - groupId
        """
        try:
            return self.sharing_registry_client_pool.removeUsersFromGroup(domain_id, user_ids, group_id)
        except TException:
            logger.exception("Error occurred in remove_users_from_group, ", )
            raise

    def transfer_group_ownership(self, domain_id, group_id, new_owner_id):
        """
        <p>API method to transfer group ownership</p>

        Parameters:
         - domainId
         - groupId
         - newOwnerId
        """
        try:
            return self.sharing_registry_client_pool.transferGroupOwnership(domain_id, group_id, new_owner_id)
        except TException:
            logger.exception("Error occurred in transfer_group_ownership, ", )
            raise

    def add_group_admins(self, domain_id, group_id, admin_ids):
        """
        <p>API method to add Admin for a group</p>

        Parameters:
         - domainId
         - groupId
         - adminIds
        """
        try:
            return self.sharing_registry_client_pool.addGroupAdmins(domain_id, group_id, admin_ids)
        except TException:
            logger.exception("Error occurred in add_group_admins, ", )
            raise

    def remove_group_admins(self, domain_id, group_id, admin_ids):
        """
        <p>API method to remove Admin for a group</p>

        Parameters:
         - domainId
         - groupId
         - adminIds
        """
        try:
            return self.sharing_registry_client_pool.removeGroupAdmins(domain_id, group_id, admin_ids)
        except TException:
            logger.exception("Error occurred in remove_group_admins, ", )
            raise

    def has_admin_access(self, domain_id, group_id, admin_id):
        """
        <p>API method to check whether the user has Admin access for the group</p>

        Parameters:
         - domainId
         - groupId
         - adminId
        """
        try:
            return self.sharing_registry_client_pool.hasAdminAccess(domain_id, group_id, admin_id)
        except TException:
            logger.exception("Error occurred in has_admin_access, ", )
            raise

    def has_owner_access(self, domain_id, group_id, owner_id):
        """
        <p>API method to check whether the user has Admin access for the group</p>

        Parameters:
         - domainId
         - groupId
         - ownerId
        """
        try:
            return self.sharing_registry_client_pool.hasOwnerAccess(domain_id, group_id, owner_id)
        except TException:
            logger.exception("Error occurred in has_owner_access, ", )
            raise

    def get_group_members_of_type_user(self, domain_id, group_id, offset, limit):
        """
        <p>API method to get list of child users in a group. Only the direct members will be returned.</p>

        Parameters:
         - domainId
         - groupId
         - offset
         - limit
        """
        try:
            return self.sharing_registry_client_pool.getGroupMembersOfTypeUser(domain_id, group_id, offset, limit)
        except TException:
            logger.exception("Error occurred in get_group_members_of_type_user, ", )
            raise

    def get_group_members_of_type_group(self, domain_id, group_id, offset, limit):
        """
        <p>API method to get list of child groups in a group. Only the direct members will be returned.</p>

        Parameters:
         - domainId
         - groupId
         - offset
         - limit
        """
        try:
            return self.sharing_registry_client_pool.getGroupMembersOfTypeGroup(domain_id, group_id, offset, limit)
        except TException:
            logger.exception("Error occurred in get_group_members_of_type_group, ", )
            raise

    def add_child_groups_to_parent_group(self, domain_id, child_ids, group_id):
        """
        <p>API method to add a child group to a parent group.</p>

        Parameters:
         - domainId
         - childIds
         - groupId
        """
        try:
            return self.sharing_registry_client_pool.addChildGroupsToParentGroup(domain_id, child_ids, group_id)
        except TException:
            logger.exception("Error occurred in add_child_groups_to_parent_group, ", )
            raise

    def remove_child_group_from_parent_group(self, domain_id, child_id, group_id):
        """
        <p>API method to remove a child group from parent group.</p>

        Parameters:
         - domainId
         - childId
         - groupId
        """
        try:
            return self.sharing_registry_client_pool.removeChildGroupFromParentGroup(domain_id, child_id, group_id)
        except TException:
            logger.exception("Error occurred in remove_child_group_from_parent_group, ", )
            raise

    def get_all_member_groups_for_user(self, domain_id, user_id):
        """
        Parameters:
         - domainId
         - userId
        """
        try:
            return self.sharing_registry_client_pool.getAllMemberGroupsForUser(domain_id, user_id)
        except TException:
            logger.exception("Error occurred in get_all_member_groups_for_user, ", )
            raise

    def create_entity_type(self, entity_type):
        """
        <p>API method to create a new entity type</p>

        Parameters:
         - entityType
        """
        try:
            return self.sharing_registry_client_pool.createEntityType(entity_type)
        except TException:
            logger.exception("Error occurred in create_entity_type, ", )
            raise

    def update_entity_type(self, entity_type):
        """
        <p>API method to update entity type</p>

        Parameters:
         - entityType
        """
        try:
            return self.sharing_registry_client_pool.updateEntityType(entity_type)
        except TException:
            logger.exception("Error occurred in update_entity_type, ", )
            raise

    def is_entity_type_exists(self, domain_id, entity_type_id):
        """
        <p>API method to check EntityType Exists</p>

        Parameters:
         - domainId
         - entityTypeId
        """
        try:
            return self.sharing_registry_client_pool.isEntityTypeExists(domain_id, entity_type_id)
        except TException:
            logger.exception("Error occurred in is_entity_type_exists, ", )
            raise

    def delete_entity_type(self, domain_id, entity_type_id):
        """
        <p>API method to delete entity type</p>

        Parameters:
         - domainId
         - entityTypeId
        """
        try:
            return self.sharing_registry_client_pool.deleteEntityType(domain_id, entity_type_id)
        except TException:
            logger.exception("Error occurred in delete_entity_type, ", )
            raise

    def get_entity_type(self, domain_id, entity_type_id):
        """
        <p>API method to get an entity type</p>

        Parameters:
         - domainId
         - entityTypeId
        """
        try:
            return self.sharing_registry_client_pool.getEntityType(domain_id, entity_type_id)
        except TException:
            logger.exception("Error occurred in get_entity_type, ", )
            raise

    def get_entity_types(self, domain_id, offset, limit):
        """
        <p>API method to get entity types in a domainId.</p>

        Parameters:
         - domainId
         - offset
         - limit
        """
        try:
            return self.sharing_registry_client_pool.getEntityTypes(domain_id, offset, limit)
        except TException:
            logger.exception("Error occurred in get_entity_types, ", )
            raise

    def create_entity(self, entity):
        """
        <p>API method to register new entity</p>

        Parameters:
         - entity
        """
        try:
            return self.sharing_registry_client_pool.createEntity(entity)
        except TException:
            logger.exception("Error occurred in create_entity, ", )
            raise

    def update_entity(self, entity):
        """
        <p>API method to update entity</p>

        Parameters:
         - entity
        """
        try:
            return self.sharing_registry_client_pool.updateEntity(entity)
        except TException:
            logger.exception("Error occurred in update_entity, ", )
            raise

    def is_entity_exists(self, domain_id, entity_id):
        """
        <p>API method to check Entity Exists</p>

        Parameters:
         - domainId
         - entityId
        """
        try:
            return self.sharing_registry_client_pool.isEntityExists(domain_id, entity_id)
        except TException:
            logger.exception("Error occurred in is_entity_exists, ", )
            raise

    def delete_entity(self, domain_id, entity_id):
        """
        <p>API method to delete entity</p>

        Parameters:
         - domainId
         - entityId
        """
        try:
            return self.sharing_registry_client_pool.deleteEntity(domain_id, entity_id)
        except TException:
            logger.exception("Error occurred in delete_entity, ", )
            raise

    def get_entity(self, domain_id, entity_id):
        """
        <p>API method to get entity</p>

        Parameters:
         - domainId
         - entityId
        """
        try:
            return self.sharing_registry_client_pool.getEntity(domain_id, entity_id)
        except TException:
            logger.exception("Error occurred in get_entity, ", )
            raise

    def search_entities(self, domain_id, user_id, filters, offset, limit):
        """
        <p>API method to search entities</p>

        Parameters:
         - domainId
         - userId
         - filters
         - offset
         - limit
        """
        try:
            return self.sharing_registry_client_pool.searchEntities(domain_id, user_id, filters, offset, limit)
        except TException:
            logger.exception("Error occurred in search_entities, ", )
            raise

    def get_list_of_shared_users(self, domain_id, entity_id, permission_type_id):
        """
        <p>API method to get a list of shared users given the entity id</p>

        Parameters:
         - domainId
         - entityId
         - permissionTypeId
        """
        try:
            return self.sharing_registry_client_pool.getListOfSharedUsers(domain_id, entity_id, permission_type_id)
        except TException:
            logger.exception("Error occurred in get_list_of_shared_users, ", )
            raise

    def get_list_of_directly_shared_users(self, domain_id, entity_id, permission_type_id):
        """
        <p>API method to get a list of shared users given the entity id where the sharing type is directly applied</p>

        Parameters:
         - domainId
         - entityId
         - permissionTypeId
        """
        try:
            return self.sharing_registry_client_pool.getListOfDirectlySharedUsers(domain_id, entity_id,
                                                                                  permission_type_id)
        except TException:
            logger.exception("Error occurred in get_list_of_directly_shared_users, ", )
            raise

    def get_list_of_shared_groups(self, domain_id, entity_id, permission_type_id):
        """
        <p>API method to get a list of shared groups given the entity id</p>

        Parameters:
         - domainId
         - entityId
         - permissionTypeId
        """
        try:
            return self.sharing_registry_client_pool.getListOfSharedGroups(domain_id, entity_id, permission_type_id)
        except TException:
            logger.exception("Error occurred in get_list_of_shared_groups, ", )
            raise

    def get_list_of_directly_shared_groups(self, domain_id, entity_id, permission_type_id):
        """
        <p>API method to get a list of directly shared groups given the entity id where the sharing type is directly applied</p>

        Parameters:
         - domainId
         - entityId
         - permissionTypeId
        """
        try:
            return self.sharing_registry_client_pool.getListOfDirectlySharedGroups(domain_id, entity_id,
                                                                                   permission_type_id)
        except TException:
            logger.exception("Error occurred in get_list_of_directly_shared_groups, ", )
            raise

    def create_permission_type(self, permission_type):
        """
        <p>API method to create permission type</p>

        Parameters:
         - permissionType
        """
        try:
            return self.sharing_registry_client_pool.createPermissionType(permission_type)
        except TException:
            logger.exception("Error occurred in create_permission_type, ", )
            raise

    def update_permission_type(self, permission_type):
        """
        <p>API method to update permission type</p>

        Parameters:
         - permissionType
        """
        try:
            return self.sharing_registry_client_pool.updatePermissionType(permission_type)
        except TException:
            logger.exception("Error occurred in update_permission_type, ", )
            raise

    def is_permission_exists(self, domain_id, permission_id):
        """
        <p>API method to check Permission Exists</p>

        Parameters:
         - dimainId
         - permissionId
        """
        try:
            return self.sharing_registry_client_pool.isPermissionExists(domain_id, permission_id)
        except TException:
            logger.exception("Error occurred in is_permission_exists, ", )
            raise

    def delete_permission_type(self, domain_id, permission_type_id):
        """
        <p>API method to delete permission type</p>

        Parameters:
         - domainId
         - permissionTypeId
        """
        try:
            return self.sharing_registry_client_pool.deletePermissionType(domain_id, permission_type_id)
        except TException:
            logger.exception("Error occurred in delete_permission_type, ", )
            raise

    def get_permission_type(self, domain_id, permission_type_id):
        """
        <p>API method to get permission type</p>

        Parameters:
         - domainId
         - permissionTypeId
        """
        try:
            return self.sharing_registry_client_pool.getPermissionType(domain_id, permission_type_id)
        except TException:
            logger.exception("Error occurred in get_permission_type, ", )
            raise

    def get_permission_types(self, domain_id, offset, limit):
        """
        <p>API method to get list of permission types in a given domainId.</p>

        Parameters:
         - domainId
         - offset
         - limit
        """
        try:
            return self.sharing_registry_client_pool.getPermissionTypes(domain_id, offset, limit)
        except TException:
            logger.exception("Error occurred in get_permission_types, ", )
            raise

    def share_entity_with_users(self, domain_id, entity_id, user_list, permission_type_id, cascade_permission):
        """
        <p>API method to share an entity with users</p>

        Parameters:
         - domainId
         - entityId
         - userList
         - permissionTypeId
         - cascadePermission
        """
        try:
            return self.sharing_registry_client_pool.shareEntityWithUsers(domain_id, entity_id, user_list,
                                                                          permission_type_id, cascade_permission)
        except TException:
            logger.exception("Error occurred in share_entity_with_users, ", )
            raise

    def revoke_entity_sharing_from_users(self, domain_id, entity_id, user_list, permission_type_id):
        """
        <p>API method to revoke sharing from a list of users</p>

        Parameters:
         - domainId
         - entityId
         - userList
         - permissionTypeId
        """
        try:
            return self.sharing_registry_client_pool.revokeEntitySharingFromUsers(domain_id, entity_id, user_list,
                                                                                  permission_type_id)
        except TException:
            logger.exception("Error occurred in revoke_entity_sharing_from_users, ", )
            raise

    def share_entity_with_groups(self, domain_id, entity_id, group_list, permission_type_id, cascade_permission):
        """
        <p>API method to share an entity with list of groups</p>

        Parameters:
         - domainId
         - entityId
         - groupList
         - permissionTypeId
         - cascadePermission
        """
        try:
            return self.sharing_registry_client_pool.shareEntityWithGroups(domain_id, entity_id, group_list,
                                                                           permission_type_id, cascade_permission)
        except TException:
            logger.exception("Error occurred in share_entity_with_groups, ", )
            raise

    def revoke_entity_sharing_from_groups(self, domain_id, entity_id, group_list, permission_type_id):
        """
        <p>API method to revoke sharing from list of users</p>

        Parameters:
         - domainId
         - entityId
         - groupList
         - permissionTypeId
        """
        try:
            return self.sharing_registry_client_pool.revokeEntitySharingFromGroups(domain_id, entity_id, group_list,
                                                                                   permission_type_id)
        except TException:
            logger.exception("Error occurred in revoke_entity_sharing_from_groups, ", )
            raise

    def user_has_access(self, domain_id, user_id, entity_id, permission_type_id):
        """
        <p>API method to check whether a user has access to a specific entity</p>

        Parameters:
         - domainId
         - userId
         - entityId
         - permissionTypeId
        """
        try:
            return self.sharing_registry_client_pool.userHasAccess(domain_id, user_id, entity_id, permission_type_id)
        except TException:
            logger.exception("Error occurred in user_has_access, ", )
            raise

    def _load_settings(self, configuration_file_location):
        if configuration_file_location is not None:
            config = configparser.ConfigParser()
            config.read(configuration_file_location)
            self.sharing_registry_client_settings.SHARING_API_HOST = config.get('SharingServer', 'SHARING_API_HOST')
            self.sharing_registry_client_settings.SHARING_API_PORT = config.getint('SharingServer', 'SHARING_API_PORT')
            self.sharing_registry_client_settings.SHARING_API_SECURE = config.getboolean('SharingServer',
                                                                                         'SHARING_API_SECURE')
