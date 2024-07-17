package main

import (
	"context"
	"fmt"
	"io"
	"log"
	"net"
	"os"
	"os/exec"

	protos "airavata-agent/protos"

	"golang.org/x/crypto/ssh"
	"google.golang.org/grpc"
)

func main() {

	args := os.Args[1:]
	serverUrl := args[0]
	agentId := args[1]
	grpcStreamChannel := make(chan struct{})

	conn, err := grpc.Dial(serverUrl, grpc.WithInsecure(), grpc.WithBlock())
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
		for {
			in, err := stream.Recv()
			if err == io.EOF {
				close(grpcStreamChannel)
				return
			}
			if err != nil {
				log.Fatalf("Failed to receive a message : %v", err)
			}
			log.Printf("Received message %s", in.Message)
			switch x := in.GetMessage().(type) {
			case *protos.ServerMessage_CommandExecutionRequest:
				log.Printf("Recived a command execution request")
				executionId := x.CommandExecutionRequest.ExecutionId
				execArgs := x.CommandExecutionRequest.Arguments
				log.Printf("Execution id %s", executionId)
				cmd := exec.Command(execArgs[0], execArgs[1:]...)
				stdout, err := cmd.Output()
				if err != nil {
					log.Fatalf(err.Error())
					return
				}
				log.Printf("Execution output is %s", string(stdout))

			case *protos.ServerMessage_TunnelCreationRequest:
				log.Printf("Received a tunnel creation request")
				host := x.TunnelCreationRequest.DestinationHost
				destPort := x.TunnelCreationRequest.DestinationPort
				srcPort := x.TunnelCreationRequest.SourcePort
				keyPath := x.TunnelCreationRequest.SshKeyPath
				sshUser := x.TunnelCreationRequest.SshUserName
				openRemoteTunnel(host, destPort, srcPort, sshUser, keyPath)
			}

		}
	}()

	<-grpcStreamChannel

	if err := stream.CloseSend(); err != nil {
		log.Fatalf("failed to close the stream: %v", err)
	}

}

func openRemoteTunnel(remoteHost string, remotePort string, localPort string, sshUser string, sshKeyFile string) {
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

	// Connect to the SSH server
	sshConn, err := ssh.Dial("tcp", sshHost, sshConfig)
	if err != nil {
		log.Fatalf("Failed to dial SSH: %s", err)
	}
	defer sshConn.Close()

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

	// Start copying data between remote and local connections
	go copyConn(remoteConn, localConn, done)
	go copyConn(localConn, remoteConn, done)

	// Wait for both copy operations to complete
	<-done
	<-done
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