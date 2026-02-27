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
from airavata_sdk.clients.utils.api_server_client_util import APIServerClientUtil

logger = logging.getLogger(__name__)
logger.setLevel(logging.DEBUG)


class DataModelCreationUtil(object):

    def __init__(self, gateway_id: str, username: str, password: Optional[str], access_token: Optional[str] = None):
        self.authenticator = Authenticator()
        if access_token:
            self.access_token = self.authenticator.get_airavata_authz_token(
                gateway_id=gateway_id,
                username=username,
                token=access_token,
            )
        else:
            assert password is not None
            self.access_token = self.authenticator.get_token_and_user_info_password_flow(
                gateway_id=gateway_id,
                username=username,
                password=password,
            )
        self.gateway_id = gateway_id
        self.username = username
        self.password = password
        self.api_server_client = APIServerClient(access_token=self.access_token)
        self.airavata_util = APIServerClientUtil(
            self.gateway_id,
            self.username,
            self.password,
            access_token,
        )

    def get_experiment_data_model_for_single_application(self, project_name: str, application_name: str, experiment_name: str, description: str):
        execution_id = self.airavata_util.get_execution_id(application_name)
        project_id = self.airavata_util.get_project_id(project_name)
        assert project_id is not None
        return {
            "experimentName": experiment_name,
            "gatewayId": self.gateway_id,
            "userName": self.username,
            "description": description,
            "projectId": project_id,
            "experimentType": "SINGLE_APPLICATION",
            "executionId": execution_id,
        }

    def configure_computation_resource_scheduling(
            self,
            experiment_model: dict,
            computation_resource_name: str,
            group_resource_profile_name: str,
            inputStorageId: str,
            outputStorageId: str,
            node_count: int,
            total_cpu_count: int,
            queue_name: str,
            wall_time_limit: int,
            experiment_dir_path: str,
            auto_schedule: bool = False,
    ):
        resource_host_id = self.airavata_util.get_resource_host_id(computation_resource_name)
        groupResourceProfileId = self.airavata_util.get_group_resource_profile_id(group_resource_profile_name)
        experiment_model["userConfigurationData"] = {
            "computationalResourceScheduling": {
                "resourceHostId": resource_host_id,
                "nodeCount": node_count,
                "totalCPUCount": total_cpu_count,
                "queueName": queue_name,
                "wallTimeLimit": wall_time_limit,
            },
            "groupResourceProfileId": groupResourceProfileId,
            "inputStorageResourceId": inputStorageId,
            "outputStorageResourceId": outputStorageId,
            "experimentDataDir": experiment_dir_path,
            "airavataAutoSchedule": auto_schedule,
        }
        return experiment_model

    def register_input_file(
            self,
            file_identifier: str,
            storage_name: str,
            storageId: str,
            input_file_name: str,
            uploaded_storage_path: str,
    ):
        artifact = {
            "gatewayId": self.gateway_id,
            "ownerName": self.username,
            "name": file_identifier,
            "artifactType": "DATASET",
            "replicaLocations": [{
                "replicaName": "{} gateway data store copy".format(input_file_name),
                "replicaLocationCategory": "GATEWAY_DATA_STORE",
                "storageResourceId": storageId,
                "filePath": "file://{}:{}".format(storage_name, uploaded_storage_path + input_file_name),
            }],
        }
        return self.api_server_client.register_artifact(artifact)

    def configure_input_and_outputs(
        self,
        experiment_model: dict,
        input_files: list[str],
        application_name: str,
        file_mapping: dict[str, str]={},
    ):
        execution_id = self.airavata_util.get_execution_id(application_name)
        assert execution_id is not None
        inputs = self.api_server_client.get_application_inputs(execution_id)

        configured_inputs = []
        if len(file_mapping.keys()) == 0:
            count = 0
            for obj in inputs:
                if len(input_files) > count:
                    inputs[count]["value"] = input_files[count]
                    count = count + 1
            configured_inputs = inputs
        else:
            for key in file_mapping.keys():
                for inp in inputs:
                    if key == inp["name"]:
                        if inp["type"] == 3:
                            inp["value"] = file_mapping[key]
                            configured_inputs.append(inp)
                        elif inp["type"] == 4:
                            val = ','.join(file_mapping[key])
                            inp["value"] = val
                            configured_inputs.append(inp)

        experiment_model["experimentInputs"] = configured_inputs

        outputs = self.api_server_client.get_application_outputs(execution_id)
        experiment_model["experimentOutputs"] = outputs

        return experiment_model
