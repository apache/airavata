package main

import (
	protos "airavata-agent/protos"
	"bufio"
	"bytes"
	"context"
	"encoding/json"
	"fmt"
	"io"
	"log"
	"net"
	"net/http"
	"net/url"
	"os"
	"os/exec"
	"strings"
	"time"

	"golang.org/x/crypto/ssh"
	"google.golang.org/grpc"
	"google.golang.org/grpc/credentials/insecure"
)

type Stream = grpc.BidiStreamingClient[protos.AgentMessage, protos.ServerMessage]

var pidMap = make(map[string]int)

func main() {

	// get CLI args
	serverUrl := os.Args[1]
	agentId := os.Args[2]

	conn, err := grpc.NewClient(serverUrl, grpc.WithTransportCredentials(insecure.NewCredentials()))
	if err != nil {
		log.Fatalf("[agent.go] main() Did not connect to %s: %v\n", serverUrl, err)
	}
	log.Printf("[agent.go] main() Connected to %s\n", serverUrl)
	defer conn.Close()

	c := protos.NewAgentCommunicationServiceClient(conn)
	stream, err := c.CreateMessageBus(context.Background())
	if err != nil {
		log.Fatalf("[agent.go] main() Error creating stream: %v\n", err)
	}
	log.Printf("[agent.go] main() Created stream...\n")

	log.Printf("[agent.go] main() Trying to connect to %s with agent id %s\n", serverUrl, agentId)
	msg := &protos.AgentMessage{
		Message: &protos.AgentMessage_AgentPing{
			AgentPing: &protos.AgentPing{
				AgentId: agentId,
			},
		},
	}
	if err = stream.Send(msg); err != nil {
		log.Fatalf("[agent.go] main() Failed to connect to the server: %v\n", err)
	}
	log.Printf("[agent.go] main() Connected to the server.\n")

	// start interceptor
	ch := make(chan struct{})
	go startInterceptor(stream, ch)

	<-ch

	if err := stream.CloseSend(); err != nil {
		log.Fatalf("[agent.go] main() failed to close the stream: %v\n", err)
	}

}

func startInterceptor(stream Stream, grpcStreamChannel chan struct{}) {

	envName := "base"

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
			envName = x.EnvSetupRequest.EnvName
			envLibs := x.EnvSetupRequest.Libraries
			envPip := x.EnvSetupRequest.Pip
			go createEnv(stream, executionId, envName, envLibs, envPip)

		case *protos.ServerMessage_PythonExecutionRequest:
			log.Printf("[agent.go] Recived a python execution request\n")
			executionId := x.PythonExecutionRequest.ExecutionId
			envName = x.PythonExecutionRequest.EnvName
			workingDir := x.PythonExecutionRequest.WorkingDir
			code := x.PythonExecutionRequest.Code
			go executePython(stream, executionId, envName, workingDir, code)

		case *protos.ServerMessage_CommandExecutionRequest:
			log.Printf("[agent.go] Recived a shell execution request\n")
			executionId := x.CommandExecutionRequest.ExecutionId
			envName = x.CommandExecutionRequest.EnvName
			workingDir := x.CommandExecutionRequest.WorkingDir
			execArgs := x.CommandExecutionRequest.Arguments
			go executeShell(stream, executionId, envName, workingDir, execArgs)

		case *protos.ServerMessage_JupyterExecutionRequest:
			log.Printf("[agent.go] Recived a jupyter execution request\n")
			executionId := x.JupyterExecutionRequest.ExecutionId
			envName = x.JupyterExecutionRequest.EnvName
			code := x.JupyterExecutionRequest.Code
			go executeJupyter(stream, executionId, envName, code)

		case *protos.ServerMessage_KernelRestartRequest:
			log.Printf("[agent.go] Recived a kernel restart request\n")
			executionId := x.KernelRestartRequest.ExecutionId
			envName = x.KernelRestartRequest.EnvName
			go restartKernel(stream, executionId, envName)

		case *protos.ServerMessage_TunnelCreationRequest:
			log.Printf("[agent.go] Received a tunnel creation request\n")
			executionId := x.TunnelCreationRequest.ExecutionId
			destHost := x.TunnelCreationRequest.DestinationHost
			destPort := x.TunnelCreationRequest.DestinationPort
			srcPort := x.TunnelCreationRequest.SourcePort
			sshUser := x.TunnelCreationRequest.SshUserName
			keyPath := x.TunnelCreationRequest.SshKeyPath
			go openRemoteTunnel(stream, executionId, destHost, destPort, srcPort, sshUser, keyPath)
		}
	}
}

func createEnv(stream Stream, executionId string, envName string, envLibs []string, envPip []string) {
	log.Printf("[agent.go] createEnv() Execution id %s\n", executionId)
	log.Printf("[agent.go] createEnv() Env name %s\n", envName)
	log.Printf("[agent.go] createEnv() Env libs %s\n", envLibs)
	log.Printf("[agent.go] createEnv() Env pip %s\n", envPip)
	// cleanup previous kernel if exists
	if pid, exists := pidMap[envName]; exists {
		cmd := exec.Command("kill", fmt.Sprintf("%d", pid))
		if err := cmd.Run(); err != nil {
			log.Printf("[agent.go] createEnv() Failed to kill existing process with PID %d: %v\n", pid, err)
		} else {
			log.Printf("[agent.go] createEnv() Successfully killed existing process with PID %d\n", pid)
		}
		delete(pidMap, envName)
	}
	// create environment
	if envName != "base" {
		createEnvCmd := exec.Command("micromamba", "create", "-n", envName, "--yes", "--quiet")
		if err := createEnvCmd.Run(); err != nil {
			log.Printf("[agent.go] createEnv() Error creating environment: %v\n", err)
			return
		}
		log.Printf("[agent.go] createEnv() Environment created: %s\n", envName)
	}
	envLibs = append(envLibs, "python<3.12", "pip", "ipykernel", "git", "flask", "jupyter_client")
	installDepsCmd := exec.Command("micromamba", "install", "-n", envName, "--yes")
	installDepsCmd.Args = append(installDepsCmd.Args, envLibs...)
	if err := installDepsCmd.Run(); err != nil {
		log.Printf("[agent.go] createEnv() Error waiting for command: %v\n", err)
		return
	}
	if len(envPip) > 0 {
		installPipCmd := exec.Command("micromamba", "run", "-n", envName, "pip", "install")
		installPipCmd.Args = append(installPipCmd.Args, envPip...)
		if err := installPipCmd.Run(); err != nil {
			log.Printf("[agent.go] createEnv() Error waiting for command: %v\n", err)
			return
		}
	}
	// start kernel in new environment
	pidMap[envName] = startJupyterKernel(envName)
	msg := &protos.AgentMessage{
		Message: &protos.AgentMessage_EnvSetupResponse{
			EnvSetupResponse: &protos.EnvSetupResponse{
				ExecutionId: executionId,
				Status:      "OK",
			},
		},
	}
	if err := stream.Send(msg); err != nil {
		log.Printf("[agent.go] executePython() failed to send result to server: %v\n", err)
	} else {
		log.Printf("[agent.go] executePython() sent result to server\n")
	}
}

func startJupyterKernel(envName string) int {
	log.Printf("[agent.go] startJupyterKernel() Starting python server in env: %s...\n", envName)
	// Run command
	cmd := exec.Command("micromamba", "run", "-n", envName, "python", "/opt/jupyter/kernel.py")
	stdout, err := cmd.StdoutPipe()
	if err != nil {
		log.Printf("[agent.go] startJupyterKernel() Error creating StdoutPipe for cmd: %v\n", err)
		return 0
	}
	stderr, err := cmd.StderrPipe()
	if err != nil {
		log.Printf("[agent.go] startJupyterKernel() Error creating StderrPipe for cmd: %v\n", err)
		return 0
	}
	if err := cmd.Start(); err != nil {
		log.Printf("[agent.go] startJupyterKernel() Error during start: %v\n", err)
		return 0
	}
	log.Printf("[agent.go] startJupyterKernel() Started python server.\n")
	go func() {
		stdoutScanner := bufio.NewScanner(stdout)
		for stdoutScanner.Scan() {
			log.Printf("[agent.go] startJupyterKernel() stdout: %s\n", stdoutScanner.Text())
		}
	}()
	go func() {
		stderrScanner := bufio.NewScanner(stderr)
		for stderrScanner.Scan() {
			log.Printf("[agent.go] startJupyterKernel() stderr: %s\n", stderrScanner.Text())
		}
	}()
	go func() {
		if err := cmd.Wait(); err != nil {
			log.Printf("[agent.go] startJupyterKernel() Error waiting for command: %v\n", err)
		}
	}()
	log.Printf("[agent.go] startJupyterKernel() Command finished.\n")
	return cmd.Process.Pid
}

func executePython(stream Stream, executionId string, envName string, workingDir string, code string) {
	log.Printf("[agent.go] executePython() Execution id %s\n", executionId)
	log.Printf("[agent.go] executePython() Env name %s\n", envName)
	log.Printf("[agent.go] executePython() Working Dir %s\n", workingDir)
	log.Printf("[agent.go] executePython() Code %s\n", code)
	// Run command
	cmd := exec.Command("micromamba", "run", "-n", envName, "python", "-c", code)
	cmd.Dir = workingDir
	output, err := cmd.CombinedOutput()
	responseString := string(output)
	if err != nil {
		log.Printf("[agent.go] executePython() error: %v\n", err)
	} else {
		log.Printf("[agent.go] executePython() completed: %s\n", responseString)
	}
	msg := &protos.AgentMessage{
		Message: &protos.AgentMessage_PythonExecutionResponse{
			PythonExecutionResponse: &protos.PythonExecutionResponse{
				ExecutionId:    executionId,
				ResponseString: responseString,
			},
		},
	}
	if err := stream.Send(msg); err != nil {
		log.Printf("[agent.go] executePython() failed to send result to server: %v\n", err)
	} else {
		log.Printf("[agent.go] executePython() Sent result to server: %s\n", output)
	}
}

func executeShell(stream Stream, executionId string, envName string, workingDir string, execArgs []string) {
	log.Printf("[agent.go] executeShell() Execution id %s\n", executionId)
	log.Printf("[agent.go] executeShell() Env name %s\n", envName)
	log.Printf("[agent.go] executeShell() Exec args %s\n", execArgs)
	// Run command
	cmd := exec.Command("micromamba", "run", "-n", envName, "bash", "-c", strings.Join(execArgs, " "))
	cmd.Dir = workingDir
	output, err := cmd.CombinedOutput()
	responseString := string(output)
	if err != nil {
		log.Printf("[agent.go] executeShell() %s failed: %v\n", executionId, err)
	} else {
		log.Printf("[agent.go] executeShell() %s done: %s\n", executionId, responseString)
	}
	msg := &protos.AgentMessage{
		Message: &protos.AgentMessage_CommandExecutionResponse{
			CommandExecutionResponse: &protos.CommandExecutionResponse{
				ExecutionId:    executionId,
				ResponseString: responseString,
			},
		},
	}
	if err := stream.Send(msg); err != nil {
		log.Printf("[agent.go] executeShell() Failed to send execution result to server: %v\n", err)
	} else {
		log.Printf("[agent.go] executeShell() Sent execution result to server: %s\n", output)
	}
}

func executeJupyter(stream Stream, executionId string, envName string, code string) {
	if _, exists := pidMap[envName]; !exists {
		log.Printf("[agent.go] executeJupyter() Starting python server in env: %s...\n", envName)
		pidMap[envName] = startJupyterKernel(envName)
		time.Sleep(5 * time.Second)
	}
	log.Printf("[agent.go] executeJupyter() Execution ID: %s, Env: %s, Code: %s\n", executionId, envName, code)
	unixSock := os.Getenv("KERNEL_SOCK")
	client := &http.Client{
		Transport: &http.Transport{
			DialContext: func(_ context.Context, _, _ string) (net.Conn, error) {
				return net.Dial("unix", unixSock)
			},
		},
	}
	sendResponse := func(response string, err error) {
		if err != nil {
			log.Printf("[agent.go] executeJupyter() Error: %v\n", err)
			response = "Failed while running the cell in remote. Please retry"
		}
		msg := &protos.AgentMessage{
			Message: &protos.AgentMessage_JupyterExecutionResponse{
				JupyterExecutionResponse: &protos.JupyterExecutionResponse{
					ExecutionId:    executionId,
					ResponseString: response,
				},
			},
		}
		if streamErr := stream.Send(msg); streamErr != nil {
			log.Printf("[agent.go] executeJupyter() Failed to send jupyter execution result to server: %v\n", streamErr)
		}
	}
	// Start kernel
	startUrl := &url.URL{Scheme: "http", Host: "localhost", Path: "/start"}
	req, err := http.NewRequest("GET", startUrl.String(), nil)
	if err != nil {
		sendResponse("", fmt.Errorf("failed to create start kernel request: %w", err))
		return
	}
	resp, err := client.Do(req)
	if err != nil {
		sendResponse("", fmt.Errorf("failed to send start kernel request: %w", err))
		return
	}
	defer resp.Body.Close()
	if _, err := io.ReadAll(resp.Body); err != nil {
		sendResponse("", fmt.Errorf("failed to read start kernel response: %w", err))
		return
	}
	log.Printf("[agent.go] executeJupyter() Successfully started the jupyter kernel\n")
	// Execute code on kernel
	executeUrl := &url.URL{Scheme: "http", Host: "localhost", Path: "/execute"}
	data := map[string]string{"code": code, "executionId": executionId}
	jsonData, err := json.Marshal(data)
	if err != nil {
		sendResponse("", fmt.Errorf("failed to marshal JSON: %w", err))
		return
	}
	req, err = http.NewRequest("POST", executeUrl.String(), bytes.NewBuffer(jsonData))
	if err != nil {
		sendResponse("", fmt.Errorf("failed to create execute code request: %w", err))
		return
	}
	req.Header.Set("Content-Type", "application/json")
	resp, err = client.Do(req)
	if err != nil {
		sendResponse("", fmt.Errorf("failed to send execute code request: %w", err))
		return
	}
	defer resp.Body.Close()
	bodyBytes, err := io.ReadAll(resp.Body)
	if err != nil {
		sendResponse("", fmt.Errorf("failed to read execute code response: %w", err))
		return
	}
	jupyterResponse := string(bodyBytes)
	log.Printf("[agent.go] executeJupyter() id: %s response: %s\n", executionId, jupyterResponse)
	sendResponse(jupyterResponse, nil)
}

func restartKernel(stream Stream, executionId string, envName string) {
	log.Printf("[agent.go] restartKernel() Execution id %s\n", executionId)
	log.Printf("[agent.go] restartKernel() Env name %s\n", envName)
	if pid, exists := pidMap[envName]; exists {
		cmd := exec.Command("kill", fmt.Sprintf("%d", pid))
		if err := cmd.Run(); err != nil {
			log.Printf("[agent.go] restartKernel() Failed to kill existing process with PID %d: %v\n", pid, err)
		} else {
			log.Printf("[agent.go] restartKernel() Successfully killed existing process with PID %d\n", pid)
		}
		delete(pidMap, envName)
	}
	pidMap[envName] = startJupyterKernel(envName)
	msg := &protos.AgentMessage{
		Message: &protos.AgentMessage_KernelRestartResponse{
			KernelRestartResponse: &protos.KernelRestartResponse{
				ExecutionId: executionId,
				Status:      "OK",
			},
		},
	}
	if err := stream.Send(msg); err != nil {
		log.Printf("[agent.go] restartKernel() failed to send result to server: %v\n", err)
	} else {
		log.Printf("[agent.go] restartKernel() sent result to server\n")
	}
}

func openRemoteTunnel(stream Stream, executionId string, destHost string, destPort string, localPort string, sshUser string, sshKeyFile string) {
	log.Printf("[agent.go] openRemoteTunnel() rhost: %s, rport: %s, lport: %s, user: %s, keyfile: %s\n", destHost, destPort, localPort, sshUser, sshKeyFile)
	key, err := os.ReadFile(sshKeyFile)
	if err != nil {
		log.Printf("[agent.go] openRemoteTunnel() unable to read private key: %v\n", err)
		return
	}
	signer, err := ssh.ParsePrivateKey(key)
	if err != nil {
		log.Printf("[agent.go] openRemoteTunnel() unable to parse private key: %v\n", err)
		return
	}
	sshConfig := &ssh.ClientConfig{
		User:            sshUser,
		Auth:            []ssh.AuthMethod{ssh.PublicKeys(signer)},
		HostKeyCallback: ssh.InsecureIgnoreHostKey(),
	}
	sshConn, err := ssh.Dial("tcp", net.JoinHostPort(destHost, "22"), sshConfig)
	if err != nil {
		log.Printf("[agent.go] openRemoteTunnel() failed to dial SSH: %v\n", err)
		return
	}
	defer sshConn.Close()
	log.Printf("[agent.go] openRemoteTunnel() SSH connection established.\n")
	remoteListener, err := sshConn.Listen("tcp", fmt.Sprintf("0.0.0.0:%s", destPort))
	if err != nil {
		log.Printf("[agent.go] openRemoteTunnel() failed to listen on remote port %s: %s\n", destPort, err)
		return
	}
	defer remoteListener.Close()
	log.Printf("[agent.go] openRemoteTunnel() reverse SSH tunnel established. Listening on remote port: %s\n", destPort)
	msg := &protos.AgentMessage{
		Message: &protos.AgentMessage_TunnelCreationResponse{
			TunnelCreationResponse: &protos.TunnelCreationResponse{
				ExecutionId: executionId,
				Status:      "OK",
			},
		},
	}
	if streamErr := stream.Send(msg); streamErr != nil {
		log.Printf("[agent.go] openRemoteTunnel() failed to inform the server: %v\n", streamErr)
	}
	for {
		remoteConn, err := remoteListener.Accept()
		if err != nil {
			log.Printf("[agent.go] openRemoteTunnel() failed to accept remote connection: %v\n", err)
			continue
		}
		go handleConnection(remoteConn, "localhost", localPort)
	}
}

func handleConnection(remoteConn net.Conn, localHost, localPort string) {
	log.Printf("[agent.go] handleConnection() Handling connection to local host %s:%s\n", localHost, localPort)
	defer remoteConn.Close()
	localConn, err := net.Dial("tcp", net.JoinHostPort(localHost, localPort))
	if err != nil {
		log.Printf("[agent.go] handleConnection() failed to connect to local host %s:%s! %v\n", localHost, localPort, err)
		return
	}
	defer localConn.Close()
	done := make(chan struct{}, 2) // Buffered channel to prevent goroutine leaks
	log.Printf("[agent.go] handleConnection() starting data transfer between remote and local connections...\n")
	go copyConn(remoteConn, localConn, done)
	go copyConn(localConn, remoteConn, done)
	<-done // Wait for both copy operations to complete
	<-done
	log.Printf("[agent.go] handleConnection() Data transfer completed.\n")
}

func copyConn(writer, reader net.Conn, done chan struct{}) {
	defer func() { done <- struct{}{} }()
	if _, err := io.Copy(writer, reader); err != nil && err != io.EOF {
		log.Printf("[agent.go] copyConn() Data copy error: %v\n", err)
	}
}
