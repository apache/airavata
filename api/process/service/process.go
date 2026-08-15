// Package service holds the process vertical's business rules: submitting a job under
// the caller's own identity, and recording what happened to it.
package service

import (
	"context"
	"errors"

	"gorm.io/gorm"

	"github.com/apache/airavata/internal/auth"
	"github.com/apache/airavata/internal/httpx"

	applicationdto "github.com/apache/airavata/api/application/dto"
	applicationmodel "github.com/apache/airavata/api/application/model"
	applicationrepo "github.com/apache/airavata/api/application/repository"
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

// ProcessService manages batch job processes: individual runs of a deployment.
//
// Note the asymmetry with SSH endpoint credentials and SCP data. Submitting is
// self-service and ownership is taken from the token, but reads are not owner-scoped:
// listing by deployment and fetching by id carry no authorisation at all, matching
// the Java service. That is worth revisiting, but it is the behaviour clients have.
type ProcessService struct {
	db          *gorm.DB
	processes   *repository.ProcessRepository
	deployments *applicationrepo.BatchDeploymentRepository
	users       *iamrepo.UserRepository
	statuses    *StatusService
}

// NewProcessService returns a batch job process service.
func NewProcessService(
	db *gorm.DB,
	processes *repository.ProcessRepository,
	deployments *applicationrepo.BatchDeploymentRepository,
	users *iamrepo.UserRepository,
	statuses *StatusService,
) *ProcessService {
	return &ProcessService{db: db, processes: processes, deployments: deployments, users: users, statuses: statuses}
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

// ListByDeployment returns every process of one deployment.
func (s *ProcessService) ListByDeployment(ctx context.Context, deploymentID string) ([]dto.Response, error) {
	found, err := s.processes.FindByDeploymentID(ctx, deploymentID)
	if err != nil {
		return nil, err
	}
	return dto.ToResponses(found), nil
}

// Get returns one process.
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
		processes, deployments, users := s.processes.WithTx(tx), s.deployments.WithTx(tx), s.users.WithTx(tx)

		deployment, err := deployments.FindByID(ctx, req.DeploymentID)
		if err != nil {
			return notFoundAs(err, "Deployment not found: %s", req.DeploymentID)
		}
		owner, err := users.FindByID(ctx, principal.Name)
		if err != nil {
			return notFoundAs(err, "No user record found for authenticated principal: %s", principal.Name)
		}

		config := &applicationmodel.BatchJobConfig{}
		applicationdto.ApplyBatchJobConfigRequest(config, req.BatchJobConfig)
		if err := processes.SaveConfig(ctx, config); err != nil {
			return err
		}

		proc := &model.BatchJobProcess{
			DeploymentID:     &deployment.ID,
			OwnerID:          &owner.ID,
			BatchJobConfigID: config.ID,
			BatchJobConfig:   config,
		}
		if err := processes.Save(ctx, proc); err != nil {
			return err
		}

		// Recorded in the same transaction as the process itself, so a caller never
		// observes a process that exists but has no status yet.
		if _, err := s.statuses.RecordTx(ctx, tx, proc.ID, model.BatchProcessStatusTypeCreated, nil); err != nil {
			return err
		}

		out = dto.ToResponse(proc)
		return nil
	})
	if err != nil {
		return nil, err
	}
	return &out, nil
}

// Update is an administrative correction of a process's deployment or resources.
//
// Ownership is deliberately immutable: re-deriving it from the caller's token would
// reassign the process to whichever admin issued the request.
func (s *ProcessService) Update(ctx context.Context, id string, req *dto.Request) (*dto.Response, error) {
	if _, err := auth.RequireAdmin(ctx); err != nil {
		return nil, err
	}

	var out dto.Response
	err := s.db.WithContext(ctx).Transaction(func(tx *gorm.DB) error {
		processes, deployments := s.processes.WithTx(tx), s.deployments.WithTx(tx)

		proc, err := processes.FindByID(ctx, id)
		if err != nil {
			return notFoundAs(err, "Process not found: %s", id)
		}
		deployment, err := deployments.FindByID(ctx, req.DeploymentID)
		if err != nil {
			return notFoundAs(err, "Deployment not found: %s", req.DeploymentID)
		}

		config := proc.BatchJobConfig
		if config == nil {
			config = &applicationmodel.BatchJobConfig{ID: proc.BatchJobConfigID}
		}
		applicationdto.ApplyBatchJobConfigRequest(config, req.BatchJobConfig)
		if err := processes.SaveConfig(ctx, config); err != nil {
			return err
		}

		proc.DeploymentID = &deployment.ID
		proc.BatchJobConfigID = config.ID
		proc.BatchJobConfig = config

		if err := processes.Save(ctx, proc); err != nil {
			return err
		}
		out = dto.ToResponse(proc)
		return nil
	})
	if err != nil {
		return nil, err
	}
	return &out, nil
}

// Delete removes a process and its owned resource request. Admin only.
func (s *ProcessService) Delete(ctx context.Context, id string) error {
	if _, err := auth.RequireAdmin(ctx); err != nil {
		return err
	}
	proc, err := s.processes.FindByID(ctx, id)
	if err != nil {
		return notFoundAs(err, "Process not found: %s", id)
	}
	return s.processes.Delete(ctx, proc)
}
