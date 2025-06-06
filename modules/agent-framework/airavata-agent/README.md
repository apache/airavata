<!--
    Licensed to the Apache Software Foundation (ASF) under one
    or more contributor license agreements.  See the NOTICE file
    distributed with this work for additional information
    regarding copyright ownership.  The ASF licenses this file
    to you under the Apache License, Version 2.0 (the
    "License"); you may not use this file except in compliance
    with the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied.  See the License for the
    specific language governing permissions and limitations
    under the License.
-->

# The Agent for orchestrating Airavata job workloads
This agent is part of the Apache Airavata platform and is responsible for executing remote workloads, managing Jupyter sessions, and setting up tunnels on behalf of users.

## Download and Set Up micromamba

### Download the binary
Linux
```
wget https://github.com/mamba-org/micromamba-releases/releases/latest/download/micromamba-linux-64 -O micromamba
```

macOS (Intel)
```
wget https://github.com/mamba-org/micromamba-releases/releases/latest/download/micromamba-osx-64 -O micromamba
```

macOS (Apple Silicon)
```
wget https://github.com/mamba-org/micromamba-releases/releases/latest/download/micromamba-osx-arm64 -O micromamba
```

### Make the binary executable
```
chmod +x micromamba
```

### Create required directories
```
mkdir -p $HOME/cybershuttle/scratch/tmp
```

### Export environment variables
```
export MAMBA_ROOT_PREFIX=$HOME/cybershuttle/scratch
export TMPDIR=$HOME/cybershuttle/scratch/tmp
export PATH=$PWD:$PATH
```

## Running the Agent (Using Prebuilt Binary)

### Download prebuilt agent binary
```
wget https://github.com/cyber-shuttle/binaries/releases/download/1.0.1/airavata-agent-linux-amd64 -O airavata-agent
chmod +x airavata-agent
```

### Run the agent
```
./airavata-agent \
  --server <connection_server_url>:19900 \
  --agent <agent_id> \
  --environ <environment_id> \
  --lib "python=3.10,pip,<packages>" \
  --pip "<pip_packages_or_git_urls>"
```
Replace placeholders with your actual configuration values.

#### Example:
```
./airavata-agent
--server loalhost:19900
--agent agent_dd9667fe-78d1-4ffa-a0d2-19074e41dd45
--environ base
--lib "python=3.10,pip,mattersim,torchmetrics,numpy"
--pip "git+https://github.com/cyber-shuttle/mattertune.git"
```

## Running the Agent (Development Mode)

### Set up the go environment
```
go mod tidy
```

### Building proto files

```
go install google.golang.org/protobuf/cmd/protoc-gen-go@latest
go install google.golang.org/grpc/cmd/protoc-gen-go-grpc@latest
export PATH=$PATH:$HOME/go/bin
protoc --go_out=. --go-grpc_out=. agent-communication.proto
```

### Run the agent
```
go install
go run agent.go <connection_server_url> --agent <agent_id> --environ <env>
```

#### Example
```
go run agent.go --server localhost:19900 --agent agent1 --environ base
```

## Build the agent

### Build for the current platform
```
go build
```

### Build for a specific platform
```
env GOOS=linux GOARCH=amd64 go build
```

## Sample Requests

Execute a Shell Command
```
POST http://localhost:18880/api/v1/agent/execute/shell

{
    "agentId": "agent1",
    "workingDir": "",
    "arguments": ["docker", "ps", "-a"],
    "envName": "base"
} 

Response

{
    "executionId": "78fe66aa-4895-4768-8701-5ef50367732e",
    "error": null
}
```

To extract the result, pass the executionId
```
GET http://localhost:18880/api/v1/agent/execute/shell/78fe66aa-4895-4768-8701-5ef50367732e
```

Execute Jupyter Code
```
http://localhost:18880/api/v1/agent/execute/jupyter

{
    "sessionId": "session1",
    "keepAlive": true,
    "code": "print(4 + 5)",
    "agentId": "agent3"
} 

Response

{
    "executionId": "22f02087-87cc-4e90-bc3b-3b969179c31b",
    "error": null
}
```
```
http://localhost:18880/api/v1/agent/execute/jupyter/22f02087-87cc-4e90-bc3b-3b969179c31b

Response

{
    "executionId": "93d82c06-31e5-477f-a73a-760908a7a482",
    "sessionId": null,
    "responseString": "{\"result\":\"9\\n\"}\n",
    "available": true
}
```

Set Up a tcp Tunnel
```
POST http://localhost:18880/api/v1/agent/setup/tunnel

{
    "agentId": "agent1",
    "localPort": 7000,
    "localBindHost": "localhost"
}

Response 

{
    "executionId": "f2f1c982-5a8b-4813-b048-8a71cdfc9578",
    "status": 0,
    "error": null
}

```
Get tunnel info

```
GET http://localhost:18880/api/v1/agent/setup/tunnel/<execution_id>

Response

{
    "executionId": "f2f1c982-5a8b-4813-b048-8a71cdfc9578",
    "tunnelId": "ff2516f6-d5a4-426c-adf4-cd988c66bfc2",
    "poxyPort": 10000,
    "proxyHost": "poxy-host",
    "status": "OK"
}
```

Terminate tunnel

```
POST http://localhost:18880/api/v1/agent/terminate/tunnel

{
    "agentId": "agent1",
    "tunnelId": "0c03281d-a713-4361-9a34-ad833b98dc4f"
}

Response 

{
    "executionId": "fa88ccd8-515d-4c31-bc5a-71471c23d189",
    "status": 0,
    "error": null
}
```
