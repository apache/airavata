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

// slurmScriptTemplate is the batch script a BATCH_JOB process is submitted with.
//
// It is a Jinja template rather than a string built in Go so that the shape of the
// script stays readable as a script. Every value a run supplies is a {{ }} placeholder,
// and every optional resource sits behind an {% if %}: an unset field renders no
// directive at all, which is what lets the scheduler apply its own default rather than
// being handed a zero the run never asked for.
//
// The run section is rendered separately and interpolated here as {{ run_section }},
// already expanded — see buildSlurmScript.
//
// Slurm has no --cpus flag, so the config's CPUs stands in for CPUsPerTask when the
// run declared only the one: a count of processors per task is the only thing it can
// mean to a scheduler that counts tasks and the CPUs each is given.
const slurmScriptTemplate = `#!/bin/bash
#SBATCH --job-name={{ job_name }}
#SBATCH --chdir={{ work_dir }}
#SBATCH --output={{ stdout_file }}
#SBATCH --error={{ stderr_file }}
#SBATCH --time={{ wall_time }}
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
//
// What a job asks for comes from the run's own BatchJobConfig, which the launch copied
// from the deployment and which the run may have departed from; the deployment's
// default stands in only when the run carries no snapshot of its own. The deployment
// contributes the run section — the module loads and the command itself — and the
// partition; the cluster config contributes the work root when the run named none.
//
// The template declares the inputs and outputs by name, and those names are what the
// run section addresses through {{ inputs.x }} and {{ outputs.y }}. File-valued inputs
// and every output resolve to a path under the run's work directory, because that is
// where the staging tasks put them and where they are collected from afterwards.
func buildSlurmScript(
	process *model.Process,
	deployment *appmodel.BatchDeployment,
	template *appmodel.Template,
	clusterConfig *computemodel.SlurmClusterConfig,
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

	jobName := jobNameFor(batchProcess, process.ID)

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

// renderJinja expands one template against ctx.
//
// Autoescaping is turned off around the source: pongo2 escapes for HTML by default,
// which would turn an ampersand or a quote in a path into an entity and break the
// script. Trimming the newline after a block tag, and the indentation before one, is
// what lets the optional directives be written as readable {% if %} lines without
// leaving a blank line behind for every resource the run did not ask for.
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

// workDirFor is the directory this run works in: the run's own base directory when it
// named one, the cluster config's work root otherwise, with the process id beneath it.
// The per-process segment is what keeps two runs sharing a work root from writing over
// each other's staged inputs.
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

// jobNameFor returns the name the job is submitted under, reduced to characters that
// survive a scheduler and a file name. A run that named none is identified by its
// process id, which is what the monitoring side has to match on anyway.
func jobNameFor(batch *model.BatchJobProcess, processID string) string {
	name := ""
	if batch.JobName != nil {
		name = strings.TrimSpace(*batch.JobName)
	}
	if name == "" {
		name = "airavata-" + processID
	}
	safe := strings.Map(func(r rune) rune {
		switch {
		case r >= 'a' && r <= 'z', r >= 'A' && r <= 'Z', r >= '0' && r <= '9':
			return r
		case r == '-', r == '_', r == '.':
			return r
		}
		return '_'
	}, name)
	if len(safe) > 64 {
		safe = safe[:64]
	}
	return safe
}

// slurmWallTime renders a minute count as a Slurm duration: days-hours:minutes:seconds
// once it runs past a day, hours:minutes:seconds below that. A run that declared no
// wall time gets the scheduler's smallest meaningful request rather than a zero, which
// Slurm reads as "no limit" on some configurations and rejects outright on others.
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
// Example: ['input name'] -> '/work/dir/input name' for file inputs, or the literal value for scalar inputs.
func inputValues(batchProcess *model.BatchJobProcess, template *appmodel.Template, workDir string) (map[string]string, error) {
	templateInputMap := map[string]*appmodel.TemplateInput{}
	if template != nil {
		for i := range template.Inputs {
			templateInputMap[template.Inputs[i].ID] = &template.Inputs[i]
		}
	}

	out := map[string]string{}
	for _, mapping := range batchProcess.InputMappings {
		if mapping == nil || mapping.TemplateInputID == nil {
			continue
		}
		declared, ok := templateInputMap[*mapping.TemplateInputID]
		if !ok || declared.InputName == nil {
			continue
		}
		name := *declared.InputName

		// If the input is a file, file list, or directory, it will be staged under the work directory.
		if declared.InputType != nil && isFileInput(*declared.InputType) {
			out[name] = path.Join(workDir, name)
			continue
		}

		value := mapping.Value
		if value == nil && declared.DefaultValue != nil {
			value = declared.DefaultValue
		}
		rendered, err := mappingValue(value)
		if err != nil {
			return nil, fmt.Errorf("input %s: %w", name, err)
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

// mappingValue decodes the {"value": ...} or {"values": [...]} document a mapping
// carries, joining a list with spaces so it reaches a command line as the argument
// list it is.
//
// A value that is not one of those documents is taken literally: mappings have been
// written with a bare string in that column, and reporting those as malformed would
// fail the run over a value that is perfectly usable.
func mappingValue(raw *string) (string, error) {
	if raw == nil {
		return "", nil
	}
	text := strings.TrimSpace(*raw)
	if text == "" || !strings.HasPrefix(text, "{") {
		return text, nil
	}

	var doc struct {
		Value  any   `json:"value"`
		Values []any `json:"values"`
	}
	decoder := json.NewDecoder(strings.NewReader(text))
	// Numbers keep the text they were written with, so an integer count does not come
	// back through float64 as 1e+06.
	decoder.UseNumber()
	if err := decoder.Decode(&doc); err != nil {
		return text, nil
	}

	if doc.Values != nil {
		parts := make([]string, 0, len(doc.Values))
		for _, v := range doc.Values {
			parts = append(parts, fmt.Sprint(v))
		}
		return strings.Join(parts, " "), nil
	}
	if doc.Value == nil {
		return "", nil
	}
	return fmt.Sprint(doc.Value), nil
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
