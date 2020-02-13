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

from clients.keycloak_token_fetcher import Authenticator

from clients.api_server_client import APIServerClient



from airavata.model.workspace.ttypes import Gateway, Notification, Project
from airavata.model.experiment.ttypes import ExperimentModel, ExperimentType, UserConfigurationDataModel
from airavata.model.scheduling.ttypes import ComputationalResourceSchedulingModel

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

# create Experiment data Model
experiment = ExperimentModel()
experiment.experimentName = "Testing_ECHO_SDK 10"
experiment.gatewayId = "cyberwater"
experiment.userName = "isuru_janith"
experiment.description = "SDK testing"
experiment.projectId = "Default_Project_6b6f1a82-2db8-4f57-ac4d-15c3ec8cafa9"
experiment.experimentType = ExperimentType.SINGLE_APPLICATION
experiment.executionId = "Echo_a2081a37-fbe2-4aec-8657-b1d8f7f66bef"

computRes = ComputationalResourceSchedulingModel()
computRes.resourceHostId = "karst.uits.iu.edu_a9a65e7d-d104-4c11-829b-412168bed7a8"
computRes.nodeCount = 1
computRes.totalCPUCount = 16
computRes.queueName = "batch"
computRes.wallTimeLimit = 15

userConfigData = UserConfigurationDataModel()
userConfigData.computationalResourceScheduling = computRes

userConfigData.groupResourceProfileId = "9f27b6b2-70e2-4508-9229-7b5394e2a522"
userConfigData.storageId = "pgadev.scigap.org_7ddf28fd-d503-4ff8-bbc5-3279a7c3b99e"
userConfigData.experimentDataDir = "/var/www/portals/gateway-user-data/django-cyberwater/isuru_janith/Default_Project/TestingData"

experiment.userConfigurationData = userConfigData

inputs = api_server_client.get_application_inputs(token, "Echo_a2081a37-fbe2-4aec-8657-b1d8f7f66bef")

experiment.experimentInputs = inputs

outputs = api_server_client.get_application_outputs(token, "Echo_a2081a37-fbe2-4aec-8657-b1d8f7f66bef")

experiment.experimentOutputs = outputs

# create experiment
ex_id = api_server_client.create_experiment(token, "cyberwater", experiment)

# launch experiment
api_server_client.launch_experiment(token, ex_id,
                                    "cyberwater")
