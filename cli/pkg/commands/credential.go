package commands

import (
	"fmt"

	"github.com/spf13/cobra"
)

func NewCredentialCommand() *cobra.Command {
	cmd := &cobra.Command{
		Use:   "credential",
		Short: "Credential management commands",
		Long:  "Manage credentials (SSH, password, certificate)",
	}

	cmd.AddCommand(&cobra.Command{
		Use:   "add-ssh --gateway <id> --token <id> --private-key <file>",
		Short: "Add SSH credential",
		RunE: func(cmd *cobra.Command, args []string) error {
			return fmt.Errorf("credential add-ssh not yet implemented")
		},
	})

	cmd.AddCommand(&cobra.Command{
		Use:   "add-password --gateway <id> --token <id> --username <user> --password <pwd>",
		Short: "Add password credential",
		RunE: func(cmd *cobra.Command, args []string) error {
			return fmt.Errorf("credential add-password not yet implemented")
		},
	})

	cmd.AddCommand(&cobra.Command{
		Use:   "add-cert --gateway <id> --token <id>",
		Short: "Add certificate credential",
		RunE: func(cmd *cobra.Command, args []string) error {
			return fmt.Errorf("credential add-cert not yet implemented")
		},
	})

	cmd.AddCommand(&cobra.Command{
		Use:   "get-ssh <token> --gateway <id>",
		Short: "Get SSH credential",
		Args:  cobra.ExactArgs(1),
		RunE: func(cmd *cobra.Command, args []string) error {
			return fmt.Errorf("credential get-ssh not yet implemented")
		},
	})

	cmd.AddCommand(&cobra.Command{
		Use:   "get-password <token> --gateway <id>",
		Short: "Get password credential",
		Args:  cobra.ExactArgs(1),
		RunE: func(cmd *cobra.Command, args []string) error {
			return fmt.Errorf("credential get-password not yet implemented")
		},
	})

	cmd.AddCommand(&cobra.Command{
		Use:   "get-cert <token> --gateway <id>",
		Short: "Get certificate credential",
		Args:  cobra.ExactArgs(1),
		RunE: func(cmd *cobra.Command, args []string) error {
			return fmt.Errorf("credential get-cert not yet implemented")
		},
	})

	cmd.AddCommand(&cobra.Command{
		Use:   "list --gateway <id> --type <ssh|password|cert>",
		Short: "List credentials",
		RunE: func(cmd *cobra.Command, args []string) error {
			return fmt.Errorf("credential list not yet implemented")
		},
	})

	cmd.AddCommand(&cobra.Command{
		Use:   "delete-ssh <token> --gateway <id>",
		Short: "Delete SSH credential",
		Args:  cobra.ExactArgs(1),
		RunE: func(cmd *cobra.Command, args []string) error {
			return fmt.Errorf("credential delete-ssh not yet implemented")
		},
	})

	cmd.AddCommand(&cobra.Command{
		Use:   "delete-password <token> --gateway <id>",
		Short: "Delete password credential",
		Args:  cobra.ExactArgs(1),
		RunE: func(cmd *cobra.Command, args []string) error {
			return fmt.Errorf("credential delete-password not yet implemented")
		},
	})

	return cmd
}
