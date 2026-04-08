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
from airavata_sdk.transport.utils import create_agent_interaction_service_stub

logger = logging.getLogger(__name__)
logger.setLevel(logging.DEBUG)


class AgentInteractionClient:
    """Client for the Agent Interaction Service (gRPC).

    Provides methods for agent management, tunnel setup, environment setup,
    shell/async command execution, Jupyter/Python execution, and kernel restart.
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

        self._stub = create_agent_interaction_service_stub(self.channel)

    def close(self):
        self.channel.close()

    # --- Agent info ---

    def get_agent_info(self, agent_id: str):
        from airavata_sdk.generated.services import agent_service_pb2 as pb2
        return self._stub.GetAgentInfo(
            pb2.GetAgentInfoRequest(agent_id=agent_id),
            metadata=self._metadata,
        )

    # --- Tunnel ---

    def setup_tunnel(self, agent_id: str, local_port: int, local_bind_host: str = ""):
        from airavata_sdk.generated.services import agent_service_pb2 as pb2
        return self._stub.SetupTunnel(
            pb2.AgentTunnelCreateRequest(agent_id=agent_id, local_port=local_port, local_bind_host=local_bind_host),
            metadata=self._metadata,
        )

    def get_tunnel_response(self, execution_id: str):
        from airavata_sdk.generated.services import agent_service_pb2 as pb2
        return self._stub.GetTunnelResponse(
            pb2.GetExecutionResponseRequest(execution_id=execution_id),
            metadata=self._metadata,
        )

    def terminate_tunnel(self, agent_id: str, tunnel_id: str):
        from airavata_sdk.generated.services import agent_service_pb2 as pb2
        return self._stub.TerminateTunnel(
            pb2.AgentTunnelTerminateRequest(agent_id=agent_id, tunnel_id=tunnel_id),
            metadata=self._metadata,
        )

    # --- Environment setup ---

    def setup_env(self, agent_id: str, env_name: str, libraries: list[str] = None, pip: list[str] = None):
        from airavata_sdk.generated.services import agent_service_pb2 as pb2
        return self._stub.SetupEnv(
            pb2.AgentEnvSetupRequest(
                agent_id=agent_id, env_name=env_name,
                libraries=libraries or [], pip=pip or [],
            ),
            metadata=self._metadata,
        )

    def get_env_setup_response(self, execution_id: str):
        from airavata_sdk.generated.services import agent_service_pb2 as pb2
        return self._stub.GetEnvSetupResponse(
            pb2.GetExecutionResponseRequest(execution_id=execution_id),
            metadata=self._metadata,
        )

    # --- Kernel restart ---

    def restart_kernel(self, agent_id: str, env_name: str):
        from airavata_sdk.generated.services import agent_service_pb2 as pb2
        return self._stub.RestartKernel(
            pb2.AgentKernelRestartRequest(agent_id=agent_id, env_name=env_name),
            metadata=self._metadata,
        )

    def get_kernel_restart_response(self, execution_id: str):
        from airavata_sdk.generated.services import agent_service_pb2 as pb2
        return self._stub.GetKernelRestartResponse(
            pb2.GetExecutionResponseRequest(execution_id=execution_id),
            metadata=self._metadata,
        )

    # --- Shell command execution ---

    def execute_command(self, agent_id: str, env_name: str, working_dir: str, arguments: list[str] = None):
        from airavata_sdk.generated.services import agent_service_pb2 as pb2
        return self._stub.ExecuteCommand(
            pb2.AgentCommandExecutionRequest(
                agent_id=agent_id, env_name=env_name,
                working_dir=working_dir, arguments=arguments or [],
            ),
            metadata=self._metadata,
        )

    def get_command_response(self, execution_id: str):
        from airavata_sdk.generated.services import agent_service_pb2 as pb2
        return self._stub.GetCommandResponse(
            pb2.GetExecutionResponseRequest(execution_id=execution_id),
            metadata=self._metadata,
        )

    # --- Async shell command execution ---

    def execute_async_command(self, agent_id: str, env_name: str, working_dir: str, arguments: list[str] = None):
        from airavata_sdk.generated.services import agent_service_pb2 as pb2
        return self._stub.ExecuteAsyncCommand(
            pb2.AgentAsyncCommandExecutionRequest(
                agent_id=agent_id, env_name=env_name,
                working_dir=working_dir, arguments=arguments or [],
            ),
            metadata=self._metadata,
        )

    def get_async_command_response(self, execution_id: str):
        from airavata_sdk.generated.services import agent_service_pb2 as pb2
        return self._stub.GetAsyncCommandResponse(
            pb2.GetExecutionResponseRequest(execution_id=execution_id),
            metadata=self._metadata,
        )

    def list_async_commands(self, agent_id: str):
        from airavata_sdk.generated.services import agent_service_pb2 as pb2
        return self._stub.ListAsyncCommands(
            pb2.AgentAsyncCommandListRequest(agent_id=agent_id),
            metadata=self._metadata,
        )

    def get_async_command_list_response(self, execution_id: str):
        from airavata_sdk.generated.services import agent_service_pb2 as pb2
        return self._stub.GetAsyncCommandListResponse(
            pb2.GetExecutionResponseRequest(execution_id=execution_id),
            metadata=self._metadata,
        )

    def terminate_async_command(self, agent_id: str, process_id: int):
        from airavata_sdk.generated.services import agent_service_pb2 as pb2
        return self._stub.TerminateAsyncCommand(
            pb2.AgentAsyncCommandTerminateRequest(agent_id=agent_id, process_id=process_id),
            metadata=self._metadata,
        )

    def get_async_command_terminate_response(self, execution_id: str):
        from airavata_sdk.generated.services import agent_service_pb2 as pb2
        return self._stub.GetAsyncCommandTerminateResponse(
            pb2.GetExecutionResponseRequest(execution_id=execution_id),
            metadata=self._metadata,
        )

    # --- Jupyter execution ---

    def execute_jupyter(self, agent_id: str, env_name: str, code: str):
        from airavata_sdk.generated.services import agent_service_pb2 as pb2
        return self._stub.ExecuteJupyter(
            pb2.AgentJupyterExecutionRequest(agent_id=agent_id, env_name=env_name, code=code),
            metadata=self._metadata,
        )

    def get_jupyter_response(self, execution_id: str):
        from airavata_sdk.generated.services import agent_service_pb2 as pb2
        return self._stub.GetJupyterResponse(
            pb2.GetExecutionResponseRequest(execution_id=execution_id),
            metadata=self._metadata,
        )

    # --- Python execution ---

    def execute_python(self, agent_id: str, env_name: str, working_dir: str, code: str):
        from airavata_sdk.generated.services import agent_service_pb2 as pb2
        return self._stub.ExecutePython(
            pb2.AgentPythonExecutionRequest(
                agent_id=agent_id, env_name=env_name,
                working_dir=working_dir, code=code,
            ),
            metadata=self._metadata,
        )

    def get_python_response(self, execution_id: str):
        from airavata_sdk.generated.services import agent_service_pb2 as pb2
        return self._stub.GetPythonResponse(
            pb2.GetExecutionResponseRequest(execution_id=execution_id),
            metadata=self._metadata,
        )
