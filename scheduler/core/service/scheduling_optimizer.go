package services

import (
	"context"
	"fmt"
	"strings"

	"github.com/apache/airavata/scheduler/core/domain"
	ports "github.com/apache/airavata/scheduler/core/port"
)

// WorkerPoolPlan defines how many workers to provision on each resource
type WorkerPoolPlan struct {
	ExperimentID       string
	TotalWorkers       int
	WorkersPerResource map[string]int // resourceID -> worker_count
	EstimatedCost      float64
}

// SchedulingOptimizer calculates optimal worker pool allocation
type SchedulingOptimizer struct {
	repo ports.RepositoryPort
}

// NewSchedulingOptimizer creates a new SchedulingOptimizer
func NewSchedulingOptimizer(repo ports.RepositoryPort) *SchedulingOptimizer {
	return &SchedulingOptimizer{
		repo: repo,
	}
}

// CalculateOptimalWorkerPool computes the least-cost worker pool strategy
func (so *SchedulingOptimizer) CalculateOptimalWorkerPool(
	ctx context.Context,
	analysis *ComputeAnalysisResult,
	accessibleResources []*domain.ComputeResource,
) (*WorkerPoolPlan, error) {

	fmt.Printf("=== SCHEDULING COST ANALYSIS ===\n")
	fmt.Printf("Experiment ID: %s\n", analysis.ExperimentID)
	fmt.Printf("Total Tasks: %d\n", analysis.TotalTasks)
	fmt.Printf("CPU Cores per Task: %d\n", analysis.CPUCoresPerTask)
	fmt.Printf("Memory per Task: %d MB\n", analysis.MemoryMBPerTask)
	fmt.Printf("GPUs per Task: %d\n", analysis.GPUsPerTask)
	fmt.Printf("Available Resources: %d\n", len(accessibleResources))

	plan := &WorkerPoolPlan{
		ExperimentID:       analysis.ExperimentID,
		WorkersPerResource: make(map[string]int),
	}

	// Get current queue depth for each resource
	queueDepth := so.getQueueDepths(ctx, accessibleResources)
	fmt.Printf("\n--- QUEUE DEPTH ANALYSIS ---\n")
	for _, resource := range accessibleResources {
		fmt.Printf("Resource %s: %d queued tasks\n", resource.ID, queueDepth[resource.ID])
	}

	// Cost factors:
	// 1. Minimize worker count (workers < tasks)
	// 2. Consider queue depth (prefer less busy resources)
	// 3. Consider data locality (prefer resources close to data)
	// 4. Respect resource constraints (max workers per resource)

	fmt.Printf("\n--- RESOURCE SCORING BREAKDOWN ---\n")
	fmt.Printf("Scoring weights: Data Locality=60%%, Queue Depth=40%%\n")

	var resourceScores []struct {
		resourceID    string
		localityScore float64
		queueScore    float64
		finalScore    float64
		workerCount   int
	}

	for _, resource := range accessibleResources {
		// Score this resource based on:
		localityScore := so.calculateLocalityScore(analysis, resource.ID)
		queueScore := 1.0 / float64(queueDepth[resource.ID]+1)
		finalScore := localityScore*0.6 + queueScore*0.4

		// Allocate workers proportional to score
		// Ensure: total_workers < total_tasks
		workerCount := so.allocateWorkers(analysis.TotalTasks, finalScore)

		resourceScores = append(resourceScores, struct {
			resourceID    string
			localityScore float64
			queueScore    float64
			finalScore    float64
			workerCount   int
		}{
			resourceID:    resource.ID,
			localityScore: localityScore,
			queueScore:    queueScore,
			finalScore:    finalScore,
			workerCount:   workerCount,
		})

		fmt.Printf("Resource %s:\n", resource.ID)
		fmt.Printf("  - Data Locality Score: %.3f (%.1f%% of tasks have local data)\n", localityScore, localityScore*100)
		fmt.Printf("  - Queue Depth Score: %.3f (1/(%d+1) = %.3f)\n", queueScore, queueDepth[resource.ID], queueScore)
		fmt.Printf("  - Final Score: %.3f (%.3f*0.6 + %.3f*0.4)\n", finalScore, localityScore, queueScore)
		fmt.Printf("  - Initial Worker Allocation: %d\n", workerCount)

		if workerCount > 0 {
			plan.WorkersPerResource[resource.ID] = workerCount
			plan.TotalWorkers += workerCount
		}
	}

	// Ensure constraint: workers < tasks
	fmt.Printf("\n--- CONSTRAINT ENFORCEMENT ---\n")
	fmt.Printf("Initial total workers: %d\n", plan.TotalWorkers)
	fmt.Printf("Constraint: workers < tasks (%d)\n", analysis.TotalTasks)

	if plan.TotalWorkers >= analysis.TotalTasks {
		// Scale down proportionally
		scaleFactor := float64(analysis.TotalTasks-1) / float64(plan.TotalWorkers)
		fmt.Printf("Scaling down by factor: %.3f\n", scaleFactor)

		plan.TotalWorkers = 0
		for resourceID, count := range plan.WorkersPerResource {
			newCount := int(float64(count) * scaleFactor)
			if newCount < 1 {
				newCount = 1
			}
			fmt.Printf("  Resource %s: %d -> %d workers\n", resourceID, count, newCount)
			plan.WorkersPerResource[resourceID] = newCount
			plan.TotalWorkers += newCount
		}
	}

	// Calculate detailed cost breakdown
	fmt.Printf("\n--- COST CALCULATION BREAKDOWN ---\n")
	plan.EstimatedCost = so.calculateDetailedCost(plan, accessibleResources, analysis)

	fmt.Printf("\n=== FINAL SCHEDULING STRATEGY ===\n")
	fmt.Printf("Total Workers: %d\n", plan.TotalWorkers)
	fmt.Printf("Workers per Resource:\n")
	for resourceID, count := range plan.WorkersPerResource {
		fmt.Printf("  - %s: %d workers\n", resourceID, count)
	}
	fmt.Printf("Estimated Total Cost: 🪙%.2f\n", plan.EstimatedCost)
	fmt.Printf("Cost per Worker: 🪙%.2f\n", plan.EstimatedCost/float64(plan.TotalWorkers))
	fmt.Printf("=====================================\n")

	return plan, nil
}

// calculateLocalityScore scores a resource based on data proximity
func (so *SchedulingOptimizer) calculateLocalityScore(analysis *ComputeAnalysisResult, resourceID string) float64 {
	// Count how many tasks have data on storage resources near this compute resource
	localTasks := 0

	for _, dataLocs := range analysis.DataLocations {
		for _, storageLoc := range dataLocs {
			// Check if storage location is co-located with compute resource
			if so.isColocated(resourceID, storageLoc) {
				localTasks++
				break
			}
		}
	}

	if analysis.TotalTasks == 0 {
		return 0.0
	}

	return float64(localTasks) / float64(analysis.TotalTasks)
}

// getQueueDepths returns current queue depth per resource
func (so *SchedulingOptimizer) getQueueDepths(ctx context.Context, resources []*domain.ComputeResource) map[string]int {
	depths := make(map[string]int)

	for _, resource := range resources {
		// Count queued tasks assigned to this resource
		tasks, _, _ := so.repo.GetTasksByStatus(ctx, domain.TaskStatusQueued, 10000, 0)
		count := 0
		for _, task := range tasks {
			if task.ComputeResourceID == resource.ID {
				count++
			}
		}
		depths[resource.ID] = count
	}

	return depths
}

// allocateWorkers calculates how many workers to allocate based on score
func (so *SchedulingOptimizer) allocateWorkers(totalTasks int, score float64) int {
	// Simple allocation: allocate 1 worker per 2-3 tasks, scaled by score
	baseWorkers := totalTasks / 3
	if baseWorkers < 1 {
		baseWorkers = 1
	}

	allocated := int(float64(baseWorkers) * score)
	if allocated < 1 {
		allocated = 1
	}

	return allocated
}

// calculateCost estimates the cost of the worker pool plan
func (so *SchedulingOptimizer) calculateCost(plan *WorkerPoolPlan, resources []*domain.ComputeResource) float64 {
	// Simple cost calculation based on number of workers
	// In a real implementation, this would consider resource pricing, walltime, etc.
	return float64(plan.TotalWorkers) * 1.0 // 🪙1 per worker per hour
}

// calculateDetailedCost provides a detailed cost breakdown with logging
func (so *SchedulingOptimizer) calculateDetailedCost(plan *WorkerPoolPlan, resources []*domain.ComputeResource, analysis *ComputeAnalysisResult) float64 {
	totalCost := 0.0

	fmt.Printf("Cost calculation parameters:\n")
	fmt.Printf("  - Base worker cost: 🪙1.00/hour\n")
	fmt.Printf("  - Estimated walltime: 30 minutes (0.5 hours)\n")
	fmt.Printf("  - CPU cost factor: 🪙0.10 per core per hour\n")
	fmt.Printf("  - Memory cost factor: 🪙0.05 per GB per hour\n")
	fmt.Printf("  - GPU cost factor: 🪙2.00 per GPU per hour\n")
	fmt.Printf("  - Data transfer cost: 🪙0.01 per GB\n")

	for resourceID, workerCount := range plan.WorkersPerResource {
		// Find the resource details
		var resource *domain.ComputeResource
		for _, r := range resources {
			if r.ID == resourceID {
				resource = r
				break
			}
		}

		if resource == nil {
			continue
		}

		fmt.Printf("\nResource %s (%d workers):\n", resourceID, workerCount)

		// Base worker cost
		baseCost := float64(workerCount) * 1.0 * 0.5 // 🪙1/hour * 0.5 hours
		fmt.Printf("  - Base worker cost: %d workers × 🪙1.00/hour × 0.5h = 🪙%.2f\n", workerCount, baseCost)

		// CPU cost
		cpuCost := float64(workerCount) * float64(analysis.CPUCoresPerTask) * 0.10 * 0.5
		fmt.Printf("  - CPU cost: %d workers × %d cores × 🪙0.10/core/hour × 0.5h = 🪙%.2f\n", workerCount, analysis.CPUCoresPerTask, cpuCost)

		// Memory cost
		memoryGB := float64(analysis.MemoryMBPerTask) / 1024.0
		memoryCost := float64(workerCount) * memoryGB * 0.05 * 0.5
		fmt.Printf("  - Memory cost: %d workers × %.2f GB × 🪙0.05/GB/hour × 0.5h = 🪙%.2f\n", workerCount, memoryGB, memoryCost)

		// GPU cost (if applicable)
		gpuCost := 0.0
		if analysis.GPUsPerTask > 0 {
			gpuCost = float64(workerCount) * float64(analysis.GPUsPerTask) * 2.0 * 0.5
			fmt.Printf("  - GPU cost: %d workers × %d GPUs × 🪙2.00/GPU/hour × 0.5h = 🪙%.2f\n", workerCount, analysis.GPUsPerTask, gpuCost)
		}

		// Data transfer cost (estimated based on task count)
		dataTransferCost := float64(analysis.TotalTasks) * 0.01 // 🪙0.01 per GB per task
		fmt.Printf("  - Data transfer cost: %d tasks × 🪙0.01/GB = 🪙%.2f\n", analysis.TotalTasks, dataTransferCost)

		// Resource type multiplier
		resourceMultiplier := 1.0
		switch resource.Type {
		case domain.ComputeResourceTypeSlurm:
			resourceMultiplier = 0.8 // SLURM clusters are typically cheaper
			fmt.Printf("  - Resource type: SLURM (0.8x multiplier)\n")
		case domain.ComputeResourceTypeKubernetes:
			resourceMultiplier = 1.2 // K8s has overhead
			fmt.Printf("  - Resource type: Kubernetes (1.2x multiplier)\n")
		case domain.ComputeResourceTypeBareMetal:
			resourceMultiplier = 1.0 // Standard pricing
			fmt.Printf("  - Resource type: Bare Metal (1.0x multiplier)\n")
		default:
			fmt.Printf("  - Resource type: Unknown (1.0x multiplier)\n")
		}

		resourceTotalCost := (baseCost + cpuCost + memoryCost + gpuCost + dataTransferCost) * resourceMultiplier
		fmt.Printf("  - Subtotal before multiplier: 🪙%.2f\n", baseCost+cpuCost+memoryCost+gpuCost+dataTransferCost)
		fmt.Printf("  - Resource type multiplier: %.1fx\n", resourceMultiplier)
		fmt.Printf("  - Resource total cost: 🪙%.2f\n", resourceTotalCost)

		totalCost += resourceTotalCost
	}

	fmt.Printf("\nTotal estimated cost: 🪙%.2f\n", totalCost)

	return totalCost
}

// isColocated checks if compute and storage resources are co-located
func (so *SchedulingOptimizer) isColocated(computeResourceID, storageResourceID string) bool {
	// Check resource metadata for location/datacenter/region
	// For now, simple name-based heuristic
	return strings.Contains(computeResourceID, storageResourceID) ||
		strings.Contains(storageResourceID, computeResourceID)
}
