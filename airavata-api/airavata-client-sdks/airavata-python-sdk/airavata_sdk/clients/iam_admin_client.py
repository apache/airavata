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

from airavata_sdk.transport.settings import IAMAdminClientSettings
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


class IAMAdminClient(object):

    def __init__(self, configuration_file_location=None):
        self.iam_admin_settings = IAMAdminClientSettings(configuration_file_location)
        self._load_settings(configuration_file_location)
        self.iam_admin_client_pool = utils.initialize_iam_admin_client(
            self.iam_admin_settings.PROFILE_SERVICE_HOST,
            self.iam_admin_settings.PROFILE_SERVICE_PORT,
            self.iam_admin_settings.PROFILE_SERVICE_SECURE)

    def set_up_gateway(self, authz_token, gateway):
        """
        Parameters:
         - authz_token
         - gateway
        """
        try:
            return self.iam_admin_client_pool.setUpGateway(authz_token, gateway)
        except TException:
            logger.exception("Error occurred in set_up_gateway, ", TException)
            raise

    def is_username_available(self, authz_token, username):
        """
        Parameters:
         - authz_token
         - username
        """
        try:
            return self.iam_admin_client_pool.isUsernameAvailable(authz_token, username)
        except TException:
            logger.exception("Error occurred in is_username_available, ", TException)
            raise

    def register_user(self, authz_token, username, email_address, first_name, last_name, new_password):
        """
        Parameters:
         - authz_token
         - username
         - email_address
         - first_name
         - last_name
         - new_password
        """
        try:
            return self.iam_admin_client_pool.registerUser(authz_token, username, email_address,
                                                              first_name, last_name, new_password)
        except TException:
            logger.exception("Error occurred in register_user, ", TException)
            raise

    def enable_user(self, authz_token, username):
        """
        Parameters:
         - authz_token
         - username
        """
        try:
            return self.iam_admin_client_pool.enableUser(authz_token, username)
        except TException:
            logger.exception("Error occurred in enable_user, ", TException)
            raise

    def is_user_enabled(self, authz_token, username):
        """
        Parameters:
         - authzToken
         - username
        """
        try:
            return self.iam_admin_client_pool.isUserEnabled(authz_token, username)
        except TException:
            logger.exception("Error occurred in is_user_enabled, ", TException)
            raise

    def is_user_exist(self, authz_token, username):
        """
        Parameters:
         - authzToken
         - username
        """
        try:
            return self.iam_admin_client_pool.isUserExist(authz_token, username)
        except TException:
            logger.exception("Error occurred in is_user_exist, ", TException)
            raise

    def get_user(self, authz_token, username):
        """
        Parameters:
         - authzToken
         - username
        """
        try:
            return self.iam_admin_client_pool.getUser(authz_token, username)
        except TException:
            logger.exception("Error occurred in get_user, ", TException)
            raise

    def get_users(self, authz_token, offset, limit, search):
        """
        Parameters:
         - authzToken
         - offset
         - limit
         - search
        """
        try:
            return self.iam_admin_client_pool.getUsers(authz_token, offset, limit, search)
        except TException:
            logger.exception("Error occurred in get_users, ", TException)
            raise

    def reset_user_password(self, authz_token, username, new_password):
        """
        Parameters:
         - authzToken
         - username
         - newPassword
        """
        try:
            return self.iam_admin_client_pool.resetUserPassword( authz_token, username, new_password)
        except TException:
            logger.exception("Error occurred in reset_user_password, ", TException)
            raise

    def find_users(self, authz_token, email, user_id):
        """
        Parameters:
         - authzToken
         - email
         - userId
        """
        try:
            return self.iam_admin_client_pool.findUsers(authz_token, email, user_id)
        except TException:
            logger.exception("Error occurred in find_users, ", TException)
            raise

    def update_user_profile(self, authz_token, user_details):
        """
        Parameters:
         - authzToken
         - userDetails
        """
        try:
            return self.iam_admin_client_pool.updateUserProfile(authz_token, user_details)
        except TException:
            logger.exception("Error occurred in update_user_profile, ", TException)
            raise

    def delete_user(self, authz_token, username):
        """
        Parameters:
         - authzToken
         - username
        """
        try:
            return self.iam_admin_client_pool.deleteUser(authz_token, username)
        except TException:
            logger.exception("Error occurred in delete_user, ", TException)
            raise

    def add_role_to_user(self, authz_token, username, role_name):
        """
        Parameters:
         - authzToken
         - username
         - roleName
        """
        try:
            return self.iam_admin_client_pool.addRoleToUser(authz_token, username, role_name)
        except TException:
            logger.exception("Error occurred in add_role_to_user, ", TException)
            raise

    def remove_role_from_user(self, authz_token, username, role_name):
        """
        Parameters:
         - authzToken
         - username
         - roleName
        """
        try:
            return self.iam_admin_client_pool.removeRoleFromUser(authz_token, username, role_name)
        except TException:
            logger.exception("Error occurred in remove_role_from_user, ", TException)
            raise

    def get_users_with_role(self, authz_token, role_name):
        """
        Parameters:
         - authzToken
         - roleName
        """
        try:
            return self.iam_admin_client_pool.getUsersWithRole(authz_token, role_name)
        except TException:
            logger.exception("Error occurred in create_group, ", TException)
            raise

    def _load_settings(self, configuration_file_location):
        if configuration_file_location is not None:
            config = configparser.ConfigParser()
            config.read(configuration_file_location)
            self.iam_admin_settings.PROFILE_SERVICE_HOST = config.get('ProfileServer', 'PROFILE_SERVICE_HOST')
            self.iam_admin_settings.PROFILE_SERVICE_PORT = config.getint('ProfileServer', 'PROFILE_SERVICE_PORT')
            self.iam_admin_settings.PROFILE_SERVICE_SECURE = config.getboolean('ProfileServer',
                                                                                   'PROFILE_SERVICE_SECURE')

