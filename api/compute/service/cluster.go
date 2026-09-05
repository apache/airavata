// Package service holds the compute vertical's business rules: the cluster catalogue,
// the partitions it is carved into, and the login configs users reach a cluster
// through.
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
)

func notFoundAs(err error, format string, args ...any) error {
	if errors.Is(err, gorm.ErrRecordNotFound) {
		return httpx.NotFound(format, args...)
	}
	return err
}

// SlurmClusterService manages Slurm clusters. Reads are open; writes are
// administrative.
//
// A cluster is deployment topology holding no secret — a head node, a data endpoint
// and a set of queues — which is why it is readable by anyone and writable only by an
// admin. Who may log in to it, and as whom, is a SlurmClusterConfig and is governed by
// its own ownership and sharing rules.
type SlurmClusterService struct {
	db         *gorm.DB
	clusters   *repository.SlurmClusterRepository
	partitions *repository.ClusterPartitionRepository
	configs    *repository.SlurmClusterConfigRepository
}

// NewSlurmClusterService returns a cluster service.
//
// It holds the partition repository so a create can register a cluster's partitions in
// the same transaction as the cluster itself. Writes still go through that repository
// rather than through the cluster's owned collection, the same discipline the partition
// service keeps. The config repository is read-only here, and only so that deleting a
// cluster can report what still logs in to it.
func NewSlurmClusterService(
	db *gorm.DB,
	clusters *repository.SlurmClusterRepository,
	partitions *repository.ClusterPartitionRepository,
	configs *repository.SlurmClusterConfigRepository,
) *SlurmClusterService {
	return &SlurmClusterService{db: db, clusters: clusters, partitions: partitions, configs: configs}
}

// List returns every cluster.
func (s *SlurmClusterService) List(ctx context.Context) ([]dto.SlurmClusterResponse, error) {
	clusters, err := s.clusters.FindAll(ctx)
	if err != nil {
		return nil, err
	}
	return dto.ToSlurmClusterResponses(clusters), nil
}

// Get returns one cluster.
func (s *SlurmClusterService) Get(ctx context.Context, id string) (*dto.SlurmClusterResponse, error) {
	cluster, err := s.clusters.FindByID(ctx, id)
	if err != nil {
		return nil, notFoundAs(err, "Slurm cluster not found: %s", id)
	}
	out := dto.ToSlurmClusterResponse(cluster)
	return &out, nil
}

// Create registers a cluster, along with any partitions the request carves it into.
//
// The partitions are written in the same transaction as the cluster, so a cluster
// never becomes visible carrying half the layout it was registered with. A cluster
// that gains partitions later adds them through
// POST /api/v1/slurm-clusters/{slurmClusterId}/partitions instead.
func (s *SlurmClusterService) Create(ctx context.Context, req *dto.SlurmClusterRequest) (*dto.SlurmClusterResponse, error) {
	if _, err := auth.RequireAdmin(ctx); err != nil {
		return nil, err
	}

	var out dto.SlurmClusterResponse
	err := s.db.WithContext(ctx).Transaction(func(tx *gorm.DB) error {
		clusters, partitions := s.clusters.WithTx(tx), s.partitions.WithTx(tx)

		cluster := &model.SlurmCluster{}
		dto.ApplySlurmClusterRequest(cluster, req)
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

		out = dto.ToSlurmClusterResponse(cluster)
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
func (s *SlurmClusterService) Update(ctx context.Context, id string, req *dto.SlurmClusterRequest) (*dto.SlurmClusterResponse, error) {
	if _, err := auth.RequireAdmin(ctx); err != nil {
		return nil, err
	}
	if len(req.Partitions) > 0 {
		return nil, httpx.Invalid([]httpx.FieldError{{
			Field:   "partitions",
			Message: "Partitions are only accepted when a cluster is created; use /api/v1/slurm-clusters/{slurmClusterId}/partitions to change them",
		}})
	}

	var out dto.SlurmClusterResponse
	err := s.db.WithContext(ctx).Transaction(func(tx *gorm.DB) error {
		clusters := s.clusters.WithTx(tx)

		cluster, err := clusters.FindByID(ctx, id)
		if err != nil {
			return notFoundAs(err, "Slurm cluster not found: %s", id)
		}

		dto.ApplySlurmClusterRequest(cluster, req)
		if err := clusters.Save(ctx, cluster); err != nil {
			return err
		}
		out = dto.ToSlurmClusterResponse(cluster)
		return nil
	})
	if err != nil {
		return nil, err
	}
	return &out, nil
}

// Delete removes a cluster nothing logs in to, taking its partitions with it.
//
// The foreign key from a config is RESTRICT, so the database would refuse this anyway;
// checking first turns an opaque constraint violation into a 409 naming how many
// configs still point at it. Their owners have to retire them, since an admin deleting
// the cluster out from under them would silently strand every share on those configs.
func (s *SlurmClusterService) Delete(ctx context.Context, id string) error {
	if _, err := auth.RequireAdmin(ctx); err != nil {
		return err
	}
	cluster, err := s.clusters.FindByID(ctx, id)
	if err != nil {
		return notFoundAs(err, "Slurm cluster not found: %s", id)
	}
	configs, err := s.configs.FindBySlurmClusterID(ctx, cluster.ID)
	if err != nil {
		return err
	}
	if len(configs) > 0 {
		return httpx.Conflict("Slurm cluster %s is still used by %d cluster config(s)", cluster.ID, len(configs))
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
	clusters   *repository.SlurmClusterRepository
}

// NewClusterPartitionService returns a partition service.
func NewClusterPartitionService(db *gorm.DB, partitions *repository.ClusterPartitionRepository, clusters *repository.SlurmClusterRepository) *ClusterPartitionService {
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

func (s *ClusterPartitionService) requireCluster(ctx context.Context, clusterID string) (*model.SlurmCluster, error) {
	cluster, err := s.clusters.FindByID(ctx, clusterID)
	if err != nil {
		return nil, notFoundAs(err, "Slurm cluster not found: %s", clusterID)
	}
	return cluster, nil
}

func (s *ClusterPartitionService) requirePartition(ctx context.Context, clusterID, partitionID string) (*model.ClusterPartition, error) {
	if _, err := s.requireCluster(ctx, clusterID); err != nil {
		return nil, err
	}
	partition, err := s.partitions.FindByIDAndClusterID(ctx, partitionID, clusterID)
	if err != nil {
		return nil, notFoundAs(err, "Partition not found: %s in Slurm cluster %s", partitionID, clusterID)
	}
	return partition, nil
}
