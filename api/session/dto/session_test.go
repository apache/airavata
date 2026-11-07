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

package dto

import (
	"testing"
	"time"

	applicationdto "github.com/apache/airavata/api/application/dto"
	applicationmodel "github.com/apache/airavata/api/application/model"
	processdto "github.com/apache/airavata/api/process/dto"
	processmodel "github.com/apache/airavata/api/process/model"
	sessionmodel "github.com/apache/airavata/api/session/model"
	"github.com/apache/airavata/internal/ptr"
)

func sample(gpuCount int32, gpuType string) Request {
	return Request{
		SlurmClusterConfigID: "cfg",
		DeploymentID:         "dep",
		Account:              "acct",
		Partition:            "part",
		Resources:            Resources{Cores: 2, MemoryMB: 4096, WallMinutes: 60, GPUCount: gpuCount, GPUType: gpuType},
	}
}

// respond builds the process response the server would return for a request.
func respond(id string, r Request, status processmodel.ProcessStatusType, at int64, log string, begin int64) processdto.Response {
	b := r.RunRequest().BatchProcess
	var cfg applicationmodel.BatchJobConfig
	applicationdto.ApplyBatchJobConfigRequest(&cfg, b.BatchJobConfig)
	resp := processdto.Response{
		ProcessID:  id,
		LastStatus: processdto.StatusResponse{Status: &status, Timestamp: &at},
		BatchProcess: &processdto.BatchProcessResponse{
			DeploymentID:         &b.DeploymentID,
			SlurmClusterConfigID: *b.SlurmClusterConfigID,
			ControlPort:          7000,
			BatchJobConfig:       applicationdto.ToBatchJobConfigResponse(&cfg),
		},
	}
	if log != "" {
		resp.LastStatus.Log = &log
	}
	if begin != 0 {
		resp.BatchProcess.BatchJobStatuses = []processdto.BatchJobStatusResponse{{Status: processmodel.BatchJobStatusBegin, UpdatedAt: begin}}
	}
	return resp
}

func TestRunRequestWritesTheServersOwnFormat(t *testing.T) {
	cfg := func(r Request) *applicationdto.BatchJobConfigRequest {
		pr := r.RunRequest()
		if *pr.ProcessType != processmodel.ProcessTypeBatchJob {
			t.Fatalf("type = %v", *pr.ProcessType)
		}
		return pr.BatchProcess.BatchJobConfig
	}
	typed := cfg(sample(2, "a100"))
	if ptr.From(typed.Gres) != "gpu:a100:2" || typed.GPUs != nil {
		t.Fatalf("typed gres=%v gpus=%v", typed.Gres, typed.GPUs)
	}
	untyped := cfg(sample(2, ""))
	if ptr.From(untyped.GPUs) != 2 || untyped.Gres != nil {
		t.Fatalf("untyped gres=%v gpus=%v", untyped.Gres, untyped.GPUs)
	}
	if ptr.From(untyped.Mem) != "4096M" || ptr.From(untyped.CPUs) != 2 || untyped.Allocation != "acct" || ptr.From(untyped.Partition) != "part" {
		t.Fatalf("config = %+v", untyped)
	}
}

func TestRequestOfReadsBackWhatRunRequestWrote(t *testing.T) {
	for _, want := range []Request{sample(2, "a100"), sample(2, ""), sample(0, "")} {
		run := respond("p", want, processmodel.ProcessStatusTypeCreated, 1, "", 0)
		if got := RequestOf(&run); got != want {
			t.Fatalf("got %+v want %+v", got, want)
		}
	}
}

func TestARunPastItsDeadlineReadsCompleted(t *testing.T) {
	now := time.UnixMilli(10_000_000_000)
	min := int64(time.Minute / time.Millisecond)
	s := &sessionmodel.Session{ID: "s", CurrentProcessID: "p"}

	begin := now.UnixMilli() - 61*min
	past := respond("p", sample(0, ""), processmodel.ProcessStatusTypeReady, begin, "", begin)
	got := ToResponse(s, []processdto.Response{past}, now)
	r := got.Runs[0]
	if r.State != processmodel.ProcessStatusTypeCompleted || got.Live || got.State != r.State || ptr.From(r.EndedAt) != begin+60*min {
		t.Fatalf("past = %+v live=%v", r, got.Live)
	}

	begin = now.UnixMilli() - 59*min
	within := respond("p", sample(0, ""), processmodel.ProcessStatusTypeReady, begin, "", begin)
	got = ToResponse(s, []processdto.Response{within}, now)
	r = got.Runs[0]
	if r.State != processmodel.ProcessStatusTypeReady || !got.Live || r.EndedAt != nil || ptr.From(r.Deadline) != begin+60*min {
		t.Fatalf("within = %+v live=%v", r, got.Live)
	}
}

func TestARunBeforeBeginHasNoClockAndIsLive(t *testing.T) {
	queued := respond("q", sample(0, ""), processmodel.ProcessStatusTypeSubmitted, 5, "", 0)
	got := ToResponse(&sessionmodel.Session{ID: "s"}, []processdto.Response{queued}, time.UnixMilli(10_000_000_000))
	if r := got.Runs[0]; r.StartedAt != nil || r.Deadline != nil || r.EndedAt != nil || r.State != processmodel.ProcessStatusTypeSubmitted || !got.Live {
		t.Fatalf("queued = %+v live=%v", r, got.Live)
	}
}

func TestAFailedRunCarriesItsError(t *testing.T) {
	failed := respond("f", sample(0, ""), processmodel.ProcessStatusTypeFailed, 5, "boom", 0)
	cancelled := respond("c", sample(0, ""), processmodel.ProcessStatusTypeCancelled, 6, "stopped", 0)
	got := ToResponse(&sessionmodel.Session{ID: "s", CurrentProcessID: "f"}, []processdto.Response{failed, cancelled}, time.UnixMilli(1000))
	if ptr.From(got.Runs[0].Error) != "boom" || got.Runs[1].Error != nil {
		t.Fatalf("errors = %v, %v", got.Runs[0].Error, got.Runs[1].Error)
	}
}
