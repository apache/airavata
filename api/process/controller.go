package process

import (
	"net/http"

	"github.com/apache/airavata/internal/httpx"

	dto "github.com/apache/airavata/api/process/dto"
)

// Controller serves /api/v1/batch-job-processes.
type Controller struct{ svc *Service }

// NewController returns a handler delegating to svc.
func NewController(svc *Service) *Controller { return &Controller{svc: svc} }

// Register mounts the batch job process routes.
func (h *Controller) Register(mux *http.ServeMux) {
	mux.HandleFunc("GET /api/v1/batch-job-processes", h.list)
	mux.HandleFunc("POST /api/v1/batch-job-processes", h.create)
	mux.HandleFunc("GET /api/v1/batch-job-processes/{processId}", h.get)
	mux.HandleFunc("PUT /api/v1/batch-job-processes/{processId}", h.update)
	mux.HandleFunc("DELETE /api/v1/batch-job-processes/{processId}", h.delete)
}

// list returns every process, or only those of a deployment when deploymentId is
// supplied. The two branches have different authorisation: the unfiltered listing is
// admin only, while the per-deployment one is open.
func (h *Controller) list(w http.ResponseWriter, r *http.Request) {
	var (
		found []dto.Response
		err   error
	)
	if deploymentID := r.URL.Query().Get("deploymentId"); deploymentID != "" {
		found, err = h.svc.ListByDeployment(r.Context(), deploymentID)
	} else {
		found, err = h.svc.List(r.Context())
	}
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusOK, found)
}

func (h *Controller) get(w http.ResponseWriter, r *http.Request) {
	proc, err := h.svc.Get(r.Context(), r.PathValue("processId"))
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusOK, proc)
}

func (h *Controller) create(w http.ResponseWriter, r *http.Request) {
	var req dto.Request
	if err := httpx.Bind(r, &req); err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	proc, err := h.svc.Create(r.Context(), &req)
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusCreated, proc)
}

func (h *Controller) update(w http.ResponseWriter, r *http.Request) {
	var req dto.Request
	if err := httpx.Bind(r, &req); err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	proc, err := h.svc.Update(r.Context(), r.PathValue("processId"), &req)
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusOK, proc)
}

func (h *Controller) delete(w http.ResponseWriter, r *http.Request) {
	if err := h.svc.Delete(r.Context(), r.PathValue("processId")); err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusNoContent, nil)
}
