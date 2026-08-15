// Package controller serves the SSH endpoint, cluster and endpoint-credential routes.
package controller

import (
	"net/http"

	"github.com/apache/airavata/internal/httpx"

	dto "github.com/apache/airavata/api/compute/dto"
	"github.com/apache/airavata/api/compute/service"
)

// ClusterController serves /api/v1/clusters.
type ClusterController struct{ svc *service.ClusterService }

// NewClusterController returns a handler delegating to svc.
func NewClusterController(svc *service.ClusterService) *ClusterController {
	return &ClusterController{svc: svc}
}

// Register mounts the cluster routes.
func (h *ClusterController) Register(mux *http.ServeMux) {
	mux.HandleFunc("GET /api/v1/clusters", h.list)
	mux.HandleFunc("POST /api/v1/clusters", h.create)
	mux.HandleFunc("GET /api/v1/clusters/{clusterId}", h.get)
	mux.HandleFunc("PUT /api/v1/clusters/{clusterId}", h.update)
	mux.HandleFunc("DELETE /api/v1/clusters/{clusterId}", h.delete)
}

func (h *ClusterController) list(w http.ResponseWriter, r *http.Request) {
	clusters, err := h.svc.List(r.Context())
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusOK, clusters)
}

func (h *ClusterController) get(w http.ResponseWriter, r *http.Request) {
	cluster, err := h.svc.Get(r.Context(), r.PathValue("clusterId"))
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusOK, cluster)
}

func (h *ClusterController) create(w http.ResponseWriter, r *http.Request) {
	var req dto.ClusterRequest
	if err := httpx.Bind(r, &req); err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	cluster, err := h.svc.Create(r.Context(), &req)
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusCreated, cluster)
}

func (h *ClusterController) update(w http.ResponseWriter, r *http.Request) {
	var req dto.ClusterRequest
	if err := httpx.Bind(r, &req); err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	cluster, err := h.svc.Update(r.Context(), r.PathValue("clusterId"), &req)
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusOK, cluster)
}

func (h *ClusterController) delete(w http.ResponseWriter, r *http.Request) {
	if err := h.svc.Delete(r.Context(), r.PathValue("clusterId")); err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusNoContent, nil)
}

// ClusterPartitionController serves /api/v1/clusters/{clusterId}/partitions.
type ClusterPartitionController struct {
	svc *service.ClusterPartitionService
}

// NewClusterPartitionController returns a handler delegating to svc.
func NewClusterPartitionController(svc *service.ClusterPartitionService) *ClusterPartitionController {
	return &ClusterPartitionController{svc: svc}
}

// Register mounts the partition routes as a sub-resource of a cluster.
func (h *ClusterPartitionController) Register(mux *http.ServeMux) {
	mux.HandleFunc("GET /api/v1/clusters/{clusterId}/partitions", h.list)
	mux.HandleFunc("POST /api/v1/clusters/{clusterId}/partitions", h.create)
	mux.HandleFunc("GET /api/v1/clusters/{clusterId}/partitions/{partitionId}", h.get)
	mux.HandleFunc("PUT /api/v1/clusters/{clusterId}/partitions/{partitionId}", h.update)
	mux.HandleFunc("DELETE /api/v1/clusters/{clusterId}/partitions/{partitionId}", h.delete)
}

func (h *ClusterPartitionController) list(w http.ResponseWriter, r *http.Request) {
	partitions, err := h.svc.List(r.Context(), r.PathValue("clusterId"))
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusOK, partitions)
}

func (h *ClusterPartitionController) get(w http.ResponseWriter, r *http.Request) {
	partition, err := h.svc.Get(r.Context(), r.PathValue("clusterId"), r.PathValue("partitionId"))
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusOK, partition)
}

func (h *ClusterPartitionController) create(w http.ResponseWriter, r *http.Request) {
	var req dto.ClusterPartitionRequest
	if err := httpx.Bind(r, &req); err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	partition, err := h.svc.Create(r.Context(), r.PathValue("clusterId"), &req)
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusCreated, partition)
}

func (h *ClusterPartitionController) update(w http.ResponseWriter, r *http.Request) {
	var req dto.ClusterPartitionRequest
	if err := httpx.Bind(r, &req); err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	partition, err := h.svc.Update(r.Context(), r.PathValue("clusterId"), r.PathValue("partitionId"), &req)
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusOK, partition)
}

func (h *ClusterPartitionController) delete(w http.ResponseWriter, r *http.Request) {
	err := h.svc.Delete(r.Context(), r.PathValue("clusterId"), r.PathValue("partitionId"))
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusNoContent, nil)
}
