# Agent Service Module

The Agent Service module provides both gRPC and HTTP interfaces for agent management and operations.

## API Types

The Agent API exposes two complementary interfaces:

### gRPC Persistent Connectivity
- **Server**: gRPC Server (unified)
- **Port**: 9090 (default)
- **Configuration**: `airavata.services.grpc.server.port`
- **Protocol**: Bidirectional streaming gRPC for persistent connections

### HTTP Endpoints
- **Server**: HTTP Server (unified)
- **Port**: 8080 (default)
- **Configuration**: `airavata.services.http.server.port`
- **Base Path**: `/api/v1/agent`

## Overview

The Agent API provides communication interfaces between the Airavata server and remote agents. It supports both gRPC persistent connectivity (bidirectional streaming) and HTTP endpoints for different interaction patterns.

This is part of the external API layers in Airavata:
- **Thrift Server** (port 8930) - Thrift Endpoints for Airavata API functions
- **HTTP Server** (port 8080):
  - Airavata API - HTTP Endpoints for Airavata API functions
  - File API - HTTP Endpoints for file upload/download
  - **Agent API** (this module) - HTTP Endpoints for interactive job contexts
  - Research API - HTTP Endpoints for use by research hub
- **gRPC Server** (port 9090) - For airavata binaries to open persistent channels with airavata APIs
- **Dapr gRPC** (port 50001) - Sidecar for pub/sub, state, and workflow execution

**Note:** Agent API provides both HTTP endpoints (on HTTP Server, port 8080) and persistent channels (on gRPC Server, port 9090). The HTTP endpoints handle request/response operations, while the gRPC server enables airavata binaries to open persistent channels with airavata APIs for long-lived bidirectional streaming connections.

## gRPC Persistent Connectivity

The Agent API provides persistent channels via gRPC bidirectional streaming, defined in:
- `modules/agent-framework/proto/agent-communication.proto`

### Service: `AgentCommunicationService`

**Method**: `createMessageBus(stream AgentMessage) returns (stream ServerMessage)`

Bidirectional streaming for persistent agent-server communication. This runs on the gRPC Server (port 9090) and handles long-lived connections for real-time agent operations.

### Message Types

- **Agent Messages**: AgentPing, CreateAgentResponse, TerminateAgentResponse, EnvSetupResponse, CommandExecutionResponse, PythonExecutionResponse, JupyterExecutionResponse, KernelRestartResponse, TunnelCreationResponse, TunnelTerminationResponse, AsyncCommandExecutionResponse, AsyncCommandListResponse, AsyncCommandTerminateResponse
- **Server Messages**: ShutdownRequest, CreateAgentRequest, TerminateAgentRequest, EnvSetupRequest, CommandExecutionRequest, PythonExecutionRequest, JupyterExecutionRequest, KernelRestartRequest, TunnelCreationRequest, TunnelTerminationRequest, AsyncCommandExecutionRequest, AsyncCommandListRequest, AsyncCommandTerminateRequest

## HTTP API Endpoints

The Agent API provides HTTP endpoints for request/response operations. All endpoints are prefixed with `/api/v1/agent` and run on the HTTP Server (port 8080):

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/{agentId}` | GET | Get agent information |
| `/setup/tunnel` | POST | Create TCP tunnel |
| `/setup/tunnel/{executionId}` | GET | Get tunnel creation response |
| `/terminate/tunnel` | POST | Terminate TCP tunnel |
| `/setup/env` | POST | Setup environment (conda/pip) |
| `/setup/env/{executionId}` | GET | Get environment setup response |
| `/setup/restart` | POST | Restart Jupyter kernel |
| `/setup/restart/{executionId}` | GET | Get kernel restart response |
| `/execute/shell` | POST | Execute shell command |
| `/execute/shell/{executionId}` | GET | Get command execution response |
| `/execute/asyncshell` | POST | Execute async shell command |
| `/execute/asyncshell/{executionId}` | GET | Get async command execution response |
| `/list/asyncshell` | POST | List async commands |
| `/list/asyncshell/{executionId}` | GET | Get async command list response |
| `/terminate/asyncshell` | POST | Terminate async command |
| `/terminate/asyncshell/{executionId}` | GET | Get terminate response |
| `/execute/jupyter` | POST | Execute Jupyter notebook cell |
| `/execute/jupyter/{executionId}` | GET | Get Jupyter execution response |
| `/execute/python` | POST | Execute Python script |
| `/execute/python/{executionId}` | GET | Get Python execution response |

## Internal Services Used

The Agent Service uses the following internal components:

- **Agent Connection Handler** - Manages persistent gRPC streams with agents
- **Agent Management Handler** - Agent lifecycle management
- **Plan Handler** - Execution plan management
- **FuseFS Handler** - Filesystem operations

These are internal components within the Agent Service module, not separate services.

## Configuration Properties

| Property | Default | Description |
|----------|---------|-------------|
| `airavata.services.agent.enabled` | `false` | Enable/disable Agent Service |
| `airavata.services.grpc.server.port` | `9090` | Unified gRPC server port |
| `airavata.services.http.server.port` | `8080` | Unified HTTP server port |
| `airavata.services.agent.grpc.max-inbound-message-size` | `20971520` | Max gRPC message size (20MB) |
| `airavata.services.agent.spring.servlet.multipart.max-file-size` | `200MB` | Max HTTP upload size |
| `airavata.services.agent.spring.servlet.multipart.max-request-size` | `200MB` | Max HTTP request size |
| `airavata.services.agent.tunnelserver.host` | - | Remote tunnel server hostname |
| `airavata.services.agent.tunnelserver.port` | `17000` | Remote tunnel server port |
| `airavata.services.agent.tunnelserver.url` | - | Remote tunnel server API URL |
| `airavata.services.agent.tunnelserver.token` | - | Authentication token for tunnel server |

**Note**: The Agent Tunnel Server configuration properties point to a **remote server location**, not a service started by Airavata. These properties are passed to agents via gRPC messages.

## Implementation

### gRPC Handler
- `src/main/java/org/apache/airavata/agent/connection/service/handlers/AgentConnectionHandler.java`

### HTTP Controllers
- `src/main/java/org/apache/airavata/agent/connection/service/controllers/AgentController.java`
- `src/main/java/org/apache/airavata/agent/connection/service/controllers/ExperimentController.java`
- `src/main/java/org/apache/airavata/agent/connection/service/controllers/PlanController.java`

### Configuration
- `src/main/java/org/apache/airavata/agent/connection/service/config/AgentServiceConfiguration.java`

## Related Documentation

- Main project README: [../../../README.md](../../../README.md)
- Agent implementation: [../airavata-agent/README.md](../airavata-agent/README.md)
- gRPC Server config: [../../airavata-api/src/main/java/org/apache/airavata/config/GrpcServerConfig.java](../../airavata-api/src/main/java/org/apache/airavata/config/GrpcServerConfig.java)
