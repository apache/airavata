package service_test

import (
	"context"
	"testing"

	"gorm.io/gorm"

	"github.com/apache/airavata/internal/auth"
	"github.com/apache/airavata/internal/httpx"

	dto "github.com/apache/airavata/api/compute/dto"
	computemodel "github.com/apache/airavata/api/compute/model"
	computerepo "github.com/apache/airavata/api/compute/repository"
	service "github.com/apache/airavata/api/compute/service"
	credmodel "github.com/apache/airavata/api/credentials/model"
	credrepo "github.com/apache/airavata/api/credentials/repository"
	iammodel "github.com/apache/airavata/api/iam/model"
	iamrepo "github.com/apache/airavata/api/iam/repository"
)

// configFixture is everything a cluster-config test needs: the two services under
// test, a registered cluster and key for a config to point at, and contexts for the
// owner, an outsider and an admin.
type configFixture struct {
	gdb       *gorm.DB
	configs   *service.SlurmClusterConfigService
	sharing   *service.SlurmClusterConfigSharingService
	clusterID string
	keyID     string

	owner    context.Context
	outsider context.Context
	admin    context.Context
}

func userCtx(name string) context.Context {
	return auth.WithPrincipal(context.Background(), &auth.Principal{Name: name})
}

func adminCtx(name string) context.Context {
	return auth.WithPrincipal(context.Background(), &auth.Principal{Name: name, Authorities: []string{"ADMIN"}})
}

func newConfigFixture(t *testing.T) *configFixture {
	t.Helper()
	gdb := newDB(t)

	for _, id := range []string{"alice", "bob", "root"} {
		if err := gdb.Create(&iammodel.User{ID: id, CreatedAt: 1}).Error; err != nil {
			t.Fatalf("create user %s: %v", id, err)
		}
	}
	cluster := &computemodel.SlurmCluster{ClusterName: "expanse", HeadnodeHost: "login.expanse.edu", HeadnodePort: 22}
	if err := gdb.Create(cluster).Error; err != nil {
		t.Fatalf("create cluster: %v", err)
	}
	key := &credmodel.SSHKey{SSHKeyName: "alice-key", PublicKey: "ssh-ed25519 AAAA", PrivateKey: "secret"}
	if err := gdb.Create(key).Error; err != nil {
		t.Fatalf("create key: %v", err)
	}

	configs := computerepo.NewSlurmClusterConfigRepository(gdb)
	shares := computerepo.NewSlurmClusterConfigSharingRepository(gdb)
	clusters := computerepo.NewSlurmClusterRepository(gdb)
	keys := credrepo.NewSSHKeyRepository(gdb)
	users := iamrepo.NewUserRepository(gdb)
	groups := iamrepo.NewGroupRepository(gdb)
	members := iamrepo.NewGroupMemberRepository(gdb)

	return &configFixture{
		gdb:       gdb,
		configs:   service.NewSlurmClusterConfigService(gdb, configs, shares, clusters, keys, users, members),
		sharing:   service.NewSlurmClusterConfigSharingService(gdb, configs, shares, groups, users, members),
		clusterID: cluster.ID,
		keyID:     key.ID,
		owner:     userCtx("alice"),
		outsider:  userCtx("bob"),
		admin:     adminCtx("root"),
	}
}

func (f *configFixture) req() *dto.SlurmClusterConfigRequest {
	return &dto.SlurmClusterConfigRequest{
		Name:           str("expanse allocation"),
		SlurmClusterID: f.clusterID,
		LoginUser:      "alice",
		WorkRoot:       "/scratch/alice",
		SSHKeyID:       f.keyID,
	}
}

// mustCreate registers a config owned by alice.
func (f *configFixture) mustCreate(t *testing.T) *dto.SlurmClusterConfigResponse {
	t.Helper()
	out, err := f.configs.Create(f.owner, f.req())
	if err != nil {
		t.Fatalf("Create: %v", err)
	}
	return out
}

// The owner comes from the token, not the body, and the caller gets WRITE on what they
// just registered. The cluster and the key summary are inlined, since a config is only
// meaningful together with the machine it logs in to.
func TestConfigCreateTakesOwnerFromToken(t *testing.T) {
	f := newConfigFixture(t)
	out := f.mustCreate(t)

	if out.OwnerID == nil || *out.OwnerID != "alice" {
		t.Errorf("ownerId = %v, want alice", out.OwnerID)
	}
	if out.Permission == nil || *out.Permission != "WRITE" {
		t.Errorf("permission = %v, want WRITE", out.Permission)
	}
	if out.SlurmCluster == nil || out.SlurmCluster.ClusterName != "expanse" {
		t.Errorf("slurmCluster = %+v, want the expanse cluster inlined", out.SlurmCluster)
	}
	if out.SSHKey == nil || out.SSHKey.SSHKeyName != "alice-key" {
		t.Errorf("sshKey = %+v, want the key summary inlined", out.SSHKey)
	}
}

// An unshared config is invisible to everyone but its owner and the platform admins.
func TestConfigIsPrivateUntilShared(t *testing.T) {
	f := newConfigFixture(t)
	out := f.mustCreate(t)

	if _, err := f.configs.Get(f.outsider, out.SlurmClusterConfigID); httpx.StatusOf(err) != 403 {
		t.Errorf("outsider Get = %v (status %d), want 403", err, httpx.StatusOf(err))
	}
	if _, err := f.configs.Get(f.admin, out.SlurmClusterConfigID); err != nil {
		t.Errorf("admin Get: %v", err)
	}
	if _, err := f.configs.Get(f.owner, out.SlurmClusterConfigID); err != nil {
		t.Errorf("owner Get: %v", err)
	}
}

// A READ share opens the config for reading and nothing more: the grantee sees it, and
// sees what it grants them, but cannot edit it.
func TestUserShareGrantsRead(t *testing.T) {
	f := newConfigFixture(t)
	out := f.mustCreate(t)

	if _, err := f.sharing.ShareWithUser(f.owner, out.SlurmClusterConfigID,
		&dto.SlurmClusterConfigUserSharingRequest{UserID: "bob"}); err != nil {
		t.Fatalf("ShareWithUser: %v", err)
	}

	got, err := f.configs.Get(f.outsider, out.SlurmClusterConfigID)
	if err != nil {
		t.Fatalf("grantee Get: %v", err)
	}
	if got.Permission == nil || *got.Permission != "READ" {
		t.Errorf("permission = %v, want READ", got.Permission)
	}
	if _, err := f.configs.Update(f.outsider, out.SlurmClusterConfigID, f.req()); httpx.StatusOf(err) != 403 {
		t.Errorf("grantee Update = %v (status %d), want 403", err, httpx.StatusOf(err))
	}

	shared, err := f.configs.ListSharedWithMe(f.outsider)
	if err != nil {
		t.Fatalf("ListSharedWithMe: %v", err)
	}
	if len(shared) != 1 || shared[0].SlurmClusterConfigID != out.SlurmClusterConfigID {
		t.Errorf("shared-with-me = %+v, want the one shared config", shared)
	}
	// Ownership is not a share: the owner's own config does not show up there.
	if mine, _ := f.configs.ListSharedWithMe(f.owner); len(mine) != 0 {
		t.Errorf("owner shared-with-me = %d, want 0", len(mine))
	}
}

// WRITE lets a grantee edit the config but not delete it or manage its shares: who else
// may submit under this identity stays with the owner.
func TestWriteShareStopsShortOfControl(t *testing.T) {
	f := newConfigFixture(t)
	out := f.mustCreate(t)

	write := computemodel.ClusterPermissionWrite
	if _, err := f.sharing.ShareWithUser(f.owner, out.SlurmClusterConfigID,
		&dto.SlurmClusterConfigUserSharingRequest{UserID: "bob", Permission: &write}); err != nil {
		t.Fatalf("ShareWithUser: %v", err)
	}

	req := f.req()
	req.WorkRoot = "/scratch/shared"
	if _, err := f.configs.Update(f.outsider, out.SlurmClusterConfigID, req); err != nil {
		t.Fatalf("grantee Update: %v", err)
	}
	if _, err := f.sharing.ListUserShares(f.outsider, out.SlurmClusterConfigID); httpx.StatusOf(err) != 403 {
		t.Errorf("grantee ListUserShares = %v (status %d), want 403", err, httpx.StatusOf(err))
	}
	if err := f.configs.Delete(f.outsider, out.SlurmClusterConfigID); httpx.StatusOf(err) != 403 {
		t.Errorf("grantee Delete = %v (status %d), want 403", err, httpx.StatusOf(err))
	}
}

// An update never re-derives the owner from the caller's token — otherwise a WRITE
// grantee editing a config would quietly take it over.
func TestUpdateLeavesOwnerAlone(t *testing.T) {
	f := newConfigFixture(t)
	out := f.mustCreate(t)

	write := computemodel.ClusterPermissionWrite
	if _, err := f.sharing.ShareWithUser(f.owner, out.SlurmClusterConfigID,
		&dto.SlurmClusterConfigUserSharingRequest{UserID: "bob", Permission: &write}); err != nil {
		t.Fatalf("ShareWithUser: %v", err)
	}
	got, err := f.configs.Update(f.outsider, out.SlurmClusterConfigID, f.req())
	if err != nil {
		t.Fatalf("grantee Update: %v", err)
	}
	if got.OwnerID == nil || *got.OwnerID != "alice" {
		t.Errorf("ownerId after a grantee's update = %v, want alice", got.OwnerID)
	}
}

// A group share reaches the caller only through an ACTIVE membership: an inactive
// member keeps their place in the group without keeping access through it.
func TestGroupShareNeedsAnActiveMembership(t *testing.T) {
	f := newConfigFixture(t)
	out := f.mustCreate(t)

	group := &iammodel.Group{OwnerID: str("alice"), CreatedAt: 1}
	if err := f.gdb.Create(group).Error; err != nil {
		t.Fatalf("create group: %v", err)
	}
	member := &iammodel.GroupMember{
		GroupID: group.ID, UserID: "bob",
		GroupRole:         iammodel.GroupRoleMember,
		GroupMemberStatus: iammodel.GroupMemberStatusActive,
	}
	if err := f.gdb.Create(member).Error; err != nil {
		t.Fatalf("create membership: %v", err)
	}
	if _, err := f.sharing.ShareWithGroup(f.owner, out.SlurmClusterConfigID,
		&dto.SlurmClusterConfigGroupSharingRequest{GroupID: group.ID}); err != nil {
		t.Fatalf("ShareWithGroup: %v", err)
	}

	if _, err := f.configs.Get(f.outsider, out.SlurmClusterConfigID); err != nil {
		t.Fatalf("active member Get: %v", err)
	}

	member.GroupMemberStatus = iammodel.GroupMemberStatusInactive
	if err := f.gdb.Save(member).Error; err != nil {
		t.Fatalf("deactivate membership: %v", err)
	}
	if _, err := f.configs.Get(f.outsider, out.SlurmClusterConfigID); httpx.StatusOf(err) != 403 {
		t.Errorf("inactive member Get = %v (status %d), want 403", err, httpx.StatusOf(err))
	}
}

// Sharing with the owner grants nothing they do not already have, so it is refused
// rather than stored; sharing twice with the same user is likewise a conflict.
func TestDuplicateAndSelfSharesAreRefused(t *testing.T) {
	f := newConfigFixture(t)
	out := f.mustCreate(t)

	if _, err := f.sharing.ShareWithUser(f.owner, out.SlurmClusterConfigID,
		&dto.SlurmClusterConfigUserSharingRequest{UserID: "alice"}); httpx.StatusOf(err) != 409 {
		t.Errorf("share with owner = %v (status %d), want 409", err, httpx.StatusOf(err))
	}
	if _, err := f.sharing.ShareWithUser(f.owner, out.SlurmClusterConfigID,
		&dto.SlurmClusterConfigUserSharingRequest{UserID: "bob"}); err != nil {
		t.Fatalf("ShareWithUser: %v", err)
	}
	if _, err := f.sharing.ShareWithUser(f.owner, out.SlurmClusterConfigID,
		&dto.SlurmClusterConfigUserSharingRequest{UserID: "bob"}); httpx.StatusOf(err) != 409 {
		t.Errorf("duplicate share = %v (status %d), want 409", err, httpx.StatusOf(err))
	}
}

// Revoking a share closes the access it opened, and deleting a config takes its shares
// with it rather than leaving rows pointing at nothing.
func TestRevokeAndDeleteClearShares(t *testing.T) {
	f := newConfigFixture(t)
	out := f.mustCreate(t)

	share, err := f.sharing.ShareWithUser(f.owner, out.SlurmClusterConfigID,
		&dto.SlurmClusterConfigUserSharingRequest{UserID: "bob"})
	if err != nil {
		t.Fatalf("ShareWithUser: %v", err)
	}
	if err := f.sharing.RevokeUserShare(f.owner, out.SlurmClusterConfigID, share.SharingID); err != nil {
		t.Fatalf("RevokeUserShare: %v", err)
	}
	if _, err := f.configs.Get(f.outsider, out.SlurmClusterConfigID); httpx.StatusOf(err) != 403 {
		t.Errorf("after revoke, Get = %v (status %d), want 403", err, httpx.StatusOf(err))
	}

	if _, err := f.sharing.ShareWithUser(f.owner, out.SlurmClusterConfigID,
		&dto.SlurmClusterConfigUserSharingRequest{UserID: "bob"}); err != nil {
		t.Fatalf("re-share: %v", err)
	}
	if err := f.configs.Delete(f.owner, out.SlurmClusterConfigID); err != nil {
		t.Fatalf("Delete: %v", err)
	}
	var remaining int64
	f.gdb.Model(&computemodel.SlurmClusterConfigUserSharing{}).
		Where("slurm_cluster_config_id = ?", out.SlurmClusterConfigID).Count(&remaining)
	if remaining != 0 {
		t.Errorf("%d share rows survived the config delete, want 0", remaining)
	}
}

// A config pointing at a machine or a key that does not exist is a 404, not a dangling
// reference.
func TestConfigReferencesMustResolve(t *testing.T) {
	f := newConfigFixture(t)

	req := f.req()
	req.SlurmClusterID = "no-such-cluster"
	if _, err := f.configs.Create(f.owner, req); httpx.StatusOf(err) != 404 {
		t.Errorf("unknown cluster = %v (status %d), want 404", err, httpx.StatusOf(err))
	}

	req = f.req()
	req.SSHKeyID = "no-such-key"
	if _, err := f.configs.Create(f.owner, req); httpx.StatusOf(err) != 404 {
		t.Errorf("unknown key = %v (status %d), want 404", err, httpx.StatusOf(err))
	}
}

// Listing every config across every owner names who may submit as whom, and where, so
// it is admin-only.
func TestConfigListAcrossOwnersIsAdminOnly(t *testing.T) {
	f := newConfigFixture(t)
	f.mustCreate(t)

	if _, err := f.configs.List(f.owner); httpx.StatusOf(err) != 403 {
		t.Errorf("owner List = %v (status %d), want 403", err, httpx.StatusOf(err))
	}
	all, err := f.configs.List(f.admin)
	if err != nil {
		t.Fatalf("admin List: %v", err)
	}
	if len(all) != 1 {
		t.Errorf("admin List = %d configs, want 1", len(all))
	}
	mine, err := f.configs.ListMine(f.owner)
	if err != nil {
		t.Fatalf("ListMine: %v", err)
	}
	if len(mine) != 1 {
		t.Errorf("ListMine = %d, want 1", len(mine))
	}
	if theirs, _ := f.configs.ListMine(f.outsider); len(theirs) != 0 {
		t.Errorf("outsider ListMine = %d, want 0", len(theirs))
	}
}

// A cluster that configs still log in to cannot be deleted out from under them: the
// foreign key is RESTRICT, and the service turns that into a 409 naming how many.
func TestClusterDeleteIsRefusedWhileConfigsExist(t *testing.T) {
	f := newConfigFixture(t)
	f.mustCreate(t)

	clusters := computerepo.NewSlurmClusterRepository(f.gdb)
	partitions := computerepo.NewClusterPartitionRepository(f.gdb)
	configs := computerepo.NewSlurmClusterConfigRepository(f.gdb)
	svc := service.NewSlurmClusterService(f.gdb, clusters, partitions, configs)

	if err := svc.Delete(f.admin, f.clusterID); httpx.StatusOf(err) != 409 {
		t.Fatalf("Delete with configs = %v (status %d), want 409", err, httpx.StatusOf(err))
	}
}
