package compute

import (
	"net/http"

	"github.com/apache/airavata/internal/httpx"

	dto "github.com/apache/airavata/api/compute/dto"
)

// SSHEndpointCredentialController serves /api/v1/ssh-endpoint-credentials.
type SSHEndpointCredentialController struct{ svc *SSHEndpointCredentialService }

// NewSSHEndpointCredentialController returns a handler delegating to svc.
func NewSSHEndpointCredentialController(svc *SSHEndpointCredentialService) *SSHEndpointCredentialController {
	return &SSHEndpointCredentialController{svc: svc}
}

// Register mounts the endpoint credential routes.
//
// The literal /me and /shared-with-me patterns take precedence over /{id}, so neither
// is ever mistaken for a binding id.
func (h *SSHEndpointCredentialController) Register(mux *http.ServeMux) {
	mux.HandleFunc("GET /api/v1/ssh-endpoint-credentials", h.list)
	mux.HandleFunc("GET /api/v1/ssh-endpoint-credentials/me", h.listMine)
	mux.HandleFunc("GET /api/v1/ssh-endpoint-credentials/shared-with-me", h.listSharedWithMe)
	mux.HandleFunc("POST /api/v1/ssh-endpoint-credentials", h.create)
	mux.HandleFunc("GET /api/v1/ssh-endpoint-credentials/{id}", h.get)
	mux.HandleFunc("PUT /api/v1/ssh-endpoint-credentials/{id}", h.update)
	mux.HandleFunc("DELETE /api/v1/ssh-endpoint-credentials/{id}", h.delete)
}

func (h *SSHEndpointCredentialController) list(w http.ResponseWriter, r *http.Request) {
	found, err := h.svc.List(r.Context(), r.URL.Query().Get("sshEndpointId"))
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusOK, found)
}

func (h *SSHEndpointCredentialController) listMine(w http.ResponseWriter, r *http.Request) {
	found, err := h.svc.ListMine(r.Context(), r.URL.Query().Get("sshEndpointId"))
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusOK, found)
}

func (h *SSHEndpointCredentialController) listSharedWithMe(w http.ResponseWriter, r *http.Request) {
	found, err := h.svc.ListSharedWithMe(r.Context())
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusOK, found)
}

func (h *SSHEndpointCredentialController) get(w http.ResponseWriter, r *http.Request) {
	binding, err := h.svc.Get(r.Context(), r.PathValue("id"))
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusOK, binding)
}

func (h *SSHEndpointCredentialController) create(w http.ResponseWriter, r *http.Request) {
	var req dto.SSHEndpointCredentialRequest
	if err := httpx.Bind(r, &req); err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	binding, err := h.svc.Create(r.Context(), &req)
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusCreated, binding)
}

func (h *SSHEndpointCredentialController) update(w http.ResponseWriter, r *http.Request) {
	var req dto.SSHEndpointCredentialRequest
	if err := httpx.Bind(r, &req); err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	binding, err := h.svc.Update(r.Context(), r.PathValue("id"), &req)
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusOK, binding)
}

func (h *SSHEndpointCredentialController) delete(w http.ResponseWriter, r *http.Request) {
	if err := h.svc.Delete(r.Context(), r.PathValue("id")); err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusNoContent, nil)
}

// SSHEndpointCredentialSharingController serves the share sub-resources of one
// binding.
type SSHEndpointCredentialSharingController struct {
	svc *SSHEndpointCredentialSharingService
}

// NewSSHEndpointCredentialSharingController returns a handler delegating to svc.
func NewSSHEndpointCredentialSharingController(svc *SSHEndpointCredentialSharingService) *SSHEndpointCredentialSharingController {
	return &SSHEndpointCredentialSharingController{svc: svc}
}

// Register mounts the sharing routes.
//
// Group and user shares are separate collections rather than one with a type field:
// they name different subjects, and collapsing them would make every request state
// which kind it meant anyway.
func (h *SSHEndpointCredentialSharingController) Register(mux *http.ServeMux) {
	const base = "/api/v1/ssh-endpoint-credentials/{credentialId}"

	mux.HandleFunc("GET "+base+"/group-shares", h.listGroupShares)
	mux.HandleFunc("POST "+base+"/group-shares", h.shareWithGroup)
	mux.HandleFunc("PUT "+base+"/group-shares/{sharingId}", h.updateGroupShare)
	mux.HandleFunc("DELETE "+base+"/group-shares/{sharingId}", h.revokeGroupShare)

	mux.HandleFunc("GET "+base+"/user-shares", h.listUserShares)
	mux.HandleFunc("POST "+base+"/user-shares", h.shareWithUser)
	mux.HandleFunc("PUT "+base+"/user-shares/{sharingId}", h.updateUserShare)
	mux.HandleFunc("DELETE "+base+"/user-shares/{sharingId}", h.revokeUserShare)
}

func (h *SSHEndpointCredentialSharingController) listGroupShares(w http.ResponseWriter, r *http.Request) {
	shares, err := h.svc.ListGroupShares(r.Context(), r.PathValue("credentialId"))
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusOK, shares)
}

func (h *SSHEndpointCredentialSharingController) shareWithGroup(w http.ResponseWriter, r *http.Request) {
	var req dto.SSHEndpointCredentialGroupSharingRequest
	if err := httpx.Bind(r, &req); err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	share, err := h.svc.ShareWithGroup(r.Context(), r.PathValue("credentialId"), &req)
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusCreated, share)
}

func (h *SSHEndpointCredentialSharingController) updateGroupShare(w http.ResponseWriter, r *http.Request) {
	var req dto.SharingUpdate
	if err := httpx.Bind(r, &req); err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	share, err := h.svc.UpdateGroupShare(r.Context(), r.PathValue("credentialId"), r.PathValue("sharingId"), &req)
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusOK, share)
}

func (h *SSHEndpointCredentialSharingController) revokeGroupShare(w http.ResponseWriter, r *http.Request) {
	if err := h.svc.RevokeGroupShare(r.Context(), r.PathValue("credentialId"), r.PathValue("sharingId")); err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusNoContent, nil)
}

func (h *SSHEndpointCredentialSharingController) listUserShares(w http.ResponseWriter, r *http.Request) {
	shares, err := h.svc.ListUserShares(r.Context(), r.PathValue("credentialId"))
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusOK, shares)
}

func (h *SSHEndpointCredentialSharingController) shareWithUser(w http.ResponseWriter, r *http.Request) {
	var req dto.SSHEndpointCredentialUserSharingRequest
	if err := httpx.Bind(r, &req); err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	share, err := h.svc.ShareWithUser(r.Context(), r.PathValue("credentialId"), &req)
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusCreated, share)
}

func (h *SSHEndpointCredentialSharingController) updateUserShare(w http.ResponseWriter, r *http.Request) {
	var req dto.SharingUpdate
	if err := httpx.Bind(r, &req); err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	share, err := h.svc.UpdateUserShare(r.Context(), r.PathValue("credentialId"), r.PathValue("sharingId"), &req)
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusOK, share)
}

func (h *SSHEndpointCredentialSharingController) revokeUserShare(w http.ResponseWriter, r *http.Request) {
	if err := h.svc.RevokeUserShare(r.Context(), r.PathValue("credentialId"), r.PathValue("sharingId")); err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusNoContent, nil)
}
