import asyncio
import base64
import binascii
from functools import partial
import json
import os
import time
from argparse import ArgumentParser
from dataclasses import dataclass
from enum import IntEnum
from pathlib import Path
from types import CodeType
from typing import Any, NamedTuple, Optional

import jwt
import requests
import yaml
from IPython.core.getipython import get_ipython
from IPython.core.interactiveshell import ExecutionResult
from IPython.core.magic import register_cell_magic, register_line_magic
from IPython.display import HTML, Image, display
from rich.console import Console

from .device_auth import DeviceFlowAuthenticator

# ========================================================================
# DATA STRUCTURES


class InvalidStateError(Exception):
    pass


class RequestedRuntime:
    cluster: str
    cpus: int
    memory: int | None
    walltime: int
    queue: str
    group: str
    file: str | None


class ProcessState(IntEnum):
    CREATED = 0
    VALIDATED = 1
    STARTED = 2
    PRE_PROCESSING = 3
    CONFIGURING_WORKSPACE = 4
    INPUT_DATA_STAGING = 5
    EXECUTING = 6
    MONITORING = 7
    OUTPUT_DATA_STAGING = 8
    POST_PROCESSING = 9
    COMPLETED = 10
    FAILED = 11
    CANCELLING = 12
    CANCELED = 13
    QUEUED = 14
    DEQUEUING = 15
    REQUEUED = 16


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
])


PENDING_STATES = [
    ProcessState.CREATED,
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
    ProcessState.CANCELLING,
    ProcessState.COMPLETED,
    ProcessState.FAILED,
    ProcessState.CANCELED,
]


@dataclass
class State:
    current_runtime: str  # none => local
    all_runtimes: dict[str, RuntimeInfo]  # user-defined runtime dict


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
        msg = f"Runtime={rt_name} is in state=PROCESS_{pstate.name}"
        raise InvalidStateError(msg)

    # third, check the state of agent
    url = f"{api_base_url}/api/v1/agent/{rt.agentId}"
    res = requests.get(url)
    code = res.status_code
    astate = "AGENT_CREATED"
    if code == 202:
        data: dict = res.json()
        if data.get("agentUp", False):
            astate = "READY"
    else:
        print(f"[{code}] Runtime status check failed: {res.text}")
    return astate == "READY", astate


def get_experiment_state(experiment_id: str, headers: dict) -> tuple[ProcessState, str]:
    """
    Get experiment state by experiment id

    @param experiment_id: the experiment id
    @param headers: the headers
    @returns: the experiment state

    """
    url = f"{api_base_url}/api/v1/exp/{experiment_id}"
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
    url = f"{api_base_url}/api/v1/exp/{experiment_id}/process"
    pid, pstate = "", ProcessState.CREATED
    while not pid:
        res = requests.get(url, headers=headers)
        code = res.status_code
        if code == 200:
            data: dict = res.json()
            pid = data.get("processId")
            procs = data.get("processStatuses")
            if procs and len(procs):
                for proc in procs:
                    ps = ProcessState[proc.get("state")]
                    if ps > pstate:
                        pstate = ps
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
    cluster: str | None = None,
    cpus: int | None = None,
    memory: int | None = None,
    walltime: int | None = None,
    queue: str | None = None,
    group: str | None = None,
    gateway_id: str = 'default',
    file: str | None = None,
) -> None:
    """
    Submit an agent job to the given runtime

    @param rt_name: the runtime name
    @param access_token: the access token
    @param app_name: the application name
    @param cluster: the cluster
    @param cpus: the number of cpus
    @param memory: the memory
    @param walltime: the walltime
    @param queue: the queue
    @param group: the group
    @param gateway_id: the gateway id
    @param file: environment file
    @returns: None

    """
    # URL to which the POST request will be sent
    url = api_base_url + '/api/v1/exp/launch'

    # Data to be sent in the POST request
    if file is not None:
        fp = Path(file)
        assert fp.exists(), f"File {file} does not exist"
        with open(fp, "r") as f:
            content = yaml.safe_load(f)
        # workspace
        resources = content["workspace"]["resources"]
        models = content["workspace"]["model_collection"]
        datasets = content["workspace"]["data_collection"]
        collection = models + datasets
        mounts = [f"{i['identifier']}:{i['mount_point']}" for i in collection]
        # dependencies
        condas = content["additional_dependencies"]["conda"]
        pips = content["additional_dependencies"]["pip"]
        data = {
            'experimentName': app_name,
            'nodeCount': 1,
            'cpuCount': resources["min_cpu"],
            'memory': resources["min_mem"],
            'wallTime': resources["walltime"],
            'remoteCluster': resources["cluster"],
            'group': resources["group"],
            'queue': resources["queue"],
            'libraries': condas,
            'pip': pips,
            'mounts': mounts,
        }
    else:
        data = {
            'experimentName': app_name,
            'nodeCount': 1,
            'cpuCount': cpus,
            'memory': memory,
            'wallTime': walltime,
            'remoteCluster': cluster,
            'group': group,
            'queue': queue,
            'libraries': [],
            'pip': [],
            'mounts': [],
        }

    print(f"Requesting runtime={rt_name}", flush=True)
    print(yaml.dump(data, indent=2), flush=True)
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
        )
        state.all_runtimes[rt_name] = rt
        print(f'Requested runtime={rt_name}. state={pstate.name}')
    else:
        print(f'[{code}] Failed to request runtime={rt_name}. error={res.text}')


def wait_until_runtime_ready(access_token: str, rt_name: str):
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
    with console.status(f"Connecting to={rt_name}...") as status:
        while True:
            ready, rstate = is_runtime_ready(access_token, rt, rt_name)
            if ready:
                status.update(f"Connecting to={rt_name}... status=READY")
                break
            else:
                status.update(f"Connecting to={rt_name}... status={rstate}")
                time.sleep(5)
        status.stop()
    console.clear()


def restart_runtime_kernel(access_token: str, rt_name: str, env_name: str, runtime: RuntimeInfo):
    """
    Restart the kernel runtime on the given runtime.

    @param access_token: the access token
    @param env_name: the environment name
    @param runtime: the runtime info
    @returns: None

    """

    url = api_base_url + '/api/v1/agent/setup/restart'

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
        "envName": env_name,
    }))
    data = res.json()

    executionId = data.get("executionId")
    if not executionId:
        print(f"Failed to restart kernel runtime={runtime.agentId}")
        return

    # Check if the request was successful
    while True:
        url = api_base_url + "/api/v1/agent/setup/restart/" + executionId
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

    url = api_base_url + '/api/v1/exp/terminate/' + runtime.experimentId

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

    url = api_base_url + '/api/v1/agent/execute/jupyter'
    data = {
        "agentId": rt.agentId,
        "envName": "base",
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
        url = api_base_url + "/api/v1/agent/execute/jupyter/" + execution_id
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
                    return False
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
    # upload file
    print(f"Pushing local:{local_path} to remote:{remot_path}")
    url = f"{file_server_url}/upload/live/{state.all_runtimes[state.current_runtime].processId}/{remot_path}"
    with open(local_path, "rb") as file:
        files = {"file": file}
        response = requests.post(url, files=files)
    print(
        f"[{response.status_code}] Uploaded local:{local_path} to remote:{remot_path}")


def pull_remote(local_path: str, remot_rt: str, remot_path: str) -> None:
    """
    Pull a remote file to a local runtime

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
    # download file
    print(f"Pulling remote:{remot_path} to local:{local_path}")
    url = f"{file_server_url}/download/live/{state.all_runtimes[state.current_runtime].processId}/{remot_path}"
    response = requests.get(url)
    with open(local_path, "wb") as file:
        file.write(response.content)
    print(
        f"[{response.status_code}] Downloaded remote:{remot_path} to local:{local_path}")


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
        authenticator = DeviceFlowAuthenticator()
        authenticator.login()
    except ValueError as e:
        msg = f"Configuration error: {e}"
        print(msg)
        raise RuntimeError(msg)


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
    p.add_argument("--walltime", type=int, help="time (mins)", required=False)
    p.add_argument("--queue", type=str, help="resource queue", required=False)
    p.add_argument("--group", type=str, help="resource group", required=False)
    p.add_argument("--file", type=str, help="yml file", required=False)
    args = p.parse_args(cmd_args, namespace=RequestedRuntime())

    if args.file is not None:
        return submit_agent_job(
            rt_name=rt_name,
            access_token=access_token,
            app_name='CS_Agent',
            file=args.file,
        )
    else:
        assert args.cluster is not None
        assert args.cpus is not None
        assert args.walltime is not None
        assert args.queue is not None
        assert args.group is not None
        return submit_agent_job(
            rt_name=rt_name,
            access_token=access_token,
            app_name='CS_Agent',
            cluster=args.cluster,
            cpus=args.cpus,
            memory=args.memory,
            walltime=args.walltime,
            queue=args.queue,
            group=args.group,
        )


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
    args = {}
    for part in parts:
        if "=" in part:
            k, v = part.split("=", 1)
            args[k] = v
    source = args.get("source")
    target = args.get("target")
    if not source or not target:
        return print("Usage: %copy_data source=<runtime>:<path> target=<runtime>:<path>")

    source_runtime, source_path = source.split(":")
    target_runtime, target_path = target.split(":")
    print(
        f"Copying from {source_runtime}:{source_path} to {target_runtime}:{target_path}")

    if source_runtime == "local":
        push_remote(source_path, target_runtime, target_path)
    elif target_runtime == "local":
        pull_remote(target_path, source_runtime, source_path)
    else:
        print("remote-to-remote copy is not supported yet")


# END OF MAGIC FUNCTIONS
# ========================================================================
# AUTORUN


ipython = get_ipython()
if ipython is None:
    raise RuntimeError("airavata_jupyter_magic requires an ipython session")
assert ipython is not None
api_base_url = "https://api.gateway.cybershuttle.org"
file_server_url = "http://3.142.234.94:8050"
MSG_NOT_INITIALIZED = r"Runtime not found. Please run %request_runtime name=<name> cluster=<cluster> cpu=<cpu> memory=<memory mb> queue=<queue> walltime=<walltime minutes> group=<group> to request one."

state = State(current_runtime="local", all_runtimes={})
orig_run_code = ipython.run_cell_async


def cell_has_magic(raw_cell: str) -> bool:
    lines = raw_cell.strip().splitlines()
    magics = (r"%switch_runtime", r"%%run_on", r"%authenticate",
              r"%request_runtime", r"%stop_runtime", r"%stat_runtime", r"%copy_data")
    return any(line.strip().startswith(magics) for line in lines)


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
        access_token = get_access_token()
        assert access_token is not None
        result = ExecutionResult(info=None)
        try:
            wait_until_runtime_ready(access_token, rt)
            run_on_runtime(rt, raw_cell, result)
            return result
        except Exception as e:
            result.error_in_exec = e
            return result

ipython.run_cell_async = run_cell_async

print(r"""
Loaded airavata_jupyter_magic
(current runtime = local)

  %authenticate                      -- Authenticate to access high-performance runtimes.
  %request_runtime <rt> [args]       -- Request a runtime named <rt> with configuration <args>. Call multiple times to request multiple runtimes.
  %restart_runtime <rt>              -- Restart runtime <rt>. Run this if you install new dependencies or if the runtime hangs.
  %stop_runtime <rt>                 -- Stop runtime <rt> when no longer needed.
  %switch_runtime <rt>               -- Switch active runtime to <rt>. All subsequent executions will use this runtime.
  %%run_on <rt>                      -- Force a cell to always execute on <rt>, regardless of the active runtime.
  %copy_data <r1:file1> <r2:file2>   -- Copy <file1> in <r1> to <file2> in <r2>.
""")

# END OF AUTORUN
# ========================================================================
