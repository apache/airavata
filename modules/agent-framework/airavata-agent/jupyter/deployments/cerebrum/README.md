## Setting up Dev Environment

### Agent
Build the Agent
```shell
cd v1l4
./build-container.sh
```

Execute the following command to run an agent locally.
```shell
docker run --rm -v $(pwd):/workspace -w /workspace lahiruj/airavata-cerebrum-agent bash -c "./docker-agent.sh"
```

### Jupyter lab
To run the jupyter lab

```shell
cd jupyterlab
./build-jupyter-image.sh
./run-jupyter-lab-container
```


