package repository

import (
	"context"

	"gorm.io/gorm"

	model "github.com/apache/airavata/api/compute/model"
	iammodel "github.com/apache/airavata/api/iam/model"
)

// SlurmClusterConfigRepository reads and writes cluster login configs.
//
// Reads preload the cluster and the key summary because the response DTO carries both:
// a config without the machine it logs in to, and without which key it presents, is not
// much use to a caller deciding whether to launch through it.
type SlurmClusterConfigRepository struct{ db *gorm.DB }

// NewSlurmClusterConfigRepository returns a repository backed by db.
func NewSlurmClusterConfigRepository(db *gorm.DB) *SlurmClusterConfigRepository {
	return &SlurmClusterConfigRepository{db: db}
}

// WithTx returns a repository bound to tx.
func (r *SlurmClusterConfigRepository) WithTx(tx *gorm.DB) *SlurmClusterConfigRepository {
	return &SlurmClusterConfigRepository{db: tx}
}

// withReferences is the read scope every lookup that feeds a response DTO starts from.
func (r *SlurmClusterConfigRepository) withReferences(ctx context.Context) *gorm.DB {
	return r.db.WithContext(ctx).
		Preload("SlurmCluster").
		Preload("SlurmCluster.Partitions").
		Preload("SSHKey")
}

// FindAll returns every config.
func (r *SlurmClusterConfigRepository) FindAll(ctx context.Context) ([]model.SlurmClusterConfig, error) {
	var out []model.SlurmClusterConfig
	err := r.withReferences(ctx).Find(&out).Error
	return out, err
}

// FindByID returns one config, or gorm.ErrRecordNotFound.
func (r *SlurmClusterConfigRepository) FindByID(ctx context.Context, id string) (*model.SlurmClusterConfig, error) {
	var out model.SlurmClusterConfig
	if err := r.withReferences(ctx).First(&out, "slurm_cluster_config_id = ?", id).Error; err != nil {
		return nil, err
	}
	return &out, nil
}

// FindByOwnerID returns every config owned by one user.
func (r *SlurmClusterConfigRepository) FindByOwnerID(ctx context.Context, userID string) ([]model.SlurmClusterConfig, error) {
	var out []model.SlurmClusterConfig
	err := r.withReferences(ctx).Where("user_id = ?", userID).Find(&out).Error
	return out, err
}

// FindBySlurmClusterID returns every config registered against one cluster. It is what
// makes deleting a cluster still in use reportable as a conflict rather than a foreign
// key error.
func (r *SlurmClusterConfigRepository) FindBySlurmClusterID(ctx context.Context, clusterID string) ([]model.SlurmClusterConfig, error) {
	var out []model.SlurmClusterConfig
	err := r.db.WithContext(ctx).Where("slurm_cluster_id = ?", clusterID).Find(&out).Error
	return out, err
}

// FindSharedWith returns every config reaching userID through a share: named directly,
// or through a group they are an active member of.
//
// Configs they own are excluded — ownership is not a share, and /me already covers
// their own.
func (r *SlurmClusterConfigRepository) FindSharedWith(ctx context.Context, userID string) ([]model.SlurmClusterConfig, error) {
	sharedDirectly := r.db.Model(&model.SlurmClusterConfigUserSharing{}).
		Select("slurm_cluster_config_id").
		Where("user_id = ?", userID)

	sharedByGroup := r.db.Model(&model.SlurmClusterConfigGroupSharing{}).
		Select("slurm_cluster_config_id").
		Where("group_id IN (?)", r.db.Model(&iammodel.GroupMember{}).
			Select("group_id").
			Where("user_id = ? AND group_member_status = ?", userID, iammodel.GroupMemberStatusActive))

	var out []model.SlurmClusterConfig
	err := r.withReferences(ctx).
		Where("(slurm_cluster_config_id IN (?) OR slurm_cluster_config_id IN (?)) AND (user_id IS NULL OR user_id <> ?)",
			sharedDirectly, sharedByGroup, userID).
		Find(&out).Error
	return out, err
}

// Save inserts or updates a config.
func (r *SlurmClusterConfigRepository) Save(ctx context.Context, c *model.SlurmClusterConfig) error {
	return r.db.WithContext(ctx).Save(c).Error
}

// Delete removes a config.
func (r *SlurmClusterConfigRepository) Delete(ctx context.Context, c *model.SlurmClusterConfig) error {
	return r.db.WithContext(ctx).Delete(c).Error
}

// SlurmClusterConfigSharingRepository reads and writes both kinds of config share.
type SlurmClusterConfigSharingRepository struct{ db *gorm.DB }

// NewSlurmClusterConfigSharingRepository returns a repository backed by db.
func NewSlurmClusterConfigSharingRepository(db *gorm.DB) *SlurmClusterConfigSharingRepository {
	return &SlurmClusterConfigSharingRepository{db: db}
}

// WithTx returns a repository bound to tx.
func (r *SlurmClusterConfigSharingRepository) WithTx(tx *gorm.DB) *SlurmClusterConfigSharingRepository {
	return &SlurmClusterConfigSharingRepository{db: tx}
}

// FindGroupSharesByConfigID returns every group share of one config.
func (r *SlurmClusterConfigSharingRepository) FindGroupSharesByConfigID(ctx context.Context, configID string) ([]model.SlurmClusterConfigGroupSharing, error) {
	var out []model.SlurmClusterConfigGroupSharing
	err := r.db.WithContext(ctx).Where("slurm_cluster_config_id = ?", configID).Find(&out).Error
	return out, err
}

// FindGroupShare returns one group share scoped to its config, or
// gorm.ErrRecordNotFound.
func (r *SlurmClusterConfigSharingRepository) FindGroupShare(ctx context.Context, configID, sharingID string) (*model.SlurmClusterConfigGroupSharing, error) {
	var out model.SlurmClusterConfigGroupSharing
	err := r.db.WithContext(ctx).First(&out,
		"slurm_cluster_config_group_sharing_id = ? AND slurm_cluster_config_id = ?", sharingID, configID).Error
	if err != nil {
		return nil, err
	}
	return &out, nil
}

// FindGroupShareByGroupID returns the share of one config with one group, or
// gorm.ErrRecordNotFound.
func (r *SlurmClusterConfigSharingRepository) FindGroupShareByGroupID(ctx context.Context, configID, groupID string) (*model.SlurmClusterConfigGroupSharing, error) {
	var out model.SlurmClusterConfigGroupSharing
	err := r.db.WithContext(ctx).First(&out,
		"slurm_cluster_config_id = ? AND group_id = ?", configID, groupID).Error
	if err != nil {
		return nil, err
	}
	return &out, nil
}

// SaveGroupShare inserts or updates a group share.
func (r *SlurmClusterConfigSharingRepository) SaveGroupShare(ctx context.Context, s *model.SlurmClusterConfigGroupSharing) error {
	return r.db.WithContext(ctx).Save(s).Error
}

// DeleteGroupShare removes a group share.
func (r *SlurmClusterConfigSharingRepository) DeleteGroupShare(ctx context.Context, s *model.SlurmClusterConfigGroupSharing) error {
	return r.db.WithContext(ctx).Delete(s).Error
}

// FindUserSharesByConfigID returns every user share of one config.
func (r *SlurmClusterConfigSharingRepository) FindUserSharesByConfigID(ctx context.Context, configID string) ([]model.SlurmClusterConfigUserSharing, error) {
	var out []model.SlurmClusterConfigUserSharing
	err := r.db.WithContext(ctx).Where("slurm_cluster_config_id = ?", configID).Find(&out).Error
	return out, err
}

// FindUserShare returns one user share scoped to its config, or
// gorm.ErrRecordNotFound.
func (r *SlurmClusterConfigSharingRepository) FindUserShare(ctx context.Context, configID, sharingID string) (*model.SlurmClusterConfigUserSharing, error) {
	var out model.SlurmClusterConfigUserSharing
	err := r.db.WithContext(ctx).First(&out,
		"slurm_cluster_config_user_sharing_id = ? AND slurm_cluster_config_id = ?", sharingID, configID).Error
	if err != nil {
		return nil, err
	}
	return &out, nil
}

// FindUserShareByUserID returns the share of one config with one user, or
// gorm.ErrRecordNotFound.
func (r *SlurmClusterConfigSharingRepository) FindUserShareByUserID(ctx context.Context, configID, userID string) (*model.SlurmClusterConfigUserSharing, error) {
	var out model.SlurmClusterConfigUserSharing
	err := r.db.WithContext(ctx).First(&out,
		"slurm_cluster_config_id = ? AND user_id = ?", configID, userID).Error
	if err != nil {
		return nil, err
	}
	return &out, nil
}

// SaveUserShare inserts or updates a user share.
func (r *SlurmClusterConfigSharingRepository) SaveUserShare(ctx context.Context, s *model.SlurmClusterConfigUserSharing) error {
	return r.db.WithContext(ctx).Save(s).Error
}

// DeleteUserShare removes a user share.
func (r *SlurmClusterConfigSharingRepository) DeleteUserShare(ctx context.Context, s *model.SlurmClusterConfigUserSharing) error {
	return r.db.WithContext(ctx).Delete(s).Error
}

// DeleteByConfigID removes every share of one config.
func (r *SlurmClusterConfigSharingRepository) DeleteByConfigID(ctx context.Context, configID string) error {
	if err := r.db.WithContext(ctx).Where("slurm_cluster_config_id = ?", configID).
		Delete(&model.SlurmClusterConfigGroupSharing{}).Error; err != nil {
		return err
	}
	return r.db.WithContext(ctx).Where("slurm_cluster_config_id = ?", configID).
		Delete(&model.SlurmClusterConfigUserSharing{}).Error
}
