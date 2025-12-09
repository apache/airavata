package testutil

import (
	"context"
	"fmt"
	"os"
	"os/exec"
	"path/filepath"
	"time"
)

// DockerComposeManager manages Docker Compose operations for integration tests
type DockerComposeManager struct {
	composeFile string
	projectDir  string
}

// StorageConfig represents storage configuration
type StorageConfig struct {
	Host     string
	Port     int
	Username string
	BasePath string
}

// NewDockerComposeManager creates a new Docker Compose manager
func NewDockerComposeManager() (*DockerComposeManager, error) {
	projectDir, err := os.Getwd()
	if err != nil {
		return nil, fmt.Errorf("failed to get current directory: %w", err)
	}

	// Look for docker-compose.yml in current directory or parent
	composeFile := filepath.Join(projectDir, "docker-compose.yml")
	if _, err := os.Stat(composeFile); os.IsNotExist(err) {
		// Try parent directory
		parentDir := filepath.Dir(projectDir)
		composeFile = filepath.Join(parentDir, "docker-compose.yml")
		if _, err := os.Stat(composeFile); os.IsNotExist(err) {
			return nil, fmt.Errorf("docker-compose.yml not found in current or parent directory")
		}
	}

	return &DockerComposeManager{
		composeFile: composeFile,
		projectDir:  filepath.Dir(composeFile),
	}, nil
}

// StartDockerCompose starts the Docker Compose environment
func (dcm *DockerComposeManager) StartDockerCompose() error {
	ctx, cancel := context.WithTimeout(context.Background(), 5*time.Minute)
	defer cancel()

	cmd := exec.CommandContext(ctx, "docker", "compose", "-f", dcm.composeFile, "up", "-d")
	cmd.Dir = dcm.projectDir
	cmd.Stdout = os.Stdout
	cmd.Stderr = os.Stderr

	if err := cmd.Run(); err != nil {
		return fmt.Errorf("failed to start docker-compose: %w", err)
	}

	// Wait for services to be ready
	time.Sleep(10 * time.Second)

	return nil
}

// StopDockerCompose stops the Docker Compose environment
func (dcm *DockerComposeManager) StopDockerCompose() error {
	ctx, cancel := context.WithTimeout(context.Background(), 2*time.Minute)
	defer cancel()

	cmd := exec.CommandContext(ctx, "docker", "compose", "-f", dcm.composeFile, "down", "-v")
	cmd.Dir = dcm.projectDir
	cmd.Stdout = os.Stdout
	cmd.Stderr = os.Stderr

	if err := cmd.Run(); err != nil {
		return fmt.Errorf("failed to stop docker-compose: %w", err)
	}

	return nil
}

// GetDatabaseURL returns the database connection URL
func (dcm *DockerComposeManager) GetDatabaseURL() string {
	// Default PostgreSQL connection for test environment
	return "postgres://test_user:test_password@localhost:5433/airavata_scheduler_test?sslmode=disable"
}

// GetCentralStorageConfig returns the central storage configuration
func (dcm *DockerComposeManager) GetCentralStorageConfig() *StorageConfig {
	return &StorageConfig{
		Host:     "localhost",
		Port:     2200,
		Username: "testuser",
		BasePath: "/data",
	}
}

// GetComputeStorageConfig returns the storage configuration for a specific compute resource
func (dcm *DockerComposeManager) GetComputeStorageConfig(computeID string) *StorageConfig {
	switch computeID {
	case "slurm-cluster":
		return &StorageConfig{
			Host:     "localhost",
			Port:     2201,
			Username: "slurmuser",
			BasePath: "/data",
		}
	case "baremetal-cluster":
		return &StorageConfig{
			Host:     "localhost",
			Port:     2202,
			Username: "bareuser",
			BasePath: "/data",
		}
	default:
		// Return central storage as fallback
		return dcm.GetCentralStorageConfig()
	}
}

// KillRandomWorker kills a random worker for failure testing
func (dcm *DockerComposeManager) KillRandomWorker(experimentID string) error {
	ctx, cancel := context.WithTimeout(context.Background(), 30*time.Second)
	defer cancel()

	// Find a worker container for this experiment
	cmd := exec.CommandContext(ctx, "docker", "ps", "--filter", "label=experiment="+experimentID, "--format", "{{.Names}}")
	cmd.Dir = dcm.projectDir
	output, err := cmd.Output()
	if err != nil {
		return fmt.Errorf("failed to find worker containers: %w", err)
	}

	if len(output) == 0 {
		return fmt.Errorf("no worker containers found for experiment %s", experimentID)
	}

	// Get the first worker container name
	containerName := string(output[:len(output)-1]) // Remove newline

	// Kill the container
	killCmd := exec.CommandContext(ctx, "docker", "kill", containerName)
	killCmd.Dir = dcm.projectDir
	killCmd.Stdout = os.Stdout
	killCmd.Stderr = os.Stderr

	if err := killCmd.Run(); err != nil {
		return fmt.Errorf("failed to kill worker container %s: %w", containerName, err)
	}

	return nil
}

// CopyWorkerBinary copies the worker binary to containers
func (dcm *DockerComposeManager) CopyWorkerBinary() error {
	ctx, cancel := context.WithTimeout(context.Background(), 2*time.Minute)
	defer cancel()

	// Find the worker binary
	workerBinary := filepath.Join(dcm.projectDir, "worker")
	if _, err := os.Stat(workerBinary); os.IsNotExist(err) {
		// Try in build directory
		workerBinary = filepath.Join(dcm.projectDir, "build", "worker")
		if _, err := os.Stat(workerBinary); os.IsNotExist(err) {
			return fmt.Errorf("worker binary not found")
		}
	}

	// Copy to all worker containers
	containers := []string{"slurm-worker-1", "slurm-worker-2", "baremetal-worker-1"}
	for _, container := range containers {
		cmd := exec.CommandContext(ctx, "docker", "cp", workerBinary, container+":/usr/local/bin/worker")
		cmd.Dir = dcm.projectDir
		cmd.Stdout = os.Stdout
		cmd.Stderr = os.Stderr

		if err := cmd.Run(); err != nil {
			// Log error but continue - container might not exist yet
			fmt.Printf("Warning: failed to copy worker binary to %s: %v\n", container, err)
		}
	}

	return nil
}

// PauseContainer pauses a Docker container
func (dcm *DockerComposeManager) PauseContainer(containerName string) error {
	ctx, cancel := context.WithTimeout(context.Background(), 30*time.Second)
	defer cancel()

	cmd := exec.CommandContext(ctx, "docker", "pause", containerName)
	cmd.Dir = dcm.projectDir
	cmd.Stdout = os.Stdout
	cmd.Stderr = os.Stderr

	if err := cmd.Run(); err != nil {
		return fmt.Errorf("failed to pause container %s: %w", containerName, err)
	}

	return nil
}

// ResumeContainer resumes a Docker container
func (dcm *DockerComposeManager) ResumeContainer(containerName string) error {
	ctx, cancel := context.WithTimeout(context.Background(), 30*time.Second)
	defer cancel()

	cmd := exec.CommandContext(ctx, "docker", "unpause", containerName)
	cmd.Dir = dcm.projectDir
	cmd.Stdout = os.Stdout
	cmd.Stderr = os.Stderr

	if err := cmd.Run(); err != nil {
		return fmt.Errorf("failed to resume container %s: %w", containerName, err)
	}

	return nil
}

// StopContainer stops a Docker container
func (dcm *DockerComposeManager) StopContainer(containerName string) error {
	ctx, cancel := context.WithTimeout(context.Background(), 30*time.Second)
	defer cancel()

	cmd := exec.CommandContext(ctx, "docker", "stop", containerName)
	cmd.Dir = dcm.projectDir
	cmd.Stdout = os.Stdout
	cmd.Stderr = os.Stderr

	if err := cmd.Run(); err != nil {
		return fmt.Errorf("failed to stop container %s: %w", containerName, err)
	}

	return nil
}
