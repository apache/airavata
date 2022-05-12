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

from requests_oauthlib import OAuth2Session
from oauthlib.oauth2 import LegacyApplicationClient
from airavata.model.security.ttypes import AuthzToken

from airavata_sdk.transport.settings import KeycloakConfiguration
import os

import urllib3

urllib3.disable_warnings(urllib3.exceptions.InsecureRequestWarning)

BASE_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))


class Authenticator(object):

    def __init__(self, configuration_file_location=None):
        self.keycloak_settings = KeycloakConfiguration(configuration_file_location)
        self._load_settings(configuration_file_location)

    def get_token_and_user_info_password_flow(self, username, password, gateway_id):
        client_id = self.keycloak_settings.CLIENT_ID
        client_secret = self.keycloak_settings.CLIENT_SECRET
        token_url = self.keycloak_settings.TOKEN_URL
        userinfo_url = self.keycloak_settings.USER_INFO_URL
        verify_ssl = self.keycloak_settings.VERIFY_SSL
        oauth2_session = OAuth2Session(client=LegacyApplicationClient(
            client_id=client_id))
        token = oauth2_session.fetch_token(token_url=token_url,
                                           username=username,
                                           password=password,
                                           client_id=client_id,
                                           client_secret=client_secret,
                                           verify=self.keycloak_settings.KEYCLOAK_CA_CERTIFICATE if verify_ssl else False)

        claimsMap = {
            "userName": username,
            "gatewayID": gateway_id
        }
        return AuthzToken(accessToken=token['access_token'], claimsMap=claimsMap)

    def _load_settings(self, configuration_file_location):
        if configuration_file_location is not None:
            config = configparser.ConfigParser()
            config.read(configuration_file_location)
            self.keycloak_settings.KEYCLOAK_CA_CERTIFICATE = config.get("KeycloakServer", 'CERTIFICATE_FILE_PATH')
            self.keycloak_settings.CLIENT_ID = config.get('KeycloakServer', 'CLIENT_ID')
            self.keycloak_settings.CLIENT_SECRET = config.get('KeycloakServer', 'CLIENT_SECRET')
            self.keycloak_settings.TOKEN_URL = config.get('KeycloakServer', 'TOKEN_URL')
            self.keycloak_settings.USER_INFO_URL = config.get('KeycloakServer', 'USER_INFO_URL')
            self.keycloak_settings.VERIFY_SSL = config.getboolean('KeycloakServer', 'VERIFY_SSL')
