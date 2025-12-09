package testutil

import (
	"context"
	"fmt"
	"net"
	"os"
	"os/exec"
	"path/filepath"
	"strings"
	"testing"
	"time"
)

// ServiceChecker provides utilities for checking service availability
type ServiceChecker struct{}

// NewServiceChecker creates a new service checker
func NewServiceChecker() *ServiceChecker {
	return &ServiceChecker{}
}

// CheckDockerAvailability checks if Docker is available
func (sc *ServiceChecker) CheckDockerAvailability() error {
	cmd := exec.Command("docker", "version")
	if err := cmd.Run(); err != nil {
		return fmt.Errorf("docker is not available: %w", err)
	}

	cmd = exec.Command("docker-compose", "version")
	if err := cmd.Run(); err != nil {
		return fmt.Errorf("docker compose is not available: %w", err)
	}

	return nil
}

// CheckKubernetesAvailability checks if Kubernetes cluster is available
func (sc *ServiceChecker) CheckKubernetesAvailability() error {
	// Check if kubectl is available
	cmd := exec.Command("kubectl", "version", "--client")
	if err := cmd.Run(); err != nil {
		return fmt.Errorf("kubectl is not available: %w", err)
	}

	// Check if cluster is accessible
	cmd = exec.Command("kubectl", "cluster-info")
	if err := cmd.Run(); err != nil {
		return fmt.Errorf("kubernetes cluster is not accessible: %w", err)
	}

	// Check if kubeadm is available (for local cluster setup)
	cmd = exec.Command("kubeadm", "version")
	if err := cmd.Run(); err != nil {
		return fmt.Errorf("kubeadm is not available: %w", err)
	}

	return nil
}

// CheckServicePort checks if a service is listening on a specific port
func (sc *ServiceChecker) CheckServicePort(host string, port string, timeout time.Duration) error {
	ctx, cancel := context.WithTimeout(context.Background(), timeout)
	defer cancel()

	address := net.JoinHostPort(host, port)

	for {
		select {
		case <-ctx.Done():
			return fmt.Errorf("timeout waiting for service on %s", address)
		default:
			conn, err := net.DialTimeout("tcp", address, 1*time.Second)
			if err == nil {
				conn.Close()
				return nil
			}
			time.Sleep(100 * time.Millisecond)
		}
	}
}

// CheckMinIOService checks if MinIO service is available
func (sc *ServiceChecker) CheckMinIOService() error {
	return sc.CheckServicePort("localhost", "9000", 30*time.Second)
}

// CheckSFTPService checks if SFTP service is available
func (sc *ServiceChecker) CheckSFTPService() error {
	return sc.CheckServicePort("localhost", "2222", 30*time.Second)
}

// CheckNFSService checks if NFS service is available
func (sc *ServiceChecker) CheckNFSService() error {
	return sc.CheckServicePort("localhost", "2049", 30*time.Second)
}

// CheckSLURMService checks if SLURM service is available
func (sc *ServiceChecker) CheckSLURMService() error {
	return sc.CheckServicePort("localhost", "6817", 30*time.Second)
}

// CheckSSHService checks if SSH service is available
func (sc *ServiceChecker) CheckSSHService() error {
	return sc.CheckServicePort("localhost", "2223", 30*time.Second)
}

// CheckAllServices checks if all required services are available
func (sc *ServiceChecker) CheckAllServices() error {
	services := []struct {
		name  string
		check func() error
	}{
		{"MinIO", sc.CheckMinIOService},
		{"SFTP", sc.CheckSFTPService},
		{"NFS", sc.CheckNFSService},
		{"SLURM", sc.CheckSLURMService},
		{"SSH", sc.CheckSSHService},
	}

	for _, service := range services {
		if err := service.check(); err != nil {
			return fmt.Errorf("service %s is not available: %w", service.name, err)
		}
	}

	return nil
}

// SkipIfDockerNotAvailable skips the test if Docker is not available

// SkipIfServiceNotAvailable skips the test if a specific service is not available
func SkipIfServiceNotAvailable(t *testing.T, serviceName string) {
	t.Helper()

	checker := NewServiceChecker()

	var err error
	switch serviceName {
	case "minio":
		err = checker.CheckMinIOService()
	case "sftp":
		err = checker.CheckSFTPService()
	case "nfs":
		err = checker.CheckNFSService()
	case "slurm":
		err = checker.CheckSLURMService()
	case "ssh":
		err = checker.CheckSSHService()
	case "kubernetes":
		err = checker.CheckKubernetesAvailability()
	default:
		t.Fatalf("Unknown service: %s", serviceName)
	}

	if err != nil {
		t.Skipf("Service %s is not available: %v", serviceName, err)
	}
}

// WaitForService waits for a service to become available
func WaitForService(t *testing.T, serviceName string, timeout time.Duration) {
	t.Helper()

	checker := NewServiceChecker()

	var checkFunc func() error
	switch serviceName {
	case "minio":
		checkFunc = checker.CheckMinIOService
	case "sftp":
		checkFunc = checker.CheckSFTPService
	case "nfs":
		checkFunc = checker.CheckNFSService
	case "slurm":
		checkFunc = checker.CheckSLURMService
	case "ssh":
		checkFunc = checker.CheckSSHService
	default:
		t.Fatalf("Unknown service: %s", serviceName)
	}

	ctx, cancel := context.WithTimeout(context.Background(), timeout)
	defer cancel()

	ticker := time.NewTicker(1 * time.Second)
	defer ticker.Stop()

	for {
		select {
		case <-ctx.Done():
			t.Fatalf("Timeout waiting for service %s", serviceName)
		case <-ticker.C:
			if err := checkFunc(); err == nil {
				return
			}
		}
	}
}

// GetKubeconfigPath returns the path to the kubeconfig file
func GetKubeconfigPath() string {
	if kubeconfig := os.Getenv("KUBECONFIG"); kubeconfig != "" {
		return kubeconfig
	}

	homeDir, err := os.UserHomeDir()
	if err != nil {
		return ""
	}

	return filepath.Join(homeDir, ".kube", "config")
}

// CheckKubeconfigExists checks if kubeconfig file exists
func CheckKubeconfigExists() error {
	kubeconfigPath := GetKubeconfigPath()
	if kubeconfigPath == "" {
		return fmt.Errorf("unable to determine kubeconfig path")
	}

	if _, err := os.Stat(kubeconfigPath); os.IsNotExist(err) {
		return fmt.Errorf("kubeconfig file does not exist at %s", kubeconfigPath)
	}

	return nil
}

// SkipIfKubeconfigNotAvailable skips the test if kubeconfig is not available
func SkipIfKubeconfigNotAvailable(t *testing.T) {
	t.Helper()

	if err := CheckKubeconfigExists(); err != nil {
		t.Skipf("Kubeconfig is not available: %v", err)
	}
}

// GetServiceConnectionInfo returns connection information for a service
func GetServiceConnectionInfo(serviceName string) (host, port string, err error) {
	switch serviceName {
	case "minio":
		return "localhost", "9000", nil
	case "sftp":
		return "localhost", "2222", nil
	case "nfs":
		return "localhost", "2049", nil
	case "slurm":
		return "localhost", "6817", nil
	case "ssh":
		return "localhost", "2223", nil
	default:
		return "", "", fmt.Errorf("unknown service: %s", serviceName)
	}
}

// CheckSSHWithKey tests SSH connection with the master key
func (sc *ServiceChecker) CheckSSHWithKey(endpoint, keyPath string) error {
	// Parse endpoint to get host and port
	parts := strings.Split(endpoint, ":")
	if len(parts) != 2 {
		return fmt.Errorf("invalid endpoint format: %s", endpoint)
	}
	host, port := parts[0], parts[1]

	// Check if key file exists and has correct permissions
	if _, err := os.Stat(keyPath); os.IsNotExist(err) {
		return fmt.Errorf("SSH key file does not exist: %s", keyPath)
	}

	// Test SSH connection with the master key
	sshArgs := []string{
		"-o", "StrictHostKeyChecking=no",
		"-o", "UserKnownHostsFile=/dev/null",
		"-o", "PubkeyAuthentication=yes",
		"-o", "PasswordAuthentication=no",
		"-o", "PreferredAuthentications=publickey",
		"-o", "IdentitiesOnly=yes",
		"-o", "ConnectTimeout=10",
		"-i", keyPath,
		"-p", port,
		fmt.Sprintf("testuser@%s", host),
		"echo 'SSH connection successful'",
	}

	sshCmd := exec.Command("ssh", sshArgs...)
	output, err := sshCmd.CombinedOutput()
	if err != nil {
		return fmt.Errorf("SSH connection failed: %w, output: %s", err, string(output))
	}

	// Verify the expected output
	if !strings.Contains(string(output), "SSH connection successful") {
		return fmt.Errorf("unexpected SSH output: %s", string(output))
	}

	return nil
}

// CheckSSHWithKeyAndRetry tests SSH connection with retry logic
func (sc *ServiceChecker) CheckSSHWithKeyAndRetry(endpoint, keyPath string, maxRetries int, retryDelay time.Duration) error {
	var lastErr error

	for i := 0; i < maxRetries; i++ {
		if err := sc.CheckSSHWithKey(endpoint, keyPath); err == nil {
			return nil
		} else {
			lastErr = err
			if i < maxRetries-1 {
				time.Sleep(retryDelay)
			}
		}
	}

	return fmt.Errorf("SSH connection failed after %d retries: %w", maxRetries, lastErr)
}

// CheckSSHWithPassword tests SSH connection with password authentication
func (sc *ServiceChecker) CheckSSHWithPassword(endpoint, username, password string) error {
	// Parse endpoint to get host and port
	parts := strings.Split(endpoint, ":")
	if len(parts) != 2 {
		return fmt.Errorf("invalid endpoint format: %s", endpoint)
	}
	host, port := parts[0], parts[1]

	// Test SSH connection with password authentication
	sshArgs := []string{
		"-o", "StrictHostKeyChecking=no",
		"-o", "UserKnownHostsFile=/dev/null",
		"-o", "ConnectTimeout=10",
		"-p", port,
		fmt.Sprintf("%s@%s", username, host),
		"echo 'SSH connection successful'",
	}

	// Use sshpass for password authentication
	sshpassArgs := append([]string{"-p", password, "ssh"}, sshArgs...)
	cmd := exec.Command("sshpass", sshpassArgs...)
	output, err := cmd.CombinedOutput()
	if err != nil {
		return fmt.Errorf("SSH connection failed: %w, output: %s", err, string(output))
	}

	return nil
}

// CheckSSHWithPasswordAndRetry tests SSH connection with password authentication and retry logic
func (sc *ServiceChecker) CheckSSHWithPasswordAndRetry(endpoint, username, password string, maxRetries int, retryDelay time.Duration) error {
	var lastErr error

	for i := 0; i < maxRetries; i++ {
		if err := sc.CheckSSHWithPassword(endpoint, username, password); err == nil {
			return nil
		} else {
			lastErr = err
			if i < maxRetries-1 {
				time.Sleep(retryDelay)
			}
		}
	}

	return fmt.Errorf("SSH connection failed after %d retries: %w", maxRetries, lastErr)
}

// GetServiceCredentials returns credentials for a service
// Note: This function is deprecated as credentials are now managed via the registration workflow
func GetServiceCredentials(serviceName string) (username, password string, err error) {
	switch serviceName {
	case "minio":
		return "minioadmin", "minioadmin", nil
	case "sftp":
		return "testuser", "", nil // No password - uses SSH keys
	case "ssh":
		return "testuser", "", nil // No password - uses SSH keys
	case "nfs":
		return "", "", nil // NFS doesn't use username/password
	case "slurm":
		return "testuser", "", nil // No password - uses SSH keys
	default:
		return "", "", fmt.Errorf("unknown service: %s", serviceName)
	}
}
