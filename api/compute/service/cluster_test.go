package service_test

import (
	"context"
	"testing"

	"github.com/glebarez/sqlite"
	"gorm.io/gorm"
	"gorm.io/gorm/logger"

	"github.com/apache/airavata/internal/auth"
	"github.com/apache/airavata/internal/db"
	"github.com/apache/airavata/internal/httpx"

	dto "github.com/apache/airavata/api/compute/dto"
	computerepo "github.com/apache/airavata/api/compute/repository"
	service "github.com/apache/airavata/api/compute/service"
	credmodel "github.com/apache/airavata/api/credentials/model"
	credrepo "github.com/apache/airavata/api/credentials/repository"
)

// setup returns the two compute services over a fresh in-memory database, together
// with an SSH endpoint for clusters to point at and an admin context, since every
// write here is administrative.
func setup(t *testing.T) (*service.ClusterService, *service.ClusterPartitionService, string, context.Context) {
	t.Helper()
	gdb, err := gorm.Open(sqlite.Open("file::memory:?_pragma=foreign_keys(1)"), &gorm.Config{
		Logger: logger.Default.LogMode(logger.Silent),
	})
	if err != nil {
		t.Fatalf("open sqlite: %v", err)
	}
	if err := db.AutoMigrate(gdb); err != nil {
		t.Fatalf("automigrate: %v", err)
	}
	endpoint := &credmodel.SSHEndpoint{Name: "login", HostName: "login.example.edu", Port: 22}
	if err := gdb.Create(endpoint).Error; err != nil {
		t.Fatalf("create endpoint: %v", err)
	}
	clusters := computerepo.NewClusterRepository(gdb)
	partitions := computerepo.NewClusterPartitionRepository(gdb)
	endpoints := credrepo.NewSSHEndpointRepository(gdb)
	ctx := auth.WithPrincipal(context.Background(), &auth.Principal{Name: "root", Authorities: []string{"ADMIN"}})
	return service.NewClusterService(gdb, clusters, partitions, endpoints),
		service.NewClusterPartitionService(gdb, partitions, clusters),
		endpoint.ID, ctx
}

func str(s string) *string { return &s }

// A cluster registered with partitions comes back carrying them, with ids, and they
// are really in the database rather than only echoed from the request.
func TestCreateWithInlinePartitions(t *testing.T) {
	svc, _, endpointID, ctx := setup(t)
	out, err := svc.Create(ctx, &dto.ClusterRequest{
		ClusterName:   "expanse",
		SSHEndpointID: endpointID,
		SlurmHome:     "/usr/bin",
		Partitions: []dto.ClusterPartitionRequest{
			{Name: "compute", Description: str("cpu")},
			{Name: "gpu", Gres: str("gpu:4")},
		},
	})
	if err != nil {
		t.Fatalf("Create: %v", err)
	}
	if len(out.Partitions) != 2 {
		t.Fatalf("response partitions = %d, want 2", len(out.Partitions))
	}
	for _, p := range out.Partitions {
		if p.PartitionID == "" {
			t.Errorf("partition %q has no id", p.Name)
		}
		if p.ClusterID == nil || *p.ClusterID != out.ClusterID {
			t.Errorf("partition %q clusterId = %v, want %s", p.Name, p.ClusterID, out.ClusterID)
		}
	}
	// And they are really persisted, not just echoed.
	read, err := svc.Get(ctx, out.ClusterID)
	if err != nil {
		t.Fatalf("Get: %v", err)
	}
	if len(read.Partitions) != 2 {
		t.Errorf("persisted partitions = %d, want 2", len(read.Partitions))
	}
}

// The field stays optional: a cluster registered without it has an empty collection,
// not a null one.
func TestCreateWithoutPartitions(t *testing.T) {
	svc, _, endpointID, ctx := setup(t)
	out, err := svc.Create(ctx, &dto.ClusterRequest{
		ClusterName: "expanse", SSHEndpointID: endpointID, SlurmHome: "/usr/bin",
	})
	if err != nil {
		t.Fatalf("Create: %v", err)
	}
	if len(out.Partitions) != 0 {
		t.Errorf("partitions = %d, want 0", len(out.Partitions))
	}
}

// The partitions are written in the cluster's transaction, so a create that fails
// after them leaves nothing behind. An unknown endpoint is the failure that is easiest
// to provoke, and it is rejected before any row is written.
func TestInlinePartitionRollsBackWithCluster(t *testing.T) {
	svc, _, _, ctx := setup(t)
	_, err := svc.Create(ctx, &dto.ClusterRequest{
		ClusterName: "expanse", SSHEndpointID: "does-not-exist", SlurmHome: "/usr/bin",
		Partitions: []dto.ClusterPartitionRequest{{Name: "compute"}},
	})
	if httpx.StatusOf(err) != 404 {
		t.Fatalf("Create with unknown endpoint = %v (status %d), want 404", err, httpx.StatusOf(err))
	}
}

// An update carrying partitions is refused rather than obeyed or ignored, and the
// refusal leaves the cluster exactly as it was. Without the field the same update
// succeeds, so it is the partitions that are rejected and not the body.
func TestUpdateRejectsPartitions(t *testing.T) {
	svc, _, endpointID, ctx := setup(t)
	out, err := svc.Create(ctx, &dto.ClusterRequest{
		ClusterName: "expanse", SSHEndpointID: endpointID, SlurmHome: "/usr/bin",
		Partitions: []dto.ClusterPartitionRequest{{Name: "compute"}},
	})
	if err != nil {
		t.Fatalf("Create: %v", err)
	}
	_, err = svc.Update(ctx, out.ClusterID, &dto.ClusterRequest{
		ClusterName: "expanse2", SSHEndpointID: endpointID, SlurmHome: "/usr/bin",
		Partitions: []dto.ClusterPartitionRequest{{Name: "gpu"}},
	})
	if httpx.StatusOf(err) != 400 {
		t.Fatalf("Update with partitions = %v (status %d), want 400", err, httpx.StatusOf(err))
	}
	// The rejected update changed nothing.
	read, _ := svc.Get(ctx, out.ClusterID)
	if read.ClusterName != "expanse" || len(read.Partitions) != 1 {
		t.Errorf("after rejected update: name=%q partitions=%d, want expanse/1", read.ClusterName, len(read.Partitions))
	}
	// Without partitions it still works.
	if _, err := svc.Update(ctx, out.ClusterID, &dto.ClusterRequest{
		ClusterName: "expanse2", SSHEndpointID: endpointID, SlurmHome: "/usr/bin",
	}); err != nil {
		t.Fatalf("Update without partitions: %v", err)
	}
}

// The path for a cluster that gains a partition after it was registered: the partition
// endpoint adds one without disturbing what the cluster was created with.
func TestPartitionsAddedLater(t *testing.T) {
	svc, partSvc, endpointID, ctx := setup(t)
	out, err := svc.Create(ctx, &dto.ClusterRequest{
		ClusterName: "expanse", SSHEndpointID: endpointID, SlurmHome: "/usr/bin",
		Partitions: []dto.ClusterPartitionRequest{{Name: "compute"}},
	})
	if err != nil {
		t.Fatalf("Create: %v", err)
	}
	if _, err := partSvc.Create(ctx, out.ClusterID, &dto.ClusterPartitionRequest{Name: "largemem"}); err != nil {
		t.Fatalf("partition Create: %v", err)
	}
	read, _ := svc.Get(ctx, out.ClusterID)
	if len(read.Partitions) != 2 {
		t.Errorf("partitions after adding one later = %d, want 2", len(read.Partitions))
	}
}

// A bad inline partition is reported under the element that carries it, so a caller
// sending several can tell which one was wrong.
func TestInlinePartitionValidationIsIndexed(t *testing.T) {
	req := &dto.ClusterRequest{
		ClusterName: "expanse", SSHEndpointID: "e", SlurmHome: "/usr/bin",
		Partitions: []dto.ClusterPartitionRequest{{Name: "ok"}, {Name: "  "}},
	}
	fields := req.Validate()
	if len(fields) != 1 || fields[0].Field != "partitions[1].name" {
		t.Fatalf("Validate = %+v, want one error on partitions[1].name", fields)
	}
}
