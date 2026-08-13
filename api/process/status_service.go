package process

import (
	"context"
	"time"

	"gorm.io/gorm"

	"github.com/apache/airavata/internal/ptr"

	dto "github.com/apache/airavata/api/process/dto"
	model "github.com/apache/airavata/api/process/model"
)

// StatusService records and reads BatchJobProcess status history.
//
// There is no create/update HTTP endpoint for statuses, by design: they are recorded
// internally rather than accepted from a client request body — an initial CREATED
// status when a process is submitted (see Service.Create), and later transitions by
// whatever submits and monitors the actual job. Reads are still a service concern
// other code can call directly, and (via StatusController) a read-only API a client
// can poll for progress.
type StatusService struct {
	db        *gorm.DB
	statuses  *StatusRepository
	processes *Repository
}

// NewStatusService returns a status service.
func NewStatusService(db *gorm.DB, statuses *StatusRepository, processes *Repository) *StatusService {
	return &StatusService{db: db, statuses: statuses, processes: processes}
}

// Record appends a new status for a process, in its own transaction.
//
// This is the entry point for a caller that is not already inside a transaction of
// its own — the normal case for a service in another package recording a transition
// it observed (e.g. a job monitor noticing a submission succeeded). It does not check
// authorisation: recording a status is an internal operation performed by trusted
// service code, never taken directly from a client request.
func (s *StatusService) Record(ctx context.Context, processID string, status model.BatchProcessStatusType, log *string) (*dto.StatusResponse, error) {
	var out dto.StatusResponse
	err := s.db.WithContext(ctx).Transaction(func(tx *gorm.DB) error {
		created, err := s.RecordTx(ctx, tx, processID, status, log)
		if err != nil {
			return err
		}
		out = dto.ToStatusResponse(created)
		return nil
	})
	if err != nil {
		return nil, err
	}
	return &out, nil
}

// RecordTx appends a new status inside an already-open transaction, so a caller that
// is already inside one of its own — Service.Create recording the initial CREATED
// status alongside the process it belongs to, or another service's own multi-step
// write — commits or rolls back the status together with the rest of its work.
func (s *StatusService) RecordTx(ctx context.Context, tx *gorm.DB, processID string, status model.BatchProcessStatusType, log *string) (*model.BatchJobProcessStatus, error) {
	if _, err := s.processes.WithTx(tx).FindByID(ctx, processID); err != nil {
		return nil, notFoundAs(err, "Process not found: %s", processID)
	}

	created := &model.BatchJobProcessStatus{
		ProcessID: &processID,
		Status:    &status,
		Log:       log,
		Timestamp: ptr.To(time.Now().UnixMilli()),
	}
	if err := s.statuses.WithTx(tx).Create(ctx, created); err != nil {
		return nil, err
	}
	return created, nil
}

// ListForProcess returns every recorded status of a process, oldest first.
//
// Not owner-scoped, matching the rest of this package's reads (see Service's doc
// comment) — the Java service carried no authorisation on process status either.
func (s *StatusService) ListForProcess(ctx context.Context, processID string) ([]dto.StatusResponse, error) {
	if _, err := s.processes.FindByID(ctx, processID); err != nil {
		return nil, notFoundAs(err, "Process not found: %s", processID)
	}
	found, err := s.statuses.FindByProcessID(ctx, processID)
	if err != nil {
		return nil, err
	}
	return dto.ToStatusResponses(found), nil
}

// Get returns one status, scoped to the process it must belong to.
func (s *StatusService) Get(ctx context.Context, processID, statusID string) (*dto.StatusResponse, error) {
	if _, err := s.processes.FindByID(ctx, processID); err != nil {
		return nil, notFoundAs(err, "Process not found: %s", processID)
	}
	found, err := s.statuses.FindByIDAndProcessID(ctx, statusID, processID)
	if err != nil {
		return nil, notFoundAs(err, "Status not found: %s in process %s", statusID, processID)
	}
	out := dto.ToStatusResponse(found)
	return &out, nil
}
