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

// pendingSession registers a session owned by owner and starts a handshake waiting on
// it, returning the channel the handshake's answers arrive on.
func pendingSession(t *testing.T, sshCtx *ssh.GlobalSSHContext, owner string, prompts []ssh.Prompt) (*ssh.SSHInteractiveSession, <-chan []string) {
	t.Helper()
	dial := ssh.NewSSHDialConfig("login.example.org", 22, "alice", credmodel.SSHCredential{ID: "cred-1"}, owner)
	session := ssh.NewSSHInteractiveSession("session-1", dial)
	sshCtx.InteractiveSessionStore.AddSession(session)

	answers := make(chan []string, 1)
	// The handshake may outlive the test (a cancelled session errors out), so it hands
	// back what it got rather than reporting on t; callers that care check the answers.
	go func() {
		got, _ := session.Challenge(context.Background(), "", "Two-factor login", prompts)
		answers <- got
	}()
	// Challenge records its prompts before blocking; wait for that.
	for deadline := time.Now().Add(time.Second); ; {
		if got, _ := session.Read(); len(got) == len(prompts) {
			break
		}
		if time.Now().After(deadline) {
			t.Fatal("challenge never recorded its prompts")
		}
		time.Sleep(time.Millisecond)
	}
	return session, answers
}

// newServer serves the interactive session routes, taking the caller's identity from
// X-Test-User in place of a bearer token.
func newServer(t *testing.T, sshCtx *ssh.GlobalSSHContext) string {
	t.Helper()
	mux := http.NewServeMux()
	controller.NewInteractiveSessionController(service.NewInteractiveSessionService(sshCtx)).Register(mux)
	srv := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		if user := r.Header.Get("X-Test-User"); user != "" {
			r = r.WithContext(auth.WithPrincipal(r.Context(), &auth.Principal{Name: user}))
		}
		mux.ServeHTTP(w, r)
	}))
	t.Cleanup(srv.Close)
	return "ws" + strings.TrimPrefix(srv.URL, "http") + "/api/v1/ssh-interactive-sessions/session-1/ws"
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

func TestInteractiveSessionSocketAnswersChallenge(t *testing.T) {
	sshCtx := ssh.NewGlobalSSHContext()
	prompts := []ssh.Prompt{{Text: "Password: "}, {Text: "OTP: "}}
	_, answers := pendingSession(t, sshCtx, "cilogon:alice", prompts)

	conn, _, err := dial(t, newServer(t, sshCtx), "cilogon:alice")
	if err != nil {
		t.Fatalf("dial: %v", err)
	}
	defer conn.CloseNow()
	ctx := context.Background()

	var msg dto.InteractiveSessionMessage
	if err := wsjson.Read(ctx, conn, &msg); err != nil {
		t.Fatalf("reading challenge: %v", err)
	}
	if msg.Type != "challenge" || msg.Session == nil || len(msg.Session.Prompts) != 2 ||
		msg.Session.Instruction != "Two-factor login" || msg.Session.HostName != "login.example.org" {
		t.Fatalf("unexpected challenge: %+v", msg)
	}

	// The wrong number of responses is refused, and the socket stays open for a retry.
	if err := wsjson.Write(ctx, conn, dto.InteractiveSessionAnswer{Responses: []string{"only-one"}}); err != nil {
		t.Fatal(err)
	}
	if err := wsjson.Read(ctx, conn, &msg); err != nil || msg.Type != "error" {
		t.Fatalf("want error message, got %+v (%v)", msg, err)
	}

	if err := wsjson.Write(ctx, conn, dto.InteractiveSessionAnswer{Responses: []string{"hunter2", "123456"}}); err != nil {
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

func TestInteractiveSessionSocketRefusesOthers(t *testing.T) {
	sshCtx := ssh.NewGlobalSSHContext()
	session, _ := pendingSession(t, sshCtx, "cilogon:alice", []ssh.Prompt{{Text: "OTP: "}})
	t.Cleanup(func() { _ = session.SetResponse(nil, context.Canceled) })
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
	session := ssh.NewSSHInteractiveSession("s", ssh.NewSSHDialConfig("h", 22, "u", credmodel.SSHCredential{}, "o"))
	if err := session.SetResponse([]string{"a"}, nil); err != nil {
		t.Fatal(err)
	}
	if err := session.SetResponse([]string{"b"}, nil); err != ssh.ErrSessionAnswered {
		t.Fatalf("second answer: want ErrSessionAnswered, got %v", err)
	}
}
