package integration

import (
	"fmt"
	"testing"
	"time"

	"github.com/apache/airavata/scheduler/tests/testutil"
	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
)

func TestStorage_VeryLargeFile(t *testing.T) {
	if testing.Short() {
		t.Skip("Skipping integration test in short mode")
	}

	suite := testutil.SetupIntegrationTest(t)
	defer suite.Cleanup()

	// Services are already running and verified by SetupIntegrationTest

	// Wait for MinIO to be ready
	var err error
	err = suite.Compose.WaitForServices(t, 1*time.Minute)
	require.NoError(t, err)

	// Register S3 resource
	resource, err := suite.RegisterS3Resource("large-file-minio", "localhost:9000")
	require.NoError(t, err)

	// Create a large file (10MB)
	largeData := make([]byte, 10*1024*1024) // 10MB
	for i := range largeData {
		largeData[i] = byte(i % 256)
	}

	// Upload large file
	err = suite.UploadFile(resource.ID, "large-file.bin", largeData)
	require.NoError(t, err)

	// Download and verify
	downloadedData, err := suite.DownloadFile(resource.ID, "large-file.bin")
	require.NoError(t, err)
	assert.Equal(t, largeData, downloadedData)
}

func TestStorage_SpecialCharactersInFilename(t *testing.T) {
	if testing.Short() {
		t.Skip("Skipping integration test in short mode")
	}

	suite := testutil.SetupIntegrationTest(t)
	defer suite.Cleanup()

	// Services are already running and verified by SetupIntegrationTest

	// Wait for MinIO to be ready
	var err error
	err = suite.Compose.WaitForServices(t, 1*time.Minute)
	require.NoError(t, err)

	// Register S3 resource
	resource, err := suite.RegisterS3Resource("special-chars-minio", "localhost:9000")
	require.NoError(t, err)

	// Test various special characters in filenames
	specialFilenames := []string{
		"file with spaces.txt",
		"file-with-dashes.txt",
		"file_with_underscores.txt",
		"file.with.dots.txt",
		"file(with)parentheses.txt",
		"file[with]brackets.txt",
		"file{with}braces.txt",
		"file@with#symbols$.txt",
		"file%with&special*chars.txt",
		"file+with=operators.txt",
		"file|with|pipes.txt",
		"file\\with\\backslashes.txt",
		"file/with/forward/slashes.txt",
		"file:with:colons.txt",
		"file;with;semicolons.txt",
		"file\"with\"quotes.txt",
		"file'with'apostrophes.txt",
		"file<with>angle>brackets.txt",
		"file?with?question?marks.txt",
		"file!with!exclamation!marks.txt",
	}

	testData := []byte("test data with special characters")

	for _, filename := range specialFilenames {
		// Upload file
		err = suite.UploadFile(resource.ID, filename, testData)
		require.NoError(t, err, "Failed to upload file with special characters: %s", filename)

		// Download and verify
		downloadedData, err := suite.DownloadFile(resource.ID, filename)
		require.NoError(t, err, "Failed to download file with special characters: %s", filename)
		assert.Equal(t, testData, downloadedData, "Data mismatch for file: %s", filename)
	}
}

func TestStorage_DeepDirectoryNesting(t *testing.T) {
	if testing.Short() {
		t.Skip("Skipping integration test in short mode")
	}

	suite := testutil.SetupIntegrationTest(t)
	defer suite.Cleanup()

	// Services are already running and verified by SetupIntegrationTest

	// Wait for MinIO to be ready
	var err error
	err = suite.Compose.WaitForServices(t, 1*time.Minute)
	require.NoError(t, err)

	// Register S3 resource
	resource, err := suite.RegisterS3Resource("deep-nesting-minio", "localhost:9000")
	require.NoError(t, err)

	// Create deeply nested directory structure
	deepPath := "level1/level2/level3/level4/level5/level6/level7/level8/level9/level10/deep-file.txt"
	testData := []byte("test data in deep directory")

	// Upload file to deep path
	err = suite.UploadFile(resource.ID, deepPath, testData)
	require.NoError(t, err)

	// Download and verify
	downloadedData, err := suite.DownloadFile(resource.ID, deepPath)
	require.NoError(t, err)
	assert.Equal(t, testData, downloadedData)
}

func TestStorage_BinaryFiles(t *testing.T) {
	if testing.Short() {
		t.Skip("Skipping integration test in short mode")
	}

	suite := testutil.SetupIntegrationTest(t)
	defer suite.Cleanup()

	// Services are already running and verified by SetupIntegrationTest

	// Wait for MinIO to be ready
	var err error
	err = suite.Compose.WaitForServices(t, 1*time.Minute)
	require.NoError(t, err)

	// Register S3 resource
	resource, err := suite.RegisterS3Resource("binary-files-minio", "localhost:9000")
	require.NoError(t, err)

	// Create binary data with various byte patterns
	binaryData := make([]byte, 1024)
	for i := range binaryData {
		binaryData[i] = byte(i % 256)
	}

	// Test various binary file types
	binaryFiles := []string{
		"binary-data.bin",
		"image.jpg",
		"document.pdf",
		"archive.zip",
		"executable.exe",
		"library.so",
		"database.db",
	}

	for _, filename := range binaryFiles {
		// Upload binary file
		err = suite.UploadFile(resource.ID, filename, binaryData)
		require.NoError(t, err, "Failed to upload binary file: %s", filename)

		// Download and verify
		downloadedData, err := suite.DownloadFile(resource.ID, filename)
		require.NoError(t, err, "Failed to download binary file: %s", filename)
		assert.Equal(t, binaryData, downloadedData, "Binary data mismatch for file: %s", filename)
	}
}

func TestStorage_ConcurrentWrites(t *testing.T) {
	if testing.Short() {
		t.Skip("Skipping integration test in short mode")
	}

	suite := testutil.SetupIntegrationTest(t)
	defer suite.Cleanup()

	// Services are already running and verified by SetupIntegrationTest

	// Wait for MinIO to be ready
	var err error
	err = suite.Compose.WaitForServices(t, 1*time.Minute)
	require.NoError(t, err)

	// Register S3 resource
	resource, err := suite.RegisterS3Resource("concurrent-writes-minio", "localhost:9000")
	require.NoError(t, err)

	// Test concurrent uploads
	numConcurrent := 10
	done := make(chan error, numConcurrent)

	for i := 0; i < numConcurrent; i++ {
		go func(index int) {
			filename := fmt.Sprintf("concurrent-file-%d.txt", index)
			testData := []byte(fmt.Sprintf("concurrent test data %d", index))

			err := suite.UploadFile(resource.ID, filename, testData)
			done <- err
		}(i)
	}

	// Wait for all uploads to complete
	for i := 0; i < numConcurrent; i++ {
		err := <-done
		require.NoError(t, err, "Concurrent upload %d failed", i)
	}

	// Verify all files were uploaded correctly
	for i := 0; i < numConcurrent; i++ {
		filename := fmt.Sprintf("concurrent-file-%d.txt", i)
		expectedData := []byte(fmt.Sprintf("concurrent test data %d", i))

		downloadedData, err := suite.DownloadFile(resource.ID, filename)
		require.NoError(t, err, "Failed to download concurrent file: %s", filename)
		assert.Equal(t, expectedData, downloadedData, "Data mismatch for concurrent file: %s", filename)
	}
}

func TestStorage_EmptyFile(t *testing.T) {
	if testing.Short() {
		t.Skip("Skipping integration test in short mode")
	}

	suite := testutil.SetupIntegrationTest(t)
	defer suite.Cleanup()

	// Services are already running and verified by SetupIntegrationTest

	// Wait for MinIO to be ready
	var err error
	err = suite.Compose.WaitForServices(t, 1*time.Minute)
	require.NoError(t, err)

	// Register S3 resource
	resource, err := suite.RegisterS3Resource("empty-file-minio", "localhost:9000")
	require.NoError(t, err)

	// Test empty file
	emptyData := []byte{}
	err = suite.UploadFile(resource.ID, "empty-file.txt", emptyData)
	require.NoError(t, err)

	// Download and verify
	downloadedData, err := suite.DownloadFile(resource.ID, "empty-file.txt")
	require.NoError(t, err)
	assert.Equal(t, emptyData, downloadedData)
	assert.Len(t, downloadedData, 0)
}

func TestStorage_SFTP_SpecialCharacters(t *testing.T) {
	if testing.Short() {
		t.Skip("Skipping integration test in short mode")
	}

	suite := testutil.SetupIntegrationTest(t)
	defer suite.Cleanup()

	// Services are already running and verified by SetupIntegrationTest

	// Wait for SFTP to be ready
	var err error
	err = suite.Compose.WaitForServices(t, 1*time.Minute)
	require.NoError(t, err)

	// Register SFTP resource
	resource, err := suite.RegisterSFTPResource("sftp-special-chars", "localhost:2222")
	require.NoError(t, err)

	// Test special characters in SFTP filenames
	specialFilenames := []string{
		"file with spaces.txt",
		"file-with-dashes.txt",
		"file_with_underscores.txt",
		"file.with.dots.txt",
		"file(with)parentheses.txt",
		"file[with]brackets.txt",
		"file{with}braces.txt",
	}

	testData := []byte("test data for SFTP with special characters")

	for _, filename := range specialFilenames {
		// Upload file
		err = suite.UploadFile(resource.ID, filename, testData)
		require.NoError(t, err, "Failed to upload SFTP file with special characters: %s", filename)

		// Download and verify
		downloadedData, err := suite.DownloadFile(resource.ID, filename)
		require.NoError(t, err, "Failed to download SFTP file with special characters: %s", filename)
		assert.Equal(t, testData, downloadedData, "Data mismatch for SFTP file: %s", filename)
	}
}

func TestStorage_SFTP_DeepNesting(t *testing.T) {
	if testing.Short() {
		t.Skip("Skipping integration test in short mode")
	}

	suite := testutil.SetupIntegrationTest(t)
	defer suite.Cleanup()

	// Services are already running and verified by SetupIntegrationTest

	// Wait for SFTP to be ready
	var err error
	err = suite.Compose.WaitForServices(t, 1*time.Minute)
	require.NoError(t, err)

	// Register SFTP resource
	resource, err := suite.RegisterSFTPResource("sftp-deep-nesting", "localhost:2222")
	require.NoError(t, err)

	// Create deeply nested directory structure
	deepPath := "level1/level2/level3/level4/level5/deep-file.txt"
	testData := []byte("test data in deep SFTP directory")

	// Upload file to deep path
	err = suite.UploadFile(resource.ID, deepPath, testData)
	require.NoError(t, err)

	// Download and verify
	downloadedData, err := suite.DownloadFile(resource.ID, deepPath)
	require.NoError(t, err)
	assert.Equal(t, testData, downloadedData)
}

func TestStorage_SFTP_BinaryFiles(t *testing.T) {
	if testing.Short() {
		t.Skip("Skipping integration test in short mode")
	}

	suite := testutil.SetupIntegrationTest(t)
	defer suite.Cleanup()

	// Services are already running and verified by SetupIntegrationTest

	// Wait for SFTP to be ready
	var err error
	err = suite.Compose.WaitForServices(t, 1*time.Minute)
	require.NoError(t, err)

	// Register SFTP resource
	resource, err := suite.RegisterSFTPResource("sftp-binary-files", "localhost:2222")
	require.NoError(t, err)

	// Create binary data
	binaryData := make([]byte, 1024)
	for i := range binaryData {
		binaryData[i] = byte(i % 256)
	}

	// Test binary file upload/download
	err = suite.UploadFile(resource.ID, "binary-data.bin", binaryData)
	require.NoError(t, err)

	// Download and verify
	downloadedData, err := suite.DownloadFile(resource.ID, "binary-data.bin")
	require.NoError(t, err)
	assert.Equal(t, binaryData, downloadedData)
}
