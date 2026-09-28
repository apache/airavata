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

package orchestration

import (
	"bytes"
	"encoding/json"
	"fmt"
	"path"
	"strings"

	"github.com/flosch/pongo2/v6"

	appmodel "github.com/apache/airavata/api/application/model"
	computemodel "github.com/apache/airavata/api/compute/model"
	model "github.com/apache/airavata/api/process/model"
)

const slurmScriptTemplate = `#!/bin/bash
#SBATCH --job-name={{ job_name }}
#SBATCH --chdir={{ work_dir }}
#SBATCH --output={{ stdout_file }}
#SBATCH --error={{ stderr_file }}
#SBATCH --time={{ wall_time }}
#SBATCH --mail-user={{ mail_user }}
#SBATCH --mail-type=BEGIN,END,FAIL,REQUEUE,INVALID_DEPEND,STAGE_OUT,TIME_LIMIT,TIME_LIMIT_90,TIME_LIMIT_80,TIME_LIMIT_50
{% if account %}
#SBATCH --account={{ account }}
{% endif %}
{% if partition %}
#SBATCH --partition={{ partition }}
{% endif %}
{% if nodes %}
#SBATCH --nodes={{ nodes }}
{% endif %}
{% if ntasks %}
#SBATCH --ntasks={{ ntasks }}
{% endif %}
{% if ntasks_per_node %}
#SBATCH --ntasks-per-node={{ ntasks_per_node }}
{% endif %}
{% if cpus_per_task %}
#SBATCH --cpus-per-task={{ cpus_per_task }}
{% elif cpus %}
#SBATCH --cpus-per-task={{ cpus }}
{% endif %}
{% if mem %}
#SBATCH --mem={{ mem }}
{% endif %}
{% if mem_per_cpu %}
#SBATCH --mem-per-cpu={{ mem_per_cpu }}
{% endif %}
{% if gres %}
#SBATCH --gres={{ gres }}
{% endif %}
{% if gpus %}
#SBATCH --gpus={{ gpus }}
{% endif %}
{% if gpus_per_node %}
#SBATCH --gpus-per-node={{ gpus_per_node }}
{% endif %}
{% if mem_per_gpu %}
#SBATCH --mem-per-gpu={{ mem_per_gpu }}
{% endif %}
{% if cpus_per_gpu %}
#SBATCH --cpus-per-gpu={{ cpus_per_gpu }}
{% endif %}
{% if constraints %}
#SBATCH --constraint={{ constraints }}
{% endif %}

echo "Airavata process {{ process_id }} starting on $(hostname) at $(date)"
cd {{ work_dir_quoted }} || exit 1

{{ run_section }}
airavata_status=$?

echo "Airavata process {{ process_id }} finished at $(date) with exit status ${airavata_status}"
exit ${airavata_status}
`

// buildSlurmScript renders the submission script for one run.
func buildSlurmScript(
	process *model.Process,
	deployment *appmodel.BatchDeployment,
	template *appmodel.Template,
	clusterConfig *computemodel.SlurmClusterConfig,
	globalJobConfigs *GlobalJobConfigs,
) (string, error) {
	batchProcess := process.BatchProcess

	jobConfig := batchProcess.BatchJobConfig
	if jobConfig == nil {
		jobConfig = deployment.DefaultBatchJobConfig
	}
	if jobConfig == nil {
		return "", fmt.Errorf("process %s names no batch job config and deployment %s declares no default",
			process.ID, deployment.ID)
	}

	workDir, err := workDirFor(batchProcess, clusterConfig, process.ID)
	if err != nil {
		return "", err
	}

	inputs, err := inputValues(batchProcess, template, workDir)
	if err != nil {
		return "", err
	}
	outputs := outputPaths(batchProcess, template, workDir)

	jobName := process.ID

	ctx := pongo2.Context{
		"process_id": process.ID,
		"job_name":   jobName,
		"work_dir":   workDir,
		// The same directory as a shell word. An SBATCH directive is not shell and must
		// carry the path bare, but the cd in the body is, and a work root with a space
		// in it would otherwise become two arguments.
		"work_dir_quoted": shellQuote(workDir),
		"stdout_file":     path.Join(workDir, process.ID+".stdout"),
		"stderr_file":     path.Join(workDir, process.ID+".stderr"),
		"wall_time":       slurmWallTime(jobConfig.WallTimeMinutes),
		"account":         strings.TrimSpace(jobConfig.Allocation),
		"partition":       optional(deployment.DefaultPartition),
		"mail_user":       globalJobConfigs.MailUser,

		"nodes":           optional(jobConfig.Nodes),
		"ntasks":          optional(jobConfig.Ntasks),
		"ntasks_per_node": optional(jobConfig.NtasksPerNode),
		"cpus":            optional(jobConfig.CPUs),
		"cpus_per_task":   optional(jobConfig.CPUsPerTask),
		"mem":             optional(jobConfig.Mem),
		"mem_per_cpu":     optional(jobConfig.MemPerCPU),
		"gres":            optional(jobConfig.Gres),
		"gpus":            optional(jobConfig.GPUs),
		"gpus_per_node":   optional(jobConfig.GPUsPerNode),
		"mem_per_gpu":     optional(jobConfig.MemPerGPU),
		"cpus_per_gpu":    optional(jobConfig.CPUsPerGPU),
		"constraints":     optional(jobConfig.Constraints),

		"inputs":  inputs,
		"outputs": outputs,
	}

	// A directive is one line, so a value carrying a newline would not extend the
	// directive — it would start a fresh line in the script that Slurm or the shell
	// reads as something else entirely. The values interpolated into the #SBATCH block
	// are checked here rather than escaped, because none of them has any business
	// spanning lines.
	for _, key := range []string{"job_name", "work_dir", "stdout_file", "stderr_file",
		"wall_time", "account", "partition", "gres", "constraints", "mem", "mem_per_cpu",
		"mem_per_gpu", "cpus_per_gpu"} {
		if value, ok := ctx[key].(string); ok && strings.ContainsAny(value, "\r\n") {
			return "", fmt.Errorf("process %s: %s contains a line break, which cannot be written into an SBATCH directive", process.ID, key)
		}
	}

	// The run section is the deployment's own Jinja, expanded against the same context
	// the script is. It is rendered first and handed to the script as a plain value, so
	// that a value a user supplied for an input is never itself re-read as template
	// source.
	runSection, err := renderJinja("run section of deployment "+deployment.ID, deployment.SlurmRunSection, ctx)
	if err != nil {
		return "", err
	}
	ctx["run_section"] = strings.TrimRight(runSection, "\n")

	return renderJinja("slurm script for process "+process.ID, slurmScriptTemplate, ctx)
}

func renderJinja(name, source string, ctx pongo2.Context) (string, error) {
	tpl, err := pongo2.FromString("{% autoescape off %}" + source + "{% endautoescape %}")
	if err != nil {
		return "", fmt.Errorf("parsing %s: %w", name, err)
	}
	tpl.Options.TrimBlocks = true
	tpl.Options.LStripBlocks = true

	var out bytes.Buffer
	if err := tpl.ExecuteWriter(ctx, &out); err != nil {
		return "", fmt.Errorf("rendering %s: %w", name, err)
	}
	return out.String(), nil
}

func workDirFor(batch *model.BatchJobProcess, clusterConfig *computemodel.SlurmClusterConfig, processID string) (string, error) {
	root := ""
	if batch.BaseWorkDir != nil {
		root = strings.TrimSpace(*batch.BaseWorkDir)
	}
	if root == "" && clusterConfig != nil {
		root = strings.TrimSpace(clusterConfig.WorkRoot)
	}
	if root == "" {
		return "", fmt.Errorf("process %s has no base work dir and its cluster config declares no work root", processID)
	}
	return path.Join(root, processID), nil
}

func slurmWallTime(minutes int64) string {
	if minutes <= 0 {
		minutes = 1
	}
	days := minutes / (24 * 60)
	hours := (minutes % (24 * 60)) / 60
	mins := minutes % 60
	if days > 0 {
		return fmt.Sprintf("%d-%02d:%02d:00", days, hours, mins)
	}
	return fmt.Sprintf("%02d:%02d:00", hours, mins)
}

// InputValues maps each declared input name to what the run section should see for it.
//
// Mapping:
//   - File, file list, and directory inputs are mapped to their staged paths under the work directory.
//   - Scalar inputs are mapped to their literal values, using the default value if no explicit value is provided.
//
// Example: ['input name'] -> ['/work/dir/input name'] for file inputs, or the literal value for scalar inputs.
func inputValues(batchProcess *model.BatchJobProcess, template *appmodel.Template, workDir string) (map[string][]string, error) {
	templateInputMap := map[string]*appmodel.TemplateInput{}
	if template != nil {
		for i := range template.Inputs {
			templateInputMap[template.Inputs[i].ID] = &template.Inputs[i]
		}
	}

	out := map[string][]string{}
	for _, mapping := range batchProcess.InputMappings {
		if mapping == nil || mapping.TemplateInputID == nil {
			continue
		}
		declared, ok := templateInputMap[*mapping.TemplateInputID]
		if !ok || declared.InputName == nil {
			continue
		}
		name := *declared.InputName

		value := mapping.Value
		if value == nil && declared.DefaultValue != nil {
			value = declared.DefaultValue
		}
		rendered, err := mappingValue(value)
		if err != nil {
			return nil, fmt.Errorf("input %s: %w", name, err)
		}

		// Append workdir to file inputs that are not already absolute paths.
		if isFileInput(*declared.InputType) {
			for i := range rendered {
				if strings.HasPrefix(rendered[i], "/") {
					continue
				}
				rendered[i] = path.Join(workDir, rendered[i])
			}
		}

		// Later, support more complex input types such as lists or nested structures.
		out[name] = rendered
	}
	return out, nil
}

// OutputPaths maps each declared output name to where the job is expected to leave it.
// The convention is the same one the staging tasks collect from: the output's name,
// directly under the run's work directory.
// Example: ['output name'] -> '/work/dir/output path'
func outputPaths(batchProcess *model.BatchJobProcess, template *appmodel.Template, workDir string) map[string]string {
	byID := map[string]*appmodel.TemplateOutput{}
	if template != nil {
		for i := range template.Outputs {
			byID[template.Outputs[i].ID] = &template.Outputs[i]
		}
	}

	out := map[string]string{}
	for _, mapping := range batchProcess.OutputMappings {
		if mapping == nil || mapping.TemplateOutputID == nil {
			continue
		}
		declared, ok := byID[*mapping.TemplateOutputID]
		if !ok || declared.OutputName == nil {
			continue
		}
		out[*declared.OutputName] = path.Join(workDir, *declared.OutputName)
	}
	return out
}

// isFileInput reports whether an input is staged onto the cluster rather than passed
// through as a literal.
func isFileInput(t appmodel.TemplateInputType) bool {
	switch t {
	case appmodel.TemplateInputTypeFile, appmodel.TemplateInputTypeFileList,
		appmodel.TemplateInputTypeDirectory:
		return true
	}
	return false
}

func mappingValue(raw *string) ([]string, error) {
	if raw == nil {
		return nil, nil
	}
	text := strings.TrimSpace(*raw)
	if text == "" {
		return []string{text}, nil
	}

	switch text[0] {
	case '[':
		var items []any
		if err := decodeJSON(text, &items); err != nil {
			return []string{text}, nil
		}
		return stringify(items), nil

	case '{':
		var doc struct {
			Value  any   `json:"value"`
			Values []any `json:"values"`
		}
		if err := decodeJSON(text, &doc); err != nil {
			return []string{text}, nil
		}
		if doc.Values != nil { // If the mapping contains multiple values, return them as a slice.
			return stringify(doc.Values), nil
		}
		if doc.Value == nil {
			return nil, nil
		}
		return []string{fmt.Sprint(doc.Value)}, nil
	}

	return []string{text}, nil
}

// decodeJSON reads text into v with numbers kept as the text they were written with, so
// an integer count does not come back through float64 as 1e+06.
func decodeJSON(text string, v any) error {
	decoder := json.NewDecoder(strings.NewReader(text))
	decoder.UseNumber()
	return decoder.Decode(v)
}

func stringify(items []any) []string {
	parts := make([]string, 0, len(items))
	for _, item := range items {
		parts = append(parts, fmt.Sprint(item))
	}
	return parts
}

// optional renders a pointer field for the template: the empty string when it is unset,
// which is what the {% if %} guards read as "the run asked for nothing here", and the
// value otherwise. Everything becomes a string so that an explicit zero is still
// distinguishable from an absent value.
func optional[T any](p *T) string {
	if p == nil {
		return ""
	}
	return strings.TrimSpace(fmt.Sprint(*p))
}
