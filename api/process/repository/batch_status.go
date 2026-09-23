package repository

import (
	"context"

	"gorm.io/gorm"

	model "github.com/apache/airavata/api/process/model"
)

// BatchJobStatusRepository reads and writes the scheduler states observed for a batch
// job
type BatchJobStatusRepository struct{ db *gorm.DB }

// NewBatchJobStatusRepository returns a repository backed by db.
func NewBatchJobStatusRepository(db *gorm.DB) *BatchJobStatusRepository {
	return &BatchJobStatusRepository{db: db}
}

// WithTx returns a repository bound to tx.
func (r *BatchJobStatusRepository) WithTx(tx *gorm.DB) *BatchJobStatusRepository {
	return &BatchJobStatusRepository{db: tx}
}

// Create inserts a status row. BeforeCreate assigns its id and GORM stamps UpdatedAt
// with the time of the insert.
func (r *BatchJobStatusRepository) Create(ctx context.Context, s *model.BatchJobStatus) error {
	return r.db.WithContext(ctx).Create(s).Error
}

// FindByBatchProcessID returns every status recorded for one batch process, oldest
// first.
func (r *BatchJobStatusRepository) FindByBatchProcessID(ctx context.Context, batchProcessID string) ([]model.BatchJobStatus, error) {
	var out []model.BatchJobStatus
	err := r.db.WithContext(ctx).
		Where("batch_process_id = ?", batchProcessID).
		Order("updated_at").
		Find(&out).Error
	return out, err
}

// FindLatestByBatchProcessID returns the most recent state reported for one batch
// process — what a caller asking "where is this job now" wants — or
// gorm.ErrRecordNotFound when nothing has been recorded for it yet.
func (r *BatchJobStatusRepository) FindLatestByBatchProcessID(ctx context.Context, batchProcessID string) (*model.BatchJobStatus, error) {
	var out model.BatchJobStatus
	err := r.db.WithContext(ctx).
		Where("batch_process_id = ?", batchProcessID).
		Order("updated_at DESC").
		First(&out).Error
	if err != nil {
		return nil, err
	}
	return &out, nil
}

// FindByProcessID returns every status recorded for the batch section of one process,
// oldest first.
//
// The filter goes through batch_processes because a caller coming from the API holds a
// process id, not the id of the section that owns the statuses — the same indirection
// ProcessRepository.FindByDeploymentID makes.
func (r *BatchJobStatusRepository) FindByProcessID(ctx context.Context, processID string) ([]model.BatchJobStatus, error) {
	var out []model.BatchJobStatus
	batch := r.db.WithContext(ctx).Model(&model.BatchJobProcess{}).
		Select("batch_process_id").Where("parent_process_id = ?", processID)
	err := r.db.WithContext(ctx).
		Where("batch_process_id IN (?)", batch).
		Order("updated_at").
		Find(&out).Error
	return out, err
}
