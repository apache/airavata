import time
from jupyter_client import KernelManager
from flask import Flask, request, jsonify
import os
import json


app = Flask(__name__)

km = None
kc = None

kernel_running = False

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

@app.route('/execute', methods=['POST'])
def execute():

    global km
    global kc

    code = request.json.get('code', '')
    if not code:
        return jsonify({'error': 'No code provided'}), 400
 
    kc.execute(code)

    # Wait for the result and display it

    execution_noticed = False
    content_text = ""
    while True:
        try:
            msg = kc.get_iopub_msg(timeout=1)
            print("------------------")
            print(msg)
            print("-================-")
            content = msg["content"]
            parent_header = msg["parent_header"]

            # When a message with the text stream comes and it's the result of our execution
            if msg["msg_type"] == "execute_input":
                execution_noticed = True
            if msg["msg_type"] == "stream" and content["name"] == "stdout":
                print(content["text"])
                content_text = content_text + content["text"]
            if msg["msg_type"] == "display_data":
                return jsonify({'display': content}), 200
            if msg["msg_type"] == "error":
                return jsonify({'error': content}), 200
            if msg["msg_type"] == "status" and execution_noticed:
                if content["execution_state"] and content["execution_state"] == "idle":
                    if parent_header and parent_header["msg_type"]:
                        if parent_header["msg_type"] == "execute_request": ## This is a result without stdout like a = 12
                            return jsonify({'result': content_text}), 200
        except KeyboardInterrupt:
            print("Interrupted by user.")
            return jsonify({'error': "Intterrupted by user"}), 500
        except:
            pass

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
    app.run(port=15000)
