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

from airavata_sdk.transport.settings import TenantProfileServerClientSettings
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


class TenantProfileClient(object):

    def __init__(self, configuration_file_location=None):
        self.tenant_profile_settings = TenantProfileServerClientSettings(configuration_file_location)
        self._load_settings(configuration_file_location)
        self.tenant_profile_client_pool = utils.initialize_tenant_profile_client(
            self.tenant_profile_settings.PROFILE_SERVICE_HOST,
            self.tenant_profile_settings.PROFILE_SERVICE_PORT,
            self.tenant_profile_settings.PROFILE_SERVICE_SECURE)

    def add_gateway(self, authz_token, gateway):
        """
        Return the airavataInternalGatewayId assigned to given gateway.

        Parameters:
         - authz_token
         - gateway
        """
        try:
            return self.tenant_profile_client_pool.addGateway(authz_token, gateway)
        except TException:
            logger.exception("Error occurred in add_gateway, ", TException)
            raise

    def update_gateway(self, authz_token, updated_gateway):
        """
        Parameters:
         - authz_token
         - updated_gateway
        """
        try:
            return self.tenant_profile_client_pool.updateGateway(authz_token, updated_gateway)
        except TException:
            logger.exception("Error occurred in update_gateway, ", TException)
            raise

    def get_gateway(self, authz_token, airavata_internal_gateway_id):
        """
        Parameters:
         - authz_token
         - airavata_internal_gateway_id
        """
        try:
            return self.tenant_profile_client_pool.getGateway(authz_token, airavata_internal_gateway_id)
        except TException:
            logger.exception("Error occurred in get_gateway, ", TException)
            raise

    def delete_gateway(self, authz_token, airavata_internal_gateway_id, gateway_id):
        """
        Parameters:
         - authz_token
         - airavata_internal_gateway_id
         - gateway_id
        """
        try:
            return self.tenant_profile_client_pool.deleteGateway(authz_token, airavata_internal_gateway_id, gateway_id)
        except TException:
            logger.exception("Error occurred in delete_gateway, ", TException)
            raise

    def get_all_gateways(self, authz_token):
        """
        Parameters:
         - authz_token
        """
        try:
            return self.tenant_profile_client_pool.getAllGateways(authz_token)
        except TException:
            logger.exception("Error occurred in get_all_gateways, ", TException)
            raise

    def is_gateway_exist(self, authz_token, gateway_id):
        """
        Parameters:
         - authz_token
         - gateway_id
        """
        try:
            return self.tenant_profile_client_pool.isGatewayExist(authz_token, gateway_id)
        except TException:
            logger.exception("Error occurred in is_gateway_exist, ", TException)
            raise

    def get_all_gateways_for_user(self, authz_token, requester_username):
        """
        Parameters:
         - authz_token
         - requester_username
        """
        try:
            return self.tenant_profile_client_pool.getAllGatewaysForUser(authz_token, requester_username)
        except TException:
            logger.exception("Error occurred in get_all_gateways_for_user, ", TException)
            raise

    def _load_settings(self, configuration_file_location):
        if configuration_file_location is not None:
            config = configparser.ConfigParser()
            config.read(configuration_file_location)
            settings = config['ProfileServer']
            self.tenant_profile_settings.PROFILE_SERVICE_HOST = config.get('ProfileServer', 'PROFILE_SERVICE_HOST')
            self.tenant_profile_settings.PROFILE_SERVICE_PORT = config.getint('ProfileServer', 'PROFILE_SERVICE_PORT')
            self.tenant_profile_settings.PROFILE_SERVICE_SECURE = config.getboolean('ProfileServer', 'PROFILE_SERVICE_SECURE')
