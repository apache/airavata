package compute

import (
	"context"

	"gorm.io/gorm"

	"github.com/apache/airavata/internal/auth"
	"github.com/apache/airavata/internal/httpx"

	dto "github.com/apache/airavata/api/compute/dto"
	model "github.com/apache/airavata/api/compute/model"
)

// SSHEndpointService manages the hosts clusters and credentials are reached through.
//
// It follows the cluster catalog's rules rather than the credential's: an endpoint is
// a piece of deployment topology holding no secret, so reads are open and writes are
// administrative.
type SSHEndpointService struct {
	db          *gorm.DB
	endpoints   *SSHEndpointRepository
	clusters    *ClusterRepository
	credentials *SSHEndpointCredentialRepository
}

// NewSSHEndpointService returns an endpoint service.
func NewSSHEndpointService(
	db *gorm.DB,
	endpoints *SSHEndpointRepository,
	clusters *ClusterRepository,
	credentials *SSHEndpointCredentialRepository,
) *SSHEndpointService {
	return &SSHEndpointService{db: db, endpoints: endpoints, clusters: clusters, credentials: credentials}
}

// List returns every endpoint.
func (s *SSHEndpointService) List(ctx context.Context) ([]dto.SSHEndpointResponse, error) {
	endpoints, err := s.endpoints.FindAll(ctx)
	if err != nil {
		return nil, err
	}
	return dto.ToSSHEndpointResponses(endpoints), nil
}

// Get returns one endpoint.
func (s *SSHEndpointService) Get(ctx context.Context, id string) (*dto.SSHEndpointResponse, error) {
	endpoint, err := s.requireEndpoint(ctx, id)
	if err != nil {
		return nil, err
	}
	out := dto.ToSSHEndpointResponse(endpoint)
	return &out, nil
}

// Create registers an endpoint.
func (s *SSHEndpointService) Create(ctx context.Context, req *dto.SSHEndpointRequest) (*dto.SSHEndpointResponse, error) {
	if _, err := auth.RequireAdmin(ctx); err != nil {
		return nil, err
	}
	endpoint := &model.SSHEndpoint{}
	dto.ApplySSHEndpointRequest(endpoint, req)
	if err := s.endpoints.Save(ctx, endpoint); err != nil {
		return nil, err
	}
	out := dto.ToSSHEndpointResponse(endpoint)
	return &out, nil
}

// Update changes an endpoint.
//
// Repointing an endpoint at a different host silently redirects every cluster and
// credential that names it, which is exactly what an operator moving a login node
// wants — so it is allowed, and left to the admin authority to gate.
func (s *SSHEndpointService) Update(ctx context.Context, id string, req *dto.SSHEndpointRequest) (*dto.SSHEndpointResponse, error) {
	if _, err := auth.RequireAdmin(ctx); err != nil {
		return nil, err
	}
	endpoint, err := s.requireEndpoint(ctx, id)
	if err != nil {
		return nil, err
	}
	dto.ApplySSHEndpointRequest(endpoint, req)
	if err := s.endpoints.Save(ctx, endpoint); err != nil {
		return nil, err
	}
	out := dto.ToSSHEndpointResponse(endpoint)
	return &out, nil
}

// Delete removes an endpoint that nothing references.
//
// The foreign keys from clusters and credentials are RESTRICT, so the database would
// refuse this anyway; checking first turns an opaque constraint violation into a 409
// naming what is still using it.
func (s *SSHEndpointService) Delete(ctx context.Context, id string) error {
	if _, err := auth.RequireAdmin(ctx); err != nil {
		return err
	}
	endpoint, err := s.requireEndpoint(ctx, id)
	if err != nil {
		return err
	}

	clusters, err := s.clusters.FindBySSHEndpointID(ctx, endpoint.ID)
	if err != nil {
		return err
	}
	if len(clusters) > 0 {
		return httpx.Conflict("SSH endpoint %s is still used by %d cluster(s)", endpoint.ID, len(clusters))
	}
	credentials, err := s.credentials.FindBySSHEndpointID(ctx, endpoint.ID)
	if err != nil {
		return err
	}
	if len(credentials) > 0 {
		return httpx.Conflict("SSH endpoint %s is still used by %d credential binding(s)", endpoint.ID, len(credentials))
	}

	return s.endpoints.Delete(ctx, endpoint)
}

func (s *SSHEndpointService) requireEndpoint(ctx context.Context, id string) (*model.SSHEndpoint, error) {
	endpoint, err := s.endpoints.FindByID(ctx, id)
	if err != nil {
		return nil, notFoundAs(err, "SSH endpoint not found: %s", id)
	}
	return endpoint, nil
}
