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

// Package server wires the verticals together into one HTTP handler.
package server

import (
	"net/http"
	"strings"

	"github.com/apache/airavata/internal/app"
	"github.com/apache/airavata/internal/auth"
	"github.com/apache/airavata/internal/config"
	"github.com/apache/airavata/internal/httpx"

	applicationctl "github.com/apache/airavata/api/application/controller"
	computectl "github.com/apache/airavata/api/compute/controller"
	credentialsctl "github.com/apache/airavata/api/credentials/controller"
	datactl "github.com/apache/airavata/api/data/controller"
	iamctl "github.com/apache/airavata/api/iam/controller"
	processctl "github.com/apache/airavata/api/process/controller"
	sharingctl "github.com/apache/airavata/api/sharing/controller"
)

// New builds the fully wired HTTP handler over an already-assembled object graph.
//
// Routing only: the services come from internal/app, which builds them once so the
// workflow worker can be handed the same ones rather than constructing a second set
// over the same database.
func New(cfg config.Config, svcs *app.Services, introspector auth.Introspector) http.Handler {
	mux := http.NewServeMux()
	iamctl.NewUserController(svcs.User).Register(mux)
	iamctl.NewGroupController(svcs.Group).Register(mux)
	iamctl.NewGroupMemberController(svcs.GroupMember).Register(mux)
	credentialsctl.NewSSHKeyController(svcs.SSHKey).Register(mux)
	computectl.NewSlurmClusterController(svcs.SlurmCluster).Register(mux)
	computectl.NewClusterPartitionController(svcs.ClusterPartition).Register(mux)
	computectl.NewSlurmClusterConfigController(svcs.SlurmClusterConfig).Register(mux)
	applicationctl.NewTemplateController(svcs.Template).Register(mux)
	applicationctl.NewBatchDeploymentController(svcs.BatchDeployment).Register(mux)
	datactl.NewSCPDataStorageController(svcs.SCPDataStorage).Register(mux)
	datactl.NewDataProductController(svcs.DataProduct).Register(mux)
	datactl.NewVirtualDataDirectoryController(svcs.VirtualDataDirectory).Register(mux)
	datactl.NewVirtualDataFileController(svcs.VirtualDataFile).Register(mux)
	// One sharing controller per shareable record, all four over the same service type
	// and the same table. Adding a shareable record adds a line here, not a vertical's
	// worth of sharing code.
	sharingctl.New(svcs.SCPDataStorageSharing, "/api/v1/scp-data-storages", "dataStorageId").Register(mux)
	sharingctl.New(svcs.DataProductSharing, "/api/v1/data-products", "dataProductId").Register(mux)
	sharingctl.New(svcs.VirtualDataDirectorySharing, "/api/v1/virtual-data-directories", "virtualDataDirectoryId").Register(mux)
	sharingctl.New(svcs.SlurmClusterConfigSharing, "/api/v1/slurm-cluster-configs", "slurmClusterConfigId").Register(mux)
	processctl.NewProcessController(svcs.Process).Register(mux)
	processctl.NewLaunchController(svcs.Launch).Register(mux)
	processctl.NewStatusController(svcs.ProcessStatus).Register(mux)
	processctl.NewDataStagingTaskController(svcs.DataStagingTask).Register(mux)
	processctl.NewJobSubmissionTaskController(svcs.JobSubmissionTask).Register(mux)
	processctl.NewJobMonitoringTaskController(svcs.JobMonitoringTask).Register(mux)
	processctl.NewInteractiveCommandTaskController(svcs.InteractiveCommandTask).Register(mux)

	mux.HandleFunc("GET /health", func(w http.ResponseWriter, _ *http.Request) {
		httpx.WriteJSON(w, http.StatusOK, map[string]string{"status": "UP"})
	})

	// Outermost first: CORS answers preflights before authentication runs, since a
	// preflight carries no credentials by definition.
	return cors(cfg.CORSAllowedOrigins)(auth.Middleware(introspector)(mux))
}

// cors applies the airavata.cors.allowed-origins policy.
func cors(allowed []string) func(http.Handler) http.Handler {
	allowAll := false
	origins := make(map[string]bool, len(allowed))
	for _, o := range allowed {
		o = strings.TrimSpace(o)
		if o == "*" {
			allowAll = true
		}
		if o != "" {
			origins[o] = true
		}
	}

	return func(next http.Handler) http.Handler {
		return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
			origin := r.Header.Get("Origin")
			switch {
			case origin == "":
				// Not a cross-origin request.
			case allowAll:
				// Echo the caller's origin rather than "*". A wildcard cannot be
				// combined with credentialed requests, and every useful call here
				// carries an Authorization header.
				w.Header().Set("Access-Control-Allow-Origin", origin)
				w.Header().Add("Vary", "Origin")
			case origins[origin]:
				w.Header().Set("Access-Control-Allow-Origin", origin)
				w.Header().Add("Vary", "Origin")
			}

			if origin != "" && (allowAll || origins[origin]) {
				w.Header().Set("Access-Control-Allow-Credentials", "true")
				w.Header().Set("Access-Control-Allow-Headers", "Authorization, Content-Type")
				w.Header().Set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
			}

			if r.Method == http.MethodOptions {
				w.WriteHeader(http.StatusNoContent)
				return
			}
			next.ServeHTTP(w, r)
		})
	}
}
