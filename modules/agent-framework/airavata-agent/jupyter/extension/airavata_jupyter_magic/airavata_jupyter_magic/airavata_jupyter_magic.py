from IPython.core.magic import register_line_magic, register_cell_magic

def load_ipython_extension(ipython):
    
    @register_cell_magic
    def run_remote(line, cell):
        url = 'http://localhost:18880/api/v1/agent/executejupyterrequest'

        data = {
            "sessionId": "session1",
            "keepAlive": True,
            "code": cell,
            "agentId": "agent3"
        }

        json_data = json.dumps(data)    
        response = requests.post(url, headers={'Content-Type': 'application/json'}, data=json_data)
        execution_resp = response.json()
        execution_id = execution_resp["executionId"]
        error = execution_resp["error"]
        if error:
            print("Cell execution failed. Error: " + error)
        if execution_id:
            while True:
                url = "http://localhost:18880/api/v1/agent/executejupyterresponse/" + execution_id
                response = requests.get(url, headers={'Accept': 'application/json'})
                json_response = response.json()
                #print(json_response)
                if json_response['available']:
                    result_str = json_response['responseString']
                    result = json.loads(result_str)
                    if 'result' in result:
                        print(result['result'])
                    elif 'error' in result:
                        print(result['error']['ename'])
                        print(result['error']['evalue'])
                        print(result['error']['traceback'])
                    break
                time.sleep(1)

    @register_line_magic
    def init_remote(line):
        print(f"Your input: {line}")

    @register_line_magic
    def terminate_remote(line):
        print(f"Your input: {line}")

def unload_ipython_extension(ipython):
    pass