package adapters

import (
	"context"
	"fmt"
	"os"
	"os/exec"
	"path/filepath"
	"strings"
	"text/template"
	"time"

	"github.com/apache/airavata/scheduler/core/domain"
	ports "github.com/apache/airavata/scheduler/core/port"
	v1 "k8s.io/api/batch/v1"
	corev1 "k8s.io/api/core/v1"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/client-go/kubernetes"
	"k8s.io/client-go/rest"
	"k8s.io/client-go/tools/clientcmd"
	metricsclientset "k8s.io/metrics/pkg/client/clientset/versioned"
)

// KubernetesAdapter implements the ComputeAdapter interface for Kubernetes clusters
type KubernetesAdapter struct {
	resource      domain.ComputeResource
	vault         domain.CredentialVault
	clientset     *kubernetes.Clientset
	metricsClient metricsclientset.Interface
	namespace     string
	config        *ScriptConfig
}

// Compile-time interface verification
var _ ports.ComputePort = (*KubernetesAdapter)(nil)

// NewKubernetesAdapter creates a new Kubernetes adapter
func NewKubernetesAdapter(resource domain.ComputeResource, vault domain.CredentialVault) *KubernetesAdapter {
	return NewKubernetesAdapterWithConfig(resource, vault, nil)
}

// NewKubernetesAdapterWithConfig creates a new Kubernetes adapter with custom script configuration
func NewKubernetesAdapterWithConfig(resource domain.ComputeResource, vault domain.CredentialVault, config *ScriptConfig) *KubernetesAdapter {
	if config == nil {
		config = &ScriptConfig{
			WorkerBinaryURL:   "https://server/api/worker-binary",
			ServerGRPCAddress: "scheduler", // Use service name for container-to-container communication
			ServerGRPCPort:    50051,
			DefaultWorkingDir: "/tmp/worker",
		}
	}
	return &KubernetesAdapter{
		resource:  resource,
		vault:     vault,
		namespace: "default", // Default namespace
		config:    config,
	}
}

// kubernetesWorkerSpawnTemplate defines the Kubernetes worker spawn pod template
const kubernetesWorkerSpawnTemplate = `apiVersion: v1
kind: Pod
metadata:
  name: worker-{{.WorkerID}}
  labels:
    app: airavata-worker
    experiment-id: "{{.ExperimentID}}"
    compute-resource-id: "{{.ComputeResourceID}}"
spec:
  restartPolicy: Never
  activeDeadlineSeconds: {{.WalltimeSeconds}}
  containers:
  - name: worker
    image: ubuntu:20.04
    command: ["/bin/bash"]
    args:
      - -c
      - |
        set -euo pipefail
        
        # Install curl
        apt-get update && apt-get install -y curl
        
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
        
        # Start worker
        echo "Starting worker: $WORKER_ID"
        exec ./worker \
            --server-url="$SERVER_URL" \
            --worker-id="$WORKER_ID" \
            --experiment-id="$EXPERIMENT_ID" \
            --compute-resource-id="$COMPUTE_RESOURCE_ID" \
            --working-dir="$WORK_DIR"
    resources:
      requests:
        cpu: "{{.CPUCores}}"
        memory: "{{.MemoryMB}}Mi"
      limits:
        cpu: "{{.CPUCores}}"
        memory: "{{.MemoryMB}}Mi"
{{if .GPUs}}
      limits:
        nvidia.com/gpu: {{.GPUs}}
{{end}}
    env:
    - name: WORKER_ID
      value: "{{.WorkerID}}"
    - name: EXPERIMENT_ID
      value: "{{.ExperimentID}}"
    - name: COMPUTE_RESOURCE_ID
      value: "{{.ComputeResourceID}}"
    - name: SERVER_URL
      value: "grpc://{{.ServerAddress}}:{{.ServerPort}}"
`

// connect establishes connection to the Kubernetes cluster
func (k *KubernetesAdapter) connect() error {
	if k.clientset != nil {
		return nil // Already connected
	}

	// Unmarshal resource metadata
	// Extract resource metadata
	resourceMetadata := make(map[string]string)
	if k.resource.Metadata != nil {
		for key, value := range k.resource.Metadata {
			resourceMetadata[key] = fmt.Sprintf("%v", value)
		}
	}

	// Get namespace from resource metadata
	if ns, ok := resourceMetadata["namespace"]; ok {
		k.namespace = ns
	}

	// Create Kubernetes client
	var config *rest.Config
	var err error

	// Check if we're running inside a cluster
	if k.resource.Endpoint == "" || k.resource.Endpoint == "in-cluster" {
		// Use in-cluster config
		config, err = rest.InClusterConfig()
		if err != nil {
			return fmt.Errorf("failed to create in-cluster config: %w", err)
		}
	} else {
		// Use external cluster config
		// Get kubeconfig path from resource metadata
		kubeconfigPath := ""
		if kubeconfig, ok := resourceMetadata["kubeconfig"]; ok {
			kubeconfigPath = kubeconfig
		} else {
			// Use default kubeconfig location
			kubeconfigPath = filepath.Join(homeDir(), ".kube", "config")
		}

		// Build config from kubeconfig file
		loadingRules := clientcmd.NewDefaultClientConfigLoadingRules()
		loadingRules.ExplicitPath = kubeconfigPath

		// Get context from metadata or use current context
		context := ""
		if ctx, ok := resourceMetadata["context"]; ok {
			context = ctx
		}

		configOverrides := &clientcmd.ConfigOverrides{}
		if context != "" {
			configOverrides.CurrentContext = context
		}

		kubeConfig := clientcmd.NewNonInteractiveDeferredLoadingClientConfig(
			loadingRules,
			configOverrides,
		)

		config, err = kubeConfig.ClientConfig()
		if err != nil {
			return fmt.Errorf("failed to build config from kubeconfig %s: %w", kubeconfigPath, err)
		}
	}

	// Create clientset
	clientset, err := kubernetes.NewForConfig(config)
	if err != nil {
		return fmt.Errorf("failed to create Kubernetes clientset: %w", err)
	}

	// Create metrics client
	metricsClient, err := metricsclientset.NewForConfig(config)
	if err != nil {
		// Metrics client is optional - if metrics-server is not available, we'll handle gracefully
		// Don't fail the connection, just log the warning
		fmt.Printf("Warning: Failed to create metrics client (metrics-server may not be available): %v\n", err)
		metricsClient = nil
	}

	k.clientset = clientset
	k.metricsClient = metricsClient
	return nil
}

// GenerateScript generates a Kubernetes Job manifest for the task
func (k *KubernetesAdapter) GenerateScript(task domain.Task, outputDir string) (string, error) {
	err := k.connect()
	if err != nil {
		return "", err
	}

	// Create job manifest
	job := &v1.Job{
		ObjectMeta: metav1.ObjectMeta{
			Name:      fmt.Sprintf("task-%s", task.ID),
			Namespace: k.namespace,
			Labels: map[string]string{
				"app":        "airavata-scheduler",
				"task-id":    task.ID,
				"experiment": task.ExperimentID,
			},
		},
		Spec: v1.JobSpec{
			Template: corev1.PodTemplateSpec{
				ObjectMeta: metav1.ObjectMeta{
					Labels: map[string]string{
						"app":        "airavata-scheduler",
						"task-id":    task.ID,
						"experiment": task.ExperimentID,
					},
				},
				Spec: corev1.PodSpec{
					Containers: []corev1.Container{
						{
							Name:    "task-executor",
							Image:   k.getContainerImage(),
							Command: []string{"/bin/bash", "-c"},
							Args:    []string{task.Command},
							Env:     k.getEnvironmentVariables(task),
							VolumeMounts: []corev1.VolumeMount{
								{
									Name:      "output-volume",
									MountPath: "/output",
								},
							},
						},
					},
					RestartPolicy: corev1.RestartPolicyNever,
					Volumes: []corev1.Volume{
						{
							Name: "output-volume",
							VolumeSource: corev1.VolumeSource{
								EmptyDir: &corev1.EmptyDirVolumeSource{},
							},
						},
					},
				},
			},
			BackoffLimit: int32Ptr(3),
		},
	}

	// Save job manifest to file
	manifestPath := filepath.Join(outputDir, fmt.Sprintf("%s-job.yaml", task.ID))
	err = k.saveJobManifest(job, manifestPath)
	if err != nil {
		return "", fmt.Errorf("failed to save job manifest: %w", err)
	}

	return manifestPath, nil
}

// SubmitTask submits the task to Kubernetes using kubectl apply
func (k *KubernetesAdapter) SubmitTask(ctx context.Context, scriptPath string) (string, error) {
	err := k.connect()
	if err != nil {
		return "", err
	}

	// Apply the job manifest
	job, err := k.applyJobManifest(ctx, scriptPath)
	if err != nil {
		return "", fmt.Errorf("failed to apply job manifest: %w", err)
	}

	return job.Name, nil
}

// GetJobStatus gets the status of a Kubernetes job (interface method)
func (k *KubernetesAdapter) GetJobStatus(ctx context.Context, jobID string) (*ports.JobStatus, error) {
	status, err := k.getJobStatus(ctx, jobID)
	if err != nil {
		return nil, err
	}
	jobStatus := ports.JobStatus(status)
	return &jobStatus, nil
}

// GetNodeInfo gets information about a specific node
func (k *KubernetesAdapter) GetNodeInfo(ctx context.Context, nodeID string) (*ports.NodeInfo, error) {
	err := k.connect()
	if err != nil {
		return nil, err
	}

	// Get node from Kubernetes
	node, err := k.clientset.CoreV1().Nodes().Get(ctx, nodeID, metav1.GetOptions{})
	if err != nil {
		return nil, fmt.Errorf("failed to get node: %w", err)
	}

	// Extract node information
	info := &ports.NodeInfo{
		ID:       nodeID,
		Name:     node.Name,
		Status:   ports.NodeStatusUp,
		CPUCores: 0,
		MemoryGB: 0,
	}

	// Parse resource capacity
	if cpu, exists := node.Status.Capacity["cpu"]; exists {
		if cores, ok := cpu.AsInt64(); ok {
			info.CPUCores = int(cores)
		}
	}
	if memory, exists := node.Status.Capacity["memory"]; exists {
		if mem, ok := memory.AsInt64(); ok {
			info.MemoryGB = int(mem / (1024 * 1024 * 1024)) // Convert to GB
		}
	}

	return info, nil
}

// GetQueueInfo gets information about a specific queue
func (k *KubernetesAdapter) GetQueueInfo(ctx context.Context, queueName string) (*ports.QueueInfo, error) {
	// For Kubernetes, we can get namespace information
	info := &ports.QueueInfo{
		Name:           queueName,
		Status:         ports.QueueStatusActive,
		MaxWalltime:    time.Hour * 24,
		MaxCPUCores:    8,
		MaxMemoryMB:    16384,
		MaxDiskGB:      100,
		MaxGPUs:        0,
		MaxJobs:        100,
		MaxJobsPerUser: 10,
		Priority:       1,
	}

	// In practice, you'd query the Kubernetes cluster for actual queue info
	return info, nil
}

// GetResourceInfo gets information about the compute resource
func (k *KubernetesAdapter) GetResourceInfo(ctx context.Context) (*ports.ResourceInfo, error) {
	// For Kubernetes, we can get cluster information
	info := &ports.ResourceInfo{
		Name:              k.resource.Name,
		Type:              k.resource.Type,
		Version:           "1.0",
		TotalNodes:        0,
		ActiveNodes:       0,
		TotalCPUCores:     0,
		AvailableCPUCores: 0,
		TotalMemoryGB:     0,
		AvailableMemoryGB: 0,
		TotalDiskGB:       0,
		AvailableDiskGB:   0,
		TotalGPUs:         0,
		AvailableGPUs:     0,
		Queues:            []*ports.QueueInfo{},
		Metadata:          make(map[string]interface{}),
	}

	// In practice, you'd query the Kubernetes cluster for actual resource info
	return info, nil
}

// GetStats gets statistics about the compute resource
func (k *KubernetesAdapter) GetStats(ctx context.Context) (*ports.ComputeStats, error) {
	// For Kubernetes, we have simple stats
	stats := &ports.ComputeStats{
		TotalJobs:       0,
		ActiveJobs:      0,
		CompletedJobs:   0,
		FailedJobs:      0,
		CancelledJobs:   0,
		AverageJobTime:  time.Minute * 10,
		TotalCPUTime:    time.Hour,
		TotalWalltime:   time.Hour * 2,
		UtilizationRate: 0.0,
		ErrorRate:       0.0,
		Uptime:          time.Hour * 24,
		LastActivity:    time.Now(),
	}

	// In practice, you'd query the Kubernetes cluster for actual stats
	return stats, nil
}

// GetWorkerStatus gets the status of a worker
func (k *KubernetesAdapter) GetWorkerStatus(ctx context.Context, workerID string) (*ports.WorkerStatus, error) {
	// For Kubernetes, workers are pods
	status, err := k.GetJobStatus(ctx, workerID)
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
		AverageTaskDuration: time.Minute * 10,
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
func (k *KubernetesAdapter) IsConnected() bool {
	// For Kubernetes, we can check if the client is initialized
	return k.clientset != nil
}

// ListJobs lists all jobs on the compute resource
func (k *KubernetesAdapter) ListJobs(ctx context.Context, filters *ports.JobFilters) ([]*ports.Job, error) {
	err := k.connect()
	if err != nil {
		return nil, err
	}

	// List jobs from Kubernetes
	jobs, err := k.clientset.BatchV1().Jobs(k.namespace).List(ctx, metav1.ListOptions{})
	if err != nil {
		return nil, fmt.Errorf("failed to list jobs: %w", err)
	}

	var jobList []*ports.Job
	for _, job := range jobs.Items {
		// Map Kubernetes job status to our job status
		var status ports.JobStatus
		if job.Status.Succeeded > 0 {
			status = ports.JobStatusCompleted
		} else if job.Status.Failed > 0 {
			status = ports.JobStatusFailed
		} else if job.Status.Active > 0 {
			status = ports.JobStatusRunning
		} else {
			status = ports.JobStatusPending
		}

		jobInfo := &ports.Job{
			ID:     job.Name,
			Name:   job.Name,
			Status: status,
			NodeID: "", // Kubernetes doesn't directly map to nodes in job info
		}

		// Apply filters if provided
		if filters != nil {
			if filters.UserID != nil && *filters.UserID != "" && jobInfo.Metadata["userID"] != *filters.UserID {
				continue
			}
			if filters.Status != nil && string(jobInfo.Status) != string(*filters.Status) {
				continue
			}
		}

		jobList = append(jobList, jobInfo)
	}

	return jobList, nil
}

// ListNodes lists all nodes in the compute resource
func (k *KubernetesAdapter) ListNodes(ctx context.Context) ([]*ports.NodeInfo, error) {
	err := k.connect()
	if err != nil {
		return nil, err
	}

	// List nodes from Kubernetes
	nodes, err := k.clientset.CoreV1().Nodes().List(ctx, metav1.ListOptions{})
	if err != nil {
		return nil, fmt.Errorf("failed to list nodes: %w", err)
	}

	var nodeList []*ports.NodeInfo
	for _, node := range nodes.Items {
		nodeInfo := &ports.NodeInfo{
			ID:       node.Name,
			Name:     node.Name,
			Status:   ports.NodeStatusUp, // Default to up
			CPUCores: 0,
			MemoryGB: 0,
		}

		// Parse resource capacity
		if cpu, exists := node.Status.Capacity["cpu"]; exists {
			if cores, ok := cpu.AsInt64(); ok {
				nodeInfo.CPUCores = int(cores)
			}
		}
		if memory, exists := node.Status.Capacity["memory"]; exists {
			if mem, ok := memory.AsInt64(); ok {
				nodeInfo.MemoryGB = int(mem / (1024 * 1024 * 1024)) // Convert to GB
			}
		}

		nodeList = append(nodeList, nodeInfo)
	}

	return nodeList, nil
}

// ListQueues lists all queues in the compute resource
func (k *KubernetesAdapter) ListQueues(ctx context.Context) ([]*ports.QueueInfo, error) {
	// For Kubernetes, we don't have traditional queues
	// Return empty list or implement based on your Kubernetes queue system
	return []*ports.QueueInfo{}, nil
}

// ListWorkers lists all workers in the compute resource
func (k *KubernetesAdapter) ListWorkers(ctx context.Context) ([]*ports.Worker, error) {
	// For Kubernetes, we typically don't have workers in the traditional sense
	// Return empty list or implement based on your Kubernetes worker system
	return []*ports.Worker{}, nil
}

// Ping checks if the compute resource is reachable
func (k *KubernetesAdapter) Ping(ctx context.Context) error {
	err := k.connect()
	if err != nil {
		return err
	}

	// Try to list namespaces to check connectivity
	_, err = k.clientset.CoreV1().Namespaces().List(ctx, metav1.ListOptions{Limit: 1})
	if err != nil {
		return fmt.Errorf("failed to ping Kubernetes: %w", err)
	}

	return nil
}

// getJobStatus gets the status of a Kubernetes job (internal method)
func (k *KubernetesAdapter) getJobStatus(ctx context.Context, jobID string) (string, error) {
	err := k.connect()
	if err != nil {
		return "", err
	}

	// Get job from Kubernetes
	job, err := k.clientset.BatchV1().Jobs(k.namespace).Get(ctx, jobID, metav1.GetOptions{})
	if err != nil {
		return "UNKNOWN", fmt.Errorf("failed to get job: %w", err)
	}

	// Check job conditions
	for _, condition := range job.Status.Conditions {
		if condition.Type == v1.JobComplete && condition.Status == corev1.ConditionTrue {
			return "COMPLETED", nil
		}
		if condition.Type == v1.JobFailed && condition.Status == corev1.ConditionTrue {
			return "FAILED", nil
		}
	}

	// Check if job is running
	if job.Status.Active > 0 {
		return "RUNNING", nil
	}

	// Check if job is pending
	if job.Status.Succeeded == 0 && job.Status.Failed == 0 {
		return "PENDING", nil
	}

	return "UNKNOWN", nil
}

// CancelJob cancels a Kubernetes job
func (k *KubernetesAdapter) CancelJob(ctx context.Context, jobID string) error {
	err := k.connect()
	if err != nil {
		return err
	}

	// Delete the job
	err = k.clientset.BatchV1().Jobs(k.namespace).Delete(ctx, jobID, metav1.DeleteOptions{})
	if err != nil {
		return fmt.Errorf("failed to delete job: %w", err)
	}

	return nil
}

// GetType returns the compute resource type
func (k *KubernetesAdapter) GetType() string {
	return "kubernetes"
}

// Connect establishes connection to the compute resource
func (k *KubernetesAdapter) Connect(ctx context.Context) error {
	return k.connect()
}

// Disconnect closes the connection to the compute resource
func (k *KubernetesAdapter) Disconnect(ctx context.Context) error {
	// No persistent connections to close for Kubernetes
	return nil
}

// GetConfig returns the compute resource configuration
func (k *KubernetesAdapter) GetConfig() *ports.ComputeConfig {
	return &ports.ComputeConfig{
		Type:     "kubernetes",
		Endpoint: k.resource.Endpoint,
		Metadata: k.resource.Metadata,
	}
}

// getContainerImage returns the container image to use
func (k *KubernetesAdapter) getContainerImage() string {
	// Extract resource metadata
	resourceMetadata := make(map[string]string)
	if k.resource.Metadata != nil {
		for key, value := range k.resource.Metadata {
			resourceMetadata[key] = fmt.Sprintf("%v", value)
		}
	}

	if image, ok := resourceMetadata["container_image"]; ok {
		return image
	}
	return "airavata/scheduler-worker:latest" // Default worker image
}

// getEnvironmentVariables returns environment variables for the task
func (k *KubernetesAdapter) getEnvironmentVariables(task domain.Task) []corev1.EnvVar {
	envVars := []corev1.EnvVar{
		{
			Name:  "TASK_ID",
			Value: task.ID,
		},
		{
			Name:  "EXPERIMENT_ID",
			Value: task.ExperimentID,
		},
		{
			Name:  "OUTPUT_DIR",
			Value: "/output",
		},
	}

	// Add task-specific environment variables from metadata
	if task.Metadata != nil {
		for key, value := range task.Metadata {
			envVars = append(envVars, corev1.EnvVar{
				Name:  key,
				Value: fmt.Sprintf("%v", value),
			})
		}
	}

	return envVars
}

// saveJobManifest saves a job manifest to a file
func (k *KubernetesAdapter) saveJobManifest(job *v1.Job, path string) error {
	// This would typically use a YAML marshaler
	// For now, we'll create a simple YAML representation
	yamlContent := fmt.Sprintf(`apiVersion: batch/v1
kind: Job
metadata:
  name: %s
  namespace: %s
  labels:
    app: airavata-scheduler
    task-id: %s
    experiment: %s
spec:
  template:
    metadata:
      labels:
        app: airavata-scheduler
        task-id: %s
        experiment: %s
    spec:
      containers:
      - name: task-executor
        image: %s
        command: ["/bin/bash", "-c"]
        args: ["%s"]
        env:
        - name: TASK_ID
          value: "%s"
        - name: EXPERIMENT_ID
          value: "%s"
        - name: OUTPUT_DIR
          value: "/output"
        volumeMounts:
        - name: output-volume
          mountPath: /output
      restartPolicy: Never
      volumes:
      - name: output-volume
        emptyDir: {}
  backoffLimit: 3
`,
		job.Name,
		job.Namespace,
		job.Labels["task-id"],
		job.Labels["experiment"],
		job.Labels["task-id"],
		job.Labels["experiment"],
		k.getContainerImage(),
		strings.ReplaceAll(job.Spec.Template.Spec.Containers[0].Args[0], `"`, `\"`),
		job.Labels["task-id"],
		job.Labels["experiment"],
	)

	// Write to file
	err := k.writeToFile(path, yamlContent)
	if err != nil {
		return fmt.Errorf("failed to write job manifest: %w", err)
	}

	return nil
}

// applyJobManifest applies a job manifest to the Kubernetes cluster
func (k *KubernetesAdapter) applyJobManifest(ctx context.Context, manifestPath string) (*v1.Job, error) {
	// Read the manifest file
	content, err := k.readFromFile(manifestPath)
	if err != nil {
		return nil, fmt.Errorf("failed to read manifest file: %w", err)
	}

	// Parse the YAML (simplified - in production, use proper YAML parser)
	job, err := k.parseJobManifest(content)
	if err != nil {
		return nil, fmt.Errorf("failed to parse job manifest: %w", err)
	}

	// Create the job in Kubernetes
	createdJob, err := k.clientset.BatchV1().Jobs(k.namespace).Create(ctx, job, metav1.CreateOptions{})
	if err != nil {
		return nil, fmt.Errorf("failed to create job: %w", err)
	}

	return createdJob, nil
}

// parseJobManifest parses a job manifest from YAML content
func (k *KubernetesAdapter) parseJobManifest(content string) (*v1.Job, error) {
	// This is a simplified parser - in production, use proper YAML parsing
	// For now, we'll create a basic job structure
	lines := strings.Split(content, "\n")
	var jobName, namespace, command string

	for _, line := range lines {
		line = strings.TrimSpace(line)
		if strings.HasPrefix(line, "name:") {
			jobName = strings.TrimSpace(strings.TrimPrefix(line, "name:"))
		} else if strings.HasPrefix(line, "namespace:") {
			namespace = strings.TrimSpace(strings.TrimPrefix(line, "namespace:"))
		} else if strings.HasPrefix(line, "args: [\"") {
			command = strings.TrimSpace(strings.TrimPrefix(line, "args: [\""))
			command = strings.TrimSuffix(command, "\"]")
		}
	}

	if jobName == "" {
		return nil, fmt.Errorf("job name not found in manifest")
	}

	// Create job object
	job := &v1.Job{
		ObjectMeta: metav1.ObjectMeta{
			Name:      jobName,
			Namespace: namespace,
		},
		Spec: v1.JobSpec{
			Template: corev1.PodTemplateSpec{
				Spec: corev1.PodSpec{
					Containers: []corev1.Container{
						{
							Name:    "task-executor",
							Image:   k.getContainerImage(),
							Command: []string{"/bin/bash", "-c"},
							Args:    []string{command},
						},
					},
					RestartPolicy: corev1.RestartPolicyNever,
				},
			},
			BackoffLimit: int32Ptr(3),
		},
	}

	return job, nil
}

// writeToFile writes content to a file
func (k *KubernetesAdapter) writeToFile(path, content string) error {
	// Create directory if it doesn't exist
	dir := filepath.Dir(path)
	if err := os.MkdirAll(dir, 0755); err != nil {
		return fmt.Errorf("failed to create directory %s: %w", dir, err)
	}

	// Write content to file with proper permissions
	err := os.WriteFile(path, []byte(content), 0644)
	if err != nil {
		return fmt.Errorf("failed to write file %s: %w", path, err)
	}

	return nil
}

// readFromFile reads content from a file
func (k *KubernetesAdapter) readFromFile(path string) (string, error) {
	// Check if file exists
	if _, err := os.Stat(path); os.IsNotExist(err) {
		return "", fmt.Errorf("file does not exist: %s", path)
	}

	// Read file content
	content, err := os.ReadFile(path)
	if err != nil {
		return "", fmt.Errorf("failed to read file %s: %w", path, err)
	}

	return string(content), nil
}

// homeDir returns the home directory
func homeDir() string {
	if h := os.Getenv("HOME"); h != "" {
		return h
	}
	return os.Getenv("USERPROFILE") // windows
}

// int32Ptr returns a pointer to an int32
func int32Ptr(i int32) *int32 { return &i }

// Close closes the Kubernetes adapter connections
func (k *KubernetesAdapter) Close() error {
	// No persistent connections to close
	return nil
}

// SpawnWorker spawns a worker on the Kubernetes cluster
func (k *KubernetesAdapter) SpawnWorker(ctx context.Context, req *ports.SpawnWorkerRequest) (*ports.Worker, error) {
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

	spawnScript, err := k.GenerateWorkerSpawnScript(context.Background(), experiment, req.Walltime)
	if err != nil {
		return nil, fmt.Errorf("failed to generate worker spawn script: %w", err)
	}

	// Write script to temporary file
	scriptPath := fmt.Sprintf("/tmp/worker_spawn_%s.yaml", req.WorkerID)
	if err := os.WriteFile(scriptPath, []byte(spawnScript), 0644); err != nil {
		return nil, fmt.Errorf("failed to write spawn script: %w", err)
	}

	// Apply the Kubernetes pod specification
	cmd := exec.CommandContext(ctx, "kubectl", "apply", "-f", scriptPath)
	if err := cmd.Run(); err != nil {
		os.Remove(scriptPath) // Clean up script file
		return nil, fmt.Errorf("failed to apply worker pod: %w", err)
	}

	// Update worker with pod name
	worker.JobID = fmt.Sprintf("pod_%s", req.WorkerID)
	worker.Status = domain.WorkerStatusIdle

	// Clean up script file
	os.Remove(scriptPath)

	return worker, nil
}

// SubmitJob submits a job to the compute resource
func (k *KubernetesAdapter) SubmitJob(ctx context.Context, req *ports.SubmitJobRequest) (*ports.Job, error) {
	// Generate a unique job ID
	jobID := fmt.Sprintf("job_%s_%d", k.resource.ID, time.Now().UnixNano())

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
	// 1. Create a Kubernetes Job resource
	// 2. Submit the job to the cluster
	// 3. Return the job record

	return job, nil
}

// SubmitTaskWithWorker submits a task using the worker context
func (k *KubernetesAdapter) SubmitTaskWithWorker(ctx context.Context, task *domain.Task, worker *domain.Worker) (string, error) {
	// Generate script with worker context
	outputDir := fmt.Sprintf("/tmp/worker_%s", worker.ID)
	scriptPath, err := k.GenerateScript(*task, outputDir)
	if err != nil {
		return "", fmt.Errorf("failed to generate script: %w", err)
	}

	// Submit task
	jobID, err := k.SubmitTask(ctx, scriptPath)
	if err != nil {
		return "", fmt.Errorf("failed to submit task: %w", err)
	}

	return jobID, nil
}

// GetWorkerMetrics retrieves worker performance metrics from Kubernetes
// GetWorkerMetrics retrieves worker performance metrics from Kubernetes cluster
func (k *KubernetesAdapter) GetWorkerMetrics(ctx context.Context, worker *domain.Worker) (*domain.WorkerMetrics, error) {
	// Query Kubernetes metrics API for real metrics
	// This would require the metrics-server to be installed in the cluster
	metrics := &domain.WorkerMetrics{
		WorkerID:            worker.ID,
		CPUUsagePercent:     k.getCPUUsageFromK8s(ctx, worker.ID),
		MemoryUsagePercent:  k.getMemoryUsageFromK8s(ctx, worker.ID),
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

// getCPUUsageFromK8s queries Kubernetes metrics API for CPU usage
func (k *KubernetesAdapter) getCPUUsageFromK8s(ctx context.Context, workerID string) float64 {
	if k.metricsClient == nil {
		// Metrics client not available (metrics-server not installed)
		return 0.0
	}

	// Find the pod for this worker
	podName := fmt.Sprintf("worker-%s", workerID)

	// Get pod metrics
	podMetrics, err := k.metricsClient.MetricsV1beta1().PodMetricses(k.namespace).Get(ctx, podName, metav1.GetOptions{})
	if err != nil {
		// Pod not found or metrics not available
		return 0.0
	}

	// Calculate total CPU usage across all containers
	var totalCPUUsage int64
	for _, container := range podMetrics.Containers {
		if container.Usage.Cpu() != nil {
			totalCPUUsage += container.Usage.Cpu().MilliValue()
		}
	}

	// Get pod resource requests/limits to calculate percentage
	pod, err := k.clientset.CoreV1().Pods(k.namespace).Get(ctx, podName, metav1.GetOptions{})
	if err != nil {
		// Can't get pod specs, return raw usage
		return float64(totalCPUUsage) / 1000.0 // Convert millicores to cores
	}

	// Calculate total CPU requests/limits
	var totalCPULimit int64
	for _, container := range pod.Spec.Containers {
		if container.Resources.Limits != nil {
			if cpu := container.Resources.Limits.Cpu(); cpu != nil {
				totalCPULimit += cpu.MilliValue()
			}
		} else if container.Resources.Requests != nil {
			if cpu := container.Resources.Requests.Cpu(); cpu != nil {
				totalCPULimit += cpu.MilliValue()
			}
		}
	}

	if totalCPULimit == 0 {
		// No limits set, return raw usage
		return float64(totalCPUUsage) / 1000.0
	}

	// Calculate percentage
	usagePercent := float64(totalCPUUsage) / float64(totalCPULimit) * 100.0
	if usagePercent > 100.0 {
		usagePercent = 100.0
	}

	return usagePercent
}

// getMemoryUsageFromK8s queries Kubernetes metrics API for memory usage
func (k *KubernetesAdapter) getMemoryUsageFromK8s(ctx context.Context, workerID string) float64 {
	if k.metricsClient == nil {
		// Metrics client not available (metrics-server not installed)
		return 0.0
	}

	// Find the pod for this worker
	podName := fmt.Sprintf("worker-%s", workerID)

	// Get pod metrics
	podMetrics, err := k.metricsClient.MetricsV1beta1().PodMetricses(k.namespace).Get(ctx, podName, metav1.GetOptions{})
	if err != nil {
		// Pod not found or metrics not available
		return 0.0
	}

	// Calculate total memory usage across all containers
	var totalMemoryUsage int64
	for _, container := range podMetrics.Containers {
		if container.Usage.Memory() != nil {
			totalMemoryUsage += container.Usage.Memory().Value()
		}
	}

	// Get pod resource requests/limits to calculate percentage
	pod, err := k.clientset.CoreV1().Pods(k.namespace).Get(ctx, podName, metav1.GetOptions{})
	if err != nil {
		// Can't get pod specs, return raw usage in MB
		return float64(totalMemoryUsage) / (1024 * 1024) // Convert bytes to MB
	}

	// Calculate total memory requests/limits
	var totalMemoryLimit int64
	for _, container := range pod.Spec.Containers {
		if container.Resources.Limits != nil {
			if memory := container.Resources.Limits.Memory(); memory != nil {
				totalMemoryLimit += memory.Value()
			}
		} else if container.Resources.Requests != nil {
			if memory := container.Resources.Requests.Memory(); memory != nil {
				totalMemoryLimit += memory.Value()
			}
		}
	}

	if totalMemoryLimit == 0 {
		// No limits set, return raw usage in MB
		return float64(totalMemoryUsage) / (1024 * 1024)
	}

	// Calculate percentage
	usagePercent := float64(totalMemoryUsage) / float64(totalMemoryLimit) * 100.0
	if usagePercent > 100.0 {
		usagePercent = 100.0
	}

	return usagePercent
}

// TerminateWorker terminates a worker on the Kubernetes cluster
func (k *KubernetesAdapter) TerminateWorker(ctx context.Context, workerID string) error {
	// In a real implementation, this would:
	// 1. Delete the Kubernetes pod for the worker
	// 2. Clean up worker resources
	// 3. Update worker status

	// For now, just log the termination
	fmt.Printf("Terminating worker %s\n", workerID)
	return nil
}

// GenerateWorkerSpawnScript generates a Kubernetes-specific script to spawn a worker
func (k *KubernetesAdapter) GenerateWorkerSpawnScript(ctx context.Context, experiment *domain.Experiment, walltime time.Duration) (string, error) {
	capabilities := k.resource.Capabilities
	data := struct {
		WorkerID          string
		ExperimentID      string
		ComputeResourceID string
		WalltimeSeconds   int64
		CPUCores          int
		MemoryMB          int
		GPUs              int
		WorkingDir        string
		WorkerBinaryURL   string
		ServerAddress     string
		ServerPort        int
	}{
		WorkerID:          fmt.Sprintf("worker_%s_%d", k.resource.ID, time.Now().UnixNano()),
		ExperimentID:      experiment.ID,
		ComputeResourceID: k.resource.ID,
		WalltimeSeconds:   int64(walltime.Seconds()),
		CPUCores:          getIntFromCapabilities(capabilities, "cpu_cores", 1),
		MemoryMB:          getIntFromCapabilities(capabilities, "memory_mb", 1024),
		GPUs:              getIntFromCapabilities(capabilities, "gpus", 0),
		WorkingDir:        k.config.DefaultWorkingDir,
		WorkerBinaryURL:   k.config.WorkerBinaryURL,
		ServerAddress:     k.config.ServerGRPCAddress,
		ServerPort:        k.config.ServerGRPCPort,
	}

	t, err := template.New("kubernetes_spawn").Parse(kubernetesWorkerSpawnTemplate)
	if err != nil {
		return "", fmt.Errorf("failed to parse Kubernetes spawn template: %w", err)
	}

	var buf strings.Builder
	if err := t.Execute(&buf, data); err != nil {
		return "", fmt.Errorf("failed to execute Kubernetes spawn template: %w", err)
	}

	return buf.String(), nil
}
