package commands

import (
	"context"
	"fmt"

	"github.com/apache/airavata/cli/gen-go/airavata_api"
	"github.com/apache/airavata/cli/gen-go/security_model"
	"github.com/apache/airavata/cli/pkg/client"
	"github.com/spf13/cobra"
)

// NewProjectCommand creates the project command
func NewProjectCommand() *cobra.Command {
	projectCmd := &cobra.Command{
		Use:   "project",
		Short: "Project management commands",
		Long:  "Manage Airavata projects (create, update, delete, get, list)",
	}

	projectCmd.AddCommand(NewProjectCreateCommand())
	projectCmd.AddCommand(NewProjectUpdateCommand())
	projectCmd.AddCommand(NewProjectGetCommand())
	projectCmd.AddCommand(NewProjectListCommand())
	projectCmd.AddCommand(NewProjectDeleteCommand())

	return projectCmd
}

func NewProjectCreateCommand() *cobra.Command {
	return &cobra.Command{
		Use:   "create --gateway <id> --name <name> --owner <user>",
		Short: "Create a new project",
		RunE: func(cmd *cobra.Command, args []string) error {
			return fmt.Errorf("project create not yet implemented")
		},
	}
}

func NewProjectUpdateCommand() *cobra.Command {
	return &cobra.Command{
		Use:   "update <id> --name <name>",
		Short: "Update an existing project",
		Args:  cobra.ExactArgs(1),
		RunE: func(cmd *cobra.Command, args []string) error {
			return fmt.Errorf("project update not yet implemented")
		},
	}
}

func NewProjectGetCommand() *cobra.Command {
	return &cobra.Command{
		Use:   "get <id>",
		Short: "Get project details",
		Long:  "Get detailed information about a specific project",
		Args:  cobra.ExactArgs(1),
		RunE:  runProjectGet,
	}
}

func runProjectGet(cmd *cobra.Command, args []string) error {
	projectID := args[0]

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

	// Call GetProject
	ctx := context.Background()
	project, err := airavataClient.GetProject(ctx, authzToken, projectID)
	if err != nil {
		return fmt.Errorf("failed to get project: %w", err)
	}

	// Format output
	outputData := map[string]interface{}{
		"ID":          project.GetProjectID(),
		"Name":        project.GetName(),
		"Description": project.GetDescription(),
		"Owner":       project.GetOwner(),
		"Created":     project.GetCreationTime(),
		"Gateway":     project.GetGatewayId(),
	}

	// Display results
	headers := []string{"Field", "Value"}
	var rows [][]string
	for key, value := range outputData {
		rows = append(rows, []string{key, fmt.Sprintf("%v", value)})
	}

	return formatter.Write(rows, headers)
}

func NewProjectListCommand() *cobra.Command {
	return &cobra.Command{
		Use:   "list",
		Short: "List projects",
		Long:  "List all available Airavata projects",
		RunE:  runProjectList,
	}
}

func runProjectList(cmd *cobra.Command, args []string) error {
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

	// Call GetUserProjects
	ctx := context.Background()
	projects, err := airavataClient.GetUserProjects(ctx, authzToken, cfg.Gateway.ID, cfg.Auth.Username, 100, 0)
	if err != nil {
		return fmt.Errorf("failed to get projects: %w", err)
	}

	// Format output
	if len(projects) == 0 {
		fmt.Println("No projects found")
		return nil
	}

	// Convert to output format
	var outputData []map[string]interface{}
	for _, project := range projects {
		outputData = append(outputData, map[string]interface{}{
			"ID":          project.GetProjectID(),
			"Name":        project.GetName(),
			"Description": project.GetDescription(),
			"Owner":       project.GetOwner(),
			"Created":     project.GetCreationTime(),
		})
	}

	// Display results
	headers := []string{"ID", "Name", "Description", "Owner", "Created"}
	return formatter.Write(outputData, headers)
}

func NewProjectDeleteCommand() *cobra.Command {
	return &cobra.Command{
		Use:   "delete <id>",
		Short: "Delete a project",
		Args:  cobra.ExactArgs(1),
		RunE: func(cmd *cobra.Command, args []string) error {
			return fmt.Errorf("project delete not yet implemented")
		},
	}
}
