package process

import (
	"context"

	"gorm.io/gorm"

	model "github.com/apache/airavata/api/process/model"
)

// StatusRepository reads and writes batch job process statuses.
//
// There is no Update: statuses are append-only history, never corrected in place.
type StatusRepository struct{ db *gorm.DB }

// NewStatusRepository returns a repository backed by db.
func NewStatusRepository(db *gorm.DB) *StatusRepository { return &StatusRepository{db: db} }

// WithTx returns a repository bound to tx.
func (r *StatusRepository) WithTx(tx *gorm.DB) *StatusRepository { return &StatusRepository{db: tx} }

// FindByProcessID returns every status recorded for one process, oldest first.
func (r *StatusRepository) FindByProcessID(ctx context.Context, processID string) ([]model.BatchJobProcessStatus, error) {
	var out []model.BatchJobProcessStatus
	err := r.db.WithContext(ctx).
		Where("process_id = ?", processID).
		Order("timestamp").
		Find(&out).Error
	return out, err
}

// FindByIDAndProcessID returns one status scoped to its process, or
// gorm.ErrRecordNotFound if the id does not belong to that process.
func (r *StatusRepository) FindByIDAndProcessID(ctx context.Context, id, processID string) (*model.BatchJobProcessStatus, error) {
	var out model.BatchJobProcessStatus
	err := r.db.WithContext(ctx).
		First(&out, "process_status_id = ? AND process_id = ?", id, processID).Error
	if err != nil {
		return nil, err
	}
	return &out, nil
}

// Create inserts a status row. BeforeCreate assigns its id and AfterCreate repoints
// the owning process's LastStatusID at it.
func (r *StatusRepository) Create(ctx context.Context, s *model.BatchJobProcessStatus) error {
	return r.db.WithContext(ctx).Create(s).Error
}
