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
	"encoding/json"
	"errors"
	"fmt"
	"slices"
	"strconv"
	"strings"

	"github.com/modelcontextprotocol/go-sdk/mcp"

	"github.com/apache/airavata/internal/mcp/client"
	"github.com/apache/airavata/internal/mcp/confirm"
	"github.com/apache/airavata/internal/mcp/estimate"
	"github.com/apache/airavata/internal/mcp/policy"
	tk "github.com/apache/airavata/internal/mcp/tools/toolkit"
)

// RunKind is the proposal-handle kind for launches.
const RunKind = "run"

// RunPlan is a validated launch, stored behind a proposal handle.
type RunPlan struct {
	TemplateID      string           `json:"application_id"`
	TemplateName    string           `json:"application"`
	DeploymentID    string           `json:"deployment_id"`
	ClusterID       string           `json:"cluster_id"`
	ClusterName     string           `json:"cluster"`
	Partition       string           `json:"partition"`
	ClusterConfigID string           `json:"cluster_config_id"`
	LoginUser       string           `json:"login_user"`
	Interactive     bool             `json:"needs_interactive_ssh_auth,omitempty"`
	BaseWorkDir     string           `json:"base_work_dir,omitempty"`
	Inputs          []PlannedValue   `json:"inputs"`
	Outputs         []PlannedValue   `json:"outputs"`
	Job             estimate.Request `json:"resources"`
	Hours           estimate.Hours   `json:"hours"`
	Warnings        []string         `json:"warnings,omitempty"`

	inputMappings  []client.InputMapping
	outputMappings []client.OutputMapping
	partition      *client.ClusterPartitionResponse
}

// PlannedValue is one input or output as it will be sent.
type PlannedValue struct {
	Name  string `json:"name"`
	Type  string `json:"type"`
	Value string `json:"value,omitempty"`
	Note  string `json:"note,omitempty"`
}

type planRunIn struct {
	TemplateID      string            `json:"application_id,omitempty" jsonschema:"application (template) id from list_applications"`
	DeploymentID    string            `json:"deployment_id,omitempty" jsonschema:"a specific deployment from get_application; picks the cluster and partition"`
	ClusterID       string            `json:"cluster_id,omitempty" jsonschema:"prefer this application's deployment on this cluster"`
	ClusterConfigID string            `json:"cluster_config_id,omitempty" jsonschema:"login config from list_cluster_configs; inferred when only one fits"`
	Inputs          map[string]string `json:"inputs,omitempty" jsonschema:"input name -> value. FILE inputs take a data product id from list_data_products"`
	Outputs         map[string]string `json:"outputs,omitempty" jsonschema:"FILE output name -> data product id to stage the result to"`
	Resources       estimate.Request  `json:"resources,omitempty" jsonschema:"overrides of the deployment's default job config (walltime, nodes, cpus, memory, GPUs, allocation)"`
	BaseWorkDir     string            `json:"base_work_dir,omitempty" jsonschema:"parent directory on the cluster; defaults to the config's work root"`

	reuse *client.BatchProcessResponse
}

type planOut struct {
	ProposalHandle string  `json:"proposal_handle"`
	ExpiresIn      string  `json:"expires_in"`
	Plan           RunPlan `json:"plan"`
	Next           string  `json:"next"`
}

func planRun(ctx context.Context, d *tk.Deps, req *mcp.CallToolRequest, in planRunIn) (*mcp.CallToolResult, planOut, error) {
	principal, err := d.Principal(ctx)
	if err != nil {
		return nil, planOut{}, err
	}
	dep, cfg, pending, err := resolveTargets(ctx, d, req, principal, in)
	if err != nil || pending != nil {
		return pending, planOut{}, err
	}
	plan, err := buildPlan(ctx, d, dep, cfg, in)
	if err != nil {
		return nil, planOut{}, err
	}
	return storePlan(d, principal, plan)
}

func storePlan(d *tk.Deps, principal string, plan *RunPlan) (*mcp.CallToolResult, planOut, error) {
	handle, err := d.Store.Put(principal, RunKind, plan)
	if err != nil {
		return nil, planOut{}, err
	}
	out := planOut{ProposalHandle: handle, ExpiresIn: "24h", Plan: *plan,
		Next: "Show the plan to the user, then call launch_process with proposal_handle. The user confirms it in their client."}
	res, err := tk.Links(out, tk.Link(tk.ApplicationURI(plan.TemplateID), plan.TemplateName))
	return res, out, err
}

func resolveTargets(ctx context.Context, d *tk.Deps, req *mcp.CallToolRequest, principal string, in planRunIn) (*client.BatchDeploymentResponse, *client.SlurmClusterConfigResponse, *mcp.CallToolResult, error) {
	var candidates []client.BatchDeploymentResponse
	switch {
	case in.DeploymentID != "":
		dep, err := d.Deployment(ctx, in.DeploymentID)
		if err != nil {
			return nil, nil, nil, tk.NotFound(err, "deployment", in.DeploymentID)
		}
		candidates = []client.BatchDeploymentResponse{*dep}
	case in.TemplateID != "":
		deps, err := d.Deployments(ctx, in.TemplateID)
		if err != nil {
			return nil, nil, nil, err
		}
		for _, dep := range deps {
			if in.ClusterID == "" || (dep.SlurmClusterId != nil && dep.SlurmClusterId.String() == in.ClusterID) {
				candidates = append(candidates, dep)
			}
		}
		if len(candidates) == 0 {
			return nil, nil, nil, fmt.Errorf("application %s has no deployment%s; an admin has to deploy it first", in.TemplateID, onCluster(in.ClusterID))
		}
	default:
		return nil, nil, nil, errors.New("give application_id (from list_applications) or deployment_id")
	}

	configs, err := d.ClusterConfigs(ctx)
	if err != nil {
		return nil, nil, nil, err
	}
	configsFor := func(dep client.BatchDeploymentResponse) []tk.ClusterConfig {
		var out []tk.ClusterConfig
		for _, c := range configs {
			if dep.SlurmClusterId != nil && c.SlurmClusterId == *dep.SlurmClusterId &&
				(in.ClusterConfigID == "" || c.SlurmClusterConfigId.String() == in.ClusterConfigID) {
				out = append(out, c)
			}
		}
		return out
	}

	names, err := d.ClusterNames(ctx)
	if err != nil {
		return nil, nil, nil, err
	}
	type option struct {
		label string
		dep   client.BatchDeploymentResponse
		cfg   tk.ClusterConfig
	}
	var options []option
	for _, dep := range candidates {
		for _, c := range configsFor(dep) {
			label := fmt.Sprintf("%s / %s as %s (%s)", names[uuidStr(dep.SlurmClusterId)], tk.Str(dep.DefaultPartition), c.LoginUser,
				firstNonEmpty(tk.Str(c.Name), c.SlurmClusterConfigId.String()[:8]))
			options = append(options, option{label, dep, c})
		}
	}
	if len(options) == 0 {
		if in.ClusterConfigID != "" {
			return nil, nil, nil, fmt.Errorf("cluster config %s is not visible to you or is not for the deployment's cluster", in.ClusterConfigID)
		}
		return nil, nil, nil, errors.New("you have no cluster login config for any cluster this application is deployed on; create one with register_cluster_config (see list_clusters and list_ssh_credentials)")
	}
	if len(options) == 1 {
		return &options[0].dep, &options[0].cfg.SlurmClusterConfigResponse, nil, nil
	}

	var labels []string
	for i := range options {
		if slices.Contains(labels, options[i].label) {
			options[i].label += " " + options[i].dep.DeploymentId.String()[:8]
		}
		labels = append(labels, options[i].label)
	}
	values, pending, err := d.Ask.Choose(req, principal,
		"Several deployments or login configs fit this run. Which cluster, partition and login should it use?",
		[]confirm.Field{{Name: "target", Title: "Where to run", Type: "string", Enum: labels}})
	if err != nil {
		if errors.Is(err, confirm.ErrDeclined) || errors.Is(err, confirm.ErrCancelled) {
			return nil, nil, nil, err
		}
		return nil, nil, nil, fmt.Errorf("%w. Candidates: %s. Pass deployment_id and cluster_config_id to choose", err, strings.Join(labels, "; "))
	}
	if pending != nil {
		return nil, nil, pending, nil
	}
	choice := values.String("target", "")
	for _, o := range options {
		if o.label == choice {
			return &o.dep, &o.cfg.SlurmClusterConfigResponse, nil, nil
		}
	}
	return nil, nil, nil, fmt.Errorf("%q is not one of the offered choices", choice)
}

func onCluster(id string) string {
	if id == "" {
		return ""
	}
	return " on cluster " + id
}

func uuidStr(id *client.SlurmClusterId) string {
	if id == nil {
		return ""
	}
	return id.String()
}

func firstNonEmpty(s ...string) string {
	for _, v := range s {
		if v != "" {
			return v
		}
	}
	return ""
}

// buildPlan validates inputs, resources and limits for one deployment and login config.
func buildPlan(ctx context.Context, d *tk.Deps, dep *client.BatchDeploymentResponse, cfg *client.SlurmClusterConfigResponse, in planRunIn) (*RunPlan, error) {
	if dep.TemplateId == nil || dep.SlurmClusterId == nil {
		return nil, fmt.Errorf("deployment %s names no application or cluster, so it cannot run", dep.DeploymentId)
	}
	if cfg.SlurmClusterId != *dep.SlurmClusterId {
		return nil, fmt.Errorf("cluster config %s is for a different cluster than deployment %s", cfg.SlurmClusterConfigId, dep.DeploymentId)
	}
	tmpl, err := d.Template(ctx, dep.TemplateId.String())
	if err != nil {
		return nil, err
	}
	cluster, err := d.Cluster(ctx, dep.SlurmClusterId.String())
	if err != nil {
		return nil, err
	}
	plan := &RunPlan{TemplateID: tmpl.TemplateId.String(), TemplateName: tk.Str(tmpl.TemplateName), DeploymentID: dep.DeploymentId.String(),
		ClusterID: cluster.SlurmClusterId.String(), ClusterName: cluster.ClusterName, Partition: tk.Str(dep.DefaultPartition),
		ClusterConfigID: cfg.SlurmClusterConfigId.String(), LoginUser: cfg.LoginUser, BaseWorkDir: in.BaseWorkDir,
		Inputs: []PlannedValue{}, Outputs: []PlannedValue{}}
	if cfg.SshCredential != nil && cfg.SshCredential.FullInteractive {
		plan.Interactive = true
		plan.Warnings = append(plan.Warnings, "The login credential is fully interactive: the run will wait until the user answers its prompts with `airavata-mcp ssh-auth` in a terminal.")
	}
	if in.BaseWorkDir != "" && !strings.HasPrefix(in.BaseWorkDir, "/") {
		return nil, errors.New("base_work_dir must be an absolute path on the cluster")
	}

	var problems []string
	if in.reuse != nil {
		reuseMappings(tmpl, in.reuse, plan)
	} else {
		planInputs(ctx, d, tmpl, in.Inputs, plan, &problems)
		planOutputs(ctx, d, tmpl, in.Outputs, plan, &problems)
	}

	plan.Job = estimate.FromResponse(dep.DefaultBatchJobConfig).Overlay(in.Resources)
	if strings.TrimSpace(plan.Job.Allocation) == "" {
		problems = append(problems, "no allocation (Slurm account): pass resources.allocation")
	}
	for _, s := range []*string{plan.Job.Mem, plan.Job.Gres, plan.Job.Constraints, plan.Job.MemPerCpu, plan.Job.MemPerGpu, plan.Job.CpusPerGpu} {
		if s != nil && strings.ContainsAny(*s, "\r\n") {
			problems = append(problems, "resource values must be on one line")
		}
	}
	plan.partition = tk.Partition(cluster, plan.Partition)
	if plan.partition == nil {
		plan.Warnings = append(plan.Warnings, fmt.Sprintf("partition %q is not recorded on %s, so its limits could not be checked", plan.Partition, cluster.ClusterName))
	}
	problems = append(problems, checkJob(d.Policy, plan)...)
	if len(problems) > 0 {
		return nil, fmt.Errorf("the run is not valid: %s", strings.Join(problems, "; "))
	}
	return plan, nil
}

// checkJob re-checks partition limits and local caps; launch calls it again after form edits.
func checkJob(p *policy.Policy, plan *RunPlan) []string {
	var problems []string
	if plan.partition != nil {
		fit := estimate.Check(plan.Job, plan.partition)
		problems = append(problems, fit.Violations...)
		for _, w := range fit.Warnings {
			if !slices.Contains(plan.Warnings, w) {
				plan.Warnings = append(plan.Warnings, w)
			}
		}
	} else if plan.Job.WallTimeMinutes <= 0 {
		problems = append(problems, "a walltime (resources.wall_time_minutes) is required")
	}
	plan.Hours = plan.Job.Hours()
	problems = append(problems, p.CheckLimits(policy.Estimate{WalltimeMinutes: plan.Job.WallTimeMinutes,
		CoreHours: plan.Hours.CoreHours, GPUHours: plan.Hours.GPUHours})...)
	return problems
}

func jsonValue(v string) *string {
	b, _ := json.Marshal(map[string]string{"value": v})
	s := string(b)
	return &s
}

func jsonValues(vs []string) *string {
	b, _ := json.Marshal(map[string][]string{"values": vs})
	s := string(b)
	return &s
}

func planInputs(ctx context.Context, d *tk.Deps, tmpl *client.TemplateResponse, given map[string]string, plan *RunPlan, problems *[]string) {
	known := map[string]bool{}
	for _, ti := range tmpl.Inputs {
		known[ti.InputName] = true
		if ti.InputId == nil {
			continue
		}
		value, ok := given[ti.InputName]
		pv := PlannedValue{Name: ti.InputName, Type: string(ti.InputType)}
		mapping := client.InputMapping{TemplateInputId: *ti.InputId}
		if !ok {
			if ti.DefaultValue != nil {
				pv.Value, pv.Note = *ti.DefaultValue, "default"
				mapping.Value = ti.DefaultValue
				plan.Inputs = append(plan.Inputs, pv)
				plan.inputMappings = append(plan.inputMappings, mapping)
			} else if ti.Required != nil && *ti.Required {
				*problems = append(*problems, fmt.Sprintf("input %q (%s) is required", ti.InputName, ti.InputType))
			}
			continue
		}
		switch ti.InputType {
		case client.TemplateInputTypeINTEGER:
			if _, err := strconv.ParseInt(strings.TrimSpace(value), 10, 64); err != nil {
				*problems = append(*problems, fmt.Sprintf("input %q must be an integer", ti.InputName))
			}
			mapping.Value = jsonValue(strings.TrimSpace(value))
		case client.TemplateInputTypeFLOAT:
			if _, err := strconv.ParseFloat(strings.TrimSpace(value), 64); err != nil {
				*problems = append(*problems, fmt.Sprintf("input %q must be a number", ti.InputName))
			}
			mapping.Value = jsonValue(strings.TrimSpace(value))
		case client.TemplateInputTypeBOOLEAN:
			if _, err := strconv.ParseBool(strings.TrimSpace(value)); err != nil {
				*problems = append(*problems, fmt.Sprintf("input %q must be true or false", ti.InputName))
			}
			mapping.Value = jsonValue(strings.ToLower(strings.TrimSpace(value)))
		case client.TemplateInputTypeFILE:
			dp, err := d.DataProduct(ctx, value)
			if err != nil {
				*problems = append(*problems, fmt.Sprintf("input %q: %v", ti.InputName, tk.NotFound(err, "data product", value)))
				continue
			}
			if !dp.IsFile {
				*problems = append(*problems, fmt.Sprintf("input %q needs a file, and data product %s is a directory", ti.InputName, value))
			}
			id := dp.DataId.String()
			mapping.Value = &id
			pv.Note = fmt.Sprintf("data product %q (%s), staged into the run's work directory", tk.Str(dp.DataName), tk.Str(dp.Path))
		case client.TemplateInputTypeFILELIST:
			var parts []string
			for _, p := range strings.Split(value, ",") {
				if p = strings.TrimSpace(p); p != "" {
					parts = append(parts, p)
				}
			}
			mapping.Value = jsonValues(parts)
			pv.Note = "passed as paths on the cluster; airavata-server does not stage FILE_LIST inputs"
		case client.TemplateInputTypeDIRECTORY:
			mapping.Value = jsonValue(value)
			pv.Note = "passed as a path on the cluster; airavata-server does not stage DIRECTORY inputs"
		default:
			mapping.Value = jsonValue(value)
		}
		pv.Value = value
		plan.Inputs = append(plan.Inputs, pv)
		plan.inputMappings = append(plan.inputMappings, mapping)
	}
	for name := range given {
		if !known[name] {
			*problems = append(*problems, fmt.Sprintf("the application has no input %q", name))
		}
	}
}

func planOutputs(ctx context.Context, d *tk.Deps, tmpl *client.TemplateResponse, given map[string]string, plan *RunPlan, problems *[]string) {
	known := map[string]bool{}
	for _, to := range tmpl.Outputs {
		known[to.OutputName] = true
		value, ok := given[to.OutputName]
		if !ok || to.OutputId == nil {
			if to.OutputType == client.TemplateOutputTypeFILE {
				plan.Warnings = append(plan.Warnings, fmt.Sprintf("output %q is not mapped to a data product, so it stays in the run's work directory on the cluster", to.OutputName))
			}
			continue
		}
		if to.OutputType != client.TemplateOutputTypeFILE {
			*problems = append(*problems, fmt.Sprintf("output %q is %s; airavata-server only stages FILE outputs", to.OutputName, to.OutputType))
			continue
		}
		dp, err := d.DataProduct(ctx, value)
		if err != nil {
			*problems = append(*problems, fmt.Sprintf("output %q: %v", to.OutputName, tk.NotFound(err, "data product", value)))
			continue
		}
		id := dp.DataId.String()
		plan.outputMappings = append(plan.outputMappings, client.OutputMapping{TemplateOutputId: *to.OutputId, Value: &id})
		plan.Outputs = append(plan.Outputs, PlannedValue{Name: to.OutputName, Type: string(to.OutputType), Value: id,
			Note: fmt.Sprintf("staged to data product %q (%s)", tk.Str(dp.DataName), tk.Str(dp.Path))})
	}
	for name := range given {
		if !known[name] {
			*problems = append(*problems, fmt.Sprintf("the application has no output %q", name))
		}
	}
}

// reuseMappings copies an earlier run's input and output mappings unchanged.
func reuseMappings(tmpl *client.TemplateResponse, bp *client.BatchProcessResponse, plan *RunPlan) {
	inputNames := map[client.TemplateId]client.TemplateInput{}
	for _, ti := range tmpl.Inputs {
		if ti.InputId != nil {
			inputNames[*ti.InputId] = ti
		}
	}
	for _, m := range bp.InputMappings {
		plan.inputMappings = append(plan.inputMappings, client.InputMapping{TemplateInputId: m.TemplateInputId, Value: m.Value})
		ti := inputNames[m.TemplateInputId]
		plan.Inputs = append(plan.Inputs, PlannedValue{Name: ti.InputName, Type: string(ti.InputType), Value: tk.Str(m.Value), Note: "same as the earlier run"})
	}
	outputNames := map[client.TemplateId]string{}
	for _, to := range tmpl.Outputs {
		if to.OutputId != nil {
			outputNames[*to.OutputId] = to.OutputName
		}
	}
	for _, m := range bp.OutputMappings {
		plan.outputMappings = append(plan.outputMappings, client.OutputMapping{TemplateOutputId: m.TemplateOutputId, Value: m.Value})
		plan.Outputs = append(plan.Outputs, PlannedValue{Name: outputNames[m.TemplateOutputId], Value: tk.Str(m.Value), Note: "same as the earlier run"})
	}
}

// Describe is the confirmation text for a launch.
func (p *RunPlan) Describe() string {
	var b strings.Builder
	fmt.Fprintf(&b, "Launch %s on %s, partition %s, as %s.\n", p.TemplateName, p.ClusterName, p.Partition, p.LoginUser)
	j := p.Job
	fmt.Fprintf(&b, "Resources: walltime %d min, %d node(s), %d core(s), %d GPU(s)", j.WallTimeMinutes, j.NodeCount(), j.Cores(), j.GPUs())
	if j.Mem != nil {
		fmt.Fprintf(&b, ", memory %s", *j.Mem)
	}
	fmt.Fprintf(&b, ", allocation %s.\n", j.Allocation)
	fmt.Fprintf(&b, "Requested: %.1f core-hours, %.1f GPU-hours (the request, not the charge).\n", p.Hours.CoreHours, p.Hours.GPUHours)
	for _, in := range p.Inputs {
		fmt.Fprintf(&b, "Input %s = %s", in.Name, in.Value)
		if in.Note != "" {
			fmt.Fprintf(&b, " (%s)", in.Note)
		}
		b.WriteString("\n")
	}
	for _, out := range p.Outputs {
		fmt.Fprintf(&b, "Output %s -> %s\n", out.Name, out.Note)
	}
	for _, w := range p.Warnings {
		fmt.Fprintf(&b, "Note: %s\n", w)
	}
	return strings.TrimSpace(b.String())
}

// request builds the create-process body.
func (p *RunPlan) request() (client.ProcessRequest, error) {
	depID, err := client.ParseUUID("deployment_id", p.DeploymentID)
	if err != nil {
		return client.ProcessRequest{}, err
	}
	cfgID, err := client.ParseUUID("cluster_config_id", p.ClusterConfigID)
	if err != nil {
		return client.ProcessRequest{}, err
	}
	bp := client.BatchProcessRequest{DeploymentId: depID, SlurmClusterConfigId: cfgID, BatchJobConfig: p.Job.API(),
		InputMappings: &p.inputMappings, OutputMappings: &p.outputMappings}
	if p.BaseWorkDir != "" {
		bp.BaseWorkDir = &p.BaseWorkDir
	}
	return client.ProcessRequest{ProcessType: client.BATCHJOB, BatchProcess: &bp}, nil
}
