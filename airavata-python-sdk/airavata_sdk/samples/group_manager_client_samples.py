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

from airavata_sdk.clients.group_manager_client import GroupManagerClient
from airavata_sdk.clients.keycloak_token_fetcher import Authenticator

logger = logging.getLogger(__name__)

logger.setLevel(logging.DEBUG)

authenticator = Authenticator()
token = authenticator.get_token_and_user_info_password_flow("default-admin", "admin123", "default")

# load GroupManagerClient with default configuration
client = GroupManagerClient(access_token=token)


# create group in airavata
def create_group():
    try:
        group_model = {
            "id": "testing_group",
            "name": "testing_group_name",
            "ownerId": "default-admin",
            "description": "This group is used for testing users",
            "members": ["default-admin"],
            "admins": ["default-admin"],
        }
        created_group = client.create_group(group_model)
        print(created_group)
    except Exception:
        logger.exception("Exception occurred")


# get all groups
def get_groups():
    try:
        created_group = client.get_groups()
        print("Groups :", created_group)
    except Exception:
        logger.exception("Exception occurred")


def add_group_admin():
    try:
        created_group = client.add_group_admins("testing_group", ["default-admin"])
        print("Groups :", created_group)
    except Exception:
        logger.exception("Exception occurred")


def has_owner_access():
    try:
        has_access = client.has_owner_access("testing_group", "default-admin")
        print("Is have access ", has_access)
    except Exception:
        logger.exception("Exception occurred")

get_groups()
