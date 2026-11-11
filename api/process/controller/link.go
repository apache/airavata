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
	"log/slog"
	"net/http"
	"net/http/httputil"
	"strconv"
	"strings"

	"github.com/coder/websocket"

	"github.com/apache/airavata/internal/httpx"
	"github.com/apache/airavata/internal/link"

	"github.com/apache/airavata/api/process/service"
)

// LinkController serves the routes that make Airavata a process's Linkspan control
// plane: the link Linkspan dials, the owner's Jupyter access, and the Jupyter proxy.
//
// The link and the proxy sit outside bearer authentication, each opened by a secret
// issued to the process: the link by its link token, offered as a WebSocket
// subprotocol, and the proxy by its Jupyter token, which Jupyter clients send as
// "Authorization: token …" or, on WebSockets, as ?token=. Neither header is a bearer
// token, so the auth middleware lets both through as anonymous.
type LinkController struct {
	svc   *service.LinkService
	proxy *httputil.ReverseProxy
}

// NewLinkController returns a handler delegating to svc.
func NewLinkController(svc *service.LinkService) *LinkController {
	return &LinkController{
		svc: svc,
		proxy: &httputil.ReverseProxy{
			Transport: svc.Registry().Transport(),
			// The target was set on the request by the linkspan handler; Rewrite only keeps the
			// client's forwarding headers out.
			Rewrite: func(out *httputil.ProxyRequest) {
				out.Out.URL.Scheme = "http"
				out.Out.URL.Host = out.In.URL.Host
				out.Out.Host = out.In.Host
			},
			ErrorHandler: func(w http.ResponseWriter, r *http.Request, err error) {
				slog.Warn("Linkspan proxy could not reach the process's job", "path", r.URL.Path, "error", err)
				httpx.WriteJSON(w, http.StatusBadGateway, map[string]string{
					"error":   "Bad Gateway",
					"message": "The process's Linkspan could not be reached: its job is not linked to Airavata, or the server has not started",
				})
			},
		},
	}
}

// Register mounts the link routes.
func (h *LinkController) Register(mux *http.ServeMux) {
	mux.HandleFunc("GET /api/v1/processes/{processId}/link", h.link)
	mux.HandleFunc("GET /api/v1/processes/{processId}/jupyter-server", h.jupyterServer)
	mux.HandleFunc("/api/v1/processes/{processId}/linkspan/", h.linkspan)
}

// link admits the WebSocket a process's Linkspan dials and holds it as the process's
// link until it closes.
func (h *LinkController) link(w http.ResponseWriter, r *http.Request) {
	processID := r.PathValue("processId")
	if !h.svc.Authenticate(r.Context(), processID, link.OfferedToken(r)) {
		httpx.WriteError(w, r, httpx.Unauthorized("The link token is not the one issued to process %s", processID))
		return
	}
	if err := h.svc.Begun(r.Context(), processID); err != nil {
		slog.Warn("Failed to record a linking process as running", "processId", processID, "error", err)
	}
	conn, err := websocket.Accept(w, r, &websocket.AcceptOptions{Subprotocols: []string{link.Protocol}})
	if err != nil {
		return
	}
	defer conn.CloseNow()
	// yamux frames span WebSocket messages freely; the default 32 KiB message limit
	// is Linkspan's own write size, so lift it rather than depend on that.
	conn.SetReadLimit(-1)

	ctx := r.Context()
	slog.Info("Linkspan linked", "processId", processID, "remote", r.RemoteAddr)
	_ = h.svc.Registry().Serve(ctx, processID, websocket.NetConn(ctx, conn, websocket.MessageBinary))
	slog.Info("Linkspan link closed", "processId", processID)
}

// jupyterServer returns the owner's access to the process's Jupyter Server.
func (h *LinkController) jupyterServer(w http.ResponseWriter, r *http.Request) {
	resp, err := h.svc.JupyterServer(r.Context(), r.PathValue("processId"))
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusOK, resp)
}

// linkspan proxies a request over the process's link: to Linkspan's API, or to the port
// itself for a path under forward/{port}/, as Linkspan's forward carries TCP only.
// The token stays on the request because the Jupyter Server checks it again.
func (h *LinkController) linkspan(w http.ResponseWriter, r *http.Request) {
	processID := r.PathValue("processId")
	token := r.URL.Query().Get("token")
	if scheme, value, found := strings.Cut(r.Header.Get("Authorization"), " "); found && strings.EqualFold(scheme, "token") {
		token = strings.TrimSpace(value)
	}
	port, ok := h.svc.ControlPort(r.Context(), processID, token)
	if !ok {
		httpx.WriteError(w, r, httpx.Unauthorized("The Jupyter token is not the one issued to process %s", processID))
		return
	}

	prefix := "/api/v1/processes/" + processID + "/linkspan"
	out := r.Clone(r.Context())
	out.URL.Path = strings.TrimPrefix(r.URL.Path, prefix)
	out.URL.RawPath = ""
	if out.URL.Path == "" {
		out.URL.Path = "/"
	}
	// A bare forward/{port} has no slash to cut on and stays Linkspan's own TCP stream.
	if rawPort, path, ok := strings.Cut(strings.TrimPrefix(out.URL.Path, "/api/v1/forward/"), "/"); ok {
		if forwarded, err := strconv.ParseUint(rawPort, 10, 16); err == nil {
			port, out.URL.Path = uint16(forwarded), "/"+path
		}
	}
	out.URL.Host = link.Host(processID, port)
	// The server sees itself addressed on its own loopback port.
	out.Host = "127.0.0.1:" + strconv.Itoa(int(port))
	h.proxy.ServeHTTP(w, out)
}
