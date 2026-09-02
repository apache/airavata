// Package controller serves the SSH key, SSH credential, SSH endpoint and
// endpoint-credential routes.
package controller

import (
	"net/http"

	"github.com/apache/airavata/internal/httpx"

	dto "github.com/apache/airavata/api/credentials/dto"
	"github.com/apache/airavata/api/credentials/service"
)

// SSHKeyController serves /api/v1/ssh-keys.
type SSHKeyController struct{ svc *service.SSHKeyService }

// NewSSHKeyController returns a handler delegating to svc.
func NewSSHKeyController(svc *service.SSHKeyService) *SSHKeyController {
	return &SSHKeyController{svc: svc}
}

// Register mounts the SSH key routes.
func (h *SSHKeyController) Register(mux *http.ServeMux) {
	mux.HandleFunc("GET /api/v1/ssh-keys", h.list)
	mux.HandleFunc("POST /api/v1/ssh-keys", h.create)
	mux.HandleFunc("GET /api/v1/ssh-keys/{sshKeyId}", h.get)
	mux.HandleFunc("PUT /api/v1/ssh-keys/{sshKeyId}", h.update)
	mux.HandleFunc("DELETE /api/v1/ssh-keys/{sshKeyId}", h.delete)
}

func (h *SSHKeyController) list(w http.ResponseWriter, r *http.Request) {
	keys, err := h.svc.List(r.Context())
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusOK, keys)
}

func (h *SSHKeyController) get(w http.ResponseWriter, r *http.Request) {
	key, err := h.svc.Get(r.Context(), r.PathValue("sshKeyId"))
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusOK, key)
}

func (h *SSHKeyController) create(w http.ResponseWriter, r *http.Request) {
	var req dto.SSHKeyRequest
	if err := httpx.Bind(r, &req); err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	key, err := h.svc.Create(r.Context(), &req)
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusCreated, key)
}

func (h *SSHKeyController) update(w http.ResponseWriter, r *http.Request) {
	var req dto.SSHKeyRequest
	if err := httpx.Bind(r, &req); err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	key, err := h.svc.Update(r.Context(), r.PathValue("sshKeyId"), &req)
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusOK, key)
}

func (h *SSHKeyController) delete(w http.ResponseWriter, r *http.Request) {
	if err := h.svc.Delete(r.Context(), r.PathValue("sshKeyId")); err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusNoContent, nil)
}

// SSHUserCredentialController serves /api/v1/ssh-credentials.
type SSHUserCredentialController struct {
	svc *service.SSHUserCredentialService
}

// NewSSHUserCredentialController returns a handler delegating to svc.
func NewSSHUserCredentialController(svc *service.SSHUserCredentialService) *SSHUserCredentialController {
	return &SSHUserCredentialController{svc: svc}
}

// Register mounts the SSH credential routes.
func (h *SSHUserCredentialController) Register(mux *http.ServeMux) {
	mux.HandleFunc("GET /api/v1/ssh-credentials", h.list)
	mux.HandleFunc("POST /api/v1/ssh-credentials", h.create)
	mux.HandleFunc("GET /api/v1/ssh-credentials/{sshCredentialId}", h.get)
	mux.HandleFunc("PUT /api/v1/ssh-credentials/{sshCredentialId}", h.update)
	mux.HandleFunc("DELETE /api/v1/ssh-credentials/{sshCredentialId}", h.delete)
}

func (h *SSHUserCredentialController) list(w http.ResponseWriter, r *http.Request) {
	creds, err := h.svc.List(r.Context(), r.URL.Query().Get("sshKeyId"))
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusOK, creds)
}

func (h *SSHUserCredentialController) get(w http.ResponseWriter, r *http.Request) {
	cred, err := h.svc.Get(r.Context(), r.PathValue("sshCredentialId"))
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusOK, cred)
}

func (h *SSHUserCredentialController) create(w http.ResponseWriter, r *http.Request) {
	var req dto.SSHUserCredentialRequest
	if err := httpx.Bind(r, &req); err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	cred, err := h.svc.Create(r.Context(), &req)
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusCreated, cred)
}

func (h *SSHUserCredentialController) update(w http.ResponseWriter, r *http.Request) {
	var req dto.SSHUserCredentialRequest
	if err := httpx.Bind(r, &req); err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	cred, err := h.svc.Update(r.Context(), r.PathValue("sshCredentialId"), &req)
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusOK, cred)
}

func (h *SSHUserCredentialController) delete(w http.ResponseWriter, r *http.Request) {
	if err := h.svc.Delete(r.Context(), r.PathValue("sshCredentialId")); err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusNoContent, nil)
}
