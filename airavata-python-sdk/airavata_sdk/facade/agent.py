import importlib

from google.protobuf.struct_pb2 import Struct

from airavata_sdk.transport.utils import (
    create_agent_interaction_service_stub,
    create_plan_service_stub,
)


class AgentClient:
    """Agent interaction (tunnels, env setup, command/Jupyter/Python execution)
    and plan management."""

    def __init__(self, channel, metadata, gateway_id):
        self._metadata = metadata
        self._gateway_id = gateway_id
        self._agent = create_agent_interaction_service_stub(channel)
        self._plan = create_plan_service_stub(channel)

    @staticmethod
    def _svc(name):
        return importlib.import_module(f"airavata_sdk.generated.services.{name}")

    # ================================================================
    # Agent Interaction Service
    # ================================================================

    def get_agent_info(self, agent_id: str):
        from airavata_sdk.generated.services import agent_service_pb2 as pb2
        return self._agent.GetAgentInfo(
            pb2.GetAgentInfoRequest(agent_id=agent_id),
            metadata=self._metadata,
        )

    # --- Tunnel ---

    def setup_tunnel(self, agent_id: str, local_port: int, local_bind_host: str = ""):
        from airavata_sdk.generated.services import agent_service_pb2 as pb2
        return self._agent.SetupTunnel(
            pb2.AgentTunnelCreateRequest(agent_id=agent_id, local_port=local_port, local_bind_host=local_bind_host),
            metadata=self._metadata,
        )

    def get_tunnel_response(self, execution_id: str):
        from airavata_sdk.generated.services import agent_service_pb2 as pb2
        return self._agent.GetTunnelResponse(
            pb2.GetExecutionResponseRequest(execution_id=execution_id),
            metadata=self._metadata,
        )

    def terminate_tunnel(self, agent_id: str, tunnel_id: str):
        from airavata_sdk.generated.services import agent_service_pb2 as pb2
        return self._agent.TerminateTunnel(
            pb2.AgentTunnelTerminateRequest(agent_id=agent_id, tunnel_id=tunnel_id),
            metadata=self._metadata,
        )

    # --- Environment setup ---

    def setup_env(self, agent_id: str, env_name: str, libraries: list[str] = None, pip: list[str] = None):
        from airavata_sdk.generated.services import agent_service_pb2 as pb2
        return self._agent.SetupEnv(
            pb2.AgentEnvSetupRequest(
                agent_id=agent_id, env_name=env_name,
                libraries=libraries or [], pip=pip or [],
            ),
            metadata=self._metadata,
        )

    def get_env_setup_response(self, execution_id: str):
        from airavata_sdk.generated.services import agent_service_pb2 as pb2
        return self._agent.GetEnvSetupResponse(
            pb2.GetExecutionResponseRequest(execution_id=execution_id),
            metadata=self._metadata,
        )

    # --- Kernel restart ---

    def restart_kernel(self, agent_id: str, env_name: str):
        from airavata_sdk.generated.services import agent_service_pb2 as pb2
        return self._agent.RestartKernel(
            pb2.AgentKernelRestartRequest(agent_id=agent_id, env_name=env_name),
            metadata=self._metadata,
        )

    def get_kernel_restart_response(self, execution_id: str):
        from airavata_sdk.generated.services import agent_service_pb2 as pb2
        return self._agent.GetKernelRestartResponse(
            pb2.GetExecutionResponseRequest(execution_id=execution_id),
            metadata=self._metadata,
        )

    # --- Shell command execution ---

    def execute_command(self, agent_id: str, env_name: str, working_dir: str, arguments: list[str] = None):
        from airavata_sdk.generated.services import agent_service_pb2 as pb2
        return self._agent.ExecuteCommand(
            pb2.AgentCommandExecutionRequest(
                agent_id=agent_id, env_name=env_name,
                working_dir=working_dir, arguments=arguments or [],
            ),
            metadata=self._metadata,
        )

    def get_command_response(self, execution_id: str):
        from airavata_sdk.generated.services import agent_service_pb2 as pb2
        return self._agent.GetCommandResponse(
            pb2.GetExecutionResponseRequest(execution_id=execution_id),
            metadata=self._metadata,
        )

    # --- Async shell command execution ---

    def execute_async_command(self, agent_id: str, env_name: str, working_dir: str, arguments: list[str] = None):
        from airavata_sdk.generated.services import agent_service_pb2 as pb2
        return self._agent.ExecuteAsyncCommand(
            pb2.AgentAsyncCommandExecutionRequest(
                agent_id=agent_id, env_name=env_name,
                working_dir=working_dir, arguments=arguments or [],
            ),
            metadata=self._metadata,
        )

    def get_async_command_response(self, execution_id: str):
        from airavata_sdk.generated.services import agent_service_pb2 as pb2
        return self._agent.GetAsyncCommandResponse(
            pb2.GetExecutionResponseRequest(execution_id=execution_id),
            metadata=self._metadata,
        )

    def list_async_commands(self, agent_id: str):
        from airavata_sdk.generated.services import agent_service_pb2 as pb2
        return self._agent.ListAsyncCommands(
            pb2.AgentAsyncCommandListRequest(agent_id=agent_id),
            metadata=self._metadata,
        )

    def get_async_command_list_response(self, execution_id: str):
        from airavata_sdk.generated.services import agent_service_pb2 as pb2
        return self._agent.GetAsyncCommandListResponse(
            pb2.GetExecutionResponseRequest(execution_id=execution_id),
            metadata=self._metadata,
        )

    def terminate_async_command(self, agent_id: str, process_id: int):
        from airavata_sdk.generated.services import agent_service_pb2 as pb2
        return self._agent.TerminateAsyncCommand(
            pb2.AgentAsyncCommandTerminateRequest(agent_id=agent_id, process_id=process_id),
            metadata=self._metadata,
        )

    def get_async_command_terminate_response(self, execution_id: str):
        from airavata_sdk.generated.services import agent_service_pb2 as pb2
        return self._agent.GetAsyncCommandTerminateResponse(
            pb2.GetExecutionResponseRequest(execution_id=execution_id),
            metadata=self._metadata,
        )

    # --- Jupyter execution ---

    def execute_jupyter(self, agent_id: str, env_name: str, code: str):
        from airavata_sdk.generated.services import agent_service_pb2 as pb2
        return self._agent.ExecuteJupyter(
            pb2.AgentJupyterExecutionRequest(agent_id=agent_id, env_name=env_name, code=code),
            metadata=self._metadata,
        )

    def get_jupyter_response(self, execution_id: str):
        from airavata_sdk.generated.services import agent_service_pb2 as pb2
        return self._agent.GetJupyterResponse(
            pb2.GetExecutionResponseRequest(execution_id=execution_id),
            metadata=self._metadata,
        )

    # --- Python execution ---

    def execute_python(self, agent_id: str, env_name: str, working_dir: str, code: str):
        from airavata_sdk.generated.services import agent_service_pb2 as pb2
        return self._agent.ExecutePython(
            pb2.AgentPythonExecutionRequest(
                agent_id=agent_id, env_name=env_name,
                working_dir=working_dir, code=code,
            ),
            metadata=self._metadata,
        )

    def get_python_response(self, execution_id: str):
        from airavata_sdk.generated.services import agent_service_pb2 as pb2
        return self._agent.GetPythonResponse(
            pb2.GetExecutionResponseRequest(execution_id=execution_id),
            metadata=self._metadata,
        )

    # ================================================================
    # Plan Service
    # ================================================================

    def save_plan(self, id: str, data: dict):
        from airavata_sdk.generated.services import agent_service_pb2 as pb2
        struct = Struct()
        struct.update(data)
        return self._plan.SavePlan(
            pb2.SavePlanRequest(id=id, data=struct),
            metadata=self._metadata,
        )

    def get_plan(self, plan_id: str):
        from airavata_sdk.generated.services import agent_service_pb2 as pb2
        return self._plan.GetPlan(
            pb2.GetPlanRequest(plan_id=plan_id),
            metadata=self._metadata,
        )

    def get_plans_by_user(self):
        from airavata_sdk.generated.services import agent_service_pb2 as pb2
        return self._plan.GetPlansByUser(
            pb2.GetPlansByUserRequest(),
            metadata=self._metadata,
        )

    def update_plan(self, plan_id: str, data: dict):
        from airavata_sdk.generated.services import agent_service_pb2 as pb2
        struct = Struct()
        struct.update(data)
        return self._plan.UpdatePlan(
            pb2.UpdatePlanRequest(plan_id=plan_id, data=struct),
            metadata=self._metadata,
        )
