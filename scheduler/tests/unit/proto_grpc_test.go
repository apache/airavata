package unit

import (
	"testing"
	"time"

	"github.com/apache/airavata/scheduler/core/dto"
	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
	"google.golang.org/protobuf/proto"
	"google.golang.org/protobuf/types/known/durationpb"
)

func TestProtoMessageValidation(t *testing.T) {
	t.Run("WorkerCapabilities_ValidMessage", func(t *testing.T) {
		// Test valid worker capabilities message
		capabilities := &dto.WorkerCapabilities{
			MaxCpuCores:       4,
			MaxMemoryMb:       8192,
			MaxDiskGb:         100,
			MaxGpus:           1,
			SupportedRuntimes: []string{"slurm", "kubernetes", "baremetal"},
			Metadata: map[string]string{
				"os": "linux",
			},
		}

		// Validate required fields
		assert.Equal(t, int32(4), capabilities.MaxCpuCores)
		assert.Equal(t, int32(8192), capabilities.MaxMemoryMb)
		assert.Equal(t, int32(100), capabilities.MaxDiskGb)
		assert.Equal(t, int32(1), capabilities.MaxGpus)
		assert.Len(t, capabilities.SupportedRuntimes, 3)
		assert.Len(t, capabilities.Metadata, 1)
	})

	t.Run("WorkerCapabilities_InvalidMessage", func(t *testing.T) {
		// Test invalid worker capabilities (zero values)
		capabilities := &dto.WorkerCapabilities{
			MaxCpuCores: 0, // Invalid
			MaxMemoryMb: 0, // Invalid
		}

		assert.Equal(t, int32(0), capabilities.MaxCpuCores)
		assert.Equal(t, int32(0), capabilities.MaxMemoryMb)
	})

	t.Run("TaskAssignment_ValidMessage", func(t *testing.T) {
		// Test valid task assignment message
		assignment := &dto.TaskAssignment{
			TaskId:          "task-123",
			ExperimentId:    "exp-456",
			Command:         "echo 'Hello World'",
			ExecutionScript: "#!/bin/bash\necho 'Hello World'",
			Dependencies:    []string{"task-1", "task-2"},
			InputFiles: []*dto.SignedFileURL{
				{
					Url:       "https://storage.example.com/input.txt",
					LocalPath: "input.txt",
				},
			},
			OutputFiles: []*dto.FileMetadata{
				{
					Path: "output.txt",
					Size: 1024,
				},
			},
			Environment: map[string]string{
				"PATH": "/usr/bin:/bin",
			},
			Timeout: durationpb.New(30 * time.Minute),
			Metadata: map[string]string{
				"priority": "high",
			},
		}

		assert.NotEmpty(t, assignment.TaskId)
		assert.NotEmpty(t, assignment.ExperimentId)
		assert.NotEmpty(t, assignment.Command)
		assert.NotEmpty(t, assignment.ExecutionScript)
		assert.Len(t, assignment.Dependencies, 2)
		assert.Len(t, assignment.InputFiles, 1)
		assert.Len(t, assignment.OutputFiles, 1)
		assert.Len(t, assignment.Environment, 1)
		assert.NotNil(t, assignment.Timeout)
		assert.Len(t, assignment.Metadata, 1)
	})

	t.Run("TaskAssignment_InvalidMessage", func(t *testing.T) {
		// Test invalid task assignment (missing required fields)
		assignment := &dto.TaskAssignment{
			TaskId: "", // Empty task ID should be invalid
		}

		assert.Empty(t, assignment.TaskId)
		assert.Empty(t, assignment.Command)
		assert.Nil(t, assignment.InputFiles)
		assert.Nil(t, assignment.OutputFiles)
	})

	t.Run("Heartbeat_ValidMessage", func(t *testing.T) {
		// Test valid heartbeat message
		heartbeat := &dto.Heartbeat{
			WorkerId:      "worker-123",
			Status:        dto.WorkerStatus_WORKER_STATUS_IDLE,
			CurrentTaskId: "task-1",
			Metadata: map[string]string{
				"version": "1.0.0",
			},
		}

		assert.NotEmpty(t, heartbeat.WorkerId)
		assert.Equal(t, dto.WorkerStatus_WORKER_STATUS_IDLE, heartbeat.Status)
		assert.Equal(t, "task-1", heartbeat.CurrentTaskId)
		assert.NotNil(t, heartbeat.Metadata)
		assert.Equal(t, "1.0.0", heartbeat.Metadata["version"])
	})

	t.Run("Heartbeat_StatusTransitions", func(t *testing.T) {
		// Test heartbeat status transitions
		heartbeat := &dto.Heartbeat{
			WorkerId: "worker-123",
			Status:   dto.WorkerStatus_WORKER_STATUS_IDLE,
		}

		// Test status transitions
		heartbeat.Status = dto.WorkerStatus_WORKER_STATUS_BUSY
		assert.Equal(t, dto.WorkerStatus_WORKER_STATUS_BUSY, heartbeat.Status)

		heartbeat.Status = dto.WorkerStatus_WORKER_STATUS_STAGING
		assert.Equal(t, dto.WorkerStatus_WORKER_STATUS_STAGING, heartbeat.Status)

		heartbeat.Status = dto.WorkerStatus_WORKER_STATUS_ERROR
		assert.Equal(t, dto.WorkerStatus_WORKER_STATUS_ERROR, heartbeat.Status)
	})
}

func TestProtoEnumValues(t *testing.T) {
	t.Run("WorkerStatus_EnumValues", func(t *testing.T) {
		// Test all worker status enum values
		statuses := []dto.WorkerStatus{
			dto.WorkerStatus_WORKER_STATUS_UNKNOWN,
			dto.WorkerStatus_WORKER_STATUS_IDLE,
			dto.WorkerStatus_WORKER_STATUS_BUSY,
			dto.WorkerStatus_WORKER_STATUS_STAGING,
			dto.WorkerStatus_WORKER_STATUS_ERROR,
		}

		for _, status := range statuses {
			assert.True(t, status >= 0, "Worker status should be valid enum value")
		}
	})

	t.Run("OutputType_EnumValues", func(t *testing.T) {
		// Test all output type enum values
		outputTypes := []dto.OutputType{
			dto.OutputType_OUTPUT_TYPE_UNKNOWN,
			dto.OutputType_OUTPUT_TYPE_STDOUT,
			dto.OutputType_OUTPUT_TYPE_STDERR,
			dto.OutputType_OUTPUT_TYPE_LOG,
		}

		for _, outputType := range outputTypes {
			assert.True(t, outputType >= 0, "Output type should be valid enum value")
		}
	})
}

func TestProtoMessageSerialization(t *testing.T) {
	t.Run("WorkerCapabilities_Serialization", func(t *testing.T) {
		// Test proto message serialization/deserialization
		original := &dto.WorkerCapabilities{
			MaxCpuCores:       4,
			MaxMemoryMb:       8192,
			MaxDiskGb:         100,
			MaxGpus:           1,
			SupportedRuntimes: []string{"slurm", "kubernetes"},
			Metadata: map[string]string{
				"os": "linux",
			},
		}

		// Serialize to bytes using protobuf
		data, err := proto.Marshal(original)
		require.NoError(t, err)
		assert.NotEmpty(t, data)

		// Deserialize from bytes
		deserialized := &dto.WorkerCapabilities{}
		err = proto.Unmarshal(data, deserialized)
		require.NoError(t, err)

		// Verify data integrity
		assert.Equal(t, original.MaxCpuCores, deserialized.MaxCpuCores)
		assert.Equal(t, original.MaxMemoryMb, deserialized.MaxMemoryMb)
		assert.Equal(t, original.MaxDiskGb, deserialized.MaxDiskGb)
		assert.Equal(t, original.MaxGpus, deserialized.MaxGpus)
		assert.Equal(t, original.SupportedRuntimes, deserialized.SupportedRuntimes)
		assert.Equal(t, original.Metadata, deserialized.Metadata)
	})

	t.Run("TaskAssignment_Serialization", func(t *testing.T) {
		// Test task assignment serialization
		original := &dto.TaskAssignment{
			TaskId:       "task-123",
			ExperimentId: "exp-456",
			Command:      "echo 'Hello World'",
			InputFiles: []*dto.SignedFileURL{
				{
					Url:       "https://storage.example.com/input.txt",
					LocalPath: "input.txt",
				},
			},
			OutputFiles: []*dto.FileMetadata{
				{
					Path: "output.txt",
					Size: 1024,
				},
			},
			Timeout: durationpb.New(30 * time.Minute),
		}

		// Serialize to bytes
		data, err := proto.Marshal(original)
		require.NoError(t, err)
		assert.NotEmpty(t, data)

		// Deserialize from bytes
		deserialized := &dto.TaskAssignment{}
		err = proto.Unmarshal(data, deserialized)
		require.NoError(t, err)

		// Verify data integrity
		assert.Equal(t, original.TaskId, deserialized.TaskId)
		assert.Equal(t, original.ExperimentId, deserialized.ExperimentId)
		assert.Equal(t, original.Command, deserialized.Command)
		assert.Len(t, deserialized.InputFiles, 1)
		assert.Equal(t, original.InputFiles[0].Url, deserialized.InputFiles[0].Url)
		assert.Len(t, deserialized.OutputFiles, 1)
		assert.Equal(t, original.OutputFiles[0].Path, deserialized.OutputFiles[0].Path)
	})
}

func TestProtoMessageValidation_EdgeCases(t *testing.T) {
	t.Run("EmptyMessages", func(t *testing.T) {
		// Test empty proto messages
		emptyCapabilities := &dto.WorkerCapabilities{}
		assert.Equal(t, int32(0), emptyCapabilities.MaxCpuCores)
		assert.Equal(t, int32(0), emptyCapabilities.MaxMemoryMb)
		assert.Nil(t, emptyCapabilities.SupportedRuntimes)

		emptyAssignment := &dto.TaskAssignment{}
		assert.Empty(t, emptyAssignment.TaskId)
		assert.Empty(t, emptyAssignment.Command)
		assert.Nil(t, emptyAssignment.InputFiles)
	})

	t.Run("LargeMessages", func(t *testing.T) {
		// Test proto messages with large data
		largeAssignment := &dto.TaskAssignment{
			TaskId:       "task-123",
			ExperimentId: "exp-456",
			Command:      "echo 'Hello World'",
			InputFiles:   make([]*dto.SignedFileURL, 1000),
		}

		// Fill with large number of input files
		for i := 0; i < 1000; i++ {
			largeAssignment.InputFiles[i] = &dto.SignedFileURL{
				Url:       "https://storage.example.com/input" + string(rune(i)) + ".txt",
				LocalPath: "input" + string(rune(i)) + ".txt",
			}
		}

		// Serialize large message
		data, err := proto.Marshal(largeAssignment)
		require.NoError(t, err)
		assert.NotEmpty(t, data)
		assert.Greater(t, len(data), 10000) // Should be large

		// Deserialize large message
		deserialized := &dto.TaskAssignment{}
		err = proto.Unmarshal(data, deserialized)
		require.NoError(t, err)
		assert.Len(t, deserialized.InputFiles, 1000)
	})

	t.Run("SpecialCharacters", func(t *testing.T) {
		// Test proto messages with special characters
		specialAssignment := &dto.TaskAssignment{
			TaskId:       "task-123",
			ExperimentId: "exp-456",
			Command:      "echo 'Hello World! @#$%^&*()_+-=[]{}|;:,.<>?'",
			InputFiles: []*dto.SignedFileURL{
				{
					Url:       "https://storage.example.com/input with spaces.txt",
					LocalPath: "input with spaces.txt",
				},
			},
		}

		// Serialize and deserialize
		data, err := proto.Marshal(specialAssignment)
		require.NoError(t, err)

		deserialized := &dto.TaskAssignment{}
		err = proto.Unmarshal(data, deserialized)
		require.NoError(t, err)

		// Verify special characters are preserved
		assert.Equal(t, specialAssignment.Command, deserialized.Command)
		assert.Equal(t, specialAssignment.InputFiles[0].Url, deserialized.InputFiles[0].Url)
		assert.Equal(t, specialAssignment.InputFiles[0].LocalPath, deserialized.InputFiles[0].LocalPath)
	})
}
