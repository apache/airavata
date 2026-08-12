package process

import (
	"context"

	"gorm.io/gorm"

	applicationmodel "github.com/apache/airavata/api/application/model"
	model "github.com/apache/airavata/api/process/model"
)

// Repository reads and writes batch job processes.
type Repository struct{ db *gorm.DB }

// NewRepository returns a repository backed by db.
func NewRepository(db *gorm.DB) *Repository { return &Repository{db: db} }

// WithTx returns a repository bound to tx.
func (r *Repository) WithTx(tx *gorm.DB) *Repository { return &Repository{db: tx} }

// FindAll returns every process with its owned resource request.
func (r *Repository) FindAll(ctx context.Context) ([]model.BatchJobProcess, error) {
	var out []model.BatchJobProcess
	err := r.db.WithContext(ctx).Preload("BatchJobConfig").Find(&out).Error
	return out, err
}

// FindByDeploymentID returns every process of one deployment.
func (r *Repository) FindByDeploymentID(ctx context.Context, deploymentID string) ([]model.BatchJobProcess, error) {
	var out []model.BatchJobProcess
	err := r.db.WithContext(ctx).Preload("BatchJobConfig").
		Where("deployment_id = ?", deploymentID).Find(&out).Error
	return out, err
}

// FindByOwnerID returns every process launched by one user.
//
// Declared in the Java repository but never called — there is no "my processes"
// endpoint, which is why process reads are not owner-scoped at all.
func (r *Repository) FindByOwnerID(ctx context.Context, userID string) ([]model.BatchJobProcess, error) {
	var out []model.BatchJobProcess
	err := r.db.WithContext(ctx).Preload("BatchJobConfig").
		Where("user_id = ?", userID).Find(&out).Error
	return out, err
}

// FindByID returns one process, or gorm.ErrRecordNotFound.
func (r *Repository) FindByID(ctx context.Context, id string) (*model.BatchJobProcess, error) {
	var out model.BatchJobProcess
	err := r.db.WithContext(ctx).Preload("BatchJobConfig").
		First(&out, "process_id = ?", id).Error
	if err != nil {
		return nil, err
	}
	return &out, nil
}

// Save writes the process row, leaving its owned config to SaveConfig.
func (r *Repository) Save(ctx context.Context, p *model.BatchJobProcess) error {
	return r.db.WithContext(ctx).Omit("BatchJobConfig").Save(p).Error
}

// SaveConfig inserts or updates the owned resource request.
func (r *Repository) SaveConfig(ctx context.Context, c *applicationmodel.BatchJobConfig) error {
	return r.db.WithContext(ctx).Save(c).Error
}

// Delete removes a process; the AfterDelete hook removes its owned config.
func (r *Repository) Delete(ctx context.Context, p *model.BatchJobProcess) error {
	return r.db.WithContext(ctx).Delete(p).Error
}
