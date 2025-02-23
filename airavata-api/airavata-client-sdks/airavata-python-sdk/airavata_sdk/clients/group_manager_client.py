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
from airavata_sdk.transport.settings import ProfileServerSettings

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

    def __init__(self, configuration_file_location: Optional[str] = None):
        self.settings = ProfileServerSettings(configuration_file_location)
        self._load_settings(configuration_file_location)
        self.client = utils.initialize_group_manager_client(
            self.settings.PROFILE_SERVICE_HOST,
            self.settings.PROFILE_SERVICE_PORT,
            self.settings.PROFILE_SERVICE_SECURE,
        )
        # expose the needed functions
        self.get_api_version = self.client.getAPIVersion
        self.create_group = self.client.createGroup
        self.update_group = self.client.updateGroup
        self.delete_group = self.client.deleteGroup
        self.get_group = self.client.getGroup
        self.get_groups = self.client.getGroups
        self.get_all_groups_user_belongs = self.client.getAllGroupsUserBelongs
        self.add_users_to_group = self.client.addUsersToGroup
        self.remove_users_from_group = self.client.removeUsersFromGroup
        self.transfer_group_ownership = self.client.transferGroupOwnership
        self.add_group_admins = self.client.addGroupAdmins
        self.remove_group_admins = self.client.removeGroupAdmins
        self.has_admin_access = self.client.hasAdminAccess
        self.has_owner_access = self.client.hasOwnerAccess

    def _load_settings(self, configuration_file_location: Optional[str]):
        if configuration_file_location is not None:
            config = configparser.ConfigParser()
            config.read(configuration_file_location)
            self.settings.PROFILE_SERVICE_HOST = config.get('ProfileServer', 'PROFILE_SERVICE_HOST')
            self.settings.PROFILE_SERVICE_PORT = config.getint('ProfileServer', 'PROFILE_SERVICE_PORT')
            self.settings.PROFILE_SERVICE_SECURE = config.getboolean('ProfileServer', 'PROFILE_SERVICE_SECURE')
