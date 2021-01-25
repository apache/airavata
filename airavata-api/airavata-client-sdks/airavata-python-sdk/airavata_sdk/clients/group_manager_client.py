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

from airavata_sdk.transport.settings import GroupManagerClientSettings
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


class GroupManagerClient(object):

    def __init__(self, configuration_file_location=None):
        self.group_manager_settings = GroupManagerClientSettings(configuration_file_location)
        self._load_settings(configuration_file_location)
        self.group_manager_client_pool = utils.initialize_group_manager_client(
            self.group_manager_settings.PROFILE_SERVICE_HOST,
            self.group_manager_settings.PROFILE_SERVICE_PORT,
            self.group_manager_settings.PROFILE_SERVICE_SECURE)

    def get_api_version(self):
        try:
            return self.group_manager_client_pool.getAPIVersion()
        except TException:
            logger.exception("Error occurred in get_api_version, ")
            raise

    def create_group(self, authz_token, group_model):
        """
        Parameters:
         - authz_token
         - group_model
        """
        try:
            return self.group_manager_client_pool.createGroup(authz_token, group_model)
        except TException:
            logger.exception("Error occurred in create_group, ")
            raise

    def update_group(self, authz_token, group_model):
        """
        Parameters:
         - authz_token
         - group_model
        """
        try:
            return self.group_manager_client_pool.updateGroup(authz_token, group_model)
        except TException:
            logger.exception("Error occurred in update_group, ")
            raise

    def delete_group(self, authz_token, group_id, owner_id):
        """
        Parameters:
         - authz_token
         - group_id
         - owner_id
        """
        try:
            return self.group_manager_client_pool.deleteGroup(authz_token, group_id, owner_id)
        except TException:
            logger.exception("Error occurred in delete_group,")
            raise

    def get_group(self, authz_token, group_id):
        """
        Parameters:
         - authz_token
         - group_id
        """
        try:
            return self.group_manager_client_pool.getGroup(authz_token, group_id)
        except TException:
            logger.exception("Error occurred in get_group, ")
            raise

    def get_groups(self, authz_token):
        """
        Parameters:
         - authz_token
        """
        try:
            return self.group_manager_client_pool.getGroups(authz_token)
        except TException:
            logger.exception("Error occurred in get_groups, ")
            raise

    def get_all_groups_user_belongs(self, authz_token, user_name):
        """
        Parameters:
         - authz_token
         - user_name
        """
        try:
            return self.group_manager_client_pool.getAllGroupsUserBelongs(authz_token, user_name)
        except TException:
            logger.exception("Error occurred in get_all_groups_user_belongs, ")
            raise

    def add_users_to_group(self, authz_token, user_ids, group_id):
        """
        Parameters:
         - authz_token
         - user_ids
         - group_id
        """
        try:
            return self.group_manager_client_pool.addUsersToGroup(authz_token, user_ids, group_id)
        except TException:
            logger.exception("Error occurred in add_users_to_group, ")
            raise

    def remove_users_from_group(self, authz_token, user_ids, group_id):
        """
        Parameters:
         - authz_token
         - user_ids
         - group_id
        """
        try:
            return self.group_manager_client_pool.removeUsersFromGroup(authz_token, user_ids, group_id)
        except TException:
            logger.exception("Error occurred in remove_users_from_group, ")
            raise

    def transfer_group_ownership(self, authz_token, group_id, new_owner_id):
        """
        Parameters:
         - authzToken
         - groupId
         - newOwnerId
        """
        try:
            return self.group_manager_client_pool.transferGroupOwnership(authz_token, group_id, new_owner_id)
        except TException:
            logger.exception("Error occurred in transfer_group_ownership, ")
            raise

    def add_group_admins(self, authz_token, group_id, admin_ids):
        """
        Parameters:
         - authzToken
         - group_id
         - admin_ids
        """
        try:
            return self.group_manager_client_pool.addGroupAdmins(authz_token, group_id, admin_ids)
        except TException:
            logger.exception("Error occurred in add_group_admins, ")
            raise

    def remove_group_admins(self, authz_token, group_id, admin_ids):
        """
        Parameters:
         - authz_token
         - group_id
         - admin_ids
        """
        try:
            return self.group_manager_client_pool.removeGroupAdmins(authz_token, group_id, admin_ids)
        except TException:
            logger.exception("Error occurred in remove_group_admins, ")
            raise

    def has_admin_access(self, authz_token, group_id, admin_id):
        """
        Parameters:
         - authz_token
         - group_id
         - admin_id
        """
        try:
            return self.group_manager_client_pool.hasAdminAccess(authz_token, group_id, admin_id)
        except TException:
            logger.exception("Error occurred in has_admin_access, ")
            raise

    def has_owner_access(self, authz_token, group_id, owner_id):
        """
        Parameters:
         - authz_token
         - group_id
         - owner_id
        """
        try:
            return self.group_manager_client_pool.hasOwnerAccess(authz_token, group_id, owner_id)
        except TException:
            logger.exception("Error occurred in has_owner_access, ")
            raise

    def _load_settings(self, configuration_file_location):
        if configuration_file_location is not None:
            config = configparser.ConfigParser()
            config.read(configuration_file_location)
            self.group_manager_settings.PROFILE_SERVICE_HOST = config.get('ProfileServer', 'PROFILE_SERVICE_HOST')
            self.group_manager_settings.PROFILE_SERVICE_PORT = config.getint('ProfileServer', 'PROFILE_SERVICE_PORT')
            self.group_manager_settings.PROFILE_SERVICE_SECURE = config.getboolean('ProfileServer',
                                                                                   'PROFILE_SERVICE_SECURE')
