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

from airavata_sdk.clients.iam_admin_client import IAMAdminClient
from airavata_sdk.clients.keycloak_token_fetcher import Authenticator

logger = logging.getLogger(__name__)

logger.setLevel(logging.DEBUG)

authenticator = Authenticator()
token = authenticator.get_token_and_user_info_password_flow("default-admin", "admin123", "default")

# load IAMAdminClient with default configuration
client = IAMAdminClient(access_token=token)


def is_user_exists():
    try:
        user = client.is_user_exist("default-admin")
        print("Is user exists :", user)
    except Exception:
        logger.exception("Error occurred")


def get_user():
    try:
        user = client.get_user("default-admin")
        print("User :", user)
    except Exception:
        logger.exception("Error occurred")


def get_users_with_role():
    try:
        users = client.get_users_with_role("admin")
        print("Users :", users)
    except Exception:
        logger.exception("Error occurred")
