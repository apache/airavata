package main

import (
	"context"
	"flag"
	"fmt"
	"io"
	"log"
	"net/http"
	"os"
	"os/exec"
	"os/signal"
	"path/filepath"
	"runtime"
	"strings"
	"sync"
	"syscall"
	"time"

	"google.golang.org/grpc"
	"google.golang.org/grpc/credentials/insecure"
	"google.golang.org/grpc/keepalive"
	"google.golang.org/protobuf/types/known/durationpb"

	"github.com/apache/airavata/scheduler/core/dto"
	"github.com/shirou/gopsutil/v3/cpu"
	"github.com/shirou/gopsutil/v3/disk"
	"github.com/shirou/gopsutil/v3/mem"
	"google.golang.org/protobuf/types/known/timestamppb"
)

// WorkerLogger is a custom logger that streams log messages to the scheduler
type WorkerLogger struct {
	workerID string
	stream   dto.WorkerService_PollForTaskClient
	mu       sync.Mutex
}

// NewWorkerLogger creates a new WorkerLogger
func NewWorkerLogger(workerID string, stream dto.WorkerService_PollForTaskClient) *WorkerLogger {
	return &WorkerLogger{
		workerID: workerID,
		stream:   stream,
	}
}

// Write implements io.Writer interface for log streaming
func (wl *WorkerLogger) Write(p []byte) (n int, err error) {
	wl.mu.Lock()
	defer wl.mu.Unlock()

	// Send log message to scheduler
	output := &dto.WorkerMessage{
		Message: &dto.WorkerMessage_TaskOutput{
			TaskOutput: &dto.TaskOutput{
				TaskId:    "", // Empty for general worker logs
				WorkerId:  wl.workerID,
				Type:      dto.OutputType_OUTPUT_TYPE_LOG,
				Data:      p,
				Timestamp: timestamppb.Now(),
			},
		},
	}

	if err := wl.stream.Send(output); err != nil {
		// If we can't send to scheduler, fall back to standard logging
		log.Printf("Failed to send worker log to scheduler: %v", err)
	}

	return len(p), nil
}

// Printf formats and sends a log message to the scheduler
func (wl *WorkerLogger) Printf(format string, v ...interface{}) {
	message := fmt.Sprintf(format, v...)
	wl.Write([]byte(message))
}

// Println sends a log message to the scheduler
func (wl *WorkerLogger) Println(v ...interface{}) {
	message := fmt.Sprintln(v...)
	wl.Write([]byte(message))
}

// WorkerConfig holds configuration for the worker
type WorkerConfig struct {
	ServerURL         string
	WorkerID          string
	ExperimentID      string
	ComputeResourceID string
	WorkingDir        string
	HeartbeatInterval time.Duration
	TaskTimeout       time.Duration
	TLSCertPath       string
}

// Worker represents a worker instance
type Worker struct {
	config                  *WorkerConfig
	conn                    *grpc.ClientConn
	client                  dto.WorkerServiceClient
	stream                  dto.WorkerService_PollForTaskClient
	ctx                     context.Context
	cancel                  context.CancelFunc
	status                  dto.WorkerStatus
	currentTaskID           string
	currentTaskProcess      *exec.Cmd
	metrics                 *dto.WorkerMetrics
	lastServerResponse      time.Time
	serverCheckTicker       *time.Ticker
	lastTaskRequest         time.Time
	waitingForCompletionAck bool
	stateMutex              sync.RWMutex  // Protects status and currentTaskID
	logger                  *WorkerLogger // Custom logger that streams to scheduler
}

// getState safely gets the worker's current status and task ID
func (w *Worker) getState() (dto.WorkerStatus, string) {
	w.stateMutex.RLock()
	defer w.stateMutex.RUnlock()
	return w.status, w.currentTaskID
}

// setState safely updates the worker's status and task ID
func (w *Worker) setState(status dto.WorkerStatus, taskID string) {
	w.stateMutex.Lock()
	defer w.stateMutex.Unlock()
	w.status = status
	w.currentTaskID = taskID
}

// setStatus safely updates only the worker's status
func (w *Worker) setStatus(status dto.WorkerStatus) {
	w.stateMutex.Lock()
	defer w.stateMutex.Unlock()
	w.status = status
}

// hasRequestedTaskRecently checks if we've requested a task in the last 10 seconds
func (w *Worker) hasRequestedTaskRecently() bool {
	w.stateMutex.RLock()
	defer w.stateMutex.RUnlock()
	return time.Since(w.lastTaskRequest) < 10*time.Second
}

// markTaskRequested records that we've requested a task
func (w *Worker) markTaskRequested() {
	w.stateMutex.Lock()
	defer w.stateMutex.Unlock()
	w.lastTaskRequest = time.Now()
}

// setTaskID safely updates only the worker's current task ID
func (w *Worker) setTaskID(taskID string) {
	w.stateMutex.Lock()
	defer w.stateMutex.Unlock()
	w.currentTaskID = taskID
}

func main() {
	config := parseFlags()

	// Set up logging
	log.SetPrefix(fmt.Sprintf("[worker-%s] ", config.WorkerID))
	log.SetFlags(log.LstdFlags | log.Lshortfile)

	log.Printf("Starting worker with config: %+v", config)

	// Create worker instance
	w := &Worker{
		config:             config,
		status:             dto.WorkerStatus_WORKER_STATUS_IDLE,
		lastServerResponse: time.Now(),
		metrics: &dto.WorkerMetrics{
			WorkerId:           config.WorkerID,
			CpuUsagePercent:    0,
			MemoryUsagePercent: 0,
			DiskUsageBytes:     0,
			TasksCompleted:     0,
			TasksFailed:        0,
			Uptime:             &durationpb.Duration{},
			CustomMetrics:      make(map[string]string),
			Timestamp:          timestamppb.Now(),
		},
	}

	// Set up signal handling
	sigChan := make(chan os.Signal, 1)
	signal.Notify(sigChan, syscall.SIGTERM, syscall.SIGINT)

	// Create context with cancellation
	w.ctx, w.cancel = context.WithCancel(context.Background())

	// Start worker
	if err := w.start(); err != nil {
		log.Fatalf("Failed to start worker: %v", err)
	}

	// Wait for shutdown signal
	<-sigChan
	log.Println("Received shutdown signal, stopping dto...")

	// Graceful shutdown
	w.stop()
}

func parseFlags() *WorkerConfig {
	config := &WorkerConfig{}

	flag.StringVar(&config.ServerURL, "server-url", getEnvOrDefault("WORKER_SERVER_URL", "localhost:50051"), "gRPC server URL")
	flag.StringVar(&config.WorkerID, "worker-id", "", "Worker ID (required)")
	flag.StringVar(&config.ExperimentID, "experiment-id", "", "Experiment ID (required)")
	flag.StringVar(&config.ComputeResourceID, "compute-resource-id", "", "Compute resource ID (required)")
	flag.StringVar(&config.WorkingDir, "working-dir", getEnvOrDefault("WORKER_WORKING_DIR", "/tmp/worker"), "Working directory")
	flag.DurationVar(&config.HeartbeatInterval, "heartbeat-interval", getDurationEnvOrDefault("WORKER_HEARTBEAT_INTERVAL", 30*time.Second), "Heartbeat interval")
	flag.DurationVar(&config.TaskTimeout, "task-timeout", getDurationEnvOrDefault("WORKER_TASK_TIMEOUT", 24*time.Hour), "Task timeout")
	flag.StringVar(&config.TLSCertPath, "tls-cert", "", "TLS certificate path (optional)")

	flag.Parse()

	// Validate required flags
	if config.WorkerID == "" {
		log.Fatal("worker-id is required")
	}
	if config.ExperimentID == "" {
		log.Fatal("experiment-id is required")
	}
	if config.ComputeResourceID == "" {
		log.Fatal("compute-resource-id is required")
	}

	return config
}

// Helper functions for environment variable defaults
func getEnvOrDefault(key, defaultValue string) string {
	if value := os.Getenv(key); value != "" {
		return value
	}
	return defaultValue
}

func getDurationEnvOrDefault(key string, defaultValue time.Duration) time.Duration {
	if value := os.Getenv(key); value != "" {
		if duration, err := time.ParseDuration(value); err == nil {
			return duration
		}
	}
	return defaultValue
}

func (w *Worker) start() error {
	// Connect to server
	if err := w.connect(); err != nil {
		return fmt.Errorf("failed to connect to server: %w", err)
	}

	// Register worker
	if err := w.register(); err != nil {
		return fmt.Errorf("failed to register worker: %w", err)
	}

	// Start server health monitoring
	w.startServerHealthCheck()

	// Start polling for tasks
	if err := w.startPolling(); err != nil {
		return fmt.Errorf("failed to start polling: %w", err)
	}

	return nil
}

func (w *Worker) connect() error {
	// Set up gRPC connection options
	opts := []grpc.DialOption{
		grpc.WithKeepaliveParams(keepalive.ClientParameters{
			Time:                10 * time.Second,
			Timeout:             3 * time.Second,
			PermitWithoutStream: true,
		}),
	}

	// Add TLS if certificate is provided
	if w.config.TLSCertPath != "" {
		// TLS implementation would go here
		log.Println("TLS certificate provided but TLS not implemented yet")
		opts = append(opts, grpc.WithTransportCredentials(insecure.NewCredentials()))
	} else {
		opts = append(opts, grpc.WithTransportCredentials(insecure.NewCredentials()))
	}

	// Connect to server
	conn, err := grpc.Dial(w.config.ServerURL, opts...)
	if err != nil {
		return fmt.Errorf("failed to dial server: %w", err)
	}

	w.conn = conn
	w.client = dto.NewWorkerServiceClient(conn)

	log.Printf("Connected to server at %s", w.config.ServerURL)
	return nil
}

func (w *Worker) register() error {
	// Get system capabilities
	capabilities := w.getSystemCapabilities()

	// Create registration request
	req := &dto.WorkerRegistrationRequest{
		WorkerId:          w.config.WorkerID,
		ExperimentId:      w.config.ExperimentID,
		ComputeResourceId: w.config.ComputeResourceID,
		Capabilities:      capabilities,
		Metadata: map[string]string{
			"hostname":   getHostname(),
			"os":         runtime.GOOS,
			"arch":       runtime.GOARCH,
			"go_version": runtime.Version(),
			"started_at": time.Now().Format(time.RFC3339),
		},
	}

	// Register with server
	resp, err := w.client.RegisterWorker(w.ctx, req)
	if err != nil {
		return fmt.Errorf("failed to register worker: %w", err)
	}

	if !resp.Success {
		return fmt.Errorf("worker registration failed: %s", resp.Message)
	}

	log.Printf("Successfully registered worker: %s", resp.Message)
	return nil
}

func (w *Worker) startPolling() error {
	// Create bidirectional stream
	stream, err := w.client.PollForTask(w.ctx)
	if err != nil {
		return fmt.Errorf("failed to create polling stream: %w", err)
	}

	w.stream = stream

	// Initialize the worker logger that streams to scheduler
	w.logger = NewWorkerLogger(w.config.WorkerID, stream)

	// Start goroutines for sending and receiving
	go w.sendMessages()
	go w.receiveMessages()

	log.Println("Started polling for tasks")
	return nil
}

func (w *Worker) sendMessages() {
	// Separate tickers for heartbeat and task requesting
	heartbeatTicker := time.NewTicker(w.config.HeartbeatInterval)
	taskRequestTicker := time.NewTicker(5 * time.Second) // Request tasks every 5 seconds when idle
	defer heartbeatTicker.Stop()
	defer taskRequestTicker.Stop()

	for {
		select {
		case <-w.ctx.Done():
			return
		case <-heartbeatTicker.C:
			// Send heartbeat for health monitoring only
			w.sendHeartbeat()
		case <-taskRequestTicker.C:
			// Send task request if we're idle and need work
			w.sendTaskRequestIfNeeded()
		}
	}
}

// sendHeartbeat sends a heartbeat for health monitoring only
func (w *Worker) sendHeartbeat() {
	status, taskID := w.getState()
	heartbeat := &dto.WorkerMessage{
		Message: &dto.WorkerMessage_Heartbeat{
			Heartbeat: &dto.Heartbeat{
				WorkerId:      w.config.WorkerID,
				Timestamp:     timestamppb.Now(),
				Status:        status,
				CurrentTaskId: taskID,
				Metadata: map[string]string{
					"uptime": time.Since(time.Now()).String(),
				},
			},
		},
	}

	if err := w.stream.Send(heartbeat); err != nil {
		log.Printf("Failed to send heartbeat: %v", err)
		return
	}

	// Update last server response time on successful send
	w.lastServerResponse = time.Now()

	// Update and send metrics
	w.updateMetrics()
	metrics := &dto.WorkerMessage{
		Message: &dto.WorkerMessage_WorkerMetrics{
			WorkerMetrics: w.metrics,
		},
	}

	if err := w.stream.Send(metrics); err != nil {
		log.Printf("Failed to send metrics: %v", err)
		return
	}
}

// sendTaskRequestIfNeeded sends a task request if worker is idle and needs work
func (w *Worker) sendTaskRequestIfNeeded() {
	status, taskID := w.getState()

	// Check if we're waiting for completion acknowledgment
	w.stateMutex.RLock()
	waitingForAck := w.waitingForCompletionAck
	w.stateMutex.RUnlock()

	// Only request task if we're idle, have no current task, haven't requested one recently, and not waiting for completion ack
	if status == dto.WorkerStatus_WORKER_STATUS_IDLE && taskID == "" && !w.hasRequestedTaskRecently() && !waitingForAck {
		w.markTaskRequested()

		taskRequest := &dto.WorkerMessage{
			Message: &dto.WorkerMessage_TaskRequest{
				TaskRequest: &dto.TaskRequest{
					WorkerId:     w.config.WorkerID,
					Timestamp:    timestamppb.Now(),
					ExperimentId: w.config.ExperimentID,
					Metadata: map[string]string{
						"request_type": "idle_worker_polling",
					},
				},
			},
		}

		if err := w.stream.Send(taskRequest); err != nil {
			log.Printf("Failed to send task request: %v", err)
			return
		}

		log.Printf("Worker %s requested a task for experiment %s", w.config.WorkerID, w.config.ExperimentID)
	}
}

func (w *Worker) receiveMessages() {
	for {
		select {
		case <-w.ctx.Done():
			return
		default:
			msg, err := w.stream.Recv()
			if err != nil {
				if err == io.EOF {
					log.Println("Server closed connection")
					return
				}
				log.Printf("Failed to receive message: %v", err)
				return
			}

			// Update last server response time
			w.lastServerResponse = time.Now()

			w.handleServerMessage(msg)
		}
	}
}

// startServerHealthCheck starts monitoring server responsiveness
func (w *Worker) startServerHealthCheck() {
	w.serverCheckTicker = time.NewTicker(30 * time.Second)

	go func() {
		for {
			select {
			case <-w.ctx.Done():
				w.serverCheckTicker.Stop()
				return
			case <-w.serverCheckTicker.C:
				if time.Since(w.lastServerResponse) > 5*time.Minute {
					log.Printf("Server unresponsive for 5 minutes, terminating worker")
					w.cancel() // Trigger shutdown
					os.Exit(1)
				}
			}
		}
	}()
}

func (w *Worker) handleServerMessage(msg *dto.ServerMessage) {
	switch m := msg.Message.(type) {
	case *dto.ServerMessage_TaskAssignment:
		w.handleTaskAssignment(m.TaskAssignment)
	case *dto.ServerMessage_TaskCancellation:
		w.handleTaskCancellation(m.TaskCancellation)
	case *dto.ServerMessage_WorkerShutdown:
		w.handleWorkerShutdown(m.WorkerShutdown)
	case *dto.ServerMessage_ConfigUpdate:
		w.handleConfigUpdate(m.ConfigUpdate)
	case *dto.ServerMessage_OutputUploadRequest:
		w.handleOutputUploadRequest(m.OutputUploadRequest)
	default:
		log.Printf("Unknown server message type: %T", msg.Message)
	}
}

func (w *Worker) handleTaskAssignment(assignment *dto.TaskAssignment) {
	w.logger.Printf("Received task assignment: %s", assignment.TaskId)

	w.setState(dto.WorkerStatus_WORKER_STATUS_BUSY, assignment.TaskId)

	// Execute task
	go w.executeTask(assignment)
}

func (w *Worker) executeTask(assignment *dto.TaskAssignment) {
	w.logger.Printf("Executing task: %s", assignment.TaskId)

	// Step 1: DATA_STAGING - Download input files
	w.reportTaskStatus(assignment.TaskId, dto.TaskStatus_TASK_STATUS_DATA_STAGING, "Staging input data", nil, nil)

	for _, signedFile := range assignment.InputFiles {
		if err := w.downloadFile(signedFile.Url, signedFile.LocalPath); err != nil {
			w.reportTaskStatus(assignment.TaskId, dto.TaskStatus_TASK_STATUS_FAILED,
				fmt.Sprintf("Failed to download input file %s: %v", signedFile.SourcePath, err), []string{err.Error()}, nil)
			return
		}
	}

	// Step 2: ENV_SETUP - Prepare execution environment
	w.reportTaskStatus(assignment.TaskId, dto.TaskStatus_TASK_STATUS_ENV_SETUP, "Setting up execution environment", nil, nil)

	// Get user home directory as working directory for task execution
	homeDir, err := os.UserHomeDir()
	if err != nil {
		// Fallback to current working directory if home directory cannot be determined
		homeDir, err = os.Getwd()
		if err != nil {
			// Final fallback to the worker's configured working directory
			homeDir = w.config.WorkingDir
		}
	}

	// Create task execution script in the home directory
	scriptPath := filepath.Join(homeDir, fmt.Sprintf("task_%s.sh", assignment.TaskId))
	w.logger.Printf("Task assignment details - TaskId: %s, Command: %s, ExecutionScript: %s", assignment.TaskId, assignment.Command, assignment.ExecutionScript)
	if err := w.createTaskScript(scriptPath, assignment.ExecutionScript); err != nil {
		w.reportTaskStatus(assignment.TaskId, dto.TaskStatus_TASK_STATUS_FAILED,
			fmt.Sprintf("Failed to create task script: %v", err), []string{err.Error()}, nil)
		return
	}

	log.Printf("Created task script at %s with content: %s", scriptPath, assignment.ExecutionScript)

	// Step 3: RUNNING - Execute the task
	w.reportTaskStatus(assignment.TaskId, dto.TaskStatus_TASK_STATUS_RUNNING, "Task execution started", nil, nil)

	// Execute the script in the home directory
	cmd := exec.CommandContext(w.ctx, "bash", scriptPath)
	cmd.Dir = homeDir

	w.logger.Printf("Executing command: bash %s in directory: %s", scriptPath, homeDir)

	// Set up output streaming
	stdout, err := cmd.StdoutPipe()
	if err != nil {
		w.reportTaskStatus(assignment.TaskId, dto.TaskStatus_TASK_STATUS_FAILED,
			fmt.Sprintf("Failed to create stdout pipe: %v", err), []string{err.Error()}, nil)
		return
	}

	stderr, err := cmd.StderrPipe()
	if err != nil {
		w.reportTaskStatus(assignment.TaskId, dto.TaskStatus_TASK_STATUS_FAILED,
			fmt.Sprintf("Failed to create stderr pipe: %v", err), []string{err.Error()}, nil)
		return
	}

	// Start command
	if err := cmd.Start(); err != nil {
		w.reportTaskStatus(assignment.TaskId, dto.TaskStatus_TASK_STATUS_FAILED,
			fmt.Sprintf("Failed to start task: %v", err), []string{err.Error()}, nil)
		return
	}

	w.logger.Printf("Command started successfully, PID: %d", cmd.Process.Pid)

	// Stream output and capture to file
	outputPath := filepath.Join(homeDir, "output.txt")
	outputFile, err := os.Create(outputPath)
	if err != nil {
		log.Printf("Warning: Failed to create output file: %v", err)
	} else {
		log.Printf("Created output file: %s", outputPath)
	}

	log.Printf("Starting output streaming goroutines for task %s", assignment.TaskId)
	go w.streamOutput(assignment.TaskId, stdout, dto.OutputType_OUTPUT_TYPE_STDOUT, outputFile)
	go w.streamOutput(assignment.TaskId, stderr, dto.OutputType_OUTPUT_TYPE_STDERR, nil)

	// Wait for completion
	err = cmd.Wait()

	// Give a moment for output to be written
	time.Sleep(100 * time.Millisecond)

	// Close output file after task completion
	if outputFile != nil {
		outputFile.Close()
	}

	// Update metrics
	w.metrics.TasksCompleted++

	if err != nil {
		w.reportTaskStatus(assignment.TaskId, dto.TaskStatus_TASK_STATUS_FAILED,
			fmt.Sprintf("Task execution failed: %v", err), []string{err.Error()}, nil)
		w.metrics.TasksFailed++
	} else {
		// Step 4: OUTPUT_STAGING - Stage output data
		w.reportTaskStatus(assignment.TaskId, dto.TaskStatus_TASK_STATUS_OUTPUT_STAGING, "Staging output data", nil, nil)

		// Upload output files
		for _, outputFile := range assignment.OutputFiles {
			if err := w.uploadFile(outputFile.Path, outputFile.Path); err != nil {
				log.Printf("Warning: Failed to upload output file %s: %v", outputFile.Path, err)
				// Don't fail the task for output upload issues, just log warning
			}
		}

		// Step 5: COMPLETED - Task completed successfully
		w.reportTaskStatusWithWorkDir(assignment.TaskId, dto.TaskStatus_TASK_STATUS_COMPLETED,
			"Task completed successfully", nil, nil)
	}

	// Remove script file
	os.Remove(scriptPath)

	// Set flag to wait for scheduler acknowledgment of task completion
	w.stateMutex.Lock()
	w.waitingForCompletionAck = true
	w.stateMutex.Unlock()

	w.logger.Printf("Task %s completed, waiting for scheduler acknowledgment", assignment.TaskId)
}

func (w *Worker) createTaskScript(scriptPath, scriptContent string) error {
	// Create directory if it doesn't exist
	dir := filepath.Dir(scriptPath)
	if err := os.MkdirAll(dir, 0755); err != nil {
		return fmt.Errorf("failed to create script directory: %w", err)
	}

	// Write script content
	if err := os.WriteFile(scriptPath, []byte(scriptContent), 0755); err != nil {
		return fmt.Errorf("failed to write script file: %w", err)
	}

	return nil
}

func (w *Worker) streamOutput(taskID string, reader io.Reader, outputType dto.OutputType, outputFile *os.File) {
	log.Printf("streamOutput started for task %s, outputType: %s, outputFile: %v", taskID, outputType, outputFile != nil)
	buffer := make([]byte, 1024)

	for {
		n, err := reader.Read(buffer)
		if n > 0 {
			// Send to scheduler via gRPC
			output := &dto.WorkerMessage{
				Message: &dto.WorkerMessage_TaskOutput{
					TaskOutput: &dto.TaskOutput{
						TaskId:    taskID,
						WorkerId:  w.config.WorkerID,
						Type:      outputType,
						Data:      buffer[:n],
						Timestamp: timestamppb.Now(),
					},
				},
			}

			if err := w.stream.Send(output); err != nil {
				log.Printf("Failed to send task output: %v", err)
				return
			}

			// Also write to output file if provided
			if outputFile != nil {
				if _, writeErr := outputFile.Write(buffer[:n]); writeErr != nil {
					log.Printf("Warning: Failed to write to output file: %v", writeErr)
				} else {
					log.Printf("Wrote %d bytes to output file", n)
				}
			}
		}

		if err != nil {
			if err != io.EOF {
				log.Printf("Error reading task output: %v", err)
			}
			return
		}
	}
}

func (w *Worker) reportTaskStatus(taskID string, status dto.TaskStatus, message string, errors []string, metrics *dto.TaskMetrics) {
	req := &dto.TaskStatusUpdateRequest{
		TaskId:   taskID,
		WorkerId: w.config.WorkerID,
		Status:   status,
		Message:  message,
		Errors:   errors,
		Metrics:  metrics,
		Metadata: map[string]string{
			"timestamp": time.Now().Format(time.RFC3339),
		},
	}

	resp, err := w.client.ReportTaskStatus(w.ctx, req)
	if err != nil {
		log.Printf("Failed to report task status: %v", err)
		return
	}

	// If this is a terminal status (COMPLETED, FAILED, CANCELED), clear the waiting flag
	if status == dto.TaskStatus_TASK_STATUS_COMPLETED ||
		status == dto.TaskStatus_TASK_STATUS_FAILED ||
		status == dto.TaskStatus_TASK_STATUS_CANCELLED {
		w.stateMutex.Lock()
		if w.waitingForCompletionAck {
			w.waitingForCompletionAck = false
			log.Printf("Task %s reached terminal status %s, cleared completion acknowledgment flag", taskID, status)
		}
		w.stateMutex.Unlock()
	}

	log.Printf("Task status update successful: %s", resp.Message)
}

func (w *Worker) reportTaskStatusWithWorkDir(taskID string, status dto.TaskStatus, message string, errors []string, metrics *dto.TaskMetrics) {
	// Get user home directory as default working directory
	homeDir, err := os.UserHomeDir()
	if err != nil {
		// Fallback to current working directory if home directory cannot be determined
		homeDir, err = os.Getwd()
		if err != nil {
			// Final fallback to the worker's configured working directory
			homeDir = w.config.WorkingDir
		}
	}

	req := &dto.TaskStatusUpdateRequest{
		TaskId:   taskID,
		WorkerId: w.config.WorkerID,
		Status:   status,
		Message:  message,
		Errors:   errors,
		Metrics:  metrics,
		Metadata: map[string]string{
			"timestamp": time.Now().Format(time.RFC3339),
			"work_dir":  homeDir,
		},
	}

	resp, err := w.client.ReportTaskStatus(w.ctx, req)
	if err != nil {
		log.Printf("Failed to report task status: %v", err)
		return
	}

	// If this is a terminal status (COMPLETED, FAILED, CANCELED), clear the waiting flag
	if status == dto.TaskStatus_TASK_STATUS_COMPLETED ||
		status == dto.TaskStatus_TASK_STATUS_FAILED ||
		status == dto.TaskStatus_TASK_STATUS_CANCELLED {
		w.stateMutex.Lock()
		if w.waitingForCompletionAck {
			w.waitingForCompletionAck = false
			log.Printf("Task %s reached terminal status %s, cleared completion acknowledgment flag", taskID, status)
		}
		w.stateMutex.Unlock()
	}

	if resp != nil && resp.Success {
		log.Printf("Task status update successful: %s", resp.Message)
	}
}

func (w *Worker) handleTaskCancellation(cancellation *dto.TaskCancellation) {
	w.logger.Printf("Received task cancellation: %s", cancellation.TaskId)

	_, taskID := w.getState()
	if taskID == cancellation.TaskId {
		w.logger.Printf("Cancelling current task: %s", cancellation.TaskId)

		// Send SIGTERM to task process for graceful shutdown
		if w.currentTaskProcess != nil && w.currentTaskProcess.Process != nil {
			w.logger.Printf("Sending SIGTERM to task process (PID: %d)", w.currentTaskProcess.Process.Pid)
			if err := w.currentTaskProcess.Process.Signal(syscall.SIGTERM); err != nil {
				log.Printf("Failed to send SIGTERM: %v", err)
			}

			// Wait for graceful shutdown (5 seconds)
			done := make(chan error, 1)
			go func() {
				done <- w.currentTaskProcess.Wait()
			}()

			select {
			case <-time.After(5 * time.Second):
				// Force kill if still running
				log.Printf("Task did not terminate gracefully, sending SIGKILL")
				if err := w.currentTaskProcess.Process.Kill(); err != nil {
					log.Printf("Failed to send SIGKILL: %v", err)
				}
			case err := <-done:
				if err != nil {
					log.Printf("Task terminated with error: %v", err)
				} else {
					log.Printf("Task terminated gracefully")
				}
			}
		}

		// Clear current task
		w.setTaskID("")
		w.currentTaskProcess = nil
		w.setStatus(dto.WorkerStatus_WORKER_STATUS_IDLE)

		log.Printf("Task cancellation completed")
	}
}

func (w *Worker) handleWorkerShutdown(shutdown *dto.WorkerShutdown) {
	log.Printf("Received worker shutdown request: %s", shutdown.Reason)

	// Cancel context to stop all operations
	w.cancel()
}

func (w *Worker) handleConfigUpdate(update *dto.ConfigUpdate) {
	log.Printf("Received config update")

	// Handle config update
	if update.Config != nil {
		if update.Config.HeartbeatInterval != nil {
			w.config.HeartbeatInterval = update.Config.HeartbeatInterval.AsDuration()
		}
		if update.Config.TaskTimeout != nil {
			w.config.TaskTimeout = update.Config.TaskTimeout.AsDuration()
		}
	}
}

func (w *Worker) updateMetrics() {
	// Read CPU usage from /proc/stat
	cpuUsage, err := w.readCPUUsage()
	if err != nil {
		log.Printf("Failed to read CPU usage: %v", err)
		cpuUsage = 0.0
	}

	// Read memory usage from /proc/meminfo
	memUsage, err := w.readMemoryUsage()
	if err != nil {
		log.Printf("Failed to read memory usage: %v", err)
		memUsage = 0.0
	}

	// Read disk usage from df command
	diskUsage, err := w.readDiskUsage()
	if err != nil {
		log.Printf("Failed to read disk usage: %v", err)
		diskUsage = 0
	}

	w.metrics.CpuUsagePercent = float32(cpuUsage)
	w.metrics.MemoryUsagePercent = float32(memUsage)
	w.metrics.DiskUsageBytes = diskUsage
	w.metrics.Timestamp = timestamppb.Now()
}

func (w *Worker) getSystemCapabilities() *dto.WorkerCapabilities {
	// Get actual system capabilities
	maxMemoryMB := w.getTotalMemoryMB()
	maxDiskGB := w.getTotalDiskGB()
	maxGPUs := w.detectGPUCount()

	return &dto.WorkerCapabilities{
		MaxCpuCores:       int32(runtime.NumCPU()),
		MaxMemoryMb:       maxMemoryMB,
		MaxDiskGb:         maxDiskGB,
		MaxGpus:           maxGPUs,
		SupportedRuntimes: []string{"bash", "python", "conda"},
		Metadata: map[string]string{
			"os":         runtime.GOOS,
			"arch":       runtime.GOARCH,
			"go_version": runtime.Version(),
		},
	}
}

func (w *Worker) stop() {
	log.Println("Stopping dto...")

	// Cancel context
	w.cancel()

	// Close stream
	if w.stream != nil {
		w.stream.CloseSend()
	}

	// Close connection
	if w.conn != nil {
		w.conn.Close()
	}

	log.Println("Worker stopped")
}

func getHostname() string {
	hostname, err := os.Hostname()
	if err != nil {
		return "unknown"
	}
	return hostname
}

// downloadFile downloads a file from a signed URL
func (w *Worker) downloadFile(url, destPath string) error {
	resp, err := http.Get(url)
	if err != nil {
		return err
	}
	defer resp.Body.Close()

	if resp.StatusCode != 200 {
		return fmt.Errorf("download failed with status %d", resp.StatusCode)
	}

	// Create directory if it doesn't exist
	if err := os.MkdirAll(filepath.Dir(destPath), 0755); err != nil {
		return err
	}

	// Create file
	out, err := os.Create(destPath)
	if err != nil {
		return err
	}
	defer out.Close()

	// Copy data
	_, err = io.Copy(out, resp.Body)
	return err
}

// handleOutputUploadRequest handles output file upload requests from server
func (w *Worker) handleOutputUploadRequest(req *dto.OutputUploadRequest) {
	log.Printf("Received output upload request for task %s", req.TaskId)

	for _, uploadURL := range req.UploadUrls {
		if err := w.uploadFile(uploadURL.LocalPath, uploadURL.Url); err != nil {
			log.Printf("Failed to upload output file %s: %v", uploadURL.LocalPath, err)
			continue
		}
	}

	// Report upload completion
	w.reportTaskStatusWithWorkDir(req.TaskId, dto.TaskStatus_TASK_STATUS_COMPLETED,
		"Task and output upload completed", nil, nil)
}

// uploadFile uploads a file to a signed URL
func (w *Worker) uploadFile(sourcePath, url string) error {
	file, err := os.Open(sourcePath)
	if err != nil {
		return err
	}
	defer file.Close()

	req, err := http.NewRequest("PUT", url, file)
	if err != nil {
		return err
	}

	resp, err := http.DefaultClient.Do(req)
	if err != nil {
		return err
	}
	defer resp.Body.Close()

	if resp.StatusCode != 200 && resp.StatusCode != 201 {
		return fmt.Errorf("upload failed with status %d", resp.StatusCode)
	}

	return nil
}

// readCPUUsage reads CPU usage from /proc/stat
func (w *Worker) readCPUUsage() (float64, error) {
	// Use gopsutil for cross-platform CPU usage
	percentages, err := cpu.Percent(time.Second, false)
	if err != nil {
		return 0, fmt.Errorf("failed to get CPU usage: %w", err)
	}

	if len(percentages) == 0 {
		return 0, fmt.Errorf("no CPU data available")
	}

	return percentages[0], nil
}

// readMemoryUsage reads memory usage using cross-platform library
func (w *Worker) readMemoryUsage() (float64, error) {
	// Use gopsutil for cross-platform memory usage
	vmStat, err := mem.VirtualMemory()
	if err != nil {
		return 0, fmt.Errorf("failed to get memory usage: %w", err)
	}

	return vmStat.UsedPercent, nil
}

// readDiskUsage reads disk usage using cross-platform library
func (w *Worker) readDiskUsage() (int64, error) {
	// Use gopsutil for cross-platform disk usage
	usage, err := disk.Usage("/")
	if err != nil {
		return 0, fmt.Errorf("failed to get disk usage: %w", err)
	}

	return int64(usage.Used), nil
}

// getTotalMemoryMB gets total system memory in MB using cross-platform library
func (w *Worker) getTotalMemoryMB() int32 {
	vmStat, err := mem.VirtualMemory()
	if err != nil {
		return 8192 // fallback
	}

	return int32(vmStat.Total / 1024 / 1024) // Convert bytes to MB
}

// getTotalDiskGB gets total disk space in GB using cross-platform library
func (w *Worker) getTotalDiskGB() int32 {
	usage, err := disk.Usage("/")
	if err != nil {
		return 100 // fallback
	}

	return int32(usage.Total / 1024 / 1024 / 1024) // Convert bytes to GB
}

// detectGPUCount detects number of GPUs using nvidia-smi
func (w *Worker) detectGPUCount() int32 {
	cmd := exec.Command("nvidia-smi", "--list-gpus")
	output, err := cmd.Output()
	if err != nil {
		// nvidia-smi not available, try lspci
		return w.detectGPUCountLspci()
	}

	lines := strings.Split(string(output), "\n")
	count := 0
	for _, line := range lines {
		if strings.Contains(line, "GPU") {
			count++
		}
	}
	return int32(count)
}

// detectGPUCountLspci detects GPUs using lspci as fallback
func (w *Worker) detectGPUCountLspci() int32 {
	cmd := exec.Command("lspci")
	output, err := cmd.Output()
	if err != nil {
		return 0
	}

	lines := strings.Split(string(output), "\n")
	count := 0
	for _, line := range lines {
		if strings.Contains(strings.ToLower(line), "vga") ||
			strings.Contains(strings.ToLower(line), "display") ||
			strings.Contains(strings.ToLower(line), "nvidia") ||
			strings.Contains(strings.ToLower(line), "amd") {
			count++
		}
	}
	return int32(count)
}
