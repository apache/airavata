// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

package main

import (
	protos "airavata-agent/protos"
	"context"
	"errors"
	"flag"
	"io"
	"log"
	"os"
	"os/exec"
	"strings"
	"time"

	"airavata-agent/pkg"

	"google.golang.org/grpc"
	"google.golang.org/grpc/credentials/insecure"
)

type Stream = grpc.BidiStreamingClient[protos.AgentMessage, protos.ServerMessage]

var defaultLibs = []string{"python<3.12", "pip", "ipykernel", "git", "flask", "jupyter_client", "ttyd"}

const (
	noWorkBackoff   = 3 * time.Second // sleep between polls when no work
	maxEmptyReplies = 3               // exit after 3 consecutive empty replies
)

func main() {

	// Define flags with default empty values.
	serverUrl := flag.String("server", "", "Server flag (optional)")
	agentId := flag.String("agent", "", "Agent flag (optional)")
	environ := flag.String("environ", "", "Environment name (optional)")
	lib := flag.String("lib", "", "Libraries flag (optional)")
	pip := flag.String("pip", "", "Pip flag (optional)")

	// Parse the flags provided by the user.
	flag.Parse()
	log.Printf("[agent.go] main() --server=%s\n", *serverUrl)
	log.Printf("[agent.go] main() --agent=%s\n", *agentId)
	log.Printf("[agent.go] main() --environ=%s\n", *environ)
	log.Printf("[agent.go] main() --lib=%s\n", *lib)
	log.Printf("[agent.go] main() --pip=%s\n", *pip)

	// Validate required flags
	if *serverUrl == "" {
		log.Fatalf("[agent.go] main() Error: --server flag is required.\n")
	}
	if *agentId == "" {
		log.Fatalf("[agent.go] main() Error: --agent flag is required.\n")
	}
	if *environ == "" {
		log.Fatalf("[agent.go] main() Error: --environ flag is required.\n")
	}

	ctx := context.Background()
	conn, err := grpc.DialContext(
		ctx,
		*serverUrl,
		grpc.WithTransportCredentials(insecure.NewCredentials()),
		grpc.WithDefaultCallOptions(
			grpc.MaxCallRecvMsgSize(20*1024*1024), // 10MB for incoming messages
			grpc.MaxCallSendMsgSize(20*1024*1024), // 10MB for outgoing messages (if needed)
		),
	)

	//conn, err := grpc.NewClient(*serverUrl, grpc.WithTransportCredentials(insecure.NewCredentials()))
	if err != nil {
		log.Fatalf("[agent.go] main() Did not connect to %s: %v\n", *serverUrl, err)
	}
	log.Printf("[agent.go] main() Connected to %s\n", *serverUrl)
	defer conn.Close()

	c := protos.NewAgentCommunicationServiceClient(conn)
	stream, err := c.CreateMessageBus(context.Background())
	if err != nil {
		log.Fatalf("[agent.go] main() Error creating stream: %v\n", err)
	}
	log.Printf("[agent.go] main() Created stream...\n")

	// create environment, don't recreate if exists
	envCmd := exec.Command("micromamba", "create", "-n", *environ)
	envCmd.Stdin = strings.NewReader("n\n")
	if err := envCmd.Run(); err != nil {
		log.Printf("[agent.go] main() Using environment: %s", *environ)
	} else {
		log.Printf("[agent.go] main() Created environment: %s\n", *environ)
	}

	var libList []string
	if strings.TrimSpace(*lib) != "" {
		libList = append(strings.Split(*lib, ","), defaultLibs...)
		log.Printf("[agent.go] main() Installing --lib: %v\n", libList)
		libCmd := exec.Command("micromamba", "install", "-n", *environ, "--yes")
		libCmd.Args = append(libCmd.Args, libList...)
		libCmd.Stdout = os.Stdout
		libCmd.Stderr = os.Stderr
		if err := libCmd.Run(); err != nil {
			log.Fatalf("[agent.go] main() Error Installing --lib: %v\n", err)
		}
		log.Printf("[agent.go] main() Installed --lib: %v\n", libList)
	} else {
		log.Printf("[agent.go] main() No --lib to install.\n")
	}

	var pipList []string
	if strings.TrimSpace(*pip) != "" {
		pipList = strings.Split(*pip, ",")
		log.Printf("[agent.go] main() Installing --pip: %v\n", pipList)
		pipCmd := exec.Command("micromamba", "run", "-n", *environ, "pip", "install")
		pipCmd.Args = append(pipCmd.Args, pipList...)
		pipCmd.Stdout = os.Stdout
		pipCmd.Stderr = os.Stderr
		if err := pipCmd.Run(); err != nil {
			log.Fatalf("[agent.go] main() Error Installing --pip: %v\n", err)
		}
		log.Printf("[agent.go] main() Installed --pip: %v\n", pipList)
	} else {
		log.Printf("[agent.go] main() No --pip to install.\n")
	}

	log.Printf("[agent.go] main() Trying to connect to %s with agent id %s\n", *serverUrl, *agentId)
	msg := &protos.AgentMessage{
		Message: &protos.AgentMessage_AgentPing{
			AgentPing: &protos.AgentPing{
				AgentId: *agentId,
			},
		},
	}
	if err = stream.Send(msg); err != nil {
		log.Fatalf("[agent.go] main() Failed to connect to the server: %v\n", err)
	}
	log.Printf("[agent.go] main() Connected to the server.\n")

	requestNextJobUnit(stream, *agentId)

	// start interceptor
	ch := make(chan struct{})
	go startInterceptor(stream, ch, *agentId, *environ)

	<-ch

	if err := stream.CloseSend(); err != nil {
		log.Fatalf("[agent.go] main() failed to close the stream: %v\n", err)
	}

}

func startInterceptor(stream Stream, grpcStreamChannel chan struct{}, agentId string, envName string) {
	consecutiveEmpty := 0
	pollingJobEnabled := true

	for {
		in, err := stream.Recv()
		if err == io.EOF {
			close(grpcStreamChannel)
			return
		}
		if err != nil {
			log.Fatalf("[agent.go] Failed to receive a message : %v\n", err)
		}
		log.Printf("[agent.go] Received message %s\n", in.Message)
		switch x := in.GetMessage().(type) {

		case *protos.ServerMessage_EnvSetupRequest:
			log.Printf("[agent.go] Recived a env setup request\n")
			executionId := x.EnvSetupRequest.ExecutionId
			envNameReq := x.EnvSetupRequest.EnvName
			envLibs := x.EnvSetupRequest.Libraries
			envPip := x.EnvSetupRequest.Pip
			go pkg.CreateEnv(stream, executionId, envNameReq, append(envLibs, defaultLibs...), envPip)

		case *protos.ServerMessage_PythonExecutionRequest:
			log.Printf("[agent.go] Recived a python execution request\n")
			executionId := x.PythonExecutionRequest.ExecutionId
			envNameReq := x.PythonExecutionRequest.EnvName
			workingDir := x.PythonExecutionRequest.WorkingDir
			code := x.PythonExecutionRequest.Code
			go pkg.ExecutePython(stream, executionId, envNameReq, workingDir, code)

		case *protos.ServerMessage_CommandExecutionRequest:
			log.Printf("[agent.go] Recived a shell execution request\n")
			executionId := x.CommandExecutionRequest.ExecutionId
			envNameReq := x.CommandExecutionRequest.EnvName
			workingDir := x.CommandExecutionRequest.WorkingDir
			execArgs := x.CommandExecutionRequest.Arguments
			go pkg.ExecuteShell(stream, executionId, envNameReq, workingDir, execArgs)

		case *protos.ServerMessage_AsyncCommandExecutionRequest:
			log.Printf("[agent.go] Recived a async shell execution request\n")
			executionId := x.AsyncCommandExecutionRequest.ExecutionId
			envNameReq := x.AsyncCommandExecutionRequest.EnvName
			workingDir := x.AsyncCommandExecutionRequest.WorkingDir
			execArgs := x.AsyncCommandExecutionRequest.Arguments
			go pkg.ExecuteShellAsync(stream, executionId, envNameReq, workingDir, execArgs)

		case *protos.ServerMessage_AsyncCommandListRequest:
			log.Printf("[agent.go] Recived async shell list request\n")
			executionId := x.AsyncCommandListRequest.ExecutionId
			go pkg.ListAsyncProcesses(stream, executionId)

		case *protos.ServerMessage_AsyncCommandTerminateRequest:
			log.Printf("[agent.go] Recived a async shell termination request\n")
			executionId := x.AsyncCommandTerminateRequest.ExecutionId
			processId := x.AsyncCommandTerminateRequest.ProcessId
			go pkg.KillAsyncProcess(stream, executionId, processId)

		case *protos.ServerMessage_JupyterExecutionRequest:
			log.Printf("[agent.go] Recived a jupyter execution request\n")
			executionId := x.JupyterExecutionRequest.ExecutionId
			envNameReq := x.JupyterExecutionRequest.EnvName
			code := x.JupyterExecutionRequest.Code
			go pkg.ExecuteJupyter(stream, executionId, envNameReq, code)

		case *protos.ServerMessage_KernelRestartRequest:
			log.Printf("[agent.go] Recived a kernel restart request\n")
			executionId := x.KernelRestartRequest.ExecutionId
			envNameReq := x.KernelRestartRequest.EnvName
			go pkg.RestartKernel(stream, executionId, envNameReq)

		case *protos.ServerMessage_TunnelCreationRequest:
			log.Printf("[agent.go] Received a tunnel creation request\n")
			executionId := x.TunnelCreationRequest.ExecutionId
			localBindHost := x.TunnelCreationRequest.LocalBindHost
			localPort := x.TunnelCreationRequest.LocalPort
			tunnelServerHost := x.TunnelCreationRequest.TunnelServerHost
			tunnelServerPort := x.TunnelCreationRequest.TunnelServerPort
			tunnelServerApiUrl := x.TunnelCreationRequest.TunnelServerApiUrl
			tunnelServerToken := x.TunnelCreationRequest.TunnelServerToken

			pkg.OpenRemoteTunnel(stream, executionId, localBindHost, localPort, tunnelServerHost,
				tunnelServerPort, tunnelServerApiUrl, tunnelServerToken)

		case *protos.ServerMessage_TunnelTerminationRequest:
			executionId := x.TunnelTerminationRequest.ExecutionId
			tunnelId := x.TunnelTerminationRequest.TunnelId
			log.Printf("[agent.go] Received a tunnel termination request for tunnelId: %s\n", tunnelId)
			if err := pkg.CloseRemoteTunnel(stream, executionId, tunnelId); err != nil {
				log.Printf("[agent.go] Failed to close tunnel: %v\n", err)
			} else {
				log.Printf("[agent.go] Closed tunnel: %s\n", tunnelId)
			}

		case *protos.ServerMessage_AssignJobUnit:
			if !pollingJobEnabled {
				// Shouldn't happen, but ignore if an agent is in interactive mode
				break
			}

			consecutiveEmpty = 0 // reset on actual work
			ju := x.AssignJobUnit
			log.Printf("[agent.go] Assigned job unit: %s\n", ju.JobUnitId)

			exitCode := runResolvedCommand(envName, ju.ResolvedCommand)

			_ = stream.Send(&protos.AgentMessage{
				Message: &protos.AgentMessage_JobUnitCompleted{
					JobUnitCompleted: &protos.JobUnitCompleted{
						JobUnitId: ju.JobUnitId,
						ExitCode:  int32(exitCode),
					},
				},
			})
			requestNextJobUnit(stream, agentId)

		case *protos.ServerMessage_NoJobUnitAvailable:
			reason := x.NoJobUnitAvailable.Reason

			// If the server says no batch jobs have assigned for the agent
			if reason == "NO_ASSIGNMENT" {
				if pollingJobEnabled {
					log.Printf("[agent.go] No batch assignment for this agent; stopping job unit polling and staying in interactive mode.\n")
				}
				pollingJobEnabled = false
				break
			}

			// If polling is disabled, ignore no work messages
			if !pollingJobEnabled {
				break
			}

			switch reason {
			case "EMPTY", "EMPTY_ALL_DONE":
				consecutiveEmpty++
				log.Printf("[agent.go] No job unit available (%d/%d): %s\n", consecutiveEmpty, maxEmptyReplies, reason)

				if consecutiveEmpty >= maxEmptyReplies {
					log.Printf("[agent.go] No work after %d polls — shutting down agent (sweep complete).\n", maxEmptyReplies)
					close(grpcStreamChannel)
					return
				}

				time.Sleep(noWorkBackoff)
				requestNextJobUnit(stream, agentId)

			default:
				// Unknown condition, retry without counting
				time.Sleep(noWorkBackoff)
				requestNextJobUnit(stream, agentId)
			}
		}

	}
}

func requestNextJobUnit(stream Stream, agentId string) {
	_ = stream.Send(&protos.AgentMessage{
		Message: &protos.AgentMessage_RequestNextJobUnit{
			RequestNextJobUnit: &protos.RequestNextJobUnit{
				AgentId: agentId,
			},
		},
	})
}

func runResolvedCommand(envName string, resolved string) int {
	log.Printf("[agent.go] Running job unit command: %s\n", resolved)
	cmd := exec.Command("micromamba", "run", "-n", envName, "bash", "-lc", resolved)
	cmd.Stdout = os.Stdout
	cmd.Stderr = os.Stderr
	if err := cmd.Run(); err != nil {
		var ee *exec.ExitError
		if errors.As(err, &ee) {
			log.Printf("[agent.go] Command exited with code %d\n", ee.ExitCode())
			return ee.ExitCode()
		}
		log.Printf("[agent.go] Command failed to start/run: %v\n", err)
		return 1
	}
	return 0
}
