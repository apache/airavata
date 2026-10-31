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
	"reflect"
	"strings"
	"testing"

	"gopkg.in/yaml.v3"

	"github.com/apache/airavata/internal/slurmconv"
)

const sampleScript = `#!/bin/bash
#SBATCH -t 1:30:30
#SBATCH -A proj
#SBATCH -n 2 -c 4
#SBATCH --mem=8G
#SBATCH --gpus=2
module load gcc/12.2
apptainer exec --nv docker://python:3.12 python run.py data.csv 5 0.5 true samples.tar refdir $(hostname) "a\tb"
echo ${#arr[@]}
`

func convert(t *testing.T, text string, in []slurmconv.InputSuggestion) (*slurmconv.Conversion, []slurmconv.AppliedInput) {
	t.Helper()
	s, err := slurmconv.Parse(text)
	if err != nil {
		t.Fatal(err)
	}
	c := slurmconv.Convert(s)
	applied, err := slurmconv.ApplyInputs(c, in)
	if err != nil {
		t.Fatal(err)
	}
	return c, applied
}

func sampleInputs() []slurmconv.InputSuggestion {
	return []slurmconv.InputSuggestion{
		{Name: "data", Type: "FILE", Literal: "data.csv", Description: "input table"},
		{Name: "n", Type: "INTEGER", Literal: "5"},
		{Name: "alpha", Type: "FLOAT", Literal: "0.5"},
		{Name: "verbose", Type: "BOOLEAN", Literal: "true"},
		{Name: "samples", Type: "FILE_LIST", Literal: "samples.tar"},
		{Name: "ref", Type: "DIRECTORY", Literal: "refdir"},
	}
}

func i32(v int32) *int32   { return &v }
func str(v string) *string { return &v }

func TestFromConversionTool(t *testing.T) {
	c, applied := convert(t, sampleScript, sampleInputs())
	doc, err := FromConversion(c, applied, "sample")
	if err != nil {
		t.Fatal(err)
	}
	tool, ok := doc.(*CommandLineTool)
	if !ok {
		t.Fatalf("got %T", doc)
	}
	wantScript := `module load gcc/12.2
apptainer exec --nv docker://python:3.12 python run.py $(inputs.data.path) $(inputs.n) $(inputs.alpha) $(inputs.verbose) $(inputs.samples.map(function(f){return f.path;}).join(" ")) $(inputs.ref.path) \$(hostname) "a\\tb"
echo \${#arr[@]}`
	if !reflect.DeepEqual(tool.BaseCommand, []string{"bash", "-c"}) || len(tool.Arguments) != 1 || tool.Arguments[0] != wantScript {
		t.Fatalf("command %v %q", tool.BaseCommand, tool.Arguments)
	}
	var types []string
	for _, in := range tool.Inputs {
		types = append(types, in.ID+":"+in.Type)
	}
	if !reflect.DeepEqual(types, []string{"data:File", "n:int", "alpha:double", "verbose:boolean", "samples:File[]", "ref:Directory"}) {
		t.Fatalf("inputs %v", types)
	}
	classes := map[string]Requirement{}
	for _, r := range tool.Requirements {
		classes[r.Class] = r
	}
	if r := classes["ResourceRequirement"]; r.CoresMin != int64(8) || r.RAMMin != int64(8192) {
		t.Fatalf("resources %+v", r)
	}
	if classes["ToolTimeLimit"].TimeLimit != int64(91*60) {
		t.Fatalf("time limit %v", classes["ToolTimeLimit"].TimeLimit)
	}
	if classes["DockerRequirement"].DockerPull != "python:3.12" {
		t.Fatalf("docker %+v", classes["DockerRequirement"])
	}
	if classes[cudaClass].CUDADeviceCountMin != int64(2) || tool.Namespaces["cwltool"] != CWLToolNamespace {
		t.Fatalf("cuda %+v ns %v", classes[cudaClass], tool.Namespaces)
	}
	if _, ok := classes["InlineJavascriptRequirement"]; !ok {
		t.Fatal("FILE_LIST join needs InlineJavascriptRequirement")
	}
	if len(tool.Hints) != 1 || !reflect.DeepEqual(tool.Hints[0].Packages[0], SoftwarePackage{Package: "gcc", Version: []string{"12.2"}}) {
		t.Fatalf("hints %+v", tool.Hints)
	}
}

func TestFromConversionNoInterpolationKeepsScriptVerbatim(t *testing.T) {
	c, applied := convert(t, "#SBATCH -t 5\n#SBATCH -A a\necho 'a\\nb'\napptainer run /img/tool.sif\n", nil)
	doc, err := FromConversion(c, applied, "plain")
	if err != nil {
		t.Fatal(err)
	}
	tool := doc.(*CommandLineTool)
	if tool.Arguments[0] != "echo 'a\\nb'\napptainer run /img/tool.sif" {
		t.Fatalf("script %q", tool.Arguments[0])
	}
	if !strings.Contains(tool.Doc, "/img/tool.sif") {
		t.Fatalf("sif note missing: %q", tool.Doc)
	}
	for _, r := range tool.Requirements {
		if r.Class == "DockerRequirement" || r.Class == cudaClass {
			t.Fatalf("unexpected %s", r.Class)
		}
	}
	if tool.Namespaces != nil {
		t.Fatal("no namespaces without CUDA")
	}
}

func TestFromConversionArrayWorkflow(t *testing.T) {
	c, applied := convert(t, "#SBATCH -t 5\n#SBATCH -A a\n#SBATCH --array=0-4:2%1\n#SBATCH -d afterok:12:13\n#SBATCH --gpus-per-node=1\nrun $SLURM_ARRAY_TASK_ID 7\n",
		[]slurmconv.InputSuggestion{{Name: "k", Type: "INTEGER", Literal: "7"}})
	doc, err := FromConversion(c, applied, "arr")
	if err != nil {
		t.Fatal(err)
	}
	wf, ok := doc.(*Workflow)
	if !ok {
		t.Fatalf("got %T", doc)
	}
	if wf.Requirements[0].Class != "ScatterFeatureRequirement" || len(wf.Steps) != 1 || wf.Steps[0].Scatter != "array_index" {
		t.Fatalf("workflow %+v", wf)
	}
	if wf.Inputs[1].ID != "array_index" || wf.Inputs[1].Type != "int[]" || !reflect.DeepEqual(wf.Inputs[1].Default, []int{0, 2, 4}) {
		t.Fatalf("array input %+v", wf.Inputs)
	}
	if !strings.Contains(wf.Doc, "afterok on job 12, 13") || !strings.Contains(wf.Doc, "at most 1") {
		t.Fatalf("doc %q", wf.Doc)
	}
	run := wf.Steps[0].Run
	if run.CWLVersion != "" || run.Namespaces != nil || wf.Namespaces["cwltool"] == "" {
		t.Fatal("namespaces belong on the root document only")
	}
	text, err := Marshal(wf)
	if err != nil {
		t.Fatal(err)
	}
	if !strings.HasPrefix(text, "cwlVersion: v1.2\nclass: Workflow\n") || !strings.Contains(text, "envValue: $(inputs.array_index)") {
		t.Fatalf("yaml:\n%s", text)
	}
	var generic map[string]any
	if err := yaml.Unmarshal([]byte(text), &generic); err != nil {
		t.Fatal(err)
	}
}

func TestMarshalRoundTrip(t *testing.T) {
	c, applied := convert(t, sampleScript, sampleInputs())
	doc, err := FromConversion(c, applied, "sample")
	if err != nil {
		t.Fatal(err)
	}
	text, err := Marshal(doc)
	if err != nil {
		t.Fatal(err)
	}
	if !strings.HasPrefix(text, "cwlVersion: v1.2\n") {
		t.Fatalf("cwlVersion not first:\n%s", text)
	}
	var generic map[string]any
	if err := yaml.Unmarshal([]byte(text), &generic); err != nil {
		t.Fatalf("not YAML: %v", err)
	}
	parsed, err := ParseCommandLineTool(text)
	if err != nil {
		t.Fatalf("parse back: %v\n%s", err, text)
	}
	if parsed.Arguments[0] != doc.(*CommandLineTool).Arguments[0] {
		t.Fatalf("script changed:\n%v", parsed.Arguments[0])
	}
	inputs, outputs, job, problems := ToAiravata(parsed)
	var got []string
	for _, in := range inputs {
		d := "<nil>"
		if in.Default != nil {
			d = *in.Default
		}
		got = append(got, in.Name+":"+in.Type+"="+d)
	}
	want := []string{"data:FILE=data.csv", "n:INTEGER=5", "alpha:FLOAT=0.5", "verbose:BOOLEAN=true", "samples:FILE_LIST=samples.tar", "ref:DIRECTORY=refdir"}
	if !reflect.DeepEqual(got, want) {
		t.Fatalf("inputs %v", got)
	}
	if inputs[0].Description != "input table" || len(outputs) != 0 {
		t.Fatalf("inputs %+v outputs %+v", inputs, outputs)
	}
	wantJob := slurmconv.JobConfig{WallTimeMinutes: 91, Cpus: i32(8), Mem: str("8192M"), Gpus: i32(2)}
	if !reflect.DeepEqual(job, wantJob) {
		t.Fatalf("job %+v", job)
	}
	joined := strings.Join(problems, "\n")
	for _, p := range []string{"allocation", "SoftwareRequirement", "DockerRequirement", "InlineJavascriptRequirement"} {
		if !strings.Contains(joined, p) {
			t.Fatalf("problems missing %s: %v", p, problems)
		}
	}
}

func TestParseCommandLineTool(t *testing.T) {
	jsonDoc := `{"cwlVersion": "v1.2", "class": "CommandLineTool", "baseCommand": "echo",
 "$namespaces": {"cwltool": "http://commonwl.org/cwltool#"},
 "inputs": {"msg": "string", "count": {"type": "long?", "doc": "how many"}, "files": {"type": {"type": "array", "items": "File"}},
  "maybe": {"type": ["null", "double"], "default": 1.5}, "dir": "Directory", "flag": {"type": "boolean", "default": false}},
 "outputs": {"out": {"type": "stdout"}, "many": "File[]", "res": {"type": "Directory", "outputBinding": {"glob": "results"}}},
 "requirements": {"ResourceRequirement": {"coresMin": 2.5, "ramMin": 1000.2, "tmpdirMin": 10}, "ToolTimeLimit": {"timelimit": 61}},
 "hints": [{"class": "cwltool:CUDARequirement", "cudaVersionMin": "11", "cudaComputeCapability": "7.0"}, {"class": "SomethingElse"}]}`
	tool, err := ParseCommandLineTool(jsonDoc)
	if err != nil {
		t.Fatal(err)
	}
	if !reflect.DeepEqual(tool.BaseCommand, []string{"echo"}) {
		t.Fatalf("baseCommand %v", tool.BaseCommand)
	}
	inputs, outputs, job, problems := ToAiravata(tool)
	var got []string
	for _, in := range inputs {
		got = append(got, in.Name+":"+in.Type+":"+map[bool]string{true: "req", false: "opt"}[in.Required])
	}
	want := []string{"count:INTEGER:opt", "dir:DIRECTORY:req", "files:FILE_LIST:req", "flag:BOOLEAN:opt", "maybe:FLOAT:opt", "msg:STRING:req"}
	if !reflect.DeepEqual(got, want) {
		t.Fatalf("inputs %v", got)
	}
	if inputs[0].Description != "how many" || *inputs[3].Default != "false" || *inputs[4].Default != "1.5" {
		t.Fatalf("details %+v", inputs)
	}
	var outs []string
	for _, o := range outputs {
		outs = append(outs, o.Name+":"+o.Type)
	}
	if !reflect.DeepEqual(outs, []string{"many:FILE_LIST", "out:FILE", "res:DIRECTORY"}) {
		t.Fatalf("outputs %v", outs)
	}
	if !reflect.DeepEqual(job, slurmconv.JobConfig{WallTimeMinutes: 2, Cpus: i32(3), Mem: str("1001M"), Gpus: i32(1)}) {
		t.Fatalf("job %+v", job)
	}
	joined := strings.Join(problems, "\n")
	for _, p := range []string{"tmpdirMin", "SomethingElse", "glob results", "allocation"} {
		if !strings.Contains(joined, p) {
			t.Fatalf("problems missing %q: %v", p, problems)
		}
	}
}

func TestParseCommandLineToolRejects(t *testing.T) {
	base := func(extra string) string {
		return "cwlVersion: v1.2\nclass: CommandLineTool\nbaseCommand: [echo]\n" + extra
	}
	cases := map[string]string{
		"not yaml":         "{{{",
		"workflow":         "cwlVersion: v1.2\nclass: Workflow\ninputs: []\noutputs: []\n",
		"old version":      "cwlVersion: v1.0\nclass: CommandLineTool\ninputs: []\noutputs: []\n",
		"no inputs":        base("outputs: []\n"),
		"no outputs":       base("inputs: []\n"),
		"bad input type":   base("inputs: {x: Any}\noutputs: []\n"),
		"string array":     base("inputs: {x: 'string[]'}\noutputs: []\n"),
		"record type":      base("inputs: {x: {type: record}}\noutputs: []\n"),
		"union":            base("inputs: {x: [int, string]}\noutputs: []\n"),
		"bad output type":  base("inputs: []\noutputs: {o: string}\n"),
		"missing id":       base("inputs: [{type: int}]\noutputs: []\n"),
		"duplicate id":     base("inputs: [{id: a, type: int}, {id: a, type: int}]\noutputs: []\n"),
		"unknown req":      base("inputs: []\noutputs: []\nrequirements: [{class: MPIRequirement}]\n"),
		"req without cls":  base("inputs: []\noutputs: []\nrequirements: [{coresMin: 1}]\n"),
		"undeclared ns":    base("inputs: []\noutputs: []\nhints: [{class: 'cwltool:CUDARequirement'}]\n"),
		"bad base command": base("inputs: []\noutputs: []\nbaseCommand: {a: b}\n"),
	}
	for name, text := range cases {
		t.Run(name, func(t *testing.T) {
			if _, err := ParseCommandLineTool(text); err == nil {
				t.Fatalf("accepted:\n%s", text)
			}
		})
	}
}

func TestMebibytes(t *testing.T) {
	cases := []struct {
		in   string
		want int64
		ok   bool
	}{
		{"4000", 4000, true},
		{"4000M", 4000, true},
		{"4G", 4096, true},
		{"4GB", 4096, true},
		{"1T", 1048576, true},
		{"1536K", 2, true},
		{"0", 0, false},
		{"lots", 0, false},
		{"", 0, false},
	}
	for _, tc := range cases {
		got, ok := mebibytes(tc.in)
		if got != tc.want || ok != tc.ok {
			t.Errorf("mebibytes(%q) = %d, %v", tc.in, got, ok)
		}
	}
}
