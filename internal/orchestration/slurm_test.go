package orchestration

import (
	"strings"
	"testing"

	appmodel "github.com/apache/airavata/api/application/model"
	computemodel "github.com/apache/airavata/api/compute/model"
	model "github.com/apache/airavata/api/process/model"
)

func ptr[T any](v T) *T { return &v }

// scriptFixture builds a run with one file input, one scalar input and one output,
// deployed against a run section that addresses all three by name.
func scriptFixture() (*model.Process, *appmodel.BatchDeployment, *appmodel.Template, *computemodel.SlurmClusterConfig) {
	template := &appmodel.Template{
		ID: "tpl-1",
		Inputs: []appmodel.TemplateInput{
			{ID: "in-file", InputName: ptr("protein"), InputType: ptr(appmodel.TemplateInputTypeFile)},
			{ID: "in-scalar", InputName: ptr("iterations"), InputType: ptr(appmodel.TemplateInputTypeInteger)},
		},
		Outputs: []appmodel.TemplateOutput{
			{ID: "out-file", OutputName: ptr("structure.pdb"), OutputType: ptr(appmodel.TemplateOutputTypeFile)},
		},
	}

	deployment := &appmodel.BatchDeployment{
		ID:               "dep-1",
		TemplateID:       ptr("tpl-1"),
		DefaultPartition: ptr("gpu"),
		SlurmRunSection: "module load alphafold\n" +
			"run_fold --in {{ inputs.protein }} --iterations {{ inputs.iterations }} --out {{ outputs[\"structure.pdb\"] }}",
		DefaultBatchJobConfig: &appmodel.BatchJobConfig{ID: "cfg-default", WallTimeMinutes: 10, Allocation: "unused"},
	}

	process := &model.Process{
		ID:          "proc-1",
		ProcessType: ptr(model.ProcessTypeBatchJob),
		BatchProcess: &model.BatchJobProcess{
			ID:                   "batch-1",
			DeploymentID:         ptr("dep-1"),
			SlurmClusterConfigID: "cfg-1",
			JobName:              ptr("fold run/1"),
			BatchJobConfig: &appmodel.BatchJobConfig{
				ID:              "cfg-run",
				Nodes:           ptr(int32(2)),
				CPUsPerTask:     ptr(int32(8)),
				Mem:             ptr("64G"),
				GPUs:            ptr(int32(4)),
				WallTimeMinutes: 1500,
				Allocation:      "alloc-123",
			},
			InputMappings: []*model.TemplateInputMapping{
				{TemplateInputID: ptr("in-file"), Value: ptr("data-product-9")},
				{TemplateInputID: ptr("in-scalar"), Value: ptr(`{"value": 300}`)},
			},
			OutputMappings: []*model.TemplateOutputMapping{
				{TemplateOutputID: ptr("out-file"), Value: ptr("data-product-10")},
			},
		},
	}

	clusterConfig := &computemodel.SlurmClusterConfig{ID: "cfg-1", WorkRoot: "/scratch/airavata", LoginUser: "dimuthu"}

	return process, deployment, template, clusterConfig
}

func TestBuildSlurmScript(t *testing.T) {
	process, deployment, template, clusterConfig := scriptFixture()

	script, err := buildSlurmScript(process, deployment, template, clusterConfig)
	if err != nil {
		t.Fatalf("buildSlurmScript: %v", err)
	}

	want := []string{
		"#!/bin/bash",
		"#SBATCH --job-name=fold_run_1",
		"#SBATCH --chdir=/scratch/airavata/proc-1",
		"#SBATCH --output=/scratch/airavata/proc-1/proc-1.stdout",
		"#SBATCH --error=/scratch/airavata/proc-1/proc-1.stderr",
		"#SBATCH --time=1-01:00:00",
		"#SBATCH --account=alloc-123",
		"#SBATCH --partition=gpu",
		"#SBATCH --nodes=2",
		"#SBATCH --cpus-per-task=8",
		"#SBATCH --mem=64G",
		"#SBATCH --gpus=4",
		"module load alphafold",
		"run_fold --in /scratch/airavata/proc-1/protein --iterations 300 --out /scratch/airavata/proc-1/structure.pdb",
	}
	for _, line := range want {
		if !strings.Contains(script, line) {
			t.Errorf("script is missing %q\n---\n%s", line, script)
		}
	}

	// A resource the run did not ask for leaves no directive behind, and no blank line
	// where one would have been.
	for _, unwanted := range []string{"--ntasks", "--mem-per-cpu", "--gres", "--constraint", "\n\n\n"} {
		if strings.Contains(script, unwanted) {
			t.Errorf("script should not contain %q\n---\n%s", unwanted, script)
		}
	}
}

// The run's own resource request is what a job is submitted with; the deployment's
// default stands in only when the run carries none.
func TestBuildSlurmScriptFallsBackToDeploymentConfig(t *testing.T) {
	process, deployment, template, clusterConfig := scriptFixture()
	process.BatchProcess.BatchJobConfig = nil
	deployment.DefaultBatchJobConfig.Allocation = "alloc-default"

	script, err := buildSlurmScript(process, deployment, template, clusterConfig)
	if err != nil {
		t.Fatalf("buildSlurmScript: %v", err)
	}
	if !strings.Contains(script, "#SBATCH --account=alloc-default") {
		t.Errorf("expected the deployment's default allocation\n---\n%s", script)
	}
	if !strings.Contains(script, "#SBATCH --time=00:10:00") {
		t.Errorf("expected the deployment's default wall time\n---\n%s", script)
	}
}

// A base work dir on the run overrides the cluster config's work root, and the process
// id still separates one run's directory from another's.
func TestBuildSlurmScriptHonoursBaseWorkDir(t *testing.T) {
	process, deployment, template, clusterConfig := scriptFixture()
	process.BatchProcess.BaseWorkDir = ptr("/projects/fold")

	script, err := buildSlurmScript(process, deployment, template, clusterConfig)
	if err != nil {
		t.Fatalf("buildSlurmScript: %v", err)
	}
	if !strings.Contains(script, "#SBATCH --chdir=/projects/fold/proc-1") {
		t.Errorf("expected the run's own work dir\n---\n%s", script)
	}
}

// A value a user supplied is a value, not template source: a run section is expanded
// once, and what an input carries is never read as Jinja of its own.
func TestBuildSlurmScriptDoesNotReexpandInputValues(t *testing.T) {
	process, deployment, template, clusterConfig := scriptFixture()
	process.BatchProcess.InputMappings[1].Value = ptr(`{"value": "{{ account }}"}`)

	script, err := buildSlurmScript(process, deployment, template, clusterConfig)
	if err != nil {
		t.Fatalf("buildSlurmScript: %v", err)
	}
	if !strings.Contains(script, "--iterations {{ account }}") {
		t.Errorf("expected the input value verbatim\n---\n%s", script)
	}
}

func TestSlurmWallTime(t *testing.T) {
	cases := map[int64]string{
		0:    "00:01:00",
		45:   "00:45:00",
		60:   "01:00:00",
		1500: "1-01:00:00",
		2880: "2-00:00:00",
	}
	for minutes, want := range cases {
		if got := slurmWallTime(minutes); got != want {
			t.Errorf("slurmWallTime(%d) = %q, want %q", minutes, got, want)
		}
	}
}

func TestMappingValue(t *testing.T) {
	cases := []struct {
		raw  *string
		want string
	}{
		{nil, ""},
		{ptr("data-product-9"), "data-product-9"},
		{ptr(`{"value": "/scratch/in.fasta"}`), "/scratch/in.fasta"},
		{ptr(`{"value": 1000000}`), "1000000"},
		{ptr(`{"values": ["a", "b"]}`), "a b"},
		{ptr(`{not json`), `{not json`},
	}
	for _, c := range cases {
		got, err := mappingValue(c.raw)
		if err != nil {
			t.Fatalf("mappingValue: %v", err)
		}
		if got != c.want {
			t.Errorf("mappingValue(%v) = %q, want %q", c.raw, got, c.want)
		}
	}
}

// A directive is one line, so a value that would break out of it is refused rather
// than written into the script.
func TestBuildSlurmScriptRejectsLineBreaks(t *testing.T) {
	process, deployment, template, clusterConfig := scriptFixture()
	process.BatchProcess.BaseWorkDir = ptr("/projects/fold\n#SBATCH --account=someone-else")

	if _, err := buildSlurmScript(process, deployment, template, clusterConfig); err == nil {
		t.Fatal("expected a work dir spanning lines to be refused")
	}
}
