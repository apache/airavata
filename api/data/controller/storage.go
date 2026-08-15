package controller

import (
	"net/http"

	"github.com/apache/airavata/internal/httpx"

	dto "github.com/apache/airavata/api/data/dto"
	"github.com/apache/airavata/api/data/service"
)

// SCPDataStorageController serves /api/v1/scp-data-storages.
type SCPDataStorageController struct {
	svc *service.SCPDataStorageService
}

// NewSCPDataStorageController returns a handler delegating to svc.
func NewSCPDataStorageController(svc *service.SCPDataStorageService) *SCPDataStorageController {
	return &SCPDataStorageController{svc: svc}
}

// Register mounts the storage routes.
//
// The literal /me and /shared-with-me patterns take precedence over /{dataStorageId},
// so neither is ever mistaken for a storage id.
func (h *SCPDataStorageController) Register(mux *http.ServeMux) {
	mux.HandleFunc("GET /api/v1/scp-data-storages", h.list)
	mux.HandleFunc("GET /api/v1/scp-data-storages/me", h.listMine)
	mux.HandleFunc("GET /api/v1/scp-data-storages/shared-with-me", h.listSharedWithMe)
	mux.HandleFunc("POST /api/v1/scp-data-storages", h.create)
	mux.HandleFunc("GET /api/v1/scp-data-storages/{dataStorageId}", h.get)
	mux.HandleFunc("PUT /api/v1/scp-data-storages/{dataStorageId}", h.update)
	mux.HandleFunc("DELETE /api/v1/scp-data-storages/{dataStorageId}", h.delete)
}

func (h *SCPDataStorageController) list(w http.ResponseWriter, r *http.Request) {
	storages, err := h.svc.List(r.Context())
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusOK, storages)
}

func (h *SCPDataStorageController) listMine(w http.ResponseWriter, r *http.Request) {
	storages, err := h.svc.ListMine(r.Context())
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusOK, storages)
}

func (h *SCPDataStorageController) listSharedWithMe(w http.ResponseWriter, r *http.Request) {
	storages, err := h.svc.ListSharedWithMe(r.Context())
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusOK, storages)
}

func (h *SCPDataStorageController) get(w http.ResponseWriter, r *http.Request) {
	storage, err := h.svc.Get(r.Context(), r.PathValue("dataStorageId"))
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusOK, storage)
}

func (h *SCPDataStorageController) create(w http.ResponseWriter, r *http.Request) {
	var req dto.SCPDataStorageRequest
	if err := httpx.Bind(r, &req); err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	storage, err := h.svc.Create(r.Context(), &req)
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusCreated, storage)
}

func (h *SCPDataStorageController) update(w http.ResponseWriter, r *http.Request) {
	var req dto.SCPDataStorageRequest
	if err := httpx.Bind(r, &req); err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	storage, err := h.svc.Update(r.Context(), r.PathValue("dataStorageId"), &req)
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusOK, storage)
}

func (h *SCPDataStorageController) delete(w http.ResponseWriter, r *http.Request) {
	if err := h.svc.Delete(r.Context(), r.PathValue("dataStorageId")); err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusNoContent, nil)
}

// SCPDataStorageSharingController serves the share sub-resources of one storage.
type SCPDataStorageSharingController struct {
	svc *service.SCPDataStorageSharingService
}

// NewSCPDataStorageSharingController returns a handler delegating to svc.
func NewSCPDataStorageSharingController(svc *service.SCPDataStorageSharingService) *SCPDataStorageSharingController {
	return &SCPDataStorageSharingController{svc: svc}
}

// Register mounts the storage sharing routes.
func (h *SCPDataStorageSharingController) Register(mux *http.ServeMux) {
	const base = "/api/v1/scp-data-storages/{dataStorageId}"

	mux.HandleFunc("GET "+base+"/group-shares", h.listGroupShares)
	mux.HandleFunc("POST "+base+"/group-shares", h.shareWithGroup)
	mux.HandleFunc("PUT "+base+"/group-shares/{sharingId}", h.updateGroupShare)
	mux.HandleFunc("DELETE "+base+"/group-shares/{sharingId}", h.revokeGroupShare)

	mux.HandleFunc("GET "+base+"/user-shares", h.listUserShares)
	mux.HandleFunc("POST "+base+"/user-shares", h.shareWithUser)
	mux.HandleFunc("PUT "+base+"/user-shares/{sharingId}", h.updateUserShare)
	mux.HandleFunc("DELETE "+base+"/user-shares/{sharingId}", h.revokeUserShare)
}

func (h *SCPDataStorageSharingController) listGroupShares(w http.ResponseWriter, r *http.Request) {
	shares, err := h.svc.ListGroupShares(r.Context(), r.PathValue("dataStorageId"))
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusOK, shares)
}

func (h *SCPDataStorageSharingController) shareWithGroup(w http.ResponseWriter, r *http.Request) {
	var req dto.SCPDataStorageGroupSharingRequest
	if err := httpx.Bind(r, &req); err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	share, err := h.svc.ShareWithGroup(r.Context(), r.PathValue("dataStorageId"), &req)
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusCreated, share)
}

func (h *SCPDataStorageSharingController) updateGroupShare(w http.ResponseWriter, r *http.Request) {
	var req dto.SCPDataStorageSharingUpdate
	if err := httpx.Bind(r, &req); err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	share, err := h.svc.UpdateGroupShare(r.Context(), r.PathValue("dataStorageId"), r.PathValue("sharingId"), &req)
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusOK, share)
}

func (h *SCPDataStorageSharingController) revokeGroupShare(w http.ResponseWriter, r *http.Request) {
	if err := h.svc.RevokeGroupShare(r.Context(), r.PathValue("dataStorageId"), r.PathValue("sharingId")); err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusNoContent, nil)
}

func (h *SCPDataStorageSharingController) listUserShares(w http.ResponseWriter, r *http.Request) {
	shares, err := h.svc.ListUserShares(r.Context(), r.PathValue("dataStorageId"))
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusOK, shares)
}

func (h *SCPDataStorageSharingController) shareWithUser(w http.ResponseWriter, r *http.Request) {
	var req dto.SCPDataStorageUserSharingRequest
	if err := httpx.Bind(r, &req); err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	share, err := h.svc.ShareWithUser(r.Context(), r.PathValue("dataStorageId"), &req)
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusCreated, share)
}

func (h *SCPDataStorageSharingController) updateUserShare(w http.ResponseWriter, r *http.Request) {
	var req dto.SCPDataStorageSharingUpdate
	if err := httpx.Bind(r, &req); err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	share, err := h.svc.UpdateUserShare(r.Context(), r.PathValue("dataStorageId"), r.PathValue("sharingId"), &req)
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusOK, share)
}

func (h *SCPDataStorageSharingController) revokeUserShare(w http.ResponseWriter, r *http.Request) {
	if err := h.svc.RevokeUserShare(r.Context(), r.PathValue("dataStorageId"), r.PathValue("sharingId")); err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusNoContent, nil)
}
