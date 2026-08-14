package iam

import (
	"net/http"

	"github.com/apache/airavata/internal/httpx"

	dto "github.com/apache/airavata/api/iam/dto"
)

// GroupController serves /api/v1/groups.
type GroupController struct{ svc *GroupService }

// NewGroupController returns a handler delegating to svc.
func NewGroupController(svc *GroupService) *GroupController { return &GroupController{svc: svc} }

// Register mounts the group routes.
//
// /groups/me is registered alongside /groups/{groupId}; the literal segment wins for
// that exact path, the same way /ssh-endpoint-credentials/me does.
func (h *GroupController) Register(mux *http.ServeMux) {
	mux.HandleFunc("GET /api/v1/groups", h.list)
	mux.HandleFunc("GET /api/v1/groups/me", h.listMine)
	mux.HandleFunc("POST /api/v1/groups", h.create)
	mux.HandleFunc("GET /api/v1/groups/{groupId}", h.get)
	mux.HandleFunc("PUT /api/v1/groups/{groupId}", h.update)
	mux.HandleFunc("DELETE /api/v1/groups/{groupId}", h.delete)
}

func (h *GroupController) list(w http.ResponseWriter, r *http.Request) {
	groups, err := h.svc.List(r.Context())
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusOK, groups)
}

func (h *GroupController) listMine(w http.ResponseWriter, r *http.Request) {
	groups, err := h.svc.ListMine(r.Context())
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusOK, groups)
}

func (h *GroupController) get(w http.ResponseWriter, r *http.Request) {
	group, err := h.svc.Get(r.Context(), r.PathValue("groupId"))
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusOK, group)
}

func (h *GroupController) create(w http.ResponseWriter, r *http.Request) {
	var req dto.GroupRequest
	if err := httpx.Bind(r, &req); err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	group, err := h.svc.Create(r.Context(), &req)
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusCreated, group)
}

func (h *GroupController) update(w http.ResponseWriter, r *http.Request) {
	var req dto.GroupRequest
	if err := httpx.Bind(r, &req); err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	group, err := h.svc.Update(r.Context(), r.PathValue("groupId"), &req)
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusOK, group)
}

func (h *GroupController) delete(w http.ResponseWriter, r *http.Request) {
	if err := h.svc.Delete(r.Context(), r.PathValue("groupId")); err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusNoContent, nil)
}

// GroupMemberController serves /api/v1/groups/{groupId}/members.
type GroupMemberController struct{ svc *GroupMemberService }

// NewGroupMemberController returns a handler delegating to svc.
func NewGroupMemberController(svc *GroupMemberService) *GroupMemberController {
	return &GroupMemberController{svc: svc}
}

// Register mounts the membership routes.
func (h *GroupMemberController) Register(mux *http.ServeMux) {
	mux.HandleFunc("GET /api/v1/groups/{groupId}/members", h.list)
	mux.HandleFunc("POST /api/v1/groups/{groupId}/members", h.add)
	mux.HandleFunc("GET /api/v1/groups/{groupId}/members/{userId}", h.get)
	mux.HandleFunc("PUT /api/v1/groups/{groupId}/members/{userId}", h.update)
	mux.HandleFunc("DELETE /api/v1/groups/{groupId}/members/{userId}", h.remove)
}

func (h *GroupMemberController) list(w http.ResponseWriter, r *http.Request) {
	members, err := h.svc.List(r.Context(), r.PathValue("groupId"))
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusOK, members)
}

func (h *GroupMemberController) get(w http.ResponseWriter, r *http.Request) {
	member, err := h.svc.Get(r.Context(), r.PathValue("groupId"), r.PathValue("userId"))
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusOK, member)
}

func (h *GroupMemberController) add(w http.ResponseWriter, r *http.Request) {
	var req dto.GroupMemberRequest
	if err := httpx.Bind(r, &req); err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	member, err := h.svc.Add(r.Context(), r.PathValue("groupId"), &req)
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusCreated, member)
}

func (h *GroupMemberController) update(w http.ResponseWriter, r *http.Request) {
	var req dto.GroupMemberUpdate
	if err := httpx.Bind(r, &req); err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	member, err := h.svc.Update(r.Context(), r.PathValue("groupId"), r.PathValue("userId"), &req)
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusOK, member)
}

func (h *GroupMemberController) remove(w http.ResponseWriter, r *http.Request) {
	if err := h.svc.Remove(r.Context(), r.PathValue("groupId"), r.PathValue("userId")); err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusNoContent, nil)
}
