from IPython.core.magic import register_cell_magic
from IPython.core.magic import register_line_magic

from IPython.display import display, Image, HTML
import base64
import requests
import json
import jwt
import time
from pathlib import Path
import os

current_agent_info = {}
current_agent_info["agentId"] = "agent2"

EXPLICIT_TOKEN_FILE = (
        Path(os.path.expanduser("~")) / "csagent" / "token" / "keys.json"
)

def read_explicit_token_file():
    if not EXPLICIT_TOKEN_FILE.exists():
        return None
    else:
        with open(EXPLICIT_TOKEN_FILE, "r") as f:
            return json.load(f)

def get_access_token():
    expl_token_data = read_explicit_token_file()
    if expl_token_data:
        return expl_token_data["access_token"]

def get_agent_status():
    global current_agent_info

    if not current_agent_info:
        print("No agent was scheduled yet. Please run %init_remote cluster=<cluster> cpu=<cpu> memory=<memory mb> queue=<queue> walltime=<walltime minutes>")
        return

    url = 'http://scigap02.sciencegateways.iu.edu:18880/api/v1/agent/' + current_agent_info['agentId']
    response = requests.get(url)
    if response.status_code == 202:
        return response.json()
    else:
        print('Invalid response reveived. Status code:', response.status_code)
        print('Response:', response.text)

def submit_agent_job(experiment_name, cluster, queue, cpus, memory, wallTime, access_token, gateway_id='testdrive'):

    global current_agent_info
    # URL to which the POST request will be sent
    url = 'http://scigap02.sciencegateways.iu.edu:18880/api/v1/exp/launch'

    # Data to be sent in the POST request
    data = {
        'experimentName': experiment_name,
        'remoteCluster': cluster,
        'cpuCount': cpus,
        'nodeCount': 1,
        'memory': memory,
        'wallTime': wallTime,
        'queue': queue
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
        response_data = response.json()
        print('Response:', response_data)
        current_agent_info = response_data
    else:
        print('Failed to send POST request. Status code:', response.status_code)
        print('Response:', response.text)

def terminate_agent(access_token, gateway_id='testdrive'):

    global current_agent_info

    expId = current_agent_info['experimentId']
    url = 'http://scigap02.sciencegateways.iu.edu:18880/api/v1/exp/terminate/' + expId

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
        current_agent_info = None
    else:
        print('Failed to send termination request. Status code:', response.status_code)
        print('Response:', response.text)


@register_cell_magic
def run_remote(line, cell):

    global current_agent_info
    if not current_agent_info:
        print("No agent was scheduled yet. Please run %init_remote cluster=<cluster> cpu=<cpu> memory=<memory mb> queue=<queue> walltime=<walltime minutes>")
        return

    url = 'http://scigap02.sciencegateways.iu.edu:18880/api/v1/agent/executejupyterrequest'

    data = {
        "sessionId": "session1",
        "keepAlive": True,
        "code": cell,
        "agentId": current_agent_info["agentId"]
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
            url = "http://scigap02.sciencegateways.iu.edu:18880/api/v1/agent/executejupyterresponse/" + execution_id
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
                                except base64.binascii.Error as e:
                                    print(f"Failed to decode image data: {e}")
                            # Ignoring any texts in the display data
                            # if 'text/plain' in data_obj:
                            #     print(data_obj['text/plain'])

                        elif output_type == 'stream':
                            print(output.get('text', ''))

                        elif output_type == 'error':
                            ename = output.get('ename', 'Error')
                            evalue = output.get('evalue', '')
                            traceback = output.get('traceback', [])

                            error_html = f"""
                            <div style="
                                color: #a71d5d;
                                background-color: #fdd;
                                border: 1px solid #a71d5d;
                                padding: 10px;
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
                            except base64.binascii.Error as e:
                                print(f"Failed to decode image data: {e}")
                break
            time.sleep(1)

@register_line_magic
def init_remote(line):

    if current_agent_info:
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

    submit_agent_job('CS Agent', cluster_value, queue_value, cpu_value, memory_value, walltime_value, access_token)

@register_line_magic
def status_remote(line):
    status = get_agent_status()
    if status:
        if status['agentUp']:
            print("Agent", status['agentId'], 'is running')
        else:
            print("Agent", status['agentId'], 'is still prepairing. Please wait')

@register_line_magic
def terminate_remote(line):
    global current_agent_info
    access_token = get_access_token()
    if current_agent_info:
        terminate_agent(access_token)


def load_ipython_extension(ipython):
    ipython.register_magic_function(init_remote)
    ipython.register_magic_function(status_remote)
    ipython.register_magic_function(terminate_remote)
    ipython.register_magic_function(run_remote)
