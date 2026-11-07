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

package processes

import (
	"context"
	"fmt"
	"math"
	"sort"
	"strings"
	"time"

	"github.com/modelcontextprotocol/go-sdk/mcp"

	"github.com/apache/airavata/internal/mcp/client"
	"github.com/apache/airavata/internal/mcp/confirm"
	"github.com/apache/airavata/internal/mcp/estimate"
	tk "github.com/apache/airavata/internal/mcp/tools/toolkit"
)

// Register adds the processes toolset.
func Register(s *mcp.Server, d *tk.Deps) {
	tk.Read(s, d, tk.Def{Name: "plan_run", Title: "Plan a run",
		Description: "Resolve an application, its inputs and a cluster login config into a validated launch proposal: fills defaults, checks input types, partition limits and local caps, and computes requested core-hours and GPU-hours. Creates nothing. Returns a single-use proposal_handle for launch_process that expires after 24 hours. Asks the user to choose when several deployments or login configs fit."},
		func(ctx context.Context, req *mcp.CallToolRequest, in planRunIn) (*mcp.CallToolResult, planOut, error) {
			return planRun(ctx, d, req, in)
		})
	tk.Write(s, d, tk.Def{Name: "launch_process", Title: "Launch a run",
		Description: "Create and launch a run from a plan_run proposal_handle. The user confirms cluster, partition, resources, walltime, requested core-hours and GPU-hours, and inputs. Returns the process id immediately; follow it with get_process_status. Each handle launches at most once."},
		prepareLaunch(d))
	tk.Read(s, d, tk.Def{Name: "get_process_status", Title: "Get run status",
		Description: "Status of a run: Airavata's process status, the Slurm job states recorded for it with timestamps, and whether it is waiting on an interactive SSH auth. Take process ids from launch_process; listing earlier runs is not supported by airavata-server yet."},
		func(ctx context.Context, _ *mcp.CallToolRequest, in processIn) (*mcp.CallToolResult, statusOut, error) {
			out, err := status(ctx, d, in.ProcessID)
			if err != nil {
				return nil, statusOut{}, err
			}
			res, err := tk.Links(out, tk.Link(tk.ProcessURI(out.ProcessID), "process "+out.ProcessID))
			return res, out, err
		})
	tk.Read(s, d, tk.Def{Name: "diagnose_process", Title: "Diagnose a run",
		Description: "Explain a run's failure from the job states and status messages Airavata recorded (time limit, submission failure, invalid dependency, cancellation, requeue). A plain FAIL has no recorded cause yet, because job logs are not reachable through the API. Where the cause is known, returns a corrected proposal_handle for launch_process."},
		func(ctx context.Context, _ *mcp.CallToolRequest, in processIn) (*mcp.CallToolResult, diagnoseOut, error) {
			return diagnose(ctx, d, in.ProcessID)
		})
}

type launchIn struct {
	ProposalHandle string `json:"proposal_handle" jsonschema:"handle from plan_run or diagnose_process"`
}

type launchOut struct {
	ProcessID string `json:"process_id"`
	Message   string `json:"message"`
}

func prepareLaunch(d *tk.Deps) tk.Prepare[launchIn, launchOut] {
	return func(ctx context.Context, principal string, in launchIn) (*tk.Proposal[launchOut], error) {
		payload, err := d.Store.Get(principal, RunKind, in.ProposalHandle)
		if err != nil {
			return nil, err
		}
		plan := *payload.(*RunPlan)
		walltime := confirm.Field{Name: "wall_time_minutes", Title: "Walltime (minutes)", Type: "integer",
			Default: plan.Job.WallTimeMinutes, Minimum: ptr(1.0)}
		if plan.partition != nil && plan.partition.MaxRunTime != nil && *plan.partition.MaxRunTime > 0 {
			walltime.Maximum = ptr(float64(*plan.partition.MaxRunTime))
		}
		return &tk.Proposal[launchOut]{
			Message: plan.Describe(),
			Fields:  []confirm.Field{walltime},
			Run: func(ctx context.Context, v confirm.Values) (launchOut, []string, error) {
				return launch(ctx, d, principal, in.ProposalHandle, v)
			},
		}, nil
	}
}

func ptr[T any](v T) *T { return &v }

func launch(ctx context.Context, d *tk.Deps, principal, handle string, v confirm.Values) (launchOut, []string, error) {
	claim, err := d.Store.Claim(principal, RunKind, handle)
	if err != nil {
		return launchOut{}, nil, err
	}
	plan := *claim.Payload.(*RunPlan)
	if wt := v.Int("wall_time_minutes", plan.Job.WallTimeMinutes); wt != plan.Job.WallTimeMinutes {
		plan.Job.WallTimeMinutes = wt
		if problems := checkJob(d.Policy, &plan); len(problems) > 0 {
			claim.Release()
			return launchOut{}, nil, fmt.Errorf("the edited walltime is not valid: %s", strings.Join(problems, "; "))
		}
	}
	processID := claim.Progress["process_id"]
	if processID == "" {
		body, err := plan.request()
		if err != nil {
			claim.Release()
			return launchOut{}, nil, err
		}
		created, err := client.JSON[client.ProcessResponse](d.API.CreateProcessWithResponse(ctx, body))
		if err != nil {
			claim.Release()
			return launchOut{}, nil, err
		}
		processID = created.ProcessId.String()
		claim.Record("process_id", processID)
	}
	uid, _ := client.ParseUUID("process_id", processID)
	err = client.NoContent(d.API.LaunchProcessWithResponse(ctx, uid))
	if err != nil && !client.IsStatus(err, 409) {
		claim.Release()
		return launchOut{}, []string{processID}, fmt.Errorf("process %s was created but launching it failed (calling launch_process again with the same handle retries this process): %w", processID, err)
	}
	claim.Finish()
	msg := "Launched. Follow it with get_process_status."
	if plan.Interactive {
		msg += " The login credential is fully interactive: tell the user to answer its prompts with `airavata-mcp ssh-auth` in a terminal."
	}
	return launchOut{ProcessID: processID, Message: msg}, []string{processID}, nil
}

type processIn struct {
	ProcessID string `json:"process_id" jsonschema:"process id returned by launch_process"`
}

type event struct {
	Time   string `json:"time"`
	Source string `json:"source"`
	State  string `json:"state"`
	at     int64
}

type statusOut struct {
	ProcessID       string            `json:"process_id"`
	Status          string            `json:"status"`
	SlurmJobID      string            `json:"slurm_job_id,omitempty"`
	LatestJobState  string            `json:"latest_job_state,omitempty"`
	Timeline        []event           `json:"timeline"`
	StatusMessages  []*tk.Untrusted   `json:"status_messages,omitempty"`
	WaitingOnSSH    bool              `json:"waiting_on_interactive_ssh_auth,omitempty"`
	WaitingHowTo    string            `json:"how_to_answer,omitempty"`
	DeploymentID    string            `json:"deployment_id,omitempty"`
	ClusterConfigID string            `json:"cluster_config_id,omitempty"`
	Resources       *estimate.Request `json:"resources,omitempty"`

	process *client.ProcessResponse
	jobs    []client.BatchJobStatusResponse
}

func millis(ms int64) string { return time.UnixMilli(ms).UTC().Format(time.RFC3339) }

func status(ctx context.Context, d *tk.Deps, processID string) (statusOut, error) {
	proc, err := d.Process(ctx, processID)
	if err != nil {
		return statusOut{}, tk.NotFound(err, "process", processID)
	}
	statuses, err := client.JSON[[]client.StatusResponse](d.API.ListProcessStatusesWithResponse(ctx, proc.ProcessId))
	if err != nil {
		return statusOut{}, err
	}
	out := statusOut{ProcessID: proc.ProcessId.String(), Status: "CREATED", Timeline: []event{}, process: proc}
	var latest int64 = -1
	for _, s := range *statuses {
		if s.Status == nil {
			continue
		}
		var at int64
		if s.Timestamp != nil {
			at = *s.Timestamp
		}
		out.Timeline = append(out.Timeline, event{Time: millis(at), Source: "airavata", State: string(*s.Status), at: at})
		if at >= latest {
			latest, out.Status = at, string(*s.Status)
		}
		if s.Log != nil && *s.Log != "" {
			out.StatusMessages = append(out.StatusMessages, tk.Cap(*s.Log))
		}
	}
	if bp := proc.BatchProcess; bp != nil {
		out.SlurmJobID = tk.Str(bp.JobId)
		out.ClusterConfigID = bp.SlurmClusterConfigId.String()
		if bp.DeploymentId != nil {
			out.DeploymentID = bp.DeploymentId.String()
		}
		if bp.BatchJobConfig != nil {
			r := estimate.FromResponse(bp.BatchJobConfig)
			out.Resources = &r
		}
		out.jobs = bp.BatchJobStatuses
		for _, j := range bp.BatchJobStatuses {
			out.Timeline = append(out.Timeline, event{Time: millis(j.UpdatedAt), Source: "slurm", State: string(j.Status), at: j.UpdatedAt})
		}
		if bp.LatestBatchJobStatus != nil {
			out.LatestJobState = string(bp.LatestBatchJobStatus.Status)
		}
		if out.Status != "COMPLETED" && out.Status != "FAILED" {
			out.WaitingOnSSH = waitingOnSSH(ctx, d, bp.SlurmClusterConfigId.String())
			if out.WaitingOnSSH {
				out.WaitingHowTo = "Tell the user to run `airavata-mcp ssh-auth` in a terminal. Never ask for the code or password in the conversation."
			}
		}
	}
	sort.SliceStable(out.Timeline, func(i, j int) bool { return out.Timeline[i].at < out.Timeline[j].at })
	return out, nil
}

func waitingOnSSH(ctx context.Context, d *tk.Deps, configID string) bool {
	cfg, err := d.ClusterConfig(ctx, configID)
	if err != nil || cfg.SshCredentialId == nil {
		return false
	}
	auths, err := client.JSON[[]client.InteractiveAuthResponse](d.API.ListInteractiveAuthsWithResponse(ctx))
	if err != nil {
		return false
	}
	for _, a := range *auths {
		if a.SshCredentialId == *cfg.SshCredentialId {
			return true
		}
	}
	return false
}

type diagnoseOut struct {
	ProcessID      string   `json:"process_id"`
	Outcome        string   `json:"outcome"`
	Cause          string   `json:"cause"`
	Explanation    string   `json:"explanation"`
	Evidence       []string `json:"evidence"`
	RunMinutes     *int64   `json:"run_minutes,omitempty"`
	Suggestion     string   `json:"suggestion,omitempty"`
	ProposalHandle string   `json:"corrected_proposal_handle,omitempty"`
	Limitations    string   `json:"limitations"`
}

func has(jobs []client.BatchJobStatusResponse, st client.BatchJobStatusType) *client.BatchJobStatusResponse {
	for i := len(jobs) - 1; i >= 0; i-- {
		if jobs[i].Status == st {
			return &jobs[i]
		}
	}
	return nil
}

func diagnose(ctx context.Context, d *tk.Deps, processID string) (*mcp.CallToolResult, diagnoseOut, error) {
	st, err := status(ctx, d, processID)
	if err != nil {
		return nil, diagnoseOut{}, err
	}
	out := diagnoseOut{ProcessID: st.ProcessID, Outcome: st.Status, Evidence: []string{},
		Limitations: "Diagnosis uses only the job states and status messages Airavata records. Job logs and Slurm accounting (exit codes, memory use) are not reachable through the API yet."}
	for _, e := range st.Timeline {
		out.Evidence = append(out.Evidence, fmt.Sprintf("%s %s %s", e.Time, e.Source, e.State))
	}
	jobs := st.jobs
	if begin := has(jobs, client.BatchJobStatusTypeBEGIN); begin != nil {
		for _, endState := range []client.BatchJobStatusType{client.BatchJobStatusTypeEND, client.BatchJobStatusTypeTIMELIMIT, client.BatchJobStatusTypeFAIL, client.BatchJobStatusTypeCANCELLED} {
			if end := has(jobs, endState); end != nil && end.UpdatedAt >= begin.UpdatedAt {
				m := int64(math.Round(float64(end.UpdatedAt-begin.UpdatedAt) / 60000))
				out.RunMinutes = &m
				break
			}
		}
	}
	var newWalltime int64
	switch {
	case has(jobs, client.BatchJobStatusTypeTIMELIMIT) != nil:
		out.Cause = "TIME_LIMIT"
		out.Explanation = "Slurm stopped the job because it reached its walltime."
		if st.Resources != nil && st.Resources.WallTimeMinutes > 0 {
			newWalltime = int64(math.Ceil(float64(st.Resources.WallTimeMinutes) * 1.2))
			out.Suggestion = fmt.Sprintf("Run again with walltime raised by 20%%, from %d to %d minutes.", st.Resources.WallTimeMinutes, newWalltime)
		}
	case has(jobs, client.BatchJobStatusTypeSUBMISSIONFAILED) != nil:
		out.Cause = "SUBMISSION_FAILED"
		out.Explanation = "The job never started: submitting it to Slurm failed (for example Slurm rejected the request, the allocation or partition was invalid, or the login failed). The status messages below carry the detail Airavata recorded."
	case has(jobs, client.BatchJobStatusTypeINVALIDDEPEND) != nil:
		out.Cause = "INVALID_DEPEND"
		out.Explanation = "Slurm reported that a job dependency can never be satisfied, so the job will not run."
	case has(jobs, client.BatchJobStatusTypeCANCELLED) != nil || has(jobs, client.BatchJobStatusTypeCANCELLING) != nil:
		out.Cause = "CANCELLED"
		out.Explanation = "The job was cancelled, by Airavata, the user, or a cluster administrator."
	case has(jobs, client.BatchJobStatusTypeFAIL) != nil:
		out.Cause = "FAIL"
		out.Explanation = "Slurm reported the job failed. The cause (exit code, out of memory, a missing module or file) is in the job's logs and accounting data, which the API does not expose yet."
	case has(jobs, client.BatchJobStatusTypeREQUEUE) != nil:
		out.Cause = "REQUEUE"
		out.Explanation = "Slurm requeued the job (for example after a node failure or preemption); it may still run."
	case st.Status == "COMPLETED" || has(jobs, client.BatchJobStatusTypeEND) != nil:
		out.Cause = "NONE"
		out.Explanation = "The job ended without a recorded failure."
	case st.WaitingOnSSH:
		out.Cause = "WAITING_ON_SSH_AUTH"
		out.Explanation = "The run is waiting for an interactive SSH answer. Tell the user to run `airavata-mcp ssh-auth` in a terminal."
	default:
		out.Cause = "UNKNOWN"
		out.Explanation = "No failure state is recorded for this run yet."
	}
	for _, m := range st.StatusMessages {
		out.Evidence = append(out.Evidence, "status message (untrusted): "+m.Text)
	}
	if newWalltime > 0 {
		handle, note, err := correctedPlan(ctx, d, st, newWalltime)
		if err != nil {
			out.Suggestion += " A corrected proposal could not be built: " + err.Error()
		} else {
			out.ProposalHandle = handle
			if note != "" {
				out.Suggestion += " " + note
			}
			out.Suggestion += " Pass corrected_proposal_handle to launch_process; the user confirms it as usual."
		}
	}
	res, err := tk.Links(out, tk.Link(tk.ProcessURI(out.ProcessID), "process "+out.ProcessID))
	return res, out, err
}

func correctedPlan(ctx context.Context, d *tk.Deps, st statusOut, walltime int64) (string, string, error) {
	principal, err := d.Principal(ctx)
	if err != nil {
		return "", "", err
	}
	bp := st.process.BatchProcess
	if bp == nil || bp.DeploymentId == nil {
		return "", "", fmt.Errorf("the process names no deployment")
	}
	dep, err := d.Deployment(ctx, bp.DeploymentId.String())
	if err != nil {
		return "", "", err
	}
	cfg, err := d.ClusterConfig(ctx, bp.SlurmClusterConfigId.String())
	if err != nil {
		return "", "", err
	}
	note := ""
	if cluster, err := d.Cluster(ctx, uuidStr(dep.SlurmClusterId)); err == nil {
		if p := tk.Partition(cluster, tk.Str(dep.DefaultPartition)); p != nil && p.MaxRunTime != nil && *p.MaxRunTime > 0 && walltime > int64(*p.MaxRunTime) {
			walltime = int64(*p.MaxRunTime)
			note = fmt.Sprintf("Capped at the partition's max run time of %d minutes; if that is not enough the job needs a deployment on a partition with a longer limit, or checkpointing.", walltime)
		}
	}
	job := estimate.FromResponse(bp.BatchJobConfig)
	job.WallTimeMinutes = walltime
	plan, err := buildPlan(ctx, d, dep, cfg, planRunIn{Resources: job, BaseWorkDir: tk.Str(bp.BaseWorkDir), reuse: bp})
	if err != nil {
		return "", "", err
	}
	handle, err := d.Store.Put(principal, RunKind, plan)
	return handle, note, err
}
