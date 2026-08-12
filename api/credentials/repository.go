package credentials

import (
	"context"

	"gorm.io/gorm"

	model "github.com/apache/airavata/api/credentials/model"
)

// SSHKeyRepository reads and writes registered SSH keypairs.
type SSHKeyRepository struct{ db *gorm.DB }

// NewSSHKeyRepository returns a repository backed by db.
func NewSSHKeyRepository(db *gorm.DB) *SSHKeyRepository { return &SSHKeyRepository{db: db} }

// WithTx returns a repository bound to tx.
func (r *SSHKeyRepository) WithTx(tx *gorm.DB) *SSHKeyRepository { return &SSHKeyRepository{db: tx} }

// FindAll returns every key.
func (r *SSHKeyRepository) FindAll(ctx context.Context) ([]model.SSHKey, error) {
	var out []model.SSHKey
	err := r.db.WithContext(ctx).Find(&out).Error
	return out, err
}

// FindByID returns one key, or gorm.ErrRecordNotFound.
func (r *SSHKeyRepository) FindByID(ctx context.Context, id string) (*model.SSHKey, error) {
	var out model.SSHKey
	if err := r.db.WithContext(ctx).First(&out, "ssh_key_id = ?", id).Error; err != nil {
		return nil, err
	}
	return &out, nil
}

// Save inserts or updates a key.
func (r *SSHKeyRepository) Save(ctx context.Context, k *model.SSHKey) error {
	return r.db.WithContext(ctx).Save(k).Error
}

// Delete removes a key.
func (r *SSHKeyRepository) Delete(ctx context.Context, k *model.SSHKey) error {
	return r.db.WithContext(ctx).Delete(k).Error
}

// ExistsByName reports whether a key of that name is already registered. Declared but
// never called in the Java service, and preserved here for the same reason.
func (r *SSHKeyRepository) ExistsByName(ctx context.Context, name string) (bool, error) {
	var n int64
	err := r.db.WithContext(ctx).Model(&model.SSHKey{}).Where("ssh_key_name = ?", name).Count(&n).Error
	return n > 0, err
}

// SSHUserCredentialRepository reads and writes username-to-key credentials.
type SSHUserCredentialRepository struct{ db *gorm.DB }

// NewSSHUserCredentialRepository returns a repository backed by db.
func NewSSHUserCredentialRepository(db *gorm.DB) *SSHUserCredentialRepository {
	return &SSHUserCredentialRepository{db: db}
}

// WithTx returns a repository bound to tx.
func (r *SSHUserCredentialRepository) WithTx(tx *gorm.DB) *SSHUserCredentialRepository {
	return &SSHUserCredentialRepository{db: tx}
}

// FindAll returns every credential with its key preloaded, since the response nests
// the key summary.
func (r *SSHUserCredentialRepository) FindAll(ctx context.Context) ([]model.SSHUserCredential, error) {
	var out []model.SSHUserCredential
	err := r.db.WithContext(ctx).Preload("SSHKey").Find(&out).Error
	return out, err
}

// FindByKeyID returns every credential backed by one key.
func (r *SSHUserCredentialRepository) FindByKeyID(ctx context.Context, keyID string) ([]model.SSHUserCredential, error) {
	var out []model.SSHUserCredential
	err := r.db.WithContext(ctx).Preload("SSHKey").Where("ssh_key_id = ?", keyID).Find(&out).Error
	return out, err
}

// FindByID returns one credential, or gorm.ErrRecordNotFound.
func (r *SSHUserCredentialRepository) FindByID(ctx context.Context, id string) (*model.SSHUserCredential, error) {
	var out model.SSHUserCredential
	if err := r.db.WithContext(ctx).Preload("SSHKey").
		First(&out, "ssh_credential_id = ?", id).Error; err != nil {
		return nil, err
	}
	return &out, nil
}

// ExistsByKeyID reports whether any credential still references a key. This is what
// makes key deletion fail with a conflict rather than orphaning credentials — there
// is no cascade behind it, only this check.
func (r *SSHUserCredentialRepository) ExistsByKeyID(ctx context.Context, keyID string) (bool, error) {
	var n int64
	err := r.db.WithContext(ctx).Model(&model.SSHUserCredential{}).
		Where("ssh_key_id = ?", keyID).Count(&n).Error
	return n > 0, err
}

// Save inserts or updates a credential.
func (r *SSHUserCredentialRepository) Save(ctx context.Context, c *model.SSHUserCredential) error {
	return r.db.WithContext(ctx).Save(c).Error
}

// Delete removes a credential. The key it referenced is left alone.
func (r *SSHUserCredentialRepository) Delete(ctx context.Context, c *model.SSHUserCredential) error {
	return r.db.WithContext(ctx).Delete(c).Error
}
