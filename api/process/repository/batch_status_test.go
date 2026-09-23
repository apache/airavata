package repository_test

import (
	"context"
	"errors"
	"testing"
	"time"

	"github.com/glebarez/sqlite"
	"gorm.io/gorm"
	"gorm.io/gorm/logger"

	"github.com/apache/airavata/internal/db"
	"github.com/apache/airavata/internal/ptr"

	applicationmodel "github.com/apache/airavata/api/application/model"
	computemodel "github.com/apache/airavata/api/compute/model"
	dto "github.com/apache/airavata/api/process/dto"
	model "github.com/apache/airavata/api/process/model"
	repository "github.com/apache/airavata/api/process/repository"
)

// newDB returns a fresh in-memory database with the schema applied. Foreign keys are
// on, so a fixture that skips a parent row fails here rather than passing a test the
// real database would reject.
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

// newBatchProcess creates a BATCH_JOB process and everything its foreign keys require,
// returning the process and its batch section.
func newBatchProcess(t *testing.T, gdb *gorm.DB) (*model.Process, *model.BatchJobProcess) {
	t.Helper()

	cluster := &computemodel.SlurmCluster{ClusterName: "expanse", HeadnodeHost: "login.expanse.edu", HeadnodePort: 22}
	if err := gdb.Create(cluster).Error; err != nil {
		t.Fatalf("create cluster: %v", err)
	}
	clusterConfig := &computemodel.SlurmClusterConfig{
		SlurmClusterID: cluster.ID,
		LoginUser:      "someone",
		WorkRoot:       "/scratch/someone",
	}
	if err := gdb.Create(clusterConfig).Error; err != nil {
		t.Fatalf("create cluster config: %v", err)
	}
	jobConfig := &applicationmodel.BatchJobConfig{WallTimeMinutes: 60, Allocation: "TG-ABC123"}
	if err := gdb.Create(jobConfig).Error; err != nil {
		t.Fatalf("create batch job config: %v", err)
	}

	proc := &model.Process{ProcessType: ptr.To(model.ProcessTypeBatchJob)}
	if err := gdb.Create(proc).Error; err != nil {
		t.Fatalf("create process: %v", err)
	}
	batch := &model.BatchJobProcess{
		ProcessID:            &proc.ID,
		SlurmClusterConfigID: clusterConfig.ID,
		BatchJobConfigID:     jobConfig.ID,
	}
	if err := gdb.Create(batch).Error; err != nil {
		t.Fatalf("create batch process: %v", err)
	}
	return proc, batch
}

// record appends one status, spacing the rows in time so the history has an order to
// read back: GORM stamps UpdatedAt itself, and two inserts in the same test can land in
// the same instant.
func record(t *testing.T, repo *repository.BatchJobStatusRepository, batchProcessID string, status model.BatchJobStatusType, at time.Time) *model.BatchJobStatus {
	t.Helper()
	s := &model.BatchJobStatus{BatchProcessID: batchProcessID, Status: status, UpdatedAt: at}
	if err := repo.Create(context.Background(), s); err != nil {
		t.Fatalf("record %s: %v", status, err)
	}
	return s
}

func TestBatchJobStatusHistoryReadsOldestFirst(t *testing.T) {
	gdb := newDB(t)
	repo := repository.NewBatchJobStatusRepository(gdb)
	proc, batch := newBatchProcess(t, gdb)

	base := time.Now().Truncate(time.Second)
	record(t, repo, batch.ID, model.BatchJobStatusEnded, base.Add(2*time.Minute))
	record(t, repo, batch.ID, model.BatchJobStatusSubmitted, base)
	record(t, repo, batch.ID, model.BatchJobStatusBegin, base.Add(time.Minute))

	want := []model.BatchJobStatusType{
		model.BatchJobStatusSubmitted, model.BatchJobStatusBegin, model.BatchJobStatusEnded,
	}

	byBatch, err := repo.FindByBatchProcessID(context.Background(), batch.ID)
	if err != nil {
		t.Fatalf("find by batch process: %v", err)
	}
	if got := names(byBatch); !equal(got, want) {
		t.Errorf("FindByBatchProcessID = %v, want %v", got, want)
	}

	// A caller coming from the API holds the process id, not the batch section's.
	byProcess, err := repo.FindByProcessID(context.Background(), proc.ID)
	if err != nil {
		t.Fatalf("find by process: %v", err)
	}
	if got := names(byProcess); !equal(got, want) {
		t.Errorf("FindByProcessID = %v, want %v", got, want)
	}
}

// A caller recording a status says what the scheduler reported, not when it was
// written down: the row is stamped for it.
func TestBatchJobStatusIsStampedAndIdentifiedOnCreate(t *testing.T) {
	gdb := newDB(t)
	repo := repository.NewBatchJobStatusRepository(gdb)
	_, batch := newBatchProcess(t, gdb)

	s := &model.BatchJobStatus{BatchProcessID: batch.ID, Status: model.BatchJobStatusSubmitted}
	if err := repo.Create(context.Background(), s); err != nil {
		t.Fatalf("create: %v", err)
	}
	if len(s.ID) != 36 {
		t.Errorf("ID = %q, want a generated UUID", s.ID)
	}
	if s.UpdatedAt.IsZero() {
		t.Error("UpdatedAt was left zero, want the time of the insert")
	}

	var got model.BatchJobStatus
	if err := gdb.First(&got, "batch_process_status_id = ?", s.ID).Error; err != nil {
		t.Fatalf("reload: %v", err)
	}
	if got.UpdatedAt.IsZero() {
		t.Error("the stored row has no timestamp")
	}
}

// The batch section is read back with its history nested inside it, oldest first, so a
// client polling a process sees where the job is without a second request.
func TestProcessReadsBackItsBatchJobStatuses(t *testing.T) {
	gdb := newDB(t)
	repo := repository.NewBatchJobStatusRepository(gdb)
	processes := repository.NewProcessRepository(gdb)
	proc, batch := newBatchProcess(t, gdb)

	base := time.Now().Truncate(time.Second)
	record(t, repo, batch.ID, model.BatchJobStatusBegin, base.Add(time.Minute))
	record(t, repo, batch.ID, model.BatchJobStatusSubmitted, base)

	found, err := processes.FindByID(context.Background(), proc.ID)
	if err != nil {
		t.Fatalf("find process: %v", err)
	}
	loaded := make([]model.BatchJobStatus, 0, len(found.BatchProcess.BatchJobStatuses))
	for _, s := range found.BatchProcess.BatchJobStatuses {
		loaded = append(loaded, *s)
	}
	want := []model.BatchJobStatusType{model.BatchJobStatusSubmitted, model.BatchJobStatusBegin}
	if got := names(loaded); !equal(got, want) {
		t.Fatalf("nested statuses = %v, want %v", got, want)
	}

	rendered := dto.ToResponse(found).BatchProcess
	if got := len(rendered.BatchJobStatuses); got != 2 {
		t.Errorf("rendered %d statuses, want 2", got)
	}
	if rendered.LatestBatchJobStatus == nil || rendered.LatestBatchJobStatus.Status != model.BatchJobStatusBegin {
		t.Errorf("latestBatchJobStatus = %v, want %s", rendered.LatestBatchJobStatus, model.BatchJobStatusBegin)
	}
}

func TestBatchJobStatusLatestIsTheNewest(t *testing.T) {
	gdb := newDB(t)
	repo := repository.NewBatchJobStatusRepository(gdb)
	_, batch := newBatchProcess(t, gdb)

	if _, err := repo.FindLatestByBatchProcessID(context.Background(), batch.ID); !errors.Is(err, gorm.ErrRecordNotFound) {
		t.Errorf("latest of a job nothing has been reported for = %v, want ErrRecordNotFound", err)
	}

	base := time.Now().Truncate(time.Second)
	record(t, repo, batch.ID, model.BatchJobStatusSubmitted, base)
	record(t, repo, batch.ID, model.BatchJobStatusBegin, base.Add(time.Minute))

	latest, err := repo.FindLatestByBatchProcessID(context.Background(), batch.ID)
	if err != nil {
		t.Fatalf("find latest: %v", err)
	}
	if latest.Status != model.BatchJobStatusBegin {
		t.Errorf("latest = %q, want %s", latest.Status, model.BatchJobStatusBegin)
	}
}

// Statuses are scoped to the job they were reported for: one batch process's history
// must not leak into another's.
func TestBatchJobStatusIsScopedToItsBatchProcess(t *testing.T) {
	gdb := newDB(t)
	repo := repository.NewBatchJobStatusRepository(gdb)
	_, first := newBatchProcess(t, gdb)
	_, second := newBatchProcess(t, gdb)

	now := time.Now()
	record(t, repo, first.ID, model.BatchJobStatusBegin, now)
	record(t, repo, second.ID, model.BatchJobStatusFailed, now)

	got, err := repo.FindByBatchProcessID(context.Background(), first.ID)
	if err != nil {
		t.Fatalf("find: %v", err)
	}
	if !equal(names(got), []model.BatchJobStatusType{model.BatchJobStatusBegin}) {
		t.Errorf("statuses of the first job = %v, want [%s]", names(got), model.BatchJobStatusBegin)
	}
}

// The history is owned by the batch process, which is owned by the process: deleting
// the process must take both with it rather than leaving rows pointing at nothing.
func TestDeletingProcessRemovesBatchJobStatuses(t *testing.T) {
	gdb := newDB(t)
	repo := repository.NewBatchJobStatusRepository(gdb)
	proc, batch := newBatchProcess(t, gdb)
	record(t, repo, batch.ID, model.BatchJobStatusBegin, time.Now())

	if err := gdb.Delete(proc).Error; err != nil {
		t.Fatalf("delete process: %v", err)
	}

	var remaining int64
	gdb.Model(&model.BatchJobStatus{}).Where("batch_process_id = ?", batch.ID).Count(&remaining)
	if remaining != 0 {
		t.Errorf("%d status rows survived the process delete, want 0", remaining)
	}
}

// A status is recorded by service code that may already be inside a transaction of its
// own — a monitor recording a transition alongside the rest of what it observed.
func TestBatchJobStatusRollsBackWithItsTransaction(t *testing.T) {
	gdb := newDB(t)
	repo := repository.NewBatchJobStatusRepository(gdb)
	_, batch := newBatchProcess(t, gdb)

	failed := gdb.Transaction(func(tx *gorm.DB) error {
		s := &model.BatchJobStatus{BatchProcessID: batch.ID, Status: model.BatchJobStatusBegin, UpdatedAt: time.Now()}
		if err := repo.WithTx(tx).Create(context.Background(), s); err != nil {
			return err
		}
		return context.Canceled // any error, so the transaction rolls back
	})
	if failed == nil {
		t.Fatal("transaction reported success, want the error it returned")
	}

	var remaining int64
	gdb.Model(&model.BatchJobStatus{}).Where("batch_process_id = ?", batch.ID).Count(&remaining)
	if remaining != 0 {
		t.Errorf("%d status rows survived a rolled-back transaction, want 0", remaining)
	}
}

func names(in []model.BatchJobStatus) []model.BatchJobStatusType {
	out := make([]model.BatchJobStatusType, 0, len(in))
	for i := range in {
		out = append(out, in[i].Status)
	}
	return out
}

func equal(got, want []model.BatchJobStatusType) bool {
	if len(got) != len(want) {
		return false
	}
	for i := range got {
		if got[i] != want[i] {
			return false
		}
	}
	return true
}
