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
	"log"

	"github.com/cyber-shuttle/cybershuttle-tunnels/client"
	frpclient "github.com/fatedier/frp/client"
	"github.com/google/uuid"
)

// dictionary storage for the tunnel
var tunnelErrorStorage = make(map[string]chan error)
var tunnelServerStorage = make(map[string]*frpclient.Service)

func CloseRemoteTunnel(stream Stream, executionId string, tunnelId string) error {
	log.Printf("[agent.go] CloseRemoteTunnel() tunnelId: %s\n", tunnelId)

	status := "Failed"
	if srv, ok := tunnelServerStorage[tunnelId]; ok {
		srv.Close()
		delete(tunnelServerStorage, tunnelId)
		delete(tunnelErrorStorage, tunnelId)
		status = "OK"
	}
	msg := &protos.AgentMessage{
		Message: &protos.AgentMessage_TunnelTerminationResponse{
			TunnelTerminationResponse: &protos.TunnelTerminationResponse{
				ExecutionId: executionId,
				Status:      status,
			},
		},
	}
	if streamErr := stream.Send(msg); streamErr != nil {
		log.Printf("[agent.go] CloseRemoteTunnel() failed to inform the server: %v\n", streamErr)
	}
	return nil
}

func OpenRemoteTunnel(stream Stream, executionId string, localBindHost string, localPort int32,
	tunnelServerHost string, tunnelServerPort int32, tunnelServerApiUrl string, tunnelServerToken string) error {

	clientConfig := &client.ClientConfig{}
	clientConfig.AgentID = executionId
	clientConfig.LocalIP = localBindHost
	clientConfig.LocalPort = int(localPort)
	clientConfig.Transport.Protocol = "tcp"
	clientConfig.Transport.BandwidthLimitMode = "client"
	clientConfig.ServerAddr = tunnelServerHost
	clientConfig.ServerPort = int(tunnelServerPort)
	clientConfig.ServerAPI = tunnelServerApiUrl
	clientConfig.Auth.Method = "token"
	clientConfig.Auth.Token = tunnelServerToken
	clientConfig.Log.Level = "info"
	clientConfig.Log.To = "console"

	err, port, chanErr, srv := client.RunClientInternal(clientConfig)

	tunnelId := uuid.New().String()

	if err == nil {
		tunnelErrorStorage[tunnelId] = chanErr
		tunnelServerStorage[tunnelId] = srv
		log.Printf("[agent.go] OpenRemoteTunnel() tunnelId: %s\n", tunnelId)
		msg := &protos.AgentMessage{
			Message: &protos.AgentMessage_TunnelCreationResponse{
				TunnelCreationResponse: &protos.TunnelCreationResponse{
					ExecutionId: executionId,
					Status:      "OK",
					TunnelHost:  tunnelServerHost,
					TunnelPort:  int32(port),
					TunnelId:    tunnelId,
				},
			},
		}
		if streamErr := stream.Send(msg); streamErr != nil {
			log.Printf("[agent.go] OpenRemoteTunnel() failed to inform the server: %v\n", streamErr)
		}
	} else {
		log.Printf("[agent.go] OpenRemoteTunnel() failed to open tunnel: %v\n", err)
		msg := &protos.AgentMessage{
			Message: &protos.AgentMessage_TunnelCreationResponse{
				TunnelCreationResponse: &protos.TunnelCreationResponse{
					ExecutionId: executionId,
					Status:      "ERROR: " + err.Error(),
					TunnelHost:  tunnelServerHost,
					TunnelPort:  int32(port),
					TunnelId:    tunnelId,
				},
			},
		}
		if streamErr := stream.Send(msg); streamErr != nil {
			log.Printf("[agent.go] OpenRemoteTunnel() failed to inform the server: %v\n", streamErr)
		}
	}

	return err
}
