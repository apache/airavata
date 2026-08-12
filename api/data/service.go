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

func notFoundAs(err error, format string, args ...any) error {
	if errors.Is(err, gorm.ErrRecordNotFound) {
		return httpx.NotFound(format, args...)
	}
	return err
}

// Service manages dataset registrations reachable over SCP.
//
// Ownership works exactly as it does for cluster credentials: it comes from the
// access token, never from the request, and it is immutable once set.
type Service struct {
	db       *gorm.DB
	datasets *SCPDataRepository
	bindings *compute.ClusterCredentialRepository
	users    *iam.UserRepository
}

// NewService returns an SCP data service.
func NewService(
	db *gorm.DB,
	datasets *SCPDataRepository,
	bindings *compute.ClusterCredentialRepository,
	users *iam.UserRepository,
) *Service {
	return &Service{db: db, datasets: datasets, bindings: bindings, users: users}
}

// List returns every dataset across every owner. Admin only.
func (s *Service) List(ctx context.Context) ([]dto.SCPDataResponse, error) {
	if _, err := auth.RequireAdmin(ctx); err != nil {
		return nil, err
	}
	found, err := s.datasets.FindAll(ctx)
	if err != nil {
		return nil, err
	}
	return dto.ToSCPDataResponses(found), nil
}

// ListMine returns the caller's own datasets.
func (s *Service) ListMine(ctx context.Context) ([]dto.SCPDataResponse, error) {
	principal, err := auth.RequireAuthenticated(ctx)
	if err != nil {
		return nil, err
	}
	found, err := s.datasets.FindByOwnerID(ctx, principal.Name)
	if err != nil {
		return nil, err
	}
	return dto.ToSCPDataResponses(found), nil
}

// Get returns one dataset, to its owner or an admin.
func (s *Service) Get(ctx context.Context, id string) (*dto.SCPDataResponse, error) {
	dataset, err := s.requireDataset(ctx, id)
	if err != nil {
		return nil, err
	}
	if err := s.requireSelfOrAdmin(ctx, dataset); err != nil {
		return nil, err
	}
	out := dto.ToSCPDataResponse(dataset)
	return &out, nil
}

// Create registers a dataset for the calling user.
//
// The provision status is forced to REGISTERD rather than accepted from the client:
// lifecycle state is advanced by provisioning, not by whoever submits the form. No
// provisioning workflow exists yet, so nothing currently moves it past this value.
func (s *Service) Create(ctx context.Context, req *dto.SCPDataRequest) (*dto.SCPDataResponse, error) {
	principal, err := auth.RequireAuthenticated(ctx)
	if err != nil {
		return nil, err
	}

	var out dto.SCPDataResponse
	err = s.db.WithContext(ctx).Transaction(func(tx *gorm.DB) error {
		datasets, bindings, users := s.datasets.WithTx(tx), s.bindings.WithTx(tx), s.users.WithTx(tx)

		owner, err := users.FindByID(ctx, principal.Name)
		if err != nil {
			return notFoundAs(err, "No user record found for authenticated principal: %s", principal.Name)
		}
		binding, err := bindings.FindByID(ctx, req.SlurmClusterCredentialID)
		if err != nil {
			return notFoundAs(err, "Slurm cluster credential not found: %s", req.SlurmClusterCredentialID)
		}

		status := model.ProvisionStatusRegistered
		dataset := &model.SCPData{
			ClusterCredentialID: &binding.ID,
			OwnerID:             &owner.ID,
			ProvisionStatus:     &status,
		}
		dto.ApplySCPDataRequest(dataset, req)

		if err := datasets.Save(ctx, dataset); err != nil {
			return err
		}
		out = dto.ToSCPDataResponse(dataset)
		return nil
	})
	if err != nil {
		return nil, err
	}
	return &out, nil
}

// Update changes a dataset, for its owner or an admin.
//
// Neither the owner nor the provision status is touched, so an update cannot
// reassign a dataset or fake its lifecycle state.
func (s *Service) Update(ctx context.Context, id string, req *dto.SCPDataRequest) (*dto.SCPDataResponse, error) {
	var out dto.SCPDataResponse
	err := s.db.WithContext(ctx).Transaction(func(tx *gorm.DB) error {
		datasets, bindings := s.datasets.WithTx(tx), s.bindings.WithTx(tx)

		dataset, err := datasets.FindByID(ctx, id)
		if err != nil {
			return notFoundAs(err, "SCP data not found: %s", id)
		}
		if err := s.requireSelfOrAdmin(ctx, dataset); err != nil {
			return err
		}
		binding, err := bindings.FindByID(ctx, req.SlurmClusterCredentialID)
		if err != nil {
			return notFoundAs(err, "Slurm cluster credential not found: %s", req.SlurmClusterCredentialID)
		}

		dto.ApplySCPDataRequest(dataset, req)
		dataset.ClusterCredentialID = &binding.ID

		if err := datasets.Save(ctx, dataset); err != nil {
			return err
		}
		out = dto.ToSCPDataResponse(dataset)
		return nil
	})
	if err != nil {
		return nil, err
	}
	return &out, nil
}

// Delete removes a dataset, for its owner or an admin.
func (s *Service) Delete(ctx context.Context, id string) error {
	dataset, err := s.requireDataset(ctx, id)
	if err != nil {
		return err
	}
	if err := s.requireSelfOrAdmin(ctx, dataset); err != nil {
		return err
	}
	return s.datasets.Delete(ctx, dataset)
}

func (s *Service) requireDataset(ctx context.Context, id string) (*model.SCPData, error) {
	dataset, err := s.datasets.FindByID(ctx, id)
	if err != nil {
		return nil, notFoundAs(err, "SCP data not found: %s", id)
	}
	return dataset, nil
}

func (s *Service) requireSelfOrAdmin(ctx context.Context, dataset *model.SCPData) error {
	principal, err := auth.RequireAuthenticated(ctx)
	if err != nil {
		return err
	}
	if dataset.OwnedBy(principal.Name) || principal.IsAdmin() {
		return nil
	}
	return httpx.Forbidden("Access denied: you may only access your own SCP data")
}
