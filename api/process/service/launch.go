package service

import (
	"context"
	"strings"

	"gorm.io/gorm"

	"fmt"

	applicationmodel "github.com/apache/airavata/api/application/model"
	applicationrepo "github.com/apache/airavata/api/application/repository"
	datamodel "github.com/apache/airavata/api/data/model"
	datarepo "github.com/apache/airavata/api/data/repository"
	iamrepo "github.com/apache/airavata/api/iam/repository"
	model "github.com/apache/airavata/api/process/model"
	"github.com/apache/airavata/api/process/repository"
)

type LaunchService struct {
	db                 *gorm.DB
	processes          *repository.ProcessRepository
	deployments        *applicationrepo.BatchDeploymentRepository
	users              *iamrepo.UserRepository
	statuses           *StatusService
	dataStagingTasks   *repository.DataStagingTaskRepository
	jobSubmissionTasks *repository.JobSubmissionTaskRepository
	monitoringTasks    *repository.JobMonitoringTaskRepository
	interactiveTasks   *repository.InteractiveCommandTaskRepository
	data               *datarepo.DataProductRepository
}

func (s *LaunchService) LaunchProcess(ctx context.Context, processID string) error {
	// Implementation for launching a process goes here

	proc, err := s.processes.FindByID(ctx, processID)
	if err != nil {
		return notFoundAs(err, "Process not found: %s", processID)
	}

	if *proc.ProcessType == model.ProcessTypeBatchJob {
		return s.launchBatchProcess(ctx, proc)
	}

	return nil
}

func (s *LaunchService) launchBatchProcess(ctx context.Context, process *model.Process) error {
	// Implementation for launching a batch process goes here
	batchProcess := process.BatchProcess

	inputMapping := batchProcess.InputMappings
	outputMapping := batchProcess.OutputMappings

	batchDeployment := batchProcess.Deployment

	cluster := batchDeployment.Cluster

	sshCredential := batchProcess.SubmissionCredential

	clusterStorage := cluster.SCPDataStorage

	if sshCredential == nil {
		return fmt.Errorf("No SSH credential available for batch process")
	}

	// Every staging path is built beneath the run's own subdirectory of this, so a run
	// that named no base work dir has nowhere to stage to. The field is optional on the
	// wire, which is why it is checked here rather than assumed.
	if batchProcess.BaseWorkDir == nil || strings.TrimSpace(*batchProcess.BaseWorkDir) == "" {
		return fmt.Errorf("Batch process %s has no base work dir", batchProcess.ID)
	}

	for _, input := range inputMapping {
		// Process each input mapping here
		tempInput := input.TemplateInput
		if tempInput == nil {
			return fmt.Errorf("Input mapping %s has no template input", input.TemplateInputMappingID)
		}

		if tempInput.InputType == nil {
			return fmt.Errorf("Input mapping %s has no input type", input.TemplateInputMappingID)
		}

		if *tempInput.InputType == applicationmodel.TemplateInputTypeFile {
			// Create a data staging task for the file input
			dataProductId := input.Value
			if *dataProductId == "" {
				return fmt.Errorf("Input mapping %s has no value", input.TemplateInputMappingID)
			}

			dataProduct, err := s.data.FindByID(ctx, *dataProductId)
			if err != nil {
				return fmt.Errorf("Failed to find data product %s: %v", *dataProductId, err)
			}

			destPath := *batchProcess.BaseWorkDir + "/" + process.ID + "/" + *tempInput.InputName
			destStorageType := datamodel.DataStorageTypeSCP
			failureAction := model.OnFailureActionRetry
			retryCount := 3
			taskOrder := 0
			dataStagingTask := &model.DataStagingTask{
				ProcessID:                  &process.ID,
				SourceDataStorageID:        dataProduct.DataStorageID,
				SourcePath:                 dataProduct.Path,
				SourceDataStorageType:      &dataProduct.DataStorageType,
				DestinationDataStorageID:   &clusterStorage.ID,
				DestinationCredentialID:    &sshCredential.ID,
				DestinationDataStorageType: &destStorageType,
				DestinationPath:            &destPath,
				OnFailure:                  &failureAction,
				RetryCount:                 &retryCount,
				TaskOrder:                  &taskOrder,
			}
			s.dataStagingTasks.Save(ctx, dataStagingTask)
		}

		if *tempInput.InputType == applicationmodel.TemplateInputTypeFileList {
			// Create a data staging task for the list input
		}

		if *tempInput.InputType == applicationmodel.TemplateInputTypeDirectory {
			// Create a data staging task for the directory input
		}
	}

	for _, output := range outputMapping {
		// Process each output mapping here
		tempOutput := output.TemplateOutput
		if tempOutput == nil {
			return fmt.Errorf("Output mapping %s has no template output", output.TemplateOutputMappingID)
		}

		if tempOutput.OutputType == nil {
			return fmt.Errorf("Output mapping %s has no output type", output.TemplateOutputMappingID)
		}

		if *tempOutput.OutputType == applicationmodel.TemplateOutputTypeFile {

			dataProductId := output.Value
			if *dataProductId == "" {
				return fmt.Errorf("Output mapping %s has no value", output.TemplateOutputMappingID)
			}

			dataProduct, err := s.data.FindByID(ctx, *dataProductId)
			if err != nil {
				return fmt.Errorf("Failed to find data product %s: %v", *dataProductId, err)
			}

			// Create a data staging task for the file output
			sourcePath := *batchProcess.BaseWorkDir + "/" + process.ID + "/" + *tempOutput.OutputName
			sourceStorageType := datamodel.DataStorageTypeSCP
			failureAction := model.OnFailureActionRetry
			retryCount := 3
			taskOrder := 3
			dataStagingTask := &model.DataStagingTask{
				ProcessID:             &process.ID,
				SourceDataStorageID:   &clusterStorage.ID,
				SourceCredentialID:    &sshCredential.ID,
				SourcePath:            &sourcePath,
				SourceDataStorageType: &sourceStorageType,

				DestinationDataStorageID:   dataProduct.DataStorageID,
				DestinationDataStorageType: &dataProduct.DataStorageType,
				DestinationPath:            dataProduct.Path,
				OnFailure:                  &failureAction,
				RetryCount:                 &retryCount,
				TaskOrder:                  &taskOrder,
			}
			s.dataStagingTasks.Save(ctx, dataStagingTask)
		}
	}

	jobSubmissionFailureAction := model.OnFailureActionExit
	jobSubmissionRetryCount := 1
	jobSubmissionTaskOrder := 1
	jobSubmission := &model.JobSubmissionTask{
		ProcessID:  &process.ID,
		OnFailure:  &jobSubmissionFailureAction,
		RetryCount: &jobSubmissionRetryCount,
		TaskOrder:  &jobSubmissionTaskOrder,
	}

	s.jobSubmissionTasks.Save(ctx, jobSubmission)

	jobMonitoringRetryCount := 10
	jobMonitoringTaskOrder := 3
	jobMonitoringFailureAction := model.OnFailureActionRetry
	jobMonitoring := &model.JobMonitoringTask{
		ProcessID:  &process.ID,
		OnFailure:  &jobMonitoringFailureAction,
		RetryCount: &jobMonitoringRetryCount,
		TaskOrder:  &jobMonitoringTaskOrder,
	}
	s.monitoringTasks.Save(ctx, jobMonitoring)

	return nil
}
