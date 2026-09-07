package service

import (
	"context"
	"strings"

	"github.com/apache/airavata/api/process/repository"
	"gorm.io/gorm"

	"fmt"

	"github.com/apache/airavata/internal/auth"
	"github.com/apache/airavata/internal/httpx"

	appdto "github.com/apache/airavata/api/application/dto"
	appmod "github.com/apache/airavata/api/application/model"
	appserv "github.com/apache/airavata/api/application/service"
	computesev "github.com/apache/airavata/api/compute/service"
	credentialsev "github.com/apache/airavata/api/credentials/service"
	datasev "github.com/apache/airavata/api/data/service"

	datamodel "github.com/apache/airavata/api/data/model"
	"github.com/apache/airavata/api/process/dto"
	procmodel "github.com/apache/airavata/api/process/model"
)

type LaunchService struct {
	db                     *gorm.DB
	processService         *ProcessService
	batchDeploymentService *appserv.BatchDeploymentService
	templateService        *appserv.TemplateService
	clusterService         *computesev.SlurmClusterService
	clusterConfigService   *computesev.SlurmClusterConfigService
	sshKeyService          *credentialsev.SSHKeyService
	scpDataStorageService  *datasev.SCPDataStorageService
	dataProductService     *datasev.DataProductService

	dataStagingTasks   *repository.DataStagingTaskRepository
	jobSubmissionTasks *repository.JobSubmissionTaskRepository
	monitoringTasks    *repository.JobMonitoringTaskRepository
	interactiveTasks   *repository.InteractiveCommandTaskRepository
}

// NewLaunchService returns a launch service.
func NewLaunchService(
	db *gorm.DB,
	processService *ProcessService,
	batchDeploymentService *appserv.BatchDeploymentService,
	templateService *appserv.TemplateService,
	clusterService *computesev.SlurmClusterService,
	clusterConfigService *computesev.SlurmClusterConfigService,
	sshKeyService *credentialsev.SSHKeyService,
	scpDataStorageService *datasev.SCPDataStorageService,
	dataProductService *datasev.DataProductService,

	dataStagingTasks *repository.DataStagingTaskRepository,
	jobSubmissionTasks *repository.JobSubmissionTaskRepository,
	monitoringTasks *repository.JobMonitoringTaskRepository,
	interactiveTasks *repository.InteractiveCommandTaskRepository,
) *LaunchService {
	return &LaunchService{
		db:                     db,
		processService:         processService,
		batchDeploymentService: batchDeploymentService,
		templateService:        templateService,
		clusterService:         clusterService,
		clusterConfigService:   clusterConfigService,
		sshKeyService:          sshKeyService,
		scpDataStorageService:  scpDataStorageService,
		dataProductService:     dataProductService,
		dataStagingTasks:       dataStagingTasks,
		jobSubmissionTasks:     jobSubmissionTasks,
		monitoringTasks:        monitoringTasks,
		interactiveTasks:       interactiveTasks,
	}
}

// LaunchProcess turns a submitted process into the tasks that will carry it out.
//
// Owner-scoped, like the task routes and unlike the process reads: launching acts on a
// host under the identity the run was submitted with, so it is the owner's to trigger.
// The references it walks — the cluster config, each data product — are authorised in
// turn by their own services, so a run cannot reach a config or a dataset its owner has
// no standing on either.
//
// Launching is not idempotent: it writes a task per staged file plus a submission and a
// monitoring task, so a second call would double them. A process that already carries
// tasks is refused rather than launched again.
func (s *LaunchService) LaunchProcess(ctx context.Context, processID string) (*dto.Response, error) {
	proc, err := s.processService.Get(ctx, processID)
	if err != nil {
		return nil, notFoundAs(err, "Process not found: %s", processID)
	}
	if err := s.requireOwnership(ctx, proc); err != nil {
		return nil, err
	}
	if err := s.requireNotLaunched(ctx, proc.ProcessID); err != nil {
		return nil, err
	}

	if proc.ProcessType == nil {
		return nil, fmt.Errorf("Process %s has no process type", proc.ProcessID)
	}

	if *proc.ProcessType == procmodel.ProcessTypeBatchJob {
		if err := s.launchBatchProcess(ctx, proc); err != nil {
			return nil, err
		}
	}

	return proc, nil
}

// requireOwnership allows the run's owner and platform admins. A process with no owner
// belongs to nobody, so it must not match the empty principal name.
func (s *LaunchService) requireOwnership(ctx context.Context, proc *dto.Response) error {
	principal, err := auth.RequireAuthenticated(ctx)
	if err != nil {
		return err
	}
	owned := proc.UserID != nil && *proc.UserID == principal.Name
	if !owned && !principal.IsAdmin() {
		return httpx.Forbidden("Access denied: you may only launch your own processes")
	}
	return nil
}

// requireNotLaunched reports a conflict when a process already carries the tasks a
// launch creates. It is the guard that keeps a retried request from staging every input
// twice and submitting the job twice over.
func (s *LaunchService) requireNotLaunched(ctx context.Context, processID string) error {
	staging, err := s.dataStagingTasks.FindByProcessID(ctx, processID)
	if err != nil {
		return err
	}
	submissions, err := s.jobSubmissionTasks.FindByProcessID(ctx, processID)
	if err != nil {
		return err
	}
	if len(staging)+len(submissions) > 0 {
		return httpx.Conflict("Process %s has already been launched", processID)
	}
	return nil
}

func (s *LaunchService) launchBatchProcess(ctx context.Context, process *dto.Response) error {
	// Implementation for launching a batch process goes here
	batchProcess := process.BatchProcess

	if batchProcess == nil {
		return fmt.Errorf("Process %s has no batch process", process.ProcessID)
	}

	// Each of these is optional on the record it comes from, so it is checked before it
	// is followed rather than dereferenced. A run whose deployment names no cluster, or
	// whose deployment is missing outright, cannot be launched — a conflict with the
	// state it was submitted in, not a fault in the request that asked.
	if batchProcess.DeploymentID == nil {
		return httpx.Conflict("Batch process %s names no deployment", batchProcess.BatchProcessID)
	}

	batchDeployment, err := s.batchDeploymentService.Get(ctx, *batchProcess.DeploymentID)
	if err != nil {
		return fmt.Errorf("Failed to get batch deployment %s: %v", *batchProcess.DeploymentID, err)
	}

	if batchDeployment == nil {
		return fmt.Errorf("Batch process %s has no deployment", batchProcess.BatchProcessID)
	}

	if batchDeployment.TemplateID == nil {
		return httpx.Conflict("Deployment %s names no application template", batchDeployment.DeploymentID)
	}

	template, err := s.templateService.Get(ctx, *batchDeployment.TemplateID)

	if err != nil {
		return fmt.Errorf("Failed to get template %s: %v", *batchDeployment.TemplateID, err)
	}

	if template == nil {
		return fmt.Errorf("Batch deployment %s has no template", *batchDeployment.TemplateID)
	}

	if batchDeployment.SlurmClusterID == nil {
		return httpx.Conflict("Deployment %s names no Slurm cluster, so there is nowhere to launch %s",
			batchDeployment.DeploymentID, process.ProcessID)
	}

	cluster, err := s.clusterService.Get(ctx, *batchDeployment.SlurmClusterID)
	if err != nil {
		return fmt.Errorf("Failed to get cluster %s: %v", *batchDeployment.SlurmClusterID, err)
	}

	if cluster == nil {
		return fmt.Errorf("Deployment %s has no cluster", *batchDeployment.SlurmClusterID)
	}

	clusterConfig, err := s.clusterConfigService.Get(ctx, batchProcess.SlurmClusterConfigID)
	if err != nil {
		return fmt.Errorf("Failed to get cluster config %s: %v", batchProcess.SlurmClusterConfigID, err)
	}

	if clusterConfig == nil {
		return fmt.Errorf("Batch process %s has no cluster config", batchProcess.SlurmClusterConfigID)
	}

	workRoot := batchProcess.BaseWorkDir
	if workRoot == nil || strings.TrimSpace(*workRoot) == "" {
		workRoot = &clusterConfig.WorkRoot
	}
	if workRoot == nil || strings.TrimSpace(*workRoot) == "" {
		return fmt.Errorf("Batch process %s has no base work dir", batchProcess.BatchProcessID)
	}

	inputMapping := batchProcess.InputMappings
	outputMapping := batchProcess.OutputMappings
	hpcStorageType := datamodel.DataStorageTypeHPC
	for _, input := range inputMapping {
		// Process each input mapping here
		tempInput := findTemplateInput(template, input.TemplateInputID)

		if tempInput == nil {
			return fmt.Errorf("Input mapping %s has no template input", input.TemplateInputMappingID)
		}

		if tempInput.InputType == nil {
			return fmt.Errorf("Input mapping %s has no input type", input.TemplateInputMappingID)
		}

		if *tempInput.InputType == appmod.TemplateInputTypeFile {
			// Create a data staging task for the file input
			dataProductId := input.Value
			if dataProductId == nil || *dataProductId == "" {
				return fmt.Errorf("Input mapping %s has no value", input.TemplateInputMappingID)
			}

			dataProduct, err := s.dataProductService.Get(ctx, *dataProductId)
			if err != nil {
				return fmt.Errorf("Failed to find data product %s: %v", *dataProductId, err)
			}

			destPath := *workRoot + "/" + process.ProcessID + "/" + *&tempInput.InputName

			failureAction := procmodel.OnFailureActionRetry
			retryCount := 3
			taskOrder := 0

			dataStagingTask := &procmodel.DataStagingTask{
				ProcessID:             &process.ProcessID,
				SourceDataStorageID:   dataProduct.DataStorageID,
				SourcePath:            dataProduct.Path,
				SourceDataStorageType: &dataProduct.DataStorageType,

				DestinationDataStorageID:   &clusterConfig.SlurmClusterConfigID,
				DestinationDataStorageType: &hpcStorageType,
				DestinationPath:            &destPath,
				OnFailure:                  &failureAction,
				RetryCount:                 &retryCount,
				TaskOrder:                  &taskOrder,
			}

			if err := s.dataStagingTasks.Save(ctx, dataStagingTask); err != nil {
				return err
			}
		}

		if *tempInput.InputType == appmod.TemplateInputTypeFileList {
			// Create a data staging task for the list input
		}

		if *tempInput.InputType == appmod.TemplateInputTypeDirectory {
			// Create a data staging task for the directory input
		}
	}

	for _, output := range outputMapping {
		// Process each output mapping here
		tempOutput := findTemplateOutput(template, output.TemplateOutputID)
		if tempOutput == nil {
			return fmt.Errorf("Output mapping %s has no template output", output.TemplateOutputMappingID)
		}

		if tempOutput.OutputType == nil {
			return fmt.Errorf("Output mapping %s has no output type", output.TemplateOutputMappingID)
		}

		if *tempOutput.OutputType == appmod.TemplateOutputTypeFile {

			dataProductId := output.Value
			if dataProductId == nil || *dataProductId == "" {
				return fmt.Errorf("Output mapping %s has no value", output.TemplateOutputMappingID)
			}

			dataProduct, err := s.dataProductService.Get(ctx, *dataProductId)
			if err != nil {
				return fmt.Errorf("Failed to find data product %s: %v", *dataProductId, err)
			}

			// Create a data staging task for the file output

			sourcePath := *workRoot + "/" + process.ProcessID + "/" + *&tempOutput.OutputName
			destStorageType := datamodel.DataStorageTypeSCP
			failureAction := procmodel.OnFailureActionRetry
			retryCount := 3
			taskOrder := 3
			dataStagingTask := &procmodel.DataStagingTask{
				ProcessID:             &process.ProcessID,
				SourceDataStorageID:   &clusterConfig.SlurmClusterConfigID,
				SourcePath:            &sourcePath,
				SourceDataStorageType: &hpcStorageType,

				DestinationDataStorageID:   dataProduct.DataStorageID,
				DestinationDataStorageType: &destStorageType,
				DestinationPath:            dataProduct.Path,
				OnFailure:                  &failureAction,
				RetryCount:                 &retryCount,
				TaskOrder:                  &taskOrder,
			}
			if err := s.dataStagingTasks.Save(ctx, dataStagingTask); err != nil {
				return err
			}
		}
	}

	jobSubmissionFailureAction := procmodel.OnFailureActionExit
	jobSubmissionRetryCount := 1
	jobSubmissionTaskOrder := 1
	jobSubmission := &procmodel.JobSubmissionTask{
		ProcessID:  &process.ProcessID,
		OnFailure:  &jobSubmissionFailureAction,
		RetryCount: &jobSubmissionRetryCount,
		TaskOrder:  &jobSubmissionTaskOrder,
	}

	if err := s.jobSubmissionTasks.Save(ctx, jobSubmission); err != nil {
		return err
	}

	jobMonitoringRetryCount := 10
	jobMonitoringTaskOrder := 3
	jobMonitoringFailureAction := procmodel.OnFailureActionRetry
	jobMonitoring := &procmodel.JobMonitoringTask{
		ProcessID:  &process.ProcessID,
		OnFailure:  &jobMonitoringFailureAction,
		RetryCount: &jobMonitoringRetryCount,
		TaskOrder:  &jobMonitoringTaskOrder,
	}
	if err := s.monitoringTasks.Save(ctx, jobMonitoring); err != nil {
		return err
	}

	return nil
}

func findTemplateInput(template *appdto.TemplateResponse, inputID string) *appdto.TemplateInputDTO {
	for _, input := range template.Inputs {
		if input.InputID == inputID {
			return &input
		}
	}
	return nil
}

func findTemplateOutput(template *appdto.TemplateResponse, outputID string) *appdto.TemplateOutputDTO {
	for _, output := range template.Outputs {
		if output.OutputID == outputID {
			return &output
		}
	}
	return nil
}
