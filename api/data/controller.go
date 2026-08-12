package data

import (
	"net/http"

	"github.com/apache/airavata/internal/httpx"

	dto "github.com/apache/airavata/api/data/dto"
)

// Controller serves /api/v1/scp-data.
type Controller struct{ svc *Service }

// NewController returns a handler delegating to svc.
func NewController(svc *Service) *Controller { return &Controller{svc: svc} }

// Register mounts the SCP data routes. The literal /me pattern takes precedence over
// /{id}, so "me" is never mistaken for a dataset id.
func (h *Controller) Register(mux *http.ServeMux) {
	mux.HandleFunc("GET /api/v1/scp-data", h.list)
	mux.HandleFunc("GET /api/v1/scp-data/me", h.listMine)
	mux.HandleFunc("POST /api/v1/scp-data", h.create)
	mux.HandleFunc("GET /api/v1/scp-data/{id}", h.get)
	mux.HandleFunc("PUT /api/v1/scp-data/{id}", h.update)
	mux.HandleFunc("DELETE /api/v1/scp-data/{id}", h.delete)
}

func (h *Controller) list(w http.ResponseWriter, r *http.Request) {
	found, err := h.svc.List(r.Context())
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusOK, found)
}

func (h *Controller) listMine(w http.ResponseWriter, r *http.Request) {
	found, err := h.svc.ListMine(r.Context())
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusOK, found)
}

func (h *Controller) get(w http.ResponseWriter, r *http.Request) {
	dataset, err := h.svc.Get(r.Context(), r.PathValue("id"))
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusOK, dataset)
}

func (h *Controller) create(w http.ResponseWriter, r *http.Request) {
	var req dto.SCPDataRequest
	if err := httpx.Bind(r, &req); err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	dataset, err := h.svc.Create(r.Context(), &req)
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusCreated, dataset)
}

func (h *Controller) update(w http.ResponseWriter, r *http.Request) {
	var req dto.SCPDataRequest
	if err := httpx.Bind(r, &req); err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	dataset, err := h.svc.Update(r.Context(), r.PathValue("id"), &req)
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusOK, dataset)
}

func (h *Controller) delete(w http.ResponseWriter, r *http.Request) {
	if err := h.svc.Delete(r.Context(), r.PathValue("id")); err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusNoContent, nil)
}
