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

from airavata_sdk.transport.settings import CredentialStoreAPIClientSettings
from airavata_sdk.transport import utils
from airavata.api.credential.store.error.ttypes import CredentialStoreException

logger = logging.getLogger(__name__)
logger.setLevel(logging.DEBUG)


class CredentialStoreClient(object):

    def __init__(self, configuration_file_location=None):
        self.credential_store_server_settings = CredentialStoreAPIClientSettings(configuration_file_location)
        self._load_settings(configuration_file_location)
        self.credential_store_client_pool = utils.initialize_credential_store_client(
            self.credential_store_server_settings.CREDENTIAL_STORE_API_HOST,
            self.credential_store_server_settings.CREDENTIAL_STORE_API_PORT,
            self.credential_store_server_settings.CREDENTIAL_STORE_API_SECURE)

    def get_SSH_credential(self, token_id, gateway_id):
        """
        :param token_id:
        :param gateway_id
        :return: credential
        """
        try:
            return self.credential_store_client_pool.getSSHCredential(token_id, gateway_id)
        except CredentialStoreException:
            logger.exception("Error occurred in get_SSH_credential, probably due to invalid parameters ")
            raise

    def _load_settings(self, configuration_file_location):
        if configuration_file_location is not None:
            config = configparser.ConfigParser()
            config.read(configuration_file_location)
            self.credential_store_server_settings.CREDENTIAL_STORE_API_HOST = config.get('CredentialStoreServer',
                                                                                         'CREDENTIAL_STORE_API_HOST')
            self.credential_store_server_settings.CREDENTIAL_STORE_API_PORT = config.getint('CredentialStoreServer',
                                                                                            'CREDENTIAL_STORE_API_PORT')
            self.credential_store_server_settings.CREDENTIAL_STORE_API_SECURE = config.getboolean(
                'CredentialStoreServer',
                'CREDENTIAL_STORE_API_SECURE')
