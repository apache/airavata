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

import json
import logging
from typing import Optional

import grpc

from airavata_sdk import Settings
from airavata_sdk.transport.utils import create_user_profile_service_stub

logger = logging.getLogger(__name__)
logger.setLevel(logging.DEBUG)


class UserProfileClient:
    """Client for the Airavata UserProfileService (gRPC)."""

    def __init__(self, access_token: Optional[str] = None, claims: Optional[dict] = None):
        self.settings = Settings()
        host = self.settings.API_SERVER_HOSTNAME
        port = self.settings.API_SERVER_PORT
        secure = self.settings.API_SERVER_SECURE

        target = f"{host}:{port}"
        if secure:
            self.channel = grpc.secure_channel(target, grpc.ssl_channel_credentials())
        else:
            self.channel = grpc.insecure_channel(target)

        self._metadata: list[tuple[str, str]] = []
        if access_token:
            self._metadata.append(("authorization", f"Bearer {access_token}"))
        if claims:
            self._metadata.append(("x-claims", json.dumps(claims)))

        self._stub = create_user_profile_service_stub(self.channel)

    def close(self):
        self.channel.close()

    def add_user_profile(self, user_profile):
        from airavata_sdk.generated.services import user_profile_service_pb2 as pb2
        resp = self._stub.AddUserProfile(
            pb2.AddUserProfileRequest(user_profile=user_profile),
            metadata=self._metadata,
        )
        return resp.user_id

    def update_user_profile(self, user_profile):
        from airavata_sdk.generated.services import user_profile_service_pb2 as pb2
        self._stub.UpdateUserProfile(
            pb2.UpdateUserProfileRequest(user_profile=user_profile),
            metadata=self._metadata,
        )

    def get_user_profile_by_id(self, user_id, gateway_id):
        from airavata_sdk.generated.services import user_profile_service_pb2 as pb2
        return self._stub.GetUserProfileById(
            pb2.GetUserProfileByIdRequest(user_id=user_id, gateway_id=gateway_id),
            metadata=self._metadata,
        )

    def get_user_profile_by_name(self, user_name, gateway_id):
        from airavata_sdk.generated.services import user_profile_service_pb2 as pb2
        return self._stub.GetUserProfileByName(
            pb2.GetUserProfileByNameRequest(user_name=user_name, gateway_id=gateway_id),
            metadata=self._metadata,
        )

    def delete_user_profile(self, user_id):
        from airavata_sdk.generated.services import user_profile_service_pb2 as pb2
        self._stub.DeleteUserProfile(
            pb2.DeleteUserProfileRequest(user_id=user_id),
            metadata=self._metadata,
        )

    def get_all_user_profiles_in_gateway(self, gateway_id, offset=0, limit=20):
        from airavata_sdk.generated.services import user_profile_service_pb2 as pb2
        resp = self._stub.GetAllUserProfilesInGateway(
            pb2.GetAllUserProfilesInGatewayRequest(
                gateway_id=gateway_id, offset=offset, limit=limit,
            ),
            metadata=self._metadata,
        )
        return list(resp.user_profiles)

    def does_user_exist(self, user_name, gateway_id):
        from airavata_sdk.generated.services import user_profile_service_pb2 as pb2
        resp = self._stub.DoesUserExist(
            pb2.DoesUserExistRequest(user_name=user_name, gateway_id=gateway_id),
            metadata=self._metadata,
        )
        return resp.exists
