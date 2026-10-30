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

// InteractiveAuthResponse is a keyboard-interactive challenge waiting for its
// owner to answer: where the connection is going and what the host asked.
//
// The prompts are the host's questions only. Answers are never stored on anything a
// read can reach.
type InteractiveAuthResponse struct {
	AuthID          string       `json:"authId"`
	HostName        string       `json:"hostName"`
	Port            int          `json:"port"`
	LoginUser       string       `json:"loginUser"`
	SSHCredentialID string       `json:"sshCredentialId"`
	Banner          string       `json:"banner"`
	Name            string       `json:"name"`
	Instruction     string       `json:"instruction"`
	Prompts         []ssh.Prompt `json:"prompts"`
	CreatedAt       time.Time    `json:"createdAt"`
}

func ToInteractiveAuthResponse(s *ssh.SSHInteractiveAuth) InteractiveAuthResponse {
	host, port, loginUser, credentialID := s.Target()
	prompts, instruction := s.Read()
	if prompts == nil {
		prompts = []ssh.Prompt{}
	}
	return InteractiveAuthResponse{
		AuthID:          s.AuthId,
		HostName:        host,
		Port:            port,
		LoginUser:       loginUser,
		SSHCredentialID: credentialID,
		Banner:          s.Banner(),
		Name:            s.Name(),
		Instruction:     instruction,
		Prompts:         prompts,
		CreatedAt:       s.CreatedAt,
	}
}

// InteractiveAuthMessage is what the server sends on an interactive auth's
// websocket. Type is one of:
//
//   - "challenge": the first message, carrying the auth and its prompts
//   - "accepted":  the answer was handed to the SSH handshake; the socket then closes
//   - "error":     Message says what was wrong; a bad answer can be resent
type InteractiveAuthMessage struct {
	Type    string                   `json:"type"`
	Auth    *InteractiveAuthResponse `json:"auth,omitempty"`
	Message string                   `json:"message,omitempty"`
}

// InteractiveAuthAnswer is what the client sends back: one response per prompt, in
// prompt order, or Cancel to fail the login rather than leave it waiting.
type InteractiveAuthAnswer struct {
	Responses []string `json:"responses"`
	Cancel    bool     `json:"cancel"`
}
