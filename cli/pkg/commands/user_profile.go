package commands

import (
	"context"
	"fmt"

	"github.com/apache/airavata/cli/gen-go/airavata_api"
	"github.com/apache/airavata/cli/gen-go/security_model"
	"github.com/apache/airavata/cli/pkg/client"
	"github.com/spf13/cobra"
)

func NewUserProfileCommand() *cobra.Command {
	cmd := &cobra.Command{
		Use:   "user-profile",
		Short: "User profile commands",
		Long:  "Manage user profiles (init, update, get, delete, list)",
	}

	cmd.AddCommand(&cobra.Command{
		Use:   "init",
		Short: "Initialize user profile from IAM",
		RunE: func(cmd *cobra.Command, args []string) error {
			return fmt.Errorf("user-profile init not yet implemented")
		},
	})

	cmd.AddCommand(&cobra.Command{
		Use:   "update --first-name <name> --last-name <name>",
		Short: "Update user profile",
		RunE: func(cmd *cobra.Command, args []string) error {
			return fmt.Errorf("user-profile update not yet implemented")
		},
	})

	cmd.AddCommand(&cobra.Command{
		Use:   "get",
		Short: "Get current user profile",
		Long:  "Get the current user's profile information",
		RunE:  runUserProfileGet,
	})

	cmd.AddCommand(&cobra.Command{
		Use:   "list --gateway <id> [--offset 0] [--limit 50]",
		Short: "List user profiles",
		RunE: func(cmd *cobra.Command, args []string) error {
			return fmt.Errorf("user-profile list not yet implemented")
		},
	})

	cmd.AddCommand(&cobra.Command{
		Use:   "delete <user-id> --gateway <id>",
		Short: "Delete user profile",
		Args:  cobra.ExactArgs(1),
		RunE: func(cmd *cobra.Command, args []string) error {
			return fmt.Errorf("user-profile delete not yet implemented")
		},
	})

	cmd.AddCommand(&cobra.Command{
		Use:   "exists <user-id> --gateway <id>",
		Short: "Check if user profile exists",
		Args:  cobra.ExactArgs(1),
		RunE: func(cmd *cobra.Command, args []string) error {
			return fmt.Errorf("user-profile exists not yet implemented")
		},
	})

	return cmd
}

func runUserProfileGet(cmd *cobra.Command, args []string) error {
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

	// Call GetUserResourceProfile
	ctx := context.Background()
	profile, err := airavataClient.GetUserResourceProfile(ctx, authzToken, cfg.Auth.Username, cfg.Gateway.ID)
	if err != nil {
		return fmt.Errorf("failed to get user profile: %w", err)
	}

	// Format output
	outputData := map[string]interface{}{
		"User ID":                   profile.GetUserId(),
		"Gateway ID":                profile.GetGatewayID(),
		"Credential Store Token":    profile.GetCredentialStoreToken(),
		"Identity Server Tenant":    profile.GetIdentityServerTenant(),
		"Identity Server Pwd Token": profile.GetIdentityServerPwdCredToken(),
	}

	// Display results
	headers := []string{"Field", "Value"}
	var rows [][]string
	for key, value := range outputData {
		rows = append(rows, []string{key, fmt.Sprintf("%v", value)})
	}

	return formatter.Write(rows, headers)
}
