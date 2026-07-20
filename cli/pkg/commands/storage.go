package commands

import (
	"context"
	"fmt"

	"github.com/apache/airavata/cli/gen-go/airavata_api"
	"github.com/apache/airavata/cli/gen-go/security_model"
	"github.com/apache/airavata/cli/pkg/client"
	"github.com/spf13/cobra"
)

func NewStorageCommand() *cobra.Command {
	cmd := &cobra.Command{
		Use:   "storage",
		Short: "Storage resource management commands",
		Long:  "Manage storage resources (create, update, delete, get, list)",
	}

	cmd.AddCommand(&cobra.Command{
		Use:   "create --name <name> --host <host>",
		Short: "Create a storage resource",
		RunE: func(cmd *cobra.Command, args []string) error {
			return fmt.Errorf("storage create not yet implemented")
		},
	})

	cmd.AddCommand(&cobra.Command{
		Use:   "update <id>",
		Short: "Update a storage resource",
		Args:  cobra.ExactArgs(1),
		RunE: func(cmd *cobra.Command, args []string) error {
			return fmt.Errorf("storage update not yet implemented")
		},
	})

	cmd.AddCommand(&cobra.Command{
		Use:   "get <id>",
		Short: "Get storage resource details",
		Long:  "Get detailed information about a specific storage resource",
		Args:  cobra.ExactArgs(1),
		RunE:  runStorageGet,
	})

	cmd.AddCommand(&cobra.Command{
		Use:   "list",
		Short: "List storage resources",
		Long:  "List all available storage resources",
		RunE:  runStorageList,
	})

	cmd.AddCommand(&cobra.Command{
		Use:   "delete <id>",
		Short: "Delete a storage resource",
		Args:  cobra.ExactArgs(1),
		RunE: func(cmd *cobra.Command, args []string) error {
			return fmt.Errorf("storage delete not yet implemented")
		},
	})

	return cmd
}

func runStorageList(cmd *cobra.Command, args []string) error {
	// Load configuration
	cfg, err := configManager.Load()
	if err != nil {
		return fmt.Errorf("failed to load configuration: %w", err)
	}

	// Get server address
	serverAddress, err := configManager.GetServerAddress()
	if err != nil {
		return fmt.Errorf("failed to get server address: %w", err)
	}

	// Create client manager
	clientManager := client.NewClientManager(serverAddress)
	defer clientManager.Close()

	// Connect to server
	if err := clientManager.Connect(); err != nil {
		return fmt.Errorf("failed to connect to server: %w", err)
	}

	// Create multiplexed protocol for Airavata service
	protocol, err := clientManager.GetMultiplexedProtocol("Airavata")
	if err != nil {
		return fmt.Errorf("failed to create multiplexed protocol: %w", err)
	}

	// Create Airavata client using the protocol factory
	airavataClient := airavata_api.NewAiravataClientProtocol(clientManager.GetTransport(), protocol, protocol)

	// Create AuthzToken
	authzToken := &security_model.AuthzToken{
		AccessToken: cfg.Auth.AccessToken,
		ClaimsMap: map[string]string{
			"userName":  cfg.Auth.Username,
			"gatewayID": cfg.Gateway.ID,
		},
	}

	// Call GetAllStorageResourceNames
	ctx := context.Background()
	resourceNames, err := airavataClient.GetAllStorageResourceNames(ctx, authzToken)
	if err != nil {
		return fmt.Errorf("failed to get storage resources: %w", err)
	}

	// Format output
	if len(resourceNames) == 0 {
		fmt.Println("No storage resources found")
		return nil
	}

	// Convert to output format
	var outputData []map[string]interface{}
	for id, name := range resourceNames {
		outputData = append(outputData, map[string]interface{}{
			"ID":   id,
			"Name": name,
		})
	}

	// Display results
	headers := []string{"ID", "Name"}
	return formatter.Write(outputData, headers)
}

func runStorageGet(cmd *cobra.Command, args []string) error {
	resourceID := args[0]

	// Load configuration
	cfg, err := configManager.Load()
	if err != nil {
		return fmt.Errorf("failed to load configuration: %w", err)
	}

	// Get server address
	serverAddress, err := configManager.GetServerAddress()
	if err != nil {
		return fmt.Errorf("failed to get server address: %w", err)
	}

	// Create client manager
	clientManager := client.NewClientManager(serverAddress)
	defer clientManager.Close()

	// Connect to server
	if err := clientManager.Connect(); err != nil {
		return fmt.Errorf("failed to connect to server: %w", err)
	}

	// Create multiplexed protocol for Airavata service
	protocol, err := clientManager.GetMultiplexedProtocol("Airavata")
	if err != nil {
		return fmt.Errorf("failed to create multiplexed protocol: %w", err)
	}

	// Create Airavata client using the protocol factory
	airavataClient := airavata_api.NewAiravataClientProtocol(clientManager.GetTransport(), protocol, protocol)

	// Create AuthzToken
	authzToken := &security_model.AuthzToken{
		AccessToken: cfg.Auth.AccessToken,
		ClaimsMap: map[string]string{
			"userName":  cfg.Auth.Username,
			"gatewayID": cfg.Gateway.ID,
		},
	}

	// Call GetStorageResource
	ctx := context.Background()
	resource, err := airavataClient.GetStorageResource(ctx, authzToken, resourceID)
	if err != nil {
		return fmt.Errorf("failed to get storage resource: %w", err)
	}

	// Format output
	outputData := map[string]interface{}{
		"ID":          resource.GetStorageResourceId(),
		"Name":        resource.GetHostName(),
		"Description": resource.GetStorageResourceDescription(),
	}

	// Display results
	headers := []string{"Field", "Value"}
	var rows [][]string
	for key, value := range outputData {
		rows = append(rows, []string{key, fmt.Sprintf("%v", value)})
	}

	return formatter.Write(rows, headers)
}
