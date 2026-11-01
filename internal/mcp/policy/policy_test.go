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

package policy

import (
	"os"
	"path/filepath"
	"strings"
	"testing"
)

func writeFile(t *testing.T, body string) string {
	t.Helper()
	path := filepath.Join(t.TempDir(), "policy.yaml")
	if err := os.WriteFile(path, []byte(body), 0o600); err != nil {
		t.Fatal(err)
	}
	return path
}

func TestLoadPolicyFile(t *testing.T) {
	f, err := LoadFile(writeFile(t, "deny: [share_resource]\nconfirm: [list_data_products]\nlimits:\n  max_walltime_minutes: 2880\n  max_gpu_hours_per_launch: 64\n"), true)
	if err != nil {
		t.Fatal(err)
	}
	p := &Policy{AllowWrite: true, File: f}
	if p.Exposed("share_resource", false) {
		t.Error("a denied tool is exposed even with --allow-write")
	}
	if !p.NeedsConfirmation("list_data_products", true) || p.NeedsConfirmation("list_clusters", true) {
		t.Error("the confirm list was not applied exactly")
	}
	if got := p.CheckLimits(Estimate{WalltimeMinutes: 3000, GPUHours: 65}); len(got) != 2 {
		t.Errorf("CheckLimits = %v", got)
	}
}

func TestPolicyFileRejectsUnknownKeys(t *testing.T) {
	if _, err := LoadFile(writeFile(t, "allow: [launch_process]\n"), true); err == nil {
		t.Error("an unknown key (which might be read as a permission) was accepted")
	}
}

func TestMissingPolicyFile(t *testing.T) {
	missing := filepath.Join(t.TempDir(), "none.yaml")
	if _, err := LoadFile(missing, false); err != nil {
		t.Errorf("a missing default policy file is an error: %v", err)
	}
	if _, err := LoadFile(missing, true); err == nil {
		t.Error("a missing explicit policy file was ignored")
	}
}

func TestWritesNeedAllowWriteAndConfirmation(t *testing.T) {
	p := &Policy{}
	if p.Exposed("launch_process", false) || !p.Exposed("list_clusters", true) {
		t.Error("read-only default not applied")
	}
	if !p.NeedsConfirmation("launch_process", false) {
		t.Error("a write tool does not need confirmation")
	}
}

func TestValidate(t *testing.T) {
	for _, tc := range []struct {
		p         Policy
		transport string
		want      string
	}{
		{Policy{Toolsets: []string{"bogus"}}, "stdio", "unknown toolset"},
		{Policy{Toolsets: []string{Admin}}, "stdio", "--admin"},
		{Policy{Admin: true}, "http", "stdio"},
		{Policy{ConfirmVia: "auto"}, "stdio", "unknown --confirm"},
		{Policy{ConfirmVia: ConfirmHost, File: File{Confirm: []string{"list_data_products"}}}, "stdio", "confirm list"},
	} {
		if err := tc.p.Validate(tc.transport); err == nil || !strings.Contains(err.Error(), tc.want) {
			t.Errorf("%+v on %s: err = %v, want %q", tc.p, tc.transport, err, tc.want)
		}
	}
}
