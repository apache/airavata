package testutil

import (
	"context"
	"fmt"
	"os"
	"os/exec"
	"path/filepath"
	"strings"
	"time"

	"github.com/apache/airavata/scheduler/core/domain"
)

// ResourceRegistrar orchestrates the resource registration workflow for tests
type ResourceRegistrar struct {
	config *TestConfig
	suite  *IntegrationTestSuite
}

// NewResourceRegistrar creates a new resource registrar
func NewResourceRegistrar() *ResourceRegistrar {
	return &ResourceRegistrar{
		config: GetTestConfig(),
	}
}

// NewResourceRegistrarWithSuite creates a new resource registrar with suite access
func NewResourceRegistrarWithSuite(suite *IntegrationTestSuite) *ResourceRegistrar {
	return &ResourceRegistrar{
		config: GetTestConfig(),
		suite:  suite,
	}
}

// RegisterComputeResourceViaWorkflow registers a compute resource using the full workflow
func (rr *ResourceRegistrar) RegisterComputeResourceViaWorkflow(name, endpoint, masterKeyPath, sshEndpoint, resourceType string) (*domain.ComputeResource, error) {
	// Step 0: Pre-flight SSH connectivity check (using password authentication)
	checker := NewServiceChecker()
	if err := checker.CheckSSHWithPasswordAndRetry(sshEndpoint, "testuser", "testpass", 3, 2*time.Second); err != nil {
		return nil, fmt.Errorf("pre-flight SSH connectivity check failed: %w", err)
	}

	// Step 1: Create inactive resource entry and get token
	token, resourceID, err := rr.createInactiveResourceEntry(name, endpoint, resourceType)
	if err != nil {
		return nil, fmt.Errorf("failed to create inactive resource entry: %w", err)
	}

	// Step 2: Deploy CLI binary to the resource
	if err := rr.deployCLIBinary(sshEndpoint, masterKeyPath); err != nil {
		return nil, fmt.Errorf("failed to deploy CLI binary: %w", err)
	}

	// Step 3: Execute registration command on the resource
	// For testing, we'll simulate the CLI registration by directly calling the server logic
	_, err = rr.simulateCLIRegistration(token, name, resourceID, sshEndpoint)
	if err != nil {
		return nil, fmt.Errorf("failed to simulate CLI registration: %w", err)
	}

	// Step 4: Wait for registration to complete and validate
	resource, err := rr.waitForRegistrationCompletion(resourceID)
	if err != nil {
		return nil, fmt.Errorf("failed to wait for registration completion: %w", err)
	}

	return resource, nil
}

// RegisterStorageResourceViaWorkflow registers a storage resource using the full workflow
func (rr *ResourceRegistrar) RegisterStorageResourceViaWorkflow(name, endpoint, masterKeyPath string) (*domain.StorageResource, error) {
	// For storage resources, we'll use a simplified approach since they don't need
	// the same level of auto-discovery as compute resources

	// Step 1: Create inactive resource entry and get token
	token, resourceID, err := rr.createInactiveStorageResourceEntry(name, endpoint)
	if err != nil {
		return nil, fmt.Errorf("failed to create inactive storage resource entry: %w", err)
	}

	// Step 2: Deploy CLI binary to the resource
	if err := rr.deployCLIBinary(endpoint, masterKeyPath); err != nil {
		return nil, fmt.Errorf("failed to deploy CLI binary: %w", err)
	}

	// Step 3: Execute storage registration command on the resource
	_, err = rr.executeStorageRegistrationCommand(endpoint, masterKeyPath, token, name)
	if err != nil {
		return nil, fmt.Errorf("failed to execute storage registration command: %w", err)
	}

	// Step 4: Wait for registration to complete and validate
	resource, err := rr.waitForStorageRegistrationCompletion(resourceID)
	if err != nil {
		return nil, fmt.Errorf("failed to wait for storage registration completion: %w", err)
	}

	return resource, nil
}

// createInactiveResourceEntry creates an inactive compute resource entry and returns a token and resource ID
func (rr *ResourceRegistrar) createInactiveResourceEntry(name, endpoint, resourceType string) (string, string, error) {
	if rr.suite == nil {
		return "", "", fmt.Errorf("suite not available - use NewResourceRegistrarWithSuite")
	}

	// Generate a secure one-time-use token
	token := fmt.Sprintf("reg-token-%s-%d", name, time.Now().UnixNano())

	// Generate resource ID
	resourceID := fmt.Sprintf("res_%s_%d", name, time.Now().UnixNano())

	// Create inactive compute resource directly in database (bypassing service layer)
	now := time.Now().UTC()

	// Use raw SQL connection to ensure transaction is committed
	rawDB, err := rr.suite.DB.DB.GetDB().DB()
	if err != nil {
		return "", "", fmt.Errorf("failed to get raw database connection: %w", err)
	}

	// Create compute resource
	_, err = rawDB.ExecContext(context.Background(), `
		INSERT INTO compute_resources (id, name, type, endpoint, owner_id, status, max_workers, current_workers, cost_per_hour, capabilities, created_at, updated_at, metadata)
		VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12, $13)
	`, resourceID, name, resourceType, endpoint, rr.suite.TestUser.ID, "INACTIVE", 10, 0, 0.0, "{}", now, now, fmt.Sprintf(`{"registration_token": "%s", "token_expires_at": %d}`, token, now.Add(1*time.Hour).Unix()))
	if err != nil {
		return "", "", fmt.Errorf("failed to create inactive resource: %w", err)
	}

	// Store the token in the database for validation
	tokenID := fmt.Sprintf("token-%s-%d", name, time.Now().UnixNano())
	_, err = rawDB.ExecContext(context.Background(), `
		INSERT INTO registration_tokens (id, token, resource_id, user_id, expires_at, created_at)
		VALUES ($1, $2, $3, $4, $5, $6)
	`, tokenID, token, resourceID, rr.suite.TestUser.ID, now.Add(1*time.Hour), now)
	if err != nil {
		return "", "", fmt.Errorf("failed to store registration token: %w", err)
	}

	// Debug: Verify token was stored
	fmt.Printf("DEBUG: Created token %s for resource %s, user %s\n", token, resourceID, rr.suite.TestUser.ID)

	// Debug: Verify token is actually in database
	var expiresAt time.Time
	err = rawDB.QueryRowContext(context.Background(), "SELECT expires_at FROM registration_tokens WHERE token = $1", token).Scan(&expiresAt)
	if err != nil {
		fmt.Printf("DEBUG: Failed to verify token in database: %v\n", err)
	} else {
		fmt.Printf("DEBUG: Token verification: token found in database, expires at: %v (now: %v)\n", expiresAt, time.Now())
	}

	return token, resourceID, nil
}

// createInactiveStorageResourceEntry creates an inactive storage resource entry and returns a token and resource ID
func (rr *ResourceRegistrar) createInactiveStorageResourceEntry(name, endpoint string) (string, string, error) {
	if rr.suite == nil {
		return "", "", fmt.Errorf("suite not available - use NewResourceRegistrarWithSuite")
	}

	// Generate a secure one-time-use token
	token := fmt.Sprintf("storage-reg-token-%s-%d", name, time.Now().UnixNano())

	// Generate resource ID
	resourceID := fmt.Sprintf("res_%s_%d", name, time.Now().UnixNano())

	// Create inactive storage resource directly in database (bypassing service layer)
	now := time.Now()
	capacity := int64(1000000000) // 1GB
	err := rr.suite.DB.DB.GetDB().WithContext(context.Background()).Exec(`
		INSERT INTO storage_resources (id, name, type, endpoint, owner_id, status, total_capacity, used_capacity, available_capacity, region, zone, created_at, updated_at, metadata)
		VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12, $13, $14)
	`, resourceID, name, "SFTP", endpoint, rr.suite.TestUser.ID, "INACTIVE", capacity, 0, capacity, "", "", now, now, fmt.Sprintf(`{"registration_token": "%s", "token_expires_at": %d}`, token, now.Add(1*time.Hour).Unix())).Error
	if err != nil {
		return "", "", fmt.Errorf("failed to create inactive storage resource: %w", err)
	}

	// Store the token in the database for validation
	tokenID := fmt.Sprintf("storage-token-%s-%d", name, time.Now().UnixNano())
	err = rr.suite.DB.DB.GetDB().WithContext(context.Background()).Exec(`
		INSERT INTO registration_tokens (id, token, resource_id, user_id, expires_at, created_at)
		VALUES ($1, $2, $3, $4, $5, $6)
	`, tokenID, token, resourceID, rr.suite.TestUser.ID, now.Add(1*time.Hour), now).Error
	if err != nil {
		return "", "", fmt.Errorf("failed to store registration token: %w", err)
	}

	return token, resourceID, nil
}

// deployCLIBinary deploys the CLI binary to the target resource
func (rr *ResourceRegistrar) deployCLIBinary(endpoint, masterKeyPath string) error {
	// Build CLI binary if it doesn't exist
	currentDir, err := os.Getwd()
	if err != nil {
		return fmt.Errorf("failed to get current directory: %w", err)
	}

	projectRoot := filepath.Join(currentDir, "..", "..")
	projectRoot, err = filepath.Abs(projectRoot)
	if err != nil {
		return fmt.Errorf("failed to get absolute path: %w", err)
	}

	cliBinaryPath := filepath.Join(projectRoot, "bin", "airavata")

	// Always rebuild the CLI binary to ensure correct architecture
	// Remove existing binary if it exists (might be wrong architecture)
	if _, err := os.Stat(cliBinaryPath); err == nil {
		os.Remove(cliBinaryPath)
	}

	// Build the CLI binary for the correct target architecture
	if err := rr.buildCLIBinary(); err != nil {
		return fmt.Errorf("failed to build CLI binary: %w", err)
	}

	// Parse endpoint to get host and port
	host, port, err := rr.parseEndpoint(endpoint)
	if err != nil {
		return fmt.Errorf("failed to parse endpoint: %w", err)
	}

	// Use SCP to copy the binary
	// Use appropriate path based on container type
	remotePath := "/tmp/airavata"           // Default to /tmp for all containers
	if strings.Contains(endpoint, "2222") { // SFTP container
		remotePath = "/home/testuser/upload/airavata"
	} else if strings.Contains(endpoint, "2225") || strings.Contains(endpoint, "2226") { // Bare metal containers
		remotePath = "/tmp/airavata"
	}
	scpArgs := []string{
		"-o", "StrictHostKeyChecking=no",
		"-o", "UserKnownHostsFile=/dev/null",
		"-o", "ConnectTimeout=10",
		"-P", port,
		cliBinaryPath,
		fmt.Sprintf("testuser@%s:%s", host, remotePath),
	}

	// Retry SCP with exponential backoff
	var output []byte
	maxRetries := 3
	baseDelay := 2 * time.Second

	for attempt := 0; attempt < maxRetries; attempt++ {
		// Use sshpass for password authentication
		sshpassArgs := append([]string{"-p", "testpass", "scp"}, scpArgs...)
		scpCmd := exec.Command("sshpass", sshpassArgs...)
		output, err = scpCmd.CombinedOutput()
		if err == nil {
			break
		}

		if attempt < maxRetries-1 {
			delay := time.Duration(attempt+1) * baseDelay
			fmt.Printf("SCP attempt %d failed, retrying in %v: %v\n", attempt+1, delay, err)
			time.Sleep(delay)
		}
	}

	if err != nil {
		return fmt.Errorf("failed to copy CLI binary after %d attempts: %w, output: %s", maxRetries, err, string(output))
	}

	// Make the binary executable
	sshArgs := []string{
		"-o", "StrictHostKeyChecking=no",
		"-o", "UserKnownHostsFile=/dev/null",
		"-o", "PubkeyAuthentication=yes",
		"-o", "PasswordAuthentication=no",
		"-o", "PreferredAuthentications=publickey",
		"-o", "IdentitiesOnly=yes",
		"-o", "ConnectTimeout=10",
		"-v", // Verbose output for debugging
		"-i", masterKeyPath,
		"-p", port,
		fmt.Sprintf("testuser@%s", host),
		"chmod", "+x", remotePath,
	}

	// Retry SSH with exponential backoff
	for attempt := 0; attempt < maxRetries; attempt++ {
		sshCmd := exec.Command("ssh", sshArgs...)
		output, err = sshCmd.CombinedOutput()
		if err == nil {
			break
		}

		if attempt < maxRetries-1 {
			delay := time.Duration(attempt+1) * baseDelay
			fmt.Printf("SSH attempt %d failed, retrying in %v: %v\n", attempt+1, delay, err)
			time.Sleep(delay)
		}
	}

	if err != nil {
		return fmt.Errorf("failed to make CLI binary executable after %d attempts: %w, output: %s", maxRetries, err, string(output))
	}

	return nil
}

// executeRegistrationCommand executes the registration command on the target resource
func (rr *ResourceRegistrar) executeRegistrationCommand(endpoint, masterKeyPath, token, name string) (string, error) {
	// Parse endpoint to get host and port
	host, port, err := rr.parseEndpoint(endpoint)
	if err != nil {
		return "", fmt.Errorf("failed to parse endpoint: %w", err)
	}

	// Execute the registration command
	remotePath := "/home/testuser/airavata"
	if strings.Contains(endpoint, "2222") { // SFTP container
		remotePath = "/home/testuser/upload/airavata"
	} else if strings.Contains(endpoint, "2225") || strings.Contains(endpoint, "2226") { // Bare metal containers
		remotePath = "/config/airavata"
	}
	serverURL := "http://host.docker.internal:8080" // Use host.docker.internal to connect to host machine from container
	registrationCmd := fmt.Sprintf("%s resource compute register --token=%s --name=%s --server=%s", remotePath, token, name, serverURL)

	sshArgs := []string{
		"-o", "StrictHostKeyChecking=no",
		"-o", "UserKnownHostsFile=/dev/null",
		"-o", "PubkeyAuthentication=yes",
		"-o", "PasswordAuthentication=no",
		"-o", "PreferredAuthentications=publickey",
		"-i", masterKeyPath,
		"-p", port,
		fmt.Sprintf("testuser@%s", host),
		registrationCmd,
	}

	sshCmd := exec.Command("ssh", sshArgs...)
	fmt.Printf("DEBUG: Executing registration command: %s\n", registrationCmd)
	output, err := sshCmd.CombinedOutput()
	fmt.Printf("DEBUG: Registration command output: %s\n", string(output))
	if err != nil {
		return "", fmt.Errorf("failed to execute registration command: %w, output: %s", err, string(output))
	}

	// Parse the output to extract the resource ID
	// The CLI should output something like "Resource ID: abc123"
	lines := strings.Split(string(output), "\n")
	for _, line := range lines {
		if strings.Contains(line, "Resource ID:") {
			parts := strings.Split(line, "Resource ID:")
			if len(parts) == 2 {
				return strings.TrimSpace(parts[1]), nil
			}
		}
	}

	return "", fmt.Errorf("failed to extract resource ID from output: %s", string(output))
}

// executeStorageRegistrationCommand executes the storage registration command on the target resource
func (rr *ResourceRegistrar) executeStorageRegistrationCommand(endpoint, masterKeyPath, token, name string) (string, error) {
	// For storage resources, we'll use a simplified registration approach
	// since they don't need the same level of auto-discovery

	// Parse endpoint to get host and port
	host, port, err := rr.parseEndpoint(endpoint)
	if err != nil {
		return "", fmt.Errorf("failed to parse endpoint: %w", err)
	}

	// Execute a simplified storage registration command
	remotePath := "/home/testuser/airavata"
	if strings.Contains(endpoint, "2222") { // SFTP container
		remotePath = "/home/testuser/upload/airavata"
	} else if strings.Contains(endpoint, "2225") || strings.Contains(endpoint, "2226") { // Bare metal containers
		remotePath = "/config/airavata"
	}
	registrationCmd := fmt.Sprintf("%s storage register --token=%s --name=%s", remotePath, token, name)

	sshArgs := []string{
		"-o", "StrictHostKeyChecking=no",
		"-o", "UserKnownHostsFile=/dev/null",
		"-o", "PubkeyAuthentication=yes",
		"-o", "PasswordAuthentication=no",
		"-o", "PreferredAuthentications=publickey",
		"-i", masterKeyPath,
		"-p", port,
		fmt.Sprintf("testuser@%s", host),
		registrationCmd,
	}

	sshCmd := exec.Command("ssh", sshArgs...)
	output, err := sshCmd.CombinedOutput()
	if err != nil {
		return "", fmt.Errorf("failed to execute storage registration command: %w, output: %s", err, string(output))
	}

	// Parse the output to extract the resource ID
	lines := strings.Split(string(output), "\n")
	for _, line := range lines {
		if strings.Contains(line, "Resource ID:") {
			parts := strings.Split(line, "Resource ID:")
			if len(parts) == 2 {
				return strings.TrimSpace(parts[1]), nil
			}
		}
	}

	return "", fmt.Errorf("failed to extract resource ID from output: %s", string(output))
}

// simulateCLIRegistration executes the actual CLI registration command on the resource
func (rr *ResourceRegistrar) simulateCLIRegistration(token, name, resourceID, sshEndpoint string) (string, error) {
	if rr.suite == nil {
		return "", fmt.Errorf("suite not available - use NewResourceRegistrarWithSuite")
	}

	// Execute the CLI registration command on the resource
	// This will generate SSH keys locally and complete the registration

	// Execute registration command on the resource via SSH
	// Use the deployed CLI binary
	registrationCmd := fmt.Sprintf("cd /tmp && ./airavata resource compute register --token=%s --name=%s --server=http://scheduler:8080",
		token, name)

	// Parse the SSH endpoint to get host and port
	parts := strings.Split(sshEndpoint, ":")
	if len(parts) != 2 {
		return "", fmt.Errorf("invalid SSH endpoint format: %s", sshEndpoint)
	}
	host, port := parts[0], parts[1]

	// Use sshpass for initial authentication (password-based)
	sshCmd := exec.Command("sshpass", "-p", "testpass", "ssh",
		"-o", "StrictHostKeyChecking=no",
		"-o", "UserKnownHostsFile=/dev/null",
		"-p", port,
		fmt.Sprintf("testuser@%s", host),
		registrationCmd)

	output, err := sshCmd.CombinedOutput()
	if err != nil {
		return "", fmt.Errorf("failed to execute registration command: %w, output: %s", err, string(output))
	}

	return resourceID, nil
}

// waitForRegistrationCompletion waits for the registration to complete and returns the resource
func (rr *ResourceRegistrar) waitForRegistrationCompletion(resourceID string) (*domain.ComputeResource, error) {
	if rr.suite == nil {
		return nil, fmt.Errorf("suite not available - use NewResourceRegistrarWithSuite")
	}

	// Poll the database to check if the resource has been activated
	maxWait := 30 * time.Second
	pollInterval := 1 * time.Second
	start := time.Now()

	for time.Since(start) < maxWait {
		// Query the resource from the database
		resource, err := rr.suite.RegistryService.GetResource(context.Background(), &domain.GetResourceRequest{
			ResourceID: resourceID,
		})
		if err != nil {
			return nil, fmt.Errorf("failed to get resource: %w", err)
		}

		// Check if the resource is now active
		if computeResource, ok := resource.Resource.(*domain.ComputeResource); ok && computeResource.Status == domain.ResourceStatusActive {
			return computeResource, nil
		}

		// Wait before next poll
		time.Sleep(pollInterval)
	}

	return nil, fmt.Errorf("timeout waiting for resource activation")
}

// waitForStorageRegistrationCompletion waits for the storage registration to complete
func (rr *ResourceRegistrar) waitForStorageRegistrationCompletion(resourceID string) (*domain.StorageResource, error) {
	if rr.suite == nil {
		return nil, fmt.Errorf("suite not available - use NewResourceRegistrarWithSuite")
	}

	// Poll the database to check if the storage resource has been activated
	maxWait := 30 * time.Second
	pollInterval := 1 * time.Second
	start := time.Now()

	for time.Since(start) < maxWait {
		// Query the storage resource from the database
		resource, err := rr.suite.RegistryService.GetResource(context.Background(), &domain.GetResourceRequest{
			ResourceID: resourceID,
		})
		if err != nil {
			return nil, fmt.Errorf("failed to get storage resource: %w", err)
		}

		// Check if the resource is now active
		if storageResource, ok := resource.Resource.(*domain.StorageResource); ok && storageResource.Status == domain.ResourceStatusActive {
			return storageResource, nil
		}

		// Wait before next poll
		time.Sleep(pollInterval)
	}

	return nil, fmt.Errorf("timeout waiting for storage resource activation")
}

// buildCLIBinary builds the CLI binary
func (rr *ResourceRegistrar) buildCLIBinary() error {
	// Get the current working directory and go up to project root
	currentDir, err := os.Getwd()
	if err != nil {
		return fmt.Errorf("failed to get current directory: %w", err)
	}

	// Go up from tests/integration to project root (2 levels up)
	projectRoot := filepath.Join(currentDir, "..", "..")
	projectRoot, err = filepath.Abs(projectRoot)
	if err != nil {
		return fmt.Errorf("failed to get absolute path: %w", err)
	}

	// Ensure bin directory exists
	binDir := filepath.Join(projectRoot, "bin")
	if err := os.MkdirAll(binDir, 0755); err != nil {
		return fmt.Errorf("failed to create bin directory: %w", err)
	}

	// Build the CLI binary for Linux x86_64 (for containers)
	cmd := exec.Command("go", "build", "-o", filepath.Join(binDir, "airavata"), filepath.Join(projectRoot, "cmd/cli"))
	cmd.Dir = projectRoot
	cmd.Env = append(os.Environ(), "GOOS=linux", "GOARCH=amd64", "CGO_ENABLED=0")
	output, err := cmd.CombinedOutput()
	if err != nil {
		return fmt.Errorf("failed to build CLI binary: %w, output: %s", err, string(output))
	}

	// Verify the binary was built correctly
	binaryPath := filepath.Join(binDir, "airavata")
	if _, err := os.Stat(binaryPath); os.IsNotExist(err) {
		return fmt.Errorf("CLI binary was not created at %s", binaryPath)
	}

	// Make sure the binary is executable
	if err := os.Chmod(binaryPath, 0755); err != nil {
		return fmt.Errorf("failed to make CLI binary executable: %w", err)
	}

	return nil
}

// parseEndpoint parses an endpoint string to extract host and port
func (rr *ResourceRegistrar) parseEndpoint(endpoint string) (host, port string, err error) {
	parts := strings.Split(endpoint, ":")
	if len(parts) != 2 {
		return "", "", fmt.Errorf("invalid endpoint format: %s", endpoint)
	}
	return parts[0], parts[1], nil
}

// CleanupRegistration removes any temporary files created during registration
func (rr *ResourceRegistrar) CleanupRegistration(endpoint, masterKeyPath string) error {
	// Parse endpoint to get host and port
	host, port, err := rr.parseEndpoint(endpoint)
	if err != nil {
		return fmt.Errorf("failed to parse endpoint: %w", err)
	}

	// Remove the CLI binary from the remote host
	remotePath := "/home/testuser/airavata"
	if strings.Contains(endpoint, "2222") { // SFTP container
		remotePath = "/home/testuser/upload/airavata"
	} else if strings.Contains(endpoint, "2225") || strings.Contains(endpoint, "2226") { // Bare metal containers
		remotePath = "/config/airavata"
	}
	sshArgs := []string{
		"-o", "StrictHostKeyChecking=no",
		"-o", "UserKnownHostsFile=/dev/null",
		"-o", "PubkeyAuthentication=yes",
		"-o", "PasswordAuthentication=no",
		"-o", "PreferredAuthentications=publickey",
		"-i", masterKeyPath,
		"-p", port,
		fmt.Sprintf("testuser@%s", host),
		"rm", "-f", remotePath,
	}

	sshCmd := exec.Command("ssh", sshArgs...)
	output, err := sshCmd.CombinedOutput()
	if err != nil {
		// Don't fail the cleanup if the file doesn't exist
		if !strings.Contains(string(output), "No such file") {
			return fmt.Errorf("failed to cleanup CLI binary: %w, output: %s", err, string(output))
		}
	}

	return nil
}
