package controller

import (
	"net/http"

	"github.com/apache/airavata/internal/httpx"

	"github.com/apache/airavata/api/process/service"
)

// StatusController serves read-only access to a process's status history, nested
// under /api/v1/processes/{processId}.
//
// There is deliberately no POST or PUT: statuses are recorded internally by
// service.StatusService — from process submission and from whatever submits and monitors the
// actual job — never accepted as a request body from a client.
type StatusController struct{ svc *service.StatusService }

// NewStatusController returns a handler delegating to svc.
func NewStatusController(svc *service.StatusService) *StatusController {
	return &StatusController{svc: svc}
}

// Register mounts the read-only status routes.
func (h *StatusController) Register(mux *http.ServeMux) {
	mux.HandleFunc("GET /api/v1/processes/{processId}/statuses", h.list)
	mux.HandleFunc("GET /api/v1/processes/{processId}/statuses/{statusId}", h.get)
}

func (h *StatusController) list(w http.ResponseWriter, r *http.Request) {
	statuses, err := h.svc.ListForProcess(r.Context(), r.PathValue("processId"))
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusOK, statuses)
}

func (h *StatusController) get(w http.ResponseWriter, r *http.Request) {
	status, err := h.svc.Get(r.Context(), r.PathValue("processId"), r.PathValue("statusId"))
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusOK, status)
}
