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

from airavata_sdk.generated.org.apache.airavata.model.user import user_profile_pb2
from airavata_sdk.clients.keycloak_token_fetcher import Authenticator
from airavata_sdk.clients.user_profile_client import UserProfileClient

logger = logging.getLogger(__name__)

logger.setLevel(logging.DEBUG)

authenticator = Authenticator()
token = authenticator.get_token_and_user_info_password_flow("default-admin", "123456", "default")

# load UserProfileClient with access token
client = UserProfileClient(access_token=token)


def add_user_profile():
    try:
        profile = user_profile_pb2.UserProfile(
            gateway_id="default",
            user_id="default-admin",
            emails=["gw@scigap.org"],
            airavata_internal_user_id="default-admin",
            user_model_version="1.0.0",
            first_name="Isuru",
            last_name="Ranawaka",
            creation_time=1576103354,
            last_access_time=1576103296,
            valid_until=1607725696,
            state=user_profile_pb2.ACTIVE,
        )
        added_profile = client.add_user_profile(profile)
        print("Add user profile", added_profile)
    except Exception:
        logger.exception("Error Occurred")


def get_all_user_profiles_in_gateway():
    try:
        profiles = client.get_all_user_profiles_in_gateway("default", offset=0, limit=20)
        print("User Profiles ", profiles)
    except Exception:
        logger.exception("Error Occurred")
