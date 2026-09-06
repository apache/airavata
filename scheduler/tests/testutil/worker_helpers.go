package testutil

import (
	"context"
	"fmt"
	"os/exec"
	"strings"
	"time"

	"github.com/apache/airavata/scheduler/core/domain"
	"github.com/stretchr/testify/require"
)

// WorkerTestHelper provides utilities for testing real worker spawning
type WorkerTestHelper struct {
	suite *IntegrationTestSuite
}

// NewWorkerTestHelper creates a new worker test helper
func NewWorkerTestHelper(suite *IntegrationTestSuite) *WorkerTestHelper {
	return &WorkerTestHelper{
		suite: suite,
	}
}

// SpawnRealWorker spawns a real worker process on the specified compute resource
func (w *WorkerTestHelper) SpawnRealWorker(t require.TestingT, computeResource *domain.ComputeResource, duration time.Duration) (*domain.Worker, error) {
	workerID := fmt.Sprintf("worker_%s_%d", computeResource.ID, time.Now().UnixNano())

	// Create worker domain object
	worker := &domain.Worker{
		ID:                workerID,
		ComputeResourceID: computeResource.ID,
		Status:            domain.WorkerStatusIdle,
		CreatedAt:         time.Now(),
		LastHeartbeat:     time.Now(),
	}

	// Spawn worker based on compute resource type
	switch computeResource.Type {
	case domain.ComputeResourceTypeSlurm:
		return w.spawnSLURMWorker(t, worker, duration)
	case domain.ComputeResourceTypeKubernetes:
		return w.spawnKubernetesWorker(t, worker, duration)
	case domain.ComputeResourceTypeBareMetal:
		return w.spawnBareMetalWorker(t, worker, duration)
	default:
		return nil, fmt.Errorf("unsupported compute resource type: %s", computeResource.Type)
	}
}

// spawnSLURMWorker spawns a worker on SLURM cluster
func (w *WorkerTestHelper) spawnSLURMWorker(t require.TestingT, worker *domain.Worker, duration time.Duration) (*domain.Worker, error) {
	// Create SLURM job script
	script := fmt.Sprintf(`#!/bin/bash
#SBATCH --job-name=worker-%s
#SBATCH --output=/tmp/worker-%s.log
#SBATCH --error=/tmp/worker-%s.err
#SBATCH --time=%d
#SBATCH --nodes=1
#SBATCH --ntasks=1

# Start worker binary
%s --worker-id=%s --scheduler-addr=%s --compute-resource-id=%s --duration=%s
`, worker.ID, worker.ID, worker.ID, int(duration.Seconds()),
		w.suite.WorkerBinaryPath, worker.ID, w.suite.GRPCAddr, worker.ComputeResourceID, duration.String())

	// Submit job to SLURM
	cmd := exec.Command("docker", "exec", "airavata-scheduler-slurm-cluster-01-1", "sbatch", "--parsable")
	cmd.Stdin = strings.NewReader(script)

	output, err := cmd.Output()
	if err != nil {
		return nil, fmt.Errorf("failed to submit SLURM job: %w", err)
	}

	jobID := strings.TrimSpace(string(output))
	worker.Metadata = map[string]interface{}{
		"slurm_job_id": jobID,
		"container":    "airavata-scheduler-slurm-cluster-01-1",
	}

	return worker, nil
}

// spawnKubernetesWorker spawns a worker on Kubernetes cluster
func (w *WorkerTestHelper) spawnKubernetesWorker(t require.TestingT, worker *domain.Worker, duration time.Duration) (*domain.Worker, error) {
	// Create Kubernetes job manifest
	manifest := fmt.Sprintf(`apiVersion: batch/v1
kind: Job
metadata:
  name: worker-%s
spec:
  template:
    spec:
      containers:
      - name: worker
        image: airavata-worker:latest
        command: ["%s"]
        args: ["--worker-id=%s", "--scheduler-addr=%s", "--compute-resource-id=%s", "--duration=%s"]
        resources:
          requests:
            memory: "64Mi"
            cpu: "100m"
          limits:
            memory: "128Mi"
            cpu: "200m"
      restartPolicy: Never
  backoffLimit: 3
`, worker.ID, w.suite.WorkerBinaryPath, worker.ID, w.suite.GRPCAddr, worker.ComputeResourceID, duration.String())

	// Apply Kubernetes job
	cmd := exec.Command("kubectl", "apply", "-f", "-")
	cmd.Stdin = strings.NewReader(manifest)

	_, err := cmd.Output()
	if err != nil {
		return nil, fmt.Errorf("failed to create Kubernetes job: %w", err)
	}

	worker.Metadata = map[string]interface{}{
		"kubernetes_job": fmt.Sprintf("worker-%s", worker.ID),
		"manifest":       manifest,
	}

	return worker, nil
}

// spawnBareMetalWorker spawns a worker on bare metal node
func (w *WorkerTestHelper) spawnBareMetalWorker(t require.TestingT, worker *domain.Worker, duration time.Duration) (*domain.Worker, error) {
	// Start worker via SSH
	cmd := exec.Command("ssh", "-o", "StrictHostKeyChecking=no", "-p", "2225", "test@localhost",
		fmt.Sprintf("%s --worker-id=%s --scheduler-addr=%s --compute-resource-id=%s --duration=%s &",
			w.suite.WorkerBinaryPath, worker.ID, w.suite.GRPCAddr, worker.ComputeResourceID, duration.String()))

	err := cmd.Run()
	if err != nil {
		return nil, fmt.Errorf("failed to start bare metal worker: %w", err)
	}

	worker.Metadata = map[string]interface{}{
		"ssh_host": "localhost:2225",
		"ssh_user": "test",
	}

	return worker, nil
}

// WaitForWorkerRegistration waits for a worker to register with the scheduler
func (w *WorkerTestHelper) WaitForWorkerRegistration(t require.TestingT, workerID string, timeout time.Duration) error {
	ctx, cancel := context.WithTimeout(context.Background(), timeout)
	defer cancel()

	ticker := time.NewTicker(1 * time.Second)
	defer ticker.Stop()

	for {
		select {
		case <-ctx.Done():
			return fmt.Errorf("timeout waiting for worker %s to register", workerID)
		case <-ticker.C:
			// Check if worker is registered by getting its status
			status, err := w.suite.SchedulerSvc.GetWorkerStatus(context.Background(), workerID)
			if err != nil {
				continue
			}

			if status != nil && status.Status == domain.WorkerStatusIdle {
				return nil
			}
		}
	}
}

// WaitForWorkerReady waits for a worker to be ready to accept tasks
func (w *WorkerTestHelper) WaitForWorkerReady(t require.TestingT, workerID string, timeout time.Duration) error {
	ctx, cancel := context.WithTimeout(context.Background(), timeout)
	defer cancel()

	ticker := time.NewTicker(1 * time.Second)
	defer ticker.Stop()

	for {
		select {
		case <-ctx.Done():
			return fmt.Errorf("timeout waiting for worker %s to be ready", workerID)
		case <-ticker.C:
			// Check if worker is ready
			status, err := w.suite.SchedulerSvc.GetWorkerStatus(context.Background(), workerID)
			if err != nil {
				continue
			}

			if status != nil && (status.Status == domain.WorkerStatusIdle || status.Status == domain.WorkerStatusBusy) {
				return nil
			}
		}
	}
}

// TerminateWorker terminates a worker process
func (w *WorkerTestHelper) TerminateWorker(t require.TestingT, worker *domain.Worker) error {
	switch worker.ComputeResourceID {
	case "slurm-cluster-01", "slurm-cluster-02":
		return w.terminateSLURMWorker(t, worker)
	case "kubernetes-cluster":
		return w.terminateKubernetesWorker(t, worker)
	case "baremetal-node-1", "baremetal-node-2":
		return w.terminateBareMetalWorker(t, worker)
	default:
		return fmt.Errorf("unknown compute resource: %s", worker.ComputeResourceID)
	}
}

// terminateSLURMWorker terminates a SLURM worker
func (w *WorkerTestHelper) terminateSLURMWorker(t require.TestingT, worker *domain.Worker) error {
	jobID, ok := worker.Metadata["slurm_job_id"].(string)
	if !ok {
		return fmt.Errorf("no SLURM job ID found for worker %s", worker.ID)
	}

	cmd := exec.Command("docker", "exec", "airavata-scheduler-slurm-cluster-01-1", "scancel", jobID)
	return cmd.Run()
}

// terminateKubernetesWorker terminates a Kubernetes worker
func (w *WorkerTestHelper) terminateKubernetesWorker(t require.TestingT, worker *domain.Worker) error {
	jobName, ok := worker.Metadata["kubernetes_job"].(string)
	if !ok {
		return fmt.Errorf("no Kubernetes job name found for worker %s", worker.ID)
	}

	cmd := exec.Command("kubectl", "delete", "job", jobName)
	return cmd.Run()
}

// terminateBareMetalWorker terminates a bare metal worker
func (w *WorkerTestHelper) terminateBareMetalWorker(t require.TestingT, worker *domain.Worker) error {
	// Kill worker process via SSH
	cmd := exec.Command("ssh", "-o", "StrictHostKeyChecking=no", "-p", "2225", "test@localhost",
		fmt.Sprintf("pkill -f 'worker.*%s'", worker.ID))
	return cmd.Run()
}

// GetWorkerLogs retrieves logs from a worker
func (w *WorkerTestHelper) GetWorkerLogs(t require.TestingT, worker *domain.Worker) (string, error) {
	switch worker.ComputeResourceID {
	case "slurm-cluster-01", "slurm-cluster-02":
		return w.getSLURMWorkerLogs(t, worker)
	case "kubernetes-cluster":
		return w.getKubernetesWorkerLogs(t, worker)
	case "baremetal-node-1", "baremetal-node-2":
		return w.getBareMetalWorkerLogs(t, worker)
	default:
		return "", fmt.Errorf("unknown compute resource: %s", worker.ComputeResourceID)
	}
}

// getSLURMWorkerLogs retrieves logs from a SLURM worker
func (w *WorkerTestHelper) getSLURMWorkerLogs(t require.TestingT, worker *domain.Worker) (string, error) {
	// Get SLURM job logs
	cmd := exec.Command("docker", "exec", "airavata-scheduler-slurm-cluster-01-1", "cat", fmt.Sprintf("/tmp/worker-%s.log", worker.ID))
	output, err := cmd.Output()
	if err != nil {
		return "", fmt.Errorf("failed to get SLURM worker logs: %w", err)
	}
	return string(output), nil
}

// getKubernetesWorkerLogs retrieves logs from a Kubernetes worker
func (w *WorkerTestHelper) getKubernetesWorkerLogs(t require.TestingT, worker *domain.Worker) (string, error) {
	jobName, ok := worker.Metadata["kubernetes_job"].(string)
	if !ok {
		return "", fmt.Errorf("no Kubernetes job name found for worker %s", worker.ID)
	}

	// Get pod logs
	cmd := exec.Command("kubectl", "logs", "-l", fmt.Sprintf("job-name=%s", jobName))
	output, err := cmd.Output()
	if err != nil {
		return "", fmt.Errorf("failed to get Kubernetes worker logs: %w", err)
	}
	return string(output), nil
}

// getBareMetalWorkerLogs retrieves logs from a bare metal worker
func (w *WorkerTestHelper) getBareMetalWorkerLogs(t require.TestingT, worker *domain.Worker) (string, error) {
	// Get worker logs via SSH
	cmd := exec.Command("ssh", "-o", "StrictHostKeyChecking=no", "-p", "2225", "test@localhost",
		fmt.Sprintf("cat /tmp/worker-%s.log 2>/dev/null || echo 'No logs found'", worker.ID))
	output, err := cmd.Output()
	if err != nil {
		return "", fmt.Errorf("failed to get bare metal worker logs: %w", err)
	}
	return string(output), nil
}

// VerifyWorkerExecution verifies that a worker executed tasks correctly
func (w *WorkerTestHelper) VerifyWorkerExecution(t require.TestingT, worker *domain.Worker, expectedTasks int) error {
	// Get worker status
	status, err := w.suite.SchedulerSvc.GetWorkerStatus(context.Background(), worker.ID)
	if err != nil {
		return fmt.Errorf("failed to get worker status: %w", err)
	}

	if status == nil {
		return fmt.Errorf("worker %s not found in scheduler", worker.ID)
	}

	// Check if worker completed expected number of tasks
	if status.TasksCompleted < expectedTasks {
		return fmt.Errorf("worker %s completed %d tasks, expected %d",
			worker.ID, status.TasksCompleted, expectedTasks)
	}

	return nil
}
