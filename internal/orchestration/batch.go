package orchestration

import (
	"context"
	"fmt"
	"log/slog"
	"os"
	"path"
	"regexp"
	"strings"

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

	// save script to a temporary location or a known path before uploading
	scriptPath := fmt.Sprintf("/tmp/%s.slurm", processID)
	err = os.WriteFile(scriptPath, []byte(script), 0644)
	if err != nil {
		slog.Error("Failed to write slurm script to temporary location", "taskId", taskID, "processId", processID, "error", err)
		return nil, err
	}

	defer os.Remove(scriptPath)

	clusterConfig, err := a.slurmClusterConfigs.FindByID(ctx, process.BatchProcess.SlurmClusterConfigID)
	if err != nil {
		slog.Error("Failed to retrieve slurm cluster config for submitting batch job", "taskId", taskID, "processId", processID, "error", err)
		return nil, err
	}

	slog.Info("Retrieved slurm cluster config for batch job", "taskId", taskID, "processId", processID, "clusterConfigId", clusterConfig.ID)

	if jst.WorkingDir == nil || strings.TrimSpace(*jst.WorkingDir) == "" {
		err := fmt.Errorf("job submission task %s of process %s names no working directory", taskID, processID)
		slog.Error("Failed to submit batch job", "taskId", taskID, "processId", processID, "error", err)
		return nil, err
	}

	scriptUploadPath := path.Join(*jst.WorkingDir, "script.slurm")
	slog.Info("Uploading slurm script to cluster", "taskId", taskID, "processId", processID, "scriptPath", scriptPath, "remotePath", scriptUploadPath)
	if err := UploadFileToSCP(ctx, clusterConfig.SlurmCluster.HeadnodeHost,
		clusterConfig.SlurmCluster.HeadnodePort, clusterConfig.LoginUser,
		*clusterConfig.SSHKey, scriptPath, scriptUploadPath); err != nil {
		slog.Error("Failed to upload slurm script to cluster", "taskId", taskID, "processId", processID, "remotePath", scriptUploadPath, "error", err)
		return nil, err
	}

	slog.Info("Uploaded slurm script to cluster", "taskId", taskID, "processId", processID, "scriptPath", scriptPath, "remotePath", scriptUploadPath)

	slog.Info("Submitting slurm script to cluster", "taskId", taskID, "processId", processID, "remotePath", scriptUploadPath)
	// sbatch is run from the working directory so that a script writing relative paths —
	// and the job's own stdout and stderr files — land beside the staged inputs rather
	// than in the login user's home.
	submitCommand := "cd " + shellQuote(*jst.WorkingDir) + " && sbatch " + shellQuote(scriptUploadPath)
	stdout, stderr, err := runSSHCommand(ctx, clusterConfig.SlurmCluster.HeadnodeHost,
		clusterConfig.SlurmCluster.HeadnodePort, clusterConfig.LoginUser,
		*clusterConfig.SSHKey, submitCommand,
		fmt.Sprintf("ssh sbatch %s@%s:%s", clusterConfig.LoginUser, clusterConfig.SlurmCluster.HeadnodeHost, scriptUploadPath))
	if err != nil {
		slog.Error("Failed to submit slurm script to cluster", "taskId", taskID, "processId", processID, "remotePath", scriptUploadPath, "stdout", stdout, "stderr", stderr, "error", err)
		return nil, err
	}

	jobID, err := parseSbatchJobID(stdout)
	if err != nil {
		// The job may well be queued: sbatch exited zero, so something was accepted and
		// only its id was not understood. Failing loudly is still right — without an id
		// nothing downstream can monitor or cancel it — but the output is logged so the
		// run can be reconciled by hand.
		slog.Error("Failed to read job id from sbatch output", "taskId", taskID, "processId", processID, "stdout", stdout, "stderr", stderr, "error", err)
		return nil, err
	}

	slog.Info("Submitted batch job", "jobId", jobID, "taskId", taskID, "processId", processID, "jobId", jobID)

	// Recorded in both places the rest of the run reads it from: on the task that did the
	// submitting, and on the batch process, which is what monitoring and cancellation
	// look at.
	jst.JobId = &jobID
	if err := a.jobSubmissionTasks.Save(ctx, jst); err != nil {
		slog.Error("Failed to record job id on job submission task", "taskId", taskID, "processId", processID, "jobId", jobID, "error", err)
		return nil, err
	}

	process.BatchProcess.JobID = &jobID
	if err := a.processes.SaveBatchProcess(ctx, process.BatchProcess); err != nil {
		slog.Error("Failed to record job id on batch process", "taskId", taskID, "processId", processID, "jobId", jobID, "error", err)
		return nil, err
	}

	slog.Info("Completed submitting batch job", "taskId", taskID, "processId", processID, "deploymentId", process.BatchProcess.DeploymentID, "JST Id", jst.ID, "jobId", jobID)
	return executionContext, nil
}

// sbatchJobID matches the line sbatch prints when it accepts a script: "Submitted batch
// job 4242", which on a federated install carries a trailing " on cluster <name>".
var sbatchJobID = regexp.MustCompile(`Submitted batch job (\d+)`)

// parseSbatchJobID reads the scheduler's job id out of what sbatch printed.
//
// The line is searched for rather than the whole output matched: a login shell is free
// to print a banner, a module load notice or a warning before it, and none of that makes
// the submission any less successful.
func parseSbatchJobID(out string) (string, error) {
	match := sbatchJobID.FindStringSubmatch(out)
	if match == nil {
		return "", fmt.Errorf("sbatch announced no job id, and its output was %q", strings.TrimSpace(out))
	}
	return match[1], nil
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
