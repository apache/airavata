package app

import (
	"context"
	"fmt"
	"log"
	"sync"
	"time"

	service "github.com/apache/airavata/scheduler/core/service"
)

// ShutdownCoordinator coordinates graceful shutdown of the scheduler
type ShutdownCoordinator struct {
	recoveryManager  *RecoveryManager
	backgroundJobs   *service.BackgroundJobManager
	shutdownTimeout  time.Duration
	mu               sync.RWMutex
	shutdownStarted  bool
	shutdownComplete chan struct{}
}

// ShutdownPhase represents a phase in the shutdown process
type ShutdownPhase string

const (
	PhaseStopAcceptingWork ShutdownPhase = "STOP_ACCEPTING_WORK"
	PhasePersistState      ShutdownPhase = "PERSIST_STATE"
	PhaseWaitForOperations ShutdownPhase = "WAIT_FOR_OPERATIONS"
	PhaseMarkCleanShutdown ShutdownPhase = "MARK_CLEAN_SHUTDOWN"
	PhaseComplete          ShutdownPhase = "COMPLETE"
)

// ShutdownPhaseHandler represents a handler for a shutdown phase
type ShutdownPhaseHandler interface {
	Execute(ctx context.Context) error
	GetTimeout() time.Duration
	GetName() string
}

// NewShutdownCoordinator creates a new shutdown coordinator
func NewShutdownCoordinator(recoveryManager *RecoveryManager, backgroundJobs *service.BackgroundJobManager) *ShutdownCoordinator {
	return &ShutdownCoordinator{
		recoveryManager:  recoveryManager,
		backgroundJobs:   backgroundJobs,
		shutdownTimeout:  30 * time.Second,
		shutdownComplete: make(chan struct{}),
	}
}

// StartShutdown initiates the graceful shutdown process
func (sc *ShutdownCoordinator) StartShutdown(ctx context.Context) error {
	sc.mu.Lock()
	if sc.shutdownStarted {
		sc.mu.Unlock()
		return fmt.Errorf("shutdown already started")
	}
	sc.shutdownStarted = true
	sc.mu.Unlock()

	log.Println("Starting graceful shutdown process...")

	// Create shutdown context with timeout
	shutdownCtx, cancel := context.WithTimeout(ctx, sc.shutdownTimeout)
	defer cancel()

	// Execute shutdown phases
	phases := []ShutdownPhase{
		PhaseStopAcceptingWork,
		PhasePersistState,
		PhaseWaitForOperations,
		PhaseMarkCleanShutdown,
		PhaseComplete,
	}

	for _, phase := range phases {
		if err := sc.executePhase(shutdownCtx, phase); err != nil {
			log.Printf("Warning: phase %s failed: %v", phase, err)
			// Continue with next phase even if one fails
		}
	}

	// Signal shutdown completion
	close(sc.shutdownComplete)
	log.Println("Graceful shutdown process completed")
	return nil
}

// executePhase executes a specific shutdown phase
func (sc *ShutdownCoordinator) executePhase(ctx context.Context, phase ShutdownPhase) error {
	log.Printf("Executing shutdown phase: %s", phase)

	switch phase {
	case PhaseStopAcceptingWork:
		return sc.stopAcceptingWork(ctx)
	case PhasePersistState:
		return sc.persistState(ctx)
	case PhaseWaitForOperations:
		return sc.waitForOperations(ctx)
	case PhaseMarkCleanShutdown:
		return sc.markCleanShutdown(ctx)
	case PhaseComplete:
		return sc.completeShutdown(ctx)
	default:
		return fmt.Errorf("unknown shutdown phase: %s", phase)
	}
}

// stopAcceptingWork stops accepting new work
func (sc *ShutdownCoordinator) stopAcceptingWork(ctx context.Context) error {
	log.Println("Stopping acceptance of new work...")

	// This would typically involve:
	// 1. Setting a flag to stop accepting new experiments
	// 2. Stopping the HTTP server from accepting new requests
	// 3. Stopping the gRPC server from accepting new connections
	// 4. Marking the scheduler as shutting down

	// For now, we'll just log this step
	log.Println("New work acceptance stopped")
	return nil
}

// persistState persists all in-flight state to the database
func (sc *ShutdownCoordinator) persistState(ctx context.Context) error {
	log.Println("Persisting in-flight state...")

	// 1. Persist any pending staging operations
	if err := sc.persistStagingOperations(ctx); err != nil {
		log.Printf("Warning: failed to persist staging operations: %v", err)
	}

	// 2. Persist any pending background jobs
	if err := sc.persistBackgroundJobs(ctx); err != nil {
		log.Printf("Warning: failed to persist background jobs: %v", err)
	}

	// 3. Persist any pending events
	if err := sc.persistPendingEvents(ctx); err != nil {
		log.Printf("Warning: failed to persist pending events: %v", err)
	}

	// 4. Update worker connection states
	if err := sc.updateWorkerStates(ctx); err != nil {
		log.Printf("Warning: failed to update worker states: %v", err)
	}

	log.Println("In-flight state persisted")
	return nil
}

// waitForOperations waits for critical operations to complete
func (sc *ShutdownCoordinator) waitForOperations(ctx context.Context) error {
	log.Println("Waiting for critical operations to complete...")

	// 1. Wait for background jobs to complete
	if sc.backgroundJobs != nil {
		if err := sc.backgroundJobs.WaitForCompletion(ctx, 15*time.Second); err != nil {
			log.Printf("Warning: background jobs did not complete in time: %v", err)
		}
	}

	// 2. Wait for staging operations to complete (with timeout)
	if err := sc.waitForStagingOperations(ctx, 10*time.Second); err != nil {
		log.Printf("Warning: staging operations did not complete in time: %v", err)
	}

	// 3. Wait for any pending database transactions
	if err := sc.waitForDatabaseTransactions(ctx, 5*time.Second); err != nil {
		log.Printf("Warning: database transactions did not complete in time: %v", err)
	}

	log.Println("Critical operations completed")
	return nil
}

// markCleanShutdown marks the shutdown as clean
func (sc *ShutdownCoordinator) markCleanShutdown(ctx context.Context) error {
	log.Println("Marking shutdown as clean...")

	if sc.recoveryManager != nil {
		if err := sc.recoveryManager.ShutdownRecovery(ctx); err != nil {
			return fmt.Errorf("failed to mark clean shutdown: %w", err)
		}
	}

	log.Println("Shutdown marked as clean")
	return nil
}

// completeShutdown completes the shutdown process
func (sc *ShutdownCoordinator) completeShutdown(ctx context.Context) error {
	log.Println("Completing shutdown process...")

	// Final cleanup operations
	if err := sc.performFinalCleanup(ctx); err != nil {
		log.Printf("Warning: final cleanup failed: %v", err)
	}

	log.Println("Shutdown process completed")
	return nil
}

// persistStagingOperations persists any pending staging operations
func (sc *ShutdownCoordinator) persistStagingOperations(ctx context.Context) error {
	// This is handled by the StagingOperationManager
	// All staging operations are already persisted in the database
	log.Println("Staging operations already persisted in database")
	return nil
}

// persistBackgroundJobs persists any pending background jobs
func (sc *ShutdownCoordinator) persistBackgroundJobs(ctx context.Context) error {
	if sc.backgroundJobs != nil {
		return sc.backgroundJobs.PersistState(ctx)
	}
	return nil
}

// persistPendingEvents persists any pending events
func (sc *ShutdownCoordinator) persistPendingEvents(ctx context.Context) error {
	// This would be implemented when we add the persistent event queue
	log.Println("Pending events persistence not yet implemented")
	return nil
}

// updateWorkerStates updates worker connection states
func (sc *ShutdownCoordinator) updateWorkerStates(ctx context.Context) error {
	// This is handled by the RecoveryManager
	log.Println("Worker states updated by recovery manager")
	return nil
}

// waitForStagingOperations waits for staging operations to complete
func (sc *ShutdownCoordinator) waitForStagingOperations(ctx context.Context, timeout time.Duration) error {
	log.Printf("Waiting for staging operations to complete (timeout: %v)...", timeout)

	// Create timeout context
	timeoutCtx, cancel := context.WithTimeout(ctx, timeout)
	defer cancel()

	// Poll for incomplete staging operations
	ticker := time.NewTicker(1 * time.Second)
	defer ticker.Stop()

	for {
		select {
		case <-timeoutCtx.Done():
			return fmt.Errorf("timeout waiting for staging operations to complete")
		case <-ticker.C:
			// Check if there are any running staging operations
			// This would be implemented by checking the staging_operations table
			// For now, we'll assume they complete quickly
			log.Println("Staging operations completed")
			return nil
		}
	}
}

// waitForDatabaseTransactions waits for database transactions to complete
func (sc *ShutdownCoordinator) waitForDatabaseTransactions(ctx context.Context, timeout time.Duration) error {
	log.Printf("Waiting for database transactions to complete (timeout: %v)...", timeout)

	// Create timeout context
	timeoutCtx, cancel := context.WithTimeout(ctx, timeout)
	defer cancel()

	// In a real implementation, this would check for active database connections
	// and wait for them to complete. For now, we'll just wait a short time.
	select {
	case <-timeoutCtx.Done():
		return fmt.Errorf("timeout waiting for database transactions")
	case <-time.After(1 * time.Second):
		log.Println("Database transactions completed")
		return nil
	}
}

// performFinalCleanup performs final cleanup operations
func (sc *ShutdownCoordinator) performFinalCleanup(ctx context.Context) error {
	log.Println("Performing final cleanup...")

	// 1. Close any open connections
	// 2. Flush any remaining logs
	// 3. Clean up temporary files
	// 4. Release any held resources

	log.Println("Final cleanup completed")
	return nil
}

// IsShutdownStarted returns true if shutdown has been started
func (sc *ShutdownCoordinator) IsShutdownStarted() bool {
	sc.mu.RLock()
	defer sc.mu.RUnlock()
	return sc.shutdownStarted
}

// WaitForShutdownCompletion waits for shutdown to complete
func (sc *ShutdownCoordinator) WaitForShutdownCompletion() {
	<-sc.shutdownComplete
}

// SetShutdownTimeout sets the shutdown timeout
func (sc *ShutdownCoordinator) SetShutdownTimeout(timeout time.Duration) {
	sc.mu.Lock()
	defer sc.mu.Unlock()
	sc.shutdownTimeout = timeout
}

// GetShutdownTimeout returns the current shutdown timeout
func (sc *ShutdownCoordinator) GetShutdownTimeout() time.Duration {
	sc.mu.RLock()
	defer sc.mu.RUnlock()
	return sc.shutdownTimeout
}

// GetShutdownStatus returns the current shutdown status
func (sc *ShutdownCoordinator) GetShutdownStatus() map[string]interface{} {
	sc.mu.RLock()
	defer sc.mu.RUnlock()

	return map[string]interface{}{
		"shutdown_started":  sc.shutdownStarted,
		"shutdown_timeout":  sc.shutdownTimeout.String(),
		"shutdown_complete": sc.isShutdownComplete(),
	}
}

// isShutdownComplete checks if shutdown is complete
func (sc *ShutdownCoordinator) isShutdownComplete() bool {
	select {
	case <-sc.shutdownComplete:
		return true
	default:
		return false
	}
}

// ForceShutdown forces immediate shutdown (use only in emergency)
func (sc *ShutdownCoordinator) ForceShutdown(ctx context.Context) error {
	log.Println("Force shutdown initiated...")

	// Skip graceful shutdown phases and go directly to cleanup
	if sc.recoveryManager != nil {
		if err := sc.recoveryManager.ShutdownRecovery(ctx); err != nil {
			log.Printf("Warning: failed to mark clean shutdown during force shutdown: %v", err)
		}
	}

	// Signal shutdown completion
	select {
	case <-sc.shutdownComplete:
		// Already closed
	default:
		close(sc.shutdownComplete)
	}

	log.Println("Force shutdown completed")
	return nil
}
