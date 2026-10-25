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

// Package controller serves the share sub-resource of every shareable record.
package controller

import (
	"net/http"

	"github.com/apache/airavata/internal/httpx"

	dto "github.com/apache/airavata/api/sharing/dto"
	"github.com/apache/airavata/api/sharing/service"
)

// Controller serves the share sub-resource of one kind of record.
//
// One handler for every shareable record: the resource it is mounted under is the
// only thing that differs, and that is a constructor argument rather than one
// near-identical controller per vertical. The subject of a share is a field in
// the body, so granting to a user and granting to a group are the same request.
type Controller struct {
	svc        *service.Service
	basePath   string
	resourceID string
}

// New returns a handler mounting svc under basePath.
//
// basePath is the collection the resource lives in, such as "/api/v1/data-products",
// and resourceParam is the path variable naming one of them.
func New(svc *service.Service, basePath, resourceParam string) *Controller {
	return &Controller{svc: svc, basePath: basePath, resourceID: resourceParam}
}

// Register mounts the sharing routes under one resource.
func (h *Controller) Register(mux *http.ServeMux) {
	base := h.basePath + "/{" + h.resourceID + "}/shares"

	mux.HandleFunc("GET "+base, h.list)
	mux.HandleFunc("POST "+base, h.share)
	mux.HandleFunc("PUT "+base+"/{sharingId}", h.update)
	mux.HandleFunc("DELETE "+base+"/{sharingId}", h.revoke)
}

func (h *Controller) list(w http.ResponseWriter, r *http.Request) {
	shares, err := h.svc.List(r.Context(), r.PathValue(h.resourceID))
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusOK, shares)
}

func (h *Controller) share(w http.ResponseWriter, r *http.Request) {
	var req dto.ShareRequest
	if err := httpx.Bind(r, &req); err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	share, err := h.svc.Share(r.Context(), r.PathValue(h.resourceID), &req)
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusCreated, share)
}

func (h *Controller) update(w http.ResponseWriter, r *http.Request) {
	var req dto.ShareUpdate
	if err := httpx.Bind(r, &req); err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	share, err := h.svc.Update(r.Context(), r.PathValue(h.resourceID), r.PathValue("sharingId"), &req)
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusOK, share)
}

func (h *Controller) revoke(w http.ResponseWriter, r *http.Request) {
	if err := h.svc.Revoke(r.Context(), r.PathValue(h.resourceID), r.PathValue("sharingId")); err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusNoContent, nil)
}
