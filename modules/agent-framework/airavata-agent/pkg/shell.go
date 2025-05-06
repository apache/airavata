package pkg

import (
	protos "airavata-agent/protos"
	"bufio"
	"log"
	"os/exec"
	"strings"
)

type ProcessInfo struct {
	execArgs []string
}

var shellPidMap = make(map[int]ProcessInfo)

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

func ListAsyncProcesses(stream Stream, executionId string) {
	log.Printf("[agent.go] ListAsyncProcesses() Execution id %s\n", executionId)

	asyncCommandList := []*protos.AsyncCommand{}

	for pid, processInfo := range shellPidMap {
		asyncCommandList = append(asyncCommandList, &protos.AsyncCommand{ProcessId: int32(pid), Arguments: processInfo.execArgs})
	}
	msg := &protos.AgentMessage{
		Message: &protos.AgentMessage_AsyncCommandListResponse{
			AsyncCommandListResponse: &protos.AsyncCommandListResponse{
				ExecutionId: executionId,
				Commands:    asyncCommandList,
			},
		},
	}

	if err := stream.Send(msg); err != nil {
		log.Printf("[agent.go] ListAsyncProcesses() Failed to send process list to server: %v\n", err)
	} else {
		log.Printf("[agent.go] ListAsyncProcesses() Sent process list to server: %v\n", asyncCommandList)
	}
}

func KillAsyncProcess(stream Stream, executionId string, processId int32) {

	log.Printf("[agent.go] KillAsyncProcess() Execution id %s\n", executionId)
	log.Printf("[agent.go] KillAsyncProcess() Process id %d\n", processId)

	status := "OK"
	if _, exists := shellPidMap[int(processId)]; exists {
		cmd := exec.Command("kill", "-9", string(processId))
		if err := cmd.Run(); err != nil {
			log.Printf("[agent.go] KillAsyncProcess() Error killing process: %v\n", err)
			status = "ERROR: " + err.Error()
		} else {
			log.Printf("[agent.go] KillAsyncProcess() Successfully killed process with PID %d\n", processId)
		}
		delete(shellPidMap, int(processId))
	} else {
		log.Printf("[agent.go] KillAsyncProcess() Process with PID %d not found\n", processId)
		status = "ERROR: Process not found"
	}

	msg := &protos.AgentMessage{
		Message: &protos.AgentMessage_AsyncCommandTerminateResponse{
			AsyncCommandTerminateResponse: &protos.AsyncCommandTerminateResponse{
				ExecutionId: executionId,
				Status:      status,
			},
		},
	}

	if err := stream.Send(msg); err != nil {
		log.Printf("[agent.go] KillAsyncProcess() Failed to send termination result to server: %v\n", err)
	} else {
		log.Printf("[agent.go] KillAsyncProcess() Sent termination result to server for process id %d\n", processId)
	}
}

func ExecuteShellAsync(stream Stream, executionId string, envName string, workingDir string, execArgs []string) {
	log.Printf("[agent.go] ExecuteShellAsync() Execution id %s\n", executionId)
	log.Printf("[agent.go] ExecuteShellAsync() Env name %s\n", envName)
	log.Printf("[agent.go] ExecuteShellAsync() Exec args %s\n", execArgs)

	cmd := exec.Command("micromamba", "run", "-n", envName, "bash", "-c", strings.Join(execArgs, " "))
	cmd.Dir = workingDir

	stdout, err := cmd.StdoutPipe()
	if err != nil {
		log.Printf("[agent.go] ExecuteShellAsync() Error creating StdoutPipe for cmd: %v\n", err)
		msg := &protos.AgentMessage{
			Message: &protos.AgentMessage_AsyncCommandExecutionResponse{
				AsyncCommandExecutionResponse: &protos.AsyncCommandExecutionResponse{
					ExecutionId:  executionId,
					ProcessId:    int32(cmd.Process.Pid),
					ErrorMessage: "Error creating StdoutPipe for cmd: " + err.Error(),
				},
			},
		}
		stream.Send(msg)
		return
	}
	stderr, err := cmd.StderrPipe()
	if err != nil {
		log.Printf("[agent.go] ExecuteShellAsync() Error creating StderrPipe for cmd: %v\n", err)
		msg := &protos.AgentMessage{
			Message: &protos.AgentMessage_AsyncCommandExecutionResponse{
				AsyncCommandExecutionResponse: &protos.AsyncCommandExecutionResponse{
					ExecutionId:  executionId,
					ProcessId:    int32(cmd.Process.Pid),
					ErrorMessage: "Error creating StderrPipe for cmd: " + err.Error(),
				},
			},
		}
		stream.Send(msg)
		return
	}
	if err := cmd.Start(); err != nil {
		log.Printf("[agent.go] ExecuteShellAsync() Error during start: %v\n", err)
		msg := &protos.AgentMessage{
			Message: &protos.AgentMessage_AsyncCommandExecutionResponse{
				AsyncCommandExecutionResponse: &protos.AsyncCommandExecutionResponse{
					ExecutionId:  executionId,
					ProcessId:    int32(cmd.Process.Pid),
					ErrorMessage: "Error during start: " + err.Error(),
				},
			},
		}
		stream.Send(msg)
		return
	}
	log.Printf("[agent.go] ExecuteShellAsync() Started python server.\n")
	go func() {
		stdoutScanner := bufio.NewScanner(stdout)
		for stdoutScanner.Scan() {
			log.Printf("[agent.go] ExecuteShellAsync() stdout: %s\n", stdoutScanner.Text())
		}
	}()
	go func() {
		stderrScanner := bufio.NewScanner(stderr)
		for stderrScanner.Scan() {
			log.Printf("[agent.go] ExecuteShellAsync() stderr: %s\n", stderrScanner.Text())
		}
	}()
	go func() {
		if err := cmd.Wait(); err != nil {
			log.Printf("[agent.go] ExecuteShellAsync() Error waiting for command: %v\n", err)
		}
	}()
	log.Printf("[agent.go] startJupyterKernel() Command finished.\n")
	shellPidMap[cmd.Process.Pid] = ProcessInfo{
		execArgs: execArgs,
	}
	log.Printf("[agent.go] ExecuteShellAsync() Process ID: %d\n", cmd.Process.Pid)

	msg := &protos.AgentMessage{
		Message: &protos.AgentMessage_AsyncCommandExecutionResponse{
			AsyncCommandExecutionResponse: &protos.AsyncCommandExecutionResponse{
				ExecutionId: executionId,
				ProcessId:   int32(cmd.Process.Pid),
			},
		},
	}

	if err := stream.Send(msg); err != nil {
		log.Printf("[agent.go] ExecuteShellAsync() Failed to send execution result to server: %v\n", err)
	} else {
		log.Printf("[agent.go] ExecuteShellAsync() Sent execution result to server with process id %d\n", cmd.Process.Pid)
	}
}
