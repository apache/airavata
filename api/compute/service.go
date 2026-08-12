package compute

import (
	"context"
	"errors"

	"gorm.io/gorm"

	"github.com/apache/airavata/api/credentials"
	"github.com/apache/airavata/api/iam"
	"github.com/apache/airavata/internal/auth"
	"github.com/apache/airavata/internal/httpx"

	dto "github.com/apache/airavata/api/compute/dto"
	model "github.com/apache/airavata/api/compute/model"
)

func notFoundAs(err error, format string, args ...any) error {
	if errors.Is(err, gorm.ErrRecordNotFound) {
		return httpx.NotFound(format, args...)
	}
	return err
}

// ClusterService manages Slurm clusters. Reads are open; writes are administrative.
type ClusterService struct {
	db       *gorm.DB
	clusters *ClusterRepository
}

// NewClusterService returns a cluster service.
func NewClusterService(db *gorm.DB, clusters *ClusterRepository) *ClusterService {
	return &ClusterService{db: db, clusters: clusters}
}

// List returns every cluster.
func (s *ClusterService) List(ctx context.Context) ([]dto.ClusterResponse, error) {
	clusters, err := s.clusters.FindAll(ctx)
	if err != nil {
		return nil, err
	}
	out := make([]dto.ClusterResponse, 0, len(clusters))
	for i := range clusters {
		out = append(out, dto.ToClusterResponse(&clusters[i]))
	}
	return out, nil
}

// Get returns one cluster.
func (s *ClusterService) Get(ctx context.Context, id string) (*dto.ClusterResponse, error) {
	cluster, err := s.clusters.FindByID(ctx, id)
	if err != nil {
		return nil, notFoundAs(err, "Cluster not found: %s", id)
	}
	out := dto.ToClusterResponse(cluster)
	return &out, nil
}

// Create registers a cluster.
func (s *ClusterService) Create(ctx context.Context, req *dto.ClusterRequest) (*dto.ClusterResponse, error) {
	if _, err := auth.RequireAdmin(ctx); err != nil {
		return nil, err
	}
	cluster := &model.Cluster{}
	dto.ApplyClusterRequest(cluster, req)
	if err := s.clusters.Save(ctx, cluster); err != nil {
		return nil, err
	}
	out := dto.ToClusterResponse(cluster)
	return &out, nil
}

// Update changes a cluster's own fields, leaving its partitions untouched.
func (s *ClusterService) Update(ctx context.Context, id string, req *dto.ClusterRequest) (*dto.ClusterResponse, error) {
	if _, err := auth.RequireAdmin(ctx); err != nil {
		return nil, err
	}
	cluster, err := s.clusters.FindByID(ctx, id)
	if err != nil {
		return nil, notFoundAs(err, "Cluster not found: %s", id)
	}
	dto.ApplyClusterRequest(cluster, req)
	if err := s.clusters.Save(ctx, cluster); err != nil {
		return nil, err
	}
	out := dto.ToClusterResponse(cluster)
	return &out, nil
}

// Delete removes a cluster, taking its partitions with it.
func (s *ClusterService) Delete(ctx context.Context, id string) error {
	if _, err := auth.RequireAdmin(ctx); err != nil {
		return err
	}
	cluster, err := s.clusters.FindByID(ctx, id)
	if err != nil {
		return notFoundAs(err, "Cluster not found: %s", id)
	}
	return s.clusters.Delete(ctx, cluster)
}

// ClusterPartitionService manages partitions as a sub-resource of their cluster.
//
// Every operation is scoped by the cluster id from the path, and writes go through
// the partition repository rather than through the cluster's collection: mutating an
// owned, cascading collection risks deleting rows the caller never named.
type ClusterPartitionService struct {
	db         *gorm.DB
	partitions *ClusterPartitionRepository
	clusters   *ClusterRepository
}

// NewClusterPartitionService returns a partition service.
func NewClusterPartitionService(db *gorm.DB, partitions *ClusterPartitionRepository, clusters *ClusterRepository) *ClusterPartitionService {
	return &ClusterPartitionService{db: db, partitions: partitions, clusters: clusters}
}

// List returns every partition of a cluster.
func (s *ClusterPartitionService) List(ctx context.Context, clusterID string) ([]dto.ClusterPartitionResponse, error) {
	if _, err := s.requireCluster(ctx, clusterID); err != nil {
		return nil, err
	}
	partitions, err := s.partitions.FindByClusterID(ctx, clusterID)
	if err != nil {
		return nil, err
	}
	out := make([]dto.ClusterPartitionResponse, 0, len(partitions))
	for i := range partitions {
		out = append(out, dto.ToClusterPartitionResponse(&partitions[i]))
	}
	return out, nil
}

// Get returns one partition of a cluster.
func (s *ClusterPartitionService) Get(ctx context.Context, clusterID, partitionID string) (*dto.ClusterPartitionResponse, error) {
	partition, err := s.requirePartition(ctx, clusterID, partitionID)
	if err != nil {
		return nil, err
	}
	out := dto.ToClusterPartitionResponse(partition)
	return &out, nil
}

// Create adds a partition to a cluster.
func (s *ClusterPartitionService) Create(ctx context.Context, clusterID string, req *dto.ClusterPartitionRequest) (*dto.ClusterPartitionResponse, error) {
	if _, err := auth.RequireAdmin(ctx); err != nil {
		return nil, err
	}
	cluster, err := s.requireCluster(ctx, clusterID)
	if err != nil {
		return nil, err
	}

	partition := &model.ClusterPartition{ClusterID: &cluster.ID}
	dto.ApplyClusterPartitionRequest(partition, req)
	if err := s.partitions.Save(ctx, partition); err != nil {
		return nil, err
	}
	out := dto.ToClusterPartitionResponse(partition)
	return &out, nil
}

// Update changes a partition of a cluster.
func (s *ClusterPartitionService) Update(ctx context.Context, clusterID, partitionID string, req *dto.ClusterPartitionRequest) (*dto.ClusterPartitionResponse, error) {
	if _, err := auth.RequireAdmin(ctx); err != nil {
		return nil, err
	}
	partition, err := s.requirePartition(ctx, clusterID, partitionID)
	if err != nil {
		return nil, err
	}
	dto.ApplyClusterPartitionRequest(partition, req)
	if err := s.partitions.Save(ctx, partition); err != nil {
		return nil, err
	}
	out := dto.ToClusterPartitionResponse(partition)
	return &out, nil
}

// Delete removes a partition from a cluster.
func (s *ClusterPartitionService) Delete(ctx context.Context, clusterID, partitionID string) error {
	if _, err := auth.RequireAdmin(ctx); err != nil {
		return err
	}
	partition, err := s.requirePartition(ctx, clusterID, partitionID)
	if err != nil {
		return err
	}
	return s.partitions.Delete(ctx, partition)
}

func (s *ClusterPartitionService) requireCluster(ctx context.Context, clusterID string) (*model.Cluster, error) {
	cluster, err := s.clusters.FindByID(ctx, clusterID)
	if err != nil {
		return nil, notFoundAs(err, "Cluster not found: %s", clusterID)
	}
	return cluster, nil
}

func (s *ClusterPartitionService) requirePartition(ctx context.Context, clusterID, partitionID string) (*model.ClusterPartition, error) {
	if _, err := s.requireCluster(ctx, clusterID); err != nil {
		return nil, err
	}
	partition, err := s.partitions.FindByIDAndClusterID(ctx, partitionID, clusterID)
	if err != nil {
		return nil, notFoundAs(err, "Partition not found: %s in cluster %s", partitionID, clusterID)
	}
	return partition, nil
}

// ClusterCredentialService manages the bindings that let a user act on a cluster.
//
// This is the tightest authorisation model in the API. Ownership comes from the
// access token and never from the request body, and it is immutable once set: an
// update re-resolves the cluster and SSH credential but never the owner, so a binding
// cannot be transferred by editing it.
type ClusterCredentialService struct {
	db       *gorm.DB
	bindings *ClusterCredentialRepository
	clusters *ClusterRepository
	sshCreds *credentials.SSHUserCredentialRepository
	users    *iam.UserRepository
}

// NewClusterCredentialService returns a cluster credential service.
func NewClusterCredentialService(
	db *gorm.DB,
	bindings *ClusterCredentialRepository,
	clusters *ClusterRepository,
	sshCreds *credentials.SSHUserCredentialRepository,
	users *iam.UserRepository,
) *ClusterCredentialService {
	return &ClusterCredentialService{db: db, bindings: bindings, clusters: clusters, sshCreds: sshCreds, users: users}
}

// List returns every binding across every user, optionally scoped to one cluster.
// Admin only — this exposes who holds access to what.
func (s *ClusterCredentialService) List(ctx context.Context, clusterID string) ([]dto.ClusterCredentialResponse, error) {
	if _, err := auth.RequireAdmin(ctx); err != nil {
		return nil, err
	}
	var (
		found []model.ClusterCredential
		err   error
	)
	if clusterID == "" {
		found, err = s.bindings.FindAll(ctx)
	} else {
		found, err = s.bindings.FindByClusterID(ctx, clusterID)
	}
	if err != nil {
		return nil, err
	}
	return dto.ToClusterCredentialResponses(found), nil
}

// ListMine returns the caller's own bindings, optionally scoped to one cluster.
func (s *ClusterCredentialService) ListMine(ctx context.Context, clusterID string) ([]dto.ClusterCredentialResponse, error) {
	principal, err := auth.RequireAuthenticated(ctx)
	if err != nil {
		return nil, err
	}
	var found []model.ClusterCredential
	if clusterID == "" {
		found, err = s.bindings.FindByOwnerID(ctx, principal.Name)
	} else {
		found, err = s.bindings.FindByOwnerIDAndClusterID(ctx, principal.Name, clusterID)
	}
	if err != nil {
		return nil, err
	}
	return dto.ToClusterCredentialResponses(found), nil
}

// Get returns one binding, to its owner or to an admin.
func (s *ClusterCredentialService) Get(ctx context.Context, id string) (*dto.ClusterCredentialResponse, error) {
	binding, err := s.requireBinding(ctx, id)
	if err != nil {
		return nil, err
	}
	if err := s.requireSelfOrAdmin(ctx, binding); err != nil {
		return nil, err
	}
	out := dto.ToClusterCredentialResponse(binding)
	return &out, nil
}

// Create binds an SSH credential to a cluster for the calling user.
//
// Any authenticated caller may do this for themselves. The owner is taken from the
// token, so there is no way to create a binding on someone else's behalf.
func (s *ClusterCredentialService) Create(ctx context.Context, req *dto.ClusterCredentialRequest) (*dto.ClusterCredentialResponse, error) {
	principal, err := auth.RequireAuthenticated(ctx)
	if err != nil {
		return nil, err
	}

	var out dto.ClusterCredentialResponse
	err = s.db.WithContext(ctx).Transaction(func(tx *gorm.DB) error {
		bindings, clusters := s.bindings.WithTx(tx), s.clusters.WithTx(tx)
		sshCreds, users := s.sshCreds.WithTx(tx), s.users.WithTx(tx)

		owner, err := users.FindByID(ctx, principal.Name)
		if err != nil {
			return notFoundAs(err, "No user record found for authenticated principal: %s", principal.Name)
		}
		cluster, err := clusters.FindByID(ctx, req.ClusterID)
		if err != nil {
			return notFoundAs(err, "Cluster not found: %s", req.ClusterID)
		}
		sshCred, err := sshCreds.FindByID(ctx, req.SSHCredentialID)
		if err != nil {
			return notFoundAs(err, "SSH credential not found: %s", req.SSHCredentialID)
		}

		binding := &model.ClusterCredential{
			ClusterID:       &cluster.ID,
			SSHCredentialID: &sshCred.ID,
			OwnerID:         &owner.ID,
		}
		if err := bindings.Save(ctx, binding); err != nil {
			return err
		}
		out = dto.ToClusterCredentialResponse(binding)
		return nil
	})
	if err != nil {
		return nil, err
	}
	return &out, nil
}

// Update repoints a binding at a different cluster or SSH credential.
//
// The owner is deliberately left alone: re-deriving it from the caller's token would
// hand the binding to whichever admin happened to issue the request.
func (s *ClusterCredentialService) Update(ctx context.Context, id string, req *dto.ClusterCredentialRequest) (*dto.ClusterCredentialResponse, error) {
	var out dto.ClusterCredentialResponse
	err := s.db.WithContext(ctx).Transaction(func(tx *gorm.DB) error {
		bindings, clusters := s.bindings.WithTx(tx), s.clusters.WithTx(tx)
		sshCreds := s.sshCreds.WithTx(tx)

		binding, err := bindings.FindByID(ctx, id)
		if err != nil {
			return notFoundAs(err, "Cluster credential binding not found: %s", id)
		}
		if err := s.requireSelfOrAdmin(ctx, binding); err != nil {
			return err
		}

		cluster, err := clusters.FindByID(ctx, req.ClusterID)
		if err != nil {
			return notFoundAs(err, "Cluster not found: %s", req.ClusterID)
		}
		sshCred, err := sshCreds.FindByID(ctx, req.SSHCredentialID)
		if err != nil {
			return notFoundAs(err, "SSH credential not found: %s", req.SSHCredentialID)
		}

		binding.ClusterID = &cluster.ID
		binding.SSHCredentialID = &sshCred.ID

		if err := bindings.Save(ctx, binding); err != nil {
			return err
		}
		out = dto.ToClusterCredentialResponse(binding)
		return nil
	})
	if err != nil {
		return nil, err
	}
	return &out, nil
}

// Delete removes a binding, for its owner or an admin.
func (s *ClusterCredentialService) Delete(ctx context.Context, id string) error {
	binding, err := s.requireBinding(ctx, id)
	if err != nil {
		return err
	}
	if err := s.requireSelfOrAdmin(ctx, binding); err != nil {
		return err
	}
	return s.bindings.Delete(ctx, binding)
}

func (s *ClusterCredentialService) requireBinding(ctx context.Context, id string) (*model.ClusterCredential, error) {
	binding, err := s.bindings.FindByID(ctx, id)
	if err != nil {
		return nil, notFoundAs(err, "Cluster credential binding not found: %s", id)
	}
	return binding, nil
}

func (s *ClusterCredentialService) requireSelfOrAdmin(ctx context.Context, binding *model.ClusterCredential) error {
	principal, err := auth.RequireAuthenticated(ctx)
	if err != nil {
		return err
	}
	if binding.OwnedBy(principal.Name) || principal.IsAdmin() {
		return nil
	}
	return httpx.Forbidden("Access denied: you may only access your own cluster credential bindings")
}
