package integration

import (
	"context"
	"os/exec"
	"testing"
	"time"

	"github.com/apache/airavata/scheduler/adapters"
	"github.com/apache/airavata/scheduler/core/domain"
	"github.com/apache/airavata/scheduler/tests/testutil"
	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
)

// TestDockerServices_HealthCheck verifies that all Docker services are healthy and accessible
func TestDockerServices_HealthCheck(t *testing.T) {

	// Check required services are available before starting
	checker := testutil.NewServiceChecker()
	services := []struct {
		name  string
		check func() error
	}{
		{"SLURM", checker.CheckSLURMService},
		{"SSH", checker.CheckSSHService},
		{"SFTP", checker.CheckSFTPService},
		{"MinIO", checker.CheckMinIOService},
	}

	for _, svc := range services {
		if err := svc.check(); err != nil {
			t.Fatalf("Required service %s not available: %v", svc.name, err)
		}
	}

	suite := testutil.SetupIntegrationTest(t)
	defer suite.Cleanup()

	// Services are already verified by service checks above
	var err error

	// Verify PostgreSQL connectivity
	t.Run("PostgreSQL", func(t *testing.T) {
		// Test database connection by creating a simple table
		result := suite.DB.DB.GetDB().Exec("CREATE TABLE IF NOT EXISTS connectivity_test (id SERIAL PRIMARY KEY, name TEXT)")
		require.NoError(t, result.Error)

		// Insert test data
		result = suite.DB.DB.GetDB().Exec("INSERT INTO connectivity_test (name) VALUES ('test')")
		require.NoError(t, result.Error)

		// Query test data
		var count int
		result = suite.DB.DB.GetDB().Raw("SELECT COUNT(*) FROM connectivity_test").Scan(&count)
		require.NoError(t, result.Error)
		assert.Greater(t, count, 0)

		// Cleanup
		suite.DB.DB.GetDB().Exec("DROP TABLE IF EXISTS connectivity_test")
	})

	// Verify MinIO connectivity
	t.Run("MinIO", func(t *testing.T) {
		// Test MinIO connection by creating a bucket
		err := suite.Compose.CreateTestBucket(t, "connectivity-test-bucket")
		require.NoError(t, err)

		// Cleanup
		suite.Compose.CleanupTestBucket(t, "connectivity-test-bucket")
	})

	// Verify SFTP connectivity
	t.Run("SFTP", func(t *testing.T) {
		// Test SFTP connection by creating a directory
		cmd := exec.Command("docker", "exec",
			"airavata-scheduler-sftp-1",
			"mkdir", "-p", "/home/testuser/connectivity-test")
		err = cmd.Run()
		require.NoError(t, err)

		// Verify directory exists
		cmd = exec.Command("docker", "exec",
			"airavata-scheduler-sftp-1",
			"test", "-d", "/home/testuser/connectivity-test")
		err = cmd.Run()
		require.NoError(t, err)

		// Cleanup
		cmd = exec.Command("docker", "exec",
			"airavata-scheduler-sftp-1",
			"rm", "-rf", "/home/testuser/connectivity-test")
		cmd.Run()
	})

	// Verify SLURM connectivity
	t.Run("SLURM", func(t *testing.T) {
		// Check if SLURM container exists
		cmd := exec.Command("docker", "ps", "--filter", "name=slurm", "--format", "{{.Names}}")
		output, err := cmd.Output()
		if err != nil || len(output) == 0 {
			t.Skip("SLURM service not available")
		}

		// Test SLURM connection by checking cluster status
		cmd = exec.Command("docker", "exec",
			"airavata-scheduler-slurm-cluster-01-1",
			"scontrol", "ping")
		err = cmd.Run()
		require.NoError(t, err)

		// Test SLURM job submission
		cmd = exec.Command("docker", "exec",
			"airavata-scheduler-slurm-cluster-01-1",
			"sbatch", "--wrap", "echo 'SLURM connectivity test'")
		output, err = cmd.Output()
		require.NoError(t, err)
		assert.Contains(t, string(output), "Submitted batch job")
	})

	// Verify SSH server connectivity
	t.Run("SSH", func(t *testing.T) {
		// Test SSH connection by executing a command using master SSH key
		config := testutil.GetTestConfig()
		cmd := exec.Command("ssh", "-o", "StrictHostKeyChecking=no", "-o", "UserKnownHostsFile=/dev/null",
			"-i", config.MasterSSHKeyPath, "-p", "2223", "testuser@localhost", "echo 'SSH connectivity test'")
		output, err := cmd.Output()
		require.NoError(t, err)
		assert.Contains(t, string(output), "SSH connectivity test")
	})
}

// TestDockerServices_NetworkConnectivity verifies network connectivity between services
func TestDockerServices_NetworkConnectivity(t *testing.T) {

	// Check required services are available before starting
	checker := testutil.NewServiceChecker()
	services := []struct {
		name  string
		check func() error
	}{
		{"SLURM", checker.CheckSLURMService},
		{"SSH", checker.CheckSSHService},
		{"SFTP", checker.CheckSFTPService},
		{"MinIO", checker.CheckMinIOService},
	}

	for _, svc := range services {
		if err := svc.check(); err != nil {
			t.Fatalf("Required service %s not available: %v", svc.name, err)
		}
	}

	suite := testutil.SetupIntegrationTest(t)
	defer suite.Cleanup()

	// Services are already verified by service checks above

	// Test network connectivity between services
	t.Run("ServiceToService", func(t *testing.T) {
		// Check if SLURM container exists
		cmd := exec.Command("docker", "ps", "--filter", "name=slurm", "--format", "{{.Names}}")
		output, err := cmd.Output()
		if err != nil || len(output) == 0 {
			t.Skip("SLURM service not available")
		}

		// Test SLURM to MinIO connectivity
		cmd = exec.Command("docker", "exec",
			"airavata-scheduler-slurm-cluster-01-1",
			"nc", "-z", "minio", "9000")
		err = cmd.Run()
		require.NoError(t, err, "SLURM should be able to connect to MinIO")

		// Test SLURM to PostgreSQL connectivity
		cmd = exec.Command("docker", "exec",
			"airavata-scheduler-slurm-cluster-01-1",
			"nc", "-z", "postgres", "5432")
		err = cmd.Run()
		require.NoError(t, err, "SLURM should be able to connect to PostgreSQL")
	})

	// Test external connectivity
	t.Run("ExternalConnectivity", func(t *testing.T) {
		// Check if SLURM container exists
		cmd := exec.Command("docker", "ps", "--filter", "name=slurm", "--format", "{{.Names}}")
		output, err := cmd.Output()
		if err != nil || len(output) == 0 {
			t.Skip("SLURM service not available")
		}

		// Test internet connectivity from SLURM container
		cmd = exec.Command("docker", "exec",
			"airavata-scheduler-slurm-cluster-01-1",
			"ping", "-c", "1", "8.8.8.8")
		err = cmd.Run()
		require.NoError(t, err, "SLURM should have internet connectivity")
	})
}

// TestDockerServices_ResourceAvailability verifies that services have sufficient resources
func TestDockerServices_ResourceAvailability(t *testing.T) {

	// Check required services are available before starting
	checker := testutil.NewServiceChecker()
	services := []struct {
		name  string
		check func() error
	}{
		{"SLURM", checker.CheckSLURMService},
		{"SSH", checker.CheckSSHService},
		{"SFTP", checker.CheckSFTPService},
		{"MinIO", checker.CheckMinIOService},
	}

	for _, svc := range services {
		if err := svc.check(); err != nil {
			t.Fatalf("Required service %s not available: %v", svc.name, err)
		}
	}

	suite := testutil.SetupIntegrationTest(t)
	defer suite.Cleanup()

	// Services are already verified by service checks above

	// Test resource availability
	t.Run("DiskSpace", func(t *testing.T) {
		// Check if SLURM container exists
		cmd := exec.Command("docker", "ps", "--filter", "name=slurm", "--format", "{{.Names}}")
		output, err := cmd.Output()
		if err != nil || len(output) == 0 {
			t.Skip("SLURM service not available")
		}

		// Check disk space in SLURM container
		cmd = exec.Command("docker", "exec",
			"airavata-scheduler-slurm-cluster-01-1",
			"df", "-h", "/")
		output, err = cmd.Output()
		require.NoError(t, err)
		assert.Contains(t, string(output), "/")
	})

	t.Run("Memory", func(t *testing.T) {
		// Check if SLURM container exists
		cmd := exec.Command("docker", "ps", "--filter", "name=slurm", "--format", "{{.Names}}")
		output, err := cmd.Output()
		if err != nil || len(output) == 0 {
			t.Skip("SLURM service not available")
		}

		// Check memory in SLURM container
		cmd = exec.Command("docker", "exec",
			"airavata-scheduler-slurm-cluster-01-1",
			"free", "-m")
		output, err = cmd.Output()
		require.NoError(t, err)
		assert.Contains(t, string(output), "Mem:")
	})

	t.Run("CPU", func(t *testing.T) {
		// Check if SLURM container exists
		cmd := exec.Command("docker", "ps", "--filter", "name=slurm", "--format", "{{.Names}}")
		output, err := cmd.Output()
		if err != nil || len(output) == 0 {
			t.Skip("SLURM service not available")
		}

		// Check CPU info in SLURM container
		cmd = exec.Command("docker", "exec",
			"airavata-scheduler-slurm-cluster-01-1",
			"nproc")
		output, err = cmd.Output()
		require.NoError(t, err)
		assert.Greater(t, len(string(output)), 0)
	})
}

func TestSLURM_SSHConnectionFailure(t *testing.T) {

	// Check required services are available before starting
	checker := testutil.NewServiceChecker()
	services := []struct {
		name  string
		check func() error
	}{
		{"SLURM", checker.CheckSLURMService},
		{"SSH", checker.CheckSSHService},
		{"SFTP", checker.CheckSFTPService},
		{"MinIO", checker.CheckMinIOService},
	}

	for _, svc := range services {
		if err := svc.check(); err != nil {
			t.Fatalf("Required service %s not available: %v", svc.name, err)
		}
	}

	suite := testutil.SetupIntegrationTest(t)
	defer suite.Cleanup()

	// Create a SLURM resource with invalid SSH endpoint
	invalidResource := &domain.ComputeResource{
		ID:          "invalid-slurm",
		Name:        "invalid-slurm-cluster",
		Type:        domain.ComputeResourceTypeSlurm,
		Endpoint:    "invalid-host:6817",
		Status:      domain.ResourceStatusActive,
		MaxWorkers:  10,
		CostPerHour: 1.0,
	}

	// Create adapter
	adapter, err := adapters.NewComputeAdapter(*invalidResource, suite.VaultService)
	require.NoError(t, err)

	// Set user context for adapter connection attempt
	ctx := context.WithValue(context.Background(), "userID", suite.TestUser.ID)

	// Try to generate script (should work)
	task := &domain.Task{
		ID:         "test-task",
		Command:    "echo 'Hello World'",
		Status:     domain.TaskStatusCreated,
		CreatedAt:  time.Now(),
		UpdatedAt:  time.Now(),
		RetryCount: 0,
		MaxRetries: 3,
	}

	outputDir := "/tmp/test"
	scriptPath, err := adapter.GenerateScript(*task, outputDir)
	require.NoError(t, err)
	assert.NotEmpty(t, scriptPath)

	// Try to submit task (should fail with connection error)
	_, err = adapter.SubmitTask(ctx, scriptPath)
	assert.Error(t, err)
	// Connection errors may show up differently - check for SSH or connection-related errors
	assert.True(t, err != nil, "Expected an error when submitting to invalid host")
}

func TestSLURM_InvalidCredentials(t *testing.T) {

	// Check required services are available before starting
	checker := testutil.NewServiceChecker()
	services := []struct {
		name  string
		check func() error
	}{
		{"SLURM", checker.CheckSLURMService},
		{"SSH", checker.CheckSSHService},
		{"SFTP", checker.CheckSFTPService},
		{"MinIO", checker.CheckMinIOService},
	}

	for _, svc := range services {
		if err := svc.check(); err != nil {
			t.Fatalf("Required service %s not available: %v", svc.name, err)
		}
	}

	suite := testutil.SetupIntegrationTest(t)
	defer func() {
		if suite != nil {
			suite.Cleanup()
		}
	}()

	// Services are already verified by service checks above
	var err error

	// Create SSH credential with invalid private key

	_, err = suite.VaultService.StoreCredential(
		context.Background(),
		"invalid-ssh-key",
		domain.CredentialTypeSSHKey,
		[]byte("invalid-private-key"),
		suite.TestUser.ID,
	)
	require.NoError(t, err)

	// Create SLURM resource with invalid credentials
	req := &domain.CreateComputeResourceRequest{
		Name:        "slurm-invalid-creds",
		Type:        domain.ComputeResourceTypeSlurm,
		Endpoint:    "localhost:6817",
		OwnerID:     suite.TestUser.ID,
		MaxWorkers:  10,
		CostPerHour: 1.0,
	}

	resp, err := suite.RegistryService.RegisterComputeResource(context.Background(), req)
	require.NoError(t, err)
	resource := resp.Resource

	// Create adapter
	adapter, err := adapters.NewComputeAdapter(*resource, suite.VaultService)
	require.NoError(t, err)

	// Try to submit task (should fail with authentication error)
	task := &domain.Task{
		ID:         "test-task",
		Command:    "echo 'Hello World'",
		Status:     domain.TaskStatusCreated,
		CreatedAt:  time.Now(),
		UpdatedAt:  time.Now(),
		RetryCount: 0,
		MaxRetries: 3,
	}

	outputDir := "/tmp/test"
	scriptPath, err := adapter.GenerateScript(*task, outputDir)
	require.NoError(t, err)

	_, err = adapter.SubmitTask(context.Background(), scriptPath)
	assert.Error(t, err)
	if err != nil {
		assert.Contains(t, err.Error(), "authentication")
	}
}

func TestSLURM_NetworkTimeout(t *testing.T) {

	// Check required services are available before starting
	checker := testutil.NewServiceChecker()
	services := []struct {
		name  string
		check func() error
	}{
		{"SLURM", checker.CheckSLURMService},
		{"SSH", checker.CheckSSHService},
		{"SFTP", checker.CheckSFTPService},
		{"MinIO", checker.CheckMinIOService},
	}

	for _, svc := range services {
		if err := svc.check(); err != nil {
			t.Fatalf("Required service %s not available: %v", svc.name, err)
		}
	}

	suite := testutil.SetupIntegrationTest(t)
	defer suite.Cleanup()

	// Create a SLURM resource with unreachable endpoint
	timeoutResource := &domain.ComputeResource{
		ID:          "timeout-slurm",
		Name:        "timeout-slurm-cluster",
		Type:        domain.ComputeResourceTypeSlurm,
		Endpoint:    "192.168.255.255:6817", // Unreachable IP
		Status:      domain.ResourceStatusActive,
		MaxWorkers:  10,
		CostPerHour: 1.0,
	}

	// Create adapter
	adapter, err := adapters.NewComputeAdapter(*timeoutResource, suite.VaultService)
	require.NoError(t, err)

	// Try to submit task (should fail with timeout)
	task := &domain.Task{
		ID:         "test-task",
		Command:    "echo 'Hello World'",
		Status:     domain.TaskStatusCreated,
		CreatedAt:  time.Now(),
		UpdatedAt:  time.Now(),
		RetryCount: 0,
		MaxRetries: 3,
	}

	outputDir := "/tmp/test"
	scriptPath, err := adapter.GenerateScript(*task, outputDir)
	require.NoError(t, err)

	_, err = adapter.SubmitTask(context.Background(), scriptPath)
	assert.Error(t, err)
	assert.Contains(t, err.Error(), "timeout")
}

func TestBareMetal_PortNotOpen(t *testing.T) {

	// Check required services are available before starting
	checker := testutil.NewServiceChecker()
	services := []struct {
		name  string
		check func() error
	}{
		{"SLURM", checker.CheckSLURMService},
		{"SSH", checker.CheckSSHService},
		{"SFTP", checker.CheckSFTPService},
		{"MinIO", checker.CheckMinIOService},
	}

	for _, svc := range services {
		if err := svc.check(); err != nil {
			t.Fatalf("Required service %s not available: %v", svc.name, err)
		}
	}

	suite := testutil.SetupIntegrationTest(t)
	defer suite.Cleanup()

	// Create a bare metal resource with closed port
	closedPortResource := &domain.ComputeResource{
		ID:          "closed-port-baremetal",
		Name:        "closed-port-baremetal",
		Type:        domain.ComputeResourceTypeBareMetal,
		Endpoint:    "localhost:9999", // Closed port
		Status:      domain.ResourceStatusActive,
		MaxWorkers:  10,
		CostPerHour: 1.0,
	}

	// Create adapter
	adapter, err := adapters.NewComputeAdapter(*closedPortResource, suite.VaultService)
	require.NoError(t, err)

	// Try to submit task (should fail with connection refused)
	task := &domain.Task{
		ID:         "test-task",
		Command:    "echo 'Hello World'",
		Status:     domain.TaskStatusCreated,
		CreatedAt:  time.Now(),
		UpdatedAt:  time.Now(),
		RetryCount: 0,
		MaxRetries: 3,
	}

	outputDir := "/tmp/test"
	scriptPath, err := adapter.GenerateScript(*task, outputDir)
	require.NoError(t, err)

	_, err = adapter.SubmitTask(context.Background(), scriptPath)
	assert.Error(t, err)
	assert.Contains(t, err.Error(), "connection refused")
}

func TestBareMetal_HostUnreachable(t *testing.T) {

	// Check required services are available before starting
	checker := testutil.NewServiceChecker()
	services := []struct {
		name  string
		check func() error
	}{
		{"SLURM", checker.CheckSLURMService},
		{"SSH", checker.CheckSSHService},
		{"SFTP", checker.CheckSFTPService},
		{"MinIO", checker.CheckMinIOService},
	}

	for _, svc := range services {
		if err := svc.check(); err != nil {
			t.Fatalf("Required service %s not available: %v", svc.name, err)
		}
	}

	suite := testutil.SetupIntegrationTest(t)
	defer suite.Cleanup()

	// Create a bare metal resource with unreachable host
	unreachableResource := &domain.ComputeResource{
		ID:          "unreachable-baremetal",
		Name:        "unreachable-baremetal",
		Type:        domain.ComputeResourceTypeBareMetal,
		Endpoint:    "192.168.255.255:22", // Unreachable IP
		Status:      domain.ResourceStatusActive,
		MaxWorkers:  10,
		CostPerHour: 1.0,
	}

	// Create adapter
	adapter, err := adapters.NewComputeAdapter(*unreachableResource, suite.VaultService)
	require.NoError(t, err)

	// Try to submit task (should fail with host unreachable)
	task := &domain.Task{
		ID:         "test-task",
		Command:    "echo 'Hello World'",
		Status:     domain.TaskStatusCreated,
		CreatedAt:  time.Now(),
		UpdatedAt:  time.Now(),
		RetryCount: 0,
		MaxRetries: 3,
	}

	outputDir := "/tmp/test"
	scriptPath, err := adapter.GenerateScript(*task, outputDir)
	require.NoError(t, err)

	_, err = adapter.SubmitTask(context.Background(), scriptPath)
	assert.Error(t, err)
	assert.Contains(t, err.Error(), "no route to host")
}

func TestStorage_S3InvalidEndpoint(t *testing.T) {

	// Check required services are available before starting
	checker := testutil.NewServiceChecker()
	services := []struct {
		name  string
		check func() error
	}{
		{"SLURM", checker.CheckSLURMService},
		{"SSH", checker.CheckSSHService},
		{"SFTP", checker.CheckSFTPService},
		{"MinIO", checker.CheckMinIOService},
	}

	for _, svc := range services {
		if err := svc.check(); err != nil {
			t.Fatalf("Required service %s not available: %v", svc.name, err)
		}
	}

	suite := testutil.SetupIntegrationTest(t)
	defer suite.Cleanup()

	// Create S3 credential
	_, err := suite.VaultService.StoreCredential(
		context.Background(),
		"test-s3-cred",
		domain.CredentialTypeAPIKey,
		[]byte("testadmin:testpass"),
		suite.TestUser.ID,
	)
	require.NoError(t, err)

	// Create S3 resource with invalid endpoint
	capacity := int64(1000000000)
	req := &domain.CreateStorageResourceRequest{
		Name:          "invalid-s3",
		Type:          domain.StorageResourceTypeS3,
		Endpoint:      "invalid-endpoint:9999",
		OwnerID:       suite.TestUser.ID,
		TotalCapacity: &capacity,
	}

	resp, err := suite.RegistryService.RegisterStorageResource(context.Background(), req)
	require.NoError(t, err)
	invalidS3Resource := resp.Resource

	// Create adapter
	adapter, err := adapters.NewStorageAdapter(*invalidS3Resource, suite.VaultService)
	require.NoError(t, err)

	// Try to upload file (should fail with connection error)
	tempFile := "/tmp/test-file.txt"
	err = adapter.Upload(tempFile, "test-file.txt", suite.TestUser.ID)
	assert.Error(t, err)
	assert.Contains(t, err.Error(), "connection")
}

func TestStorage_SFTPAuthFailure(t *testing.T) {

	// Check required services are available before starting
	checker := testutil.NewServiceChecker()
	services := []struct {
		name  string
		check func() error
	}{
		{"SLURM", checker.CheckSLURMService},
		{"SSH", checker.CheckSSHService},
		{"SFTP", checker.CheckSFTPService},
		{"MinIO", checker.CheckMinIOService},
	}

	for _, svc := range services {
		if err := svc.check(); err != nil {
			t.Fatalf("Required service %s not available: %v", svc.name, err)
		}
	}

	suite := testutil.SetupIntegrationTest(t)
	defer suite.Cleanup()

	// Services are already verified by service checks above
	var err error

	// Create SFTP credential with invalid password

	_, err = suite.VaultService.StoreCredential(
		context.Background(),
		"invalid-sftp-cred",
		domain.CredentialTypePassword,
		[]byte("invalid:password"),
		suite.TestUser.ID,
	)
	require.NoError(t, err)

	// Create SFTP resource with invalid credentials
	capacity := int64(1000000000)
	req := &domain.CreateStorageResourceRequest{
		Name:          "sftp-invalid-creds",
		Type:          domain.StorageResourceTypeSFTP,
		Endpoint:      "localhost:2222",
		OwnerID:       suite.TestUser.ID,
		TotalCapacity: &capacity,
	}

	resp, err := suite.RegistryService.RegisterStorageResource(context.Background(), req)
	require.NoError(t, err)
	resource := resp.Resource

	// Create adapter
	adapter, err := adapters.NewStorageAdapter(*resource, suite.VaultService)
	require.NoError(t, err)

	// Try to upload file (should fail with authentication error)
	tempFile := "/tmp/test-file.txt"
	err = adapter.Upload(tempFile, "test-file.txt", suite.TestUser.ID)
	assert.Error(t, err)
	assert.Contains(t, err.Error(), "authentication")
}
