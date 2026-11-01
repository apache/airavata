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
	"fmt"
	"math"
	"strconv"
	"strings"

	"github.com/apache/airavata/internal/mcp/client"
)

// Request is the resource request of one run, in BatchJobConfig terms.
type Request struct {
	WallTimeMinutes int64   `json:"wall_time_minutes"`
	Allocation      string  `json:"allocation,omitempty"`
	Nodes           *int32  `json:"nodes,omitempty"`
	Ntasks          *int32  `json:"ntasks,omitempty"`
	NtasksPerNode   *int32  `json:"ntasks_per_node,omitempty"`
	Cpus            *int32  `json:"cpus,omitempty"`
	CpusPerTask     *int32  `json:"cpus_per_task,omitempty"`
	Mem             *string `json:"mem,omitempty"`
	MemPerCpu       *string `json:"mem_per_cpu,omitempty"`
	Gpus            *int32  `json:"gpus,omitempty"`
	GpusPerNode     *int32  `json:"gpus_per_node,omitempty"`
	MemPerGpu       *string `json:"mem_per_gpu,omitempty"`
	CpusPerGpu      *string `json:"cpus_per_gpu,omitempty"`
	Gres            *string `json:"gres,omitempty"`
	Constraints     *string `json:"constraints,omitempty"`
}

// FromResponse copies a stored job config.
func FromResponse(c *client.BatchJobConfigResponse) Request {
	if c == nil {
		return Request{}
	}
	return Request{WallTimeMinutes: c.WallTimeMinutes, Allocation: c.Allocation, Nodes: c.Nodes, Ntasks: c.Ntasks,
		NtasksPerNode: c.NtasksPerNode, Cpus: c.Cpus, CpusPerTask: c.CpusPerTask, Mem: c.Mem, MemPerCpu: c.MemPerCpu,
		Gpus: c.Gpus, GpusPerNode: c.GpusPerNode, MemPerGpu: c.MemPerGpu, CpusPerGpu: c.CpusPerGpu, Gres: c.Gres,
		Constraints: c.Constraints}
}

// API converts to the request body airavata-server takes.
func (r Request) API() client.BatchJobConfigRequest {
	return client.BatchJobConfigRequest{WallTimeMinutes: r.WallTimeMinutes, Allocation: r.Allocation, Nodes: r.Nodes,
		Ntasks: r.Ntasks, NtasksPerNode: r.NtasksPerNode, Cpus: r.Cpus, CpusPerTask: r.CpusPerTask, Mem: r.Mem,
		MemPerCpu: r.MemPerCpu, Gpus: r.Gpus, GpusPerNode: r.GpusPerNode, MemPerGpu: r.MemPerGpu,
		CpusPerGpu: r.CpusPerGpu, Gres: r.Gres, Constraints: r.Constraints}
}

// Overlay returns r with every field set in o replacing r's.
func (r Request) Overlay(o Request) Request {
	if o.WallTimeMinutes > 0 {
		r.WallTimeMinutes = o.WallTimeMinutes
	}
	if o.Allocation != "" {
		r.Allocation = o.Allocation
	}
	pick32 := func(dst **int32, src *int32) {
		if src != nil {
			*dst = src
		}
	}
	pickS := func(dst **string, src *string) {
		if src != nil {
			*dst = src
		}
	}
	pick32(&r.Nodes, o.Nodes)
	pick32(&r.Ntasks, o.Ntasks)
	pick32(&r.NtasksPerNode, o.NtasksPerNode)
	pick32(&r.Cpus, o.Cpus)
	pick32(&r.CpusPerTask, o.CpusPerTask)
	pick32(&r.Gpus, o.Gpus)
	pick32(&r.GpusPerNode, o.GpusPerNode)
	pickS(&r.Mem, o.Mem)
	pickS(&r.MemPerCpu, o.MemPerCpu)
	pickS(&r.MemPerGpu, o.MemPerGpu)
	pickS(&r.CpusPerGpu, o.CpusPerGpu)
	pickS(&r.Gres, o.Gres)
	pickS(&r.Constraints, o.Constraints)
	return r
}

func val(p *int32, def int64) int64 {
	if p == nil || *p <= 0 {
		return def
	}
	return int64(*p)
}

// NodeCount is the number of nodes requested (1 when unset).
func (r Request) NodeCount() int64 { return val(r.Nodes, 1) }

// Cores is the total number of CPU cores requested; cpus is --cpus-per-task when cpusPerTask is unset.
func (r Request) Cores() int64 {
	perTask := val(r.CpusPerTask, val(r.Cpus, 1))
	var tasks int64
	switch {
	case r.Ntasks != nil && *r.Ntasks > 0:
		tasks = int64(*r.Ntasks)
	case r.NtasksPerNode != nil && *r.NtasksPerNode > 0:
		tasks = r.NodeCount() * int64(*r.NtasksPerNode)
	default:
		tasks = r.NodeCount()
	}
	return tasks * perTask
}

// GPUs is the total number of GPUs requested, from --gpus, --gpus-per-node or a gpu gres.
func (r Request) GPUs() int64 {
	if r.Gpus != nil && *r.Gpus > 0 {
		return int64(*r.Gpus)
	}
	if r.GpusPerNode != nil && *r.GpusPerNode > 0 {
		return r.NodeCount() * int64(*r.GpusPerNode)
	}
	if r.Gres != nil {
		return r.NodeCount() * GresGPUs(*r.Gres)
	}
	return 0
}

// GresGPUs counts the GPUs in a gres string such as "gpu:a100:2,shard:1".
func GresGPUs(gres string) int64 {
	var n int64
	for _, item := range strings.Split(gres, ",") {
		parts := strings.Split(strings.TrimSpace(item), ":")
		if len(parts) == 0 || parts[0] != "gpu" {
			continue
		}
		count := int64(1)
		if len(parts) > 1 {
			if c, err := strconv.ParseInt(parts[len(parts)-1], 10, 64); err == nil {
				count = c
			}
		}
		n += count
	}
	return n
}

// Hours is the requested core-hours and GPU-hours: the request, not the charge.
type Hours struct {
	CoreHours float64 `json:"requested_core_hours"`
	GPUHours  float64 `json:"requested_gpu_hours"`
	Note      string  `json:"note"`
}

func (r Request) Hours() Hours {
	h := float64(r.WallTimeMinutes) / 60
	return Hours{
		CoreHours: round1(h * float64(r.Cores())),
		GPUHours:  round1(h * float64(r.GPUs())),
		Note:      "Requested hours (walltime x cores/GPUs), not what the allocation will be charged.",
	}
}

func round1(f float64) float64 { return math.Round(f*10) / 10 }

// MemoryMB parses a Slurm memory size (default unit MB) into megabytes.
func MemoryMB(s string) (int64, error) {
	s = strings.TrimSpace(strings.ToUpper(s))
	if s == "" {
		return 0, fmt.Errorf("empty memory size")
	}
	mult := int64(1)
	switch s[len(s)-1] {
	case 'K':
		mult, s = -1024, s[:len(s)-1]
	case 'M':
		s = s[:len(s)-1]
	case 'G':
		mult, s = 1024, s[:len(s)-1]
	case 'T':
		mult, s = 1024*1024, s[:len(s)-1]
	}
	n, err := strconv.ParseInt(s, 10, 64)
	if err != nil || n < 0 {
		return 0, fmt.Errorf("memory size %q is not understood", s)
	}
	if mult < 0 {
		return (n + 1023) / 1024, nil
	}
	return n * mult, nil
}

// Fit is how well a request fits one partition's limits.
type Fit struct {
	Fits       bool     `json:"fits"`
	Violations []string `json:"violations,omitempty"`
	Warnings   []string `json:"warnings,omitempty"`
}

// Check compares a request against a partition's recorded limits.
func Check(r Request, p *client.ClusterPartitionResponse) Fit {
	var f Fit
	if r.WallTimeMinutes <= 0 {
		f.Violations = append(f.Violations, "a walltime (wall_time_minutes > 0) is required")
	}
	if p.MaxRunTime != nil && *p.MaxRunTime > 0 && r.WallTimeMinutes > int64(*p.MaxRunTime) {
		f.Violations = append(f.Violations, fmt.Sprintf("walltime %d min exceeds the partition's max run time of %d min", r.WallTimeMinutes, *p.MaxRunTime))
	}
	if p.MaxNodes != nil && *p.MaxNodes > 0 && r.NodeCount() > int64(*p.MaxNodes) {
		f.Violations = append(f.Violations, fmt.Sprintf("%d nodes exceed the partition's max of %d", r.NodeCount(), *p.MaxNodes))
	}
	if p.MaxProcessors != nil && *p.MaxProcessors > 0 && r.Cores() > int64(*p.MaxProcessors) {
		f.Violations = append(f.Violations, fmt.Sprintf("%d cores exceed the partition's max of %d processors", r.Cores(), *p.MaxProcessors))
	}
	if p.CpuPerNode != nil && *p.CpuPerNode > 0 {
		perNode := (r.Cores() + r.NodeCount() - 1) / r.NodeCount()
		if perNode > int64(*p.CpuPerNode) {
			f.Violations = append(f.Violations, fmt.Sprintf("%d cores per node exceed the partition's %d CPUs per node", perNode, *p.CpuPerNode))
		}
	}
	if r.Mem != nil && p.MaxMemory != nil && *p.MaxMemory > 0 {
		if mb, err := MemoryMB(*r.Mem); err != nil {
			f.Warnings = append(f.Warnings, err.Error())
		} else if mb > *p.MaxMemory {
			f.Violations = append(f.Violations, fmt.Sprintf("memory %d MB per node exceeds the partition's max of %d MB", mb, *p.MaxMemory))
		}
	}
	if gpus := r.GPUs(); gpus > 0 {
		perNode := (gpus + r.NodeCount() - 1) / r.NodeCount()
		if p.Gres == nil || GresGPUs(*p.Gres) == 0 {
			f.Violations = append(f.Violations, "GPUs requested, but the partition records no GPU gres")
		} else if have := GresGPUs(*p.Gres); perNode > have {
			f.Violations = append(f.Violations, fmt.Sprintf("%d GPUs per node exceed the partition's gres (%s)", perNode, *p.Gres))
		}
	}
	if p.MaxRunTime == nil && p.MaxNodes == nil && p.MaxProcessors == nil {
		f.Warnings = append(f.Warnings, "the partition records no limits, so the fit could not be checked")
	}
	f.Fits = len(f.Violations) == 0
	return f
}
