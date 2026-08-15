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

// DataProductSharingController serves the share sub-resources of one product.
type DataProductSharingController struct {
	svc *service.DataProductSharingService
}

// NewDataProductSharingController returns a handler delegating to svc.
func NewDataProductSharingController(svc *service.DataProductSharingService) *DataProductSharingController {
	return &DataProductSharingController{svc: svc}
}

// Register mounts the product sharing routes.
func (h *DataProductSharingController) Register(mux *http.ServeMux) {
	const base = "/api/v1/data-products/{dataProductId}"

	mux.HandleFunc("GET "+base+"/group-shares", h.listGroupShares)
	mux.HandleFunc("POST "+base+"/group-shares", h.shareWithGroup)
	mux.HandleFunc("PUT "+base+"/group-shares/{sharingId}", h.updateGroupShare)
	mux.HandleFunc("DELETE "+base+"/group-shares/{sharingId}", h.revokeGroupShare)

	mux.HandleFunc("GET "+base+"/user-shares", h.listUserShares)
	mux.HandleFunc("POST "+base+"/user-shares", h.shareWithUser)
	mux.HandleFunc("PUT "+base+"/user-shares/{sharingId}", h.updateUserShare)
	mux.HandleFunc("DELETE "+base+"/user-shares/{sharingId}", h.revokeUserShare)
}

func (h *DataProductSharingController) listGroupShares(w http.ResponseWriter, r *http.Request) {
	shares, err := h.svc.ListGroupShares(r.Context(), r.PathValue("dataProductId"))
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusOK, shares)
}

func (h *DataProductSharingController) shareWithGroup(w http.ResponseWriter, r *http.Request) {
	var req dto.DataProductGroupSharingRequest
	if err := httpx.Bind(r, &req); err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	share, err := h.svc.ShareWithGroup(r.Context(), r.PathValue("dataProductId"), &req)
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusCreated, share)
}

func (h *DataProductSharingController) updateGroupShare(w http.ResponseWriter, r *http.Request) {
	var req dto.DataProductSharingUpdate
	if err := httpx.Bind(r, &req); err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	share, err := h.svc.UpdateGroupShare(r.Context(), r.PathValue("dataProductId"), r.PathValue("sharingId"), &req)
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusOK, share)
}

func (h *DataProductSharingController) revokeGroupShare(w http.ResponseWriter, r *http.Request) {
	if err := h.svc.RevokeGroupShare(r.Context(), r.PathValue("dataProductId"), r.PathValue("sharingId")); err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusNoContent, nil)
}

func (h *DataProductSharingController) listUserShares(w http.ResponseWriter, r *http.Request) {
	shares, err := h.svc.ListUserShares(r.Context(), r.PathValue("dataProductId"))
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusOK, shares)
}

func (h *DataProductSharingController) shareWithUser(w http.ResponseWriter, r *http.Request) {
	var req dto.DataProductUserSharingRequest
	if err := httpx.Bind(r, &req); err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	share, err := h.svc.ShareWithUser(r.Context(), r.PathValue("dataProductId"), &req)
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusCreated, share)
}

func (h *DataProductSharingController) updateUserShare(w http.ResponseWriter, r *http.Request) {
	var req dto.DataProductSharingUpdate
	if err := httpx.Bind(r, &req); err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	share, err := h.svc.UpdateUserShare(r.Context(), r.PathValue("dataProductId"), r.PathValue("sharingId"), &req)
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusOK, share)
}

func (h *DataProductSharingController) revokeUserShare(w http.ResponseWriter, r *http.Request) {
	if err := h.svc.RevokeUserShare(r.Context(), r.PathValue("dataProductId"), r.PathValue("sharingId")); err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusNoContent, nil)
}
