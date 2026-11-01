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

package applications

import (
	"context"
	"encoding/json"
	"fmt"
	"regexp"
	"slices"
	"strings"

	"github.com/flosch/pongo2/v6"

	"github.com/apache/airavata/internal/mcp/client"
	"github.com/apache/airavata/internal/mcp/estimate"
	tk "github.com/apache/airavata/internal/mcp/tools/toolkit"
	"github.com/apache/airavata/internal/slurmconv"
)

// AppKind is the proposal-handle kind consumed by register_application.
const AppKind = "app"

// InputDraft is one declared template input.
type InputDraft struct {
	Name        string `json:"name"`
	Type        string `json:"type" jsonschema:"STRING, INTEGER, FLOAT, BOOLEAN, FILE, FILE_LIST or DIRECTORY"`
	Description string `json:"description,omitempty"`
	Required    bool   `json:"required,omitempty"`
	Default     string `json:"default,omitempty"`
}

// OutputDraft is one declared template output.
type OutputDraft struct {
	Name        string `json:"name"`
	Type        string `json:"type" jsonschema:"FILE, FILE_LIST or DIRECTORY"`
	Description string `json:"description,omitempty"`
}

// DeploymentDraft is one deployment of the application on a cluster partition.
type DeploymentDraft struct {
	ClusterID   string           `json:"cluster_id"`
	ClusterName string           `json:"cluster,omitempty"`
	Partition   string           `json:"partition"`
	RunSection  string           `json:"run_section"`
	Job         estimate.Request `json:"default_resources"`
	Fit         *estimate.Fit    `json:"fit,omitempty"`
}

// AppProposal is a validated template plus deployments, stored behind a proposal handle.
type AppProposal struct {
	Name        string            `json:"name"`
	Description string            `json:"description,omitempty"`
	Source      string            `json:"source"`
	Inputs      []InputDraft      `json:"inputs"`
	Outputs     []OutputDraft     `json:"outputs"`
	Deployments []DeploymentDraft `json:"deployments"`
	Problems    []string          `json:"problems,omitempty"`
	Notes       []string          `json:"notes,omitempty"`
}

var nameRE = regexp.MustCompile(`^[A-Za-z_][A-Za-z0-9_]*$`)
var inputRefRE = regexp.MustCompile(`inputs\.([A-Za-z_][A-Za-z0-9_]*)`)

var outputTypes = []string{"FILE", "FILE_LIST", "DIRECTORY"}

// Validate checks the proposal against Airavata's types and each target partition's limits.
func (p *AppProposal) Validate(ctx context.Context, d *tk.Deps) {
	if strings.TrimSpace(p.Name) == "" {
		p.Problems = append(p.Problems, "the application needs a name")
	}
	declared := map[string]bool{}
	for _, in := range p.Inputs {
		switch {
		case !nameRE.MatchString(in.Name):
			p.Problems = append(p.Problems, fmt.Sprintf("input name %q must be a identifier (letters, digits, _)", in.Name))
		case declared[in.Name]:
			p.Problems = append(p.Problems, fmt.Sprintf("input %q is declared twice", in.Name))
		}
		declared[in.Name] = true
		if !slices.Contains(slurmconv.InputTypes, in.Type) {
			p.Problems = append(p.Problems, fmt.Sprintf("input %q has type %q; use one of %s", in.Name, in.Type, strings.Join(slurmconv.InputTypes, ", ")))
		}
	}
	outs := map[string]bool{}
	for _, out := range p.Outputs {
		if !nameRE.MatchString(out.Name) || outs[out.Name] {
			p.Problems = append(p.Problems, fmt.Sprintf("output name %q must be a unique identifier", out.Name))
		}
		outs[out.Name] = true
		if !slices.Contains(outputTypes, out.Type) {
			p.Problems = append(p.Problems, fmt.Sprintf("output %q has type %q; use FILE, FILE_LIST or DIRECTORY", out.Name, out.Type))
		}
	}
	if len(p.Deployments) == 0 {
		p.Problems = append(p.Problems, "give at least one deployment (cluster_id, partition, run_section, resources)")
	}
	clusters := map[string]*client.SlurmClusterResponse{}
	for i := range p.Deployments {
		dep := &p.Deployments[i]
		label := fmt.Sprintf("deployment %d", i+1)
		if strings.TrimSpace(dep.RunSection) == "" {
			p.Problems = append(p.Problems, label+": run_section is empty")
		} else if _, err := pongo2.FromString(dep.RunSection); err != nil {
			p.Problems = append(p.Problems, fmt.Sprintf("%s: run_section is not a valid template: %v", label, err))
		}
		used := map[string]bool{}
		for _, m := range inputRefRE.FindAllStringSubmatch(dep.RunSection, -1) {
			used[m[1]] = true
			if !declared[m[1]] {
				p.Problems = append(p.Problems, fmt.Sprintf("%s: run_section uses inputs.%s, which is not declared", label, m[1]))
			}
		}
		for _, in := range p.Inputs {
			if !used[in.Name] {
				p.Notes = append(p.Notes, fmt.Sprintf("%s: input %q is not referenced in the run section (FILE inputs are still staged into the work directory under their name)", label, in.Name))
			}
		}
		if dep.Job.WallTimeMinutes <= 0 {
			p.Problems = append(p.Problems, label+": default_resources.wall_time_minutes is required")
		}
		if strings.TrimSpace(dep.Job.Allocation) == "" {
			p.Problems = append(p.Problems, label+": default_resources.allocation (the Slurm account) is required")
		}
		if strings.ContainsAny(dep.Partition, "\r\n,") {
			p.Problems = append(p.Problems, label+": partition must be a single partition name")
		}
		c, ok := clusters[dep.ClusterID]
		if !ok {
			var err error
			c, err = d.Cluster(ctx, dep.ClusterID)
			if err != nil {
				p.Problems = append(p.Problems, fmt.Sprintf("%s: %v", label, tk.NotFound(err, "cluster", dep.ClusterID)))
			}
			clusters[dep.ClusterID] = c
		}
		if c == nil {
			continue
		}
		dep.ClusterName = c.ClusterName
		part := tk.Partition(c, dep.Partition)
		if part == nil {
			p.Problems = append(p.Problems, fmt.Sprintf("%s: %s has no partition %q", label, c.ClusterName, dep.Partition))
			continue
		}
		fit := estimate.Check(dep.Job, part)
		dep.Fit = &fit
		for _, v := range fit.Violations {
			p.Problems = append(p.Problems, fmt.Sprintf("%s (%s/%s): %s", label, c.ClusterName, dep.Partition, v))
		}
	}
}

// Describe is the confirmation text for registering the proposal.
func (p *AppProposal) Describe() string {
	var b strings.Builder
	fmt.Fprintf(&b, "Register application %q", p.Name)
	if p.Description != "" {
		fmt.Fprintf(&b, ": %s", p.Description)
	}
	b.WriteString("\n")
	for _, in := range p.Inputs {
		req := "optional"
		if in.Required {
			req = "required"
		}
		fmt.Fprintf(&b, "Input %s (%s, %s)", in.Name, in.Type, req)
		if in.Default != "" {
			fmt.Fprintf(&b, " default %q", in.Default)
		}
		b.WriteString("\n")
	}
	for _, out := range p.Outputs {
		fmt.Fprintf(&b, "Output %s (%s)\n", out.Name, out.Type)
	}
	for _, dep := range p.Deployments {
		j := dep.Job
		fmt.Fprintf(&b, "Deployment on %s, partition %s: walltime %d min, %d node(s), %d core(s), %d GPU(s), allocation %s\n",
			dep.ClusterName, dep.Partition, j.WallTimeMinutes, j.NodeCount(), j.Cores(), j.GPUs(), j.Allocation)
		run := dep.RunSection
		if len(run) > 1500 {
			run = run[:1500] + "\n..."
		}
		fmt.Fprintf(&b, "Run section:\n%s\n", run)
	}
	b.WriteString("Templates and deployments are visible to every Airavata user.")
	return b.String()
}

// TemplateRequest is the create-template body.
func (p *AppProposal) TemplateRequest() client.TemplateRequest {
	inputs := []client.TemplateInput{}
	for _, in := range p.Inputs {
		ti := client.TemplateInput{InputName: in.Name, InputType: client.TemplateInputType(in.Type), Required: &in.Required}
		if in.Description != "" {
			ti.InputDescription = &in.Description
		}
		if in.Default != "" {
			b, _ := json.Marshal(map[string]string{"value": in.Default})
			s := string(b)
			ti.DefaultValue = &s
		}
		inputs = append(inputs, ti)
	}
	outputs := []client.TemplateOutput{}
	for _, out := range p.Outputs {
		to := client.TemplateOutput{OutputName: out.Name, OutputType: client.TemplateOutputType(out.Type)}
		if out.Description != "" {
			to.OutputDescription = &out.Description
		}
		outputs = append(outputs, to)
	}
	req := client.TemplateRequest{TemplateName: p.Name, Inputs: &inputs, Outputs: &outputs}
	if p.Description != "" {
		req.TemplateDescription = &p.Description
	}
	return req
}

// DeploymentRequest is the create-deployment body for one deployment.
func DeploymentRequest(templateID client.TemplateId, dep DeploymentDraft) (client.BatchDeploymentRequest, error) {
	cid, err := client.ParseUUID("cluster_id", dep.ClusterID)
	if err != nil {
		return client.BatchDeploymentRequest{}, err
	}
	part := dep.Partition
	return client.BatchDeploymentRequest{TemplateId: templateID, SlurmClusterId: &cid, SlurmRunSection: dep.RunSection,
		DefaultBatchJobConfig: dep.Job.API(), DefaultPartition: &part}, nil
}

// FromJobConfig converts a converter job config into a resource request.
func FromJobConfig(j slurmconv.JobConfig) estimate.Request {
	return estimate.Request{WallTimeMinutes: j.WallTimeMinutes, Allocation: j.Allocation, Nodes: j.Nodes, Ntasks: j.Ntasks,
		NtasksPerNode: j.NtasksPerNode, Cpus: j.Cpus, CpusPerTask: j.CpusPerTask, Mem: j.Mem, MemPerCpu: j.MemPerCpu,
		Gpus: j.Gpus, GpusPerNode: j.GpusPerNode, MemPerGpu: j.MemPerGpu, CpusPerGpu: j.CpusPerGpu, Gres: j.Gres,
		Constraints: j.Constraints}
}
