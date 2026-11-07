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

// Package dto is the wire contract of the session vertical.
//
// A session is a view over its runs, each a BATCH_JOB process, newest first. A request
// is written into the first run's batch config and read back from the current run's,
// and the response derives every state from the runs. A run past startedAt + wallMinutes
// reads COMPLETED even before the scheduler reports it, so the response never shows a
// dead allocation as live.
package dto

import (
	"fmt"
	"strconv"
	"strings"
	"time"

	applicationdto "github.com/apache/airavata/api/application/dto"
	processdto "github.com/apache/airavata/api/process/dto"
	processmodel "github.com/apache/airavata/api/process/model"
	sessionmodel "github.com/apache/airavata/api/session/model"
	"github.com/apache/airavata/internal/httpx"
	"github.com/apache/airavata/internal/ptr"
)

// Resources is what a session asks of the cluster.
type Resources struct {
	Cores       int32  `json:"cores"`
	MemoryMB    int64  `json:"memoryMb"`
	WallMinutes int64  `json:"wallMinutes"`
	GPUCount    int32  `json:"gpuCount,omitempty"`
	GPUType     string `json:"gpuType,omitempty"`
}

// Request creates a session.
type Request struct {
	SlurmClusterConfigID string    `json:"slurmClusterConfigId"`
	DeploymentID         string    `json:"deploymentId"`
	Account              string    `json:"account"`
	Partition            string    `json:"partition"`
	Resources            Resources `json:"resources"`
}

func (r *Request) Validate() []httpx.FieldError {
	var c httpx.Constraints
	c.NotBlank("slurmClusterConfigId", "Slurm cluster config id cannot be blank", r.SlurmClusterConfigID)
	c.NotBlank("deploymentId", "Deployment id cannot be blank", r.DeploymentID)
	c.NotBlank("account", "Account cannot be blank", r.Account)
	c.Positive("resources.cores", "Cores must be positive", ptr.To(int64(r.Resources.Cores)))
	c.Positive("resources.memoryMb", "Memory must be positive", &r.Resources.MemoryMB)
	c.Positive("resources.wallMinutes", "Wall minutes must be positive", &r.Resources.WallMinutes)
	if r.Resources.GPUCount < 0 {
		c.Add("resources.gpuCount", "GPU count cannot be negative")
	}
	if r.Resources.GPUType != "" && r.Resources.GPUCount == 0 {
		c.Add("resources.gpuType", "GPU type needs a positive GPU count")
	}
	return c.Fields()
}

// StartRequest starts another run; a nil WallMinutes keeps the session's.
type StartRequest struct {
	WallMinutes *int64 `json:"wallMinutes"`
}

func (r *StartRequest) Validate() []httpx.FieldError {
	var c httpx.Constraints
	if r.WallMinutes != nil {
		c.Positive("wallMinutes", "Wall minutes must be positive", r.WallMinutes)
	}
	return c.Fields()
}

// Run is one BATCH_JOB process of a session.
type Run struct {
	ProcessID   string                         `json:"processId"`
	ControlPort uint16                         `json:"controlPort"`
	State       processmodel.ProcessStatusType `json:"state"`
	StartedAt   *int64                         `json:"startedAt"`
	Deadline    *int64                         `json:"deadline"`
	EndedAt     *int64                         `json:"endedAt"`
	Error       *string                        `json:"error"`
}

// Response is the read model of a session; the request, State and Live follow its current run.
type Response struct {
	SessionID string `json:"sessionId"`
	CreatedAt int64  `json:"createdAt"`
	Request
	State processmodel.ProcessStatusType `json:"state"`
	Live  bool                           `json:"live"`
	Runs  []Run                          `json:"runs"`
}

// RunRequest is the BATCH_JOB process that runs this session.
func (r *Request) RunRequest() *processdto.Request {
	cfg := &applicationdto.BatchJobConfigRequest{
		WallTimeMinutes: &r.Resources.WallMinutes,
		Allocation:      r.Account,
		CPUs:            ptr.To(r.Resources.Cores),
		Mem:             ptr.To(fmt.Sprintf("%dM", r.Resources.MemoryMB)),
		Partition:       ptr.To(r.Partition),
	}
	switch {
	case r.Resources.GPUCount == 0:
	case r.Resources.GPUType != "":
		cfg.Gres = ptr.To(fmt.Sprintf("gpu:%s:%d", r.Resources.GPUType, r.Resources.GPUCount))
	default:
		cfg.GPUs = ptr.To(r.Resources.GPUCount)
	}
	return &processdto.Request{
		ProcessType: ptr.To(processmodel.ProcessTypeBatchJob),
		BatchProcess: &processdto.BatchProcessRequest{
			DeploymentID:         r.DeploymentID,
			SlurmClusterConfigID: &r.SlurmClusterConfigID,
			BatchJobConfig:       cfg,
		},
	}
}

// RequestOf reads back the request a run was created from.
func RequestOf(run *processdto.Response) Request {
	b := run.BatchProcess
	if b == nil || b.BatchJobConfig == nil {
		return Request{}
	}
	cfg := b.BatchJobConfig
	mem, _ := strconv.ParseInt(strings.TrimSuffix(ptr.From(cfg.Mem), "M"), 10, 64)
	out := Request{
		SlurmClusterConfigID: b.SlurmClusterConfigID,
		DeploymentID:         ptr.From(b.DeploymentID),
		Account:              cfg.Allocation,
		Partition:            ptr.From(cfg.Partition),
		Resources:            Resources{Cores: ptr.From(cfg.CPUs), MemoryMB: mem, WallMinutes: cfg.WallTimeMinutes, GPUCount: ptr.From(cfg.GPUs)},
	}
	if parts := strings.Split(ptr.From(cfg.Gres), ":"); len(parts) == 3 {
		count, _ := strconv.ParseInt(parts[2], 10, 32)
		out.Resources.GPUType, out.Resources.GPUCount = parts[1], int32(count)
	}
	return out
}

// ToResponse maps a session and its runs, newest first, as of now.
func ToResponse(s *sessionmodel.Session, runs []processdto.Response, now time.Time) Response {
	out := Response{SessionID: s.ID, CreatedAt: s.CreatedAt, Runs: make([]Run, 0, len(runs))}
	for i := range runs {
		out.Runs = append(out.Runs, toRun(&runs[i], now))
	}
	if len(runs) > 0 {
		out.Request, out.State, out.Live = RequestOf(&runs[0]), out.Runs[0].State, !out.Runs[0].State.Ended()
	}
	return out
}

func toRun(p *processdto.Response, now time.Time) Run {
	run := Run{ProcessID: p.ProcessID, State: processmodel.ProcessStatusTypeCreated}
	if p.BatchProcess != nil {
		run.ControlPort = p.BatchProcess.ControlPort
		for _, st := range p.BatchProcess.BatchJobStatuses {
			if st.Status == processmodel.BatchJobStatusBegin && st.UpdatedAt >= ptr.From(run.StartedAt) {
				run.StartedAt = ptr.To(st.UpdatedAt)
			}
		}
		if cfg := p.BatchProcess.BatchJobConfig; cfg != nil && run.StartedAt != nil {
			run.Deadline = ptr.To(*run.StartedAt + cfg.WallTimeMinutes*int64(time.Minute/time.Millisecond))
		}
	}
	if st := p.LastStatus; st.Status != nil {
		run.State = *st.Status
		if run.State.Ended() {
			run.EndedAt = st.Timestamp
		}
		if run.State == processmodel.ProcessStatusTypeFailed {
			run.Error = st.Log
		}
	}
	if !run.State.Ended() && run.Deadline != nil && now.UnixMilli() > *run.Deadline {
		run.State, run.EndedAt = processmodel.ProcessStatusTypeCompleted, run.Deadline
	}
	return run
}
