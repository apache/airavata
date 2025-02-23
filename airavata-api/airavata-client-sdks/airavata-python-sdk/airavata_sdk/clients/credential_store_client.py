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
from airavata_sdk.transport.settings import CredentialStoreServerSettings

logger = logging.getLogger(__name__)
logger.setLevel(logging.DEBUG)


class CredentialStoreClient(object):

    def __init__(self, configuration_file_location: Optional[str] = None):
        self.settings = CredentialStoreServerSettings(configuration_file_location)
        self._load_settings(configuration_file_location)
        self.client = utils.initialize_credential_store_client(
            self.settings.CREDENTIAL_STORE_API_HOST,
            self.settings.CREDENTIAL_STORE_API_PORT,
            self.settings.CREDENTIAL_STORE_API_SECURE,
        )
        # expose the needed functions
        self.get_SSH_credential = self.client.getSSHCredential

    def _load_settings(self, configuration_file_location: Optional[str]):
        if configuration_file_location is not None:
            config = configparser.ConfigParser()
            config.read(configuration_file_location)
            self.settings.CREDENTIAL_STORE_API_HOST = config.get('CredentialStoreServer', 'CREDENTIAL_STORE_API_HOST')
            self.settings.CREDENTIAL_STORE_API_PORT = config.getint('CredentialStoreServer', 'CREDENTIAL_STORE_API_PORT')
            self.settings.CREDENTIAL_STORE_API_SECURE = config.getboolean('CredentialStoreServer', 'CREDENTIAL_STORE_API_SECURE')
