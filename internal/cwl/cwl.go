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

// Package cwl maps sbatch conversions to and from a preview-only subset of CWL v1.2.
package cwl

import (
	"bytes"
	"errors"
	"fmt"
	"math"
	"strconv"
	"strings"

	"gopkg.in/yaml.v3"

	"github.com/apache/airavata/internal/slurmconv"
)

const (
	Version          = "v1.2"
	CWLToolNamespace = "http://commonwl.org/cwltool#"
	cudaClass        = "cwltool:CUDARequirement"
)

// Document is a *CommandLineTool or a *Workflow.
type Document interface {
	DocumentClass() string
}

type Input struct {
	ID      string `yaml:"id"`
	Type    string `yaml:"type"`
	Label   string `yaml:"label,omitempty"`
	Doc     string `yaml:"doc,omitempty"`
	Default any    `yaml:"default,omitempty"`
}

type OutputBinding struct {
	Glob any `yaml:"glob,omitempty"`
}

type Output struct {
	ID            string         `yaml:"id"`
	Type          string         `yaml:"type"`
	Doc           string         `yaml:"doc,omitempty"`
	OutputBinding *OutputBinding `yaml:"outputBinding,omitempty"`
}

type SoftwarePackage struct {
	Package string   `yaml:"package"`
	Version []string `yaml:"version,omitempty"`
}

type EnvDef struct {
	EnvName  string `yaml:"envName"`
	EnvValue string `yaml:"envValue"`
}

// Requirement holds the fields of the requirement classes this package reads or writes; others land in Extra.
type Requirement struct {
	Class                 string            `yaml:"class"`
	CoresMin              any               `yaml:"coresMin,omitempty"`
	RAMMin                any               `yaml:"ramMin,omitempty"`
	CUDAVersionMin        string            `yaml:"cudaVersionMin,omitempty"`
	CUDAComputeCapability string            `yaml:"cudaComputeCapability,omitempty"`
	CUDADeviceCountMin    any               `yaml:"cudaDeviceCountMin,omitempty"`
	Packages              []SoftwarePackage `yaml:"packages,omitempty"`
	DockerPull            string            `yaml:"dockerPull,omitempty"`
	TimeLimit             any               `yaml:"timelimit,omitempty"`
	EnvDef                []EnvDef          `yaml:"envDef,omitempty"`
	Extra                 map[string]any    `yaml:",inline"`
}

type CommandLineTool struct {
	CWLVersion   string            `yaml:"cwlVersion,omitempty"`
	Class        string            `yaml:"class"`
	ID           string            `yaml:"id,omitempty"`
	Label        string            `yaml:"label,omitempty"`
	Doc          string            `yaml:"doc,omitempty"`
	Namespaces   map[string]string `yaml:"$namespaces,omitempty"`
	BaseCommand  []string          `yaml:"baseCommand,omitempty"`
	Arguments    []any             `yaml:"arguments,omitempty"`
	Inputs       []Input           `yaml:"inputs"`
	Outputs      []Output          `yaml:"outputs"`
	Requirements []Requirement     `yaml:"requirements,omitempty"`
	Hints        []Requirement     `yaml:"hints,omitempty"`
}

func (*CommandLineTool) DocumentClass() string { return "CommandLineTool" }

type StepInput struct {
	ID     string `yaml:"id"`
	Source string `yaml:"source"`
}

type Step struct {
	ID      string           `yaml:"id"`
	Run     *CommandLineTool `yaml:"run"`
	In      []StepInput      `yaml:"in"`
	Out     []string         `yaml:"out"`
	Scatter string           `yaml:"scatter,omitempty"`
}

type Workflow struct {
	CWLVersion   string            `yaml:"cwlVersion"`
	Class        string            `yaml:"class"`
	ID           string            `yaml:"id,omitempty"`
	Label        string            `yaml:"label,omitempty"`
	Doc          string            `yaml:"doc,omitempty"`
	Namespaces   map[string]string `yaml:"$namespaces,omitempty"`
	Requirements []Requirement     `yaml:"requirements,omitempty"`
	Inputs       []Input           `yaml:"inputs"`
	Outputs      []Output          `yaml:"outputs"`
	Steps        []Step            `yaml:"steps"`
}

func (*Workflow) DocumentClass() string { return "Workflow" }

var toCWLType = map[string]string{
	"STRING": "string", "INTEGER": "int", "FLOAT": "double", "BOOLEAN": "boolean",
	"FILE": "File", "FILE_LIST": "File[]", "DIRECTORY": "Directory",
}

// FromConversion builds a CWL preview of a converted script; an --array becomes a scattered Workflow.
func FromConversion(c *slurmconv.Conversion, applied []slurmconv.AppliedInput, name string) (Document, error) {
	if c == nil {
		return nil, errors.New("nil conversion")
	}
	types := map[string]string{}
	tool := &CommandLineTool{CWLVersion: Version, Class: "CommandLineTool", ID: name, Label: name,
		BaseCommand: []string{"bash", "-c"}, Inputs: []Input{}, Outputs: []Output{}}
	for _, a := range applied {
		t, ok := toCWLType[a.Type]
		if !ok {
			return nil, fmt.Errorf("input %s: unknown type %q", a.Name, a.Type)
		}
		types[a.Name] = a.Type
		tool.Inputs = append(tool.Inputs, Input{ID: a.Name, Type: t, Doc: a.Description, Default: cwlDefault(a.Type, a.Default)})
	}

	script, needsJS, err := script(c.Segments(), types)
	if err != nil {
		return nil, err
	}
	tool.Arguments = []any{script}
	var notes []string
	if needsJS {
		tool.Requirements = append(tool.Requirements, Requirement{Class: "InlineJavascriptRequirement"})
	}

	if len(c.Containers) > 0 {
		img := c.Containers[0].Image
		if after, ok := strings.CutPrefix(img, "docker://"); ok {
			tool.Requirements = append(tool.Requirements, Requirement{Class: "DockerRequirement", DockerPull: after})
		} else {
			notes = append(notes, fmt.Sprintf("Container image %s (%s) is not a Docker image, so it is not expressed as a DockerRequirement.", img, c.Containers[0].Runtime))
		}
	}

	jc := c.JobConfig
	res := Requirement{Class: "ResourceRequirement"}
	cores := coresOf(jc)
	if cores > 0 {
		res.CoresMin = cores
	}
	if jc.Mem != nil {
		if mib, ok := mebibytes(*jc.Mem); ok {
			res.RAMMin = mib
		} else {
			notes = append(notes, fmt.Sprintf("Memory %q could not be converted to ramMin.", *jc.Mem))
		}
	} else if jc.MemPerCpu != nil && cores > 0 {
		if mib, ok := mebibytes(*jc.MemPerCpu); ok {
			res.RAMMin = mib * cores
		}
	}
	if res.CoresMin != nil || res.RAMMin != nil {
		tool.Requirements = append(tool.Requirements, res)
	}
	if jc.WallTimeMinutes > 0 {
		tool.Requirements = append(tool.Requirements, Requirement{Class: "ToolTimeLimit", TimeLimit: jc.WallTimeMinutes * 60})
	}
	gpus := int64(0)
	if jc.Gpus != nil {
		gpus = int64(*jc.Gpus)
	} else if jc.GpusPerNode != nil {
		gpus = int64(*jc.GpusPerNode)
		if jc.Nodes != nil {
			gpus *= int64(*jc.Nodes)
		}
	}
	if gpus > 0 {
		tool.Namespaces = map[string]string{"cwltool": CWLToolNamespace}
		tool.Requirements = append(tool.Requirements, Requirement{Class: cudaClass,
			CUDAVersionMin: "11.0", CUDAComputeCapability: "3.0", CUDADeviceCountMin: gpus})
	}
	if len(c.Software) > 0 {
		sw := Requirement{Class: "SoftwareRequirement"}
		for _, s := range c.Software {
			p := SoftwarePackage{Package: s.Name}
			if s.Version != "" {
				p.Version = []string{s.Version}
			}
			sw.Packages = append(sw.Packages, p)
		}
		tool.Hints = append(tool.Hints, sw)
	}
	if len(c.Dependencies) > 0 {
		notes = append(notes, "Upstream Slurm dependencies: "+describeDependencies(c.Dependencies)+".")
	}

	if c.Array == nil {
		tool.Doc = strings.Join(notes, "\n")
		return tool, nil
	}

	tool.Inputs = append(tool.Inputs, Input{ID: "array_index", Type: "int"})
	tool.Requirements = append(tool.Requirements, Requirement{Class: "EnvVarRequirement",
		EnvDef: []EnvDef{{EnvName: "SLURM_ARRAY_TASK_ID", EnvValue: "$(inputs.array_index)"}}})
	wf := &Workflow{CWLVersion: Version, Class: "Workflow", ID: name, Label: name,
		Namespaces: tool.Namespaces, Requirements: []Requirement{{Class: "ScatterFeatureRequirement"}},
		Outputs: []Output{}}
	tool.CWLVersion, tool.Namespaces, tool.ID = "", nil, ""
	step := Step{ID: "job", Run: tool, Out: []string{}, Scatter: "array_index"}
	for _, in := range tool.Inputs {
		wfIn := in
		if in.ID == "array_index" {
			wfIn = Input{ID: "array_index", Type: "int[]", Default: c.Array.Indices()}
		}
		wf.Inputs = append(wf.Inputs, wfIn)
		step.In = append(step.In, StepInput{ID: in.ID, Source: in.ID})
	}
	wf.Steps = []Step{step}
	notes = append([]string{"Scatters the job over Slurm array " + c.Array.Raw + "."}, notes...)
	if c.Array.Limit > 0 {
		notes = append(notes, fmt.Sprintf("Slurm ran at most %d array tasks at once.", c.Array.Limit))
	}
	wf.Doc = strings.Join(notes, "\n")
	return wf, nil
}

func describeDependencies(deps []slurmconv.Dependency) string {
	var parts []string
	for _, d := range deps {
		if len(d.JobIDs) == 0 {
			parts = append(parts, d.Type)
			continue
		}
		parts = append(parts, d.Type+" on job "+strings.Join(d.JobIDs, ", "))
	}
	sep := " and "
	if deps[0].AnyOf {
		sep = " or "
	}
	return strings.Join(parts, sep)
}

func coresOf(jc slurmconv.JobConfig) int64 {
	var perTask int64
	switch {
	case jc.CpusPerTask != nil:
		perTask = int64(*jc.CpusPerTask)
	case jc.Cpus != nil:
		perTask = int64(*jc.Cpus)
	case jc.Ntasks != nil:
		perTask = 1
	default:
		return 0
	}
	if jc.Ntasks != nil {
		return perTask * int64(*jc.Ntasks)
	}
	return perTask
}

// mebibytes reads a Slurm memory size (default unit M, Slurm units are binary).
func mebibytes(v string) (int64, bool) {
	v = strings.ToUpper(strings.TrimSpace(v))
	v = strings.TrimSuffix(v, "B")
	if v == "" {
		return 0, false
	}
	factor := 1.0
	switch v[len(v)-1] {
	case 'K':
		factor = 1.0 / 1024
	case 'M':
		factor = 1
	case 'G':
		factor = 1024
	case 'T':
		factor = 1024 * 1024
	}
	if v[len(v)-1] < '0' || v[len(v)-1] > '9' {
		v = v[:len(v)-1]
	}
	n, err := strconv.ParseFloat(v, 64)
	if err != nil || n <= 0 {
		return 0, false
	}
	return int64(math.Ceil(n * factor)), true
}

func cwlDefault(typ, literal string) any {
	switch typ {
	case "INTEGER":
		if n, err := strconv.ParseInt(literal, 10, 64); err == nil {
			return n
		}
	case "FLOAT":
		if f, err := strconv.ParseFloat(literal, 64); err == nil {
			return f
		}
	case "BOOLEAN":
		if b, err := strconv.ParseBool(literal); err == nil {
			return b
		}
	case "FILE":
		return map[string]any{"class": "File", "location": literal}
	case "DIRECTORY":
		return map[string]any{"class": "Directory", "location": literal}
	case "FILE_LIST":
		return []any{map[string]any{"class": "File", "location": literal}}
	case "STRING":
		return literal
	}
	return nil
}

// script turns run-section segments into a CWL string, escaping $( ${ and backslashes as CWL v1.2 requires.
func script(segs []slurmconv.Segment, types map[string]string) (string, bool, error) {
	interpolated := false
	for _, s := range segs {
		if s.Input != "" || strings.Contains(s.Text, "$(") || strings.Contains(s.Text, "${") {
			interpolated = true
		}
	}
	var b strings.Builder
	needsJS := false
	for _, s := range segs {
		if s.Input == "" {
			if interpolated {
				t := strings.ReplaceAll(s.Text, `\`, `\\`)
				t = strings.ReplaceAll(t, "$(", `\$(`)
				t = strings.ReplaceAll(t, "${", `\${`)
				b.WriteString(t)
			} else {
				b.WriteString(s.Text)
			}
			continue
		}
		typ, ok := types[s.Input]
		if !ok {
			return "", false, fmt.Errorf("run section references input %s that is not among the applied inputs", s.Input)
		}
		switch typ {
		case "FILE", "DIRECTORY":
			b.WriteString("$(inputs." + s.Input + ".path)")
		case "FILE_LIST":
			needsJS = true
			b.WriteString("$(inputs." + s.Input + `.map(function(f){return f.path;}).join(" "))`)
		default:
			b.WriteString("$(inputs." + s.Input + ")")
		}
	}
	return b.String(), needsJS, nil
}

// Marshal renders a document as YAML with cwlVersion first.
func Marshal(doc Document) (string, error) {
	if doc == nil {
		return "", errors.New("nil document")
	}
	var buf bytes.Buffer
	enc := yaml.NewEncoder(&buf)
	enc.SetIndent(2)
	if err := enc.Encode(doc); err != nil {
		return "", err
	}
	if err := enc.Close(); err != nil {
		return "", err
	}
	return buf.String(), nil
}
