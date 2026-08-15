package service

import (
	"context"
	"errors"
	"time"

	"gorm.io/gorm"

	"github.com/apache/airavata/internal/auth"
	"github.com/apache/airavata/internal/httpx"

	computesvc "github.com/apache/airavata/api/compute/service"
	dto "github.com/apache/airavata/api/data/dto"
	model "github.com/apache/airavata/api/data/model"
	"github.com/apache/airavata/api/data/repository"
	iamrepo "github.com/apache/airavata/api/iam/repository"
)

// productAccess resolves what the calling principal may do with a product, by loading
// its shares and handing them to the shared resolver.
type productAccess struct {
	access
	products *repository.DataProductRepository
	sharing  *repository.DataProductSharingRepository
}

func (a productAccess) withTx(tx *gorm.DB) productAccess {
	return productAccess{
		access:   a.access.withTx(tx),
		products: a.products.WithTx(tx),
		sharing:  a.sharing.WithTx(tx),
	}
}

// requireProduct loads a product or reports 404.
func (a productAccess) requireProduct(ctx context.Context, id string) (*model.DataProduct, error) {
	product, err := a.products.FindByID(ctx, id)
	if err != nil {
		return nil, notFoundAs(err, "Data product not found: %s", id)
	}
	return product, nil
}

// permissionOf returns the caller's effective permission on product and whether they
// control it.
func (a productAccess) permissionOf(ctx context.Context, product *model.DataProduct) (permission, bool, error) {
	userShares, err := a.sharing.FindUserSharesByProductID(ctx, product.ID)
	if err != nil {
		return permNone, false, err
	}
	groupShares, err := a.sharing.FindGroupSharesByProductID(ctx, product.ID)
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
	return a.access.permissionOf(ctx, product.OwnerID, users, groups)
}

// require checks that the caller holds at least want.
func (a productAccess) require(ctx context.Context, product *model.DataProduct, want permission) (permission, bool, error) {
	held, controls, err := a.permissionOf(ctx, product)
	if err != nil {
		return permNone, false, err
	}
	if !held.Allows(want) {
		return permNone, false, httpx.Forbidden(
			"Access denied: data product %s is not shared with you for %s", product.ID, want)
	}
	return held, controls, nil
}

// requireControl allows only the owner and platform admins.
func (a productAccess) requireControl(ctx context.Context, product *model.DataProduct) error {
	_, controls, err := a.permissionOf(ctx, product)
	if err != nil {
		return err
	}
	if !controls {
		return httpx.Forbidden("Access denied: only the owner of data product %s may do that", product.ID)
	}
	return nil
}

func permissionString[T ~string](p *T) *string {
	if p == nil {
		return nil
	}
	s := string(*p)
	return &s
}

// DataProductService manages registered datasets.
//
// A product belongs to whoever registered it, and everyone else reaches it only
// through a sharing rule — there is no listing an ordinary caller can use to discover
// products that were never shared with them.
type DataProductService struct {
	productAccess
	db             *gorm.DB
	storages       *repository.SCPDataStorageRepository
	storageSharing *repository.SCPDataStorageSharingRepository
	credentials    *computesvc.CredentialAccess
	users          *iamrepo.UserRepository
}

// NewDataProductService returns a data product service.
func NewDataProductService(
	db *gorm.DB,
	products *repository.DataProductRepository,
	sharing *repository.DataProductSharingRepository,
	storages *repository.SCPDataStorageRepository,
	storageSharing *repository.SCPDataStorageSharingRepository,
	credentials *computesvc.CredentialAccess,
	users *iamrepo.UserRepository,
	members *iamrepo.GroupMemberRepository,
) *DataProductService {
	return &DataProductService{
		productAccess: productAccess{
			access:   access{members: members},
			products: products,
			sharing:  sharing,
		},
		db:             db,
		storages:       storages,
		storageSharing: storageSharing,
		credentials:    credentials,
		users:          users,
	}
}

// List returns every product across every owner. Admin only — it names who holds what
// data.
func (s *DataProductService) List(ctx context.Context) ([]dto.DataProductResponse, error) {
	if _, err := auth.RequireAdmin(ctx); err != nil {
		return nil, err
	}
	products, err := s.products.FindAll(ctx)
	if err != nil {
		return nil, err
	}
	return dto.ToDataProductResponses(products), nil
}

// ListMine returns the caller's own products.
func (s *DataProductService) ListMine(ctx context.Context) ([]dto.DataProductResponse, error) {
	principal, err := auth.RequireAuthenticated(ctx)
	if err != nil {
		return nil, err
	}
	products, err := s.products.FindByOwnerID(ctx, principal.Name)
	if err != nil {
		return nil, err
	}
	return dto.ToDataProductResponses(products), nil
}

// ListSharedWithMe returns the products other users have shared with the caller,
// directly or through a group, each carrying what it grants them.
func (s *DataProductService) ListSharedWithMe(ctx context.Context) ([]dto.DataProductResponse, error) {
	principal, err := auth.RequireAuthenticated(ctx)
	if err != nil {
		return nil, err
	}
	products, err := s.products.FindSharedWith(ctx, principal.Name)
	if err != nil {
		return nil, err
	}

	out := make([]dto.DataProductResponse, 0, len(products))
	for i := range products {
		// Re-resolving per product keeps the reported permission honest when a user
		// share and a group share reach the same product with different grants.
		held, _, err := s.permissionOf(ctx, &products[i])
		if err != nil {
			return nil, err
		}
		if held == permNone {
			continue
		}
		out = append(out, dto.ToDataProductResponseWith(&products[i], string(held)))
	}
	return out, nil
}

// Get returns one product, to anyone holding READ on it.
func (s *DataProductService) Get(ctx context.Context, id string) (*dto.DataProductResponse, error) {
	product, err := s.requireProduct(ctx, id)
	if err != nil {
		return nil, err
	}
	held, _, err := s.require(ctx, product, permRead)
	if err != nil {
		return nil, err
	}
	out := dto.ToDataProductResponseWith(product, string(held))
	return &out, nil
}

// Create registers a dataset owned by the calling user.
//
// The storage it names must be one the caller can already reach: registering data into
// a storage nobody shared with them would be a way to have the platform touch a host
// they have no standing on.
func (s *DataProductService) Create(ctx context.Context, req *dto.DataProductRequest) (*dto.DataProductResponse, error) {
	principal, err := auth.RequireAuthenticated(ctx)
	if err != nil {
		return nil, err
	}

	var out dto.DataProductResponse
	err = s.db.WithContext(ctx).Transaction(func(tx *gorm.DB) error {
		products, users := s.products.WithTx(tx), s.users.WithTx(tx)

		owner, err := users.FindByID(ctx, principal.Name)
		if err != nil {
			return notFoundAs(err, "No user record found for authenticated principal: %s", principal.Name)
		}
		if err := s.resolveReferences(ctx, tx, req); err != nil {
			return err
		}

		status := model.ProvisionStatusRegistered
		product := &model.DataProduct{
			OwnerID:         &owner.ID,
			ProvisionStatus: &status,
			CreatedAt:       time.Now().UnixMilli(),
		}
		dto.ApplyDataProductRequest(product, req)
		if err := products.Save(ctx, product); err != nil {
			return err
		}
		out = dto.ToDataProductResponseWith(product, string(permWrite))
		return nil
	})
	if err != nil {
		return nil, err
	}
	return &out, nil
}

// Update changes a product's own fields. It needs WRITE, which a share can confer.
//
// The owner, the provision status and the creation time are left alone: re-deriving
// the owner from the caller's token would hand the product to whichever admin — or
// grantee — happened to issue the request.
func (s *DataProductService) Update(ctx context.Context, id string, req *dto.DataProductRequest) (*dto.DataProductResponse, error) {
	var out dto.DataProductResponse
	err := s.db.WithContext(ctx).Transaction(func(tx *gorm.DB) error {
		products := s.products.WithTx(tx)

		product, err := products.FindByID(ctx, id)
		if err != nil {
			return notFoundAs(err, "Data product not found: %s", id)
		}
		held, _, err := s.productAccess.withTx(tx).require(ctx, product, permWrite)
		if err != nil {
			return err
		}
		if err := s.resolveReferences(ctx, tx, req); err != nil {
			return err
		}

		dto.ApplyDataProductRequest(product, req)
		if err := products.Save(ctx, product); err != nil {
			return err
		}
		out = dto.ToDataProductResponseWith(product, string(held))
		return nil
	})
	if err != nil {
		return nil, err
	}
	return &out, nil
}

// Delete removes a product and every share of it, for its owner or an admin.
//
// The shares go first and in the same transaction: their foreign keys are RESTRICT, so
// leaving them would turn an ordinary delete into a constraint violation.
func (s *DataProductService) Delete(ctx context.Context, id string) error {
	product, err := s.requireProduct(ctx, id)
	if err != nil {
		return err
	}
	if err := s.requireControl(ctx, product); err != nil {
		return err
	}

	return s.db.WithContext(ctx).Transaction(func(tx *gorm.DB) error {
		if err := s.sharing.WithTx(tx).DeleteByProductID(ctx, product.ID); err != nil {
			return err
		}
		return s.products.WithTx(tx).Delete(ctx, product)
	})
}

// resolveReferences checks the storage and credential a request names.
//
// Both have to be reachable by the caller, and neither carries a foreign key, so this
// is the only thing standing between a request and a dangling reference. The
// credential must also belong to the storage's own endpoint: one for a different host
// could never move this data, and accepting it would leave a product that looks
// transferable and is not.
func (s *DataProductService) resolveReferences(ctx context.Context, tx *gorm.DB, req *dto.DataProductRequest) error {
	storage, err := s.storages.WithTx(tx).FindByID(ctx, req.DataStorageID)
	if err != nil {
		return notFoundAs(err, "SCP data storage not found: %s", req.DataStorageID)
	}
	if err := requireStorageReadable(ctx, s.access.withTx(tx), s.storageSharing.WithTx(tx), storage); err != nil {
		return err
	}

	if req.CredentialID == nil {
		return nil
	}
	credential, err := s.credentials.WithTx(tx).RequireUsable(ctx, *req.CredentialID)
	if err != nil {
		return err
	}
	if !sameEndpoint(credential.SSHEndpointID, storage.SSHEndpointID) {
		return httpx.BadRequest(
			"SSH endpoint credential %s is not for the host SCP data storage %s stages through",
			credential.ID, storage.ID)
	}
	return nil
}

func sameEndpoint(a, b *string) bool {
	return a != nil && b != nil && *a == *b
}

// DataProductSharingService manages who, besides the owner, may reach a product.
//
// Only the owner (or a platform admin) may read or change the share list: it names who
// holds a dataset, which is more than a grantee needs to know.
type DataProductSharingService struct {
	productAccess
	db     *gorm.DB
	groups *iamrepo.GroupRepository
	users  *iamrepo.UserRepository
}

// NewDataProductSharingService returns a product sharing service.
func NewDataProductSharingService(
	db *gorm.DB,
	products *repository.DataProductRepository,
	sharing *repository.DataProductSharingRepository,
	groups *iamrepo.GroupRepository,
	users *iamrepo.UserRepository,
	members *iamrepo.GroupMemberRepository,
) *DataProductSharingService {
	return &DataProductSharingService{
		productAccess: productAccess{
			access:   access{members: members},
			products: products,
			sharing:  sharing,
		},
		db:     db,
		groups: groups,
		users:  users,
	}
}

// ListGroupShares returns every group a product is shared with.
func (s *DataProductSharingService) ListGroupShares(ctx context.Context, productID string) ([]dto.DataProductGroupSharingResponse, error) {
	product, err := s.requireControlledProduct(ctx, productID)
	if err != nil {
		return nil, err
	}
	shares, err := s.sharing.FindGroupSharesByProductID(ctx, product.ID)
	if err != nil {
		return nil, err
	}
	return dto.ToDataProductGroupSharingResponses(shares), nil
}

// ShareWithGroup grants a group access to a product.
func (s *DataProductSharingService) ShareWithGroup(ctx context.Context, productID string, req *dto.DataProductGroupSharingRequest) (*dto.DataProductGroupSharingResponse, error) {
	product, err := s.requireControlledProduct(ctx, productID)
	if err != nil {
		return nil, err
	}

	var out dto.DataProductGroupSharingResponse
	err = s.db.WithContext(ctx).Transaction(func(tx *gorm.DB) error {
		sharing, groups := s.sharing.WithTx(tx), s.groups.WithTx(tx)

		if _, err := groups.FindByID(ctx, req.GroupID); err != nil {
			return notFoundAs(err, "Group not found: %s", req.GroupID)
		}
		if _, err := sharing.FindGroupShareByGroupID(ctx, product.ID, req.GroupID); err == nil {
			return httpx.Conflict("Data product %s is already shared with group %s", product.ID, req.GroupID)
		} else if !errors.Is(err, gorm.ErrRecordNotFound) {
			return err
		}

		permission := req.Grant()
		share := &model.DataProductGroupSharing{
			DataProductID: &product.ID,
			GroupID:       &req.GroupID,
			Permission:    &permission,
		}
		if err := sharing.SaveGroupShare(ctx, share); err != nil {
			return err
		}
		out = dto.ToDataProductGroupSharingResponse(share)
		return nil
	})
	if err != nil {
		return nil, err
	}
	return &out, nil
}

// UpdateGroupShare changes what a group share grants.
func (s *DataProductSharingService) UpdateGroupShare(ctx context.Context, productID, sharingID string, req *dto.DataProductSharingUpdate) (*dto.DataProductGroupSharingResponse, error) {
	product, err := s.requireControlledProduct(ctx, productID)
	if err != nil {
		return nil, err
	}
	share, err := s.sharing.FindGroupShare(ctx, product.ID, sharingID)
	if err != nil {
		return nil, notFoundAs(err, "Group sharing not found: %s on data product %s", sharingID, product.ID)
	}

	share.Permission = req.Permission
	if err := s.sharing.SaveGroupShare(ctx, share); err != nil {
		return nil, err
	}
	out := dto.ToDataProductGroupSharingResponse(share)
	return &out, nil
}

// RevokeGroupShare withdraws a group's access.
func (s *DataProductSharingService) RevokeGroupShare(ctx context.Context, productID, sharingID string) error {
	product, err := s.requireControlledProduct(ctx, productID)
	if err != nil {
		return err
	}
	share, err := s.sharing.FindGroupShare(ctx, product.ID, sharingID)
	if err != nil {
		return notFoundAs(err, "Group sharing not found: %s on data product %s", sharingID, product.ID)
	}
	return s.sharing.DeleteGroupShare(ctx, share)
}

// ListUserShares returns every user a product is shared with.
func (s *DataProductSharingService) ListUserShares(ctx context.Context, productID string) ([]dto.DataProductUserSharingResponse, error) {
	product, err := s.requireControlledProduct(ctx, productID)
	if err != nil {
		return nil, err
	}
	shares, err := s.sharing.FindUserSharesByProductID(ctx, product.ID)
	if err != nil {
		return nil, err
	}
	return dto.ToDataProductUserSharingResponses(shares), nil
}

// ShareWithUser grants one user access to a product.
//
// Sharing with the owner is refused rather than stored: it would grant nothing the
// owner does not already have.
func (s *DataProductSharingService) ShareWithUser(ctx context.Context, productID string, req *dto.DataProductUserSharingRequest) (*dto.DataProductUserSharingResponse, error) {
	product, err := s.requireControlledProduct(ctx, productID)
	if err != nil {
		return nil, err
	}
	if product.OwnedBy(req.UserID) {
		return nil, httpx.Conflict("User %s already owns data product %s", req.UserID, product.ID)
	}

	var out dto.DataProductUserSharingResponse
	err = s.db.WithContext(ctx).Transaction(func(tx *gorm.DB) error {
		sharing, users := s.sharing.WithTx(tx), s.users.WithTx(tx)

		if _, err := users.FindByID(ctx, req.UserID); err != nil {
			return notFoundAs(err, "User not found with ID: %s", req.UserID)
		}
		if _, err := sharing.FindUserShareByUserID(ctx, product.ID, req.UserID); err == nil {
			return httpx.Conflict("Data product %s is already shared with user %s", product.ID, req.UserID)
		} else if !errors.Is(err, gorm.ErrRecordNotFound) {
			return err
		}

		permission := req.Grant()
		share := &model.DataProductUserSharing{
			DataProductID: &product.ID,
			UserID:        &req.UserID,
			Permission:    &permission,
		}
		if err := sharing.SaveUserShare(ctx, share); err != nil {
			return err
		}
		out = dto.ToDataProductUserSharingResponse(share)
		return nil
	})
	if err != nil {
		return nil, err
	}
	return &out, nil
}

// UpdateUserShare changes what a user share grants.
func (s *DataProductSharingService) UpdateUserShare(ctx context.Context, productID, sharingID string, req *dto.DataProductSharingUpdate) (*dto.DataProductUserSharingResponse, error) {
	product, err := s.requireControlledProduct(ctx, productID)
	if err != nil {
		return nil, err
	}
	share, err := s.sharing.FindUserShare(ctx, product.ID, sharingID)
	if err != nil {
		return nil, notFoundAs(err, "User sharing not found: %s on data product %s", sharingID, product.ID)
	}

	share.Permission = req.Permission
	if err := s.sharing.SaveUserShare(ctx, share); err != nil {
		return nil, err
	}
	out := dto.ToDataProductUserSharingResponse(share)
	return &out, nil
}

// RevokeUserShare withdraws a user's access.
func (s *DataProductSharingService) RevokeUserShare(ctx context.Context, productID, sharingID string) error {
	product, err := s.requireControlledProduct(ctx, productID)
	if err != nil {
		return err
	}
	share, err := s.sharing.FindUserShare(ctx, product.ID, sharingID)
	if err != nil {
		return notFoundAs(err, "User sharing not found: %s on data product %s", sharingID, product.ID)
	}
	return s.sharing.DeleteUserShare(ctx, share)
}

func (s *DataProductSharingService) requireControlledProduct(ctx context.Context, productID string) (*model.DataProduct, error) {
	product, err := s.requireProduct(ctx, productID)
	if err != nil {
		return nil, err
	}
	if err := s.requireControl(ctx, product); err != nil {
		return nil, err
	}
	return product, nil
}
