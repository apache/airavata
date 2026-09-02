// Package service holds the compute vertical's business rules: the cluster catalogue
// and the partitions it is carved into.
package service

import (
	"context"
	"errors"

	"gorm.io/gorm"

	"github.com/apache/airavata/internal/auth"
	"github.com/apache/airavata/internal/httpx"

	dto "github.com/apache/airavata/api/compute/dto"
	model "github.com/apache/airavata/api/compute/model"
	"github.com/apache/airavata/api/compute/repository"
	credrepo "github.com/apache/airavata/api/credentials/repository"
)

func notFoundAs(err error, format string, args ...any) error {
	if errors.Is(err, gorm.ErrRecordNotFound) {
		return httpx.NotFound(format, args...)
	}
	return err
}

// ClusterService manages Slurm clusters. Reads are open; writes are administrative.
type ClusterService struct {
	db         *gorm.DB
	clusters   *repository.ClusterRepository
	partitions *repository.ClusterPartitionRepository
	endpoints  *credrepo.SSHEndpointRepository
}

// NewClusterService returns a cluster service.
//
// It holds the partition repository so a create can register a cluster's partitions in
// the same transaction as the cluster itself. Writes still go through that repository
// rather than through the cluster's owned collection, the same discipline the partition
// service keeps.
func NewClusterService(db *gorm.DB, clusters *repository.ClusterRepository, partitions *repository.ClusterPartitionRepository, endpoints *credrepo.SSHEndpointRepository) *ClusterService {
	return &ClusterService{db: db, clusters: clusters, partitions: partitions, endpoints: endpoints}
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

// Create registers a cluster, along with any partitions the request carves it into.
//
// The named SSH endpoint must already exist: a cluster pointing at a host nothing
// knows about could never be submitted to, so an unknown id is a 404 rather than a
// dangling reference.
//
// The partitions are written in the same transaction as the cluster, so a cluster
// never becomes visible carrying half the layout it was registered with. A cluster
// that gains partitions later adds them through
// POST /api/v1/clusters/{clusterId}/partitions instead.
func (s *ClusterService) Create(ctx context.Context, req *dto.ClusterRequest) (*dto.ClusterResponse, error) {
	if _, err := auth.RequireAdmin(ctx); err != nil {
		return nil, err
	}

	var out dto.ClusterResponse
	err := s.db.WithContext(ctx).Transaction(func(tx *gorm.DB) error {
		clusters, endpoints := s.clusters.WithTx(tx), s.endpoints.WithTx(tx)
		partitions := s.partitions.WithTx(tx)

		endpoint, err := endpoints.FindByID(ctx, req.SSHEndpointID)
		if err != nil {
			return notFoundAs(err, "SSH endpoint not found: %s", req.SSHEndpointID)
		}

		cluster := &model.Cluster{SSHEndpointID: &endpoint.ID, SSHEndpoint: endpoint}
		dto.ApplyClusterRequest(cluster, req)
		if err := clusters.Save(ctx, cluster); err != nil {
			return err
		}

		// Saved one at a time through the partition repository rather than through
		// cluster.Partitions: the collection cascades on delete, and writing it as an
		// association is what the model warns against. Each saved row is appended to
		// the read-side projection so the response carries the ids just generated.
		for i := range req.Partitions {
			partition := &model.ClusterPartition{ClusterID: &cluster.ID}
			dto.ApplyClusterPartitionRequest(partition, &req.Partitions[i])
			if err := partitions.Save(ctx, partition); err != nil {
				return err
			}
			cluster.Partitions = append(cluster.Partitions, *partition)
		}

		out = dto.ToClusterResponse(cluster)
		return nil
	})
	if err != nil {
		return nil, err
	}
	return &out, nil
}

// Update changes a cluster's own fields, leaving its partitions untouched.
//
// A body carrying partitions is rejected rather than ignored. They have no ids, so
// there is nothing to match an incoming partition to an existing row by: obeying the
// field would mean replacing the collection wholesale and deleting partitions the
// caller never mentioned, and ignoring it would silently drop what the caller asked
// for. The partition endpoints are where a cluster's layout changes.
func (s *ClusterService) Update(ctx context.Context, id string, req *dto.ClusterRequest) (*dto.ClusterResponse, error) {
	if _, err := auth.RequireAdmin(ctx); err != nil {
		return nil, err
	}
	if len(req.Partitions) > 0 {
		return nil, httpx.Invalid([]httpx.FieldError{{
			Field:   "partitions",
			Message: "Partitions are only accepted when a cluster is created; use /api/v1/clusters/{clusterId}/partitions to change them",
		}})
	}

	var out dto.ClusterResponse
	err := s.db.WithContext(ctx).Transaction(func(tx *gorm.DB) error {
		clusters, endpoints := s.clusters.WithTx(tx), s.endpoints.WithTx(tx)

		cluster, err := clusters.FindByID(ctx, id)
		if err != nil {
			return notFoundAs(err, "Cluster not found: %s", id)
		}
		endpoint, err := endpoints.FindByID(ctx, req.SSHEndpointID)
		if err != nil {
			return notFoundAs(err, "SSH endpoint not found: %s", req.SSHEndpointID)
		}

		dto.ApplyClusterRequest(cluster, req)
		cluster.SSHEndpointID = &endpoint.ID
		cluster.SSHEndpoint = endpoint
		if err := clusters.Save(ctx, cluster); err != nil {
			return err
		}
		out = dto.ToClusterResponse(cluster)
		return nil
	})
	if err != nil {
		return nil, err
	}
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
	partitions *repository.ClusterPartitionRepository
	clusters   *repository.ClusterRepository
}

// NewClusterPartitionService returns a partition service.
func NewClusterPartitionService(db *gorm.DB, partitions *repository.ClusterPartitionRepository, clusters *repository.ClusterRepository) *ClusterPartitionService {
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
