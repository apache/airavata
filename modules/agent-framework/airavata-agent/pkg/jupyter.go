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

package pkg

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
	"time"
)

var pidMap = make(map[string]int)

func CreateEnv(stream Stream, executionId string, envName string, envLibs []string, envPip []string) {
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
	pidMap[envName] = StartJupyterKernel(envName)
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

func StartJupyterKernel(envName string) int {
	log.Printf("[agent.go] startJupyterKernel() Starting python server in env: %s...\n", envName)
	// Create temp file for unix socket
	log.Printf("[agent.go] startJupyterKernel() creating unix socket...\n")
	tmpFile, err := os.CreateTemp(os.TempDir(), "kernel-*.sock")
	if err != nil {
		log.Fatalf("[agent.go] startJupyterKernel() Failed to create unix socket: %v\n", err)
	}
	log.Printf("[agent.go] startJupyterKernel() created unix socket: %s\n", tmpFile.Name())
	defer tmpFile.Close()
	os.Setenv("KERNEL_SOCK", tmpFile.Name())
	// Run command
	cmd := exec.Command("micromamba", "run", "-n", envName, "python", "kernel.py")
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

func ExecuteJupyter(stream Stream, executionId string, envName string, code string) {
	if _, exists := pidMap[envName]; !exists {
		log.Printf("[agent.go] executeJupyter() Starting python server in env: %s...\n", envName)
		pidMap[envName] = StartJupyterKernel(envName)
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
	fmt.Printf("Response size: %d bytes\n", len(jupyterResponse))

	sendResponse(jupyterResponse, nil)
}

func RestartKernel(stream Stream, executionId string, envName string) {
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
	pidMap[envName] = StartJupyterKernel(envName)
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
