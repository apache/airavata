package commands

import (
	"context"
	"fmt"

	"github.com/apache/airavata/cli/gen-go/airavata_api"
	"github.com/apache/airavata/cli/gen-go/security_model"
	"github.com/apache/airavata/cli/pkg/client"
	"github.com/spf13/cobra"
)

// NewGatewayCommand creates the gateway command
func NewGatewayCommand() *cobra.Command {
	gatewayCmd := &cobra.Command{
		Use:   "gateway",
		Short: "Gateway management commands",
		Long:  "Manage Airavata gateways (create, update, delete, get, list)",
	}

	gatewayCmd.AddCommand(NewGatewayCreateCommand())
	gatewayCmd.AddCommand(NewGatewayUpdateCommand())
	gatewayCmd.AddCommand(NewGatewayGetCommand())
	gatewayCmd.AddCommand(NewGatewayListCommand())
	gatewayCmd.AddCommand(NewGatewayDeleteCommand())
	gatewayCmd.AddCommand(NewGatewayExistsCommand())

	return gatewayCmd
}

// NewGatewayCreateCommand creates the gateway create command
func NewGatewayCreateCommand() *cobra.Command {
	return &cobra.Command{
		Use:   "create --name <name> --domain <domain>",
		Short: "Create a new gateway",
		Long:  "Create a new Airavata gateway with the specified name and domain",
		RunE: func(cmd *cobra.Command, args []string) error {
			// TODO: Implement gateway creation
			return fmt.Errorf("gateway create not yet implemented")
		},
	}
}

// NewGatewayUpdateCommand creates the gateway update command
func NewGatewayUpdateCommand() *cobra.Command {
	return &cobra.Command{
		Use:   "update <id> --name <name>",
		Short: "Update an existing gateway",
		Long:  "Update an existing Airavata gateway",
		Args:  cobra.ExactArgs(1),
		RunE: func(cmd *cobra.Command, args []string) error {
			// TODO: Implement gateway update
			return fmt.Errorf("gateway update not yet implemented")
		},
	}
}

// NewGatewayGetCommand creates the gateway get command
func NewGatewayGetCommand() *cobra.Command {
	return &cobra.Command{
		Use:   "get <id>",
		Short: "Get gateway details",
		Long:  "Get detailed information about a specific gateway",
		Args:  cobra.ExactArgs(1),
		RunE:  runGatewayGet,
	}
}

func runGatewayGet(cmd *cobra.Command, args []string) error {
	gatewayID := args[0]

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

	// Call GetGateway
	ctx := context.Background()
	gateway, err := airavataClient.GetGateway(ctx, authzToken, gatewayID)
	if err != nil {
		return fmt.Errorf("failed to get gateway: %w", err)
	}

	// Format output
	adminName := ""
	if gateway.GatewayAdminFirstName != nil && gateway.GatewayAdminLastName != nil {
		adminName = *gateway.GatewayAdminFirstName + " " + *gateway.GatewayAdminLastName
	}

	outputData := map[string]interface{}{
		"ID":          gateway.GetAiravataInternalGatewayId(),
		"Name":        gateway.GetGatewayName(),
		"Domain":      gateway.GetDomain(),
		"Email":       gateway.GetEmailAddress(),
		"Description": gateway.GetGatewayPublicAbstract(),
		"URL":         gateway.GetGatewayURL(),
		"Admin":       adminName,
		"Admin Email": gateway.GetGatewayAdminEmail(),
		"Status":      gateway.GetGatewayApprovalStatus(),
	}

	// Display results
	headers := []string{"Field", "Value"}
	var rows [][]string
	for key, value := range outputData {
		rows = append(rows, []string{key, fmt.Sprintf("%v", value)})
	}

	return formatter.Write(rows, headers)
}

// NewGatewayListCommand creates the gateway list command
func NewGatewayListCommand() *cobra.Command {
	return &cobra.Command{
		Use:   "list",
		Short: "List all gateways",
		Long:  "List all available Airavata gateways",
		RunE:  runGatewayList,
	}
}

func runGatewayList(cmd *cobra.Command, args []string) error {
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

	// Call GetAllGateways
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
		adminName := ""
		if gateway.GatewayAdminFirstName != nil && gateway.GatewayAdminLastName != nil {
			adminName = *gateway.GatewayAdminFirstName + " " + *gateway.GatewayAdminLastName
		}

		outputData = append(outputData, map[string]interface{}{
			"ID":          gateway.GetAiravataInternalGatewayId(),
			"Name":        gateway.GetGatewayName(),
			"Domain":      gateway.GetDomain(),
			"Email":       gateway.GetEmailAddress(),
			"Description": gateway.GetGatewayPublicAbstract(),
			"URL":         gateway.GetGatewayURL(),
			"Admin":       adminName,
		})
	}

	// Display results
	headers := []string{"ID", "Name", "Domain", "Email", "Description", "URL", "Admin"}
	return formatter.Write(outputData, headers)
}

// NewGatewayDeleteCommand creates the gateway delete command
func NewGatewayDeleteCommand() *cobra.Command {
	return &cobra.Command{
		Use:   "delete <id>",
		Short: "Delete a gateway",
		Long:  "Delete an Airavata gateway",
		Args:  cobra.ExactArgs(1),
		RunE: func(cmd *cobra.Command, args []string) error {
			// TODO: Implement gateway delete
			return fmt.Errorf("gateway delete not yet implemented")
		},
	}
}

// NewGatewayExistsCommand creates the gateway exists command
func NewGatewayExistsCommand() *cobra.Command {
	return &cobra.Command{
		Use:   "exists <id>",
		Short: "Check if gateway exists",
		Long:  "Check if a gateway with the specified ID exists",
		Args:  cobra.ExactArgs(1),
		RunE:  runGatewayExists,
	}
}

func runGatewayExists(cmd *cobra.Command, args []string) error {
	gatewayID := args[0]

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

	// Call IsGatewayExist
	ctx := context.Background()
	exists, err := airavataClient.IsGatewayExist(ctx, authzToken, gatewayID)
	if err != nil {
		return fmt.Errorf("failed to check if gateway exists: %w", err)
	}

	// Display result
	if exists {
		fmt.Printf("Gateway %s exists\n", gatewayID)
	} else {
		fmt.Printf("Gateway %s does not exist\n", gatewayID)
	}

	return nil
}
