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

	"github.com/apache/airavata/internal/auth"
	ssh "github.com/apache/airavata/internal/ssh"
)

type SCPTransport struct {
	sshContext *ssh.GlobalSSHContext
}

func NewSCPTransport(sshContext *ssh.GlobalSSHContext) *SCPTransport {
	return &SCPTransport{sshContext: sshContext}
}

func (t *SCPTransport) Download(ctx context.Context, ep Endpoint, remotePath, localPath string) error {

	userID, err := userIDFrom(ctx)
	if err != nil {
		return err
	}
	dialConfig := ssh.NewSSHDialConfig(ep.Host, ep.Port, ep.LoginUser, ep.Key, userID)
	return ssh.DownloadFileFromSCP(ctx, dialConfig, t.sshContext, remotePath, localPath)
}

func (t *SCPTransport) Upload(ctx context.Context, ep Endpoint, localPath, remotePath string) error {
	userID, err := userIDFrom(ctx)
	if err != nil {
		return err
	}
	dialConfig := ssh.NewSSHDialConfig(ep.Host, ep.Port, ep.LoginUser, ep.Key, userID)
	return ssh.UploadFileToSCP(ctx, dialConfig, t.sshContext, localPath, remotePath)
}

func (t *SCPTransport) MakeDirectory(ctx context.Context, ep Endpoint, remoteDir string) error {
	userID, err := userIDFrom(ctx)
	if err != nil {
		return err
	}
	dialConfig := ssh.NewSSHDialConfig(ep.Host, ep.Port, ep.LoginUser, ep.Key, userID)
	return ssh.CreateDirectorySSH(ctx, dialConfig, t.sshContext, remoteDir)
}

func userIDFrom(ctx context.Context) (string, error) {
	p, err := auth.RequireAuthenticated(ctx)
	if err != nil {
		return "", err
	}
	return p.Name, nil
}
