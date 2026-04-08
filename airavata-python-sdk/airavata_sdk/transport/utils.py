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

import json
import logging
from typing import Optional

import grpc

from airavata_sdk import Settings

log = logging.getLogger(__name__)

settings = Settings()


class GrpcChannel:
    """Manages a gRPC channel with optional TLS."""

    def __init__(self, host: str, port: int, secure: bool = False):
        self.target = f"{host}:{port}"
        if secure:
            self.channel = grpc.secure_channel(self.target, grpc.ssl_channel_credentials())
        else:
            self.channel = grpc.insecure_channel(self.target)
        log.debug(f"[AV] Created gRPC channel to {self.target} (secure={secure})")

    def close(self):
        self.channel.close()


class AuthMetadataPlugin(grpc.AuthMetadataPlugin):
    """Injects Bearer token into gRPC call metadata."""

    def __init__(self, access_token: str, claims: Optional[dict] = None):
        self.access_token = access_token
        self.claims = claims or {}

    def __call__(self, context, callback):
        metadata = [("authorization", f"Bearer {self.access_token}")]
        if self.claims:
            metadata.append(("x-claims", json.dumps(self.claims)))
        callback(metadata, None)


def build_metadata(access_token: Optional[str] = None, claims: Optional[dict] = None) -> list[tuple[str, str]]:
    """Build gRPC call metadata from token and claims."""
    metadata = []
    if access_token:
        metadata.append(("authorization", f"Bearer {access_token}"))
    if claims:
        metadata.append(("x-claims", json.dumps(claims)))
    return metadata


# ---------------------------------------------------------------------------
# Stub factory helpers
# ---------------------------------------------------------------------------

def create_experiment_service_stub(channel: grpc.Channel):
    from airavata_sdk.generated.services import experiment_service_pb2_grpc
    return experiment_service_pb2_grpc.ExperimentServiceStub(channel)


def create_project_service_stub(channel: grpc.Channel):
    from airavata_sdk.generated.services import project_service_pb2_grpc
    return project_service_pb2_grpc.ProjectServiceStub(channel)


def create_gateway_service_stub(channel: grpc.Channel):
    from airavata_sdk.generated.services import gateway_service_pb2_grpc
    return gateway_service_pb2_grpc.GatewayServiceStub(channel)


def create_application_catalog_service_stub(channel: grpc.Channel):
    from airavata_sdk.generated.services import application_catalog_service_pb2_grpc
    return application_catalog_service_pb2_grpc.ApplicationCatalogServiceStub(channel)


def create_resource_service_stub(channel: grpc.Channel):
    from airavata_sdk.generated.services import resource_service_pb2_grpc
    return resource_service_pb2_grpc.ResourceServiceStub(channel)


def create_credential_service_stub(channel: grpc.Channel):
    from airavata_sdk.generated.services import credential_service_pb2_grpc
    return credential_service_pb2_grpc.CredentialServiceStub(channel)


def create_sharing_service_stub(channel: grpc.Channel):
    from airavata_sdk.generated.services import sharing_service_pb2_grpc
    return sharing_service_pb2_grpc.SharingServiceStub(channel)


def create_notification_service_stub(channel: grpc.Channel):
    from airavata_sdk.generated.services import notification_service_pb2_grpc
    return notification_service_pb2_grpc.NotificationServiceStub(channel)


def create_data_product_service_stub(channel: grpc.Channel):
    from airavata_sdk.generated.services import data_product_service_pb2_grpc
    return data_product_service_pb2_grpc.DataProductServiceStub(channel)


def create_gateway_resource_profile_service_stub(channel: grpc.Channel):
    from airavata_sdk.generated.services import gateway_resource_profile_service_pb2_grpc
    return gateway_resource_profile_service_pb2_grpc.GatewayResourceProfileServiceStub(channel)


def create_user_resource_profile_service_stub(channel: grpc.Channel):
    from airavata_sdk.generated.services import user_resource_profile_service_pb2_grpc
    return user_resource_profile_service_pb2_grpc.UserResourceProfileServiceStub(channel)


def create_group_resource_profile_service_stub(channel: grpc.Channel):
    from airavata_sdk.generated.services import group_resource_profile_service_pb2_grpc
    return group_resource_profile_service_pb2_grpc.GroupResourceProfileServiceStub(channel)


def create_parser_service_stub(channel: grpc.Channel):
    from airavata_sdk.generated.services import parser_service_pb2_grpc
    return parser_service_pb2_grpc.ParserServiceStub(channel)


def create_group_manager_service_stub(channel: grpc.Channel):
    from airavata_sdk.generated.services import group_manager_service_pb2_grpc
    return group_manager_service_pb2_grpc.GroupManagerServiceStub(channel)


def create_iam_admin_service_stub(channel: grpc.Channel):
    from airavata_sdk.generated.services import iam_admin_service_pb2_grpc
    return iam_admin_service_pb2_grpc.IamAdminServiceStub(channel)


def create_user_profile_service_stub(channel: grpc.Channel):
    from airavata_sdk.generated.services import user_profile_service_pb2_grpc
    return user_profile_service_pb2_grpc.UserProfileServiceStub(channel)


def create_agent_interaction_service_stub(channel: grpc.Channel):
    from airavata_sdk.generated.services import agent_service_pb2_grpc
    return agent_service_pb2_grpc.AgentInteractionServiceStub(channel)


def create_plan_service_stub(channel: grpc.Channel):
    from airavata_sdk.generated.services import agent_service_pb2_grpc
    return agent_service_pb2_grpc.PlanServiceStub(channel)


def create_experiment_management_service_stub(channel: grpc.Channel):
    from airavata_sdk.generated.services import experiment_management_service_pb2_grpc
    return experiment_management_service_pb2_grpc.ExperimentManagementServiceStub(channel)


def create_research_hub_service_stub(channel: grpc.Channel):
    from airavata_sdk.generated.services import research_service_pb2_grpc
    return research_service_pb2_grpc.ResearchHubServiceStub(channel)


def create_research_project_service_stub(channel: grpc.Channel):
    from airavata_sdk.generated.services import research_service_pb2_grpc
    return research_service_pb2_grpc.ResearchProjectServiceStub(channel)


def create_research_resource_service_stub(channel: grpc.Channel):
    from airavata_sdk.generated.services import research_service_pb2_grpc
    return research_service_pb2_grpc.ResearchResourceServiceStub(channel)


def create_research_session_service_stub(channel: grpc.Channel):
    from airavata_sdk.generated.services import research_service_pb2_grpc
    return research_service_pb2_grpc.ResearchSessionServiceStub(channel)


def create_user_storage_service_stub(channel: grpc.Channel):
    from airavata_sdk.generated.services import file_service_pb2_grpc
    return file_service_pb2_grpc.UserStorageServiceStub(channel)
