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

package cwl

import (
	"errors"
	"fmt"
	"math"
	"sort"
	"strconv"
	"strings"

	"gopkg.in/yaml.v3"

	"github.com/apache/airavata/internal/slurmconv"
)

var inputBaseTypes = map[string]bool{
	"string": true, "int": true, "long": true, "float": true, "double": true,
	"boolean": true, "File": true, "Directory": true,
}

var outputBaseTypes = map[string]bool{"File": true, "Directory": true, "stdout": true, "stderr": true}

var knownRequirements = map[string]bool{
	"InlineJavascriptRequirement": true, "SchemaDefRequirement": true, "LoadListingRequirement": true,
	"DockerRequirement": true, "SoftwareRequirement": true, "InitialWorkDirRequirement": true,
	"EnvVarRequirement": true, "ShellCommandRequirement": true, "ResourceRequirement": true,
	"WorkReuse": true, "NetworkAccess": true, "InplaceUpdateRequirement": true, "ToolTimeLimit": true,
	cudaClass: true, CWLToolNamespace + "CUDARequirement": true,
}

// ParseCommandLineTool reads a CWL v1.2 CommandLineTool in YAML or JSON and normalises its shorthand forms.
func ParseCommandLineTool(text string) (*CommandLineTool, error) {
	var raw map[string]any
	if err := yaml.Unmarshal([]byte(text), &raw); err != nil {
		return nil, fmt.Errorf("not a YAML or JSON document: %w", err)
	}
	if raw == nil {
		return nil, errors.New("empty document")
	}
	if c, _ := raw["class"].(string); c != "CommandLineTool" {
		return nil, fmt.Errorf("class must be CommandLineTool, got %v", raw["class"])
	}
	if v, _ := raw["cwlVersion"].(string); v != Version {
		return nil, fmt.Errorf("cwlVersion must be %s, got %v", Version, raw["cwlVersion"])
	}
	inputs, err := normalizeParams(raw["inputs"], "inputs", func(t string) error { return checkType(t, inputBaseTypes) })
	if err != nil {
		return nil, err
	}
	outputs, err := normalizeParams(raw["outputs"], "outputs", func(t string) error { return checkType(t, outputBaseTypes) })
	if err != nil {
		return nil, err
	}
	raw["inputs"], raw["outputs"] = inputs, outputs
	namespaces, _ := raw["$namespaces"].(map[string]any)
	for _, key := range []string{"requirements", "hints"} {
		reqs, err := normalizeRequirements(raw[key], key, key == "requirements", namespaces)
		if err != nil {
			return nil, err
		}
		if reqs != nil {
			raw[key] = reqs
		}
	}
	switch bc := raw["baseCommand"].(type) {
	case string:
		raw["baseCommand"] = []any{bc}
	case nil, []any:
	default:
		return nil, errors.New("baseCommand must be a string or a list of strings")
	}

	normalized, err := yaml.Marshal(raw)
	if err != nil {
		return nil, err
	}
	var t CommandLineTool
	if err := yaml.Unmarshal(normalized, &t); err != nil {
		return nil, fmt.Errorf("invalid CommandLineTool: %w", err)
	}
	if t.Inputs == nil {
		t.Inputs = []Input{}
	}
	if t.Outputs == nil {
		t.Outputs = []Output{}
	}
	return &t, nil
}

func normalizeParams(v any, field string, check func(string) error) ([]any, error) {
	var items []map[string]any
	switch p := v.(type) {
	case nil:
		return nil, fmt.Errorf("%s is required", field)
	case []any:
		for i, e := range p {
			m, ok := e.(map[string]any)
			if !ok {
				return nil, fmt.Errorf("%s[%d] must be an object", field, i)
			}
			items = append(items, m)
		}
	case map[string]any:
		keys := make([]string, 0, len(p))
		for k := range p {
			keys = append(keys, k)
		}
		sort.Strings(keys)
		for _, k := range keys {
			m, ok := p[k].(map[string]any)
			if !ok {
				m = map[string]any{"type": p[k]}
			}
			m["id"] = k
			items = append(items, m)
		}
	default:
		return nil, fmt.Errorf("%s must be a list or a map", field)
	}
	out := make([]any, 0, len(items))
	seen := map[string]bool{}
	for _, m := range items {
		id, _ := m["id"].(string)
		id = strings.TrimPrefix(id, "#")
		if id == "" {
			return nil, fmt.Errorf("%s entry without an id", field)
		}
		if seen[id] {
			return nil, fmt.Errorf("%s: duplicate id %s", field, id)
		}
		seen[id] = true
		t, err := canonicalType(m["type"])
		if err != nil {
			return nil, fmt.Errorf("%s.%s: %w", field, id, err)
		}
		if err := check(t); err != nil {
			return nil, fmt.Errorf("%s.%s: %w", field, id, err)
		}
		m["id"], m["type"] = id, t
		out = append(out, m)
	}
	return out, nil
}

// canonicalType folds the long forms (["null", X], {type: array, items: X}) into X?, X[] shorthand.
func canonicalType(v any) (string, error) {
	switch t := v.(type) {
	case string:
		return t, nil
	case []any:
		optional := false
		var rest []any
		for _, e := range t {
			if e == "null" {
				optional = true
				continue
			}
			rest = append(rest, e)
		}
		if len(rest) != 1 {
			return "", fmt.Errorf("union types are not supported: %v", t)
		}
		inner, err := canonicalType(rest[0])
		if err != nil {
			return "", err
		}
		if optional && !strings.HasSuffix(inner, "?") {
			inner += "?"
		}
		return inner, nil
	case map[string]any:
		if t["type"] == "array" {
			items, err := canonicalType(t["items"])
			if err != nil {
				return "", err
			}
			if strings.ContainsAny(items, "?[") {
				return "", fmt.Errorf("unsupported array item type %s", items)
			}
			return items + "[]", nil
		}
		return "", fmt.Errorf("unsupported type %v", t["type"])
	case nil:
		return "", errors.New("type is required")
	}
	return "", fmt.Errorf("unsupported type %v", v)
}

func checkType(t string, base map[string]bool) error {
	b := strings.TrimSuffix(t, "?")
	array := strings.HasSuffix(b, "[]")
	b = strings.TrimSuffix(b, "[]")
	if !base[b] || (array && b != "File") {
		return fmt.Errorf("unsupported type %s", t)
	}
	return nil
}

func normalizeRequirements(v any, field string, strict bool, namespaces map[string]any) ([]any, error) {
	var items []map[string]any
	switch r := v.(type) {
	case nil:
		return nil, nil
	case []any:
		for i, e := range r {
			m, ok := e.(map[string]any)
			if !ok {
				return nil, fmt.Errorf("%s[%d] must be an object", field, i)
			}
			items = append(items, m)
		}
	case map[string]any:
		keys := make([]string, 0, len(r))
		for k := range r {
			keys = append(keys, k)
		}
		sort.Strings(keys)
		for _, k := range keys {
			m, ok := r[k].(map[string]any)
			if !ok {
				if r[k] != nil {
					return nil, fmt.Errorf("%s.%s must be an object", field, k)
				}
				m = map[string]any{}
			}
			m["class"] = k
			items = append(items, m)
		}
	default:
		return nil, fmt.Errorf("%s must be a list or a map", field)
	}
	out := make([]any, 0, len(items))
	for _, m := range items {
		class, _ := m["class"].(string)
		if class == "" {
			return nil, fmt.Errorf("%s entry without a class", field)
		}
		if prefix, _, ok := strings.Cut(class, ":"); ok && !strings.Contains(class, "://") {
			if _, declared := namespaces[prefix]; !declared {
				return nil, fmt.Errorf("%s: namespace prefix %s of %s is not declared in $namespaces", field, prefix, class)
			}
		}
		if strict && !knownRequirements[class] {
			return nil, fmt.Errorf("%s: unsupported requirement %s", field, class)
		}
		out = append(out, m)
	}
	return out, nil
}

type TemplateInputDraft struct {
	Name        string  `json:"name"`
	Type        string  `json:"type"`
	Description string  `json:"description,omitempty"`
	Required    bool    `json:"required"`
	Default     *string `json:"default,omitempty"`
}

type TemplateOutputDraft struct {
	Name        string `json:"name"`
	Type        string `json:"type"`
	Description string `json:"description,omitempty"`
}

var toAiravataInput = map[string]string{
	"string": "STRING", "int": "INTEGER", "long": "INTEGER", "float": "FLOAT", "double": "FLOAT",
	"boolean": "BOOLEAN", "File": "FILE", "File[]": "FILE_LIST", "Directory": "DIRECTORY",
}

var toAiravataOutput = map[string]string{
	"File": "FILE", "stdout": "FILE", "stderr": "FILE", "File[]": "FILE_LIST", "Directory": "DIRECTORY",
}

// ToAiravata maps a tool's interface and resources onto template inputs, outputs and a batch job config.
func ToAiravata(t *CommandLineTool) ([]TemplateInputDraft, []TemplateOutputDraft, slurmconv.JobConfig, []string) {
	var inputs []TemplateInputDraft
	var outputs []TemplateOutputDraft
	var job slurmconv.JobConfig
	var problems []string
	if t == nil {
		return nil, nil, job, []string{"no tool"}
	}

	for _, in := range t.Inputs {
		base := strings.TrimSuffix(in.Type, "?")
		typ, ok := toAiravataInput[base]
		if !ok {
			problems = append(problems, fmt.Sprintf("input %s: type %s has no Airavata equivalent", in.ID, in.Type))
			continue
		}
		d := TemplateInputDraft{Name: in.ID, Type: typ, Description: firstNonEmpty(in.Doc, in.Label),
			Required: !strings.HasSuffix(in.Type, "?") && in.Default == nil}
		if in.Default != nil {
			if s, ok := defaultString(in.Default); ok {
				d.Default = &s
			} else {
				problems = append(problems, fmt.Sprintf("input %s: default %v cannot be carried over", in.ID, in.Default))
			}
		}
		inputs = append(inputs, d)
	}
	for _, out := range t.Outputs {
		base := strings.TrimSuffix(out.Type, "?")
		typ, ok := toAiravataOutput[base]
		if !ok {
			problems = append(problems, fmt.Sprintf("output %s: type %s has no Airavata equivalent", out.ID, out.Type))
			continue
		}
		outputs = append(outputs, TemplateOutputDraft{Name: out.ID, Type: typ, Description: out.Doc})
		if out.OutputBinding != nil {
			if g, _ := out.OutputBinding.Glob.(string); g != out.ID {
				problems = append(problems, fmt.Sprintf("output %s: Airavata collects outputs by name from the working directory; glob %v is not used", out.ID, out.OutputBinding.Glob))
			}
		}
	}

	all := append(append([]Requirement{}, t.Requirements...), t.Hints...)
	for _, r := range all {
		switch r.Class {
		case "ResourceRequirement":
			if r.CoresMin != nil {
				if n, ok := ceilNumber(r.CoresMin); ok && n > 0 && n <= math.MaxInt32 {
					v := int32(n)
					job.Cpus = &v
				} else {
					problems = append(problems, fmt.Sprintf("coresMin %v is not a positive number", r.CoresMin))
				}
			}
			if r.RAMMin != nil {
				if n, ok := ceilNumber(r.RAMMin); ok && n > 0 {
					v := strconv.FormatInt(n, 10) + "M"
					job.Mem = &v
				} else {
					problems = append(problems, fmt.Sprintf("ramMin %v is not a positive number", r.RAMMin))
				}
			}
			for k := range r.Extra {
				problems = append(problems, "ResourceRequirement."+k+" has no Airavata equivalent")
			}
		case "ToolTimeLimit":
			if n, ok := ceilNumber(r.TimeLimit); ok && n > 0 {
				job.WallTimeMinutes = (n + 59) / 60
			} else {
				problems = append(problems, fmt.Sprintf("ToolTimeLimit %v is not a positive number of seconds", r.TimeLimit))
			}
		case cudaClass, CWLToolNamespace + "CUDARequirement":
			n := int64(1)
			if r.CUDADeviceCountMin != nil {
				var ok bool
				if n, ok = ceilNumber(r.CUDADeviceCountMin); !ok || n <= 0 || n > math.MaxInt32 {
					problems = append(problems, fmt.Sprintf("cudaDeviceCountMin %v is not a positive number", r.CUDADeviceCountMin))
					continue
				}
			}
			v := int32(n)
			job.Gpus = &v
		case "SoftwareRequirement":
			var pkgs []string
			for _, p := range r.Packages {
				s := p.Package
				if len(p.Version) > 0 {
					s += "/" + p.Version[0]
				}
				pkgs = append(pkgs, s)
			}
			problems = append(problems, "SoftwareRequirement is not registered; load these in the run section: "+strings.Join(pkgs, " "))
		case "DockerRequirement":
			problems = append(problems, "DockerRequirement is not registered; run the image with apptainer in the run section (docker://"+r.DockerPull+")")
		default:
			problems = append(problems, r.Class+" has no Airavata equivalent")
		}
	}
	if job.WallTimeMinutes == 0 {
		problems = append(problems, "no ToolTimeLimit: wallTimeMinutes is required")
	}
	problems = append(problems, "allocation (sbatch --account) has no CWL equivalent and must be supplied")
	return inputs, outputs, job, problems
}

func firstNonEmpty(s ...string) string {
	for _, v := range s {
		if v != "" {
			return v
		}
	}
	return ""
}

func ceilNumber(v any) (int64, bool) {
	switch n := v.(type) {
	case int:
		return int64(n), true
	case int64:
		return n, true
	case uint64:
		if n > math.MaxInt64 {
			return 0, false
		}
		return int64(n), true
	case float64:
		if math.IsNaN(n) || math.IsInf(n, 0) {
			return 0, false
		}
		return int64(math.Ceil(n)), true
	}
	return 0, false
}

func defaultString(v any) (string, bool) {
	switch d := v.(type) {
	case string:
		return d, true
	case bool:
		return strconv.FormatBool(d), true
	case int:
		return strconv.Itoa(d), true
	case int64:
		return strconv.FormatInt(d, 10), true
	case uint64:
		return strconv.FormatUint(d, 10), true
	case float64:
		return strconv.FormatFloat(d, 'g', -1, 64), true
	case map[string]any:
		for _, k := range []string{"location", "path"} {
			if s, ok := d[k].(string); ok {
				return s, true
			}
		}
	case []any:
		var parts []string
		for _, e := range d {
			s, ok := defaultString(e)
			if !ok {
				return "", false
			}
			parts = append(parts, s)
		}
		return strings.Join(parts, " "), true
	}
	return "", false
}
