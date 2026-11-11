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

// Package link holds the WebSockets Linkspan dials back to Airavata and reaches a job's
// servers through them.
//
// It speaks Linkspan's link protocol, the one cs-plane defined
// (https://github.com/cyber-shuttle/linkspan/blob/main/docs/COMPATIBILITY.md): one
// WebSocket per job, offering the subprotocols cybershuttle.v1 and link.<token> and
// carrying yamux in binary frames, with Airavata as the yamux client. For each stream
// Airavata writes the target port as two big-endian bytes; Linkspan answers 1 when it
// connected to a server it runs on that loopback port, else 0, and then carries bytes
// until either end closes. yamux's keepalive detects a dead link, and Linkspan redials.
//
// The registry is process-local. When Airavata restarts, every Linkspan redials.
package link

import (
	"context"
	"crypto/sha256"
	"encoding/binary"
	"errors"
	"fmt"
	"io"
	"net"
	"net/http"
	"strconv"
	"strings"
	"sync"
	"time"

	"github.com/hashicorp/yamux"
)

// Protocol is the WebSocket subprotocol Linkspan offers and the link endpoint selects.
const Protocol = "cybershuttle.v1"

// TokenPrefix marks the subprotocol that carries the link token.
const TokenPrefix = "link."

// hostSuffix is the pseudo-domain of the addresses Transport dials: <process id>.process.
const hostSuffix = ".process"

// ErrNotLinked reports a process whose Linkspan holds no link to this server.
var ErrNotLinked = errors.New("the process's Linkspan is not linked to Airavata")

// Registry holds the live link of each process.
type Registry struct {
	mu    sync.Mutex
	links map[string]*yamux.Session
}

// NewRegistry returns an empty registry.
func NewRegistry() *Registry {
	return &Registry{links: map[string]*yamux.Session{}}
}

// Serve runs conn, the WebSocket a process's Linkspan opened, as that process's link
// until it ends or ctx is done. A newer link replaces an older one: the older is
// closed, since only one job of the process holds valid tokens at a time and a
// redialling Linkspan should not be refused by its own stale socket.
func (r *Registry) Serve(ctx context.Context, processID string, conn net.Conn) error {
	config := yamux.DefaultConfig()
	config.LogOutput = io.Discard
	session, err := yamux.Client(conn, config)
	if err != nil {
		_ = conn.Close()
		return err
	}

	r.mu.Lock()
	previous := r.links[processID]
	r.links[processID] = session
	r.mu.Unlock()
	if previous != nil {
		_ = previous.Close()
	}

	select {
	case <-session.CloseChan():
	case <-ctx.Done():
		_ = session.Close()
	}

	r.mu.Lock()
	if r.links[processID] == session {
		delete(r.links, processID)
	}
	r.mu.Unlock()
	return nil
}

// Linked reports whether the process currently holds a link.
func (r *Registry) Linked(processID string) bool {
	return r.session(processID) != nil
}

func (r *Registry) session(processID string) *yamux.Session {
	r.mu.Lock()
	defer r.mu.Unlock()
	session := r.links[processID]
	if session != nil && session.IsClosed() {
		return nil
	}
	return session
}

// Dial opens a stream to the server the process's job runs on loopback port.
func (r *Registry) Dial(ctx context.Context, processID string, port uint16) (net.Conn, error) {
	session := r.session(processID)
	if session == nil {
		return nil, ErrNotLinked
	}
	stream, err := session.OpenStream()
	if err != nil {
		return nil, err
	}
	stop := context.AfterFunc(ctx, func() { _ = stream.Close() })
	defer stop()

	// The handshake is bounded on its own: a wedged Linkspan must not hold a proxied
	// request open until the caller gives up.
	_ = stream.SetDeadline(time.Now().Add(30 * time.Second))
	frame := binary.BigEndian.AppendUint16(nil, port)
	if _, err = stream.Write(frame); err == nil {
		_, err = io.ReadFull(stream, frame[:1])
	}
	if err == nil && frame[0] != 1 {
		err = fmt.Errorf("the process's Linkspan runs no server on port %d", port)
	}
	if err != nil {
		_ = stream.Close()
		return nil, err
	}
	_ = stream.SetDeadline(time.Time{})
	return stream, nil
}

// Host is the address Transport dials to reach port on the process's job.
func Host(processID string, port uint16) string {
	return processID + hostSuffix + ":" + strconv.Itoa(int(port))
}

// Transport returns an HTTP transport whose connections are streams over the links,
// for addresses made by Host. It pools them per process and port, and carries
// WebSocket upgrades, so a reverse proxy over it serves a Jupyter Server whole.
func (r *Registry) Transport() *http.Transport {
	return &http.Transport{
		DialContext: func(ctx context.Context, _, address string) (net.Conn, error) {
			host, rawPort, err := net.SplitHostPort(address)
			if err != nil {
				return nil, err
			}
			processID, ok := strings.CutSuffix(host, hostSuffix)
			port, perr := strconv.ParseUint(rawPort, 10, 16)
			if !ok || processID == "" || perr != nil {
				return nil, fmt.Errorf("%s is not a process address", address)
			}
			return r.Dial(ctx, processID, uint16(port))
		},
		MaxIdleConnsPerHost:   8,
		IdleConnTimeout:       90 * time.Second,
		ResponseHeaderTimeout: 5 * time.Minute,
	}
}

// Ports derives the loopback ports a process's job serves on: Linkspan's control API
// and, one above it, the Jupyter Server. They are a function of the process id, in
// 20000–39999, so Airavata knows them before the job starts. A job sharing a node with
// another that drew the same port fails to start its server, as with any fixed port.
func Ports(processID string) (control, jupyter uint16) {
	sum := sha256.Sum256([]byte(processID))
	base := 20000 + int(binary.BigEndian.Uint16(sum[:2]))%19999
	return uint16(base), uint16(base + 1)
}

// OfferedToken returns the link token among the subprotocols a WebSocket upgrade
// offered, or "".
func OfferedToken(r *http.Request) string {
	for _, offered := range r.Header.Values("Sec-WebSocket-Protocol") {
		for _, p := range strings.Split(offered, ",") {
			if token, ok := strings.CutPrefix(strings.TrimSpace(p), TokenPrefix); ok {
				return token
			}
		}
	}
	return ""
}

// Issued is what a process's job is handed so its Linkspan can link: where to dial,
// the token to offer, and the ports and Jupyter token its servers use.
type Issued struct {
	URL          string
	LinkToken    string
	JupyterToken string
	ControlPort  uint16
	JupyterPort  uint16
}

// Env is the job environment that carries Issued, under the names Linkspan reads
// (LINKSPAN_LINK_TOKEN, JUPYTER_TOKEN) and the ones its documented job script uses
// (CS_LINK_URL, CS_CONTROL_PORT), plus CS_JUPYTER_PORT for the workflow's addr.
func (i *Issued) Env() map[string]string {
	return map[string]string{
		"CS_LINK_URL":         i.URL,
		"CS_CONTROL_PORT":     strconv.Itoa(int(i.ControlPort)),
		"CS_JUPYTER_PORT":     strconv.Itoa(int(i.JupyterPort)),
		"LINKSPAN_LINK_TOKEN": i.LinkToken,
		"JUPYTER_TOKEN":       i.JupyterToken,
	}
}
