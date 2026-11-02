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

package service

import (
	"testing"
)

// Trimmed from a real `scontrol show partition -o` on a Slurm 23 cluster.
const scontrolOut = `PartitionName=cpu AllowGroups=ALL AllowAccounts=ALL AllowQos=ALL AllocNodes=ALL Default=YES QoS=N/A DefaultTime=01:00:00 DisableRootJobs=NO ExclusiveUser=NO GraceTime=0 Hidden=NO MaxNodes=UNLIMITED MaxTime=2-00:00:00 MinNodes=0 LLN=NO MaxCPUsPerNode=UNLIMITED Nodes=cn[001-128] PriorityJobFactor=1 PriorityTier=1 RootOnly=NO ReqResv=NO OverSubscribe=NO OverTimeLimit=NONE PreemptMode=OFF State=UP TotalCPUs=16384 TotalNodes=128 SelectTypeParameters=NONE JobDefaults=(null) DefMemPerCPU=1000 MaxMemPerNode=UNLIMITED TRES=cpu=16384,mem=32000000M,node=128,billing=16384
PartitionName=gpu AllowGroups=ALL Default=NO DefaultTime=NONE MaxNodes=4 MaxTime=12:00:00 Nodes=gpu[01-08] State=UP TotalCPUs=512 TotalNodes=8 MaxMemPerNode=512000 TRES=cpu=512,mem=4000000M,node=8,gres/gpu=32
PartitionName=debug Default=NO DefaultTime=15 MaxNodes=2 MaxTime=30 Nodes=(null) State=UP TotalCPUs=0 TotalNodes=0
`

const sinfoOut = `cpu|128|250000|(null)
cpu|128+|500000|(null)
gpu|64|512000|gpu:a100:4(S:0-1)
gpu|64|512000|gpu:h100:4(S:0-1)
unknown|1|1|(null)
`

func TestParsesScontrolAndSinfo(t *testing.T) {
	parts := parseScontrolPartitions(scontrolOut)
	applySinfo(parts, sinfoOut)
	if len(parts) != 3 {
		t.Fatalf("got %d partitions, want 3", len(parts))
	}

	cpu := parts[0].request()
	if cpu.Name != "cpu" || !*cpu.IsDefaultQueue {
		t.Errorf("cpu = %+v, want the default partition named cpu", cpu)
	}
	if *cpu.MaxRunTime != 2*24*60 || *cpu.DefaultWalltime != 60 {
		t.Errorf("cpu times = %d max, %d default; want 2880 and 60", *cpu.MaxRunTime, *cpu.DefaultWalltime)
	}
	// MaxNodes=UNLIMITED falls back to the partition's size.
	if *cpu.MaxNodes != 128 || *cpu.MaxProcessors != 16384 || *cpu.CPUPerNode != 128 {
		t.Errorf("cpu sizes = nodes %d, procs %d, cpu/node %d", *cpu.MaxNodes, *cpu.MaxProcessors, *cpu.CPUPerNode)
	}
	// No memory limit, so the largest node is reported.
	if *cpu.MaxMemory != 500000 || cpu.Gres != nil || *cpu.Nodes != "cn[001-128]" {
		t.Errorf("cpu mem %d, gres %v, nodes %v", *cpu.MaxMemory, cpu.Gres, cpu.Nodes)
	}

	gpu := parts[1].request()
	if *gpu.IsDefaultQueue || *gpu.MaxRunTime != 720 || gpu.DefaultWalltime != nil || *gpu.MaxNodes != 4 {
		t.Errorf("gpu = %+v", gpu)
	}
	if *gpu.Gres != "gpu:a100:4,gpu:h100:4" || *gpu.MaxMemory != 512000 {
		t.Errorf("gpu gres %q mem %d", *gpu.Gres, *gpu.MaxMemory)
	}

	debug := parts[2].request()
	if *debug.MaxRunTime != 30 || *debug.DefaultWalltime != 15 || debug.Nodes != nil || debug.CPUPerNode != nil {
		t.Errorf("debug = %+v", debug)
	}
}

func TestCPUPerNodeWithoutSinfo(t *testing.T) {
	parts := parseScontrolPartitions("PartitionName=p TotalCPUs=256 TotalNodes=4\n")
	if got := parts[0].request().CPUPerNode; got == nil || *got != 64 {
		t.Errorf("cpuPerNode = %v, want 64 from the partition totals", got)
	}
}

func TestSlurmMinutes(t *testing.T) {
	for in, want := range map[string]int64{
		"30": 30, "30:15": 30, "01:30:00": 90, "1-00": 1440, "1-02:30": 1590, "2-00:00:00": 2880,
	} {
		if got, ok := slurmMinutes(in); !ok || got != want {
			t.Errorf("slurmMinutes(%q) = %d, %v; want %d", in, got, ok, want)
		}
	}
	for _, in := range []string{"UNLIMITED", "NONE", "INFINITE", ""} {
		if _, ok := slurmMinutes(in); ok {
			t.Errorf("slurmMinutes(%q) parsed, want no duration", in)
		}
	}
}

func TestParsesAssociations(t *testing.T) {
	got := parseAssociations(`tg-bio210001|gpu|normal,gpu-shared
tg-bio210001|gpu-debug|normal
tg-che220002||normal
tg-che220002|compute|high
|||
`)
	if len(got) != 2 {
		t.Fatalf("got %d allocations, want 2: %+v", len(got), got)
	}
	bio, che := got[0], got[1]
	if bio.Account != "tg-bio210001" || len(bio.Partitions) != 2 || bio.Partitions[1] != "gpu-debug" ||
		len(bio.QOS) != 2 || bio.QOS[1] != "gpu-shared" {
		t.Errorf("bio = %+v", bio)
	}
	// An association with no partition opens every partition.
	if che.Account != "tg-che220002" || len(che.Partitions) != 0 || len(che.QOS) != 2 {
		t.Errorf("che = %+v, want no partition limit", che)
	}

	applyDescriptions(got, "TG-BIO210001|Protein folding\ntg-che220002|tg-che220002\n")
	if got[0].Description == nil || *got[0].Description != "Protein folding" {
		t.Errorf("bio description = %v", got[0].Description)
	}
	if got[1].Description != nil {
		t.Errorf("a description repeating the name should be dropped, got %q", *got[1].Description)
	}
}

func TestParsesShareAccounts(t *testing.T) {
	got := parseShareAccounts("abc123\nabc123\nxyz789\n\n")
	if len(got) != 2 || got[0].Account != "abc123" || got[1].Account != "xyz789" {
		t.Errorf("got %+v", got)
	}
}
