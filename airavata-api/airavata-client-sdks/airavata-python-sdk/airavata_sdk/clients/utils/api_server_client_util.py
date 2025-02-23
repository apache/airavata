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

from airavata_sdk.clients.api_server_client import APIServerClient
from airavata_sdk.clients.keycloak_token_fetcher import Authenticator

logger = logging.getLogger(__name__)
logger.setLevel(logging.INFO)


class APIServerClientUtil(object):

    def __init__(self, configuration_file_location: Optional[str], gateway_id: str, username: str, password: Optional[str], access_token: Optional[str] = None):
        self.authenticator = Authenticator(configuration_file_location)
        if access_token:
            self.token = self.authenticator.get_airavata_authz_token(
                gateway_id=gateway_id,
                username=username,
                token=access_token,
            )
        else:
            assert password is not None
            self.token = self.authenticator.get_token_and_user_info_password_flow(
                gateway_id=gateway_id,
                username=username,
                password=password,
            )
        self.gateway_id = gateway_id
        self.username = username
        self.api_server_client = APIServerClient(configuration_file_location)

    def get_project_id(self, project_name: str) -> Optional[str]:
        response = self.api_server_client.get_user_projects(self.token, self.gateway_id, self.username, 10, 0)
        for project in response:
            if project.name == project_name and project.owner == self.username:
                return project.projectID
        return None

    def get_execution_id(self, application_name: str):
        response = self.api_server_client.get_all_application_interfaces(self.token, self.gateway_id)
        for app in response:
            if app.applicationName == application_name:
                return app.applicationInterfaceId
        return None

    def get_resource_host_id(self, resource_name: str):
        response = self.api_server_client.get_all_compute_resource_names(self.token)
        for k in response.keys():
            if response[k] == resource_name:
                return k
        return None

    def get_group_resource_profile_id(self, group_resource_profile_name: str):
        response = self.api_server_client.get_group_resource_list(self.token, self.gateway_id)
        for x in response:
            if x.groupResourceProfileName == group_resource_profile_name:
                return x.groupResourceProfileId
        return None

    def get_storage_resource_id(self, storage_name: str):
        response = self.api_server_client.get_all_storage_resource_names(self.token)
        for k in response.keys():
            if response[k] == storage_name:
                return k
        return None

    def get_queue_names(self, resource_host_id: str):
        resource = self.api_server_client.get_compute_resource(self.token, resource_host_id)
        batchqueues = resource.batchQueues
        assert batchqueues is not None
        allowed_queue_names = list[str]()
        for queue in batchqueues:
            allowed_queue_names.append(queue.queueName)
        return allowed_queue_names
