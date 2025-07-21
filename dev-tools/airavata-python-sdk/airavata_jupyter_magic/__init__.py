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

import base64
import binascii
import importlib.metadata
import json
import os
import time
from argparse import ArgumentParser
from dataclasses import dataclass
from enum import IntEnum
from pathlib import Path
from typing import Any, NamedTuple, Optional

import jwt
import random
import requests
import shlex
import sys
import tempfile
import yaml
from IPython.core.getipython import get_ipython
from IPython.core.interactiveshell import ExecutionResult
from IPython.core.magic import register_cell_magic, register_line_magic
from IPython.display import HTML, Image, Javascript, display
from rich.console import Console
from rich.live import Live

from jupyter_client.blocking.client import BlockingKernelClient

from airavata_auth.device_auth import AuthContext
from airavata_sdk import Settings

# ========================================================================
# DATA STRUCTURES


class InvalidStateError(BaseException):
    pass


class RequestedRuntime:
    cluster: str
    cpus: int
    memory: int | None
    walltime: int
    queue: str
    group: str
    file: str | None
    use: str | None


class ProcessState(IntEnum):
    CREATED = 0
    VALIDATED = 1
    LAUNCHED = 2
    STARTED = 3
    PRE_PROCESSING = 4
    CONFIGURING_WORKSPACE = 5
    INPUT_DATA_STAGING = 6
    EXECUTING = 7
    MONITORING = 8
    OUTPUT_DATA_STAGING = 9
    POST_PROCESSING = 10
    COMPLETED = 11
    FAILED = 12
    CANCELING = 13
    CANCELED = 14
    QUEUED = 15
    DEQUEUING = 16
    REQUEUED = 17


RuntimeInfo = NamedTuple('RuntimeInfo', [
    ('agentId', str),
    ('experimentId', str),
    ('processId', str),
    ('cluster', str),
    ('queue', str),
    ('cpus', int),
    ('memory', int | None),
    ('walltime', int),
    ('gateway_id', str),
    ('group', str),
    ('libraries', list[str]),
    ('pip', list[str]),
    ('envName', str),
    ('pids', list[int]),
    ('tunnels', dict[str, tuple[str, int]]),
])

PENDING_STATES = [
    ProcessState.CREATED,
    ProcessState.LAUNCHED,
    ProcessState.VALIDATED,
    ProcessState.STARTED,
    ProcessState.PRE_PROCESSING,
    ProcessState.CONFIGURING_WORKSPACE,
    ProcessState.INPUT_DATA_STAGING,
    ProcessState.QUEUED,
    ProcessState.REQUEUED,
]


TERMINAL_STATES = [
    ProcessState.DEQUEUING,
    ProcessState.CANCELING,
    ProcessState.COMPLETED,
    ProcessState.FAILED,
    ProcessState.CANCELED,
]


@dataclass
class State:
    tunnels: dict[str, dict[str, tuple[str, int]]]
    processes: dict[str, dict]
    current_runtime: str  # none => local
    all_runtimes: dict[str, RuntimeInfo]  # user-defined runtime dict
    kernel_clients: dict[str, BlockingKernelClient]  # runtime name -> Jupyter kernel client


# END OF DATA STRUCTURES
# ========================================================================
# HELPER FUNCTIONS


def get_access_token(envar_name: str = "CS_ACCESS_TOKEN", state_path: str = "/tmp/av.json") -> str | None:
    """
    Get access token from environment or file

    @param None:
    @returns: access token if present, None otherwise

    """
    token = os.getenv(envar_name)
    if not token:
        try:
            token = json.load(Path(state_path).open("r")).get("access_token")
        except (FileNotFoundError, json.JSONDecodeError):
            pass
    return token


def is_runtime_ready(access_token: str, rt: RuntimeInfo, rt_name: str):
    """
    Check if the runtime (i.e., agent job) is ready to receive requests

    @param access_token: the access token
    @param rt: information about the runtime
    @param rt_name: the runtime name
    @returns: True if ready, False otherwise

    """
    settings = Settings()

    # first, check the experiment state
    headers = generate_headers(access_token, rt.gateway_id)
    exstate, reason = get_experiment_state(rt.experimentId, headers)
    if exstate in PENDING_STATES:
        return False, f"EXPERIMENT_{exstate.name}"
    if exstate in TERMINAL_STATES:
        msg = f"Runtime={rt_name} is in state=EXPERIMENT_{exstate.name}\n{reason}"
        raise InvalidStateError(msg)

    # second, check the state of each processes
    pid, pstate = get_process_state(rt.experimentId, headers)
    if pstate in PENDING_STATES:
        return False, f"PROCESS_{pstate.name}"
    if pstate in TERMINAL_STATES:
        msg = f"Runtime={rt_name} is in state={pstate.name}"
        raise InvalidStateError(msg)

    # third, check the state of agent
    url = f"{settings.API_SERVER_URL}/api/v1/agent/{rt.agentId}"
    res = requests.get(url)
    code = res.status_code
    astate = "CREATING_WORKSPACE"
    if code == 202:
        data: dict = res.json()
        if data.get("agentUp", False):
            astate = "CONNECTED"
    else:
        print(f"[{code}] Runtime status check failed: {res.text}")
    return astate == "CONNECTED", astate


def execute_shell_async(access_token: str, rt_name: str, arguments: list[str]) -> int | None:
    """
    Execute a shell command asynchronously (Added 2025-05-06)

    @param access_token: the access token
    @param rt_name: the runtime name
    """
    rt = state.all_runtimes.get(rt_name, None)
    if rt is None:
        raise Exception(f"Runtime {rt_name} not found.")
    
    settings = Settings()
    url = f"{settings.API_SERVER_URL}/api/v1/agent/execute/asyncshell"
    headers = generate_headers(access_token, rt_name)
    res = requests.post(url, headers=headers, data=json.dumps({
        "agentId": rt.agentId,
        "envName": rt.envName,
        "workingDir": ".",
        "arguments": arguments,
    }))
    code = res.status_code
    if code != 202:
        return print(f"[{code}] Failed to execute async shell command: {res.text}")

    executionId = res.json()["executionId"]
    if not executionId:
        return print(f"Failed to restart kernel runtime={rt.agentId}")

    # Check if the request was successful
    while True:
        url = f"{settings.API_SERVER_URL}/api/v1/agent/execute/asyncshell/{executionId}"
        res = requests.get(url, headers={'Accept': 'application/json'})
        data = res.json()

        processId = data.get('processId', -1)
        errorMessage = data.get('errorMessage', "").strip()
        if errorMessage not in ["", "Not Ready"]:
            return print(f"Error running async shell on env={rt.envName}, runtime={rt_name}: {errorMessage}")
        elif processId != -1:
            rt.pids.append(int(processId))
            return processId
        time.sleep(1)


def get_hostname(access_token: str, rt_name: str) -> str | None:
    """
    Get the hostname of the runtime (Added 2025-05-06)

    @param access_token: the access token
    @param rt_name: the runtime name
    """
    rt = state.all_runtimes.get(rt_name, None)
    if rt is None:
        raise Exception(f"Runtime {rt_name} not found.")

    settings = Settings()
    url = f"{settings.API_SERVER_URL}/api/v1/agent/execute/shell"
    headers = generate_headers(access_token, rt_name)
    res = requests.post(url, headers=headers, data=json.dumps({
        "agentId": rt.agentId,
        "envName": rt.envName,
        "workingDir": ".",
        "arguments": ["hostname"],
    }))
    code = res.status_code
    if code != 202:
        print(f"[{code}] Failed to get hostname: {res.text}")
    executionId = res.json()["executionId"]
    if not executionId:
        return print(f"Failed to get hostname for runtime={rt_name}")

    while True:
        url = f"{settings.API_SERVER_URL}/api/v1/agent/execute/shell/{executionId}"
        res = requests.get(url, headers={'Accept': 'application/json'})
        data = res.json()
        if data.get('executed'):
            responseString = str(data.get('responseString'))
            return responseString.strip()
        time.sleep(1)


def open_tunnel(access_token: str, rt_name: str, rt_hostname: str, rt_port: int) -> str | None:
    """
    Setup a tunnel to the runtime (Added 2025-05-06)

    @param access_token: the access token
    @param rt_name: the runtime name
    @param rt_hostname: the hostname of the runtime
    @param rt_port: the port of the runtime
    """
    rt = state.all_runtimes.get(rt_name, None)
    if rt is None:
        raise Exception(f"Runtime {rt_name} not found.")

    settings = Settings()
    url = f"{settings.API_SERVER_URL}/api/v1/agent/setup/tunnel"
    headers = generate_headers(access_token, rt_name)
    res = requests.post(url, headers=headers, data=json.dumps({
        "agentId": rt.agentId,
        "localBindHost": rt_hostname,
        "localPort": rt_port,
    }))
    code = res.status_code
    if code != 202:
        print(f"[{code}] Failed to setup tunnel: {res.text}")

    executionId = res.json()["executionId"]
    if not executionId:
        return print(f"Failed to setup tunnel for runtime={rt_name}")

    while True:
        url = f"{settings.API_SERVER_URL}/api/v1/agent/setup/tunnel/{executionId}"
        res = requests.get(url, headers={'Accept': 'application/json'})
        data = res.json()
        if data.get('status') == "OK":
            tunnelId = data.get('tunnelId')
            proxyPort = data.get('poxyPort')
            proxyHost = data.get('proxyHost')
            rt.tunnels[tunnelId] = (proxyHost, proxyPort)
            return tunnelId
        time.sleep(1)


def terminate_tunnel(access_token: str, rt_name: str, tunnel_id: str) -> None:
    """
    Terminate a tunnel
    """
    rt = state.all_runtimes.get(rt_name, None)
    if rt is None:
        raise Exception(f"Runtime {rt_name} not found.")

    # TODO: send actual API call to terminate tunnel
    assert access_token is not None

    # cleanup state after termination
    rt.tunnels.pop(tunnel_id)


def terminate_shell_async(access_token: str, rt_name: str, process_id: str, proc_tunnels: list[str]) -> None:
    """
    Terminate a shell command asynchronously (Added 2025-05-06)

    @param access_token: the access token
    @param rt_name: the runtime name
    @param process_id: the process id
    """
    rt = state.all_runtimes.get(rt_name, None)
    if rt is None:
        raise Exception(f"Runtime {rt_name} not found.")

    settings = Settings()
    url = f"{settings.API_SERVER_URL}/api/v1/agent/terminate/asyncshell"
    headers = generate_headers(access_token, rt_name)
    res = requests.post(url, headers=headers, data=json.dumps({
        "agentId": rt.agentId,
        "processId": process_id,
    }))
    code = res.status_code
    if code != 202:
        print(f"[{code}] Failed to terminate shell: {res.text}")

    executionId = res.json()["executionId"]
    if not executionId:
        return print(f"Failed to terminate shell for runtime={rt_name}, process_id={process_id}")

    for tunnel_id in proc_tunnels:
        terminate_tunnel(access_token, rt_name, tunnel_id)
        print(f"terminated {rt_name}:{tunnel_id}")


def get_experiment_state(experiment_id: str, headers: dict) -> tuple[ProcessState, str]:
    """
    Get experiment state by experiment id

    @param experiment_id: the experiment id
    @param headers: the headers
    @returns: the experiment state

    """
    settings = Settings()
    url = f"{settings.API_SERVER_URL}/api/v1/exp/{experiment_id}"
    res = requests.get(url, headers=headers)
    code = res.status_code
    if code != 200:
        msg = f"Failed to get experiment state: {res.text}"
        print(msg)
        raise InvalidStateError(msg)
    data: dict = res.json()
    status: dict | None = data.get("experimentStatus", [None])[-1]
    if status:
        return ProcessState[status["state"]], status["reason"] or "N/A"
    else:
        return ProcessState.CREATED, "N/A"


def get_process_state(experiment_id: str, headers: dict) -> tuple[str, ProcessState]:
    """
    Get process state by experiment id

    @param experiment_id: the experiment id
    @param headers: the headers
    @returns: process id and state

    """
    settings = Settings()
    url = f"{settings.API_SERVER_URL}/api/v1/exp/{experiment_id}/process"
    pid, pstate = "", ProcessState.CREATED
    while not pid:
        res = requests.get(url, headers=headers)
        code = res.status_code
        if code == 200:
            data: dict = res.json()
            pid = data.get("processId")
            procs = data.get("processStatuses")
            if procs and len(procs):
                tMax, wait, busy, done = 0, 0, 0, 0
                for proc in procs:
                    t = int(proc.get("timeOfStateChange"))
                    s = ProcessState[proc.get("state")]
                    if t >= tMax:
                      tMax, busy, done = t, 0, 0
                    if t == tMax:
                        wait += s in PENDING_STATES
                        done += s in TERMINAL_STATES
                        busy += s not in [*PENDING_STATES, *TERMINAL_STATES]
                if busy == 0 and done == 0:
                    pstate = ProcessState.CONFIGURING_WORKSPACE
                elif wait == 0 and busy == 0:
                    pstate = ProcessState.COMPLETED
                else:
                    pstate = ProcessState.EXECUTING
        else:
            time.sleep(5)
    return pid, pstate


def generate_headers(access_token: str, gateway_id: str) -> dict:
    """
    Generate headers for the request

    @param access_token: the access token
    @param gateway_id: the gateway id
    @returns: the headers

    """
    decode = jwt.decode(access_token, options={"verify_signature": False})
    user_id = decode['preferred_username']
    claimsMap = {
        "userName": user_id,
        "gatewayID": gateway_id
    }
    return {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer ' + access_token,
        'X-Claims': json.dumps(claimsMap)
    }


def submit_agent_job(
    rt_name: str,
    access_token: str,
    app_name: str,
    gateway_id: str,
    walltime: int,
    cluster: str,
    queue: str,
    group: str,
    cpus: int | None = None,
    memory: int | None = None,
    gpus: int | None = None,
    gpu_memory: int | None = None,
    file: str | None = None,
) -> None:
    """
    Submit an agent job to the given runtime

    @param rt_name: the runtime name (string)
    @param access_token: the access token (string)
    @param app_name: the application name (string)
    @param gateway_id: the gateway id (string)
    @param walltime: the walltime (minutes)
    @param cluster: the cluster (string)
    @param queue: the queue (string)
    @param group: the group (string)
    @param cpus: the number of cpus (int)
    @param memory: the memory for cpu (MB)
    @param gpus: the number of gpus (int)
    @param gpu_memory: the memory for gpu (MB)
    @param file: environment file (path)
    @returns: None

    """
    # URL to which the POST request will be sent
    settings = Settings()
    url = f"{settings.API_SERVER_URL}/api/v1/exp/launch"

    # data from file
    min_cpu: int = 1
    min_mem: int = 2048
    min_gpu: int = 0
    gpu_mem: int = 0
    mounts: list[str] = []
    modules: list[str] = []
    conda: list[str] = []
    pip: list[str] = []

    # if file is provided, validate it and use the given values as defaults
    if file is not None:
        fp = Path(file)
        # validation
        assert fp.exists(), f"File {file} does not exist"
        with open(fp, "r") as f:
            content = yaml.safe_load(f)
        # validation: /workspace
        assert (workspace := content.get("workspace", None)) is not None, "missing section: /workspace"
        assert (resources := workspace.get("resources", None)) is not None, "missing section: /workspace/resources"
        assert (min_cpu := resources.get("min_cpu", None)) is not None, "missing section: /workspace/resources/min_cpu"
        assert (min_mem := resources.get("min_mem", None)) is not None, "missing section: /workspace/resources/min_mem"
        assert (min_gpu := resources.get("min_gpu", None)) is not None, "missing section: /workspace/resources/min_gpu"
        assert (gpu_mem := resources.get("gpu_mem", None)) is not None, "missing section: /workspace/resources/gpu_mem"
        assert (models := workspace.get("model_collection", None)) is not None, "missing section: /workspace/model_collection"
        assert (datasets := workspace.get("data_collection", None)) is not None, "missing section: /workspace/data_collection"
        collection = models + datasets
        # validation: /additional_dependencies
        assert (additional_dependencies := content.get("additional_dependencies", None)) is not None, "missing section: /additional_dependencies"
        assert (modules := additional_dependencies.get("modules", None)) is not None, "missing /additional_dependencies/modules section"
        assert (conda := additional_dependencies.get("conda", None)) is not None, "missing /additional_dependencies/conda section"
        assert (pip := additional_dependencies.get("pip", None)) is not None, "missing /additional_dependencies/pip section"
        mounts = [f"{i['identifier']}:{i['mount_point']}" for i in collection]

    # payload
    data = {
        'experimentName': app_name,
        'nodeCount': 1,
        'cpuCount': cpus or min_cpu,
        'gpuCount': gpus or min_gpu,
        'memory': memory or min_mem,
        'gpu_memory': gpu_memory or gpu_mem,
        'wallTime': walltime,
        'remoteCluster': cluster,
        'group': group,
        'queue': queue,
        'modules': modules,
        'libraries': conda,
        'pip': pip,
        'mounts': mounts,
    }

    # print the data
    print(f"Requesting runtime={rt_name}...", flush=True)
    print(f"[{data['remoteCluster']}:{data['queue']}, {data['wallTime']} Minutes, {data['nodeCount']} Node(s), {data['cpuCount']} CPU(s), {data['gpuCount']} GPU(s), {data['memory']} MB RAM, {data['gpu_memory']} MB VRAM]", flush=True)
    print(f"* modules={data['modules']}", flush=True)
    print(f"* libraries={data['libraries']}", flush=True)
    print(f"* pip={data['pip']}", flush=True)
    print(f"* mounts={data['mounts']}", flush=True)

    # Send the POST request
    headers = generate_headers(access_token, gateway_id)
    res = requests.post(url, headers=headers, data=json.dumps(data))
    code = res.status_code

    # Check if the request was successful
    if code == 200:
        obj = res.json()
        pid, pstate = get_process_state(obj['experimentId'], headers=headers)
        if pstate in TERMINAL_STATES:
            msg = f"Runtime={rt_name} is in state={pstate.name}"
            print(msg)
            raise InvalidStateError(msg)
        rt = RuntimeInfo(
            gateway_id=gateway_id,
            processId=pid,
            agentId=obj['agentId'],
            experimentId=obj['experimentId'],
            cluster=data['remoteCluster'],
            queue=data['queue'],
            cpus=data['cpuCount'],
            memory=data['memory'],
            walltime=data['wallTime'],
            group=data['group'],
            libraries=data['libraries'],
            pip=data['pip'],
            envName=obj['envName'],
            pids=[],
            tunnels={},
        )
        state.all_runtimes[rt_name] = rt
        print(f'Requested runtime={rt_name}', flush=True)
    else:
        print(f'[{code}] Failed to request runtime={rt_name}. error={res.text}', flush=True)

def fetch_logs(rt_name: str) -> tuple[str, str]:
    """
    Fetch stdout and stderr for the runtime.

    @param rt_name: the runtime name
    @returns: (stdout, stderr)

    """
    pid = state.all_runtimes[rt_name].processId
    settings = Settings()
    stdout_res = requests.get(f"{settings.FILE_SVC_URL}/download/live/{pid}/AiravataAgent.stdout")
    stderr_res = requests.get(f"{settings.FILE_SVC_URL}/download/live/{pid}/AiravataAgent.stderr")
    stdout = "No STDOUT" if stdout_res.status_code != 200 else stdout_res.content.decode('utf-8').strip()
    stderr = "No STDERR" if stderr_res.status_code != 200 else stderr_res.content.decode('utf-8').strip()
    return stdout, stderr

def wait_until_runtime_ready(access_token: str, rt_name: str, render_live_logs: bool = False):
    """
    Block execution until the runtime is ready.

    @param access_token: the access token
    @param rt_name: the runtime name
    @returns: None when ready

    """
    rt = state.all_runtimes.get(rt_name, None)
    if rt is None:
        return print(f"Runtime {rt_name} not found.")
    if rt_name == "local":
        return
    console = Console()

    try:
      if render_live_logs:
        def render(title_text, stdout_text, stderr_text):
            stdout_text = "\n".join(stdout_text.split("\n")[-10:])
            stderr_text = "\n".join(stderr_text.split("\n")[-10:])
            text = (
                f"{title_text}\n\n"
                f"====[STDOUT]====\n[black]{stdout_text}[/black]\n\n"
                f"====[STDERR]====\n[red]{stderr_text}[/red]"
            )
            return text

        with Live(render(f"Connecting to={rt_name}...", "No STDOUT", "No STDERR"), refresh_per_second=1, console=console) as live:
            while True:
              ready, rstate = is_runtime_ready(access_token, rt, rt_name)
              stdout, stderr = fetch_logs(rt_name)
              if ready:
                  live.update(render(f"Connecting to={rt_name}... status=CONNECTED", stdout, stderr), refresh=True)
                  break
              else:
                  live.update(render(f"Connecting to={rt_name}... status={rstate}", stdout, stderr), refresh=True)
              time.sleep(5)
      else:
        with console.status(f"Connecting to={rt_name}...") as status:
            while True:
                ready, rstate = is_runtime_ready(access_token, rt, rt_name)
                if ready:
                    status.update(f"Connecting to={rt_name}... status=CONNECTED")
                    break
                else:
                    status.update(f"Connecting to={rt_name}... status={rstate}")
                    time.sleep(5)
            status.stop()
    except InvalidStateError as e:
        stdout, stderr = fetch_logs(rt_name)
        error_message = f"{str(e)}\n\nSTDOUT:\n{stdout}\n\nSTDERR:\n{stderr}"
        e.args = (error_message,) + e.args[1:] if len(e.args) > 0 else (error_message,)
        raise e


def restart_runtime_kernel(access_token: str, rt_name: str, env_name: str, runtime: RuntimeInfo):
    """
    Restart the kernel runtime on the given runtime.

    @param access_token: the access token
    @param env_name: the environment name
    @param runtime: the runtime info
    @returns: None

    """
    settings = Settings()
    url = f"{settings.API_SERVER_URL}/api/v1/agent/setup/restart"

    decode = jwt.decode(access_token, options={"verify_signature": False})
    user_id = decode['preferred_username']
    claimsMap = {
        "userName": user_id,
        "gatewayID": runtime.gateway_id
    }

    # Headers
    headers = {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer ' + access_token,
        'X-Claims': json.dumps(claimsMap)
    }

    # Send the POST request
    res = requests.post(url, headers=headers, data=json.dumps({
        "agentId": runtime.agentId,
        "envName": runtime.envName,
    }))
    data = res.json()

    executionId = data.get("executionId")
    if not executionId:
        print(f"Failed to restart kernel runtime={runtime.agentId}")
        return

    # Check if the request was successful
    while True:
        url = f"{settings.API_SERVER_URL}/api/v1/agent/setup/restart/{executionId}"
        res = requests.get(url, headers={'Accept': 'application/json'})
        data = res.json()
        if data.get('restarted'):
            print(f"Restarted kernel={env_name} on runtime={rt_name}")
            break
        time.sleep(1)


def stop_agent_job(access_token: str, runtime_name: str, runtime: RuntimeInfo):
    """
    Stop the agent job on the given runtime.

    @param access_token: the access token
    @param runtime_name: the runtime name
    @param runtime: the runtime info
    @returns: None

    """
    settings = Settings()
    url = f"{settings.API_SERVER_URL}/api/v1/exp/terminate/{runtime.experimentId}"

    decode = jwt.decode(access_token, options={"verify_signature": False})
    user_id = decode['preferred_username']
    claimsMap = {
        "userName": user_id,
        "gatewayID": runtime.gateway_id
    }

    # Headers
    headers = {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer ' + access_token,
        'X-Claims': json.dumps(claimsMap)
    }

    # Send the POST request
    res = requests.get(url, headers=headers)
    status = res.status_code

    # Check if the request was successful
    if status == 200:
        data = res.json()
        print(f"Terminated runtime={runtime_name}. state={data}")
        state.all_runtimes.pop(runtime_name, None)
    else:
        print(
            f'[{status}] Failed to terminate runtime={runtime_name}: error={res.text}')


def run_on_runtime(rt_name: str, code_obj: str, result: ExecutionResult) -> bool:
    rt = state.all_runtimes.get(rt_name, None)
    if rt is None:
        result.error_in_exec = Exception(f"Runtime {rt_name} not found.")
        return False

    settings = Settings()
    url = f"{settings.API_SERVER_URL}/api/v1/agent/execute/jupyter"
    data = {
        "agentId": rt.agentId,
        "envName": rt.envName,
        "code": code_obj,
    }
    json_data = json.dumps(data)
    response = requests.post(
        url, headers={'Content-Type': 'application/json'}, data=json_data)
    execution_resp = response.json()

    execution_id = execution_resp.get("executionId")
    if not execution_id:
        result.error_in_exec = Exception("Failed to start cell execution")
        return False

    error = execution_resp.get("error")
    if error:
        result.error_in_exec = Exception(
            "Cell execution failed. Error: " + error)
        return False

    while True:
        url = f"{settings.API_SERVER_URL}/api/v1/agent/execute/jupyter/{execution_id}"
        response = requests.get(url, headers={'Accept': 'application/json'})
        json_response = response.json()
        if json_response.get('executed'):
            break
        time.sleep(1)

    exec_result_str = json_response.get('responseString')
    try:
        exec_result = json.loads(exec_result_str)
    except json.JSONDecodeError as e:
        result.error_in_exec = Exception(
            f"Failed to decode response from runtime={rt_name}: {e.msg}")
        return False

    if 'outputs' in exec_result:
        for output in exec_result['outputs']:
            output_type = output.get('output_type')
            if output_type == 'display_data':
                data_obj = output.get('data', {})
                if 'image/png' in data_obj:
                    image_data = data_obj['image/png']
                    try:
                        image_bytes = base64.b64decode(image_data)
                        display(Image(data=image_bytes, format='png'))
                    except binascii.Error as e:
                        result.error_in_exec = Exception(
                            f"Failed to decode image data: {e}")
                        return False
                elif 'text/html' in data_obj:
                    html_data = data_obj['text/html']
                    display(HTML(html_data))
                elif 'application/javascript' in data_obj:
                    js_data = data_obj['application/javascript']
                    display(Javascript(js_data))

            elif output_type == 'stream':
                stream_name = output.get('name', 'stdout')
                stream_text = output.get('text', '').strip()
                if stream_name == 'stderr':
                    error_html = f"""
                    <div style="
                        color: #a71d5d;
                        background-color: #fdd;
                        border: 1px solid #a71d5d;
                        padding: 5px;
                        border-radius: 5px;
                        font-family: Consolas, 'Courier New', monospace;
                        white-space: pre-wrap;
                    ">
                        {stream_text}
                    </div>
                    """
                    display(HTML(error_html))
                    result.error_in_exec = Exception(stream_text)
                    #return False This prevents rest of the message not getting rendered
                else:
                    print(stream_text)

            elif output_type == 'error':
                ename = output.get('ename', 'Error')
                evalue = output.get('evalue', '')
                traceback = output.get('traceback', [])
                error_html = f"""
                <div style="
                    color: #a71d5d;
                    background-color: #fdd;
                    border: 1px solid #a71d5d;
                    padding: 5px;
                    border-radius: 5px;
                    font-family: Consolas, 'Courier New', monospace;
                ">
                    <pre><strong>{ename}: {evalue}</strong>
                """
                for line in traceback:
                    error_html += f"{line}\n"
                error_html += "</pre></div>"
                display(HTML(error_html))
                result.error_in_exec = Exception(f"{ename}: {evalue}")
                return False

            elif output_type == 'execute_result':
                data_obj = output.get('data', {})
                if 'text/plain' in data_obj:
                    print(data_obj['text/plain'])

                if 'text/html' in data_obj:
                    html_data = data_obj['text/html']
                    display(HTML(html_data))

                if 'application/javascript' in data_obj:
                    js_data = data_obj['application/javascript']
                    display(Javascript(js_data))

                if 'application/vnd.jupyter.widget-view+json' in data_obj:
                    widget_data = data_obj
                    display(widget_data, raw=True)
    else:
        if 'result' in exec_result:
            print(exec_result['result'])
        elif 'error' in exec_result:
            print(exec_result['error']['ename'])
            print(exec_result['error']['evalue'])
            print(exec_result['error']['traceback'])
        elif 'display' in exec_result:
            data_obj = exec_result['display'].get('data', {})
            if 'image/png' in data_obj:
                image_data = data_obj['image/png']
                try:
                    image_bytes = base64.b64decode(image_data)
                    display(Image(data=image_bytes, format='png'))
                except binascii.Error as e:
                    result.error_in_exec = Exception(
                        f"Failed to decode image data: {e}")
                    return False

        else:
            # Mark as failed execution if no recognized output format is found
            error_html = """
          <div style="
            color: #a71d5d;
            background-color: #fdd;
            border: 1px solid #a71d5d;
            padding: 5px;
            border-radius: 5px;
            font-family: Consolas, 'Courier New', monospace;
          ">
            <strong>Error:</strong> Execution failed with unrecognized output format from remote runtime.
            <pre>{}</pre>
          </div>
          """.format(exec_result)
            display(HTML(error_html))
            result.error_in_exec = Exception(
                "Execution failed with unrecognized output format from remote runtime.")
            return False
    return True


def push_remote(local_path: str, remot_rt: str, remot_path: str) -> None:
    """
    Push a local file to a remote runtime

    @param local_path: the local file path
    @param remot_rt: the remote runtime name
    @param remot_path: the remote file path
    @returns: None

    """
    if not state.all_runtimes.get(remot_rt, None):
        return print(MSG_NOT_INITIALIZED)
    # validate paths
    if not remot_path or not local_path:
        return print("Please provide paths for both source and target")
    if remot_path.endswith("/"):
        remot_path = remot_path + os.path.basename(local_path)
    # upload file
    print(f"local:{local_path} --> {remot_rt}:{remot_path}...", end=" ", flush=True)
    pid = state.all_runtimes[remot_rt].processId
    settings = Settings()
    url = f"{settings.FILE_SVC_URL}/upload/live/{pid}/{remot_path}"
    with open(local_path, "rb") as file:
        files = {"file": file}
        response = requests.post(url, files=files)
    print(f"[{response.status_code}]", flush=True)

def pull_remote_file(remot_rt: str, remot_fp: str, local_fp: str) -> None:
    pid = state.all_runtimes[remot_rt].processId
    settings = Settings()
    url = f"{settings.FILE_SVC_URL}/download/live/{pid}/{remot_fp}"
    print(f"GET {url}")
    response = requests.get(url)
    with open(local_fp, "wb") as file:
        file.write(response.content)
    print(f"local:{local_fp} <-- {remot_rt}:{remot_fp}...", end=" ", flush=True)

def pull_remote(remot_rt: str, remot_path: str, local_path: Path, local_is_dir: bool) -> None:
    """
    Pull a remote file to a local runtime

    @param local_path: the local file path
    @param remot_rt: the remote runtime name
    @param remot_path: the remote file path
    @returns: None

    """
    if not state.all_runtimes.get(remot_rt, None):
        return print(MSG_NOT_INITIALIZED)
    pid = state.all_runtimes[remot_rt].processId
    settings = Settings()
    url = f"{settings.FILE_SVC_URL}/list/live/{pid}/{remot_path}"
    print(f"GET {url}")
    response = requests.get(url)
    res = response.json()
    if "fileName" in res:
        # remot_path is a file
        if local_is_dir:
            local_fp = local_path / remot_path
        else:
            local_fp = local_path
        os.makedirs(local_fp.parent, exist_ok=True)
        pull_remote_file(remot_rt, remot_path, local_fp.as_posix())
    elif "directoryName" in res:
        # remot_path is a directory
        assert local_is_dir, f"Cannot pull directory {remot_path} to file {local_path}"
        os.makedirs(local_path, exist_ok=True)
        for file in [d["fileName"] for d in res["innerFiles"]]:
            local_fp = local_path / str(file)
            pull_remote_file(remot_rt, os.path.join(remot_path, file), local_fp.as_posix())

        for file in [d["directoryName"] for d in res["innerDirectories"]]:
            local_dp = local_path / str(file)
            if os.path.isfile(local_dp):
                raise RuntimeError(f"Cannot pull directory {remot_path} to file {local_path}")
            os.makedirs(local_dp, exist_ok=True)
            pull_remote(remot_rt, os.path.join(remot_path, file), local_dp, local_is_dir=True)


def run_subprocess_inner(access_token: str, rt_name: str, proc_name: str, command: str, forwarded_ports: list[int], hostname: str | None = None):

    if not hostname:
      hostname = get_hostname(access_token, rt_name)
      if not hostname:
          return print(f"failed to get hostname for runtime={rt_name}")

    process_id = execute_shell_async(access_token, rt_name, command.split())
    if process_id is None:
        return print(f"failed to start process {proc_name} on {rt_name}")
    else:
        print(f"started proc_name={proc_name} on rt={rt_name}. pid={process_id}")

    tunnels = {}
    print(f"forwarding ports={forwarded_ports}")
    for port in forwarded_ports:
        tunnel_id = open_tunnel(access_token, rt_name, hostname, port)
        if tunnel_id is None:
            return print(f"runtime={rt_name} failed to tunnel port={port}")
        tunnels[tunnel_id] = (proxy_host, proxy_port) = state.all_runtimes[rt_name].tunnels[tunnel_id]
        print(f"{rt_name}:{port} -> access via {proxy_host}:{proxy_port}")
    state.tunnels[proc_name] = tunnels

    state.processes[proc_name] = {
        "rt_name": rt_name,
        "pid": process_id,
        "tunnels": tunnels
    }


# END OF HELPER FUNCTIONS
# ========================================================================
# MAGIC FUNCTIONS


@register_cell_magic
def run_on(line: str, cell: str):
    """
    Run the cell on the given runtime

    """
    assert ipython is not None
    cell_runtime = line.strip()
    orig_runtime = state.current_runtime
    try:
        if cell_runtime in ["local", *state.all_runtimes]:
            state.current_runtime = cell_runtime
            return ipython.run_cell(cell)
        else:
            msg = f"Runtime {cell_runtime} not found."
            print(msg)
            raise InvalidStateError(msg)
    finally:
        state.current_runtime = orig_runtime


@register_line_magic
def switch_runtime(line: str):
    """
    Switch the active runtime

    """
    cell_runtime = line.strip()
    try:
        if cell_runtime not in ["local", *state.all_runtimes]:
            msg = f"Runtime {cell_runtime} not found."
            print(msg)
            raise RuntimeError(msg)
    except RuntimeError as e:
        msg = f"Could not switch to runtime={cell_runtime}. error={e}"
        print(msg)
        raise RuntimeError(msg)
    else:
        state.current_runtime = cell_runtime
        print(f"Switched to runtime={cell_runtime}.")


@register_line_magic
def authenticate(line: str):
    """
    Authenticate to access high-performance runtimes

    """
    try:
        context = AuthContext()
        context.login()
    except ValueError as e:
        msg = f"Configuration error: {e}"
        print(msg)
        raise RuntimeError(msg)


def meta_scheduler(use_list: list[str]) -> tuple[str, str]:
    """
    Schedule a runtime based on the use list.
    Currently, it picks at random. (Added 2025-05-06)

    @param use_list: list of runtimes to schedule
    @returns: tuple of cluster and queue

    """
    # TODO: replace with actual scheduler
    idx = random.randint(0, len(use_list) - 1)

    cluster, queue = use_list[idx].split(":", maxsplit=1)
    return cluster, queue


@register_line_magic
def request_runtime(line: str):
    """
    Request a runtime with given capabilities

    """
    access_token = get_access_token()
    assert access_token is not None

    [rt_name, *cmd_args] = line.strip().split()

    # validate runtime name
    if rt_name == "local":
        return print(f"Runtime={rt_name} already exists!")

    rt = state.all_runtimes.get(rt_name, None)
    if rt is not None:
        ready, rstate = is_runtime_ready(access_token, rt, rt_name)
        if ready:
            return print(f"Runtime={rt_name} already exists!")
        else:
            print(f"Runtime={rt_name} is still preparing... {rstate}")
        headers = generate_headers(access_token, rt.gateway_id)
        _, pstate = get_process_state(rt.experimentId, headers)
        if pstate in PENDING_STATES:
            return print(f"Runtime={rt_name} is in state={pstate.name}. Please wait, or run '%stop_runtime {rt_name}' to stop it.")
        if pstate in TERMINAL_STATES:
            print(f"Runtime={rt_name} is in state={pstate.name}. Cleaning up.")
            state.all_runtimes.pop(rt_name, None)

    # parse cli args
    p = ArgumentParser(
        prog="request_runtime",
        description="Request a runtime with given capabilities",
    )
    p.add_argument("--cluster", type=str, help="cluster", required=False)
    p.add_argument("--cpus", type=int, help="CPU cores", required=False)
    p.add_argument("--memory", type=int, help="memory (MB)", required=False)
    p.add_argument("--walltime", type=int, help="time (mins)", required=True)
    p.add_argument("--queue", type=str, help="resource queue", required=False)
    p.add_argument("--group", type=str, help="resource group", required=False, default="Default")
    p.add_argument("--file", type=str, help="yml file", required=False)
    p.add_argument("--use", type=str, help="allowed resources", required=False)

    args = p.parse_args(cmd_args, namespace=RequestedRuntime())

    if args.file is not None:
        assert args.use is not None
        cluster, queue  = meta_scheduler(args.use.split(","))
        submit_agent_job(
            rt_name=rt_name,
            access_token=access_token,
            app_name='CS_Agent',
            gateway_id='default',
            walltime=args.walltime,
            cluster=cluster,
            queue=queue,
            group=args.group,
            file=args.file,
        )
    else:
        assert args.cluster is not None
        assert args.queue is not None
        assert args.group is not None
        assert args.cpus is not None
        submit_agent_job(
            rt_name=rt_name,
            access_token=access_token,
            app_name='CS_Agent',
            gateway_id='default',
            walltime=args.walltime,
            cluster=args.cluster,
            queue=args.queue,
            group=args.group,
            cpus=args.cpus,
            memory=args.memory,
        )
    print(f"Request successful: runtime={rt_name}", flush=True)


@register_line_magic
def stat_runtime(line: str):
    """
    Show the status of the runtime

    """
    access_token = get_access_token()
    assert access_token is not None

    rt_name = line.strip()

    if rt_name in ["local", None]:
        return print("Runtime=local is always available")

    rt = state.all_runtimes.get(rt_name, None)
    if rt is None:
        return print(f"Runtime {rt_name} not found.")

    ready, rstate = is_runtime_ready(access_token, rt, rt_name)
    if ready:
        print(f"Runtime={rt_name} is ready!")
    else:
        print(f"Runtime={rt_name} is still preparing... {rstate}")


@register_line_magic
def wait_for_runtime(line: str):
    """
    Wait for the runtime to be ready
    """
    parts = line.strip().split()
    if len(parts) == 1:
        rt_name, render_live_logs = parts[0], False
    elif len(parts) == 2:
        assert parts[1] == "--live", "Usage: %wait_for_runtime <rt> [--live]"
        rt_name, render_live_logs = parts[0], True
    else:
        raise ValueError("Usage: %wait_for_runtime <rt> [--live]")
    access_token = get_access_token()
    assert access_token is not None

    rt = state.all_runtimes.get(rt_name, None)
    if rt is None:
        return print(f"Runtime {rt_name} not found.")
    wait_until_runtime_ready(access_token, rt_name, render_live_logs)
    # Validation: launch remote kernel if not already started
    if rt_name != "local" and rt_name not in state.kernel_clients:
        random_port = random.randint(2000, 6000) * 5
        launch_remote_kernel(rt_name, random_port, hostname="127.0.0.1")
        print(f"Remote Jupyter kernel launched and connected for runtime={rt_name}.")
    return


@register_line_magic
def run_subprocess(line: str):
    """
    Run a subprocess asynchronously

    """
    access_token = get_access_token()
    assert access_token is not None

    rt_name = state.current_runtime
    rt = state.all_runtimes.get(rt_name, None)
    if rt is None:
        return print(f"Runtime {rt_name} not found.")

    proc_name, argstring = line.strip().split(" ", maxsplit=1)

    parser = ArgumentParser(prog="run_async")
    parser.add_argument("--command", type=str, help="bash command to execute", required=True)
    parser.add_argument("--ports", type=str, help="comma-separated list of ports to forward", required=False)
    parser.add_argument("--hostname", type=str, help="hostname to serve on. otherwise serve on $(hostname)", required=False, default="127.0.0.1")
    args = parser.parse_args(shlex.split(argstring))

    command = str(args.command)
    if not command:
        return print("Usage: %run_async <proc_name> --command=<command> --forward=<ports>")

    print(f"executing command='{command}' on {rt_name}. proc_name={proc_name}")
    forwarded_ports = [] if not args.ports else [int(port.strip()) for port in str(args.ports).split(",")]

    run_subprocess_inner(access_token, rt_name, proc_name, command, forwarded_ports, hostname=args.hostname)


@register_line_magic
def kill_subprocess(line: str):
    """
    Kill a running subprocess asynchronously

    """
    access_token = get_access_token()
    assert access_token is not None

    rt_name = state.current_runtime
    rt = state.all_runtimes.get(rt_name, None)
    if rt is None:
        return print(f"Runtime {rt_name} not found.")

    proc_name = line.strip()
    proc = state.processes.get(proc_name, None)
    if proc is None:
        return print(f"Process {proc_name} not found.")

    proc_rt_name = proc["rt_name"]
    proc_pid = proc["pid"]
    proc_tunnels = proc["tunnels"]

    terminate_shell_async(access_token, proc_rt_name, proc_pid, proc_tunnels)
    print(f"terminated {proc_rt_name}:{proc_name}. pid={proc_pid}")

    state.processes.pop(proc_name)


@register_line_magic
def open_tunnels(line: str):
    """
    Open tunnels to the runtime

    """
    access_token = get_access_token()
    assert access_token is not None

    rt_name = state.current_runtime
    rt = state.all_runtimes.get(rt_name, None)
    if rt is None:
        return print(f"Runtime {rt_name} not found.")

    tunnel_name, argstring = line.strip().split(" ", maxsplit=1)
    parser = ArgumentParser(prog="open_tunnel")
    parser.add_argument("--ports", type=str, help="comma-separated list of ports to forward", required=False)
    args = parser.parse_args(shlex.split(argstring))

    ports = str(args.ports)
    if not ports:
        return print("Usage: %open_tunnel <tn> --ports=<ports>")

    forwarded_ports = [int(port.strip()) for port in ports.split(",")]

    hostname = get_hostname(access_token, rt_name)
    if not hostname:
        return print(f"failed to get hostname for runtime={rt_name}")

    tunnels = {}
    for port in forwarded_ports:
        tunnel_id = open_tunnel(access_token, rt_name, hostname, port)
        if tunnel_id is None:
            return print(f"runtime={rt_name} failed to tunnel port={port}")
        (proxy_host, proxy_port) = state.all_runtimes[rt_name].tunnels[tunnel_id]
        print(f"{rt_name}:{port} -> access via {proxy_host}:{proxy_port}")
        tunnels[tunnel_id] = (proxy_host, proxy_port)
    state.tunnels[tunnel_name] = tunnels


@register_line_magic
def close_tunnels(line: str):
    """
    Close tunnels to the runtime

    """
    access_token = get_access_token()
    assert access_token is not None

    rt_name = state.current_runtime
    rt = state.all_runtimes.get(rt_name, None)
    if rt is None:
        return print(f"Runtime {rt_name} not found.")

    tunnel_name = line.strip()
    tunnels = state.tunnels.get(tunnel_name, None)
    if tunnels is None:
        return print(f"Tunnel {tunnel_name} not found.")

    for tunnel_id in tunnels:
        terminate_tunnel(access_token, rt_name, tunnel_id)
        print(f"terminated {rt_name}:{tunnel_id}")

    state.tunnels.pop(tunnel_name)


@register_line_magic
def restart_runtime(rt_name: str):
    """
    Restart the runtime

    """
    access_token = get_access_token()
    assert access_token is not None

    rt = state.all_runtimes.get(rt_name, None)
    if rt is None:
        return print(f"Runtime {rt_name} not found.")
    return restart_runtime_kernel(access_token, rt_name, "base", rt)


@register_line_magic
def stop_runtime(rt_name: str):
    """
    Stop the runtime

    """
    access_token = get_access_token()
    assert access_token is not None

    rt = state.all_runtimes.get(rt_name, None)
    if rt is None:
        return print(f"Runtime {rt_name} not found.")
    return stop_agent_job(access_token, rt_name, rt)


@register_line_magic
def copy_data(line: str):
    """
    Copy data between runtimes

    """
    parts = line.strip().split()
    args: dict[str, str] = {}
    for part in parts:
        if "=" in part:
            k, v = part.split("=", 1)
            args[k] = v
    source: str = args.get("source", "")
    target: str = args.get("target", "")
    if not source or not target:
        return print("Usage: %copy_data source=<runtime>:<path> target=<runtime>:<path>")

    source_runtime, source_path = source.split(":")
    target_runtime, target_path = target.split(":")
    print(f"copying {source_runtime}:{source_path} to {target_runtime}:{target_path}")

    if source_runtime == "local":
        # ensure source_path is given in right form
        assert os.path.exists(source_path), f"Source path {source_path} does not exist"
        source_isdir = os.path.isdir(source_path)
        # upload all files, preserving structure
        if source_isdir:
            # Recursively upload all files, preserving structure
            for root, dirs, files in os.walk(source_path):
                for file in files:
                    local_path = Path(root) / file
                    remote_path = Path(target_path) / local_path.relative_to(source_path)
                    push_remote(
                        local_path=local_path.as_posix(),
                        remot_rt=target_runtime,
                        remot_path=remote_path.as_posix(),
                    )
        else:
            push_remote(
                local_path=source_path,
                remot_rt=target_runtime,
                remot_path=target_path,
            )
    elif target_runtime == "local":
        pull_remote(
            remot_rt=source_runtime,
            remot_path=source_path.lstrip("./ "),
            local_path=Path.cwd() / target_path.lstrip("./ "),
            local_is_dir=target_path in ["", "."] or target_path.endswith("/"),
        )
    else:
        print("remote-to-remote copy is not supported yet")


def launch_remote_kernel(rt_name: str, base_port: int, hostname: str):
    """
    Launch a remote Jupyter kernel, open tunnels, and connect a local Jupyter client.
    """
    assert ipython is not None
    access_token = get_access_token()
    assert access_token is not None

    # launch kernel and tunnel ports
    stdin, shell, iopub, hb, control = base_port, base_port+1, base_port+2, base_port+3, base_port+4

    proc_name = f"{rt_name}_kernel"
    temp_fp = Path(tempfile.mktemp(prefix="connection_", suffix=".json"))
    key = base64.b64encode(random.randbytes(32)).decode("utf-8")[:16]
    remote_connection_info = {
        "stdin_port": stdin,
        "shell_port": shell,
        "iopub_port": iopub,
        "hb_port": hb,
        "control_port": control,
        "ip": "0.0.0.0",
        "key": key,
        "signature_scheme": "hmac-sha256",
        "transport": "tcp",
        "kernel_name": proc_name,
    }
    with open(temp_fp, "w") as f:
        json.dump(remote_connection_info, f)
    push_remote(
        local_path=temp_fp.as_posix(),
        remot_rt=rt_name,
        remot_path=temp_fp.name,
    )

    cmd = f"python -m ipykernel_launcher -f {temp_fp.name}"
    run_subprocess_inner(access_token, rt_name, proc_name, cmd, [stdin, shell, iopub, hb, control], hostname=hostname)
    tunnels = state.tunnels[proc_name]

    # assert all ports are tunneled over the same host
    tunnel_host = list(tunnels.values())[0][0]
    assert all(v[0] == tunnel_host for v in tunnels.values()), "All ports must be tunneled over the same host"
    print(f"started ipykernel tunnels for {rt_name} at {tunnel_host}")

    # find which ports to connect to
    kernel_ports = [v[1] for v in tunnels.values()]
    [tstdin, tshell, tiopub, thb, tcontrol] = kernel_ports

    local_connection_info = {
        "stdin_port": tstdin,
        "shell_port": tshell,
        "iopub_port": tiopub,
        "hb_port":thb,
        "control_port": tcontrol,
        "ip": tunnel_host,
        "key": key,
        "signature_scheme": "hmac-sha256",
        "transport": "tcp",
        "kernel_name": proc_name,
    }
    client = BlockingKernelClient()
    client.load_connection_info(local_connection_info)
    client.start_channels()

    print(f"started ipykernel client for {rt_name}")
    state.kernel_clients[rt_name] = client

@register_line_magic
def open_web_terminal(line: str):
    """
    Open a web terminal to the runtime
    """
    parts = line.strip().split()
    rt_name = parts[0].strip()

    # Generate a random port for ttyd
    import random
    random_port = random.randint(10000, 65000)

    # Start ttyd subprocess on the runtime
    proc_name = f"{rt_name}_ttyd"
    cmd = f"ttyd -p {random_port} -i 0.0.0.0 --writable bash"

    # Get access token
    access_token = get_access_token()
    if access_token is None:
        print("Not authenticated. Please run %authenticate first.")
        return

    # Start the subprocess
    run_subprocess_inner(access_token, rt_name, proc_name, cmd, [random_port], hostname="127.0.0.1")
    print(f"Started web terminal on runtime {rt_name} at port {random_port}")

    tunnels = state.tunnels[proc_name]
    assert len(tunnels) == 1
    proxyhost, proxyport = list(tunnels.values())[0]
    url = f"http://{proxyhost}:{proxyport}"

    display(HTML(f'Open the web terminal from <a href="{url}" target="_blank">here</a>.'))



# END OF MAGIC FUNCTIONS
# ========================================================================
# AUTORUN


ipython = get_ipython()
if ipython is None:
    raise RuntimeError("airavata_jupyter_magic requires an ipython session")
assert ipython is not None

MSG_NOT_INITIALIZED = r"Runtime not found. Please run %request_runtime name=<name> cluster=<cluster> cpu=<cpu> memory=<memory mb> queue=<queue> walltime=<walltime minutes> group=<group> to request one."

state = State(current_runtime="local", all_runtimes={}, processes={}, tunnels={}, kernel_clients={})
orig_run_code = ipython.run_cell_async


def cell_has_magic(raw_cell: str) -> bool:
    lines = raw_cell.strip().splitlines()
    magics = (r"%authenticate", r"%request_runtime", r"%restart_runtime", r"%stop_runtime", r"%wait_for_runtime", r"%switch_runtime", r"%%run_on", r"%stat_runtime", r"%copy_data", r"%run_subprocess", r"%kill_subprocess", r"%open_tunnels", r"%close_tunnels", r"%open_web_terminal")
    return any(line.strip().startswith(magics) for line in lines)


def handle_shell_message(msg: dict, result: ExecutionResult):
    msg_type = msg['msg_type']
    content = msg['content']
    if msg_type == 'execute_reply':
        if content.get('status') == 'error':
            traceback = '\n'.join(content.get('traceback', []))
            print(traceback)
            result.error_in_exec = Exception(content.get('evalue', 'Error'))
            return 'error'
        # Optionally handle payload, user_expressions, etc.
    # Add more shell message types as needed
    return None

def handle_stdin_message(msg: dict, result: ExecutionResult, client: BlockingKernelClient):
    msg_type = msg['msg_type']
    content = msg['content']
    if msg_type == 'input_request':
        prompt = content.get('prompt', '')
        password = content.get('password', False)
        try:
            if password:
                import getpass
                value = getpass.getpass(prompt)
            else:
                value = input(prompt)
        except Exception as e:
            value = ''
        client.input(value)
    return None

def handle_control_message(msg: dict, result: ExecutionResult):
    msg_type = msg['msg_type']
    content = msg['content']
    print(f"[control] {msg_type}: {content}")
    # Add more control message handling as needed
    return None

def send_interrupt_request(client: BlockingKernelClient):
    # Send an interrupt_request message on the control channel
    msg = client.session.msg('interrupt_request', {})
    client.control_channel.send(msg)

def handle_iopub_message(msg: dict, result: ExecutionResult):
    msg_type = msg['msg_type']
    content = msg['content']

    if msg_type == 'status':
        if content['execution_state'] == 'idle':
            return 'idle'

    elif msg_type == 'stream':
        stream = content.get('name')
        text = content.get('text', '')
        if stream == 'stdout':
            print(text, end='')
        elif stream == 'stderr':
            print(text, end='', file=sys.stderr)

    elif msg_type == 'error':
        traceback = '\n'.join(content.get('traceback', []))
        print(traceback)
        result.error_in_exec = Exception(content.get('evalue', 'Error'))
        return 'error'

    elif msg_type == 'clear_output':
        from IPython.display import clear_output
        clear_output(wait=content.get('wait', False))

    elif msg_type in ('display_data', 'execute_result'):
        from IPython.display import publish_display_data
        data = content.get('data', {})
        metadata = content.get('metadata', {})
        transient = content.get('transient', {})
        publish_display_data(data, metadata, transient=transient)
        
    return None

async def run_cell_async(
    raw_cell: str,
    store_history=False,
    silent=False,
    shell_futures=True,
    *,
    transformed_cell: Optional[str] = None,
    preprocessing_exc_tuple: Optional[Any] = None,
    cell_id=None,
) -> ExecutionResult:
    rt = state.current_runtime
    if rt == "local" or cell_has_magic(raw_cell):
        return await orig_run_code(raw_cell, store_history, silent, shell_futures, transformed_cell=transformed_cell, preprocessing_exc_tuple=preprocessing_exc_tuple, cell_id=cell_id)
    else:
        # Validation: check runtime is ready and kernel is started
        access_token = get_access_token()
        assert access_token is not None
        rt_info = state.all_runtimes.get(rt, None)
        if rt_info is None:
            result = ExecutionResult(info=None)
            result.error_in_exec = Exception(f"Runtime {rt} not found.")
            return result
        ready, rstate = is_runtime_ready(access_token, rt_info, rt)
        if not ready:
            wait_until_runtime_ready(access_token, rt, render_live_logs=False)
            ready, rstate = is_runtime_ready(access_token, rt_info, rt)
            if not ready:
                result = ExecutionResult(info=None)
                result.error_in_exec = Exception(f"Runtime {rt} is not ready: {rstate}")
                return result
        if rt not in state.kernel_clients:
            random_port = random.randint(2000, 6000) * 5
            launch_remote_kernel(rt, random_port, hostname="127.0.0.1")

        # Use Jupyter kernel client for remote runtime
        result = ExecutionResult(info=None)
        client = state.kernel_clients.get(rt)
        if client is None:
            result.error_in_exec = Exception(f"No Jupyter kernel client found for runtime {rt}. Did you call launch_remote_kernel?")
            return result
        try:
            print(f"executing cell on {rt}...")
            msg_id = client.execute(raw_cell)
            print(f"waiting for cell to finish on {rt}...")
            assert ipython is not None
            done = False
            try:
                while not done:
                    got_msg = False
                    # IOPub
                    if client.iopub_channel.msg_ready():
                        msg = client.get_iopub_msg()
                        if msg['parent_header'].get('msg_id') == msg_id:
                            status = handle_iopub_message(msg, result)
                            got_msg = True
                            if status in ['idle', 'error']:
                                done = True
                    # Shell
                    if client.shell_channel.msg_ready():
                        msg = client.get_shell_msg()
                        if msg['parent_header'].get('msg_id') == msg_id:
                            status = handle_shell_message(msg, result)
                            got_msg = True
                            if status == 'error':
                                done = True
                    # Stdin
                    if client.stdin_channel.msg_ready():
                        msg = client.get_stdin_msg()
                        if msg['parent_header'].get('msg_id') == msg_id:
                            handle_stdin_message(msg, result, client)
                            got_msg = True
                    # Control
                    if client.control_channel.msg_ready():
                        msg = client.get_control_msg()
                        if msg['parent_header'].get('msg_id') == msg_id:
                            handle_control_message(msg, result)
                            got_msg = True
                    if not got_msg:
                        time.sleep(0.05)
            except KeyboardInterrupt:
                print("Interrupt sent to remote kernel, waiting for response...")
                send_interrupt_request(client)
                # Continue polling until remote kernel signals completion
                while not done:
                    got_msg = False
                    if client.iopub_channel.msg_ready():
                        msg = client.get_iopub_msg()
                        if msg['parent_header'].get('msg_id') == msg_id:
                            status = handle_iopub_message(msg, result)
                            got_msg = True
                            if status in ['idle', 'error']:
                                done = True
                    if client.shell_channel.msg_ready():
                        msg = client.get_shell_msg()
                        if msg['parent_header'].get('msg_id') == msg_id:
                            status = handle_shell_message(msg, result)
                            got_msg = True
                            if status == 'error':
                                done = True
                    if client.stdin_channel.msg_ready():
                        msg = client.get_stdin_msg()
                        if msg['parent_header'].get('msg_id') == msg_id:
                            handle_stdin_message(msg, result, client)
                            got_msg = True
                    if client.control_channel.msg_ready():
                        msg = client.get_control_msg()
                        if msg['parent_header'].get('msg_id') == msg_id:
                            handle_control_message(msg, result)
                            got_msg = True
                    if not got_msg:
                        time.sleep(0.05)
            print(f"cell finished on {rt}.")
        except Exception as e:
            result.error_in_exec = e
        if store_history and ipython:
            ipython.execution_count += 1
        return result

ipython.run_cell_async = run_cell_async


version = importlib.metadata.version("airavata-python-sdk")
print(rf"""
Loaded airavata_jupyter_magic ({version}) 
(current runtime = local)

  %authenticate                              -- Authenticate to access high-performance runtimes.
  %request_runtime <rt> [args]               -- Request a runtime named <rt> with configuration <args>.
                                                Call multiple times to request multiple runtimes.
  %restart_runtime <rt>                      -- Restart runtime <rt> if it hangs. This will clear all variables.
  %stop_runtime <rt>                         -- Stop runtime <rt> when no longer needed.
  %wait_for_runtime <rt>                     -- Wait for runtime <rt> to be ready.
  %switch_runtime <rt>                       -- Switch the active runtime to <rt>. All subsequent cells will run here.
  %%run_on <rt>                              -- Force a cell to always execute on <rt>, regardless of the active runtime.
  %stat_runtime <rt>                         -- Show the status of runtime <rt>.
  %copy_data source=<r1:f1> target=<r2:f2>   -- Copy <f1> in <r1> to <f2> in <r2>.
  %open_tunnels <tn> --forward=<ports>       -- Open a TCP tunnel on the runtime.
  %close_tunnels <tn>                        -- Close a TCP tunnel opened on the runtime.
  %run_subprocess <pn> --command=<cmd>
                       --forward=<ports>     -- Start a subprocess on the runtime.
  %kill_subprocess <pn>                      -- Kill a subprocess started on the runtime.
""")

# END OF AUTORUN
# ========================================================================
