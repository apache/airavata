// Package repository reads and writes processes, the sections they own and their
// status history.
package repository

import (
	"context"

	"gorm.io/gorm"
	"gorm.io/gorm/clause"

	applicationmodel "github.com/apache/airavata/api/application/model"
	model "github.com/apache/airavata/api/process/model"
)

// ProcessRepository reads and writes processes.
//
// The batch process, the owned resource request and the template mappings are handled
// here rather than in repositories of their own: none is reachable except through the
// process that owns it, so there is no caller that would want one on its own.
type ProcessRepository struct{ db *gorm.DB }

// NewProcessRepository returns a repository backed by db.
func NewProcessRepository(db *gorm.DB) *ProcessRepository { return &ProcessRepository{db: db} }

// WithTx returns a repository bound to tx.
func (r *ProcessRepository) WithTx(tx *gorm.DB) *ProcessRepository { return &ProcessRepository{db: tx} }

// query returns a session preloading everything a process response renders. A process
// is never read without its sections: they are part of the resource, not a follow-up
// request a caller has to make.
func (r *ProcessRepository) query(ctx context.Context) *gorm.DB {
	return r.db.WithContext(ctx).
		Preload("BatchProcess.BatchJobConfig").
		Preload("BatchProcess.InputMappings").
		Preload("BatchProcess.OutputMappings")
}

// FindAll returns every process.
func (r *ProcessRepository) FindAll(ctx context.Context) ([]model.Process, error) {
	var out []model.Process
	err := r.query(ctx).Find(&out).Error
	return out, err
}

// FindByDeploymentID returns every process run against one deployment.
//
// The deployment is named by the batch process section rather than by the process, so
// the filter goes through that table.
func (r *ProcessRepository) FindByDeploymentID(ctx context.Context, deploymentID string) ([]model.Process, error) {
	var out []model.Process
	batch := r.db.WithContext(ctx).Model(&model.BatchJobProcess{}).
		Select("parent_process_id").Where("deployment_id = ?", deploymentID)
	err := r.query(ctx).Where("process_id IN (?)", batch).Find(&out).Error
	return out, err
}

// FindByOwnerID returns every process launched by one user.
func (r *ProcessRepository) FindByOwnerID(ctx context.Context, userID string) ([]model.Process, error) {
	var out []model.Process
	err := r.query(ctx).Where("user_id = ?", userID).Find(&out).Error
	return out, err
}

// FindByID returns one process, or gorm.ErrRecordNotFound.
func (r *ProcessRepository) FindByID(ctx context.Context, id string) (*model.Process, error) {
	var out model.Process
	if err := r.query(ctx).First(&out, "process_id = ?", id).Error; err != nil {
		return nil, err
	}
	return &out, nil
}

// Save writes the process row alone, leaving its sections to the calls below. Writing
// them separately is what keeps an update from silently upserting a half-populated
// association GORM inferred from the struct.
func (r *ProcessRepository) Save(ctx context.Context, p *model.Process) error {
	return r.db.WithContext(ctx).Omit(clause.Associations).Save(p).Error
}

// Delete removes a process. Its BeforeDelete takes the statuses and the batch process
// with it — and the mappings with that — and the database cascades the tasks.
func (r *ProcessRepository) Delete(ctx context.Context, p *model.Process) error {
	return r.db.WithContext(ctx).Delete(p).Error
}

// SaveBatchProcess inserts or updates the owned batch process section.
func (r *ProcessRepository) SaveBatchProcess(ctx context.Context, b *model.BatchJobProcess) error {
	return r.db.WithContext(ctx).Omit(clause.Associations).Save(b).Error
}

// SaveBatchJobConfig inserts or updates the resource request the section owns.
func (r *ProcessRepository) SaveBatchJobConfig(ctx context.Context, c *applicationmodel.BatchJobConfig) error {
	return r.db.WithContext(ctx).Save(c).Error
}

// ReplaceInputMappings swaps a batch process's input mapping set for the given rows.
//
// Wholesale replacement, matching how a template's declarations are replaced: the ids
// are not part of the request, so there is nothing to match an incoming mapping to an
// existing row by.
func (r *ProcessRepository) ReplaceInputMappings(ctx context.Context, batchProcessID string, mappings []*model.TemplateInputMapping) error {
	db := r.db.WithContext(ctx)
	if err := db.Where("batch_process_id = ?", batchProcessID).Delete(&model.TemplateInputMapping{}).Error; err != nil {
		return err
	}
	if len(mappings) == 0 {
		return nil
	}
	return db.Create(mappings).Error
}

// ReplaceOutputMappings swaps a batch process's output mapping set for the given rows.
func (r *ProcessRepository) ReplaceOutputMappings(ctx context.Context, batchProcessID string, mappings []*model.TemplateOutputMapping) error {
	db := r.db.WithContext(ctx)
	if err := db.Where("batch_process_id = ?", batchProcessID).Delete(&model.TemplateOutputMapping{}).Error; err != nil {
		return err
	}
	if len(mappings) == 0 {
		return nil
	}
	return db.Create(mappings).Error
}
