package iam

import (
	"net/http"

	"github.com/apache/airavata/internal/httpx"

	dto "github.com/apache/airavata/api/iam/dto"
)

// Controller serves /api/v1/users.
type Controller struct{ svc *Service }

// NewController returns a handler delegating to svc.
func NewController(svc *Service) *Controller { return &Controller{svc: svc} }

// Register mounts the user routes.
//
// Update is POST /{userId}, not PUT. That is what the Java controller declared, and
// changing it would break existing clients even though it reads oddly.
func (h *Controller) Register(mux *http.ServeMux) {
	mux.HandleFunc("GET /api/v1/users", h.list)
	mux.HandleFunc("POST /api/v1/users", h.create)
	mux.HandleFunc("GET /api/v1/users/{userId}", h.get)
	mux.HandleFunc("POST /api/v1/users/{userId}", h.update)
}

func (h *Controller) list(w http.ResponseWriter, r *http.Request) {
	users, err := h.svc.List(r.Context())
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusOK, users)
}

// create registers a user.
//
// The Java controller omitted @Valid, so the constraints declared on the registration
// DTO never ran and a blank user id would be accepted as a primary key. They are
// enforced here; that is a deliberate correction, not an oversight.
func (h *Controller) create(w http.ResponseWriter, r *http.Request) {
	var req dto.UserRegistration
	if err := httpx.Bind(r, &req); err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	user, err := h.svc.Register(r.Context(), &req)
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusOK, user)
}

func (h *Controller) get(w http.ResponseWriter, r *http.Request) {
	user, err := h.svc.Get(r.Context(), r.PathValue("userId"))
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusOK, user)
}

func (h *Controller) update(w http.ResponseWriter, r *http.Request) {
	var req dto.UserRegistration
	if err := httpx.Bind(r, &req); err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	user, err := h.svc.Update(r.Context(), r.PathValue("userId"), &req)
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusOK, user)
}
