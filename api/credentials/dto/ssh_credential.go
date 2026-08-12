package dto

import (
	"github.com/apache/airavata/internal/httpx"

	model "github.com/apache/airavata/api/credentials/model"
)

// SSHKeyRequest is the create/update payload for a key.
//
// PrivateKey is optional on the type because the same payload serves both create and
// update: creation requires it, while an update that omits it keeps the stored secret.
// That split cannot be expressed as a field constraint, so the service enforces it.
//
// Java: org.apache.airavata.credentials.dto.SSHKeyRequestDto
type SSHKeyRequest struct {
	SSHKeyName string  `json:"sshKeyName"`
	PublicKey  string  `json:"publicKey"`
	PrivateKey *string `json:"privateKey"`
	Passphrase *string `json:"passphrase"`
}

// Validate implements httpx.Validator.
func (r *SSHKeyRequest) Validate() []httpx.FieldError {
	var c httpx.Constraints
	c.NotBlank("sshKeyName", "SSH key name cannot be blank", r.SSHKeyName)
	c.NotBlank("publicKey", "Public key cannot be blank", r.PublicKey)
	return c.Fields()
}

// SSHKeyResponse is the read model for a key.
//
// It has no private key or passphrase field at all. That is the containment: the
// secrets cannot leak through this endpoint because there is nowhere for them to go.
//
// Java: org.apache.airavata.credentials.dto.SSHKeyResponseDto
type SSHKeyResponse struct {
	SSHKeyID   string `json:"sshKeyId"`
	SSHKeyName string `json:"sshKeyName"`
	PublicKey  string `json:"publicKey"`
}

func ToSSHKeyResponse(k *model.SSHKey) SSHKeyResponse {
	return SSHKeyResponse{
		SSHKeyID:   k.ID,
		SSHKeyName: k.SSHKeyName,
		PublicKey:  k.PublicKey,
	}
}

// SSHUserCredentialRequest is the create/update payload for a credential.
//
// Java: org.apache.airavata.credentials.dto.SSHUserCredentialRequestDto
type SSHUserCredentialRequest struct {
	Username string `json:"username"`
	SSHKeyID string `json:"sshKeyId"`
}

// Validate implements httpx.Validator.
func (r *SSHUserCredentialRequest) Validate() []httpx.FieldError {
	var c httpx.Constraints
	c.NotBlank("username", "Username cannot be blank", r.Username)
	c.NotBlank("sshKeyId", "SSH key id cannot be blank", r.SSHKeyID)
	return c.Fields()
}

// SSHUserCredentialResponse is the read model for a credential, nesting the safe
// summary of the key it uses.
//
// Java: org.apache.airavata.credentials.dto.SSHUserCredentialResponseDto
type SSHUserCredentialResponse struct {
	SSHCredentialID string          `json:"sshCredentialId"`
	Username        string          `json:"username"`
	SSHKey          *SSHKeyResponse `json:"sshKey"`
}

func ToSSHUserCredentialResponse(c *model.SSHUserCredential) SSHUserCredentialResponse {
	out := SSHUserCredentialResponse{
		SSHCredentialID: c.ID,
		Username:        c.Username,
	}
	if c.SSHKey != nil {
		key := ToSSHKeyResponse(c.SSHKey)
		out.SSHKey = &key
	}
	return out
}
