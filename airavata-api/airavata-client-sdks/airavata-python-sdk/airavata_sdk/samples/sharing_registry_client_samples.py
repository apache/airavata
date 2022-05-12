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
from airavata_sdk.clients.sharing_registry_client import SharingRegistryClient

from airavata_sdk.clients.keycloak_token_fetcher import Authenticator

from airavata.api.error.ttypes import TException

from airavata.model.sharing.ttypes import Domain, Entity, EntityType

logger = logging.getLogger(__name__)

logger.setLevel(logging.DEBUG)

authenticator = Authenticator();
token = authenticator.get_token_and_user_info_password_flow("default-admin", "123456", "default")

# load GroupManagerClient with default configuration
client = SharingRegistryClient()


# load client with given configuration file (e.g customized_settings.ini)

# client = SharingRegistryClient('../transport/settings.ini')

# create domian
def create_domain():
    try:
        domain = Domain()
        domain.domainId = "gw@scigap.org"
        domain.name = "gw"
        domain.description = "this domain is used by testing server"

        domain = client.create_domain(domain)
        print("Domian created :", domain)

    except TException:
        logger.exception("Error occurred")


# get domain
def get_domain():
    try:

        domains = client.get_domain("gw")
        print("Domians created :", domains)

    except TException:
        logger.exception("Error occurred")


def create_entity_type():
    try:
        entity_type = EntityType()
        entity_type.domainId = "gw@scigap.org"
        entity_type.description = "project entity type"
        entity_type.name = "PROJECT"
        entity_type.entityTypeId = "gw@scigap.org:PROJECT"
        en_type = client.create_entity_type(entity_type)
        print("Entity Type ", en_type)
    except TException:
        logger.exception("Error occurred")


def create_entity():
    try:
        entity = Entity()
        entity.entityTypeId = "gw@scigap.org:PROJECT"
        entity.name = "PROJECT_ENTITY"
        entity.domainId = "gw"
        entity.ownerId = "default-admin"
        en_type = client.create_entity(entity)
        print("Entity Type ", en_type)
    except TException:
        logger.exception("Error occurred")

