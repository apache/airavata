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

package server_test

import (
	"context"
	"encoding/binary"
	"encoding/json"
	"io"
	"net"
	"net/http"
	"net/http/httptest"
	"strings"
	"testing"
	"time"

	"github.com/coder/websocket"
	"github.com/hashicorp/yamux"

	"github.com/apache/airavata/internal/link"
)

// fakeJupyter stands in for the Jupyter Server a job's Linkspan starts: it reports
// the path and Host it was asked for, and echoes on a WebSocket.
func fakeJupyter(t *testing.T) *httptest.Server {
	t.Helper()
	mux := http.NewServeMux()
	mux.HandleFunc("GET /api/status", func(w http.ResponseWriter, r *http.Request) {
		_ = json.NewEncoder(w).Encode(map[string]string{
			"path": r.URL.Path, "host": r.Host, "authorization": r.Header.Get("Authorization"),
		})
	})
	mux.HandleFunc("GET /api/kernels/k1/channels", func(w http.ResponseWriter, r *http.Request) {
		conn, err := websocket.Accept(w, r, nil)
		if err != nil {
			return
		}
		defer conn.CloseNow()
		for {
			kind, data, err := conn.Read(r.Context())
			if err != nil {
				return
			}
			if err := conn.Write(r.Context(), kind, append([]byte("echo:"), data...)); err != nil {
				return
			}
		}
	})
	srv := httptest.NewServer(mux)
	t.Cleanup(srv.Close)
	return srv
}

// fakeLinkspan dials the link as Linkspan does and carries each stream for port to
// target, refusing every other port.
func fakeLinkspan(t *testing.T, ctx context.Context, linkURL, token string, port uint16, target string) error {
	t.Helper()
	conn, resp, err := websocket.Dial(ctx, linkURL, &websocket.DialOptions{Subprotocols: []string{link.Protocol, link.TokenPrefix + token}})
	if resp != nil && resp.Body != nil {
		resp.Body.Close()
	}
	if err != nil {
		return err
	}
	conn.SetReadLimit(-1)
	session, err := yamux.Server(websocket.NetConn(ctx, conn, websocket.MessageBinary), nil)
	if err != nil {
		return err
	}
	go func() {
		defer session.Close()
		for {
			stream, err := session.Accept()
			if err != nil {
				return
			}
			go func() {
				defer stream.Close()
				var frame [2]byte
				if _, err := io.ReadFull(stream, frame[:]); err != nil {
					return
				}
				if binary.BigEndian.Uint16(frame[:]) != port {
					_, _ = stream.Write([]byte{0})
					return
				}
				server, err := net.Dial("tcp", target)
				if err != nil {
					_, _ = stream.Write([]byte{0})
					return
				}
				defer server.Close()
				_, _ = stream.Write([]byte{1})
				go func() { _, _ = io.Copy(server, stream); server.Close() }()
				_, _ = io.Copy(stream, server)
			}()
		}
	}()
	return nil
}

func TestLinkCarriesTheJupyterServerToItsOwner(t *testing.T) {
	h := newHarness(t)
	processID := h.seedProcess("link", tokenAlice)
	ctx, cancel := context.WithCancel(context.Background())
	defer cancel()

	issued, err := h.svcs.Link.Issue(ctx, processID)
	if err != nil {
		t.Fatalf("issue: %v", err)
	}
	if want := "wss://airavata.example.org/api/v1/processes/" + processID + "/link"; issued.URL != want {
		t.Errorf("link URL = %q, want %q", issued.URL, want)
	}
	control, jupyterPort := link.Ports(processID)
	if issued.ControlPort != control || issued.JupyterPort != jupyterPort || control < 20000 || jupyterPort > 40000 {
		t.Errorf("ports = %d/%d, want %d/%d in range", issued.ControlPort, issued.JupyterPort, control, jupyterPort)
	}

	api := httptest.NewServer(h.srv)
	defer api.Close()
	wsBase := "ws" + strings.TrimPrefix(api.URL, "http")
	linkURL := wsBase + "/api/v1/processes/" + processID + "/link"
	jupyter := fakeJupyter(t)
	proxy := api.URL + "/api/v1/processes/" + processID + "/linkspan/api/v1/forward/" + itoa(jupyterPort)

	get := func(path, authorization string) (*http.Response, map[string]string) {
		req, _ := http.NewRequest(http.MethodGet, proxy+path, nil)
		if authorization != "" {
			req.Header.Set("Authorization", authorization)
		}
		resp, err := http.DefaultClient.Do(req)
		if err != nil {
			t.Fatalf("GET %s: %v", path, err)
		}
		defer resp.Body.Close()
		var body map[string]string
		_ = json.NewDecoder(resp.Body).Decode(&body)
		return resp, body
	}

	if _, err := h.svcs.ProcessStatus.Record(ctx, processID, "SUBMITTED", nil); err != nil {
		t.Fatal(err)
	}

	// Before Linkspan links, the proxy has nowhere to go.
	if resp, _ := get("/api/status", "token "+issued.JupyterToken); resp.StatusCode != http.StatusBadGateway {
		t.Errorf("unlinked proxy: status = %d, want 502", resp.StatusCode)
	}

	// A wrong link token is refused before the upgrade.
	if err := fakeLinkspan(t, ctx, linkURL, "not-the-token", jupyterPort, jupyter.Listener.Addr().String()); err == nil {
		t.Fatal("link with a wrong token was admitted")
	}
	if err := fakeLinkspan(t, ctx, linkURL, issued.LinkToken, jupyterPort, jupyter.Listener.Addr().String()); err != nil {
		t.Fatalf("link: %v", err)
	}

	// Only the owner is handed the Jupyter token, and sees the link once it is up.
	if rec := h.do(http.MethodGet, "/api/v1/processes/"+processID+"/jupyter-server", tokenBob, nil); rec.Code != http.StatusForbidden {
		t.Errorf("non-owner jupyter-server: status = %d, want 403", rec.Code)
	}
	deadline := time.Now().Add(5 * time.Second)
	var access map[string]any
	for {
		access = h.mustDo(http.MethodGet, "/api/v1/processes/"+processID+"/jupyter-server", tokenAlice, nil, http.StatusOK)
		if access["linked"] == true || time.Now().After(deadline) {
			break
		}
		time.Sleep(20 * time.Millisecond)
	}
	if access["linked"] != true || access["token"] != issued.JupyterToken ||
		access["baseUrl"] != "/api/v1/processes/"+processID+"/linkspan/api/v1/forward/"+itoa(jupyterPort)+"/" {
		t.Fatalf("jupyter-server = %v", access)
	}
	if statuses := h.list("/api/v1/processes/"+processID+"/statuses", tokenAlice); statuses[len(statuses)-1]["status"] != "RUNNING" {
		t.Errorf("statuses after link = %v, want the submitted process recorded RUNNING", statuses)
	}

	// The proxy wants the Jupyter token, not a bearer token.
	if resp, _ := get("/api/status", ""); resp.StatusCode != http.StatusUnauthorized {
		t.Errorf("no token: status = %d, want 401", resp.StatusCode)
	}
	if resp, _ := get("/api/status", "Bearer "+tokenAlice); resp.StatusCode != http.StatusUnauthorized {
		t.Errorf("bearer token: status = %d, want 401", resp.StatusCode)
	}
	resp, body := get("/api/status", "token "+issued.JupyterToken)
	if resp.StatusCode != http.StatusOK {
		t.Fatalf("proxied status = %d, want 200", resp.StatusCode)
	}
	if want := "127.0.0.1:" + itoa(jupyterPort); body["path"] != "/api/status" || body["host"] != want ||
		body["authorization"] != "token "+issued.JupyterToken {
		t.Errorf("Jupyter Server saw %v", body)
	}

	// Kernel channels are WebSockets with the token in the query.
	wsURL := "ws" + strings.TrimPrefix(proxy, "http") + "/api/kernels/k1/channels?token=" + issued.JupyterToken
	kernel, wsResp, err := websocket.Dial(ctx, wsURL, nil)
	if wsResp != nil && wsResp.Body != nil {
		wsResp.Body.Close()
	}
	if err != nil {
		t.Fatalf("kernel websocket: %v", err)
	}
	defer kernel.CloseNow()
	if err := kernel.Write(ctx, websocket.MessageText, []byte("hello")); err != nil {
		t.Fatalf("write: %v", err)
	}
	if _, got, err := kernel.Read(ctx); err != nil || string(got) != "echo:hello" {
		t.Errorf("kernel echo = %q, %v", got, err)
	}

	// A resubmission is issued new tokens; the old ones open nothing.
	if _, err := h.svcs.Link.Issue(ctx, processID); err != nil {
		t.Fatalf("reissue: %v", err)
	}
	if resp, _ := get("/api/status", "token "+issued.JupyterToken); resp.StatusCode != http.StatusUnauthorized {
		t.Errorf("stale Jupyter token: status = %d, want 401", resp.StatusCode)
	}
}

func itoa(n uint16) string {
	b, _ := json.Marshal(n)
	return string(b)
}
