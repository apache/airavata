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

package controller_test

import (
	"context"
	"encoding/json"
	"net/http"
	"net/http/httptest"
	"slices"
	"strings"
	"testing"
	"time"

	"github.com/coder/websocket"
	"github.com/coder/websocket/wsjson"

	"github.com/apache/airavata/internal/auth"
	"github.com/apache/airavata/internal/ssh"

	"github.com/apache/airavata/api/credentials/controller"
	dto "github.com/apache/airavata/api/credentials/dto"
	credmodel "github.com/apache/airavata/api/credentials/model"
	"github.com/apache/airavata/api/credentials/service"
)

// pendingAuth registers an auth owned by owner and starts a handshake waiting on
// it, returning the channel the handshake's answers arrive on.
func pendingAuth(t *testing.T, sshCtx *ssh.GlobalSSHContext, owner string, prompts []ssh.Prompt) (*ssh.SSHInteractiveAuth, <-chan []string) {
	t.Helper()
	dial := ssh.NewSSHDialConfig("login.example.org", 22, "alice", credmodel.SSHCredential{ID: "cred-1"}, owner)
	pending := ssh.NewSSHInteractiveAuth("auth-1", dial)
	sshCtx.InteractiveAuthStore.AddAuth(pending)

	answers := make(chan []string, 1)
	// The handshake may outlive the test (a cancelled auth errors out), so it hands
	// back what it got rather than reporting on t; callers that care check the answers.
	go func() {
		got, _ := pending.Challenge(context.Background(), "", "Two-factor login", prompts)
		answers <- got
	}()
	// Challenge records its prompts before blocking; wait for that.
	for deadline := time.Now().Add(time.Second); ; {
		if got, _ := pending.Read(); len(got) == len(prompts) {
			break
		}
		if time.Now().After(deadline) {
			t.Fatal("challenge never recorded its prompts")
		}
		time.Sleep(time.Millisecond)
	}
	return pending, answers
}

// newServer serves the interactive auth routes, taking the caller's identity from
// X-Test-User in place of a bearer token.
func newServer(t *testing.T, sshCtx *ssh.GlobalSSHContext) string {
	t.Helper()
	mux := http.NewServeMux()
	controller.NewInteractiveAuthController(service.NewInteractiveAuthService(sshCtx)).Register(mux)
	srv := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		if user := r.Header.Get("X-Test-User"); user != "" {
			r = r.WithContext(auth.WithPrincipal(r.Context(), &auth.Principal{Name: user}))
		}
		mux.ServeHTTP(w, r)
	}))
	t.Cleanup(srv.Close)
	return "ws" + strings.TrimPrefix(srv.URL, "http") + "/api/v1/ssh-interactive-auths/auth-1/ws"
}

func dial(t *testing.T, url, user string) (*websocket.Conn, *http.Response, error) {
	t.Helper()
	ctx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
	t.Cleanup(cancel)
	header := http.Header{}
	if user != "" {
		header.Set("X-Test-User", user)
	}
	return websocket.Dial(ctx, url, &websocket.DialOptions{HTTPHeader: header})
}

func TestInteractiveAuthSocketAnswersChallenge(t *testing.T) {
	sshCtx := ssh.NewGlobalSSHContext()
	prompts := []ssh.Prompt{{Text: "Password: "}, {Text: "OTP: "}}
	_, answers := pendingAuth(t, sshCtx, "cilogon:alice", prompts)

	conn, _, err := dial(t, newServer(t, sshCtx), "cilogon:alice")
	if err != nil {
		t.Fatalf("dial: %v", err)
	}
	defer conn.CloseNow()
	ctx := context.Background()

	var msg dto.InteractiveAuthMessage
	if err := wsjson.Read(ctx, conn, &msg); err != nil {
		t.Fatalf("reading challenge: %v", err)
	}
	if msg.Type != "challenge" || msg.Auth == nil || len(msg.Auth.Prompts) != 2 ||
		msg.Auth.Instruction != "Two-factor login" || msg.Auth.HostName != "login.example.org" {
		t.Fatalf("unexpected challenge: %+v", msg)
	}

	// The wrong number of responses is refused, and the socket stays open for a retry.
	if err := wsjson.Write(ctx, conn, dto.InteractiveAuthAnswer{Responses: []string{"only-one"}}); err != nil {
		t.Fatal(err)
	}
	if err := wsjson.Read(ctx, conn, &msg); err != nil || msg.Type != "error" {
		t.Fatalf("want error message, got %+v (%v)", msg, err)
	}

	if err := wsjson.Write(ctx, conn, dto.InteractiveAuthAnswer{Responses: []string{"hunter2", "123456"}}); err != nil {
		t.Fatal(err)
	}
	if err := wsjson.Read(ctx, conn, &msg); err != nil || msg.Type != "accepted" {
		t.Fatalf("want accepted, got %+v (%v)", msg, err)
	}

	select {
	case got := <-answers:
		if !slices.Equal(got, []string{"hunter2", "123456"}) {
			t.Fatalf("handshake got %q", got)
		}
	case <-time.After(time.Second):
		t.Fatal("handshake was never released")
	}
}

func TestInteractiveAuthSocketRefusesOthers(t *testing.T) {
	sshCtx := ssh.NewGlobalSSHContext()
	pending, _ := pendingAuth(t, sshCtx, "cilogon:alice", []ssh.Prompt{{Text: "OTP: "}})
	t.Cleanup(func() { _ = pending.SetResponse(nil, context.Canceled) })
	url := newServer(t, sshCtx)

	for _, tc := range []struct {
		user   string
		status int
	}{
		{"", http.StatusUnauthorized},
		{"cilogon:mallory", http.StatusNotFound},
	} {
		_, resp, err := dial(t, url, tc.user)
		if err == nil {
			t.Fatalf("user %q: dial succeeded", tc.user)
		}
		if resp == nil || resp.StatusCode != tc.status {
			t.Fatalf("user %q: want %d, got %v", tc.user, tc.status, resp)
		}
	}
}

func TestSetResponseOnlyOnce(t *testing.T) {
	pending := ssh.NewSSHInteractiveAuth("s", ssh.NewSSHDialConfig("h", 22, "u", credmodel.SSHCredential{}, "o"))
	if err := pending.SetResponse([]string{"a"}, nil); err != nil {
		t.Fatal(err)
	}
	if err := pending.SetResponse([]string{"b"}, nil); err != ssh.ErrAuthAnswered {
		t.Fatalf("second answer: want ErrAuthAnswered, got %v", err)
	}
}

// restURL turns newServer's socket URL into the auth's REST URL.
func restURL(wsURL string) string {
	return "http" + strings.TrimSuffix(strings.TrimPrefix(wsURL, "ws"), "/ws")
}

func post(t *testing.T, url, user, body string) *http.Response {
	t.Helper()
	req, err := http.NewRequest(http.MethodPost, url, strings.NewReader(body))
	if err != nil {
		t.Fatal(err)
	}
	req.Header.Set("Content-Type", "application/json")
	if user != "" {
		req.Header.Set("X-Test-User", user)
	}
	resp, err := http.DefaultClient.Do(req)
	if err != nil {
		t.Fatal(err)
	}
	resp.Body.Close()
	return resp
}

func TestInteractiveAuthRestAnswersChallenge(t *testing.T) {
	sshCtx := ssh.NewGlobalSSHContext()
	_, answers := pendingAuth(t, sshCtx, "cilogon:alice", []ssh.Prompt{{Text: "Password: "}, {Text: "OTP: "}})
	url := restURL(newServer(t, sshCtx))

	req, _ := http.NewRequest(http.MethodGet, url, nil)
	req.Header.Set("X-Test-User", "cilogon:alice")
	resp, err := http.DefaultClient.Do(req)
	if err != nil {
		t.Fatal(err)
	}
	var pending dto.InteractiveAuthResponse
	err = json.NewDecoder(resp.Body).Decode(&pending)
	resp.Body.Close()
	if err != nil || resp.StatusCode != http.StatusOK || len(pending.Prompts) != 2 {
		t.Fatalf("GET auth: %d %+v (%v)", resp.StatusCode, pending, err)
	}

	for _, tc := range []struct {
		user, body string
		status     int
	}{
		{"", `{"responses":["a","b"]}`, http.StatusUnauthorized},
		{"cilogon:mallory", `{"responses":["a","b"]}`, http.StatusNotFound},
		{"cilogon:alice", `{"responses":["only-one"]}`, http.StatusBadRequest},
		{"cilogon:alice", `{"responses":["hunter2","123456"]}`, http.StatusNoContent},
		{"cilogon:alice", `{"responses":["again","again"]}`, http.StatusConflict},
	} {
		if got := post(t, url+"/response", tc.user, tc.body).StatusCode; got != tc.status {
			t.Fatalf("user %q body %s: want %d, got %d", tc.user, tc.body, tc.status, got)
		}
	}

	select {
	case got := <-answers:
		if !slices.Equal(got, []string{"hunter2", "123456"}) {
			t.Fatalf("handshake got %q", got)
		}
	case <-time.After(time.Second):
		t.Fatal("handshake was never released")
	}
}
