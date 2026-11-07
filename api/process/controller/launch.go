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
	"maps"
	"net/http"
	"net/http/httputil"
	"strings"
	"time"

	"github.com/coder/websocket"

	"github.com/apache/airavata/internal/auth"
	"github.com/apache/airavata/internal/httpx"
	"github.com/apache/airavata/internal/orchestration"

	"github.com/apache/airavata/api/process/service"
)

// LaunchController serves a run's verbs on /api/v1/processes/{processId}: launch, stop,
// access, and the job's link and proxy. None is a resource, so there is no CRUD alongside.
type LaunchController struct {
	svc       *service.LaunchService
	engine    *orchestration.ExecutionEngine
	transport http.RoundTripper
}

// NewLaunchController returns a handler delegating to svc.
func NewLaunchController(svc *service.LaunchService, engine *orchestration.ExecutionEngine) *LaunchController {
	return &LaunchController{svc: svc, engine: engine, transport: &http.Transport{DialContext: engine.DialLink, MaxIdleConnsPerHost: 8, IdleConnTimeout: 90 * time.Second, ResponseHeaderTimeout: 5 * time.Minute}}
}

// Register mounts the routes. Their literal segments cannot collide with the task
// collections beside them, so no ordering is implied.
func (h *LaunchController) Register(mux *http.ServeMux) {
	mux.HandleFunc("POST /api/v1/processes/{processId}/launch", h.launch)
	mux.HandleFunc("POST /api/v1/processes/{processId}/stop", h.stop)
	mux.HandleFunc("GET /api/v1/processes/{processId}/access", h.access)
	mux.HandleFunc("GET /api/v1/processes/{processId}/link", h.link)
	mux.HandleFunc("/api/v1/processes/{processId}/proxy/{port}/", h.proxy)
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

func (h *LaunchController) stop(w http.ResponseWriter, r *http.Request) {
	proc, err := h.svc.Stop(r.Context(), r.PathValue("processId"))
	httpx.Answer(w, r, proc, err)
}

func (h *LaunchController) access(w http.ResponseWriter, r *http.Request) {
	access, err := h.svc.Access(r.Context(), r.PathValue("processId"))
	httpx.Answer(w, r, access, err)
}

func (h *LaunchController) link(w http.ResponseWriter, r *http.Request) {
	proc, err := h.svc.AuthorizeLink(r.Context(), r.PathValue("processId"), auth.Subprotocol(r, "link."))
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	// Any Origin: the link token, not the origin, guards this route.
	if control, err := websocket.Accept(w, r, &websocket.AcceptOptions{Subprotocols: []string{"cybershuttle.v1"}, InsecureSkipVerify: true}); err == nil {
		h.engine.AcceptLink(r.Context(), proc, control)
	}
}

func (h *LaunchController) proxy(w http.ResponseWriter, r *http.Request) {
	token := r.URL.Query().Get("token")
	if scheme, value, found := strings.Cut(r.Header.Get("Authorization"), " "); found && strings.EqualFold(scheme, "token") {
		token = value
	}
	id := r.PathValue("processId")
	if err := h.svc.AuthorizeTraffic(id, token); err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	proxy := &httputil.ReverseProxy{
		Transport: h.transport,
		Rewrite: func(out *httputil.ProxyRequest) {
			out.Out.URL.Scheme, out.Out.URL.Host = "http", id+":"+r.PathValue("port")
			out.Out.Host = "127.0.0.1:" + r.PathValue("port")
		},
		// The CORS middleware has answered for this origin; the application's own header would make a second value.
		ModifyResponse: func(response *http.Response) error {
			maps.DeleteFunc(response.Header, func(name string, _ []string) bool { return strings.HasPrefix(name, "Access-Control-") })
			return nil
		},
		ErrorHandler: func(w http.ResponseWriter, r *http.Request, _ error) {
			httpx.WriteError(w, r, &httpx.Error{Status: http.StatusBadGateway, Message: "The job's application could not be reached"})
		},
	}
	http.StripPrefix("/api/v1/processes/"+id+"/proxy/"+r.PathValue("port"), proxy).ServeHTTP(w, r)
}
