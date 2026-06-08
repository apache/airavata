package testutil

import (
	"encoding/json"
	"fmt"
	"time"
)

// TestMetricsCollector collects and analyzes test metrics
type TestMetricsCollector struct {
	startTime            time.Time
	endTime              time.Time
	taskMetrics          *TaskMetrics
	workerMetrics        *WorkerMetrics
	stagingMetrics       *StagingMetrics
	failureMetrics       *FailureMetrics
	dataIntegrityMetrics *DataIntegrityMetrics
}

// NewTestMetricsCollector creates a new test metrics collector
func NewTestMetricsCollector() *TestMetricsCollector {
	return &TestMetricsCollector{
		startTime:            time.Now(),
		taskMetrics:          &TaskMetrics{},
		workerMetrics:        &WorkerMetrics{},
		stagingMetrics:       &StagingMetrics{},
		failureMetrics:       &FailureMetrics{},
		dataIntegrityMetrics: &DataIntegrityMetrics{},
	}
}

// StartTest starts collecting metrics for a test
func (tmc *TestMetricsCollector) StartTest(testName string) {
	tmc.startTime = time.Now()
	tmc.taskMetrics.TestName = testName
	tmc.workerMetrics.TestName = testName
	tmc.stagingMetrics.TestName = testName
	tmc.failureMetrics.TestName = testName
	tmc.dataIntegrityMetrics.TestName = testName
}

// EndTest ends metric collection for a test
func (tmc *TestMetricsCollector) EndTest() {
	tmc.endTime = time.Now()
	tmc.taskMetrics.TotalDuration = tmc.endTime.Sub(tmc.startTime)
	tmc.workerMetrics.TotalDuration = tmc.endTime.Sub(tmc.startTime)
	tmc.stagingMetrics.TotalDuration = tmc.endTime.Sub(tmc.startTime)
	tmc.failureMetrics.TotalDuration = tmc.endTime.Sub(tmc.startTime)
	tmc.dataIntegrityMetrics.TotalDuration = tmc.endTime.Sub(tmc.startTime)
}

// RecordTaskCompletion records a task completion
func (tmc *TestMetricsCollector) RecordTaskCompletion(taskID string, duration time.Duration, computeResource string) {
	tmc.taskMetrics.CompletedTasks++
	tmc.taskMetrics.TaskDurations = append(tmc.taskMetrics.TaskDurations, duration)
	tmc.taskMetrics.ComputeResourceUsage[computeResource]++
}

// RecordTaskFailure records a task failure
func (tmc *TestMetricsCollector) RecordTaskFailure(taskID string, reason string) {
	tmc.taskMetrics.FailedTasks++
	tmc.taskMetrics.FailureReasons[reason]++
}

// RecordWorkerSpawn records a worker spawn
func (tmc *TestMetricsCollector) RecordWorkerSpawn(workerID string, computeResource string, walltime time.Duration) {
	tmc.workerMetrics.WorkersSpawned++
	tmc.workerMetrics.ComputeResourceUsage[computeResource]++
	tmc.workerMetrics.WalltimeAllocations = append(tmc.workerMetrics.WalltimeAllocations, walltime)
}

// RecordWorkerTermination records a worker termination
func (tmc *TestMetricsCollector) RecordWorkerTermination(workerID string, reason string) {
	tmc.workerMetrics.WorkersTerminated++
	tmc.workerMetrics.TerminationReasons[reason]++
}

// RecordStagingOperation records a staging operation
func (tmc *TestMetricsCollector) RecordStagingOperation(operationType string, duration time.Duration, fileSize int64) {
	tmc.stagingMetrics.OperationsPerformed++
	tmc.stagingMetrics.OperationDurations = append(tmc.stagingMetrics.OperationDurations, duration)
	tmc.stagingMetrics.OperationTypes[operationType]++
	tmc.stagingMetrics.TotalDataTransferred += fileSize
}

// RecordFailure records a failure event
func (tmc *TestMetricsCollector) RecordFailure(failureType string, component string, duration time.Duration) {
	tmc.failureMetrics.FailuresDetected++
	tmc.failureMetrics.FailureTypes[failureType]++
	tmc.failureMetrics.ComponentFailures[component]++
	tmc.failureMetrics.RecoveryTimes = append(tmc.failureMetrics.RecoveryTimes, duration)
}

// RecordDataIntegrityCheck records a data integrity check
func (tmc *TestMetricsCollector) RecordDataIntegrityCheck(checkType string, passed bool, fileCount int) {
	tmc.dataIntegrityMetrics.ChecksPerformed++
	tmc.dataIntegrityMetrics.CheckTypes[checkType]++
	if passed {
		tmc.dataIntegrityMetrics.ChecksPassed++
	} else {
		tmc.dataIntegrityMetrics.ChecksFailed++
	}
	tmc.dataIntegrityMetrics.FilesVerified += fileCount
}

// GenerateReport generates a comprehensive test report
func (tmc *TestMetricsCollector) GenerateReport() *TestReport {
	report := &TestReport{
		TestName:             tmc.taskMetrics.TestName,
		StartTime:            tmc.startTime,
		EndTime:              tmc.endTime,
		TotalDuration:        tmc.endTime.Sub(tmc.startTime),
		TaskMetrics:          tmc.taskMetrics,
		WorkerMetrics:        tmc.workerMetrics,
		StagingMetrics:       tmc.stagingMetrics,
		FailureMetrics:       tmc.failureMetrics,
		DataIntegrityMetrics: tmc.dataIntegrityMetrics,
	}

	// Calculate derived metrics
	report.CalculateDerivedMetrics()

	return report
}

// TaskMetrics represents metrics about task execution
type TaskMetrics struct {
	TestName             string          `json:"testName"`
	TotalDuration        time.Duration   `json:"totalDuration"`
	CompletedTasks       int             `json:"completedTasks"`
	FailedTasks          int             `json:"failedTasks"`
	TaskDurations        []time.Duration `json:"taskDurations"`
	ComputeResourceUsage map[string]int  `json:"computeResourceUsage"`
	FailureReasons       map[string]int  `json:"failureReasons"`
}

// WorkerMetrics represents metrics about worker management
type WorkerMetrics struct {
	TestName             string          `json:"testName"`
	TotalDuration        time.Duration   `json:"totalDuration"`
	WorkersSpawned       int             `json:"workersSpawned"`
	WorkersTerminated    int             `json:"workersTerminated"`
	WalltimeAllocations  []time.Duration `json:"walltimeAllocations"`
	ComputeResourceUsage map[string]int  `json:"computeResourceUsage"`
	TerminationReasons   map[string]int  `json:"terminationReasons"`
}

// StagingMetrics represents metrics about data staging
type StagingMetrics struct {
	TestName             string          `json:"testName"`
	TotalDuration        time.Duration   `json:"totalDuration"`
	OperationsPerformed  int             `json:"operationsPerformed"`
	OperationDurations   []time.Duration `json:"operationDurations"`
	OperationTypes       map[string]int  `json:"operationTypes"`
	TotalDataTransferred int64           `json:"totalDataTransferred"`
}

// FailureMetrics represents metrics about failures and recovery
type FailureMetrics struct {
	TestName          string          `json:"testName"`
	TotalDuration     time.Duration   `json:"totalDuration"`
	FailuresDetected  int             `json:"failuresDetected"`
	FailureTypes      map[string]int  `json:"failureTypes"`
	ComponentFailures map[string]int  `json:"componentFailures"`
	RecoveryTimes     []time.Duration `json:"recoveryTimes"`
}

// DataIntegrityMetrics represents metrics about data integrity
type DataIntegrityMetrics struct {
	TestName        string         `json:"testName"`
	TotalDuration   time.Duration  `json:"totalDuration"`
	ChecksPerformed int            `json:"checksPerformed"`
	ChecksPassed    int            `json:"checksPassed"`
	ChecksFailed    int            `json:"checksFailed"`
	CheckTypes      map[string]int `json:"checkTypes"`
	FilesVerified   int            `json:"filesVerified"`
}

// TestReport represents a comprehensive test report
type TestReport struct {
	TestName             string                `json:"testName"`
	StartTime            time.Time             `json:"startTime"`
	EndTime              time.Time             `json:"endTime"`
	TotalDuration        time.Duration         `json:"totalDuration"`
	TaskMetrics          *TaskMetrics          `json:"taskMetrics"`
	WorkerMetrics        *WorkerMetrics        `json:"workerMetrics"`
	StagingMetrics       *StagingMetrics       `json:"stagingMetrics"`
	FailureMetrics       *FailureMetrics       `json:"failureMetrics"`
	DataIntegrityMetrics *DataIntegrityMetrics `json:"dataIntegrityMetrics"`

	// Derived metrics
	AverageTaskDuration time.Duration `json:"averageTaskDuration"`
	TasksPerSecond      float64       `json:"tasksPerSecond"`
	WorkerUtilization   float64       `json:"workerUtilization"`
	AverageStagingTime  time.Duration `json:"averageStagingTime"`
	DataTransferRate    float64       `json:"dataTransferRate"`
	FailureRate         float64       `json:"failureRate"`
	AverageRecoveryTime time.Duration `json:"averageRecoveryTime"`
	DataIntegrityRate   float64       `json:"dataIntegrityRate"`
}

// CalculateDerivedMetrics calculates derived metrics from the collected data
func (tr *TestReport) CalculateDerivedMetrics() {
	// Calculate average task duration
	if len(tr.TaskMetrics.TaskDurations) > 0 {
		var total time.Duration
		for _, duration := range tr.TaskMetrics.TaskDurations {
			total += duration
		}
		tr.AverageTaskDuration = total / time.Duration(len(tr.TaskMetrics.TaskDurations))
	}

	// Calculate tasks per second
	if tr.TotalDuration > 0 {
		tr.TasksPerSecond = float64(tr.TaskMetrics.CompletedTasks) / tr.TotalDuration.Seconds()
	}

	// Calculate worker utilization
	if tr.WorkerMetrics.WorkersSpawned > 0 {
		tr.WorkerUtilization = float64(tr.WorkerMetrics.WorkersTerminated) / float64(tr.WorkerMetrics.WorkersSpawned)
	}

	// Calculate average staging time
	if len(tr.StagingMetrics.OperationDurations) > 0 {
		var total time.Duration
		for _, duration := range tr.StagingMetrics.OperationDurations {
			total += duration
		}
		tr.AverageStagingTime = total / time.Duration(len(tr.StagingMetrics.OperationDurations))
	}

	// Calculate data transfer rate
	if tr.TotalDuration > 0 {
		tr.DataTransferRate = float64(tr.StagingMetrics.TotalDataTransferred) / tr.TotalDuration.Seconds()
	}

	// Calculate failure rate
	totalTasks := tr.TaskMetrics.CompletedTasks + tr.TaskMetrics.FailedTasks
	if totalTasks > 0 {
		tr.FailureRate = float64(tr.TaskMetrics.FailedTasks) / float64(totalTasks)
	}

	// Calculate average recovery time
	if len(tr.FailureMetrics.RecoveryTimes) > 0 {
		var total time.Duration
		for _, duration := range tr.FailureMetrics.RecoveryTimes {
			total += duration
		}
		tr.AverageRecoveryTime = total / time.Duration(len(tr.FailureMetrics.RecoveryTimes))
	}

	// Calculate data integrity rate
	if tr.DataIntegrityMetrics.ChecksPerformed > 0 {
		tr.DataIntegrityRate = float64(tr.DataIntegrityMetrics.ChecksPassed) / float64(tr.DataIntegrityMetrics.ChecksPerformed)
	}
}

// ToJSON converts the test report to JSON
func (tr *TestReport) ToJSON() ([]byte, error) {
	return json.MarshalIndent(tr, "", "  ")
}

// PrintSummary prints a summary of the test report
func (tr *TestReport) PrintSummary() {
	fmt.Printf("\n=== Test Report Summary ===\n")
	fmt.Printf("Test: %s\n", tr.TestName)
	fmt.Printf("Duration: %v\n", tr.TotalDuration)
	fmt.Printf("Tasks: %d completed, %d failed (%.2f%% success rate)\n",
		tr.TaskMetrics.CompletedTasks, tr.TaskMetrics.FailedTasks,
		(1-tr.FailureRate)*100)
	fmt.Printf("Workers: %d spawned, %d terminated (%.2f%% utilization)\n",
		tr.WorkerMetrics.WorkersSpawned, tr.WorkerMetrics.WorkersTerminated,
		tr.WorkerUtilization*100)
	fmt.Printf("Staging: %d operations, %.2f MB/s transfer rate\n",
		tr.StagingMetrics.OperationsPerformed, tr.DataTransferRate/1024/1024)
	fmt.Printf("Failures: %d detected, %.2f%% failure rate\n",
		tr.FailureMetrics.FailuresDetected, tr.FailureRate*100)
	fmt.Printf("Data Integrity: %d/%d checks passed (%.2f%%)\n",
		tr.DataIntegrityMetrics.ChecksPassed, tr.DataIntegrityMetrics.ChecksPerformed,
		tr.DataIntegrityRate*100)
	fmt.Printf("Performance: %.2f tasks/second\n", tr.TasksPerSecond)
	fmt.Printf("===========================\n\n")
}

// CompareReports compares two test reports
func CompareReports(report1, report2 *TestReport) *ReportComparison {
	comparison := &ReportComparison{
		Test1: report1,
		Test2: report2,
	}

	// Compare key metrics
	comparison.TaskDurationDiff = report2.AverageTaskDuration - report1.AverageTaskDuration
	comparison.TasksPerSecondDiff = report2.TasksPerSecond - report1.TasksPerSecond
	comparison.WorkerUtilizationDiff = report2.WorkerUtilization - report1.WorkerUtilization
	comparison.StagingTimeDiff = report2.AverageStagingTime - report1.AverageStagingTime
	comparison.DataTransferRateDiff = report2.DataTransferRate - report1.DataTransferRate
	comparison.FailureRateDiff = report2.FailureRate - report1.FailureRate
	comparison.RecoveryTimeDiff = report2.AverageRecoveryTime - report1.AverageRecoveryTime
	comparison.DataIntegrityRateDiff = report2.DataIntegrityRate - report1.DataIntegrityRate

	return comparison
}

// ReportComparison represents a comparison between two test reports
type ReportComparison struct {
	Test1                 *TestReport   `json:"test1"`
	Test2                 *TestReport   `json:"test2"`
	TaskDurationDiff      time.Duration `json:"taskDurationDiff"`
	TasksPerSecondDiff    float64       `json:"tasksPerSecondDiff"`
	WorkerUtilizationDiff float64       `json:"workerUtilizationDiff"`
	StagingTimeDiff       time.Duration `json:"stagingTimeDiff"`
	DataTransferRateDiff  float64       `json:"dataTransferRateDiff"`
	FailureRateDiff       float64       `json:"failureRateDiff"`
	RecoveryTimeDiff      time.Duration `json:"recoveryTimeDiff"`
	DataIntegrityRateDiff float64       `json:"dataIntegrityRateDiff"`
}

// PrintComparison prints a comparison between two reports
func (rc *ReportComparison) PrintComparison() {
	fmt.Printf("\n=== Report Comparison ===\n")
	fmt.Printf("Task Duration: %v (%+.2f%%)\n", rc.TaskDurationDiff,
		rc.TaskDurationDiff.Seconds()/rc.Test1.AverageTaskDuration.Seconds()*100)
	fmt.Printf("Tasks/Second: %.2f (%+.2f%%)\n", rc.TasksPerSecondDiff,
		rc.TasksPerSecondDiff/rc.Test1.TasksPerSecond*100)
	fmt.Printf("Worker Utilization: %.2f%% (%+.2f%%)\n", rc.WorkerUtilizationDiff*100,
		rc.WorkerUtilizationDiff/rc.Test1.WorkerUtilization*100)
	fmt.Printf("Staging Time: %v (%+.2f%%)\n", rc.StagingTimeDiff,
		rc.StagingTimeDiff.Seconds()/rc.Test1.AverageStagingTime.Seconds()*100)
	fmt.Printf("Data Transfer Rate: %.2f MB/s (%+.2f%%)\n", rc.DataTransferRateDiff/1024/1024,
		rc.DataTransferRateDiff/rc.Test1.DataTransferRate*100)
	fmt.Printf("Failure Rate: %.2f%% (%+.2f%%)\n", rc.FailureRateDiff*100,
		rc.FailureRateDiff/rc.Test1.FailureRate*100)
	fmt.Printf("Recovery Time: %v (%+.2f%%)\n", rc.RecoveryTimeDiff,
		rc.RecoveryTimeDiff.Seconds()/rc.Test1.AverageRecoveryTime.Seconds()*100)
	fmt.Printf("Data Integrity Rate: %.2f%% (%+.2f%%)\n", rc.DataIntegrityRateDiff*100,
		rc.DataIntegrityRateDiff/rc.Test1.DataIntegrityRate*100)
	fmt.Printf("========================\n\n")
}
