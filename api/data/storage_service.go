package data

import (
	"context"
	"errors"

	"gorm.io/gorm"

	"github.com/apache/airavata/api/compute"
	"github.com/apache/airavata/api/iam"
	"github.com/apache/airavata/internal/auth"
	"github.com/apache/airavata/internal/httpx"

	dto "github.com/apache/airavata/api/data/dto"
	model "github.com/apache/airavata/api/data/model"
)

// storageAccess resolves what the calling principal may do with a storage.
//
// Same model as a data product: strongest of ownership, a user share, and a group
// share reaching an active membership, with platform admins treated as owners.
// Control — deleting a storage and managing its shares — is not reachable through a
// share.
type storageAccess struct {
	access
	storages *SCPDataStorageRepository
	sharing  *SCPDataStorageSharingRepository
}

func (a storageAccess) withTx(tx *gorm.DB) storageAccess {
	return storageAccess{
		access:   a.access.withTx(tx),
		storages: a.storages.WithTx(tx),
		sharing:  a.sharing.WithTx(tx),
	}
}

// requireStorage loads a storage or reports 404.
func (a storageAccess) requireStorage(ctx context.Context, id string) (*model.SCPDataStorage, error) {
	storage, err := a.storages.FindByID(ctx, id)
	if err != nil {
		return nil, notFoundAs(err, "SCP data storage not found: %s", id)
	}
	return storage, nil
}

// permissionOf returns the caller's effective permission on storage and whether they
// control it.
func (a storageAccess) permissionOf(ctx context.Context, storage *model.SCPDataStorage) (permission, bool, error) {
	userShares, err := a.sharing.FindUserSharesByStorageID(ctx, storage.ID)
	if err != nil {
		return permNone, false, err
	}
	groupShares, err := a.sharing.FindGroupSharesByStorageID(ctx, storage.ID)
	if err != nil {
		return permNone, false, err
	}

	users := make([]share, 0, len(userShares))
	for i := range userShares {
		users = append(users, newShare(userShares[i].UserID, permissionString(userShares[i].Permission)))
	}
	groups := make([]share, 0, len(groupShares))
	for i := range groupShares {
		groups = append(groups, newShare(groupShares[i].GroupID, permissionString(groupShares[i].Permission)))
	}
	return a.access.permissionOf(ctx, storage.OwnerID, users, groups)
}

// require checks that the caller holds at least want.
func (a storageAccess) require(ctx context.Context, storage *model.SCPDataStorage, want permission) (permission, error) {
	held, _, err := a.permissionOf(ctx, storage)
	if err != nil {
		return permNone, err
	}
	if !held.Allows(want) {
		return permNone, httpx.Forbidden(
			"Access denied: SCP data storage %s is not shared with you for %s", storage.ID, want)
	}
	return held, nil
}

// requireControl allows only the owner and platform admins.
func (a storageAccess) requireControl(ctx context.Context, storage *model.SCPDataStorage) error {
	_, controls, err := a.permissionOf(ctx, storage)
	if err != nil {
		return err
	}
	if !controls {
		return httpx.Forbidden("Access denied: only the owner of SCP data storage %s may do that", storage.ID)
	}
	return nil
}

// requireStorageReadable is the check the product service runs before letting a
// dataset be registered into a storage. It lives here so both services read the same
// rule.
func requireStorageReadable(ctx context.Context, base access, sharing *SCPDataStorageSharingRepository, storage *model.SCPDataStorage) error {
	a := storageAccess{access: base, sharing: sharing}
	_, err := a.require(ctx, storage, permRead)
	return err
}

// SCPDataStorageService manages the storages datasets are staged through.
//
// Registering one is self-service: any authenticated caller may declare a storage on a
// host from the endpoint catalog, and it belongs to them. Everyone else reaches it
// through its sharing rules.
type SCPDataStorageService struct {
	storageAccess
	db        *gorm.DB
	endpoints *compute.SSHEndpointRepository
	products  *DataProductRepository
	users     *iam.UserRepository
}

// NewSCPDataStorageService returns a storage service.
func NewSCPDataStorageService(
	db *gorm.DB,
	storages *SCPDataStorageRepository,
	sharing *SCPDataStorageSharingRepository,
	endpoints *compute.SSHEndpointRepository,
	products *DataProductRepository,
	users *iam.UserRepository,
	members *iam.GroupMemberRepository,
) *SCPDataStorageService {
	return &SCPDataStorageService{
		storageAccess: storageAccess{
			access:   access{members: members},
			storages: storages,
			sharing:  sharing,
		},
		db:        db,
		endpoints: endpoints,
		products:  products,
		users:     users,
	}
}

// List returns every storage across every owner. Admin only — it names who stages what
// where.
func (s *SCPDataStorageService) List(ctx context.Context) ([]dto.SCPDataStorageResponse, error) {
	if _, err := auth.RequireAdmin(ctx); err != nil {
		return nil, err
	}
	storages, err := s.storages.FindAll(ctx)
	if err != nil {
		return nil, err
	}
	return dto.ToSCPDataStorageResponses(storages), nil
}

// ListMine returns the caller's own storages.
func (s *SCPDataStorageService) ListMine(ctx context.Context) ([]dto.SCPDataStorageResponse, error) {
	principal, err := auth.RequireAuthenticated(ctx)
	if err != nil {
		return nil, err
	}
	storages, err := s.storages.FindByOwnerID(ctx, principal.Name)
	if err != nil {
		return nil, err
	}
	return dto.ToSCPDataStorageResponses(storages), nil
}

// ListSharedWithMe returns the storages other users have shared with the caller,
// directly or through a group, each carrying what it grants them.
func (s *SCPDataStorageService) ListSharedWithMe(ctx context.Context) ([]dto.SCPDataStorageResponse, error) {
	principal, err := auth.RequireAuthenticated(ctx)
	if err != nil {
		return nil, err
	}
	storages, err := s.storages.FindSharedWith(ctx, principal.Name)
	if err != nil {
		return nil, err
	}

	out := make([]dto.SCPDataStorageResponse, 0, len(storages))
	for i := range storages {
		held, _, err := s.permissionOf(ctx, &storages[i])
		if err != nil {
			return nil, err
		}
		if held == permNone {
			continue
		}
		out = append(out, dto.ToSCPDataStorageResponseWith(&storages[i], string(held)))
	}
	return out, nil
}

// Get returns one storage, to an admin or to anyone a share reaches.
func (s *SCPDataStorageService) Get(ctx context.Context, id string) (*dto.SCPDataStorageResponse, error) {
	storage, err := s.requireStorage(ctx, id)
	if err != nil {
		return nil, err
	}
	held, err := s.require(ctx, storage, permRead)
	if err != nil {
		return nil, err
	}
	out := dto.ToSCPDataStorageResponseWith(storage, string(held))
	return &out, nil
}

// Create registers a storage owned by the calling user, on an existing SSH endpoint.
//
// The owner is taken from the token, so there is no way to register a storage on
// someone else's behalf.
func (s *SCPDataStorageService) Create(ctx context.Context, req *dto.SCPDataStorageRequest) (*dto.SCPDataStorageResponse, error) {
	principal, err := auth.RequireAuthenticated(ctx)
	if err != nil {
		return nil, err
	}

	var out dto.SCPDataStorageResponse
	err = s.db.WithContext(ctx).Transaction(func(tx *gorm.DB) error {
		storages, endpoints := s.storages.WithTx(tx), s.endpoints.WithTx(tx)

		owner, err := s.users.WithTx(tx).FindByID(ctx, principal.Name)
		if err != nil {
			return notFoundAs(err, "No user record found for authenticated principal: %s", principal.Name)
		}
		endpoint, err := endpoints.FindByID(ctx, req.SSHEndpointID)
		if err != nil {
			return notFoundAs(err, "SSH endpoint not found: %s", req.SSHEndpointID)
		}

		storage := &model.SCPDataStorage{
			SSHEndpointID: &endpoint.ID,
			SSHEndpoint:   endpoint,
			OwnerID:       &owner.ID,
		}
		dto.ApplySCPDataStorageRequest(storage, req)
		if err := storages.Save(ctx, storage); err != nil {
			return err
		}
		out = dto.ToSCPDataStorageResponseWith(storage, string(permWrite))
		return nil
	})
	if err != nil {
		return nil, err
	}
	return &out, nil
}

// Update changes a storage, including which endpoint it stages through. It needs
// WRITE, which a share can confer.
//
// The owner is deliberately left alone: re-deriving it from the caller's token would
// hand the storage to whichever admin — or grantee — happened to issue the request.
func (s *SCPDataStorageService) Update(ctx context.Context, id string, req *dto.SCPDataStorageRequest) (*dto.SCPDataStorageResponse, error) {
	var out dto.SCPDataStorageResponse
	err := s.db.WithContext(ctx).Transaction(func(tx *gorm.DB) error {
		storages, endpoints := s.storages.WithTx(tx), s.endpoints.WithTx(tx)

		storage, err := storages.FindByID(ctx, id)
		if err != nil {
			return notFoundAs(err, "SCP data storage not found: %s", id)
		}
		held, err := s.storageAccess.withTx(tx).require(ctx, storage, permWrite)
		if err != nil {
			return err
		}
		endpoint, err := endpoints.FindByID(ctx, req.SSHEndpointID)
		if err != nil {
			return notFoundAs(err, "SSH endpoint not found: %s", req.SSHEndpointID)
		}

		dto.ApplySCPDataStorageRequest(storage, req)
		storage.SSHEndpointID = &endpoint.ID
		storage.SSHEndpoint = endpoint
		if err := storages.Save(ctx, storage); err != nil {
			return err
		}
		out = dto.ToSCPDataStorageResponseWith(storage, string(held))
		return nil
	})
	if err != nil {
		return nil, err
	}
	return &out, nil
}

// Delete removes a storage nothing is staged on, together with its shares.
//
// Products are checked first: a product's storage id carries no foreign key — it is
// qualified by a storage *type* — so nothing at the database level would stop this
// from orphaning them.
func (s *SCPDataStorageService) Delete(ctx context.Context, id string) error {
	storage, err := s.requireStorage(ctx, id)
	if err != nil {
		return err
	}
	if err := s.requireControl(ctx, storage); err != nil {
		return err
	}
	products, err := s.products.FindByDataStorageID(ctx, storage.ID)
	if err != nil {
		return err
	}
	if len(products) > 0 {
		return httpx.Conflict("SCP data storage %s still holds %d data product(s)", storage.ID, len(products))
	}

	return s.db.WithContext(ctx).Transaction(func(tx *gorm.DB) error {
		if err := s.sharing.WithTx(tx).DeleteByStorageID(ctx, storage.ID); err != nil {
			return err
		}
		return s.storages.WithTx(tx).Delete(ctx, storage)
	})
}

// SCPDataStorageSharingService manages who, besides the owner, may use a storage.
//
// Only the owner (or a platform admin) may read or change the share list: it names who
// can reach a host and a path, which is more than a grantee needs to know.
type SCPDataStorageSharingService struct {
	storageAccess
	db     *gorm.DB
	groups *iam.GroupRepository
	users  *iam.UserRepository
}

// NewSCPDataStorageSharingService returns a storage sharing service.
func NewSCPDataStorageSharingService(
	db *gorm.DB,
	storages *SCPDataStorageRepository,
	sharing *SCPDataStorageSharingRepository,
	groups *iam.GroupRepository,
	users *iam.UserRepository,
	members *iam.GroupMemberRepository,
) *SCPDataStorageSharingService {
	return &SCPDataStorageSharingService{
		storageAccess: storageAccess{
			access:   access{members: members},
			storages: storages,
			sharing:  sharing,
		},
		db:     db,
		groups: groups,
		users:  users,
	}
}

// ListGroupShares returns every group a storage is shared with.
func (s *SCPDataStorageSharingService) ListGroupShares(ctx context.Context, storageID string) ([]dto.SCPDataStorageGroupSharingResponse, error) {
	storage, err := s.requireControlledStorage(ctx, storageID)
	if err != nil {
		return nil, err
	}
	shares, err := s.sharing.FindGroupSharesByStorageID(ctx, storage.ID)
	if err != nil {
		return nil, err
	}
	return dto.ToSCPDataStorageGroupSharingResponses(shares), nil
}

// ShareWithGroup grants a group access to a storage.
func (s *SCPDataStorageSharingService) ShareWithGroup(ctx context.Context, storageID string, req *dto.SCPDataStorageGroupSharingRequest) (*dto.SCPDataStorageGroupSharingResponse, error) {
	storage, err := s.requireControlledStorage(ctx, storageID)
	if err != nil {
		return nil, err
	}

	var out dto.SCPDataStorageGroupSharingResponse
	err = s.db.WithContext(ctx).Transaction(func(tx *gorm.DB) error {
		sharing, groups := s.sharing.WithTx(tx), s.groups.WithTx(tx)

		if _, err := groups.FindByID(ctx, req.GroupID); err != nil {
			return notFoundAs(err, "Group not found: %s", req.GroupID)
		}
		if _, err := sharing.FindGroupShareByGroupID(ctx, storage.ID, req.GroupID); err == nil {
			return httpx.Conflict("SCP data storage %s is already shared with group %s", storage.ID, req.GroupID)
		} else if !errors.Is(err, gorm.ErrRecordNotFound) {
			return err
		}

		permission := req.Grant()
		share := &model.SCPDataStorageGroupSharing{
			DataStorageID: &storage.ID,
			GroupID:       &req.GroupID,
			Permission:    &permission,
		}
		if err := sharing.SaveGroupShare(ctx, share); err != nil {
			return err
		}
		out = dto.ToSCPDataStorageGroupSharingResponse(share)
		return nil
	})
	if err != nil {
		return nil, err
	}
	return &out, nil
}

// UpdateGroupShare changes what a group share grants.
func (s *SCPDataStorageSharingService) UpdateGroupShare(ctx context.Context, storageID, sharingID string, req *dto.SCPDataStorageSharingUpdate) (*dto.SCPDataStorageGroupSharingResponse, error) {
	storage, err := s.requireControlledStorage(ctx, storageID)
	if err != nil {
		return nil, err
	}
	share, err := s.sharing.FindGroupShare(ctx, storage.ID, sharingID)
	if err != nil {
		return nil, notFoundAs(err, "Group sharing not found: %s on SCP data storage %s", sharingID, storage.ID)
	}

	share.Permission = req.Permission
	if err := s.sharing.SaveGroupShare(ctx, share); err != nil {
		return nil, err
	}
	out := dto.ToSCPDataStorageGroupSharingResponse(share)
	return &out, nil
}

// RevokeGroupShare withdraws a group's access.
func (s *SCPDataStorageSharingService) RevokeGroupShare(ctx context.Context, storageID, sharingID string) error {
	storage, err := s.requireControlledStorage(ctx, storageID)
	if err != nil {
		return err
	}
	share, err := s.sharing.FindGroupShare(ctx, storage.ID, sharingID)
	if err != nil {
		return notFoundAs(err, "Group sharing not found: %s on SCP data storage %s", sharingID, storage.ID)
	}
	return s.sharing.DeleteGroupShare(ctx, share)
}

// ListUserShares returns every user a storage is shared with.
func (s *SCPDataStorageSharingService) ListUserShares(ctx context.Context, storageID string) ([]dto.SCPDataStorageUserSharingResponse, error) {
	storage, err := s.requireControlledStorage(ctx, storageID)
	if err != nil {
		return nil, err
	}
	shares, err := s.sharing.FindUserSharesByStorageID(ctx, storage.ID)
	if err != nil {
		return nil, err
	}
	return dto.ToSCPDataStorageUserSharingResponses(shares), nil
}

// ShareWithUser grants one user access to a storage.
//
// Sharing with the owner is refused rather than stored: it would grant nothing the
// owner does not already have.
func (s *SCPDataStorageSharingService) ShareWithUser(ctx context.Context, storageID string, req *dto.SCPDataStorageUserSharingRequest) (*dto.SCPDataStorageUserSharingResponse, error) {
	storage, err := s.requireControlledStorage(ctx, storageID)
	if err != nil {
		return nil, err
	}
	if storage.OwnedBy(req.UserID) {
		return nil, httpx.Conflict("User %s already owns SCP data storage %s", req.UserID, storage.ID)
	}

	var out dto.SCPDataStorageUserSharingResponse
	err = s.db.WithContext(ctx).Transaction(func(tx *gorm.DB) error {
		sharing, users := s.sharing.WithTx(tx), s.users.WithTx(tx)

		if _, err := users.FindByID(ctx, req.UserID); err != nil {
			return notFoundAs(err, "User not found with ID: %s", req.UserID)
		}
		if _, err := sharing.FindUserShareByUserID(ctx, storage.ID, req.UserID); err == nil {
			return httpx.Conflict("SCP data storage %s is already shared with user %s", storage.ID, req.UserID)
		} else if !errors.Is(err, gorm.ErrRecordNotFound) {
			return err
		}

		permission := req.Grant()
		share := &model.SCPDataStorageUserSharing{
			DataStorageID: &storage.ID,
			UserID:        &req.UserID,
			Permission:    &permission,
		}
		if err := sharing.SaveUserShare(ctx, share); err != nil {
			return err
		}
		out = dto.ToSCPDataStorageUserSharingResponse(share)
		return nil
	})
	if err != nil {
		return nil, err
	}
	return &out, nil
}

// UpdateUserShare changes what a user share grants.
func (s *SCPDataStorageSharingService) UpdateUserShare(ctx context.Context, storageID, sharingID string, req *dto.SCPDataStorageSharingUpdate) (*dto.SCPDataStorageUserSharingResponse, error) {
	storage, err := s.requireControlledStorage(ctx, storageID)
	if err != nil {
		return nil, err
	}
	share, err := s.sharing.FindUserShare(ctx, storage.ID, sharingID)
	if err != nil {
		return nil, notFoundAs(err, "User sharing not found: %s on SCP data storage %s", sharingID, storage.ID)
	}

	share.Permission = req.Permission
	if err := s.sharing.SaveUserShare(ctx, share); err != nil {
		return nil, err
	}
	out := dto.ToSCPDataStorageUserSharingResponse(share)
	return &out, nil
}

// RevokeUserShare withdraws a user's access.
func (s *SCPDataStorageSharingService) RevokeUserShare(ctx context.Context, storageID, sharingID string) error {
	storage, err := s.requireControlledStorage(ctx, storageID)
	if err != nil {
		return err
	}
	share, err := s.sharing.FindUserShare(ctx, storage.ID, sharingID)
	if err != nil {
		return notFoundAs(err, "User sharing not found: %s on SCP data storage %s", sharingID, storage.ID)
	}
	return s.sharing.DeleteUserShare(ctx, share)
}

func (s *SCPDataStorageSharingService) requireControlledStorage(ctx context.Context, storageID string) (*model.SCPDataStorage, error) {
	storage, err := s.requireStorage(ctx, storageID)
	if err != nil {
		return nil, err
	}
	if err := s.requireControl(ctx, storage); err != nil {
		return nil, err
	}
	return storage, nil
}
