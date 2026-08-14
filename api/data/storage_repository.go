package data

import (
	"context"

	"gorm.io/gorm"

	model "github.com/apache/airavata/api/data/model"
	iammodel "github.com/apache/airavata/api/iam/model"
)

// SCPDataStorageRepository reads and writes SCP data storages.
//
// Reads preload the SSH endpoint because the response DTO always carries it: a storage
// without its host is not much use to a caller deciding whether to stage data there.
type SCPDataStorageRepository struct{ db *gorm.DB }

// NewSCPDataStorageRepository returns a repository backed by db.
func NewSCPDataStorageRepository(db *gorm.DB) *SCPDataStorageRepository {
	return &SCPDataStorageRepository{db: db}
}

// WithTx returns a repository bound to tx.
func (r *SCPDataStorageRepository) WithTx(tx *gorm.DB) *SCPDataStorageRepository {
	return &SCPDataStorageRepository{db: tx}
}

// FindAll returns every storage.
func (r *SCPDataStorageRepository) FindAll(ctx context.Context) ([]model.SCPDataStorage, error) {
	var out []model.SCPDataStorage
	err := r.db.WithContext(ctx).Preload("SSHEndpoint").Find(&out).Error
	return out, err
}

// FindByID returns one storage, or gorm.ErrRecordNotFound.
func (r *SCPDataStorageRepository) FindByID(ctx context.Context, id string) (*model.SCPDataStorage, error) {
	var out model.SCPDataStorage
	if err := r.db.WithContext(ctx).Preload("SSHEndpoint").First(&out, "data_id = ?", id).Error; err != nil {
		return nil, err
	}
	return &out, nil
}

// FindByOwnerID returns every storage owned by one user.
func (r *SCPDataStorageRepository) FindByOwnerID(ctx context.Context, userID string) ([]model.SCPDataStorage, error) {
	var out []model.SCPDataStorage
	err := r.db.WithContext(ctx).Preload("SSHEndpoint").Where("user_id = ?", userID).Find(&out).Error
	return out, err
}

// FindBySSHEndpointID returns every storage staged through one endpoint.
func (r *SCPDataStorageRepository) FindBySSHEndpointID(ctx context.Context, endpointID string) ([]model.SCPDataStorage, error) {
	var out []model.SCPDataStorage
	err := r.db.WithContext(ctx).Where("ssh_endpoint_id = ?", endpointID).Find(&out).Error
	return out, err
}

// FindSharedWith returns every storage reaching userID through a share: named
// directly, or through a group they are an active member of.
//
// Storages they own are excluded — ownership is not a share, and /me already covers
// their own.
func (r *SCPDataStorageRepository) FindSharedWith(ctx context.Context, userID string) ([]model.SCPDataStorage, error) {
	sharedDirectly := r.db.Model(&model.SCPDataStorageUserSharing{}).
		Select("data_storage_id").
		Where("user_id = ?", userID)

	sharedByGroup := r.db.Model(&model.SCPDataStorageGroupSharing{}).
		Select("data_storage_id").
		Where("group_id IN (?)", r.db.Model(&iammodel.GroupMember{}).
			Select("group_id").
			Where("user_id = ? AND group_member_status = ?", userID, iammodel.GroupMemberStatusActive))

	var out []model.SCPDataStorage
	err := r.db.WithContext(ctx).Preload("SSHEndpoint").
		Where("(data_id IN (?) OR data_id IN (?)) AND (user_id IS NULL OR user_id <> ?)",
			sharedDirectly, sharedByGroup, userID).
		Find(&out).Error
	return out, err
}

// Save inserts or updates a storage.
func (r *SCPDataStorageRepository) Save(ctx context.Context, s *model.SCPDataStorage) error {
	return r.db.WithContext(ctx).Save(s).Error
}

// Delete removes a storage.
func (r *SCPDataStorageRepository) Delete(ctx context.Context, s *model.SCPDataStorage) error {
	return r.db.WithContext(ctx).Delete(s).Error
}

// SCPDataStorageSharingRepository reads and writes both kinds of storage share.
type SCPDataStorageSharingRepository struct{ db *gorm.DB }

// NewSCPDataStorageSharingRepository returns a repository backed by db.
func NewSCPDataStorageSharingRepository(db *gorm.DB) *SCPDataStorageSharingRepository {
	return &SCPDataStorageSharingRepository{db: db}
}

// WithTx returns a repository bound to tx.
func (r *SCPDataStorageSharingRepository) WithTx(tx *gorm.DB) *SCPDataStorageSharingRepository {
	return &SCPDataStorageSharingRepository{db: tx}
}

// FindGroupSharesByStorageID returns every group share of one storage.
func (r *SCPDataStorageSharingRepository) FindGroupSharesByStorageID(ctx context.Context, storageID string) ([]model.SCPDataStorageGroupSharing, error) {
	var out []model.SCPDataStorageGroupSharing
	err := r.db.WithContext(ctx).Where("data_storage_id = ?", storageID).Find(&out).Error
	return out, err
}

// FindGroupShare returns one group share scoped to its storage, or
// gorm.ErrRecordNotFound.
func (r *SCPDataStorageSharingRepository) FindGroupShare(ctx context.Context, storageID, sharingID string) (*model.SCPDataStorageGroupSharing, error) {
	var out model.SCPDataStorageGroupSharing
	err := r.db.WithContext(ctx).First(&out,
		"data_storage_group_sharing_id = ? AND data_storage_id = ?", sharingID, storageID).Error
	if err != nil {
		return nil, err
	}
	return &out, nil
}

// FindGroupShareByGroupID returns the share of one storage with one group, or
// gorm.ErrRecordNotFound.
func (r *SCPDataStorageSharingRepository) FindGroupShareByGroupID(ctx context.Context, storageID, groupID string) (*model.SCPDataStorageGroupSharing, error) {
	var out model.SCPDataStorageGroupSharing
	err := r.db.WithContext(ctx).First(&out,
		"data_storage_id = ? AND group_id = ?", storageID, groupID).Error
	if err != nil {
		return nil, err
	}
	return &out, nil
}

// SaveGroupShare inserts or updates a group share.
func (r *SCPDataStorageSharingRepository) SaveGroupShare(ctx context.Context, s *model.SCPDataStorageGroupSharing) error {
	return r.db.WithContext(ctx).Save(s).Error
}

// DeleteGroupShare removes a group share.
func (r *SCPDataStorageSharingRepository) DeleteGroupShare(ctx context.Context, s *model.SCPDataStorageGroupSharing) error {
	return r.db.WithContext(ctx).Delete(s).Error
}

// FindUserSharesByStorageID returns every user share of one storage.
func (r *SCPDataStorageSharingRepository) FindUserSharesByStorageID(ctx context.Context, storageID string) ([]model.SCPDataStorageUserSharing, error) {
	var out []model.SCPDataStorageUserSharing
	err := r.db.WithContext(ctx).Where("data_storage_id = ?", storageID).Find(&out).Error
	return out, err
}

// FindUserShare returns one user share scoped to its storage, or
// gorm.ErrRecordNotFound.
func (r *SCPDataStorageSharingRepository) FindUserShare(ctx context.Context, storageID, sharingID string) (*model.SCPDataStorageUserSharing, error) {
	var out model.SCPDataStorageUserSharing
	err := r.db.WithContext(ctx).First(&out,
		"data_storage_user_sharing_id = ? AND data_storage_id = ?", sharingID, storageID).Error
	if err != nil {
		return nil, err
	}
	return &out, nil
}

// FindUserShareByUserID returns the share of one storage with one user, or
// gorm.ErrRecordNotFound.
func (r *SCPDataStorageSharingRepository) FindUserShareByUserID(ctx context.Context, storageID, userID string) (*model.SCPDataStorageUserSharing, error) {
	var out model.SCPDataStorageUserSharing
	err := r.db.WithContext(ctx).First(&out,
		"data_storage_id = ? AND user_id = ?", storageID, userID).Error
	if err != nil {
		return nil, err
	}
	return &out, nil
}

// SaveUserShare inserts or updates a user share.
func (r *SCPDataStorageSharingRepository) SaveUserShare(ctx context.Context, s *model.SCPDataStorageUserSharing) error {
	return r.db.WithContext(ctx).Save(s).Error
}

// DeleteUserShare removes a user share.
func (r *SCPDataStorageSharingRepository) DeleteUserShare(ctx context.Context, s *model.SCPDataStorageUserSharing) error {
	return r.db.WithContext(ctx).Delete(s).Error
}

// DeleteByStorageID removes every share of one storage.
func (r *SCPDataStorageSharingRepository) DeleteByStorageID(ctx context.Context, storageID string) error {
	if err := r.db.WithContext(ctx).Where("data_storage_id = ?", storageID).
		Delete(&model.SCPDataStorageGroupSharing{}).Error; err != nil {
		return err
	}
	return r.db.WithContext(ctx).Where("data_storage_id = ?", storageID).
		Delete(&model.SCPDataStorageUserSharing{}).Error
}
