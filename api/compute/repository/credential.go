package compute

import (
	"context"

	"gorm.io/gorm"

	model "github.com/apache/airavata/api/compute/model"
	iammodel "github.com/apache/airavata/api/iam/model"
)

// SSHEndpointCredentialRepository reads and writes the user-to-endpoint credential
// bindings.
type SSHEndpointCredentialRepository struct{ db *gorm.DB }

// NewSSHEndpointCredentialRepository returns a repository backed by db.
func NewSSHEndpointCredentialRepository(db *gorm.DB) *SSHEndpointCredentialRepository {
	return &SSHEndpointCredentialRepository{db: db}
}

// WithTx returns a repository bound to tx.
func (r *SSHEndpointCredentialRepository) WithTx(tx *gorm.DB) *SSHEndpointCredentialRepository {
	return &SSHEndpointCredentialRepository{db: tx}
}

// FindAll returns every binding across every user.
func (r *SSHEndpointCredentialRepository) FindAll(ctx context.Context) ([]model.SSHEndpointCredential, error) {
	var out []model.SSHEndpointCredential
	err := r.db.WithContext(ctx).Find(&out).Error
	return out, err
}

// FindBySSHEndpointID returns every binding for one endpoint.
func (r *SSHEndpointCredentialRepository) FindBySSHEndpointID(ctx context.Context, endpointID string) ([]model.SSHEndpointCredential, error) {
	var out []model.SSHEndpointCredential
	err := r.db.WithContext(ctx).Where("ssh_endpoint_id = ?", endpointID).Find(&out).Error
	return out, err
}

// FindByOwnerID returns every binding owned by one user.
func (r *SSHEndpointCredentialRepository) FindByOwnerID(ctx context.Context, userID string) ([]model.SSHEndpointCredential, error) {
	var out []model.SSHEndpointCredential
	err := r.db.WithContext(ctx).Where("user_id = ?", userID).Find(&out).Error
	return out, err
}

// FindByOwnerIDAndSSHEndpointID returns one user's bindings for one endpoint.
func (r *SSHEndpointCredentialRepository) FindByOwnerIDAndSSHEndpointID(ctx context.Context, userID, endpointID string) ([]model.SSHEndpointCredential, error) {
	var out []model.SSHEndpointCredential
	err := r.db.WithContext(ctx).
		Where("user_id = ? AND ssh_endpoint_id = ?", userID, endpointID).Find(&out).Error
	return out, err
}

// FindSharedWith returns every binding reaching userID through a share: named
// directly, or through a group they are an active member of.
//
// Bindings they own are deliberately excluded — ownership is not a share, and the
// caller asking "what has been shared with me?" already has /me for their own.
func (r *SSHEndpointCredentialRepository) FindSharedWith(ctx context.Context, userID string) ([]model.SSHEndpointCredential, error) {
	sharedDirectly := r.db.Model(&model.SSHEndpointCredentialUserSharing{}).
		Select("ssh_endpoint_credential_id").
		Where("user_id = ?", userID)

	// Group shares reach the caller only through an ACTIVE membership: a suspended
	// member keeps their place in the group without keeping access through it.
	sharedByGroup := r.db.Model(&model.SSHEndpointCredentialGroupSharing{}).
		Select("ssh_endpoint_credential_id").
		Where("group_id IN (?)", r.db.Model(&iammodel.GroupMember{}).
			Select("group_id").
			Where("user_id = ? AND group_member_status = ?", userID, iammodel.GroupMemberStatusActive))

	var out []model.SSHEndpointCredential
	err := r.db.WithContext(ctx).
		Where("(ssh_endpoint_credential_id IN (?) OR ssh_endpoint_credential_id IN (?)) AND (user_id IS NULL OR user_id <> ?)",
			sharedDirectly, sharedByGroup, userID).
		Find(&out).Error
	return out, err
}

// FindByID returns one binding, or gorm.ErrRecordNotFound.
func (r *SSHEndpointCredentialRepository) FindByID(ctx context.Context, id string) (*model.SSHEndpointCredential, error) {
	var out model.SSHEndpointCredential
	if err := r.db.WithContext(ctx).First(&out, "ssh_endpoint_credential_id = ?", id).Error; err != nil {
		return nil, err
	}
	return &out, nil
}

// Save inserts or updates a binding.
func (r *SSHEndpointCredentialRepository) Save(ctx context.Context, c *model.SSHEndpointCredential) error {
	return r.db.WithContext(ctx).Save(c).Error
}

// Delete removes a binding.
func (r *SSHEndpointCredentialRepository) Delete(ctx context.Context, c *model.SSHEndpointCredential) error {
	return r.db.WithContext(ctx).Delete(c).Error
}

// SSHEndpointCredentialSharingRepository reads and writes both kinds of share.
//
// They are one repository rather than two because every caller needs them together:
// resolving what a principal may do with a credential means asking both tables, and
// splitting that across two dependencies would only spread the same question wider.
type SSHEndpointCredentialSharingRepository struct{ db *gorm.DB }

// NewSSHEndpointCredentialSharingRepository returns a repository backed by db.
func NewSSHEndpointCredentialSharingRepository(db *gorm.DB) *SSHEndpointCredentialSharingRepository {
	return &SSHEndpointCredentialSharingRepository{db: db}
}

// WithTx returns a repository bound to tx.
func (r *SSHEndpointCredentialSharingRepository) WithTx(tx *gorm.DB) *SSHEndpointCredentialSharingRepository {
	return &SSHEndpointCredentialSharingRepository{db: tx}
}

// FindGroupSharesByCredentialID returns every group share of one credential.
func (r *SSHEndpointCredentialSharingRepository) FindGroupSharesByCredentialID(ctx context.Context, credentialID string) ([]model.SSHEndpointCredentialGroupSharing, error) {
	var out []model.SSHEndpointCredentialGroupSharing
	err := r.db.WithContext(ctx).
		Where("ssh_endpoint_credential_id = ?", credentialID).Find(&out).Error
	return out, err
}

// FindGroupShare returns one group share scoped to its credential, or
// gorm.ErrRecordNotFound.
//
// Scoping by credential is deliberate: it stops a sharing id belonging to one
// credential from being reached through another credential's path.
func (r *SSHEndpointCredentialSharingRepository) FindGroupShare(ctx context.Context, credentialID, sharingID string) (*model.SSHEndpointCredentialGroupSharing, error) {
	var out model.SSHEndpointCredentialGroupSharing
	err := r.db.WithContext(ctx).First(&out,
		"ssh_endpoint_credential_group_sharing_id = ? AND ssh_endpoint_credential_id = ?",
		sharingID, credentialID).Error
	if err != nil {
		return nil, err
	}
	return &out, nil
}

// FindGroupShareByGroupID returns the share of one credential with one group, or
// gorm.ErrRecordNotFound. It is what makes a duplicate share reportable as a conflict.
func (r *SSHEndpointCredentialSharingRepository) FindGroupShareByGroupID(ctx context.Context, credentialID, groupID string) (*model.SSHEndpointCredentialGroupSharing, error) {
	var out model.SSHEndpointCredentialGroupSharing
	err := r.db.WithContext(ctx).First(&out,
		"ssh_endpoint_credential_id = ? AND group_id = ?", credentialID, groupID).Error
	if err != nil {
		return nil, err
	}
	return &out, nil
}

// SaveGroupShare inserts or updates a group share.
func (r *SSHEndpointCredentialSharingRepository) SaveGroupShare(ctx context.Context, s *model.SSHEndpointCredentialGroupSharing) error {
	return r.db.WithContext(ctx).Save(s).Error
}

// DeleteGroupShare removes a group share.
func (r *SSHEndpointCredentialSharingRepository) DeleteGroupShare(ctx context.Context, s *model.SSHEndpointCredentialGroupSharing) error {
	return r.db.WithContext(ctx).Delete(s).Error
}

// FindUserSharesByCredentialID returns every user share of one credential.
func (r *SSHEndpointCredentialSharingRepository) FindUserSharesByCredentialID(ctx context.Context, credentialID string) ([]model.SSHEndpointCredentialUserSharing, error) {
	var out []model.SSHEndpointCredentialUserSharing
	err := r.db.WithContext(ctx).
		Where("ssh_endpoint_credential_id = ?", credentialID).Find(&out).Error
	return out, err
}

// FindUserShare returns one user share scoped to its credential, or
// gorm.ErrRecordNotFound.
func (r *SSHEndpointCredentialSharingRepository) FindUserShare(ctx context.Context, credentialID, sharingID string) (*model.SSHEndpointCredentialUserSharing, error) {
	var out model.SSHEndpointCredentialUserSharing
	err := r.db.WithContext(ctx).First(&out,
		"ssh_endpoint_credential_user_sharing_id = ? AND ssh_endpoint_credential_id = ?",
		sharingID, credentialID).Error
	if err != nil {
		return nil, err
	}
	return &out, nil
}

// FindUserShareByUserID returns the share of one credential with one user, or
// gorm.ErrRecordNotFound.
func (r *SSHEndpointCredentialSharingRepository) FindUserShareByUserID(ctx context.Context, credentialID, userID string) (*model.SSHEndpointCredentialUserSharing, error) {
	var out model.SSHEndpointCredentialUserSharing
	err := r.db.WithContext(ctx).First(&out,
		"ssh_endpoint_credential_id = ? AND user_id = ?", credentialID, userID).Error
	if err != nil {
		return nil, err
	}
	return &out, nil
}

// SaveUserShare inserts or updates a user share.
func (r *SSHEndpointCredentialSharingRepository) SaveUserShare(ctx context.Context, s *model.SSHEndpointCredentialUserSharing) error {
	return r.db.WithContext(ctx).Save(s).Error
}

// DeleteUserShare removes a user share.
func (r *SSHEndpointCredentialSharingRepository) DeleteUserShare(ctx context.Context, s *model.SSHEndpointCredentialUserSharing) error {
	return r.db.WithContext(ctx).Delete(s).Error
}

// DeleteByCredentialID removes every share of one credential.
//
// The foreign keys are RESTRICT, so deleting a credential that is still shared would
// otherwise fail; the service calls this first, inside the same transaction.
func (r *SSHEndpointCredentialSharingRepository) DeleteByCredentialID(ctx context.Context, credentialID string) error {
	if err := r.db.WithContext(ctx).
		Where("ssh_endpoint_credential_id = ?", credentialID).
		Delete(&model.SSHEndpointCredentialGroupSharing{}).Error; err != nil {
		return err
	}
	return r.db.WithContext(ctx).
		Where("ssh_endpoint_credential_id = ?", credentialID).
		Delete(&model.SSHEndpointCredentialUserSharing{}).Error
}
