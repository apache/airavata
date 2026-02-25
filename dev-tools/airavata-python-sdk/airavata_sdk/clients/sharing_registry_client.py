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
from typing import Optional

from airavata_sdk import Settings
from airavata_sdk.transport.utils import RestClient

logger = logging.getLogger(__name__)
logger.setLevel(logging.DEBUG)


class SharingRegistryClient(object):
    """Client for sharing registry operations via the unified REST API.

    Sharing is handled through the /api/v1/resource-access endpoints.
    Resource-level access grants control user and group permissions.
    """

    def __init__(self, access_token: str, base_url: Optional[str] = None):
        self.settings = Settings()
        self.client = RestClient(
            access_token=access_token,
            base_url=base_url or self.settings.API_SERVER_URL,
        )

    # Resource Access (replaces entity sharing)
    def share_entity_with_users(self, resource_id: str, user_ids: list[str], permission: str = "READ") -> None:
        for user_id in user_ids:
            self.client.post("/resource-access", json={
                "resourceId": resource_id,
                "userId": user_id,
                "permission": permission,
            })

    def revoke_entity_sharing_from_users(self, resource_id: str, user_ids: list[str]) -> None:
        grants = self.client.get("/resource-access", params={"resourceId": resource_id})
        for grant in (grants or []):
            if grant.get("userId") in user_ids:
                self.client.delete(f"/resource-access/{grant['id']}")

    def share_entity_with_groups(self, resource_id: str, group_ids: list[str], permission: str = "READ") -> None:
        for group_id in group_ids:
            self.client.post("/resource-access", json={
                "resourceId": resource_id,
                "groupId": group_id,
                "permission": permission,
            })

    def revoke_entity_sharing_from_groups(self, resource_id: str, group_ids: list[str]) -> None:
        grants = self.client.get("/resource-access", params={"resourceId": resource_id})
        for grant in (grants or []):
            if grant.get("groupId") in group_ids:
                self.client.delete(f"/resource-access/{grant['id']}")

    def user_has_access(self, resource_id: str, user_id: str, permission: str = "READ") -> bool:
        grants = self.client.get("/resource-access", params={"resourceId": resource_id})
        return any(g.get("userId") == user_id for g in (grants or []))

    def get_list_of_shared_users(self, resource_id: str) -> list[dict]:
        return self.client.get("/resource-access", params={"resourceId": resource_id})

    def get_list_of_directly_shared_users(self, resource_id: str) -> list[dict]:
        return self.get_list_of_shared_users(resource_id)

    def get_list_of_shared_groups(self, resource_id: str) -> list[dict]:
        return self.client.get("/resource-access", params={"resourceId": resource_id})

    def get_list_of_directly_shared_groups(self, resource_id: str) -> list[dict]:
        return self.get_list_of_shared_groups(resource_id)

    # Groups (delegate to /groups endpoint)
    def create_group(self, group: dict) -> str:
        return self.client.post("/groups", json=group)

    def update_group(self, group_id: str, group: dict) -> dict:
        return self.client.put(f"/groups/{group_id}", json=group)

    def delete_group(self, group_id: str) -> None:
        self.client.delete(f"/groups/{group_id}")

    def get_group(self, group_id: str) -> dict:
        return self.client.get(f"/groups/{group_id}")

    def get_groups(self, gateway_id: str = None) -> list[dict]:
        return self.client.get("/groups", params={"gatewayId": gateway_id})

    def add_users_to_group(self, group_id: str, user_ids: list[str]) -> None:
        for user_id in user_ids:
            self.client.post(f"/groups/{group_id}/members", json={"userId": user_id})

    def remove_users_from_group(self, group_id: str, user_ids: list[str]) -> None:
        for user_id in user_ids:
            self.client.delete(f"/groups/{group_id}/members/{user_id}")

    def transfer_group_ownership(self, group_id: str, new_owner_id: str) -> dict:
        group = self.get_group(group_id)
        group["ownerId"] = new_owner_id
        return self.update_group(group_id, group)

    def add_group_admins(self, group_id: str, admin_ids: list[str]) -> None:
        self.add_users_to_group(group_id, admin_ids)

    def remove_group_admins(self, group_id: str, admin_ids: list[str]) -> None:
        self.remove_users_from_group(group_id, admin_ids)

    def has_admin_access(self, group_id: str, admin_id: str) -> bool:
        group = self.get_group(group_id)
        return admin_id in (group.get("adminIds") or [])

    def has_owner_access(self, group_id: str, owner_id: str) -> bool:
        group = self.get_group(group_id)
        return group.get("ownerId") == owner_id
