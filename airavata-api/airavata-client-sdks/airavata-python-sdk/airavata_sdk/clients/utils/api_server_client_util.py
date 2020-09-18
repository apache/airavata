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
import time
import logging
import airavata_sdk.samples.file_utils as fb

from airavata_sdk.clients.keycloak_token_fetcher import Authenticator

from airavata_sdk.clients.api_server_client import APIServerClient

logger = logging.getLogger(__name__)
logger.setLevel(logging.DEBUG)


class APIServerClientUtil(object):

    def __init__(self, configuration_file_location, username, password, gateway_id):
        self.authenticator = Authenticator(configuration_file_location)
        self.token = self.authenticator.get_token_and_user_info_password_flow(username=username,
                                                                              password=password, gateway_id=gateway_id)
        self.gateway_id = gateway_id
        self.username = username
        self.password = password
        self.api_server_client = APIServerClient(configuration_file_location)

    def get_project_id(self, project_name):
        response = self.api_server_client.get_user_projects(self.token, self.gateway_id, self.username, 10, 0)
        for project in response:
            if project.name == project_name:
                return project.projectID
        return None

    def get_execution_id(self, application_name):
        response = self.api_server_client.get_all_application_interfaces(self.token, self.gateway_id)
        for app in response:
            if app.applicationName == application_name:
                return app.applicationInterfaceId
        return None

    def get_resource_host_id(self, resource_name):
        response = self.api_server_client.get_all_compute_resource_names(self.token)
        for k in response.keys():
            if response[k] == resource_name:
                return k
        return None

    def get_group_resource_profile_id(self, group_resource_profile_name):
        response = self.api_server_client.get_group_resource_list(self.token, self.gateway_id)
        for x in response:
            if x.groupResourceProfileName == group_resource_profile_name:
                return x.groupResourceProfileId
        return None

    def get_storage_resource_id(self, storage_name):
        response = self.api_server_client.get_all_storage_resource_names(self.token)
        for k in response.keys():
            if response[k] == storage_name:
                return k
        return None
