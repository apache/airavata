package controller

import (
	"net/http"

	"github.com/apache/airavata/internal/httpx"

	"github.com/apache/airavata/api/process/service"
)

// LaunchController serves the launch action on /api/v1/processes/{processId}.
//
// Launching is a verb rather than a resource: it has no body, nothing is addressable
// afterwards under /launch, and what it produces is read back through the process's
// task collections. Hence the one POST, and no CRUD alongside it.
type LaunchController struct{ svc *service.LaunchService }

// NewLaunchController returns a handler delegating to svc.
func NewLaunchController(svc *service.LaunchService) *LaunchController {
	return &LaunchController{svc: svc}
}

// Register mounts the launch route. The literal /launch segment cannot collide with
// the task collections beside it, so no ordering is implied.
func (h *LaunchController) Register(mux *http.ServeMux) {
	mux.HandleFunc("POST /api/v1/processes/{processId}/launch", h.launch)
}

// launch answers 202 rather than 200: the tasks are recorded here, and carrying them
// out is somebody else's later work. The body is the process itself, so a caller that
// launched by id learns nothing new but has the run to hand.
func (h *LaunchController) launch(w http.ResponseWriter, r *http.Request) {
	proc, err := h.svc.LaunchProcess(r.Context(), r.PathValue("processId"))
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusAccepted, proc)
}
