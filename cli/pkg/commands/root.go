package commands

import (
	"fmt"
	"os"

	"github.com/apache/airavata/cli/pkg/config"
	"github.com/apache/airavata/cli/pkg/output"
	"github.com/spf13/cobra"
)

var (
	// Global flags
	outputFormat string
	quiet        bool
	verbose      bool

	// Global config
	configManager *config.ConfigManager
	formatter     *output.Formatter
)

// NewRootCommand creates the root command
func NewRootCommand() *cobra.Command {
	configManager = config.NewConfigManager()
	formatter = output.NewFormatter(output.FormatTable, os.Stdout)

	rootCmd := &cobra.Command{
		Use:   "airavata",
		Short: "Airavata CLI - Command line interface for Apache Airavata",
		Long: `Airavata CLI provides a comprehensive command-line interface for Apache Airavata.

This CLI supports all major Airavata services including:
- Gateway and project management
- Experiment lifecycle management
- Application catalog management
- Compute and storage resource management
- User and group management
- Workflow management
- And much more...

Authentication is required for most operations. Use 'airavata auth login <hostname:port>' to authenticate.`,
		PersistentPreRun: func(cmd *cobra.Command, args []string) {
			// Skip auth check for auth commands
			if cmd.Parent() != nil && cmd.Parent().Name() == "auth" {
				return
			}

			// Check authentication for all other commands
			if !configManager.IsAuthenticated() {
				fmt.Fprintf(os.Stderr, "Error: Not authenticated. Please run 'airavata auth login <hostname:port>' first.\n")
				os.Exit(2)
			}
		},
	}

	// Global flags
	rootCmd.PersistentFlags().StringVarP(&outputFormat, "output", "o", "table", "Output format (table, json, csv)")
	rootCmd.PersistentFlags().BoolVarP(&quiet, "quiet", "q", false, "Suppress output except errors")
	rootCmd.PersistentFlags().BoolVarP(&verbose, "verbose", "v", false, "Verbose output")

	// Parse output format
	rootCmd.PersistentPreRunE = func(cmd *cobra.Command, args []string) error {
		// Set output format
		format := output.Format(outputFormat)
		switch format {
		case output.FormatTable, output.FormatJSON, output.FormatCSV:
			formatter = output.NewFormatter(format, os.Stdout)
		default:
			return fmt.Errorf("invalid output format: %s", outputFormat)
		}

		// Skip auth check for auth commands and version
		if cmd.Parent() != nil && (cmd.Parent().Name() == "auth" || cmd.Name() == "version") {
			return nil
		}

		// Check authentication for all other commands
		if !configManager.IsAuthenticated() {
			fmt.Fprintf(os.Stderr, "Error: Not authenticated. Please run 'airavata auth login <hostname:port>' first.\n")
			os.Exit(2)
		}

		return nil
	}

	// Add subcommands
	rootCmd.AddCommand(NewAuthCommand())
	rootCmd.AddCommand(NewGatewayCommand())
	rootCmd.AddCommand(NewProjectCommand())
	rootCmd.AddCommand(NewExperimentCommand())
	rootCmd.AddCommand(NewApplicationCommand())
	rootCmd.AddCommand(NewComputeCommand())
	rootCmd.AddCommand(NewStorageCommand())
	rootCmd.AddCommand(NewCredentialCommand())
	rootCmd.AddCommand(NewResourceProfileCommand())
	rootCmd.AddCommand(NewWorkflowCommand())
	rootCmd.AddCommand(NewSharingCommand())
	rootCmd.AddCommand(NewOrchestratorCommand())
	rootCmd.AddCommand(NewUserProfileCommand())
	rootCmd.AddCommand(NewTenantCommand())
	rootCmd.AddCommand(NewIAMAdminCommand())
	rootCmd.AddCommand(NewGroupManagerCommand())

	return rootCmd
}

// GetConfigManager returns the global config manager
func GetConfigManager() *config.ConfigManager {
	return configManager
}

// GetFormatter returns the global formatter
func GetFormatter() *output.Formatter {
	return formatter
}
