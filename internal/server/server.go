// Package server wires the verticals together into one HTTP handler.
package server

import (
	"net/http"
	"strings"

	"gorm.io/gorm"

	"github.com/apache/airavata/internal/auth"
	"github.com/apache/airavata/internal/config"
	"github.com/apache/airavata/internal/httpx"

	applicationctl "github.com/apache/airavata/api/application/controller"
	applicationrepo "github.com/apache/airavata/api/application/repository"
	applicationsvc "github.com/apache/airavata/api/application/service"
	computectl "github.com/apache/airavata/api/compute/controller"
	computerepo "github.com/apache/airavata/api/compute/repository"
	computesvc "github.com/apache/airavata/api/compute/service"
	credentialsctl "github.com/apache/airavata/api/credentials/controller"
	credentialsrepo "github.com/apache/airavata/api/credentials/repository"
	credentialssvc "github.com/apache/airavata/api/credentials/service"
	datactl "github.com/apache/airavata/api/data/controller"
	datarepo "github.com/apache/airavata/api/data/repository"
	datasvc "github.com/apache/airavata/api/data/service"
	iamctl "github.com/apache/airavata/api/iam/controller"
	iamrepo "github.com/apache/airavata/api/iam/repository"
	iamsvc "github.com/apache/airavata/api/iam/service"
	processctl "github.com/apache/airavata/api/process/controller"
	processrepo "github.com/apache/airavata/api/process/repository"
	processsvc "github.com/apache/airavata/api/process/service"
)

// New builds the fully wired HTTP handler.
//
// Construction is explicit rather than reflective: every repository, service and
// handler is assembled here, so the dependency graph the Java service expressed
// through component scanning is readable in one place.
func New(cfg config.Config, db *gorm.DB, introspector auth.Introspector) http.Handler {
	// Repositories.
	users := iamrepo.NewUserRepository(db)
	groups := iamrepo.NewGroupRepository(db)
	groupMembers := iamrepo.NewGroupMemberRepository(db)
	sshKeys := credentialsrepo.NewSSHKeyRepository(db)
	sshCreds := credentialsrepo.NewSSHUserCredentialRepository(db)
	endpoints := computerepo.NewSSHEndpointRepository(db)
	clusters := computerepo.NewClusterRepository(db)
	partitions := computerepo.NewClusterPartitionRepository(db)
	bindings := computerepo.NewSSHEndpointCredentialRepository(db)
	bindingShares := computerepo.NewSSHEndpointCredentialSharingRepository(db)
	templates := applicationrepo.NewTemplateRepository(db)
	deployments := applicationrepo.NewBatchDeploymentRepository(db)
	storages := datarepo.NewSCPDataStorageRepository(db)
	storageShares := datarepo.NewSCPDataStorageSharingRepository(db)
	products := datarepo.NewDataProductRepository(db)
	productShares := datarepo.NewDataProductSharingRepository(db)
	processes := processrepo.NewProcessRepository(db)
	statuses := processrepo.NewStatusRepository(db)
	stagingTasks := processrepo.NewDataStagingTaskRepository(db)
	submissionTasks := processrepo.NewJobSubmissionTaskRepository(db)
	monitoringTasks := processrepo.NewJobMonitoringTaskRepository(db)
	commandTasks := processrepo.NewInteractiveCommandTaskRepository(db)

	// Services.
	userSvc := iamsvc.NewUserService(db, users)
	groupSvc := iamsvc.NewGroupService(db, groups, groupMembers, users)
	groupMemberSvc := iamsvc.NewGroupMemberService(db, groups, groupMembers, users)
	sshKeySvc := credentialssvc.NewSSHKeyService(db, sshKeys, sshCreds)
	sshCredSvc := credentialssvc.NewSSHUserCredentialService(db, sshCreds, sshKeys)
	endpointSvc := computesvc.NewSSHEndpointService(db, endpoints, clusters, bindings)
	clusterSvc := computesvc.NewClusterService(db, clusters, endpoints)
	partitionSvc := computesvc.NewClusterPartitionService(db, partitions, clusters)
	bindingSvc := computesvc.NewSSHEndpointCredentialService(db, bindings, bindingShares, endpoints, sshCreds, users, groupMembers)
	bindingShareSvc := computesvc.NewSSHEndpointCredentialSharingService(db, bindings, bindingShares, groups, users, groupMembers)
	templateSvc := applicationsvc.NewTemplateService(db, templates, deployments)
	deploymentSvc := applicationsvc.NewBatchDeploymentService(db, deployments, templates, clusters, bindings)
	bindingAccess := computesvc.NewCredentialAccess(bindings, bindingShares, groupMembers)
	storageSvc := datasvc.NewSCPDataStorageService(db, storages, storageShares, endpoints, products, users, groupMembers)
	storageShareSvc := datasvc.NewSCPDataStorageSharingService(db, storages, storageShares, groups, users, groupMembers)
	productSvc := datasvc.NewDataProductService(db, products, productShares, storages, storageShares, bindingAccess, users, groupMembers)
	productShareSvc := datasvc.NewDataProductSharingService(db, products, productShares, groups, users, groupMembers)
	statusSvc := processsvc.NewStatusService(db, statuses, processes)
	processSvc := processsvc.NewProcessService(db, processes, deployments, users, statusSvc)
	stagingTaskSvc := processsvc.NewDataStagingTaskService(db, stagingTasks, processes)
	submissionTaskSvc := processsvc.NewJobSubmissionTaskService(db, submissionTasks, processes)
	monitoringTaskSvc := processsvc.NewJobMonitoringTaskService(db, monitoringTasks, processes)
	commandTaskSvc := processsvc.NewInteractiveCommandTaskService(db, commandTasks, processes)

	mux := http.NewServeMux()
	iamctl.NewUserController(userSvc).Register(mux)
	iamctl.NewGroupController(groupSvc).Register(mux)
	iamctl.NewGroupMemberController(groupMemberSvc).Register(mux)
	credentialsctl.NewSSHKeyController(sshKeySvc).Register(mux)
	credentialsctl.NewSSHUserCredentialController(sshCredSvc).Register(mux)
	computectl.NewSSHEndpointController(endpointSvc).Register(mux)
	computectl.NewClusterController(clusterSvc).Register(mux)
	computectl.NewClusterPartitionController(partitionSvc).Register(mux)
	computectl.NewSSHEndpointCredentialController(bindingSvc).Register(mux)
	computectl.NewSSHEndpointCredentialSharingController(bindingShareSvc).Register(mux)
	applicationctl.NewTemplateController(templateSvc).Register(mux)
	applicationctl.NewBatchDeploymentController(deploymentSvc).Register(mux)
	datactl.NewSCPDataStorageController(storageSvc).Register(mux)
	datactl.NewSCPDataStorageSharingController(storageShareSvc).Register(mux)
	datactl.NewDataProductController(productSvc).Register(mux)
	datactl.NewDataProductSharingController(productShareSvc).Register(mux)
	processctl.NewProcessController(processSvc).Register(mux)
	processctl.NewStatusController(statusSvc).Register(mux)
	processctl.NewDataStagingTaskController(stagingTaskSvc).Register(mux)
	processctl.NewJobSubmissionTaskController(submissionTaskSvc).Register(mux)
	processctl.NewJobMonitoringTaskController(monitoringTaskSvc).Register(mux)
	processctl.NewInteractiveCommandTaskController(commandTaskSvc).Register(mux)

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
