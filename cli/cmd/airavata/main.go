package main

import (
	"fmt"
	"os"

	"github.com/apache/airavata/cli/pkg/commands"
	"github.com/spf13/cobra"
)

var (
	version = "0.1.0"
	build   = "dev"
)

func main() {
	rootCmd := commands.NewRootCommand()

	// Add version information
	rootCmd.AddCommand(&cobra.Command{
		Use:   "version",
		Short: "Show version information",
		Run: func(cmd *cobra.Command, args []string) {
			fmt.Printf("Airavata CLI %s (build %s)\n", version, build)
		},
	})

	if err := rootCmd.Execute(); err != nil {
		os.Exit(1)
	}
}
