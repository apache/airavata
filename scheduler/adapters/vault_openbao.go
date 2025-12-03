package adapters

import (
	"context"
	"fmt"
	"strings"

	ports "github.com/apache/airavata/scheduler/core/port"
	"github.com/hashicorp/vault/api"
)

// OpenBaoAdapter implements the VaultPort interface using OpenBao
type OpenBaoAdapter struct {
	client    *api.Client
	mountPath string
}

// NewOpenBaoAdapter creates a new OpenBao adapter
func NewOpenBaoAdapter(client *api.Client, mountPath string) *OpenBaoAdapter {
	return &OpenBaoAdapter{
		client:    client,
		mountPath: mountPath,
	}
}

// StoreCredential stores encrypted credential data in OpenBao
func (o *OpenBaoAdapter) StoreCredential(ctx context.Context, id string, data map[string]interface{}) error {
	path := o.getCredentialPath(id)

	_, err := o.client.KVv2(o.mountPath).Put(ctx, path, data)
	if err != nil {
		return fmt.Errorf("failed to store credential %s: %w", id, err)
	}

	return nil
}

// RetrieveCredential retrieves credential data from OpenBao
func (o *OpenBaoAdapter) RetrieveCredential(ctx context.Context, id string) (map[string]interface{}, error) {
	path := o.getCredentialPath(id)

	secret, err := o.client.KVv2(o.mountPath).Get(ctx, path)
	if err != nil {
		return nil, fmt.Errorf("failed to retrieve credential %s: %w", id, err)
	}

	if secret == nil || secret.Data == nil {
		return nil, fmt.Errorf("credential %s not found", id)
	}

	return secret.Data, nil
}

// DeleteCredential removes credential data from OpenBao
func (o *OpenBaoAdapter) DeleteCredential(ctx context.Context, id string) error {
	path := o.getCredentialPath(id)

	err := o.client.KVv2(o.mountPath).Delete(ctx, path)
	if err != nil {
		return fmt.Errorf("failed to delete credential %s: %w", id, err)
	}

	return nil
}

// UpdateCredential updates existing credential data in OpenBao
func (o *OpenBaoAdapter) UpdateCredential(ctx context.Context, id string, data map[string]interface{}) error {
	// OpenBao KVv2 handles updates the same way as creates
	return o.StoreCredential(ctx, id, data)
}

// ListCredentials returns a list of all credential IDs in OpenBao
func (o *OpenBaoAdapter) ListCredentials(ctx context.Context) ([]string, error) {
	// Use Vault Logical API to list keys in the credentials path
	path := fmt.Sprintf("%s/metadata/credentials", o.mountPath)
	secret, err := o.client.Logical().ListWithContext(ctx, path)
	if err != nil {
		// If the path doesn't exist or there are no credentials, return empty list
		// This is not an error condition - it just means no credentials exist yet
		if isNotFoundError(err) {
			return []string{}, nil
		}
		return nil, fmt.Errorf("failed to list credentials: %w", err)
	}

	if secret == nil || secret.Data == nil {
		return []string{}, nil
	}

	// Extract keys from the response
	keys, ok := secret.Data["keys"].([]interface{})
	if !ok {
		return []string{}, nil
	}

	// Convert interface{} slice to string slice
	var credentialIDs []string
	for _, key := range keys {
		if keyStr, ok := key.(string); ok {
			// Remove trailing slash if present
			credentialID := strings.TrimSuffix(keyStr, "/")
			if credentialID != "" {
				credentialIDs = append(credentialIDs, credentialID)
			}
		}
	}

	return credentialIDs, nil
}

// getCredentialPath returns the full path for a credential in OpenBao
func (o *OpenBaoAdapter) getCredentialPath(id string) string {
	return fmt.Sprintf("credentials/%s", id)
}

// isNotFoundError checks if the error indicates that the path was not found
func isNotFoundError(err error) bool {
	if err == nil {
		return false
	}
	// Check for common "not found" error patterns in Vault API
	errStr := err.Error()
	return strings.Contains(errStr, "not found") ||
		strings.Contains(errStr, "no such file") ||
		strings.Contains(errStr, "path not found") ||
		strings.Contains(errStr, "404")
}

// Compile-time interface verification
var _ ports.VaultPort = (*OpenBaoAdapter)(nil)
