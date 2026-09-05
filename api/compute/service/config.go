package service

import (
	"context"
	"errors"

	"gorm.io/gorm"

	"github.com/apache/airavata/internal/auth"
	"github.com/apache/airavata/internal/httpx"

	dto "github.com/apache/airavata/api/compute/dto"
	model "github.com/apache/airavata/api/compute/model"
	"github.com/apache/airavata/api/compute/repository"
	credmodel "github.com/apache/airavata/api/credentials/model"
	credrepo "github.com/apache/airavata/api/credentials/repository"
	iamrepo "github.com/apache/airavata/api/iam/repository"
)

// configAccess resolves what the calling principal may do with a cluster config.
//
// Same model as an SCP data storage: strongest of ownership, a user share, and a group
// share reaching an active membership, with platform admins treated as owners. Control
// — deleting a config and managing its shares — is not reachable through a share,
// because deciding who else may submit under an identity stays with whoever registered
// it.
type configAccess struct {
	access
	configs *repository.SlurmClusterConfigRepository
	sharing *repository.SlurmClusterConfigSharingRepository
}

func (a configAccess) withTx(tx *gorm.DB) configAccess {
	return configAccess{
		access:  a.access.withTx(tx),
		configs: a.configs.WithTx(tx),
		sharing: a.sharing.WithTx(tx),
	}
}

// requireConfig loads a config or reports 404.
func (a configAccess) requireConfig(ctx context.Context, id string) (*model.SlurmClusterConfig, error) {
	config, err := a.configs.FindByID(ctx, id)
	if err != nil {
		return nil, notFoundAs(err, "Slurm cluster config not found: %s", id)
	}
	return config, nil
}

// permissionOf returns the caller's effective permission on config and whether they
// control it.
func (a configAccess) permissionOf(ctx context.Context, config *model.SlurmClusterConfig) (permission, bool, error) {
	userShares, err := a.sharing.FindUserSharesByConfigID(ctx, config.ID)
	if err != nil {
		return permNone, false, err
	}
	groupShares, err := a.sharing.FindGroupSharesByConfigID(ctx, config.ID)
	if err != nil {
		return permNone, false, err
	}

	users := make([]share, 0, len(userShares))
	for i := range userShares {
		users = append(users, newShare(userShares[i].UserID, userShares[i].Permission))
	}
	groups := make([]share, 0, len(groupShares))
	for i := range groupShares {
		groups = append(groups, newShare(groupShares[i].GroupID, groupShares[i].Permission))
	}
	return a.access.permissionOf(ctx, config.OwnerID, users, groups)
}

// require checks that the caller holds at least want.
func (a configAccess) require(ctx context.Context, config *model.SlurmClusterConfig, want permission) (permission, error) {
	held, _, err := a.permissionOf(ctx, config)
	if err != nil {
		return permNone, err
	}
	if !held.Allows(want) {
		return permNone, httpx.Forbidden(
			"Access denied: Slurm cluster config %s is not shared with you for %s", config.ID, want)
	}
	return held, nil
}

// requireControl allows only the owner and platform admins.
func (a configAccess) requireControl(ctx context.Context, config *model.SlurmClusterConfig) error {
	_, controls, err := a.permissionOf(ctx, config)
	if err != nil {
		return err
	}
	if !controls {
		return httpx.Forbidden("Access denied: only the owner of Slurm cluster config %s may do that", config.ID)
	}
	return nil
}

// SlurmClusterConfigService manages the login configs jobs are submitted through.
//
// Registering one is self-service: any authenticated caller may declare how they reach
// a cluster from the catalogue, under a key from the SSH key catalogue, and it belongs
// to them. Everyone else reaches it through its sharing rules.
type SlurmClusterConfigService struct {
	configAccess
	db       *gorm.DB
	clusters *repository.SlurmClusterRepository
	keys     *credrepo.SSHKeyRepository
	users    *iamrepo.UserRepository
}

// NewSlurmClusterConfigService returns a cluster config service.
func NewSlurmClusterConfigService(
	db *gorm.DB,
	configs *repository.SlurmClusterConfigRepository,
	sharing *repository.SlurmClusterConfigSharingRepository,
	clusters *repository.SlurmClusterRepository,
	keys *credrepo.SSHKeyRepository,
	users *iamrepo.UserRepository,
	members *iamrepo.GroupMemberRepository,
) *SlurmClusterConfigService {
	return &SlurmClusterConfigService{
		configAccess: configAccess{
			access:  access{members: members},
			configs: configs,
			sharing: sharing,
		},
		db:       db,
		clusters: clusters,
		keys:     keys,
		users:    users,
	}
}

// List returns every config across every owner. Admin only — it names who may submit
// as whom, and where.
func (s *SlurmClusterConfigService) List(ctx context.Context) ([]dto.SlurmClusterConfigResponse, error) {
	if _, err := auth.RequireAdmin(ctx); err != nil {
		return nil, err
	}
	configs, err := s.configs.FindAll(ctx)
	if err != nil {
		return nil, err
	}
	return dto.ToSlurmClusterConfigResponses(configs), nil
}

// ListMine returns the caller's own configs.
func (s *SlurmClusterConfigService) ListMine(ctx context.Context) ([]dto.SlurmClusterConfigResponse, error) {
	principal, err := auth.RequireAuthenticated(ctx)
	if err != nil {
		return nil, err
	}
	configs, err := s.configs.FindByOwnerID(ctx, principal.Name)
	if err != nil {
		return nil, err
	}
	return dto.ToSlurmClusterConfigResponses(configs), nil
}

// ListSharedWithMe returns the configs other users have shared with the caller,
// directly or through a group, each carrying what it grants them.
func (s *SlurmClusterConfigService) ListSharedWithMe(ctx context.Context) ([]dto.SlurmClusterConfigResponse, error) {
	principal, err := auth.RequireAuthenticated(ctx)
	if err != nil {
		return nil, err
	}
	configs, err := s.configs.FindSharedWith(ctx, principal.Name)
	if err != nil {
		return nil, err
	}

	out := make([]dto.SlurmClusterConfigResponse, 0, len(configs))
	for i := range configs {
		held, _, err := s.permissionOf(ctx, &configs[i])
		if err != nil {
			return nil, err
		}
		if held == permNone {
			continue
		}
		out = append(out, dto.ToSlurmClusterConfigResponseWith(&configs[i], string(held)))
	}
	return out, nil
}

// Get returns one config, to an admin or to anyone a share reaches.
func (s *SlurmClusterConfigService) Get(ctx context.Context, id string) (*dto.SlurmClusterConfigResponse, error) {
	config, err := s.requireConfig(ctx, id)
	if err != nil {
		return nil, err
	}
	held, err := s.require(ctx, config, permRead)
	if err != nil {
		return nil, err
	}
	out := dto.ToSlurmClusterConfigResponseWith(config, string(held))
	return &out, nil
}

// resolveReferences loads the cluster and the key a request names. Neither is created
// here, so an id that resolves to nothing is a 404 rather than a config pointing at a
// machine or a key that does not exist.
func (s *SlurmClusterConfigService) resolveReferences(ctx context.Context, tx *gorm.DB, req *dto.SlurmClusterConfigRequest) (*model.SlurmCluster, *credmodel.SSHKey, error) {
	cluster, err := s.clusters.WithTx(tx).FindByID(ctx, req.SlurmClusterID)
	if err != nil {
		return nil, nil, notFoundAs(err, "Slurm cluster not found: %s", req.SlurmClusterID)
	}
	key, err := s.keys.WithTx(tx).FindByID(ctx, req.SSHKeyID)
	if err != nil {
		return nil, nil, notFoundAs(err, "SSH key not found: %s", req.SSHKeyID)
	}
	return cluster, key, nil
}

// Create registers a config owned by the calling user, against an existing cluster and
// an existing SSH key.
//
// The owner is taken from the token, so there is no way to register a config on
// someone else's behalf.
func (s *SlurmClusterConfigService) Create(ctx context.Context, req *dto.SlurmClusterConfigRequest) (*dto.SlurmClusterConfigResponse, error) {
	principal, err := auth.RequireAuthenticated(ctx)
	if err != nil {
		return nil, err
	}

	var out dto.SlurmClusterConfigResponse
	err = s.db.WithContext(ctx).Transaction(func(tx *gorm.DB) error {
		configs := s.configs.WithTx(tx)

		owner, err := s.users.WithTx(tx).FindByID(ctx, principal.Name)
		if err != nil {
			return notFoundAs(err, "No user record found for authenticated principal: %s", principal.Name)
		}
		cluster, key, err := s.resolveReferences(ctx, tx, req)
		if err != nil {
			return err
		}

		config := &model.SlurmClusterConfig{
			SlurmClusterID: cluster.ID,
			SlurmCluster:   cluster,
			SSHKeyID:       &key.ID,
			SSHKey:         key,
			OwnerID:        &owner.ID,
		}
		dto.ApplySlurmClusterConfigRequest(config, req)
		if err := configs.Save(ctx, config); err != nil {
			return err
		}
		out = dto.ToSlurmClusterConfigResponseWith(config, string(permWrite))
		return nil
	})
	if err != nil {
		return nil, err
	}
	return &out, nil
}

// Update changes a config, including which cluster it logs in to and which key it
// presents. It needs WRITE, which a share can confer.
//
// The owner is deliberately left alone: re-deriving it from the caller's token would
// hand the config to whichever admin — or grantee — happened to issue the request.
func (s *SlurmClusterConfigService) Update(ctx context.Context, id string, req *dto.SlurmClusterConfigRequest) (*dto.SlurmClusterConfigResponse, error) {
	var out dto.SlurmClusterConfigResponse
	err := s.db.WithContext(ctx).Transaction(func(tx *gorm.DB) error {
		configs := s.configs.WithTx(tx)

		config, err := configs.FindByID(ctx, id)
		if err != nil {
			return notFoundAs(err, "Slurm cluster config not found: %s", id)
		}
		held, err := s.configAccess.withTx(tx).require(ctx, config, permWrite)
		if err != nil {
			return err
		}
		cluster, key, err := s.resolveReferences(ctx, tx, req)
		if err != nil {
			return err
		}

		dto.ApplySlurmClusterConfigRequest(config, req)
		config.SlurmClusterID = cluster.ID
		config.SlurmCluster = cluster
		config.SSHKeyID = &key.ID
		config.SSHKey = key
		if err := configs.Save(ctx, config); err != nil {
			return err
		}
		out = dto.ToSlurmClusterConfigResponseWith(config, string(held))
		return nil
	})
	if err != nil {
		return nil, err
	}
	return &out, nil
}

// Delete removes a config together with its shares. Owner and admins only: a grantee
// holding WRITE may edit a config, but retiring the identity everyone else reaches the
// cluster through is not theirs to do.
func (s *SlurmClusterConfigService) Delete(ctx context.Context, id string) error {
	config, err := s.requireConfig(ctx, id)
	if err != nil {
		return err
	}
	if err := s.requireControl(ctx, config); err != nil {
		return err
	}

	return s.db.WithContext(ctx).Transaction(func(tx *gorm.DB) error {
		if err := s.sharing.WithTx(tx).DeleteByConfigID(ctx, config.ID); err != nil {
			return err
		}
		return s.configs.WithTx(tx).Delete(ctx, config)
	})
}

// SlurmClusterConfigSharingService manages who, besides the owner, may use a config.
//
// Only the owner (or a platform admin) may read or change the share list: it names who
// can submit jobs as a particular account on a particular machine, which is more than a
// grantee needs to know.
type SlurmClusterConfigSharingService struct {
	configAccess
	db     *gorm.DB
	groups *iamrepo.GroupRepository
	users  *iamrepo.UserRepository
}

// NewSlurmClusterConfigSharingService returns a config sharing service.
func NewSlurmClusterConfigSharingService(
	db *gorm.DB,
	configs *repository.SlurmClusterConfigRepository,
	sharing *repository.SlurmClusterConfigSharingRepository,
	groups *iamrepo.GroupRepository,
	users *iamrepo.UserRepository,
	members *iamrepo.GroupMemberRepository,
) *SlurmClusterConfigSharingService {
	return &SlurmClusterConfigSharingService{
		configAccess: configAccess{
			access:  access{members: members},
			configs: configs,
			sharing: sharing,
		},
		db:     db,
		groups: groups,
		users:  users,
	}
}

// ListGroupShares returns every group a config is shared with.
func (s *SlurmClusterConfigSharingService) ListGroupShares(ctx context.Context, configID string) ([]dto.SlurmClusterConfigGroupSharingResponse, error) {
	config, err := s.requireControlledConfig(ctx, configID)
	if err != nil {
		return nil, err
	}
	shares, err := s.sharing.FindGroupSharesByConfigID(ctx, config.ID)
	if err != nil {
		return nil, err
	}
	return dto.ToSlurmClusterConfigGroupSharingResponses(shares), nil
}

// ShareWithGroup grants a group access to a config.
func (s *SlurmClusterConfigSharingService) ShareWithGroup(ctx context.Context, configID string, req *dto.SlurmClusterConfigGroupSharingRequest) (*dto.SlurmClusterConfigGroupSharingResponse, error) {
	config, err := s.requireControlledConfig(ctx, configID)
	if err != nil {
		return nil, err
	}

	var out dto.SlurmClusterConfigGroupSharingResponse
	err = s.db.WithContext(ctx).Transaction(func(tx *gorm.DB) error {
		sharing, groups := s.sharing.WithTx(tx), s.groups.WithTx(tx)

		if _, err := groups.FindByID(ctx, req.GroupID); err != nil {
			return notFoundAs(err, "Group not found: %s", req.GroupID)
		}
		if _, err := sharing.FindGroupShareByGroupID(ctx, config.ID, req.GroupID); err == nil {
			return httpx.Conflict("Slurm cluster config %s is already shared with group %s", config.ID, req.GroupID)
		} else if !errors.Is(err, gorm.ErrRecordNotFound) {
			return err
		}

		share := &model.SlurmClusterConfigGroupSharing{
			SlurmClusterConfigID: config.ID,
			GroupID:              req.GroupID,
			Permission:           req.Grant(),
		}
		if err := sharing.SaveGroupShare(ctx, share); err != nil {
			return err
		}
		out = dto.ToSlurmClusterConfigGroupSharingResponse(share)
		return nil
	})
	if err != nil {
		return nil, err
	}
	return &out, nil
}

// UpdateGroupShare changes what a group share grants.
func (s *SlurmClusterConfigSharingService) UpdateGroupShare(ctx context.Context, configID, sharingID string, req *dto.SlurmClusterConfigSharingUpdate) (*dto.SlurmClusterConfigGroupSharingResponse, error) {
	config, err := s.requireControlledConfig(ctx, configID)
	if err != nil {
		return nil, err
	}
	share, err := s.sharing.FindGroupShare(ctx, config.ID, sharingID)
	if err != nil {
		return nil, notFoundAs(err, "Group sharing not found: %s on Slurm cluster config %s", sharingID, config.ID)
	}

	share.Permission = *req.Permission
	if err := s.sharing.SaveGroupShare(ctx, share); err != nil {
		return nil, err
	}
	out := dto.ToSlurmClusterConfigGroupSharingResponse(share)
	return &out, nil
}

// RevokeGroupShare withdraws a group's access.
func (s *SlurmClusterConfigSharingService) RevokeGroupShare(ctx context.Context, configID, sharingID string) error {
	config, err := s.requireControlledConfig(ctx, configID)
	if err != nil {
		return err
	}
	share, err := s.sharing.FindGroupShare(ctx, config.ID, sharingID)
	if err != nil {
		return notFoundAs(err, "Group sharing not found: %s on Slurm cluster config %s", sharingID, config.ID)
	}
	return s.sharing.DeleteGroupShare(ctx, share)
}

// ListUserShares returns every user a config is shared with.
func (s *SlurmClusterConfigSharingService) ListUserShares(ctx context.Context, configID string) ([]dto.SlurmClusterConfigUserSharingResponse, error) {
	config, err := s.requireControlledConfig(ctx, configID)
	if err != nil {
		return nil, err
	}
	shares, err := s.sharing.FindUserSharesByConfigID(ctx, config.ID)
	if err != nil {
		return nil, err
	}
	return dto.ToSlurmClusterConfigUserSharingResponses(shares), nil
}

// ShareWithUser grants one user access to a config.
//
// Sharing with the owner is refused rather than stored: it would grant nothing the
// owner does not already have.
func (s *SlurmClusterConfigSharingService) ShareWithUser(ctx context.Context, configID string, req *dto.SlurmClusterConfigUserSharingRequest) (*dto.SlurmClusterConfigUserSharingResponse, error) {
	config, err := s.requireControlledConfig(ctx, configID)
	if err != nil {
		return nil, err
	}
	if config.OwnedBy(req.UserID) {
		return nil, httpx.Conflict("User %s already owns Slurm cluster config %s", req.UserID, config.ID)
	}

	var out dto.SlurmClusterConfigUserSharingResponse
	err = s.db.WithContext(ctx).Transaction(func(tx *gorm.DB) error {
		sharing, users := s.sharing.WithTx(tx), s.users.WithTx(tx)

		if _, err := users.FindByID(ctx, req.UserID); err != nil {
			return notFoundAs(err, "User not found with ID: %s", req.UserID)
		}
		if _, err := sharing.FindUserShareByUserID(ctx, config.ID, req.UserID); err == nil {
			return httpx.Conflict("Slurm cluster config %s is already shared with user %s", config.ID, req.UserID)
		} else if !errors.Is(err, gorm.ErrRecordNotFound) {
			return err
		}

		share := &model.SlurmClusterConfigUserSharing{
			SlurmClusterConfigID: config.ID,
			UserID:               req.UserID,
			Permission:           req.Grant(),
		}
		if err := sharing.SaveUserShare(ctx, share); err != nil {
			return err
		}
		out = dto.ToSlurmClusterConfigUserSharingResponse(share)
		return nil
	})
	if err != nil {
		return nil, err
	}
	return &out, nil
}

// UpdateUserShare changes what a user share grants.
func (s *SlurmClusterConfigSharingService) UpdateUserShare(ctx context.Context, configID, sharingID string, req *dto.SlurmClusterConfigSharingUpdate) (*dto.SlurmClusterConfigUserSharingResponse, error) {
	config, err := s.requireControlledConfig(ctx, configID)
	if err != nil {
		return nil, err
	}
	share, err := s.sharing.FindUserShare(ctx, config.ID, sharingID)
	if err != nil {
		return nil, notFoundAs(err, "User sharing not found: %s on Slurm cluster config %s", sharingID, config.ID)
	}

	share.Permission = *req.Permission
	if err := s.sharing.SaveUserShare(ctx, share); err != nil {
		return nil, err
	}
	out := dto.ToSlurmClusterConfigUserSharingResponse(share)
	return &out, nil
}

// RevokeUserShare withdraws a user's access.
func (s *SlurmClusterConfigSharingService) RevokeUserShare(ctx context.Context, configID, sharingID string) error {
	config, err := s.requireControlledConfig(ctx, configID)
	if err != nil {
		return err
	}
	share, err := s.sharing.FindUserShare(ctx, config.ID, sharingID)
	if err != nil {
		return notFoundAs(err, "User sharing not found: %s on Slurm cluster config %s", sharingID, config.ID)
	}
	return s.sharing.DeleteUserShare(ctx, share)
}

func (s *SlurmClusterConfigSharingService) requireControlledConfig(ctx context.Context, configID string) (*model.SlurmClusterConfig, error) {
	config, err := s.requireConfig(ctx, configID)
	if err != nil {
		return nil, err
	}
	if err := s.requireControl(ctx, config); err != nil {
		return nil, err
	}
	return config, nil
}
