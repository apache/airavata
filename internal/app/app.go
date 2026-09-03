// Package app assembles the object graph every entry point shares.
//
// Construction is explicit rather than reflective: every repository and service is
// built here, so the dependency graph the Java service expressed through component
// scanning is readable in one place. What this package adds over doing it inside the
// HTTP handler is a second caller — the workflow worker needs the same services the
// controllers do, and neither should be reaching for a package-level singleton to find
// them. The graph is built once per database and handed to whoever needs it.
package app

import (
	"gorm.io/gorm"

	"github.com/apache/airavata/internal/config"

	applicationrepo "github.com/apache/airavata/api/application/repository"
	applicationsvc "github.com/apache/airavata/api/application/service"
	computerepo "github.com/apache/airavata/api/compute/repository"
	computesvc "github.com/apache/airavata/api/compute/service"
	credentialsrepo "github.com/apache/airavata/api/credentials/repository"
	credentialssvc "github.com/apache/airavata/api/credentials/service"
	datarepo "github.com/apache/airavata/api/data/repository"
	datasvc "github.com/apache/airavata/api/data/service"
	iamrepo "github.com/apache/airavata/api/iam/repository"
	iamsvc "github.com/apache/airavata/api/iam/service"
	processrepo "github.com/apache/airavata/api/process/repository"
	processsvc "github.com/apache/airavata/api/process/service"
)

// Services is every service the application exposes, built over one database.
//
// The fields are exported and named after what they manage rather than after their
// types, so a caller reads Svcs.Process, not a type assertion out of a container.
type Services struct {
	// Config and DB are carried so a caller that needs to open a transaction of its
	// own — or read what it was configured with — does not have to be handed them
	// separately alongside this struct.
	Config config.Config
	DB     *gorm.DB

	// IAM.
	User        *iamsvc.UserService
	Group       *iamsvc.GroupService
	GroupMember *iamsvc.GroupMemberService

	// Credentials.
	SSHKey                       *credentialssvc.SSHKeyService
	SSHUserCredential            *credentialssvc.SSHUserCredentialService
	SSHEndpoint                  *credentialssvc.SSHEndpointService
	SSHEndpointCredential        *credentialssvc.SSHEndpointCredentialService
	SSHEndpointCredentialSharing *credentialssvc.SSHEndpointCredentialSharingService
	CredentialAccess             *credentialssvc.CredentialAccess

	// Compute.
	Cluster          *computesvc.ClusterService
	ClusterPartition *computesvc.ClusterPartitionService

	// Application catalogue.
	Template        *applicationsvc.TemplateService
	BatchDeployment *applicationsvc.BatchDeploymentService

	// Data.
	SCPDataStorage        *datasvc.SCPDataStorageService
	SCPDataStorageSharing *datasvc.SCPDataStorageSharingService
	DataProduct           *datasvc.DataProductService
	DataProductSharing    *datasvc.DataProductSharingService

	// Processes. There is no batch process service: a batch process is a section of
	// the process that owns it, written and read through ProcessService.
	Process                *processsvc.ProcessService
	ProcessStatus          *processsvc.StatusService
	DataStagingTask        *processsvc.DataStagingTaskService
	JobSubmissionTask      *processsvc.JobSubmissionTaskService
	JobMonitoringTask      *processsvc.JobMonitoringTaskService
	InteractiveCommandTask *processsvc.InteractiveCommandTaskService
}

// New builds every service over db.
//
// The repositories stay local: they are an implementation detail of the services above
// them, and nothing outside this function has ever wanted one directly.
func New(cfg config.Config, db *gorm.DB) *Services {
	// Repositories.
	users := iamrepo.NewUserRepository(db)
	groups := iamrepo.NewGroupRepository(db)
	groupMembers := iamrepo.NewGroupMemberRepository(db)
	sshKeys := credentialsrepo.NewSSHKeyRepository(db)
	sshCreds := credentialsrepo.NewSSHUserCredentialRepository(db)
	endpoints := credentialsrepo.NewSSHEndpointRepository(db)
	bindings := credentialsrepo.NewSSHEndpointCredentialRepository(db)
	bindingShares := credentialsrepo.NewSSHEndpointCredentialSharingRepository(db)
	clusters := computerepo.NewClusterRepository(db)
	partitions := computerepo.NewClusterPartitionRepository(db)
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

	// Two services are shared by others below, so they are built first rather than
	// twice: StatusService because submitting a process records its first status in
	// the same transaction, and CredentialAccess because data products resolve
	// access to their storage's submission credential through it.
	statusSvc := processsvc.NewStatusService(db, statuses, processes)
	bindingAccess := credentialssvc.NewCredentialAccess(bindings, bindingShares, groupMembers)

	return &Services{
		Config: cfg,
		DB:     db,

		User:        iamsvc.NewUserService(db, users),
		Group:       iamsvc.NewGroupService(db, groups, groupMembers, users),
		GroupMember: iamsvc.NewGroupMemberService(db, groups, groupMembers, users),

		SSHKey:                       credentialssvc.NewSSHKeyService(db, sshKeys, sshCreds),
		SSHUserCredential:            credentialssvc.NewSSHUserCredentialService(db, sshCreds, sshKeys),
		SSHEndpoint:                  credentialssvc.NewSSHEndpointService(db, endpoints, clusters, bindings),
		SSHEndpointCredential:        credentialssvc.NewSSHEndpointCredentialService(db, bindings, bindingShares, endpoints, sshCreds, users, groupMembers),
		SSHEndpointCredentialSharing: credentialssvc.NewSSHEndpointCredentialSharingService(db, bindings, bindingShares, groups, users, groupMembers),
		CredentialAccess:             bindingAccess,

		Cluster:          computesvc.NewClusterService(db, clusters, partitions, endpoints),
		ClusterPartition: computesvc.NewClusterPartitionService(db, partitions, clusters),

		Template:        applicationsvc.NewTemplateService(db, templates, deployments),
		BatchDeployment: applicationsvc.NewBatchDeploymentService(db, deployments, templates, clusters),

		SCPDataStorage:        datasvc.NewSCPDataStorageService(db, storages, storageShares, endpoints, products, users, groupMembers),
		SCPDataStorageSharing: datasvc.NewSCPDataStorageSharingService(db, storages, storageShares, groups, users, groupMembers),
		DataProduct:           datasvc.NewDataProductService(db, products, productShares, storages, storageShares, bindingAccess, users, groupMembers),
		DataProductSharing:    datasvc.NewDataProductSharingService(db, products, productShares, groups, users, groupMembers),

		Process:                processsvc.NewProcessService(db, processes, deployments, bindingAccess, users, statusSvc),
		ProcessStatus:          statusSvc,
		DataStagingTask:        processsvc.NewDataStagingTaskService(db, stagingTasks, processes),
		JobSubmissionTask:      processsvc.NewJobSubmissionTaskService(db, submissionTasks, processes),
		JobMonitoringTask:      processsvc.NewJobMonitoringTaskService(db, monitoringTasks, processes),
		InteractiveCommandTask: processsvc.NewInteractiveCommandTaskService(db, commandTasks, processes),
	}
}
