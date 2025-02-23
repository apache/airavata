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

from airavata_sdk.clients.api_server_client import APIServerClient
from airavata_sdk.clients.keycloak_token_fetcher import Authenticator

logger = logging.getLogger(__name__)

logger.setLevel(logging.DEBUG)

configFile = "/Users/isururanawaka/Documents/Cyberwater/poc/resources/settings.ini"

authenticator = Authenticator(configFile)
token = authenticator.get_token_and_user_info_password_flow("username", "password", "cyberwater")

api_server_client = APIServerClient(configFile)

# fetch all application deployments
deployments = api_server_client.get_all_application_deployments(token, "cyberwater");
print(deployments);
# appModuleId  for execution Id


# compute resource names and Ids
compute_resoure_name = api_server_client.get_all_compute_resource_names(token);
print(compute_resoure_name);

# get  resource profiles
resource_profile = api_server_client.get_all_gateway_resource_profiles(token);
print(resource_profile);

# get resource profiles
group_resource_list = api_server_client.get_group_resource_list(token, "cyberwater");
print(group_resource_list);

# provides storage resources
storage_resource = api_server_client.get_all_storage_resource_names(token)
print(storage_resource);
