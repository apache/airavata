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

// Package controller serves the data product and SCP data storage routes.
package controller

import (
	"net/http"

	"github.com/apache/airavata/internal/httpx"

	dto "github.com/apache/airavata/api/data/dto"
	"github.com/apache/airavata/api/data/service"
)

// DataProductController serves /api/v1/data-products.
type DataProductController struct{ svc *service.DataProductService }

// NewDataProductController returns a handler delegating to svc.
func NewDataProductController(svc *service.DataProductService) *DataProductController {
	return &DataProductController{svc: svc}
}

// Register mounts the data product routes.
//
// The literal /me and /shared-with-me patterns take precedence over /{dataProductId},
// so neither is ever mistaken for a product id.
func (h *DataProductController) Register(mux *http.ServeMux) {
	mux.HandleFunc("GET /api/v1/data-products", h.list)
	mux.HandleFunc("GET /api/v1/data-products/me", h.listMine)
	mux.HandleFunc("GET /api/v1/data-products/shared-with-me", h.listSharedWithMe)
	mux.HandleFunc("POST /api/v1/data-products", h.create)
	mux.HandleFunc("GET /api/v1/data-products/{dataProductId}", h.get)
	mux.HandleFunc("PUT /api/v1/data-products/{dataProductId}", h.update)
	mux.HandleFunc("DELETE /api/v1/data-products/{dataProductId}", h.delete)
}

func (h *DataProductController) list(w http.ResponseWriter, r *http.Request) {
	products, err := h.svc.List(r.Context())
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusOK, products)
}

func (h *DataProductController) listMine(w http.ResponseWriter, r *http.Request) {
	products, err := h.svc.ListMine(r.Context())
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusOK, products)
}

func (h *DataProductController) listSharedWithMe(w http.ResponseWriter, r *http.Request) {
	products, err := h.svc.ListSharedWithMe(r.Context())
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusOK, products)
}

func (h *DataProductController) get(w http.ResponseWriter, r *http.Request) {
	product, err := h.svc.Get(r.Context(), r.PathValue("dataProductId"))
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusOK, product)
}

func (h *DataProductController) create(w http.ResponseWriter, r *http.Request) {
	var req dto.DataProductRequest
	if err := httpx.Bind(r, &req); err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	product, err := h.svc.Create(r.Context(), &req)
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusCreated, product)
}

func (h *DataProductController) update(w http.ResponseWriter, r *http.Request) {
	var req dto.DataProductRequest
	if err := httpx.Bind(r, &req); err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	product, err := h.svc.Update(r.Context(), r.PathValue("dataProductId"), &req)
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusOK, product)
}

func (h *DataProductController) delete(w http.ResponseWriter, r *http.Request) {
	if err := h.svc.Delete(r.Context(), r.PathValue("dataProductId")); err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusNoContent, nil)
}
