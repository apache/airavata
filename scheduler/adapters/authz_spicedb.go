package adapters

import (
	"context"
	"fmt"

	ports "github.com/apache/airavata/scheduler/core/port"
	v1 "github.com/authzed/authzed-go/proto/authzed/api/v1"
	"github.com/authzed/authzed-go/v1"
	"google.golang.org/grpc"
	"google.golang.org/grpc/credentials/insecure"
	"google.golang.org/grpc/metadata"
)

// SpiceDBAdapter implements the AuthorizationPort interface using SpiceDB
type SpiceDBAdapter struct {
	client *authzed.Client
	token  string
}

// NewSpiceDBAdapter creates a new SpiceDBAdapter
func NewSpiceDBAdapter(endpoint, token string) (ports.AuthorizationPort, error) {
	client, err := authzed.NewClient(
		endpoint,
		grpc.WithTransportCredentials(insecure.NewCredentials()),
	)
	if err != nil {
		return nil, fmt.Errorf("failed to create SpiceDB client: %w", err)
	}
	return &SpiceDBAdapter{client: client, token: token}, nil
}

// addAuthMetadata adds authentication metadata to the context
func (s *SpiceDBAdapter) addAuthMetadata(ctx context.Context) context.Context {
	md := metadata.Pairs("authorization", "Bearer "+s.token)
	return metadata.NewOutgoingContext(ctx, md)
}

// CheckPermission checks if a user has a specific permission on an object
func (s *SpiceDBAdapter) CheckPermission(ctx context.Context, userID, objectID, objectType, permission string) (bool, error) {
	// For now, only handle credential objects
	if objectType != "credential" {
		return false, nil
	}

	// Map our permission model to SpiceDB permissions
	var spicedbPermission string
	switch permission {
	case "read":
		spicedbPermission = "read"
	case "write":
		spicedbPermission = "write"
	case "delete":
		spicedbPermission = "delete"
	default:
		return false, fmt.Errorf("unknown permission: %s", permission)
	}

	// Create the check request
	checkReq := &v1.CheckPermissionRequest{
		Resource: &v1.ObjectReference{
			ObjectType: "credential",
			ObjectId:   objectID,
		},
		Permission: spicedbPermission,
		Subject: &v1.SubjectReference{
			Object: &v1.ObjectReference{
				ObjectType: "user",
				ObjectId:   userID,
			},
		},
	}

	// Perform the check
	authCtx := s.addAuthMetadata(ctx)
	resp, err := s.client.CheckPermission(authCtx, checkReq)
	if err != nil {
		return false, fmt.Errorf("failed to check permission: %w", err)
	}

	return resp.Permissionship == v1.CheckPermissionResponse_PERMISSIONSHIP_HAS_PERMISSION, nil
}

// CreateCredentialOwner creates an owner relation for a credential
func (s *SpiceDBAdapter) CreateCredentialOwner(ctx context.Context, credentialID, ownerID string) error {
	// Create the relationship for credential ownership
	relationship := &v1.Relationship{
		Resource: &v1.ObjectReference{
			ObjectType: "credential",
			ObjectId:   credentialID,
		},
		Relation: "owner",
		Subject: &v1.SubjectReference{
			Object: &v1.ObjectReference{
				ObjectType: "user",
				ObjectId:   ownerID,
			},
		},
	}

	// Write the relationship
	writeReq := &v1.WriteRelationshipsRequest{
		Updates: []*v1.RelationshipUpdate{
			{
				Operation:    v1.RelationshipUpdate_OPERATION_CREATE,
				Relationship: relationship,
			},
		},
	}

	authCtx := s.addAuthMetadata(ctx)
	_, err := s.client.WriteRelationships(authCtx, writeReq)
	if err != nil {
		return fmt.Errorf("failed to create credential owner relationship: %w", err)
	}

	return nil
}

// ShareCredential shares a credential with a user or group
func (s *SpiceDBAdapter) ShareCredential(ctx context.Context, credentialID, principalID, principalType, permission string) error {
	// Determine the relation based on permission
	var relation string
	switch permission {
	case "read", "ro", "r":
		relation = "reader"
	case "write", "rw", "w":
		relation = "writer"
	default:
		return fmt.Errorf("unknown permission: %s", permission)
	}

	// Determine the subject type
	var subjectType string
	switch principalType {
	case "user":
		subjectType = "user"
	case "group":
		subjectType = "group"
	default:
		return fmt.Errorf("unknown principal type: %s", principalType)
	}

	// Create the relationship
	relationship := &v1.Relationship{
		Resource: &v1.ObjectReference{
			ObjectType: "credential",
			ObjectId:   credentialID,
		},
		Relation: relation,
		Subject: &v1.SubjectReference{
			Object: &v1.ObjectReference{
				ObjectType: subjectType,
				ObjectId:   principalID,
			},
		},
	}

	// Write the relationship to SpiceDB
	writeReq := &v1.WriteRelationshipsRequest{
		Updates: []*v1.RelationshipUpdate{
			{
				Operation:    v1.RelationshipUpdate_OPERATION_CREATE,
				Relationship: relationship,
			},
		},
	}

	authCtx := s.addAuthMetadata(ctx)
	_, err := s.client.WriteRelationships(authCtx, writeReq)
	if err != nil {
		return fmt.Errorf("failed to share credential: %w", err)
	}

	return nil
}

// RevokeCredentialAccess revokes access to a credential for a user or group
func (s *SpiceDBAdapter) RevokeCredentialAccess(ctx context.Context, credentialID, principalID, principalType string) error {
	// Determine the subject type
	var subjectType string
	switch principalType {
	case "user":
		subjectType = "user"
	case "group":
		subjectType = "group"
	default:
		return fmt.Errorf("unknown principal type: %s", principalType)
	}

	// Delete both reader and writer relationships for this principal
	updates := []*v1.RelationshipUpdate{}

	for _, relation := range []string{"reader", "writer"} {
		updates = append(updates, &v1.RelationshipUpdate{
			Operation: v1.RelationshipUpdate_OPERATION_DELETE,
			Relationship: &v1.Relationship{
				Resource: &v1.ObjectReference{
					ObjectType: "credential",
					ObjectId:   credentialID,
				},
				Relation: relation,
				Subject: &v1.SubjectReference{
					Object: &v1.ObjectReference{
						ObjectType: subjectType,
						ObjectId:   principalID,
					},
				},
			},
		})
	}

	writeReq := &v1.WriteRelationshipsRequest{
		Updates: updates,
	}

	authCtx := s.addAuthMetadata(ctx)
	_, err := s.client.WriteRelationships(authCtx, writeReq)
	if err != nil {
		return fmt.Errorf("failed to revoke credential access: %w", err)
	}

	return nil
}

// ListAccessibleCredentials returns all credentials accessible to a user
func (s *SpiceDBAdapter) ListAccessibleCredentials(ctx context.Context, userID, permission string) ([]string, error) {
	// Map permission to SpiceDB permission
	var spicedbPermission string
	switch permission {
	case "read", "ro", "r":
		spicedbPermission = "read"
	case "write", "rw", "w":
		spicedbPermission = "write"
	case "delete":
		spicedbPermission = "delete"
	default:
		return nil, fmt.Errorf("unknown permission: %s", permission)
	}

	// Use SpiceDB's LookupResources to find all credentials the user can access
	lookupReq := &v1.LookupResourcesRequest{
		ResourceObjectType: "credential",
		Permission:         spicedbPermission,
		Subject: &v1.SubjectReference{
			Object: &v1.ObjectReference{
				ObjectType: "user",
				ObjectId:   userID,
			},
		},
	}

	authCtx := s.addAuthMetadata(ctx)
	stream, err := s.client.LookupResources(authCtx, lookupReq)
	if err != nil {
		return nil, fmt.Errorf("failed to lookup accessible credentials: %w", err)
	}

	var accessible []string
	for {
		resp, err := stream.Recv()
		if err != nil {
			if err.Error() == "EOF" {
				break
			}
			return nil, fmt.Errorf("error reading lookup stream: %w", err)
		}
		accessible = append(accessible, resp.ResourceObjectId)
	}

	return accessible, nil
}

// GetCredentialOwner returns the owner of a credential
func (s *SpiceDBAdapter) GetCredentialOwner(ctx context.Context, credentialID string) (string, error) {
	// Read relationships for the credential with "owner" relation
	readReq := &v1.ReadRelationshipsRequest{
		RelationshipFilter: &v1.RelationshipFilter{
			ResourceType:       "credential",
			OptionalResourceId: credentialID,
			OptionalRelation:   "owner",
		},
	}

	authCtx := s.addAuthMetadata(ctx)
	stream, err := s.client.ReadRelationships(authCtx, readReq)
	if err != nil {
		return "", fmt.Errorf("failed to read owner relationship: %w", err)
	}

	// Get the first (and should be only) owner
	resp, err := stream.Recv()
	if err != nil {
		return "", fmt.Errorf("credential %s has no owner", credentialID)
	}

	return resp.Relationship.Subject.Object.ObjectId, nil
}

// ListCredentialReaders returns all users/groups with read access to a credential
func (s *SpiceDBAdapter) ListCredentialReaders(ctx context.Context, credentialID string) ([]string, error) {
	readReq := &v1.ReadRelationshipsRequest{
		RelationshipFilter: &v1.RelationshipFilter{
			ResourceType:       "credential",
			OptionalResourceId: credentialID,
			OptionalRelation:   "reader",
		},
	}

	authCtx := s.addAuthMetadata(ctx)
	stream, err := s.client.ReadRelationships(authCtx, readReq)
	if err != nil {
		return nil, fmt.Errorf("failed to read reader relationships: %w", err)
	}

	var readers []string
	for {
		resp, err := stream.Recv()
		if err != nil {
			if err.Error() == "EOF" {
				break
			}
			return nil, fmt.Errorf("failed to receive group relationship: %w", err)
		}
		readers = append(readers, resp.Relationship.Subject.Object.ObjectId)
	}

	return readers, nil
}

// ListCredentialWriters returns all users/groups with write access to a credential
func (s *SpiceDBAdapter) ListCredentialWriters(ctx context.Context, credentialID string) ([]string, error) {
	readReq := &v1.ReadRelationshipsRequest{
		RelationshipFilter: &v1.RelationshipFilter{
			ResourceType:       "credential",
			OptionalResourceId: credentialID,
			OptionalRelation:   "writer",
		},
	}

	authCtx := s.addAuthMetadata(ctx)
	stream, err := s.client.ReadRelationships(authCtx, readReq)
	if err != nil {
		return nil, fmt.Errorf("failed to read writer relationships: %w", err)
	}

	var writers []string
	for {
		resp, err := stream.Recv()
		if err != nil {
			if err.Error() == "EOF" {
				break
			}
			return nil, fmt.Errorf("failed to receive group relationship: %w", err)
		}
		writers = append(writers, resp.Relationship.Subject.Object.ObjectId)
	}

	return writers, nil
}

// AddUserToGroup adds a user to a group
func (s *SpiceDBAdapter) AddUserToGroup(ctx context.Context, userID, groupID string) error {
	relationship := &v1.Relationship{
		Resource: &v1.ObjectReference{
			ObjectType: "group",
			ObjectId:   groupID,
		},
		Relation: "member",
		Subject: &v1.SubjectReference{
			Object: &v1.ObjectReference{
				ObjectType: "user",
				ObjectId:   userID,
			},
		},
	}

	writeReq := &v1.WriteRelationshipsRequest{
		Updates: []*v1.RelationshipUpdate{
			{
				Operation:    v1.RelationshipUpdate_OPERATION_CREATE,
				Relationship: relationship,
			},
		},
	}

	authCtx := s.addAuthMetadata(ctx)
	_, err := s.client.WriteRelationships(authCtx, writeReq)
	if err != nil {
		return fmt.Errorf("failed to add user to group: %w", err)
	}

	return nil
}

// RemoveUserFromGroup removes a user from a group
func (s *SpiceDBAdapter) RemoveUserFromGroup(ctx context.Context, userID, groupID string) error {
	relationship := &v1.Relationship{
		Resource: &v1.ObjectReference{
			ObjectType: "group",
			ObjectId:   groupID,
		},
		Relation: "member",
		Subject: &v1.SubjectReference{
			Object: &v1.ObjectReference{
				ObjectType: "user",
				ObjectId:   userID,
			},
		},
	}

	writeReq := &v1.WriteRelationshipsRequest{
		Updates: []*v1.RelationshipUpdate{
			{
				Operation:    v1.RelationshipUpdate_OPERATION_DELETE,
				Relationship: relationship,
			},
		},
	}

	authCtx := s.addAuthMetadata(ctx)
	_, err := s.client.WriteRelationships(authCtx, writeReq)
	if err != nil {
		return fmt.Errorf("failed to remove user from group: %w", err)
	}

	return nil
}

// AddGroupToGroup adds a child group to a parent group
func (s *SpiceDBAdapter) AddGroupToGroup(ctx context.Context, childGroupID, parentGroupID string) error {
	relationship := &v1.Relationship{
		Resource: &v1.ObjectReference{
			ObjectType: "group",
			ObjectId:   parentGroupID,
		},
		Relation: "member",
		Subject: &v1.SubjectReference{
			Object: &v1.ObjectReference{
				ObjectType: "group",
				ObjectId:   childGroupID,
			},
		},
	}

	writeReq := &v1.WriteRelationshipsRequest{
		Updates: []*v1.RelationshipUpdate{
			{
				Operation:    v1.RelationshipUpdate_OPERATION_CREATE,
				Relationship: relationship,
			},
		},
	}

	authCtx := s.addAuthMetadata(ctx)
	_, err := s.client.WriteRelationships(authCtx, writeReq)
	if err != nil {
		return fmt.Errorf("failed to add group to group: %w", err)
	}

	return nil
}

// RemoveGroupFromGroup removes a child group from a parent group
func (s *SpiceDBAdapter) RemoveGroupFromGroup(ctx context.Context, childGroupID, parentGroupID string) error {
	relationship := &v1.Relationship{
		Resource: &v1.ObjectReference{
			ObjectType: "group",
			ObjectId:   parentGroupID,
		},
		Relation: "member",
		Subject: &v1.SubjectReference{
			Object: &v1.ObjectReference{
				ObjectType: "group",
				ObjectId:   childGroupID,
			},
		},
	}

	writeReq := &v1.WriteRelationshipsRequest{
		Updates: []*v1.RelationshipUpdate{
			{
				Operation:    v1.RelationshipUpdate_OPERATION_DELETE,
				Relationship: relationship,
			},
		},
	}

	authCtx := s.addAuthMetadata(ctx)
	_, err := s.client.WriteRelationships(authCtx, writeReq)
	if err != nil {
		return fmt.Errorf("failed to remove group from group: %w", err)
	}

	return nil
}

// GetUserGroups returns all groups a user belongs to
func (s *SpiceDBAdapter) GetUserGroups(ctx context.Context, userID string) ([]string, error) {
	readReq := &v1.ReadRelationshipsRequest{
		RelationshipFilter: &v1.RelationshipFilter{
			ResourceType:     "group",
			OptionalRelation: "member",
			OptionalSubjectFilter: &v1.SubjectFilter{
				SubjectType:       "user",
				OptionalSubjectId: userID,
			},
		},
	}

	authCtx := s.addAuthMetadata(ctx)
	stream, err := s.client.ReadRelationships(authCtx, readReq)
	if err != nil {
		return nil, fmt.Errorf("failed to read user groups: %w", err)
	}

	var groups []string
	for {
		resp, err := stream.Recv()
		if err != nil {
			if err.Error() == "EOF" {
				break
			}
			return nil, fmt.Errorf("failed to receive group relationship: %w", err)
		}
		groups = append(groups, resp.Relationship.Resource.ObjectId)
	}

	return groups, nil
}

// GetGroupMembers returns all members of a group
func (s *SpiceDBAdapter) GetGroupMembers(ctx context.Context, groupID string) ([]string, error) {
	readReq := &v1.ReadRelationshipsRequest{
		RelationshipFilter: &v1.RelationshipFilter{
			ResourceType:       "group",
			OptionalResourceId: groupID,
			OptionalRelation:   "member",
		},
	}

	authCtx := s.addAuthMetadata(ctx)
	stream, err := s.client.ReadRelationships(authCtx, readReq)
	if err != nil {
		return nil, fmt.Errorf("failed to read group members: %w", err)
	}

	var members []string
	for {
		resp, err := stream.Recv()
		if err != nil {
			if err.Error() == "EOF" {
				break
			}
			return nil, fmt.Errorf("failed to receive group relationship: %w", err)
		}
		members = append(members, resp.Relationship.Subject.Object.ObjectId)
	}

	return members, nil
}

// BindCredentialToResource binds a credential to a compute or storage resource
func (s *SpiceDBAdapter) BindCredentialToResource(ctx context.Context, credentialID, resourceID, resourceType string) error {
	// Create the relationship for credential binding
	relationship := &v1.Relationship{
		Resource: &v1.ObjectReference{
			ObjectType: resourceType,
			ObjectId:   resourceID,
		},
		Relation: "bound_credential",
		Subject: &v1.SubjectReference{
			Object: &v1.ObjectReference{
				ObjectType: "credential",
				ObjectId:   credentialID,
			},
		},
	}

	// Write the relationship
	writeReq := &v1.WriteRelationshipsRequest{
		Updates: []*v1.RelationshipUpdate{
			{
				Operation:    v1.RelationshipUpdate_OPERATION_CREATE,
				Relationship: relationship,
			},
		},
	}

	authCtx := s.addAuthMetadata(ctx)
	_, err := s.client.WriteRelationships(authCtx, writeReq)
	if err != nil {
		return fmt.Errorf("failed to bind credential to resource: %w", err)
	}

	return nil
}

// UnbindCredentialFromResource unbinds a credential from a resource
func (s *SpiceDBAdapter) UnbindCredentialFromResource(ctx context.Context, credentialID, resourceID, resourceType string) error {
	// Create the relationship to delete
	relationship := &v1.Relationship{
		Resource: &v1.ObjectReference{
			ObjectType: resourceType,
			ObjectId:   resourceID,
		},
		Relation: "credential",
		Subject: &v1.SubjectReference{
			Object: &v1.ObjectReference{
				ObjectType: "credential",
				ObjectId:   credentialID,
			},
		},
	}

	// Write the delete operation
	writeReq := &v1.WriteRelationshipsRequest{
		Updates: []*v1.RelationshipUpdate{
			{
				Operation:    v1.RelationshipUpdate_OPERATION_DELETE,
				Relationship: relationship,
			},
		},
	}

	authCtx := s.addAuthMetadata(ctx)
	_, err := s.client.WriteRelationships(authCtx, writeReq)
	if err != nil {
		return fmt.Errorf("failed to unbind credential from resource: %w", err)
	}

	return nil
}

// GetResourceCredentials returns all credentials bound to a resource
func (s *SpiceDBAdapter) GetResourceCredentials(ctx context.Context, resourceID, resourceType string) ([]string, error) {
	// Create the read request to find all credentials bound to the resource
	readReq := &v1.ReadRelationshipsRequest{
		Consistency: &v1.Consistency{
			Requirement: &v1.Consistency_FullyConsistent{
				FullyConsistent: true,
			},
		},
		RelationshipFilter: &v1.RelationshipFilter{
			ResourceType:       resourceType,
			OptionalResourceId: resourceID,
			OptionalRelation:   "bound_credential",
		},
	}

	// Read the relationships with authentication
	authCtx := s.addAuthMetadata(ctx)
	stream, err := s.client.ReadRelationships(authCtx, readReq)
	if err != nil {
		return nil, fmt.Errorf("failed to read resource credentials: %w", err)
	}

	var credentials []string
	for {
		resp, err := stream.Recv()
		if err != nil {
			if err.Error() == "EOF" {
				break
			}
			return nil, fmt.Errorf("failed to receive credential relationship: %w", err)
		}
		credentials = append(credentials, resp.Relationship.Subject.Object.ObjectId)
	}

	return credentials, nil
}

// GetCredentialResources returns all resources bound to a credential
func (s *SpiceDBAdapter) GetCredentialResources(ctx context.Context, credentialID string) ([]ports.ResourceBinding, error) {
	// Create the read request to find all resources bound to the credential
	readReq := &v1.ReadRelationshipsRequest{
		Consistency: &v1.Consistency{
			Requirement: &v1.Consistency_FullyConsistent{
				FullyConsistent: true,
			},
		},
		RelationshipFilter: &v1.RelationshipFilter{
			OptionalSubjectFilter: &v1.SubjectFilter{
				SubjectType:       "credential",
				OptionalSubjectId: credentialID,
			},
			OptionalRelation: "credential",
		},
	}

	// Read the relationships with authentication
	authCtx := s.addAuthMetadata(ctx)
	stream, err := s.client.ReadRelationships(authCtx, readReq)
	if err != nil {
		return nil, fmt.Errorf("failed to read credential resources: %w", err)
	}

	var bindings []ports.ResourceBinding
	for {
		resp, err := stream.Recv()
		if err != nil {
			if err.Error() == "EOF" {
				break
			}
			return nil, fmt.Errorf("failed to receive resource relationship: %w", err)
		}
		bindings = append(bindings, ports.ResourceBinding{
			ResourceID:   resp.Relationship.Resource.ObjectId,
			ResourceType: resp.Relationship.Resource.ObjectType,
		})
	}

	return bindings, nil
}

// GetUsableCredentialsForResource returns credentials bound to a resource that the user can access
func (s *SpiceDBAdapter) GetUsableCredentialsForResource(ctx context.Context, userID, resourceID, resourceType, permission string) ([]string, error) {
	// Get all credentials bound to the resource
	boundCredentials, err := s.GetResourceCredentials(ctx, resourceID, resourceType)
	if err != nil {
		return nil, err
	}

	// Filter by user access
	var usableCredentials []string
	for _, credentialID := range boundCredentials {
		hasAccess, err := s.CheckPermission(ctx, userID, credentialID, "credential", permission)
		if err != nil {
			continue // Skip on error
		}
		if hasAccess {
			usableCredentials = append(usableCredentials, credentialID)
		}
	}

	return usableCredentials, nil
}

// Compile-time interface verification
var _ ports.AuthorizationPort = (*SpiceDBAdapter)(nil)
