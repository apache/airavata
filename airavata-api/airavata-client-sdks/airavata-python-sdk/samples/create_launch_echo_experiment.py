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
import time
import logging
import samples.file_utils as fb

from clients.keycloak_token_fetcher import Authenticator

from clients.api_server_client import APIServerClient
from clients.file_handling_client import FileHandler
from clients.credential_store_client import CredentialStoreClient

from airavata.model.workspace.ttypes import Gateway, Notification, Project
from airavata.model.experiment.ttypes import ExperimentModel, ExperimentType, UserConfigurationDataModel
from airavata.model.scheduling.ttypes import ComputationalResourceSchedulingModel

from clients.utils.api_server_client_util import APIServerClientUtil

from airavata.model.application.io.ttypes import InputDataObjectType

from airavata.model.appcatalog.groupresourceprofile.ttypes import GroupResourceProfile

from airavata.api.error.ttypes import TException, InvalidRequestException, AiravataSystemException, \
    AiravataClientException, AuthorizationException

logger = logging.getLogger(__name__)

logger.setLevel(logging.DEBUG)

configFile = "transport/settings.ini"

authenticator = Authenticator(configFile)
token = authenticator.get_token_and_user_info_password_flow("username", "password", "cyberwater")

api_server_client = APIServerClient(configFile)

credential_store_client = CredentialStoreClient(configFile)

file_handler = FileHandler("pgadev.scigap.org", 22, "pga", "XXXXXXX")

utils_client = APIServerClientUtil(configFile, username="username", password="password", gateway_id="gatewayId")

executionId = utils_client.get_execution_id("Echo")
projectId = utils_client.get_project_id("Default Project")

resourceHostId = utils_client.get_resource_host_id("karst.uits.iu.edu")

groupResourceProfileId = utils_client.get_group_resource_profile_id("XXXX")

storageId = utils_client.get_storage_resource_id("pgadev.scigap.org")


# create Experiment data Model
experiment = ExperimentModel()
experiment.experimentName = "Testing_ECHO_SDK 10"
experiment.gatewayId = "cyberwater"
experiment.userName = "isuru_janith"
experiment.description = "SDK testing"
experiment.projectId = projectId
experiment.experimentType = ExperimentType.SINGLE_APPLICATION
experiment.executionId = executionId

computRes = ComputationalResourceSchedulingModel()
computRes.resourceHostId = resourceHostId
computRes.nodeCount = 1
computRes.totalCPUCount = 16
computRes.queueName = "batch"
computRes.wallTimeLimit = 15

userConfigData = UserConfigurationDataModel()
userConfigData.computationalResourceScheduling = computRes

userConfigData.groupResourceProfileId = groupResourceProfileId
userConfigData.storageId = storageId

path = fb.upload_files(api_server_client, credential_store_client, token, "cyberwater",
                       storageId,
                       "pgadev.scigap.org", "isuru_janith", "Default_Project", experiment.experimentName,
                       "/Users/isururanawaka/Documents/Cyberwater/poc/resources/storage")

userConfigData.experimentDataDir = path;

experiment.userConfigurationData = userConfigData

inputs = api_server_client.get_application_inputs(token, executionId)


experiment.experimentInputs = inputs

outputs = api_server_client.get_application_outputs(token, executionId)

experiment.experimentOutputs = outputs

# create experiment
ex_id = api_server_client.create_experiment(token, "cyberwater", experiment)
print(ex_id)
# launch experiment
api_server_client.launch_experiment(token, ex_id,
                                    "cyberwater")

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
                  "pgadev.scigap.org", "isuru_janith", "Default_Project", experiment.experimentName, ".")
