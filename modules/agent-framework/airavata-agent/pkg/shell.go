package pkg

import (
	protos "airavata-agent/protos"
	"log"
	"os/exec"
	"strings"
)

func ExecuteShell(stream Stream, executionId string, envName string, workingDir string, execArgs []string) {
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
