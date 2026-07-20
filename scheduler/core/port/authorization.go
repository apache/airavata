package ports

import (
	"context"
)

// ResourceBinding represents a credential bound to a resource
type ResourceBinding struct {
	ResourceID   string
	ResourceType string
}

// AuthorizationPort defines the interface for authorization and relationship management
type AuthorizationPort interface {
	// Permission checks
	CheckPermission(ctx context.Context, userID, objectID, objectType, permission string) (bool, error)

	// Credential relations
	CreateCredentialOwner(ctx context.Context, credentialID, ownerID string) error
	ShareCredential(ctx context.Context, credentialID, principalID, principalType, permission string) error
	RevokeCredentialAccess(ctx context.Context, credentialID, principalID, principalType string) error
	ListAccessibleCredentials(ctx context.Context, userID, permission string) ([]string, error)
	GetCredentialOwner(ctx context.Context, credentialID string) (string, error)
	ListCredentialReaders(ctx context.Context, credentialID string) ([]string, error)
	ListCredentialWriters(ctx context.Context, credentialID string) ([]string, error)

	// Group relations
	AddUserToGroup(ctx context.Context, userID, groupID string) error
	RemoveUserFromGroup(ctx context.Context, userID, groupID string) error
	AddGroupToGroup(ctx context.Context, childGroupID, parentGroupID string) error
	RemoveGroupFromGroup(ctx context.Context, childGroupID, parentGroupID string) error
	GetUserGroups(ctx context.Context, userID string) ([]string, error)
	GetGroupMembers(ctx context.Context, groupID string) ([]string, error)

	// Resource bindings
	BindCredentialToResource(ctx context.Context, credentialID, resourceID, resourceType string) error
	UnbindCredentialFromResource(ctx context.Context, credentialID, resourceID, resourceType string) error
	GetResourceCredentials(ctx context.Context, resourceID, resourceType string) ([]string, error)
	GetCredentialResources(ctx context.Context, credentialID string) ([]ResourceBinding, error)

	// Combined queries
	GetUsableCredentialsForResource(ctx context.Context, userID, resourceID, resourceType, permission string) ([]string, error)
}
