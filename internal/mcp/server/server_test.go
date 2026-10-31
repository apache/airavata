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

package server

import (
	"bytes"
	"context"
	"encoding/json"
	"errors"
	"net/http"
	"net/http/httptest"
	"strings"
	"sync"
	"testing"

	"github.com/modelcontextprotocol/go-sdk/jsonrpc"
	"github.com/modelcontextprotocol/go-sdk/mcp"

	"github.com/apache/airavata/internal/mcp/policy"
)

const (
	clusterID    = "11111111-1111-1111-1111-111111111111"
	partitionID  = "22222222-2222-2222-2222-222222222222"
	templateID   = "33333333-3333-3333-3333-333333333333"
	inputID      = "44444444-4444-4444-4444-444444444444"
	deploymentID = "55555555-5555-5555-5555-555555555555"
	configID     = "66666666-6666-6666-6666-666666666666"
	credID       = "77777777-7777-7777-7777-777777777777"
	processID    = "88888888-8888-8888-8888-888888888888"
	batchID      = "99999999-9999-9999-9999-999999999999"
)

// fakeAPI is a minimal airavata-server: one cluster, one application deployed on it, one login config.
type fakeAPI struct {
	mu          sync.Mutex
	creates     int
	launches    int
	failLaunch  int
	lastProcess map[string]any
}

func (f *fakeAPI) handler() http.Handler {
	mux := http.NewServeMux()
	j := func(w http.ResponseWriter, status int, v any) {
		w.Header().Set("Content-Type", "application/json")
		w.WriteHeader(status)
		_ = json.NewEncoder(w).Encode(v)
	}
	cluster := map[string]any{"slurmClusterId": clusterID, "clusterName": "expanse", "headnodeHost": "login.expanse", "headnodePort": 22,
		"partitions": []any{map[string]any{"partitionId": partitionID, "name": "gpu", "maxRunTime": 600, "maxNodes": 4,
			"cpuPerNode": 64, "gres": "gpu:a100:4"}}}
	template := map[string]any{"templateId": templateID, "templateName": "gromacs",
		"inputs":  []any{map[string]any{"inputId": inputID, "inputName": "steps", "inputType": "INTEGER", "required": true}},
		"outputs": []any{}}
	deployment := map[string]any{"deploymentId": deploymentID, "templateId": templateID, "slurmClusterId": clusterID,
		"slurmRunSection": "gmx mdrun -nsteps {{ inputs.steps[0] }}", "defaultPartition": "gpu",
		"defaultBatchJobConfig": map[string]any{"batchJobConfigId": batchID, "wallTimeMinutes": 60, "allocation": "abc123", "nodes": 1, "cpus": 8, "gpus": 1}}
	config := map[string]any{"slurmClusterConfigId": configID, "slurmClusterId": clusterID, "loginUser": "alice", "workRoot": "/scratch/alice",
		"sshCredentialId": credID, "sshCredential": map[string]any{"sshCredentialId": credID, "sshCredentialName": "laptop", "publicKey": "ssh-ed25519 AAAA", "fullInteractive": false, "ownerId": "cilogon:alice"}}

	mux.HandleFunc("GET /api/v1/slurm-clusters", func(w http.ResponseWriter, _ *http.Request) { j(w, 200, []any{cluster}) })
	mux.HandleFunc("GET /api/v1/slurm-clusters/{id}", func(w http.ResponseWriter, r *http.Request) {
		if r.PathValue("id") != clusterID {
			j(w, 404, map[string]any{"status": 404, "error": "Not Found", "message": "no such cluster"})
			return
		}
		j(w, 200, cluster)
	})
	mux.HandleFunc("GET /api/v1/application-templates", func(w http.ResponseWriter, _ *http.Request) { j(w, 200, []any{template}) })
	mux.HandleFunc("GET /api/v1/application-templates/{id}", func(w http.ResponseWriter, _ *http.Request) { j(w, 200, template) })
	mux.HandleFunc("GET /api/v1/slurm-deployments", func(w http.ResponseWriter, _ *http.Request) { j(w, 200, []any{deployment}) })
	mux.HandleFunc("GET /api/v1/slurm-deployments/{id}", func(w http.ResponseWriter, _ *http.Request) { j(w, 200, deployment) })
	mux.HandleFunc("GET /api/v1/slurm-cluster-configs/me", func(w http.ResponseWriter, _ *http.Request) { j(w, 200, []any{config}) })
	mux.HandleFunc("GET /api/v1/slurm-cluster-configs/shared-with-me", func(w http.ResponseWriter, _ *http.Request) { j(w, 200, []any{}) })
	mux.HandleFunc("GET /api/v1/slurm-cluster-configs/{id}", func(w http.ResponseWriter, _ *http.Request) { j(w, 200, config) })
	mux.HandleFunc("GET /api/v1/ssh-interactive-auths", func(w http.ResponseWriter, _ *http.Request) { j(w, 200, []any{}) })
	mux.HandleFunc("POST /api/v1/processes", func(w http.ResponseWriter, r *http.Request) {
		f.mu.Lock()
		defer f.mu.Unlock()
		f.creates++
		f.lastProcess = map[string]any{}
		_ = json.NewDecoder(r.Body).Decode(&f.lastProcess)
		j(w, 201, map[string]any{"processId": processID})
	})
	mux.HandleFunc("POST /api/v1/processes/{id}/launch", func(w http.ResponseWriter, _ *http.Request) {
		f.mu.Lock()
		defer f.mu.Unlock()
		f.launches++
		if f.failLaunch > 0 {
			f.failLaunch--
			j(w, 500, map[string]any{"status": 500, "error": "Internal Server Error", "message": "Internal server error"})
			return
		}
		j(w, 200, map[string]any{"processId": processID})
	})
	return mux
}

type harness struct {
	t    *testing.T
	api  *fakeAPI
	sess *mcp.ClientSession
}

type elicitFunc func(context.Context, *mcp.ElicitRequest) (*mcp.ElicitResult, error)

func accept(extra map[string]any) elicitFunc {
	return func(_ context.Context, req *mcp.ElicitRequest) (*mcp.ElicitResult, error) {
		content := map[string]any{"confirm": true}
		for k, v := range extra {
			content[k] = v
		}
		return &mcp.ElicitResult{Action: "accept", Content: content}, nil
	}
}

func newHarness(t *testing.T, pol *policy.Policy, elicit elicitFunc) *harness {
	t.Helper()
	api := &fakeAPI{}
	srv := httptest.NewServer(api.handler())
	t.Cleanup(srv.Close)
	s, watcher, err := New(Options{Version: "test", APIURL: srv.URL, Policy: pol, Transport: "stdio", Audit: &bytes.Buffer{},
		Token:     func(context.Context) (string, error) { return "token", nil },
		Principal: func(context.Context) (string, error) { return "cilogon:alice", nil }})
	if err != nil {
		t.Fatal(err)
	}
	t.Cleanup(watcher.Close)
	var opts *mcp.ClientOptions
	if elicit != nil {
		opts = &mcp.ClientOptions{ElicitationHandler: elicit}
	}
	c := mcp.NewClient(&mcp.Implementation{Name: "test-client", Version: "v0"}, opts)
	ct, st := mcp.NewInMemoryTransports()
	ctx := context.Background()
	if _, err := s.Connect(ctx, st, nil); err != nil {
		t.Fatal(err)
	}
	sess, err := c.Connect(ctx, ct, nil)
	if err != nil {
		t.Fatal(err)
	}
	t.Cleanup(func() { _ = sess.Close() })
	return &harness{t: t, api: api, sess: sess}
}

func writePolicy() *policy.Policy {
	return &policy.Policy{AllowWrite: true, Toolsets: policy.DefaultToolsets}
}

func (h *harness) call(name string, args map[string]any) (*mcp.CallToolResult, error) {
	return h.sess.CallTool(context.Background(), &mcp.CallToolParams{Name: name, Arguments: args})
}

func (h *harness) mustCall(name string, args map[string]any) map[string]any {
	h.t.Helper()
	res, err := h.call(name, args)
	if err != nil {
		h.t.Fatalf("%s: %v", name, err)
	}
	if res.IsError {
		h.t.Fatalf("%s returned a tool error: %s", name, text(res))
	}
	var out map[string]any
	b, _ := json.Marshal(res.StructuredContent)
	_ = json.Unmarshal(b, &out)
	return out
}

func text(res *mcp.CallToolResult) string {
	var parts []string
	for _, c := range res.Content {
		if t, ok := c.(*mcp.TextContent); ok {
			parts = append(parts, t.Text)
		}
	}
	return strings.Join(parts, " ")
}

func toolNames(t *testing.T, sess *mcp.ClientSession) []string {
	t.Helper()
	res, err := sess.ListTools(context.Background(), nil)
	if err != nil {
		t.Fatal(err)
	}
	var names []string
	for _, tool := range res.Tools {
		names = append(names, tool.Name)
	}
	return names
}

func TestToolsets(t *testing.T) {
	readOnly := newHarness(t, &policy.Policy{Toolsets: policy.DefaultToolsets}, nil)
	names := toolNames(t, readOnly.sess)
	if len(names) != 15 {
		t.Errorf("read-only default toolsets list %d tools, want 15: %v", len(names), names)
	}
	for _, n := range names {
		if n == "launch_process" || n == "register_dataset" || n == "register_cluster_config" {
			t.Errorf("write tool %s listed without --allow-write", n)
		}
	}

	write := newHarness(t, writePolicy(), nil)
	if n := len(toolNames(t, write.sess)); n != 18 {
		t.Errorf("default toolsets with --allow-write list %d tools, want 18", n)
	}

	denied := writePolicy()
	denied.File.Deny = []string{"launch_process"}
	for _, n := range toolNames(t, newHarness(t, denied, nil).sess) {
		if n == "launch_process" {
			t.Error("a denied tool is listed")
		}
	}

	admin := writePolicy()
	admin.Admin = true
	admin.Toolsets = append(admin.Toolsets, policy.Sharing)
	if n := len(toolNames(t, newHarness(t, admin, nil).sess)); n != 25 {
		t.Errorf("all toolsets list %d tools, want 25", n)
	}
}

func TestAnnotations(t *testing.T) {
	h := newHarness(t, writePolicy(), nil)
	res, err := h.sess.ListTools(context.Background(), nil)
	if err != nil {
		t.Fatal(err)
	}
	for _, tool := range res.Tools {
		if tool.Annotations == nil || tool.Title == "" {
			t.Errorf("%s has no title or annotations", tool.Name)
			continue
		}
		write := tool.Name == "launch_process" || strings.HasPrefix(tool.Name, "register_")
		if tool.Annotations.ReadOnlyHint == write {
			t.Errorf("%s readOnlyHint = %t", tool.Name, tool.Annotations.ReadOnlyHint)
		}
	}
}

func plan(t *testing.T, h *harness) string {
	t.Helper()
	out := h.mustCall("plan_run", map[string]any{"application_id": templateID, "inputs": map[string]any{"steps": "5000"}})
	handle, _ := out["proposal_handle"].(string)
	if handle == "" {
		t.Fatalf("plan_run returned no handle: %v", out)
	}
	p := out["plan"].(map[string]any)
	if p["partition"] != "gpu" || p["cluster"] != "expanse" {
		t.Errorf("plan targets %v/%v", p["cluster"], p["partition"])
	}
	hours := p["hours"].(map[string]any)
	if hours["requested_core_hours"] != 8.0 || hours["requested_gpu_hours"] != 1.0 {
		t.Errorf("hours = %v, want 8 core-hours and 1 GPU-hour", hours)
	}
	return handle
}

func TestPlanAndLaunchOnce(t *testing.T) {
	h := newHarness(t, writePolicy(), accept(nil))
	handle := plan(t, h)
	out := h.mustCall("launch_process", map[string]any{"proposal_handle": handle})
	if out["process_id"] != processID {
		t.Errorf("launch returned %v", out)
	}
	if h.api.creates != 1 || h.api.launches != 1 {
		t.Errorf("creates=%d launches=%d, want 1 and 1", h.api.creates, h.api.launches)
	}
	mappings := h.api.lastProcess["batchProcess"].(map[string]any)["inputMappings"].([]any)
	if v := mappings[0].(map[string]any)["value"]; v != `{"value":"5000"}` {
		t.Errorf("input mapping value = %v", v)
	}

	res, err := h.call("launch_process", map[string]any{"proposal_handle": handle})
	if err != nil {
		t.Fatal(err)
	}
	if !res.IsError || !strings.Contains(text(res), "already carried out") {
		t.Errorf("second launch with the same handle: %s", text(res))
	}
	if h.api.creates != 1 {
		t.Errorf("a replayed launch created another process")
	}
}

func TestLaunchDeclined(t *testing.T) {
	h := newHarness(t, writePolicy(), func(context.Context, *mcp.ElicitRequest) (*mcp.ElicitResult, error) {
		return &mcp.ElicitResult{Action: "decline"}, nil
	})
	handle := plan(t, h)
	res, err := h.call("launch_process", map[string]any{"proposal_handle": handle})
	if err != nil {
		t.Fatal(err)
	}
	if !res.IsError || !strings.Contains(text(res), "declined") {
		t.Errorf("declined launch returned %s", text(res))
	}
	if h.api.creates != 0 {
		t.Error("a declined launch created a process")
	}
}

func TestLaunchUncheckedBoxIsDecline(t *testing.T) {
	h := newHarness(t, writePolicy(), func(context.Context, *mcp.ElicitRequest) (*mcp.ElicitResult, error) {
		return &mcp.ElicitResult{Action: "accept", Content: map[string]any{"confirm": false}}, nil
	})
	res, err := h.call("launch_process", map[string]any{"proposal_handle": plan(t, h)})
	if err != nil {
		t.Fatal(err)
	}
	if !res.IsError || h.api.creates != 0 {
		t.Errorf("accept without confirm=true launched: %s", text(res))
	}
}

func TestLaunchWithoutElicitationFailsClosed(t *testing.T) {
	h := newHarness(t, writePolicy(), nil)
	handle := plan(t, h)
	res, err := h.call("launch_process", map[string]any{"proposal_handle": handle})
	var wire *jsonrpc.Error
	switch {
	case errors.As(err, &wire):
		if wire.Code != mcp.CodeMissingRequiredClientCapabilities {
			t.Errorf("error code %d, want %d", wire.Code, mcp.CodeMissingRequiredClientCapabilities)
		}
	case err != nil:
		t.Fatalf("unexpected error: %v", err)
	case !res.IsError:
		t.Error("a write ran without elicitation support")
	}
	if h.api.creates != 0 {
		t.Error("a process was created without confirmation")
	}
}

func TestEditedWalltimeIsRevalidated(t *testing.T) {
	pol := writePolicy()
	pol.File.Limits.MaxWalltimeMinutes = 120
	h := newHarness(t, pol, accept(map[string]any{"wall_time_minutes": 300}))
	handle := plan(t, h)
	res, err := h.call("launch_process", map[string]any{"proposal_handle": handle})
	if err != nil {
		t.Fatal(err)
	}
	if !res.IsError || !strings.Contains(text(res), "exceeds the local cap") {
		t.Errorf("an edited walltime above the local cap was accepted: %s", text(res))
	}
	if h.api.creates != 0 {
		t.Error("an invalid edited walltime created a process")
	}
}

func TestLaunchRetryReusesProcess(t *testing.T) {
	h := newHarness(t, writePolicy(), accept(nil))
	h.api.failLaunch = 1
	handle := plan(t, h)
	res, err := h.call("launch_process", map[string]any{"proposal_handle": handle})
	if err != nil {
		t.Fatal(err)
	}
	if !res.IsError {
		t.Fatal("the failed launch reported success")
	}
	h.mustCall("launch_process", map[string]any{"proposal_handle": handle})
	if h.api.creates != 1 || h.api.launches != 2 {
		t.Errorf("creates=%d launches=%d, want the retry to launch the same process", h.api.creates, h.api.launches)
	}
}

func TestLocalCapsBlockPlan(t *testing.T) {
	pol := writePolicy()
	pol.File.Limits.MaxGPUHoursPerLaunch = 0.5
	h := newHarness(t, pol, accept(nil))
	res, err := h.call("plan_run", map[string]any{"application_id": templateID, "inputs": map[string]any{"steps": "5000"}})
	if err != nil {
		t.Fatal(err)
	}
	if !res.IsError || !strings.Contains(text(res), "GPU-hours exceed the local cap") {
		t.Errorf("plan over the GPU-hour cap: %s", text(res))
	}
}

func TestPlanValidatesInputs(t *testing.T) {
	h := newHarness(t, writePolicy(), accept(nil))
	for _, tc := range []struct {
		inputs map[string]any
		want   string
	}{
		{map[string]any{}, `input "steps" (INTEGER) is required`},
		{map[string]any{"steps": "many"}, "must be an integer"},
		{map[string]any{"steps": "1", "bogus": "x"}, `no input "bogus"`},
	} {
		res, err := h.call("plan_run", map[string]any{"application_id": templateID, "inputs": tc.inputs})
		if err != nil {
			t.Fatal(err)
		}
		if !res.IsError || !strings.Contains(text(res), tc.want) {
			t.Errorf("inputs %v: got %q, want it to mention %q", tc.inputs, text(res), tc.want)
		}
	}
}

func TestPolicyConfirmListGatesReadTool(t *testing.T) {
	pol := writePolicy()
	pol.File.Confirm = []string{"list_clusters"}
	asked := 0
	h := newHarness(t, pol, func(ctx context.Context, req *mcp.ElicitRequest) (*mcp.ElicitResult, error) {
		asked++
		return accept(nil)(ctx, req)
	})
	out := h.mustCall("list_clusters", nil)
	if asked != 1 || len(out["clusters"].([]any)) != 1 {
		t.Errorf("asked=%d out=%v", asked, out)
	}
}

func TestConvertScriptPreview(t *testing.T) {
	h := newHarness(t, writePolicy(), nil)
	script := "#!/bin/bash\n#SBATCH -J md\n#SBATCH -p gpu\n#SBATCH -t 02:00:00\n#SBATCH -A abc123\n#SBATCH -N 1\n#SBATCH --qos=high\nmodule load gromacs/2024\ngmx mdrun -s topol.tpr\n"
	out := h.mustCall("convert_sbatch_script", map[string]any{"script": script, "cluster_id": clusterID,
		"inputs": []any{map[string]any{"name": "tpr", "type": "FILE", "literal": "topol.tpr"}}})
	if out["registrable"] != false || out["proposal_handle"] != nil {
		t.Errorf("a script with --qos was offered for registration: %v", out["problems"])
	}
	if out["round_trip_diff"] == "" {
		t.Error("no round-trip diff")
	}

	clean := strings.Replace(script, "#SBATCH --qos=high\n", "", 1)
	out = h.mustCall("convert_sbatch_script", map[string]any{"script": clean, "cluster_id": clusterID,
		"inputs": []any{map[string]any{"name": "tpr", "type": "FILE", "literal": "topol.tpr"}}})
	if out["proposal_handle"] == nil {
		t.Errorf("a registrable script got no handle: %v", out["proposal"])
	}
	if !strings.Contains(out["run_section"].(string), "{{ inputs.tpr[0] }}") {
		t.Errorf("run section %q does not reference the input", out["run_section"])
	}
}

func TestResourcesAndPrompts(t *testing.T) {
	h := newHarness(t, writePolicy(), nil)
	ctx := context.Background()
	res, err := h.sess.ReadResource(ctx, &mcp.ReadResourceParams{URI: "airavata://clusters/" + clusterID})
	if err != nil {
		t.Fatal(err)
	}
	if !strings.Contains(res.Contents[0].Text, "expanse") || res.CacheScope != "private" {
		t.Errorf("cluster resource: scope %q", res.CacheScope)
	}
	if _, err := h.sess.ReadResource(ctx, &mcp.ReadResourceParams{URI: "airavata://clusters/00000000-0000-0000-0000-000000000000"}); err == nil {
		t.Error("an unknown cluster resolved")
	}
	prompts, err := h.sess.ListPrompts(ctx, nil)
	if err != nil {
		t.Fatal(err)
	}
	if len(prompts.Prompts) != 4 {
		t.Errorf("%d prompts, want 4", len(prompts.Prompts))
	}
	comp, err := h.sess.Complete(ctx, &mcp.CompleteParams{Ref: &mcp.CompleteReference{Type: "ref/resource", URI: "airavata://clusters/{clusterId}"},
		Argument: mcp.CompleteParamsArgument{Name: "clusterId", Value: "exp"}})
	if err != nil {
		t.Fatal(err)
	}
	if len(comp.Completion.Values) != 1 || comp.Completion.Values[0] != clusterID {
		t.Errorf("completion = %v", comp.Completion.Values)
	}
}
