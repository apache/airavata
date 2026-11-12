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

// Package batch holds the tasks that act on a batch job through its cluster's
// scheduler: building the submission script, submitting it and cancelling it.
package batch

import (
	"context"
	"fmt"
	"log/slog"
	"os"
	"path"
	"regexp"
	"slices"
	"sort"
	"strings"
	"time"

	applicationmodel "github.com/apache/airavata/api/application/model"
	applicationrepo "github.com/apache/airavata/api/application/repository"
	computerepo "github.com/apache/airavata/api/compute/repository"
	model "github.com/apache/airavata/api/process/model"
	processrepo "github.com/apache/airavata/api/process/repository"
	"github.com/apache/airavata/internal/link"
	"github.com/apache/airavata/internal/orchestration/execution"
	ssh "github.com/apache/airavata/internal/ssh"
)

// LinkIssuer mints the secrets a submission's job is handed so a Linkspan it runs can
// link back to Airavata. It is process/service.LinkService.
type LinkIssuer interface {
	Issue(ctx context.Context, processID string) (*link.Issued, error)
}

// linkEnvFile is the file, in the job's working directory, that carries the issued
// link secrets into the job. It is written mode 0600 and sourced by the script, so
// the tokens never appear in the script, which is logged, stored on the task and left
// readable on the cluster.
const linkEnvFile = ".airavata-link.env"

// Tasks carries out the batch tasks of a process. Its methods are workflow activities.
type Tasks struct {
	processes           *processrepo.ProcessRepository
	jobSubmissionTasks  *processrepo.JobSubmissionTaskRepository
	slurmClusterConfigs *computerepo.SlurmClusterConfigRepository
	batchDeployments    *applicationrepo.BatchDeploymentRepository
	templates           *applicationrepo.TemplateRepository
	batchStatus         *processrepo.BatchJobStatusRepository
	sshContext          *ssh.GlobalSSHContext
	tracker             *execution.TaskTracker
	links               LinkIssuer
}

// New returns the batch tasks over the given repositories.
func New(processes *processrepo.ProcessRepository,
	jobSubmissionTasks *processrepo.JobSubmissionTaskRepository,
	slurmClusterConfigs *computerepo.SlurmClusterConfigRepository,
	batchDeployments *applicationrepo.BatchDeploymentRepository,
	templates *applicationrepo.TemplateRepository,
	batchStatus *processrepo.BatchJobStatusRepository,
	sshContext *ssh.GlobalSSHContext,
	tracker *execution.TaskTracker,
	links LinkIssuer,
) *Tasks {
	return &Tasks{
		processes:           processes,
		jobSubmissionTasks:  jobSubmissionTasks,
		slurmClusterConfigs: slurmClusterConfigs,
		batchDeployments:    batchDeployments,
		templates:           templates,
		batchStatus:         batchStatus,
		sshContext:          sshContext,
		tracker:             tracker,
		links:               links,
	}
}

type SubmitBatchJobParameters struct {
	ProcessID      string
	ExecutablePath string
	Arguments      []string
	Environment    map[string]string
}

// SubmitBatchJob builds the process's submission script, uploads it and submits it,
// recording the task's status as it goes.
func (a *Tasks) SubmitBatchJob(ctx context.Context, executionContext *execution.Context, processID string, taskID string) (*execution.Context, error) {
	return a.tracker.Track(ctx, processID, taskID, model.TaskTypeJobSubmission, executionContext, func(ctx context.Context) (*execution.Context, error) {
		return a.submitBatchJob(ctx, executionContext, processID, taskID)
	})
}

func (a *Tasks) submitBatchJob(ctx context.Context, executionContext *execution.Context, processID string, taskID string) (*execution.Context, error) {
	slog.Info("Starting to submit batch job", "taskId", taskID, "processId", processID)

	process, err := execution.FindBatchProcess(ctx, a.processes, processID)
	if err != nil {
		slog.Error("Failed to retrieve process for submitting batch job", "taskId", taskID, "processId", processID, "error", err)
		return nil, err
	}
	jst, err := a.jobSubmissionTasks.FindByIDAndProcessID(ctx, taskID, processID)
	if err != nil {
		slog.Error("Failed to retrieve job submission task for submitting batch job", "taskId", taskID, "processId", processID, "error", err)
		return nil, err
	}

	if jst.WorkingDir == nil || strings.TrimSpace(*jst.WorkingDir) == "" {
		err := fmt.Errorf("job submission task %s of process %s names no working directory", taskID, processID)
		slog.Error("Failed to submit batch job", "taskId", taskID, "processId", processID, "error", err)
		return nil, err
	}

	// Each submission is issued fresh link secrets, so a job from an earlier
	// submission of the process can no longer link.
	var issued *link.Issued
	envFilePath := ""
	if a.links != nil {
		issued, err = a.links.Issue(ctx, processID)
		if err != nil {
			slog.Error("Failed to issue link secrets for batch job", "taskId", taskID, "processId", processID, "error", err)
			return nil, err
		}
		envFilePath = path.Join(*jst.WorkingDir, linkEnvFile)
	}

	script, err := a.slurmScript(ctx, process, executionContext.GlobalJobConfigs, issued, envFilePath)
	if err != nil {
		slog.Error("Failed to build slurm script for submitting batch job", "taskId", taskID, "processId", processID, "error", err)
		return nil, err
	}

	slog.Info("Built slurm script for batch job", "taskId", taskID, "processId", processID, "script", script)

	jst.Script = &script
	if err := a.jobSubmissionTasks.Save(ctx, jst); err != nil {
		slog.Error("Failed to record slurm script on job submission task", "taskId", taskID, "processId", processID, "error", err)
		return nil, err
	}

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

	if issued != nil {
		if err := a.uploadLinkEnv(ctx, dialConfig, issued, envFilePath); err != nil {
			slog.Error("Failed to upload link secrets to cluster", "taskId", taskID, "processId", processID, "remotePath", envFilePath, "error", err)
			return nil, err
		}
	}

	scriptUploadPath := path.Join(*jst.WorkingDir, "script.slurm")
	slog.Info("Uploading slurm script to cluster", "taskId", taskID, "processId", processID, "scriptPath", scriptPath, "remotePath", scriptUploadPath)
	if err := ssh.UploadFileToSCP(ctx, dialConfig, a.sshContext, scriptPath, scriptUploadPath); err != nil {
		slog.Error("Failed to upload slurm script to cluster", "taskId", taskID, "processId", processID, "remotePath", scriptUploadPath, "error", err)
		return nil, err
	}

	slog.Info("Uploaded slurm script to cluster", "taskId", taskID, "processId", processID, "scriptPath", scriptPath, "remotePath", scriptUploadPath)

	slog.Info("Submitting slurm script to cluster", "taskId", taskID, "processId", processID, "remotePath", scriptUploadPath)

	// From here a stop no longer interrupts: a job submitted but not recorded could
	// never be cancelled.
	ctx = context.WithoutCancel(ctx)

	sbatchPath := "sbatch"
	if clusterConfig.SlurmCluster.SlurmHome != nil && strings.TrimSpace(*clusterConfig.SlurmCluster.SlurmHome) != "" {
		sbatchPath = path.Join(*clusterConfig.SlurmCluster.SlurmHome, "sbatch")
	}

	// sbatch is run from the working directory so that a script writing relative paths —
	// and the job's own stdout and stderr files — land beside the staged inputs rather
	// than in the login user's home.
	submitCommand := "cd " + ssh.ShellQuote(*jst.WorkingDir) + " && " + ssh.ShellQuote(sbatchPath) + " " + ssh.ShellQuote(scriptUploadPath)

	stdout, stderr, err := ssh.RunSSHCommand(ctx, dialConfig, a.sshContext, submitCommand,
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
// uploadLinkEnv writes the issued link secrets to remotePath as shell exports, mode
// 0600. scp keeps the local file's mode on a file it creates, and the remote file is
// removed first so an existing one's wider mode is not inherited.
func (a *Tasks) uploadLinkEnv(ctx context.Context, dialConfig ssh.SSHDialConfig, issued *link.Issued, remotePath string) error {
	env := issued.Env()
	names := make([]string, 0, len(env))
	for name := range env {
		names = append(names, name)
	}
	sort.Strings(names)
	var b strings.Builder
	b.WriteString("# Written by Airavata for this job's Linkspan. Do not share.\n")
	for _, name := range names {
		b.WriteString("export " + name + "=" + ssh.ShellQuote(env[name]) + "\n")
	}

	local, err := os.CreateTemp("", "airavata-link-*.env")
	if err != nil {
		return err
	}
	defer os.Remove(local.Name())
	if _, err := local.WriteString(b.String()); err != nil {
		local.Close()
		return err
	}
	if err := local.Close(); err != nil {
		return err
	}
	if err := os.Chmod(local.Name(), 0600); err != nil {
		return err
	}

	if _, _, err := ssh.RunSSHCommand(ctx, dialConfig, a.sshContext, "rm -f "+ssh.ShellQuote(remotePath),
		"ssh rm "+remotePath); err != nil {
		return err
	}
	return ssh.UploadFileToSCP(ctx, dialConfig, a.sshContext, local.Name(), remotePath)
}

func (a *Tasks) slurmScript(ctx context.Context, process *model.Process, globalJobConfigs *execution.GlobalJobConfigs, issued *link.Issued, envFilePath string) (string, error) {
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
	return buildSlurmScript(process, deployment, template, clusterConfig, globalJobConfigs, issued, envFilePath)
}

var jobGone = regexp.MustCompile(`(?i)invalid job id specified|already completing or completed`)

func (a *Tasks) CancelBatchJob(ctx context.Context, processID string) error {
	process, err := a.processes.FindByID(ctx, processID)
	if err != nil {
		return err
	}
	if process.BatchProcess == nil || process.BatchProcess.JobID == nil ||
		slices.ContainsFunc(process.BatchProcess.BatchJobStatuses, func(s *model.BatchJobStatus) bool {
			return slices.Contains([]model.BatchJobStatusType{model.BatchJobStatusCancelled, model.BatchJobStatusEnded,
				model.BatchJobStatusFailed, model.BatchJobStatusTimeLimit}, s.Status)
		}) {
		return nil
	}
	clusterConfig, err := a.slurmClusterConfigs.FindByID(ctx, process.BatchProcess.SlurmClusterConfigID)
	if err != nil {
		return err
	}
	cluster := clusterConfig.SlurmCluster
	scancel := "scancel"
	if cluster.SlurmHome != nil && strings.TrimSpace(*cluster.SlurmHome) != "" {
		scancel = path.Join(*cluster.SlurmHome, "scancel")
	}
	dialConfig := ssh.NewSSHDialConfig(cluster.HeadnodeHost, cluster.HeadnodePort, clusterConfig.LoginUser, *clusterConfig.SSHCredential, *process.OwnerID)
	// Bounded, since a stop request waits on this.
	sshCtx, cancel := context.WithTimeout(ctx, time.Minute)
	defer cancel()
	if _, stderr, err := ssh.RunSSHCommand(sshCtx, dialConfig, a.sshContext, ssh.ShellQuote(scancel)+" "+ssh.ShellQuote(*process.BatchProcess.JobID),
		fmt.Sprintf("ssh scancel %s@%s", clusterConfig.LoginUser, cluster.HeadnodeHost)); err != nil && !jobGone.MatchString(stderr) {
		return err
	}
	return a.batchStatus.Create(ctx, &model.BatchJobStatus{BatchProcessID: process.BatchProcess.ID, Status: model.BatchJobStatusCancelled, UpdatedAt: time.Now()})
}
