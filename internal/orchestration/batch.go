package orchestration

import (
	"context"
	"fmt"
	"log/slog"

	applicationmodel "github.com/apache/airavata/api/application/model"
	model "github.com/apache/airavata/api/process/model"
)

type SubmitBatchJobParameters struct {
	ProcessID      string
	ExecutablePath string
	Arguments      []string
	Environment    map[string]string
}

func (a *ExecutionEngine) submitBatchJob(ctx context.Context, executionContext *ExecutionContext, processID string, taskID string) (*ExecutionContext, error) {
	slog.Info("Starting to submit batch job", "taskId", taskID, "processId", processID)
	process, err := a.process(ctx, processID)
	if err != nil {
		slog.Error("Failed to retrieve process for submitting batch job", "taskId", taskID, "processId", processID, "error", err)
		return nil, err
	}
	jst, err := a.jobSubmissionTasks.FindByIDAndProcessID(ctx, taskID, processID)
	if err != nil {
		slog.Error("Failed to retrieve job submission task for submitting batch job", "taskId", taskID, "processId", processID, "error", err)
		return nil, err
	}

	script, err := a.slurmScript(ctx, process)
	if err != nil {
		slog.Error("Failed to build slurm script for submitting batch job", "taskId", taskID, "processId", processID, "error", err)
		return nil, err
	}

	slog.Info("Built slurm script for batch job", "taskId", taskID, "processId", processID, "script", script)

	// TODO: Implement the actual submission of the batch job using the built slurm script.

	slog.Info("Completed submitting batch job", "taskId", taskID, "processId", processID, "deploymentId", process.BatchProcess.DeploymentID, "JST Id", jst.ID)
	return executionContext, nil
}

// slurmScript gathers what a submission script is built from and renders it.
//
// Each reference is optional on the record it is read from, so it is checked before it
// is followed: a run naming no deployment has nothing to submit, and saying so here is
// clearer than a nil dereference inside the renderer.
func (a *ExecutionEngine) slurmScript(ctx context.Context, process *model.Process) (string, error) {
	batch := process.BatchProcess

	if batch.DeploymentID == nil {
		return "", fmt.Errorf("batch process %s names no deployment", batch.ID)
	}
	deployment, err := a.batchDeployments.FindByID(ctx, *batch.DeploymentID)
	if err != nil {
		return "", fmt.Errorf("deployment %s of process %s could not be read: %w", *batch.DeploymentID, process.ID, err)
	}

	clusterConfig, err := a.slurmClusterConfigs.FindByID(ctx, batch.SlurmClusterConfigID)
	if err != nil {
		return "", fmt.Errorf("cluster config %s of process %s could not be read: %w", batch.SlurmClusterConfigID, process.ID, err)
	}

	var template *applicationmodel.Template
	if deployment.TemplateID != nil {
		template, err = a.templates.FindByID(ctx, *deployment.TemplateID)
		if err != nil {
			return "", fmt.Errorf("template %s of deployment %s could not be read: %w", *deployment.TemplateID, deployment.ID, err)
		}
	}

	return buildSlurmScript(process, deployment, template, clusterConfig)
}

// slurmScriptKey names the built script in the execution context, scoped by process so
// that nothing a later activity reads can belong to a different run.
func slurmScriptKey(processID string) string { return "slurm-script:" + processID }

func (a *ExecutionEngine) CancelBatchJob(ctx context.Context, executionContext *ExecutionContext, processID string, taskID string) (*ExecutionContext, error) {
	slog.Info("Starting to cancel batch job", "taskId", taskID, "processId", processID)
	process, err := a.process(ctx, processID)
	if err != nil {
		slog.Error("Failed to retrieve process for cancelling batch job", "taskId", taskID, "processId", processID, "error", err)
		return nil, err
	}
	jst, err := a.jobSubmissionTasks.FindByIDAndProcessID(ctx, taskID, processID)
	if err != nil {
		slog.Error("Failed to retrieve job submission task for cancelling batch job", "taskId", taskID, "processId", processID, "error", err)
		return nil, err
	}
	slog.Info("Completed cancelling batch job", "taskId", taskID, "processId", processID, "jobId", process.BatchProcess.JobID, "JST Id", jst.ID)
	return executionContext, nil
}

func (a *ExecutionEngine) monitorBatchJob(ctx context.Context, executionContext *ExecutionContext, processID string, taskID string) (*ExecutionContext, error) {
	slog.Info("Starting to monitor batch job ", "taskId", taskID, "processId", processID)
	process, err := a.process(ctx, processID)
	if err != nil {
		slog.Error("Failed to retrieve process for monitoring batch job", "taskId", taskID, "processId", processID, "error", err)
		return nil, err
	}
	jmt, err := a.jobMonitoringTasks.FindByIDAndProcessID(ctx, taskID, processID)
	if err != nil {
		slog.Error("Failed to retrieve job monitoring task for monitoring batch job", "taskId", taskID, "processId", processID, "error", err)
		return nil, err
	}
	slog.Info("Completed monitoring batch job", "taskId", taskID, "processId", processID, "jobId", process.BatchProcess.JobID, "JMT Id", jmt.ID)
	return executionContext, nil
}

func (a *ExecutionEngine) process(ctx context.Context, processID string) (*model.Process, error) {
	process, err := a.processes.FindByID(ctx, processID)
	if err != nil {
		return nil, err
	}
	if process.BatchProcess == nil {
		return nil, fmt.Errorf("process %s carries no batch process section", processID)
	}
	return process, nil
}
