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
import json
import airavata_sdk.samples.file_utils as fb

from airavata_sdk.clients.keycloak_token_fetcher import Authenticator

from airavata_sdk.clients.api_server_client import APIServerClient

from airavata_sdk.clients.utils.api_server_client_util import APIServerClientUtil

from airavata_sdk.clients.credential_store_client import CredentialStoreClient

from airavata_sdk.clients.utils.data_model_creation_util import DataModelCreationUtil

from airavata.model.workspace.ttypes import Gateway, Notification, Project
from airavata.model.experiment.ttypes import ExperimentModel, ExperimentType, UserConfigurationDataModel
from airavata.model.scheduling.ttypes import ComputationalResourceSchedulingModel
from airavata.model.data.replica.ttypes import DataProductModel, DataProductType, DataReplicaLocationModel, \
    ReplicaLocationCategory, ReplicaPersistentType

from airavata.model.application.io.ttypes import InputDataObjectType

from airavata.model.appcatalog.groupresourceprofile.ttypes import GroupResourceProfile

from airavata.api.error.ttypes import TException, InvalidRequestException, AiravataSystemException, \
    AiravataClientException, AuthorizationException

logger = logging.getLogger(__name__)

logger.setLevel(logging.DEBUG)

configFile = "/Users/isururanawaka/Documents/Cyberwater/poc/resources/settings.ini"

authenticator = Authenticator(configFile)

user_name = "username"
password = "password"
gateway_id = "cyberwater"

token = authenticator.get_token_and_user_info_password_flow(username=user_name, password=password,
                                                            gateway_id=gateway_id)

api_server_client = APIServerClient(configFile)

airavata_util = APIServerClientUtil(configFile, username=user_name, password=password, gateway_id=gateway_id)
data_model_client = DataModelCreationUtil(configFile,
                                          username=user_name,
                                          password=password,
                                          gateway_id=gateway_id)

credential_store_client = CredentialStoreClient(configFile)

executionId = airavata_util.get_execution_id("Gaussian")
projectId = airavata_util.get_project_id("Default Project")

resourceHostId = airavata_util.get_resource_host_id("karst.uits.iu.edu")

groupResourceProfileId = airavata_util.get_group_resource_profile_id("Default Gateway Profile")

storageId = airavata_util.get_storage_resource_id("pgadev.scigap.org")

# create Experiment data Model

experiment = data_model_client.get_experiment_data_model_for_single_application(
    project_name="Default Project",
    application_name="Gaussian",
    experiment_name="Gaussian_16",
    description="Testing")

folder_name = "storage"

path = fb.upload_files(api_server_client, credential_store_client, token, gateway_id,
                       storageId,
                       "pgadev.scigap.org", user_name, "Default_Project", executionId,
                       "/Users/isururanawaka/Documents/Cyberwater/poc/resources/storage/")

experiment = data_model_client.configure_computation_resource_scheduling(experiment_model=experiment,
                                                                         computation_resource_name="karst.uits.iu.edu",
                                                                         group_resource_profile_name="Default Gateway Profile",
                                                                         storage_name="pgadev.scigap.org",
                                                                         node_count=1,
                                                                         total_cpu_count=16,
                                                                         wall_time_limit=15,
                                                                         queue_name="batch",
                                                                         experiment_dir_path=path)

data_uri = data_model_client.register_input_file(file_identifier="npentane12diol.inp",
                                                 storage_name='pgadev.scigap.org',
                                                 storageId='pgadev.scigap.org_asdasdad',
                                                 input_file_name="npentane12diol.inp",
                                                 uploaded_storage_path=path)

input_files = [data_uri]

experiment = data_model_client.configure_input_and_outputs(experiment, input_files=input_files,
                                                           application_name="Gaussian")

# create experiment
ex_id = api_server_client.create_experiment(token, "cyberwater", experiment)

# launch experiment
api_server_client.launch_experiment(token, ex_id, "cyberwater")

status = api_server_client.get_experiment_status(token, ex_id);

if status is not None:
    print("Initial state " + str(status.state))
while status.state <= 6:
    status = api_server_client.get_experiment_status(token,
                                                     ex_id);
    time.sleep(30)
    print("State " + str(status.state))

print("Completed")

fb.download_files(api_server_client, credential_store_client, token, "cyberwater",
                  storageId,
                  "pgadev.scigap.org", user_name, "Default_Project", executionId, ".")
