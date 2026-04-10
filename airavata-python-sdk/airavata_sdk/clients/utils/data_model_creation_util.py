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

from airavata_sdk.generated.org.apache.airavata.model.application.io import application_io_pb2
from airavata_sdk.generated.org.apache.airavata.model.data.replica import replica_catalog_pb2
from airavata_sdk.generated.org.apache.airavata.model.experiment import experiment_pb2
from airavata_sdk.generated.org.apache.airavata.model.scheduling import scheduling_pb2
from airavata_sdk.clients.api_server_client import APIServerClient
from airavata_sdk.clients.keycloak_token_fetcher import Authenticator
from airavata_sdk.clients.utils.api_server_client_util import APIServerClientUtil

logger = logging.getLogger(__name__)
logger.setLevel(logging.DEBUG)


class DataModelCreationUtil(object):

    def __init__(self, gateway_id: str, username: str, password: Optional[str], access_token: Optional[str] = None):
        self.authenticator = Authenticator()
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
        self.password = password
        self.api_server_client = APIServerClient(
            access_token=self.token.access_token,
            claims=self.token.claims_map,
        )
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
        experiment = experiment_pb2.ExperimentModel(
            experiment_name=experiment_name,
            gateway_id=self.gateway_id,
            user_name=self.username,
            description=description,
            project_id=project_id,
            experiment_type=experiment_pb2.SINGLE_APPLICATION,
            execution_id=execution_id,
        )
        return experiment

    def configure_computation_resource_scheduling(
            self,
            experiment_model,
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
        group_resource_profile_id = self.airavata_util.get_group_resource_profile_id(group_resource_profile_name)
        computRes = scheduling_pb2.ComputationalResourceSchedulingModel(
            resource_host_id=resource_host_id,
            node_count=node_count,
            total_cpu_count=total_cpu_count,
            queue_name=queue_name,
            wall_time_limit=wall_time_limit,
        )

        userConfigData = experiment_pb2.UserConfigurationDataModel(
            computational_resource_scheduling=computRes,
            group_resource_profile_id=group_resource_profile_id,
            input_storage_resource_id=inputStorageId,
            output_storage_resource_id=outputStorageId,
            experiment_data_dir=experiment_dir_path,
            airavata_auto_schedule=auto_schedule,
        )
        experiment_model.user_configuration_data.CopyFrom(userConfigData)
        return experiment_model

    def register_input_file(
            self,
            file_identifier: str,
            storage_name: str,
            storageId: str,
            input_file_name: str,
            uploaded_storage_path: str,
    ):
        replicaLocation = replica_catalog_pb2.DataReplicaLocationModel(
            storage_resource_id=storageId,
            replica_name="{} gateway data store copy".format(input_file_name),
            replica_location_category=replica_catalog_pb2.GATEWAY_DATA_STORE,
            file_path="file://{}:{}".format(storage_name, uploaded_storage_path + input_file_name),
        )
        dataProductModel = replica_catalog_pb2.DataProductModel(
            gateway_id=self.gateway_id,
            owner_name=self.username,
            product_name=file_identifier,
            data_product_type=replica_catalog_pb2.FILE,
            replica_locations=[replicaLocation],
        )
        return self.api_server_client.register_data_product(dataProductModel)

    def configure_input_and_outputs(
        self,
        experiment_model,
        input_files: list[str],
        application_name: str,
        file_mapping: dict[str, str]={},
    ):
        execution_id = self.airavata_util.get_execution_id(application_name)
        assert execution_id is not None
        inputs_response = self.api_server_client.get_application_inputs(execution_id)

        # inputs_response is a protobuf response with repeated fields
        inputs = list(inputs_response.inputs) if hasattr(inputs_response, 'inputs') else []

        configured_inputs = []
        if len(file_mapping.keys()) == 0:
            count = 0
            for obj in inputs:
                if count < len(input_files):
                    obj.value = input_files[count]
                    count += 1
            configured_inputs = inputs
        else:
            for key in file_mapping.keys():
                for inp in inputs:
                    if key == inp.name:
                        if inp.type == 3:
                            inp.value = file_mapping[key]
                            configured_inputs.append(inp)
                        elif inp.type == 4:
                            val = ','.join(file_mapping[key])
                            inp.value = val
                            configured_inputs.append(inp)

        del experiment_model.experiment_inputs[:]
        experiment_model.experiment_inputs.extend(configured_inputs)

        outputs_response = self.api_server_client.get_application_outputs(execution_id)
        outputs = list(outputs_response.outputs) if hasattr(outputs_response, 'outputs') else []

        del experiment_model.experiment_outputs[:]
        experiment_model.experiment_outputs.extend(outputs)

        return experiment_model
