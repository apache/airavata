package pkg

import (
	protos "airavata-agent/protos"
	"log"
	"os/exec"
)

func ExecutePython(stream Stream, executionId string, envName string, workingDir string, code string) {
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
