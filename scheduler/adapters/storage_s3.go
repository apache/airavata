package adapters

import (
	"context"
	"crypto/sha256"
	"encoding/json"
	"fmt"
	"io"
	"os"
	"path/filepath"
	"strings"
	"time"

	"github.com/apache/airavata/scheduler/core/domain"
	ports "github.com/apache/airavata/scheduler/core/port"
	"github.com/aws/aws-sdk-go-v2/aws"
	"github.com/aws/aws-sdk-go-v2/config"
	"github.com/aws/aws-sdk-go-v2/credentials"
	"github.com/aws/aws-sdk-go-v2/service/s3"
	"github.com/aws/aws-sdk-go-v2/service/s3/types"
)

// S3Adapter implements the StorageAdapter interface for S3-compatible storage
type S3Adapter struct {
	resource       domain.StorageResource
	s3Client       *s3.Client
	vault          domain.CredentialVault
	bucketName     string
	region         string
	connectionTime time.Time
}

// Compile-time interface verification
var _ ports.StoragePort = (*S3Adapter)(nil)

// NewS3Adapter creates a new S3 adapter
func NewS3Adapter(resource domain.StorageResource, vault domain.CredentialVault) *S3Adapter {
	return &S3Adapter{
		resource:       resource,
		vault:          vault,
		connectionTime: time.Now(),
	}
}

// connect establishes S3 client connection
func (s *S3Adapter) connect(userID string) error {
	if s.s3Client != nil {
		return nil // Already connected
	}

	// Retrieve credentials from vault with user context
	ctx := context.Background()
	fmt.Printf("S3 Storage: retrieving credentials for resource %s, user %s, type storage_resource\n", s.resource.ID, userID)
	credential, decryptedData, err := s.vault.GetUsableCredentialForResource(ctx, s.resource.ID, "storage_resource", userID, nil)
	if err != nil {
		fmt.Printf("S3 Storage: failed to retrieve credentials: %v\n", err)
		return fmt.Errorf("failed to retrieve credentials for user %s: %w", userID, err)
	}
	fmt.Printf("S3 Storage: successfully retrieved credentials for resource %s\n", s.resource.ID)

	// Extract credential data
	var accessKeyID, secretAccessKey, sessionToken string

	if credential.Type == domain.CredentialTypeAPIKey {
		// Parse the decrypted credential data (JSON format)
		var credData map[string]string
		if err := json.Unmarshal(decryptedData, &credData); err != nil {
			return fmt.Errorf("failed to unmarshal credential data: %w", err)
		}

		// API key authentication (Access Key ID + Secret Access Key)
		if keyID, ok := credData["access_key_id"]; ok {
			accessKeyID = keyID
		}
		if secretKey, ok := credData["secret_access_key"]; ok {
			secretAccessKey = secretKey
		}
		if session, ok := credData["session_token"]; ok {
			sessionToken = session
		}
	}

	if accessKeyID == "" || secretAccessKey == "" {
		return fmt.Errorf("access key ID and secret access key not found in credentials")
	}

	// Unmarshal resource metadata
	var resourceMetadata map[string]string
	resourceMetadataBytes, err := json.Marshal(s.resource.Metadata)
	if err != nil {
		return fmt.Errorf("failed to marshal resource metadata: %w", err)
	}
	if err := json.Unmarshal(resourceMetadataBytes, &resourceMetadata); err != nil {
		return fmt.Errorf("failed to unmarshal resource metadata: %w", err)
	}

	// Get bucket name and region from resource metadata
	if bucket, ok := resourceMetadata["bucket"]; ok {
		s.bucketName = bucket
	}
	if region, ok := resourceMetadata["region"]; ok {
		s.region = region
	}

	if s.bucketName == "" {
		return fmt.Errorf("bucket name not found in resource metadata")
	}
	if s.region == "" {
		s.region = "us-east-1" // Default region
	}

	// Get endpoint URL for S3-compatible services (like MinIO)
	var endpointURL string
	if endpoint, ok := resourceMetadata["endpoint_url"]; ok {
		endpointURL = endpoint
	}

	// Create AWS config
	cfg, err := config.LoadDefaultConfig(ctx,
		config.WithRegion(s.region),
		config.WithCredentialsProvider(credentials.NewStaticCredentialsProvider(
			accessKeyID,
			secretAccessKey,
			sessionToken,
		)),
	)
	if err != nil {
		return fmt.Errorf("failed to load AWS config: %w", err)
	}

	// Create S3 client
	s3Client := s3.NewFromConfig(cfg, func(o *s3.Options) {
		if endpointURL != "" {
			o.BaseEndpoint = aws.String(endpointURL)
			o.UsePathStyle = true // Required for MinIO and other S3-compatible services
		}
	})

	s.s3Client = s3Client
	return nil
}

// Upload uploads a file to S3 storage
func (s *S3Adapter) Upload(localPath, remotePath string, userID string) error {
	err := s.connect(userID)
	if err != nil {
		return err
	}

	// Open local file
	localFile, err := os.Open(localPath)
	if err != nil {
		return fmt.Errorf("failed to open local file: %w", err)
	}
	defer localFile.Close()

	// Upload to S3
	ctx := context.Background()
	_, err = s.s3Client.PutObject(ctx, &s3.PutObjectInput{
		Bucket: aws.String(s.bucketName),
		Key:    aws.String(remotePath),
		Body:   localFile,
	})
	if err != nil {
		return fmt.Errorf("failed to upload file to S3: %w", err)
	}

	return nil
}

// Download downloads a file from S3 storage
func (s *S3Adapter) Download(remotePath, localPath string, userID string) error {
	err := s.connect(userID)
	if err != nil {
		return err
	}

	// Create local directory if needed
	localDir := filepath.Dir(localPath)
	err = os.MkdirAll(localDir, 0755)
	if err != nil {
		return fmt.Errorf("failed to create local directory: %w", err)
	}

	// Download from S3
	ctx := context.Background()
	result, err := s.s3Client.GetObject(ctx, &s3.GetObjectInput{
		Bucket: aws.String(s.bucketName),
		Key:    aws.String(remotePath),
	})
	if err != nil {
		return fmt.Errorf("failed to download file from S3: %w", err)
	}
	defer result.Body.Close()

	// Create local file
	localFile, err := os.Create(localPath)
	if err != nil {
		return fmt.Errorf("failed to create local file: %w", err)
	}
	defer localFile.Close()

	// Copy data
	_, err = io.Copy(localFile, result.Body)
	if err != nil {
		return fmt.Errorf("failed to copy data to local file: %w", err)
	}

	return nil
}

// List lists files in a directory (S3 prefix)
func (s *S3Adapter) List(ctx context.Context, prefix string, recursive bool) ([]*ports.StorageObject, error) {
	userID := extractUserIDFromContext(ctx)
	err := s.connect(userID)
	if err != nil {
		return nil, err
	}

	// Ensure prefix ends with / for directory listing
	if !strings.HasSuffix(prefix, "/") && prefix != "" {
		prefix += "/"
	}

	result, err := s.s3Client.ListObjectsV2(ctx, &s3.ListObjectsV2Input{
		Bucket: aws.String(s.bucketName),
		Prefix: aws.String(prefix),
	})
	if err != nil {
		return nil, fmt.Errorf("failed to list objects in S3: %w", err)
	}

	// Convert to StorageObject
	var objects []*ports.StorageObject
	for _, obj := range result.Contents {
		// Skip directory markers (objects ending with /)
		if strings.HasSuffix(*obj.Key, "/") {
			continue
		}

		objects = append(objects, &ports.StorageObject{
			Path:         *obj.Key,
			Size:         *obj.Size,
			Checksum:     *obj.ETag,
			ContentType:  "", // Would need to get from metadata
			LastModified: *obj.LastModified,
			Metadata:     make(map[string]string),
		})
	}

	return objects, nil
}

// Move moves a file from one location to another
func (s *S3Adapter) Move(ctx context.Context, srcPath, dstPath string) error {
	userID := extractUserIDFromContext(ctx)
	err := s.connect(userID)
	if err != nil {
		return err
	}

	// Copy the object to the new location
	_, err = s.s3Client.CopyObject(ctx, &s3.CopyObjectInput{
		Bucket:     aws.String(s.bucketName),
		CopySource: aws.String(fmt.Sprintf("%s/%s", s.bucketName, srcPath)),
		Key:        aws.String(dstPath),
	})
	if err != nil {
		return fmt.Errorf("failed to copy object: %w", err)
	}

	// Delete the original object
	_, err = s.s3Client.DeleteObject(ctx, &s3.DeleteObjectInput{
		Bucket: aws.String(s.bucketName),
		Key:    aws.String(srcPath),
	})
	if err != nil {
		return fmt.Errorf("failed to delete original object: %w", err)
	}

	return nil
}

// Delete deletes a file from S3 storage
func (s *S3Adapter) Delete(ctx context.Context, path string) error {
	// Extract userID from context
	userID := extractUserIDFromContext(ctx)

	err := s.connect(userID)
	if err != nil {
		return err
	}

	_, err = s.s3Client.DeleteObject(ctx, &s3.DeleteObjectInput{
		Bucket: aws.String(s.bucketName),
		Key:    aws.String(path),
	})
	if err != nil {
		return fmt.Errorf("failed to delete file from S3: %w", err)
	}

	return nil
}

// DeleteDirectory deletes a directory from S3 storage
func (s *S3Adapter) DeleteDirectory(ctx context.Context, path string) error {
	// Extract userID from context
	userID := extractUserIDFromContext(ctx)

	err := s.connect(userID)
	if err != nil {
		return err
	}

	// List all objects with the prefix
	listInput := &s3.ListObjectsV2Input{
		Bucket: aws.String(s.bucketName),
		Prefix: aws.String(path),
	}

	// Delete all objects with the prefix
	for {
		result, err := s.s3Client.ListObjectsV2(ctx, listInput)
		if err != nil {
			return fmt.Errorf("failed to list objects: %w", err)
		}

		if len(result.Contents) == 0 {
			break
		}

		// Prepare delete request
		var objects []types.ObjectIdentifier
		for _, obj := range result.Contents {
			objects = append(objects, types.ObjectIdentifier{Key: obj.Key})
		}

		deleteInput := &s3.DeleteObjectsInput{
			Bucket: aws.String(s.bucketName),
			Delete: &types.Delete{
				Objects: objects,
			},
		}

		_, err = s.s3Client.DeleteObjects(ctx, deleteInput)
		if err != nil {
			return fmt.Errorf("failed to delete objects: %w", err)
		}

		// Continue if there are more objects
		if result.IsTruncated == nil || !*result.IsTruncated {
			break
		}
		listInput.ContinuationToken = result.NextContinuationToken
	}

	return nil
}

// DeleteMultiple deletes multiple files from S3 storage
func (s *S3Adapter) DeleteMultiple(ctx context.Context, paths []string) error {
	// Extract userID from context
	userID := extractUserIDFromContext(ctx)

	err := s.connect(userID)
	if err != nil {
		return err
	}

	// Prepare delete request
	var objects []types.ObjectIdentifier
	for _, path := range paths {
		objects = append(objects, types.ObjectIdentifier{Key: aws.String(path)})
	}

	deleteInput := &s3.DeleteObjectsInput{
		Bucket: aws.String(s.bucketName),
		Delete: &types.Delete{
			Objects: objects,
		},
	}

	_, err = s.s3Client.DeleteObjects(ctx, deleteInput)
	if err != nil {
		return fmt.Errorf("failed to delete objects: %w", err)
	}

	return nil
}

// Disconnect disconnects from S3 storage
func (s *S3Adapter) Disconnect(ctx context.Context) error {
	// S3 doesn't require persistent connections
	return nil
}

// Exists checks if a file exists in S3 storage
func (s *S3Adapter) Exists(ctx context.Context, path string) (bool, error) {
	// Extract userID from context
	userID := extractUserIDFromContext(ctx)

	err := s.connect(userID)
	if err != nil {
		return false, err
	}

	_, err = s.s3Client.HeadObject(ctx, &s3.HeadObjectInput{
		Bucket: aws.String(s.bucketName),
		Key:    aws.String(path),
	})
	if err != nil {
		return false, nil // File doesn't exist
	}

	return true, nil
}

// Get gets a file from S3 storage
func (s *S3Adapter) Get(ctx context.Context, path string) (io.ReadCloser, error) {
	// Extract userID from context
	userID := extractUserIDFromContext(ctx)

	err := s.connect(userID)
	if err != nil {
		return nil, err
	}

	result, err := s.s3Client.GetObject(ctx, &s3.GetObjectInput{
		Bucket: aws.String(s.bucketName),
		Key:    aws.String(path),
	})
	if err != nil {
		return nil, fmt.Errorf("failed to get object: %w", err)
	}

	return result.Body, nil
}

// GetMetadata gets metadata for a file
func (s *S3Adapter) GetMetadata(ctx context.Context, path string) (map[string]string, error) {
	userID := extractUserIDFromContext(ctx)
	err := s.connect(userID)
	if err != nil {
		return nil, err
	}

	result, err := s.s3Client.HeadObject(ctx, &s3.HeadObjectInput{
		Bucket: aws.String(s.bucketName),
		Key:    aws.String(path),
	})
	if err != nil {
		return nil, fmt.Errorf("failed to get object metadata from S3: %w", err)
	}

	metadata := make(map[string]string)
	metadata["size"] = fmt.Sprintf("%d", *result.ContentLength)
	metadata["lastModified"] = result.LastModified.Format(time.RFC3339)
	metadata["etag"] = *result.ETag
	metadata["contentType"] = *result.ContentType

	return metadata, nil
}

// GetMultiple retrieves multiple files
func (s *S3Adapter) GetMultiple(ctx context.Context, paths []string) (map[string]io.ReadCloser, error) {
	userID := extractUserIDFromContext(ctx)
	err := s.connect(userID)
	if err != nil {
		return nil, err
	}

	result := make(map[string]io.ReadCloser)
	for _, path := range paths {
		obj, err := s.s3Client.GetObject(ctx, &s3.GetObjectInput{
			Bucket: aws.String(s.bucketName),
			Key:    aws.String(path),
		})
		if err != nil {
			// Close any already opened objects
			for _, reader := range result {
				reader.Close()
			}
			return nil, fmt.Errorf("failed to get object %s: %w", path, err)
		}
		result[path] = obj.Body
	}

	return result, nil
}

// GetStats returns storage statistics
func (s *S3Adapter) GetStats(ctx context.Context) (*ports.StorageStats, error) {
	userID := extractUserIDFromContext(ctx)
	err := s.connect(userID)
	if err != nil {
		return nil, err
	}

	// For S3, we can't easily get all these stats without expensive operations
	// Return basic stats or implement based on your needs
	stats := &ports.StorageStats{
		TotalObjects:   0,                            // Would need to list all objects
		TotalSize:      0,                            // Would need to sum all object sizes
		AvailableSpace: 0,                            // S3 doesn't have a concept of available space
		Uptime:         time.Since(s.connectionTime), // Real uptime since connection
		LastActivity:   time.Now(),
		ErrorRate:      0.0,
		Throughput:     0.0,
	}

	return stats, nil
}

// IsConnected checks if the adapter is connected
func (s *S3Adapter) IsConnected() bool {
	// For S3, we can check if the client is initialized
	return s.s3Client != nil
}

// GetType returns the storage resource type
func (s *S3Adapter) GetType() string {
	return "s3"
}

// Connect establishes connection to the storage resource
func (s *S3Adapter) Connect(ctx context.Context) error {
	// Extract userID from context or use empty string
	userID := ""
	if userIDValue := ctx.Value("userID"); userIDValue != nil {
		if id, ok := userIDValue.(string); ok {
			userID = id
		}
	}
	return s.connect(userID)
}

// Copy copies a file from srcPath to dstPath
func (s *S3Adapter) Copy(ctx context.Context, srcPath, dstPath string) error {
	// Download from srcPath and upload to dstPath
	// This is a simplified implementation
	return fmt.Errorf("Copy method not implemented for S3 adapter")
}

// CreateDirectory creates a directory in the storage resource
func (s *S3Adapter) CreateDirectory(ctx context.Context, path string) error {
	// Extract userID from context or use empty string
	userID := ""
	if userIDValue := ctx.Value("userID"); userIDValue != nil {
		if id, ok := userIDValue.(string); ok {
			userID = id
		}
	}

	err := s.connect(userID)
	if err != nil {
		return err
	}

	// Ensure path ends with /
	if !strings.HasSuffix(path, "/") {
		path += "/"
	}

	// Create a placeholder object to represent the directory
	_, err = s.s3Client.PutObject(ctx, &s3.PutObjectInput{
		Bucket: aws.String(s.bucketName),
		Key:    aws.String(path + ".keep"),
		Body:   strings.NewReader(""),
	})

	return err
}

// Close closes the S3 adapter (no persistent connections to close)
func (s *S3Adapter) Close() error {
	return nil
}

// Checksum computes SHA-256 checksum of remote file (interface method)
func (s *S3Adapter) Checksum(ctx context.Context, path string) (string, error) {
	// Extract userID from context or use empty string
	userID := ""
	if userIDValue := ctx.Value("userID"); userIDValue != nil {
		if id, ok := userIDValue.(string); ok {
			userID = id
		}
	}
	return s.CalculateChecksum(path, userID)
}

// UploadWithProgress uploads a file with progress tracking
func (s *S3Adapter) UploadWithProgress(localPath, remotePath string, progressCallback func(int64, int64), userID string) error {
	err := s.connect(userID)
	if err != nil {
		return err
	}

	// Get file info for progress tracking
	localFileInfo, err := os.Stat(localPath)
	if err != nil {
		return fmt.Errorf("failed to get file info: %w", err)
	}
	totalSize := localFileInfo.Size()

	// Open local file
	localFile, err := os.Open(localPath)
	if err != nil {
		return fmt.Errorf("failed to open local file: %w", err)
	}
	defer localFile.Close()

	// Create a progress reader
	progressReader := &ProgressReader{
		Reader:           localFile,
		TotalSize:        totalSize,
		ProgressCallback: progressCallback,
	}

	// Upload to S3
	ctx := context.Background()
	_, err = s.s3Client.PutObject(ctx, &s3.PutObjectInput{
		Bucket: aws.String(s.bucketName),
		Key:    aws.String(remotePath),
		Body:   progressReader,
	})
	if err != nil {
		return fmt.Errorf("failed to upload file to S3: %w", err)
	}

	return nil
}

// DownloadWithProgress downloads a file with progress tracking
func (s *S3Adapter) DownloadWithProgress(remotePath, localPath string, progressCallback func(int64, int64), userID string) error {
	err := s.connect(userID)
	if err != nil {
		return err
	}

	// Create local directory if needed
	localDir := filepath.Dir(localPath)
	err = os.MkdirAll(localDir, 0755)
	if err != nil {
		return fmt.Errorf("failed to create local directory: %w", err)
	}

	// Download from S3
	ctx := context.Background()
	result, err := s.s3Client.GetObject(ctx, &s3.GetObjectInput{
		Bucket: aws.String(s.bucketName),
		Key:    aws.String(remotePath),
	})
	if err != nil {
		return fmt.Errorf("failed to download file from S3: %w", err)
	}
	defer result.Body.Close()

	// Get total size for progress tracking
	totalSize := int64(0)
	if result.ContentLength != nil {
		totalSize = *result.ContentLength
	}

	// Create local file
	localFile, err := os.Create(localPath)
	if err != nil {
		return fmt.Errorf("failed to create local file: %w", err)
	}
	defer localFile.Close()

	// Create a progress writer
	progressWriter := &ProgressWriter{
		Writer:           localFile,
		TotalSize:        totalSize,
		ProgressCallback: progressCallback,
	}

	// Copy data with progress tracking
	_, err = io.Copy(progressWriter, result.Body)
	if err != nil {
		return fmt.Errorf("failed to copy data to local file: %w", err)
	}

	return nil
}

// ProgressReader wraps an io.Reader to track progress
type ProgressReader struct {
	Reader           io.Reader
	TotalSize        int64
	BytesRead        int64
	ProgressCallback func(int64, int64)
}

func (pr *ProgressReader) Read(p []byte) (n int, err error) {
	n, err = pr.Reader.Read(p)
	pr.BytesRead += int64(n)
	if pr.ProgressCallback != nil {
		pr.ProgressCallback(pr.BytesRead, pr.TotalSize)
	}
	return n, err
}

// ProgressWriter wraps an io.Writer to track progress
type ProgressWriter struct {
	Writer           io.Writer
	TotalSize        int64
	BytesWritten     int64
	ProgressCallback func(int64, int64)
}

func (pw *ProgressWriter) Write(p []byte) (n int, err error) {
	n, err = pw.Writer.Write(p)
	pw.BytesWritten += int64(n)
	if pw.ProgressCallback != nil {
		pw.ProgressCallback(pw.BytesWritten, pw.TotalSize)
	}
	return n, err
}

// CalculateChecksum computes SHA-256 checksum of remote file
func (s *S3Adapter) CalculateChecksum(remotePath string, userID string) (string, error) {
	err := s.connect(userID)
	if err != nil {
		return "", err
	}

	ctx := context.Background()
	result, err := s.s3Client.GetObject(ctx, &s3.GetObjectInput{
		Bucket: aws.String(s.bucketName),
		Key:    aws.String(remotePath),
	})
	if err != nil {
		return "", fmt.Errorf("failed to get object from S3: %w", err)
	}
	defer result.Body.Close()

	// Calculate SHA-256 while streaming
	hash := sha256.New()
	if _, err := io.Copy(hash, result.Body); err != nil {
		return "", fmt.Errorf("failed to calculate checksum: %w", err)
	}

	return fmt.Sprintf("%x", hash.Sum(nil)), nil
}

// VerifyChecksum verifies file integrity against expected checksum
func (s *S3Adapter) VerifyChecksum(remotePath string, expectedChecksum string, userID string) (bool, error) {
	actualChecksum, err := s.CalculateChecksum(remotePath, userID)
	if err != nil {
		return false, err
	}

	return actualChecksum == expectedChecksum, nil
}

// calculateLocalChecksum calculates SHA-256 checksum of a local file
func calculateLocalChecksum(filePath string) (string, error) {
	file, err := os.Open(filePath)
	if err != nil {
		return "", fmt.Errorf("failed to open file: %w", err)
	}
	defer file.Close()

	hash := sha256.New()
	if _, err := io.Copy(hash, file); err != nil {
		return "", fmt.Errorf("failed to calculate local checksum: %w", err)
	}

	return fmt.Sprintf("%x", hash.Sum(nil)), nil
}

// UploadWithVerification uploads file and verifies checksum
func (s *S3Adapter) UploadWithVerification(localPath, remotePath string, userID string) (string, error) {
	// Calculate local file checksum first
	localChecksum, err := calculateLocalChecksum(localPath)
	if err != nil {
		return "", fmt.Errorf("failed to calculate local checksum: %w", err)
	}

	// Upload file
	if err := s.Upload(localPath, remotePath, userID); err != nil {
		return "", fmt.Errorf("upload failed: %w", err)
	}

	// Verify uploaded file checksum
	remoteChecksum, err := s.CalculateChecksum(remotePath, userID)
	if err != nil {
		return "", fmt.Errorf("failed to calculate remote checksum: %w", err)
	}

	if localChecksum != remoteChecksum {
		return "", fmt.Errorf("checksum mismatch after upload: local=%s remote=%s", localChecksum, remoteChecksum)
	}

	return remoteChecksum, nil
}

// DownloadWithVerification downloads file and verifies checksum
func (s *S3Adapter) DownloadWithVerification(remotePath, localPath string, expectedChecksum string, userID string) error {
	// Download file
	if err := s.Download(remotePath, localPath, userID); err != nil {
		return fmt.Errorf("download failed: %w", err)
	}

	// Calculate downloaded file checksum
	actualChecksum, err := calculateLocalChecksum(localPath)
	if err != nil {
		return fmt.Errorf("failed to calculate downloaded file checksum: %w", err)
	}

	if actualChecksum != expectedChecksum {
		return fmt.Errorf("checksum mismatch after download: expected=%s actual=%s", expectedChecksum, actualChecksum)
	}

	return nil
}

// GetConfig returns the storage configuration
func (s *S3Adapter) GetConfig() *ports.StorageConfig {
	return &ports.StorageConfig{
		Type:       "s3",
		Endpoint:   s.resource.Endpoint,
		Region:     s.region,
		Bucket:     s.bucketName,
		PathPrefix: "",
	}
}

// GetFileMetadata retrieves metadata for a file
func (s *S3Adapter) GetFileMetadata(remotePath string, userID string) (*domain.FileMetadata, error) {
	err := s.connect(userID)
	if err != nil {
		return nil, err
	}

	// Get object metadata from S3
	result, err := s.s3Client.HeadObject(context.Background(), &s3.HeadObjectInput{
		Bucket: aws.String(s.bucketName),
		Key:    aws.String(remotePath),
	})
	if err != nil {
		return nil, fmt.Errorf("failed to get file metadata: %w", err)
	}

	metadata := &domain.FileMetadata{
		Path:     remotePath,
		Size:     *result.ContentLength,
		Checksum: *result.ETag,
		Type:     *result.ContentType,
	}

	return metadata, nil
}

// Ping checks if the storage is accessible
func (s *S3Adapter) Ping(ctx context.Context) error {
	_, err := s.s3Client.HeadBucket(ctx, &s3.HeadBucketInput{
		Bucket: aws.String(s.bucketName),
	})
	return err
}

// Put uploads data to the specified path
func (s *S3Adapter) Put(ctx context.Context, path string, data io.Reader, metadata map[string]string) error {
	// Extract userID from context
	userID := extractUserIDFromContext(ctx)

	err := s.connect(userID)
	if err != nil {
		return err
	}

	_, err = s.s3Client.PutObject(ctx, &s3.PutObjectInput{
		Bucket:   aws.String(s.bucketName),
		Key:      aws.String(path),
		Body:     data,
		Metadata: metadata,
	})
	return err
}

// PutMultiple uploads multiple objects
func (s *S3Adapter) PutMultiple(ctx context.Context, objects []*ports.StorageObject) error {
	for _, obj := range objects {
		if err := s.Put(ctx, obj.Path, obj.Data, obj.Metadata); err != nil {
			return err
		}
	}
	return nil
}

// SetMetadata sets metadata for a file
func (s *S3Adapter) SetMetadata(ctx context.Context, path string, metadata map[string]string) error {
	_, err := s.s3Client.CopyObject(ctx, &s3.CopyObjectInput{
		Bucket:     aws.String(s.bucketName),
		Key:        aws.String(path),
		CopySource: aws.String(fmt.Sprintf("%s/%s", s.bucketName, path)),
		Metadata:   metadata,
	})
	return err
}

// Size returns the size of a file
func (s *S3Adapter) Size(ctx context.Context, path string) (int64, error) {
	result, err := s.s3Client.HeadObject(ctx, &s3.HeadObjectInput{
		Bucket: aws.String(s.bucketName),
		Key:    aws.String(path),
	})
	if err != nil {
		return 0, err
	}
	return *result.ContentLength, nil
}

// Transfer transfers a file from source storage to destination
func (s *S3Adapter) Transfer(ctx context.Context, srcStorage ports.StoragePort, srcPath, dstPath string) error {
	// Get data from source storage
	data, err := srcStorage.Get(ctx, srcPath)
	if err != nil {
		return err
	}
	defer data.Close()

	// Put data to destination
	return s.Put(ctx, dstPath, data, nil)
}

// TransferWithProgress transfers a file with progress callback
func (s *S3Adapter) TransferWithProgress(ctx context.Context, srcStorage ports.StoragePort, srcPath, dstPath string, progress ports.ProgressCallback) error {
	// For now, just call Transfer without progress tracking
	return s.Transfer(ctx, srcStorage, srcPath, dstPath)
}

// UpdateMetadata updates metadata for a file
func (s *S3Adapter) UpdateMetadata(ctx context.Context, path string, metadata map[string]string) error {
	// For S3, we need to copy the object with new metadata
	_, err := s.s3Client.CopyObject(ctx, &s3.CopyObjectInput{
		Bucket:     aws.String(s.bucketName),
		Key:        aws.String(path),
		CopySource: aws.String(fmt.Sprintf("%s/%s", s.bucketName, path)),
		Metadata:   metadata,
	})
	return err
}

// GenerateSignedURL generates a presigned URL for S3 operations
func (s *S3Adapter) GenerateSignedURL(ctx context.Context, path string, expiresIn time.Duration, method string) (string, error) {
	userID := extractUserIDFromContext(ctx)
	err := s.connect(userID)
	if err != nil {
		return "", err
	}

	presignClient := s3.NewPresignClient(s.s3Client)

	if method == "PUT" {
		req, err := presignClient.PresignPutObject(ctx, &s3.PutObjectInput{
			Bucket: aws.String(s.bucketName),
			Key:    aws.String(path),
		}, func(opts *s3.PresignOptions) {
			opts.Expires = expiresIn
		})
		if err != nil {
			return "", fmt.Errorf("failed to presign PUT request: %w", err)
		}
		return req.URL, nil
	}

	req, err := presignClient.PresignGetObject(ctx, &s3.GetObjectInput{
		Bucket: aws.String(s.bucketName),
		Key:    aws.String(path),
	}, func(opts *s3.PresignOptions) {
		opts.Expires = expiresIn
	})
	if err != nil {
		return "", fmt.Errorf("failed to presign GET request: %w", err)
	}
	return req.URL, nil
}
