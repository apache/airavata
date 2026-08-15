// Package repository reads and writes batch job processes and their status history.
package repository

import (
	"context"

	"gorm.io/gorm"

	applicationmodel "github.com/apache/airavata/api/application/model"
	model "github.com/apache/airavata/api/process/model"
)

// ProcessRepository reads and writes batch job processes.
type ProcessRepository struct{ db *gorm.DB }

// NewProcessRepository returns a repository backed by db.
func NewProcessRepository(db *gorm.DB) *ProcessRepository { return &ProcessRepository{db: db} }

// WithTx returns a repository bound to tx.
func (r *ProcessRepository) WithTx(tx *gorm.DB) *ProcessRepository { return &ProcessRepository{db: tx} }

// FindAll returns every process with its owned resource request.
func (r *ProcessRepository) FindAll(ctx context.Context) ([]model.BatchJobProcess, error) {
	var out []model.BatchJobProcess
	err := r.db.WithContext(ctx).Preload("BatchJobConfig").Find(&out).Error
	return out, err
}

// FindByDeploymentID returns every process of one deployment.
func (r *ProcessRepository) FindByDeploymentID(ctx context.Context, deploymentID string) ([]model.BatchJobProcess, error) {
	var out []model.BatchJobProcess
	err := r.db.WithContext(ctx).Preload("BatchJobConfig").
		Where("deployment_id = ?", deploymentID).Find(&out).Error
	return out, err
}

// FindByOwnerID returns every process launched by one user.
//
// Declared in the Java repository but never called — there is no "my processes"
// endpoint, which is why process reads are not owner-scoped at all.
func (r *ProcessRepository) FindByOwnerID(ctx context.Context, userID string) ([]model.BatchJobProcess, error) {
	var out []model.BatchJobProcess
	err := r.db.WithContext(ctx).Preload("BatchJobConfig").
		Where("user_id = ?", userID).Find(&out).Error
	return out, err
}

// FindByID returns one process, or gorm.ErrRecordNotFound.
func (r *ProcessRepository) FindByID(ctx context.Context, id string) (*model.BatchJobProcess, error) {
	var out model.BatchJobProcess
	err := r.db.WithContext(ctx).Preload("BatchJobConfig").
		First(&out, "process_id = ?", id).Error
	if err != nil {
		return nil, err
	}
	return &out, nil
}

// Save writes the process row, leaving its owned config to SaveConfig.
func (r *ProcessRepository) Save(ctx context.Context, p *model.BatchJobProcess) error {
	return r.db.WithContext(ctx).Omit("BatchJobConfig").Save(p).Error
}

// SaveConfig inserts or updates the owned resource request.
func (r *ProcessRepository) SaveConfig(ctx context.Context, c *applicationmodel.BatchJobConfig) error {
	return r.db.WithContext(ctx).Save(c).Error
}

// Delete removes a process; the AfterDelete hook removes its owned config.
func (r *ProcessRepository) Delete(ctx context.Context, p *model.BatchJobProcess) error {
	return r.db.WithContext(ctx).Delete(p).Error
}
