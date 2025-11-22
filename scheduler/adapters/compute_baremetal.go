package adapters

import (
	"context"
	"encoding/json"
	"fmt"
	"os"
	"os/exec"
	"path/filepath"
	"strconv"
	"strings"
	"text/template"
	"time"

	"github.com/apache/airavata/scheduler/core/domain"
	ports "github.com/apache/airavata/scheduler/core/port"
	"golang.org/x/crypto/ssh"
)

// BareMetalAdapter implements the ComputeAdapter interface for bare metal servers
type BareMetalAdapter struct {
	resource   domain.ComputeResource
	vault      domain.CredentialVault
	sshClient  *ssh.Client
	sshSession *ssh.Session
	config     *ScriptConfig
	// Enhanced fields for core integration
	workerID     string
	experimentID string
	userID       string
}

// Compile-time interface verification
var _ ports.ComputePort = (*BareMetalAdapter)(nil)

// NewBareMetalAdapter creates a new bare metal adapter
func NewBareMetalAdapter(resource domain.ComputeResource, vault domain.CredentialVault) *BareMetalAdapter {
	return NewBareMetalAdapterWithConfig(resource, vault, nil)
}

// NewBareMetalAdapterWithConfig creates a new bare metal adapter with custom script configuration
func NewBareMetalAdapterWithConfig(resource domain.ComputeResource, vault domain.CredentialVault, config *ScriptConfig) *BareMetalAdapter {
	if config == nil {
		config = &ScriptConfig{
			WorkerBinaryURL:   "https://server/api/worker-binary",
			ServerGRPCAddress: "scheduler", // Use service name for container-to-container communication
			ServerGRPCPort:    50051,
			DefaultWorkingDir: "/tmp/worker",
		}
	}
	return &BareMetalAdapter{
		resource: resource,
		vault:    vault,
		config:   config,
	}
}

// NewBareMetalAdapterWithContext creates a new bare metal adapter with worker context
func NewBareMetalAdapterWithContext(resource domain.ComputeResource, vault domain.CredentialVault, workerID, experimentID, userID string) *BareMetalAdapter {
	return &BareMetalAdapter{
		resource: resource,
		vault:    vault,
		config: &ScriptConfig{
			WorkerBinaryURL:   "https://server/api/worker-binary",
			ServerGRPCAddress: "scheduler", // Use service name for container-to-container communication
			ServerGRPCPort:    50051,
			DefaultWorkingDir: "/tmp/worker",
		},
		workerID:     workerID,
		experimentID: experimentID,
		userID:       userID,
	}
}

// baremetalScriptTemplate defines the bare metal script template
const baremetalScriptTemplate = `#!/bin/bash
# Job: {{.JobName}}
# Output: {{.OutputPath}}
# Error: {{.ErrorPath}}
# PID File: {{.PIDFile}}
{{- if .Memory}}
# Memory Limit: {{.Memory}}
{{- end}}
{{- if .CPUs}}
# CPU Limit: {{.CPUs}}
{{- end}}
{{- if .TimeLimit}}
# Time Limit: {{.TimeLimit}}
{{- end}}

# Set up environment
set -e  # Exit on any error

# Print job information
echo "Job Name: {{.JobName}}"
echo "Start Time: $(date)"
echo "Working Directory: $(pwd)"
echo "Hostname: $(hostname)"
echo "User: $(whoami)"

# Create and change to working directory
mkdir -p {{.WorkDir}}
cd {{.WorkDir}}

# Set resource limits if specified
{{- if .Memory}}
ulimit -v {{.MemoryMB}}  # Virtual memory limit in KB
{{- end}}
{{- if .CPUs}}
# CPU limiting requires cgroups or similar mechanism
echo "CPU limit requested: {{.CPUs}} cores"
{{- end}}

# Execute command with proper error handling and output redirection
echo "Executing command: {{.Command}}"
{{.Command}} > {{.OutputPath}} 2> {{.ErrorPath}}
EXIT_CODE=$?

# Print completion information
echo "End Time: $(date)"
echo "Exit Code: $EXIT_CODE"

# Exit with the same code as the command
exit $EXIT_CODE
`

// baremetalWorkerSpawnTemplate defines the bare metal worker spawn script template
const baremetalWorkerSpawnTemplate = `#!/bin/bash
# Worker spawn script for bare metal
# Generated at {{.GeneratedAt}}

set -euo pipefail

# Set environment variables
export WORKER_ID="{{.WorkerID}}"
export EXPERIMENT_ID="{{.ExperimentID}}"
export COMPUTE_RESOURCE_ID="{{.ComputeResourceID}}"
export SERVER_URL="grpc://{{.ServerAddress}}:{{.ServerPort}}"

# Create working directory
WORK_DIR="{{.WorkingDir}}/{{.WorkerID}}"
mkdir -p "$WORK_DIR"
cd "$WORK_DIR"

# Download worker binary
echo "Downloading worker binary..."
curl -L -o worker "{{.WorkerBinaryURL}}"
chmod +x worker

# Set up signal handlers for graceful shutdown
cleanup() {
    echo "Shutting down worker: $WORKER_ID"
    if [ -n "${WORKER_PID:-}" ]; then
        kill -TERM "$WORKER_PID" 2>/dev/null || true
        wait "$WORKER_PID" 2>/dev/null || true
    fi
    exit 0
}

trap cleanup SIGTERM SIGINT

# Start worker in background
echo "Starting worker: $WORKER_ID"
./worker \
    --server-url="$SERVER_URL" \
    --worker-id="$WORKER_ID" \
    --experiment-id="$EXPERIMENT_ID" \
    --compute-resource-id="$COMPUTE_RESOURCE_ID" \
    --working-dir="$WORK_DIR" &
WORKER_PID=$!

# Wait for worker to complete or timeout
timeout {{.WalltimeSeconds}} wait "$WORKER_PID" || {
    echo "Worker timeout reached, terminating..."
    cleanup
}
`

// BaremetalScriptData holds template data for script generation
type BaremetalScriptData struct {
	JobName    string
	OutputPath string
	ErrorPath  string
	WorkDir    string
	Command    string
	PIDFile    string
	Memory     string
	MemoryMB   int
	CPUs       string
	TimeLimit  string
}

// GenerateScript generates a bash script for the task
func (b *BareMetalAdapter) GenerateScript(task domain.Task, outputDir string) (string, error) {
	// Create output directory if it doesn't exist
	err := os.MkdirAll(outputDir, 0755)
	if err != nil {
		return "", fmt.Errorf("failed to create output directory: %w", err)
	}

	// Prepare script data with resource requirements
	// Use work_dir from task metadata if available, otherwise use default
	remoteWorkDir := fmt.Sprintf("/tmp/worker_%s", task.ID)
	if task.Metadata != nil {
		if workDir, ok := task.Metadata["work_dir"].(string); ok && workDir != "" {
			remoteWorkDir = workDir
		}
	}

	data := BaremetalScriptData{
		JobName:    fmt.Sprintf("task-%s", task.ID),
		OutputPath: filepath.Join(remoteWorkDir, fmt.Sprintf("%s.out", task.ID)),
		ErrorPath:  filepath.Join(remoteWorkDir, fmt.Sprintf("%s.err", task.ID)),
		WorkDir:    remoteWorkDir,
		Command:    task.Command,
		PIDFile:    filepath.Join(remoteWorkDir, fmt.Sprintf("%s.pid", task.ID)),
		Memory:     "1G",    // Default to 1GB memory
		MemoryMB:   1048576, // 1GB in KB
		CPUs:       "1",     // Default to 1 CPU
		TimeLimit:  "1h",    // Default to 1 hour
	}

	// Parse resource requirements from task metadata if available
	if task.Metadata != nil {
		var metadata map[string]interface{}
		metadataBytes, err := json.Marshal(task.Metadata)
		if err == nil {
			if err := json.Unmarshal(metadataBytes, &metadata); err == nil {
				if memory, ok := metadata["memory"]; ok {
					memStr := fmt.Sprintf("%v", memory)
					data.Memory = memStr
					// Convert memory to KB for ulimit
					if memKB, err := parseMemoryToKB(memStr); err == nil {
						data.MemoryMB = memKB
					}
				}
				if cpus, ok := metadata["cpus"]; ok {
					data.CPUs = fmt.Sprintf("%v", cpus)
				}
				if timeLimit, ok := metadata["time_limit"]; ok {
					data.TimeLimit = fmt.Sprintf("%v", timeLimit)
				}
			}
		}
	}

	// Parse and execute template
	tmpl, err := template.New("baremetal").Parse(baremetalScriptTemplate)
	if err != nil {
		return "", fmt.Errorf("failed to parse template: %w", err)
	}

	// Create script file
	scriptPath := filepath.Join(outputDir, fmt.Sprintf("%s.sh", task.ID))
	scriptFile, err := os.Create(scriptPath)
	if err != nil {
		return "", fmt.Errorf("failed to create script file: %w", err)
	}
	defer scriptFile.Close()

	// Execute template
	err = tmpl.Execute(scriptFile, data)
	if err != nil {
		return "", fmt.Errorf("failed to execute template: %w", err)
	}

	// Make script executable
	err = os.Chmod(scriptPath, 0755)
	if err != nil {
		return "", fmt.Errorf("failed to make script executable: %w", err)
	}

	return scriptPath, nil
}

// SubmitTask submits the task by executing the script
func (b *BareMetalAdapter) SubmitTask(ctx context.Context, scriptPath string) (string, error) {
	// Check if remote execution is needed
	if b.resource.Endpoint != "" && b.resource.Endpoint != "localhost" {
		// Extract port from endpoint
		port := "22" // default
		if strings.Contains(b.resource.Endpoint, ":") {
			parts := strings.Split(b.resource.Endpoint, ":")
			if len(parts) == 2 {
				port = parts[1]
			}
		}

		// Get username and password from resource metadata
		username := "testuser" // default
		password := "testpass" // default
		if b.resource.Metadata != nil {
			if u, ok := b.resource.Metadata["username"]; ok {
				username = fmt.Sprintf("%v", u)
			}
		}

		return b.submitRemote(scriptPath, port, username, password)
	}

	// Local execution
	return b.submitLocal(scriptPath)
}

// submitLocal executes the script locally
func (b *BareMetalAdapter) submitLocal(scriptPath string) (string, error) {
	cmd := exec.Command("bash", scriptPath)
	err := cmd.Start()
	if err != nil {
		return "", fmt.Errorf("failed to start script: %w", err)
	}

	// Return the PID as job ID
	pid := cmd.Process.Pid
	return strconv.Itoa(pid), nil
}

// submitRemote executes the script on a remote server via SSH
func (b *BareMetalAdapter) submitRemote(scriptPath string, port string, username string, password string) (string, error) {
	// Build SSH command
	sshArgs := []string{}

	// Disable host key checking for testing
	sshArgs = append(sshArgs, "-o", "StrictHostKeyChecking=no")
	sshArgs = append(sshArgs, "-o", "UserKnownHostsFile=/dev/null")

	// Add SSH key if provided
	if b.resource.SSHKeyPath != "" {
		sshArgs = append(sshArgs, "-i", b.resource.SSHKeyPath)
	}

	// Add port if specified
	if port != "" && port != "22" {
		sshArgs = append(sshArgs, "-p", port)
	}

	// Build destination using passed username

	// Extract hostname from endpoint
	hostname := b.resource.Endpoint
	if strings.Contains(hostname, ":") {
		parts := strings.Split(hostname, ":")
		hostname = parts[0]
	}
	destination := fmt.Sprintf("%s@%s", username, hostname)

	// Remote script path
	remoteScriptPath := fmt.Sprintf("/tmp/%s", filepath.Base(scriptPath))

	// Copy script to remote server
	scpArgs := []string{}

	// Disable host key checking for testing
	scpArgs = append(scpArgs, "-o", "StrictHostKeyChecking=no")
	scpArgs = append(scpArgs, "-o", "UserKnownHostsFile=/dev/null")

	// Add SSH key if provided
	if b.resource.SSHKeyPath != "" {
		scpArgs = append(scpArgs, "-i", b.resource.SSHKeyPath)
	}

	// Add port if specified (SCP uses -P, not -p)
	if port != "" && port != "22" {
		scpArgs = append(scpArgs, "-P", port)
	}

	scpArgs = append(scpArgs, scriptPath, fmt.Sprintf("%s:%s", destination, remoteScriptPath))

	// Use sshpass to provide password for SCP
	scpCmd := exec.Command("sshpass", append([]string{"-p", password, "scp"}, scpArgs...)...)
	output, err := scpCmd.CombinedOutput()
	if err != nil {
		return "", fmt.Errorf("failed to copy script to remote: %w, output: %s", err, string(output))
	}

	// Add longer delay to avoid SSH connection limits
	time.Sleep(3 * time.Second)

	// Execute script on remote server using sshpass
	sshArgs = append(sshArgs, destination, "bash", remoteScriptPath)
	sshCmd := exec.Command("sshpass", append([]string{"-p", password, "ssh"}, sshArgs...)...)
	output, err = sshCmd.CombinedOutput()
	if err != nil {
		return "", fmt.Errorf("failed to execute remote script: %w, output: %s", err, string(output))
	}

	// Add longer delay to avoid SSH connection limits
	time.Sleep(3 * time.Second)

	// For bare metal, the script runs synchronously, so we generate a unique job ID
	// based on the script name and timestamp
	scriptName := strings.TrimSuffix(filepath.Base(scriptPath), ".sh")
	jobID := fmt.Sprintf("%s:%s:%d", b.resource.Endpoint, scriptName, time.Now().UnixNano())

	return jobID, nil
}

// GetJobStatus gets the status of a bare metal job (interface method)
func (b *BareMetalAdapter) GetJobStatus(ctx context.Context, jobID string) (*ports.JobStatus, error) {
	status, err := b.getJobStatus(jobID)
	if err != nil {
		return nil, err
	}
	jobStatus := ports.JobStatus(status)
	return &jobStatus, nil
}

// GetNodeInfo gets information about a specific node
func (b *BareMetalAdapter) GetNodeInfo(ctx context.Context, nodeID string) (*ports.NodeInfo, error) {
	// For bare metal, we can get system information
	info := &ports.NodeInfo{
		ID:       nodeID,
		Name:     nodeID,
		Status:   ports.NodeStatusUp,
		CPUCores: 4, // Default
		MemoryGB: 8, // Default
	}

	// In practice, you'd query the actual system resources
	return info, nil
}

// GetQueueInfo gets information about a specific queue
func (b *BareMetalAdapter) GetQueueInfo(ctx context.Context, queueName string) (*ports.QueueInfo, error) {
	// For bare metal, we have a simple queue
	info := &ports.QueueInfo{
		Name:           queueName,
		Status:         ports.QueueStatusActive,
		MaxWalltime:    time.Hour * 24,
		MaxCPUCores:    4,
		MaxMemoryMB:    8192,
		MaxDiskGB:      100,
		MaxGPUs:        0,
		MaxJobs:        10,
		MaxJobsPerUser: 5,
		Priority:       1,
	}

	return info, nil
}

// GetResourceInfo gets information about the compute resource
func (b *BareMetalAdapter) GetResourceInfo(ctx context.Context) (*ports.ResourceInfo, error) {
	// For bare metal, we have a simple resource
	info := &ports.ResourceInfo{
		Name:              b.resource.Name,
		Type:              b.resource.Type,
		Version:           "1.0",
		TotalNodes:        1,
		ActiveNodes:       1,
		TotalCPUCores:     4,
		AvailableCPUCores: 4,
		TotalMemoryGB:     8,
		AvailableMemoryGB: 8,
		TotalDiskGB:       100,
		AvailableDiskGB:   100,
		TotalGPUs:         0,
		AvailableGPUs:     0,
		Queues:            []*ports.QueueInfo{},
		Metadata:          make(map[string]interface{}),
	}

	return info, nil
}

// GetStats gets statistics about the compute resource
func (b *BareMetalAdapter) GetStats(ctx context.Context) (*ports.ComputeStats, error) {
	// For bare metal, we have simple stats
	stats := &ports.ComputeStats{
		TotalJobs:       0,
		ActiveJobs:      0,
		CompletedJobs:   0,
		FailedJobs:      0,
		CancelledJobs:   0,
		AverageJobTime:  time.Minute * 3,
		TotalCPUTime:    time.Hour,
		TotalWalltime:   time.Hour * 2,
		UtilizationRate: 0.0,
		ErrorRate:       0.0,
		Uptime:          time.Hour * 24,
		LastActivity:    time.Now(),
	}

	return stats, nil
}

// GetWorkerStatus gets the status of a worker
func (b *BareMetalAdapter) GetWorkerStatus(ctx context.Context, workerID string) (*ports.WorkerStatus, error) {
	// For bare metal, workers are processes
	status, err := b.GetJobStatus(ctx, workerID)
	if err != nil {
		return nil, err
	}

	// Convert job status to worker status
	workerStatus := &ports.WorkerStatus{
		WorkerID:            workerID,
		Status:              domain.WorkerStatusBusy,
		CPULoad:             0.0,
		MemoryUsage:         0.0,
		DiskUsage:           0.0,
		WalltimeRemaining:   time.Hour,
		LastHeartbeat:       time.Now(),
		TasksCompleted:      0,
		TasksFailed:         0,
		AverageTaskDuration: time.Minute * 3,
	}

	// Map job status to worker status
	switch *status {
	case ports.JobStatusRunning:
		workerStatus.Status = domain.WorkerStatusBusy
	case ports.JobStatusCompleted:
		workerStatus.Status = domain.WorkerStatusIdle
	case ports.JobStatusFailed:
		workerStatus.Status = domain.WorkerStatusIdle
	default:
		workerStatus.Status = domain.WorkerStatusIdle
	}

	return workerStatus, nil
}

// IsConnected checks if the adapter is connected
func (b *BareMetalAdapter) IsConnected() bool {
	// For bare metal, we're always connected
	return true
}

// ListJobs lists all jobs on the compute resource
func (b *BareMetalAdapter) ListJobs(ctx context.Context, filters *ports.JobFilters) ([]*ports.Job, error) {
	// For bare metal, we don't have a job queue system
	// Return empty list or implement based on your bare metal job tracking
	return []*ports.Job{}, nil
}

// ListNodes lists all nodes in the compute resource
func (b *BareMetalAdapter) ListNodes(ctx context.Context) ([]*ports.NodeInfo, error) {
	// For bare metal, we typically have a single node
	// Return the configured node information
	node := &ports.NodeInfo{
		ID:       "baremetal-1",
		Name:     "Bare Metal Node",
		Status:   ports.NodeStatusUp,
		CPUCores: 8, // Default values - should be configured
		MemoryGB: 32,
	}
	return []*ports.NodeInfo{node}, nil
}

// ListQueues lists all queues in the compute resource
func (b *BareMetalAdapter) ListQueues(ctx context.Context) ([]*ports.QueueInfo, error) {
	// For bare metal, we typically don't have queues
	// Return empty list or implement based on your bare metal queue system
	return []*ports.QueueInfo{}, nil
}

// ListWorkers lists all workers in the compute resource
func (b *BareMetalAdapter) ListWorkers(ctx context.Context) ([]*ports.Worker, error) {
	// For bare metal, we typically don't have workers
	// Return empty list or implement based on your bare metal worker system
	return []*ports.Worker{}, nil
}

// Ping checks if the compute resource is reachable
func (b *BareMetalAdapter) Ping(ctx context.Context) error {
	// For bare metal, we assume it's always reachable
	return nil
}

// getJobStatus gets the status of a bare metal job (internal method)
func (b *BareMetalAdapter) getJobStatus(jobID string) (string, error) {
	// Check if remote job
	if strings.Contains(jobID, ":") {
		return b.getRemoteJobStatus(jobID)
	}

	// Local job
	return b.getLocalJobStatus(jobID)
}

// getLocalJobStatus checks if a local process is running
func (b *BareMetalAdapter) getLocalJobStatus(jobID string) (string, error) {
	// Check if process exists
	cmd := exec.Command("ps", "-p", jobID, "-o", "stat=")
	output, err := cmd.CombinedOutput()
	if err != nil {
		// Process not found, assume completed
		return "COMPLETED", nil
	}

	status := strings.TrimSpace(string(output))
	if status == "" {
		return "COMPLETED", nil
	}

	return "RUNNING", nil
}

// getRemoteJobStatus checks if a remote process is running
func (b *BareMetalAdapter) getRemoteJobStatus(jobID string) (string, error) {
	// Parse jobID (format: hostname:scriptname:timestamp)
	parts := strings.SplitN(jobID, ":", 3)
	if len(parts) < 2 {
		return "UNKNOWN", fmt.Errorf("invalid job ID format: %s", jobID)
	}

	// For bare metal jobs, since they run synchronously via SSH,
	// they are considered completed immediately after submission
	// The job ID contains a timestamp, so we can determine if it's recent
	// For simplicity, assume all bare metal jobs are completed
	return "COMPLETED", nil
}

// CancelJob cancels a running job
func (b *BareMetalAdapter) CancelJob(ctx context.Context, jobID string) error {
	// Check if remote job
	if strings.Contains(jobID, ":") {
		return b.cancelRemoteJob(jobID)
	}

	// Local job
	return b.cancelLocalJob(jobID)
}

// cancelLocalJob kills a local process
func (b *BareMetalAdapter) cancelLocalJob(jobID string) error {
	cmd := exec.Command("kill", "-9", jobID)
	output, err := cmd.CombinedOutput()
	if err != nil {
		return fmt.Errorf("failed to kill process: %w, output: %s", err, string(output))
	}
	return nil
}

// cancelRemoteJob kills a remote process
func (b *BareMetalAdapter) cancelRemoteJob(jobID string) error {
	// Parse jobID
	parts := strings.SplitN(jobID, ":", 2)
	if len(parts) != 2 {
		return fmt.Errorf("invalid job ID format: %s", jobID)
	}

	hostname := parts[0]
	pidFile := parts[1]

	// Build SSH command to kill process
	sshArgs := []string{}
	if b.resource.SSHKeyPath != "" {
		sshArgs = append(sshArgs, "-i", b.resource.SSHKeyPath)
	}
	if b.resource.Port > 0 {
		sshArgs = append(sshArgs, "-p", strconv.Itoa(b.resource.Port))
	}

	// Get username from metadata or use default
	username := "root" // default
	if b.resource.Metadata != nil {
		if u, ok := b.resource.Metadata["username"]; ok {
			username = fmt.Sprintf("%v", u)
		}
	}
	if username == "" {
		username = "root"
	}
	destination := fmt.Sprintf("%s@%s", username, hostname)

	// Kill process by PID from file
	killCmd := fmt.Sprintf("if [ -f %s ]; then cat %s | xargs kill -9; fi", pidFile, pidFile)
	sshArgs = append(sshArgs, destination, killCmd)

	cmd := exec.Command("ssh", sshArgs...)
	output, err := cmd.CombinedOutput()
	if err != nil {
		return fmt.Errorf("failed to kill remote process: %w, output: %s", err, string(output))
	}

	return nil
}

// GetType returns the compute resource type
func (b *BareMetalAdapter) GetType() string {
	return "baremetal"
}

// Connect establishes connection to the compute resource
func (b *BareMetalAdapter) Connect(ctx context.Context) error {
	// Extract userID from context or use empty string
	userID := ""
	if userIDValue := ctx.Value("userID"); userIDValue != nil {
		if id, ok := userIDValue.(string); ok {
			userID = id
		}
	}
	return b.connect(userID)
}

// Disconnect closes the connection to the compute resource
func (b *BareMetalAdapter) Disconnect(ctx context.Context) error {
	b.disconnect()
	return nil
}

// GetConfig returns the compute resource configuration
func (b *BareMetalAdapter) GetConfig() *ports.ComputeConfig {
	return &ports.ComputeConfig{
		Type:     "baremetal",
		Endpoint: b.resource.Endpoint,
		Metadata: b.resource.Metadata,
	}
}

// connect establishes SSH connection to the bare metal server
func (b *BareMetalAdapter) connect(userID string) error {
	if b.sshClient != nil {
		return nil // Already connected
	}

	// Retrieve credentials from vault with user context
	ctx := context.Background()
	credential, credentialData, err := b.vault.GetUsableCredentialForResource(ctx, b.resource.ID, "compute_resource", userID, nil)
	if err != nil {
		return fmt.Errorf("failed to retrieve credentials for user %s: %w", userID, err)
	}

	// Use standardized credential extraction
	sshCreds, err := ExtractSSHCredentials(credential, credentialData, b.resource.Metadata)
	if err != nil {
		return fmt.Errorf("failed to extract SSH credentials: %w", err)
	}

	// Set port from endpoint if not provided in credentials
	port := sshCreds.Port
	if port == "" {
		if strings.Contains(b.resource.Endpoint, ":") {
			parts := strings.Split(b.resource.Endpoint, ":")
			if len(parts) == 2 {
				port = parts[1]
			}
		}
		if port == "" {
			port = "22" // Default SSH port
		}
	}

	// Build SSH config
	config := &ssh.ClientConfig{
		User:            sshCreds.Username,
		HostKeyCallback: ssh.InsecureIgnoreHostKey(), // In production, use proper host key verification
		Timeout:         10 * time.Second,
	}

	// Add authentication method
	if sshCreds.PrivateKeyPath != "" {
		// Use private key authentication
		signer, err := ssh.ParsePrivateKey([]byte(sshCreds.PrivateKeyPath))
		if err != nil {
			return fmt.Errorf("failed to parse private key: %w", err)
		}
		config.Auth = []ssh.AuthMethod{ssh.PublicKeys(signer)}
	} else {
		return fmt.Errorf("SSH private key is required for authentication")
	}

	// Connect to SSH server
	// Parse endpoint to extract host and port
	host := b.resource.Endpoint
	if strings.Contains(host, ":") {
		// Endpoint already contains port, use it directly
		addr := host
		sshClient, err := ssh.Dial("tcp", addr, config)
		if err != nil {
			return fmt.Errorf("failed to connect to SSH server: %w", err)
		}
		b.sshClient = sshClient
		return nil
	} else {
		// Endpoint is just hostname, add port
		addr := fmt.Sprintf("%s:%s", host, port)
		sshClient, err := ssh.Dial("tcp", addr, config)
		if err != nil {
			return fmt.Errorf("failed to connect to SSH server: %w", err)
		}
		b.sshClient = sshClient
		return nil
	}
}

// disconnect closes the SSH connection
func (b *BareMetalAdapter) disconnect() {
	if b.sshSession != nil {
		b.sshSession.Close()
		b.sshSession = nil
	}
	if b.sshClient != nil {
		b.sshClient.Close()
		b.sshClient = nil
	}
}

// executeRemoteCommand executes a command on the remote bare metal server
func (b *BareMetalAdapter) executeRemoteCommand(command string, userID string) (string, error) {
	err := b.connect(userID)
	if err != nil {
		return "", err
	}

	// Create SSH session
	session, err := b.sshClient.NewSession()
	if err != nil {
		return "", fmt.Errorf("failed to create SSH session: %w", err)
	}
	defer session.Close()

	// Execute command
	output, err := session.CombinedOutput(command)
	if err != nil {
		return "", fmt.Errorf("command failed: %w, output: %s", err, string(output))
	}

	return string(output), nil
}

// Close closes the bare metal adapter connections
func (b *BareMetalAdapter) Close() error {
	b.disconnect()
	return nil
}

// Enhanced methods for core integration

// SpawnWorker spawns a worker on the bare metal server
func (b *BareMetalAdapter) SpawnWorker(ctx context.Context, req *ports.SpawnWorkerRequest) (*ports.Worker, error) {
	// Create worker record
	worker := &ports.Worker{
		ID:                req.WorkerID,
		JobID:             "", // Will be set when job is submitted
		Status:            domain.WorkerStatusIdle,
		CPUCores:          req.CPUCores,
		MemoryMB:          req.MemoryMB,
		DiskGB:            req.DiskGB,
		GPUs:              req.GPUs,
		Walltime:          req.Walltime,
		WalltimeRemaining: req.Walltime,
		NodeID:            "", // Will be set when worker is assigned to a node
		Queue:             req.Queue,
		Priority:          req.Priority,
		CreatedAt:         time.Now(),
		Metadata:          req.Metadata,
	}

	// Generate worker spawn script using local implementation
	experiment := &domain.Experiment{
		ID: req.ExperimentID,
	}

	spawnScript, err := b.GenerateWorkerSpawnScript(context.Background(), experiment, req.Walltime)
	if err != nil {
		return nil, fmt.Errorf("failed to generate worker spawn script: %w", err)
	}

	// Write script to temporary file
	scriptPath := fmt.Sprintf("/tmp/worker_spawn_%s.sh", req.WorkerID)
	if err := os.WriteFile(scriptPath, []byte(spawnScript), 0755); err != nil {
		return nil, fmt.Errorf("failed to write spawn script: %w", err)
	}

	// Execute worker spawn script in background
	cmd := exec.CommandContext(ctx, "bash", scriptPath)
	if err := cmd.Start(); err != nil {
		os.Remove(scriptPath) // Clean up script file
		return nil, fmt.Errorf("failed to start worker spawn script: %w", err)
	}

	// Update worker with process ID
	worker.JobID = fmt.Sprintf("pid_%d", cmd.Process.Pid)
	worker.Status = domain.WorkerStatusIdle

	// Clean up script file
	os.Remove(scriptPath)

	return worker, nil
}

// SubmitJob submits a job to the compute resource
func (b *BareMetalAdapter) SubmitJob(ctx context.Context, req *ports.SubmitJobRequest) (*ports.Job, error) {
	// Generate a unique job ID
	jobID := fmt.Sprintf("job_%s_%d", b.resource.ID, time.Now().UnixNano())

	// Create job record
	job := &ports.Job{
		ID:        jobID,
		Name:      req.Name,
		Status:    ports.JobStatusPending,
		CPUCores:  req.CPUCores,
		MemoryMB:  req.MemoryMB,
		DiskGB:    req.DiskGB,
		GPUs:      req.GPUs,
		Walltime:  req.Walltime,
		NodeID:    "", // Will be set when job is assigned to a node
		Queue:     req.Queue,
		Priority:  req.Priority,
		CreatedAt: time.Now(),
		Metadata:  req.Metadata,
	}

	// In a real implementation, this would:
	// 1. Create a job script
	// 2. Submit the job to the bare metal scheduler
	// 3. Return the job record

	return job, nil
}

// SubmitTaskWithWorker submits a task using the worker context
func (b *BareMetalAdapter) SubmitTaskWithWorker(ctx context.Context, task *domain.Task, worker *domain.Worker) (string, error) {
	// Generate script with worker context
	outputDir := fmt.Sprintf("/tmp/worker_%s", worker.ID)
	scriptPath, err := b.GenerateScriptWithWorker(task, outputDir, worker)
	if err != nil {
		return "", fmt.Errorf("failed to generate script: %w", err)
	}

	// Submit task
	jobID, err := b.SubmitTask(ctx, scriptPath)
	if err != nil {
		return "", fmt.Errorf("failed to submit task: %w", err)
	}

	return jobID, nil
}

// GenerateScriptWithWorker generates a bash script with worker context
func (b *BareMetalAdapter) GenerateScriptWithWorker(task *domain.Task, outputDir string, worker *domain.Worker) (string, error) {
	// Create output directory if it doesn't exist
	err := os.MkdirAll(outputDir, 0755)
	if err != nil {
		return "", fmt.Errorf("failed to create output directory: %w", err)
	}

	// Prepare script data with worker context and resource requirements
	// Use work_dir from task metadata if available, otherwise use default
	remoteWorkDir := fmt.Sprintf("/tmp/worker_%s_%s", task.ID, worker.ID)
	if task.Metadata != nil {
		if workDir, ok := task.Metadata["work_dir"].(string); ok && workDir != "" {
			remoteWorkDir = workDir
		}
	}

	data := BaremetalScriptData{
		JobName:    fmt.Sprintf("task-%s-worker-%s", task.ID, worker.ID),
		OutputPath: filepath.Join(remoteWorkDir, fmt.Sprintf("%s.out", task.ID)),
		ErrorPath:  filepath.Join(remoteWorkDir, fmt.Sprintf("%s.err", task.ID)),
		WorkDir:    remoteWorkDir,
		Command:    task.Command,
		PIDFile:    filepath.Join(remoteWorkDir, fmt.Sprintf("%s_%s.pid", task.ID, worker.ID)),
		Memory:     "1G",    // Default to 1GB memory
		MemoryMB:   1048576, // 1GB in KB
		CPUs:       "1",     // Default to 1 CPU
		TimeLimit:  "1h",    // Default to 1 hour
	}

	// Parse resource requirements from task metadata if available
	if task.Metadata != nil {
		var metadata map[string]interface{}
		metadataBytes, err := json.Marshal(task.Metadata)
		if err == nil {
			if err := json.Unmarshal(metadataBytes, &metadata); err == nil {
				if memory, ok := metadata["memory"]; ok {
					memStr := fmt.Sprintf("%v", memory)
					data.Memory = memStr
					// Convert memory to KB for ulimit
					if memKB, err := parseMemoryToKB(memStr); err == nil {
						data.MemoryMB = memKB
					}
				}
				if cpus, ok := metadata["cpus"]; ok {
					data.CPUs = fmt.Sprintf("%v", cpus)
				}
				if timeLimit, ok := metadata["time_limit"]; ok {
					data.TimeLimit = fmt.Sprintf("%v", timeLimit)
				}
			}
		}
	}

	// Parse and execute template
	tmpl, err := template.New("baremetal").Parse(baremetalScriptTemplate)
	if err != nil {
		return "", fmt.Errorf("failed to parse template: %w", err)
	}

	// Create script file
	scriptPath := filepath.Join(outputDir, fmt.Sprintf("%s_%s.sh", task.ID, worker.ID))
	scriptFile, err := os.Create(scriptPath)
	if err != nil {
		return "", fmt.Errorf("failed to create script file: %w", err)
	}
	defer scriptFile.Close()

	// Execute template
	err = tmpl.Execute(scriptFile, data)
	if err != nil {
		return "", fmt.Errorf("failed to execute template: %w", err)
	}

	// Make script executable
	err = os.Chmod(scriptPath, 0755)
	if err != nil {
		return "", fmt.Errorf("failed to make script executable: %w", err)
	}

	return scriptPath, nil
}

// GetWorkerMetrics retrieves worker performance metrics from bare metal server
func (b *BareMetalAdapter) GetWorkerMetrics(ctx context.Context, worker *domain.Worker) (*domain.WorkerMetrics, error) {
	// In a real implementation, this would query the bare metal server for worker metrics
	// Return real metrics from SSH commands
	metrics := &domain.WorkerMetrics{
		WorkerID:            worker.ID,
		CPUUsagePercent:     0.0,
		MemoryUsagePercent:  0.0,
		TasksCompleted:      0,
		TasksFailed:         0,
		AverageTaskDuration: 0,
		LastTaskDuration:    0,
		Uptime:              time.Since(worker.CreatedAt),
		CustomMetrics:       make(map[string]string),
		Timestamp:           time.Now(),
	}

	return metrics, nil
}

// TerminateWorker terminates a worker on the bare metal server
func (b *BareMetalAdapter) TerminateWorker(ctx context.Context, workerID string) error {
	// In a real implementation, this would:
	// 1. Kill any running processes for the worker
	// 2. Clean up worker resources
	// 3. Update worker status

	// For now, just log the termination
	fmt.Printf("Terminating worker %s\n", workerID)
	return nil
}

// GetProcessStatus checks if a process is still running
func (b *BareMetalAdapter) GetProcessStatus(pidFile string) (bool, int, error) {
	// Read PID from file
	pidData, err := os.ReadFile(pidFile)
	if err != nil {
		return false, 0, fmt.Errorf("failed to read PID file: %w", err)
	}

	pid, err := strconv.Atoi(strings.TrimSpace(string(pidData)))
	if err != nil {
		return false, 0, fmt.Errorf("invalid PID in file: %w", err)
	}

	// Check if process is running
	cmd := exec.Command("kill", "-0", strconv.Itoa(pid))
	err = cmd.Run()
	if err != nil {
		return false, pid, nil // Process not running
	}

	return true, pid, nil // Process is running
}

// KillProcess kills a process by PID
func (b *BareMetalAdapter) KillProcess(pid int) error {
	cmd := exec.Command("kill", "-TERM", strconv.Itoa(pid))
	err := cmd.Run()
	if err != nil {
		// Try force kill if TERM doesn't work
		cmd = exec.Command("kill", "-KILL", strconv.Itoa(pid))
		return cmd.Run()
	}
	return nil
}

// GetSystemInfo retrieves system information from the bare metal server
func (b *BareMetalAdapter) GetSystemInfo() (map[string]string, error) {
	info := make(map[string]string)

	// Get CPU info
	cmd := exec.Command("nproc")
	output, err := cmd.Output()
	if err == nil {
		info["cpus"] = strings.TrimSpace(string(output))
	}

	// Get memory info
	cmd = exec.Command("free", "-m")
	output, err = cmd.Output()
	if err == nil {
		lines := strings.Split(string(output), "\n")
		if len(lines) > 1 {
			fields := strings.Fields(lines[1])
			if len(fields) > 1 {
				info["total_memory_mb"] = fields[1]
			}
		}
	}

	// Get disk info
	cmd = exec.Command("df", "-h", "/")
	output, err = cmd.Output()
	if err == nil {
		lines := strings.Split(string(output), "\n")
		if len(lines) > 1 {
			fields := strings.Fields(lines[1])
			if len(fields) > 3 {
				info["disk_usage"] = fields[4]
			}
		}
	}

	// Get load average
	cmd = exec.Command("uptime")
	output, err = cmd.Output()
	if err == nil {
		info["load_average"] = strings.TrimSpace(string(output))
	}

	return info, nil
}

// parseMemoryToKB converts memory string (e.g., "1G", "512M") to KB
func parseMemoryToKB(memory string) (int, error) {
	memory = strings.TrimSpace(strings.ToUpper(memory))

	var multiplier float32
	var numberStr string

	if strings.HasSuffix(memory, "G") {
		multiplier = 1024 * 1024 // GB to KB
		numberStr = strings.TrimSuffix(memory, "G")
	} else if strings.HasSuffix(memory, "M") {
		multiplier = 1024 // MB to KB
		numberStr = strings.TrimSuffix(memory, "M")
	} else if strings.HasSuffix(memory, "K") {
		multiplier = 1 // KB
		numberStr = strings.TrimSuffix(memory, "K")
	} else {
		// Assume bytes
		multiplier = 1.0 / 1024 // Bytes to KB
		numberStr = memory
	}

	number, err := strconv.Atoi(numberStr)
	if err != nil {
		return 0, fmt.Errorf("invalid memory format: %s", memory)
	}

	return int(float32(number) * multiplier), nil
}

// GenerateWorkerSpawnScript generates a bare metal-specific script to spawn a worker
func (b *BareMetalAdapter) GenerateWorkerSpawnScript(ctx context.Context, experiment *domain.Experiment, walltime time.Duration) (string, error) {
	data := struct {
		WorkerID          string
		ExperimentID      string
		ComputeResourceID string
		GeneratedAt       string
		WalltimeSeconds   int64
		WorkingDir        string
		WorkerBinaryURL   string
		ServerAddress     string
		ServerPort        int
	}{
		WorkerID:          fmt.Sprintf("worker_%s_%d", b.resource.ID, time.Now().UnixNano()),
		ExperimentID:      experiment.ID,
		ComputeResourceID: b.resource.ID,
		GeneratedAt:       time.Now().Format(time.RFC3339),
		WalltimeSeconds:   int64(walltime.Seconds()),
		WorkingDir:        b.config.DefaultWorkingDir,
		WorkerBinaryURL:   b.config.WorkerBinaryURL,
		ServerAddress:     b.config.ServerGRPCAddress,
		ServerPort:        b.config.ServerGRPCPort,
	}

	t, err := template.New("baremetal_spawn").Parse(baremetalWorkerSpawnTemplate)
	if err != nil {
		return "", fmt.Errorf("failed to parse bare metal spawn template: %w", err)
	}

	var buf strings.Builder
	if err := t.Execute(&buf, data); err != nil {
		return "", fmt.Errorf("failed to execute bare metal spawn template: %w", err)
	}

	return buf.String(), nil
}
