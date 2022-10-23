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
import os

from airavata_sdk.clients.keycloak_token_fetcher import Authenticator

from airavata_sdk.clients.api_server_client import APIServerClient
from airavata_sdk.clients.utils.api_server_client_util import APIServerClientUtil

from airavata_sdk.clients.utils.data_model_creation_util import DataModelCreationUtil

from airavata_sdk.transport.settings import GatewaySettings, ExperimentSettings

from airavata_sdk.clients.sftp_file_handling_client import SFTPConnector

logger = logging.getLogger(__name__)
logger.setLevel(logging.DEBUG)


class ExperimentHandlerUtil(object):
    def __init__(self, configuration_file_location=None):
        self.configuration_file = configuration_file_location
        self.authenticator = Authenticator(configuration_file_location)
        self.gateway_conf = GatewaySettings(configuration_file_location)
        self.experiment_conf = ExperimentSettings(configuration_file_location)

    def launch_experiment(self, user_id, experiment_name="default_exp", description="this is default exp",
                          local_input_path="/tmp"):
        authenticator = Authenticator(self.configuration_file)
        authenticator.authenticate_with_auth_code()
        access_token = input("Copy paste the access token")
        access_token = access_token.strip()

        print("access token",access_token)
        airavata_token = authenticator.get_airavata_authz_token(user_id, access_token, self.gateway_conf.GATEWAY_ID)
        airavata_util = APIServerClientUtil(self.configuration_file, username=user_id, password=None,
                                            gateway_id=self.gateway_conf.GATEWAY_ID, access_token=access_token)

        data_model_client = DataModelCreationUtil(self.configuration_file,
                                                  username=user_id,
                                                  password=None,
                                                  gateway_id=self.gateway_conf.GATEWAY_ID, access_token=access_token)

        api_server_client = APIServerClient(self.configuration_file)

        execution_id = airavata_util.get_execution_id(self.experiment_conf.APPLICATION_NAME)
        project_id = airavata_util.get_project_id(self.experiment_conf.PROJECT_NAME)
        resource_host_id = airavata_util.get_resource_host_id(self.experiment_conf.COMPUTE_HOST_DOMAIN)
        group_resource_profile_id = airavata_util.get_group_resource_profile_id(
            self.experiment_conf.GROUP_RESOURCE_PROFILE_NAME)
        storage_id = airavata_util.get_storage_resource_id(self.experiment_conf.STORAGE_RESOURCE_HOST)

        logger.debug("creating experiment ", experiment_name)
        experiment = data_model_client.get_experiment_data_model_for_single_application(
            project_name=self.experiment_conf.PROJECT_NAME,
            application_name=self.experiment_conf.APPLICATION_NAME,
            experiment_name=experiment_name,
            description=description)

        logger.debug("connnecting to file upload endpoint ", self.experiment_conf.STORAGE_RESOURCE_HOST,
                     ":", self.experiment_conf.SFTP_PORT)
        sftp_connector = SFTPConnector(host=self.experiment_conf.STORAGE_RESOURCE_HOST,
                                       port=self.experiment_conf.SFTP_PORT,
                                       username=user_id,
                                       password=access_token)

        path_suffix = sftp_connector.upload_files(local_input_path,
                                                  self.experiment_conf.PROJECT_NAME,
                                                  experiment.experimentName)

        logger.debug("Input files uploaded to ", path_suffix)

        path = self.gateway_conf.GATEWAY_DATA_STORE_DIR + path_suffix

        experiment = data_model_client.configure_computation_resource_scheduling(experiment_model=experiment,
                                                                                 computation_resource_name=self.experiment_conf.COMPUTE_HOST_DOMAIN,
                                                                                 group_resource_profile_name=self.experiment_conf.GROUP_RESOURCE_PROFILE_NAME,
                                                                                 storageId=storage_id,
                                                                                 node_count=int(self.experiment_conf.NODE_COUNT),
                                                                                 total_cpu_count=int(self.experiment_conf.TOTAL_CPU_COUNT),
                                                                                 wall_time_limit=int(self.experiment_conf.WALL_TIME_LIMIT),
                                                                                 queue_name=self.experiment_conf.QUEUE_NAME,
                                                                                 experiment_dir_path=path)

        input_files = []
        for x in os.listdir(local_input_path):
            if os.path.isfile(local_input_path + '/' + x):
               input_files.append(x)

        if len(input_files) > 0:
            data_uris = []
            for x in input_files:
                data_uri = data_model_client.register_input_file(file_identifier=x,
                                                                 storage_name=self.experiment_conf.STORAGE_RESOURCE_HOST,
                                                                 storageId=storage_id,
                                                                 input_file_name=x,
                                                                 uploaded_storage_path=path)
                data_uris.append(data_uri)
            experiment = data_model_client.configure_input_and_outputs(experiment, input_files=data_uris,
                                                                       application_name=self.experiment_conf.APPLICATION_NAME)

        else:
            inputs = api_server_client.get_application_inputs(airavata_token, execution_id)
            experiment.experimentInputs = inputs

        outputs = api_server_client.get_application_outputs(airavata_token, execution_id)

        experiment.experimentOutputs = outputs

        # create experiment
        ex_id = api_server_client.create_experiment(airavata_token, self.gateway_conf.GATEWAY_ID, experiment)

        # launch experiment
        api_server_client.launch_experiment(airavata_token, ex_id, self.gateway_conf.GATEWAY_ID)

        logger.info("experiment launched ", ex_id)

        if self.experiment_conf.MONITOR_STATUS:
            status = api_server_client.get_experiment_status(airavata_token, ex_id)

            if status is not None:
                print("Initial state " + str(status.state))
            while status.state <= 6:
                status = api_server_client.get_experiment_status(airavata_token,
                                                                 ex_id);
                time.sleep(30)
                print("State " + str(status.state))

                print("Completed")

                # sftp_connector.download_files(".", self.experiment_conf.PROJECT_NAME, experiment.experimentName)
