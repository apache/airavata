package ports

import (
	"context"
)

// VaultPort defines the interface for secure credential storage
type VaultPort interface {
	// StoreCredential stores encrypted credential data in the vault
	StoreCredential(ctx context.Context, id string, data map[string]interface{}) error

	// RetrieveCredential retrieves credential data from the vault
	RetrieveCredential(ctx context.Context, id string) (map[string]interface{}, error)

	// DeleteCredential removes credential data from the vault
	DeleteCredential(ctx context.Context, id string) error

	// UpdateCredential updates existing credential data in the vault
	UpdateCredential(ctx context.Context, id string, data map[string]interface{}) error

	// ListCredentials returns a list of all credential IDs in the vault
	ListCredentials(ctx context.Context) ([]string, error)
}
