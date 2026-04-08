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
from airavata_sdk.transport.utils import create_iam_admin_service_stub

logger = logging.getLogger(__name__)
logger.setLevel(logging.DEBUG)


class IAMAdminClient:
    """Client for the Airavata IamAdminService (gRPC)."""

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

        self._stub = create_iam_admin_service_stub(self.channel)

    def close(self):
        self.channel.close()

    def set_up_gateway(self, gateway):
        from airavata_sdk.generated.services import iam_admin_service_pb2 as pb2
        return self._stub.SetUpGateway(
            pb2.SetUpGatewayRequest(gateway=gateway),
            metadata=self._metadata,
        )

    def is_username_available(self, username):
        from airavata_sdk.generated.services import iam_admin_service_pb2 as pb2
        resp = self._stub.IsUsernameAvailable(
            pb2.IsUsernameAvailableRequest(username=username),
            metadata=self._metadata,
        )
        return resp.available

    def register_user(self, username, email_address, first_name, last_name, new_password):
        from airavata_sdk.generated.services import iam_admin_service_pb2 as pb2
        self._stub.RegisterUser(
            pb2.RegisterUserRequest(
                username=username,
                email_address=email_address,
                first_name=first_name,
                last_name=last_name,
                new_password=new_password,
            ),
            metadata=self._metadata,
        )

    def enable_user(self, username):
        from airavata_sdk.generated.services import iam_admin_service_pb2 as pb2
        self._stub.EnableUser(
            pb2.EnableUserRequest(username=username),
            metadata=self._metadata,
        )

    def is_user_enabled(self, username):
        from airavata_sdk.generated.services import iam_admin_service_pb2 as pb2
        resp = self._stub.IsUserEnabled(
            pb2.IsUserEnabledRequest(username=username),
            metadata=self._metadata,
        )
        return resp.enabled

    def is_user_exist(self, username):
        from airavata_sdk.generated.services import iam_admin_service_pb2 as pb2
        resp = self._stub.IsUserExist(
            pb2.IsUserExistRequest(username=username),
            metadata=self._metadata,
        )
        return resp.exists

    def get_user(self, username):
        from airavata_sdk.generated.services import iam_admin_service_pb2 as pb2
        return self._stub.GetUser(
            pb2.GetIamUserRequest(username=username),
            metadata=self._metadata,
        )

    def get_users(self, offset=0, limit=20, search=""):
        from airavata_sdk.generated.services import iam_admin_service_pb2 as pb2
        resp = self._stub.GetUsers(
            pb2.GetIamUsersRequest(offset=offset, limit=limit, search=search),
            metadata=self._metadata,
        )
        return list(resp.users)

    def reset_user_password(self, username, new_password):
        from airavata_sdk.generated.services import iam_admin_service_pb2 as pb2
        self._stub.ResetUserPassword(
            pb2.ResetUserPasswordRequest(username=username, new_password=new_password),
            metadata=self._metadata,
        )

    def find_users(self, email, user_id=""):
        from airavata_sdk.generated.services import iam_admin_service_pb2 as pb2
        resp = self._stub.FindUsers(
            pb2.FindUsersRequest(email=email, user_id=user_id),
            metadata=self._metadata,
        )
        return list(resp.users)

    def update_user_profile(self, user_details):
        from airavata_sdk.generated.services import iam_admin_service_pb2 as pb2
        self._stub.UpdateUserProfile(
            pb2.UpdateIamUserProfileRequest(user_details=user_details),
            metadata=self._metadata,
        )

    def delete_user(self, username):
        from airavata_sdk.generated.services import iam_admin_service_pb2 as pb2
        self._stub.DeleteUser(
            pb2.DeleteUserRequest(username=username),
            metadata=self._metadata,
        )

    def add_role_to_user(self, username, role_name):
        from airavata_sdk.generated.services import iam_admin_service_pb2 as pb2
        self._stub.AddRoleToUser(
            pb2.AddRoleToUserRequest(username=username, role_name=role_name),
            metadata=self._metadata,
        )

    def remove_role_from_user(self, username, role_name):
        from airavata_sdk.generated.services import iam_admin_service_pb2 as pb2
        self._stub.RemoveRoleFromUser(
            pb2.RemoveRoleFromUserRequest(username=username, role_name=role_name),
            metadata=self._metadata,
        )

    def get_users_with_role(self, role_name):
        from airavata_sdk.generated.services import iam_admin_service_pb2 as pb2
        resp = self._stub.GetUsersWithRole(
            pb2.GetUsersWithRoleRequest(role_name=role_name),
            metadata=self._metadata,
        )
        return list(resp.users)
