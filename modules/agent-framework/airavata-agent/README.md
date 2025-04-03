## The Agent for orchestrating Airavata job workloads

## Set up the go environment
```
go mod tidy
```

## Building proto files

```
go install google.golang.org/protobuf/cmd/protoc-gen-go@latest
go install google.golang.org/grpc/cmd/protoc-gen-go-grpc@latest
export PATH=$PATH:$HOME/go/bin
protoc --go_out=. --go-grpc_out=. agent-communication.proto
```

## Running the agent
```
go install
go run agent.go <connection_server_url> <agent_id>
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


```
POST http://localhost:18880/api/v1/agent/execute/shell

{
    "agentId": "agent1",
    "workingDir": "",
    "arguments": ["docker", "ps", "-a"]
} 
```

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

```
POST http://localhost:18880/api/v1/agent/create/tunnel

{
    "destinationHost": "32.241.33.22",
    "destinationPort": "9999",
    "sshUserName": "sshuser",
    "sourcePort": "9001",
    "sshKeyPath": "/Users/dwannipu/.ssh/id_rsa_unencrypted",
    "processId": "process1"
}
```
