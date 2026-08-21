package controller

import (
	"net/http"

	"github.com/apache/airavata/internal/httpx"

	dto "github.com/apache/airavata/api/process/dto"
	model "github.com/apache/airavata/api/process/model"
	"github.com/apache/airavata/api/process/service"
)

// TaskController serves one kind of task nested under
// /api/v1/processes/{processId}.
//
// The four kinds are the same five routes over a different payload, so the handlers
// are written once and instantiated per kind. What differs is supplied at
// construction: the collection's path segment and how a row is rendered.
type TaskController[T any, Req any, Res any] struct {
	svc  *service.TaskService[T, Req]
	path string

	render    func(*T) Res
	renderAll func([]T) []Res
}

// Register mounts the task routes for this kind.
func (h *TaskController[T, Req, Res]) Register(mux *http.ServeMux) {
	base := "/api/v1/processes/{processId}/" + h.path

	mux.HandleFunc("GET "+base, h.list)
	mux.HandleFunc("POST "+base, h.create)
	mux.HandleFunc("GET "+base+"/{taskId}", h.get)
	mux.HandleFunc("PUT "+base+"/{taskId}", h.update)
	mux.HandleFunc("DELETE "+base+"/{taskId}", h.delete)
}

func (h *TaskController[T, Req, Res]) list(w http.ResponseWriter, r *http.Request) {
	tasks, err := h.svc.ListForProcess(r.Context(), r.PathValue("processId"))
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusOK, h.renderAll(tasks))
}

func (h *TaskController[T, Req, Res]) get(w http.ResponseWriter, r *http.Request) {
	task, err := h.svc.Get(r.Context(), r.PathValue("processId"), r.PathValue("taskId"))
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusOK, h.render(task))
}

func (h *TaskController[T, Req, Res]) create(w http.ResponseWriter, r *http.Request) {
	var req Req
	if err := httpx.Bind(r, &req); err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	task, err := h.svc.Create(r.Context(), r.PathValue("processId"), &req)
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusCreated, h.render(task))
}

func (h *TaskController[T, Req, Res]) update(w http.ResponseWriter, r *http.Request) {
	var req Req
	if err := httpx.Bind(r, &req); err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	task, err := h.svc.Update(r.Context(), r.PathValue("processId"), r.PathValue("taskId"), &req)
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusOK, h.render(task))
}

func (h *TaskController[T, Req, Res]) delete(w http.ResponseWriter, r *http.Request) {
	if err := h.svc.Delete(r.Context(), r.PathValue("processId"), r.PathValue("taskId")); err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusNoContent, nil)
}

// NewDataStagingTaskController serves .../data-staging-tasks.
func NewDataStagingTaskController(svc *service.DataStagingTaskService) *TaskController[model.DataStagingTask, dto.DataStagingTaskRequest, dto.DataStagingTaskResponse] {
	return &TaskController[model.DataStagingTask, dto.DataStagingTaskRequest, dto.DataStagingTaskResponse]{
		svc:       svc,
		path:      "data-staging-tasks",
		render:    dto.ToDataStagingTaskResponse,
		renderAll: dto.ToDataStagingTaskResponses,
	}
}

// NewJobSubmissionTaskController serves .../job-submission-tasks.
func NewJobSubmissionTaskController(svc *service.JobSubmissionTaskService) *TaskController[model.JobSubmissionTask, dto.JobSubmissionTaskRequest, dto.JobSubmissionTaskResponse] {
	return &TaskController[model.JobSubmissionTask, dto.JobSubmissionTaskRequest, dto.JobSubmissionTaskResponse]{
		svc:       svc,
		path:      "job-submission-tasks",
		render:    dto.ToJobSubmissionTaskResponse,
		renderAll: dto.ToJobSubmissionTaskResponses,
	}
}

// NewJobMonitoringTaskController serves .../job-monitoring-tasks.
func NewJobMonitoringTaskController(svc *service.JobMonitoringTaskService) *TaskController[model.JobMonitoringTask, dto.JobMonitoringTaskRequest, dto.JobMonitoringTaskResponse] {
	return &TaskController[model.JobMonitoringTask, dto.JobMonitoringTaskRequest, dto.JobMonitoringTaskResponse]{
		svc:       svc,
		path:      "job-monitoring-tasks",
		render:    dto.ToJobMonitoringTaskResponse,
		renderAll: dto.ToJobMonitoringTaskResponses,
	}
}

// NewInteractiveCommandTaskController serves .../interactive-command-tasks.
func NewInteractiveCommandTaskController(svc *service.InteractiveCommandTaskService) *TaskController[model.InteractiveCommandTask, dto.InteractiveCommandTaskRequest, dto.InteractiveCommandTaskResponse] {
	return &TaskController[model.InteractiveCommandTask, dto.InteractiveCommandTaskRequest, dto.InteractiveCommandTaskResponse]{
		svc:       svc,
		path:      "interactive-command-tasks",
		render:    dto.ToInteractiveCommandTaskResponse,
		renderAll: dto.ToInteractiveCommandTaskResponses,
	}
}
