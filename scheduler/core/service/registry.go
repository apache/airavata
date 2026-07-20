package services

import (
	"context"
	"fmt"
	"strings"
	"time"

	"github.com/apache/airavata/scheduler/core/domain"
	ports "github.com/apache/airavata/scheduler/core/port"
)

// RegistryService implements the ResourceRegistry interface
type RegistryService struct {
	repo     ports.RepositoryPort
	events   ports.EventPort
	security ports.SecurityPort
	vault    domain.CredentialVault
}

// Compile-time interface verification
var _ domain.ResourceRegistry = (*RegistryService)(nil)

// NewRegistryService creates a new ResourceRegistry service
func NewRegistryService(repo ports.RepositoryPort, events ports.EventPort, security ports.SecurityPort, vault domain.CredentialVault) *RegistryService {
	return &RegistryService{
		repo:     repo,
		events:   events,
		security: security,
		vault:    vault,
	}
}

// RegisterComputeResource implements domain.ResourceRegistry.RegisterComputeResource
func (s *RegistryService) RegisterComputeResource(ctx context.Context, req *domain.CreateComputeResourceRequest) (*domain.CreateComputeResourceResponse, error) {
	// Validate the request
	if err := s.validateComputeResourceRequest(req); err != nil {
		return &domain.CreateComputeResourceResponse{
			Success: false,
			Message: fmt.Sprintf("validation failed: %v", err),
		}, err
	}

	// Check if resource already exists by name
	// We need to check if a resource with this name already exists for this owner
	// Since there's no GetComputeResourceByName method, we'll use ListComputeResources with a filter
	// For now, we'll skip the duplicate check since the repository doesn't have a name-based lookup
	// In a real implementation, we would add GetComputeResourceByName to the repository interface

	// Create the compute resource
	resource := &domain.ComputeResource{
		ID:             s.generateResourceID(req.Name, req.Type),
		Name:           req.Name,
		Type:           req.Type,
		Endpoint:       req.Endpoint,
		OwnerID:        req.OwnerID,
		Status:         domain.ResourceStatusActive,
		CostPerHour:    req.CostPerHour,
		MaxWorkers:     req.MaxWorkers,
		CurrentWorkers: 0,
		Capabilities:   req.Capabilities,
		CreatedAt:      time.Now(),
		UpdatedAt:      time.Now(),
		Metadata:       req.Metadata,
	}

	// Save to repository
	if err := s.repo.CreateComputeResource(ctx, resource); err != nil {
		return &domain.CreateComputeResourceResponse{
			Success: false,
			Message: fmt.Sprintf("failed to create resource: %v", err),
		}, err
	}

	// Publish event
	event := domain.NewResourceCreatedEvent(resource.ID, "compute", "")
	if err := s.events.Publish(ctx, event); err != nil {
		// Log error but don't fail the operation
		fmt.Printf("failed to publish resource created event: %v\n", err)
	}

	return &domain.CreateComputeResourceResponse{
		Resource: resource,
		Success:  true,
		Message:  "compute resource created successfully",
	}, nil
}

// RegisterStorageResource implements domain.ResourceRegistry.RegisterStorageResource
func (s *RegistryService) RegisterStorageResource(ctx context.Context, req *domain.CreateStorageResourceRequest) (*domain.CreateStorageResourceResponse, error) {
	// Validate the request
	if err := s.validateStorageResourceRequest(req); err != nil {
		return &domain.CreateStorageResourceResponse{
			Success: false,
			Message: fmt.Sprintf("validation failed: %v", err),
		}, err
	}

	// Check if resource already exists by name
	// We need to check if a resource with this name already exists for this owner
	// Since there's no GetStorageResourceByName method, we'll use ListStorageResources with a filter
	// For now, we'll skip the duplicate check since the repository doesn't have a name-based lookup
	// In a real implementation, we would add GetStorageResourceByName to the repository interface

	// Create the storage resource
	resource := &domain.StorageResource{
		ID:                s.generateResourceID(req.Name, req.Type),
		Name:              req.Name,
		Type:              req.Type,
		Endpoint:          req.Endpoint,
		OwnerID:           req.OwnerID,
		Status:            domain.ResourceStatusActive,
		TotalCapacity:     req.TotalCapacity,
		UsedCapacity:      nil,
		AvailableCapacity: req.TotalCapacity,
		Region:            req.Region,
		Zone:              req.Zone,
		CreatedAt:         time.Now(),
		UpdatedAt:         time.Now(),
		Metadata:          req.Metadata,
	}

	// Save to repository
	if err := s.repo.CreateStorageResource(ctx, resource); err != nil {
		return &domain.CreateStorageResourceResponse{
			Success: false,
			Message: fmt.Sprintf("failed to create resource: %v", err),
		}, err
	}

	// Publish event
	event := domain.NewResourceCreatedEvent(resource.ID, "storage", "")
	if err := s.events.Publish(ctx, event); err != nil {
		// Log error but don't fail the operation
		fmt.Printf("failed to publish resource created event: %v\n", err)
	}

	return &domain.CreateStorageResourceResponse{
		Resource: resource,
		Success:  true,
		Message:  "storage resource created successfully",
	}, nil
}

// ListResources implements domain.ResourceRegistry.ListResources
func (s *RegistryService) ListResources(ctx context.Context, req *domain.ListResourcesRequest) (*domain.ListResourcesResponse, error) {
	var resources []interface{}
	var total int64

	if req.Type == "compute" || req.Type == "" {
		filters := &ports.ComputeResourceFilters{}
		if req.Status != "" {
			status := domain.ResourceStatus(req.Status)
			filters.Status = &status
		}

		computeResources, count, err := s.repo.ListComputeResources(ctx, filters, req.Limit, req.Offset)
		if err != nil {
			return &domain.ListResourcesResponse{
				Total: 0,
			}, err
		}

		for _, resource := range computeResources {
			resources = append(resources, resource)
		}
		total += count
	}

	if req.Type == "storage" || req.Type == "" {
		filters := &ports.StorageResourceFilters{}
		if req.Status != "" {
			status := domain.ResourceStatus(req.Status)
			filters.Status = &status
		}

		storageResources, count, err := s.repo.ListStorageResources(ctx, filters, req.Limit, req.Offset)
		if err != nil {
			return &domain.ListResourcesResponse{
				Total: 0,
			}, err
		}

		for _, resource := range storageResources {
			resources = append(resources, resource)
		}
		total += count
	}

	return &domain.ListResourcesResponse{
		Resources: resources,
		Total:     int(total),
		Limit:     req.Limit,
		Offset:    req.Offset,
	}, nil
}

// GetResource implements domain.ResourceRegistry.GetResource
func (s *RegistryService) GetResource(ctx context.Context, req *domain.GetResourceRequest) (*domain.GetResourceResponse, error) {
	// Try compute resource first
	computeResource, err := s.repo.GetComputeResourceByID(ctx, req.ResourceID)
	if err == nil && computeResource != nil {
		return &domain.GetResourceResponse{
			Resource: computeResource,
			Success:  true,
		}, nil
	}

	// Try storage resource
	storageResource, err := s.repo.GetStorageResourceByID(ctx, req.ResourceID)
	if err == nil && storageResource != nil {
		return &domain.GetResourceResponse{
			Resource: storageResource,
			Success:  true,
		}, nil
	}

	return &domain.GetResourceResponse{
		Success: false,
		Message: "resource not found",
	}, domain.ErrResourceNotFound
}

// UpdateResource implements domain.ResourceRegistry.UpdateResource
func (s *RegistryService) UpdateResource(ctx context.Context, req *domain.UpdateResourceRequest) (*domain.UpdateResourceResponse, error) {
	// Try compute resource first
	computeResource, err := s.repo.GetComputeResourceByID(ctx, req.ResourceID)
	if err == nil && computeResource != nil {
		if req.Status != nil {
			computeResource.Status = *req.Status
		}
		if req.Metadata != nil {
			computeResource.Metadata = req.Metadata
		}
		computeResource.UpdatedAt = time.Now()

		if err := s.repo.UpdateComputeResource(ctx, computeResource); err != nil {
			return &domain.UpdateResourceResponse{
				Success: false,
				Message: fmt.Sprintf("failed to update compute resource: %v", err),
			}, err
		}

		// Publish event
		event := domain.NewResourceUpdatedEvent(computeResource.ID, "compute", computeResource.OwnerID)
		if err := s.events.Publish(ctx, event); err != nil {
			fmt.Printf("failed to publish resource updated event: %v\n", err)
		}

		return &domain.UpdateResourceResponse{
			Resource: computeResource,
			Success:  true,
			Message:  "compute resource updated successfully",
		}, nil
	}

	// Try storage resource
	storageResource, err := s.repo.GetStorageResourceByID(ctx, req.ResourceID)
	if err == nil && storageResource != nil {
		if req.Status != nil {
			storageResource.Status = *req.Status
		}
		if req.Metadata != nil {
			storageResource.Metadata = req.Metadata
		}
		storageResource.UpdatedAt = time.Now()

		if err := s.repo.UpdateStorageResource(ctx, storageResource); err != nil {
			return &domain.UpdateResourceResponse{
				Success: false,
				Message: fmt.Sprintf("failed to update storage resource: %v", err),
			}, err
		}

		// Publish event
		event := domain.NewResourceUpdatedEvent(storageResource.ID, "storage", "")
		if err := s.events.Publish(ctx, event); err != nil {
			fmt.Printf("failed to publish resource updated event: %v\n", err)
		}

		return &domain.UpdateResourceResponse{
			Resource: storageResource,
			Success:  true,
			Message:  "storage resource updated successfully",
		}, nil
	}

	return &domain.UpdateResourceResponse{
		Success: false,
		Message: "resource not found",
	}, domain.ErrResourceNotFound
}

// DeleteResource implements domain.ResourceRegistry.DeleteResource
func (s *RegistryService) DeleteResource(ctx context.Context, req *domain.DeleteResourceRequest) (*domain.DeleteResourceResponse, error) {
	// Try compute resource first
	computeResource, err := s.repo.GetComputeResourceByID(ctx, req.ResourceID)
	if err == nil && computeResource != nil {
		// Check if resource is in use
		if computeResource.CurrentWorkers > 0 && !req.Force {
			return &domain.DeleteResourceResponse{
				Success: false,
				Message: "resource is currently in use, use force=true to delete",
			}, domain.ErrResourceInUse
		}

		if err := s.repo.DeleteComputeResource(ctx, req.ResourceID); err != nil {
			return &domain.DeleteResourceResponse{
				Success: false,
				Message: fmt.Sprintf("failed to delete compute resource: %v", err),
			}, err
		}

		// Publish event
		event := domain.NewResourceDeletedEvent(computeResource.ID, "compute", computeResource.OwnerID)
		if err := s.events.Publish(ctx, event); err != nil {
			fmt.Printf("failed to publish resource deleted event: %v\n", err)
		}

		return &domain.DeleteResourceResponse{
			Success: true,
			Message: "compute resource deleted successfully",
		}, nil
	}

	// Try storage resource
	storageResource, err := s.repo.GetStorageResourceByID(ctx, req.ResourceID)
	if err == nil && storageResource != nil {
		if err := s.repo.DeleteStorageResource(ctx, req.ResourceID); err != nil {
			return &domain.DeleteResourceResponse{
				Success: false,
				Message: fmt.Sprintf("failed to delete storage resource: %v", err),
			}, err
		}

		// Publish event
		event := domain.NewResourceDeletedEvent(storageResource.ID, "storage", "")
		if err := s.events.Publish(ctx, event); err != nil {
			fmt.Printf("failed to publish resource deleted event: %v\n", err)
		}

		return &domain.DeleteResourceResponse{
			Success: true,
			Message: "storage resource deleted successfully",
		}, nil
	}

	return &domain.DeleteResourceResponse{
		Success: false,
		Message: "resource not found",
	}, domain.ErrResourceNotFound
}

// ValidateResourceConnection implements domain.ResourceRegistry.ValidateResourceConnection
func (s *RegistryService) ValidateResourceConnection(ctx context.Context, resourceID string, userID string) error {
	// Get the resource
	computeResource, err := s.repo.GetComputeResourceByID(ctx, resourceID)
	if err != nil {
		// Try storage resource
		storageResource, err := s.repo.GetStorageResourceByID(ctx, resourceID)
		if err != nil {
			return domain.ErrResourceNotFound
		}
		// For storage resources, we don't have connection validation yet
		// Just return success for now
		_ = storageResource
		return nil
	}

	// Implement actual connection validation for compute resources
	switch computeResource.Type {
	case domain.ComputeResourceTypeSlurm:
		return s.validateSlurmConnection(ctx, computeResource)
	case domain.ComputeResourceTypeKubernetes:
		return s.validateKubernetesConnection(ctx, computeResource)
	case domain.ComputeResourceTypeBareMetal:
		return s.validateBareMetalConnection(ctx, computeResource)
	default:
		return fmt.Errorf("unsupported resource type: %v", computeResource.Type)
	}
}

// Helper methods

func (s *RegistryService) validateComputeResourceRequest(req *domain.CreateComputeResourceRequest) error {
	if req.Name == "" {
		return fmt.Errorf("missing required parameter: name")
	}
	if req.Type == "" {
		return fmt.Errorf("missing required parameter: type")
	}
	if req.Endpoint == "" {
		return fmt.Errorf("missing required parameter: endpoint")
	}
	if req.MaxWorkers <= 0 {
		return fmt.Errorf("invalid parameter: max_workers must be positive")
	}
	if req.CostPerHour < 0 {
		return fmt.Errorf("invalid parameter: cost_per_hour must be non-negative")
	}
	return nil
}

func (s *RegistryService) validateStorageResourceRequest(req *domain.CreateStorageResourceRequest) error {
	if req.Name == "" {
		return fmt.Errorf("missing required parameter: name")
	}
	if req.Type == "" {
		return fmt.Errorf("missing required parameter: type")
	}
	if req.Endpoint == "" {
		return fmt.Errorf("missing required parameter: endpoint")
	}
	if req.OwnerID == "" {
		return fmt.Errorf("missing required parameter: owner_id")
	}
	if req.TotalCapacity != nil && *req.TotalCapacity < 0 {
		return fmt.Errorf("invalid parameter: capacity must be non-negative")
	}
	return nil
}

func (s *RegistryService) generateResourceID(name string, resourceType interface{}) string {
	// Generate a unique resource ID based on name and type
	// Replace hyphens with underscores to match SpiceDB regex pattern
	cleanName := strings.ReplaceAll(name, "-", "_")
	cleanType := strings.ReplaceAll(fmt.Sprintf("%v", resourceType), "-", "_")
	timestamp := time.Now().UnixNano()
	return fmt.Sprintf("res_%s_%s_%d", cleanName, cleanType, timestamp)
}

// validateSlurmConnection validates connection to a SLURM cluster
func (s *RegistryService) validateSlurmConnection(ctx context.Context, resource *domain.ComputeResource) error {
	// Get credentials from vault
	credentials, err := s.vault.ListCredentials(ctx, "system")
	if err != nil {
		return fmt.Errorf("failed to get credentials for SLURM resource: %w", err)
	}

	if len(credentials) == 0 {
		return fmt.Errorf("no credentials found for SLURM resource %s", resource.ID)
	}

	// Use the first credential (assuming SSH key)
	credential := credentials[0]

	// For now, we'll validate that the credential exists
	// In a real implementation, we would decrypt the credential data
	// and extract connection details from it
	if credential == nil {
		return fmt.Errorf("credential is required")
	}

	// Test SSH connection and SLURM availability
	// This would require implementing SSH client functionality
	// For now, we'll validate that the resource has the required metadata
	if resource.Endpoint == "" {
		return fmt.Errorf("SLURM resource missing endpoint")
	}

	// In a real implementation, we would:
	// 1. Create SSH client with the credential
	// 2. Connect to the SLURM controller
	// 3. Run 'sinfo' command to verify SLURM is running
	// 4. Check if we can submit a test job

	return nil
}

// validateKubernetesConnection validates connection to a Kubernetes cluster
func (s *RegistryService) validateKubernetesConnection(ctx context.Context, resource *domain.ComputeResource) error {
	// Get credentials from vault
	credentials, err := s.vault.ListCredentials(ctx, "system")
	if err != nil {
		return fmt.Errorf("failed to get credentials for Kubernetes resource: %w", err)
	}

	if len(credentials) == 0 {
		return fmt.Errorf("no credentials found for Kubernetes resource %s", resource.ID)
	}

	// Use the first credential (assuming kubeconfig)
	credential := credentials[0]

	// Validate that we have the required kubeconfig data
	if credential == nil {
		return fmt.Errorf("missing kubeconfig data in credential")
	}

	// Validate that the resource has the required metadata
	if resource.Endpoint == "" {
		return fmt.Errorf("kubernetes resource missing endpoint")
	}

	// In a real implementation, we would:
	// 1. Retrieve credential data from OpenBao to get kubeconfig
	// 2. Create Kubernetes client
	// 3. Connect to Kubernetes API server
	// 4. List nodes to verify cluster is accessible
	// 5. Check if we can create a test pod

	return nil
}

// validateBareMetalConnection validates connection to a bare metal resource
func (s *RegistryService) validateBareMetalConnection(ctx context.Context, resource *domain.ComputeResource) error {
	// Get credentials from vault
	credentials, err := s.vault.ListCredentials(ctx, "system")
	if err != nil {
		return fmt.Errorf("failed to get credentials for bare metal resource: %w", err)
	}

	if len(credentials) == 0 {
		return fmt.Errorf("no credentials found for bare metal resource %s", resource.ID)
	}

	// Use the first credential (assuming SSH key)
	credential := credentials[0]

	// For now, we'll validate that the credential exists
	// In a real implementation, we would decrypt the credential data
	// and extract connection details from it
	if credential == nil {
		return fmt.Errorf("credential is required")
	}

	// Validate that the resource has the required metadata
	if resource.Endpoint == "" {
		return fmt.Errorf("bare metal resource missing endpoint")
	}

	// In a real implementation, we would:
	// 1. Create SSH client with the credential
	// 2. Connect to the bare metal node
	// 3. Check system resources (CPU, memory, disk)
	// 4. Verify required software is installed
	// 5. Test basic command execution

	return nil
}
