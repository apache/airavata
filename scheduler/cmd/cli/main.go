package main

import (
	"archive/tar"
	"bytes"
	"compress/gzip"
	"context"
	"encoding/json"
	"fmt"
	"io"
	"log"
	"net/http"
	"os"
	"path/filepath"
	"strings"
	"time"

	"github.com/spf13/cobra"
	"gopkg.in/yaml.v3"
)

// ExperimentSpec defines the YAML structure
type ExperimentSpec struct {
	Parameters map[string]ParameterDef `yaml:"parameters"`
	Scripts    map[string]string       `yaml:"scripts"`
	Tasks      map[string]TaskDef      `yaml:"tasks"`
	Resources  ResourceSpec            `yaml:"resources"`
}

type ParameterDef struct {
	Description string      `yaml:"description"`
	Type        string      `yaml:"type"`
	Default     interface{} `yaml:"default"`
}

type TaskDef struct {
	Script      string                 `yaml:"script"`
	TaskInputs  map[string]interface{} `yaml:"task_inputs"`
	TaskOutputs map[string]string      `yaml:"task_outputs"`
	Foreach     []string               `yaml:"foreach"`
	DependsOn   []string               `yaml:"depends_on"`
}

type ResourceSpec struct {
	Compute     ComputeSpec `yaml:"compute"`
	Storage     []string    `yaml:"storage"`
	Conda       []string    `yaml:"conda"`
	Pip         []string    `yaml:"pip"`
	Environment []string    `yaml:"environment"`
}

type ComputeSpec struct {
	Node   int    `yaml:"node"`
	CPU    int    `yaml:"cpu"`
	GPU    int    `yaml:"gpu"`
	DiskGB int    `yaml:"disk_gb"`
	RAMGB  int    `yaml:"ram_gb"`
	VRAMGB int    `yaml:"vram_gb"`
	Time   string `yaml:"time"`
}

// ExperimentSubmission represents the experiment data sent to the server
type ExperimentSubmission struct {
	Name       string                 `json:"name"`
	ProjectID  string                 `json:"project_id"`
	ComputeID  string                 `json:"compute_id"`
	StorageID  string                 `json:"storage_id"`
	Spec       ExperimentSpec         `json:"spec"`
	Parameters map[string]interface{} `json:"parameters"`
}

// ExperimentStatus represents the status response from the server
type ExperimentStatus struct {
	ID        string       `json:"id"`
	Name      string       `json:"name"`
	Status    string       `json:"status"`
	ProjectID string       `json:"project_id"`
	CreatedAt time.Time    `json:"created_at"`
	UpdatedAt time.Time    `json:"updated_at"`
	Tasks     []TaskStatus `json:"tasks"`
}

type TaskStatus struct {
	ID     string `json:"id"`
	Name   string `json:"name"`
	Status string `json:"status"`
}

func main() {
	var rootCmd = &cobra.Command{
		Use:   "airavata",
		Short: "Airavata CLI - Complete scheduler management tool",
		Long: `Airavata CLI is a comprehensive command-line tool for managing the Airavata Scheduler.

Features:
  • User authentication and session management
  • Resource management (compute, storage, credentials)
  • Experiment submission and monitoring
  • Real-time progress tracking with rich TUI
  • Project and user management

Examples:
  # Login to the scheduler
  airavata auth login

  # List available compute resources
  airavata resource compute list

  # Run an experiment with real-time monitoring
  airavata run experiment.yml --project my-project --compute cluster-1 --watch

  # Check your user profile
  airavata user profile`,
	}

	// Global flags
	rootCmd.PersistentFlags().String("server", "", "Scheduler server URL (e.g., http://localhost:8080)")
	rootCmd.PersistentFlags().Bool("admin", false, "Use admin credentials for sudo operations")

	// Add command groups
	rootCmd.AddCommand(createAuthCommands())
	rootCmd.AddCommand(createUserCommands())
	rootCmd.AddCommand(createResourceCommands())
	rootCmd.AddCommand(createExperimentCommands())
	rootCmd.AddCommand(createDataCommands())
	rootCmd.AddCommand(createProjectCommands())
	rootCmd.AddCommand(createConfigCommands())

	// Set server URL from flag if provided
	rootCmd.PersistentPreRunE = func(cmd *cobra.Command, args []string) error {
		if serverURL, _ := cmd.Flags().GetString("server"); serverURL != "" {
			configManager := NewConfigManager()
			if err := configManager.SetServerURL(serverURL); err != nil {
				return fmt.Errorf("failed to set server URL: %w", err)
			}
		}
		return nil
	}

	if err := rootCmd.Execute(); err != nil {
		log.Fatal(err)
	}
}

func runExperiment(experimentFile string, cmd *cobra.Command) error {
	ctx := context.Background()

	// Read YAML file
	data, err := os.ReadFile(experimentFile)
	if err != nil {
		return fmt.Errorf("failed to read experiment file: %w", err)
	}

	// Parse YAML
	var spec ExperimentSpec
	if err := yaml.Unmarshal(data, &spec); err != nil {
		return fmt.Errorf("failed to parse experiment YAML: %w", err)
	}

	// Get flags
	projectID, _ := cmd.Flags().GetString("project")
	experimentName, _ := cmd.Flags().GetString("name")
	computeID, _ := cmd.Flags().GetString("compute")
	storageID, _ := cmd.Flags().GetString("storage")
	dryRun, _ := cmd.Flags().GetBool("dry-run")
	watch, _ := cmd.Flags().GetBool("watch")

	// Default experiment name from filename
	if experimentName == "" {
		experimentName = filepath.Base(experimentFile)
		experimentName = experimentName[:len(experimentName)-len(filepath.Ext(experimentName))]
	}

	// Validate required flags
	if projectID == "" {
		return fmt.Errorf("--project flag is required")
	}
	if computeID == "" {
		return fmt.Errorf("--compute flag is required")
	}

	// Validate experiment
	if err := validateExperiment(&spec); err != nil {
		return fmt.Errorf("experiment validation failed: %w", err)
	}

	if dryRun {
		fmt.Println("✅ Experiment validation successful")
		fmt.Printf("Experiment: %s\n", experimentName)
		fmt.Printf("Project: %s\n", projectID)
		fmt.Printf("Compute: %s\n", computeID)
		fmt.Printf("Storage: %s\n", storageID)
		fmt.Printf("Tasks: %d\n", len(spec.Tasks))
		return nil
	}

	// Get server URL and token
	configManager := NewConfigManager()
	serverURL, err := configManager.GetServerURL()
	if err != nil {
		return fmt.Errorf("failed to get server URL: %w", err)
	}

	token, err := configManager.GetToken()
	if err != nil {
		return fmt.Errorf("failed to get token: %w", err)
	}

	// Submit experiment to server
	experimentID, err := submitExperimentWithAuth(ctx, serverURL, token, ExperimentSubmission{
		Name:       experimentName,
		ProjectID:  projectID,
		ComputeID:  computeID,
		StorageID:  storageID,
		Spec:       spec,
		Parameters: make(map[string]interface{}),
	})
	if err != nil {
		return fmt.Errorf("failed to submit experiment: %w", err)
	}

	fmt.Printf("✅ Experiment submitted successfully\n")
	fmt.Printf("Experiment ID: %s\n", experimentID)

	if watch {
		return watchExperimentWithTUI(experimentID)
	}

	return nil
}

func submitExperimentWithAuth(ctx context.Context, serverURL, token string, submission ExperimentSubmission) (string, error) {
	// Marshal to JSON
	jsonData, err := json.Marshal(submission)
	if err != nil {
		return "", fmt.Errorf("failed to marshal experiment: %w", err)
	}

	// Create HTTP request
	req, err := http.NewRequestWithContext(ctx, "POST", serverURL+"/api/v2/experiments", bytes.NewBuffer(jsonData))
	if err != nil {
		return "", fmt.Errorf("failed to create request: %w", err)
	}

	req.Header.Set("Content-Type", "application/json")
	req.Header.Set("Authorization", "Bearer "+token)

	// Send request
	client := &http.Client{Timeout: 30 * time.Second}
	resp, err := client.Do(req)
	if err != nil {
		return "", fmt.Errorf("failed to submit experiment: %w", err)
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusCreated {
		body, _ := io.ReadAll(resp.Body)
		return "", fmt.Errorf("server error: %d - %s", resp.StatusCode, string(body))
	}

	// Parse response
	var result struct {
		ID string `json:"id"`
	}
	if err := json.NewDecoder(resp.Body).Decode(&result); err != nil {
		return "", fmt.Errorf("failed to parse response: %w", err)
	}

	return result.ID, nil
}

func showExperimentStatus(experimentID string) error {
	configManager := NewConfigManager()

	if !configManager.IsAuthenticated() {
		return fmt.Errorf("not authenticated - run 'airavata auth login' first")
	}

	serverURL, err := configManager.GetServerURL()
	if err != nil {
		return fmt.Errorf("failed to get server URL: %w", err)
	}

	token, err := configManager.GetToken()
	if err != nil {
		return fmt.Errorf("failed to get token: %w", err)
	}

	ctx := context.Background()

	// Create HTTP request
	req, err := http.NewRequestWithContext(ctx, "GET", serverURL+"/api/v2/experiments/"+experimentID, nil)
	if err != nil {
		return fmt.Errorf("failed to create request: %w", err)
	}

	req.Header.Set("Authorization", "Bearer "+token)

	// Send request
	client := &http.Client{Timeout: 10 * time.Second}
	resp, err := client.Do(req)
	if err != nil {
		return fmt.Errorf("failed to get experiment status: %w", err)
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK {
		body, _ := io.ReadAll(resp.Body)
		return fmt.Errorf("server error: %d - %s", resp.StatusCode, string(body))
	}

	// Parse response
	var status ExperimentStatus
	if err := json.NewDecoder(resp.Body).Decode(&status); err != nil {
		return fmt.Errorf("failed to parse response: %w", err)
	}

	// Display status
	fmt.Printf("Experiment: %s\n", status.Name)
	fmt.Printf("ID: %s\n", status.ID)
	fmt.Printf("Status: %s\n", status.Status)
	fmt.Printf("Project: %s\n", status.ProjectID)
	fmt.Printf("Created: %s\n", status.CreatedAt.Format(time.RFC3339))
	fmt.Printf("Updated: %s\n", status.UpdatedAt.Format(time.RFC3339))

	if len(status.Tasks) > 0 {
		fmt.Println("\nTasks:")
		for _, task := range status.Tasks {
			fmt.Printf("  %s: %s\n", task.Name, task.Status)
		}
	}

	return nil
}

func watchExperiment(experimentID string) error {
	fmt.Printf("Watching experiment %s...\n", experimentID)

	// Simple polling implementation
	for {
		status, err := getExperimentStatus(experimentID)
		if err != nil {
			return err
		}

		fmt.Printf("\rStatus: %s", status.Status)

		if status.Status == "completed" || status.Status == "failed" || status.Status == "cancelled" {
			fmt.Println()
			return nil
		}

		time.Sleep(2 * time.Second)
	}
}

func getExperimentStatus(experimentID string) (*ExperimentStatus, error) {
	configManager := NewConfigManager()

	serverURL, err := configManager.GetServerURL()
	if err != nil {
		return nil, fmt.Errorf("failed to get server URL: %w", err)
	}

	token, err := configManager.GetToken()
	if err != nil {
		return nil, fmt.Errorf("failed to get token: %w", err)
	}

	ctx := context.Background()

	req, err := http.NewRequestWithContext(ctx, "GET", serverURL+"/api/v2/experiments/"+experimentID, nil)
	if err != nil {
		return nil, err
	}

	req.Header.Set("Authorization", "Bearer "+token)

	client := &http.Client{Timeout: 10 * time.Second}
	resp, err := client.Do(req)
	if err != nil {
		return nil, err
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK {
		body, _ := io.ReadAll(resp.Body)
		return nil, fmt.Errorf("server error: %d - %s", resp.StatusCode, string(body))
	}

	var status ExperimentStatus
	if err := json.NewDecoder(resp.Body).Decode(&status); err != nil {
		return nil, err
	}

	return &status, nil
}

func listExperiments() error {
	configManager := NewConfigManager()

	if !configManager.IsAuthenticated() {
		return fmt.Errorf("not authenticated - run 'airavata auth login' first")
	}

	serverURL, err := configManager.GetServerURL()
	if err != nil {
		return fmt.Errorf("failed to get server URL: %w", err)
	}

	token, err := configManager.GetToken()
	if err != nil {
		return fmt.Errorf("failed to get token: %w", err)
	}

	ctx := context.Background()

	req, err := http.NewRequestWithContext(ctx, "GET", serverURL+"/api/v2/experiments", nil)
	if err != nil {
		return fmt.Errorf("failed to create request: %w", err)
	}

	req.Header.Set("Authorization", "Bearer "+token)

	client := &http.Client{Timeout: 10 * time.Second}
	resp, err := client.Do(req)
	if err != nil {
		return fmt.Errorf("failed to list experiments: %w", err)
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK {
		body, _ := io.ReadAll(resp.Body)
		return fmt.Errorf("server error: %d - %s", resp.StatusCode, string(body))
	}

	var experiments []ExperimentStatus
	if err := json.NewDecoder(resp.Body).Decode(&experiments); err != nil {
		return fmt.Errorf("failed to parse response: %w", err)
	}

	fmt.Printf("Found %d experiments:\n\n", len(experiments))
	for _, exp := range experiments {
		fmt.Printf("%s  %s  %s  %s\n", exp.ID, exp.Name, exp.Status, exp.CreatedAt.Format("2006-01-02 15:04"))
	}

	return nil
}

func validateExperiment(spec *ExperimentSpec) error {
	if len(spec.Tasks) == 0 {
		return fmt.Errorf("experiment must have at least one task")
	}

	// Validate task dependencies
	for taskName, task := range spec.Tasks {
		for _, dep := range task.DependsOn {
			if _, exists := spec.Tasks[dep]; !exists {
				return fmt.Errorf("task '%s' depends on non-existent task '%s'", taskName, dep)
			}
		}
	}

	return nil
}

// Helper functions

// createExperimentCommands creates experiment-related commands
func createExperimentCommands() *cobra.Command {
	experimentCmd := &cobra.Command{
		Use:   "experiment",
		Short: "Experiment management commands",
		Long:  "Commands for submitting, monitoring, and managing experiments",
	}

	// Run command
	runCmd := &cobra.Command{
		Use:   "run [experiment-file]",
		Short: "Run an experiment from a YAML file",
		Long: `Execute an experiment defined in a YAML file with parameter sweeps and task dependencies.

The CLI automatically resolves credentials bound to compute and storage resources using SpiceDB
and OpenBao. Credentials are retrieved securely and provided to workers during execution.

Examples:
  # Run experiment with automatic credential resolution
  airavata experiment run experiment.yml --project my-project --compute cluster-1 --storage s3-bucket-1

  # Validate experiment without executing
  airavata experiment run experiment.yml --dry-run

  # Run with real-time progress monitoring
  airavata experiment run experiment.yml --project my-project --compute cluster-1 --watch`,
		Args: cobra.ExactArgs(1),
		RunE: func(cmd *cobra.Command, args []string) error {
			return runExperiment(args[0], cmd)
		},
	}

	// Add flags to run command
	runCmd.Flags().String("project", "", "Project ID to run experiment under")
	runCmd.Flags().String("name", "", "Experiment name (default: filename)")
	runCmd.Flags().String("compute", "", "Compute resource ID to use")
	runCmd.Flags().String("storage", "global-scratch", "Central storage resource name")
	runCmd.Flags().Bool("dry-run", false, "Validate experiment without executing")
	runCmd.Flags().Bool("watch", false, "Watch experiment progress in real-time with TUI")

	// Status command
	statusCmd := &cobra.Command{
		Use:   "status [experiment-id]",
		Short: "Check experiment status",
		Args:  cobra.ExactArgs(1),
		RunE: func(cmd *cobra.Command, args []string) error {
			return showExperimentStatus(args[0])
		},
	}

	// Watch command
	watchCmd := &cobra.Command{
		Use:   "watch [experiment-id]",
		Short: "Watch experiment progress in real-time with TUI",
		Args:  cobra.ExactArgs(1),
		RunE: func(cmd *cobra.Command, args []string) error {
			return watchExperimentWithTUI(args[0])
		},
	}

	// List command
	listCmd := &cobra.Command{
		Use:   "list",
		Short: "List experiments",
		RunE: func(cmd *cobra.Command, args []string) error {
			return listExperiments()
		},
	}

	// Outputs command
	outputsCmd := &cobra.Command{
		Use:   "outputs <experiment-id>",
		Short: "List experiment outputs organized by task",
		Long: `List all output files for a completed experiment, organized by task ID.

Examples:
  airavata experiment outputs exp-123
  airavata experiment outputs exp-456 --format json`,
		Args: cobra.ExactArgs(1),
		RunE: func(cmd *cobra.Command, args []string) error {
			return listExperimentOutputs(args[0], cmd)
		},
	}

	// Download command
	downloadCmd := &cobra.Command{
		Use:   "download <experiment-id>",
		Short: "Download experiment outputs",
		Long: `Download experiment outputs as archive or specific files.

Examples:
  airavata experiment download exp-123 --output ./results/
  airavata experiment download exp-123 --task task-456 --output ./task-outputs/
  airavata experiment download exp-123 --file task-456/output.txt --output ./output.txt`,
		Args: cobra.ExactArgs(1),
		RunE: func(cmd *cobra.Command, args []string) error {
			return downloadExperimentOutputs(args[0], cmd)
		},
	}

	// Add flags to download command
	downloadCmd.Flags().String("output", "./", "Output directory or file path")
	downloadCmd.Flags().String("task", "", "Download outputs for specific task only")
	downloadCmd.Flags().String("file", "", "Download specific file only")
	downloadCmd.Flags().Bool("extract", true, "Extract archive after download")

	// Tasks command
	tasksCmd := &cobra.Command{
		Use:   "tasks <experiment-id>",
		Short: "List experiment tasks with detailed status",
		Long: `List all tasks for an experiment with detailed status information.

Examples:
  airavata experiment tasks exp-123
  airavata experiment tasks exp-456 --status running`,
		Args: cobra.ExactArgs(1),
		RunE: func(cmd *cobra.Command, args []string) error {
			return listExperimentTasks(args[0])
		},
	}

	// Task command
	taskCmd := &cobra.Command{
		Use:   "task <task-id>",
		Short: "Get specific task details",
		Long: `Get detailed information about a specific task.

Examples:
  airavata experiment task task-123
  airavata experiment task task-456 --logs`,
		Args: cobra.ExactArgs(1),
		RunE: func(cmd *cobra.Command, args []string) error {
			showLogs, _ := cmd.Flags().GetBool("logs")
			if showLogs {
				return getTaskLogs(args[0])
			}
			return getTaskDetails(args[0])
		},
	}

	taskCmd.Flags().Bool("logs", false, "Show task execution logs")

	// Lifecycle commands
	cancelCmd := &cobra.Command{
		Use:   "cancel <experiment-id>",
		Short: "Cancel a running experiment",
		Long: `Cancel a running experiment and all its tasks.

Examples:
  airavata experiment cancel exp-123`,
		Args: cobra.ExactArgs(1),
		RunE: func(cmd *cobra.Command, args []string) error {
			return cancelExperiment(args[0])
		},
	}

	pauseCmd := &cobra.Command{
		Use:   "pause <experiment-id>",
		Short: "Pause a running experiment",
		Long: `Pause a running experiment (if supported by the compute resource).

Examples:
  airavata experiment pause exp-123`,
		Args: cobra.ExactArgs(1),
		RunE: func(cmd *cobra.Command, args []string) error {
			return pauseExperiment(args[0])
		},
	}

	resumeCmd := &cobra.Command{
		Use:   "resume <experiment-id>",
		Short: "Resume a paused experiment",
		Long: `Resume a paused experiment.

Examples:
  airavata experiment resume exp-123`,
		Args: cobra.ExactArgs(1),
		RunE: func(cmd *cobra.Command, args []string) error {
			return resumeExperiment(args[0])
		},
	}

	logsCmd := &cobra.Command{
		Use:   "logs <experiment-id>",
		Short: "View experiment logs",
		Long: `View aggregated logs for an experiment or specific task.

Examples:
  airavata experiment logs exp-123
  airavata experiment logs exp-123 --task task-456`,
		Args: cobra.ExactArgs(1),
		RunE: func(cmd *cobra.Command, args []string) error {
			return getExperimentLogs(args[0], cmd)
		},
	}

	logsCmd.Flags().String("task", "", "View logs for specific task only")

	resubmitCmd := &cobra.Command{
		Use:   "resubmit <experiment-id>",
		Short: "Resubmit a failed experiment",
		Long: `Resubmit a failed experiment with the same parameters.

Examples:
  airavata experiment resubmit exp-123
  airavata experiment resubmit exp-123 --failed-only`,
		Args: cobra.ExactArgs(1),
		RunE: func(cmd *cobra.Command, args []string) error {
			return resubmitExperiment(args[0], cmd)
		},
	}

	resubmitCmd.Flags().Bool("failed-only", false, "Resubmit only failed tasks")

	retryCmd := &cobra.Command{
		Use:   "retry <experiment-id>",
		Short: "Retry failed tasks in an experiment",
		Long: `Retry failed tasks in an experiment.

Examples:
  airavata experiment retry exp-123
  airavata experiment retry exp-123 --failed-only`,
		Args: cobra.ExactArgs(1),
		RunE: func(cmd *cobra.Command, args []string) error {
			return retryExperiment(args[0], cmd)
		},
	}

	retryCmd.Flags().Bool("failed-only", true, "Retry only failed tasks")

	experimentCmd.AddCommand(runCmd, statusCmd, watchCmd, listCmd, outputsCmd, downloadCmd, tasksCmd, taskCmd, cancelCmd, pauseCmd, resumeCmd, logsCmd, resubmitCmd, retryCmd)
	return experimentCmd
}

// createConfigCommands creates configuration management commands
func createConfigCommands() *cobra.Command {
	configCmd := &cobra.Command{
		Use:   "config",
		Short: "CLI configuration management",
		Long:  "Commands for managing CLI configuration and settings",
	}

	configSetCmd := &cobra.Command{
		Use:   "set [key] [value]",
		Short: "Set configuration value",
		Args:  cobra.ExactArgs(2),
		RunE: func(cmd *cobra.Command, args []string) error {
			return setConfig(args[0], args[1])
		},
	}

	configGetCmd := &cobra.Command{
		Use:   "get [key]",
		Short: "Get configuration value",
		Args:  cobra.ExactArgs(1),
		RunE: func(cmd *cobra.Command, args []string) error {
			return getConfig(args[0])
		},
	}

	configShowCmd := &cobra.Command{
		Use:   "show",
		Short: "Show all configuration",
		RunE: func(cmd *cobra.Command, args []string) error {
			return showConfig()
		},
	}

	configCmd.AddCommand(configSetCmd, configGetCmd, configShowCmd)
	return configCmd
}

// watchExperimentWithTUI watches experiment with rich TUI
func watchExperimentWithTUI(experimentID string) error {
	configManager := NewConfigManager()

	if !configManager.IsAuthenticated() {
		return fmt.Errorf("not authenticated - run 'airavata auth login' first")
	}

	serverURL, err := configManager.GetServerURL()
	if err != nil {
		return fmt.Errorf("failed to get server URL: %w", err)
	}

	token, err := configManager.GetToken()
	if err != nil {
		return fmt.Errorf("failed to get token: %w", err)
	}

	// Get experiment details
	experiment, err := getExperimentDetails(serverURL, token, experimentID)
	if err != nil {
		return fmt.Errorf("failed to get experiment details: %w", err)
	}

	// Create WebSocket client
	wsClient := NewWebSocketClient(serverURL, token)
	if err := wsClient.Connect(); err != nil {
		return fmt.Errorf("failed to connect to WebSocket: %w", err)
	}
	defer wsClient.Close()

	// Run TUI
	return RunTUI(experiment, wsClient)
}

// getExperimentDetails gets experiment details from the API
func getExperimentDetails(serverURL, token, experimentID string) (*Experiment, error) {
	ctx, cancel := context.WithTimeout(context.Background(), 10*time.Second)
	defer cancel()

	req, err := http.NewRequestWithContext(ctx, "GET", serverURL+"/api/v2/experiments/"+experimentID+"?includeTasks=true", nil)
	if err != nil {
		return nil, fmt.Errorf("failed to create request: %w", err)
	}

	req.Header.Set("Authorization", "Bearer "+token)

	client := &http.Client{Timeout: 10 * time.Second}
	resp, err := client.Do(req)
	if err != nil {
		return nil, fmt.Errorf("failed to send request: %w", err)
	}
	defer resp.Body.Close()

	body, err := io.ReadAll(resp.Body)
	if err != nil {
		return nil, fmt.Errorf("failed to read response: %w", err)
	}

	if resp.StatusCode != http.StatusOK {
		return nil, fmt.Errorf("failed to get experiment: %s", string(body))
	}

	var experiment Experiment
	if err := json.Unmarshal(body, &experiment); err != nil {
		return nil, fmt.Errorf("failed to parse experiment: %w", err)
	}

	return &experiment, nil
}

// setConfig sets a configuration value
func setConfig(key, value string) error {
	configManager := NewConfigManager()

	switch key {
	case "server":
		return configManager.SetServerURL(value)
	default:
		return fmt.Errorf("unknown config key: %s", key)
	}
}

// getConfig gets a configuration value
func getConfig(key string) error {
	configManager := NewConfigManager()

	switch key {
	case "server":
		value, err := configManager.GetServerURL()
		if err != nil {
			return err
		}
		fmt.Println(value)
		return nil
	default:
		return fmt.Errorf("unknown config key: %s", key)
	}
}

// showConfig shows all configuration
func showConfig() error {
	configManager := NewConfigManager()

	serverURL, err := configManager.GetServerURL()
	if err != nil {
		return fmt.Errorf("failed to get server URL: %w", err)
	}

	username, err := configManager.GetUsername()
	if err != nil {
		username = "not logged in"
	}

	fmt.Println("CLI Configuration")
	fmt.Println("=================")
	fmt.Printf("Server URL: %s\n", serverURL)
	fmt.Printf("Username:   %s\n", username)
	fmt.Printf("Authenticated: %t\n", configManager.IsAuthenticated())

	return nil
}

// Experiment output management functions

// ExperimentOutput represents an experiment output file
type ExperimentOutput struct {
	Path     string `json:"path"`
	Size     int64  `json:"size"`
	Checksum string `json:"checksum"`
	Type     string `json:"type"`
}

// TaskOutput represents outputs for a specific task
type TaskOutput struct {
	TaskID string             `json:"task_id"`
	Files  []ExperimentOutput `json:"files"`
}

// ExperimentOutputsResponse represents the response from outputs API
type ExperimentOutputsResponse struct {
	ExperimentID string       `json:"experiment_id"`
	Outputs      []TaskOutput `json:"outputs"`
}

// listExperimentOutputs lists all output files for an experiment
func listExperimentOutputs(experimentID string, cmd *cobra.Command) error {
	configManager := NewConfigManager()
	if !configManager.IsAuthenticated() {
		return fmt.Errorf("not authenticated - run 'airavata auth login' first")
	}

	serverURL, err := configManager.GetServerURL()
	if err != nil {
		return fmt.Errorf("failed to get server URL: %w", err)
	}

	token, err := configManager.GetToken()
	if err != nil {
		return fmt.Errorf("failed to get token: %w", err)
	}

	ctx := context.Background()
	url := fmt.Sprintf("%s/api/v1/experiments/%s/outputs", serverURL, experimentID)
	req, err := http.NewRequestWithContext(ctx, "GET", url, nil)
	if err != nil {
		return fmt.Errorf("failed to create request: %w", err)
	}

	req.Header.Set("Authorization", "Bearer "+token)

	client := &http.Client{Timeout: 30 * time.Second}
	resp, err := client.Do(req)
	if err != nil {
		return fmt.Errorf("failed to send request: %w", err)
	}
	defer resp.Body.Close()

	body, err := io.ReadAll(resp.Body)
	if err != nil {
		return fmt.Errorf("failed to read response: %w", err)
	}

	if resp.StatusCode != http.StatusOK {
		return fmt.Errorf("failed to get experiment outputs: %s", string(body))
	}

	var response ExperimentOutputsResponse
	if err := json.Unmarshal(body, &response); err != nil {
		return fmt.Errorf("failed to parse response: %w", err)
	}

	if len(response.Outputs) == 0 {
		fmt.Printf("📁 No outputs found for experiment %s\n", experimentID)
		return nil
	}

	fmt.Printf("📁 Experiment Outputs: %s (%d tasks)\n", experimentID, len(response.Outputs))
	fmt.Println("==========================================")

	for _, taskOutput := range response.Outputs {
		fmt.Printf("📋 Task: %s (%d files)\n", taskOutput.TaskID, len(taskOutput.Files))
		for _, file := range taskOutput.Files {
			icon := "📄"
			if file.Type == "directory" {
				icon = "📁"
			}
			fmt.Printf("  %s %s (%d bytes)", icon, file.Path, file.Size)
			if file.Checksum != "" {
				fmt.Printf(" [%s]", file.Checksum[:8])
			}
			fmt.Println()
		}
		fmt.Println()
	}

	return nil
}

// downloadExperimentOutputs downloads experiment outputs
func downloadExperimentOutputs(experimentID string, cmd *cobra.Command) error {
	configManager := NewConfigManager()
	if !configManager.IsAuthenticated() {
		return fmt.Errorf("not authenticated - run 'airavata auth login' first")
	}

	serverURL, err := configManager.GetServerURL()
	if err != nil {
		return fmt.Errorf("failed to get server URL: %w", err)
	}

	token, err := configManager.GetToken()
	if err != nil {
		return fmt.Errorf("failed to get token: %w", err)
	}

	outputPath, _ := cmd.Flags().GetString("output")
	taskID, _ := cmd.Flags().GetString("task")
	filePath, _ := cmd.Flags().GetString("file")
	extract, _ := cmd.Flags().GetBool("extract")

	// Download specific file
	if filePath != "" {
		return downloadOutputFile(serverURL, token, experimentID, filePath, outputPath)
	}

	// Download specific task outputs
	if taskID != "" {
		return downloadTaskOutputs(serverURL, token, experimentID, taskID, outputPath)
	}

	// Download all outputs as archive
	return downloadAllOutputs(serverURL, token, experimentID, outputPath, extract)
}

// downloadAllOutputs downloads all experiment outputs as archive
func downloadAllOutputs(serverURL, token, experimentID, outputPath string, extract bool) error {
	ctx := context.Background()
	url := fmt.Sprintf("%s/api/v1/experiments/%s/outputs/archive", serverURL, experimentID)
	req, err := http.NewRequestWithContext(ctx, "GET", url, nil)
	if err != nil {
		return fmt.Errorf("failed to create request: %w", err)
	}

	req.Header.Set("Authorization", "Bearer "+token)

	client := &http.Client{Timeout: 5 * time.Minute}
	resp, err := client.Do(req)
	if err != nil {
		return fmt.Errorf("failed to send request: %w", err)
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK {
		body, _ := io.ReadAll(resp.Body)
		return fmt.Errorf("failed to download archive: %s", string(body))
	}

	// Create output file
	archivePath := filepath.Join(outputPath, fmt.Sprintf("experiment_%s_outputs.tar.gz", experimentID))
	if err := os.MkdirAll(filepath.Dir(archivePath), 0755); err != nil {
		return fmt.Errorf("failed to create output directory: %w", err)
	}

	file, err := os.Create(archivePath)
	if err != nil {
		return fmt.Errorf("failed to create archive file: %w", err)
	}
	defer file.Close()

	// Copy archive data
	bytesWritten, err := io.Copy(file, resp.Body)
	if err != nil {
		return fmt.Errorf("failed to write archive: %w", err)
	}

	fmt.Printf("✅ Archive downloaded: %s (%d bytes)\n", archivePath, bytesWritten)

	// Extract if requested
	if extract {
		fmt.Printf("📦 Extracting archive...\n")
		file.Close() // Close before extracting

		// Reopen for reading
		file, err = os.Open(archivePath)
		if err != nil {
			return fmt.Errorf("failed to reopen archive: %w", err)
		}
		defer file.Close()

		extractPath := filepath.Join(outputPath, fmt.Sprintf("experiment_%s_outputs", experimentID))
		if err := extractTarGz(file, extractPath); err != nil {
			return fmt.Errorf("failed to extract archive: %w", err)
		}

		fmt.Printf("✅ Archive extracted to: %s\n", extractPath)
	}

	return nil
}

// downloadTaskOutputs downloads outputs for a specific task
func downloadTaskOutputs(serverURL, token, experimentID, taskID, outputPath string) error {
	// This would require a specific API endpoint for task outputs
	// For now, we'll download the full archive and extract only the task directory
	fmt.Printf("📥 Downloading outputs for task %s...\n", taskID)

	// Download full archive first
	ctx := context.Background()
	url := fmt.Sprintf("%s/api/v1/experiments/%s/outputs/archive", serverURL, experimentID)
	req, err := http.NewRequestWithContext(ctx, "GET", url, nil)
	if err != nil {
		return fmt.Errorf("failed to create request: %w", err)
	}

	req.Header.Set("Authorization", "Bearer "+token)

	client := &http.Client{Timeout: 5 * time.Minute}
	resp, err := client.Do(req)
	if err != nil {
		return fmt.Errorf("failed to send request: %w", err)
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK {
		body, _ := io.ReadAll(resp.Body)
		return fmt.Errorf("failed to download archive: %s", string(body))
	}

	// Extract only the specific task directory
	extractPath := filepath.Join(outputPath, fmt.Sprintf("task_%s_outputs", taskID))
	if err := os.MkdirAll(extractPath, 0755); err != nil {
		return fmt.Errorf("failed to create output directory: %w", err)
	}

	// Extract tar.gz and filter for specific task
	if err := extractTaskFromTarGz(resp.Body, extractPath, taskID); err != nil {
		return fmt.Errorf("failed to extract task outputs: %w", err)
	}

	fmt.Printf("✅ Task outputs downloaded to: %s\n", extractPath)
	return nil
}

// downloadOutputFile downloads a specific output file
func downloadOutputFile(serverURL, token, experimentID, filePath, outputPath string) error {
	ctx := context.Background()
	url := fmt.Sprintf("%s/api/v1/experiments/%s/outputs/%s", serverURL, experimentID, filePath)
	req, err := http.NewRequestWithContext(ctx, "GET", url, nil)
	if err != nil {
		return fmt.Errorf("failed to create request: %w", err)
	}

	req.Header.Set("Authorization", "Bearer "+token)

	client := &http.Client{Timeout: 5 * time.Minute}
	resp, err := client.Do(req)
	if err != nil {
		return fmt.Errorf("failed to send request: %w", err)
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK {
		body, _ := io.ReadAll(resp.Body)
		return fmt.Errorf("failed to download file: %s", string(body))
	}

	// Create output file
	if err := os.MkdirAll(filepath.Dir(outputPath), 0755); err != nil {
		return fmt.Errorf("failed to create output directory: %w", err)
	}

	file, err := os.Create(outputPath)
	if err != nil {
		return fmt.Errorf("failed to create output file: %w", err)
	}
	defer file.Close()

	// Copy file data
	bytesWritten, err := io.Copy(file, resp.Body)
	if err != nil {
		return fmt.Errorf("failed to write file: %w", err)
	}

	fmt.Printf("✅ File downloaded: %s (%d bytes)\n", outputPath, bytesWritten)
	return nil
}

// listExperimentTasks lists all tasks for an experiment
func listExperimentTasks(experimentID string) error {
	configManager := NewConfigManager()
	if !configManager.IsAuthenticated() {
		return fmt.Errorf("not authenticated - run 'airavata auth login' first")
	}

	serverURL, err := configManager.GetServerURL()
	if err != nil {
		return fmt.Errorf("failed to get server URL: %w", err)
	}

	token, err := configManager.GetToken()
	if err != nil {
		return fmt.Errorf("failed to get token: %w", err)
	}

	ctx := context.Background()
	url := fmt.Sprintf("%s/api/v1/experiments/%s?includeTasks=true", serverURL, experimentID)
	req, err := http.NewRequestWithContext(ctx, "GET", url, nil)
	if err != nil {
		return fmt.Errorf("failed to create request: %w", err)
	}

	req.Header.Set("Authorization", "Bearer "+token)

	client := &http.Client{Timeout: 30 * time.Second}
	resp, err := client.Do(req)
	if err != nil {
		return fmt.Errorf("failed to send request: %w", err)
	}
	defer resp.Body.Close()

	body, err := io.ReadAll(resp.Body)
	if err != nil {
		return fmt.Errorf("failed to read response: %w", err)
	}

	if resp.StatusCode != http.StatusOK {
		return fmt.Errorf("failed to get experiment tasks: %s", string(body))
	}

	var experiment ExperimentStatus
	if err := json.Unmarshal(body, &experiment); err != nil {
		return fmt.Errorf("failed to parse response: %w", err)
	}

	if len(experiment.Tasks) == 0 {
		fmt.Printf("📋 No tasks found for experiment %s\n", experimentID)
		return nil
	}

	fmt.Printf("📋 Experiment Tasks: %s (%d tasks)\n", experimentID, len(experiment.Tasks))
	fmt.Println("==========================================")

	for _, task := range experiment.Tasks {
		statusIcon := getStatusIcon(task.Status)
		fmt.Printf("%s %s: %s\n", statusIcon, task.ID, task.Status)
	}

	return nil
}

// getTaskDetails gets detailed information about a specific task
func getTaskDetails(taskID string) error {
	configManager := NewConfigManager()
	if !configManager.IsAuthenticated() {
		return fmt.Errorf("not authenticated - run 'airavata auth login' first")
	}

	serverURL, err := configManager.GetServerURL()
	if err != nil {
		return fmt.Errorf("failed to get server URL: %w", err)
	}

	token, err := configManager.GetToken()
	if err != nil {
		return fmt.Errorf("failed to get token: %w", err)
	}

	ctx := context.Background()
	url := fmt.Sprintf("%s/api/v1/tasks/%s", serverURL, taskID)
	req, err := http.NewRequestWithContext(ctx, "GET", url, nil)
	if err != nil {
		return fmt.Errorf("failed to create request: %w", err)
	}

	req.Header.Set("Authorization", "Bearer "+token)

	client := &http.Client{Timeout: 30 * time.Second}
	resp, err := client.Do(req)
	if err != nil {
		return fmt.Errorf("failed to send request: %w", err)
	}
	defer resp.Body.Close()

	body, err := io.ReadAll(resp.Body)
	if err != nil {
		return fmt.Errorf("failed to read response: %w", err)
	}

	if resp.StatusCode != http.StatusOK {
		return fmt.Errorf("failed to get task details: %s", string(body))
	}

	// Parse task details (assuming similar structure to TaskStatus)
	var task TaskStatus
	if err := json.Unmarshal(body, &task); err != nil {
		return fmt.Errorf("failed to parse response: %w", err)
	}

	fmt.Printf("📋 Task Details: %s\n", taskID)
	fmt.Println("========================")
	fmt.Printf("ID:     %s\n", task.ID)
	fmt.Printf("Name:   %s\n", task.Name)
	fmt.Printf("Status: %s\n", task.Status)

	return nil
}

// getTaskLogs gets execution logs for a specific task
func getTaskLogs(taskID string) error {
	configManager := NewConfigManager()
	if !configManager.IsAuthenticated() {
		return fmt.Errorf("not authenticated - run 'airavata auth login' first")
	}

	serverURL, err := configManager.GetServerURL()
	if err != nil {
		return fmt.Errorf("failed to get server URL: %w", err)
	}

	token, err := configManager.GetToken()
	if err != nil {
		return fmt.Errorf("failed to get token: %w", err)
	}

	ctx := context.Background()
	url := fmt.Sprintf("%s/api/v1/tasks/%s/logs", serverURL, taskID)
	req, err := http.NewRequestWithContext(ctx, "GET", url, nil)
	if err != nil {
		return fmt.Errorf("failed to create request: %w", err)
	}

	req.Header.Set("Authorization", "Bearer "+token)

	client := &http.Client{Timeout: 30 * time.Second}
	resp, err := client.Do(req)
	if err != nil {
		return fmt.Errorf("failed to send request: %w", err)
	}
	defer resp.Body.Close()

	body, err := io.ReadAll(resp.Body)
	if err != nil {
		return fmt.Errorf("failed to read response: %w", err)
	}

	if resp.StatusCode != http.StatusOK {
		return fmt.Errorf("failed to get task logs: %s", string(body))
	}

	fmt.Printf("📋 Task Logs: %s\n", taskID)
	fmt.Println("========================")
	fmt.Println(string(body))

	return nil
}

// Helper functions

// getStatusIcon returns an emoji icon for task status
func getStatusIcon(status string) string {
	switch strings.ToLower(status) {
	case "completed":
		return "✅"
	case "running":
		return "🔄"
	case "failed":
		return "❌"
	case "queued":
		return "⏳"
	case "cancelled":
		return "⏹️"
	default:
		return "📋"
	}
}

// extractTaskFromTarGz extracts only files for a specific task from tar.gz
func extractTaskFromTarGz(r io.Reader, destDir, taskID string) error {
	gzReader, err := gzip.NewReader(r)
	if err != nil {
		return err
	}
	defer gzReader.Close()

	tarReader := tar.NewReader(gzReader)

	for {
		header, err := tarReader.Next()
		if err == io.EOF {
			break
		}
		if err != nil {
			return err
		}

		// Only extract files that belong to the specified task
		if !strings.HasPrefix(header.Name, taskID+"/") {
			continue
		}

		// Create full path
		targetPath := filepath.Join(destDir, strings.TrimPrefix(header.Name, taskID+"/"))

		// Create directory if needed
		if header.Typeflag == tar.TypeDir {
			if err := os.MkdirAll(targetPath, 0755); err != nil {
				return err
			}
			continue
		}

		// Create parent directories
		if err := os.MkdirAll(filepath.Dir(targetPath), 0755); err != nil {
			return err
		}

		// Create file
		file, err := os.Create(targetPath)
		if err != nil {
			return err
		}

		// Copy file content
		if _, err := io.Copy(file, tarReader); err != nil {
			file.Close()
			return err
		}

		file.Close()
	}

	return nil
}

// Experiment lifecycle management functions

// cancelExperiment cancels a running experiment
func cancelExperiment(experimentID string) error {
	configManager := NewConfigManager()
	if !configManager.IsAuthenticated() {
		return fmt.Errorf("not authenticated - run 'airavata auth login' first")
	}

	serverURL, err := configManager.GetServerURL()
	if err != nil {
		return fmt.Errorf("failed to get server URL: %w", err)
	}

	token, err := configManager.GetToken()
	if err != nil {
		return fmt.Errorf("failed to get token: %w", err)
	}

	ctx := context.Background()
	url := fmt.Sprintf("%s/api/v1/experiments/%s/cancel", serverURL, experimentID)
	req, err := http.NewRequestWithContext(ctx, "POST", url, nil)
	if err != nil {
		return fmt.Errorf("failed to create request: %w", err)
	}

	req.Header.Set("Authorization", "Bearer "+token)

	client := &http.Client{Timeout: 30 * time.Second}
	resp, err := client.Do(req)
	if err != nil {
		return fmt.Errorf("failed to send request: %w", err)
	}
	defer resp.Body.Close()

	body, err := io.ReadAll(resp.Body)
	if err != nil {
		return fmt.Errorf("failed to read response: %w", err)
	}

	if resp.StatusCode != http.StatusOK {
		return fmt.Errorf("failed to cancel experiment: %s", string(body))
	}

	fmt.Printf("✅ Experiment %s cancelled successfully\n", experimentID)
	return nil
}

// pauseExperiment pauses a running experiment
func pauseExperiment(experimentID string) error {
	configManager := NewConfigManager()
	if !configManager.IsAuthenticated() {
		return fmt.Errorf("not authenticated - run 'airavata auth login' first")
	}

	serverURL, err := configManager.GetServerURL()
	if err != nil {
		return fmt.Errorf("failed to get server URL: %w", err)
	}

	token, err := configManager.GetToken()
	if err != nil {
		return fmt.Errorf("failed to get token: %w", err)
	}

	ctx := context.Background()
	url := fmt.Sprintf("%s/api/v1/experiments/%s/pause", serverURL, experimentID)
	req, err := http.NewRequestWithContext(ctx, "POST", url, nil)
	if err != nil {
		return fmt.Errorf("failed to create request: %w", err)
	}

	req.Header.Set("Authorization", "Bearer "+token)

	client := &http.Client{Timeout: 30 * time.Second}
	resp, err := client.Do(req)
	if err != nil {
		return fmt.Errorf("failed to send request: %w", err)
	}
	defer resp.Body.Close()

	body, err := io.ReadAll(resp.Body)
	if err != nil {
		return fmt.Errorf("failed to read response: %w", err)
	}

	if resp.StatusCode != http.StatusOK {
		return fmt.Errorf("failed to pause experiment: %s", string(body))
	}

	fmt.Printf("✅ Experiment %s paused successfully\n", experimentID)
	return nil
}

// resumeExperiment resumes a paused experiment
func resumeExperiment(experimentID string) error {
	configManager := NewConfigManager()
	if !configManager.IsAuthenticated() {
		return fmt.Errorf("not authenticated - run 'airavata auth login' first")
	}

	serverURL, err := configManager.GetServerURL()
	if err != nil {
		return fmt.Errorf("failed to get server URL: %w", err)
	}

	token, err := configManager.GetToken()
	if err != nil {
		return fmt.Errorf("failed to get token: %w", err)
	}

	ctx := context.Background()
	url := fmt.Sprintf("%s/api/v1/experiments/%s/resume", serverURL, experimentID)
	req, err := http.NewRequestWithContext(ctx, "POST", url, nil)
	if err != nil {
		return fmt.Errorf("failed to create request: %w", err)
	}

	req.Header.Set("Authorization", "Bearer "+token)

	client := &http.Client{Timeout: 30 * time.Second}
	resp, err := client.Do(req)
	if err != nil {
		return fmt.Errorf("failed to send request: %w", err)
	}
	defer resp.Body.Close()

	body, err := io.ReadAll(resp.Body)
	if err != nil {
		return fmt.Errorf("failed to read response: %w", err)
	}

	if resp.StatusCode != http.StatusOK {
		return fmt.Errorf("failed to resume experiment: %s", string(body))
	}

	fmt.Printf("✅ Experiment %s resumed successfully\n", experimentID)
	return nil
}

// getExperimentLogs gets logs for an experiment or specific task
func getExperimentLogs(experimentID string, cmd *cobra.Command) error {
	configManager := NewConfigManager()
	if !configManager.IsAuthenticated() {
		return fmt.Errorf("not authenticated - run 'airavata auth login' first")
	}

	serverURL, err := configManager.GetServerURL()
	if err != nil {
		return fmt.Errorf("failed to get server URL: %w", err)
	}

	token, err := configManager.GetToken()
	if err != nil {
		return fmt.Errorf("failed to get token: %w", err)
	}

	taskID, _ := cmd.Flags().GetString("task")

	ctx := context.Background()
	var url string
	if taskID != "" {
		url = fmt.Sprintf("%s/api/v1/experiments/%s/logs?task=%s", serverURL, experimentID, taskID)
	} else {
		url = fmt.Sprintf("%s/api/v1/experiments/%s/logs", serverURL, experimentID)
	}

	req, err := http.NewRequestWithContext(ctx, "GET", url, nil)
	if err != nil {
		return fmt.Errorf("failed to create request: %w", err)
	}

	req.Header.Set("Authorization", "Bearer "+token)

	client := &http.Client{Timeout: 30 * time.Second}
	resp, err := client.Do(req)
	if err != nil {
		return fmt.Errorf("failed to send request: %w", err)
	}
	defer resp.Body.Close()

	body, err := io.ReadAll(resp.Body)
	if err != nil {
		return fmt.Errorf("failed to read response: %w", err)
	}

	if resp.StatusCode != http.StatusOK {
		return fmt.Errorf("failed to get experiment logs: %s", string(body))
	}

	if taskID != "" {
		fmt.Printf("📋 Experiment Logs: %s (Task: %s)\n", experimentID, taskID)
	} else {
		fmt.Printf("📋 Experiment Logs: %s\n", experimentID)
	}
	fmt.Println("========================")
	fmt.Println(string(body))

	return nil
}

// resubmitExperiment resubmits a failed experiment
func resubmitExperiment(experimentID string, cmd *cobra.Command) error {
	configManager := NewConfigManager()
	if !configManager.IsAuthenticated() {
		return fmt.Errorf("not authenticated - run 'airavata auth login' first")
	}

	serverURL, err := configManager.GetServerURL()
	if err != nil {
		return fmt.Errorf("failed to get server URL: %w", err)
	}

	token, err := configManager.GetToken()
	if err != nil {
		return fmt.Errorf("failed to get token: %w", err)
	}

	failedOnly, _ := cmd.Flags().GetBool("failed-only")

	// Create request body
	requestBody := map[string]interface{}{
		"failed_only": failedOnly,
	}

	jsonData, err := json.Marshal(requestBody)
	if err != nil {
		return fmt.Errorf("failed to marshal request: %w", err)
	}

	ctx := context.Background()
	url := fmt.Sprintf("%s/api/v1/experiments/%s/resubmit", serverURL, experimentID)
	req, err := http.NewRequestWithContext(ctx, "POST", url, bytes.NewBuffer(jsonData))
	if err != nil {
		return fmt.Errorf("failed to create request: %w", err)
	}

	req.Header.Set("Content-Type", "application/json")
	req.Header.Set("Authorization", "Bearer "+token)

	client := &http.Client{Timeout: 30 * time.Second}
	resp, err := client.Do(req)
	if err != nil {
		return fmt.Errorf("failed to send request: %w", err)
	}
	defer resp.Body.Close()

	body, err := io.ReadAll(resp.Body)
	if err != nil {
		return fmt.Errorf("failed to read response: %w", err)
	}

	if resp.StatusCode != http.StatusCreated {
		return fmt.Errorf("failed to resubmit experiment: %s", string(body))
	}

	// Parse response to get new experiment ID
	var response struct {
		ID string `json:"id"`
	}
	if err := json.Unmarshal(body, &response); err != nil {
		return fmt.Errorf("failed to parse response: %w", err)
	}

	fmt.Printf("✅ Experiment resubmitted successfully!\n")
	fmt.Printf("   Original: %s\n", experimentID)
	fmt.Printf("   New:      %s\n", response.ID)

	return nil
}

// retryExperiment retries failed tasks in an experiment
func retryExperiment(experimentID string, cmd *cobra.Command) error {
	configManager := NewConfigManager()
	if !configManager.IsAuthenticated() {
		return fmt.Errorf("not authenticated - run 'airavata auth login' first")
	}

	serverURL, err := configManager.GetServerURL()
	if err != nil {
		return fmt.Errorf("failed to get server URL: %w", err)
	}

	token, err := configManager.GetToken()
	if err != nil {
		return fmt.Errorf("failed to get token: %w", err)
	}

	failedOnly, _ := cmd.Flags().GetBool("failed-only")

	// Create request body
	requestBody := map[string]interface{}{
		"failed_only": failedOnly,
	}

	jsonData, err := json.Marshal(requestBody)
	if err != nil {
		return fmt.Errorf("failed to marshal request: %w", err)
	}

	ctx := context.Background()
	url := fmt.Sprintf("%s/api/v1/experiments/%s/retry", serverURL, experimentID)
	req, err := http.NewRequestWithContext(ctx, "POST", url, bytes.NewBuffer(jsonData))
	if err != nil {
		return fmt.Errorf("failed to create request: %w", err)
	}

	req.Header.Set("Content-Type", "application/json")
	req.Header.Set("Authorization", "Bearer "+token)

	client := &http.Client{Timeout: 30 * time.Second}
	resp, err := client.Do(req)
	if err != nil {
		return fmt.Errorf("failed to send request: %w", err)
	}
	defer resp.Body.Close()

	body, err := io.ReadAll(resp.Body)
	if err != nil {
		return fmt.Errorf("failed to read response: %w", err)
	}

	if resp.StatusCode != http.StatusOK {
		return fmt.Errorf("failed to retry experiment: %s", string(body))
	}

	fmt.Printf("✅ Experiment %s retry initiated successfully\n", experimentID)
	if failedOnly {
		fmt.Println("   Retrying only failed tasks")
	} else {
		fmt.Println("   Retrying all tasks")
	}

	return nil
}
