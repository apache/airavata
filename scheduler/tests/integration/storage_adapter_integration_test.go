package integration

import (
	"bytes"
	"context"
	"io"
	"testing"
	"time"

	"github.com/apache/airavata/scheduler/tests/testutil"
	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
)

func TestS3Storage_CRUD(t *testing.T) {

	suite := testutil.SetupIntegrationTest(t)
	defer suite.Cleanup()

	storage := suite.GetS3Storage()
	require.NotNil(t, storage, "S3 storage adapter should be available")

	ctx := context.Background()
	testData := []byte("test data for S3 storage")
	testPath := "test/file.txt"

	t.Run("UploadFile", func(t *testing.T) {
		err := storage.Put(ctx, testPath, bytes.NewReader(testData), nil)
		assert.NoError(t, err, "Should upload file successfully")
	})

	t.Run("FileExists", func(t *testing.T) {
		exists, err := storage.Exists(ctx, testPath)
		assert.NoError(t, err, "Should check file existence without error")
		assert.True(t, exists, "File should exist after upload")
	})

	t.Run("DownloadFile", func(t *testing.T) {
		reader, err := storage.Get(ctx, testPath)
		assert.NoError(t, err, "Should download file without error")
		require.NotNil(t, reader, "Reader should not be nil")
		defer reader.Close()

		downloadedData, err := io.ReadAll(reader)
		assert.NoError(t, err, "Should read downloaded data without error")
		assert.Equal(t, testData, downloadedData, "Downloaded data should match uploaded data")
	})

	t.Run("DeleteFile", func(t *testing.T) {
		err := storage.Delete(ctx, testPath)
		assert.NoError(t, err, "Should delete file successfully")

		// Verify file no longer exists
		exists, err := storage.Exists(ctx, testPath)
		assert.NoError(t, err, "Should check file existence after deletion")
		assert.False(t, exists, "File should not exist after deletion")
	})
}

func TestS3Storage_SignedURL(t *testing.T) {

	suite := testutil.SetupIntegrationTest(t)
	defer suite.Cleanup()

	storage := suite.GetS3Storage()
	require.NotNil(t, storage, "S3 storage adapter should be available")

	ctx := context.Background()
	testData := []byte("test data for signed URL")
	testPath := "test/signed-url.txt"

	// Upload test file first
	err := storage.Put(ctx, testPath, bytes.NewReader(testData), nil)
	require.NoError(t, err, "Should upload test file for signed URL test")

	t.Run("GenerateSignedURL", func(t *testing.T) {
		url, err := storage.GenerateSignedURL(ctx, testPath, time.Hour, "GET")
		assert.NoError(t, err, "Should generate signed URL without error")
		assert.NotEmpty(t, url, "Signed URL should not be empty")
		assert.Contains(t, url, "http", "Signed URL should be a valid HTTP URL")
	})

	// Clean up
	storage.Delete(ctx, testPath)
}

func TestSFTPStorage_CRUD(t *testing.T) {

	suite := testutil.SetupIntegrationTest(t)
	defer suite.Cleanup()

	storage := suite.GetSFTPStorage()
	require.NotNil(t, storage, "SFTP storage adapter should be available")

	ctx := context.Background()
	testData := []byte("test data for SFTP storage")
	testPath := "/upload/test.txt"

	t.Run("UploadFile", func(t *testing.T) {
		err := storage.Put(ctx, testPath, bytes.NewReader(testData), nil)
		assert.NoError(t, err, "Should upload file to SFTP successfully")
	})

	t.Run("FileExists", func(t *testing.T) {
		exists, err := storage.Exists(ctx, testPath)
		assert.NoError(t, err, "Should check file existence without error")
		assert.True(t, exists, "File should exist after upload")
	})

	t.Run("DownloadFile", func(t *testing.T) {
		reader, err := storage.Get(ctx, testPath)
		assert.NoError(t, err, "Should download file from SFTP without error")
		require.NotNil(t, reader, "Reader should not be nil")
		defer reader.Close()

		downloadedData, err := io.ReadAll(reader)
		assert.NoError(t, err, "Should read downloaded data without error")
		assert.Equal(t, testData, downloadedData, "Downloaded data should match uploaded data")
	})

	t.Run("DeleteFile", func(t *testing.T) {
		err := storage.Delete(ctx, testPath)
		assert.NoError(t, err, "Should delete file from SFTP successfully")

		// Verify file no longer exists
		exists, err := storage.Exists(ctx, testPath)
		assert.NoError(t, err, "Should check file existence after deletion")
		assert.False(t, exists, "File should not exist after deletion")
	})
}

func TestNFSStorage_CRUD(t *testing.T) {

	suite := testutil.SetupIntegrationTest(t)
	defer suite.Cleanup()

	storage := suite.GetNFSStorage()
	require.NotNil(t, storage, "NFS storage adapter should be available")

	ctx := context.Background()
	testData := []byte("test data for NFS storage")
	testPath := "/nfsshare/test.txt"

	t.Run("UploadFile", func(t *testing.T) {
		err := storage.Put(ctx, testPath, bytes.NewReader(testData), nil)
		assert.NoError(t, err, "Should upload file to NFS successfully")
	})

	t.Run("FileExists", func(t *testing.T) {
		exists, err := storage.Exists(ctx, testPath)
		assert.NoError(t, err, "Should check file existence without error")
		assert.True(t, exists, "File should exist after upload")
	})

	t.Run("DownloadFile", func(t *testing.T) {
		reader, err := storage.Get(ctx, testPath)
		assert.NoError(t, err, "Should download file from NFS without error")
		require.NotNil(t, reader, "Reader should not be nil")
		defer reader.Close()

		downloadedData, err := io.ReadAll(reader)
		assert.NoError(t, err, "Should read downloaded data without error")
		assert.Equal(t, testData, downloadedData, "Downloaded data should match uploaded data")
	})

	t.Run("DeleteFile", func(t *testing.T) {
		err := storage.Delete(ctx, testPath)
		assert.NoError(t, err, "Should delete file from NFS successfully")

		// Verify file no longer exists
		exists, err := storage.Exists(ctx, testPath)
		assert.NoError(t, err, "Should check file existence after deletion")
		assert.False(t, exists, "File should not exist after deletion")
	})
}
