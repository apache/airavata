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

from airavata.api.error.ttypes import TException
from airavata_sdk.clients.keycloak_token_fetcher import Authenticator
from airavata_sdk.clients.tenant_profile_client import TenantProfileClient

logger = logging.getLogger(__name__)

logger.setLevel(logging.DEBUG)

authenticator = Authenticator();
token = authenticator.get_token_and_user_info_password_flow("default-admin", "123456", "default")

# load GroupManagerClient with default configuration
#client = TenantProfileClient()


# load client with given configuration file (e.g customized_settings.ini)
client = TenantProfileClient('../transport/settings.ini')


def get_all_gateways():
    try:
        gws = client.get_all_gateways(token)
        print("Gateways ", gws)
    except TException:
        logger.exception("Error occurred")


def is_gateway_exsist():
    try:
        gw_exisist = client.is_gateway_exist(token, "default")
        print("Gateways ", gw_exisist)
    except TException:
        logger.exception("Error occurred")

