/**
*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
 */

package dto

import (
	"time"

	"github.com/apache/airavata/internal/ssh"
)

// InteractiveSessionResponse is a keyboard-interactive challenge waiting for its
// owner to answer: where the connection is going and what the host asked.
//
// The prompts are the host's questions only. Answers are never stored on anything a
// read can reach.
type InteractiveSessionResponse struct {
	SessionID       string       `json:"sessionId"`
	HostName        string       `json:"hostName"`
	Port            int          `json:"port"`
	LoginUser       string       `json:"loginUser"`
	SSHCredentialID string       `json:"sshCredentialId"`
	Name            string       `json:"name"`
	Instruction     string       `json:"instruction"`
	Prompts         []ssh.Prompt `json:"prompts"`
	CreatedAt       time.Time    `json:"createdAt"`
}

func ToInteractiveSessionResponse(s *ssh.SSHInteractiveSession) InteractiveSessionResponse {
	host, port, loginUser, credentialID := s.Target()
	prompts, instruction := s.Read()
	if prompts == nil {
		prompts = []ssh.Prompt{}
	}
	return InteractiveSessionResponse{
		SessionID:       s.SessionId,
		HostName:        host,
		Port:            port,
		LoginUser:       loginUser,
		SSHCredentialID: credentialID,
		Name:            s.Name(),
		Instruction:     instruction,
		Prompts:         prompts,
		CreatedAt:       s.CreatedAt,
	}
}
