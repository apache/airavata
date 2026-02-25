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

import os

from oauthlib.oauth2 import LegacyApplicationClient
from requests_oauthlib import OAuth2Session

from airavata_sdk import Settings

BASE_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))


class Authenticator(object):

    def __init__(self):
        self.settings = Settings()

    @property
    def TOKEN_URL(self):
        return f"{self.settings.AUTH_SERVER_URL}/realms/{self.settings.AUTH_REALM}/protocol/openid-connect/token"

    @property
    def USER_INFO_URL(self):
        return f"{self.settings.AUTH_SERVER_URL}/realms/{self.settings.AUTH_REALM}/protocol/openid-connect/userinfo"

    @property
    def LOGIN_DESKTOP_URI(self):
        return f"{self.settings.AUTH_SERVER_URL}/realms/{self.settings.AUTH_REALM}/protocol/openid-connect/auth"

    @property
    def CLIENT_ID(self):
        return "airavata"

    @property
    def CLIENT_SECRET(self):
        return "airavata"

    def get_token_and_user_info_password_flow(self, username: str, password: str, gateway_id: str) -> str:
        """Authenticate with username/password and return an access token string."""
        client_id = self.CLIENT_ID
        client_secret = self.CLIENT_SECRET
        token_url = self.TOKEN_URL
        verify_ssl = self.settings.VERIFY_SSL
        oauth2_session = OAuth2Session(
            client=LegacyApplicationClient(client_id=client_id))
        token = oauth2_session.fetch_token(
            token_url=token_url,
            username=username,
            password=password,
            client_id=client_id,
            client_secret=client_secret,
            verify=verify_ssl,
        )
        return token['access_token']

    def get_airavata_authz_token(self, username: str, token: str, gateway_id: str) -> str:
        """Wrap an existing access token. Returns the token string directly."""
        return token

    def get_authorize_url(self, username: str, password: str, gateway_id: str) -> str:
        """Authenticate and return the access token string."""
        return self.get_token_and_user_info_password_flow(username, password, gateway_id)

    def authenticate_with_auth_code(self):
        print("Click on Login URI ", self.LOGIN_DESKTOP_URI)
        return self.LOGIN_DESKTOP_URI
