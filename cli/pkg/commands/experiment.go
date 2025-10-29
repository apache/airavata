package commands

import (
	"context"
	"fmt"

	"github.com/apache/airavata/cli/gen-go/airavata_api"
	"github.com/apache/airavata/cli/gen-go/security_model"
	"github.com/apache/airavata/cli/pkg/client"
	"github.com/spf13/cobra"
)

// NewExperimentCommand creates the experiment command
func NewExperimentCommand() *cobra.Command {
	experimentCmd := &cobra.Command{
		Use:   "experiment",
		Short: "Experiment management commands",
		Long:  "Manage Airavata experiments (create, launch, clone, terminate, get, list)",
	}

	experimentCmd.AddCommand(NewExperimentCreateCommand())
	experimentCmd.AddCommand(NewExperimentUpdateCommand())
	experimentCmd.AddCommand(NewExperimentGetCommand())
	experimentCmd.AddCommand(NewExperimentListCommand())
	experimentCmd.AddCommand(NewExperimentDeleteCommand())
	experimentCmd.AddCommand(NewExperimentLaunchCommand())
	experimentCmd.AddCommand(NewExperimentTerminateCommand())
	experimentCmd.AddCommand(NewExperimentCloneCommand())
	experimentCmd.AddCommand(NewExperimentValidateCommand())
	experimentCmd.AddCommand(NewExperimentGetStatusCommand())
	experimentCmd.AddCommand(NewExperimentGetOutputsCommand())

	return experimentCmd
}

func NewExperimentCreateCommand() *cobra.Command {
	return &cobra.Command{
		Use:   "create --gateway <id> --project <id> --name <name>",
		Short: "Create a new experiment",
		RunE: func(cmd *cobra.Command, args []string) error {
			return fmt.Errorf("experiment create not yet implemented")
		},
	}
}

func NewExperimentUpdateCommand() *cobra.Command {
	return &cobra.Command{
		Use:   "update <id>",
		Short: "Update an existing experiment",
		Args:  cobra.ExactArgs(1),
		RunE: func(cmd *cobra.Command, args []string) error {
			return fmt.Errorf("experiment update not yet implemented")
		},
	}
}

func NewExperimentGetCommand() *cobra.Command {
	return &cobra.Command{
		Use:   "get <id>",
		Short: "Get experiment details",
		Long:  "Get detailed information about a specific experiment",
		Args:  cobra.ExactArgs(1),
		RunE:  runExperimentGet,
	}
}

func runExperimentGet(cmd *cobra.Command, args []string) error {
	experimentID := args[0]

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

	// Call GetExperiment
	ctx := context.Background()
	experiment, err := airavataClient.GetExperiment(ctx, authzToken, experimentID)
	if err != nil {
		return fmt.Errorf("failed to get experiment: %w", err)
	}

	// Format output
	outputData := map[string]interface{}{
		"ID":          experiment.GetExperimentId(),
		"Name":        experiment.GetExperimentName(),
		"Description": experiment.GetDescription(),
		"Project":     experiment.GetProjectId(),
		"User":        experiment.GetUserName(),
		"Created":     experiment.GetCreationTime(),
		"Gateway":     experiment.GetGatewayId(),
	}

	// Display results
	headers := []string{"Field", "Value"}
	var rows [][]string
	for key, value := range outputData {
		rows = append(rows, []string{key, fmt.Sprintf("%v", value)})
	}

	return formatter.Write(rows, headers)
}

func NewExperimentListCommand() *cobra.Command {
	return &cobra.Command{
		Use:   "list",
		Short: "List experiments",
		Long:  "List all available Airavata experiments",
		RunE:  runExperimentList,
	}
}

func runExperimentList(cmd *cobra.Command, args []string) error {
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

	// Call GetUserExperiments
	ctx := context.Background()
	experiments, err := airavataClient.GetUserExperiments(ctx, authzToken, cfg.Gateway.ID, cfg.Auth.Username, 100, 0)
	if err != nil {
		return fmt.Errorf("failed to get experiments: %w", err)
	}

	// Format output
	if len(experiments) == 0 {
		fmt.Println("No experiments found")
		return nil
	}

	// Convert to output format
	var outputData []map[string]interface{}
	for _, experiment := range experiments {
		outputData = append(outputData, map[string]interface{}{
			"ID":          experiment.GetExperimentId(),
			"Name":        experiment.GetExperimentName(),
			"Description": experiment.GetDescription(),
			"Project":     experiment.GetProjectId(),
			"User":        experiment.GetUserName(),
			"Created":     experiment.GetCreationTime(),
		})
	}

	// Display results
	headers := []string{"ID", "Name", "Description", "Project", "User", "Created"}
	return formatter.Write(outputData, headers)
}

func NewExperimentDeleteCommand() *cobra.Command {
	return &cobra.Command{
		Use:   "delete <id>",
		Short: "Delete an experiment",
		Args:  cobra.ExactArgs(1),
		RunE: func(cmd *cobra.Command, args []string) error {
			return fmt.Errorf("experiment delete not yet implemented")
		},
	}
}

func NewExperimentLaunchCommand() *cobra.Command {
	return &cobra.Command{
		Use:   "launch <id>",
		Short: "Launch an experiment",
		Long:  "Launch an experiment for execution",
		Args:  cobra.ExactArgs(1),
		RunE:  runExperimentLaunch,
	}
}

func runExperimentLaunch(cmd *cobra.Command, args []string) error {
	experimentID := args[0]

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

	// Call LaunchExperiment
	ctx := context.Background()
	err = airavataClient.LaunchExperiment(ctx, authzToken, experimentID, cfg.Gateway.ID)
	if err != nil {
		return fmt.Errorf("failed to launch experiment: %w", err)
	}

	fmt.Printf("Experiment %s launched successfully\n", experimentID)
	return nil
}

func NewExperimentTerminateCommand() *cobra.Command {
	return &cobra.Command{
		Use:   "terminate <id>",
		Short: "Terminate an experiment",
		Args:  cobra.ExactArgs(1),
		RunE: func(cmd *cobra.Command, args []string) error {
			return fmt.Errorf("experiment terminate not yet implemented")
		},
	}
}

func NewExperimentCloneCommand() *cobra.Command {
	return &cobra.Command{
		Use:   "clone <id> --new-name <name>",
		Short: "Clone an experiment",
		Args:  cobra.ExactArgs(1),
		RunE: func(cmd *cobra.Command, args []string) error {
			return fmt.Errorf("experiment clone not yet implemented")
		},
	}
}

func NewExperimentValidateCommand() *cobra.Command {
	return &cobra.Command{
		Use:   "validate <id>",
		Short: "Validate an experiment",
		Args:  cobra.ExactArgs(1),
		RunE: func(cmd *cobra.Command, args []string) error {
			return fmt.Errorf("experiment validate not yet implemented")
		},
	}
}

func NewExperimentGetStatusCommand() *cobra.Command {
	return &cobra.Command{
		Use:   "get-status <id>",
		Short: "Get experiment status",
		Long:  "Get the current status of an experiment",
		Args:  cobra.ExactArgs(1),
		RunE:  runExperimentStatus,
	}
}

func runExperimentStatus(cmd *cobra.Command, args []string) error {
	experimentID := args[0]

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

	// Call GetExperimentStatus
	ctx := context.Background()
	status, err := airavataClient.GetExperimentStatus(ctx, authzToken, experimentID)
	if err != nil {
		return fmt.Errorf("failed to get experiment status: %w", err)
	}

	// Format output
	outputData := map[string]interface{}{
		"Experiment ID":        experimentID,
		"State":                status.GetState(),
		"Time of State Change": status.GetTimeOfStateChange(),
		"Reason":               status.GetReason(),
	}

	// Display results
	headers := []string{"Field", "Value"}
	var rows [][]string
	for key, value := range outputData {
		rows = append(rows, []string{key, fmt.Sprintf("%v", value)})
	}

	return formatter.Write(rows, headers)
}

func NewExperimentGetOutputsCommand() *cobra.Command {
	return &cobra.Command{
		Use:   "get-outputs <id>",
		Short: "Get experiment outputs",
		Args:  cobra.ExactArgs(1),
		RunE: func(cmd *cobra.Command, args []string) error {
			return fmt.Errorf("experiment get-outputs not yet implemented")
		},
	}
}
