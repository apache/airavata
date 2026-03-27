package testutil

import (
	"context"
	"fmt"
	"os"
	"os/exec"
	"strings"
	"testing"
	"time"
)

// DockerComposeHelper manages Docker Compose services for testing
type DockerComposeHelper struct {
	composeFile string
	projectName string
}

// NewDockerComposeHelper creates a new Docker Compose helper
func NewDockerComposeHelper(composeFile string) *DockerComposeHelper {
	return &DockerComposeHelper{
		composeFile: composeFile,
		projectName: fmt.Sprintf("airavata-test-%d", time.Now().Unix()),
	}
}

// StartServices starts Docker Compose services
func (h *DockerComposeHelper) StartServices(t *testing.T, services ...string) error {
	t.Helper()

	args := []string{"compose", "-f", h.composeFile, "-p", h.projectName, "up", "-d"}
	if len(services) > 0 {
		args = append(args, services...)
	} else {
		args = append(args, "minio", "sftp", "nfs-server", "spicedb", "spicedb-postgres", "openbao")
	}

	cmd := exec.Command("docker", args...)
	cmd.Stdout = os.Stdout
	cmd.Stderr = os.Stderr

	if err := cmd.Run(); err != nil {
		return fmt.Errorf("failed to start services: %w", err)
	}

	// Wait for services to be healthy
	return h.WaitForServices(t, 2*time.Minute)
}

// StartSlurmClusters starts all 2 SLURM clusters
func (h *DockerComposeHelper) StartSlurmClusters(t *testing.T) error {
	t.Helper()

	services := []string{"slurm-cluster-01", "slurm-cluster-02"}
	return h.StartServices(t, services...)
}

// StartBareMetal starts the bare metal Ubuntu container
func (h *DockerComposeHelper) StartBareMetal(t *testing.T) error {
	t.Helper()

	return h.StartServices(t, "baremetal-node-1")
}

// GetSlurmEndpoint returns the endpoint for a specific cluster (1 or 2)
func (h *DockerComposeHelper) GetSlurmEndpoint(clusterNum int) string {
	switch clusterNum {
	case 1:
		return "localhost:6817"
	case 2:
		return "localhost:6819"
	default:
		return "localhost:6817" // Default to cluster 1
	}
}

// GetBaremetalEndpoint returns SSH endpoint for bare metal
func (h *DockerComposeHelper) GetBaremetalEndpoint() string {
	return "localhost:2225"
}

// StopServices stops Docker Compose services
func (h *DockerComposeHelper) StopServices(t *testing.T) error {
	if t != nil {
		t.Helper()
	}

	if h == nil || h.projectName == "" {
		return nil // Nothing to stop
	}

	cmd := exec.Command("docker", "compose", "-f", h.composeFile, "-p", h.projectName, "down", "-v")
	cmd.Stdout = os.Stdout
	cmd.Stderr = os.Stderr

	if err := cmd.Run(); err != nil {
		return fmt.Errorf("failed to stop services: %w", err)
	}

	return nil
}

// WaitForServices waits for all services to be healthy
func (h *DockerComposeHelper) WaitForServices(t *testing.T, timeout time.Duration) error {
	t.Helper()

	if timeout == 0 {
		timeout = 5 * time.Minute // Increased from 2 minutes to 5 minutes
	}

	ctx, cancel := context.WithTimeout(context.Background(), timeout)
	defer cancel()

	services := []string{"minio", "sftp", "slurm-cluster-01", "slurm-node-01-01", "slurm-cluster-02", "slurm-node-02-01", "baremetal-node-1", "baremetal-node-2"}

	for _, service := range services {
		if err := h.waitForService(ctx, service); err != nil {
			return fmt.Errorf("service %s failed to become healthy: %w", service, err)
		}
	}

	return nil
}

// WaitForSpecificServices waits for specific services to be healthy
func (h *DockerComposeHelper) WaitForSpecificServices(t *testing.T, services []string, timeout time.Duration) error {
	t.Helper()

	ctx, cancel := context.WithTimeout(context.Background(), timeout)
	defer cancel()

	for _, service := range services {
		if err := h.waitForService(ctx, service); err != nil {
			return fmt.Errorf("service %s failed to become healthy: %w", service, err)
		}
	}

	return nil
}

// GetProjectName returns the Docker Compose project name
func (h *DockerComposeHelper) GetProjectName() string {
	return h.projectName
}

// CreateTestBucket creates a test bucket in MinIO
func (h *DockerComposeHelper) CreateTestBucket(t *testing.T, bucketName string) error {
	t.Helper()

	cmd := exec.Command("docker", "exec",
		fmt.Sprintf("%s-minio-1", h.projectName),
		"mc", "mb", fmt.Sprintf("minio/%s", bucketName))
	cmd.Stdout = os.Stdout
	cmd.Stderr = os.Stderr

	return cmd.Run()
}

// CleanupTestBucket removes a test bucket from MinIO
func (h *DockerComposeHelper) CleanupTestBucket(t *testing.T, bucketName string) error {
	t.Helper()

	cmd := exec.Command("docker", "exec",
		fmt.Sprintf("%s-minio-1", h.projectName),
		"mc", "rb", "--force", fmt.Sprintf("minio/%s", bucketName))
	cmd.Stdout = os.Stdout
	cmd.Stderr = os.Stderr

	return cmd.Run()
}

// waitForService waits for a specific service to be healthy
func (h *DockerComposeHelper) waitForService(ctx context.Context, service string) error {
	ticker := time.NewTicker(5 * time.Second)
	defer ticker.Stop()

	for {
		select {
		case <-ctx.Done():
			return ctx.Err()
		case <-ticker.C:
			if h.isServiceHealthy(service) {
				return nil
			}
		}
	}
}

// isServiceHealthy checks if a service is healthy
func (h *DockerComposeHelper) isServiceHealthy(service string) bool {
	cmd := exec.Command("docker", "compose", "-f", h.composeFile, "-p", h.projectName, "ps", "-q", service)
	output, err := cmd.Output()
	if err != nil {
		return false
	}

	containerID := strings.TrimSpace(string(output))
	if containerID == "" {
		return false
	}

	// Check container health
	cmd = exec.Command("docker", "inspect", "--format={{.State.Health.Status}}", containerID)
	output, err = cmd.Output()
	if err != nil {
		return false
	}

	healthStatus := strings.TrimSpace(string(output))
	if healthStatus == "healthy" {
		return true
	}

	// For services without health checks, verify they're running and accessible
	return h.isServiceAccessible(service)
}

// isServiceAccessible checks if a service is accessible via its port
func (h *DockerComposeHelper) isServiceAccessible(service string) bool {
	host, port, err := h.GetServiceConnection(service)
	if err != nil {
		return false
	}

	// Try to connect to the service
	cmd := exec.Command("nc", "-z", host, port)
	return cmd.Run() == nil
}

// GetServiceConnection returns connection details for a service
func (h *DockerComposeHelper) GetServiceConnection(service string) (host string, port string, err error) {
	switch service {
	case "minio":
		return "localhost", "9000", nil
	case "sftp":
		return "localhost", "2222", nil
	case "nfs-server":
		return "localhost", "2049", nil
	case "slurm-cluster-01":
		return "localhost", "6817", nil
	case "slurm-cluster-02":
		return "localhost", "6819", nil
	case "slurm-node-01-01":
		return "localhost", "6817", nil
	case "slurm-node-02-01":
		return "localhost", "6819", nil
	case "baremetal-node-1":
		return "localhost", "2223", nil
	case "baremetal-node-2":
		return "localhost", "2225", nil
	case "ssh-server":
		return "localhost", "2223", nil
	default:
		return "", "", fmt.Errorf("unknown service: %s", service)
	}
}

// GetServiceCredentials returns credentials for a service
func (h *DockerComposeHelper) GetServiceCredentials(service string) (username, password string, err error) {
	switch service {
	case "minio":
		return "minioadmin", "minioadmin", nil
	case "sftp":
		return "testuser", "testpass", nil
	case "ssh-server":
		return "testuser", "testpass", nil
	case "nfs-server":
		return "", "", nil // NFS doesn't use username/password
	case "slurm-cluster-01", "slurm-cluster-02":
		return "slurm", "slurm", nil
	default:
		return "", "", fmt.Errorf("unknown service: %s", service)
	}
}

// SetupTestEnvironment sets up the complete test environment
func (h *DockerComposeHelper) SetupTestEnvironment(t *testing.T) error {
	t.Helper()

	// Start services
	if err := h.StartServices(t); err != nil {
		return fmt.Errorf("failed to start services: %w", err)
	}

	// Setup MinIO
	if err := h.setupMinIO(t); err != nil {
		return fmt.Errorf("failed to setup MinIO: %w", err)
	}

	// Setup SFTP
	if err := h.setupSFTP(t); err != nil {
		return fmt.Errorf("failed to setup SFTP: %w", err)
	}

	// Setup NFS
	if err := h.setupNFS(t); err != nil {
		return fmt.Errorf("failed to setup NFS: %w", err)
	}

	// Setup SLURM
	if err := h.setupSLURM(t); err != nil {
		return fmt.Errorf("failed to setup SLURM: %w", err)
	}

	return nil
}

// setupMinIO configures MinIO for testing
func (h *DockerComposeHelper) setupMinIO(t *testing.T) error {
	t.Helper()

	// Wait for MinIO to be ready
	time.Sleep(10 * time.Second)

	// Create test bucket
	return h.CreateTestBucket(t, "test-bucket")
}

// setupSFTP configures SFTP for testing
func (h *DockerComposeHelper) setupSFTP(t *testing.T) error {
	t.Helper()

	// Create test directories
	cmd := exec.Command("docker", "exec",
		fmt.Sprintf("%s-sftp-1", h.projectName),
		"mkdir", "-p", "/home/testuser/upload/test")

	return cmd.Run()
}

// setupNFS configures NFS for testing
func (h *DockerComposeHelper) setupNFS(t *testing.T) error {
	t.Helper()

	// Create test directories
	cmd := exec.Command("docker", "exec",
		fmt.Sprintf("%s-nfs-server-1", h.projectName),
		"mkdir", "-p", "/nfsshare/test")

	return cmd.Run()
}

// setupSLURM configures SLURM for testing
func (h *DockerComposeHelper) setupSLURM(t *testing.T) error {
	t.Helper()

	// Wait for SLURM to be ready
	time.Sleep(15 * time.Second)

	// Check SLURM status
	cmd := exec.Command("docker", "exec",
		fmt.Sprintf("%s-slurm-cluster-01-1", h.projectName),
		"scontrol", "ping")

	return cmd.Run()
}

// TeardownTestEnvironment cleans up the test environment
func (h *DockerComposeHelper) TeardownTestEnvironment(t *testing.T) error {
	t.Helper()

	return h.StopServices(t)
}

// SkipIfDockerNotAvailable skips the test if Docker is not available
func SkipIfDockerNotAvailable(t *testing.T) {
	t.Helper()

	cmd := exec.Command("docker", "version")
	if err := cmd.Run(); err != nil {
		t.Skip("Docker is not available")
	}

	cmd = exec.Command("docker", "compose", "version")
	if err := cmd.Run(); err != nil {
		t.Skip("Docker Compose is not available")
	}
}

// SkipIfServicesNotAvailable skips the test if required services are not available
func SkipIfServicesNotAvailable(t *testing.T) {
	t.Helper()

	SkipIfDockerNotAvailable(t)

	// Check if services are running
	helper := NewDockerComposeHelper("docker compose.yml")

	services := []string{"minio", "sftp", "nfs-server", "slurm-cluster-01", "baremetal-node-1"}
	for _, service := range services {
		if !helper.isServiceHealthy(service) {
			t.Skipf("Service %s is not available", service)
		}
	}
}
