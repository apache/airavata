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

"""Call Airavata directly through the generated gRPC stubs.

``airavata`` ships only the protoc-generated stubs (``airavata``).
Build a Bearer-authenticated channel with ``airavata.auth.authenticated_channel``
and construct any service stub on it; the channel's interceptor attaches the
access token to every call, so there is no per-call metadata to thread. This is
exactly how the Django portal and ``airavata.experiments`` talk to the server —
no hand-written client wrapper in between.

    python -m airavata.samples.read_with_raw_stubs
"""

from airavata.auth import authenticated_channel
from airavata.auth.device_auth import AuthContext
from airavata import Settings
from airavata.services import (
    application_catalog_service_pb2,
    application_catalog_service_pb2_grpc,
)


def main() -> None:
    settings = Settings()
    # Device-code login; caches the token in CS_ACCESS_TOKEN.
    access_token = AuthContext.get_access_token()

    channel = authenticated_channel(
        settings.API_SERVER_HOSTNAME,
        settings.API_SERVER_PORT,
        access_token,
        secure=settings.API_SERVER_SECURE,
    )
    catalog = application_catalog_service_pb2_grpc.ApplicationCatalogServiceStub(channel)

    response = catalog.GetAllApplicationInterfaces(
        application_catalog_service_pb2.GetAllApplicationInterfacesRequest(
            gateway_id=settings.GATEWAY_ID,
        )
    )
    for interface in response.application_interfaces:
        print(interface.application_interface_id, "-", interface.application_name)


if __name__ == "__main__":
    main()
