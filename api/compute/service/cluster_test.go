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
)

// newDB returns a fresh in-memory database with the schema applied.
func newDB(t *testing.T) *gorm.DB {
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
	return gdb
}

// setup returns the two cluster-catalogue services over a fresh database, with an
// admin context, since every write here is administrative.
func setup(t *testing.T) (*service.SlurmClusterService, *service.ClusterPartitionService, context.Context) {
	t.Helper()
	gdb := newDB(t)
	clusters := computerepo.NewSlurmClusterRepository(gdb)
	partitions := computerepo.NewClusterPartitionRepository(gdb)
	configs := computerepo.NewSlurmClusterConfigRepository(gdb)
	ctx := auth.WithPrincipal(context.Background(), &auth.Principal{Name: "root", Authorities: []string{"ADMIN"}})
	return service.NewSlurmClusterService(gdb, clusters, partitions, configs),
		service.NewClusterPartitionService(gdb, partitions, clusters),
		ctx
}

func str(s string) *string { return &s }

// clusterReq is a valid create body, so each test only spells out what it is about.
func clusterReq(name string, partitions ...dto.ClusterPartitionRequest) *dto.SlurmClusterRequest {
	return &dto.SlurmClusterRequest{
		ClusterName:  name,
		HeadnodeHost: "login." + name + ".edu",
		HeadnodePort: 22,
		Partitions:   partitions,
	}
}

// A cluster registered with partitions comes back carrying them, with ids, and they
// are really in the database rather than only echoed from the request.
func TestCreateWithInlinePartitions(t *testing.T) {
	svc, _, ctx := setup(t)
	out, err := svc.Create(ctx, clusterReq("expanse",
		dto.ClusterPartitionRequest{Name: "compute", Description: str("cpu")},
		dto.ClusterPartitionRequest{Name: "gpu", Gres: str("gpu:4")},
	))
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
		if p.ClusterID == nil || *p.ClusterID != out.SlurmClusterID {
			t.Errorf("partition %q clusterId = %v, want %s", p.Name, p.ClusterID, out.SlurmClusterID)
		}
	}
	// And they are really persisted, not just echoed.
	read, err := svc.Get(ctx, out.SlurmClusterID)
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
	svc, _, ctx := setup(t)
	out, err := svc.Create(ctx, clusterReq("expanse"))
	if err != nil {
		t.Fatalf("Create: %v", err)
	}
	if len(out.Partitions) != 0 {
		t.Errorf("partitions = %d, want 0", len(out.Partitions))
	}
}

// The head node is what a job is submitted through, so a cluster that names none — or
// names an impossible port — is rejected before anything is written.
func TestCreateRequiresAHeadnode(t *testing.T) {
	req := &dto.SlurmClusterRequest{ClusterName: "expanse", HeadnodeHost: "  ", HeadnodePort: 0}
	fields := req.Validate()
	got := map[string]string{}
	for _, f := range fields {
		got[f.Field] = f.Message
	}
	if _, ok := got["headnodeHost"]; !ok {
		t.Errorf("Validate = %+v, want an error on headnodeHost", fields)
	}
	if _, ok := got["headnodePort"]; !ok {
		t.Errorf("Validate = %+v, want an error on headnodePort", fields)
	}
}

// The data endpoint is optional as a whole, but a port outside the usable range is a
// configuration error rather than a default.
func TestDataPortIsRangeChecked(t *testing.T) {
	port := 0
	req := clusterReq("expanse")
	req.DataHost, req.DataPort = str("data.expanse.edu"), &port
	fields := req.Validate()
	if len(fields) != 1 || fields[0].Field != "dataPort" {
		t.Fatalf("Validate = %+v, want one error on dataPort", fields)
	}
}

// An update carrying partitions is refused rather than obeyed or ignored, and the
// refusal leaves the cluster exactly as it was. Without the field the same update
// succeeds, so it is the partitions that are rejected and not the body.
func TestUpdateRejectsPartitions(t *testing.T) {
	svc, _, ctx := setup(t)
	out, err := svc.Create(ctx, clusterReq("expanse", dto.ClusterPartitionRequest{Name: "compute"}))
	if err != nil {
		t.Fatalf("Create: %v", err)
	}
	_, err = svc.Update(ctx, out.SlurmClusterID,
		clusterReq("expanse2", dto.ClusterPartitionRequest{Name: "gpu"}))
	if httpx.StatusOf(err) != 400 {
		t.Fatalf("Update with partitions = %v (status %d), want 400", err, httpx.StatusOf(err))
	}
	// The rejected update changed nothing.
	read, _ := svc.Get(ctx, out.SlurmClusterID)
	if read.ClusterName != "expanse" || len(read.Partitions) != 1 {
		t.Errorf("after rejected update: name=%q partitions=%d, want expanse/1", read.ClusterName, len(read.Partitions))
	}
	// Without partitions it still works.
	if _, err := svc.Update(ctx, out.SlurmClusterID, clusterReq("expanse2")); err != nil {
		t.Fatalf("Update without partitions: %v", err)
	}
}

// The path for a cluster that gains a partition after it was registered: the partition
// endpoint adds one without disturbing what the cluster was created with.
func TestPartitionsAddedLater(t *testing.T) {
	svc, partSvc, ctx := setup(t)
	out, err := svc.Create(ctx, clusterReq("expanse", dto.ClusterPartitionRequest{Name: "compute"}))
	if err != nil {
		t.Fatalf("Create: %v", err)
	}
	if _, err := partSvc.Create(ctx, out.SlurmClusterID, &dto.ClusterPartitionRequest{Name: "largemem"}); err != nil {
		t.Fatalf("partition Create: %v", err)
	}
	read, _ := svc.Get(ctx, out.SlurmClusterID)
	if len(read.Partitions) != 2 {
		t.Errorf("partitions after adding one later = %d, want 2", len(read.Partitions))
	}
}

// A bad inline partition is reported under the element that carries it, so a caller
// sending several can tell which one was wrong.
func TestInlinePartitionValidationIsIndexed(t *testing.T) {
	req := clusterReq("expanse",
		dto.ClusterPartitionRequest{Name: "ok"},
		dto.ClusterPartitionRequest{Name: "  "},
	)
	fields := req.Validate()
	if len(fields) != 1 || fields[0].Field != "partitions[1].name" {
		t.Fatalf("Validate = %+v, want one error on partitions[1].name", fields)
	}
}
