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
import jwt

import getpass

from airavata_sdk.clients.keycloak_token_fetcher import Authenticator

from airavata_sdk.clients.api_server_client import APIServerClient
from airavata_sdk.clients.utils.api_server_client_util import APIServerClientUtil

from airavata_sdk.clients.utils.data_model_creation_util import DataModelCreationUtil

from airavata_sdk.transport.settings import GatewaySettings, ExperimentSettings, KeycloakConfiguration

from airavata_sdk.clients.sftp_file_handling_client import SFTPConnector

logger = logging.getLogger('airavata_sdk.clients')
logger.setLevel(logging.INFO)


class ExperimentHandlerUtil(object):
    def __init__(self, configuration_file_location=None):
        self.configuration_file = configuration_file_location
        self.authenticator = Authenticator(configuration_file_location)
        self.gateway_conf = GatewaySettings(configuration_file_location)
        self.experiment_conf = ExperimentSettings(configuration_file_location)
        self.keycloak_conf = KeycloakConfiguration(configuration_file_location)
        self.authenticator = Authenticator(self.configuration_file)
        self.authenticator.authenticate_with_auth_code()
        access_token = getpass.getpass('Copy paste the access token')
        self.access_token = access_token
        decode = jwt.decode(access_token, options={"verify_signature": False})
        self.user_id = decode['preferred_username']
        self.airavata_token = self.authenticator.get_airavata_authz_token(self.user_id, access_token,
                                                                          self.gateway_conf.GATEWAY_ID)
        self.airavata_util = APIServerClientUtil(self.configuration_file, username=self.user_id, password=None,
                                                 gateway_id=self.gateway_conf.GATEWAY_ID, access_token=access_token)

        self.data_model_client = DataModelCreationUtil(self.configuration_file,
                                                       username=self.user_id,
                                                       password=None,
                                                       gateway_id=self.gateway_conf.GATEWAY_ID,
                                                       access_token=access_token)

        self.api_server_client = APIServerClient(self.configuration_file)

    def queue_names(self, computation_resource_name):
        resource_id = self.airavata_util.get_resource_host_id(computation_resource_name)
        return self.airavata_util.get_queue_names(resource_id)

    def launch_experiment(self, experiment_name="default_exp", description="this is default exp",
                          local_input_path="/tmp", input_file_mapping={}, computation_resource_name=None,
                          queue_name=None, node_count=1, cpu_count=1, walltime=30, output_path='.'):
        execution_id = self.airavata_util.get_execution_id(self.experiment_conf.APPLICATION_NAME)
        project_id = self.airavata_util.get_project_id(self.experiment_conf.PROJECT_NAME)
        hosts = self.experiment_conf.COMPUTE_HOST_DOMAIN.split(",")

        computation_resource_name = computation_resource_name if computation_resource_name is not None else hosts[0]
        resource_host_id = self.airavata_util.get_resource_host_id(computation_resource_name)
        group_resource_profile_id = self.airavata_util.get_group_resource_profile_id(
            self.experiment_conf.GROUP_RESOURCE_PROFILE_NAME)
        storage_id = self.airavata_util.get_storage_resource_id(self.experiment_conf.STORAGE_RESOURCE_HOST)

        logger.info("creating experiment %s", experiment_name)
        experiment = self.data_model_client.get_experiment_data_model_for_single_application(
            project_name=self.experiment_conf.PROJECT_NAME,
            application_name=self.experiment_conf.APPLICATION_NAME,
            experiment_name=experiment_name,
            description=description)

        logger.info("connnecting to file upload endpoint %s : %s", self.experiment_conf.STORAGE_RESOURCE_HOST,
                    self.experiment_conf.SFTP_PORT)
        sftp_connector = SFTPConnector(host=self.experiment_conf.STORAGE_RESOURCE_HOST,
                                       port=self.experiment_conf.SFTP_PORT,
                                       username=self.user_id,
                                       password=self.access_token)

        path_suffix = sftp_connector.upload_files(local_input_path,
                                                  self.experiment_conf.PROJECT_NAME,
                                                  experiment.experimentName)

        logger.info("Input files uploaded to %s", path_suffix)

        path = self.gateway_conf.GATEWAY_DATA_STORE_DIR + path_suffix

        queue_name = queue_name if queue_name is not None else self.experiment_conf.QUEUE_NAME

        node_count = node_count if node_count is not None else self.experiment_conf.NODE_COUNT

        cpu_count = cpu_count if cpu_count is not None else self.experiment_conf.TOTAL_CPU_COUNT

        walltime = walltime if walltime is not None else self.experiment_conf.WALL_TIME_LIMIT

        logger.info("configuring inputs ......")
        experiment = self.data_model_client.configure_computation_resource_scheduling(experiment_model=experiment,
                                                                                      computation_resource_name=computation_resource_name,
                                                                                      group_resource_profile_name=self.experiment_conf.GROUP_RESOURCE_PROFILE_NAME,
                                                                                      storageId=storage_id,
                                                                                      node_count=int(node_count),
                                                                                      total_cpu_count=int(cpu_count),
                                                                                      wall_time_limit=int(walltime),
                                                                                      queue_name=queue_name,
                                                                                      experiment_dir_path=path)
        input_files = []
        if (len(input_file_mapping.keys()) > 0):
            new_file_mapping = {}
            for key in input_file_mapping.keys():
                if type(input_file_mapping[key]) == list:
                    data_uris = []
                    for x in input_file_mapping[key]:
                        data_uri = self.data_model_client.register_input_file(file_identifier=x,
                                                                              storage_name=self.experiment_conf.STORAGE_RESOURCE_HOST,
                                                                              storageId=storage_id,
                                                                              input_file_name=x,
                                                                              uploaded_storage_path=path)
                        data_uris.append(data_uri)
                    new_file_mapping[key] = data_uris
                else:
                    x = input_file_mapping[key]
                    data_uri = self.data_model_client.register_input_file(file_identifier=x,
                                                                          storage_name=self.experiment_conf.STORAGE_RESOURCE_HOST,
                                                                          storageId=storage_id,
                                                                          input_file_name=x,
                                                                          uploaded_storage_path=path)
                    new_file_mapping[key] = data_uri
            experiment = self.data_model_client.configure_input_and_outputs(experiment, input_files=None,
                                                                            application_name=self.experiment_conf.APPLICATION_NAME,
                                                                            file_mapping=new_file_mapping)
        else:
            for x in os.listdir(local_input_path):
                if os.path.isfile(local_input_path + '/' + x):
                    input_files.append(x)

            if len(input_files) > 0:
                data_uris = []
                for x in input_files:
                    data_uri = self.data_model_client.register_input_file(file_identifier=x,
                                                                          storage_name=self.experiment_conf.STORAGE_RESOURCE_HOST,
                                                                          storageId=storage_id,
                                                                          input_file_name=x,
                                                                          uploaded_storage_path=path)
                    data_uris.append(data_uri)
                experiment = self.data_model_client.configure_input_and_outputs(experiment, input_files=data_uris,
                                                                                application_name=self.experiment_conf.APPLICATION_NAME)
            else:
                inputs = self.api_server_client.get_application_inputs(self.airavata_token, execution_id)
                experiment.experimentInputs = inputs

        outputs = self.api_server_client.get_application_outputs(self.airavata_token, execution_id)

        experiment.experimentOutputs = outputs

        # create experiment
        ex_id = self.api_server_client.create_experiment(self.airavata_token, self.gateway_conf.GATEWAY_ID, experiment)

        # launch experiment
        self.api_server_client.launch_experiment(self.airavata_token, ex_id, self.gateway_conf.GATEWAY_ID)

        logger.info("experiment launched id: %s", ex_id)

        experiment_url = 'https://' + self.gateway_conf.GATEWAY_ID + '.org/workspace/experiments/' + ex_id
        logger.info("For more information visit %s", experiment_url)

        if self.experiment_conf.MONITOR_STATUS:
            status = self.api_server_client.get_experiment_status(self.airavata_token, ex_id)
            status_dict = {'0': 'EXECUTING', '4': 'JOB_ACTIVE', '7': 'COMPLETED'}

            if status is not None:
                logger.info("Initial state " + status_dict[str(status.state)])
            while status.state <= 6:
                status = self.api_server_client.get_experiment_status(self.airavata_token,
                                                                      ex_id);
                time.sleep(30)
                if (str(status.state) in status_dict.keys()):
                      logger.info("State " + status_dict[str(status.state)])

            logger.info("Completed")
            remote_path = path_suffix.split(self.user_id)[1]
            sftp_connector.download_files(output_path, remote_path)
