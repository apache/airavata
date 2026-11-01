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

package confirm

import (
	"encoding/json"
	"errors"
	"strings"
	"testing"
	"time"

	"github.com/modelcontextprotocol/go-sdk/jsonrpc"
	"github.com/modelcontextprotocol/go-sdk/mcp"
)

func request(args string, caps *mcp.ClientCapabilities) *mcp.CallToolRequest {
	meta := mcp.Meta{mcp.MetaKeyProtocolVersion: "2026-07-28"}
	if caps != nil {
		b, _ := json.Marshal(caps)
		var m map[string]any
		_ = json.Unmarshal(b, &m)
		meta[mcp.MetaKeyClientCapabilities] = m
	}
	return &mcp.CallToolRequest{Params: &mcp.CallToolParamsRaw{Name: "launch_process", Arguments: json.RawMessage(args), Meta: meta}}
}

var elicitCaps = &mcp.ClientCapabilities{Elicitation: &mcp.ElicitationCapabilities{Form: &mcp.FormElicitationCapabilities{}}}

// answer turns the pending result into the retried request a client would send.
func answer(req *mcp.CallToolRequest, pending *mcp.CallToolResult, action string, content map[string]any) *mcp.CallToolRequest {
	p := *req.Params
	p.RequestState = pending.RequestState
	p.InputResponses = mcp.InputResponseMap{inputKey: &mcp.ElicitResult{Action: action, Content: content}}
	return &mcp.CallToolRequest{Params: &p}
}

func newAsker(t *testing.T) *Asker {
	t.Helper()
	a, err := New()
	if err != nil {
		t.Fatal(err)
	}
	return a
}

func ask(t *testing.T, a *Asker, req *mcp.CallToolRequest) *mcp.CallToolResult {
	t.Helper()
	_, pending, err := a.Confirm(req, "cilogon:alice", "Launch?", nil)
	if err != nil || pending == nil || pending.InputRequests == nil {
		t.Fatalf("first call should ask: pending=%v err=%v", pending, err)
	}
	return pending
}

func TestConfirmAcceptedOnce(t *testing.T) {
	a := newAsker(t)
	req := request(`{"proposal_handle":"run_x"}`, elicitCaps)
	pending := ask(t, a, req)
	values, again, err := a.Confirm(answer(req, pending, "accept", map[string]any{"confirm": true}), "cilogon:alice", "Launch?", nil)
	if err != nil || again != nil || values == nil {
		t.Fatalf("accepted answer: values=%v pending=%v err=%v", values, again, err)
	}
}

func TestConfirmArgumentKeyOrderDoesNotMatter(t *testing.T) {
	a := newAsker(t)
	req := request(`{"a":1,"b":2}`, elicitCaps)
	pending := ask(t, a, req)
	reordered := request(`{"b":2,"a":1}`, elicitCaps)
	if _, _, err := a.Confirm(answer(reordered, pending, "accept", map[string]any{"confirm": true}), "cilogon:alice", "", nil); err != nil {
		t.Errorf("reordered keys were rejected: %v", err)
	}
}

func TestConfirmRefusals(t *testing.T) {
	a := newAsker(t)
	req := request(`{"proposal_handle":"run_x"}`, elicitCaps)
	pending := ask(t, a, req)
	yes := map[string]any{"confirm": true}

	cases := []struct {
		name      string
		req       *mcp.CallToolRequest
		principal string
		want      string
	}{
		{"declined", answer(req, pending, "decline", nil), "cilogon:alice", "declined"},
		{"cancelled", answer(req, pending, "cancel", nil), "cilogon:alice", "did not decline"},
		{"unknown action", answer(req, pending, "maybe", yes), "cilogon:alice", "declined"},
		{"box unchecked", answer(req, pending, "accept", map[string]any{"confirm": false}), "cilogon:alice", "declined"},
		{"other user", answer(req, pending, "accept", yes), "cilogon:mallory", "different user"},
		{"changed arguments", answer(request(`{"proposal_handle":"run_y"}`, elicitCaps), pending, "accept", yes), "cilogon:alice", "arguments changed"},
	}
	for _, tc := range cases {
		_, _, err := a.Confirm(tc.req, tc.principal, "", nil)
		if err == nil || !strings.Contains(err.Error(), tc.want) {
			t.Errorf("%s: err = %v, want %q", tc.name, err, tc.want)
		}
	}

	tampered := answer(req, pending, "accept", yes)
	tampered.Params.RequestState = pending.RequestState[:len(pending.RequestState)-2] + "AA"
	if _, _, err := a.Confirm(tampered, "cilogon:alice", "", nil); err == nil || !strings.Contains(err.Error(), "invalid") {
		t.Errorf("tampered state: %v", err)
	}
	if _, _, err := newAsker(t).Confirm(answer(req, pending, "accept", yes), "cilogon:alice", "", nil); err == nil {
		t.Error("state sealed by another process was accepted")
	}
}

func TestConfirmExpires(t *testing.T) {
	a := newAsker(t)
	req := request(`{}`, elicitCaps)
	pending := ask(t, a, req)
	a.now = func() time.Time { return time.Now().Add(StateTTL + time.Minute) }
	if _, _, err := a.Confirm(answer(req, pending, "accept", map[string]any{"confirm": true}), "cilogon:alice", "", nil); err == nil || !strings.Contains(err.Error(), "expired") {
		t.Errorf("expired confirmation: %v", err)
	}
}

func TestChooseAnswerCannotConfirm(t *testing.T) {
	a := newAsker(t)
	req := request(`{}`, elicitCaps)
	_, pending, err := a.Choose(req, "cilogon:alice", "Which?", []Field{{Name: "target", Type: "string", Enum: []string{"a"}}})
	if err != nil {
		t.Fatal(err)
	}
	if _, _, err := a.Confirm(answer(req, pending, "accept", map[string]any{"confirm": true}), "cilogon:alice", "", nil); err == nil {
		t.Error("an answer to a choice was accepted as a confirmation")
	}
}

func TestNoElicitationFailsClosed(t *testing.T) {
	a := newAsker(t)
	_, pending, err := a.Confirm(request(`{}`, &mcp.ClientCapabilities{}), "cilogon:alice", "", nil)
	if pending != nil || err == nil {
		t.Fatalf("a client without elicitation was asked anyway")
	}
	var wire *jsonrpc.Error
	if !errors.As(err, &wire) || wire.Code != mcp.CodeMissingRequiredClientCapabilities {
		t.Errorf("err = %v, want a %d error", err, mcp.CodeMissingRequiredClientCapabilities)
	}
}
