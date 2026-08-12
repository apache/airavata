package data

import (
	"context"

	"gorm.io/gorm"

	model "github.com/apache/airavata/api/data/model"
)

// SCPDataRepository reads and writes registered datasets.
type SCPDataRepository struct{ db *gorm.DB }

// NewSCPDataRepository returns a repository backed by db.
func NewSCPDataRepository(db *gorm.DB) *SCPDataRepository { return &SCPDataRepository{db: db} }

// WithTx returns a repository bound to tx.
func (r *SCPDataRepository) WithTx(tx *gorm.DB) *SCPDataRepository {
	return &SCPDataRepository{db: tx}
}

// FindAll returns every dataset across every owner.
func (r *SCPDataRepository) FindAll(ctx context.Context) ([]model.SCPData, error) {
	var out []model.SCPData
	err := r.db.WithContext(ctx).Find(&out).Error
	return out, err
}

// FindByOwnerID returns every dataset owned by one user.
func (r *SCPDataRepository) FindByOwnerID(ctx context.Context, userID string) ([]model.SCPData, error) {
	var out []model.SCPData
	err := r.db.WithContext(ctx).Where("user_id = ?", userID).Find(&out).Error
	return out, err
}

// FindByID returns one dataset, or gorm.ErrRecordNotFound.
func (r *SCPDataRepository) FindByID(ctx context.Context, id string) (*model.SCPData, error) {
	var out model.SCPData
	if err := r.db.WithContext(ctx).First(&out, "data_id = ?", id).Error; err != nil {
		return nil, err
	}
	return &out, nil
}

// Save inserts or updates a dataset.
func (r *SCPDataRepository) Save(ctx context.Context, d *model.SCPData) error {
	return r.db.WithContext(ctx).Save(d).Error
}

// Delete removes a dataset.
func (r *SCPDataRepository) Delete(ctx context.Context, d *model.SCPData) error {
	return r.db.WithContext(ctx).Delete(d).Error
}
