package commands

import (
	"fmt"

	"github.com/spf13/cobra"
)

func NewIAMAdminCommand() *cobra.Command {
	cmd := &cobra.Command{
		Use:   "iam-admin",
		Short: "IAM admin commands",
		Long:  "Manage IAM admin services (user/role management)",
	}

	cmd.AddCommand(&cobra.Command{
		Use:   "setup-gateway --name <name> --domain <domain>",
		Short: "Set up a gateway",
		RunE: func(cmd *cobra.Command, args []string) error {
			return fmt.Errorf("iam-admin setup-gateway not yet implemented")
		},
	})

	cmd.AddCommand(&cobra.Command{
		Use:   "register-user --username <user> --email <email> --first-name <fn> --last-name <ln> --password <pwd>",
		Short: "Register a new user",
		RunE: func(cmd *cobra.Command, args []string) error {
			return fmt.Errorf("iam-admin register-user not yet implemented")
		},
	})

	cmd.AddCommand(&cobra.Command{
		Use:   "get-user <username>",
		Short: "Get user details",
		Args:  cobra.ExactArgs(1),
		RunE: func(cmd *cobra.Command, args []string) error {
			return fmt.Errorf("iam-admin get-user not yet implemented")
		},
	})

	cmd.AddCommand(&cobra.Command{
		Use:   "list-users [--offset 0] [--limit 50] [--search <query>]",
		Short: "List users",
		RunE: func(cmd *cobra.Command, args []string) error {
			return fmt.Errorf("iam-admin list-users not yet implemented")
		},
	})

	cmd.AddCommand(&cobra.Command{
		Use:   "enable-user <username>",
		Short: "Enable a user",
		Args:  cobra.ExactArgs(1),
		RunE: func(cmd *cobra.Command, args []string) error {
			return fmt.Errorf("iam-admin enable-user not yet implemented")
		},
	})

	cmd.AddCommand(&cobra.Command{
		Use:   "disable-user <username>",
		Short: "Disable a user",
		Args:  cobra.ExactArgs(1),
		RunE: func(cmd *cobra.Command, args []string) error {
			return fmt.Errorf("iam-admin disable-user not yet implemented")
		},
	})

	cmd.AddCommand(&cobra.Command{
		Use:   "delete-user <username>",
		Short: "Delete a user",
		Args:  cobra.ExactArgs(1),
		RunE: func(cmd *cobra.Command, args []string) error {
			return fmt.Errorf("iam-admin delete-user not yet implemented")
		},
	})

	cmd.AddCommand(&cobra.Command{
		Use:   "reset-password <username> --new-password <pwd>",
		Short: "Reset user password",
		Args:  cobra.ExactArgs(1),
		RunE: func(cmd *cobra.Command, args []string) error {
			return fmt.Errorf("iam-admin reset-password not yet implemented")
		},
	})

	cmd.AddCommand(&cobra.Command{
		Use:   "add-role <username> --role <role-name>",
		Short: "Add role to user",
		Args:  cobra.ExactArgs(1),
		RunE: func(cmd *cobra.Command, args []string) error {
			return fmt.Errorf("iam-admin add-role not yet implemented")
		},
	})

	cmd.AddCommand(&cobra.Command{
		Use:   "remove-role <username> --role <role-name>",
		Short: "Remove role from user",
		Args:  cobra.ExactArgs(1),
		RunE: func(cmd *cobra.Command, args []string) error {
			return fmt.Errorf("iam-admin remove-role not yet implemented")
		},
	})

	cmd.AddCommand(&cobra.Command{
		Use:   "list-users-with-role <role-name>",
		Short: "List users with role",
		Args:  cobra.ExactArgs(1),
		RunE: func(cmd *cobra.Command, args []string) error {
			return fmt.Errorf("iam-admin list-users-with-role not yet implemented")
		},
	})

	cmd.AddCommand(&cobra.Command{
		Use:   "username-available <username>",
		Short: "Check if username is available",
		Args:  cobra.ExactArgs(1),
		RunE: func(cmd *cobra.Command, args []string) error {
			return fmt.Errorf("iam-admin username-available not yet implemented")
		},
	})

	cmd.AddCommand(&cobra.Command{
		Use:   "user-exists <username>",
		Short: "Check if user exists",
		Args:  cobra.ExactArgs(1),
		RunE: func(cmd *cobra.Command, args []string) error {
			return fmt.Errorf("iam-admin user-exists not yet implemented")
		},
	})

	return cmd
}
