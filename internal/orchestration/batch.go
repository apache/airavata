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

package orchestration

import (
	"cmp"
	"context"
	"fmt"
	"log/slog"
	"os"
	"path"
	"regexp"
	"strings"
	"time"

	applicationmodel "github.com/apache/airavata/api/application/model"
	model "github.com/apache/airavata/api/process/model"
	"github.com/apache/airavata/internal/auth"
	ssh "github.com/apache/airavata/internal/ssh"
)

type SubmitBatchJobParameters struct {
	ProcessID      string
	ExecutablePath string
	Arguments      []string
	Environment    map[string]string
}

func (a *ExecutionEngine) submitBatchJob(ctx context.Context, executionContext *ExecutionContext, processID string, taskID string) (*ExecutionContext, error) {
	slog.Info("Starting to submit batch job", "taskId", taskID, "processId", processID)
	ctx, done := a.activity(ctx, processID)
	defer done()

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

	script, err := a.slurmScript(ctx, process, executionContext.GlobalJobConfigs)
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

	dialConfig := ssh.NewSSHDialConfig(
		clusterConfig.SlurmCluster.HeadnodeHost,
		clusterConfig.SlurmCluster.HeadnodePort,
		clusterConfig.LoginUser,
		*clusterConfig.SSHCredential,
		*process.OwnerID,
	)

	slog.Info("Creating job working directory through SSH", "taskId", taskID, "processId", processID, "destinationDirectory", *jst.WorkingDir)
	if err := ssh.CreateDirectorySSH(ctx, dialConfig, a.sshContext, *jst.WorkingDir); err != nil {
		slog.Error("Failed to create destination directory through SSH", "taskId", taskID, "processId", processID,
			"destinationDirectory", *jst.WorkingDir, "error", err)
		return nil, err
	}

	scriptUploadPath := path.Join(*jst.WorkingDir, "script.slurm")
	slog.Info("Uploading slurm script to cluster", "taskId", taskID, "processId", processID, "scriptPath", scriptPath, "remotePath", scriptUploadPath)
	if err := ssh.UploadFileToSCP(ctx, dialConfig, a.sshContext, scriptPath, scriptUploadPath); err != nil {
		slog.Error("Failed to upload slurm script to cluster", "taskId", taskID, "processId", processID, "remotePath", scriptUploadPath, "error", err)
		return nil, err
	}

	slog.Info("Uploaded slurm script to cluster", "taskId", taskID, "processId", processID, "scriptPath", scriptPath, "remotePath", scriptUploadPath)

	slog.Info("Submitting slurm script to cluster", "taskId", taskID, "processId", processID, "remotePath", scriptUploadPath)

	// From sbatch on, a Stop cancels the job rather than abandoning the submission, which would orphan it.
	ctx = context.WithoutCancel(ctx)
	sbatchPath := "sbatch"
	if clusterConfig.SlurmCluster.SlurmHome != nil && strings.TrimSpace(*clusterConfig.SlurmCluster.SlurmHome) != "" {
		sbatchPath = path.Join(*clusterConfig.SlurmCluster.SlurmHome, "sbatch")
	}

	// sbatch is run from the working directory so that a script writing relative paths —
	// and the job's own stdout and stderr files — land beside the staged inputs rather
	// than in the login user's home. The Linkspan environment goes in on stdin, so no token
	// reaches the login node's argv.
	submitCommand := "cd " + ssh.ShellQuote(*jst.WorkingDir) + " && exec sh -s"
	program := a.linkExports(ctx, dialConfig, process, fmt.Sprintf("ssh provision-linkspan %s@%s", clusterConfig.LoginUser, clusterConfig.SlurmCluster.HeadnodeHost)) +
		"exec " + ssh.ShellQuote(sbatchPath) + " " + ssh.ShellQuote(scriptUploadPath) + "\n"

	stdout, stderr, err := ssh.RunSSHCommand(ctx, dialConfig, a.sshContext, submitCommand, strings.NewReader(program),
		fmt.Sprintf("ssh sbatch %s@%s:%s", clusterConfig.LoginUser, clusterConfig.SlurmCluster.HeadnodeHost, scriptUploadPath))
	if err != nil {
		slog.Error("Failed to submit slurm script to cluster", "taskId", taskID, "processId", processID, "remotePath", scriptUploadPath, "stdout", stdout, "stderr", stderr, "error", err)
		return nil, err
	}

	jobID, err := parseSbatchJobID(stdout)
	if err != nil {
		slog.Error("Failed to read job id from sbatch output", "taskId", taskID, "processId", processID, "stdout", stdout, "stderr", stderr, "error", err)
		a.batchStatus.Create(ctx, &model.BatchJobStatus{
			BatchProcessID: process.BatchProcess.ID,
			Status:         model.BatchJobStatusSubmissionFailed,
			UpdatedAt:      time.Now(),
		})
		return nil, err
	}

	slog.Info("Submitted batch job", "jobId", jobID, "taskId", taskID, "processId", processID, "jobId", jobID)

	err = a.batchStatus.Create(ctx, &model.BatchJobStatus{
		BatchProcessID: process.BatchProcess.ID,
		Status:         model.BatchJobStatusSubmitted,
		UpdatedAt:      time.Now(),
	})

	if err != nil {
		slog.Error("Failed to record batch job status", "taskId", taskID, "processId", processID, "jobId", jobID, "error", err)
		return nil, err
	}

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
	if err := a.cancelIfStopped(ctx, processID); err != nil {
		return executionContext, err
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
func (a *ExecutionEngine) slurmScript(ctx context.Context, process *model.Process, globalJobConfigs *GlobalJobConfigs) (string, error) {
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

	slog.Info("Building slurm script for process", "processId", process.ID)
	return buildSlurmScript(process, deployment, template, clusterConfig, globalJobConfigs)
}

func (a *ExecutionEngine) linkExports(ctx context.Context, dialConfig ssh.SSHDialConfig, process *model.Process, target string) string {
	if a.PublicURL == "" {
		return ""
	}
	if _, stderr, err := ssh.RunSSHCommand(ctx, dialConfig, a.sshContext, "sh -s", strings.NewReader(ProvisionScript), target); err != nil {
		a.recordStatus(ctx, process.ID, process.State(), "Linkspan could not be provisioned, so the job runs without a link: "+cmp.Or(strings.TrimSpace(stderr), err.Error()))
		return ""
	}
	control, app := model.PortsOf(process.ID)
	return fmt.Sprintf("export CS_LINK_URL='ws%s/api/v1/processes/%s/link' LINKSPAN_LINK_TOKEN=%s CS_APP_TOKEN=%s CS_APP_PORT=%d CS_CONTROL_PORT=%d\n",
		strings.TrimPrefix(a.PublicURL, "http"), process.ID, process.BatchProcess.LinkToken, process.BatchProcess.Token, app, control)
}

// activity gives an activity a context that prompts the run's launching connection and that a Stop cancels.
func (a *ExecutionEngine) activity(ctx context.Context, processID string) (context.Context, func()) {
	ctx, cancel := context.WithCancel(ctx)
	a.stops.Store(processID, cancel)
	if process, err := a.process(ctx, processID); err == nil {
		ctx = auth.WithConnection(ctx, process.BatchProcess.Connection)
		if process.State() == model.ProcessStatusTypeCancelled {
			cancel()
		}
	}
	return ctx, func() {
		a.stops.Delete(processID)
		cancel()
	}
}

// cancelIfStopped cancels the job of a run a Stop ended while sbatch ran. The submission saves its job id
// before this read and a Stop records CANCELLED before its own, so one of the two sees the other.
func (a *ExecutionEngine) cancelIfStopped(ctx context.Context, processID string) error {
	if latest, err := a.process(ctx, processID); err == nil && latest.State() == model.ProcessStatusTypeCancelled {
		return a.scancel(ctx, latest)
	}
	return nil
}

// CancelBatchJob ends a run for Stop. With no job id yet it records CANCELLED first, then re-reads for one.
func (a *ExecutionEngine) CancelBatchJob(ctx context.Context, process *model.Process) error {
	if process.BatchProcess.JobID == nil {
		a.end(ctx, process.ID)
		latest, err := a.process(ctx, process.ID)
		if err != nil {
			return err
		}
		return a.scancel(ctx, latest)
	}
	if err := a.scancel(ctx, process); err != nil {
		return err
	}
	a.end(ctx, process.ID)
	return nil
}

func (a *ExecutionEngine) end(ctx context.Context, processID string) {
	if linked, ok := a.links.LoadAndDelete(processID); ok {
		_ = linked.(*Link).Close()
	}
	a.recordStatus(ctx, processID, model.ProcessStatusTypeCancelled, "Cancelled")
	if cancel, ok := a.stops.Load(processID); ok {
		cancel.(context.CancelFunc)()
	}
}

// jobGone matches scancel's refusals for a job that has already ended, which leave nothing to cancel.
var jobGone = regexp.MustCompile(`(?i)invalid job id specified|already completing or completed`)

func (a *ExecutionEngine) scancel(ctx context.Context, process *model.Process) error {
	if process.BatchProcess.JobID == nil {
		return nil
	}
	clusterConfig, err := a.slurmClusterConfigs.FindByID(ctx, process.BatchProcess.SlurmClusterConfigID)
	if err != nil {
		return err
	}
	dialConfig := ssh.NewSSHDialConfig(clusterConfig.SlurmCluster.HeadnodeHost, clusterConfig.SlurmCluster.HeadnodePort, clusterConfig.LoginUser, *clusterConfig.SSHCredential, *process.OwnerID)
	scancel := ssh.ShellQuote(path.Join(optional(clusterConfig.SlurmCluster.SlurmHome), "scancel")) + " " + ssh.ShellQuote(*process.BatchProcess.JobID)
	if _, stderr, err := ssh.RunSSHCommand(ctx, dialConfig, a.sshContext, scancel, nil, fmt.Sprintf("ssh scancel %s@%s", clusterConfig.LoginUser, clusterConfig.SlurmCluster.HeadnodeHost)); err != nil && !jobGone.MatchString(stderr) {
		return fmt.Errorf("scancel %s: %w: %s", *process.BatchProcess.JobID, err, strings.TrimSpace(stderr))
	}
	return nil
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
