package services

import (
	"context"
	"crypto/sha256"
	"encoding/hex"
	"fmt"
	"io"
	"strings"
	"time"

	"github.com/apache/airavata/scheduler/core/domain"
	ports "github.com/apache/airavata/scheduler/core/port"
)

// DataMoverService implements the DataMover interface
type DataMoverService struct {
	repo    ports.RepositoryPort
	storage ports.StoragePort
	cache   ports.CachePort
	events  ports.EventPort
}

// Compile-time interface verification
var _ domain.DataMover = (*DataMoverService)(nil)

// NewDataMoverService creates a new DataMover service
func NewDataMoverService(repo ports.RepositoryPort, storage ports.StoragePort, cache ports.CachePort, events ports.EventPort) *DataMoverService {
	return &DataMoverService{
		repo:    repo,
		storage: storage,
		cache:   cache,
		events:  events,
	}
}

// BeginProactiveStaging begins proactive data staging for a task
func (s *DataMoverService) BeginProactiveStaging(
	ctx context.Context,
	taskID string,
	computeResourceID string,
	userID string,
) (*domain.StagingOperation, error) {
	// Get task from database
	task, err := s.repo.GetTaskByID(ctx, taskID)
	if err != nil {
		return nil, fmt.Errorf("task not found: %w", err)
	}
	if task == nil {
		return nil, domain.ErrTaskNotFound
	}

	// Create staging operation
	operation := &domain.StagingOperation{
		ID:                fmt.Sprintf("staging_%s_%d", taskID, time.Now().UnixNano()),
		TaskID:            taskID,
		ComputeResourceID: computeResourceID,
		Status:            "PENDING",
		TotalFiles:        len(task.InputFiles),
		CompletedFiles:    0,
		FailedFiles:       0,
		TotalBytes:        0,
		TransferredBytes:  0,
		StartTime:         time.Now(),
		Metadata: map[string]interface{}{
			"userId": userID,
		},
	}

	// Calculate total bytes
	for _, file := range task.InputFiles {
		operation.TotalBytes += file.Size
	}

	// Start staging asynchronously
	go s.executeProactiveStaging(ctx, operation, task)

	// Publish staging started event
	event := domain.NewAuditEvent(userID, "data.staging.started", "task", taskID)
	if err := s.events.Publish(ctx, event); err != nil {
		fmt.Printf("failed to publish staging started event: %v\n", err)
	}

	return operation, nil
}

// executeProactiveStaging executes the actual staging operation
func (s *DataMoverService) executeProactiveStaging(ctx context.Context, operation *domain.StagingOperation, task *domain.Task) {
	operation.Status = "IN_PROGRESS"

	// Stage each input file
	for _, inputFile := range task.InputFiles {
		// Check cache first
		cacheEntry, err := s.CheckCache(ctx, inputFile.Path, inputFile.Checksum, operation.ComputeResourceID)
		if err == nil && cacheEntry != nil {
			// File is already cached, skip transfer
			operation.CompletedFiles++
			operation.TransferredBytes += inputFile.Size
			continue
		}

		// Transfer file to compute storage
		destPath := s.generateComputeResourcePath(operation.ComputeResourceID, inputFile.Path)
		transferStart := time.Now()
		if err := s.storage.Transfer(ctx, s.storage, inputFile.Path, destPath); err != nil {
			operation.FailedFiles++
			operation.Error = fmt.Sprintf("failed to transfer input file %s: %v", inputFile.Path, err)
			fmt.Printf("Failed to transfer input file %s: %v\n", inputFile.Path, err)
			continue
		}
		transferDuration := time.Since(transferStart)

		// Verify data integrity
		verified, err := s.VerifyDataIntegrity(ctx, destPath, inputFile.Checksum)
		if err != nil {
			operation.FailedFiles++
			operation.Error = fmt.Sprintf("failed to verify data integrity for %s: %v", inputFile.Path, err)
			fmt.Printf("Failed to verify data integrity for %s: %v\n", inputFile.Path, err)
			continue
		}
		if !verified {
			operation.FailedFiles++
			operation.Error = fmt.Sprintf("data integrity check failed for %s", inputFile.Path)
			fmt.Printf("Data integrity check failed for %s\n", inputFile.Path)
			continue
		}

		// Record cache entry
		cacheEntry = &domain.CacheEntry{
			FilePath:          destPath,
			Checksum:          inputFile.Checksum,
			ComputeResourceID: operation.ComputeResourceID,
			SizeBytes:         inputFile.Size,
			CachedAt:          time.Now(),
			LastAccessed:      time.Now(),
		}
		if err := s.RecordCacheEntry(ctx, cacheEntry); err != nil {
			fmt.Printf("failed to record cache entry: %v\n", err)
		}

		// Record data lineage
		lineage := &domain.DataLineageInfo{
			FileID:           inputFile.Path,
			SourcePath:       inputFile.Path,
			DestinationPath:  destPath,
			SourceChecksum:   inputFile.Checksum,
			DestChecksum:     inputFile.Checksum,
			TransferSize:     inputFile.Size,
			TransferDuration: transferDuration,
			TransferredAt:    time.Now(),
			Metadata: map[string]interface{}{
				"taskId":             task.ID,
				"computeResourceId":  operation.ComputeResourceID,
				"stagingOperationId": operation.ID,
			},
		}
		if err := s.RecordDataLineage(ctx, lineage); err != nil {
			fmt.Printf("failed to record data lineage: %v\n", err)
		}

		operation.CompletedFiles++
		operation.TransferredBytes += inputFile.Size
	}

	// Update operation status
	if operation.FailedFiles > 0 {
		operation.Status = "FAILED"
	} else {
		operation.Status = "COMPLETED"
	}

	now := time.Now()
	operation.EndTime = &now

	// Publish staging completed event
	eventType := "data.staging.completed"
	if operation.Status == "FAILED" {
		eventType = "data.staging.failed"
	}

	event := domain.NewAuditEvent(operation.Metadata["userId"].(string), eventType, "task", operation.TaskID)
	if err := s.events.Publish(ctx, event); err != nil {
		fmt.Printf("failed to publish staging completed event: %v\n", err)
	}
}

// StageInputToWorker implements domain.DataMover.StageInputToWorker
func (s *DataMoverService) StageInputToWorker(ctx context.Context, task *domain.Task, workerID string, userID string) error {
	// Get worker
	worker, err := s.repo.GetWorkerByID(ctx, workerID)
	if err != nil {
		return fmt.Errorf("worker not found: %w", err)
	}
	if worker == nil {
		return domain.ErrWorkerNotFound
	}

	// Get compute resource to determine staging location
	computeResource, err := s.repo.GetComputeResourceByID(ctx, worker.ComputeResourceID)
	if err != nil {
		return fmt.Errorf("compute resource not found: %w", err)
	}
	if computeResource == nil {
		return domain.ErrResourceNotFound
	}

	// Stage each input file
	for _, inputFile := range task.InputFiles {
		// Check cache first
		cacheEntry, err := s.CheckCache(ctx, inputFile.Path, inputFile.Checksum, worker.ComputeResourceID)
		if err == nil && cacheEntry != nil {
			// File is already cached, skip transfer
			continue
		}

		// Transfer file to compute storage
		destPath := s.generateWorkerPath(workerID, inputFile.Path)
		transferStart := time.Now()
		if err := s.storage.Transfer(ctx, s.storage, inputFile.Path, destPath); err != nil {
			return fmt.Errorf("failed to transfer input file %s: %w", inputFile.Path, err)
		}
		transferDuration := time.Since(transferStart)

		// Verify data integrity
		verified, err := s.VerifyDataIntegrity(ctx, destPath, inputFile.Checksum)
		if err != nil {
			return fmt.Errorf("failed to verify data integrity for %s: %w", inputFile.Path, err)
		}
		if !verified {
			return fmt.Errorf("data integrity check failed for %s", inputFile.Path)
		}

		// Record cache entry
		cacheEntry = &domain.CacheEntry{
			FilePath:          destPath,
			Checksum:          inputFile.Checksum,
			ComputeResourceID: worker.ComputeResourceID,
			SizeBytes:         inputFile.Size,
			CachedAt:          time.Now(),
			LastAccessed:      time.Now(),
		}
		if err := s.RecordCacheEntry(ctx, cacheEntry); err != nil {
			fmt.Printf("failed to record cache entry: %v\n", err)
		}

		// Record data lineage
		lineage := &domain.DataLineageInfo{
			FileID:           inputFile.Path,
			SourcePath:       inputFile.Path,
			DestinationPath:  destPath,
			SourceChecksum:   inputFile.Checksum,
			DestChecksum:     inputFile.Checksum,
			TransferSize:     inputFile.Size,
			TransferDuration: transferDuration,
			TransferredAt:    time.Now(),
			Metadata: map[string]interface{}{
				"workerId": workerID,
				"taskId":   task.ID,
				"userId":   userID,
			},
		}
		if err := s.RecordDataLineage(ctx, lineage); err != nil {
			fmt.Printf("failed to record data lineage: %v\n", err)
		}

		// Publish event
		event := domain.NewDataStagedEvent(inputFile.Path, workerID, inputFile.Size)
		if err := s.events.Publish(ctx, event); err != nil {
			fmt.Printf("failed to publish data staged event: %v\n", err)
		}
	}

	return nil
}

// StageOutputFromWorker implements domain.DataMover.StageOutputFromWorker
func (s *DataMoverService) StageOutputFromWorker(ctx context.Context, task *domain.Task, workerID string, userID string) error {
	// Get worker
	worker, err := s.repo.GetWorkerByID(ctx, workerID)
	if err != nil {
		return fmt.Errorf("worker not found: %w", err)
	}
	if worker == nil {
		return domain.ErrWorkerNotFound
	}

	// Stage each output file
	for _, outputFile := range task.OutputFiles {
		// Transfer file from compute storage to central storage
		workerPath := s.generateWorkerPath(workerID, outputFile.Path)
		centralPath := s.generateCentralPath(task.ExperimentID, outputFile.Path)

		transferStart := time.Now()
		if err := s.storage.Transfer(ctx, s.storage, workerPath, centralPath); err != nil {
			return fmt.Errorf("failed to transfer output file %s: %w", outputFile.Path, err)
		}
		transferDuration := time.Since(transferStart)

		// Verify data integrity
		verified, err := s.VerifyDataIntegrity(ctx, centralPath, outputFile.Checksum)
		if err != nil {
			return fmt.Errorf("failed to verify data integrity for %s: %w", outputFile.Path, err)
		}
		if !verified {
			return fmt.Errorf("data integrity check failed for %s", outputFile.Path)
		}

		// Record data lineage
		lineage := &domain.DataLineageInfo{
			FileID:           outputFile.Path,
			SourcePath:       workerPath,
			DestinationPath:  centralPath,
			SourceChecksum:   outputFile.Checksum,
			DestChecksum:     outputFile.Checksum,
			TransferSize:     outputFile.Size,
			TransferDuration: transferDuration,
			TransferredAt:    time.Now(),
			Metadata: map[string]interface{}{
				"workerId": workerID,
				"taskId":   task.ID,
				"userId":   userID,
			},
		}
		if err := s.RecordDataLineage(ctx, lineage); err != nil {
			fmt.Printf("failed to record data lineage: %v\n", err)
		}

		// Publish event
		event := domain.NewDataStagedEvent(outputFile.Path, workerID, outputFile.Size)
		if err := s.events.Publish(ctx, event); err != nil {
			fmt.Printf("failed to publish data staged event: %v\n", err)
		}
	}

	return nil
}

// CheckCache implements domain.DataMover.CheckCache
func (s *DataMoverService) CheckCache(ctx context.Context, filePath string, checksum string, computeResourceID string) (*domain.CacheEntry, error) {
	// Get cache entry from repository
	cacheEntry, err := s.repo.GetDataCacheByPath(ctx, filePath, computeResourceID)
	if err != nil {
		return nil, err
	}
	if cacheEntry == nil {
		return nil, nil
	}

	// Verify checksum matches
	if cacheEntry.Checksum != checksum {
		// Cache entry is stale, remove it
		if err := s.repo.DeleteDataCache(ctx, cacheEntry.ID); err != nil {
			fmt.Printf("failed to delete stale cache entry: %v\n", err)
		}
		return nil, nil
	}

	// Update last accessed time
	cacheEntry.LastAccessed = time.Now()
	if err := s.repo.UpdateDataCache(ctx, cacheEntry); err != nil {
		fmt.Printf("failed to update cache entry access time: %v\n", err)
	}

	return &domain.CacheEntry{
		FilePath:          cacheEntry.FilePath,
		Checksum:          cacheEntry.Checksum,
		ComputeResourceID: cacheEntry.ComputeResourceID,
		SizeBytes:         cacheEntry.SizeBytes,
		CachedAt:          cacheEntry.CachedAt,
		LastAccessed:      cacheEntry.LastAccessed,
	}, nil
}

// RecordCacheEntry implements domain.DataMover.RecordCacheEntry
func (s *DataMoverService) RecordCacheEntry(ctx context.Context, entry *domain.CacheEntry) error {
	// Convert to repository model
	cacheRecord := &domain.DataCache{
		ID:                s.generateCacheID(entry.FilePath, entry.ComputeResourceID),
		FilePath:          entry.FilePath,
		Checksum:          entry.Checksum,
		ComputeResourceID: entry.ComputeResourceID,
		StorageResourceID: "default-storage", // Default storage resource
		LocationType:      "COMPUTE_STORAGE", // Default location type
		SizeBytes:         entry.SizeBytes,
		CachedAt:          entry.CachedAt,
		LastAccessed:      entry.LastAccessed,
	}

	// Check if entry already exists
	existing, err := s.repo.GetDataCacheByPath(ctx, entry.FilePath, entry.ComputeResourceID)
	if err == nil && existing != nil {
		// Update existing entry
		existing.SizeBytes = entry.SizeBytes
		existing.LastAccessed = entry.LastAccessed
		return s.repo.UpdateDataCache(ctx, existing)
	}

	// Create new entry
	return s.repo.CreateDataCache(ctx, cacheRecord)
}

// RecordDataLineage implements domain.DataMover.RecordDataLineage
func (s *DataMoverService) RecordDataLineage(ctx context.Context, lineage *domain.DataLineageInfo) error {
	// Convert to repository model
	lineageRecord := &domain.DataLineageRecord{
		ID:               s.generateLineageID(lineage.FileID, lineage.TransferredAt),
		FileID:           lineage.FileID,
		SourcePath:       lineage.SourcePath,
		DestinationPath:  lineage.DestinationPath,
		SourceChecksum:   lineage.SourceChecksum,
		DestChecksum:     lineage.DestChecksum,
		TransferType:     "STAGE_IN", // Default transfer type
		TransferSize:     lineage.TransferSize,
		TransferDuration: lineage.TransferDuration,
		Success:          true, // Default to success
		TransferredAt:    lineage.TransferredAt,
		Metadata:         lineage.Metadata,
	}

	// Extract task and worker IDs from metadata if available
	if lineage.Metadata != nil {
		if taskID, ok := lineage.Metadata["taskId"].(string); ok && taskID != "" {
			lineageRecord.TaskID = taskID
		}
		if workerID, ok := lineage.Metadata["workerId"].(string); ok && workerID != "" {
			lineageRecord.WorkerID = workerID
		}
	}

	return s.repo.CreateDataLineage(ctx, lineageRecord)
}

// GetDataLineage implements domain.DataMover.GetDataLineage
func (s *DataMoverService) GetDataLineage(ctx context.Context, fileID string) ([]*domain.DataLineageInfo, error) {
	// Get lineage records from repository
	records, err := s.repo.GetDataLineageByFileID(ctx, fileID)
	if err != nil {
		return nil, err
	}

	// Convert to interface model
	var lineage []*domain.DataLineageInfo
	for _, record := range records {
		lineage = append(lineage, &domain.DataLineageInfo{
			FileID:           record.FileID,
			SourcePath:       record.SourcePath,
			DestinationPath:  record.DestinationPath,
			SourceChecksum:   record.SourceChecksum,
			DestChecksum:     record.DestChecksum,
			TransferSize:     record.TransferSize,
			TransferDuration: record.TransferDuration,
			TransferredAt:    record.TransferredAt,
			Metadata:         record.Metadata,
		})
	}

	return lineage, nil
}

// VerifyDataIntegrity implements domain.DataMover.VerifyDataIntegrity
func (s *DataMoverService) VerifyDataIntegrity(ctx context.Context, filePath string, expectedChecksum string) (bool, error) {
	// Get file from storage
	reader, err := s.storage.Get(ctx, filePath)
	if err != nil {
		return false, fmt.Errorf("failed to get file: %w", err)
	}
	defer reader.Close()

	// Calculate checksum
	actualChecksum, err := s.calculateChecksum(reader)
	if err != nil {
		return false, fmt.Errorf("failed to calculate checksum: %w", err)
	}

	return actualChecksum == expectedChecksum, nil
}

// CleanupWorkerData implements domain.DataMover.CleanupWorkerData
func (s *DataMoverService) CleanupWorkerData(ctx context.Context, taskID string, workerID string) error {
	// Get task to find input/output files
	task, err := s.repo.GetTaskByID(ctx, taskID)
	if err != nil {
		return fmt.Errorf("task not found: %w", err)
	}
	if task == nil {
		return domain.ErrTaskNotFound
	}

	// Clean up input files
	for _, inputFile := range task.InputFiles {
		workerPath := s.generateWorkerPath(workerID, inputFile.Path)
		if err := s.storage.Delete(ctx, workerPath); err != nil {
			fmt.Printf("failed to delete input file %s: %v\n", workerPath, err)
		}
	}

	// Clean up output files
	for _, outputFile := range task.OutputFiles {
		workerPath := s.generateWorkerPath(workerID, outputFile.Path)
		if err := s.storage.Delete(ctx, workerPath); err != nil {
			fmt.Printf("failed to delete output file %s: %v\n", workerPath, err)
		}
	}

	// Publish event
	event := domain.NewAuditEvent(workerID, "data.cleaned", "task", taskID)
	if err := s.events.Publish(ctx, event); err != nil {
		fmt.Printf("failed to publish data cleaned event: %v\n", err)
	}

	return nil
}

// GenerateSignedURLsForTask generates signed URLs for input files
func (s *DataMoverService) GenerateSignedURLsForTask(
	ctx context.Context,
	taskID string,
	computeResourceID string,
) ([]domain.SignedURL, error) {
	task, err := s.repo.GetTaskByID(ctx, taskID)
	if err != nil {
		return nil, fmt.Errorf("task not found: %w", err)
	}
	if task == nil {
		return nil, domain.ErrTaskNotFound
	}

	var urls []domain.SignedURL
	for _, inputFile := range task.InputFiles {
		// Generate time-limited signed URL (valid 1 hour)
		url, err := s.storage.GenerateSignedURL(
			ctx,
			inputFile.Path,
			time.Hour,
			"read",
		)
		if err != nil {
			return nil, fmt.Errorf("failed to generate signed URL for %s: %w", inputFile.Path, err)
		}

		urls = append(urls, domain.SignedURL{
			SourcePath: inputFile.Path,
			URL:        url,
			LocalPath:  inputFile.Path, // Worker will save to same relative path
			ExpiresAt:  time.Now().Add(time.Hour),
			Method:     "GET",
		})
	}

	return urls, nil
}

// GenerateUploadURLsForTask generates signed URLs for output file uploads
func (s *DataMoverService) GenerateUploadURLsForTask(
	ctx context.Context,
	taskID string,
) ([]domain.SignedURL, error) {
	task, err := s.repo.GetTaskByID(ctx, taskID)
	if err != nil {
		return nil, fmt.Errorf("task not found: %w", err)
	}
	if task == nil {
		return nil, domain.ErrTaskNotFound
	}

	var urls []domain.SignedURL
	for _, outputFile := range task.OutputFiles {
		centralPath := s.generateCentralPath(task.ExperimentID, outputFile.Path)

		// Generate time-limited upload URL (valid 1 hour)
		url, err := s.storage.GenerateSignedURL(
			ctx,
			centralPath,
			time.Hour,
			"write",
		)
		if err != nil {
			return nil, fmt.Errorf("failed to generate upload URL for %s: %w", outputFile.Path, err)
		}

		urls = append(urls, domain.SignedURL{
			SourcePath: outputFile.Path,
			URL:        url,
			LocalPath:  outputFile.Path,
			ExpiresAt:  time.Now().Add(time.Hour),
			Method:     "PUT",
		})
	}

	return urls, nil
}

// Helper methods

func (s *DataMoverService) generateWorkerPath(workerID string, filePath string) string {
	return fmt.Sprintf("/workers/%s/%s", workerID, filePath)
}

func (s *DataMoverService) generateComputeResourcePath(computeResourceID string, filePath string) string {
	return fmt.Sprintf("/cache/%s/%s", computeResourceID, filePath)
}

func (s *DataMoverService) generateCentralPath(experimentID string, filePath string) string {
	return fmt.Sprintf("/experiments/%s/outputs/%s", experimentID, filePath)
}

func (s *DataMoverService) generateCacheID(filePath string, computeResourceID string) string {
	return fmt.Sprintf("cache_%s_%s_%d", filePath, computeResourceID, time.Now().UnixNano())
}

func (s *DataMoverService) generateLineageID(fileID string, timestamp time.Time) string {
	return fmt.Sprintf("lineage_%s_%d", fileID, timestamp.UnixNano())
}

func (s *DataMoverService) calculateChecksum(reader interface{}) (string, error) {
	hasher := sha256.New()

	// Handle different reader types
	switch r := reader.(type) {
	case io.Reader:
		if _, err := io.Copy(hasher, r); err != nil {
			return "", fmt.Errorf("failed to calculate checksum: %w", err)
		}
	case []byte:
		hasher.Write(r)
	case string:
		hasher.Write([]byte(r))
	default:
		return "", fmt.Errorf("unsupported reader type: %T", reader)
	}

	return hex.EncodeToString(hasher.Sum(nil)), nil
}

// ListExperimentOutputs lists all output files for an experiment
func (s *DataMoverService) ListExperimentOutputs(ctx context.Context, experimentID string) ([]domain.FileMetadata, error) {
	// Get all tasks for the experiment
	tasks, _, err := s.repo.ListTasksByExperiment(ctx, experimentID, 1000, 0)
	if err != nil {
		return nil, fmt.Errorf("failed to get tasks for experiment: %w", err)
	}

	var outputs []domain.FileMetadata
	for _, task := range tasks {
		// Get output files for each task
		taskOutputs, err := s.getTaskOutputs(ctx, task.ID)
		if err != nil {
			continue // Skip tasks with errors
		}

		// Add task ID to outputs (FileMetadata doesn't have Metadata field)
		for _, output := range taskOutputs {
			// Create a new FileMetadata with task ID in the path
			outputWithTaskID := domain.FileMetadata{
				Path:     fmt.Sprintf("%s/%s", task.ID, output.Path),
				Size:     output.Size,
				Checksum: output.Checksum,
				Type:     output.Type,
			}
			outputs = append(outputs, outputWithTaskID)
		}
	}

	return outputs, nil
}

// GetExperimentOutputArchive creates an archive of all experiment outputs
func (s *DataMoverService) GetExperimentOutputArchive(ctx context.Context, experimentID string) (io.Reader, error) {
	// Get all output files
	outputs, err := s.ListExperimentOutputs(ctx, experimentID)
	if err != nil {
		return nil, fmt.Errorf("failed to list experiment outputs: %w", err)
	}

	// Create archive
	archive, err := s.createArchive(outputs)
	if err != nil {
		return nil, fmt.Errorf("failed to create archive: %w", err)
	}

	return archive, nil
}

// GetFile retrieves a file from storage
func (s *DataMoverService) GetFile(ctx context.Context, filePath string) (io.Reader, error) {
	// This would need to be implemented based on the storage adapter
	// For now, return a placeholder
	return strings.NewReader("file content for " + filePath), nil
}

// getTaskOutputs gets output files for a specific task
func (s *DataMoverService) getTaskOutputs(ctx context.Context, taskID string) ([]domain.FileMetadata, error) {
	// This would need to be implemented to read from the actual storage
	// For now, return empty slice
	return []domain.FileMetadata{}, nil
}

// createArchive creates an archive from a list of files
func (s *DataMoverService) createArchive(files []domain.FileMetadata) (io.Reader, error) {
	// This would need to be implemented to create a tar.gz archive
	// For now, return a placeholder
	return strings.NewReader("archive content"), nil
}
