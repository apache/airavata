// Package repository reads and writes Slurm clusters, their partitions, and the login
// configs users reach them through.
package repository

import (
	"context"

	"gorm.io/gorm"

	model "github.com/apache/airavata/api/compute/model"
)

// SlurmClusterRepository reads and writes clusters.
//
// Reads preload Partitions because the response DTO always carries them; the Java
// service achieved the same thing by running reads inside a transaction so the lazy
// association could still be walked during mapping.
type SlurmClusterRepository struct{ db *gorm.DB }

// NewSlurmClusterRepository returns a repository backed by db.
func NewSlurmClusterRepository(db *gorm.DB) *SlurmClusterRepository {
	return &SlurmClusterRepository{db: db}
}

// WithTx returns a repository bound to tx.
func (r *SlurmClusterRepository) WithTx(tx *gorm.DB) *SlurmClusterRepository {
	return &SlurmClusterRepository{db: tx}
}

// FindAll returns every cluster with its partitions.
func (r *SlurmClusterRepository) FindAll(ctx context.Context) ([]model.SlurmCluster, error) {
	var out []model.SlurmCluster
	err := r.db.WithContext(ctx).Preload("Partitions").Find(&out).Error
	return out, err
}

// FindByID returns one cluster with its partitions, or gorm.ErrRecordNotFound.
func (r *SlurmClusterRepository) FindByID(ctx context.Context, id string) (*model.SlurmCluster, error) {
	var out model.SlurmCluster
	err := r.db.WithContext(ctx).Preload("Partitions").
		First(&out, "slurm_cluster_id = ?", id).Error
	if err != nil {
		return nil, err
	}
	return &out, nil
}

// Save inserts or updates a cluster.
func (r *SlurmClusterRepository) Save(ctx context.Context, c *model.SlurmCluster) error {
	return r.db.WithContext(ctx).Save(c).Error
}

// Delete removes a cluster; its partitions go with it via the cascading constraint.
func (r *SlurmClusterRepository) Delete(ctx context.Context, c *model.SlurmCluster) error {
	return r.db.WithContext(ctx).Delete(c).Error
}

// ExistsByName reports whether a cluster of that name is already registered.
//
// Carried over from the Java repository, which declared it but never called it — no
// service rejects a duplicate name, and no unique constraint backs it either.
func (r *SlurmClusterRepository) ExistsByName(ctx context.Context, name string) (bool, error) {
	var n int64
	err := r.db.WithContext(ctx).Model(&model.SlurmCluster{}).
		Where("cluster_name = ?", name).Count(&n).Error
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
