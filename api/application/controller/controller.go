// Package controller serves the application template and batch deployment routes.
package controller

import (
	"net/http"

	"github.com/apache/airavata/internal/httpx"

	dto "github.com/apache/airavata/api/application/dto"
	"github.com/apache/airavata/api/application/service"
)

// TemplateController serves /api/v1/application-templates.
type TemplateController struct{ svc *service.TemplateService }

// NewTemplateController returns a handler delegating to svc.
func NewTemplateController(svc *service.TemplateService) *TemplateController {
	return &TemplateController{svc: svc}
}

// Register mounts the template routes.
func (h *TemplateController) Register(mux *http.ServeMux) {
	mux.HandleFunc("GET /api/v1/application-templates", h.list)
	mux.HandleFunc("POST /api/v1/application-templates", h.create)
	mux.HandleFunc("GET /api/v1/application-templates/{templateId}", h.get)
	mux.HandleFunc("PUT /api/v1/application-templates/{templateId}", h.update)
	mux.HandleFunc("DELETE /api/v1/application-templates/{templateId}", h.delete)
}

func (h *TemplateController) list(w http.ResponseWriter, r *http.Request) {
	templates, err := h.svc.List(r.Context())
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusOK, templates)
}

func (h *TemplateController) get(w http.ResponseWriter, r *http.Request) {
	template, err := h.svc.Get(r.Context(), r.PathValue("templateId"))
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusOK, template)
}

func (h *TemplateController) create(w http.ResponseWriter, r *http.Request) {
	var req dto.TemplateRequest
	if err := httpx.Bind(r, &req); err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	template, err := h.svc.Create(r.Context(), &req)
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusCreated, template)
}

func (h *TemplateController) update(w http.ResponseWriter, r *http.Request) {
	var req dto.TemplateRequest
	if err := httpx.Bind(r, &req); err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	template, err := h.svc.Update(r.Context(), r.PathValue("templateId"), &req)
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusOK, template)
}

func (h *TemplateController) delete(w http.ResponseWriter, r *http.Request) {
	if err := h.svc.Delete(r.Context(), r.PathValue("templateId")); err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusNoContent, nil)
}

// BatchDeploymentController serves /api/v1/slurm-deployments.
type BatchDeploymentController struct {
	svc *service.BatchDeploymentService
}

// NewBatchDeploymentController returns a handler delegating to svc.
func NewBatchDeploymentController(svc *service.BatchDeploymentService) *BatchDeploymentController {
	return &BatchDeploymentController{svc: svc}
}

// Register mounts the deployment routes.
func (h *BatchDeploymentController) Register(mux *http.ServeMux) {
	mux.HandleFunc("GET /api/v1/slurm-deployments", h.list)
	mux.HandleFunc("POST /api/v1/slurm-deployments", h.create)
	mux.HandleFunc("GET /api/v1/slurm-deployments/{deploymentId}", h.get)
	mux.HandleFunc("PUT /api/v1/slurm-deployments/{deploymentId}", h.update)
	mux.HandleFunc("DELETE /api/v1/slurm-deployments/{deploymentId}", h.delete)
}

func (h *BatchDeploymentController) list(w http.ResponseWriter, r *http.Request) {
	found, err := h.svc.List(r.Context(), r.URL.Query().Get("templateId"))
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusOK, found)
}

func (h *BatchDeploymentController) get(w http.ResponseWriter, r *http.Request) {
	deployment, err := h.svc.Get(r.Context(), r.PathValue("deploymentId"))
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusOK, deployment)
}

func (h *BatchDeploymentController) create(w http.ResponseWriter, r *http.Request) {
	var req dto.BatchDeploymentRequest
	if err := httpx.Bind(r, &req); err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	deployment, err := h.svc.Create(r.Context(), &req)
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusCreated, deployment)
}

func (h *BatchDeploymentController) update(w http.ResponseWriter, r *http.Request) {
	var req dto.BatchDeploymentRequest
	if err := httpx.Bind(r, &req); err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	deployment, err := h.svc.Update(r.Context(), r.PathValue("deploymentId"), &req)
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusOK, deployment)
}

func (h *BatchDeploymentController) delete(w http.ResponseWriter, r *http.Request) {
	if err := h.svc.Delete(r.Context(), r.PathValue("deploymentId")); err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusNoContent, nil)
}
