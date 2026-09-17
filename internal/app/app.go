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
	orchestration "github.com/apache/airavata/internal/orchestration"
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
	SSHKey *credentialssvc.SSHKeyService

	// Compute.
	SlurmCluster              *computesvc.SlurmClusterService
	ClusterPartition          *computesvc.ClusterPartitionService
	SlurmClusterConfig        *computesvc.SlurmClusterConfigService
	SlurmClusterConfigSharing *computesvc.SlurmClusterConfigSharingService

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

	// Launch is a higher-level service that orchestrates the other services to submit
	// a process and its tasks, and to monitor the job until it completes.
	Launch *processsvc.LaunchService

	ExecutionEngine *orchestration.ExecutionEngine
}

type Repositories struct {
	Users                    *iamrepo.UserRepository
	Groups                   *iamrepo.GroupRepository
	GroupMembers             *iamrepo.GroupMemberRepository
	SSHKeys                  *credentialsrepo.SSHKeyRepository
	SlurmClusters            *computerepo.SlurmClusterRepository
	ClusterPartitions        *computerepo.ClusterPartitionRepository
	SlurmClusterConfigs      *computerepo.SlurmClusterConfigRepository
	SlurmClusterConfigShares *computerepo.SlurmClusterConfigSharingRepository
	Templates                *applicationrepo.TemplateRepository
	BatchDeployments         *applicationrepo.BatchDeploymentRepository
	SCPDataStorages          *datarepo.SCPDataStorageRepository
	SCPDataStorageShares     *datarepo.SCPDataStorageSharingRepository
	DataProducts             *datarepo.DataProductRepository
	DataProductShares        *datarepo.DataProductSharingRepository
	Processes                *processrepo.ProcessRepository
	Statuses                 *processrepo.StatusRepository
	DataStagingTasks         *processrepo.DataStagingTaskRepository
	JobSubmissionTasks       *processrepo.JobSubmissionTaskRepository
	JobMonitoringTasks       *processrepo.JobMonitoringTaskRepository
	InteractiveCommandTasks  *processrepo.InteractiveCommandTaskRepository
}

func NewRepositories(db *gorm.DB) *Repositories {
	return &Repositories{
		Users:                    iamrepo.NewUserRepository(db),
		Groups:                   iamrepo.NewGroupRepository(db),
		GroupMembers:             iamrepo.NewGroupMemberRepository(db),
		SSHKeys:                  credentialsrepo.NewSSHKeyRepository(db),
		SlurmClusters:            computerepo.NewSlurmClusterRepository(db),
		ClusterPartitions:        computerepo.NewClusterPartitionRepository(db),
		SlurmClusterConfigs:      computerepo.NewSlurmClusterConfigRepository(db),
		SlurmClusterConfigShares: computerepo.NewSlurmClusterConfigSharingRepository(db),
		Templates:                applicationrepo.NewTemplateRepository(db),
		BatchDeployments:         applicationrepo.NewBatchDeploymentRepository(db),
		SCPDataStorages:          datarepo.NewSCPDataStorageRepository(db),
		SCPDataStorageShares:     datarepo.NewSCPDataStorageSharingRepository(db),
		DataProducts:             datarepo.NewDataProductRepository(db),
		DataProductShares:        datarepo.NewDataProductSharingRepository(db),
		Processes:                processrepo.NewProcessRepository(db),
		Statuses:                 processrepo.NewStatusRepository(db),
		DataStagingTasks:         processrepo.NewDataStagingTaskRepository(db),
		JobSubmissionTasks:       processrepo.NewJobSubmissionTaskRepository(db),
		JobMonitoringTasks:       processrepo.NewJobMonitoringTaskRepository(db),
		InteractiveCommandTasks:  processrepo.NewInteractiveCommandTaskRepository(db),
	}
}

// New builds every service over db.
//
// The repositories stay local: they are an implementation detail of the services above
// them, and nothing outside this function has ever wanted one directly.
func NewServices(cfg config.Config, db *gorm.DB, repos *Repositories) *Services {
	// Repositories.
	users := repos.Users
	groups := repos.Groups
	groupMembers := repos.GroupMembers
	sshKeys := repos.SSHKeys
	clusters := repos.SlurmClusters
	partitions := repos.ClusterPartitions
	clusterConfigs := repos.SlurmClusterConfigs
	clusterConfigShares := repos.SlurmClusterConfigShares
	templates := repos.Templates
	deployments := repos.BatchDeployments
	storages := repos.SCPDataStorages
	storageShares := repos.SCPDataStorageShares
	products := repos.DataProducts
	productShares := repos.DataProductShares
	processes := repos.Processes
	statuses := repos.Statuses
	stagingTasks := repos.DataStagingTasks
	submissionTasks := repos.JobSubmissionTasks
	monitoringTasks := repos.JobMonitoringTasks
	commandTasks := repos.InteractiveCommandTasks

	statusSvc := processsvc.NewStatusService(db, statuses, processes)
	configAccess := computesvc.NewConfigAccess(clusterConfigs, clusterConfigShares, groupMembers)
	keyAccess := credentialssvc.NewKeyAccess(sshKeys)
	processSvs := processsvc.NewProcessService(db, processes, deployments, configAccess, users, statusSvc)
	batchDeploymentSvc := applicationsvc.NewBatchDeploymentService(db, deployments, templates, clusters)
	templateSvc := applicationsvc.NewTemplateService(db, templates, deployments)
	slurmClusterSvc := computesvc.NewSlurmClusterService(db, clusters, partitions, clusterConfigs)
	slurmClusterConfigSvc := computesvc.NewSlurmClusterConfigService(db, clusterConfigs, clusterConfigShares, clusters, keyAccess, users, groupMembers)
	slurmClusterConfigSharingSvc := computesvc.NewSlurmClusterConfigSharingService(db, clusterConfigs, clusterConfigShares, groups, users, groupMembers)
	clusterPartitionSvc := computesvc.NewClusterPartitionService(db, partitions, clusters)
	scpDataStorageSvc := datasvc.NewSCPDataStorageService(db, storages, storageShares, keyAccess, products, users, groupMembers)
	scpDataStorageSharingSvc := datasvc.NewSCPDataStorageSharingService(db, storages, storageShares, groups, users, groupMembers)
	dataProductSvc := datasvc.NewDataProductService(db, products, productShares, storages, storageShares, users, groupMembers)
	dataProductSharingSvc := datasvc.NewDataProductSharingService(db, products, productShares, groups, users, groupMembers)
	sshKeySvc := credentialssvc.NewSSHKeyService(sshKeys, users, clusterConfigs, storages)

	executionEngine := orchestration.NewExecutionEngine(stagingTasks, submissionTasks, monitoringTasks, storages, clusterConfigs, processes, deployments, templates)
	executionEngine.StartEngine()

	return &Services{
		Config: cfg,
		DB:     db,

		User:        iamsvc.NewUserService(db, users),
		Group:       iamsvc.NewGroupService(db, groups, groupMembers, users),
		GroupMember: iamsvc.NewGroupMemberService(db, groups, groupMembers, users),

		// A key is deleted only when nothing presents it, and what can present one
		// lives in other verticals — hence both repositories here.
		SSHKey: sshKeySvc,

		SlurmCluster:              slurmClusterSvc,
		ClusterPartition:          clusterPartitionSvc,
		SlurmClusterConfig:        slurmClusterConfigSvc,
		SlurmClusterConfigSharing: slurmClusterConfigSharingSvc,

		Template:        templateSvc,
		BatchDeployment: batchDeploymentSvc,

		SCPDataStorage:        scpDataStorageSvc,
		SCPDataStorageSharing: scpDataStorageSharingSvc,
		DataProduct:           dataProductSvc,
		DataProductSharing:    dataProductSharingSvc,

		Process:                processSvs,
		ProcessStatus:          statusSvc,
		DataStagingTask:        processsvc.NewDataStagingTaskService(db, stagingTasks, processes),
		JobSubmissionTask:      processsvc.NewJobSubmissionTaskService(db, submissionTasks, processes),
		JobMonitoringTask:      processsvc.NewJobMonitoringTaskService(db, monitoringTasks, processes),
		InteractiveCommandTask: processsvc.NewInteractiveCommandTaskService(db, commandTasks, processes),

		Launch: processsvc.NewLaunchService(db, processSvs, batchDeploymentSvc,
			templateSvc, slurmClusterSvc, slurmClusterConfigSvc,
			sshKeySvc, scpDataStorageSvc, dataProductSvc, stagingTasks, submissionTasks,
			monitoringTasks, commandTasks, executionEngine),

		ExecutionEngine: executionEngine,
	}
}
