package commands

import (
	"context"
	"fmt"

	"github.com/apache/airavata/cli/gen-go/airavata_api"
	"github.com/apache/airavata/cli/gen-go/security_model"
	"github.com/apache/airavata/cli/pkg/client"
	"github.com/spf13/cobra"
)

func NewTenantCommand() *cobra.Command {
	cmd := &cobra.Command{
		Use:   "tenant",
		Short: "Tenant profile commands",
		Long:  "Manage tenant profiles (gateway management)",
	}

	cmd.AddCommand(&cobra.Command{
		Use:   "add-gateway --name <name> --domain <domain>",
		Short: "Add a gateway",
		RunE: func(cmd *cobra.Command, args []string) error {
			return fmt.Errorf("tenant add-gateway not yet implemented")
		},
	})

	cmd.AddCommand(&cobra.Command{
		Use:   "update-gateway <id>",
		Short: "Update a gateway",
		Args:  cobra.ExactArgs(1),
		RunE: func(cmd *cobra.Command, args []string) error {
			return fmt.Errorf("tenant update-gateway not yet implemented")
		},
	})

	cmd.AddCommand(&cobra.Command{
		Use:   "get <id>",
		Short: "Get tenant profile",
		Long:  "Get detailed information about a specific tenant",
		Args:  cobra.ExactArgs(1),
		RunE:  runTenantGet,
	})

	cmd.AddCommand(&cobra.Command{
		Use:   "list",
		Short: "List all tenants",
		Long:  "List all available tenant profiles",
		RunE:  runTenantList,
	})

	cmd.AddCommand(&cobra.Command{
		Use:   "delete-gateway <id>",
		Short: "Delete a gateway",
		Args:  cobra.ExactArgs(1),
		RunE: func(cmd *cobra.Command, args []string) error {
			return fmt.Errorf("tenant delete-gateway not yet implemented")
		},
	})

	cmd.AddCommand(&cobra.Command{
		Use:   "gateway-exists <id>",
		Short: "Check if gateway exists",
		Args:  cobra.ExactArgs(1),
		RunE: func(cmd *cobra.Command, args []string) error {
			return fmt.Errorf("tenant gateway-exists not yet implemented")
		},
	})

	return cmd
}

func runTenantGet(cmd *cobra.Command, args []string) error {
	tenantID := args[0]

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

	// Call GetGateway (using gateway as tenant for now)
	ctx := context.Background()
	gateway, err := airavataClient.GetGateway(ctx, authzToken, tenantID)
	if err != nil {
		return fmt.Errorf("failed to get gateway: %w", err)
	}

	// Format output
	outputData := map[string]interface{}{
		"Gateway ID":    gateway.GetAiravataInternalGatewayId(),
		"Name":          gateway.GetGatewayName(),
		"Description":   gateway.GetGatewayPublicAbstract(),
		"Domain":        gateway.GetDomain(),
		"Contact Email": gateway.GetEmailAddress(),
	}

	// Display results
	headers := []string{"Field", "Value"}
	var rows [][]string
	for key, value := range outputData {
		rows = append(rows, []string{key, fmt.Sprintf("%v", value)})
	}

	return formatter.Write(rows, headers)
}

func runTenantList(cmd *cobra.Command, args []string) error {
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

	// Call GetAllGateways (using gateways as tenants for now)
	ctx := context.Background()
	gateways, err := airavataClient.GetAllGateways(ctx, authzToken)
	if err != nil {
		return fmt.Errorf("failed to get gateways: %w", err)
	}

	// Format output
	if len(gateways) == 0 {
		fmt.Println("No gateways found")
		return nil
	}

	// Convert to output format
	var outputData []map[string]interface{}
	for _, gateway := range gateways {
		outputData = append(outputData, map[string]interface{}{
			"Gateway ID":    gateway.GetAiravataInternalGatewayId(),
			"Name":          gateway.GetGatewayName(),
			"Description":   gateway.GetGatewayPublicAbstract(),
			"Domain":        gateway.GetDomain(),
			"Contact Email": gateway.GetEmailAddress(),
		})
	}

	// Display results
	headers := []string{"Gateway ID", "Name", "Description", "Domain", "Contact Email"}
	return formatter.Write(outputData, headers)
}
