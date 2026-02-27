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

from airavata_sdk.clients.keycloak_token_fetcher import Authenticator
from airavata_sdk.clients.gateway_profile_client import GatewayProfileClient

logger = logging.getLogger(__name__)

logger.setLevel(logging.DEBUG)

authenticator = Authenticator()
token = authenticator.get_token_and_user_info_password_flow("default-admin", "admin123", "default")

# load GatewayProfileClient with default configuration
client = GatewayProfileClient(access_token=token)


def get_all_gateways():
    try:
        gws = client.get_all_gateways()
        print("Gateways ", gws)
    except Exception:
        logger.exception("Error occurred")


def is_gateway_exist():
    try:
        gw_exist = client.is_gateway_exist("default")
        print("Gateway exists ", gw_exist)
    except Exception:
        logger.exception("Error occurred")
