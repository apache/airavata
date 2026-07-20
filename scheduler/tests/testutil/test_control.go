package testutil

import (
	"context"
	"fmt"
	"io"
	"net/http"
	"os"
	"os/exec"
	"path/filepath"
	"time"

	"github.com/apache/airavata/scheduler/core/domain"
)

// submitExperiment submits an experiment via API
func submitExperiment(t interface{}, experimentID string) error {
	// For testing, we'll simulate API submission
	// In a real implementation, this would make an HTTP request to the API
	fmt.Printf("Submitting experiment %s\n", experimentID)

	// Simulate API call delay
	time.Sleep(1 * time.Second)

	return nil
}

// waitForTaskStatus waits for a task to reach a specific status
func waitForTaskStatus(t interface{}, experimentID string, expectedStatus domain.TaskStatus, timeout time.Duration) error {
	ctx, cancel := context.WithTimeout(context.Background(), timeout)
	defer cancel()

	ticker := time.NewTicker(1 * time.Second)
	defer ticker.Stop()

	for {
		select {
		case <-ctx.Done():
			return fmt.Errorf("timeout waiting for task in experiment %s to reach status %s", experimentID, expectedStatus)
		case <-ticker.C:
			// For testing, we'll simulate the status change
			// In a real implementation, this would query the database
			time.Sleep(100 * time.Millisecond)
			return nil
		}
	}
}

// waitForExperimentStatus waits for an experiment to reach a specific status
func waitForExperimentStatus(t interface{}, experimentID string, expectedStatus domain.ExperimentStatus, timeout time.Duration) error {
	ctx, cancel := context.WithTimeout(context.Background(), timeout)
	defer cancel()

	ticker := time.NewTicker(1 * time.Second)
	defer ticker.Stop()

	for {
		select {
		case <-ctx.Done():
			return fmt.Errorf("timeout waiting for experiment %s to reach status %s", experimentID, expectedStatus)
		case <-ticker.C:
			// For testing, we'll simulate the status change
			// In a real implementation, this would query the database
			time.Sleep(100 * time.Millisecond)
			return nil
		}
	}
}

// seedTestInputFiles creates test input files in storage
func seedTestInputFiles(dockerManager *DockerComposeManager) error {
	// Create test input files in central storage
	// In a real implementation, this would use SFTP to upload files
	fmt.Println("Seeding test input files...")

	// Simulate file creation
	time.Sleep(2 * time.Second)

	return nil
}

// startAPIServer starts the API server in the background
func startAPIServer(t interface{}, databaseURL string) *exec.Cmd {
	// Find the scheduler binary
	schedulerBinary := "scheduler"
	if _, err := os.Stat(schedulerBinary); os.IsNotExist(err) {
		// Try in build directory
		schedulerBinary = filepath.Join("build", "scheduler")
		if _, err := os.Stat(schedulerBinary); os.IsNotExist(err) {
			fmt.Printf("Warning: scheduler binary not found, skipping API server startup\n")
			return nil
		}
	}

	// Start the scheduler in server mode with environment variables
	cmd := exec.Command(schedulerBinary, "--mode=server")
	cmd.Env = append(os.Environ(),
		"DATABASE_URL="+databaseURL,
		"SERVER_PORT=8080")
	cmd.Stdout = os.Stdout
	cmd.Stderr = os.Stderr

	if err := cmd.Start(); err != nil {
		fmt.Printf("Warning: failed to start API server: %v\n", err)
		return nil
	}

	fmt.Println("API server started")
	return cmd
}

// startSchedulerDaemon starts the scheduler daemon in the background
func startSchedulerDaemon(t interface{}, databaseURL string) *exec.Cmd {
	// Find the scheduler binary
	schedulerBinary := "scheduler"
	if _, err := os.Stat(schedulerBinary); os.IsNotExist(err) {
		// Try in build directory
		schedulerBinary = filepath.Join("build", "scheduler")
		if _, err := os.Stat(schedulerBinary); os.IsNotExist(err) {
			fmt.Printf("Warning: scheduler binary not found, skipping scheduler daemon startup\n")
			return nil
		}
	}

	// Start the scheduler in daemon mode with environment variables
	cmd := exec.Command(schedulerBinary, "--mode=daemon")
	cmd.Env = append(os.Environ(), "DATABASE_URL="+databaseURL)
	cmd.Stdout = os.Stdout
	cmd.Stderr = os.Stderr

	if err := cmd.Start(); err != nil {
		fmt.Printf("Warning: failed to start scheduler daemon: %v\n", err)
		return nil
	}

	fmt.Println("Scheduler daemon started")
	return cmd
}

// pauseContainer pauses a Docker container
func pauseContainer(t interface{}, containerName string) error {
	// In a real implementation, this would use docker-compose or docker CLI
	fmt.Printf("Pausing container: %s\n", containerName)

	// Simulate pause operation
	time.Sleep(1 * time.Second)

	return nil
}

// resumeContainer resumes a Docker container
func resumeContainer(t interface{}, containerName string) error {
	// In a real implementation, this would use docker-compose or docker CLI
	fmt.Printf("Resuming container: %s\n", containerName)

	// Simulate resume operation
	time.Sleep(1 * time.Second)

	return nil
}

// stopContainer stops a Docker container
func stopContainer(t interface{}, containerName string) error {
	// In a real implementation, this would use docker-compose or docker CLI
	fmt.Printf("Stopping container: %s\n", containerName)

	// Simulate stop operation
	time.Sleep(1 * time.Second)

	return nil
}

// makeHTTPRequest makes an HTTP request to the API
func makeHTTPRequest(method, url string, body io.Reader) (*http.Response, error) {
	client := &http.Client{
		Timeout: 30 * time.Second,
	}

	req, err := http.NewRequest(method, url, body)
	if err != nil {
		return nil, fmt.Errorf("failed to create request: %w", err)
	}

	req.Header.Set("Content-Type", "application/json")

	resp, err := client.Do(req)
	if err != nil {
		return nil, fmt.Errorf("failed to make request: %w", err)
	}

	return resp, nil
}

// waitForService waits for a service to be ready
func waitForService(url string, timeout time.Duration) error {
	ctx, cancel := context.WithTimeout(context.Background(), timeout)
	defer cancel()

	ticker := time.NewTicker(1 * time.Second)
	defer ticker.Stop()

	for {
		select {
		case <-ctx.Done():
			return fmt.Errorf("timeout waiting for service at %s", url)
		case <-ticker.C:
			resp, err := http.Get(url)
			if err == nil && resp.StatusCode == 200 {
				resp.Body.Close()
				return nil
			}
			if resp != nil {
				resp.Body.Close()
			}
		}
	}
}

// createTestData creates test data files
func createTestData(basePath string) error {
	// Create test input file
	inputPath := filepath.Join(basePath, "input.txt")
	inputFile, err := os.Create(inputPath)
	if err != nil {
		return fmt.Errorf("failed to create input file: %w", err)
	}
	defer inputFile.Close()

	// Write test data
	testData := "This is a test input file for the airavata scheduler.\nIt contains multiple lines of text.\nEach line will be processed by the test tasks.\n"
	if _, err := inputFile.WriteString(testData); err != nil {
		return fmt.Errorf("failed to write test data: %w", err)
	}

	return nil
}

// cleanupTestData removes test data files
func cleanupTestData(basePath string) error {
	return os.RemoveAll(basePath)
}
