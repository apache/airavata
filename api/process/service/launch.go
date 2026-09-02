package service

import (
	"context"

	"gorm.io/gorm"

	applicationrepo "github.com/apache/airavata/api/application/repository"
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
		//return s.launchBatchProcess(ctx, proc)
	}

	return nil
}

/*
func (s *LaunchService) launchBatchProcess(ctx context.Context, process *model.Process) error {
	// Implementation for launching a batch process goes here
	batchProcess := process.BatchProcess

	inputMapping := batchProcess.InputMappings
	outputMapping := batchProcess.OutputMappings

	batchDeployment := batchProcess.Deployment

	template := batchDeployment.Template

	cluster := batchDeployment.Cluster

	sshEndpoint := cluster.SSHEndpoint

	sshCredential := batchProcess.SubmissionCredential

	if sshCredential == nil {
		sshCredential = batchDeployment.DefaultSubmissionCredential
	}

	if sshCredential == nil {
		return fmt.Errorf("No SSH credential available for batch process")
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

			dataStagingTask := &model.DataStagingTask{
				ProcessID:             &process.ID,
				SourceDataStorageID:   dataProduct.DataStorageID,
				SourceCredentialID:    dataProduct.CredentialID,
				SourceDataStorageType: &dataProduct.DataStorageType,


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

	return nil
}*/
