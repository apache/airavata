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
from airavata_sdk.clients.user_profile_client import UserProfileClient

from airavata_sdk.clients.keycloak_token_fetcher import Authenticator

from airavata.api.error.ttypes import TException

from airavata.model.user.ttypes import UserProfile, Status

logger = logging.getLogger(__name__)

logger.setLevel(logging.DEBUG)

authenticator = Authenticator();
token = authenticator.get_token_and_user_info_password_flow("default-admin", "123456", "default")

# load GroupManagerClient with default configuration
client = UserProfileClient()


# load client with given configuration file (e.g customized_settings.ini)
# client = UserProfileClient('../transport/settings.ini')


def add_user_profile():
    try:
        profile = UserProfile()
        profile.gatewayId = "default"
        profile.userId = "default-admin"
        profile.emails = ['gw@scigap.org']
        profile.airavataInternalUserId = "default-admin"
        profile.userModelVersion = "1.0.0"
        profile.firstName = "Isuru"
        profile.lastName = "Ranawaka"
        profile.creationTime = 1576103354
        profile.lastAccessTime = 1576103296
        profile.validUntil = 1607725696
        profile.State = Status.ACTIVE
        added_profile = client.add_user_profile(token, profile)
        print("Add user proflile", added_profile)
    except TException:
        logger.exception("Error Occurred")


def get_all_user_profiles_in_gateway():
    try:
        profiles = client.get_all_user_profiles_in_gateway(token, "default", 0, -1)
        print("User Profiles ", profiles)
    except TException:
        logger.exception("Error Occurred")
