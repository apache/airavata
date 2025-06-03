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

import configparser
import logging
from typing import Optional

from airavata_sdk.transport import utils
from airavata_sdk.transport.settings import SharingServerSettings

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

    def __init__(self, configuration_file_location: Optional[str] = None):
        self.settings = SharingServerSettings(configuration_file_location)
        self._load_settings(configuration_file_location)
        self.client = utils.initialize_sharing_registry_client(
            self.settings.SHARING_API_HOST,
            self.settings.SHARING_API_PORT,
            self.settings.SHARING_API_SECURE,
        )
        # expose the needed functions
        self.create_domain = self.client.createDomain
        self.update_domain = self.client.updateDomain
        self.is_domain_exists = self.client.isDomainExists
        self.delete_domain = self.client.deleteDomain
        self.get_domain = self.client.getDomain
        self.get_domains = self.client.getDomains
        self.create_user = self.client.createUser
        self.updated_user = self.client.updatedUser
        self.is_user_exists = self.client.isUserExists
        self.delete_user = self.client.deleteUser
        self.get_user = self.client.getUser
        self.get_users = self.client.getUsers
        self.create_group = self.client.createGroup
        self.update_group = self.client.updateGroup
        self.is_group_exists = self.client.isGroupExists
        self.delete_group = self.client.deleteGroup
        self.get_group = self.client.getGroup
        self.get_groups = self.client.getGroups
        self.add_users_to_group = self.client.addUsersToGroup
        self.remove_users_from_group = self.client.removeUsersFromGroup
        self.transfer_group_ownership = self.client.transferGroupOwnership
        self.add_group_admins = self.client.addGroupAdmins
        self.remove_group_admins = self.client.removeGroupAdmins
        self.has_admin_access = self.client.hasAdminAccess
        self.has_owner_access = self.client.hasOwnerAccess
        self.get_group_members_of_type_user = self.client.getGroupMembersOfTypeUser
        self.get_group_members_of_type_group = self.client.getGroupMembersOfTypeGroup
        self.add_child_groups_to_parent_group = self.client.addChildGroupsToParentGroup
        self.remove_child_group_from_parent_group = self.client.removeChildGroupFromParentGroup
        self.get_all_member_groups_for_user = self.client.getAllMemberGroupsForUser
        self.create_entity_type = self.client.createEntityType
        self.update_entity_type = self.client.updateEntityType
        self.is_entity_type_exists = self.client.isEntityTypeExists
        self.delete_entity_type = self.client.deleteEntityType
        self.get_entity_type = self.client.getEntityType
        self.get_entity_types = self.client.getEntityTypes
        self.create_entity = self.client.createEntity
        self.update_entity = self.client.updateEntity
        self.is_entity_exists = self.client.isEntityExists
        self.delete_entity = self.client.deleteEntity
        self.get_entity = self.client.getEntity
        self.search_entities = self.client.searchEntities
        self.get_list_of_shared_users = self.client.getListOfSharedUsers
        self.get_list_of_directly_shared_users = self.client.getListOfDirectlySharedUsers
        self.get_list_of_shared_groups = self.client.getListOfSharedGroups
        self.get_list_of_directly_shared_groups = self.client.getListOfDirectlySharedGroups
        self.create_permission_type = self.client.createPermissionType
        self.update_permission_type = self.client.updatePermissionType
        self.is_permission_exists = self.client.isPermissionExists
        self.delete_permission_type = self.client.deletePermissionType
        self.get_permission_type = self.client.getPermissionType
        self.get_permission_types = self.client.getPermissionTypes
        self.share_entity_with_users = self.client.shareEntityWithUsers
        self.revoke_entity_sharing_from_users = self.client.revokeEntitySharingFromUsers
        self.share_entity_with_groups = self.client.shareEntityWithGroups
        self.revoke_entity_sharing_from_groups = self.client.revokeEntitySharingFromGroups
        self.user_has_access = self.client.userHasAccess


    def _load_settings(self, configuration_file_location: Optional[str]):
        if configuration_file_location is not None:
            config = configparser.ConfigParser()
            config.read(configuration_file_location)
            self.settings.SHARING_API_HOST = config.get('SharingServer', 'SHARING_API_HOST')
            self.settings.SHARING_API_PORT = config.getint('SharingServer', 'SHARING_API_PORT')
            self.settings.SHARING_API_SECURE = config.getboolean('SharingServer', 'SHARING_API_SECURE')
