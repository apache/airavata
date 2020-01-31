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
from airavata.model.data.replica.ttypes import DataProductModel, DataProductType, DataReplicaLocationModel, \
    ReplicaLocationCategory, ReplicaPersistentType

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
experiment.experimentName = "Gaussian experiment testing 4"
experiment.gatewayId = "cyberwater"
experiment.userName = "isuru_janith"
experiment.description = "SDK testing"
experiment.projectId = "Default_Project_6b6f1a82-2db8-4f57-ac4d-15c3ec8cafa9"
experiment.experimentType = ExperimentType.SINGLE_APPLICATION
experiment.executionId = "Gaussian_2cf3d51a-d9e7-4c3d-a326-dfcc4364b1d9"

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

dataProductModel = DataProductModel()
dataProductModel.gatewayId = "cyberwater"
dataProductModel.ownerName = "isuru_janith"
dataProductModel.productName = "gaussian_file"
dataProductModel.dataProductType = DataProductType.FILE

replicaLocation = DataReplicaLocationModel()
replicaLocation.storageResourceId = "pgadev.scigap.org_7ddf28fd-d503-4ff8-bbc5-3279a7c3b99e"
replicaLocation.replicaName = "{} gateway data store copy".format("npentane12diol.inp")
replicaLocation.replicaLocationCategory = ReplicaLocationCategory.GATEWAY_DATA_STORE
replicaLocation.filePath = "file://{}:{}".format("pgadev.scigap.org",
                                                 "/home/pga/portals/django-cyberwater/npentane12diol.inp")

print(replicaLocation.filePath)
dataProductModel.replicaLocations = [replicaLocation]

dataURI = api_server_client.register_data_product(token, dataProductModel)
print(dataURI)
inputs = api_server_client.get_application_inputs(token, "Gaussian_2cf3d51a-d9e7-4c3d-a326-dfcc4364b1d9")
print(inputs)
if isinstance(inputs[0], InputDataObjectType):
    inputs[0].value = dataURI
    print(inputs)

# gEXP = api_server_client.get_experiment(token, "Gaussian_on_Jan_30,_2020_2:11_PM_da332f49-35dc-4586-916c-06a04254d0c9 ")

# api_server_client.register_data_product()

# print(gEXP)

experiment.experimentInputs = inputs

outputs = api_server_client.get_application_outputs(token, "Gaussian_2cf3d51a-d9e7-4c3d-a326-dfcc4364b1d9")

experiment.experimentOutputs = outputs

# create experiment
ex_id = api_server_client.create_experiment(token, "cyberwater", experiment)

# launch experiment
api_server_client.launch_experiment(token, ex_id, "cyberwater")
