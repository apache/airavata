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


### Build and upload the magic extension

* Make sure you have `setuptools` and `wheel` installed:

```
pip install setuptools wheel
```

* Build the extension
```
cd jupyter/extension/airavata_jupyter_magic
python setup.py sdist bdist_wheel
```

* Upload the distribution to pypi
```
pip install twine
twine upload dist/*
```

* Extension is available at https://pypi.org/project/airavata-jupyter-magic



### Jupyter container setup

#### Build
docker build --platform linux/x86_64 -t dimuthuupe/airavata-jupyter-lab .

#### Run
docker run --privileged --device /dev/fuse  --platform linux/x86_64 --security-opt apparmor:unconfined -p 18888:8888 dimuthuupe/airavata-jupyter-lab 

