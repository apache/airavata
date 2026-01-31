<!--
    Licensed to the Apache Software Foundation (ASF) under one
    or more contributor license agreements.  See the NOTICE file
    distributed with this work for additional information
    regarding copyright ownership.  The ASF licenses this file
    to you under the Apache License, Version 2.0 (the
    "License"); you may not use this file except in compliance
    with the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied.  See the License for the
    specific language governing permissions and limitations
    under the License.
-->

# Research Service

The Research Service is part of the unified Airavata server architecture. It runs as a component within the single Spring Boot application (`AiravataServer`) alongside all other Airavata services.

## Architecture

The Research Service is **not a standalone application**. It is integrated into the unified Airavata server and accessible via:

This is part of one of four external API layers in Airavata:
- **Thrift Server** (port 8930) - Thrift Endpoints for Airavata API functions
- **HTTP Server** (port 8080):
  - Airavata API - HTTP Endpoints for Airavata API functions
  - File API - HTTP Endpoints for file upload/download
  - Agent API - HTTP Endpoints for interactive job contexts
  - **Research API** (this module) - HTTP Endpoints for use by research hub
- **gRPC Server** (port 9090) - For airavata binaries to open persistent channels with airavata APIs
- **Dapr gRPC** (port 50001) - Sidecar for pub/sub, state, and workflow execution

**Note:** Research API provides both HTTP endpoints (on HTTP Server, port 8080) and persistent channels (on gRPC Server, port 9090). The HTTP endpoints handle request/response operations, while the gRPC server enables airavata binaries to open persistent channels with airavata APIs for long-lived persistent connections.

## Configuration

Research Service is configured via `application.properties`:

```properties
# Enable/disable Research Service
airavata.services.research.enabled=true

# Research Hub (JupyterHub) configuration
airavata.services.research.hub.url=http://localhost:20000
airavata.services.research.hub.adminApiKey=JUPYTER_ADMIN_API_KEY
airavata.services.research.hub.limit=10

# OpenID Connect configuration
airavata.services.research.openid.url=http://localhost:18080/realms/default

# Portal URLs for CORS
airavata.services.research.portal.url=http://localhost:5173
airavata.services.research.portal.dev-url=http://localhost:5173

# gRPC keepalive settings
airavata.services.research.grpc.keepalive-time=30s
airavata.services.research.grpc.keepalive-timeout=5s
airavata.services.research.grpc.permit-keepalive-time=5m
airavata.services.research.grpc.permit-keepalive-without-calls=true
airavata.services.research.grpc.max-inbound-message-size=20971520
```

## Security

The Research Service includes authentication and authorization:

- **AuthzTokenFilter**: Enforces authentication for mutation requests (POST, PUT, PATCH, DELETE)
- **OpenAPI/Swagger**: Configured with OAuth2 PKCE flow for API documentation
- **CORS**: Configured to allow requests from configured portal URLs

## Running the Unified Server

To run the Research Service, start the unified Airavata server:

```bash
# From project root (no module path to specify)
./scripts/dev.sh serve

# Or from modules/distribution
cd modules/distribution && mvn exec:java -Dexec.args="serve"
```

The Research Service will be available at:
- HTTP: `http://localhost:8080/api/v1/research/...`
- gRPC: `localhost:9090` (via gRPC client)
- Swagger UI: `http://localhost:8080/swagger-ui.html` (when enabled)

See the main [README.md](../../../../README.md) for complete server startup instructions.
