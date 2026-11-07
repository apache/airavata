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
	"bytes"
	"reflect"
	"strings"
	"testing"

	"github.com/flosch/pongo2/v6"
)

func mustParse(t *testing.T, text string) *Script {
	t.Helper()
	s, err := Parse(text)
	if err != nil {
		t.Fatalf("Parse: %v", err)
	}
	return s
}

func TestParseOptionForms(t *testing.T) {
	cases := []struct {
		line     string
		name     string
		value    string
		hasValue bool
	}{
		{"#SBATCH --time=10", "time", "10", true},
		{"#SBATCH --time 10", "time", "10", true},
		{"#SBATCH -t 10", "time", "10", true},
		{"#SBATCH -t10", "time", "10", true},
		{"#SBATCH -N 2", "nodes", "2", true},
		{"#SBATCH -n4", "ntasks", "4", true},
		{"#SBATCH -c 8", "cpus-per-task", "8", true},
		{"#SBATCH -p gpu", "partition", "gpu", true},
		{"#SBATCH -A proj123", "account", "proj123", true},
		{"#SBATCH -J myjob", "job-name", "myjob", true},
		{"#SBATCH -o out.%j", "output", "out.%j", true},
		{"#SBATCH -e err.%j", "error", "err.%j", true},
		{"#SBATCH -G a100:2", "gpus", "a100:2", true},
		{"#SBATCH -C haswell", "constraint", "haswell", true},
		{"#SBATCH -a 1-10", "array", "1-10", true},
		{"#SBATCH -d afterok:12", "dependency", "afterok:12", true},
		{"#SBATCH -q high", "qos", "high", true},
		{"#SBATCH -D /scratch", "chdir", "/scratch", true},
		{"#SBATCH -w node01", "nodelist", "node01", true},
		{"#SBATCH -x node02", "exclude", "node02", true},
		{"#SBATCH -L matlab", "licenses", "matlab", true},
		{"#SBATCH -M cluster2", "clusters", "cluster2", true},
		{"#SBATCH -m block", "distribution", "block", true},
		{"#SBATCH -S 1", "core-spec", "1", true},
		{"#SBATCH -B 2:4:1", "extra-node-info", "2:4:1", true},
		{"#SBATCH -b now+1hour", "begin", "now+1hour", true},
		{"#SBATCH -F hosts.txt", "nodefile", "hosts.txt", true},
		{"#SBATCH -i in.txt", "input", "in.txt", true},
		{"#SBATCH -H", "hold", "", false},
		{"#SBATCH -O", "overcommit", "", false},
		{"#SBATCH -s", "oversubscribe", "", false},
		{"#SBATCH -k", "no-kill", "", false},
		{"#SBATCH --mem=4G", "mem", "4G", true},
		{"#SBATCH --mem 4000", "mem", "4000", true},
		{"#SBATCH --exclusive", "exclusive", "", false},
		{"#SBATCH --exclusive=user", "exclusive", "user", true},
		{"#SBATCH --requeue", "requeue", "", false},
		{"#SBATCH --time=10 # ten minutes", "time", "10", true},
		{"#SBATCH --job-name=\"my job\"", "job-name", "my job", true},
		{"#SBATCH --parti=gpu", "partition", "gpu", true},
		{"#SBATCH --acc=proj", "acc", "proj", true},
		{"#SBATCH\t--qos=low", "qos", "low", true},
	}
	for _, tc := range cases {
		t.Run(tc.line, func(t *testing.T) {
			s := mustParse(t, "#!/bin/bash\n"+tc.line+"\necho hi\n")
			if len(s.Options) != 1 {
				t.Fatalf("got %d options: %+v", len(s.Options), s.Options)
			}
			o := s.Options[0]
			if o.Name != tc.name || o.Value != tc.value || o.HasValue != tc.hasValue || o.Line != 2 || o.Raw != tc.line {
				t.Fatalf("got %+v", o)
			}
		})
	}
}

func TestParseMultipleOptionsAndUnknown(t *testing.T) {
	s := mustParse(t, "#SBATCH -N 2 --ntasks=4 -Hv # comment --qos=x\n#SBATCH --bogus=1 -Z3\nrun\n")
	var got []string
	for _, o := range s.Options {
		got = append(got, o.Name+"="+o.Value)
	}
	want := []string{"nodes=2", "ntasks=4", "hold=", "verbose=", "bogus=1", "Z=3"}
	if !reflect.DeepEqual(got, want) {
		t.Fatalf("got %v want %v", got, want)
	}
	if !s.Options[4].Unknown || !s.Options[5].Unknown {
		t.Fatalf("expected unknown options flagged: %+v", s.Options[4:])
	}
}

func TestParseErrors(t *testing.T) {
	cases := map[string]string{
		"missing value":      "#SBATCH --time\n",
		"missing short":      "#SBATCH -t\n",
		"value on flag":      "#SBATCH --requeue=yes\n",
		"unterminated quote": "#SBATCH --job-name=\"abc\n",
		"stray argument":     "#SBATCH foo\n",
		"carriage return":    "#SBATCH --time=10\rmalicious\n",
	}
	for name, text := range cases {
		t.Run(name, func(t *testing.T) {
			if _, err := Parse(text); err == nil {
				t.Fatalf("expected error for %q", text)
			}
		})
	}
}

func TestParseCRLFAndBlock(t *testing.T) {
	text := "#!/bin/bash\r\n#SBATCH --time=5\r\n\r\n# just a comment\r\n#SBATCH -A acct\r\nmodule load gcc\r\n#SBATCH --qos=ignored\r\necho done\r\n"
	s := mustParse(t, text)
	if s.Shebang != "#!/bin/bash" {
		t.Fatalf("shebang %q", s.Shebang)
	}
	if len(s.Options) != 2 || s.Options[0].Value != "5" || s.Options[1].Value != "acct" {
		t.Fatalf("options %+v", s.Options)
	}
	if !reflect.DeepEqual(s.Ignored, []int{7}) {
		t.Fatalf("ignored %v", s.Ignored)
	}
	if s.Body != "module load gcc\n#SBATCH --qos=ignored\necho done\n" {
		t.Fatalf("body %q", s.Body)
	}
	if strings.Contains(s.Body, "\r") {
		t.Fatal("CR left in body")
	}
	c := Convert(s)
	if len(c.Unsupported) != 0 || !c.Registrable {
		t.Fatalf("late directive should be ignored: %+v %v", c.Unsupported, c.Problems)
	}
}

func TestParseWallTime(t *testing.T) {
	cases := []struct {
		in   string
		want int64
		err  bool
	}{
		{"10", 10, false},
		{"10:30", 11, false},
		{"10:00", 10, false},
		{"01:30:00", 90, false},
		{"01:30:01", 91, false},
		{"2-0", 2880, false},
		{"1-2", 1560, false},
		{"1-2:30", 1590, false},
		{"1-02:30:15", 1591, false},
		{"0:0:30", 1, false},
		{"UNLIMITED", 0, true},
		{"infinite", 0, true},
		{"0", 0, true},
		{"", 0, true},
		{"abc", 0, true},
		{"1:2:3:4", 0, true},
		{"1-2:3:4:5", 0, true},
		{"-5", 0, true},
	}
	for _, tc := range cases {
		got, err := ParseWallTime(tc.in)
		if (err != nil) != tc.err || got != tc.want {
			t.Errorf("ParseWallTime(%q) = %d, %v; want %d, err=%v", tc.in, got, err, tc.want, tc.err)
		}
	}
}

func names(opts []Option) []string {
	var out []string
	for _, o := range opts {
		out = append(out, o.Name)
	}
	return out
}

func TestConvertSplit(t *testing.T) {
	s := mustParse(t, `#!/bin/bash
#SBATCH -J job
#SBATCH -o out.txt
#SBATCH -e err.txt
#SBATCH --chdir=/scratch
#SBATCH --mail-user=a@b.c
#SBATCH --mail-type=ALL
#SBATCH -t 1-00:00:00
#SBATCH -A proj
#SBATCH -p gpu
#SBATCH -N 2
#SBATCH -n 8
#SBATCH --ntasks-per-node=4
#SBATCH -c 2
#SBATCH --mem=16G
#SBATCH --mem-per-cpu=2G
#SBATCH --mem-per-gpu=8G
#SBATCH --cpus-per-gpu=4
#SBATCH --gres=gpu:1
#SBATCH -C skylake
#SBATCH --gpus=4
#SBATCH --gpus-per-node=2
#SBATCH --qos=high
#SBATCH --exclusive
#SBATCH --signal=B:USR1@60

srun ./app
`)
	c := Convert(s)
	if got := names(c.Replaced); !reflect.DeepEqual(got, []string{"job-name", "output", "error", "chdir", "mail-user", "mail-type"}) {
		t.Fatalf("replaced %v", got)
	}
	if got := names(c.Unsupported); !reflect.DeepEqual(got, []string{"qos", "exclusive", "signal"}) {
		t.Fatalf("unsupported %v", got)
	}
	if len(c.Mapped)+len(c.Replaced)+len(c.Unsupported) != len(s.Options) {
		t.Fatal("an option was dropped")
	}
	jc := c.JobConfig
	if jc.WallTimeMinutes != 1440 || jc.Allocation != "proj" || c.Partition != "gpu" || *jc.Nodes != 2 ||
		*jc.Ntasks != 8 || *jc.NtasksPerNode != 4 || *jc.CpusPerTask != 2 || *jc.Mem != "16G" ||
		*jc.MemPerCpu != "2G" || *jc.MemPerGpu != "8G" || *jc.CpusPerGpu != "4" || *jc.Gres != "gpu:1" ||
		*jc.Constraints != "skylake" || *jc.Gpus != 4 || *jc.GpusPerNode != 2 {
		t.Fatalf("job config %+v", jc)
	}
	if c.Registrable {
		t.Fatal("unsupported options should make it unregistrable")
	}
	if c.RunSection != "srun ./app" {
		t.Fatalf("run section %q", c.RunSection)
	}
}

func TestConvertProblems(t *testing.T) {
	cases := []struct {
		name        string
		directives  string
		registrable bool
		problem     string
		unsupported []string
	}{
		{"complete", "#SBATCH -t 10\n#SBATCH -A a\n", true, "", nil},
		{"no time", "#SBATCH -A a\n", false, "no --time: wallTimeMinutes is required", nil},
		{"no account", "#SBATCH -t 10\n", false, "no --account: allocation is required", nil},
		{"partition list", "#SBATCH -t 10\n#SBATCH -A a\n#SBATCH -p a,b\n", false, "a deployment holds one partition", []string{"partition"}},
		{"node range", "#SBATCH -t 10\n#SBATCH -A a\n#SBATCH -N 2-4\n", false, "single positive integer", []string{"nodes"}},
		{"unlimited", "#SBATCH -t UNLIMITED\n#SBATCH -A a\n", false, "unlimited", []string{"time"}},
		{"typed gpus one node", "#SBATCH -t 10\n#SBATCH -A a\n#SBATCH -N 1\n#SBATCH --gpus=a100:2\n", true, "", nil},
		{"typed gpus many nodes", "#SBATCH -t 10\n#SBATCH -A a\n#SBATCH --gpus=a100:2\n", false, "per node", []string{"gpus"}},
		{"typed gpus per node", "#SBATCH -t 10\n#SBATCH -A a\n#SBATCH --gpus-per-node=v100:4\n", true, "", nil},
		{"typed gpus with gres", "#SBATCH -t 10\n#SBATCH -A a\n#SBATCH --gres=foo:1\n#SBATCH --gpus-per-node=v100:4\n", false, "existing --gres", []string{"gpus-per-node"}},
		{"bad gpus", "#SBATCH -t 10\n#SBATCH -A a\n#SBATCH --gpus=a:b:c\n", false, "unrecognised GPU", []string{"gpus"}},
		{"unknown", "#SBATCH -t 10\n#SBATCH -A a\n#SBATCH --frobnicate\n", false, "not an sbatch option", []string{"frobnicate"}},
		{"array", "#SBATCH -t 10\n#SBATCH -A a\n#SBATCH --array=1-3\n", false, "job arrays", []string{"array"}},
		{"bad array", "#SBATCH -t 10\n#SBATCH -A a\n#SBATCH --array=x\n", false, "invalid --array", []string{"array"}},
		{"dependency", "#SBATCH -t 10\n#SBATCH -A a\n#SBATCH -d afterok:1\n", false, "dependencies", []string{"dependency"}},
	}
	for _, tc := range cases {
		t.Run(tc.name, func(t *testing.T) {
			c := Convert(mustParse(t, "#!/bin/bash\n"+tc.directives+"echo\n"))
			if c.Registrable != tc.registrable {
				t.Fatalf("registrable=%v problems=%v", c.Registrable, c.Problems)
			}
			if tc.problem != "" && !strings.Contains(strings.Join(c.Problems, "\n"), tc.problem) {
				t.Fatalf("problems %v missing %q", c.Problems, tc.problem)
			}
			if got := names(c.Unsupported); !reflect.DeepEqual(got, tc.unsupported) {
				t.Fatalf("unsupported %v want %v", got, tc.unsupported)
			}
		})
	}
	c := Convert(mustParse(t, "#SBATCH -t 10\n#SBATCH -A a\n#SBATCH -N 1\n#SBATCH --gpus=a100:2\necho\n"))
	if c.JobConfig.Gres == nil || *c.JobConfig.Gres != "gpu:a100:2" || c.JobConfig.Gpus != nil {
		t.Fatalf("gres %v gpus %v", c.JobConfig.Gres, c.JobConfig.Gpus)
	}
}

func TestArrayAndDependency(t *testing.T) {
	arrays := []struct {
		in      string
		indices []int
		limit   int
		err     bool
	}{
		{"0-3", []int{0, 1, 2, 3}, 0, false},
		{"1,3,5", []int{1, 3, 5}, 0, false},
		{"0-10:5%2", []int{0, 5, 10}, 2, false},
		{"1-2,7", []int{1, 2, 7}, 0, false},
		{"5-1", nil, 0, true},
		{"1:2", nil, 0, true},
		{"1-4%0", nil, 0, true},
	}
	for _, tc := range arrays {
		a, err := parseArray(tc.in)
		if (err != nil) != tc.err {
			t.Fatalf("parseArray(%q) err=%v", tc.in, err)
		}
		if err == nil && (!reflect.DeepEqual(a.Indices(), tc.indices) || a.Limit != tc.limit) {
			t.Fatalf("parseArray(%q) = %v %%%d", tc.in, a.Indices(), a.Limit)
		}
	}
	deps := []struct {
		in   string
		want []Dependency
		err  bool
	}{
		{"afterok:1:2", []Dependency{{Type: "afterok", JobIDs: []string{"1", "2"}}}, false},
		{"afterok:1,afterany:3", []Dependency{{Type: "afterok", JobIDs: []string{"1"}}, {Type: "afterany", JobIDs: []string{"3"}}}, false},
		{"afterok:1?afternotok:2", []Dependency{{Type: "afterok", JobIDs: []string{"1"}, AnyOf: true}, {Type: "afternotok", JobIDs: []string{"2"}, AnyOf: true}}, false},
		{"after:5+10", []Dependency{{Type: "after", JobIDs: []string{"5+10"}}}, false},
		{"singleton", []Dependency{{Type: "singleton"}}, false},
		{"afterok", nil, true},
		{"before:1", nil, true},
		{"afterok:1,afterok:2?afterok:3", nil, true},
	}
	for _, tc := range deps {
		got, err := parseDependency(tc.in)
		if (err != nil) != tc.err || !reflect.DeepEqual(got, tc.want) {
			t.Fatalf("parseDependency(%q) = %+v, %v", tc.in, got, err)
		}
	}
}

func TestSoftwareAndContainers(t *testing.T) {
	c := Convert(mustParse(t, `#!/bin/bash
#SBATCH -t 10
module purge
module load gcc/12.2 openmpi/4.1
ml cuda/12.1
module add python
conda activate myenv
source activate other
spack load gromacs@2023.1
apptainer exec --nv -B /data:/data docker://ghcr.io/x/y:1.0 python run.py
srun -n 4 singularity run /images/tool.sif --flag
echo apptainer exec not-a-container # comment
`))
	var sw []string
	for _, s := range c.Software {
		sw = append(sw, s.Manager+":"+s.Name+"@"+s.Version)
	}
	want := []string{"module:gcc@12.2", "module:openmpi@4.1", "module:cuda@12.1", "module:python@",
		"conda:myenv@", "conda:other@", "spack:gromacs@2023.1"}
	if !reflect.DeepEqual(sw, want) {
		t.Fatalf("software %v", sw)
	}
	if c.Software[0].Line != 4 {
		t.Fatalf("line %d", c.Software[0].Line)
	}
	if len(c.Containers) != 2 ||
		c.Containers[0] != (Container{Runtime: "apptainer", Image: "docker://ghcr.io/x/y:1.0", Command: "python run.py", Line: 10}) ||
		c.Containers[1].Image != "/images/tool.sif" || c.Containers[1].Runtime != "singularity" {
		t.Fatalf("containers %+v", c.Containers)
	}
}

func TestRenderDiffOnlyReplaced(t *testing.T) {
	original := `#!/bin/bash
#SBATCH --job-name=fold
#SBATCH --time=01:30:00
#SBATCH --account=abc123
#SBATCH --partition=gpu
#SBATCH --output=out.log
#SBATCH --nodes=1
#SBATCH --cpus-per-task=8
#SBATCH --mem=32G
#SBATCH --gpus=2

module load alphafold
run_fold --in protein.fa
`
	c := Convert(mustParse(t, original))
	rendered := Render(c)
	diff := Diff(original, rendered)
	want := `--- original
+++ airavata
@@ -1,8 +1,6 @@
 #!/bin/bash
-#SBATCH --job-name=fold
 #SBATCH --time=01:30:00
 #SBATCH --account=abc123
 #SBATCH --partition=gpu
-#SBATCH --output=out.log
 #SBATCH --nodes=1
 #SBATCH --cpus-per-task=8
`
	if diff != want {
		t.Fatalf("diff:\n%s\nrendered:\n%s", diff, rendered)
	}
	if Diff(rendered, Render(Convert(mustParse(t, rendered)))) != "" {
		t.Fatal("rendering is not stable")
	}
}

func TestRenderUnsupportedAndInputs(t *testing.T) {
	c := Convert(mustParse(t, "#!/bin/bash\n#SBATCH -t 5\n#SBATCH -A a\n#SBATCH --qos=\"long q\"\n#SBATCH --exclusive\n#SBATCH -Z9\n\nrun --in data.txt\n"))
	if _, err := ApplyInputs(c, []InputSuggestion{{Name: "data", Type: "FILE", Literal: "data.txt"}}); err != nil {
		t.Fatal(err)
	}
	got := Render(c)
	want := `#!/bin/bash
#SBATCH --time=00:05:00
#SBATCH --account=a
#SBATCH --qos="long q" # not registrable
#SBATCH --exclusive # not registrable
#SBATCH -Z9 # not registrable

run --in {{ inputs.data[0] }}
`
	if got != want {
		t.Fatalf("got:\n%s", got)
	}
}

func TestDiff(t *testing.T) {
	cases := []struct {
		a, b, want string
	}{
		{"x\ny\n", "x\ny\n", ""},
		{"", "a\n", "--- original\n+++ airavata\n@@ -0,0 +1 @@\n+a\n"},
		{"1\n2\n3\n4\n5\n6\n7\n8\n9\n", "1\n2\n3\nX\n5\n6\n7\n8\nY\n",
			"--- original\n+++ airavata\n@@ -2,8 +2,8 @@\n 2\n 3\n-4\n+X\n 5\n 6\n 7\n 8\n-9\n+Y\n"},
		{"1\n2\n3\n4\n5\n6\n7\n8\n9\n10\n", "X\n2\n3\n4\n5\n6\n7\n8\n9\nY\n",
			"--- original\n+++ airavata\n@@ -1,3 +1,3 @@\n-1\n+X\n 2\n 3\n@@ -8,3 +8,3 @@\n 8\n 9\n-10\n+Y\n"},
		{"a\r\nb\r\n", "a\nb\n", ""},
	}
	for i, tc := range cases {
		if got := Diff(tc.a, tc.b); got != tc.want {
			t.Errorf("case %d:\n%s\nwant:\n%s", i, got, tc.want)
		}
	}
}

func TestApplyInputsValidation(t *testing.T) {
	body := "#!/bin/bash\n#SBATCH -t 5\n#SBATCH -A a\nrun --in protein.fa --n 300 --out out/protein.fa\n"
	cases := []struct {
		name string
		in   []InputSuggestion
		want string
	}{
		{"bad name", []InputSuggestion{{Name: "1x", Type: "STRING", Literal: "300"}}, `suggestion 1 ("1x"): name must match`},
		{"bad name dash", []InputSuggestion{{Name: "a-b", Type: "STRING", Literal: "300"}}, "name must match"},
		{"duplicate", []InputSuggestion{{Name: "n", Type: "INTEGER", Literal: "300"}, {Name: "n", Type: "FILE", Literal: "protein.fa"}}, `suggestion 2 ("n"): duplicate`},
		{"bad type", []InputSuggestion{{Name: "n", Type: "NUMBER", Literal: "300"}}, `type "NUMBER"`},
		{"empty literal", []InputSuggestion{{Name: "n", Type: "INTEGER"}}, "literal is empty"},
		{"same literal", []InputSuggestion{{Name: "a", Type: "INTEGER", Literal: "300"}, {Name: "b", Type: "INTEGER", Literal: "300"}}, "already claimed"},
		{"absent", []InputSuggestion{{Name: "n", Type: "INTEGER", Literal: "400"}}, `suggestion 1 ("n"): literal "400" does not appear`},
		{"substring only", []InputSuggestion{{Name: "n", Type: "INTEGER", Literal: "30"}}, "does not appear as a whole word"},
	}
	for _, tc := range cases {
		t.Run(tc.name, func(t *testing.T) {
			c := Convert(mustParse(t, body))
			before := c.RunSection
			_, err := ApplyInputs(c, tc.in)
			if err == nil || !strings.Contains(err.Error(), tc.want) {
				t.Fatalf("err %v, want %q", err, tc.want)
			}
			if c.RunSection != before {
				t.Fatal("run section changed on error")
			}
		})
	}
}

func renderRunSection(t *testing.T, source string, ctx pongo2.Context) string {
	t.Helper()
	tpl, err := pongo2.FromString("{% autoescape off %}" + source + "{% endautoescape %}")
	if err != nil {
		t.Fatalf("parse %q: %v", source, err)
	}
	tpl.Options.TrimBlocks = true
	tpl.Options.LStripBlocks = true
	var out bytes.Buffer
	if err := tpl.ExecuteWriter(ctx, &out); err != nil {
		t.Fatalf("execute: %v", err)
	}
	return out.String()
}

func TestApplyInputsPongoRoundTrip(t *testing.T) {
	body := `#!/bin/bash
#SBATCH -t 5
#SBATCH -A a
arr=(a b c)
echo "${#arr[@]} {{ not a var }} {% raw %} {#x"
{ echo grouped; }
x={protein.fa
run --in protein.fa --list protein.fa.bak data/protein.fa --n 300 --model model_v2 -n=300
cp protein.fa}} out/ && echo {{300}}
tar xf inputs.tar
`
	cases := []struct {
		name string
		in   []InputSuggestion
		want map[string]int
	}{
		{"none", nil, map[string]int{}},
		{"overlapping", []InputSuggestion{
			{Name: "protein", Type: "FILE", Literal: "protein.fa"},
			{Name: "backup", Type: "FILE", Literal: "protein.fa.bak"},
			{Name: "n", Type: "INTEGER", Literal: "300"},
			{Name: "model", Type: "STRING", Literal: "model_v2"},
			{Name: "tarball", Type: "FILE_LIST", Literal: "inputs.tar"},
		}, map[string]int{"protein": 3, "backup": 1, "n": 3, "model": 1, "tarball": 1}},
	}
	for _, tc := range cases {
		t.Run(tc.name, func(t *testing.T) {
			c := Convert(mustParse(t, body))
			applied, err := ApplyInputs(c, tc.in)
			if err != nil {
				t.Fatal(err)
			}
			ctx := pongo2.Context{"inputs": map[string][]string{}}
			got := map[string]int{}
			for _, a := range applied {
				got[a.Name] = a.Occurrences
				ctx["inputs"].(map[string][]string)[a.Name] = []string{a.Default}
				if a.Default != literalFor(tc.in, a.Name) {
					t.Fatalf("default %q", a.Default)
				}
			}
			if !reflect.DeepEqual(got, tc.want) {
				t.Fatalf("occurrences %v want %v", got, tc.want)
			}
			if out := renderRunSection(t, c.RunSection, ctx); out != c.body {
				t.Fatalf("round trip mismatch\nsource:\n%s\ngot:\n%s\nwant:\n%s", c.RunSection, out, c.body)
			}
			if len(tc.in) > 0 && !strings.Contains(c.RunSection, `--in {{ inputs.protein[0] }} --list {{ inputs.backup[0] }} data/protein.fa`) {
				t.Fatalf("unexpected substitution:\n%s", c.RunSection)
			}
		})
	}
}

func TestApplyInputsFileListJoin(t *testing.T) {
	c := Convert(mustParse(t, "#SBATCH -t 5\n#SBATCH -A a\ncat {a.txt}\n"))
	applied, err := ApplyInputs(c, []InputSuggestion{{Name: "files", Type: "FILE_LIST", Literal: "a.txt"}})
	if err != nil {
		t.Fatal(err)
	}
	if applied[0].Note == "" {
		t.Fatal("expected a FILE_LIST note")
	}
	out := renderRunSection(t, c.RunSection, pongo2.Context{"inputs": map[string][]string{"files": {"x.txt", "y.txt"}}})
	if out != "cat {x.txt y.txt}" {
		t.Fatalf("got %q from %q", out, c.RunSection)
	}
}

func literalFor(in []InputSuggestion, name string) string {
	for _, s := range in {
		if s.Name == name {
			return s.Literal
		}
	}
	return ""
}
