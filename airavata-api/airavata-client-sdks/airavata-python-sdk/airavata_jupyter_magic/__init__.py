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
import requests
import yaml
from IPython.core.getipython import get_ipython
from IPython.core.interactiveshell import ExecutionResult
from IPython.core.magic import register_cell_magic, register_line_magic
from IPython.display import HTML, Image, Javascript, display
from rich.console import Console
from rich.layout import Layout
from rich.live import Live
from rich.panel import Panel

from .device_auth import DeviceFlowAuthenticator

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
        msg = f"Runtime={rt_name} is in state={pstate.name}"
        raise InvalidStateError(msg)

    # third, check the state of agent
    url = f"{api_base_url}/api/v1/agent/{rt.agentId}"
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
    url = api_base_url + '/api/v1/exp/launch'

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
            envName=obj['envName']
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
    stdout_res = requests.get(f"{file_server_url}/download/live/{pid}/AiravataAgent.stdout")
    stderr_res = requests.get(f"{file_server_url}/download/live/{pid}/AiravataAgent.stderr")
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
            outer = Layout()
            outer.split_column(
                Layout(Panel(title_text, height=3)),
                Layout(Panel(stdout_text, style="black", height=12)),
                Layout(Panel(stderr_text, style="red", height=12)),
            )
            return outer
        
        with Live(render(f"Connecting to={rt_name}...", "No STDOUT", "No STDERR"), refresh_per_second=1, console=console) as live:
            while True:
              ready, rstate = is_runtime_ready(access_token, rt, rt_name)
              stdout, stderr = fetch_logs(rt_name)
              if ready:
                  live.update(render(f"Connecting to={rt_name}... status=CONNECTED", stdout, stderr))
                  break
              else:
                  live.update(render(f"Connecting to={rt_name}... status={rstate}", stdout, stderr))
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
        "envName": runtime.envName,
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
    # upload file
    print(f"local:{local_path} --> {remot_rt}:{remot_path}...", end=" ", flush=True)
    pid = state.all_runtimes[remot_rt].processId
    url = f"{file_server_url}/upload/live/{pid}/{remot_path}"
    with open(local_path, "rb") as file:
        files = {"file": file}
        response = requests.post(url, files=files)
    print(f"[{response.status_code}]", flush=True)


def list_remote_content(remot_rt: str, remot_path: str) -> tuple[list[str], list[str]]:
    """
    List content in a remote runtime

    @param remot_rt: the remote runtime name
    @param remot_path: the remote file path
    @param include_dirs: whether to include directories
    @returns: list of files, list of directories

    """
    if not state.all_runtimes.get(remot_rt, None):
        raise RuntimeError(MSG_NOT_INITIALIZED)
    pid = state.all_runtimes[remot_rt].processId
    url = f"{file_server_url}/list/live/{pid}/{remot_path}"
    response = requests.get(url)
    res = response.json()
    if "fileName" in res:
        return [remot_path + "/" + res["fileName"]], []
    elif "directoryName" in res:
        files = [remot_path + "/" + d["fileName"] for d in res["innerFiles"]]
        dirs = [remot_path + "/" + d["directoryName"] for d in res["innerDirectories"]]
        return files, dirs
    else:
        raise Exception(f"Unexpected response from {url}: {res}")

def pull_remote(remot_rt: str, remot_path: str, local_path: str) -> None:
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
    pid = state.all_runtimes[remot_rt].processId
    remote_files, remote_dirs = list_remote_content(remot_rt, remot_path)
    # download files first
    for remote_file in remote_files:
        url = f"{file_server_url}/download/live/{pid}/{remote_file}"
        response = requests.get(url)
        # download to local path
        fp = Path(local_path) / remote_file
        print(f"local:{fp} <-- {remot_rt}:{remot_path}...", end=" ", flush=True)
        with open(fp, "wb") as file:
            file.write(response.content)
        print(f"[{response.status_code}]", flush=True)
    # download directories next
    for remote_dir in remote_dirs:
        target_dir = Path(local_path) / remote_dir
        os.makedirs(target_dir.as_posix(), exist_ok=True)
        pull_remote(remot_rt, remote_dir, target_dir.as_posix())


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
    p.add_argument("--walltime", type=int, help="time (mins)", required=True)
    p.add_argument("--queue", type=str, help="resource queue", required=False)
    p.add_argument("--group", type=str, help="resource group", required=False, default="Default")
    p.add_argument("--file", type=str, help="yml file", required=False)
    p.add_argument("--use", type=str, help="allowed resources", required=False)

    args = p.parse_args(cmd_args, namespace=RequestedRuntime())

    if args.file is not None:
        assert args.use is not None
        cluster, queue  = args.use.split(",")[0].split(":", maxsplit=1) # TODO replace with meta-scheduler
        return submit_agent_job(
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
        return submit_agent_job(
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
    return wait_until_runtime_ready(access_token, rt_name, render_live_logs)


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
        # Check if source_path is a directory
        source_matches = Path.cwd().glob(source_path)
        for source_path in source_matches:
            if source_path.is_dir():
                # Recursively upload all files, preserving structure
                for root, dirs, files in os.walk(source_path):
                    for file in files:
                        file_path = Path(root) / file
                        # Compute relative path from the source directory
                        rel_path = file_path.relative_to(source_path)
                        # Construct the corresponding remote path
                    remote_file_path = str(Path(target_path) / rel_path)
                    push_remote(str(file_path), target_runtime, remote_file_path)
            else:
                push_remote(source_path.as_posix(), target_runtime, target_path)
    elif target_runtime == "local":
        pull_remote(source_runtime, source_path, target_path)
    else:
        print("remote-to-remote copy is not supported yet")


# END OF MAGIC FUNCTIONS
# ========================================================================
# AUTORUN


ipython = get_ipython()
if ipython is None:
    raise RuntimeError("airavata_jupyter_magic requires an ipython session")
assert ipython is not None
api_host = "api.gateway.cybershuttle.org"
api_base_url = f"https://{api_host}"
file_server_url = f"http://{api_host}:8050"
MSG_NOT_INITIALIZED = r"Runtime not found. Please run %request_runtime name=<name> cluster=<cluster> cpu=<cpu> memory=<memory mb> queue=<queue> walltime=<walltime minutes> group=<group> to request one."

state = State(current_runtime="local", all_runtimes={})
orig_run_code = ipython.run_cell_async


def cell_has_magic(raw_cell: str) -> bool:
    lines = raw_cell.strip().splitlines()
    magics = (r"%authenticate", r"%request_runtime", r"%restart_runtime", r"%stop_runtime", r"%wait_for_runtime", r"%switch_runtime", r"%%run_on", r"%stat_runtime", r"%copy_data")
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
            wait_until_runtime_ready(access_token, rt, render_live_logs=False)
            run_on_runtime(rt, raw_cell, result)
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
""")

# END OF AUTORUN
# ========================================================================
