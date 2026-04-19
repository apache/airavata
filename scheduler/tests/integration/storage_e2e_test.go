package integration

import (
	"context"
	"encoding/json"
	"fmt"
	"testing"
	"time"

	"github.com/apache/airavata/scheduler/core/domain"
	"github.com/apache/airavata/scheduler/tests/testutil"
	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
)

func TestMinIO_UploadDownload(t *testing.T) {

	suite := testutil.SetupIntegrationTest(t)
	defer suite.Cleanup()

	// Services are already verified by service checks above

	// Register MinIO as storage
	storageResource, err := suite.RegisterS3Resource("global-scratch", "localhost:9000")
	require.NoError(t, err)
	assert.NotNil(t, storageResource)

	// Create MinIO credentials
	credentialData := map[string]string{
		"access_key_id":     "minioadmin",
		"secret_access_key": "minioadmin",
	}
	credentialJSON, err := json.Marshal(credentialData)
	require.NoError(t, err)

	credential, err := suite.VaultService.StoreCredential(context.Background(), "minio-credentials", domain.CredentialTypeAPIKey, credentialJSON, suite.TestUser.ID)
	require.NoError(t, err)

	// Bind credential to storage resource
	err = suite.SpiceDBAdapter.BindCredentialToResource(context.Background(), credential.ID, storageResource.ID, "storage_resource")
	require.NoError(t, err)

	// Create test file
	testData := []byte("Hello from MinIO storage test")

	// Upload file
	err = suite.UploadFile(storageResource.ID, "test-file.txt", testData)
	require.NoError(t, err)

	// Download and verify
	downloaded, err := suite.DownloadFile(storageResource.ID, "test-file.txt")
	require.NoError(t, err)
	assert.Equal(t, testData, downloaded)
}

func TestMinIO_MultipleFiles(t *testing.T) {

	suite := testutil.SetupIntegrationTest(t)
	defer suite.Cleanup()

	// Services are already verified by service checks above

	// Register MinIO as storage
	storageResource, err := suite.RegisterS3Resource("global-scratch", "localhost:9000")
	require.NoError(t, err)
	assert.NotNil(t, storageResource)

	// Upload multiple files
	files := map[string][]byte{
		"file1.txt": []byte("Content of file 1"),
		"file2.txt": []byte("Content of file 2"),
		"file3.txt": []byte("Content of file 3"),
	}

	for filename, content := range files {
		err = suite.UploadFile(storageResource.ID, filename, content)
		require.NoError(t, err, "Failed to upload %s", filename)
	}

	// Download and verify all files
	for filename, expectedContent := range files {
		downloaded, err := suite.DownloadFile(storageResource.ID, filename)
		require.NoError(t, err, "Failed to download %s", filename)
		assert.Equal(t, expectedContent, downloaded, "Content mismatch for %s", filename)
	}
}

func TestMinIO_LargeFile(t *testing.T) {

	suite := testutil.SetupIntegrationTest(t)
	defer suite.Cleanup()

	// Services are already verified by service checks above

	// Register MinIO as storage
	storageResource, err := suite.RegisterS3Resource("global-scratch", "localhost:9000")
	require.NoError(t, err)
	assert.NotNil(t, storageResource)

	// Create large test file (1MB)
	largeData := make([]byte, 1024*1024)
	for i := range largeData {
		largeData[i] = byte(i % 256)
	}

	// Upload large file
	err = suite.UploadFile(storageResource.ID, "large-file.bin", largeData)
	require.NoError(t, err)

	// Download and verify
	downloaded, err := suite.DownloadFile(storageResource.ID, "large-file.bin")
	require.NoError(t, err)
	assert.Equal(t, largeData, downloaded)
	assert.Len(t, downloaded, 1024*1024)
}

func TestSFTP_DataStaging(t *testing.T) {

	suite := testutil.SetupIntegrationTest(t)
	defer suite.Cleanup()

	// Services are already verified by service checks above

	// Register SFTP as storage
	storageResource, err := suite.RegisterSFTPResource("test-sftp", "localhost:2222")
	require.NoError(t, err)
	assert.NotNil(t, storageResource)

	// Create test file
	testData := []byte("Hello from SFTP storage test")

	// Upload file
	err = suite.UploadFile(storageResource.ID, "sftp-test-file.txt", testData)
	require.NoError(t, err)

	// Download and verify
	downloaded, err := suite.DownloadFile(storageResource.ID, "sftp-test-file.txt")
	require.NoError(t, err)
	assert.Equal(t, testData, downloaded)
}

func TestSFTP_MultipleFiles(t *testing.T) {

	suite := testutil.SetupIntegrationTest(t)
	defer suite.Cleanup()

	// Services are already verified by service checks above

	// Register SFTP as storage
	storageResource, err := suite.RegisterSFTPResource("test-sftp", "localhost:2222")
	require.NoError(t, err)
	assert.NotNil(t, storageResource)

	// Upload multiple files
	files := map[string][]byte{
		"sftp-file1.txt": []byte("SFTP Content of file 1"),
		"sftp-file2.txt": []byte("SFTP Content of file 2"),
		"sftp-file3.txt": []byte("SFTP Content of file 3"),
	}

	for filename, content := range files {
		err = suite.UploadFile(storageResource.ID, filename, content)
		require.NoError(t, err, "Failed to upload %s", filename)
	}

	// Download and verify all files
	for filename, expectedContent := range files {
		downloaded, err := suite.DownloadFile(storageResource.ID, filename)
		require.NoError(t, err, "Failed to download %s", filename)
		assert.Equal(t, expectedContent, downloaded, "Content mismatch for %s", filename)
	}
}

func TestSFTP_DirectoryOperations(t *testing.T) {

	suite := testutil.SetupIntegrationTest(t)
	defer suite.Cleanup()

	// Services are already verified by service checks above

	// Register SFTP as storage
	storageResource, err := suite.RegisterSFTPResource("test-sftp", "localhost:2222")
	require.NoError(t, err)
	assert.NotNil(t, storageResource)

	// Test directory operations
	testData := []byte("Directory operation test")

	// Upload file to subdirectory
	err = suite.UploadFile(storageResource.ID, "subdir/test-file.txt", testData)
	require.NoError(t, err)

	// Download from subdirectory
	downloaded, err := suite.DownloadFile(storageResource.ID, "subdir/test-file.txt")
	require.NoError(t, err)
	assert.Equal(t, testData, downloaded)
}

func TestStorage_ConcurrentAccess(t *testing.T) {

	suite := testutil.SetupIntegrationTest(t)
	defer suite.Cleanup()

	// Services are already verified by service checks above

	// Register both storage resources
	minioResource, err := suite.RegisterS3Resource("global-scratch", "localhost:9000")
	require.NoError(t, err)
	assert.NotNil(t, minioResource)

	sftpResource, err := suite.RegisterSFTPResource("test-sftp", "localhost:2222")
	require.NoError(t, err)
	assert.NotNil(t, sftpResource)

	// Test concurrent access to both storage systems
	testData := []byte("Concurrent storage test")

	// Upload to both storage systems
	err = suite.UploadFile(minioResource.ID, "concurrent-minio.txt", testData)
	require.NoError(t, err)

	err = suite.UploadFile(sftpResource.ID, "concurrent-sftp.txt", testData)
	require.NoError(t, err)

	// Download from both storage systems
	minioData, err := suite.DownloadFile(minioResource.ID, "concurrent-minio.txt")
	require.NoError(t, err)
	assert.Equal(t, testData, minioData)

	sftpData, err := suite.DownloadFile(sftpResource.ID, "concurrent-sftp.txt")
	require.NoError(t, err)
	assert.Equal(t, testData, sftpData)
}

func TestStorage_ErrorHandling(t *testing.T) {

	suite := testutil.SetupIntegrationTest(t)
	defer suite.Cleanup()

	// Services are already verified by service checks above

	// Register MinIO as storage
	storageResource, err := suite.RegisterS3Resource("global-scratch", "localhost:9000")
	require.NoError(t, err)
	assert.NotNil(t, storageResource)

	// Test downloading non-existent file
	_, err = suite.DownloadFile(storageResource.ID, "non-existent-file.txt")
	assert.Error(t, err, "Should return error for non-existent file")

	// Test uploading empty file
	err = suite.UploadFile(storageResource.ID, "empty-file.txt", []byte{})
	require.NoError(t, err, "Should allow empty file upload")

	// Download empty file
	downloaded, err := suite.DownloadFile(storageResource.ID, "empty-file.txt")
	require.NoError(t, err)
	assert.Empty(t, downloaded)
}

func TestStorage_Performance(t *testing.T) {

	suite := testutil.SetupIntegrationTest(t)
	defer suite.Cleanup()

	// Services are already verified by service checks above

	// Register MinIO as storage
	storageResource, err := suite.RegisterS3Resource("global-scratch", "localhost:9000")
	require.NoError(t, err)
	assert.NotNil(t, storageResource)

	// Test performance with multiple small files
	start := time.Now()

	for i := 0; i < 10; i++ {
		testData := []byte(fmt.Sprintf("Performance test file %d", i))
		err = suite.UploadFile(storageResource.ID, fmt.Sprintf("perf-file-%d.txt", i), testData)
		require.NoError(t, err)
	}

	uploadDuration := time.Since(start)
	t.Logf("Uploaded 10 files in %v", uploadDuration)

	// Download all files
	start = time.Now()

	for i := 0; i < 10; i++ {
		downloaded, err := suite.DownloadFile(storageResource.ID, fmt.Sprintf("perf-file-%d.txt", i))
		require.NoError(t, err)
		expected := []byte(fmt.Sprintf("Performance test file %d", i))
		assert.Equal(t, expected, downloaded)
	}

	downloadDuration := time.Since(start)
	t.Logf("Downloaded 10 files in %v", downloadDuration)

	// Performance assertions (adjust thresholds as needed)
	assert.Less(t, uploadDuration, 30*time.Second, "Upload should complete within 30 seconds")
	assert.Less(t, downloadDuration, 30*time.Second, "Download should complete within 30 seconds")
}
