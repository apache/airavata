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
import getpass
import logging
import os
import time
from typing import Optional

import jwt

from airavata_sdk.clients.api_server_client import APIServerClient
from airavata_sdk.clients.keycloak_token_fetcher import Authenticator
from airavata_sdk.clients.sftp_file_handling_client import SFTPConnector
from airavata_sdk.clients.utils.api_server_client_util import APIServerClientUtil
from airavata_sdk.clients.utils.data_model_creation_util import DataModelCreationUtil
from airavata_sdk import Settings

logger = logging.getLogger('airavata_sdk.clients')
logger.setLevel(logging.INFO)


class ExperimentHandlerUtil(object):
    def __init__(self, access_token: Optional[str] = None):
        self.settings = Settings()
        self.authenticator = Authenticator()
        if access_token is None:
          self.authenticator.authenticate_with_auth_code()
          access_token = getpass.getpass('Copy paste the access token')
        self.access_token = access_token
        decode = jwt.decode(access_token, options={"verify_signature": False})
        self.user_id = decode['preferred_username']
        self.airavata_token = self.authenticator.get_airavata_authz_token(
            gateway_id=self.settings.GATEWAY_ID,
            username=self.user_id,
            token=access_token,
        )
        self.airavata_util = APIServerClientUtil(
            gateway_id=self.settings.GATEWAY_ID,
            username=self.user_id,
            password=None,
            access_token=access_token,
        )

        self.data_model_client = DataModelCreationUtil(
                                                       username=self.user_id,
                                                       password=None,
                                                       gateway_id=self.settings.GATEWAY_ID,
                                                       access_token=access_token)

        self.api_server_client = APIServerClient()

    def queue_names(self, computation_resource_name: str):
        resource_id = self.airavata_util.get_resource_host_id(computation_resource_name)
        assert resource_id is not None
        return self.airavata_util.get_queue_names(resource_id)

    def launch_experiment(
        self,
        experiment_name: str = "default_exp",
        description: str = "this is default exp",
        local_input_path: str = "/tmp",
        input_file_mapping: dict[str, str] = {},
        computation_resource_name: Optional[str] = None,
        queue_name: Optional[str] = None,
        node_count: int = 1,
        cpu_count: int = 1,
        walltime: int = 30,
        auto_schedule: bool = False,
        output_path: str = '.',
        group_name: str = "Default",
        application_name: str = "Default Application",
        project_name: str = "Default Project",
    ):
        execution_id = self.airavata_util.get_execution_id(application_name)
        assert execution_id is not None
        project_id = self.airavata_util.get_project_id(project_name)
        assert computation_resource_name is not None
        resource_host_id = self.airavata_util.get_resource_host_id(computation_resource_name)
        group_resource_profile_id = self.airavata_util.get_group_resource_profile_id(group_name)

        storage_host = self.settings.STORAGE_RESOURCE_HOST
        assert storage_host is not None

        sftp_port = self.settings.SFTP_PORT
        assert sftp_port is not None

        storage_id = self.airavata_util.get_storage_resource_id(storage_host)
        assert storage_id is not None

        assert project_name is not None
        assert application_name is not None
        assert experiment_name is not None
        assert description is not None
        logger.info("creating experiment %s", experiment_name)
        experiment = self.data_model_client.get_experiment_data_model_for_single_application(
            project_name=project_name,
            application_name=application_name,
            experiment_name=experiment_name,
            description=description,
        )

        logger.info("connnecting to file upload endpoint %s : %s", storage_host, sftp_port)
        sftp_connector = SFTPConnector(host=storage_host,
                                       port=sftp_port,
                                       username=self.user_id,
                                       password=self.access_token)

        assert local_input_path is not None
        path_suffix = sftp_connector.upload_files(local_input_path,
                                                  project_name,
                                                  experiment_name)

        logger.info("Input files uploaded to %s", path_suffix)

        path = self.settings.GATEWAY_DATA_STORE_DIR + path_suffix

        assert queue_name is not None
        assert node_count is not None
        assert cpu_count is not None
        assert walltime is not None

        logger.info("configuring inputs ......")
        experiment = self.data_model_client.configure_computation_resource_scheduling(experiment_model=experiment,
                                                                                      computation_resource_name=computation_resource_name,
                                                                                      group_resource_profile_name=group_name,
                                                                                      storageId=storage_id,
                                                                                      node_count=int(node_count),
                                                                                      total_cpu_count=int(cpu_count),
                                                                                      wall_time_limit=int(walltime),
                                                                                      queue_name=queue_name,
                                                                                      experiment_dir_path=path,
                                                                                      auto_schedule=auto_schedule)
        input_files = []
        if (len(input_file_mapping.keys()) > 0):
            new_file_mapping = {}
            for key in input_file_mapping.keys():
                if type(input_file_mapping[key]) == list:
                    data_uris = []
                    for x in input_file_mapping[key]:
                        data_uri = self.data_model_client.register_input_file(file_identifier=x,
                                                                              storage_name=storage_host,
                                                                              storageId=storage_id,
                                                                              input_file_name=x,
                                                                              uploaded_storage_path=path)
                        data_uris.append(data_uri)
                    new_file_mapping[key] = data_uris
                else:
                    x = input_file_mapping[key]
                    data_uri = self.data_model_client.register_input_file(file_identifier=x,
                                                                          storage_name=storage_host,
                                                                          storageId=storage_id,
                                                                          input_file_name=x,
                                                                          uploaded_storage_path=path)
                    new_file_mapping[key] = data_uri
            experiment = self.data_model_client.configure_input_and_outputs(experiment, input_files=[],
                                                                            application_name=application_name,
                                                                            file_mapping=new_file_mapping)
        else:
            for x in os.listdir(local_input_path):
                if os.path.isfile(local_input_path + '/' + x):
                    input_files.append(x)

            if len(input_files) > 0:
                data_uris = []
                for x in input_files:
                    data_uri = self.data_model_client.register_input_file(file_identifier=x,
                                                                          storage_name=storage_host,
                                                                          storageId=storage_id,
                                                                          input_file_name=x,
                                                                          uploaded_storage_path=path)
                    data_uris.append(data_uri)
                experiment = self.data_model_client.configure_input_and_outputs(experiment, input_files=data_uris,
                                                                                application_name=application_name)
            else:
                inputs = self.api_server_client.get_application_inputs(self.airavata_token, execution_id)
                experiment.experimentInputs = inputs

        outputs = self.api_server_client.get_application_outputs(self.airavata_token, execution_id)

        experiment.experimentOutputs = outputs

        # create experiment
        ex_id = self.api_server_client.create_experiment(self.airavata_token, self.settings.GATEWAY_ID, experiment)

        # launch experiment
        self.api_server_client.launch_experiment(self.airavata_token, ex_id, self.settings.GATEWAY_ID)

        logger.info("experiment launched id: %s", ex_id)

        experiment_url = f"{self.settings.GATEWAY_URL}/workspace/experiments/{ex_id}"
        logger.info("For more information visit %s", experiment_url)

        if self.settings.MONITOR_STATUS:
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
