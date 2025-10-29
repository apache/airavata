package services

import (
	"fmt"
	"strings"
	"text/template"
	"time"

	"github.com/apache/airavata/scheduler/core/domain"
)

// ScriptGenerator handles generation of runtime-specific scripts
type ScriptGenerator struct {
	config *ScriptGeneratorConfig
}

// ScriptGeneratorConfig contains configuration for script generation
type ScriptGeneratorConfig struct {
	WorkerBinaryURL   string
	MicromambaURL     string
	DefaultWorkingDir string
	DefaultTimeout    time.Duration
	ServerGRPCAddress string
	ServerGRPCPort    int
}

// NewScriptGenerator creates a new script generator
func NewScriptGenerator(config *ScriptGeneratorConfig) *ScriptGenerator {
	if config == nil {
		config = &ScriptGeneratorConfig{
			WorkerBinaryURL:   "https://server/api/worker-binary",
			MicromambaURL:     "https://micro.mamba.pm/api/micromamba/linux-64/latest",
			DefaultWorkingDir: "/tmp/worker",
			DefaultTimeout:    24 * time.Hour,
			ServerGRPCAddress: "scheduler", // Use service name for container-to-container communication
			ServerGRPCPort:    50051,
		}
	}
	return &ScriptGenerator{config: config}
}

// GenerateTaskExecutionScript generates a script to execute a task with micromamba
func (sg *ScriptGenerator) GenerateTaskExecutionScript(
	task *domain.Task,
	dependencies []string,
	command string,
) (string, error) {
	tmpl := `#!/bin/bash
set -euo pipefail

# Task execution script for task {{.TaskID}}
# Generated at {{.GeneratedAt}}

# Set up logging
LOG_FILE="/tmp/task_{{.TaskID}}.log"
exec > >(tee -a "$LOG_FILE")
exec 2>&1

echo "Starting task execution: {{.TaskID}}"
echo "Command: {{.Command}}"
echo "Dependencies: {{.Dependencies}}"

# Create working directory
WORK_DIR="{{.WorkingDir}}/{{.TaskID}}"
mkdir -p "$WORK_DIR"
cd "$WORK_DIR"

# Download and install micromamba if not present
if [ ! -f "./bin/micromamba" ]; then
    echo "Downloading micromamba..."
    curl -Ls "{{.MicromambaURL}}" | tar -xvj bin/micromamba
    chmod +x ./bin/micromamba
fi

# Create conda environment for this task
ENV_NAME="task_{{.TaskID}}"
echo "Creating conda environment: $ENV_NAME"

# Create environment
./bin/micromamba create -n "$ENV_NAME" -y

# Install dependencies if any
{{if .Dependencies}}
echo "Installing dependencies..."
./bin/micromamba install -n "$ENV_NAME" -y {{.Dependencies}}
{{end}}

# Set up input file symlinks
{{range .InputFiles}}
if [ -f "{{.SourcePath}}" ]; then
    ln -sf "{{.SourcePath}}" "{{.TargetPath}}"
    echo "Linked input file: {{.TargetPath}}"
else
    echo "Warning: Input file not found: {{.SourcePath}}"
fi
{{end}}

# Create output directory
mkdir -p "{{.OutputDir}}"

# Execute the command in the conda environment
echo "Executing command in environment: $ENV_NAME"
./bin/micromamba run -n "$ENV_NAME" bash -c "{{.Command}}"

# Verify output files exist
{{range .OutputFiles}}
if [ -f "{{.Path}}" ]; then
    echo "Output file created: {{.Path}}"
    ls -la "{{.Path}}"
else
    echo "Warning: Expected output file not found: {{.Path}}"
fi
{{end}}

echo "Task execution completed: {{.TaskID}}"
`

	data := struct {
		TaskID        string
		GeneratedAt   string
		Command       string
		Dependencies  string
		WorkingDir    string
		MicromambaURL string
		InputFiles    []FileLink
		OutputDir     string
		OutputFiles   []domain.FileMetadata
	}{
		TaskID:        task.ID,
		GeneratedAt:   time.Now().Format(time.RFC3339),
		Command:       command,
		Dependencies:  strings.Join(dependencies, " "),
		WorkingDir:    sg.config.DefaultWorkingDir,
		MicromambaURL: sg.config.MicromambaURL,
		InputFiles:    sg.generateInputFileLinks(task.InputFiles),
		OutputDir:     "/tmp/outputs",
		OutputFiles:   task.OutputFiles,
	}

	t, err := template.New("task_execution").Parse(tmpl)
	if err != nil {
		return "", fmt.Errorf("failed to parse task execution template: %w", err)
	}

	var buf strings.Builder
	if err := t.Execute(&buf, data); err != nil {
		return "", fmt.Errorf("failed to execute task execution template: %w", err)
	}

	return buf.String(), nil
}

// SubstituteParametersInScript substitutes parameter values in a script template
func (sg *ScriptGenerator) SubstituteParametersInScript(template string, parameters map[string]string) string {
	result := template
	for key, value := range parameters {
		placeholder := fmt.Sprintf("{{%s}}", key)
		result = strings.ReplaceAll(result, placeholder, value)
	}
	return result
}

// FileLink represents a link between source and target file paths
type FileLink struct {
	SourcePath string
	TargetPath string
}

// generateInputFileLinks creates file links for input files
func (sg *ScriptGenerator) generateInputFileLinks(inputFiles []domain.FileMetadata) []FileLink {
	var links []FileLink
	for _, file := range inputFiles {
		// Map from central storage path to worker-local path
		links = append(links, FileLink{
			SourcePath: file.Path,
			TargetPath: fmt.Sprintf("/cache/%s", file.Path),
		})
	}
	return links
}
