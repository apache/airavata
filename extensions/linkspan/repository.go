package linkspan

import (
	"context"

	"gorm.io/gorm"
)

// LinkRepository persists the link secrets issued to each process's job.
type LinkRepository struct{ db *gorm.DB }

// NewLinkRepository returns a repository over db.
func NewLinkRepository(db *gorm.DB) *LinkRepository { return &LinkRepository{db: db} }

// FindByProcessID returns the process's link, or gorm.ErrRecordNotFound.
func (r *LinkRepository) FindByProcessID(ctx context.Context, processID string) (*ProcessLink, error) {
	var out ProcessLink
	if err := r.db.WithContext(ctx).First(&out, "process_id = ?", processID).Error; err != nil {
		return nil, err
	}
	return &out, nil
}

// Save inserts the link or replaces the process's existing one.
func (r *LinkRepository) Save(ctx context.Context, link *ProcessLink) error {
	return r.db.WithContext(ctx).Save(link).Error
}
