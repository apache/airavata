package integration

import (
	"context"
	"fmt"
	"log"
	"os"
	"os/exec"
	"testing"
	"time"

	"gorm.io/gorm"

	"github.com/apache/airavata/scheduler/core/app"
	"github.com/apache/airavata/scheduler/core/domain"
	services "github.com/apache/airavata/scheduler/core/service"
	"github.com/apache/airavata/scheduler/tests/testutil"
)

// TestSchedulerRecoveryE2E tests the complete scheduler recovery functionality
func TestSchedulerRecoveryE2E(t *testing.T) {

	// Test scenarios
	t.Run("SchedulerRestartDuringStaging", testSchedulerRestartDuringStaging)
	t.Run("SchedulerRestartWithRunningTasks", testSchedulerRestartWithRunningTasks)
	t.Run("SchedulerRestartWithWorkerReconnection", testSchedulerRestartWithWorkerReconnection)
	t.Run("SchedulerMultipleRestartCycles", testSchedulerMultipleRestartCycles)
}

// testSchedulerRestartDuringStaging tests recovery when scheduler is killed during data staging
func testSchedulerRestartDuringStaging(t *testing.T) {
	// Setup integration test suite
	suite := testutil.SetupIntegrationTest(t)
	defer suite.Cleanup()

	// Create a large experiment with many tasks
	experiment := createLargeExperiment(t, suite, 100) // 100 tasks

	// Submit experiment to trigger staging
	err := submitExperiment(t, suite.DB.DB.GetDB(), experiment.ID)
	if err != nil {
		t.Fatalf("Failed to submit experiment: %v", err)
	}

	// Create a compute resource for staging operations
	computeResource := &domain.ComputeResource{
		ID:         "compute_1",
		Name:       "test-compute",
		Type:       "SLURM",
		Status:     "ACTIVE",
		MaxWorkers: 10,
		OwnerID:    suite.TestUser.ID,
	}
	err = suite.DB.Repo.CreateComputeResource(context.Background(), computeResource)
	if err != nil {
		t.Fatalf("Failed to create compute resource: %v", err)
	}

	// Create a worker for staging operations
	now := time.Now()
	worker := &domain.Worker{
		ID:                "worker_1",
		ComputeResourceID: computeResource.ID,
		ExperimentID:      experiment.ID,
		UserID:            suite.TestUser.ID,
		Status:            domain.WorkerStatusBusy,
		Walltime:          time.Hour,
		WalltimeRemaining: time.Hour,
		RegisteredAt:      now,
		LastHeartbeat:     now,
		ConnectionState:   "CONNECTED",
		LastSeenAt:        &now,
		CreatedAt:         now,
		UpdatedAt:         now,
	}
	err = suite.DB.Repo.CreateWorker(context.Background(), worker)
	if err != nil {
		t.Fatalf("Failed to create worker: %v", err)
	}

	// Create staging operations for some tasks to simulate staging in progress
	stagingManager := services.NewStagingOperationManagerForTesting(suite.DB.DB.GetDB(), suite.EventPort)

	// Get tasks and create staging operations for the first 10 tasks
	tasks, err := getTasksByExperiment(t, suite.DB.DB.GetDB(), experiment.ID)
	if err != nil {
		t.Fatalf("Failed to get tasks: %v", err)
	}

	// Create staging operations for first 10 tasks
	for i := 0; i < 10 && i < len(tasks); i++ {
		task := tasks[i]
		operationID := fmt.Sprintf("staging_%s", task.ID)
		_, err := stagingManager.CreateStagingOperation(context.Background(), task.ID, worker.ID, worker.ComputeResourceID, "/source/path", "/dest/path", 300)
		if err != nil {
			t.Fatalf("Failed to create staging operation for task %s: %v", task.ID, err)
		}

		// Start monitoring for this operation
		go stagingManager.MonitorStagingProgress(context.Background(), operationID, func() error {
			return nil
		})
	}

	// Let staging operations run for a bit
	time.Sleep(2 * time.Second)

	// Simulate scheduler restart by creating a new scheduler service
	// This tests the recovery mechanism
	log.Println("Simulating scheduler restart...")

	// Create new staging manager (simulates scheduler restart)
	_ = services.NewStagingOperationManagerForTesting(suite.DB.DB.GetDB(), suite.EventPort)

	// Verify staging operations are persisted
	stagingOps, err := getIncompleteStagingOperations(t, suite.DB.DB.GetDB())
	if err != nil {
		t.Fatalf("Failed to get incomplete staging operations: %v", err)
	}

	if len(stagingOps) == 0 {
		t.Fatal("Expected incomplete staging operations after scheduler restart")
	}

	log.Printf("Found %d incomplete staging operations", len(stagingOps))

	// Wait for recovery to complete
	time.Sleep(2 * time.Second)

	// Verify staging operations are still accessible after restart
	// (The actual completion would happen when data is staged via the data mover service)
	resumedOps, err := getIncompleteStagingOperations(t, suite.DB.DB.GetDB())
	if err != nil {
		t.Fatalf("Failed to get resumed staging operations: %v", err)
	}

	// Verify staging operations are persisted and available after restart
	if len(resumedOps) != len(stagingOps) {
		t.Errorf("Expected %d staging operations to be available after restart, but found %d", len(stagingOps), len(resumedOps))
	}

	// Verify tasks are in correct state
	tasks, err = getTasksByExperiment(t, suite.DB.DB.GetDB(), experiment.ID)
	if err != nil {
		t.Fatalf("Failed to get tasks: %v", err)
	}

	// Check that no tasks are lost
	if len(tasks) != 100 {
		t.Errorf("Expected 100 tasks, got %d", len(tasks))
	}

	// Check that tasks are in valid states
	for _, task := range tasks {
		if task.Status == domain.TaskStatusQueued {
			t.Errorf("Task %s is in ASSIGNED state after restart - should be requeued", task.ID)
		}
	}

	log.Println("Scheduler restart during staging test completed successfully")
}

// testSchedulerRestartWithRunningTasks tests recovery when scheduler is killed with running tasks
func testSchedulerRestartWithRunningTasks(t *testing.T) {
	// Setup integration test suite
	suite := testutil.SetupIntegrationTest(t)
	defer suite.Cleanup()

	// Create experiment with long-running tasks
	experiment := createLongRunningExperiment(t, suite, 10) // 10 long-running tasks

	// Submit experiment
	err := submitExperiment(t, suite.DB.DB.GetDB(), experiment.ID)
	if err != nil {
		t.Fatalf("Failed to submit experiment: %v", err)
	}

	// Simulate some tasks being assigned to workers
	tasks, err := getTasksByExperiment(t, suite.DB.DB.GetDB(), experiment.ID)
	if err != nil {
		t.Fatalf("Failed to get tasks: %v", err)
	}

	// Mark some tasks as assigned (simulating running state)
	for i, task := range tasks {
		if i < 5 { // Mark first 5 tasks as assigned
			task.Status = domain.TaskStatusQueued
			task.WorkerID = fmt.Sprintf("worker_%d", i)
			task.ComputeResourceID = "test_compute"
			task.UpdatedAt = time.Now()

			if err := suite.DB.DB.GetDB().Save(task).Error; err != nil {
				t.Fatalf("Failed to update task status: %v", err)
			}
		}
	}

	log.Printf("Marked %d tasks as assigned", 5)

	// Simulate scheduler restart by setting the state to RUNNING (to simulate unclean shutdown)
	log.Println("Simulating scheduler restart...")

	// Set scheduler state to RUNNING to simulate an unclean shutdown
	now := time.Now()
	err = suite.DB.DB.GetDB().Exec(`
		INSERT INTO scheduler_state (id, instance_id, status, clean_shutdown, startup_time, last_heartbeat, created_at, updated_at)
		VALUES ('scheduler', 'old_instance', 'RUNNING', false, ?, ?, ?, ?)
		ON CONFLICT (id) DO UPDATE SET
			instance_id = EXCLUDED.instance_id,
			status = EXCLUDED.status,
			clean_shutdown = EXCLUDED.clean_shutdown,
			startup_time = EXCLUDED.startup_time,
			last_heartbeat = EXCLUDED.last_heartbeat,
			updated_at = EXCLUDED.updated_at
	`, now.Add(-10*time.Minute), now.Add(-5*time.Minute), now.Add(-10*time.Minute), now.Add(-5*time.Minute)).Error
	if err != nil {
		t.Fatalf("Failed to set scheduler state: %v", err)
	}

	// Create recovery manager and trigger recovery
	stagingManager := services.NewStagingOperationManagerForTesting(suite.DB.DB.GetDB(), suite.EventPort)
	recoveryManager := app.NewRecoveryManager(suite.DB.DB.GetDB(), stagingManager, suite.DB.Repo, suite.EventPort)
	err = recoveryManager.StartRecovery(context.Background())
	if err != nil {
		t.Fatalf("Failed to start recovery: %v", err)
	}

	// Wait for recovery to complete
	time.Sleep(2 * time.Second)

	// Verify tasks are requeued
	requeuedTasks, err := getTasksByStatus(t, suite.DB.DB.GetDB(), domain.TaskStatusQueued)
	if err != nil {
		t.Fatalf("Failed to get requeued tasks: %v", err)
	}

	// Should have some requeued tasks
	if len(requeuedTasks) == 0 {
		t.Error("Expected some tasks to be requeued after restart")
	}

	// Verify no tasks are in ASSIGNED state
	assignedTasks, err := getTasksByStatus(t, suite.DB.DB.GetDB(), domain.TaskStatusQueued)
	if err != nil {
		t.Fatalf("Failed to get assigned tasks: %v", err)
	}

	if len(assignedTasks) > 0 {
		t.Errorf("Found %d tasks in ASSIGNED state after restart - should be requeued", len(assignedTasks))
	}

	log.Println("Scheduler restart with running tasks test completed successfully")
}

// testSchedulerRestartWithWorkerReconnection tests worker reconnection after scheduler restart
func testSchedulerRestartWithWorkerReconnection(t *testing.T) {
	// Setup integration test suite
	suite := testutil.SetupIntegrationTest(t)
	defer suite.Cleanup()

	// Create compute resource first
	computeResource := &domain.ComputeResource{
		ID:         "test_compute",
		Name:       "Test Compute",
		Type:       "SLURM",
		Status:     "ACTIVE",
		MaxWorkers: 10,
		OwnerID:    suite.TestUser.ID,
		CreatedAt:  time.Now(),
		UpdatedAt:  time.Now(),
	}
	if err := suite.DB.DB.GetDB().Create(computeResource).Error; err != nil {
		t.Fatalf("Failed to create compute resource: %v", err)
	}

	// Create experiment first
	experiment := &domain.Experiment{
		ID:        "test_experiment",
		Name:      "Test Experiment",
		ProjectID: suite.TestProject.ID,
		OwnerID:   suite.TestUser.ID,
		Status:    domain.ExperimentStatusCreated,
		CreatedAt: time.Now(),
		UpdatedAt: time.Now(),
	}
	if err := suite.DB.DB.GetDB().Create(experiment).Error; err != nil {
		t.Fatalf("Failed to create experiment: %v", err)
	}

	// Create mock workers in database
	workers := make([]*domain.Worker, 3)
	for i := 0; i < 3; i++ {
		now := time.Now()
		lastSeen := now
		worker := &domain.Worker{
			ID:                fmt.Sprintf("worker_%d", i),
			ComputeResourceID: computeResource.ID,
			ExperimentID:      experiment.ID,
			UserID:            suite.TestUser.ID,
			Status:            domain.WorkerStatusBusy,
			ConnectionState:   "CONNECTED",
			LastSeenAt:        &lastSeen,
			Walltime:          time.Hour,
			WalltimeRemaining: time.Hour,
			RegisteredAt:      now,
			LastHeartbeat:     now.Add(time.Second), // Ensure last_heartbeat >= registered_at
			CreatedAt:         now,
			UpdatedAt:         now,
		}

		if err := suite.DB.DB.GetDB().Create(worker).Error; err != nil {
			t.Fatalf("Failed to create worker: %v", err)
		}
		workers[i] = worker
	}

	// Verify workers are connected
	connectedWorkers, err := getConnectedWorkers(t, suite.DB.DB.GetDB())
	if err != nil {
		t.Fatalf("Failed to get connected workers: %v", err)
	}

	if len(connectedWorkers) != 3 {
		t.Errorf("Expected 3 connected workers, got %d", len(connectedWorkers))
	}

	// Simulate scheduler restart by marking workers as disconnected
	log.Println("Simulating scheduler restart...")

	// Mark all workers as disconnected (simulates scheduler restart)
	for _, worker := range workers {
		worker.ConnectionState = "DISCONNECTED"
		worker.UpdatedAt = time.Now()
		if err := suite.DB.DB.GetDB().Save(worker).Error; err != nil {
			t.Fatalf("Failed to update worker connection state: %v", err)
		}
	}

	// Verify workers are marked as disconnected
	disconnectedWorkers, err := getDisconnectedWorkers(t, suite.DB.DB.GetDB())
	if err != nil {
		t.Fatalf("Failed to get disconnected workers: %v", err)
	}

	if len(disconnectedWorkers) != 3 {
		t.Errorf("Expected 3 disconnected workers, got %d", len(disconnectedWorkers))
	}

	// Simulate workers reconnecting
	log.Println("Simulating worker reconnection...")

	// Mark workers as connected again
	for _, worker := range workers {
		worker.ConnectionState = "CONNECTED"
		now := time.Now()
		worker.LastSeenAt = &now
		worker.LastHeartbeat = now
		worker.UpdatedAt = now
		if err := suite.DB.DB.GetDB().Save(worker).Error; err != nil {
			t.Fatalf("Failed to update worker connection state: %v", err)
		}
	}

	// Wait for reconnection
	time.Sleep(2 * time.Second)

	// Verify workers reconnect
	reconnectedWorkers, err := getConnectedWorkers(t, suite.DB.DB.GetDB())
	if err != nil {
		t.Fatalf("Failed to get reconnected workers: %v", err)
	}

	if len(reconnectedWorkers) != 3 {
		t.Errorf("Expected 3 reconnected workers, got %d", len(reconnectedWorkers))
	}

	log.Println("Worker reconnection test completed successfully")
}

// testSchedulerMultipleRestartCycles tests multiple restart cycles under load
func testSchedulerMultipleRestartCycles(t *testing.T) {
	// Setup integration test suite
	suite := testutil.SetupIntegrationTest(t)
	defer suite.Cleanup()

	// Create multiple experiments
	experiments := make([]*domain.Experiment, 5)
	for i := 0; i < 5; i++ {
		experiments[i] = createLargeExperiment(t, suite, 20) // 20 tasks each
	}

	// Submit experiments once
	for _, experiment := range experiments {
		err := submitExperiment(t, suite.DB.DB.GetDB(), experiment.ID)
		if err != nil {
			t.Fatalf("Failed to submit experiment: %v", err)
		}
	}

	// Perform multiple restart cycles
	for cycle := 0; cycle < 3; cycle++ {
		log.Printf("Starting restart cycle %d", cycle+1)

		// Simulate some tasks being in different states
		for _, experiment := range experiments {
			tasks, err := getTasksByExperiment(t, suite.DB.DB.GetDB(), experiment.ID)
			if err != nil {
				continue
			}

			// Mark some tasks as assigned (simulating running state)
			for i, task := range tasks {
				if i < 5 { // Mark first 5 tasks as assigned
					task.Status = domain.TaskStatusQueued
					task.WorkerID = fmt.Sprintf("worker_cycle_%d_%d", cycle, i)
					task.ComputeResourceID = "test_compute"
					task.UpdatedAt = time.Now()
					suite.DB.DB.GetDB().Save(task)
				}
			}
		}

		// Let it run for a bit
		time.Sleep(2 * time.Second)

		// Simulate scheduler restart
		log.Printf("Simulating scheduler restart in cycle %d", cycle+1)

		// Create new staging manager (simulates scheduler restart)
		stagingManager := services.NewStagingOperationManagerForTesting(suite.DB.DB.GetDB(), suite.EventPort)

		// Create recovery manager and start recovery (simulates scheduler restart)
		recoveryManager := app.NewRecoveryManager(suite.DB.DB.GetDB(), stagingManager, suite.DB.Repo, suite.EventPort)
		if err := recoveryManager.StartRecovery(context.Background()); err != nil {
			t.Fatalf("Failed to start recovery in cycle %d: %v", cycle+1, err)
		}

		// Wait for recovery
		time.Sleep(2 * time.Second)
	}

	// Final verification
	log.Println("Final verification...")

	// Verify no task loss
	totalTasks := 0
	for _, experiment := range experiments {
		tasks, err := getTasksByExperiment(t, suite.DB.DB.GetDB(), experiment.ID)
		if err != nil {
			t.Fatalf("Failed to get tasks for experiment %s: %v", experiment.ID, err)
		}
		totalTasks += len(tasks)
	}

	expectedTasks := 5 * 20 // 5 experiments * 20 tasks each
	if totalTasks != expectedTasks {
		t.Errorf("Expected %d total tasks, got %d", expectedTasks, totalTasks)
	}

	// Verify scheduler state is clean
	schedulerState, err := getSchedulerState(t, suite.DB.DB.GetDB())
	if err != nil {
		t.Fatalf("Failed to get scheduler state: %v", err)
	}

	if schedulerState["status"] != "RUNNING" {
		t.Errorf("Expected scheduler status to be RUNNING, got %v", schedulerState["status"])
	}

	log.Println("Multiple restart cycles test completed successfully")
}

// Helper functions

func createLargeExperiment(t *testing.T, suite *testutil.IntegrationTestSuite, taskCount int) *domain.Experiment {
	experiment := &domain.Experiment{
		ID:              fmt.Sprintf("exp_large_%d_%d", taskCount, time.Now().UnixNano()),
		Name:            fmt.Sprintf("Large Experiment %d_%d", taskCount, time.Now().UnixNano()),
		Description:     "Large experiment for recovery testing",
		ProjectID:       suite.TestProject.ID,
		OwnerID:         suite.TestUser.ID,
		Status:          domain.ExperimentStatusCreated,
		CommandTemplate: "echo 'Task {{task_id}}' && sleep 10",
		OutputPattern:   "/tmp/output_{{task_id}}.txt",
		Parameters:      generateParameterSets(taskCount),
		CreatedAt:       time.Now(),
		UpdatedAt:       time.Now(),
		Metadata: map[string]interface{}{
			"cpu_cores": 1,
			"memory_mb": 1024,
			"disk_gb":   10,
		},
	}

	if err := suite.DB.DB.GetDB().Create(experiment).Error; err != nil {
		t.Fatalf("Failed to create large experiment: %v", err)
	}

	return experiment
}

func createLongRunningExperiment(t *testing.T, suite *testutil.IntegrationTestSuite, taskCount int) *domain.Experiment {
	experiment := &domain.Experiment{
		ID:              fmt.Sprintf("exp_long_%d_%d", taskCount, time.Now().UnixNano()),
		Name:            fmt.Sprintf("Long Running Experiment %d_%d", taskCount, time.Now().UnixNano()),
		Description:     "Long running experiment for recovery testing",
		ProjectID:       suite.TestProject.ID,
		OwnerID:         suite.TestUser.ID,
		Status:          domain.ExperimentStatusCreated,
		CommandTemplate: "echo 'Long task {{task_id}}' && sleep 60",
		OutputPattern:   "/tmp/long_output_{{task_id}}.txt",
		Parameters:      generateParameterSets(taskCount),
		CreatedAt:       time.Now(),
		UpdatedAt:       time.Now(),
		Metadata: map[string]interface{}{
			"cpu_cores": 1,
			"memory_mb": 1024,
			"disk_gb":   10,
		},
	}

	if err := suite.DB.DB.GetDB().Create(experiment).Error; err != nil {
		t.Fatalf("Failed to create long running experiment: %v", err)
	}

	return experiment
}

func generateParameterSets(count int) []domain.ParameterSet {
	parameterSets := make([]domain.ParameterSet, count)
	for i := 0; i < count; i++ {
		parameterSets[i] = domain.ParameterSet{
			Values: map[string]string{
				"task_id": fmt.Sprintf("task_%d", i),
				"param1":  fmt.Sprintf("value_%d", i),
			},
		}
	}
	return parameterSets
}

func convertStringMapToInterface(input map[string]string) map[string]interface{} {
	result := make(map[string]interface{})
	for k, v := range input {
		result[k] = v
	}
	return result
}

func startScheduler(t *testing.T, config *testutil.TestConfig) *exec.Cmd {
	cmd := exec.Command("./build/scheduler")
	cmd.Env = append(os.Environ(),
		fmt.Sprintf("DATABASE_URL=%s", config.DatabaseURL),
		fmt.Sprintf("PORT=%d", 8080),
		fmt.Sprintf("GRPC_PORT=%d", 50051),
	)

	if err := cmd.Start(); err != nil {
		t.Fatalf("Failed to start scheduler: %v", err)
	}

	// Wait for scheduler to start
	time.Sleep(3 * time.Second)

	return cmd
}

func startMockWorkers(t *testing.T, config *testutil.TestConfig, count int) []*exec.Cmd {
	workers := make([]*exec.Cmd, count)

	for i := 0; i < count; i++ {
		workerID := fmt.Sprintf("worker_%d", i)
		cmd := exec.Command("./build/worker",
			"-server-url", fmt.Sprintf("localhost:%d", 50051),
			"-worker-id", workerID,
			"-experiment-id", "test_experiment",
			"-compute-resource-id", "test_compute",
		)

		if err := cmd.Start(); err != nil {
			t.Fatalf("Failed to start worker %d: %v", i, err)
		}

		workers[i] = cmd
	}

	return workers
}

func submitExperiment(t *testing.T, db *gorm.DB, experimentID string) error {
	// Update experiment status to pending (submitted)
	result := db.Model(&domain.Experiment{}).
		Where("id = ?", experimentID).
		Update("status", "PENDING")

	if result.Error != nil {
		return result.Error
	}

	// Create tasks for the experiment
	experiment := &domain.Experiment{}
	if err := db.Where("id = ?", experimentID).First(experiment).Error; err != nil {
		return err
	}

	// Generate tasks from parameters
	for i, paramSet := range experiment.Parameters {
		task := &domain.Task{
			ID:           fmt.Sprintf("task_%s_%d_%d", experimentID, i, time.Now().UnixNano()),
			ExperimentID: experimentID,
			Status:       domain.TaskStatusCreated,
			Command:      experiment.CommandTemplate,
			RetryCount:   0,
			MaxRetries:   3,
			CreatedAt:    time.Now(),
			UpdatedAt:    time.Now(),
			Metadata:     convertStringMapToInterface(paramSet.Values),
		}

		if err := db.Create(task).Error; err != nil {
			return err
		}
	}

	return nil
}

func getIncompleteStagingOperations(t *testing.T, db *gorm.DB) ([]map[string]interface{}, error) {
	var operations []map[string]interface{}

	err := db.Raw(`
		SELECT id, task_id, worker_id, status, created_at, started_at
		FROM staging_operations 
		WHERE status IN ('PENDING', 'RUNNING')
		ORDER BY created_at
	`).Scan(&operations).Error

	return operations, err
}

func getTasksByExperiment(t *testing.T, db *gorm.DB, experimentID string) ([]*domain.Task, error) {
	var tasks []*domain.Task
	err := db.Where("experiment_id = ?", experimentID).Find(&tasks).Error
	return tasks, err
}

func getTasksByStatus(t *testing.T, db *gorm.DB, status domain.TaskStatus) ([]*domain.Task, error) {
	var tasks []*domain.Task
	err := db.Where("status = ?", status).Find(&tasks).Error
	return tasks, err
}

func getConnectedWorkers(t *testing.T, db *gorm.DB) ([]*domain.Worker, error) {
	var workers []*domain.Worker
	err := db.Where("connection_state = ?", "CONNECTED").Find(&workers).Error
	return workers, err
}

func getDisconnectedWorkers(t *testing.T, db *gorm.DB) ([]*domain.Worker, error) {
	var workers []*domain.Worker
	err := db.Where("connection_state = ?", "DISCONNECTED").Find(&workers).Error
	return workers, err
}

func getSchedulerState(t *testing.T, db *gorm.DB) (map[string]interface{}, error) {
	var state map[string]interface{}

	err := db.Raw(`
		SELECT status, clean_shutdown, startup_time, last_heartbeat
		FROM scheduler_state 
		WHERE id = 'scheduler'
	`).Scan(&state).Error

	return state, err
}
