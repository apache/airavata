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
	"errors"
	"fmt"
	"strings"

	"github.com/modelcontextprotocol/go-sdk/mcp"

	"github.com/apache/airavata/internal/mcp/client"
	"github.com/apache/airavata/internal/mcp/estimate"
	tk "github.com/apache/airavata/internal/mcp/tools/toolkit"
	"github.com/apache/airavata/internal/slurmconv"
)

// MaxScriptBytes caps the size of an sbatch script a tool accepts.
const MaxScriptBytes = 256 * 1024

// Register adds the applications toolset.
func Register(s *mcp.Server, d *tk.Deps) {
	tk.Read(s, d, tk.Def{Name: "list_applications", Title: "List applications",
		Description: "List the registered applications (templates) with their inputs and where each is deployed (cluster and partition)."},
		func(ctx context.Context, _ *mcp.CallToolRequest, in listIn) (*mcp.CallToolResult, listOut, error) {
			return list(ctx, d, in)
		})
	tk.Read(s, d, tk.Def{Name: "get_application", Title: "Get an application",
		Description: "Get one application: its typed inputs (with defaults) and outputs, and every deployment with its cluster, partition, run section and default resources."},
		func(ctx context.Context, _ *mcp.CallToolRequest, in getIn) (*mcp.CallToolResult, appOut, error) {
			out, err := get(ctx, d, in.ApplicationID)
			if err != nil {
				return nil, appOut{}, err
			}
			res, err := tk.Links(out, tk.Link(tk.ApplicationURI(out.ApplicationID), out.Name))
			return res, out, err
		})
	tk.Read(s, d, tk.Def{Name: "convert_sbatch_script", Title: "Convert an sbatch script",
		Description: "Parse an sbatch script into a proposed Airavata application: default resources, partition and run section, the modules and containers it uses, a round-trip diff against the original, and the options the API cannot hold. Creates nothing. Suggest inputs to turn hard-coded values into template inputs (each literal must appear in the script). With cluster_id, a registrable script also returns a proposal_handle for register_application (admins), which expires after 24 hours."},
		func(ctx context.Context, _ *mcp.CallToolRequest, in convertIn) (*mcp.CallToolResult, convertOut, error) {
			return convert(ctx, d, in)
		})
	tk.Read(s, d, tk.Def{Name: "draft_application", Title: "Draft an application",
		Description: "Build an application proposal from a description (explicit inputs, outputs and run section) or a container image and command, and check it against Airavata's input types and each target partition's limits. Creates nothing. A valid draft returns a proposal_handle for register_application (admins), which expires after 24 hours; others can hand the draft to an admin."},
		func(ctx context.Context, _ *mcp.CallToolRequest, in draftIn) (*mcp.CallToolResult, draftOut, error) {
			return draft(ctx, d, in)
		})
}

type listIn struct {
	tk.PageArgs
	NameContains string `json:"name_contains,omitempty"`
}

type deploymentOut struct {
	DeploymentID string            `json:"deployment_id"`
	ClusterID    string            `json:"cluster_id,omitempty"`
	ClusterName  string            `json:"cluster,omitempty"`
	Partition    string            `json:"partition,omitempty"`
	RunSection   string            `json:"run_section,omitempty"`
	Resources    *estimate.Request `json:"default_resources,omitempty"`
}

type appOut struct {
	ApplicationID string          `json:"application_id"`
	Name          string          `json:"name"`
	Description   *tk.Untrusted   `json:"description,omitempty"`
	Inputs        []InputDraft    `json:"inputs"`
	Outputs       []OutputDraft   `json:"outputs"`
	Deployments   []deploymentOut `json:"deployments"`
}

type listOut struct {
	Applications []appOut `json:"applications"`
	tk.PageInfo
}

func summarize(t client.TemplateResponse, deps []client.BatchDeploymentResponse, names map[string]string, full bool) appOut {
	out := appOut{ApplicationID: t.TemplateId.String(), Name: tk.Str(t.TemplateName), Inputs: []InputDraft{}, Outputs: []OutputDraft{}, Deployments: []deploymentOut{}}
	if t.TemplateDescription != nil && *t.TemplateDescription != "" {
		out.Description = tk.Cap(*t.TemplateDescription)
	}
	for _, in := range t.Inputs {
		i := InputDraft{Name: in.InputName, Type: string(in.InputType), Description: tk.Str(in.InputDescription), Required: in.Required != nil && *in.Required}
		if in.DefaultValue != nil {
			i.Default = *in.DefaultValue
		}
		out.Inputs = append(out.Inputs, i)
	}
	for _, o := range t.Outputs {
		out.Outputs = append(out.Outputs, OutputDraft{Name: o.OutputName, Type: string(o.OutputType), Description: tk.Str(o.OutputDescription)})
	}
	for _, dep := range deps {
		if dep.TemplateId == nil || *dep.TemplateId != t.TemplateId {
			continue
		}
		o := deploymentOut{DeploymentID: dep.DeploymentId.String(), Partition: tk.Str(dep.DefaultPartition)}
		if dep.SlurmClusterId != nil {
			o.ClusterID = dep.SlurmClusterId.String()
			o.ClusterName = names[o.ClusterID]
		}
		if full {
			o.RunSection = tk.Cap(dep.SlurmRunSection).Text
			if dep.DefaultBatchJobConfig != nil {
				r := estimate.FromResponse(dep.DefaultBatchJobConfig)
				o.Resources = &r
			}
		}
		out.Deployments = append(out.Deployments, o)
	}
	return out
}

func list(ctx context.Context, d *tk.Deps, in listIn) (*mcp.CallToolResult, listOut, error) {
	templates, err := d.Templates(ctx)
	if err != nil {
		return nil, listOut{}, err
	}
	deps, err := d.Deployments(ctx, "")
	if err != nil {
		return nil, listOut{}, err
	}
	names, err := d.ClusterNames(ctx)
	if err != nil {
		return nil, listOut{}, err
	}
	var apps []appOut
	for _, t := range templates {
		if tk.Contains(tk.Str(t.TemplateName), in.NameContains) {
			apps = append(apps, summarize(t, deps, names, false))
		}
	}
	page, info, err := tk.Paginate(apps, in.PageArgs)
	if err != nil {
		return nil, listOut{}, err
	}
	out := listOut{Applications: page, PageInfo: info}
	var links []*mcp.ResourceLink
	for _, a := range page {
		links = append(links, tk.Link(tk.ApplicationURI(a.ApplicationID), a.Name))
	}
	res, err := tk.Links(out, links...)
	return res, out, err
}

type getIn struct {
	ApplicationID string `json:"application_id" jsonschema:"application id from list_applications"`
}

func get(ctx context.Context, d *tk.Deps, id string) (appOut, error) {
	t, err := d.Template(ctx, id)
	if err != nil {
		return appOut{}, tk.NotFound(err, "application", id)
	}
	deps, err := d.Deployments(ctx, id)
	if err != nil {
		return appOut{}, err
	}
	names, err := d.ClusterNames(ctx)
	if err != nil {
		return appOut{}, err
	}
	return summarize(*t, deps, names, true), nil
}

type convertIn struct {
	Script      string                      `json:"script" jsonschema:"the sbatch script text"`
	Name        string                      `json:"name,omitempty" jsonschema:"application name; defaults to the script's --job-name"`
	Description string                      `json:"description,omitempty"`
	Inputs      []slurmconv.InputSuggestion `json:"inputs,omitempty" jsonschema:"hard-coded values to turn into template inputs: name, type, the literal as it appears in the script, description"`
	Outputs     []OutputDraft               `json:"outputs,omitempty" jsonschema:"files the job leaves in its work directory, to declare as outputs"`
	ClusterID   string                      `json:"cluster_id,omitempty" jsonschema:"cluster to deploy on; needed for a registrable proposal"`
}

type optionOut struct {
	Line   int    `json:"line"`
	Option string `json:"option"`
}

type convertOut struct {
	Mapped         []optionOut                     `json:"mapped_options"`
	Replaced       []optionOut                     `json:"replaced_by_airavata"`
	Unsupported    []optionOut                     `json:"unsupported_options"`
	Partition      string                          `json:"partition,omitempty"`
	Resources      estimate.Request                `json:"default_resources"`
	RunSection     string                          `json:"run_section"`
	Inputs         []slurmconv.AppliedInput        `json:"inputs"`
	Software       []slurmconv.SoftwareRequirement `json:"software,omitempty"`
	Containers     []slurmconv.Container           `json:"containers,omitempty"`
	Diff           string                          `json:"round_trip_diff"`
	Registrable    bool                            `json:"registrable"`
	Problems       []string                        `json:"problems,omitempty"`
	Notes          []string                        `json:"notes,omitempty"`
	Proposal       *AppProposal                    `json:"proposal,omitempty"`
	ProposalHandle string                          `json:"proposal_handle,omitempty"`
	Next           string                          `json:"next"`
}

func options(opts []slurmconv.Option) []optionOut {
	out := []optionOut{}
	for _, o := range opts {
		out = append(out, optionOut{Line: o.Line, Option: strings.TrimSpace(o.Raw)})
	}
	return out
}

func convert(ctx context.Context, d *tk.Deps, in convertIn) (*mcp.CallToolResult, convertOut, error) {
	if len(in.Script) > MaxScriptBytes {
		return nil, convertOut{}, fmt.Errorf("the script is larger than %d bytes", MaxScriptBytes)
	}
	script, err := slurmconv.Parse(in.Script)
	if err != nil {
		return nil, convertOut{}, fmt.Errorf("the script could not be parsed as sbatch: %w", err)
	}
	conv := slurmconv.Convert(script)
	applied, err := slurmconv.ApplyInputs(conv, in.Inputs)
	if err != nil {
		return nil, convertOut{}, err
	}
	if applied == nil {
		applied = []slurmconv.AppliedInput{}
	}
	name := in.Name
	if name == "" {
		for _, o := range conv.Replaced {
			if o.Name == "job-name" {
				name = o.Value
			}
		}
	}
	if name == "" {
		name = "converted_script"
	}
	out := convertOut{Mapped: options(conv.Mapped), Replaced: options(conv.Replaced), Unsupported: options(conv.Unsupported),
		Partition: conv.Partition, Resources: FromJobConfig(conv.JobConfig), RunSection: conv.RunSection, Inputs: applied,
		Software: conv.Software, Containers: conv.Containers, Diff: slurmconv.Diff(in.Script, slurmconv.Render(conv)),
		Registrable: conv.Registrable, Problems: conv.Problems, Notes: conv.Notes}
	if conv.Array != nil || len(conv.Dependencies) > 0 {
		out.Notes = append(out.Notes, "Job arrays and dependency chains cannot be registered yet; they will map to Airavata's sweep and chain workflow recipes.")
	}
	if !conv.Registrable {
		out.Next = "Not registrable as-is: show the problems and unsupported options to the user. They may edit the script, or keep the preview to hand to an admin."
		return nil, out, nil
	}
	if in.ClusterID == "" {
		out.Next = "Registrable. Call again with cluster_id (from list_clusters) to get a proposal_handle for register_application."
		return nil, out, nil
	}
	p := &AppProposal{Name: name, Description: in.Description, Source: "sbatch", Outputs: in.Outputs, Inputs: []InputDraft{},
		Deployments: []DeploymentDraft{{ClusterID: in.ClusterID, Partition: conv.Partition, RunSection: conv.RunSection, Job: out.Resources}}}
	if p.Outputs == nil {
		p.Outputs = []OutputDraft{}
	}
	for _, a := range applied {
		p.Inputs = append(p.Inputs, InputDraft{Name: a.Name, Type: a.Type, Description: a.Description, Default: a.Default})
	}
	if conv.Partition == "" {
		p.Problems = append(p.Problems, "the script names no --partition; draft_application can set one")
	}
	out.Proposal = p
	out.ProposalHandle, out.Next, err = finish(ctx, d, p)
	return nil, out, err
}

// finish validates p and, when it has no problems, stores it behind a handle.
func finish(ctx context.Context, d *tk.Deps, p *AppProposal) (handle, next string, err error) {
	p.Validate(ctx, d)
	if len(p.Problems) > 0 {
		return "", "The proposal has problems; fix them and draft again.", nil
	}
	principal, err := d.Principal(ctx)
	if err != nil {
		return "", "", err
	}
	handle, err = d.Store.Put(principal, AppKind, p)
	if err != nil {
		return "", "", err
	}
	return handle, "Show the proposal to the user. An admin registers it with register_application (requires --admin); others can hand the proposal to an admin.", nil
}

type draftDeployment struct {
	ClusterID  string           `json:"cluster_id"`
	Partition  string           `json:"partition"`
	RunSection string           `json:"run_section,omitempty" jsonschema:"the job body; reference inputs as {{ inputs.NAME[0] }}. Optional with container_image + command"`
	Resources  estimate.Request `json:"default_resources"`
}

type draftIn struct {
	Name           string            `json:"name"`
	Description    string            `json:"description,omitempty"`
	Inputs         []InputDraft      `json:"inputs,omitempty"`
	Outputs        []OutputDraft     `json:"outputs,omitempty"`
	ContainerImage string            `json:"container_image,omitempty" jsonschema:"a docker image reference, run with apptainer"`
	Command        string            `json:"command,omitempty" jsonschema:"the command to run inside container_image"`
	Deployments    []draftDeployment `json:"deployments" jsonschema:"one per cluster to deploy on"`
}

type draftOut struct {
	Proposal       *AppProposal `json:"proposal"`
	ProposalHandle string       `json:"proposal_handle,omitempty"`
	Next           string       `json:"next"`
}

func draft(ctx context.Context, d *tk.Deps, in draftIn) (*mcp.CallToolResult, draftOut, error) {
	p := &AppProposal{Name: in.Name, Description: in.Description, Source: "description", Inputs: in.Inputs, Outputs: in.Outputs}
	if p.Inputs == nil {
		p.Inputs = []InputDraft{}
	}
	if p.Outputs == nil {
		p.Outputs = []OutputDraft{}
	}
	containerRun := ""
	if in.ContainerImage != "" {
		if in.Command == "" || strings.ContainsAny(in.ContainerImage, " \t\r\n'\"$`;&|") {
			return nil, draftOut{}, errors.New("container_image must be a plain image reference and command is required with it")
		}
		image := in.ContainerImage
		if !strings.Contains(image, "://") && !strings.HasSuffix(image, ".sif") {
			image = "docker://" + image
		}
		containerRun = fmt.Sprintf("apptainer exec %s %s", image, in.Command)
		p.Source = "container"
	}
	for _, dd := range in.Deployments {
		job := dd.Resources
		run := dd.RunSection
		if run == "" {
			run = containerRun
		}
		p.Deployments = append(p.Deployments, DeploymentDraft{ClusterID: dd.ClusterID, Partition: dd.Partition, RunSection: run, Job: job})
	}
	out := draftOut{Proposal: p}
	var err error
	out.ProposalHandle, out.Next, err = finish(ctx, d, p)
	return nil, out, err
}
