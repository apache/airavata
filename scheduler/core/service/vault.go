package services

import (
	"context"
	"fmt"
	"time"

	"github.com/apache/airavata/scheduler/core/domain"
	ports "github.com/apache/airavata/scheduler/core/port"
)

// VaultService implements the CredentialVault interface
type VaultService struct {
	vault    ports.VaultPort         // OpenBao
	authz    ports.AuthorizationPort // SpiceDB
	security ports.SecurityPort      // Encryption (for data before OpenBao)
	events   ports.EventPort
}

// Compile-time interface verification
var _ domain.CredentialVault = (*VaultService)(nil)

// NewVaultService creates a new CredentialVault service
func NewVaultService(vault ports.VaultPort, authz ports.AuthorizationPort, security ports.SecurityPort, events ports.EventPort) *VaultService {
	return &VaultService{
		vault:    vault,
		authz:    authz,
		security: security,
		events:   events,
	}
}

// StoreCredential implements domain.CredentialVault.StoreCredential
func (s *VaultService) StoreCredential(ctx context.Context, name string, credentialType domain.CredentialType, data []byte, ownerID string) (*domain.Credential, error) {
	// Validate inputs
	if name == "" {
		name = fmt.Sprintf("credential_%d", time.Now().UnixNano())
	}
	if credentialType == "" {
		return nil, domain.ErrInvalidCredentialType
	}
	if len(data) == 0 {
		return nil, fmt.Errorf("missing required parameter: credential_data")
	}
	if ownerID == "" {
		return nil, fmt.Errorf("missing required parameter: owner_id")
	}

	// Generate credential ID
	credentialID := s.generateCredentialID(name)

	// Encrypt the credential data
	encryptedData, err := s.security.Encrypt(ctx, data, "default-key")
	if err != nil {
		return nil, fmt.Errorf("failed to encrypt credential: %w", err)
	}

	// Prepare data for OpenBao
	vaultData := map[string]interface{}{
		"name":              name,
		"type":              string(credentialType),
		"owner_id":          ownerID,
		"encrypted_data":    string(encryptedData),
		"encryption_key_id": "default-key",
		"created_at":        time.Now().Format(time.RFC3339),
		"updated_at":        time.Now().Format(time.RFC3339),
		"metadata":          make(map[string]interface{}),
	}

	// Store in OpenBao
	if err := s.vault.StoreCredential(ctx, credentialID, vaultData); err != nil {
		return nil, fmt.Errorf("failed to store credential in vault: %w", err)
	}

	// Create owner relation in SpiceDB
	if err := s.authz.CreateCredentialOwner(ctx, credentialID, ownerID); err != nil {
		// Clean up from vault if SpiceDB fails
		s.vault.DeleteCredential(ctx, credentialID)
		return nil, fmt.Errorf("failed to create credential owner relation: %w", err)
	}

	// Create credential object for return
	credential := &domain.Credential{
		ID:        credentialID,
		Name:      name,
		Type:      credentialType,
		OwnerID:   ownerID,
		CreatedAt: time.Now(),
		UpdatedAt: time.Now(),
		Metadata:  make(map[string]interface{}),
	}

	// Publish event
	event := domain.NewAuditEvent(ownerID, "credential.created", "credential", credentialID)
	if err := s.events.Publish(ctx, event); err != nil {
		// Failed to publish credential created event
	}

	return credential, nil
}

// RetrieveCredential implements domain.CredentialVault.RetrieveCredential
func (s *VaultService) RetrieveCredential(ctx context.Context, credentialID string, userID string) (*domain.Credential, []byte, error) {
	// Check permission via SpiceDB
	hasAccess, err := s.authz.CheckPermission(ctx, userID, credentialID, "credential", "read")
	if err != nil {
		return nil, nil, fmt.Errorf("failed to check permission: %w", err)
	}
	if !hasAccess {
		return nil, nil, domain.ErrCredentialAccessDenied
	}

	// Get credential from OpenBao
	vaultData, err := s.vault.RetrieveCredential(ctx, credentialID)
	if err != nil {
		return nil, nil, fmt.Errorf("credential not found: %w", err)
	}

	// Extract encrypted data
	encryptedDataStr, ok := vaultData["encrypted_data"].(string)
	if !ok {
		return nil, nil, fmt.Errorf("invalid credential data format")
	}

	// Decrypt the credential data
	decryptedData, err := s.security.Decrypt(ctx, []byte(encryptedDataStr), "default-key")
	if err != nil {
		return nil, nil, fmt.Errorf("failed to decrypt credential: %w", err)
	}

	// Create credential object from vault data
	credential := s.createCredentialFromVaultData(credentialID, vaultData)

	// Publish event
	event := domain.NewAuditEvent(userID, "credential.accessed", "credential", credentialID)
	if err := s.events.Publish(ctx, event); err != nil {
		// Failed to publish credential accessed event
	}

	return credential, decryptedData, nil
}

// UpdateCredential implements domain.CredentialVault.UpdateCredential
func (s *VaultService) UpdateCredential(ctx context.Context, credentialID string, data []byte, userID string) (*domain.Credential, error) {
	// Check write permission via SpiceDB
	hasAccess, err := s.authz.CheckPermission(ctx, userID, credentialID, "credential", "write")
	if err != nil {
		return nil, fmt.Errorf("failed to check permission: %w", err)
	}
	if !hasAccess {
		return nil, domain.ErrCredentialAccessDenied
	}

	// Get existing credential from OpenBao
	vaultData, err := s.vault.RetrieveCredential(ctx, credentialID)
	if err != nil {
		return nil, fmt.Errorf("credential not found: %w", err)
	}

	// Encrypt the new data
	encryptedData, err := s.security.Encrypt(ctx, data, "default-key")
	if err != nil {
		return nil, fmt.Errorf("failed to encrypt credential: %w", err)
	}

	// Update vault data
	vaultData["encrypted_data"] = string(encryptedData)
	vaultData["updated_at"] = time.Now().Format(time.RFC3339)

	// Update in OpenBao
	if err := s.vault.UpdateCredential(ctx, credentialID, vaultData); err != nil {
		return nil, fmt.Errorf("failed to update credential in vault: %w", err)
	}

	// Create credential object for return
	credential := s.createCredentialFromVaultData(credentialID, vaultData)

	// Publish event
	event := domain.NewAuditEvent(userID, "credential.updated", "credential", credentialID)
	if err := s.events.Publish(ctx, event); err != nil {
		// Failed to publish credential updated event
	}

	return credential, nil
}

// DeleteCredential implements domain.CredentialVault.DeleteCredential
func (s *VaultService) DeleteCredential(ctx context.Context, credentialID string, userID string) error {
	// Check delete permission via SpiceDB
	hasAccess, err := s.authz.CheckPermission(ctx, userID, credentialID, "credential", "delete")
	if err != nil {
		return fmt.Errorf("failed to check permission: %w", err)
	}
	if !hasAccess {
		return domain.ErrCredentialAccessDenied
	}

	// Delete from OpenBao
	if err := s.vault.DeleteCredential(ctx, credentialID); err != nil {
		return fmt.Errorf("failed to delete credential from vault: %w", err)
	}

	// Note: SpiceDB relations will be cleaned up automatically when the credential object is deleted
	// or we could explicitly delete them, but it's not necessary for the current implementation

	// Publish event
	event := domain.NewAuditEvent(userID, "credential.deleted", "credential", credentialID)
	if err := s.events.Publish(ctx, event); err != nil {
		// Failed to publish credential deleted event
	}

	return nil
}

// ListCredentials implements domain.CredentialVault.ListCredentials
func (s *VaultService) ListCredentials(ctx context.Context, userID string) ([]*domain.Credential, error) {
	// Query SpiceDB for accessible credential IDs
	credentialIDs, err := s.authz.ListAccessibleCredentials(ctx, userID, "read")
	if err != nil {
		return nil, fmt.Errorf("failed to list accessible credentials: %w", err)
	}

	// Fetch metadata from OpenBao for each credential
	var credentials []*domain.Credential
	for _, credentialID := range credentialIDs {
		vaultData, err := s.vault.RetrieveCredential(ctx, credentialID)
		if err != nil {
			// Skip credentials that can't be retrieved
			// Failed to retrieve credential
			continue
		}

		credential := s.createCredentialFromVaultData(credentialID, vaultData)
		credentials = append(credentials, credential)
	}

	return credentials, nil
}

// ShareCredential implements domain.CredentialVault.ShareCredential
func (s *VaultService) ShareCredential(ctx context.Context, credentialID string, targetUserID, targetGroupID string, permissions string, userID string) error {
	// Check if user owns credential (can share)
	hasAccess, err := s.authz.CheckPermission(ctx, userID, credentialID, "credential", "delete") // Only owner can share
	if err != nil {
		return fmt.Errorf("failed to check permission: %w", err)
	}
	if !hasAccess {
		return domain.ErrCredentialAccessDenied
	}

	// Determine target principal
	var principalID, principalType string
	if targetUserID != "" {
		principalID = targetUserID
		principalType = "user"
	} else if targetGroupID != "" {
		principalID = targetGroupID
		principalType = "group"
	} else {
		return fmt.Errorf("either targetUserID or targetGroupID must be provided")
	}

	// Share credential via SpiceDB
	if err := s.authz.ShareCredential(ctx, credentialID, principalID, principalType, permissions); err != nil {
		return fmt.Errorf("failed to share credential: %w", err)
	}

	// Publish event
	event := domain.NewAuditEvent(userID, "credential.shared", "credential", credentialID)
	if err := s.events.Publish(ctx, event); err != nil {
		// Failed to publish credential shared event
	}

	return nil
}

// RevokeCredentialAccess implements domain.CredentialVault.RevokeCredentialAccess
func (s *VaultService) RevokeCredentialAccess(ctx context.Context, credentialID string, targetUserID, targetGroupID string, userID string) error {
	// Check if user owns credential (can revoke)
	hasAccess, err := s.authz.CheckPermission(ctx, userID, credentialID, "credential", "delete") // Only owner can revoke
	if err != nil {
		return fmt.Errorf("failed to check permission: %w", err)
	}
	if !hasAccess {
		return domain.ErrCredentialAccessDenied
	}

	// Determine target principal
	var principalID, principalType string
	if targetUserID != "" {
		principalID = targetUserID
		principalType = "user"
	} else if targetGroupID != "" {
		principalID = targetGroupID
		principalType = "group"
	} else {
		return fmt.Errorf("either targetUserID or targetGroupID must be provided")
	}

	// Revoke access via SpiceDB
	if err := s.authz.RevokeCredentialAccess(ctx, credentialID, principalID, principalType); err != nil {
		return fmt.Errorf("failed to revoke credential access: %w", err)
	}

	// Publish event
	event := domain.NewAuditEvent(userID, "credential.access_revoked", "credential", credentialID)
	if err := s.events.Publish(ctx, event); err != nil {
		// Failed to publish credential access revoked event
	}

	return nil
}

// RotateCredential implements domain.CredentialVault.RotateCredential
func (s *VaultService) RotateCredential(ctx context.Context, credentialID string, userID string) error {
	// Check if user owns credential (can rotate)
	hasAccess, err := s.authz.CheckPermission(ctx, userID, credentialID, "credential", "delete") // Only owner can rotate
	if err != nil {
		return fmt.Errorf("failed to check permission: %w", err)
	}
	if !hasAccess {
		return domain.ErrCredentialAccessDenied
	}

	// Get existing credential from OpenBao
	vaultData, err := s.vault.RetrieveCredential(ctx, credentialID)
	if err != nil {
		return fmt.Errorf("credential not found: %w", err)
	}

	// Generate new encryption key
	newKeyID := fmt.Sprintf("key_%d", time.Now().UnixNano())
	if err := s.security.GenerateKey(ctx, newKeyID); err != nil {
		return fmt.Errorf("failed to generate new key: %w", err)
	}

	// Decrypt with old key
	encryptedDataStr, ok := vaultData["encrypted_data"].(string)
	if !ok {
		return fmt.Errorf("invalid credential data format")
	}

	decryptedData, err := s.security.Decrypt(ctx, []byte(encryptedDataStr), "default-key")
	if err != nil {
		return fmt.Errorf("failed to decrypt with old key: %w", err)
	}

	// Encrypt with new key
	encryptedData, err := s.security.Encrypt(ctx, decryptedData, newKeyID)
	if err != nil {
		return fmt.Errorf("failed to encrypt with new key: %w", err)
	}

	// Update vault data
	vaultData["encrypted_data"] = string(encryptedData)
	vaultData["encryption_key_id"] = newKeyID
	vaultData["updated_at"] = time.Now().Format(time.RFC3339)

	// Update in OpenBao
	if err := s.vault.UpdateCredential(ctx, credentialID, vaultData); err != nil {
		return fmt.Errorf("failed to update credential in vault: %w", err)
	}

	// Publish event
	event := domain.NewAuditEvent(userID, "credential.rotated", "credential", credentialID)
	if err := s.events.Publish(ctx, event); err != nil {
		// Failed to publish credential rotated event
	}

	return nil
}

// GetUsableCredentialForResource implements domain.CredentialVault.GetUsableCredentialForResource
func (s *VaultService) GetUsableCredentialForResource(ctx context.Context, resourceID, resourceType, userID string, metadata map[string]interface{}) (*domain.Credential, []byte, error) {
	// Query SpiceDB for credentials bound to resource with user access
	credentialIDs, err := s.authz.GetUsableCredentialsForResource(ctx, userID, resourceID, resourceType, "read")
	if err != nil {
		return nil, nil, fmt.Errorf("failed to get usable credentials for resource: %w", err)
	}

	if len(credentialIDs) == 0 {
		return nil, nil, domain.ErrCredentialNotFound
	}

	// Use the first available credential
	credentialID := credentialIDs[0]

	// Get credential from OpenBao
	vaultData, err := s.vault.RetrieveCredential(ctx, credentialID)
	if err != nil {
		return nil, nil, fmt.Errorf("failed to retrieve credential: %w", err)
	}

	// Extract encrypted data
	encryptedDataStr, ok := vaultData["encrypted_data"].(string)
	if !ok {
		return nil, nil, fmt.Errorf("invalid credential data format")
	}

	// Decrypt the credential data
	decryptedData, err := s.security.Decrypt(ctx, []byte(encryptedDataStr), "default-key")
	if err != nil {
		return nil, nil, fmt.Errorf("failed to decrypt credential: %w", err)
	}

	// Create credential object from vault data
	credential := s.createCredentialFromVaultData(credentialID, vaultData)

	// Publish event
	event := domain.NewAuditEvent(userID, "credential.used_for_resource", "credential", credentialID)
	if err := s.events.Publish(ctx, event); err != nil {
		// Failed to publish credential used event
	}

	return credential, decryptedData, nil
}

// Helper methods

func (s *VaultService) generateCredentialID(name string) string {
	timestamp := time.Now().UnixNano()
	return fmt.Sprintf("cred_%s_%d", name, timestamp)
}

// createCredentialFromVaultData creates a domain.Credential from OpenBao data
func (s *VaultService) createCredentialFromVaultData(credentialID string, vaultData map[string]interface{}) *domain.Credential {
	credential := &domain.Credential{
		ID:       credentialID,
		Metadata: make(map[string]interface{}),
	}

	if name, ok := vaultData["name"].(string); ok {
		credential.Name = name
	}
	if typeStr, ok := vaultData["type"].(string); ok {
		credential.Type = domain.CredentialType(typeStr)
	}
	if ownerID, ok := vaultData["owner_id"].(string); ok {
		credential.OwnerID = ownerID
	}
	if createdAtStr, ok := vaultData["created_at"].(string); ok {
		if createdAt, err := time.Parse(time.RFC3339, createdAtStr); err == nil {
			credential.CreatedAt = createdAt
		}
	}
	if updatedAtStr, ok := vaultData["updated_at"].(string); ok {
		if updatedAt, err := time.Parse(time.RFC3339, updatedAtStr); err == nil {
			credential.UpdatedAt = updatedAt
		}
	}
	if metadata, ok := vaultData["metadata"].(map[string]interface{}); ok {
		credential.Metadata = metadata
	}

	return credential
}

// GetVaultPort returns the VaultPort for testing purposes
func (s *VaultService) GetVaultPort() ports.VaultPort {
	return s.vault
}

// GetAuthzPort returns the AuthorizationPort for testing purposes
func (s *VaultService) GetAuthzPort() ports.AuthorizationPort {
	return s.authz
}

// CheckPermission checks if a user has a specific permission on an object
func (s *VaultService) CheckPermission(ctx context.Context, userID, objectID, objectType, permission string) (bool, error) {
	return s.authz.CheckPermission(ctx, userID, objectID, objectType, permission)
}

// GetUsableCredentialsForResource returns credentials bound to a resource that the user can access
func (s *VaultService) GetUsableCredentialsForResource(ctx context.Context, userID, resourceID, resourceType, permission string) ([]string, error) {
	return s.authz.GetUsableCredentialsForResource(ctx, userID, resourceID, resourceType, permission)
}

// BindCredentialToResource binds a credential to a resource using SpiceDB
func (s *VaultService) BindCredentialToResource(ctx context.Context, credentialID, resourceID, resourceType string) error {
	return s.authz.BindCredentialToResource(ctx, credentialID, resourceID, resourceType)
}
