package repository

import (
	"context"

	"gorm.io/gorm"

	model "github.com/apache/airavata/api/credentials/model"
)

// SSHEndpointRepository reads and writes SSH endpoints.
type SSHEndpointRepository struct{ db *gorm.DB }

// NewSSHEndpointRepository returns a repository backed by db.
func NewSSHEndpointRepository(db *gorm.DB) *SSHEndpointRepository {
	return &SSHEndpointRepository{db: db}
}

// WithTx returns a repository bound to tx.
func (r *SSHEndpointRepository) WithTx(tx *gorm.DB) *SSHEndpointRepository {
	return &SSHEndpointRepository{db: tx}
}

// FindAll returns every endpoint.
func (r *SSHEndpointRepository) FindAll(ctx context.Context) ([]model.SSHEndpoint, error) {
	var out []model.SSHEndpoint
	err := r.db.WithContext(ctx).Find(&out).Error
	return out, err
}

// FindByID returns one endpoint, or gorm.ErrRecordNotFound.
func (r *SSHEndpointRepository) FindByID(ctx context.Context, id string) (*model.SSHEndpoint, error) {
	var out model.SSHEndpoint
	if err := r.db.WithContext(ctx).First(&out, "ssh_endpoint_id = ?", id).Error; err != nil {
		return nil, err
	}
	return &out, nil
}

// Save inserts or updates an endpoint.
func (r *SSHEndpointRepository) Save(ctx context.Context, e *model.SSHEndpoint) error {
	return r.db.WithContext(ctx).Save(e).Error
}

// Delete removes an endpoint.
func (r *SSHEndpointRepository) Delete(ctx context.Context, e *model.SSHEndpoint) error {
	return r.db.WithContext(ctx).Delete(e).Error
}
