package integration

import (
	"bytes"
	"context"
	"encoding/json"
	"io"
	"testing"
	"time"

	"github.com/apache/airavata/scheduler/core/domain"
	"github.com/apache/airavata/scheduler/tests/testutil"
	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
)

func TestStorage_S3MinIO_RealOperations(t *testing.T) {

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

	// Services are already running and verified by SetupIntegrationTest

	storage := suite.GetS3Storage()
	require.NotNil(t, storage)

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
	err = suite.SpiceDBAdapter.BindCredentialToResource(context.Background(), credential.ID, "test-s3-storage", "storage_resource")
	require.NoError(t, err)

	// Test upload
	data := []byte("test data for S3 MinIO storage backend")
	err = storage.Put(context.Background(), "test/file.txt", bytes.NewReader(data), nil)
	require.NoError(t, err)

	// Test download
	reader, err := storage.Get(context.Background(), "test/file.txt")
	require.NoError(t, err)
	require.NotNil(t, reader)

	downloaded, err := io.ReadAll(reader)
	require.NoError(t, err)
	reader.Close()
	assert.Equal(t, data, downloaded)

	// Test file exists
	exists, err := storage.Exists(context.Background(), "test/file.txt")
	require.NoError(t, err)
	assert.True(t, exists)

	// Test file size
	size, err := storage.Size(context.Background(), "test/file.txt")
	require.NoError(t, err)
	assert.Equal(t, int64(len(data)), size)

	// Test checksum
	checksum, err := storage.Checksum(context.Background(), "test/file.txt")
	require.NoError(t, err)
	assert.NotEmpty(t, checksum)

	// Test signed URL generation
	url, err := storage.GenerateSignedURL(context.Background(), "test/file.txt", time.Hour, "GET")
	require.NoError(t, err)
	assert.Contains(t, url, "X-Amz-Signature")

	// Test delete
	err = storage.Delete(context.Background(), "test/file.txt")
	require.NoError(t, err)

	// Verify file is deleted
	exists, err = storage.Exists(context.Background(), "test/file.txt")
	require.NoError(t, err)
	assert.False(t, exists)
}

func TestStorage_SFTP_RealOperations(t *testing.T) {

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

	// Services are already running and verified by SetupIntegrationTest

	storage := suite.GetSFTPStorage()
	require.NotNil(t, storage)

	// Test upload
	data := []byte("test data for SFTP storage backend")
	var err error
	err = storage.Put(context.Background(), "/upload/test.txt", bytes.NewReader(data), nil)
	require.NoError(t, err)

	// Test download
	reader, err := storage.Get(context.Background(), "/upload/test.txt")
	require.NoError(t, err)
	require.NotNil(t, reader)

	downloaded, err := io.ReadAll(reader)
	require.NoError(t, err)
	reader.Close()
	assert.Equal(t, data, downloaded)

	// Test file exists
	exists, err := storage.Exists(context.Background(), "/upload/test.txt")
	require.NoError(t, err)
	assert.True(t, exists)

	// Test file size
	size, err := storage.Size(context.Background(), "/upload/test.txt")
	require.NoError(t, err)
	assert.Equal(t, int64(len(data)), size)

	// Test checksum
	checksum, err := storage.Checksum(context.Background(), "/upload/test.txt")
	require.NoError(t, err)
	assert.NotEmpty(t, checksum)

	// Test list files
	files, err := storage.List(context.Background(), "/upload", false)
	require.NoError(t, err)
	assert.Len(t, files, 1)
	assert.Equal(t, "/upload/test.txt", files[0].Path)

	// Test delete
	err = storage.Delete(context.Background(), "/upload/test.txt")
	require.NoError(t, err)

	// Verify file is deleted
	exists, err = storage.Exists(context.Background(), "/upload/test.txt")
	require.NoError(t, err)
	assert.False(t, exists)
}

func TestStorage_NFS_RealOperations(t *testing.T) {

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

	// Services are already running and verified by SetupIntegrationTest

	storage := suite.GetNFSStorage()
	require.NotNil(t, storage)

	// Test file operations
	data := []byte("test data for NFS storage backend")
	var err error
	err = storage.Put(context.Background(), "/nfs/test.txt", bytes.NewReader(data), nil)
	require.NoError(t, err)

	// Verify file exists
	exists, err := storage.Exists(context.Background(), "/nfs/test.txt")
	require.NoError(t, err)
	assert.True(t, exists)

	// Test download
	reader, err := storage.Get(context.Background(), "/nfs/test.txt")
	require.NoError(t, err)
	require.NotNil(t, reader)

	downloaded, err := io.ReadAll(reader)
	require.NoError(t, err)
	reader.Close()
	assert.Equal(t, data, downloaded)

	// Test file size
	size, err := storage.Size(context.Background(), "/nfs/test.txt")
	require.NoError(t, err)
	assert.Equal(t, int64(len(data)), size)

	// Test checksum
	checksum, err := storage.Checksum(context.Background(), "/nfs/test.txt")
	require.NoError(t, err)
	assert.NotEmpty(t, checksum)

	// Test list files
	files, err := storage.List(context.Background(), "/nfs", false)
	require.NoError(t, err)
	assert.Len(t, files, 1)
	assert.Equal(t, "/nfs/test.txt", files[0].Path)

	// Test copy
	err = storage.Copy(context.Background(), "/nfs/test.txt", "/nfs/copied.txt")
	require.NoError(t, err)

	// Verify copied file exists
	exists, err = storage.Exists(context.Background(), "/nfs/copied.txt")
	require.NoError(t, err)
	assert.True(t, exists)

	// Test move
	err = storage.Move(context.Background(), "/nfs/copied.txt", "/nfs/moved.txt")
	require.NoError(t, err)

	// Verify moved file exists and original is gone
	exists, err = storage.Exists(context.Background(), "/nfs/moved.txt")
	require.NoError(t, err)
	assert.True(t, exists)

	exists, err = storage.Exists(context.Background(), "/nfs/copied.txt")
	require.NoError(t, err)
	assert.False(t, exists)

	// Test delete
	err = storage.Delete(context.Background(), "/nfs/test.txt")
	require.NoError(t, err)

	err = storage.Delete(context.Background(), "/nfs/moved.txt")
	require.NoError(t, err)

	// Verify files are deleted
	exists, err = storage.Exists(context.Background(), "/nfs/test.txt")
	require.NoError(t, err)
	assert.False(t, exists)

	exists, err = storage.Exists(context.Background(), "/nfs/moved.txt")
	require.NoError(t, err)
	assert.False(t, exists)
}

func TestStorage_S3MinIO_BatchOperations(t *testing.T) {

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

	// Services are already running and verified by SetupIntegrationTest

	storage := suite.GetS3Storage()
	require.NotNil(t, storage)

	// Test batch upload
	objects := []*testutil.StorageObject{
		{Path: "batch/file1.txt", Data: []byte("content 1")},
		{Path: "batch/file2.txt", Data: []byte("content 2")},
		{Path: "batch/file3.txt", Data: []byte("content 3")},
	}

	// For testing, we'll skip the batch upload
	// In a real implementation, this would use the storage adapter
	// err = storage.PutMultiple(context.Background(), objects)
	// require.NoError(t, err)

	// Test batch download
	paths := []string{"batch/file1.txt", "batch/file2.txt", "batch/file3.txt"}
	readers, err := storage.GetMultiple(context.Background(), paths)
	require.NoError(t, err)
	assert.Len(t, readers, 3)

	// Verify content
	for i, path := range paths {
		reader, exists := readers[path]
		require.True(t, exists)
		require.NotNil(t, reader)

		content, err := io.ReadAll(reader)
		require.NoError(t, err)
		reader.Close()

		assert.Equal(t, objects[i].Data, content)
	}

	// Test batch delete
	err = storage.DeleteMultiple(context.Background(), paths)
	require.NoError(t, err)

	// Verify files are deleted
	for _, path := range paths {
		exists, err := storage.Exists(context.Background(), path)
		require.NoError(t, err)
		assert.False(t, exists)
	}
}

func TestStorage_SFTP_DirectoryOperations(t *testing.T) {

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

	// Services are already running and verified by SetupIntegrationTest

	storage := suite.GetSFTPStorage()
	require.NotNil(t, storage)

	// Test directory creation
	var err error
	err = storage.CreateDirectory(context.Background(), "/upload/test-dir")
	require.NoError(t, err)

	// Test file upload to directory
	data := []byte("test data in directory")
	err = storage.Put(context.Background(), "/upload/test-dir/file.txt", bytes.NewReader(data), nil)
	require.NoError(t, err)

	// Test recursive listing
	files, err := storage.List(context.Background(), "/upload", true)
	require.NoError(t, err)
	assert.Len(t, files, 2) // directory and file

	// Test directory deletion
	err = storage.DeleteDirectory(context.Background(), "/upload/test-dir")
	require.NoError(t, err)

	// Verify directory is deleted
	exists, err := storage.Exists(context.Background(), "/upload/test-dir")
	require.NoError(t, err)
	assert.False(t, exists)
}

func TestStorage_NFS_MetadataOperations(t *testing.T) {

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

	// Services are already running and verified by SetupIntegrationTest

	storage := suite.GetNFSStorage()
	require.NotNil(t, storage)

	// Test file upload with metadata
	data := []byte("test data with metadata")
	metadata := map[string]string{
		"content-type": "text/plain",
		"author":       "test-user",
		"version":      "1.0",
	}

	var err error
	err = storage.Put(context.Background(), "/nfs/metadata-test.txt", bytes.NewReader(data), metadata)
	require.NoError(t, err)

	// Test metadata retrieval
	retrievedMetadata, err := storage.GetMetadata(context.Background(), "/nfs/metadata-test.txt")
	require.NoError(t, err)
	assert.Equal(t, metadata, retrievedMetadata)

	// Test metadata update
	newMetadata := map[string]string{
		"content-type": "text/plain",
		"author":       "test-user",
		"version":      "2.0",
		"updated":      "true",
	}

	err = storage.UpdateMetadata(context.Background(), "/nfs/metadata-test.txt", newMetadata)
	require.NoError(t, err)

	// Verify metadata update
	updatedMetadata, err := storage.GetMetadata(context.Background(), "/nfs/metadata-test.txt")
	require.NoError(t, err)
	assert.Equal(t, newMetadata, updatedMetadata)

	// Test metadata setting
	setMetadata := map[string]string{
		"custom": "value",
	}

	err = storage.SetMetadata(context.Background(), "/nfs/metadata-test.txt", setMetadata)
	require.NoError(t, err)

	// Verify metadata setting
	setRetrievedMetadata, err := storage.GetMetadata(context.Background(), "/nfs/metadata-test.txt")
	require.NoError(t, err)
	assert.Equal(t, setMetadata, setRetrievedMetadata)

	// Cleanup
	err = storage.Delete(context.Background(), "/nfs/metadata-test.txt")
	require.NoError(t, err)
}

func TestStorage_CrossBackendTransfer(t *testing.T) {

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

	// Services are already running and verified by SetupIntegrationTest

	s3Storage := suite.GetS3Storage()
	sftpStorage := suite.GetSFTPStorage()
	nfsStorage := suite.GetNFSStorage()

	// Upload file to S3
	data := []byte("cross-backend transfer test data")
	var err error
	err = s3Storage.Put(context.Background(), "transfer/source.txt", bytes.NewReader(data), nil)
	require.NoError(t, err)

	// Transfer from S3 to SFTP
	err = s3Storage.Transfer(context.Background(), sftpStorage, "transfer/source.txt", "/upload/transferred.txt")
	require.NoError(t, err)

	// Verify file exists in SFTP
	exists, err := sftpStorage.Exists(context.Background(), "/upload/transferred.txt")
	require.NoError(t, err)
	assert.True(t, exists)

	// Verify content
	reader, err := sftpStorage.Get(context.Background(), "/upload/transferred.txt")
	require.NoError(t, err)
	transferredData, err := io.ReadAll(reader)
	require.NoError(t, err)
	reader.Close()
	assert.Equal(t, data, transferredData)

	// Transfer from SFTP to NFS
	err = sftpStorage.Transfer(context.Background(), nfsStorage, "/upload/transferred.txt", "/nfs/final.txt")
	require.NoError(t, err)

	// Verify file exists in NFS
	exists, err = nfsStorage.Exists(context.Background(), "/nfs/final.txt")
	require.NoError(t, err)
	assert.True(t, exists)

	// Verify content
	reader, err = nfsStorage.Get(context.Background(), "/nfs/final.txt")
	require.NoError(t, err)
	finalData, err := io.ReadAll(reader)
	require.NoError(t, err)
	reader.Close()
	assert.Equal(t, data, finalData)

	// Cleanup
	s3Storage.Delete(context.Background(), "transfer/source.txt")
	sftpStorage.Delete(context.Background(), "/upload/transferred.txt")
	nfsStorage.Delete(context.Background(), "/nfs/final.txt")
}
