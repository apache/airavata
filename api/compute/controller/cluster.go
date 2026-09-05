// Package controller serves the Slurm cluster, partition and cluster-config routes.
package controller

import (
	"net/http"

	"github.com/apache/airavata/internal/httpx"

	dto "github.com/apache/airavata/api/compute/dto"
	"github.com/apache/airavata/api/compute/service"
)

// SlurmClusterController serves /api/v1/slurm-clusters.
type SlurmClusterController struct{ svc *service.SlurmClusterService }

// NewSlurmClusterController returns a handler delegating to svc.
func NewSlurmClusterController(svc *service.SlurmClusterService) *SlurmClusterController {
	return &SlurmClusterController{svc: svc}
}

// Register mounts the cluster routes.
func (h *SlurmClusterController) Register(mux *http.ServeMux) {
	mux.HandleFunc("GET /api/v1/slurm-clusters", h.list)
	mux.HandleFunc("POST /api/v1/slurm-clusters", h.create)
	mux.HandleFunc("GET /api/v1/slurm-clusters/{slurmClusterId}", h.get)
	mux.HandleFunc("PUT /api/v1/slurm-clusters/{slurmClusterId}", h.update)
	mux.HandleFunc("DELETE /api/v1/slurm-clusters/{slurmClusterId}", h.delete)
}

func (h *SlurmClusterController) list(w http.ResponseWriter, r *http.Request) {
	clusters, err := h.svc.List(r.Context())
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusOK, clusters)
}

func (h *SlurmClusterController) get(w http.ResponseWriter, r *http.Request) {
	cluster, err := h.svc.Get(r.Context(), r.PathValue("slurmClusterId"))
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusOK, cluster)
}

func (h *SlurmClusterController) create(w http.ResponseWriter, r *http.Request) {
	var req dto.SlurmClusterRequest
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

func (h *SlurmClusterController) update(w http.ResponseWriter, r *http.Request) {
	var req dto.SlurmClusterRequest
	if err := httpx.Bind(r, &req); err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	cluster, err := h.svc.Update(r.Context(), r.PathValue("slurmClusterId"), &req)
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusOK, cluster)
}

func (h *SlurmClusterController) delete(w http.ResponseWriter, r *http.Request) {
	if err := h.svc.Delete(r.Context(), r.PathValue("slurmClusterId")); err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusNoContent, nil)
}

// ClusterPartitionController serves /api/v1/slurm-clusters/{slurmClusterId}/partitions.
type ClusterPartitionController struct {
	svc *service.ClusterPartitionService
}

// NewClusterPartitionController returns a handler delegating to svc.
func NewClusterPartitionController(svc *service.ClusterPartitionService) *ClusterPartitionController {
	return &ClusterPartitionController{svc: svc}
}

// Register mounts the partition routes as a sub-resource of a cluster.
func (h *ClusterPartitionController) Register(mux *http.ServeMux) {
	mux.HandleFunc("GET /api/v1/slurm-clusters/{slurmClusterId}/partitions", h.list)
	mux.HandleFunc("POST /api/v1/slurm-clusters/{slurmClusterId}/partitions", h.create)
	mux.HandleFunc("GET /api/v1/slurm-clusters/{slurmClusterId}/partitions/{partitionId}", h.get)
	mux.HandleFunc("PUT /api/v1/slurm-clusters/{slurmClusterId}/partitions/{partitionId}", h.update)
	mux.HandleFunc("DELETE /api/v1/slurm-clusters/{slurmClusterId}/partitions/{partitionId}", h.delete)
}

func (h *ClusterPartitionController) list(w http.ResponseWriter, r *http.Request) {
	partitions, err := h.svc.List(r.Context(), r.PathValue("slurmClusterId"))
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusOK, partitions)
}

func (h *ClusterPartitionController) get(w http.ResponseWriter, r *http.Request) {
	partition, err := h.svc.Get(r.Context(), r.PathValue("slurmClusterId"), r.PathValue("partitionId"))
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
	partition, err := h.svc.Create(r.Context(), r.PathValue("slurmClusterId"), &req)
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
	partition, err := h.svc.Update(r.Context(), r.PathValue("slurmClusterId"), r.PathValue("partitionId"), &req)
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusOK, partition)
}

func (h *ClusterPartitionController) delete(w http.ResponseWriter, r *http.Request) {
	err := h.svc.Delete(r.Context(), r.PathValue("slurmClusterId"), r.PathValue("partitionId"))
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusNoContent, nil)
}
