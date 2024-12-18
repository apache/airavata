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
	"os"
	"os/exec"
	"strings"

	"golang.org/x/crypto/ssh"
	"google.golang.org/grpc"
	"google.golang.org/grpc/credentials/insecure"
)

func main() {

	args := os.Args[1:]
	serverUrl := args[0]
	agentId := args[1]
	grpcStreamChannel := make(chan struct{})
	kernelChannel := make(chan struct{})

	conn, err := grpc.NewClient(serverUrl, grpc.WithTransportCredentials(insecure.NewCredentials()))
	if err != nil {
		log.Fatalf("did not connect: %v", err)
	}
	defer conn.Close()

	c := protos.NewAgentCommunicationServiceClient(conn)

	stream, err := c.CreateMessageBus(context.Background())

	if err != nil {
		log.Fatalf("Error creating stream: %v", err)
	}

	log.Printf("Trying to connect to %s with agent id %s", serverUrl, agentId)

	if err := stream.Send(&protos.AgentMessage{Message: &protos.AgentMessage_AgentPing{AgentPing: &protos.AgentPing{AgentId: agentId}}}); err != nil {
		log.Fatalf("Failed to connect to the server: %v", err)
	} else {
		log.Printf("Connected to the server...")
	}

	go func() {
		log.Printf("Starting jupyter kernel")
		cmd := exec.Command("python", "/opt/jupyter/kernel.py")
		//cmd := exec.Command("jupyter/venv/bin/python", "jupyter/kernel.py")
		stdout, err := cmd.StdoutPipe()

		if err != nil {
			fmt.Println("[agent.go] Error creating StdoutPipe:", err)
			return
		}

		// Get stderr pipe
		stderr, err := cmd.StderrPipe()
		if err != nil {
			fmt.Println("[agent.go] Error creating StderrPipe:", err)
			return
		}

		log.Printf("[agent.go] Starting command for execution")
		// Start the command
		if err := cmd.Start(); err != nil {
			fmt.Println("[agent.go] Error starting command:", err)
			return
		}

		// Create channels to read from stdout and stderr
		stdoutScanner := bufio.NewScanner(stdout)
		stderrScanner := bufio.NewScanner(stderr)

		// Stream stdout
		go func() {
			for stdoutScanner.Scan() {
				fmt.Printf("[agent.go] stdout: %s\n", stdoutScanner.Text())
			}
		}()

		// Stream stderr
		go func() {
			for stderrScanner.Scan() {
				fmt.Printf("[agent.go] stderr: %s\n", stderrScanner.Text())
			}
		}()

		// Wait for the command to finish
		if err := cmd.Wait(); err != nil {
			fmt.Println("[agent.go] Error waiting for command:", err)
			return
		}

		fmt.Println("[agent.go] Command finished")
	}()

	go func() {
		for {
			in, err := stream.Recv()
			if err == io.EOF {
				close(grpcStreamChannel)
				return
			}
			if err != nil {
				log.Fatalf("[agent.go] Failed to receive a message : %v", err)
			}
			log.Printf("[agent.go] Received message %s", in.Message)
			switch x := in.GetMessage().(type) {
			case *protos.ServerMessage_PythonExecutionRequest:
				log.Printf("[agent.go] Recived a python execution request")
				executionId := x.PythonExecutionRequest.ExecutionId
				sessionId := x.PythonExecutionRequest.SessionId
				code := x.PythonExecutionRequest.Code
				workingDir := x.PythonExecutionRequest.WorkingDir
				libraries := x.PythonExecutionRequest.Libraries

				log.Printf("[agent.go] Execution id %s", executionId)
				log.Printf("[agent.go] Session id %s", sessionId)
				log.Printf("[agent.go] Code %s", code)
				log.Printf("[agent.go] Working Dir %s", workingDir)
				log.Printf("[agent.go] Libraries %s", libraries)

				go func() {

					// setup the venv
					venvCmd := fmt.Sprintf(`
					agentId="%s"
					pkgs="%s"

					if [ ! -f "/tmp/$agentId/venv" ]; then
						mkdir -p /tmp/$agentId
						python3 -m venv /tmp/$agentId/venv
					fi

					source /tmp/$agentId/venv/bin/activate
					python3 -m pip install $pkgs
					
					`, agentId, strings.Join(libraries, " "))
					log.Println("[agent.go] venv setup:", venvCmd)
					venvExc := exec.Command("bash", "-c", venvCmd)
					venvOut, venvErr := venvExc.CombinedOutput()
					if venvErr != nil {
						fmt.Println("[agent.go] venv setup: ERR", venvErr)
						return
					}
					venvStdout := string(venvOut)
					fmt.Println("[agent.go] venv setup:", venvStdout)

					// execute the python code
					pyCmd := fmt.Sprintf(`
					workingDir="%s";
					agentId="%s";

					cd $workingDir;
					source /tmp/$agentId/venv/bin/activate;
					python3 <<EOF
%s
EOF`, workingDir, agentId, code)
					log.Println("[agent.go] python code:", pyCmd)
					pyExc := exec.Command("bash", "-c", pyCmd)
					pyOut, pyErr := pyExc.CombinedOutput()
					if pyErr != nil {
						fmt.Println("[agent.go] python code: ERR", pyErr)
					}

					// send the result back to the server
					pyStdout := string(pyOut)
					if err := stream.Send(&protos.AgentMessage{Message: &protos.AgentMessage_PythonExecutionResponse{
						PythonExecutionResponse: &protos.PythonExecutionResponse{
							SessionId:      sessionId,
							ExecutionId:    executionId,
							ResponseString: pyStdout}}}); err != nil {
						log.Printf("[agent.go] Failed to send execution result to server: %v", err)
					} else {
						log.Printf("[agent.go] Sent execution result to the server: %v", pyStdout)
					}
				}()

			case *protos.ServerMessage_CommandExecutionRequest:
				log.Printf("[agent.go] Recived a command execution request")
				executionId := x.CommandExecutionRequest.ExecutionId
				execArgs := x.CommandExecutionRequest.Arguments
				log.Printf("[agent.go] Execution id %s", executionId)
				cmd := exec.Command(execArgs[0], execArgs[1:]...)
				log.Printf("[agent.go] Completed execution with the id %s", executionId)
				output, err := cmd.CombinedOutput() // combined output of stdout and stderr
				if err != nil {
					log.Printf("[agent.go] command execution failed: %s", err)
				}

				outputString := string(output)
				log.Printf("[agent.go] Execution output is %s", outputString)

				if err := stream.Send(&protos.AgentMessage{Message: &protos.AgentMessage_CommandExecutionResponse{
					CommandExecutionResponse: &protos.CommandExecutionResponse{ExecutionId: executionId, ResponseString: outputString}}}); err != nil {
					log.Printf("[agent.go] Failed to send execution result to server: %v", err)
				}

			case *protos.ServerMessage_JupyterExecutionRequest:
				log.Printf("[agent.go] Recived a jupyter execution request")
				executionId := x.JupyterExecutionRequest.ExecutionId
				sessionId := x.JupyterExecutionRequest.SessionId
				code := x.JupyterExecutionRequest.Code

				log.Printf("[agent.go] Execution ID: %s, Session ID: %s, Code: %s", executionId, sessionId, code)

				url := "http://127.0.0.1:15000/start"
				client := &http.Client{}
				req, err := http.NewRequest("GET", url, nil)
				if err != nil {
					log.Printf("[agent.go] Failed to create the request start jupyter kernel: %v", err)

					jupyterResponse := "Failed while running the cell in remote. Please retry"
					if err := stream.Send(&protos.AgentMessage{Message: &protos.AgentMessage_JupyterExecutionResponse{
						JupyterExecutionResponse: &protos.JupyterExecutionResponse{ExecutionId: executionId, ResponseString: jupyterResponse, SessionId: sessionId}}}); err != nil {
						log.Printf("[agent.go] Failed to send jupyter execution result to server: %v", err)
					}
					return

				}

				log.Printf("[agent.go] Sending the jupyter kernel start request to server...")
				resp, err := client.Do(req)
				if err != nil {
					log.Printf("[agent.go] Failed to send the request start jupyter kernel: %v", err)

					jupyterResponse := "Failed while running the cell in remote. Please retry"
					if err := stream.Send(&protos.AgentMessage{Message: &protos.AgentMessage_JupyterExecutionResponse{
						JupyterExecutionResponse: &protos.JupyterExecutionResponse{ExecutionId: executionId, ResponseString: jupyterResponse, SessionId: sessionId}}}); err != nil {
						log.Printf("[agent.go] Failed to send jupyter execution result to server: %v", err)
					}
					return

				}
				log.Printf("[agent.go] Successfully sent the jupyter kernel start request to server")

				defer func() {
					err := resp.Body.Close()
					if err != nil {
						log.Printf("[agent.go] Failed to close the response body for kernel start: %v", err)

						jupyterResponse := "Failed while running the cell in remote. Please retry"
						if err := stream.Send(&protos.AgentMessage{Message: &protos.AgentMessage_JupyterExecutionResponse{
							JupyterExecutionResponse: &protos.JupyterExecutionResponse{ExecutionId: executionId, ResponseString: jupyterResponse, SessionId: sessionId}}}); err != nil {
							log.Printf("[agent.go] Failed to send jupyter execution result to server: %v", err)
						}
						return

					}
				}()

				body, err := io.ReadAll(resp.Body)
				if err != nil {
					log.Printf("[agent.go] Failed to read response for start jupyter kernel: %v", err)

					jupyterResponse := "Failed while running the cell in remote. Please retry"
					if err := stream.Send(&protos.AgentMessage{Message: &protos.AgentMessage_JupyterExecutionResponse{
						JupyterExecutionResponse: &protos.JupyterExecutionResponse{ExecutionId: executionId, ResponseString: jupyterResponse, SessionId: sessionId}}}); err != nil {
						log.Printf("[agent.go] Failed to send jupyter execution result to server: %v", err)
					}
					return

				}

				log.Printf("[agent.go] Starting to marshal execution request JSON data...")
				url = "http://127.0.0.1:15000/execute"
				data := map[string]string{
					"code":        code,
					"executionId": executionId,
				}
				jsonData, err := json.Marshal(data)

				if err != nil {
					log.Fatalf("[agent.go] Failed to marshal JSON: %v", err)

					jupyterResponse := "Failed while running the cell in remote. Please retry"
					if err := stream.Send(&protos.AgentMessage{Message: &protos.AgentMessage_JupyterExecutionResponse{
						JupyterExecutionResponse: &protos.JupyterExecutionResponse{ExecutionId: executionId, ResponseString: jupyterResponse, SessionId: sessionId}}}); err != nil {
						log.Printf("[agent.go] Failed to send jupyter execution result to server: %v", err)
					}
					return

				}
				log.Printf("[agent.go] Successful marshaling the JSON data")

				req, err = http.NewRequest("POST", url, bytes.NewBuffer(jsonData))
				if err != nil {
					log.Printf("[agent.go] Failed to create the request run jupyter kernel: %v", err)

					jupyterResponse := "Failed while running the cell in remote. Please retry"
					if err := stream.Send(&protos.AgentMessage{Message: &protos.AgentMessage_JupyterExecutionResponse{
						JupyterExecutionResponse: &protos.JupyterExecutionResponse{ExecutionId: executionId, ResponseString: jupyterResponse, SessionId: sessionId}}}); err != nil {
						log.Printf("[agent.go] Failed to send jupyter execution result to server: %v", err)
					}
					return

				}
				req.Header.Set("Content-Type", "application/json")

				client = &http.Client{}

				resp, err = client.Do(req)
				if err != nil {
					log.Printf("[agent.go] Failed to send the request run jupyter kernel: %v", err)

					jupyterResponse := "Failed while running the cell in remote. Please retry"
					if err := stream.Send(&protos.AgentMessage{Message: &protos.AgentMessage_JupyterExecutionResponse{
						JupyterExecutionResponse: &protos.JupyterExecutionResponse{ExecutionId: executionId, ResponseString: jupyterResponse, SessionId: sessionId}}}); err != nil {
						log.Printf("[agent.go] Failed to send jupyter execution result to server: %v", err)
					}
					return

				}

				defer func() {
					log.Printf("[agent.go] Closing the response...")
					err := resp.Body.Close()
					if err != nil {
						log.Printf("[agent.go] Failed to close the response body for kernel execution: %v", err)

						jupyterResponse := "Failed while running the cell in remote. Please retry"
						if err := stream.Send(&protos.AgentMessage{Message: &protos.AgentMessage_JupyterExecutionResponse{
							JupyterExecutionResponse: &protos.JupyterExecutionResponse{ExecutionId: executionId, ResponseString: jupyterResponse, SessionId: sessionId}}}); err != nil {
							log.Printf("[agent.go] Failed to send jupyter execution result to server: %v", err)
						}
						return

					}
				}()

				log.Printf("[agent.go] Sending the jupyter execution " + executionId + "result to server...")
				body, err = io.ReadAll(resp.Body)
				if err != nil {
					log.Printf("[agent.go] Failed to read response for run jupyter kernel: %v", err)

					jupyterResponse := "Failed while running the cell in remote. Please retry"
					if err := stream.Send(&protos.AgentMessage{Message: &protos.AgentMessage_JupyterExecutionResponse{
						JupyterExecutionResponse: &protos.JupyterExecutionResponse{ExecutionId: executionId, ResponseString: jupyterResponse, SessionId: sessionId}}}); err != nil {
						log.Printf("[agent.go] Failed to send jupyter execution result to server: %v", err)
					}
					return

				}

				jupyterResponse := string(body)
				log.Println("[agent.go] Jupyter execution " + executionId + "response: " + jupyterResponse)

				if err := stream.Send(&protos.AgentMessage{Message: &protos.AgentMessage_JupyterExecutionResponse{
					JupyterExecutionResponse: &protos.JupyterExecutionResponse{ExecutionId: executionId, ResponseString: jupyterResponse, SessionId: sessionId}}}); err != nil {
					log.Printf("[agent.go] Failed to send jupyter execution result to server: %v", err)
				}

			case *protos.ServerMessage_TunnelCreationRequest:
				log.Printf("[agent.go] Received a tunnel creation request")
				host := x.TunnelCreationRequest.DestinationHost
				destPort := x.TunnelCreationRequest.DestinationPort
				srcPort := x.TunnelCreationRequest.SourcePort
				keyPath := x.TunnelCreationRequest.SshKeyPath
				sshUser := x.TunnelCreationRequest.SshUserName
				log.Printf("[agent.go] Tunnel details - Host: %s, DestPort: %s, SrcPort: %s, KeyPath: %s, SSH User: %s", host, destPort, srcPort, keyPath, sshUser)
				openRemoteTunnel(host, destPort, srcPort, sshUser, keyPath)
			}

		}
	}()

	<-grpcStreamChannel
	<-kernelChannel

	if err := stream.CloseSend(); err != nil {
		log.Fatalf("failed to close the stream: %v", err)
	}

}

func openRemoteTunnel(remoteHost string, remotePort string, localPort string, sshUser string, sshKeyFile string) {
	log.Printf("Opening remote SSH tunnel - Remote Host: %s, Remote Port: %s, Local Port: %s", remoteHost, remotePort, localPort)
	// SSH server details
	sshHost := remoteHost + ":22"
	//sshPassword := "your_ssh_password"

	// Remote and local ports
	localHost := "localhost"

	key, err := os.ReadFile(sshKeyFile)
	if err != nil {
		log.Fatalf("unable to read private key: %v", err)
	}

	// Create the Signer for this private key.
	signer, err := ssh.ParsePrivateKey(key)
	if err != nil {
		log.Fatalf("unable to parse private key: %v", err)
	}

	// Create SSH client configuration
	sshConfig := &ssh.ClientConfig{
		User: sshUser,
		Auth: []ssh.AuthMethod{
			//ssh.Password(sshPassword),
			ssh.PublicKeys(signer),
		},
		HostKeyCallback: ssh.InsecureIgnoreHostKey(), // Replace with proper host key verification for production
	}

	log.Println("Connecting to SSH server...")
	// Connect to the SSH server
	sshConn, err := ssh.Dial("tcp", sshHost, sshConfig)
	if err != nil {
		log.Fatalf("Failed to dial SSH: %s", err)
	}
	defer sshConn.Close()
	log.Println("SSH connection established.")

	// Listen on the remote port
	remoteListener, err := sshConn.Listen("tcp", fmt.Sprintf("0.0.0.0:%s", remotePort))
	if err != nil {
		log.Fatalf("Failed to listen on remote port %s: %s", remotePort, err)
	}
	defer remoteListener.Close()

	log.Printf("Reverse SSH tunnel established. Listening on remote port %s", remotePort)

	for {
		remoteConn, err := remoteListener.Accept()
		if err != nil {
			log.Printf("Failed to accept remote connection: %s", err)
			continue
		}

		go handleConnection(remoteConn, localHost, localPort)
	}
}

func handleConnection(remoteConn net.Conn, localHost, localPort string) {
	log.Printf("Handling connection to local host %s:%s", localHost, localPort)
	defer remoteConn.Close()

	// Connect to the local host
	localConn, err := net.Dial("tcp", net.JoinHostPort(localHost, localPort))
	if err != nil {
		log.Printf("Failed to connect to local host %s:%s: %s", localHost, localPort, err)
		return
	}
	defer localConn.Close()

	// Create channels to signal when copying is done
	done := make(chan struct{})

	log.Println("Starting data transfer between remote and local connections...")
	// Start copying data between remote and local connections
	go copyConn(remoteConn, localConn, done)
	go copyConn(localConn, remoteConn, done)

	// Wait for both copy operations to complete
	<-done
	<-done
	log.Println("Data transfer completed.")
}

func copyConn(writer, reader net.Conn, done chan struct{}) {
	defer func() {
		// Signal that copying is done
		done <- struct{}{}
	}()

	_, err := io.Copy(writer, reader)
	if err != nil {
		if err != io.EOF {
			log.Printf("Data copy error: %s", err)
		}
	}
}
