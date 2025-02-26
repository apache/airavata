import base64
import binascii
import json
import time
from pathlib import Path
from typing import NamedTuple
import jwt
import os
import requests
from IPython.core.magic import register_cell_magic, register_line_magic
from IPython.display import HTML, Image, display
from device_auth import DeviceFlowAuthenticator


AgentInfo = NamedTuple('AgentInfo', [
    ('agentId', str),
    ('experimentId', str),
    ('processId', str),
    ('cluster', str),
    ('queue', str),
    ('cpus', int),
    ('memory', int),
    ('walltime', int),
    ('gateway_id', str),
    ('group', str),
])
api_base_url = "https://api.gateway.cybershuttle.org"
file_server_url = "http://3.142.234.94:8050"
current_agent : AgentInfo | None = None
MSG_NOT_INITIALIZED = r"Remote agent not initialized. Please run %init_remote cluster=<cluster> cpu=<cpu> memory=<memory mb> queue=<queue> walltime=<walltime minutes> group=<group>"


def get_access_token() -> str | None:
    token_from_env = os.getenv('CS_ACCESS_TOKEN')
    if token_from_env:
        return token_from_env
    EXPLICIT_TOKEN_FILE = Path("~").expanduser() / "csagent" / "token" / "keys.json"
    if EXPLICIT_TOKEN_FILE.exists():
        with open(EXPLICIT_TOKEN_FILE, "r") as f:
            return json.load(f).get("access_token")


def get_agent_status() -> dict | None:
    if not current_agent:
        return print(MSG_NOT_INITIALIZED)
    url = f"{api_base_url}/api/v1/agent/{current_agent.agentId}"
    response = requests.get(url)
    if response.status_code == 202:
        return response.json()
    return print(f"Got [{response.status_code}] Response: {response.text}")


def get_process_id(experiment_id: str, headers) -> str:
    """
    Get process id by experiment id

    """
    url = f"{api_base_url}/api/v1/exp/{experiment_id}/process"
    process_id = ""
    while not process_id:
        response = requests.get(url, headers=headers)
        if response.status_code == 200:
            process_id = response.json().get("processId")
        else:
            time.sleep(5)
    return process_id


def submit_agent_job(experiment_name, cluster, queue, cpus, memory, walltime, access_token, group, gateway_id='default'):
    global current_agent

    # URL to which the POST request will be sent
    url = api_base_url + '/api/v1/exp/launch'

    # Data to be sent in the POST request
    data = {
        'experimentName': experiment_name,
        'remoteCluster': cluster,
        'cpuCount': cpus,
        'nodeCount': 1,
        'memory': memory,
        'wallTime': walltime,
        'queue': queue,
        'group': group,
    }

    # Convert the data to JSON format
    json_data = json.dumps(data)

    decode = jwt.decode(access_token, options={"verify_signature": False})
    user_id = decode['preferred_username']
    claimsMap = {
        "userName": user_id,
        "gatewayID": gateway_id
    }

    # Headers
    headers = {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer ' + access_token,
        'X-Claims': json.dumps(claimsMap)
    }

    # Send the POST request
    response = requests.post(url, headers=headers, data=json_data)

    # Check if the request was successful
    if response.status_code == 200:
        # Parse the JSON response
        obj = response.json()
        current_agent = AgentInfo(
            agentId=obj['agentId'],
            experimentId=obj['experimentId'],
            processId=get_process_id(obj['experimentId'], headers=headers),
            cluster=cluster,
            queue=queue,
            cpus=cpus,
            memory=memory,
            walltime=walltime,
            gateway_id=gateway_id,
            group=group,
        )
        print('Agent Initialized:', current_agent)
    else:
        print('Failed to send POST request. Status code:', response.status_code)
        print('Response:', response.text)


def terminate_agent(access_token, gateway_id='default'):
    global current_agent
    if not current_agent:
        return print(MSG_NOT_INITIALIZED)

    expId = current_agent.experimentId
    url = api_base_url + '/api/v1/exp/terminate/' + expId

    decode = jwt.decode(access_token, options={"verify_signature": False})
    user_id = decode['preferred_username']
    claimsMap = {
        "userName": user_id,
        "gatewayID": gateway_id
    }

    # Headers
    headers = {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer ' + access_token,
        'X-Claims': json.dumps(claimsMap)
    }

    # Send the POST request
    response = requests.get(url, headers=headers)

    if response.status_code == 200:
        # Parse the JSON response
        response_data = response.json()
        print('Agent terminated:', response_data)
        current_agent = None
    else:
        print('Failed to send termination request. Status code:', response.status_code)
        print('Response:', response.text)


@register_cell_magic
def run_remote(line, cell):
    global current_agent
    if not current_agent:
        return print(MSG_NOT_INITIALIZED)

    url = api_base_url + '/api/v1/agent/executejupyterrequest'

    data = {
        "sessionId": "session1",
        "keepAlive": True,
        "code": cell,
        "agentId": current_agent.agentId
    }

    json_data = json.dumps(data)
    response = requests.post(url, headers={'Content-Type': 'application/json'}, data=json_data)
    execution_resp = response.json()
    execution_id = execution_resp.get("executionId")
    error = execution_resp.get("error")
    if error:
        print("Cell execution failed. Error: " + error)
    if execution_id:
        while True:
            url = api_base_url + "/api/v1/agent/executejupyterresponse/" + execution_id
            response = requests.get(url, headers={'Accept': 'application/json'})
            json_response = response.json()
            if json_response.get('available'):
                result_str = json_response.get('responseString')
                try:
                    result = json.loads(result_str)
                except json.JSONDecodeError as e:
                    print(f"Failed to decode JSON response: {e}")
                    break

                if 'outputs' in result:
                    for output in result['outputs']:
                        output_type = output.get('output_type')
                        if output_type == 'display_data':
                            data_obj = output.get('data', {})
                            if 'image/png' in data_obj:
                                image_data = data_obj['image/png']
                                try:
                                    image_bytes = base64.b64decode(image_data)
                                    display(Image(data=image_bytes, format='png'))
                                except binascii.Error as e:
                                    print(f"Failed to decode image data: {e}")
                            # Ignoring any texts in the display data
                            # if 'text/plain' in data_obj:
                            #     print(data_obj['text/plain'])

                        elif output_type == 'stream':
                            stream_name = output.get('name', 'stdout')
                            stream_text = output.get('text', '')
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

                        elif output_type == 'execute_result':
                            data_obj = output.get('data', {})
                            if 'text/plain' in data_obj:
                                print(data_obj['text/plain'])
                else:
                    if 'result' in result:
                        print(result['result'])
                    elif 'error' in result:
                        print(result['error']['ename'])
                        print(result['error']['evalue'])
                        print(result['error']['traceback'])
                    elif 'display' in result:
                        data_obj = result['display'].get('data', {})
                        if 'image/png' in data_obj:
                            image_data = data_obj['image/png']
                            try:
                                image_bytes = base64.b64decode(image_data)
                                display(Image(data=image_bytes, format='png'))
                            except binascii.Error as e:
                                print(f"Failed to decode image data: {e}")
                break
            time.sleep(1)


@register_line_magic
def cs_login(line):
    try:
        authenticator = DeviceFlowAuthenticator()
        authenticator.login()
    except ValueError as e:
        print(f"Configuration error: {e}")


@register_line_magic
def init_remote(line):
    if current_agent:
        status = get_agent_status()
        if status:
            if status['agentUp']:
                print("An agent is already running. Please terminate it first by running %terminate_remote")
                return
            else:
                print("An agent was scheduled. Please terminate it first by running %terminate_remote")
                return

    access_token = get_access_token()
    pairs = line.split()

    # Initialize variable to store the cluster value
    cluster_value = None
    memory_value = None
    cpu_value = None
    queue_value = None
    walltime_value = None
    group_value = ""

    # Iterate through the pairs to find the cluster value
    for pair in pairs:
        if pair.startswith("cluster="):
            cluster_value = pair.split("=")[1]
        if pair.startswith("cpu="):
            cpu_value = pair.split("=")[1]
        if pair.startswith("memory="):
            memory_value = pair.split("=")[1]
        if pair.startswith("queue="):
            queue_value = pair.split("=")[1]
        if pair.startswith("walltime="):
            walltime_value = pair.split("=")[1]
        if pair.startswith("group="):
            group_value = pair.split("=")[1]

    submit_agent_job('CS_Agent', cluster_value, queue_value, cpu_value, memory_value, walltime_value, access_token, group_value)


@register_line_magic
def status_remote(line):
    status = get_agent_status()
    if status:
        if status['agentUp']:
            print("Agent", status['agentId'], 'is running')
        else:
            print("Agent", status['agentId'], 'is still preparing. Please wait')


@register_line_magic
def terminate_remote(line):
    global current_agent
    access_token = get_access_token()
    if current_agent:
        terminate_agent(access_token)


@register_line_magic
def push_remote(line):
    if not current_agent:
        return print(MSG_NOT_INITIALIZED)
    pairs = line.split()
    remot_path = None
    local_path = None
    for pair in pairs:
        if pair.startswith("source="):
            local_path = pair.split("=")[1]
        if pair.startswith("target="):
            remot_path = pair.split("=")[1]
    # validate paths
    if not remot_path or not local_path:
        return print("Please provide paths for both source and target")
    # upload file
    print(f"Pushing local:{local_path} to remote:{remot_path}")
    url = f"{file_server_url}/upload/live/{current_agent.processId}/{remot_path}"
    with open(local_path, "rb") as file:
        files = {"file": file}
        response = requests.post(url, files=files)
    print(f"[{response.status_code}] Uploaded local:{local_path} to remote:{remot_path}")


@register_line_magic
def pull_remote(line):
    if not current_agent:
        return print(MSG_NOT_INITIALIZED)
    pairs = line.split()
    remot_path = None
    local_path = None
    for pair in pairs:
        if pair.startswith("source="):
            remot_path = pair.split("=")[1]
        if pair.startswith("target="):
            local_path = pair.split("=")[1]
    # validate paths
    if not remot_path or not local_path:
        return print("Please provide paths for both source and target")
    # download file
    print(f"Pulling remote:{remot_path} to local:{local_path}")
    url = f"{file_server_url}/download/live/{current_agent.processId}/{remot_path}"
    response = requests.get(url)
    with open(local_path, "wb") as file:
        file.write(response.content)
    print(f"[{response.status_code}] Downloaded remote:{remot_path} to local:{local_path}")

# autorun when imported
ipython.register_magic_function(cs_login)
ipython.register_magic_function(init_remote)
ipython.register_magic_function(status_remote)
ipython.register_magic_function(terminate_remote)
ipython.register_magic_function(run_remote)
ipython.register_magic_function(push_remote)
ipython.register_magic_function(pull_remote)
