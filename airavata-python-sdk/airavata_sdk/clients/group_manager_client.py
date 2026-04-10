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
from airavata_sdk.transport.utils import create_group_manager_service_stub

logger = logging.getLogger(__name__)
logger.setLevel(logging.DEBUG)


class GroupManagerClient:
    """Client for the Airavata GroupManagerService (gRPC)."""

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

        self._stub = create_group_manager_service_stub(self.channel)

    def close(self):
        self.channel.close()

    def create_group(self, group):
        from airavata_sdk.generated.services import group_manager_service_pb2 as pb2
        resp = self._stub.CreateGroup(
            pb2.CreateGroupRequest(group=group),
            metadata=self._metadata,
        )
        return resp.group_id

    def update_group(self, group):
        from airavata_sdk.generated.services import group_manager_service_pb2 as pb2
        self._stub.UpdateGroup(
            pb2.UpdateGroupRequest(group=group),
            metadata=self._metadata,
        )

    def delete_group(self, group_id, owner_id):
        from airavata_sdk.generated.services import group_manager_service_pb2 as pb2
        self._stub.DeleteGroup(
            pb2.DeleteGroupRequest(group_id=group_id, owner_id=owner_id),
            metadata=self._metadata,
        )

    def get_group(self, group_id):
        from airavata_sdk.generated.services import group_manager_service_pb2 as pb2
        return self._stub.GetGroup(
            pb2.GetGroupRequest(group_id=group_id),
            metadata=self._metadata,
        )

    def get_groups(self):
        from airavata_sdk.generated.services import group_manager_service_pb2 as pb2
        resp = self._stub.GetGroups(
            pb2.GetGroupsRequest(),
            metadata=self._metadata,
        )
        return list(resp.groups)

    def get_all_groups_user_belongs(self, user_name):
        from airavata_sdk.generated.services import group_manager_service_pb2 as pb2
        resp = self._stub.GetAllGroupsUserBelongs(
            pb2.GetAllGroupsUserBelongsRequest(user_name=user_name),
            metadata=self._metadata,
        )
        return list(resp.groups)

    def add_users_to_group(self, user_ids, group_id):
        from airavata_sdk.generated.services import group_manager_service_pb2 as pb2
        self._stub.AddUsersToGroup(
            pb2.AddUsersToGroupRequest(group_id=group_id, user_ids=user_ids),
            metadata=self._metadata,
        )

    def remove_users_from_group(self, user_ids, group_id):
        from airavata_sdk.generated.services import group_manager_service_pb2 as pb2
        self._stub.RemoveUsersFromGroup(
            pb2.RemoveUsersFromGroupRequest(group_id=group_id, user_ids=user_ids),
            metadata=self._metadata,
        )

    def transfer_group_ownership(self, group_id, new_owner_id):
        from airavata_sdk.generated.services import group_manager_service_pb2 as pb2
        self._stub.TransferGroupOwnership(
            pb2.TransferGroupOwnershipRequest(group_id=group_id, new_owner_id=new_owner_id),
            metadata=self._metadata,
        )

    def add_group_admins(self, group_id, admin_ids):
        from airavata_sdk.generated.services import group_manager_service_pb2 as pb2
        self._stub.AddGroupAdmins(
            pb2.AddGroupAdminsRequest(group_id=group_id, admin_ids=admin_ids),
            metadata=self._metadata,
        )

    def remove_group_admins(self, group_id, admin_ids):
        from airavata_sdk.generated.services import group_manager_service_pb2 as pb2
        self._stub.RemoveGroupAdmins(
            pb2.RemoveGroupAdminsRequest(group_id=group_id, admin_ids=admin_ids),
            metadata=self._metadata,
        )

    def has_admin_access(self, group_id, admin_id):
        from airavata_sdk.generated.services import group_manager_service_pb2 as pb2
        resp = self._stub.HasAdminAccess(
            pb2.HasAdminAccessRequest(group_id=group_id, admin_id=admin_id),
            metadata=self._metadata,
        )
        return resp.has_access

    def has_owner_access(self, group_id, owner_id):
        from airavata_sdk.generated.services import group_manager_service_pb2 as pb2
        resp = self._stub.HasOwnerAccess(
            pb2.HasOwnerAccessRequest(group_id=group_id, owner_id=owner_id),
            metadata=self._metadata,
        )
        return resp.has_access
