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


class IAMAdminClient(object):
    """Client for IAM admin operations via the unified REST API."""

    def __init__(self, access_token: str, base_url: Optional[str] = None):
        self.settings = Settings()
        self.client = RestClient(
            access_token=access_token,
            base_url=base_url or self.settings.API_SERVER_URL,
        )

    def is_username_available(self, user_name: str) -> bool:
        result = self.client.get(f"/users/{user_name}/exists")
        return not result

    def register_user(self, user: dict) -> str:
        return self.client.post("/users", json=user)

    def enable_user(self, user_id: str) -> bool:
        self.client.post(f"/users/{user_id}/enable")
        return True

    def is_user_enabled(self, user_id: str) -> bool:
        return self.client.get(f"/users/{user_id}/enabled")

    def is_user_exist(self, user_id: str) -> bool:
        return self.client.get(f"/users/{user_id}/exists")

    def get_user(self, user_id: str) -> dict:
        return self.client.get(f"/users/{user_id}")

    def get_users(self, gateway_id: str = None) -> list[dict]:
        return self.client.get("/users", params={"gatewayId": gateway_id})

    def reset_user_password(self, user_id: str, new_password: str) -> None:
        self.client.put(f"/users/{user_id}", json={"password": new_password})

    def find_users(self, search_filter: str) -> list[dict]:
        return self.client.get("/users", params={"search": search_filter})

    def update_user_profile(self, user: dict) -> bool:
        user_id = user.get("userId") or user.get("airavataInternalUserId")
        self.client.put(f"/users/{user_id}", json=user)
        return True

    def delete_user(self, user_id: str) -> None:
        self.client.delete(f"/users/{user_id}")

    def add_role_to_user(self, user_id: str, role: str) -> bool:
        self.client.put(f"/users/{user_id}", json={"roles": [role]})
        return True

    def remove_role_from_user(self, user_id: str, role: str) -> bool:
        self.client.put(f"/users/{user_id}", json={"removeRoles": [role]})
        return True

    def get_users_with_role(self, role: str) -> list[dict]:
        return self.client.get("/users", params={"role": role})
