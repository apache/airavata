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
from airavata_sdk.clients.utils.api_server_client_util import APIServerClientUtil

from airavata.model.experiment.ttypes import ExperimentModel, ExperimentType, UserConfigurationDataModel
from airavata.model.scheduling.ttypes import ComputationalResourceSchedulingModel
from airavata.model.data.replica.ttypes import DataProductModel, DataProductType, DataReplicaLocationModel, \
    ReplicaLocationCategory, ReplicaPersistentType

from airavata.model.application.io.ttypes import InputDataObjectType

logger = logging.getLogger(__name__)
logger.setLevel(logging.DEBUG)


class DataModelCreationUtil(object):

    def __init__(self, configuration_file_location, username, password, gateway_id, access_token):
        self.authenticator = Authenticator(configuration_file_location)
        if access_token:
            self.token = self.authenticator.get_airavata_authz_token(username=username,
                                                                     token=access_token,
                                                                     gateway_id=gateway_id)
        else:
            self.token = self.authenticator.get_token_and_user_info_password_flow(username=username,
                                                                                  password=password,
                                                                                  gateway_id=gateway_id)
        self.gateway_id = gateway_id
        self.username = username
        self.password = password
        self.api_server_client = APIServerClient(configuration_file_location)
        self.airavata_util = APIServerClientUtil(configuration_file_location, self.username, self.password,
                                                 self.gateway_id, access_token)

    def get_experiment_data_model_for_single_application(self, project_name, application_name, experiment_name,
                                                         description):
        execution_id = self.airavata_util.get_execution_id(application_name)
        project_id = self.airavata_util.get_project_id(project_name)
        experiment = ExperimentModel()
        experiment.experimentName = experiment_name
        experiment.gatewayId = self.gateway_id
        experiment.userName = self.username
        experiment.description = description
        experiment.projectId = project_id
        experiment.experimentType = ExperimentType.SINGLE_APPLICATION
        experiment.executionId = execution_id
        return experiment

    def configure_computation_resource_scheduling(self,
                                                  experiment_model, computation_resource_name,
                                                  group_resource_profile_name,
                                                  storageId,
                                                  node_count, total_cpu_count, queue_name, wall_time_limit,
                                                  experiment_dir_path, auto_schedule=False):
        resource_host_id = self.airavata_util.get_resource_host_id(computation_resource_name)
        groupResourceProfileId = self.airavata_util.get_group_resource_profile_id(group_resource_profile_name)
        computRes = ComputationalResourceSchedulingModel()
        computRes.resourceHostId = resource_host_id
        computRes.nodeCount = node_count
        computRes.totalCPUCount = total_cpu_count
        computRes.queueName = queue_name
        computRes.wallTimeLimit = wall_time_limit

        userConfigData = UserConfigurationDataModel()
        userConfigData.computationalResourceScheduling = computRes

        userConfigData.groupResourceProfileId = groupResourceProfileId
        userConfigData.storageId = storageId

        userConfigData.experimentDataDir = experiment_dir_path
        userConfigData.airavataAutoSchedule = auto_schedule
        experiment_model.userConfigurationData = userConfigData

        return experiment_model

    def register_input_file(self, file_identifier, storage_name, storageId, input_file_name, uploaded_storage_path):
        dataProductModel = DataProductModel()
        dataProductModel.gatewayId = self.gateway_id
        dataProductModel.ownerName = self.username
        dataProductModel.productName = file_identifier
        dataProductModel.dataProductType = DataProductType.FILE

        replicaLocation = DataReplicaLocationModel()
        replicaLocation.storageResourceId = storageId
        replicaLocation.replicaName = "{} gateway data store copy".format(input_file_name)
        replicaLocation.replicaLocationCategory = ReplicaLocationCategory.GATEWAY_DATA_STORE
        replicaLocation.filePath = "file://{}:{}".format(storage_name,
                                                         uploaded_storage_path + input_file_name)
        dataProductModel.replicaLocations = [replicaLocation]

        return self.api_server_client.register_data_product(self.token, dataProductModel)

    def configure_input_and_outputs(self, experiment_model, input_files, application_name, file_mapping={}):
        execution_id = self.airavata_util.get_execution_id(application_name)

        inputs = self.api_server_client.get_application_inputs(self.token, execution_id)

        configured_inputs = []
        if (len(file_mapping.keys()) == 0):
            count = 0
            for obj in inputs:
                if isinstance(inputs[count], InputDataObjectType) and len(input_files) > count:
                    inputs[count].value = input_files[count]
                    count = count + 1
            configured_inputs = inputs
        else:
            for key in file_mapping.keys():
                for input in inputs:
                    if key == input.name:
                        if input.type == 3:
                            input.value = file_mapping[key]
                            configured_inputs.append(input)
                        elif input.type == 4:
                            val = ','.join(file_mapping[key])
                            input.value = val
                            configured_inputs.append(input)

        experiment_model.experimentInputs = configured_inputs

        outputs = self.api_server_client.get_application_outputs(self.token, execution_id)

        experiment_model.experimentOutputs = outputs

        return experiment_model
