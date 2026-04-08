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
from airavata_sdk.transport.utils import create_research_project_service_stub

logger = logging.getLogger(__name__)
logger.setLevel(logging.DEBUG)


class ResearchProjectClient:
    """Client for the Research Project Service (gRPC).

    Provides methods for creating, listing, and deleting research projects.
    """

    def __init__(self, access_token: Optional[str] = None, claims: Optional[dict] = None):
        self.settings = Settings()
        host = self.settings.API_SERVER_HOSTNAME
        port = self.settings.API_SERVER_PORT
        secure = self.settings.API_SERVER_SECURE

        target = f"{host}:{port}"
        if secure:
            self.channel = grpc.secure_channel(target, grpc.ssl_channel_credentials())
        else:
            self.channel = grpc.insecure_channel(target)

        self._metadata: list[tuple[str, str]] = []
        if access_token:
            self._metadata.append(("authorization", f"Bearer {access_token}"))
        if claims:
            self._metadata.append(("x-claims", json.dumps(claims)))

        self._stub = create_research_project_service_stub(self.channel)

    def close(self):
        self.channel.close()

    def get_all_projects(self):
        from airavata_sdk.generated.services import research_service_pb2 as pb2
        return self._stub.GetAllProjects(
            pb2.GetAllProjectsRequest(),
            metadata=self._metadata,
        )

    def get_projects_by_owner(self, owner_id: str):
        from airavata_sdk.generated.services import research_service_pb2 as pb2
        return self._stub.GetProjectsByOwner(
            pb2.GetProjectsByOwnerRequest(owner_id=owner_id),
            metadata=self._metadata,
        )

    def create_project(self, name: str, owner_id: str, repository_id: str = "", dataset_ids: list[str] = None):
        from airavata_sdk.generated.services import research_service_pb2 as pb2
        return self._stub.CreateProject(
            pb2.CreateResearchProjectRequest(
                name=name, owner_id=owner_id,
                repository_id=repository_id, dataset_ids=dataset_ids or [],
            ),
            metadata=self._metadata,
        )

    def delete_project(self, project_id: str):
        from airavata_sdk.generated.services import research_service_pb2 as pb2
        return self._stub.DeleteProject(
            pb2.DeleteProjectRequest(project_id=project_id),
            metadata=self._metadata,
        )
