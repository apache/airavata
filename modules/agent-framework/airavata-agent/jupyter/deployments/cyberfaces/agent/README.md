## Setting up Dev Environment

### Agent
Build the Agent
```shell
./build-container.sh
```

Execute the following command to run an agent locally.
```shell
docker run --rm -v $(pwd):/workspace -w /workspace cybershuttle/remote-agent-cyberfaces bash -c "./docker-agent.sh"
```


