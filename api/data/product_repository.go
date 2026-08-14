package data

import (
	"context"

	"gorm.io/gorm"

	model "github.com/apache/airavata/api/data/model"
	iammodel "github.com/apache/airavata/api/iam/model"
)

// DataProductRepository reads and writes registered datasets.
type DataProductRepository struct{ db *gorm.DB }

// NewDataProductRepository returns a repository backed by db.
func NewDataProductRepository(db *gorm.DB) *DataProductRepository {
	return &DataProductRepository{db: db}
}

// WithTx returns a repository bound to tx.
func (r *DataProductRepository) WithTx(tx *gorm.DB) *DataProductRepository {
	return &DataProductRepository{db: tx}
}

// FindAll returns every product across every owner.
func (r *DataProductRepository) FindAll(ctx context.Context) ([]model.DataProduct, error) {
	var out []model.DataProduct
	err := r.db.WithContext(ctx).Find(&out).Error
	return out, err
}

// FindByID returns one product, or gorm.ErrRecordNotFound.
func (r *DataProductRepository) FindByID(ctx context.Context, id string) (*model.DataProduct, error) {
	var out model.DataProduct
	if err := r.db.WithContext(ctx).First(&out, "data_id = ?", id).Error; err != nil {
		return nil, err
	}
	return &out, nil
}

// FindByOwnerID returns every product owned by one user.
func (r *DataProductRepository) FindByOwnerID(ctx context.Context, userID string) ([]model.DataProduct, error) {
	var out []model.DataProduct
	err := r.db.WithContext(ctx).Where("user_id = ?", userID).Find(&out).Error
	return out, err
}

// FindByDataStorageID returns every product staged on one storage. It is what makes
// deleting a storage still in use reportable as a conflict.
func (r *DataProductRepository) FindByDataStorageID(ctx context.Context, storageID string) ([]model.DataProduct, error) {
	var out []model.DataProduct
	err := r.db.WithContext(ctx).Where("data_storage_id = ?", storageID).Find(&out).Error
	return out, err
}

// FindSharedWith returns every product reaching userID through a share: named
// directly, or through a group they are an active member of.
//
// Products they own are excluded — ownership is not a share, and the caller asking
// "what has been shared with me?" already has /me for their own.
func (r *DataProductRepository) FindSharedWith(ctx context.Context, userID string) ([]model.DataProduct, error) {
	sharedDirectly := r.db.Model(&model.DataProductUserSharing{}).
		Select("data_product_id").
		Where("user_id = ?", userID)

	sharedByGroup := r.db.Model(&model.DataProductGroupSharing{}).
		Select("data_product_id").
		Where("group_id IN (?)", r.db.Model(&iammodel.GroupMember{}).
			Select("group_id").
			Where("user_id = ? AND group_member_status = ?", userID, iammodel.GroupMemberStatusActive))

	var out []model.DataProduct
	err := r.db.WithContext(ctx).
		Where("(data_id IN (?) OR data_id IN (?)) AND (user_id IS NULL OR user_id <> ?)",
			sharedDirectly, sharedByGroup, userID).
		Find(&out).Error
	return out, err
}

// Save inserts or updates a product.
func (r *DataProductRepository) Save(ctx context.Context, p *model.DataProduct) error {
	return r.db.WithContext(ctx).Save(p).Error
}

// Delete removes a product.
func (r *DataProductRepository) Delete(ctx context.Context, p *model.DataProduct) error {
	return r.db.WithContext(ctx).Delete(p).Error
}

// DataProductSharingRepository reads and writes both kinds of product share.
//
// They are one repository rather than two because every caller needs them together:
// resolving what a principal may do with a product means asking both tables.
type DataProductSharingRepository struct{ db *gorm.DB }

// NewDataProductSharingRepository returns a repository backed by db.
func NewDataProductSharingRepository(db *gorm.DB) *DataProductSharingRepository {
	return &DataProductSharingRepository{db: db}
}

// WithTx returns a repository bound to tx.
func (r *DataProductSharingRepository) WithTx(tx *gorm.DB) *DataProductSharingRepository {
	return &DataProductSharingRepository{db: tx}
}

// FindGroupSharesByProductID returns every group share of one product.
func (r *DataProductSharingRepository) FindGroupSharesByProductID(ctx context.Context, productID string) ([]model.DataProductGroupSharing, error) {
	var out []model.DataProductGroupSharing
	err := r.db.WithContext(ctx).Where("data_product_id = ?", productID).Find(&out).Error
	return out, err
}

// FindGroupShare returns one group share scoped to its product, or
// gorm.ErrRecordNotFound.
//
// Scoping by product is deliberate: it stops a sharing id belonging to one product
// from being reached through another product's path.
func (r *DataProductSharingRepository) FindGroupShare(ctx context.Context, productID, sharingID string) (*model.DataProductGroupSharing, error) {
	var out model.DataProductGroupSharing
	err := r.db.WithContext(ctx).First(&out,
		"data_product_group_sharing_id = ? AND data_product_id = ?", sharingID, productID).Error
	if err != nil {
		return nil, err
	}
	return &out, nil
}

// FindGroupShareByGroupID returns the share of one product with one group, or
// gorm.ErrRecordNotFound. It is what makes a duplicate share reportable as a conflict.
func (r *DataProductSharingRepository) FindGroupShareByGroupID(ctx context.Context, productID, groupID string) (*model.DataProductGroupSharing, error) {
	var out model.DataProductGroupSharing
	err := r.db.WithContext(ctx).First(&out,
		"data_product_id = ? AND group_id = ?", productID, groupID).Error
	if err != nil {
		return nil, err
	}
	return &out, nil
}

// SaveGroupShare inserts or updates a group share.
func (r *DataProductSharingRepository) SaveGroupShare(ctx context.Context, s *model.DataProductGroupSharing) error {
	return r.db.WithContext(ctx).Save(s).Error
}

// DeleteGroupShare removes a group share.
func (r *DataProductSharingRepository) DeleteGroupShare(ctx context.Context, s *model.DataProductGroupSharing) error {
	return r.db.WithContext(ctx).Delete(s).Error
}

// FindUserSharesByProductID returns every user share of one product.
func (r *DataProductSharingRepository) FindUserSharesByProductID(ctx context.Context, productID string) ([]model.DataProductUserSharing, error) {
	var out []model.DataProductUserSharing
	err := r.db.WithContext(ctx).Where("data_product_id = ?", productID).Find(&out).Error
	return out, err
}

// FindUserShare returns one user share scoped to its product, or
// gorm.ErrRecordNotFound.
func (r *DataProductSharingRepository) FindUserShare(ctx context.Context, productID, sharingID string) (*model.DataProductUserSharing, error) {
	var out model.DataProductUserSharing
	err := r.db.WithContext(ctx).First(&out,
		"data_product_user_sharing_id = ? AND data_product_id = ?", sharingID, productID).Error
	if err != nil {
		return nil, err
	}
	return &out, nil
}

// FindUserShareByUserID returns the share of one product with one user, or
// gorm.ErrRecordNotFound.
func (r *DataProductSharingRepository) FindUserShareByUserID(ctx context.Context, productID, userID string) (*model.DataProductUserSharing, error) {
	var out model.DataProductUserSharing
	err := r.db.WithContext(ctx).First(&out,
		"data_product_id = ? AND user_id = ?", productID, userID).Error
	if err != nil {
		return nil, err
	}
	return &out, nil
}

// SaveUserShare inserts or updates a user share.
func (r *DataProductSharingRepository) SaveUserShare(ctx context.Context, s *model.DataProductUserSharing) error {
	return r.db.WithContext(ctx).Save(s).Error
}

// DeleteUserShare removes a user share.
func (r *DataProductSharingRepository) DeleteUserShare(ctx context.Context, s *model.DataProductUserSharing) error {
	return r.db.WithContext(ctx).Delete(s).Error
}

// DeleteByProductID removes every share of one product.
//
// The foreign keys are RESTRICT, so deleting a product that is still shared would
// otherwise fail; the service calls this first, inside the same transaction.
func (r *DataProductSharingRepository) DeleteByProductID(ctx context.Context, productID string) error {
	if err := r.db.WithContext(ctx).Where("data_product_id = ?", productID).
		Delete(&model.DataProductGroupSharing{}).Error; err != nil {
		return err
	}
	return r.db.WithContext(ctx).Where("data_product_id = ?", productID).
		Delete(&model.DataProductUserSharing{}).Error
}
