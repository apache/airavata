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

package admin

import (
	"context"
	"errors"
	"fmt"
	"strconv"
	"strings"

	"github.com/modelcontextprotocol/go-sdk/mcp"

	"github.com/apache/airavata/internal/mcp/client"
	"github.com/apache/airavata/internal/mcp/confirm"
	"github.com/apache/airavata/internal/mcp/estimate"
	"github.com/apache/airavata/internal/mcp/tools/applications"
	tk "github.com/apache/airavata/internal/mcp/tools/toolkit"
)

// Register adds the admin toolset. airavata-server enforces that the caller is an ADMIN.
func Register(s *mcp.Server, d *tk.Deps) {
	tk.Write(s, d, tk.Def{Name: "register_cluster", Title: "Register a Slurm cluster",
		Description: "Register a Slurm cluster and its partitions (admins only). Asks the user to confirm."},
		prepareCluster(d))
	tk.Write(s, d, tk.Def{Name: "update_partition", Title: "Update a partition",
		Description: "Change a cluster partition's limits or description (admins only). Only the fields given change. Asks the user to confirm."},
		preparePartition(d))
	tk.Write(s, d, tk.Def{Name: "register_application", Title: "Register an application",
		Description: "Create an application template and its deployments from a proposal_handle returned by draft_application or convert_sbatch_script (admins only). Asks the user to confirm. Each handle registers at most once."},
		prepareApplication(d))
	tk.Write(s, d, tk.Def{Name: "update_deployment", Title: "Update a deployment",
		Description: "Change a deployment's run section, partition or default resources (admins only). Only the fields given change. Asks the user to confirm, showing old and new values."},
		prepareDeployment(d))
}

type partitionIn struct {
	Name            string  `json:"name"`
	Description     *string `json:"description,omitempty"`
	MaxRunTime      *int32  `json:"max_run_time_minutes,omitempty"`
	MaxNodes        *int32  `json:"max_nodes,omitempty"`
	MaxProcessors   *int32  `json:"max_processors,omitempty"`
	MaxJobsInQueue  *int32  `json:"max_jobs_in_queue,omitempty"`
	MaxMemoryMB     *int64  `json:"max_memory_mb,omitempty"`
	CPUsPerNode     *int32  `json:"cpus_per_node,omitempty"`
	DefaultNodes    *int32  `json:"default_node_count,omitempty"`
	DefaultCPUs     *int32  `json:"default_cpu_count,omitempty"`
	DefaultWalltime *int64  `json:"default_walltime_minutes,omitempty"`
	Gres            *string `json:"gres,omitempty" jsonschema:"e.g. gpu:a100:4"`
	IsDefault       *bool   `json:"is_default,omitempty"`
}

func (p partitionIn) request() client.ClusterPartitionRequest {
	return client.ClusterPartitionRequest{Name: p.Name, Description: p.Description, MaxRunTime: p.MaxRunTime, MaxNodes: p.MaxNodes,
		MaxProcessors: p.MaxProcessors, MaxJobsInQueue: p.MaxJobsInQueue, MaxMemory: p.MaxMemoryMB, CpuPerNode: p.CPUsPerNode,
		DefaultNodeCount: p.DefaultNodes, DefaultCpuCount: p.DefaultCPUs, DefaultWalltime: p.DefaultWalltime, Gres: p.Gres,
		IsDefaultQueue: p.IsDefault}
}

type clusterIn struct {
	Name         string        `json:"name"`
	Description  string        `json:"description,omitempty"`
	HeadnodeHost string        `json:"headnode_host"`
	HeadnodePort int           `json:"headnode_port,omitempty" jsonschema:"SSH port, default 22"`
	SlurmHome    string        `json:"slurm_home,omitempty"`
	DataHost     string        `json:"data_host,omitempty"`
	DataPort     int           `json:"data_port,omitempty"`
	Partitions   []partitionIn `json:"partitions"`
}

type createdOut struct {
	ID      string   `json:"id"`
	Created []string `json:"created,omitempty"`
	Message string   `json:"message"`
}

func oneLine(fields ...string) error {
	for _, f := range fields {
		if strings.ContainsAny(f, "\r\n") {
			return errors.New("names, hosts and paths must be on one line")
		}
	}
	return nil
}

func prepareCluster(d *tk.Deps) tk.Prepare[clusterIn, createdOut] {
	return func(ctx context.Context, _ string, in clusterIn) (*tk.Proposal[createdOut], error) {
		if in.Name == "" || in.HeadnodeHost == "" {
			return nil, errors.New("name and headnode_host are required")
		}
		if err := oneLine(in.Name, in.HeadnodeHost, in.SlurmHome, in.DataHost); err != nil {
			return nil, err
		}
		if in.HeadnodePort == 0 {
			in.HeadnodePort = 22
		}
		body := client.SlurmClusterRequest{ClusterName: in.Name, HeadnodeHost: in.HeadnodeHost, HeadnodePort: in.HeadnodePort}
		if in.Description != "" {
			body.ClusterDescription = &in.Description
		}
		if in.SlurmHome != "" {
			body.SlurmHome = &in.SlurmHome
		}
		if in.DataHost != "" {
			body.DataHost = &in.DataHost
		}
		if in.DataPort != 0 {
			body.DataPort = &in.DataPort
		}
		var parts []client.ClusterPartitionRequest
		var b strings.Builder
		fmt.Fprintf(&b, "Register Slurm cluster %q at %s:%d, visible to every Airavata user.\n", in.Name, in.HeadnodeHost, in.HeadnodePort)
		for _, p := range in.Partitions {
			if p.Name == "" {
				return nil, errors.New("every partition needs a name")
			}
			parts = append(parts, p.request())
			fmt.Fprintf(&b, "Partition %s: max run time %s min, max nodes %s, gres %s\n", p.Name, num(p.MaxRunTime), num(p.MaxNodes), str(p.Gres))
		}
		body.Partitions = &parts
		return &tk.Proposal[createdOut]{Message: strings.TrimSpace(b.String()), Run: func(ctx context.Context, _ confirm.Values) (createdOut, []string, error) {
			r, err := client.JSON[client.SlurmClusterResponse](d.API.CreateSlurmClusterWithResponse(ctx, body))
			if err != nil {
				return createdOut{}, nil, err
			}
			id := r.SlurmClusterId.String()
			return createdOut{ID: id, Message: "Cluster registered."}, []string{id}, nil
		}}, nil
	}
}

func num[T int32 | int64](p *T) string {
	if p == nil {
		return "unset"
	}
	return strconv.FormatInt(int64(*p), 10)
}

func str(p *string) string {
	if p == nil {
		return "unset"
	}
	return *p
}

type updatePartitionIn struct {
	ClusterID   string `json:"cluster_id"`
	PartitionID string `json:"partition_id"`
	partitionIn
}

func preparePartition(d *tk.Deps) tk.Prepare[updatePartitionIn, createdOut] {
	return func(ctx context.Context, _ string, in updatePartitionIn) (*tk.Proposal[createdOut], error) {
		cid, err := client.ParseUUID("cluster_id", in.ClusterID)
		if err != nil {
			return nil, err
		}
		pid, err := client.ParseUUID("partition_id", in.PartitionID)
		if err != nil {
			return nil, err
		}
		cur, err := client.JSON[client.ClusterPartitionResponse](d.API.GetClusterPartitionWithResponse(ctx, cid, pid))
		if err != nil {
			return nil, tk.NotFound(err, "partition", in.PartitionID)
		}
		body := client.ClusterPartitionRequest{Name: cur.Name, Description: cur.Description, MaxRunTime: cur.MaxRunTime,
			MaxNodes: cur.MaxNodes, MaxProcessors: cur.MaxProcessors, MaxJobsInQueue: cur.MaxJobsInQueue, MaxMemory: cur.MaxMemory,
			CpuPerNode: cur.CpuPerNode, DefaultNodeCount: cur.DefaultNodeCount, DefaultCpuCount: cur.DefaultCpuCount,
			DefaultWalltime: cur.DefaultWalltime, Gres: cur.Gres, Nodes: cur.Nodes, IsDefaultQueue: cur.IsDefaultQueue,
			IsCheckpointable: cur.IsCheckpointable}
		var changes []string
		set32 := func(label string, dst **int32, v *int32) {
			if v != nil {
				changes = append(changes, fmt.Sprintf("%s: %s -> %d", label, num(*dst), *v))
				*dst = v
			}
		}
		set32("max run time (min)", &body.MaxRunTime, in.MaxRunTime)
		set32("max nodes", &body.MaxNodes, in.MaxNodes)
		set32("max processors", &body.MaxProcessors, in.MaxProcessors)
		set32("max jobs in queue", &body.MaxJobsInQueue, in.MaxJobsInQueue)
		set32("cpus per node", &body.CpuPerNode, in.CPUsPerNode)
		set32("default nodes", &body.DefaultNodeCount, in.DefaultNodes)
		set32("default cpus", &body.DefaultCpuCount, in.DefaultCPUs)
		if in.MaxMemoryMB != nil {
			changes = append(changes, fmt.Sprintf("max memory (MB): %s -> %d", num(body.MaxMemory), *in.MaxMemoryMB))
			body.MaxMemory = in.MaxMemoryMB
		}
		if in.DefaultWalltime != nil {
			changes = append(changes, fmt.Sprintf("default walltime (min): %s -> %d", num(body.DefaultWalltime), *in.DefaultWalltime))
			body.DefaultWalltime = in.DefaultWalltime
		}
		if in.Gres != nil {
			changes = append(changes, fmt.Sprintf("gres: %s -> %s", str(body.Gres), *in.Gres))
			body.Gres = in.Gres
		}
		if in.Description != nil {
			changes = append(changes, "description updated")
			body.Description = in.Description
		}
		if in.IsDefault != nil {
			changes = append(changes, fmt.Sprintf("default partition: %t", *in.IsDefault))
			body.IsDefaultQueue = in.IsDefault
		}
		if in.Name != "" && in.Name != cur.Name {
			changes = append(changes, fmt.Sprintf("name: %s -> %s", cur.Name, in.Name))
			body.Name = in.Name
		}
		if len(changes) == 0 {
			return nil, errors.New("no fields to change were given")
		}
		msg := fmt.Sprintf("Update partition %s:\n%s", cur.Name, strings.Join(changes, "\n"))
		return &tk.Proposal[createdOut]{Message: msg, Run: func(ctx context.Context, _ confirm.Values) (createdOut, []string, error) {
			if _, err := client.JSON[client.ClusterPartitionResponse](d.API.UpdateClusterPartitionWithResponse(ctx, cid, pid, body)); err != nil {
				return createdOut{}, nil, err
			}
			return createdOut{ID: in.PartitionID, Message: "Partition updated."}, []string{in.PartitionID}, nil
		}}, nil
	}
}

type registerIn struct {
	ProposalHandle string `json:"proposal_handle" jsonschema:"handle from draft_application or convert_sbatch_script"`
}

func prepareApplication(d *tk.Deps) tk.Prepare[registerIn, createdOut] {
	return func(ctx context.Context, principal string, in registerIn) (*tk.Proposal[createdOut], error) {
		payload, err := d.Store.Get(principal, applications.AppKind, in.ProposalHandle)
		if err != nil {
			return nil, err
		}
		p := payload.(*applications.AppProposal)
		return &tk.Proposal[createdOut]{Message: p.Describe(), Run: func(ctx context.Context, _ confirm.Values) (createdOut, []string, error) {
			return registerApplication(ctx, d, principal, in.ProposalHandle)
		}}, nil
	}
}

func registerApplication(ctx context.Context, d *tk.Deps, principal, handle string) (createdOut, []string, error) {
	claim, err := d.Store.Claim(principal, applications.AppKind, handle)
	if err != nil {
		return createdOut{}, nil, err
	}
	p := claim.Payload.(*applications.AppProposal)
	var created []string
	templateID := claim.Progress["template_id"]
	if templateID == "" {
		t, err := client.JSON[client.TemplateResponse](d.API.CreateApplicationTemplateWithResponse(ctx, p.TemplateRequest()))
		if err != nil {
			claim.Release()
			return createdOut{}, nil, err
		}
		templateID = t.TemplateId.String()
		claim.Record("template_id", templateID)
	}
	created = append(created, templateID)
	tid, _ := client.ParseUUID("template_id", templateID)
	for i, dep := range p.Deployments {
		key := fmt.Sprintf("deployment_%d", i)
		if id := claim.Progress[key]; id != "" {
			created = append(created, id)
			continue
		}
		body, err := applications.DeploymentRequest(tid, dep)
		if err != nil {
			claim.Release()
			return createdOut{}, created, err
		}
		r, err := client.JSON[client.BatchDeploymentResponse](d.API.CreateBatchDeploymentWithResponse(ctx, body))
		if err != nil {
			claim.Release()
			return createdOut{}, created, fmt.Errorf("template %s was created, but deployment %d on %s failed (calling register_application again with the same handle resumes): %w", templateID, i+1, dep.ClusterName, err)
		}
		claim.Record(key, r.DeploymentId.String())
		created = append(created, r.DeploymentId.String())
	}
	claim.Finish()
	return createdOut{ID: templateID, Created: created, Message: "Application registered. Researchers can now plan_run it."}, created, nil
}

type updateDeploymentIn struct {
	DeploymentID string            `json:"deployment_id"`
	RunSection   *string           `json:"run_section,omitempty"`
	Partition    *string           `json:"partition,omitempty"`
	Resources    *estimate.Request `json:"default_resources,omitempty" jsonschema:"fields given here replace the deployment's defaults"`
}

func prepareDeployment(d *tk.Deps) tk.Prepare[updateDeploymentIn, createdOut] {
	return func(ctx context.Context, _ string, in updateDeploymentIn) (*tk.Proposal[createdOut], error) {
		cur, err := d.Deployment(ctx, in.DeploymentID)
		if err != nil {
			return nil, tk.NotFound(err, "deployment", in.DeploymentID)
		}
		if cur.TemplateId == nil || cur.SlurmClusterId == nil {
			return nil, errors.New("the deployment names no template or cluster")
		}
		draft := applications.DeploymentDraft{ClusterID: cur.SlurmClusterId.String(), Partition: tk.Str(cur.DefaultPartition),
			RunSection: cur.SlurmRunSection, Job: estimate.FromResponse(cur.DefaultBatchJobConfig)}
		var changes []string
		if in.RunSection != nil && *in.RunSection != draft.RunSection {
			changes = append(changes, "run section:\n--- old\n"+draft.RunSection+"\n+++ new\n"+*in.RunSection)
			draft.RunSection = *in.RunSection
		}
		if in.Partition != nil && *in.Partition != draft.Partition {
			changes = append(changes, fmt.Sprintf("partition: %s -> %s", draft.Partition, *in.Partition))
			draft.Partition = *in.Partition
		}
		if in.Resources != nil {
			before := draft.Job
			draft.Job = draft.Job.Overlay(*in.Resources)
			changes = append(changes, fmt.Sprintf("default resources: walltime %d -> %d min, cores %d -> %d, GPUs %d -> %d, allocation %s -> %s",
				before.WallTimeMinutes, draft.Job.WallTimeMinutes, before.Cores(), draft.Job.Cores(), before.GPUs(), draft.Job.GPUs(), before.Allocation, draft.Job.Allocation))
		}
		if len(changes) == 0 {
			return nil, errors.New("no fields to change were given")
		}
		tmpl, err := d.Template(ctx, cur.TemplateId.String())
		if err != nil {
			return nil, err
		}
		check := &applications.AppProposal{Name: tk.Str(tmpl.TemplateName), Deployments: []applications.DeploymentDraft{draft}}
		for _, ti := range tmpl.Inputs {
			check.Inputs = append(check.Inputs, applications.InputDraft{Name: ti.InputName, Type: string(ti.InputType)})
		}
		check.Validate(ctx, d)
		if len(check.Problems) > 0 {
			return nil, fmt.Errorf("the updated deployment is not valid: %s", strings.Join(check.Problems, "; "))
		}
		body, err := applications.DeploymentRequest(*cur.TemplateId, check.Deployments[0])
		if err != nil {
			return nil, err
		}
		msg := fmt.Sprintf("Update the deployment of %s on %s (%s). Every future run of it is affected.\n%s",
			check.Name, check.Deployments[0].ClusterName, in.DeploymentID, strings.Join(changes, "\n"))
		return &tk.Proposal[createdOut]{Message: msg, Run: func(ctx context.Context, _ confirm.Values) (createdOut, []string, error) {
			if _, err := client.JSON[client.BatchDeploymentResponse](d.API.UpdateBatchDeploymentWithResponse(ctx, cur.DeploymentId, body)); err != nil {
				return createdOut{}, nil, err
			}
			return createdOut{ID: in.DeploymentID, Message: "Deployment updated."}, []string{in.DeploymentID}, nil
		}}, nil
	}
}
