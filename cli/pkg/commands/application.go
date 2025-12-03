package commands

import (
	"context"
	"fmt"

	"github.com/apache/airavata/cli/gen-go/airavata_api"
	"github.com/apache/airavata/cli/gen-go/security_model"
	"github.com/apache/airavata/cli/pkg/client"
	"github.com/spf13/cobra"
)

// NewApplicationCommand creates the application command
func NewApplicationCommand() *cobra.Command {
	appCmd := &cobra.Command{
		Use:   "app",
		Short: "Application management commands",
		Long:  "Manage Airavata applications (modules, deployments, interfaces)",
	}

	// Application Modules
	moduleCmd := &cobra.Command{
		Use:   "module",
		Short: "Application module commands",
	}
	moduleCmd.AddCommand(NewAppModuleCreateCommand())
	moduleCmd.AddCommand(NewAppModuleUpdateCommand())
	moduleCmd.AddCommand(NewAppModuleGetCommand())
	moduleCmd.AddCommand(NewAppModuleListCommand())
	moduleCmd.AddCommand(NewAppModuleDeleteCommand())

	// Application Deployments
	deploymentCmd := &cobra.Command{
		Use:   "deployment",
		Short: "Application deployment commands",
	}
	deploymentCmd.AddCommand(NewAppDeploymentCreateCommand())
	deploymentCmd.AddCommand(NewAppDeploymentUpdateCommand())
	deploymentCmd.AddCommand(NewAppDeploymentGetCommand())
	deploymentCmd.AddCommand(NewAppDeploymentListCommand())
	deploymentCmd.AddCommand(NewAppDeploymentDeleteCommand())

	// Application Interfaces
	interfaceCmd := &cobra.Command{
		Use:   "interface",
		Short: "Application interface commands",
	}
	interfaceCmd.AddCommand(NewAppInterfaceCreateCommand())
	interfaceCmd.AddCommand(NewAppInterfaceUpdateCommand())
	interfaceCmd.AddCommand(NewAppInterfaceGetCommand())
	interfaceCmd.AddCommand(NewAppInterfaceListCommand())
	interfaceCmd.AddCommand(NewAppInterfaceDeleteCommand())
	interfaceCmd.AddCommand(NewAppInterfaceCloneCommand())

	appCmd.AddCommand(moduleCmd)
	appCmd.AddCommand(deploymentCmd)
	appCmd.AddCommand(interfaceCmd)

	return appCmd
}

// Application Module Commands
func NewAppModuleCreateCommand() *cobra.Command {
	return &cobra.Command{
		Use:   "create --gateway <id> --name <name> --version <ver>",
		Short: "Create a new application module",
		RunE: func(cmd *cobra.Command, args []string) error {
			return fmt.Errorf("app module create not yet implemented")
		},
	}
}

func NewAppModuleUpdateCommand() *cobra.Command {
	return &cobra.Command{
		Use:   "update <id>",
		Short: "Update an application module",
		Args:  cobra.ExactArgs(1),
		RunE: func(cmd *cobra.Command, args []string) error {
			return fmt.Errorf("app module update not yet implemented")
		},
	}
}

func NewAppModuleGetCommand() *cobra.Command {
	return &cobra.Command{
		Use:   "get <id>",
		Short: "Get application module details",
		Long:  "Get detailed information about a specific application module",
		Args:  cobra.ExactArgs(1),
		RunE:  runAppModuleGet,
	}
}

func runAppModuleGet(cmd *cobra.Command, args []string) error {
	moduleID := args[0]

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

	// Call GetApplicationModule
	ctx := context.Background()
	module, err := airavataClient.GetApplicationModule(ctx, authzToken, moduleID)
	if err != nil {
		return fmt.Errorf("failed to get application module: %w", err)
	}

	// Format output
	outputData := map[string]interface{}{
		"ID":          module.GetAppModuleId(),
		"Name":        module.GetAppModuleName(),
		"Version":     module.GetAppModuleVersion(),
		"Description": module.GetAppModuleDescription(),
	}

	// Display results
	headers := []string{"Field", "Value"}
	var rows [][]string
	for key, value := range outputData {
		rows = append(rows, []string{key, fmt.Sprintf("%v", value)})
	}

	return formatter.Write(rows, headers)
}

func NewAppModuleListCommand() *cobra.Command {
	return &cobra.Command{
		Use:   "list",
		Short: "List application modules",
		Long:  "List all available application modules",
		RunE:  runAppModuleList,
	}
}

func runAppModuleList(cmd *cobra.Command, args []string) error {
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

	// Call GetAllAppModules
	ctx := context.Background()
	modules, err := airavataClient.GetAllAppModules(ctx, authzToken, cfg.Gateway.ID)
	if err != nil {
		return fmt.Errorf("failed to get application modules: %w", err)
	}

	// Format output
	if len(modules) == 0 {
		fmt.Println("No application modules found")
		return nil
	}

	// Convert to output format
	var outputData []map[string]interface{}
	for _, module := range modules {
		outputData = append(outputData, map[string]interface{}{
			"ID":          module.GetAppModuleId(),
			"Name":        module.GetAppModuleName(),
			"Version":     module.GetAppModuleVersion(),
			"Description": module.GetAppModuleDescription(),
		})
	}

	// Display results
	headers := []string{"ID", "Name", "Version", "Description"}
	return formatter.Write(outputData, headers)
}

func NewAppModuleDeleteCommand() *cobra.Command {
	return &cobra.Command{
		Use:   "delete <id>",
		Short: "Delete an application module",
		Args:  cobra.ExactArgs(1),
		RunE: func(cmd *cobra.Command, args []string) error {
			return fmt.Errorf("app module delete not yet implemented")
		},
	}
}

// Application Deployment Commands
func NewAppDeploymentCreateCommand() *cobra.Command {
	return &cobra.Command{
		Use:   "create --gateway <id> --module <id> --compute <id>",
		Short: "Create a new application deployment",
		RunE: func(cmd *cobra.Command, args []string) error {
			return fmt.Errorf("app deployment create not yet implemented")
		},
	}
}

func NewAppDeploymentUpdateCommand() *cobra.Command {
	return &cobra.Command{
		Use:   "update <id>",
		Short: "Update an application deployment",
		Args:  cobra.ExactArgs(1),
		RunE: func(cmd *cobra.Command, args []string) error {
			return fmt.Errorf("app deployment update not yet implemented")
		},
	}
}

func NewAppDeploymentGetCommand() *cobra.Command {
	return &cobra.Command{
		Use:   "get <id>",
		Short: "Get application deployment details",
		Long:  "Get detailed information about a specific application deployment",
		Args:  cobra.ExactArgs(1),
		RunE:  runAppDeploymentGet,
	}
}

func runAppDeploymentGet(cmd *cobra.Command, args []string) error {
	deploymentID := args[0]

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

	// Call GetApplicationDeployment
	ctx := context.Background()
	deployment, err := airavataClient.GetApplicationDeployment(ctx, authzToken, deploymentID)
	if err != nil {
		return fmt.Errorf("failed to get application deployment: %w", err)
	}

	// Format output
	outputData := map[string]interface{}{
		"ID":          deployment.GetAppDeploymentId(),
		"Module":      deployment.GetAppModuleId(),
		"Description": deployment.GetAppDeploymentDescription(),
	}

	// Display results
	headers := []string{"Field", "Value"}
	var rows [][]string
	for key, value := range outputData {
		rows = append(rows, []string{key, fmt.Sprintf("%v", value)})
	}

	return formatter.Write(rows, headers)
}

func NewAppDeploymentListCommand() *cobra.Command {
	return &cobra.Command{
		Use:   "list",
		Short: "List application deployments",
		Long:  "List all available application deployments",
		RunE:  runAppDeploymentList,
	}
}

func runAppDeploymentList(cmd *cobra.Command, args []string) error {
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

	// Call GetAllApplicationDeployments
	ctx := context.Background()
	deployments, err := airavataClient.GetAllApplicationDeployments(ctx, authzToken, cfg.Gateway.ID)
	if err != nil {
		return fmt.Errorf("failed to get application deployments: %w", err)
	}

	// Format output
	if len(deployments) == 0 {
		fmt.Println("No application deployments found")
		return nil
	}

	// Convert to output format
	var outputData []map[string]interface{}
	for _, deployment := range deployments {
		outputData = append(outputData, map[string]interface{}{
			"ID":          deployment.GetAppDeploymentId(),
			"Module":      deployment.GetAppModuleId(),
			"Description": deployment.GetAppDeploymentDescription(),
		})
	}

	// Display results
	headers := []string{"ID", "Module", "Description"}
	return formatter.Write(outputData, headers)
}

func NewAppDeploymentDeleteCommand() *cobra.Command {
	return &cobra.Command{
		Use:   "delete <id>",
		Short: "Delete an application deployment",
		Args:  cobra.ExactArgs(1),
		RunE: func(cmd *cobra.Command, args []string) error {
			return fmt.Errorf("app deployment delete not yet implemented")
		},
	}
}

// Application Interface Commands
func NewAppInterfaceCreateCommand() *cobra.Command {
	return &cobra.Command{
		Use:   "create --gateway <id> --name <name>",
		Short: "Create a new application interface",
		RunE: func(cmd *cobra.Command, args []string) error {
			return fmt.Errorf("app interface create not yet implemented")
		},
	}
}

func NewAppInterfaceUpdateCommand() *cobra.Command {
	return &cobra.Command{
		Use:   "update <id>",
		Short: "Update an application interface",
		Args:  cobra.ExactArgs(1),
		RunE: func(cmd *cobra.Command, args []string) error {
			return fmt.Errorf("app interface update not yet implemented")
		},
	}
}

func NewAppInterfaceGetCommand() *cobra.Command {
	return &cobra.Command{
		Use:   "get <id>",
		Short: "Get application interface details",
		Long:  "Get detailed information about a specific application interface",
		Args:  cobra.ExactArgs(1),
		RunE:  runAppInterfaceGet,
	}
}

func runAppInterfaceGet(cmd *cobra.Command, args []string) error {
	interfaceID := args[0]

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

	// Call GetApplicationInterface
	ctx := context.Background()
	iface, err := airavataClient.GetApplicationInterface(ctx, authzToken, interfaceID)
	if err != nil {
		return fmt.Errorf("failed to get application interface: %w", err)
	}

	// Format output
	outputData := map[string]interface{}{
		"ID":          iface.GetApplicationInterfaceId(),
		"Name":        iface.GetApplicationName(),
		"Description": iface.GetApplicationDescription(),
	}

	// Display results
	headers := []string{"Field", "Value"}
	var rows [][]string
	for key, value := range outputData {
		rows = append(rows, []string{key, fmt.Sprintf("%v", value)})
	}

	return formatter.Write(rows, headers)
}

func NewAppInterfaceListCommand() *cobra.Command {
	return &cobra.Command{
		Use:   "list",
		Short: "List application interfaces",
		Long:  "List all available application interfaces",
		RunE:  runAppInterfaceList,
	}
}

func runAppInterfaceList(cmd *cobra.Command, args []string) error {
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

	// Call GetAllApplicationInterfaces
	ctx := context.Background()
	interfaces, err := airavataClient.GetAllApplicationInterfaces(ctx, authzToken, cfg.Gateway.ID)
	if err != nil {
		return fmt.Errorf("failed to get application interfaces: %w", err)
	}

	// Format output
	if len(interfaces) == 0 {
		fmt.Println("No application interfaces found")
		return nil
	}

	// Convert to output format
	var outputData []map[string]interface{}
	for _, iface := range interfaces {
		outputData = append(outputData, map[string]interface{}{
			"ID":          iface.GetApplicationInterfaceId(),
			"Name":        iface.GetApplicationName(),
			"Description": iface.GetApplicationDescription(),
		})
	}

	// Display results
	headers := []string{"ID", "Name", "Description"}
	return formatter.Write(outputData, headers)
}

func NewAppInterfaceDeleteCommand() *cobra.Command {
	return &cobra.Command{
		Use:   "delete <id>",
		Short: "Delete an application interface",
		Args:  cobra.ExactArgs(1),
		RunE: func(cmd *cobra.Command, args []string) error {
			return fmt.Errorf("app interface delete not yet implemented")
		},
	}
}

func NewAppInterfaceCloneCommand() *cobra.Command {
	return &cobra.Command{
		Use:   "clone <id> --new-name <name>",
		Short: "Clone an application interface",
		Args:  cobra.ExactArgs(1),
		RunE: func(cmd *cobra.Command, args []string) error {
			return fmt.Errorf("app interface clone not yet implemented")
		},
	}
}
