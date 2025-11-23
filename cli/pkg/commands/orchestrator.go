package commands

import (
	"fmt"

	"github.com/spf13/cobra"
)

func NewOrchestratorCommand() *cobra.Command {
	cmd := &cobra.Command{
		Use:   "orchestrator",
		Short: "Orchestrator commands",
		Long:  "Manage experiment orchestration (launch, validate, terminate)",
	}

	cmd.AddCommand(&cobra.Command{
		Use:   "launch-experiment <experiment-id> --gateway <id>",
		Short: "Launch an experiment",
		Args:  cobra.ExactArgs(1),
		RunE: func(cmd *cobra.Command, args []string) error {
			return fmt.Errorf("orchestrator launch-experiment not yet implemented")
		},
	})

	cmd.AddCommand(&cobra.Command{
		Use:   "launch-process <process-id> --gateway <id> --token <cred-token>",
		Short: "Launch a process",
		Args:  cobra.ExactArgs(1),
		RunE: func(cmd *cobra.Command, args []string) error {
			return fmt.Errorf("orchestrator launch-process not yet implemented")
		},
	})

	cmd.AddCommand(&cobra.Command{
		Use:   "validate-experiment <experiment-id>",
		Short: "Validate an experiment",
		Args:  cobra.ExactArgs(1),
		RunE: func(cmd *cobra.Command, args []string) error {
			return fmt.Errorf("orchestrator validate-experiment not yet implemented")
		},
	})

	cmd.AddCommand(&cobra.Command{
		Use:   "validate-process <experiment-id>",
		Short: "Validate a process",
		Args:  cobra.ExactArgs(1),
		RunE: func(cmd *cobra.Command, args []string) error {
			return fmt.Errorf("orchestrator validate-process not yet implemented")
		},
	})

	cmd.AddCommand(&cobra.Command{
		Use:   "terminate-experiment <experiment-id> --gateway <id>",
		Short: "Terminate an experiment",
		Args:  cobra.ExactArgs(1),
		RunE: func(cmd *cobra.Command, args []string) error {
			return fmt.Errorf("orchestrator terminate-experiment not yet implemented")
		},
	})

	return cmd
}
