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

// Package transport holds the connections Airavata opens to clusters and jobs: SSH to
// a cluster's headnode, and the link a job's Linkspan dials back.
package transport

import (
	computemodel "github.com/apache/airavata/api/compute/model"
	ssh "github.com/apache/airavata/internal/ssh"
)

// SSHDialConfig dials the config's headnode as its login user, on behalf of userID.
func SSHDialConfig(config *computemodel.SlurmClusterConfig, userID string) ssh.SSHDialConfig {
	return ssh.NewSSHDialConfig(config.SlurmCluster.HeadnodeHost, config.SlurmCluster.HeadnodePort, config.LoginUser, *config.SSHCredential, userID)
}
