package commands

import (
	"context"
	"fmt"

	"github.com/apache/airavata/cli/gen-go/airavata_api"
	"github.com/apache/airavata/cli/gen-go/security_model"
	"github.com/apache/airavata/cli/pkg/client"
	"github.com/spf13/cobra"
)

func NewComputeCommand() *cobra.Command {
	cmd := &cobra.Command{
		Use:   "compute",
		Short: "Compute resource management commands",
		Long:  "Manage compute resources (create, update, delete, get, list)",
	}

	cmd.AddCommand(&cobra.Command{
		Use:   "create --name <name> --host <host>",
		Short: "Create a compute resource",
		RunE: func(cmd *cobra.Command, args []string) error {
			return fmt.Errorf("compute create not yet implemented")
		},
	})

	cmd.AddCommand(&cobra.Command{
		Use:   "update <id>",
		Short: "Update a compute resource",
		Args:  cobra.ExactArgs(1),
		RunE: func(cmd *cobra.Command, args []string) error {
			return fmt.Errorf("compute update not yet implemented")
		},
	})

	cmd.AddCommand(&cobra.Command{
		Use:   "get <id>",
		Short: "Get compute resource details",
		Long:  "Get detailed information about a specific compute resource",
		Args:  cobra.ExactArgs(1),
		RunE:  runComputeGet,
	})

	cmd.AddCommand(&cobra.Command{
		Use:   "list",
		Short: "List compute resources",
		Long:  "List all available compute resources",
		RunE:  runComputeList,
	})

	cmd.AddCommand(&cobra.Command{
		Use:   "delete <id>",
		Short: "Delete a compute resource",
		Args:  cobra.ExactArgs(1),
		RunE: func(cmd *cobra.Command, args []string) error {
			return fmt.Errorf("compute delete not yet implemented")
		},
	})

	return cmd
}

func runComputeList(cmd *cobra.Command, args []string) error {
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

	// Call GetAllComputeResourceNames
	ctx := context.Background()
	resourceNames, err := airavataClient.GetAllComputeResourceNames(ctx, authzToken)
	if err != nil {
		return fmt.Errorf("failed to get compute resources: %w", err)
	}

	// Format output
	if len(resourceNames) == 0 {
		fmt.Println("No compute resources found")
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

func runComputeGet(cmd *cobra.Command, args []string) error {
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

	// Call GetComputeResource
	ctx := context.Background()
	resource, err := airavataClient.GetComputeResource(ctx, authzToken, resourceID)
	if err != nil {
		return fmt.Errorf("failed to get compute resource: %w", err)
	}

	// Format output
	outputData := map[string]interface{}{
		"ID":          resource.GetComputeResourceId(),
		"Name":        resource.GetHostName(),
		"Description": resource.GetResourceDescription(),
	}

	// Display results
	headers := []string{"Field", "Value"}
	var rows [][]string
	for key, value := range outputData {
		rows = append(rows, []string{key, fmt.Sprintf("%v", value)})
	}

	return formatter.Write(rows, headers)
}
