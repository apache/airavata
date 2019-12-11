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

from requests_oauthlib import OAuth2Session
from oauthlib.oauth2 import LegacyApplicationClient
from airavata.model.security.ttypes import AuthzToken

from transport.settings import KeycloakConfiguration

keycloak_config = KeycloakConfiguration()


def get_token_and_user_info_password_flow(username, password, gateway_id):
    client_id = keycloak_config.CLIENT_ID
    client_secret = keycloak_config.CLIENT_SECRET
    token_url = keycloak_config.TOKEN_URL
    userinfo_url = keycloak_config.USER_INFO_URL
    verify_ssl = keycloak_config.VERIFY_SSL
    oauth2_session = OAuth2Session(client=LegacyApplicationClient(
        client_id=client_id))
    oauth2_session.verify = keycloak_config.KEYCLOAK_CA_CERTIFICATE
    token = oauth2_session.fetch_token(token_url=token_url,
                                       username=username,
                                       password=password,
                                       client_id=client_id,
                                       client_secret=client_secret,
                                       verify=verify_ssl)

    claimsMap = {
        "userName": username,
        "gatewayID": gateway_id
    }
    return AuthzToken(accessToken=token['access_token'], claimsMap=claimsMap)
