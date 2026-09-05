package controller

import (
	"net/http"

	"github.com/apache/airavata/internal/httpx"

	dto "github.com/apache/airavata/api/compute/dto"
	"github.com/apache/airavata/api/compute/service"
)

// SlurmClusterConfigController serves /api/v1/slurm-cluster-configs.
type SlurmClusterConfigController struct {
	svc *service.SlurmClusterConfigService
}

// NewSlurmClusterConfigController returns a handler delegating to svc.
func NewSlurmClusterConfigController(svc *service.SlurmClusterConfigService) *SlurmClusterConfigController {
	return &SlurmClusterConfigController{svc: svc}
}

// Register mounts the cluster config routes.
//
// The literal /me and /shared-with-me patterns take precedence over
// /{slurmClusterConfigId}, so neither is ever mistaken for a config id.
func (h *SlurmClusterConfigController) Register(mux *http.ServeMux) {
	mux.HandleFunc("GET /api/v1/slurm-cluster-configs", h.list)
	mux.HandleFunc("GET /api/v1/slurm-cluster-configs/me", h.listMine)
	mux.HandleFunc("GET /api/v1/slurm-cluster-configs/shared-with-me", h.listSharedWithMe)
	mux.HandleFunc("POST /api/v1/slurm-cluster-configs", h.create)
	mux.HandleFunc("GET /api/v1/slurm-cluster-configs/{slurmClusterConfigId}", h.get)
	mux.HandleFunc("PUT /api/v1/slurm-cluster-configs/{slurmClusterConfigId}", h.update)
	mux.HandleFunc("DELETE /api/v1/slurm-cluster-configs/{slurmClusterConfigId}", h.delete)
}

func (h *SlurmClusterConfigController) list(w http.ResponseWriter, r *http.Request) {
	configs, err := h.svc.List(r.Context())
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusOK, configs)
}

func (h *SlurmClusterConfigController) listMine(w http.ResponseWriter, r *http.Request) {
	configs, err := h.svc.ListMine(r.Context())
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusOK, configs)
}

func (h *SlurmClusterConfigController) listSharedWithMe(w http.ResponseWriter, r *http.Request) {
	configs, err := h.svc.ListSharedWithMe(r.Context())
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusOK, configs)
}

func (h *SlurmClusterConfigController) get(w http.ResponseWriter, r *http.Request) {
	config, err := h.svc.Get(r.Context(), r.PathValue("slurmClusterConfigId"))
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusOK, config)
}

func (h *SlurmClusterConfigController) create(w http.ResponseWriter, r *http.Request) {
	var req dto.SlurmClusterConfigRequest
	if err := httpx.Bind(r, &req); err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	config, err := h.svc.Create(r.Context(), &req)
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusCreated, config)
}

func (h *SlurmClusterConfigController) update(w http.ResponseWriter, r *http.Request) {
	var req dto.SlurmClusterConfigRequest
	if err := httpx.Bind(r, &req); err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	config, err := h.svc.Update(r.Context(), r.PathValue("slurmClusterConfigId"), &req)
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusOK, config)
}

func (h *SlurmClusterConfigController) delete(w http.ResponseWriter, r *http.Request) {
	if err := h.svc.Delete(r.Context(), r.PathValue("slurmClusterConfigId")); err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusNoContent, nil)
}

// SlurmClusterConfigSharingController serves the share sub-resources of one config.
type SlurmClusterConfigSharingController struct {
	svc *service.SlurmClusterConfigSharingService
}

// NewSlurmClusterConfigSharingController returns a handler delegating to svc.
func NewSlurmClusterConfigSharingController(svc *service.SlurmClusterConfigSharingService) *SlurmClusterConfigSharingController {
	return &SlurmClusterConfigSharingController{svc: svc}
}

// Register mounts the cluster config sharing routes.
func (h *SlurmClusterConfigSharingController) Register(mux *http.ServeMux) {
	const base = "/api/v1/slurm-cluster-configs/{slurmClusterConfigId}"

	mux.HandleFunc("GET "+base+"/group-shares", h.listGroupShares)
	mux.HandleFunc("POST "+base+"/group-shares", h.shareWithGroup)
	mux.HandleFunc("PUT "+base+"/group-shares/{sharingId}", h.updateGroupShare)
	mux.HandleFunc("DELETE "+base+"/group-shares/{sharingId}", h.revokeGroupShare)

	mux.HandleFunc("GET "+base+"/user-shares", h.listUserShares)
	mux.HandleFunc("POST "+base+"/user-shares", h.shareWithUser)
	mux.HandleFunc("PUT "+base+"/user-shares/{sharingId}", h.updateUserShare)
	mux.HandleFunc("DELETE "+base+"/user-shares/{sharingId}", h.revokeUserShare)
}

func (h *SlurmClusterConfigSharingController) listGroupShares(w http.ResponseWriter, r *http.Request) {
	shares, err := h.svc.ListGroupShares(r.Context(), r.PathValue("slurmClusterConfigId"))
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusOK, shares)
}

func (h *SlurmClusterConfigSharingController) shareWithGroup(w http.ResponseWriter, r *http.Request) {
	var req dto.SlurmClusterConfigGroupSharingRequest
	if err := httpx.Bind(r, &req); err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	share, err := h.svc.ShareWithGroup(r.Context(), r.PathValue("slurmClusterConfigId"), &req)
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusCreated, share)
}

func (h *SlurmClusterConfigSharingController) updateGroupShare(w http.ResponseWriter, r *http.Request) {
	var req dto.SlurmClusterConfigSharingUpdate
	if err := httpx.Bind(r, &req); err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	share, err := h.svc.UpdateGroupShare(r.Context(), r.PathValue("slurmClusterConfigId"), r.PathValue("sharingId"), &req)
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusOK, share)
}

func (h *SlurmClusterConfigSharingController) revokeGroupShare(w http.ResponseWriter, r *http.Request) {
	if err := h.svc.RevokeGroupShare(r.Context(), r.PathValue("slurmClusterConfigId"), r.PathValue("sharingId")); err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusNoContent, nil)
}

func (h *SlurmClusterConfigSharingController) listUserShares(w http.ResponseWriter, r *http.Request) {
	shares, err := h.svc.ListUserShares(r.Context(), r.PathValue("slurmClusterConfigId"))
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusOK, shares)
}

func (h *SlurmClusterConfigSharingController) shareWithUser(w http.ResponseWriter, r *http.Request) {
	var req dto.SlurmClusterConfigUserSharingRequest
	if err := httpx.Bind(r, &req); err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	share, err := h.svc.ShareWithUser(r.Context(), r.PathValue("slurmClusterConfigId"), &req)
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusCreated, share)
}

func (h *SlurmClusterConfigSharingController) updateUserShare(w http.ResponseWriter, r *http.Request) {
	var req dto.SlurmClusterConfigSharingUpdate
	if err := httpx.Bind(r, &req); err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	share, err := h.svc.UpdateUserShare(r.Context(), r.PathValue("slurmClusterConfigId"), r.PathValue("sharingId"), &req)
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusOK, share)
}

func (h *SlurmClusterConfigSharingController) revokeUserShare(w http.ResponseWriter, r *http.Request) {
	if err := h.svc.RevokeUserShare(r.Context(), r.PathValue("slurmClusterConfigId"), r.PathValue("sharingId")); err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusNoContent, nil)
}
