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
from google.protobuf.struct_pb2 import Struct

from airavata_sdk import Settings
from airavata_sdk.transport.utils import create_research_resource_service_stub

logger = logging.getLogger(__name__)
logger.setLevel(logging.DEBUG)


class ResearchResourceClient:
    """Client for the Research Resource Service (gRPC).

    Provides methods for creating, searching, starring, and managing
    datasets, notebooks, repositories, and models.
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

        self._stub = create_research_resource_service_stub(self.channel)

    def close(self):
        self.channel.close()

    # --- Resource creation ---

    def create_dataset(self, data: dict):
        struct = Struct()
        struct.update(data)
        return self._stub.CreateDataset(
            struct,
            metadata=self._metadata,
        )

    def create_notebook(self, data: dict):
        struct = Struct()
        struct.update(data)
        return self._stub.CreateNotebook(
            struct,
            metadata=self._metadata,
        )

    def create_repository(self, name: str, description: str = "", header_image: str = "",
                          tags: list[str] = None, authors: list[str] = None,
                          privacy: str = "PUBLIC", github_url: str = ""):
        from airavata_sdk.generated.services import research_service_pb2 as pb2
        resource = pb2.CreateResourceRequest(
            name=name, description=description, header_image=header_image,
            tags=tags or [], authors=authors or [], privacy=privacy,
        )
        return self._stub.CreateRepository(
            pb2.CreateRepositoryResourceRequest(resource=resource, github_url=github_url),
            metadata=self._metadata,
        )

    def modify_repository(self, id: str, name: str = "", description: str = "",
                          header_image: str = "", tags: list[str] = None,
                          authors: list[str] = None, privacy: str = ""):
        from airavata_sdk.generated.services import research_service_pb2 as pb2
        return self._stub.ModifyRepository(
            pb2.ModifyResourceRequest(
                id=id, name=name, description=description,
                header_image=header_image, tags=tags or [],
                authors=authors or [], privacy=privacy,
            ),
            metadata=self._metadata,
        )

    def create_model(self, data: dict):
        struct = Struct()
        struct.update(data)
        return self._stub.CreateModel(
            struct,
            metadata=self._metadata,
        )

    # --- Resource queries ---

    def get_tags(self, page_number: int = 0, page_size: int = 0, name_search: str = "",
                 types: list[str] = None, tags: list[str] = None):
        from airavata_sdk.generated.services import research_service_pb2 as pb2
        return self._stub.GetTags(
            pb2.GetAllResourcesRequest(
                page_number=page_number, page_size=page_size,
                name_search=name_search, types=types or [], tags=tags or [],
            ),
            metadata=self._metadata,
        )

    def get_resource(self, id: str):
        from airavata_sdk.generated.services import research_service_pb2 as pb2
        return self._stub.GetResource(
            pb2.ResourceIdRequest(id=id),
            metadata=self._metadata,
        )

    def delete_resource(self, id: str):
        from airavata_sdk.generated.services import research_service_pb2 as pb2
        return self._stub.DeleteResource(
            pb2.ResourceIdRequest(id=id),
            metadata=self._metadata,
        )

    def get_all_resources(self, page_number: int = 0, page_size: int = 0, name_search: str = "",
                          types: list[str] = None, tags: list[str] = None):
        from airavata_sdk.generated.services import research_service_pb2 as pb2
        return self._stub.GetAllResources(
            pb2.GetAllResourcesRequest(
                page_number=page_number, page_size=page_size,
                name_search=name_search, types=types or [], tags=tags or [],
            ),
            metadata=self._metadata,
        )

    def search_resources(self, type: str, name: str):
        from airavata_sdk.generated.services import research_service_pb2 as pb2
        return self._stub.SearchResources(
            pb2.SearchResourceRequest(type=type, name=name),
            metadata=self._metadata,
        )

    def get_projects_for_resource(self, id: str):
        from airavata_sdk.generated.services import research_service_pb2 as pb2
        return self._stub.GetProjectsForResource(
            pb2.ResourceIdRequest(id=id),
            metadata=self._metadata,
        )

    # --- Starring ---

    def star_resource(self, id: str):
        from airavata_sdk.generated.services import research_service_pb2 as pb2
        return self._stub.StarResource(
            pb2.StarResourceRequest(id=id),
            metadata=self._metadata,
        )

    def check_user_starred_resource(self, id: str):
        from airavata_sdk.generated.services import research_service_pb2 as pb2
        return self._stub.CheckUserStarredResource(
            pb2.StarResourceRequest(id=id),
            metadata=self._metadata,
        )

    def get_resource_star_count(self, id: str):
        from airavata_sdk.generated.services import research_service_pb2 as pb2
        return self._stub.GetResourceStarCount(
            pb2.GetResourceStarCountRequest(id=id),
            metadata=self._metadata,
        )

    def get_starred_resources(self, user_id: str):
        from airavata_sdk.generated.services import research_service_pb2 as pb2
        return self._stub.GetStarredResources(
            pb2.GetStarredResourcesRequest(user_id=user_id),
            metadata=self._metadata,
        )
