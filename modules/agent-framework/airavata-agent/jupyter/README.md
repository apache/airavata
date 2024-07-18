## This initializes a jupyter kernel using a python script and controls it using a HTTP API

### Install dependencies
```
pip install flask jupyter jupyter-client
```

### Start the Wrapped Jupyter Kernel
```
python3 kernel.py
```

### Manage the jupyter kernel from the HTTP API
```
curl -X GET  http://127.0.0.1:15000/start
curl -X POST http://127.0.0.1:15000/execute -H "Content-Type: application/json" -d '{"code": "print(4)"}'
curl -X GET  http://127.0.0.1:15000/stop
```