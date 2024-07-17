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
POST http://localhost:18880/api/v1/agent/execute

{
    "agentId": "agent1",
    "workingDir": "",
    "arguments": ["docker", "ps", "-a"]
} 
```
```
POST http://localhost:18880/api/v1/agent/tunnel

{
    "destinationHost": "32.241.33.22",
    "destinationPort": "9999",
    "sshUserName": "sshuser",
    "sourcePort": "9001",
    "sshKeyPath": "/Users/dwannipu/.ssh/id_rsa_unencrypted",
    "processId": "process1"
}
```
