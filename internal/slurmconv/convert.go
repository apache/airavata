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

package slurmconv

import (
	"errors"
	"fmt"
	"strconv"
	"strings"
)

// JobConfig mirrors the API's batch job config fields.
type JobConfig struct {
	WallTimeMinutes int64   `json:"wallTimeMinutes"`
	Allocation      string  `json:"allocation"`
	Cpus            *int32  `json:"cpus,omitempty"`
	Nodes           *int32  `json:"nodes,omitempty"`
	Ntasks          *int32  `json:"ntasks,omitempty"`
	NtasksPerNode   *int32  `json:"ntasksPerNode,omitempty"`
	CpusPerTask     *int32  `json:"cpusPerTask,omitempty"`
	Gpus            *int32  `json:"gpus,omitempty"`
	GpusPerNode     *int32  `json:"gpusPerNode,omitempty"`
	Mem             *string `json:"mem,omitempty"`
	MemPerCpu       *string `json:"memPerCpu,omitempty"`
	MemPerGpu       *string `json:"memPerGpu,omitempty"`
	CpusPerGpu      *string `json:"cpusPerGpu,omitempty"`
	Gres            *string `json:"gres,omitempty"`
	Constraints     *string `json:"constraints,omitempty"`
}

type ArrayRange struct {
	Start int `json:"start"`
	End   int `json:"end"`
	Step  int `json:"step"`
}

// ArraySpec is a parsed --array value.
type ArraySpec struct {
	Raw    string       `json:"raw"`
	Ranges []ArrayRange `json:"ranges"`
	Limit  int          `json:"limit,omitempty"`
}

// Indices expands the ranges in order.
func (a *ArraySpec) Indices() []int {
	var out []int
	for _, r := range a.Ranges {
		for i := r.Start; i <= r.End; i += r.Step {
			out = append(out, i)
		}
	}
	return out
}

// Dependency is one clause of --dependency; AnyOf is set when clauses are joined with '?'.
type Dependency struct {
	Type   string   `json:"type"`
	JobIDs []string `json:"jobIds,omitempty"`
	AnyOf  bool     `json:"anyOf,omitempty"`
}

type SoftwareRequirement struct {
	Manager string `json:"manager"`
	Name    string `json:"name"`
	Version string `json:"version,omitempty"`
	Line    int    `json:"line"`
}

type Container struct {
	Runtime string `json:"runtime"`
	Image   string `json:"image"`
	Command string `json:"command,omitempty"`
	Line    int    `json:"line"`
}

// Segment is a piece of the run section: literal shell text, or a reference to an input.
type Segment struct {
	Text      string `json:"text,omitempty"`
	Input     string `json:"input,omitempty"`
	InputType string `json:"inputType,omitempty"`
}

type Conversion struct {
	JobConfig    JobConfig             `json:"jobConfig"`
	Partition    string                `json:"partition,omitempty"`
	RunSection   string                `json:"runSection"`
	Shebang      string                `json:"shebang,omitempty"`
	Mapped       []Option              `json:"mapped"`
	Replaced     []Option              `json:"replaced"`
	Unsupported  []Option              `json:"unsupported"`
	Array        *ArraySpec            `json:"array,omitempty"`
	Dependencies []Dependency          `json:"dependencies,omitempty"`
	Software     []SoftwareRequirement `json:"software,omitempty"`
	Containers   []Container           `json:"containers,omitempty"`
	Registrable  bool                  `json:"registrable"`
	Problems     []string              `json:"problems,omitempty"`
	Notes        []string              `json:"notes,omitempty"`

	body     string
	segments []Segment
}

var replacedNotes = map[string]string{
	"job-name":  "Airavata names the job after the process id",
	"chdir":     "Airavata runs the job in its own per-process working directory",
	"output":    "Airavata writes stdout to stdout.txt in the working directory",
	"error":     "Airavata writes stderr to stderr.txt in the working directory",
	"mail-user": "Airavata sets the notification address it monitors",
	"mail-type": "Airavata sets the notification types it monitors",
}

// Convert maps a parsed script onto the Airavata deployment model.
func Convert(s *Script) *Conversion {
	c := &Conversion{Shebang: s.Shebang}
	jc := &c.JobConfig
	seen := map[string]int{}
	var gpus, gpusPerNode *Option

	unsupported := func(o Option, why string) {
		c.Unsupported = append(c.Unsupported, o)
		c.Problems = append(c.Problems, fmt.Sprintf("%s (line %d): %s", describe(o), o.Line, why))
	}
	setInt := func(o Option, dst **int32) {
		n, err := strconv.ParseInt(strings.TrimSpace(o.Value), 10, 32)
		if err != nil || n <= 0 {
			unsupported(o, "Airavata needs a single positive integer here")
			return
		}
		v := int32(n)
		*dst = &v
		c.Mapped = append(c.Mapped, o)
	}
	setStr := func(o Option, dst **string) {
		v := o.Value
		*dst = &v
		c.Mapped = append(c.Mapped, o)
	}

	for i := range s.Options {
		o := s.Options[i]
		if prev, ok := seen[o.Name]; ok && !o.Unknown {
			c.Notes = append(c.Notes, fmt.Sprintf("--%s appears on lines %d and %d; sbatch uses the last one", o.Name, prev, o.Line))
		}
		seen[o.Name] = o.Line
		if o.Unknown {
			unsupported(o, "not an sbatch option")
			continue
		}
		if note, ok := replacedNotes[o.Name]; ok {
			c.Replaced = append(c.Replaced, o)
			c.Notes = append(c.Notes, fmt.Sprintf("%s (line %d) dropped: %s", describe(o), o.Line, note))
			continue
		}
		switch o.Name {
		case "time":
			m, err := ParseWallTime(o.Value)
			if err != nil {
				unsupported(o, err.Error())
				continue
			}
			jc.WallTimeMinutes = m
			c.Mapped = append(c.Mapped, o)
		case "account":
			jc.Allocation = o.Value
			c.Mapped = append(c.Mapped, o)
		case "partition":
			if strings.Contains(o.Value, ",") {
				c.Partition = ""
				unsupported(o, "a deployment holds one partition; pick one of "+o.Value)
				continue
			}
			c.Partition = o.Value
			c.Mapped = append(c.Mapped, o)
		case "nodes":
			setInt(o, &jc.Nodes)
		case "ntasks":
			setInt(o, &jc.Ntasks)
		case "ntasks-per-node":
			setInt(o, &jc.NtasksPerNode)
		case "cpus-per-task":
			setInt(o, &jc.CpusPerTask)
		case "mem":
			setStr(o, &jc.Mem)
		case "mem-per-cpu":
			setStr(o, &jc.MemPerCpu)
		case "mem-per-gpu":
			setStr(o, &jc.MemPerGpu)
		case "cpus-per-gpu":
			setStr(o, &jc.CpusPerGpu)
		case "gres":
			setStr(o, &jc.Gres)
		case "constraint":
			setStr(o, &jc.Constraints)
		case "gpus":
			gpus = &s.Options[i]
		case "gpus-per-node":
			gpusPerNode = &s.Options[i]
		case "array":
			a, err := parseArray(o.Value)
			if err != nil {
				unsupported(o, err.Error())
				continue
			}
			c.Array = a
			unsupported(o, "job arrays cannot be registered; each run is one job")
		case "dependency":
			d, err := parseDependency(o.Value)
			if err != nil {
				unsupported(o, err.Error())
				continue
			}
			c.Dependencies = d
			unsupported(o, "job dependencies cannot be registered")
		default:
			unsupported(o, "no Airavata batch job config field")
		}
	}

	if gpusPerNode != nil {
		c.convertGPU(*gpusPerNode, &jc.GpusPerNode, true, unsupported)
	}
	if gpus != nil {
		c.convertGPU(*gpus, &jc.Gpus, jc.Nodes != nil && *jc.Nodes == 1, unsupported)
	}

	if jc.WallTimeMinutes <= 0 {
		c.Problems = append(c.Problems, "no --time: wallTimeMinutes is required")
	}
	if strings.TrimSpace(jc.Allocation) == "" {
		c.Problems = append(c.Problems, "no --account: allocation is required")
	}
	for _, line := range s.Ignored {
		c.Notes = append(c.Notes, fmt.Sprintf("line %d: #SBATCH after the first command is ignored by sbatch and stays in the run section", line))
	}

	c.body = strings.TrimRight(strings.TrimLeft(s.Body, "\n"), "\n\t ")
	c.segments = []Segment{{Text: c.body}}
	c.RunSection = templateSource(c.segments)
	c.Software, c.Containers = scanBody(s.Body, bodyStartLine(s))
	c.Registrable = len(c.Unsupported) == 0 && c.Array == nil && len(c.Dependencies) == 0 &&
		jc.WallTimeMinutes > 0 && strings.TrimSpace(jc.Allocation) != ""
	return c
}

func (c *Conversion) convertGPU(o Option, count **int32, typedLossless bool, unsupported func(Option, string)) {
	v := strings.TrimSpace(o.Value)
	if n, err := strconv.ParseInt(v, 10, 32); err == nil && n > 0 {
		x := int32(n)
		*count = &x
		c.Mapped = append(c.Mapped, o)
		return
	}
	kind, num, ok := strings.Cut(v, ":")
	if n, err := strconv.ParseInt(num, 10, 32); !ok || err != nil || n <= 0 || kind == "" || strings.Contains(num, ":") {
		unsupported(o, "unrecognised GPU request")
		return
	}
	if c.JobConfig.Gres != nil {
		unsupported(o, "typed GPU request cannot be merged with the existing --gres")
		return
	}
	if !typedLossless {
		unsupported(o, "--gpus counts GPUs per job but --gres is per node; set --nodes=1 or use --gpus-per-node")
		return
	}
	g := "gpu:" + kind + ":" + num
	c.JobConfig.Gres = &g
	c.Mapped = append(c.Mapped, o)
	c.Notes = append(c.Notes, fmt.Sprintf("%s (line %d) stored as gres %s", describe(o), o.Line, g))
}

func bodyStartLine(s *Script) int {
	n := 1
	if s.Shebang != "" {
		n = 2
	}
	for _, o := range s.Options {
		if o.Line+1 > n {
			n = o.Line + 1
		}
	}
	return n
}

func describe(o Option) string {
	if o.HasValue {
		return "--" + o.Name + "=" + o.Value
	}
	return "--" + o.Name
}

// ParseWallTime converts any sbatch --time form to whole minutes, rounding seconds up.
func ParseWallTime(v string) (int64, error) {
	v = strings.TrimSpace(v)
	switch strings.ToUpper(v) {
	case "UNLIMITED", "INFINITE", "-1":
		return 0, errors.New("unlimited wall time cannot be registered; wallTimeMinutes must be positive")
	case "":
		return 0, errors.New("empty --time")
	}
	num := func(s string) (int64, error) {
		if s == "" {
			return 0, fmt.Errorf("invalid --time %q", v)
		}
		for _, r := range s {
			if r < '0' || r > '9' {
				return 0, fmt.Errorf("invalid --time %q", v)
			}
		}
		return strconv.ParseInt(s, 10, 64)
	}
	var days, hours, mins, secs int64
	rest := v
	if d, r, ok := strings.Cut(v, "-"); ok {
		n, err := num(d)
		if err != nil {
			return 0, err
		}
		days, rest = n, r
		parts := strings.Split(rest, ":")
		if len(parts) > 3 {
			return 0, fmt.Errorf("invalid --time %q", v)
		}
		vals := make([]int64, 3)
		for i, p := range parts {
			if vals[i], err = num(p); err != nil {
				return 0, err
			}
		}
		hours, mins, secs = vals[0], vals[1], vals[2]
	} else {
		parts := strings.Split(rest, ":")
		vals := make([]int64, len(parts))
		for i, p := range parts {
			n, err := num(p)
			if err != nil {
				return 0, err
			}
			vals[i] = n
		}
		switch len(vals) {
		case 1:
			mins = vals[0]
		case 2:
			mins, secs = vals[0], vals[1]
		case 3:
			hours, mins, secs = vals[0], vals[1], vals[2]
		default:
			return 0, fmt.Errorf("invalid --time %q", v)
		}
	}
	total := days*24*60 + hours*60 + mins + (secs+59)/60
	if total <= 0 {
		return 0, errors.New("--time of zero means no limit; wallTimeMinutes must be positive")
	}
	return total, nil
}

func formatWallTime(minutes int64) string {
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

func parseArray(v string) (*ArraySpec, error) {
	a := &ArraySpec{Raw: v}
	list := v
	if i := strings.LastIndex(v, "%"); i >= 0 {
		n, err := strconv.Atoi(v[i+1:])
		if err != nil || n <= 0 {
			return nil, fmt.Errorf("invalid --array limit in %q", v)
		}
		a.Limit, list = n, v[:i]
	}
	for _, item := range strings.Split(list, ",") {
		r := ArrayRange{Step: 1}
		span, step, hasStep := strings.Cut(item, ":")
		lo, hi, isRange := strings.Cut(span, "-")
		var err error
		if r.Start, err = strconv.Atoi(lo); err != nil || r.Start < 0 {
			return nil, fmt.Errorf("invalid --array element %q", item)
		}
		r.End = r.Start
		if isRange {
			if r.End, err = strconv.Atoi(hi); err != nil || r.End < r.Start {
				return nil, fmt.Errorf("invalid --array element %q", item)
			}
		}
		if hasStep {
			if !isRange {
				return nil, fmt.Errorf("invalid --array element %q: a step needs a range", item)
			}
			if r.Step, err = strconv.Atoi(step); err != nil || r.Step <= 0 {
				return nil, fmt.Errorf("invalid --array step in %q", item)
			}
		}
		a.Ranges = append(a.Ranges, r)
	}
	return a, nil
}

var dependencyTypes = map[string]bool{
	"after": true, "afterany": true, "afterburstbuffer": true, "aftercorr": true,
	"afternotok": true, "afterok": true, "singleton": true,
}

func parseDependency(v string) ([]Dependency, error) {
	sep, anyOf := ",", false
	if strings.Contains(v, "?") {
		if strings.Contains(v, ",") {
			return nil, fmt.Errorf("--dependency %q mixes ',' and '?'", v)
		}
		sep, anyOf = "?", true
	}
	var out []Dependency
	for _, clause := range strings.Split(v, sep) {
		parts := strings.Split(clause, ":")
		if !dependencyTypes[parts[0]] {
			return nil, fmt.Errorf("unknown dependency type %q", parts[0])
		}
		d := Dependency{Type: parts[0], AnyOf: anyOf}
		for _, id := range parts[1:] {
			if id == "" {
				return nil, fmt.Errorf("empty job id in --dependency %q", v)
			}
			d.JobIDs = append(d.JobIDs, id)
		}
		if d.Type == "singleton" && len(d.JobIDs) > 0 {
			return nil, fmt.Errorf("singleton dependency takes no job ids")
		}
		if d.Type != "singleton" && len(d.JobIDs) == 0 {
			return nil, fmt.Errorf("dependency %s names no job ids", d.Type)
		}
		out = append(out, d)
	}
	return out, nil
}

var containerValueFlags = map[string]bool{
	"-B": true, "--bind": true, "--mount": true, "-H": true, "--home": true, "--env": true,
	"--env-file": true, "-W": true, "--workdir": true, "--pwd": true, "-o": true, "--overlay": true,
	"--security": true, "--network": true, "--network-args": true, "--hostname": true, "--app": true,
	"-S": true, "--scratch": true, "--fusemount": true, "--dns": true, "--add-caps": true,
	"--drop-caps": true, "--apply-cgroups": true, "--pem-path": true, "--cpus": true, "--memory": true,
	"--memory-reservation": true, "--memory-swap": true, "--cpu-shares": true, "--cpuset-cpus": true,
	"--cpuset-mems": true, "--blkio-weight": true, "--pids-limit": true, "--config": true,
}

var lmodSubcommands = map[string]bool{
	"purge": true, "list": true, "avail": true, "unload": true, "spider": true, "show": true,
	"help": true, "save": true, "restore": true, "swap": true, "use": true, "unuse": true,
	"keyword": true, "whatis": true, "reset": true, "load": true, "add": true,
}

func scanBody(body string, firstLine int) ([]SoftwareRequirement, []Container) {
	var sw []SoftwareRequirement
	var cs []Container
	for i, line := range strings.Split(body, "\n") {
		lineNo := firstLine + i
		words, err := splitWords(strings.TrimSpace(line), true)
		if err != nil {
			continue
		}
		for _, cmd := range splitCommands(words) {
			sw = append(sw, softwareIn(cmd, lineNo)...)
			if ct, ok := containerIn(cmd, lineNo); ok {
				cs = append(cs, ct)
			}
		}
	}
	return sw, cs
}

func splitCommands(words []string) [][]string {
	var out [][]string
	var cur []string
	for _, w := range words {
		switch w {
		case ";", "&&", "||", "|", "&":
			out = append(out, cur)
			cur = nil
			continue
		}
		if strings.HasSuffix(w, ";") {
			cur = append(cur, strings.TrimSuffix(w, ";"))
			out = append(out, cur)
			cur = nil
			continue
		}
		cur = append(cur, w)
	}
	return append(out, cur)
}

func softwareIn(cmd []string, line int) []SoftwareRequirement {
	if len(cmd) < 2 {
		return nil
	}
	var out []SoftwareRequirement
	args := func(from int) []string {
		var a []string
		for _, w := range cmd[from:] {
			if !strings.HasPrefix(w, "-") && w != "" {
				a = append(a, w)
			}
		}
		return a
	}
	switch {
	case cmd[0] == "module" && (cmd[1] == "load" || cmd[1] == "add" || cmd[1] == "try-load"):
		for _, m := range args(2) {
			name, ver, _ := strings.Cut(m, "/")
			out = append(out, SoftwareRequirement{Manager: "module", Name: name, Version: ver, Line: line})
		}
	case cmd[0] == "ml" && !lmodSubcommands[cmd[1]]:
		for _, m := range args(1) {
			name, ver, _ := strings.Cut(m, "/")
			out = append(out, SoftwareRequirement{Manager: "module", Name: name, Version: ver, Line: line})
		}
	case cmd[0] == "spack" && cmd[1] == "load":
		for _, m := range args(2) {
			name, ver, _ := strings.Cut(m, "@")
			out = append(out, SoftwareRequirement{Manager: "spack", Name: name, Version: ver, Line: line})
		}
	case (cmd[0] == "conda" || cmd[0] == "mamba" || cmd[0] == "micromamba" || cmd[0] == "source" || cmd[0] == ".") && cmd[1] == "activate":
		if a := args(2); len(a) > 0 {
			out = append(out, SoftwareRequirement{Manager: "conda", Name: a[0], Line: line})
		}
	}
	return out
}

var launchers = map[string]bool{"srun": true, "mpirun": true, "mpiexec": true, "ibrun": true, "exec": true, "time": true, "env": true, "nohup": true}

func launchedDirectly(prefix []string) bool {
	for _, w := range prefix {
		if _, err := strconv.Atoi(w); err != nil && !launchers[w] && !strings.HasPrefix(w, "-") && !strings.Contains(w, "=") {
			return false
		}
	}
	return true
}

func containerIn(cmd []string, line int) (Container, bool) {
	for i, w := range cmd {
		base := w[strings.LastIndex(w, "/")+1:]
		if base != "apptainer" && base != "singularity" {
			continue
		}
		if i+1 >= len(cmd) || (cmd[i+1] != "exec" && cmd[i+1] != "run") || !launchedDirectly(cmd[:i]) {
			continue
		}
		for j := i + 2; j < len(cmd); j++ {
			a := cmd[j]
			if strings.HasPrefix(a, "-") {
				if !strings.Contains(a, "=") && containerValueFlags[a] {
					j++
				}
				continue
			}
			return Container{Runtime: base, Image: a, Command: strings.Join(cmd[j+1:], " "), Line: line}, true
		}
	}
	return Container{}, false
}
