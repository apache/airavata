// Package repository reads and writes clusters and their partitions.
package repository

import (
	"context"

	"gorm.io/gorm"

	model "github.com/apache/airavata/api/compute/model"
)

// ClusterRepository reads and writes clusters.
//
// Reads preload Partitions and the SSH endpoint because the response DTO always
// carries them; the Java service achieved the same thing by running reads inside a
// transaction so the lazy associations could still be walked during mapping.
type ClusterRepository struct{ db *gorm.DB }

// NewClusterRepository returns a repository backed by db.
func NewClusterRepository(db *gorm.DB) *ClusterRepository { return &ClusterRepository{db: db} }

// WithTx returns a repository bound to tx.
func (r *ClusterRepository) WithTx(tx *gorm.DB) *ClusterRepository { return &ClusterRepository{db: tx} }

// FindAll returns every cluster with its partitions and endpoint.
func (r *ClusterRepository) FindAll(ctx context.Context) ([]model.Cluster, error) {
	var out []model.Cluster
	err := r.db.WithContext(ctx).Preload("Partitions").Preload("SSHEndpoint").Find(&out).Error
	return out, err
}

// FindByID returns one cluster with its partitions and endpoint, or
// gorm.ErrRecordNotFound.
func (r *ClusterRepository) FindByID(ctx context.Context, id string) (*model.Cluster, error) {
	var out model.Cluster
	err := r.db.WithContext(ctx).Preload("Partitions").Preload("SSHEndpoint").
		First(&out, "cluster_id = ?", id).Error
	if err != nil {
		return nil, err
	}
	return &out, nil
}

// FindBySSHEndpointID returns every cluster reachable through one endpoint. It is what
// makes deleting an endpoint still in use reportable as a conflict rather than a
// foreign key error.
func (r *ClusterRepository) FindBySSHEndpointID(ctx context.Context, endpointID string) ([]model.Cluster, error) {
	var out []model.Cluster
	err := r.db.WithContext(ctx).Where("ssh_endpoint_id = ?", endpointID).Find(&out).Error
	return out, err
}

// Save inserts or updates a cluster.
func (r *ClusterRepository) Save(ctx context.Context, c *model.Cluster) error {
	return r.db.WithContext(ctx).Save(c).Error
}

// Delete removes a cluster; its partitions go with it via the cascading constraint.
func (r *ClusterRepository) Delete(ctx context.Context, c *model.Cluster) error {
	return r.db.WithContext(ctx).Delete(c).Error
}

// ExistsByName reports whether a cluster of that name is already registered.
//
// Carried over from the Java repository, which declared it but never called it — no
// service rejects a duplicate name, and no unique constraint backs it either.
func (r *ClusterRepository) ExistsByName(ctx context.Context, name string) (bool, error) {
	var n int64
	err := r.db.WithContext(ctx).Model(&model.Cluster{}).Where("cluster_name = ?", name).Count(&n).Error
	return n > 0, err
}

// ClusterPartitionRepository reads and writes partitions.
type ClusterPartitionRepository struct{ db *gorm.DB }

// NewClusterPartitionRepository returns a repository backed by db.
func NewClusterPartitionRepository(db *gorm.DB) *ClusterPartitionRepository {
	return &ClusterPartitionRepository{db: db}
}

// WithTx returns a repository bound to tx.
func (r *ClusterPartitionRepository) WithTx(tx *gorm.DB) *ClusterPartitionRepository {
	return &ClusterPartitionRepository{db: tx}
}

// FindByClusterID returns every partition of one cluster.
func (r *ClusterPartitionRepository) FindByClusterID(ctx context.Context, clusterID string) ([]model.ClusterPartition, error) {
	var out []model.ClusterPartition
	err := r.db.WithContext(ctx).Where("cluster_id = ?", clusterID).Find(&out).Error
	return out, err
}

// FindByIDAndClusterID looks a partition up within one cluster.
//
// Scoping the lookup by cluster is deliberate: it stops a partition id belonging to
// one cluster from being reached through another cluster's path.
func (r *ClusterPartitionRepository) FindByIDAndClusterID(ctx context.Context, partitionID, clusterID string) (*model.ClusterPartition, error) {
	var out model.ClusterPartition
	err := r.db.WithContext(ctx).
		First(&out, "partition_id = ? AND cluster_id = ?", partitionID, clusterID).Error
	if err != nil {
		return nil, err
	}
	return &out, nil
}

// Save inserts or updates a partition.
func (r *ClusterPartitionRepository) Save(ctx context.Context, p *model.ClusterPartition) error {
	return r.db.WithContext(ctx).Save(p).Error
}

// Delete removes a partition.
func (r *ClusterPartitionRepository) Delete(ctx context.Context, p *model.ClusterPartition) error {
	return r.db.WithContext(ctx).Delete(p).Error
}
