// Package server wires the verticals together into one HTTP handler.
package server

import (
	"net/http"
	"strings"

	"gorm.io/gorm"

	"github.com/apache/airavata/api/application"
	"github.com/apache/airavata/api/compute"
	"github.com/apache/airavata/api/credentials"
	"github.com/apache/airavata/api/iam"
	"github.com/apache/airavata/api/process"
	"github.com/apache/airavata/internal/auth"
	"github.com/apache/airavata/internal/config"
	"github.com/apache/airavata/internal/httpx"
)

// New builds the fully wired HTTP handler.
//
// Construction is explicit rather than reflective: every repository, service and
// handler is assembled here, so the dependency graph the Java service expressed
// through component scanning is readable in one place.
func New(cfg config.Config, db *gorm.DB, introspector auth.Introspector) http.Handler {
	// Repositories.
	users := iam.NewUserRepository(db)
	groups := iam.NewGroupRepository(db)
	groupMembers := iam.NewGroupMemberRepository(db)
	sshKeys := credentials.NewSSHKeyRepository(db)
	sshCreds := credentials.NewSSHUserCredentialRepository(db)
	clusters := compute.NewClusterRepository(db)
	partitions := compute.NewClusterPartitionRepository(db)
	bindings := compute.NewClusterCredentialRepository(db)
	templates := application.NewTemplateRepository(db)
	deployments := application.NewBatchDeploymentRepository(db)
	processes := process.NewRepository(db)
	statuses := process.NewStatusRepository(db)

	// Services.
	userSvc := iam.NewService(db, users)
	groupSvc := iam.NewGroupService(db, groups, groupMembers, users)
	groupMemberSvc := iam.NewGroupMemberService(db, groups, groupMembers, users)
	sshKeySvc := credentials.NewSSHKeyService(db, sshKeys, sshCreds)
	sshCredSvc := credentials.NewSSHUserCredentialService(db, sshCreds, sshKeys)
	clusterSvc := compute.NewClusterService(db, clusters)
	partitionSvc := compute.NewClusterPartitionService(db, partitions, clusters)
	bindingSvc := compute.NewClusterCredentialService(db, bindings, clusters, sshCreds, users)
	templateSvc := application.NewTemplateService(db, templates, deployments)
	deploymentSvc := application.NewBatchDeploymentService(db, deployments, templates, clusters, bindings)
	statusSvc := process.NewStatusService(db, statuses, processes)
	processSvc := process.NewService(db, processes, deployments, users, statusSvc)

	mux := http.NewServeMux()
	iam.NewController(userSvc).Register(mux)
	iam.NewGroupController(groupSvc).Register(mux)
	iam.NewGroupMemberController(groupMemberSvc).Register(mux)
	credentials.NewSSHKeyController(sshKeySvc).Register(mux)
	credentials.NewSSHUserCredentialController(sshCredSvc).Register(mux)
	compute.NewClusterController(clusterSvc).Register(mux)
	compute.NewClusterPartitionController(partitionSvc).Register(mux)
	compute.NewClusterCredentialController(bindingSvc).Register(mux)
	application.NewTemplateController(templateSvc).Register(mux)
	application.NewBatchDeploymentController(deploymentSvc).Register(mux)
	process.NewController(processSvc).Register(mux)
	process.NewStatusController(statusSvc).Register(mux)

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
