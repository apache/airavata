package testutil

import (
	"context"
	"fmt"
	"sync"

	ports "github.com/apache/airavata/scheduler/core/port"
)

// MockVaultPort implements VaultPort for testing
type MockVaultPort struct {
	credentials map[string]map[string]interface{}
	mu          sync.RWMutex
}

// NewMockVaultPort creates a new mock vault port
func NewMockVaultPort() *MockVaultPort {
	return &MockVaultPort{
		credentials: make(map[string]map[string]interface{}),
	}
}

// StoreCredential stores credential data in memory
func (m *MockVaultPort) StoreCredential(ctx context.Context, id string, data map[string]interface{}) error {
	m.mu.Lock()
	defer m.mu.Unlock()

	// Create a copy of the data to avoid external modifications
	credData := make(map[string]interface{})
	for k, v := range data {
		credData[k] = v
	}

	m.credentials[id] = credData
	return nil
}

// RetrieveCredential retrieves credential data from memory
func (m *MockVaultPort) RetrieveCredential(ctx context.Context, id string) (map[string]interface{}, error) {
	m.mu.RLock()
	defer m.mu.RUnlock()

	data, exists := m.credentials[id]
	if !exists {
		return nil, &NotFoundError{ID: id}
	}

	// Return a copy to avoid external modifications
	result := make(map[string]interface{})
	for k, v := range data {
		result[k] = v
	}

	return result, nil
}

// DeleteCredential removes credential data from memory
func (m *MockVaultPort) DeleteCredential(ctx context.Context, id string) error {
	m.mu.Lock()
	defer m.mu.Unlock()

	delete(m.credentials, id)
	return nil
}

// UpdateCredential updates existing credential data in memory
func (m *MockVaultPort) UpdateCredential(ctx context.Context, id string, data map[string]interface{}) error {
	return m.StoreCredential(ctx, id, data)
}

// ListCredentials returns all credential IDs
func (m *MockVaultPort) ListCredentials(ctx context.Context) ([]string, error) {
	m.mu.RLock()
	defer m.mu.RUnlock()

	var ids []string
	for id := range m.credentials {
		ids = append(ids, id)
	}

	return ids, nil
}

// NotFoundError represents a credential not found error
type NotFoundError struct {
	ID string
}

func (e *NotFoundError) Error() string {
	return "credential " + e.ID + " not found"
}

// MockAuthorizationPort implements AuthorizationPort for testing
type MockAuthorizationPort struct {
	// Credential ownership: credentialID -> ownerID
	credentialOwners map[string]string

	// Credential readers: credentialID -> set of user/group IDs
	credentialReaders map[string]map[string]bool

	// Credential writers: credentialID -> set of user/group IDs
	credentialWriters map[string]map[string]bool

	// Group memberships: groupID -> set of member IDs (users or groups)
	groupMembers map[string]map[string]bool

	// Resource bindings: resourceID -> set of credential IDs
	resourceCredentials map[string]map[string]bool

	mu sync.RWMutex
}

// NewMockAuthorizationPort creates a new mock authorization port
func NewMockAuthorizationPort() *MockAuthorizationPort {
	return &MockAuthorizationPort{
		credentialOwners:    make(map[string]string),
		credentialReaders:   make(map[string]map[string]bool),
		credentialWriters:   make(map[string]map[string]bool),
		groupMembers:        make(map[string]map[string]bool),
		resourceCredentials: make(map[string]map[string]bool),
	}
}

// CheckPermission checks if a user has a specific permission on an object
func (m *MockAuthorizationPort) CheckPermission(ctx context.Context, userID, objectID, objectType, permission string) (bool, error) {
	m.mu.RLock()
	defer m.mu.RUnlock()

	// For now, only handle credential objects
	if objectType != "credential" {
		return false, nil
	}

	credentialID := objectID

	// Check if user is owner
	if ownerID, exists := m.credentialOwners[credentialID]; exists && ownerID == userID {
		return true, nil
	}

	// Check direct permissions
	switch permission {
	case "read":
		if readers, exists := m.credentialReaders[credentialID]; exists && readers[userID] {
			return true, nil
		}
		if writers, exists := m.credentialWriters[credentialID]; exists && writers[userID] {
			return true, nil
		}
	case "write":
		if writers, exists := m.credentialWriters[credentialID]; exists && writers[userID] {
			return true, nil
		}
	case "delete":
		if ownerID, exists := m.credentialOwners[credentialID]; exists && ownerID == userID {
			return true, nil
		}
	}

	// Check group memberships (simplified - no recursive hierarchy for mock)
	for groupID, members := range m.groupMembers {
		if members[userID] {
			switch permission {
			case "read":
				if readers, exists := m.credentialReaders[credentialID]; exists && readers[groupID] {
					return true, nil
				}
				if writers, exists := m.credentialWriters[credentialID]; exists && writers[groupID] {
					return true, nil
				}
			case "write":
				if writers, exists := m.credentialWriters[credentialID]; exists && writers[groupID] {
					return true, nil
				}
			}
		}
	}

	return false, nil
}

// CreateCredentialOwner creates an owner relation for a credential
func (m *MockAuthorizationPort) CreateCredentialOwner(ctx context.Context, credentialID, ownerID string) error {
	m.mu.Lock()
	defer m.mu.Unlock()

	m.credentialOwners[credentialID] = ownerID
	return nil
}

// ShareCredential shares a credential with a user or group
func (m *MockAuthorizationPort) ShareCredential(ctx context.Context, credentialID, principalID, principalType, permission string) error {
	m.mu.Lock()
	defer m.mu.Unlock()

	switch permission {
	case "read", "ro":
		if m.credentialReaders[credentialID] == nil {
			m.credentialReaders[credentialID] = make(map[string]bool)
		}
		m.credentialReaders[credentialID][principalID] = true
	case "write", "rw":
		if m.credentialWriters[credentialID] == nil {
			m.credentialWriters[credentialID] = make(map[string]bool)
		}
		m.credentialWriters[credentialID][principalID] = true
		// Writers also get read access
		if m.credentialReaders[credentialID] == nil {
			m.credentialReaders[credentialID] = make(map[string]bool)
		}
		m.credentialReaders[credentialID][principalID] = true
	}

	return nil
}

// RevokeCredentialAccess revokes access to a credential for a user or group
func (m *MockAuthorizationPort) RevokeCredentialAccess(ctx context.Context, credentialID, principalID, principalType string) error {
	m.mu.Lock()
	defer m.mu.Unlock()

	if readers, exists := m.credentialReaders[credentialID]; exists {
		delete(readers, principalID)
	}
	if writers, exists := m.credentialWriters[credentialID]; exists {
		delete(writers, principalID)
	}

	return nil
}

// ListAccessibleCredentials returns all credentials accessible to a user
func (m *MockAuthorizationPort) ListAccessibleCredentials(ctx context.Context, userID, permission string) ([]string, error) {
	m.mu.RLock()
	defer m.mu.RUnlock()

	var accessible []string

	for credentialID := range m.credentialOwners {
		hasAccess, _ := m.CheckPermission(ctx, userID, credentialID, "credential", permission)
		if hasAccess {
			accessible = append(accessible, credentialID)
		}
	}

	return accessible, nil
}

// GetCredentialOwner returns the owner of a credential
func (m *MockAuthorizationPort) GetCredentialOwner(ctx context.Context, credentialID string) (string, error) {
	m.mu.RLock()
	defer m.mu.RUnlock()

	ownerID, exists := m.credentialOwners[credentialID]
	if !exists {
		return "", &NotFoundError{ID: credentialID}
	}

	return ownerID, nil
}

// ListCredentialReaders returns all users/groups with read access to a credential
func (m *MockAuthorizationPort) ListCredentialReaders(ctx context.Context, credentialID string) ([]string, error) {
	m.mu.RLock()
	defer m.mu.RUnlock()

	var readers []string
	if readerMap, exists := m.credentialReaders[credentialID]; exists {
		for readerID := range readerMap {
			readers = append(readers, readerID)
		}
	}

	return readers, nil
}

// ListCredentialWriters returns all users/groups with write access to a credential
func (m *MockAuthorizationPort) ListCredentialWriters(ctx context.Context, credentialID string) ([]string, error) {
	m.mu.RLock()
	defer m.mu.RUnlock()

	var writers []string
	if writerMap, exists := m.credentialWriters[credentialID]; exists {
		for writerID := range writerMap {
			writers = append(writers, writerID)
		}
	}

	return writers, nil
}

// AddUserToGroup adds a user to a group
func (m *MockAuthorizationPort) AddUserToGroup(ctx context.Context, userID, groupID string) error {
	m.mu.Lock()
	defer m.mu.Unlock()

	if m.groupMembers[groupID] == nil {
		m.groupMembers[groupID] = make(map[string]bool)
	}
	m.groupMembers[groupID][userID] = true

	return nil
}

// RemoveUserFromGroup removes a user from a group
func (m *MockAuthorizationPort) RemoveUserFromGroup(ctx context.Context, userID, groupID string) error {
	m.mu.Lock()
	defer m.mu.Unlock()

	if members, exists := m.groupMembers[groupID]; exists {
		delete(members, userID)
	}

	return nil
}

// AddGroupToGroup adds a child group to a parent group
func (m *MockAuthorizationPort) AddGroupToGroup(ctx context.Context, childGroupID, parentGroupID string) error {
	m.mu.Lock()
	defer m.mu.Unlock()

	if m.groupMembers[parentGroupID] == nil {
		m.groupMembers[parentGroupID] = make(map[string]bool)
	}
	m.groupMembers[parentGroupID][childGroupID] = true

	return nil
}

// RemoveGroupFromGroup removes a child group from a parent group
func (m *MockAuthorizationPort) RemoveGroupFromGroup(ctx context.Context, childGroupID, parentGroupID string) error {
	m.mu.Lock()
	defer m.mu.Unlock()

	if members, exists := m.groupMembers[parentGroupID]; exists {
		delete(members, childGroupID)
	}

	return nil
}

// GetUserGroups returns all groups a user belongs to
func (m *MockAuthorizationPort) GetUserGroups(ctx context.Context, userID string) ([]string, error) {
	m.mu.RLock()
	defer m.mu.RUnlock()

	var groups []string
	for groupID, members := range m.groupMembers {
		if members[userID] {
			groups = append(groups, groupID)
		}
	}

	return groups, nil
}

// GetGroupMembers returns all members of a group
func (m *MockAuthorizationPort) GetGroupMembers(ctx context.Context, groupID string) ([]string, error) {
	m.mu.RLock()
	defer m.mu.RUnlock()

	var members []string
	if memberMap, exists := m.groupMembers[groupID]; exists {
		for memberID := range memberMap {
			members = append(members, memberID)
		}
	}

	return members, nil
}

// BindCredentialToResource binds a credential to a compute or storage resource
func (m *MockAuthorizationPort) BindCredentialToResource(ctx context.Context, credentialID, resourceID, resourceType string) error {
	m.mu.Lock()
	defer m.mu.Unlock()

	if m.resourceCredentials[resourceID] == nil {
		m.resourceCredentials[resourceID] = make(map[string]bool)
	}
	m.resourceCredentials[resourceID][credentialID] = true

	return nil
}

// UnbindCredentialFromResource unbinds a credential from a resource
func (m *MockAuthorizationPort) UnbindCredentialFromResource(ctx context.Context, credentialID, resourceID, resourceType string) error {
	m.mu.Lock()
	defer m.mu.Unlock()

	if credentials, exists := m.resourceCredentials[resourceID]; exists {
		delete(credentials, credentialID)
	}

	return nil
}

// GetResourceCredentials returns all credentials bound to a resource
func (m *MockAuthorizationPort) GetResourceCredentials(ctx context.Context, resourceID, resourceType string) ([]string, error) {
	m.mu.RLock()
	defer m.mu.RUnlock()

	var credentials []string
	if credentialMap, exists := m.resourceCredentials[resourceID]; exists {
		for credentialID := range credentialMap {
			credentials = append(credentials, credentialID)
		}
	}

	return credentials, nil
}

// GetCredentialResources returns all resources bound to a credential
func (m *MockAuthorizationPort) GetCredentialResources(ctx context.Context, credentialID string) ([]ports.ResourceBinding, error) {
	m.mu.RLock()
	defer m.mu.RUnlock()

	var bindings []ports.ResourceBinding
	for resourceID, credentialMap := range m.resourceCredentials {
		if credentialMap[credentialID] {
			// For mock, we'll assume all resources are compute resources
			bindings = append(bindings, ports.ResourceBinding{
				ResourceID:   resourceID,
				ResourceType: "compute",
			})
		}
	}

	return bindings, nil
}

// GetUsableCredentialsForResource returns credentials bound to a resource that the user can access
func (m *MockAuthorizationPort) GetUsableCredentialsForResource(ctx context.Context, userID, resourceID, resourceType, permission string) ([]string, error) {
	// Get all credentials bound to the resource
	boundCredentials, err := m.GetResourceCredentials(ctx, resourceID, resourceType)
	if err != nil {
		return nil, err
	}

	// Filter by user access
	var usableCredentials []string
	for _, credentialID := range boundCredentials {
		hasAccess, err := m.CheckPermission(ctx, userID, credentialID, "credential", permission)
		if err != nil {
			continue // Skip on error
		}
		if hasAccess {
			usableCredentials = append(usableCredentials, credentialID)
		}
	}

	return usableCredentials, nil
}

// DebugPrint prints the internal state of the mock for debugging
func (m *MockAuthorizationPort) DebugPrint() {
	m.mu.RLock()
	defer m.mu.RUnlock()

	fmt.Printf("MockAuthorizationPort Debug:\n")
	fmt.Printf("  Owners: %+v\n", m.credentialOwners)
	fmt.Printf("  Readers: %+v\n", m.credentialReaders)
	fmt.Printf("  Writers: %+v\n", m.credentialWriters)
	fmt.Printf("  Group Members: %+v\n", m.groupMembers)
}

// Compile-time interface verification
var _ ports.VaultPort = (*MockVaultPort)(nil)
var _ ports.AuthorizationPort = (*MockAuthorizationPort)(nil)
