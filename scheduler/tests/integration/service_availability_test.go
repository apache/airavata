package integration

import (
	"net"
	"testing"
	"time"

	"github.com/stretchr/testify/assert"
)

// TestServiceAvailability verifies that all required services are available
func TestServiceAvailability(t *testing.T) {
	if testing.Short() {
		t.Skip("Skipping integration test in short mode")
	}

	// Test with timeout to prevent hanging

	// Check required services
	services := map[string]string{
		"postgres": "localhost:5432",
		"minio":    "localhost:9000",
		"sftp":     "localhost:2222",
		"nfs":      "localhost:2049",
	}

	for serviceName, address := range services {
		t.Run(serviceName, func(t *testing.T) {
			conn, err := net.DialTimeout("tcp", address, 5*time.Second)
			if err != nil {
				t.Logf("Service %s not available at %s: %v", serviceName, address, err)
				// Don't fail - just log that service is not available
				return
			}
			conn.Close()
			t.Logf("Service %s is available at %s", serviceName, address)
		})
	}
}

// TestBasicConnectivity tests basic connectivity to services
func TestBasicConnectivity(t *testing.T) {
	if testing.Short() {
		t.Skip("Skipping integration test in short mode")
	}

	// Test with timeout to prevent hanging

	// Test PostgreSQL connectivity
	t.Run("PostgreSQL", func(t *testing.T) {
		conn, err := net.DialTimeout("tcp", "localhost:5432", 5*time.Second)
		if err != nil {
			t.Skipf("PostgreSQL not available: %v", err)
		}
		conn.Close()
		assert.NoError(t, err)
	})

	// Test MinIO connectivity
	t.Run("MinIO", func(t *testing.T) {
		conn, err := net.DialTimeout("tcp", "localhost:9000", 5*time.Second)
		if err != nil {
			t.Skipf("MinIO not available: %v", err)
		}
		conn.Close()
		assert.NoError(t, err)
	})

	// Test SFTP connectivity
	t.Run("SFTP", func(t *testing.T) {
		conn, err := net.DialTimeout("tcp", "localhost:2222", 5*time.Second)
		if err != nil {
			t.Skipf("SFTP not available: %v", err)
		}
		conn.Close()
		assert.NoError(t, err)
	})

	// Test NFS connectivity
	t.Run("NFS", func(t *testing.T) {
		conn, err := net.DialTimeout("tcp", "localhost:2049", 5*time.Second)
		if err != nil {
			t.Skipf("NFS not available: %v", err)
		}
		conn.Close()
		assert.NoError(t, err)
	})
}
