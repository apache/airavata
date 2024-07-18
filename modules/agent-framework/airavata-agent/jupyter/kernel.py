import time
from jupyter_client import KernelManager
from flask import Flask, request, jsonify
import os
import json


app = Flask(__name__)

km = None
kc = None

@app.route('/start', methods=['GET'])
def start_kernel():

    global km
    global kc
    # Create a new kernel manager
    km = KernelManager(kernel_name='python3')
    km.start_kernel()

    # Create a client to interact with the kernel
    kc = km.client()
    kc.start_channels()

    # Ensure the client is connected before executing code
    kc.wait_for_ready()
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
    while True:
        try:
            msg = kc.get_iopub_msg(timeout=1)
            #print(msg)
            content = msg["content"]

            # When a message with the text stream comes and it's the result of our execution
            if msg["msg_type"] == "stream" and content["name"] == "stdout":
                print(content["text"])
                return jsonify({'result': content["text"]}), 200
            if msg["msg_type"] == "error":
                return jsonify({'error': content}), 200
        except KeyboardInterrupt:
            print("Interrupted by user.")
            return jsonify({'error': "Intterrupted by user"}), 500
        except:
            pass

@app.route('/stop', methods=['GET'])
def stop():

    global km
    global kc

    kc.stop_channels()
    km.shutdown_kernel()
    return 'Kernel shutting down...'


if __name__ == '__main__':
    app.run(port=15000)
