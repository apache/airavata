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

from airavata_sdk.transport.settings import UserProfileClientSettings
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


class UserProfileClient(object):

    def __init__(self, configuration_file_location=None):
        self.user_profile_client_settings = UserProfileClientSettings(configuration_file_location)
        self._load_settings(configuration_file_location)
        self.user_profile_client_pool = utils.initialize_user_profile_client(
            self.user_profile_client_settings.PROFILE_SERVICE_HOST,
            self.user_profile_client_settings.PROFILE_SERVICE_PORT,
            self.user_profile_client_settings.PROFILE_SERVICE_SECURE)

    def get_api_version(self):
        try:
            return self.user_profile_client_pool.getAPIVersion()
        except TException:
            logger.exception("Error occurred in get_api_version, ", TException)
            raise

    def initialize_user_profile(self, authz_token):
        """
        Create an initial UserProfile based on information in the IAM service for this user.

        Parameters:
         - authzToken
        """
        try:
            return self.user_profile_client_pool.initializeUserProfile(authz_token)
        except TException:
            logger.exception("Error occurred in add_gateway, ", TException)
            raise

    def add_user_profile(self, authz_token, user_profile):
        """
        Parameters:
         - authzToken
         - userProfile
        """
        try:
            return self.user_profile_client_pool.addUserProfile(authz_token, user_profile)
        except TException:
            logger.exception("Error occurred in add_gateway, ", TException)
            raise

    def update_user_profile(self, authz_token, user_profile):
        """
        Parameters:
         - authzToken
         - userProfile
        """
        try:
            return self.user_profile_client_pool.updateUserProfile(authz_token, user_profile)
        except TException:
            logger.exception("Error occurred in add_gateway, ", TException)
            raise

    def get_user_profile_by_id(self, authz_token, user_id, gateway_id):
        """
        Parameters:
         - authzToken
         - userId
         - gatewayId
        """
        try:
            return self.user_profile_client_pool.getUserProfileById(authz_token, user_id, gateway_id)
        except TException:
            logger.exception("Error occurred in add_gateway, ", TException)
            raise

    def delete_user_profile(self, authz_token, user_id, gateway_id):
        """
        Parameters:
         - authzToken
         - userId
         - gatewayId
        """
        try:
            return self.user_profile_client_pool.deleteUserProfile(authz_token, user_id, gateway_id)
        except TException:
            logger.exception("Error occurred in add_gateway, ", TException)
            raise

    def get_all_user_profiles_in_gateway(self, authz_token, gateway_id, offset, limit):
        """
        Parameters:
         - authzToken
         - gatewayId
         - offset
         - limit
        """
        try:
            return self.user_profile_client_pool.getAllUserProfilesInGateway(authz_token, gateway_id, offset, limit)
        except TException:
            logger.exception("Error occurred in add_gateway, ", TException)
            raise

    def does_user_exist(self, authz_token, user_id, gateway_id):
        """
        Parameters:
         - authzToken
         - userId
         - gatewayId
        """
        try:
            return self.user_profile_client_pool.doesUserExist(authz_token, user_id, gateway_id)
        except TException:
            logger.exception("Error occurred in add_gateway, ", TException)
            raise

    def _load_settings(self, configuration_file_location):
        if configuration_file_location is not None:
            config = configparser.ConfigParser()
            config.read(configuration_file_location)
            self.user_profile_client_settings.PROFILE_SERVICE_HOST = config.get('ProfileServer', 'PROFILE_SERVICE_HOST')
            self.user_profile_client_settings.PROFILE_SERVICE_PORT = config.getint('ProfileServer', 'PROFILE_SERVICE_PORT')
            self.user_profile_client_settings.PROFILE_SERVICE_SECURE = config.getboolean('ProfileServer', 'PROFILE_SERVICE_SECURE')
