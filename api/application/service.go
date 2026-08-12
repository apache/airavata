package application

import (
	"context"
	"errors"
	"strings"

	"gorm.io/gorm"

	"github.com/apache/airavata/api/compute"
	"github.com/apache/airavata/api/credentials"
	"github.com/apache/airavata/internal/auth"
	"github.com/apache/airavata/internal/httpx"

	dto "github.com/apache/airavata/api/application/dto"
	model "github.com/apache/airavata/api/application/model"
	credentialsmodel "github.com/apache/airavata/api/credentials/model"
)

func notFoundAs(err error, format string, args ...any) error {
	if errors.Is(err, gorm.ErrRecordNotFound) {
		return httpx.NotFound(format, args...)
	}
	return err
}

// TemplateService manages application templates and their owned declarations.
//
// Reads are open; writes are administrative. A template describes an application's
// contract independently of where it runs, so its declarations are replaced wholesale
// on update rather than patched.
type TemplateService struct {
	db          *gorm.DB
	templates   *TemplateRepository
	deployments *BatchDeploymentRepository
}

// NewTemplateService returns a template service.
func NewTemplateService(db *gorm.DB, templates *TemplateRepository, deployments *BatchDeploymentRepository) *TemplateService {
	return &TemplateService{db: db, templates: templates, deployments: deployments}
}

// List returns every template.
func (s *TemplateService) List(ctx context.Context) ([]dto.TemplateResponse, error) {
	templates, err := s.templates.FindAll(ctx)
	if err != nil {
		return nil, err
	}
	out := make([]dto.TemplateResponse, 0, len(templates))
	for i := range templates {
		out = append(out, dto.ToTemplateResponse(&templates[i]))
	}
	return out, nil
}

// Get returns one template.
func (s *TemplateService) Get(ctx context.Context, id string) (*dto.TemplateResponse, error) {
	template, err := s.templates.FindByID(ctx, id)
	if err != nil {
		return nil, notFoundAs(err, "Template not found: %s", id)
	}
	out := dto.ToTemplateResponse(template)
	return &out, nil
}

// Create registers a template together with its declarations.
func (s *TemplateService) Create(ctx context.Context, req *dto.TemplateRequest) (*dto.TemplateResponse, error) {
	if _, err := auth.RequireAdmin(ctx); err != nil {
		return nil, err
	}

	var out dto.TemplateResponse
	err := s.db.WithContext(ctx).Transaction(func(tx *gorm.DB) error {
		templates := s.templates.WithTx(tx)

		name := req.TemplateName
		template := &model.Template{TemplateName: &name, TemplateDescription: req.TemplateDescription}
		if err := templates.SaveTemplate(ctx, template); err != nil {
			return err
		}
		if err := templates.ReplaceChildren(ctx, template.ID, dto.ToInputEntities(req.Inputs), dto.ToOutputEntities(req.Outputs)); err != nil {
			return err
		}

		saved, err := templates.FindByID(ctx, template.ID)
		if err != nil {
			return err
		}
		out = dto.ToTemplateResponse(saved)
		return nil
	})
	if err != nil {
		return nil, err
	}
	return &out, nil
}

// Update replaces a template's fields and its declaration set.
func (s *TemplateService) Update(ctx context.Context, id string, req *dto.TemplateRequest) (*dto.TemplateResponse, error) {
	if _, err := auth.RequireAdmin(ctx); err != nil {
		return nil, err
	}

	var out dto.TemplateResponse
	err := s.db.WithContext(ctx).Transaction(func(tx *gorm.DB) error {
		templates := s.templates.WithTx(tx)

		template, err := templates.FindByID(ctx, id)
		if err != nil {
			return notFoundAs(err, "Template not found: %s", id)
		}

		name := req.TemplateName
		template.TemplateName = &name
		template.TemplateDescription = req.TemplateDescription
		if err := templates.SaveTemplate(ctx, template); err != nil {
			return err
		}
		if err := templates.ReplaceChildren(ctx, template.ID, dto.ToInputEntities(req.Inputs), dto.ToOutputEntities(req.Outputs)); err != nil {
			return err
		}

		saved, err := templates.FindByID(ctx, template.ID)
		if err != nil {
			return err
		}
		out = dto.ToTemplateResponse(saved)
		return nil
	})
	if err != nil {
		return nil, err
	}
	return &out, nil
}

// Delete removes a template, refusing while it still has deployments.
//
// Deployments outlive individual template edits and are managed through their own
// endpoints, so removing the template out from under them is rejected rather than
// cascaded.
func (s *TemplateService) Delete(ctx context.Context, id string) error {
	if _, err := auth.RequireAdmin(ctx); err != nil {
		return err
	}

	return s.db.WithContext(ctx).Transaction(func(tx *gorm.DB) error {
		templates, deployments := s.templates.WithTx(tx), s.deployments.WithTx(tx)

		template, err := templates.FindByID(ctx, id)
		if err != nil {
			return notFoundAs(err, "Template not found: %s", id)
		}
		deployed, err := deployments.ExistsByTemplateID(ctx, id)
		if err != nil {
			return err
		}
		if deployed {
			return httpx.Conflict("Template has deployments and cannot be deleted: %s", id)
		}
		return templates.Delete(ctx, template)
	})
}

// BatchDeploymentService manages deployments: a template made runnable somewhere.
type BatchDeploymentService struct {
	db          *gorm.DB
	deployments *BatchDeploymentRepository
	templates   *TemplateRepository
	clusters    *compute.ClusterRepository
	sshCreds    *credentials.SSHUserCredentialRepository
}

// NewBatchDeploymentService returns a deployment service.
func NewBatchDeploymentService(
	db *gorm.DB,
	deployments *BatchDeploymentRepository,
	templates *TemplateRepository,
	clusters *compute.ClusterRepository,
	sshCreds *credentials.SSHUserCredentialRepository,
) *BatchDeploymentService {
	return &BatchDeploymentService{db: db, deployments: deployments, templates: templates, clusters: clusters, sshCreds: sshCreds}
}

// List returns every deployment, or only those of templateID when it is non-empty.
func (s *BatchDeploymentService) List(ctx context.Context, templateID string) ([]dto.BatchDeploymentResponse, error) {
	var (
		found []model.BatchDeployment
		err   error
	)
	if templateID == "" {
		found, err = s.deployments.FindAll(ctx)
	} else {
		found, err = s.deployments.FindByTemplateID(ctx, templateID)
	}
	if err != nil {
		return nil, err
	}
	out := make([]dto.BatchDeploymentResponse, 0, len(found))
	for i := range found {
		out = append(out, dto.ToBatchDeploymentResponse(&found[i]))
	}
	return out, nil
}

// Get returns one deployment.
func (s *BatchDeploymentService) Get(ctx context.Context, id string) (*dto.BatchDeploymentResponse, error) {
	deployment, err := s.deployments.FindByID(ctx, id)
	if err != nil {
		return nil, notFoundAs(err, "Deployment not found: %s", id)
	}
	out := dto.ToBatchDeploymentResponse(deployment)
	return &out, nil
}

// Create registers a deployment and the resource request it owns.
func (s *BatchDeploymentService) Create(ctx context.Context, req *dto.BatchDeploymentRequest) (*dto.BatchDeploymentResponse, error) {
	if _, err := auth.RequireAdmin(ctx); err != nil {
		return nil, err
	}

	var out dto.BatchDeploymentResponse
	err := s.db.WithContext(ctx).Transaction(func(tx *gorm.DB) error {
		deployments := s.deployments.WithTx(tx)

		template, cluster, sshCred, err := s.resolveReferences(ctx, tx, req)
		if err != nil {
			return err
		}

		config := &model.BatchJobConfig{}
		dto.ApplyBatchJobConfigRequest(config, req.BatchJobConfig)
		if err := deployments.SaveConfig(ctx, config); err != nil {
			return err
		}

		deployment := &model.BatchDeployment{
			TemplateID:                    &template.ID,
			ClusterID:                     cluster,
			SlurmRunSection:               req.SlurmRunSection,
			BatchJobConfigID:              config.ID,
			BatchJobConfig:                config,
			DefaultSubmissionCredentialID: sshCred.ID,
			WorkDir:                       req.WorkDir,
			Partition:                     req.Partition,
		}
		if err := deployments.Save(ctx, deployment); err != nil {
			return err
		}
		out = dto.ToBatchDeploymentResponse(deployment)
		return nil
	})
	if err != nil {
		return nil, err
	}
	return &out, nil
}

// Update changes a deployment, mutating its owned config in place so the config row
// survives the edit rather than being replaced.
func (s *BatchDeploymentService) Update(ctx context.Context, id string, req *dto.BatchDeploymentRequest) (*dto.BatchDeploymentResponse, error) {
	if _, err := auth.RequireAdmin(ctx); err != nil {
		return nil, err
	}

	var out dto.BatchDeploymentResponse
	err := s.db.WithContext(ctx).Transaction(func(tx *gorm.DB) error {
		deployments := s.deployments.WithTx(tx)

		deployment, err := deployments.FindByID(ctx, id)
		if err != nil {
			return notFoundAs(err, "Deployment not found: %s", id)
		}
		template, cluster, sshCred, err := s.resolveReferences(ctx, tx, req)
		if err != nil {
			return err
		}

		config := deployment.BatchJobConfig
		if config == nil {
			config = &model.BatchJobConfig{ID: deployment.BatchJobConfigID}
		}
		dto.ApplyBatchJobConfigRequest(config, req.BatchJobConfig)
		if err := deployments.SaveConfig(ctx, config); err != nil {
			return err
		}

		deployment.TemplateID = &template.ID
		deployment.ClusterID = cluster
		deployment.SlurmRunSection = req.SlurmRunSection
		deployment.BatchJobConfigID = config.ID
		deployment.BatchJobConfig = config
		deployment.DefaultSubmissionCredentialID = sshCred.ID
		deployment.WorkDir = req.WorkDir
		deployment.Partition = req.Partition

		if err := deployments.Save(ctx, deployment); err != nil {
			return err
		}
		out = dto.ToBatchDeploymentResponse(deployment)
		return nil
	})
	if err != nil {
		return nil, err
	}
	return &out, nil
}

// Delete removes a deployment and its owned config.
func (s *BatchDeploymentService) Delete(ctx context.Context, id string) error {
	if _, err := auth.RequireAdmin(ctx); err != nil {
		return err
	}
	deployment, err := s.deployments.FindByID(ctx, id)
	if err != nil {
		return notFoundAs(err, "Deployment not found: %s", id)
	}
	return s.deployments.Delete(ctx, deployment)
}

// resolveReferences looks up the template, optional cluster and submission credential
// a deployment request names.
//
// The cluster is the only optional one: an absent or blank id is legitimate and
// leaves the deployment unbound to a cluster, while an id that is supplied but
// unknown is an error. Conflating those would let a typo silently unbind a deployment.
func (s *BatchDeploymentService) resolveReferences(ctx context.Context, tx *gorm.DB, req *dto.BatchDeploymentRequest) (*model.Template, *string, *credentialsmodel.SSHUserCredential, error) {
	template, err := s.templates.WithTx(tx).FindByID(ctx, req.TemplateID)
	if err != nil {
		return nil, nil, nil, notFoundAs(err, "Template not found: %s", req.TemplateID)
	}

	var clusterID *string
	if req.SlurmClusterID != nil && strings.TrimSpace(*req.SlurmClusterID) != "" {
		cluster, err := s.clusters.WithTx(tx).FindByID(ctx, *req.SlurmClusterID)
		if err != nil {
			return nil, nil, nil, notFoundAs(err, "Cluster not found: %s", *req.SlurmClusterID)
		}
		clusterID = &cluster.ID
	}

	sshCred, err := s.sshCreds.WithTx(tx).FindByID(ctx, req.DefaultSubmissionCredentialID)
	if err != nil {
		return nil, nil, nil, notFoundAs(err, "SSH credential not found: %s", req.DefaultSubmissionCredentialID)
	}

	return template, clusterID, sshCred, nil
}
