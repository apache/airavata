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

import time
from jupyter_client import KernelManager
from flask import Flask, request, jsonify
import os
import json
import re


app = Flask(__name__)

km = None
kc = None

kernel_running = False

ansi_escape = re.compile(r'\x1B\[[0-?]*[ -/]*[@-~]')

@app.route('/start', methods=['GET'])
def start_kernel():

    global km
    global kc
    global kernel_running

    if kernel_running:
        return "Kernel already running"
    # Create a new kernel manager
    km = KernelManager(kernel_name='python3')
    km.start_kernel()

    # Create a client to interact with the kernel
    kc = km.client()
    kc.start_channels()

    # Ensure the client is connected before executing code
    kc.wait_for_ready()
    kernel_running = True
    return "Kernel started"

def strip_ansi_codes(text):
    return ansi_escape.sub('', text)

@app.route('/execute', methods=['POST'])
def execute():

    global km
    global kc

    code = request.json.get('code', '')
    if not code:
        return jsonify({'error': 'No code provided'}), 400
 
    kc.execute(code)

    outputs = []
    execution_noticed = False

    while True:
        try:
            msg = kc.get_iopub_msg(timeout=5)

            content = msg.get("content", {})
            msg_type = msg.get("msg_type", "")

            # When a message with the text stream comes and it's the result of our execution
            if msg_type == "execute_input":
                execution_noticed = True

            # Handle stdout streams
            if msg_type == "stream" and content.get("name") == "stdout":
                outputs.append({
                    "output_type": "stream",
                    "name": "stdout",
                    "text": content.get("text", "")
                })

            # Handle stderr streams
            if msg_type == "stream" and content.get("name") == "stderr":
                outputs.append({
                    "output_type": "stream",
                    "name": "stderr",
                    "text": content.get("text", "")
                })

            # Handle display data (e.g. plots)
            if msg_type == "display_data":
                outputs.append({
                    "output_type": "display_data",
                    "data": content.get("data", {}),
                    "metadata": content.get("metadata", {})
                })

            # Handle execution results (e.g. return values)
            if msg_type == "execute_result":
                outputs.append({
                    "output_type": "execute_result",
                    "data": content.get("data", {}),
                    "metadata": content.get("metadata", {}),
                    "execution_count": content.get("execution_count", None)
                })

            # Handle errors
            if msg_type == "error":
                # Strip ANSI codes from traceback
                clean_traceback = [strip_ansi_codes(line) for line in content.get("traceback", [])]
                outputs.append({
                    "output_type": "error",
                    "ename": content.get("ename", ""),
                    "evalue": content.get("evalue", ""),
                    "traceback": clean_traceback
                })

            # Check for end of execution
            if msg_type == "status" and content.get("execution_state") == "idle" and execution_noticed:
                break

        except KeyboardInterrupt:
            return jsonify({'error': "Execution interrupted by user"}), 500
        except Exception as e:
            print(f"Error while getting Jupyter message: {str(e)}")

    response = {
        "outputs": outputs
    }

    return jsonify(response), 200

@app.route('/stop', methods=['GET'])
def stop():

    global km
    global kc
    global kernel_running

    if not kernel_running:
        return "Kernel is not running to shut down"

    kc.stop_channels()
    km.shutdown_kernel()
    kernel_running = False
    return 'Kernel shutting down...'


if __name__ == '__main__':
    unix_socket = os.getenv("KERNEL_SOCK")
    assert unix_socket is not None
    app.run(host=f"unix://{unix_socket}")
