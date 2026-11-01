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

package estimate

import (
	"testing"

	"github.com/apache/airavata/internal/mcp/client"
)

func i32(v int32) *int32 { return &v }
func i64(v int64) *int64 { return &v }
func s(v string) *string { return &v }

func TestRequestedHours(t *testing.T) {
	for _, tc := range []struct {
		name       string
		r          Request
		core, gpus float64
	}{
		{"one core", Request{WallTimeMinutes: 60}, 1, 0},
		{"cpus per task", Request{WallTimeMinutes: 120, Cpus: i32(8)}, 16, 0},
		{"tasks per node", Request{WallTimeMinutes: 60, Nodes: i32(2), NtasksPerNode: i32(4), CpusPerTask: i32(2)}, 16, 0},
		{"gpus", Request{WallTimeMinutes: 30, Gpus: i32(4)}, 0.5, 2},
		{"gres per node", Request{WallTimeMinutes: 60, Nodes: i32(2), Gres: s("gpu:a100:4")}, 2, 8},
	} {
		h := tc.r.Hours()
		if h.CoreHours != tc.core || h.GPUHours != tc.gpus {
			t.Errorf("%s: %v core-hours, %v GPU-hours; want %v and %v", tc.name, h.CoreHours, h.GPUHours, tc.core, tc.gpus)
		}
	}
}

func TestMemoryMB(t *testing.T) {
	for in, want := range map[string]int64{"4000": 4000, "4G": 4096, "512M": 512, "1T": 1048576, "2048K": 2} {
		if got, err := MemoryMB(in); err != nil || got != want {
			t.Errorf("MemoryMB(%q) = %d, %v; want %d", in, got, err, want)
		}
	}
	if _, err := MemoryMB("lots"); err == nil {
		t.Error("an unparseable size was accepted")
	}
}

func TestPartitionFit(t *testing.T) {
	p := &client.ClusterPartitionResponse{Name: "gpu", MaxRunTime: i32(600), MaxNodes: i32(2), CpuPerNode: i32(64),
		MaxMemory: i64(256000), Gres: s("gpu:a100:4")}
	if f := Check(Request{WallTimeMinutes: 60, Nodes: i32(1), Gpus: i32(4), Mem: s("200G")}, p); !f.Fits {
		t.Errorf("a fitting request was refused: %v", f.Violations)
	}
	f := Check(Request{WallTimeMinutes: 700, Nodes: i32(3), Cpus: i32(128), Gpus: i32(16), Mem: s("300G")}, p)
	if f.Fits || len(f.Violations) != 5 {
		t.Errorf("violations = %v, want walltime, nodes, CPUs per node, memory and GPUs", f.Violations)
	}
	cpu := &client.ClusterPartitionResponse{Name: "cpu", MaxRunTime: i32(60)}
	if f := Check(Request{WallTimeMinutes: 10, Gpus: i32(1)}, cpu); f.Fits {
		t.Error("GPUs were accepted on a partition without GPU gres")
	}
}

func TestOverlayKeepsUnsetFields(t *testing.T) {
	base := Request{WallTimeMinutes: 60, Allocation: "abc", Cpus: i32(8)}
	got := base.Overlay(Request{WallTimeMinutes: 120, Gpus: i32(1)})
	if got.WallTimeMinutes != 120 || got.Allocation != "abc" || *got.Cpus != 8 || *got.Gpus != 1 {
		t.Errorf("Overlay = %+v", got)
	}
}
