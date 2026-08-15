// Package repository reads and writes application templates and batch deployments.
package repository

import (
	"context"

	"gorm.io/gorm"

	model "github.com/apache/airavata/api/application/model"
)

// TemplateRepository reads and writes application templates and their owned
// input/output declarations.
type TemplateRepository struct{ db *gorm.DB }

// NewTemplateRepository returns a repository backed by db.
func NewTemplateRepository(db *gorm.DB) *TemplateRepository { return &TemplateRepository{db: db} }

// WithTx returns a repository bound to tx.
func (r *TemplateRepository) WithTx(tx *gorm.DB) *TemplateRepository {
	return &TemplateRepository{db: tx}
}

// FindAll returns every template with its declarations.
func (r *TemplateRepository) FindAll(ctx context.Context) ([]model.Template, error) {
	var out []model.Template
	err := r.db.WithContext(ctx).Preload("Inputs").Preload("Outputs").Find(&out).Error
	return out, err
}

// FindByID returns one template with its declarations, or gorm.ErrRecordNotFound.
func (r *TemplateRepository) FindByID(ctx context.Context, id string) (*model.Template, error) {
	var out model.Template
	err := r.db.WithContext(ctx).Preload("Inputs").Preload("Outputs").
		First(&out, "template_id = ?", id).Error
	if err != nil {
		return nil, err
	}
	return &out, nil
}

// SaveTemplate writes the template row only, leaving its declarations alone. Children
// are replaced explicitly by ReplaceChildren rather than cascaded from here, so an
// update cannot quietly drop declarations the caller did not mention.
func (r *TemplateRepository) SaveTemplate(ctx context.Context, t *model.Template) error {
	return r.db.WithContext(ctx).Omit("Inputs", "Outputs").Save(t).Error
}

// ReplaceChildren swaps a template's declarations for the given ones.
//
// The Java service replaced the collections wholesale on every update, so child ids
// are regenerated and any id supplied on the request is ignored. That is preserved
// here: an update declares what the inputs and outputs now are, not a patch of them.
func (r *TemplateRepository) ReplaceChildren(ctx context.Context, templateID string, inputs []model.TemplateInput, outputs []model.TemplateOutput) error {
	db := r.db.WithContext(ctx)

	if err := db.Where("template_id = ?", templateID).Delete(&model.TemplateInput{}).Error; err != nil {
		return err
	}
	if err := db.Where("template_id = ?", templateID).Delete(&model.TemplateOutput{}).Error; err != nil {
		return err
	}
	if len(inputs) > 0 {
		for i := range inputs {
			inputs[i].ID = ""
			inputs[i].TemplateID = &templateID
		}
		if err := db.Create(&inputs).Error; err != nil {
			return err
		}
	}
	if len(outputs) > 0 {
		for i := range outputs {
			outputs[i].ID = ""
			outputs[i].TemplateID = &templateID
		}
		if err := db.Create(&outputs).Error; err != nil {
			return err
		}
	}
	return nil
}

// Delete removes a template; its declarations go with it via the cascade.
func (r *TemplateRepository) Delete(ctx context.Context, t *model.Template) error {
	return r.db.WithContext(ctx).Select("Inputs", "Outputs").Delete(t).Error
}

// ExistsByName reports whether a template of that name exists. Declared but never
// called in the Java service.
func (r *TemplateRepository) ExistsByName(ctx context.Context, name string) (bool, error) {
	var n int64
	err := r.db.WithContext(ctx).Model(&model.Template{}).Where("template_name = ?", name).Count(&n).Error
	return n > 0, err
}

// BatchDeploymentRepository reads and writes deployments.
type BatchDeploymentRepository struct{ db *gorm.DB }

// NewBatchDeploymentRepository returns a repository backed by db.
func NewBatchDeploymentRepository(db *gorm.DB) *BatchDeploymentRepository {
	return &BatchDeploymentRepository{db: db}
}

// WithTx returns a repository bound to tx.
func (r *BatchDeploymentRepository) WithTx(tx *gorm.DB) *BatchDeploymentRepository {
	return &BatchDeploymentRepository{db: tx}
}

// FindAll returns every deployment with its owned batch job config.
func (r *BatchDeploymentRepository) FindAll(ctx context.Context) ([]model.BatchDeployment, error) {
	var out []model.BatchDeployment
	err := r.db.WithContext(ctx).Preload("BatchJobConfig").Find(&out).Error
	return out, err
}

// FindByTemplateID returns every deployment of one template.
func (r *BatchDeploymentRepository) FindByTemplateID(ctx context.Context, templateID string) ([]model.BatchDeployment, error) {
	var out []model.BatchDeployment
	err := r.db.WithContext(ctx).Preload("BatchJobConfig").
		Where("template_id = ?", templateID).Find(&out).Error
	return out, err
}

// FindByID returns one deployment, or gorm.ErrRecordNotFound.
func (r *BatchDeploymentRepository) FindByID(ctx context.Context, id string) (*model.BatchDeployment, error) {
	var out model.BatchDeployment
	err := r.db.WithContext(ctx).Preload("BatchJobConfig").
		First(&out, "deployment_id = ?", id).Error
	if err != nil {
		return nil, err
	}
	return &out, nil
}

// ExistsByTemplateID reports whether a template still has deployments. This is what
// blocks template deletion with a conflict.
func (r *BatchDeploymentRepository) ExistsByTemplateID(ctx context.Context, templateID string) (bool, error) {
	var n int64
	err := r.db.WithContext(ctx).Model(&model.BatchDeployment{}).
		Where("template_id = ?", templateID).Count(&n).Error
	return n > 0, err
}

// Save writes the deployment row. The owned config is written separately by
// SaveConfig, so that an update mutates the existing config row rather than
// replacing it and leaving an orphan.
func (r *BatchDeploymentRepository) Save(ctx context.Context, d *model.BatchDeployment) error {
	return r.db.WithContext(ctx).Omit("BatchJobConfig").Save(d).Error
}

// SaveConfig inserts or updates an owned batch job config.
func (r *BatchDeploymentRepository) SaveConfig(ctx context.Context, c *model.BatchJobConfig) error {
	return r.db.WithContext(ctx).Save(c).Error
}

// Delete removes a deployment; the AfterDelete hook removes its owned config.
func (r *BatchDeploymentRepository) Delete(ctx context.Context, d *model.BatchDeployment) error {
	return r.db.WithContext(ctx).Delete(d).Error
}
