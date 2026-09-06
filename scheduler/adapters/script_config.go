package adapters

import (
	"fmt"
	"time"
)

// ScriptConfig contains configuration for script generation
type ScriptConfig struct {
	WorkerBinaryURL   string // URL to download worker binary (e.g., http://server/api/worker)
	WorkerBinaryPath  string // Local path to worker binary for direct transfer
	ServerGRPCAddress string
	ServerGRPCPort    int
	DefaultWorkingDir string
}

// Helper functions for script generation

// formatWalltime formats a duration as SLURM time limit (HH:MM:SS)
func formatWalltime(duration time.Duration) string {
	hours := int(duration.Hours())
	minutes := int(duration.Minutes()) % 60
	seconds := int(duration.Seconds()) % 60
	return fmt.Sprintf("%02d:%02d:%02d", hours, minutes, seconds)
}

// getIntFromCapabilities extracts an integer value from capabilities map
func getIntFromCapabilities(capabilities map[string]interface{}, key string, defaultValue int) int {
	if capabilities == nil {
		return defaultValue
	}
	if value, ok := capabilities[key]; ok {
		switch v := value.(type) {
		case int:
			return v
		case float64:
			return int(v)
		case string:
			// Try to parse string as int
			var parsed int
			if _, err := fmt.Sscanf(v, "%d", &parsed); err == nil {
				return parsed
			}
		}
	}
	return defaultValue
}
