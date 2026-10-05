/**
*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
 */

package controller

import (
	"net/http"

	"github.com/apache/airavata/internal/httpx"

	dto "github.com/apache/airavata/api/data/dto"
	"github.com/apache/airavata/api/data/service"
)

// VirtualDataDirectoryController serves /api/v1/virtual-data-directories.
type VirtualDataDirectoryController struct {
	svc *service.VirtualDataDirectoryService
}

// NewVirtualDataDirectoryController returns a handler delegating to svc.
func NewVirtualDataDirectoryController(svc *service.VirtualDataDirectoryService) *VirtualDataDirectoryController {
	return &VirtualDataDirectoryController{svc: svc}
}

// Register mounts the virtual data directory routes.
//
// The literal /me and /shared-with-me patterns take precedence over
// /{virtualDataDirectoryId}, so neither is ever mistaken for a directory id.
func (h *VirtualDataDirectoryController) Register(mux *http.ServeMux) {
	const base = "/api/v1/virtual-data-directories"

	mux.HandleFunc("GET "+base, h.list)
	mux.HandleFunc("GET "+base+"/me", h.listMine)
	mux.HandleFunc("GET "+base+"/shared-with-me", h.listSharedWithMe)
	mux.HandleFunc("POST "+base, h.create)
	mux.HandleFunc("GET "+base+"/{virtualDataDirectoryId}", h.get)
	mux.HandleFunc("GET "+base+"/{virtualDataDirectoryId}/contents", h.contents)
	mux.HandleFunc("PUT "+base+"/{virtualDataDirectoryId}", h.update)
	mux.HandleFunc("DELETE "+base+"/{virtualDataDirectoryId}", h.delete)
}

func (h *VirtualDataDirectoryController) list(w http.ResponseWriter, r *http.Request) {
	dirs, err := h.svc.List(r.Context())
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusOK, dirs)
}

func (h *VirtualDataDirectoryController) listMine(w http.ResponseWriter, r *http.Request) {
	dirs, err := h.svc.ListMine(r.Context())
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusOK, dirs)
}

func (h *VirtualDataDirectoryController) listSharedWithMe(w http.ResponseWriter, r *http.Request) {
	dirs, err := h.svc.ListSharedWithMe(r.Context())
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusOK, dirs)
}

func (h *VirtualDataDirectoryController) get(w http.ResponseWriter, r *http.Request) {
	dir, err := h.svc.Get(r.Context(), r.PathValue("virtualDataDirectoryId"))
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusOK, dir)
}

func (h *VirtualDataDirectoryController) contents(w http.ResponseWriter, r *http.Request) {
	dir, err := h.svc.GetContents(r.Context(), r.PathValue("virtualDataDirectoryId"))
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusOK, dir)
}

func (h *VirtualDataDirectoryController) create(w http.ResponseWriter, r *http.Request) {
	var req dto.VirtualDataDirectoryRequest
	if err := httpx.Bind(r, &req); err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	dir, err := h.svc.Create(r.Context(), &req)
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusCreated, dir)
}

func (h *VirtualDataDirectoryController) update(w http.ResponseWriter, r *http.Request) {
	var req dto.VirtualDataDirectoryRequest
	if err := httpx.Bind(r, &req); err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	dir, err := h.svc.Update(r.Context(), r.PathValue("virtualDataDirectoryId"), &req)
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusOK, dir)
}

func (h *VirtualDataDirectoryController) delete(w http.ResponseWriter, r *http.Request) {
	if err := h.svc.Delete(r.Context(), r.PathValue("virtualDataDirectoryId")); err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusNoContent, nil)
}

// VirtualDataFileController serves /api/v1/virtual-data-files.
//
// Files are addressed at the top level rather than under their directory: a file moves
// between directories, so nesting its id under one would change its URL whenever it
// was moved. Listing them is the exception, since "what is in this directory" is a
// question about the directory.
type VirtualDataFileController struct {
	svc *service.VirtualDataFileService
}

// NewVirtualDataFileController returns a handler delegating to svc.
func NewVirtualDataFileController(svc *service.VirtualDataFileService) *VirtualDataFileController {
	return &VirtualDataFileController{svc: svc}
}

// Register mounts the virtual data file routes.
func (h *VirtualDataFileController) Register(mux *http.ServeMux) {
	const base = "/api/v1/virtual-data-files"

	mux.HandleFunc("POST "+base, h.create)
	mux.HandleFunc("GET "+base+"/{virtualDataFileId}", h.get)
	mux.HandleFunc("PUT "+base+"/{virtualDataFileId}", h.update)
	mux.HandleFunc("DELETE "+base+"/{virtualDataFileId}", h.delete)

	mux.HandleFunc("GET /api/v1/virtual-data-directories/{virtualDataDirectoryId}/files", h.listByDirectory)
}

func (h *VirtualDataFileController) listByDirectory(w http.ResponseWriter, r *http.Request) {
	files, err := h.svc.ListByDirectory(r.Context(), r.PathValue("virtualDataDirectoryId"))
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusOK, files)
}

func (h *VirtualDataFileController) get(w http.ResponseWriter, r *http.Request) {
	file, err := h.svc.Get(r.Context(), r.PathValue("virtualDataFileId"))
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusOK, file)
}

func (h *VirtualDataFileController) create(w http.ResponseWriter, r *http.Request) {
	var req dto.VirtualDataFileRequest
	if err := httpx.Bind(r, &req); err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	file, err := h.svc.Create(r.Context(), &req)
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusCreated, file)
}

func (h *VirtualDataFileController) update(w http.ResponseWriter, r *http.Request) {
	var req dto.VirtualDataFileRequest
	if err := httpx.Bind(r, &req); err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	file, err := h.svc.Update(r.Context(), r.PathValue("virtualDataFileId"), &req)
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusOK, file)
}

func (h *VirtualDataFileController) delete(w http.ResponseWriter, r *http.Request) {
	if err := h.svc.Delete(r.Context(), r.PathValue("virtualDataFileId")); err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusNoContent, nil)
}

// VirtualDataDirectorySharingController serves the share sub-resources of one
// directory.
type VirtualDataDirectorySharingController struct {
	svc *service.VirtualDataDirectorySharingService
}

// NewVirtualDataDirectorySharingController returns a handler delegating to svc.
func NewVirtualDataDirectorySharingController(svc *service.VirtualDataDirectorySharingService) *VirtualDataDirectorySharingController {
	return &VirtualDataDirectorySharingController{svc: svc}
}

// Register mounts the directory sharing routes.
func (h *VirtualDataDirectorySharingController) Register(mux *http.ServeMux) {
	const base = "/api/v1/virtual-data-directories/{virtualDataDirectoryId}"

	mux.HandleFunc("GET "+base+"/group-shares", h.listGroupShares)
	mux.HandleFunc("POST "+base+"/group-shares", h.shareWithGroup)
	mux.HandleFunc("PUT "+base+"/group-shares/{sharingId}", h.updateGroupShare)
	mux.HandleFunc("DELETE "+base+"/group-shares/{sharingId}", h.revokeGroupShare)

	mux.HandleFunc("GET "+base+"/user-shares", h.listUserShares)
	mux.HandleFunc("POST "+base+"/user-shares", h.shareWithUser)
	mux.HandleFunc("PUT "+base+"/user-shares/{sharingId}", h.updateUserShare)
	mux.HandleFunc("DELETE "+base+"/user-shares/{sharingId}", h.revokeUserShare)
}

func (h *VirtualDataDirectorySharingController) listGroupShares(w http.ResponseWriter, r *http.Request) {
	shares, err := h.svc.ListGroupShares(r.Context(), r.PathValue("virtualDataDirectoryId"))
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusOK, shares)
}

func (h *VirtualDataDirectorySharingController) shareWithGroup(w http.ResponseWriter, r *http.Request) {
	var req dto.VirtualDataDirectoryGroupSharingRequest
	if err := httpx.Bind(r, &req); err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	share, err := h.svc.ShareWithGroup(r.Context(), r.PathValue("virtualDataDirectoryId"), &req)
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusCreated, share)
}

func (h *VirtualDataDirectorySharingController) updateGroupShare(w http.ResponseWriter, r *http.Request) {
	var req dto.DataProductSharingUpdate
	if err := httpx.Bind(r, &req); err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	share, err := h.svc.UpdateGroupShare(r.Context(), r.PathValue("virtualDataDirectoryId"), r.PathValue("sharingId"), &req)
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusOK, share)
}

func (h *VirtualDataDirectorySharingController) revokeGroupShare(w http.ResponseWriter, r *http.Request) {
	if err := h.svc.RevokeGroupShare(r.Context(), r.PathValue("virtualDataDirectoryId"), r.PathValue("sharingId")); err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusNoContent, nil)
}

func (h *VirtualDataDirectorySharingController) listUserShares(w http.ResponseWriter, r *http.Request) {
	shares, err := h.svc.ListUserShares(r.Context(), r.PathValue("virtualDataDirectoryId"))
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusOK, shares)
}

func (h *VirtualDataDirectorySharingController) shareWithUser(w http.ResponseWriter, r *http.Request) {
	var req dto.VirtualDataDirectoryUserSharingRequest
	if err := httpx.Bind(r, &req); err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	share, err := h.svc.ShareWithUser(r.Context(), r.PathValue("virtualDataDirectoryId"), &req)
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusCreated, share)
}

func (h *VirtualDataDirectorySharingController) updateUserShare(w http.ResponseWriter, r *http.Request) {
	var req dto.DataProductSharingUpdate
	if err := httpx.Bind(r, &req); err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	share, err := h.svc.UpdateUserShare(r.Context(), r.PathValue("virtualDataDirectoryId"), r.PathValue("sharingId"), &req)
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusOK, share)
}

func (h *VirtualDataDirectorySharingController) revokeUserShare(w http.ResponseWriter, r *http.Request) {
	if err := h.svc.RevokeUserShare(r.Context(), r.PathValue("virtualDataDirectoryId"), r.PathValue("sharingId")); err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusNoContent, nil)
}
