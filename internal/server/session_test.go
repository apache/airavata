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
	"net/http"
	"slices"
	"sync"
	"testing"

	model "github.com/apache/airavata/api/process/model"
)

// seedSessionDeps registers a cluster config owned by the caller of token and a
// deployment on its cluster, and returns both ids.
func (h *harness) seedSessionDeps(name, token string) (configID, deploymentID string) {
	h.t.Helper()
	configID, clusterID := h.seedClusterConfig(token, name)
	return configID, h.seedDeployment(name, clusterID)
}

// seedDeployment registers a deployment of a fresh template; a blank clusterID leaves
// it with nowhere to launch.
func (h *harness) seedDeployment(name, clusterID string) string {
	h.t.Helper()
	tmpl := h.mustDo(http.MethodPost, "/api/v1/application-templates", tokenAdmin,
		map[string]any{"templateName": name}, http.StatusCreated)
	deployment := h.mustDo(http.MethodPost, "/api/v1/slurm-deployments", tokenAdmin, map[string]any{
		"templateId": tmpl["templateId"], "slurmRunSection": "run", "slurmClusterId": clusterID,
		"defaultBatchJobConfig": map[string]any{"wallTimeMinutes": 60, "allocation": "DEFAULT"},
	}, http.StatusCreated)
	return deployment["deploymentId"].(string)
}

func sessionBody(configID, deploymentID string) map[string]any {
	return map[string]any{
		"slurmClusterConfigId": configID, "deploymentId": deploymentID, "account": "ALLOC", "partition": "batch",
		"resources": map[string]any{"cores": 4, "memoryMb": 8192, "wallMinutes": 60},
	}
}

func runsOf(session map[string]any) []map[string]any {
	var out []map[string]any
	for _, run := range session["runs"].([]any) {
		out = append(out, run.(map[string]any))
	}
	return out
}

// stoppedSession creates a session for alice and stops its first run.
func (h *harness) stoppedSession(name string) (base string) {
	h.t.Helper()
	configID, deploymentID := h.seedSessionDeps(name, tokenAlice)
	created := h.mustDo(http.MethodPost, "/api/v1/sessions", tokenAlice, sessionBody(configID, deploymentID), http.StatusCreated)
	h.mustDo(http.MethodPost, "/api/v1/processes/"+runsOf(created)[0]["processId"].(string)+"/stop", tokenAlice, nil, http.StatusOK)
	return "/api/v1/sessions/" + created["sessionId"].(string)
}

func TestSessionLifecycle(t *testing.T) {
	h := newHarness(t)
	configID, deploymentID := h.seedSessionDeps("lifecycle", tokenAlice)

	created := h.mustDo(http.MethodPost, "/api/v1/sessions", tokenAlice, sessionBody(configID, deploymentID), http.StatusCreated)
	if created["state"] != "SUBMITTED" || created["live"] != true || len(runsOf(created)) != 1 {
		t.Fatalf("created = %v", created)
	}
	base := "/api/v1/sessions/" + created["sessionId"].(string)
	if mine := h.list("/api/v1/sessions/me", tokenAlice); len(mine) != 1 || mine[0]["sessionId"] != created["sessionId"] {
		t.Fatalf("/sessions/me = %v", mine)
	}

	h.mustDo(http.MethodPost, base+"/start", tokenAlice, map[string]any{}, http.StatusConflict)
	h.mustDo(http.MethodPost, base+"/start", tokenAlice, map[string]any{"wallMinutes": 0}, http.StatusBadRequest)

	first := runsOf(created)[0]["processId"].(string)
	stopped := h.mustDo(http.MethodPost, "/api/v1/processes/"+first+"/stop", tokenAlice, nil, http.StatusOK)
	if stopped["lastStatus"].(map[string]any)["status"] != "CANCELLED" {
		t.Fatalf("stopped = %v", stopped["lastStatus"])
	}

	started := h.mustDo(http.MethodPost, base+"/start", tokenAlice, map[string]any{"wallMinutes": 30}, http.StatusOK)
	runs := runsOf(started)
	if len(runs) != 2 || runs[0]["processId"] == first || runs[0]["state"] != "SUBMITTED" || runs[1]["state"] != "CANCELLED" {
		t.Fatalf("runs after start = %v", runs)
	}
	if res := started["resources"].(map[string]any); res["wallMinutes"] != float64(30) || res["cores"] != float64(4) {
		t.Errorf("resources after start = %v, want wallMinutes overridden and cores kept", res)
	}

	h.mustDo(http.MethodDelete, base, tokenAlice, nil, http.StatusConflict)
	h.mustDo(http.MethodPost, "/api/v1/processes/"+runs[0]["processId"].(string)+"/stop", tokenAlice, nil, http.StatusOK)
	h.mustDo(http.MethodDelete, base, tokenAlice, nil, http.StatusNoContent)
	h.mustDo(http.MethodGet, base, tokenAlice, nil, http.StatusNotFound)
	for _, run := range runs {
		h.mustDo(http.MethodGet, "/api/v1/processes/"+run["processId"].(string), tokenAdmin, nil, http.StatusNotFound)
	}
}

func TestTwoStartsAtOnceMakeOneRun(t *testing.T) {
	h := newHarness(t)
	base := h.stoppedSession("race")

	codes := make([]int, 2)
	var wg sync.WaitGroup
	for i := range codes {
		wg.Go(func() { codes[i] = h.do(http.MethodPost, base+"/start", tokenAlice, nil).Code })
	}
	wg.Wait()
	slices.Sort(codes)
	if !slices.Equal(codes, []int{http.StatusOK, http.StatusConflict}) {
		t.Fatalf("start codes = %v, want one 200 and one 409", codes)
	}
	got := h.mustDo(http.MethodGet, base, tokenAlice, nil, http.StatusOK)
	if runs := runsOf(got); len(runs) != 2 {
		t.Fatalf("runs = %d, want 2", len(runs))
	}
	if wall := got["resources"].(map[string]any)["wallMinutes"]; wall != float64(60) {
		t.Errorf("wallMinutes after a body-less start = %v, want 60 kept", wall)
	}
}

func TestRunsStayInCreationOrder(t *testing.T) {
	h := newHarness(t)
	configID, deploymentID := h.seedSessionDeps("ordered", tokenAlice)
	created := h.mustDo(http.MethodPost, "/api/v1/sessions", tokenAlice, sessionBody(configID, deploymentID), http.StatusCreated)
	base, first := "/api/v1/sessions/"+created["sessionId"].(string), runsOf(created)[0]["processId"].(string)
	if _, err := h.svcs.ProcessStatus.Record(h.t.Context(), first, model.ProcessStatusTypeCompleted, nil); err != nil {
		t.Fatal(err)
	}
	current := runsOf(h.mustDo(http.MethodPost, base+"/start", tokenAlice, nil, http.StatusOK))[0]["processId"]

	if _, err := h.svcs.ProcessStatus.Record(h.t.Context(), first, model.ProcessStatusTypeFailed, nil); err != nil {
		t.Fatal(err)
	}
	got := h.mustDo(http.MethodGet, base, tokenAlice, nil, http.StatusOK)
	if runs := runsOf(got); runs[0]["processId"] != current || runs[1]["processId"] != first || got["state"] != "SUBMITTED" {
		t.Fatalf("after a late status on the first run: state %v, runs %v", got["state"], runs)
	}
}

func TestSessionsAreOwnerScoped(t *testing.T) {
	h := newHarness(t)
	base := h.stoppedSession("scoped")
	unknown := "/api/v1/sessions/nope"

	h.mustDo(http.MethodGet, "/api/v1/sessions/me", "", nil, http.StatusUnauthorized)
	h.mustDo(http.MethodPost, "/api/v1/sessions", "", sessionBody("c", "d"), http.StatusUnauthorized)
	for _, tc := range []struct {
		method, path string
		body         any
	}{
		{http.MethodGet, "", nil},
		{http.MethodPost, "/start", map[string]any{}},
		{http.MethodDelete, "", nil},
	} {
		if rec := h.do(tc.method, base+tc.path, "", tc.body); rec.Code != http.StatusUnauthorized {
			t.Errorf("anonymous %s %s: status = %d, want 401", tc.method, tc.path, rec.Code)
		}
		if rec := h.do(tc.method, base+tc.path, tokenBob, tc.body); rec.Code != http.StatusForbidden {
			t.Errorf("bob %s %s: status = %d, want 403", tc.method, tc.path, rec.Code)
		}
		if rec := h.do(tc.method, unknown+tc.path, tokenAlice, tc.body); rec.Code != http.StatusNotFound {
			t.Errorf("unknown %s %s: status = %d, want 404", tc.method, tc.path, rec.Code)
		}
	}
	if theirs := h.list("/api/v1/sessions/me", tokenBob); len(theirs) != 0 {
		t.Errorf("bob's /sessions/me = %v, want empty", theirs)
	}

	h.mustDo(http.MethodGet, base, tokenAdmin, nil, http.StatusOK)
	h.mustDo(http.MethodPost, base+"/start", tokenAdmin, map[string]any{}, http.StatusForbidden)
	h.mustDo(http.MethodDelete, base, tokenAdmin, nil, http.StatusNoContent)
}

func TestAFailedLaunchIsAnsweredInTheRun(t *testing.T) {
	h := newHarness(t)
	configID, _ := h.seedClusterConfig(tokenAlice, "unplaced")
	deploymentID := h.seedDeployment("unplaced", "")

	created := h.mustDo(http.MethodPost, "/api/v1/sessions", tokenAlice, sessionBody(configID, deploymentID), http.StatusCreated)
	if runs := runsOf(created); created["live"] != false || len(runs) != 1 || runs[0]["state"] != "FAILED" || runs[0]["error"] == nil {
		t.Fatalf("created = %v, want its one run FAILED with its error", created)
	}
	base := "/api/v1/sessions/" + created["sessionId"].(string)
	started := h.mustDo(http.MethodPost, base+"/start", tokenAlice, nil, http.StatusOK)
	if runs := runsOf(started); len(runs) != 2 || runs[0]["state"] != "FAILED" || runs[0]["error"] == nil {
		t.Fatalf("runs after a start = %v, want a second FAILED run with its error", runs)
	}
	h.mustDo(http.MethodDelete, base, tokenAlice, nil, http.StatusNoContent)
}

func TestCreateValidates(t *testing.T) {
	h := newHarness(t)
	configID, deploymentID := h.seedSessionDeps("validated", tokenAlice)

	for field, edit := range map[string]func(map[string]any){
		"account":            func(b map[string]any) { b["account"] = " " },
		"resources.cores":    func(b map[string]any) { b["resources"].(map[string]any)["cores"] = 0 },
		"deploymentId":       func(b map[string]any) { b["deploymentId"] = "" },
		"resources.gpuCount": func(b map[string]any) { b["resources"].(map[string]any)["gpuCount"] = -1 },
		"resources.gpuType":  func(b map[string]any) { b["resources"].(map[string]any)["gpuType"] = "a100" },
	} {
		body := sessionBody(configID, deploymentID)
		edit(body)
		rec := h.do(http.MethodPost, "/api/v1/sessions", tokenAlice, body)
		if rec.Code != http.StatusBadRequest || firstFieldError(t, rec) != field {
			t.Errorf("%s: status = %d, body %s", field, rec.Code, rec.Body.String())
		}
	}

	bobConfig, _ := h.seedClusterConfig(tokenBob, "bobs")
	if rec := h.do(http.MethodPost, "/api/v1/sessions", tokenAlice, sessionBody(bobConfig, deploymentID)); rec.Code != http.StatusForbidden {
		t.Errorf("bob's unshared config: status = %d, want 403", rec.Code)
	}
	if mine := h.list("/api/v1/sessions/me", tokenAlice); len(mine) != 0 {
		t.Errorf("/sessions/me = %v, want empty", mine)
	}
}
