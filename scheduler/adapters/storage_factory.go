package adapters

import (
	"context"
	"errors"
	"strings"

	"github.com/apache/airavata/scheduler/core/domain"
	ports "github.com/apache/airavata/scheduler/core/port"
)

// StorageFactory creates storage adapters
type StorageFactory struct {
	repo  ports.RepositoryPort
	vault domain.CredentialVault
}

// NewStorageFactory creates a new storage factory
func NewStorageFactory(repo ports.RepositoryPort, vault domain.CredentialVault) *StorageFactory {
	return &StorageFactory{
		repo:  repo,
		vault: vault,
	}
}

// CreateDefaultStorage creates a storage port based on configuration
func (f *StorageFactory) CreateDefaultStorage(ctx context.Context, config interface{}) (ports.StoragePort, error) {
	// For now, return S3 adapter as default with global-scratch configuration
	// In production, this would read from config to determine storage type
	return f.CreateS3Storage(ctx, &S3Config{
		ResourceID: "global-scratch",
		Region:     "us-east-1",
		BucketName: "global-scratch",
		Endpoint:   "http://minio:9000", // Use service name for container-to-container communication
	})
}

// CreateS3Storage creates an S3 storage adapter
func (f *StorageFactory) CreateS3Storage(ctx context.Context, config *S3Config) (ports.StoragePort, error) {
	resource := domain.StorageResource{
		ID:       config.ResourceID,
		Name:     config.ResourceID,
		Type:     domain.StorageResourceTypeS3,
		Endpoint: config.Endpoint,
		Status:   domain.ResourceStatusActive,
	}

	return NewS3Adapter(resource, f.vault), nil
}

// S3Config represents S3 storage configuration
type S3Config struct {
	ResourceID string
	Region     string
	BucketName string
	Endpoint   string
}

// NewStorageAdapter creates a storage adapter based on the resource type
func NewStorageAdapter(resource domain.StorageResource, vault domain.CredentialVault) (ports.StoragePort, error) {
	// Validate input parameters
	if resource.ID == "" {
		return nil, errors.New("storage resource ID cannot be empty")
	}
	if resource.Type == "" {
		return nil, errors.New("storage resource type cannot be empty")
	}
	if vault == nil {
		return nil, errors.New("credential vault cannot be nil")
	}

	switch strings.ToLower(string(resource.Type)) {
	case "s3", "aws-s3", "aws_s3":
		return NewS3Adapter(resource, vault), nil
	case "sftp":
		return NewSFTPAdapter(resource, vault), nil
	case "nfs":
		return NewNFSAdapter(resource, vault), nil
	default:
		return nil, errors.New("unsupported storage type: " + string(resource.Type))
	}
}
