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


class TenantProfileClient(object):

    def __init__(self, configuration_file_location: Optional[str] = None):
        self.settings = ProfileServerSettings(configuration_file_location)
        self._load_settings(configuration_file_location)
        self.client = utils.initialize_tenant_profile_client(
            self.settings.PROFILE_SERVICE_HOST,
            self.settings.PROFILE_SERVICE_PORT,
            self.settings.PROFILE_SERVICE_SECURE,
        )
        # expose the needed functions
        self.add_gateway = self.client.addGateway
        self.update_gateway = self.client.updateGateway
        self.get_gateway = self.client.getGateway
        self.delete_gateway = self.client.deleteGateway
        self.get_all_gateways = self.client.getAllGateways
        self.is_gateway_exist = self.client.isGatewayExist
        self.get_all_gateways_for_user = self.client.getAllGatewaysForUser


    def _load_settings(self, configuration_file_location: Optional[str]):
        if configuration_file_location is not None:
            config = configparser.ConfigParser()
            config.read(configuration_file_location)
            self.settings.PROFILE_SERVICE_HOST = config.get('ProfileServer', 'PROFILE_SERVICE_HOST')
            self.settings.PROFILE_SERVICE_PORT = config.getint('ProfileServer', 'PROFILE_SERVICE_PORT')
            self.settings.PROFILE_SERVICE_SECURE = config.getboolean('ProfileServer', 'PROFILE_SERVICE_SECURE')
