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
	credentialsctl.NewSSHUserCredentialController(svcs.SSHUserCredential).Register(mux)
	computectl.NewSSHEndpointController(svcs.SSHEndpoint).Register(mux)
	computectl.NewClusterController(svcs.Cluster).Register(mux)
	computectl.NewClusterPartitionController(svcs.ClusterPartition).Register(mux)
	computectl.NewSSHEndpointCredentialController(svcs.SSHEndpointCredential).Register(mux)
	computectl.NewSSHEndpointCredentialSharingController(svcs.SSHEndpointCredentialSharing).Register(mux)
	applicationctl.NewTemplateController(svcs.Template).Register(mux)
	applicationctl.NewBatchDeploymentController(svcs.BatchDeployment).Register(mux)
	datactl.NewSCPDataStorageController(svcs.SCPDataStorage).Register(mux)
	datactl.NewSCPDataStorageSharingController(svcs.SCPDataStorageSharing).Register(mux)
	datactl.NewDataProductController(svcs.DataProduct).Register(mux)
	datactl.NewDataProductSharingController(svcs.DataProductSharing).Register(mux)
	processctl.NewProcessController(svcs.Process).Register(mux)
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
