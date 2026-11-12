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
	"github.com/apache/airavata/internal/link"

	applicationrepo "github.com/apache/airavata/api/application/repository"
	applicationsvc "github.com/apache/airavata/api/application/service"
	auditrepo "github.com/apache/airavata/api/audit/repository"
	auditsvc "github.com/apache/airavata/api/audit/service"
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
	sharingrepo "github.com/apache/airavata/api/sharing/repository"
	sharingsvc "github.com/apache/airavata/api/sharing/service"
	orchestration "github.com/apache/airavata/internal/orchestration"
	"github.com/apache/airavata/internal/ssh"
	"github.com/cschleiden/go-workflows/backend"
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

	// Permissions resolves roles to the permissions they grant. The HTTP middleware
	// resolves each caller through it, and whatever changes role_permissions calls
	// Invalidate on it so the change takes effect at once.
	Permissions *iamrepo.PermissionResolver

	// Audit log.
	Audit *auditsvc.EventService

	// IAM.
	User        *iamsvc.UserService
	Group       *iamsvc.GroupService
	GroupMember *iamsvc.GroupMemberService
	// ServiceAccount administers non-human users and their expiring tokens.
	ServiceAccount *iamsvc.ServiceAccountService

	// Credentials.
	SSHCredential   *credentialssvc.SSHCredentialService
	InteractiveAuth *credentialssvc.InteractiveAuthService

	// Compute.
	SlurmCluster              *computesvc.SlurmClusterService
	ClusterPartition          *computesvc.ClusterPartitionService
	SlurmClusterConfig        *computesvc.SlurmClusterConfigService
	SlurmClusterConfigSharing *sharingsvc.Service
	ClusterDiscovery          *computesvc.ClusterDiscoveryService

	// Application catalogue.
	Template        *applicationsvc.TemplateService
	BatchDeployment *applicationsvc.BatchDeploymentService

	// Data.
	SCPDataStorage              *datasvc.SCPDataStorageService
	SCPDataStorageSharing       *sharingsvc.Service
	DataProduct                 *datasvc.DataProductService
	DataProductSharing          *sharingsvc.Service
	DataTransfer                *datasvc.DataTransferService
	VirtualDataDirectory        *datasvc.VirtualDataDirectoryService
	VirtualDataFile             *datasvc.VirtualDataFileService
	VirtualDataDirectorySharing *sharingsvc.Service

	// Processes. There is no batch process service: a batch process is a section of
	// the process that owns it, written and read through ProcessService.
	Process                *processsvc.ProcessService
	ProcessStatus          *processsvc.StatusService
	TaskStatus             *processsvc.TaskStatusService
	ProcessWorkflow        *processsvc.WorkflowService
	DataStagingTask        *processsvc.DataStagingTaskService
	JobSubmissionTask      *processsvc.JobSubmissionTaskService
	JobMonitoringTask      *processsvc.JobMonitoringTaskService
	InteractiveCommandTask *processsvc.InteractiveCommandTaskService

	// Launch is a higher-level service that orchestrates the other services to submit
	// a process and its tasks, and to monitor the job until it completes.
	Launch *processsvc.LaunchService
	Link   *processsvc.LinkService

	ExecutionEngine *orchestration.ExecutionEngine
}

type Repositories struct {
	AuditEvents             *auditrepo.EventRepository
	Users                   *iamrepo.UserRepository
	Groups                  *iamrepo.GroupRepository
	GroupMembers            *iamrepo.GroupMemberRepository
	ServiceAccounts         *iamrepo.ServiceAccountRepository
	SSHCredentials          *credentialsrepo.SSHCredentialRepository
	SlurmClusters           *computerepo.SlurmClusterRepository
	ClusterPartitions       *computerepo.ClusterPartitionRepository
	SlurmClusterConfigs     *computerepo.SlurmClusterConfigRepository
	Templates               *applicationrepo.TemplateRepository
	BatchDeployments        *applicationrepo.BatchDeploymentRepository
	SCPDataStorages         *datarepo.SCPDataStorageRepository
	DataProducts            *datarepo.DataProductRepository
	VirtualDataDirectories  *datarepo.VirtualDataDirectoryRepository
	VirtualDataFiles        *datarepo.VirtualDataFileRepository
	Shares                  *sharingrepo.Repository
	Processes               *processrepo.ProcessRepository
	Statuses                *processrepo.StatusRepository
	TaskStatuses            *processrepo.TaskStatusRepository
	ProcessWorkflows        *processrepo.WorkflowRepository
	BatchJobStatuses        *processrepo.BatchJobStatusRepository
	DataStagingTasks        *processrepo.DataStagingTaskRepository
	JobSubmissionTasks      *processrepo.JobSubmissionTaskRepository
	JobMonitoringTasks      *processrepo.JobMonitoringTaskRepository
	InteractiveCommandTasks *processrepo.InteractiveCommandTaskRepository
	Links                   *processrepo.LinkRepository
}

func NewRepositories(db *gorm.DB) *Repositories {
	return &Repositories{
		Users:                   iamrepo.NewUserRepository(db),
		Groups:                  iamrepo.NewGroupRepository(db),
		GroupMembers:            iamrepo.NewGroupMemberRepository(db),
		ServiceAccounts:         iamrepo.NewServiceAccountRepository(db),
		AuditEvents:             auditrepo.NewEventRepository(db),
		SSHCredentials:          credentialsrepo.NewSSHCredentialRepository(db),
		SlurmClusters:           computerepo.NewSlurmClusterRepository(db),
		ClusterPartitions:       computerepo.NewClusterPartitionRepository(db),
		SlurmClusterConfigs:     computerepo.NewSlurmClusterConfigRepository(db),
		Templates:               applicationrepo.NewTemplateRepository(db),
		BatchDeployments:        applicationrepo.NewBatchDeploymentRepository(db),
		SCPDataStorages:         datarepo.NewSCPDataStorageRepository(db),
		DataProducts:            datarepo.NewDataProductRepository(db),
		VirtualDataDirectories:  datarepo.NewVirtualDataDirectoryRepository(db),
		VirtualDataFiles:        datarepo.NewVirtualDataFileRepository(db),
		Shares:                  sharingrepo.NewRepository(db),
		Processes:               processrepo.NewProcessRepository(db),
		Statuses:                processrepo.NewStatusRepository(db),
		TaskStatuses:            processrepo.NewTaskStatusRepository(db),
		ProcessWorkflows:        processrepo.NewWorkflowRepository(db),
		BatchJobStatuses:        processrepo.NewBatchJobStatusRepository(db),
		DataStagingTasks:        processrepo.NewDataStagingTaskRepository(db),
		JobSubmissionTasks:      processrepo.NewJobSubmissionTaskRepository(db),
		JobMonitoringTasks:      processrepo.NewJobMonitoringTaskRepository(db),
		InteractiveCommandTasks: processrepo.NewInteractiveCommandTaskRepository(db),
		Links:                   processrepo.NewLinkRepository(db),
	}
}

// New builds every service over db.
//
// The repositories stay local: they are an implementation detail of the services above
// them, and nothing outside this function has ever wanted one directly.
func NewServices(cfg config.Config, db *gorm.DB, repos *Repositories, workflowBackend backend.Backend) *Services {
	// Repositories.
	users := repos.Users
	groups := repos.Groups
	groupMembers := repos.GroupMembers
	sshCredentials := repos.SSHCredentials
	clusters := repos.SlurmClusters
	partitions := repos.ClusterPartitions
	clusterConfigs := repos.SlurmClusterConfigs
	templates := repos.Templates
	deployments := repos.BatchDeployments
	storages := repos.SCPDataStorages
	shares := repos.Shares
	products := repos.DataProducts
	virtualDirs := repos.VirtualDataDirectories
	virtualFiles := repos.VirtualDataFiles
	processes := repos.Processes
	statuses := repos.Statuses
	stagingTasks := repos.DataStagingTasks
	submissionTasks := repos.JobSubmissionTasks
	monitoringTasks := repos.JobMonitoringTasks
	commandTasks := repos.InteractiveCommandTasks
	batchStatus := repos.BatchJobStatuses
	processWorkflows := repos.ProcessWorkflows

	statusSvc := processsvc.NewStatusService(db, statuses, processes)
	processWorkflowSvc := processsvc.NewWorkflowService(db, processWorkflows, processes)
	configAccess := computesvc.NewConfigAccess(clusterConfigs, shares, groupMembers)
	keyAccess := credentialssvc.NewKeyAccess(sshCredentials)
	processSvs := processsvc.NewProcessService(db, processes, deployments, configAccess, users, statusSvc)
	batchDeploymentSvc := applicationsvc.NewBatchDeploymentService(db, deployments, templates, clusters)
	templateSvc := applicationsvc.NewTemplateService(db, templates, deployments)
	slurmClusterSvc := computesvc.NewSlurmClusterService(db, clusters, partitions, clusterConfigs)
	slurmClusterConfigSvc := computesvc.NewSlurmClusterConfigService(db, clusterConfigs, shares, clusters, keyAccess, users, groupMembers)
	slurmClusterConfigSharingSvc := computesvc.NewSlurmClusterConfigSharingService(db, clusterConfigs, shares, groups, users, groupMembers)
	clusterPartitionSvc := computesvc.NewClusterPartitionService(db, partitions, clusters)
	scpDataStorageSvc := datasvc.NewSCPDataStorageService(db, storages, shares, keyAccess, products, users, groupMembers)
	scpDataStorageSharingSvc := datasvc.NewSCPDataStorageSharingService(db, storages, shares, groups, users, groupMembers)
	dataProductSvc := datasvc.NewDataProductService(db, products, shares, storages, users, groupMembers)
	dataProductSharingSvc := datasvc.NewDataProductSharingService(db, products, shares, groups, users, groupMembers)

	sshContext := ssh.NewGlobalSSHContext()

	dataTransferSvc := datasvc.NewDataTransferService(db, products, shares, storages, groupMembers, datasvc.NewSCPTransport(sshContext))
	virtualDataDirectorySvc := datasvc.NewVirtualDataDirectoryService(db, virtualDirs, virtualFiles, shares, products, users, groupMembers)
	virtualDataFileSvc := datasvc.NewVirtualDataFileService(db, virtualDirs, virtualFiles, shares, products, groupMembers)
	virtualDataDirectorySharingSvc := datasvc.NewVirtualDataDirectorySharingService(db, virtualDirs, shares, groups, users, groupMembers)
	sshCredentialSvc := credentialssvc.NewSSHCredentialService(sshCredentials, users, clusterConfigs, storages)

	linkSvc := processsvc.NewLinkService(repos.Links, processes, link.NewRegistry(), cfg.PublicURL)

	executionEngine := orchestration.NewExecutionEngine(stagingTasks, submissionTasks,
		monitoringTasks, storages, clusterConfigs, processes, deployments, templates, batchStatus, statuses, repos.TaskStatuses, processWorkflows, workflowBackend, sshContext, linkSvc)
	executionEngine.StartEngine()

	return &Services{
		Config: cfg,
		DB:     db,

		Permissions: iamrepo.NewPermissionResolver(db, iamrepo.DefaultPermissionTTL),

		Audit: auditsvc.NewEventService(repos.AuditEvents),

		User:        iamsvc.NewUserService(db, users),
		Group:       iamsvc.NewGroupService(db, groups, groupMembers, users, shares),
		GroupMember: iamsvc.NewGroupMemberService(db, groups, groupMembers, users),
		// A deleted service account takes the shares granted to it along, hence the
		// sharing repository.
		ServiceAccount: iamsvc.NewServiceAccountService(db, repos.ServiceAccounts, users, shares),

		// A key is deleted only when nothing presents it, and what can present one
		// lives in other verticals — hence both repositories here.
		SSHCredential:   sshCredentialSvc,
		InteractiveAuth: credentialssvc.NewInteractiveAuthService(sshContext),

		SlurmCluster:              slurmClusterSvc,
		ClusterPartition:          clusterPartitionSvc,
		SlurmClusterConfig:        slurmClusterConfigSvc,
		SlurmClusterConfigSharing: slurmClusterConfigSharingSvc,
		ClusterDiscovery:          computesvc.NewClusterDiscoveryService(keyAccess, configAccess, sshContext),

		Template:        templateSvc,
		BatchDeployment: batchDeploymentSvc,

		SCPDataStorage:        scpDataStorageSvc,
		SCPDataStorageSharing: scpDataStorageSharingSvc,
		DataProduct:           dataProductSvc,
		DataProductSharing:    dataProductSharingSvc,
		DataTransfer:          dataTransferSvc,

		VirtualDataDirectory:        virtualDataDirectorySvc,
		VirtualDataFile:             virtualDataFileSvc,
		VirtualDataDirectorySharing: virtualDataDirectorySharingSvc,

		Process:                processSvs,
		ProcessStatus:          statusSvc,
		TaskStatus:             processsvc.NewTaskStatusService(repos.TaskStatuses, processes),
		ProcessWorkflow:        processWorkflowSvc,
		DataStagingTask:        processsvc.NewDataStagingTaskService(db, stagingTasks, processes),
		JobSubmissionTask:      processsvc.NewJobSubmissionTaskService(db, submissionTasks, processes),
		JobMonitoringTask:      processsvc.NewJobMonitoringTaskService(db, monitoringTasks, processes),
		InteractiveCommandTask: processsvc.NewInteractiveCommandTaskService(db, commandTasks, processes),

		Launch: processsvc.NewLaunchService(db, processSvs, batchDeploymentSvc,
			templateSvc, slurmClusterSvc, slurmClusterConfigSvc,
			sshCredentialSvc, scpDataStorageSvc, dataProductSvc, statusSvc, stagingTasks, submissionTasks,
			monitoringTasks, commandTasks, processWorkflows, executionEngine),

		Link: linkSvc,

		ExecutionEngine: executionEngine,
	}
}
