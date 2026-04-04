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

from airavata_sdk.generated.org.apache.airavata.model.sharing import sharing_pb2
from airavata_sdk.clients.keycloak_token_fetcher import Authenticator
from airavata_sdk.clients.sharing_registry_client import SharingRegistryClient

logger = logging.getLogger(__name__)

logger.setLevel(logging.DEBUG)

authenticator = Authenticator()
token = authenticator.get_token_and_user_info_password_flow("default-admin", "123456", "default")

# load SharingRegistryClient with access token
client = SharingRegistryClient(access_token=token)


# create domain
def create_domain():
    try:
        domain = sharing_pb2.Domain(
            domain_id="gw@scigap.org",
            name="gw",
            description="this domain is used by testing server",
        )
        domain_id = client.create_domain(domain)
        print("Domain created :", domain_id)
    except Exception:
        logger.exception("Error occurred")


# get domain
def get_domain():
    try:
        domain = client.get_domain("gw")
        print("Domain :", domain)
    except Exception:
        logger.exception("Error occurred")


def create_entity_type():
    try:
        entity_type = sharing_pb2.EntityType(
            domain_id="gw@scigap.org",
            description="project entity type",
            name="PROJECT",
            entity_type_id="gw@scigap.org:PROJECT",
        )
        en_type = client.create_entity_type(entity_type)
        print("Entity Type ", en_type)
    except Exception:
        logger.exception("Error occurred")


def create_entity():
    try:
        entity = sharing_pb2.Entity(
            entity_type_id="gw@scigap.org:PROJECT",
            name="PROJECT_ENTITY",
            domain_id="gw",
            owner_id="default-admin",
        )
        en_type = client.create_entity(entity)
        print("Entity Type ", en_type)
    except Exception:
        logger.exception("Error occurred")
