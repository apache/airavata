package client

import (
	"fmt"
	"time"

	"github.com/apache/thrift/lib/go/thrift"
)

// ClientManager manages Thrift client connections
type ClientManager struct {
	serverAddress string
	transport     thrift.TTransport
	protocol      thrift.TProtocol
}

// NewClientManager creates a new client manager
func NewClientManager(serverAddress string) *ClientManager {
	return &ClientManager{
		serverAddress: serverAddress,
	}
}

// Connect establishes a connection to the Airavata server
func (cm *ClientManager) Connect() error {
	// Create socket transport
	transport := thrift.NewTSocketConf(cm.serverAddress, &thrift.TConfiguration{
		ConnectTimeout: 30 * time.Second,
		SocketTimeout:  30 * time.Second,
	})

	// Open transport
	if err := transport.Open(); err != nil {
		return fmt.Errorf("failed to open transport: %w", err)
	}

	// Create binary protocol
	protocol := thrift.NewTBinaryProtocolTransport(transport)

	cm.transport = transport
	cm.protocol = protocol

	return nil
}

// Close closes the connection
func (cm *ClientManager) Close() error {
	if cm.transport != nil {
		return cm.transport.Close()
	}
	return nil
}

// GetProtocol returns the protocol for creating clients
func (cm *ClientManager) GetProtocol() thrift.TProtocol {
	return cm.protocol
}

// IsConnected checks if the client is connected
func (cm *ClientManager) IsConnected() bool {
	return cm.transport != nil && cm.transport.IsOpen()
}

// Reconnect reconnects to the server
func (cm *ClientManager) Reconnect() error {
	if cm.transport != nil {
		cm.transport.Close()
	}
	return cm.Connect()
}

// GetMultiplexedProtocol creates a multiplexed protocol for a specific service
func (cm *ClientManager) GetMultiplexedProtocol(serviceName string) (thrift.TProtocol, error) {
	if !cm.IsConnected() {
		if err := cm.Connect(); err != nil {
			return nil, fmt.Errorf("failed to connect: %w", err)
		}
	}

	// Create multiplexed protocol for the specific service
	multiplexedProtocol := thrift.NewTMultiplexedProtocol(cm.protocol, serviceName)
	return multiplexedProtocol, nil
}

// GetTransport returns the underlying transport
func (cm *ClientManager) GetTransport() thrift.TTransport {
	return cm.transport
}
