package adapters

import (
	"context"
	"fmt"
	"strings"

	"github.com/apache/airavata/scheduler/core/domain"
	ports "github.com/apache/airavata/scheduler/core/port"
)

// ComputeFactory creates compute adapters
type ComputeFactory struct {
	repo   ports.RepositoryPort
	events ports.EventPort
	vault  domain.CredentialVault
}

// NewComputeFactory creates a new compute factory
func NewComputeFactory(repo ports.RepositoryPort, events ports.EventPort, vault domain.CredentialVault) *ComputeFactory {
	return &ComputeFactory{
		repo:   repo,
		events: events,
		vault:  vault,
	}
}

// CreateDefaultCompute creates a compute port based on configuration
func (f *ComputeFactory) CreateDefaultCompute(ctx context.Context, config interface{}) (ports.ComputePort, error) {
	// For now, return SLURM adapter as default
	// In production, this would read from config to determine compute type
	return f.CreateSlurmCompute(ctx, &SlurmConfig{
		Endpoint: "scheduler:6817", // Use service name for container-to-container communication
	})
}

// CreateSlurmCompute creates a SLURM compute adapter
func (f *ComputeFactory) CreateSlurmCompute(ctx context.Context, config *SlurmConfig) (ports.ComputePort, error) {
	resource := domain.ComputeResource{
		ID:          "default-slurm",
		Name:        "default-slurm",
		Type:        domain.ComputeResourceTypeSlurm,
		Endpoint:    config.Endpoint,
		Status:      domain.ResourceStatusActive,
		MaxWorkers:  10,
		CostPerHour: 1.0,
	}

	return NewSlurmAdapter(resource, f.vault), nil
}

// SlurmConfig represents SLURM compute configuration
type SlurmConfig struct {
	Endpoint string
}

// NewComputeAdapter creates a compute adapter based on the resource type
func NewComputeAdapter(resource domain.ComputeResource, vault domain.CredentialVault) (ports.ComputePort, error) {
	switch strings.ToLower(string(resource.Type)) {
	case "slurm":
		return NewSlurmAdapter(resource, vault), nil
	case "baremetal", "bare-metal", "bare_metal":
		return NewBareMetalAdapter(resource, vault), nil
	case "kubernetes", "k8s":
		return NewKubernetesAdapter(resource, vault), nil
	default:
		return nil, fmt.Errorf("unsupported compute type: %s", string(resource.Type))
	}
}
