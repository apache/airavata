// Package service holds the process vertical's business rules: submitting a run under
// the caller's own identity, and recording what happened to it.
package service

import (
	"context"
	"errors"
	"strings"

	"gorm.io/gorm"

	"github.com/apache/airavata/internal/auth"
	"github.com/apache/airavata/internal/httpx"

	applicationdto "github.com/apache/airavata/api/application/dto"
	applicationmodel "github.com/apache/airavata/api/application/model"
	applicationrepo "github.com/apache/airavata/api/application/repository"
	credsvc "github.com/apache/airavata/api/credentials/service"
	iamrepo "github.com/apache/airavata/api/iam/repository"
	dto "github.com/apache/airavata/api/process/dto"
	model "github.com/apache/airavata/api/process/model"
	"github.com/apache/airavata/api/process/repository"
)

func notFoundAs(err error, format string, args ...any) error {
	if errors.Is(err, gorm.ErrRecordNotFound) {
		return httpx.NotFound(format, args...)
	}
	return err
}

// ProcessService manages processes: individual runs, of whatever kind.
//
// Everything a run carries is managed from here. A batch process is written, read and
// deleted as a section of the process that owns it, never addressed on its own, so
// there is no service for it either — the API surface and the service surface say the
// same thing about what is a resource.
//
// Note the asymmetry with SSH endpoint credentials and SCP data. Submitting is
// self-service and ownership is taken from the token, but reads are not owner-scoped:
// listing by deployment and fetching by id carry no authorisation at all, matching
// the Java service. That is worth revisiting, but it is the behaviour clients have.
type ProcessService struct {
	db          *gorm.DB
	processes   *repository.ProcessRepository
	deployments *applicationrepo.BatchDeploymentRepository
	credentials *credsvc.CredentialAccess
	users       *iamrepo.UserRepository
	statuses    *StatusService
}

// NewProcessService returns a process service.
func NewProcessService(
	db *gorm.DB,
	processes *repository.ProcessRepository,
	deployments *applicationrepo.BatchDeploymentRepository,
	credentials *credsvc.CredentialAccess,
	users *iamrepo.UserRepository,
	statuses *StatusService,
) *ProcessService {
	return &ProcessService{
		db:          db,
		processes:   processes,
		deployments: deployments,
		credentials: credentials,
		users:       users,
		statuses:    statuses,
	}
}

// List returns every process across every user. Admin only.
func (s *ProcessService) List(ctx context.Context) ([]dto.Response, error) {
	if _, err := auth.RequireAdmin(ctx); err != nil {
		return nil, err
	}
	found, err := s.processes.FindAll(ctx)
	if err != nil {
		return nil, err
	}
	return dto.ToResponses(found), nil
}

// ListByDeployment returns every process run against one deployment.
func (s *ProcessService) ListByDeployment(ctx context.Context, deploymentID string) ([]dto.Response, error) {
	found, err := s.processes.FindByDeploymentID(ctx, deploymentID)
	if err != nil {
		return nil, err
	}
	return dto.ToResponses(found), nil
}

// Get returns one process with every section it carries.
func (s *ProcessService) Get(ctx context.Context, id string) (*dto.Response, error) {
	proc, err := s.processes.FindByID(ctx, id)
	if err != nil {
		return nil, notFoundAs(err, "Process not found: %s", id)
	}
	out := dto.ToResponse(proc)
	return &out, nil
}

// Create submits a process for the calling user.
//
// Any authenticated caller may submit for themselves — this is self-service, not an
// administrative operation — and the owner always comes from the token.
func (s *ProcessService) Create(ctx context.Context, req *dto.Request) (*dto.Response, error) {
	principal, err := auth.RequireAuthenticated(ctx)
	if err != nil {
		return nil, err
	}

	var out dto.Response
	err = s.db.WithContext(ctx).Transaction(func(tx *gorm.DB) error {
		processes, users := s.processes.WithTx(tx), s.users.WithTx(tx)

		owner, err := users.FindByID(ctx, principal.Name)
		if err != nil {
			return notFoundAs(err, "No user record found for authenticated principal: %s", principal.Name)
		}

		proc := &model.Process{OwnerID: &owner.ID, ProcessType: req.ProcessType}
		if err := processes.Save(ctx, proc); err != nil {
			return err
		}

		if req.BatchProcess != nil {
			batch, err := s.saveBatchProcess(ctx, tx, proc.ID, nil, req.BatchProcess)
			if err != nil {
				return err
			}
			if err := s.saveMappings(ctx, tx, batch.ID, req.BatchProcess); err != nil {
				return err
			}
		}

		// Recorded in the same transaction as the process itself, so a caller never
		// observes a process that exists but has no status yet.
		if _, err := s.statuses.RecordTx(ctx, tx, proc.ID, model.ProcessStatusTypeCreated, nil); err != nil {
			return err
		}

		// Read back rather than assembled from the request, so the response is the
		// stored row — including last_status_id, which the status write just set.
		return s.render(ctx, processes, proc.ID, &out)
	})
	if err != nil {
		return nil, err
	}
	return &out, nil
}

// Update is an administrative correction of a process: its deployment, the resources
// it asked for, or its template mappings.
//
// Two things are deliberately immutable. Ownership, because re-deriving it from the
// caller's token would reassign the process to whichever admin issued the request.
// And the process type, because the sections a process carries follow from it — a
// BATCH_JOB turned into something else would strand its batch process with no way to
// reach it.
func (s *ProcessService) Update(ctx context.Context, id string, req *dto.Request) (*dto.Response, error) {
	if _, err := auth.RequireAdmin(ctx); err != nil {
		return nil, err
	}

	var out dto.Response
	err := s.db.WithContext(ctx).Transaction(func(tx *gorm.DB) error {
		processes := s.processes.WithTx(tx)

		proc, err := processes.FindByID(ctx, id)
		if err != nil {
			return notFoundAs(err, "Process not found: %s", id)
		}
		if proc.ProcessType != nil && req.ProcessType != nil && *proc.ProcessType != *req.ProcessType {
			return httpx.Conflict("Process type cannot be changed: %s is a %s process", id, *proc.ProcessType)
		}

		if req.BatchProcess != nil {
			batch, err := s.saveBatchProcess(ctx, tx, proc.ID, proc.BatchProcess, req.BatchProcess)
			if err != nil {
				return err
			}
			if err := s.saveMappings(ctx, tx, batch.ID, req.BatchProcess); err != nil {
				return err
			}
		}
		return s.render(ctx, processes, proc.ID, &out)
	})
	if err != nil {
		return nil, err
	}
	return &out, nil
}

// Delete removes a process and everything it owns: its batch process and that
// section's resource request, its status history, its tasks and its mappings. Admin
// only.
func (s *ProcessService) Delete(ctx context.Context, id string) error {
	if _, err := auth.RequireAdmin(ctx); err != nil {
		return err
	}
	return s.db.WithContext(ctx).Transaction(func(tx *gorm.DB) error {
		processes := s.processes.WithTx(tx)
		proc, err := processes.FindByID(ctx, id)
		if err != nil {
			return notFoundAs(err, "Process not found: %s", id)
		}
		return processes.Delete(ctx, proc)
	})
}

// saveBatchProcess writes the batchProcess section, creating it on first write and
// mutating it afterwards.
//
// existing is the section already stored, or nil when there is none. Its ids are
// carried over rather than regenerated: a new BatchJobConfig id on every update would
// orphan the previous row, which is exactly what AfterDelete exists to prevent.
func (s *ProcessService) saveBatchProcess(
	ctx context.Context,
	tx *gorm.DB,
	processID string,
	existing *model.BatchJobProcess,
	req *dto.BatchProcessRequest,
) (*model.BatchJobProcess, error) {
	processes, deployments := s.processes.WithTx(tx), s.deployments.WithTx(tx)

	deployment, err := deployments.FindByID(ctx, req.DeploymentID)
	if err != nil {
		return nil, notFoundAs(err, "Deployment not found: %s", req.DeploymentID)
	}
	submissionCredentialID, err := s.resolveSubmissionCredential(ctx, tx, req)
	if err != nil {
		return nil, err
	}

	batch := &model.BatchJobProcess{ProcessID: &processID}
	config := &applicationmodel.BatchJobConfig{}
	if existing != nil {
		batch.ID = existing.ID
		batch.BatchJobConfigID = existing.BatchJobConfigID
		if existing.BatchJobConfig != nil {
			config = existing.BatchJobConfig
		} else {
			config.ID = existing.BatchJobConfigID
		}
	}

	applicationdto.ApplyBatchJobConfigRequest(config, req.BatchJobConfig)
	if err := processes.SaveBatchJobConfig(ctx, config); err != nil {
		return nil, err
	}

	dto.ApplyBatchProcessRequest(batch, req)
	batch.DeploymentID = &deployment.ID
	batch.SubmissionCredentialID = submissionCredentialID
	batch.BatchJobConfigID = config.ID
	if err := processes.SaveBatchProcess(ctx, batch); err != nil {
		return nil, err
	}
	return batch, nil
}

// resolveSubmissionCredential authorises the SSH endpoint credential binding this run
// submits under.
//
// The binding is authorised against the caller — this is the one place in a
// self-service submission where a caller supplies an identity to act under, so
// RequireUsable is what keeps them to their own bindings and the ones shared with
// them. A deployment carries no default to fall back on, so validation already
// rejects a request that names none.
func (s *ProcessService) resolveSubmissionCredential(
	ctx context.Context,
	tx *gorm.DB,
	req *dto.BatchProcessRequest,
) (string, error) {
	if req.SubmissionCredentialID == nil || strings.TrimSpace(*req.SubmissionCredentialID) == "" {
		return "", httpx.BadRequest("Submission credential id cannot be blank")
	}
	credential, err := s.credentials.WithTx(tx).RequireUsable(ctx, *req.SubmissionCredentialID)
	if err != nil {
		return "", err
	}
	return credential.ID, nil
}

// saveMappings replaces a batch process's template input and output mapping sets.
//
// The sets are part of the batchProcess section rather than of the process, so this
// runs after the section has been written and is keyed by its id. A process with no
// batch section carries no mappings at all — there is no field to send them in.
func (s *ProcessService) saveMappings(ctx context.Context, tx *gorm.DB, batchProcessID string, req *dto.BatchProcessRequest) error {
	processes := s.processes.WithTx(tx)
	if err := processes.ReplaceInputMappings(ctx, batchProcessID, dto.ToInputMappingEntities(batchProcessID, req.InputMappings)); err != nil {
		return err
	}
	return processes.ReplaceOutputMappings(ctx, batchProcessID, dto.ToOutputMappingEntities(batchProcessID, req.OutputMappings))
}

// render reloads a process and writes its response into out.
func (s *ProcessService) render(ctx context.Context, processes *repository.ProcessRepository, id string, out *dto.Response) error {
	stored, err := processes.FindByID(ctx, id)
	if err != nil {
		return err
	}
	*out = dto.ToResponse(stored)
	return nil
}
