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

package service

import (
	"context"

	ssh "github.com/apache/airavata/internal/ssh"
)

type SCPTransport struct{}

// NewSCPTransport returns the transport used outside tests.
func NewSCPTransport() SCPTransport { return SCPTransport{} }

func (SCPTransport) Download(ctx context.Context, ep Endpoint, remotePath, localPath string) error {

	dialConfig := ssh.SSHDialConfig{
		Host:     ep.Host,
		Port:     ep.Port,
		Username: ep.LoginUser,
		Cred:     ep.Key,
	}
	return ssh.DownloadFileFromSCP(ctx, dialConfig, remotePath, localPath)
}

func (SCPTransport) Upload(ctx context.Context, ep Endpoint, localPath, remotePath string) error {
	dialConfig := ssh.SSHDialConfig{
		Host:     ep.Host,
		Port:     ep.Port,
		Username: ep.LoginUser,
		Cred:     ep.Key,
	}
	return ssh.UploadFileToSCP(ctx, dialConfig, localPath, remotePath)
}

func (SCPTransport) MakeDirectory(ctx context.Context, ep Endpoint, remoteDir string) error {
	dialConfig := ssh.SSHDialConfig{
		Host:     ep.Host,
		Port:     ep.Port,
		Username: ep.LoginUser,
		Cred:     ep.Key,
	}
	return ssh.CreateDirectorySSH(ctx, dialConfig, remoteDir)
}
