package controller

import (
	"net/http"

	"github.com/apache/airavata/internal/httpx"

	dto "github.com/apache/airavata/api/credentials/dto"
	"github.com/apache/airavata/api/credentials/service"
)

// SSHEndpointController serves /api/v1/ssh-endpoints.
type SSHEndpointController struct{ svc *service.SSHEndpointService }

// NewSSHEndpointController returns a handler delegating to svc.
func NewSSHEndpointController(svc *service.SSHEndpointService) *SSHEndpointController {
	return &SSHEndpointController{svc: svc}
}

// Register mounts the SSH endpoint routes.
func (h *SSHEndpointController) Register(mux *http.ServeMux) {
	mux.HandleFunc("GET /api/v1/ssh-endpoints", h.list)
	mux.HandleFunc("POST /api/v1/ssh-endpoints", h.create)
	mux.HandleFunc("GET /api/v1/ssh-endpoints/{sshEndpointId}", h.get)
	mux.HandleFunc("PUT /api/v1/ssh-endpoints/{sshEndpointId}", h.update)
	mux.HandleFunc("DELETE /api/v1/ssh-endpoints/{sshEndpointId}", h.delete)
}

func (h *SSHEndpointController) list(w http.ResponseWriter, r *http.Request) {
	endpoints, err := h.svc.List(r.Context())
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusOK, endpoints)
}

func (h *SSHEndpointController) get(w http.ResponseWriter, r *http.Request) {
	endpoint, err := h.svc.Get(r.Context(), r.PathValue("sshEndpointId"))
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusOK, endpoint)
}

func (h *SSHEndpointController) create(w http.ResponseWriter, r *http.Request) {
	var req dto.SSHEndpointRequest
	if err := httpx.Bind(r, &req); err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	endpoint, err := h.svc.Create(r.Context(), &req)
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusCreated, endpoint)
}

func (h *SSHEndpointController) update(w http.ResponseWriter, r *http.Request) {
	var req dto.SSHEndpointRequest
	if err := httpx.Bind(r, &req); err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	endpoint, err := h.svc.Update(r.Context(), r.PathValue("sshEndpointId"), &req)
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusOK, endpoint)
}

func (h *SSHEndpointController) delete(w http.ResponseWriter, r *http.Request) {
	if err := h.svc.Delete(r.Context(), r.PathValue("sshEndpointId")); err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusNoContent, nil)
}
