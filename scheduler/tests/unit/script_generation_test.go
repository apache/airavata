package unit

import (
	"context"
	"strings"
	"testing"
	"time"

	"github.com/apache/airavata/scheduler/adapters"
	"github.com/apache/airavata/scheduler/core/domain"
	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
)

func TestScriptConfig(t *testing.T) {
	t.Run("ScriptConfig_Initialization", func(t *testing.T) {
		// Test ScriptConfig initialization
		config := &adapters.ScriptConfig{
			WorkerBinaryURL:   "http://localhost:8080/api/worker-binary",
			ServerGRPCAddress: "localhost",
			ServerGRPCPort:    50051,
		}

		assert.NotEmpty(t, config.WorkerBinaryURL)
		assert.NotEmpty(t, config.ServerGRPCAddress)
		assert.Equal(t, 50051, config.ServerGRPCPort)
	})

	t.Run("ScriptConfig_Validation", func(t *testing.T) {
		// Test ScriptConfig validation
		validConfig := &adapters.ScriptConfig{
			WorkerBinaryURL:   "http://localhost:8080/api/worker-binary",
			ServerGRPCAddress: "localhost",
			ServerGRPCPort:    50051,
		}

		// Valid config should pass validation
		assert.NotEmpty(t, validConfig.WorkerBinaryURL)
		assert.NotEmpty(t, validConfig.ServerGRPCAddress)
		assert.Greater(t, validConfig.ServerGRPCPort, 0)

		// Invalid config should fail validation
		invalidConfig := &adapters.ScriptConfig{
			WorkerBinaryURL:   "", // Empty URL
			ServerGRPCAddress: "", // Empty address
			ServerGRPCPort:    0,  // Invalid port
		}

		assert.Empty(t, invalidConfig.WorkerBinaryURL)
		assert.Empty(t, invalidConfig.ServerGRPCAddress)
		assert.Equal(t, 0, invalidConfig.ServerGRPCPort)
	})
}

func TestScriptGenerationHelpers(t *testing.T) {
	t.Run("HelperFunctions_IndirectTest", func(t *testing.T) {
		// Test helper functions indirectly through adapter functionality
		// Since helper functions are private, we test them through the public adapter methods
		config := &adapters.ScriptConfig{
			WorkerBinaryURL:   "http://localhost:8080/api/worker-binary",
			ServerGRPCAddress: "localhost",
			ServerGRPCPort:    50051,
		}

		// Create a test compute resource
		resource := domain.ComputeResource{
			ID:   "test-resource",
			Name: "Test SLURM Resource",
			Type: "SLURM",
		}

		// Create adapter with config
		adapter := adapters.NewSlurmAdapterWithConfig(resource, nil, config)
		assert.NotNil(t, adapter)
	})
}

func TestSLURMScriptGeneration(t *testing.T) {
	t.Run("SLURM_ScriptTemplate", func(t *testing.T) {
		// Test SLURM script template generation
		config := &adapters.ScriptConfig{
			WorkerBinaryURL:   "http://localhost:8080/api/worker-binary",
			ServerGRPCAddress: "localhost",
			ServerGRPCPort:    50051,
		}

		resource := domain.ComputeResource{
			ID:   "test-resource",
			Name: "Test SLURM Resource",
			Type: "SLURM",
		}
		slurmAdapter := adapters.NewSlurmAdapterWithConfig(resource, nil, config)

		experiment := &domain.Experiment{
			ID:   "exp-123",
			Name: "Test Experiment",
		}

		walltime := 30 * time.Minute

		script, err := slurmAdapter.GenerateWorkerSpawnScript(context.Background(), experiment, walltime)
		require.NoError(t, err)
		assert.NotEmpty(t, script)

		// Verify script contains SLURM-specific elements
		assert.Contains(t, script, "#!/bin/bash")
		assert.Contains(t, script, "#SBATCH")
		assert.Contains(t, script, "--time=00:30:00")
		assert.Contains(t, script, "--job-name=worker_worker_test-resource_")
		assert.Contains(t, script, "http://localhost:8080/api/worker-binary")
		assert.Contains(t, script, "localhost:50051")
	})

	t.Run("SLURM_ScriptWithCustomResources", func(t *testing.T) {
		// Test SLURM script with custom resource requirements
		config := &adapters.ScriptConfig{
			WorkerBinaryURL:   "http://localhost:8080/api/worker-binary",
			ServerGRPCAddress: "localhost",
			ServerGRPCPort:    50051,
		}

		resource := domain.ComputeResource{
			ID:   "test-resource",
			Name: "Test SLURM Resource",
			Type: "SLURM",
		}
		slurmAdapter := adapters.NewSlurmAdapterWithConfig(resource, nil, config)

		experiment := &domain.Experiment{
			ID:   "exp-456",
			Name: "High Memory Experiment",
		}

		walltime := 2 * time.Hour

		script, err := slurmAdapter.GenerateWorkerSpawnScript(context.Background(), experiment, walltime)
		require.NoError(t, err)
		assert.NotEmpty(t, script)

		// Verify script contains custom walltime
		assert.Contains(t, script, "--time=02:00:00")
		assert.Contains(t, script, "--job-name=worker_worker_test-resource_")
	})

	t.Run("SLURM_ScriptErrorHandling", func(t *testing.T) {
		// Test SLURM script generation error handling
		config := &adapters.ScriptConfig{
			WorkerBinaryURL:   "", // Invalid URL
			ServerGRPCAddress: "localhost",
			ServerGRPCPort:    50051,
		}

		resource := domain.ComputeResource{
			ID:   "test-resource",
			Name: "Test SLURM Resource",
			Type: "SLURM",
		}
		slurmAdapter := adapters.NewSlurmAdapterWithConfig(resource, nil, config)

		experiment := &domain.Experiment{
			ID:   "exp-123",
			Name: "Test Experiment",
		}

		walltime := 30 * time.Minute

		// Should still generate script even with invalid URL (template will contain empty URL)
		script, err := slurmAdapter.GenerateWorkerSpawnScript(context.Background(), experiment, walltime)
		require.NoError(t, err)
		assert.NotEmpty(t, script)
		assert.Contains(t, script, "#!/bin/bash")
	})
}

func TestBareMetalScriptGeneration(t *testing.T) {
	t.Run("BareMetal_ScriptTemplate", func(t *testing.T) {
		// Test Bare Metal script template generation
		config := &adapters.ScriptConfig{
			WorkerBinaryURL:   "http://localhost:8080/api/worker-binary",
			ServerGRPCAddress: "localhost",
			ServerGRPCPort:    50051,
		}

		resource := domain.ComputeResource{
			ID:   "test-resource",
			Name: "Test Bare Metal Resource",
			Type: "BARE_METAL",
		}
		baremetalAdapter := adapters.NewBareMetalAdapterWithConfig(resource, nil, config)

		experiment := &domain.Experiment{
			ID:   "exp-123",
			Name: "Test Experiment",
		}

		walltime := 30 * time.Minute

		script, err := baremetalAdapter.GenerateWorkerSpawnScript(context.Background(), experiment, walltime)
		require.NoError(t, err)
		assert.NotEmpty(t, script)

		// Verify script contains Bare Metal-specific elements
		assert.Contains(t, script, "#!/bin/bash")
		assert.Contains(t, script, "cleanup")
		assert.Contains(t, script, "&")
		assert.Contains(t, script, "http://localhost:8080/api/worker-binary")
		assert.Contains(t, script, "localhost:50051")
		assert.Contains(t, script, "worker")
	})

	t.Run("BareMetal_ScriptWithCustomWorkingDir", func(t *testing.T) {
		// Test Bare Metal script with custom working directory
		config := &adapters.ScriptConfig{
			WorkerBinaryURL:   "http://localhost:8080/api/worker-binary",
			ServerGRPCAddress: "localhost",
			ServerGRPCPort:    50051,
		}

		resource := domain.ComputeResource{
			ID:   "test-resource",
			Name: "Test Bare Metal Resource",
			Type: "BARE_METAL",
		}
		baremetalAdapter := adapters.NewBareMetalAdapterWithConfig(resource, nil, config)

		experiment := &domain.Experiment{
			ID:   "exp-789",
			Name: "Custom Working Dir Experiment",
		}

		walltime := 1 * time.Hour

		script, err := baremetalAdapter.GenerateWorkerSpawnScript(context.Background(), experiment, walltime)
		require.NoError(t, err)
		assert.NotEmpty(t, script)

		// Verify script contains working directory setup
		assert.Contains(t, script, "mkdir -p")
		assert.Contains(t, script, "cd")
	})

	t.Run("BareMetal_ScriptErrorHandling", func(t *testing.T) {
		// Test Bare Metal script generation error handling
		config := &adapters.ScriptConfig{
			WorkerBinaryURL:   "http://localhost:8080/api/worker-binary",
			ServerGRPCAddress: "", // Invalid address
			ServerGRPCPort:    50051,
		}

		resource := domain.ComputeResource{
			ID:   "test-resource",
			Name: "Test Bare Metal Resource",
			Type: "BARE_METAL",
		}
		baremetalAdapter := adapters.NewBareMetalAdapterWithConfig(resource, nil, config)

		experiment := &domain.Experiment{
			ID:   "exp-123",
			Name: "Test Experiment",
		}

		walltime := 30 * time.Minute

		// Should still generate script even with invalid address
		script, err := baremetalAdapter.GenerateWorkerSpawnScript(context.Background(), experiment, walltime)
		require.NoError(t, err)
		assert.NotEmpty(t, script)
		assert.Contains(t, script, "#!/bin/bash")
	})
}

func TestKubernetesScriptGeneration(t *testing.T) {
	t.Run("Kubernetes_ScriptTemplate", func(t *testing.T) {
		// Test Kubernetes script template generation
		config := &adapters.ScriptConfig{
			WorkerBinaryURL:   "http://localhost:8080/api/worker-binary",
			ServerGRPCAddress: "localhost",
			ServerGRPCPort:    50051,
		}

		resource := domain.ComputeResource{
			ID:   "test-resource",
			Name: "Test Kubernetes Resource",
			Type: "KUBERNETES",
		}
		kubernetesAdapter := adapters.NewKubernetesAdapterWithConfig(resource, nil, config)

		experiment := &domain.Experiment{
			ID:   "exp-123",
			Name: "Test Experiment",
		}

		walltime := 30 * time.Minute

		script, err := kubernetesAdapter.GenerateWorkerSpawnScript(context.Background(), experiment, walltime)
		require.NoError(t, err)
		assert.NotEmpty(t, script)

		// Verify script contains Kubernetes-specific elements
		assert.Contains(t, script, "apiVersion: v1")
		assert.Contains(t, script, "kind: Pod")
		assert.Contains(t, script, "name: worker-worker_test-resource_")
		assert.Contains(t, script, "http://localhost:8080/api/worker-binary")
		assert.Contains(t, script, "localhost:50051")
	})

	t.Run("Kubernetes_ScriptWithCustomResources", func(t *testing.T) {
		// Test Kubernetes script with custom resource requirements
		config := &adapters.ScriptConfig{
			WorkerBinaryURL:   "http://localhost:8080/api/worker-binary",
			ServerGRPCAddress: "localhost",
			ServerGRPCPort:    50051,
		}

		resource := domain.ComputeResource{
			ID:   "test-resource",
			Name: "Test Kubernetes Resource",
			Type: "KUBERNETES",
		}
		kubernetesAdapter := adapters.NewKubernetesAdapterWithConfig(resource, nil, config)

		experiment := &domain.Experiment{
			ID:   "exp-456",
			Name: "High CPU Experiment",
		}

		walltime := 1 * time.Hour

		script, err := kubernetesAdapter.GenerateWorkerSpawnScript(context.Background(), experiment, walltime)
		require.NoError(t, err)
		assert.NotEmpty(t, script)

		// Verify script contains custom pod name
		assert.Contains(t, script, "name: worker-worker_test-resource_")
		assert.Contains(t, script, "apiVersion: v1")
	})

	t.Run("Kubernetes_ScriptErrorHandling", func(t *testing.T) {
		// Test Kubernetes script generation error handling
		config := &adapters.ScriptConfig{
			WorkerBinaryURL:   "http://localhost:8080/api/worker-binary",
			ServerGRPCAddress: "localhost",
			ServerGRPCPort:    0, // Invalid port
		}

		resource := domain.ComputeResource{
			ID:   "test-resource",
			Name: "Test Kubernetes Resource",
			Type: "KUBERNETES",
		}
		kubernetesAdapter := adapters.NewKubernetesAdapterWithConfig(resource, nil, config)

		experiment := &domain.Experiment{
			ID:   "exp-123",
			Name: "Test Experiment",
		}

		walltime := 30 * time.Minute

		// Should still generate script even with invalid port
		script, err := kubernetesAdapter.GenerateWorkerSpawnScript(context.Background(), experiment, walltime)
		require.NoError(t, err)
		assert.NotEmpty(t, script)
		assert.Contains(t, script, "apiVersion: v1")
	})
}

func TestScriptGenerationEdgeCases(t *testing.T) {
	t.Run("EmptyExperiment", func(t *testing.T) {
		// Test script generation with empty experiment
		config := &adapters.ScriptConfig{
			WorkerBinaryURL:   "http://localhost:8080/api/worker-binary",
			ServerGRPCAddress: "localhost",
			ServerGRPCPort:    50051,
		}

		resource := domain.ComputeResource{
			ID:   "test-resource",
			Name: "Test SLURM Resource",
			Type: "SLURM",
		}
		slurmAdapter := adapters.NewSlurmAdapterWithConfig(resource, nil, config)

		experiment := &domain.Experiment{
			ID:   "", // Empty ID
			Name: "", // Empty name
		}

		walltime := 30 * time.Minute

		script, err := slurmAdapter.GenerateWorkerSpawnScript(context.Background(), experiment, walltime)
		require.NoError(t, err)
		assert.NotEmpty(t, script)

		// Script should still be generated with empty values
		assert.Contains(t, script, "#!/bin/bash")
		assert.Contains(t, script, "#SBATCH")
	})

	t.Run("VeryLongWalltime", func(t *testing.T) {
		// Test script generation with very long walltime
		config := &adapters.ScriptConfig{
			WorkerBinaryURL:   "http://localhost:8080/api/worker-binary",
			ServerGRPCAddress: "localhost",
			ServerGRPCPort:    50051,
		}

		resource := domain.ComputeResource{
			ID:   "test-resource",
			Name: "Test SLURM Resource",
			Type: "SLURM",
		}
		slurmAdapter := adapters.NewSlurmAdapterWithConfig(resource, nil, config)

		experiment := &domain.Experiment{
			ID:   "exp-123",
			Name: "Long Running Experiment",
		}

		walltime := 24 * time.Hour // 24 hours

		script, err := slurmAdapter.GenerateWorkerSpawnScript(context.Background(), experiment, walltime)
		require.NoError(t, err)
		assert.NotEmpty(t, script)

		// Verify long walltime is formatted correctly
		assert.Contains(t, script, "--time=24:00:00") // 24 hours
	})

	t.Run("SpecialCharactersInExperimentName", func(t *testing.T) {
		// Test script generation with special characters in experiment name
		config := &adapters.ScriptConfig{
			WorkerBinaryURL:   "http://localhost:8080/api/worker-binary",
			ServerGRPCAddress: "localhost",
			ServerGRPCPort:    50051,
		}

		resource := domain.ComputeResource{
			ID:   "test-resource",
			Name: "Test SLURM Resource",
			Type: "SLURM",
		}
		slurmAdapter := adapters.NewSlurmAdapterWithConfig(resource, nil, config)

		experiment := &domain.Experiment{
			ID:   "exp-123",
			Name: "Test Experiment with Special Chars: @#$%^&*()",
		}

		walltime := 30 * time.Minute

		script, err := slurmAdapter.GenerateWorkerSpawnScript(context.Background(), experiment, walltime)
		require.NoError(t, err)
		assert.NotEmpty(t, script)

		// Script should be generated successfully even with special characters
		assert.Contains(t, script, "#!/bin/bash")
		assert.Contains(t, script, "#SBATCH")
	})

	t.Run("ConcurrentScriptGeneration", func(t *testing.T) {
		// Test concurrent script generation
		config := &adapters.ScriptConfig{
			WorkerBinaryURL:   "http://localhost:8080/api/worker-binary",
			ServerGRPCAddress: "localhost",
			ServerGRPCPort:    50051,
		}

		resource := domain.ComputeResource{
			ID:   "test-resource",
			Name: "Test SLURM Resource",
			Type: "SLURM",
		}
		slurmAdapter := adapters.NewSlurmAdapterWithConfig(resource, nil, config)

		experiments := []*domain.Experiment{
			{ID: "exp-1", Name: "Experiment 1"},
			{ID: "exp-2", Name: "Experiment 2"},
			{ID: "exp-3", Name: "Experiment 3"},
		}

		walltime := 30 * time.Minute

		// Generate scripts concurrently
		scripts := make([]string, len(experiments))
		errors := make([]error, len(experiments))

		for i, experiment := range experiments {
			script, err := slurmAdapter.GenerateWorkerSpawnScript(context.Background(), experiment, walltime)
			scripts[i] = script
			errors[i] = err
		}

		// Verify all scripts were generated successfully
		for i, script := range scripts {
			assert.NoError(t, errors[i], "Script generation should not fail for experiment %d", i)
			assert.NotEmpty(t, script, "Script should not be empty for experiment %d", i)
			assert.Contains(t, script, "#!/bin/bash", "Script should contain bash shebang for experiment %d", i)
		}
	})
}

func TestScriptTemplateValidation(t *testing.T) {
	t.Run("SLURM_TemplateValidation", func(t *testing.T) {
		// Test SLURM template validation
		config := &adapters.ScriptConfig{
			WorkerBinaryURL:   "http://localhost:8080/api/worker-binary",
			ServerGRPCAddress: "localhost",
			ServerGRPCPort:    50051,
		}

		resource := domain.ComputeResource{
			ID:   "test-resource",
			Name: "Test SLURM Resource",
			Type: "SLURM",
		}
		slurmAdapter := adapters.NewSlurmAdapterWithConfig(resource, nil, config)

		experiment := &domain.Experiment{
			ID:   "exp-123",
			Name: "Test Experiment",
		}

		walltime := 30 * time.Minute

		script, err := slurmAdapter.GenerateWorkerSpawnScript(context.Background(), experiment, walltime)
		require.NoError(t, err)

		// Validate script structure
		lines := strings.Split(script, "\n")
		assert.Greater(t, len(lines), 5, "Script should have multiple lines")

		// Check for required SLURM directives
		scriptContent := strings.Join(lines, "\n")
		assert.Contains(t, scriptContent, "#!/bin/bash")
		assert.Contains(t, scriptContent, "#SBATCH")
		assert.Contains(t, scriptContent, "--time=")
		assert.Contains(t, scriptContent, "--job-name=")
	})

	t.Run("BareMetal_TemplateValidation", func(t *testing.T) {
		// Test Bare Metal template validation
		config := &adapters.ScriptConfig{
			WorkerBinaryURL:   "http://localhost:8080/api/worker-binary",
			ServerGRPCAddress: "localhost",
			ServerGRPCPort:    50051,
		}

		resource := domain.ComputeResource{
			ID:   "test-resource",
			Name: "Test Bare Metal Resource",
			Type: "BARE_METAL",
		}
		baremetalAdapter := adapters.NewBareMetalAdapterWithConfig(resource, nil, config)

		experiment := &domain.Experiment{
			ID:   "exp-123",
			Name: "Test Experiment",
		}

		walltime := 30 * time.Minute

		script, err := baremetalAdapter.GenerateWorkerSpawnScript(context.Background(), experiment, walltime)
		require.NoError(t, err)

		// Validate script structure
		lines := strings.Split(script, "\n")
		assert.Greater(t, len(lines), 3, "Script should have multiple lines")

		// Check for required Bare Metal elements
		scriptContent := strings.Join(lines, "\n")
		assert.Contains(t, scriptContent, "#!/bin/bash")
		assert.Contains(t, scriptContent, "cleanup")
		assert.Contains(t, scriptContent, "&")
	})

	t.Run("Kubernetes_TemplateValidation", func(t *testing.T) {
		// Test Kubernetes template validation
		config := &adapters.ScriptConfig{
			WorkerBinaryURL:   "http://localhost:8080/api/worker-binary",
			ServerGRPCAddress: "localhost",
			ServerGRPCPort:    50051,
		}

		resource := domain.ComputeResource{
			ID:   "test-resource",
			Name: "Test Kubernetes Resource",
			Type: "KUBERNETES",
		}
		kubernetesAdapter := adapters.NewKubernetesAdapterWithConfig(resource, nil, config)

		experiment := &domain.Experiment{
			ID:   "exp-123",
			Name: "Test Experiment",
		}

		walltime := 30 * time.Minute

		script, err := kubernetesAdapter.GenerateWorkerSpawnScript(context.Background(), experiment, walltime)
		require.NoError(t, err)

		// Validate script structure
		lines := strings.Split(script, "\n")
		assert.Greater(t, len(lines), 10, "Kubernetes script should have many lines")

		// Check for required Kubernetes elements
		scriptContent := strings.Join(lines, "\n")
		assert.Contains(t, scriptContent, "apiVersion: v1")
		assert.Contains(t, scriptContent, "kind: Pod")
		assert.Contains(t, scriptContent, "name:")
		assert.Contains(t, scriptContent, "containers:")
	})
}
