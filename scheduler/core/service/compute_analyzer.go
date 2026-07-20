package services

import (
	"context"
	"fmt"
	"strings"
	"time"

	"github.com/apache/airavata/scheduler/core/domain"
	ports "github.com/apache/airavata/scheduler/core/port"
)

// ComputeAnalysisResult contains the analysis of an experiment's compute needs
type ComputeAnalysisResult struct {
	ExperimentID      string
	TotalTasks        int
	CPUCoresPerTask   int
	MemoryMBPerTask   int64
	GPUsPerTask       int
	EstimatedDuration time.Duration
	DataLocations     map[string][]string // taskID -> []storageResourceIDs
}

// ComputeAnalyzer analyzes experiments to determine compute requirements
type ComputeAnalyzer struct {
	repo      ports.RepositoryPort
	dataMover domain.DataMover
	authz     ports.AuthorizationPort
}

// NewComputeAnalyzer creates a new ComputeAnalyzer
func NewComputeAnalyzer(repo ports.RepositoryPort, dataMover domain.DataMover, authz ports.AuthorizationPort) *ComputeAnalyzer {
	return &ComputeAnalyzer{
		repo:      repo,
		dataMover: dataMover,
		authz:     authz,
	}
}

// AnalyzeExperiment analyzes an experiment's compute needs
func (ca *ComputeAnalyzer) AnalyzeExperiment(ctx context.Context, experimentID string) (*ComputeAnalysisResult, error) {
	// Get tasks for the experiment
	tasks, _, err := ca.repo.ListTasksByExperiment(ctx, experimentID, 10000, 0)
	if err != nil {
		return nil, err
	}

	result := &ComputeAnalysisResult{
		ExperimentID:  experimentID,
		TotalTasks:    len(tasks),
		DataLocations: make(map[string][]string),
	}

	// Analyze each task
	for _, task := range tasks {
		// Extract compute requirements from task metadata
		if task.Metadata != nil {
			if cpu, ok := task.Metadata["cpu_cores"].(float64); ok {
				if int(cpu) > result.CPUCoresPerTask {
					result.CPUCoresPerTask = int(cpu)
				}
			}
			if memory, ok := task.Metadata["memory_mb"].(float64); ok {
				if int64(memory) > result.MemoryMBPerTask {
					result.MemoryMBPerTask = int64(memory)
				}
			}
			if gpu, ok := task.Metadata["gpus"].(float64); ok {
				if int(gpu) > result.GPUsPerTask {
					result.GPUsPerTask = int(gpu)
				}
			}
		}

		// Determine data locations for this task
		dataLocs := ca.findDataLocations(ctx, task)
		result.DataLocations[task.ID] = dataLocs
	}

	// Set default values if not specified
	if result.CPUCoresPerTask == 0 {
		result.CPUCoresPerTask = 1
	}
	if result.MemoryMBPerTask == 0 {
		result.MemoryMBPerTask = 1024 // 1GB default
	}

	return result, nil
}

// findDataLocations finds which storage resources contain input data for a task
func (ca *ComputeAnalyzer) findDataLocations(ctx context.Context, task *domain.Task) []string {
	var locations []string

	// For now, we'll use a simple heuristic based on file paths
	// In a real implementation, this would query the data mover or storage registry
	for _, inputFile := range task.InputFiles {
		// Simple heuristic: if path contains "s3" or "minio", assume it's on S3 storage
		// if path contains "nfs", assume it's on NFS storage
		if strings.Contains(inputFile.Path, "s3") || strings.Contains(inputFile.Path, "minio") {
			locations = append(locations, "s3-storage")
		} else if strings.Contains(inputFile.Path, "nfs") {
			locations = append(locations, "nfs-storage")
		} else {
			// Default to local storage
			locations = append(locations, "local-storage")
		}
	}

	return locations
}

// LogDataLocalityAnalysis logs detailed data locality information
func (ca *ComputeAnalyzer) LogDataLocalityAnalysis(analysis *ComputeAnalysisResult) {
	fmt.Printf("\n--- DATA LOCALITY ANALYSIS ---\n")
	fmt.Printf("Total tasks analyzed: %d\n", analysis.TotalTasks)

	storageTypeCounts := make(map[string]int)
	taskLocalityCounts := make(map[string]int)

	for _, dataLocs := range analysis.DataLocations {
		// Count storage types used
		for _, loc := range dataLocs {
			storageTypeCounts[loc]++
		}

		// Count tasks by locality pattern
		localityKey := strings.Join(dataLocs, "+")
		taskLocalityCounts[localityKey]++
	}

	fmt.Printf("Storage type distribution:\n")
	for storageType, count := range storageTypeCounts {
		fmt.Printf("  - %s: %d files\n", storageType, count)
	}

	fmt.Printf("Task locality patterns:\n")
	for pattern, count := range taskLocalityCounts {
		fmt.Printf("  - %s: %d tasks\n", pattern, count)
	}
}

// ResolveAccessibleResources determines which compute resources user can access
func (ca *ComputeAnalyzer) ResolveAccessibleResources(ctx context.Context, userID string) ([]*domain.ComputeResource, error) {
	// Get all compute resources
	allResources, _, err := ca.repo.ListComputeResources(ctx, &ports.ComputeResourceFilters{}, 10000, 0)
	if err != nil {
		return nil, err
	}

	// Filter by authorization
	var accessible []*domain.ComputeResource
	for _, resource := range allResources {
		// Check if user has "execute" permission on resource
		allowed, err := ca.authz.CheckPermission(ctx, userID, "execute", "compute_resource", resource.ID)
		if err != nil {
			continue
		}
		if allowed {
			accessible = append(accessible, resource)
		}
	}

	return accessible, nil
}
