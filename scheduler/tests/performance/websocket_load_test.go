package performance

import (
	"context"
	"net/http"
	"net/url"
	"sync"
	"testing"
	"time"

	"github.com/apache/airavata/scheduler/core/domain"
	"github.com/apache/airavata/scheduler/tests/testutil"
	"github.com/gorilla/websocket"
	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
)

// connectWebSocketWithAuth creates a WebSocket connection with authentication headers
func connectWebSocketWithAuth(userID string) (*websocket.Conn, error) {
	u := url.URL{Scheme: "ws", Host: "localhost:8080", Path: "/ws"}

	// Create headers with user ID for authentication
	headers := http.Header{}
	headers.Set("X-User-ID", userID)

	conn, _, err := websocket.DefaultDialer.Dial(u.String(), headers)
	return conn, err
}

func TestWebSocketConcurrentConnections(t *testing.T) {

	suite := testutil.SetupIntegrationTest(t)
	defer suite.Cleanup()

	// Start the scheduler service
	err := suite.Compose.StartServices(t, "scheduler")
	require.NoError(t, err)

	// Wait for service to be ready
	err = suite.Compose.WaitForServices(t, 2*time.Minute)
	require.NoError(t, err)

	// Test concurrent WebSocket connections
	numConnections := 50
	var wg sync.WaitGroup
	results := make(chan error, numConnections)

	startTime := time.Now()

	for i := 0; i < numConnections; i++ {
		wg.Add(1)
		go func(index int) {
			defer wg.Done()

			// Connect to WebSocket with authentication
			conn, err := connectWebSocketWithAuth(suite.TestUser.ID)
			if err != nil {
				results <- err
				return
			}
			defer conn.Close()

			// Keep connection alive for a bit
			time.Sleep(2 * time.Second)

			results <- nil
		}(i)
	}

	wg.Wait()
	close(results)

	// Check results
	var errors []error
	for err := range results {
		if err != nil {
			errors = append(errors, err)
		}
	}

	duration := time.Since(startTime)
	t.Logf("Established %d WebSocket connections in %v", numConnections, duration)
	t.Logf("Connection throughput: %.2f connections/second", float64(numConnections)/duration.Seconds())

	// Allow some failures but not too many
	assert.Less(t, len(errors), numConnections/4, "Too many connection failures: %d", len(errors))
}

func TestWebSocketMessageThroughput(t *testing.T) {

	suite := testutil.SetupIntegrationTest(t)
	defer suite.Cleanup()

	// Start the scheduler service
	err := suite.Compose.StartServices(t, "scheduler")
	require.NoError(t, err)

	// Wait for service to be ready
	err = suite.Compose.WaitForServices(t, 2*time.Minute)
	require.NoError(t, err)

	// Create an experiment to monitor
	req := &domain.CreateExperimentRequest{
		Name:            "websocket-test-exp",
		Description:     "WebSocket test experiment",
		ProjectID:       suite.TestProject.ID,
		CommandTemplate: "echo 'Hello World'",
		Parameters: []domain.ParameterSet{
			{
				Values: map[string]string{
					"param1": "value1",
				},
			},
		},
	}

	exp, err := suite.OrchestratorSvc.CreateExperiment(context.Background(), req, suite.TestUser.ID)
	require.NoError(t, err)

	// Test message throughput
	numMessages := 1000
	var wg sync.WaitGroup
	results := make(chan error, numMessages)

	// Connect to WebSocket with authentication
	conn, err := connectWebSocketWithAuth(suite.TestUser.ID)
	require.NoError(t, err)
	defer conn.Close()

	startTime := time.Now()

	// Send messages concurrently
	for i := 0; i < numMessages; i++ {
		wg.Add(1)
		go func(index int) {
			defer wg.Done()

			// Subscribe to experiment updates
			subscribeMsg := map[string]interface{}{
				"type": "subscribe",
				"data": map[string]string{
					"experiment_id": exp.Experiment.ID,
				},
			}

			err := conn.WriteJSON(subscribeMsg)
			results <- err
		}(i)
	}

	wg.Wait()
	close(results)

	// Check results
	var errors []error
	for err := range results {
		if err != nil {
			errors = append(errors, err)
		}
	}

	duration := time.Since(startTime)
	t.Logf("Sent %d WebSocket messages in %v", numMessages, duration)
	t.Logf("Message throughput: %.2f messages/second", float64(numMessages)/duration.Seconds())

	// All messages should succeed
	assert.Empty(t, errors, "Message failures: %v", errors)
}

func TestWebSocketConnectionStability(t *testing.T) {

	suite := testutil.SetupIntegrationTest(t)
	defer suite.Cleanup()

	// Start the scheduler service
	err := suite.Compose.StartServices(t, "scheduler")
	require.NoError(t, err)

	// Wait for service to be ready
	err = suite.Compose.WaitForServices(t, 2*time.Minute)
	require.NoError(t, err)

	// Test connection stability over time
	numConnections := 20
	duration := 30 * time.Second
	var wg sync.WaitGroup
	results := make(chan error, numConnections)

	startTime := time.Now()

	for i := 0; i < numConnections; i++ {
		wg.Add(1)
		go func(index int) {
			defer wg.Done()

			// Connect to WebSocket with authentication
			conn, err := connectWebSocketWithAuth(suite.TestUser.ID)
			if err != nil {
				results <- err
				return
			}
			defer conn.Close()

			// Keep connection alive and send periodic pings
			ticker := time.NewTicker(5 * time.Second)
			defer ticker.Stop()

			timeout := time.After(duration)
			for {
				select {
				case <-ticker.C:
					// Send ping
					pingMsg := map[string]interface{}{
						"type": "ping",
						"data": map[string]string{
							"timestamp": time.Now().Format(time.RFC3339),
						},
					}

					err := conn.WriteJSON(pingMsg)
					if err != nil {
						results <- err
						return
					}

					// Read pong
					var pongResponse map[string]interface{}
					err = conn.ReadJSON(&pongResponse)
					if err != nil {
						results <- err
						return
					}

				case <-timeout:
					// Connection stable for the duration
					results <- nil
					return
				}
			}
		}(i)
	}

	wg.Wait()
	close(results)

	// Check results
	var errors []error
	for err := range results {
		if err != nil {
			errors = append(errors, err)
		}
	}

	totalDuration := time.Since(startTime)
	t.Logf("Maintained %d WebSocket connections for %v", numConnections, totalDuration)
	t.Logf("Connection stability: %.2f%% success rate", float64(numConnections-len(errors))/float64(numConnections)*100)

	// Most connections should remain stable
	assert.Less(t, len(errors), numConnections/4, "Too many connection failures: %d", len(errors))
}
