package compute

import (
	"context"
	"errors"

	"gorm.io/gorm"

	"github.com/apache/airavata/api/credentials"
	"github.com/apache/airavata/api/iam"
	"github.com/apache/airavata/internal/auth"
	"github.com/apache/airavata/internal/httpx"

	dto "github.com/apache/airavata/api/compute/dto"
	model "github.com/apache/airavata/api/compute/model"
)

const (
	permRead  = model.SSHEndpointCredentialPermissionRead
	permWrite = model.SSHEndpointCredentialPermissionWrite
)

// credentialAccess resolves what the calling principal may do with a binding.
//
// Access comes from three places, strongest wins: owning it, being named in a user
// share, or being an active member of a group the binding is shared with. Platform
// admins are treated as owners, which is the same latitude they had before sharing
// existed.
//
// "Control" — deleting the binding and managing its shares — is deliberately not
// reachable through a WRITE share. A share lets someone use and repoint a credential;
// deciding who else gets it stays with the owner.
type credentialAccess struct {
	credentials *SSHEndpointCredentialRepository
	sharing     *SSHEndpointCredentialSharingRepository
	members     *iam.GroupMemberRepository
}

// withTx binds every repository the check reads to tx.
//
// Resolving a permission touches three tables, so a check made from inside a
// transaction has to run on that transaction: reading around it would take a second
// connection and see the pre-transaction state.
func (a credentialAccess) withTx(tx *gorm.DB) credentialAccess {
	return credentialAccess{
		credentials: a.credentials.WithTx(tx),
		sharing:     a.sharing.WithTx(tx),
		members:     a.members.WithTx(tx),
	}
}

// requireCredential loads a binding or reports 404.
func (a credentialAccess) requireCredential(ctx context.Context, id string) (*model.SSHEndpointCredential, error) {
	credential, err := a.credentials.FindByID(ctx, id)
	if err != nil {
		return nil, notFoundAs(err, "SSH endpoint credential binding not found: %s", id)
	}
	return credential, nil
}

// permissionOf returns the caller's effective permission on credential and whether
// they control it. An empty permission means no access at all.
func (a credentialAccess) permissionOf(ctx context.Context, credential *model.SSHEndpointCredential) (model.SSHEndpointCredentialPermission, bool, error) {
	principal, err := auth.RequireAuthenticated(ctx)
	if err != nil {
		return "", false, err
	}
	if principal.IsAdmin() || credential.OwnedBy(principal.Name) {
		return permWrite, true, nil
	}

	best := model.SSHEndpointCredentialPermission("")
	if share, err := a.sharing.FindUserShareByUserID(ctx, credential.ID, principal.Name); err == nil {
		best = strongest(best, share.Permission)
	} else if !errors.Is(err, gorm.ErrRecordNotFound) {
		return "", false, err
	}

	// A group share reaches the caller only through an ACTIVE membership: a suspended
	// member keeps their place in the group without keeping access through it.
	groupShares, err := a.sharing.FindGroupSharesByCredentialID(ctx, credential.ID)
	if err != nil {
		return "", false, err
	}
	if len(groupShares) > 0 {
		memberships, err := a.members.FindByUserID(ctx, principal.Name)
		if err != nil {
			return "", false, err
		}
		active := make(map[string]bool, len(memberships))
		for i := range memberships {
			if memberships[i].IsActive() {
				active[memberships[i].GroupID] = true
			}
		}
		for i := range groupShares {
			if groupShares[i].GroupID != nil && active[*groupShares[i].GroupID] {
				best = strongest(best, groupShares[i].Permission)
			}
		}
	}

	return best, false, nil
}

// require checks that the caller holds at least want, returning their effective
// permission and whether they control the binding.
func (a credentialAccess) require(ctx context.Context, credential *model.SSHEndpointCredential, want model.SSHEndpointCredentialPermission) (model.SSHEndpointCredentialPermission, bool, error) {
	permission, controls, err := a.permissionOf(ctx, credential)
	if err != nil {
		return "", false, err
	}
	if !permission.Allows(want) {
		return "", false, httpx.Forbidden(
			"Access denied: you may only %s SSH endpoint credential bindings you own or that are shared with you for %s",
			verbFor(want), want)
	}
	return permission, controls, nil
}

// requireControl allows only the owner and platform admins.
func (a credentialAccess) requireControl(ctx context.Context, credential *model.SSHEndpointCredential) error {
	_, controls, err := a.permissionOf(ctx, credential)
	if err != nil {
		return err
	}
	if !controls {
		return httpx.Forbidden("Access denied: only the owner of an SSH endpoint credential binding may do that")
	}
	return nil
}

func strongest(have model.SSHEndpointCredentialPermission, candidate *model.SSHEndpointCredentialPermission) model.SSHEndpointCredentialPermission {
	if candidate == nil || !candidate.Valid() {
		return have
	}
	if *candidate == permWrite || have == "" {
		return *candidate
	}
	return have
}

func verbFor(want model.SSHEndpointCredentialPermission) string {
	if want == permWrite {
		return "modify"
	}
	return "read"
}

// CredentialAccess answers "may this caller act under this binding?" for services
// outside the compute package.
//
// The data vertical asks it before letting a dataset be registered under a credential.
// Exposing the question rather than the tables is what keeps one definition of who may
// use a credential: owner, admin, or a share, resolved exactly as it is here.
type CredentialAccess struct{ credentialAccess }

// NewCredentialAccess returns a checker over the credential and sharing tables.
func NewCredentialAccess(
	bindings *SSHEndpointCredentialRepository,
	sharing *SSHEndpointCredentialSharingRepository,
	members *iam.GroupMemberRepository,
) *CredentialAccess {
	return &CredentialAccess{credentialAccess{credentials: bindings, sharing: sharing, members: members}}
}

// WithTx returns a checker bound to tx, for checks made from inside a transaction.
func (a *CredentialAccess) WithTx(tx *gorm.DB) *CredentialAccess {
	return &CredentialAccess{a.credentialAccess.withTx(tx)}
}

// RequireUsable loads a binding and checks the caller may act under it: 404 when there
// is no such binding, 403 when it is neither theirs nor shared with them.
func (a *CredentialAccess) RequireUsable(ctx context.Context, id string) (*model.SSHEndpointCredential, error) {
	credential, err := a.requireCredential(ctx, id)
	if err != nil {
		return nil, err
	}
	if _, _, err := a.require(ctx, credential, permRead); err != nil {
		return nil, err
	}
	return credential, nil
}

// SSHEndpointCredentialService manages the bindings that let a user act on a host.
//
// This is the tightest authorisation model in the API. Ownership comes from the access
// token and never from the request body, and it is immutable once set: an update
// re-resolves the endpoint and SSH credential but never the owner, so a binding cannot
// be transferred by editing it — it can only be shared.
type SSHEndpointCredentialService struct {
	credentialAccess
	db        *gorm.DB
	endpoints *SSHEndpointRepository
	sshCreds  *credentials.SSHUserCredentialRepository
	users     *iam.UserRepository
}

// NewSSHEndpointCredentialService returns an endpoint credential service.
func NewSSHEndpointCredentialService(
	db *gorm.DB,
	bindings *SSHEndpointCredentialRepository,
	sharing *SSHEndpointCredentialSharingRepository,
	endpoints *SSHEndpointRepository,
	sshCreds *credentials.SSHUserCredentialRepository,
	users *iam.UserRepository,
	members *iam.GroupMemberRepository,
) *SSHEndpointCredentialService {
	return &SSHEndpointCredentialService{
		credentialAccess: credentialAccess{credentials: bindings, sharing: sharing, members: members},
		db:               db,
		endpoints:        endpoints,
		sshCreds:         sshCreds,
		users:            users,
	}
}

// List returns every binding across every user, optionally scoped to one endpoint.
// Admin only — this exposes who holds access to what.
func (s *SSHEndpointCredentialService) List(ctx context.Context, endpointID string) ([]dto.SSHEndpointCredentialResponse, error) {
	if _, err := auth.RequireAdmin(ctx); err != nil {
		return nil, err
	}
	var (
		found []model.SSHEndpointCredential
		err   error
	)
	if endpointID == "" {
		found, err = s.credentials.FindAll(ctx)
	} else {
		found, err = s.credentials.FindBySSHEndpointID(ctx, endpointID)
	}
	if err != nil {
		return nil, err
	}
	return dto.ToSSHEndpointCredentialResponses(found), nil
}

// ListMine returns the caller's own bindings, optionally scoped to one endpoint.
func (s *SSHEndpointCredentialService) ListMine(ctx context.Context, endpointID string) ([]dto.SSHEndpointCredentialResponse, error) {
	principal, err := auth.RequireAuthenticated(ctx)
	if err != nil {
		return nil, err
	}
	var found []model.SSHEndpointCredential
	if endpointID == "" {
		found, err = s.credentials.FindByOwnerID(ctx, principal.Name)
	} else {
		found, err = s.credentials.FindByOwnerIDAndSSHEndpointID(ctx, principal.Name, endpointID)
	}
	if err != nil {
		return nil, err
	}
	return dto.ToSSHEndpointCredentialResponses(found), nil
}

// ListSharedWithMe returns the bindings other users have shared with the caller,
// directly or through a group, each carrying what it grants them.
func (s *SSHEndpointCredentialService) ListSharedWithMe(ctx context.Context) ([]dto.SSHEndpointCredentialResponse, error) {
	principal, err := auth.RequireAuthenticated(ctx)
	if err != nil {
		return nil, err
	}
	found, err := s.credentials.FindSharedWith(ctx, principal.Name)
	if err != nil {
		return nil, err
	}

	out := make([]dto.SSHEndpointCredentialResponse, 0, len(found))
	for i := range found {
		// Re-resolving per binding is what keeps the reported permission honest when
		// a user share and a group share reach the same binding with different grants.
		permission, _, err := s.permissionOf(ctx, &found[i])
		if err != nil {
			return nil, err
		}
		if permission == "" {
			continue
		}
		out = append(out, dto.ToSSHEndpointCredentialResponseWith(&found[i], permission))
	}
	return out, nil
}

// Get returns one binding, to anyone holding READ on it.
func (s *SSHEndpointCredentialService) Get(ctx context.Context, id string) (*dto.SSHEndpointCredentialResponse, error) {
	credential, err := s.requireCredential(ctx, id)
	if err != nil {
		return nil, err
	}
	permission, _, err := s.require(ctx, credential, permRead)
	if err != nil {
		return nil, err
	}
	out := dto.ToSSHEndpointCredentialResponseWith(credential, permission)
	return &out, nil
}

// Create binds an SSH credential to an endpoint for the calling user.
//
// Any authenticated caller may do this for themselves. The owner is taken from the
// token, so there is no way to create a binding on someone else's behalf.
func (s *SSHEndpointCredentialService) Create(ctx context.Context, req *dto.SSHEndpointCredentialRequest) (*dto.SSHEndpointCredentialResponse, error) {
	principal, err := auth.RequireAuthenticated(ctx)
	if err != nil {
		return nil, err
	}

	var out dto.SSHEndpointCredentialResponse
	err = s.db.WithContext(ctx).Transaction(func(tx *gorm.DB) error {
		bindings, endpoints := s.credentials.WithTx(tx), s.endpoints.WithTx(tx)
		sshCreds, users := s.sshCreds.WithTx(tx), s.users.WithTx(tx)

		owner, err := users.FindByID(ctx, principal.Name)
		if err != nil {
			return notFoundAs(err, "No user record found for authenticated principal: %s", principal.Name)
		}
		endpoint, err := endpoints.FindByID(ctx, req.SSHEndpointID)
		if err != nil {
			return notFoundAs(err, "SSH endpoint not found: %s", req.SSHEndpointID)
		}
		sshCred, err := sshCreds.FindByID(ctx, req.SSHCredentialID)
		if err != nil {
			return notFoundAs(err, "SSH credential not found: %s", req.SSHCredentialID)
		}

		binding := &model.SSHEndpointCredential{
			SSHEndpointID:   &endpoint.ID,
			SSHCredentialID: &sshCred.ID,
			OwnerID:         &owner.ID,
		}
		if err := bindings.Save(ctx, binding); err != nil {
			return err
		}
		out = dto.ToSSHEndpointCredentialResponseWith(binding, permWrite)
		return nil
	})
	if err != nil {
		return nil, err
	}
	return &out, nil
}

// Update repoints a binding at a different endpoint or SSH credential. It needs WRITE,
// which a share can confer.
//
// The owner is deliberately left alone: re-deriving it from the caller's token would
// hand the binding to whichever admin — or grantee — happened to issue the request.
func (s *SSHEndpointCredentialService) Update(ctx context.Context, id string, req *dto.SSHEndpointCredentialRequest) (*dto.SSHEndpointCredentialResponse, error) {
	var out dto.SSHEndpointCredentialResponse
	err := s.db.WithContext(ctx).Transaction(func(tx *gorm.DB) error {
		bindings, endpoints := s.credentials.WithTx(tx), s.endpoints.WithTx(tx)
		sshCreds := s.sshCreds.WithTx(tx)

		binding, err := bindings.FindByID(ctx, id)
		if err != nil {
			return notFoundAs(err, "SSH endpoint credential binding not found: %s", id)
		}
		permission, _, err := s.credentialAccess.withTx(tx).require(ctx, binding, permWrite)
		if err != nil {
			return err
		}

		endpoint, err := endpoints.FindByID(ctx, req.SSHEndpointID)
		if err != nil {
			return notFoundAs(err, "SSH endpoint not found: %s", req.SSHEndpointID)
		}
		sshCred, err := sshCreds.FindByID(ctx, req.SSHCredentialID)
		if err != nil {
			return notFoundAs(err, "SSH credential not found: %s", req.SSHCredentialID)
		}

		binding.SSHEndpointID = &endpoint.ID
		binding.SSHCredentialID = &sshCred.ID

		if err := bindings.Save(ctx, binding); err != nil {
			return err
		}
		out = dto.ToSSHEndpointCredentialResponseWith(binding, permission)
		return nil
	})
	if err != nil {
		return nil, err
	}
	return &out, nil
}

// Delete removes a binding and every share of it, for its owner or an admin.
//
// The shares go first and in the same transaction: their foreign keys are RESTRICT, so
// leaving them would turn an ordinary delete into a constraint violation.
func (s *SSHEndpointCredentialService) Delete(ctx context.Context, id string) error {
	credential, err := s.requireCredential(ctx, id)
	if err != nil {
		return err
	}
	if err := s.requireControl(ctx, credential); err != nil {
		return err
	}

	return s.db.WithContext(ctx).Transaction(func(tx *gorm.DB) error {
		if err := s.sharing.WithTx(tx).DeleteByCredentialID(ctx, credential.ID); err != nil {
			return err
		}
		return s.credentials.WithTx(tx).Delete(ctx, credential)
	})
}

// SSHEndpointCredentialSharingService manages who, besides the owner, may use a
// binding.
//
// Every operation is scoped by the credential id from the path, and only the owner (or
// a platform admin) may read or change the share list: it names who can reach a host,
// which is more than a grantee needs to know.
type SSHEndpointCredentialSharingService struct {
	credentialAccess
	db     *gorm.DB
	groups *iam.GroupRepository
	users  *iam.UserRepository
}

// NewSSHEndpointCredentialSharingService returns a sharing service.
func NewSSHEndpointCredentialSharingService(
	db *gorm.DB,
	bindings *SSHEndpointCredentialRepository,
	sharing *SSHEndpointCredentialSharingRepository,
	groups *iam.GroupRepository,
	users *iam.UserRepository,
	members *iam.GroupMemberRepository,
) *SSHEndpointCredentialSharingService {
	return &SSHEndpointCredentialSharingService{
		credentialAccess: credentialAccess{credentials: bindings, sharing: sharing, members: members},
		db:               db,
		groups:           groups,
		users:            users,
	}
}

// ListGroupShares returns every group a binding is shared with.
func (s *SSHEndpointCredentialSharingService) ListGroupShares(ctx context.Context, credentialID string) ([]dto.SSHEndpointCredentialGroupSharingResponse, error) {
	credential, err := s.requireControlledCredential(ctx, credentialID)
	if err != nil {
		return nil, err
	}
	shares, err := s.sharing.FindGroupSharesByCredentialID(ctx, credential.ID)
	if err != nil {
		return nil, err
	}
	return dto.ToGroupSharingResponses(shares), nil
}

// ShareWithGroup grants a group access to a binding.
func (s *SSHEndpointCredentialSharingService) ShareWithGroup(ctx context.Context, credentialID string, req *dto.SSHEndpointCredentialGroupSharingRequest) (*dto.SSHEndpointCredentialGroupSharingResponse, error) {
	credential, err := s.requireControlledCredential(ctx, credentialID)
	if err != nil {
		return nil, err
	}

	var out dto.SSHEndpointCredentialGroupSharingResponse
	err = s.db.WithContext(ctx).Transaction(func(tx *gorm.DB) error {
		sharing, groups := s.sharing.WithTx(tx), s.groups.WithTx(tx)

		if _, err := groups.FindByID(ctx, req.GroupID); err != nil {
			return notFoundAs(err, "Group not found: %s", req.GroupID)
		}
		if _, err := sharing.FindGroupShareByGroupID(ctx, credential.ID, req.GroupID); err == nil {
			return httpx.Conflict("SSH endpoint credential binding %s is already shared with group %s",
				credential.ID, req.GroupID)
		} else if !errors.Is(err, gorm.ErrRecordNotFound) {
			return err
		}

		permission := req.Grant()
		share := &model.SSHEndpointCredentialGroupSharing{
			SSHEndpointCredentialID: &credential.ID,
			GroupID:                 &req.GroupID,
			Permission:              &permission,
		}
		if err := sharing.SaveGroupShare(ctx, share); err != nil {
			return err
		}
		out = dto.ToGroupSharingResponse(share)
		return nil
	})
	if err != nil {
		return nil, err
	}
	return &out, nil
}

// UpdateGroupShare changes what a group share grants.
func (s *SSHEndpointCredentialSharingService) UpdateGroupShare(ctx context.Context, credentialID, sharingID string, req *dto.SharingUpdate) (*dto.SSHEndpointCredentialGroupSharingResponse, error) {
	credential, err := s.requireControlledCredential(ctx, credentialID)
	if err != nil {
		return nil, err
	}
	share, err := s.sharing.FindGroupShare(ctx, credential.ID, sharingID)
	if err != nil {
		return nil, notFoundAs(err, "Group sharing not found: %s on binding %s", sharingID, credential.ID)
	}

	share.Permission = req.Permission
	if err := s.sharing.SaveGroupShare(ctx, share); err != nil {
		return nil, err
	}
	out := dto.ToGroupSharingResponse(share)
	return &out, nil
}

// RevokeGroupShare withdraws a group's access.
func (s *SSHEndpointCredentialSharingService) RevokeGroupShare(ctx context.Context, credentialID, sharingID string) error {
	credential, err := s.requireControlledCredential(ctx, credentialID)
	if err != nil {
		return err
	}
	share, err := s.sharing.FindGroupShare(ctx, credential.ID, sharingID)
	if err != nil {
		return notFoundAs(err, "Group sharing not found: %s on binding %s", sharingID, credential.ID)
	}
	return s.sharing.DeleteGroupShare(ctx, share)
}

// ListUserShares returns every user a binding is shared with.
func (s *SSHEndpointCredentialSharingService) ListUserShares(ctx context.Context, credentialID string) ([]dto.SSHEndpointCredentialUserSharingResponse, error) {
	credential, err := s.requireControlledCredential(ctx, credentialID)
	if err != nil {
		return nil, err
	}
	shares, err := s.sharing.FindUserSharesByCredentialID(ctx, credential.ID)
	if err != nil {
		return nil, err
	}
	return dto.ToUserSharingResponses(shares), nil
}

// ShareWithUser grants one user access to a binding.
//
// Sharing with the owner is refused rather than stored: it would grant nothing the
// owner does not already have, and a share that looks revocable but is not is worse
// than an error.
func (s *SSHEndpointCredentialSharingService) ShareWithUser(ctx context.Context, credentialID string, req *dto.SSHEndpointCredentialUserSharingRequest) (*dto.SSHEndpointCredentialUserSharingResponse, error) {
	credential, err := s.requireControlledCredential(ctx, credentialID)
	if err != nil {
		return nil, err
	}
	if credential.OwnedBy(req.UserID) {
		return nil, httpx.Conflict("User %s already owns SSH endpoint credential binding %s", req.UserID, credential.ID)
	}

	var out dto.SSHEndpointCredentialUserSharingResponse
	err = s.db.WithContext(ctx).Transaction(func(tx *gorm.DB) error {
		sharing, users := s.sharing.WithTx(tx), s.users.WithTx(tx)

		if _, err := users.FindByID(ctx, req.UserID); err != nil {
			return notFoundAs(err, "User not found with ID: %s", req.UserID)
		}
		if _, err := sharing.FindUserShareByUserID(ctx, credential.ID, req.UserID); err == nil {
			return httpx.Conflict("SSH endpoint credential binding %s is already shared with user %s",
				credential.ID, req.UserID)
		} else if !errors.Is(err, gorm.ErrRecordNotFound) {
			return err
		}

		permission := req.Grant()
		share := &model.SSHEndpointCredentialUserSharing{
			SSHEndpointCredentialID: &credential.ID,
			UserID:                  &req.UserID,
			Permission:              &permission,
		}
		if err := sharing.SaveUserShare(ctx, share); err != nil {
			return err
		}
		out = dto.ToUserSharingResponse(share)
		return nil
	})
	if err != nil {
		return nil, err
	}
	return &out, nil
}

// UpdateUserShare changes what a user share grants.
func (s *SSHEndpointCredentialSharingService) UpdateUserShare(ctx context.Context, credentialID, sharingID string, req *dto.SharingUpdate) (*dto.SSHEndpointCredentialUserSharingResponse, error) {
	credential, err := s.requireControlledCredential(ctx, credentialID)
	if err != nil {
		return nil, err
	}
	share, err := s.sharing.FindUserShare(ctx, credential.ID, sharingID)
	if err != nil {
		return nil, notFoundAs(err, "User sharing not found: %s on binding %s", sharingID, credential.ID)
	}

	share.Permission = req.Permission
	if err := s.sharing.SaveUserShare(ctx, share); err != nil {
		return nil, err
	}
	out := dto.ToUserSharingResponse(share)
	return &out, nil
}

// RevokeUserShare withdraws a user's access.
func (s *SSHEndpointCredentialSharingService) RevokeUserShare(ctx context.Context, credentialID, sharingID string) error {
	credential, err := s.requireControlledCredential(ctx, credentialID)
	if err != nil {
		return err
	}
	share, err := s.sharing.FindUserShare(ctx, credential.ID, sharingID)
	if err != nil {
		return notFoundAs(err, "User sharing not found: %s on binding %s", sharingID, credential.ID)
	}
	return s.sharing.DeleteUserShare(ctx, share)
}

func (s *SSHEndpointCredentialSharingService) requireControlledCredential(ctx context.Context, credentialID string) (*model.SSHEndpointCredential, error) {
	credential, err := s.requireCredential(ctx, credentialID)
	if err != nil {
		return nil, err
	}
	if err := s.requireControl(ctx, credential); err != nil {
		return nil, err
	}
	return credential, nil
}
