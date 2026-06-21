package commands

import (
	"fmt"
	"strings"
	"time"

	"github.com/apache/airavata/cli/pkg/auth"
	"github.com/apache/airavata/cli/pkg/config"
	"github.com/spf13/cobra"
)

// NewAuthCommand creates the auth command
func NewAuthCommand() *cobra.Command {
	authCmd := &cobra.Command{
		Use:   "auth",
		Short: "Authentication commands",
		Long:  "Manage authentication with Airavata server",
	}

	authCmd.AddCommand(NewAuthLoginCommand())
	authCmd.AddCommand(NewAuthLogoutCommand())
	authCmd.AddCommand(NewAuthStatusCommand())
	authCmd.AddCommand(NewAuthRefreshCommand())

	return authCmd
}

// NewAuthLoginCommand creates the login command
func NewAuthLoginCommand() *cobra.Command {
	return &cobra.Command{
		Use:   "login <hostname:port>",
		Short: "Authenticate with Airavata server using device flow",
		Long: `Authenticate with an Airavata server using OAuth2 device authorization flow.

This command will:
1. Connect to the specified Airavata server
2. Initiate OAuth2 device authorization flow
3. Display a user code and verification URL
4. Wait for you to complete authentication in your browser
5. Store the authentication tokens for future use

Examples:
  airavata auth login api.scigap.org:9930
  airavata auth login localhost:9930
  airavata auth login 192.168.1.100:9930`,
		Args: cobra.ExactArgs(1),
		RunE: runAuthLogin,
	}
}

// NewAuthLogoutCommand creates the logout command
func NewAuthLogoutCommand() *cobra.Command {
	return &cobra.Command{
		Use:   "logout",
		Short: "Clear stored authentication tokens",
		Long:  "Remove stored authentication tokens and server configuration",
		RunE:  runAuthLogout,
	}
}

// NewAuthStatusCommand creates the status command
func NewAuthStatusCommand() *cobra.Command {
	return &cobra.Command{
		Use:   "status",
		Short: "Show authentication status",
		Long:  "Display current authentication status and server information",
		RunE:  runAuthStatus,
	}
}

// NewAuthRefreshCommand creates the refresh command
func NewAuthRefreshCommand() *cobra.Command {
	return &cobra.Command{
		Use:   "refresh",
		Short: "Refresh authentication token",
		Long:  "Manually refresh the stored authentication token",
		RunE:  runAuthRefresh,
	}
}

func runAuthLogin(cmd *cobra.Command, args []string) error {
	serverArg := args[0]

	// Parse hostname:port
	parts := strings.Split(serverArg, ":")
	if len(parts) != 2 {
		return fmt.Errorf("invalid server format. Expected hostname:port, got: %s", serverArg)
	}

	hostname := parts[0]
	port := parts[1]

	// Validate hostname and port
	if hostname == "" {
		return fmt.Errorf("hostname cannot be empty")
	}
	if port == "" {
		return fmt.Errorf("port cannot be empty")
	}

	// Discover Keycloak configuration
	fmt.Printf("Discovering Keycloak configuration for %s...\n", serverArg)
	keycloakURL, realm, err := auth.DiscoverKeycloakInfo(hostname)
	if err != nil {
		return fmt.Errorf("failed to discover Keycloak configuration: %w", err)
	}

	fmt.Printf("Found Keycloak at: %s (realm: %s)\n", keycloakURL, realm)

	// Create auth manager
	authManager := auth.NewAuthManager(keycloakURL, realm, "airavata-cli")

	// Start device auth flow
	fmt.Println("Starting device authorization flow...")
	deviceResp, err := authManager.StartDeviceAuth()
	if err != nil {
		return fmt.Errorf("failed to start device auth: %w", err)
	}

	// Display instructions to user
	fmt.Println("\n" + strings.Repeat("=", 60))
	fmt.Println("DEVICE AUTHORIZATION REQUIRED")
	fmt.Println(strings.Repeat("=", 60))
	fmt.Printf("1. Open your browser and go to: %s\n", deviceResp.VerificationURI)
	fmt.Printf("2. Enter the following code: %s\n", deviceResp.UserCode)
	fmt.Println("3. Complete the authentication in your browser")
	fmt.Println("4. This window will automatically continue once authenticated")
	fmt.Println(strings.Repeat("=", 60))
	fmt.Println()

	// Poll for token
	fmt.Println("Waiting for authentication...")
	interval := time.Duration(deviceResp.Interval) * time.Second
	if interval == 0 {
		interval = 5 * time.Second
	}

	tokenResp, err := authManager.PollForToken(deviceResp.DeviceCode, interval)
	if err != nil {
		return fmt.Errorf("failed to get token: %w", err)
	}

	// Get user info
	fmt.Println("Getting user information...")
	userInfo, err := authManager.GetUserInfo(tokenResp.AccessToken)
	if err != nil {
		return fmt.Errorf("failed to get user info: %w", err)
	}

	// Create configuration
	cfg := config.DefaultConfig()
	cfg.Server.Hostname = hostname
	cfg.Server.Port = 9930 // Default port, could be parsed from port
	cfg.Server.TLS = true  // Assume TLS for production servers

	cfg.Auth.KeycloakURL = keycloakURL
	cfg.Auth.Realm = realm
	cfg.Auth.AccessToken = tokenResp.AccessToken
	cfg.Auth.RefreshToken = tokenResp.RefreshToken
	cfg.Auth.ExpiresAt = time.Now().Add(time.Duration(tokenResp.ExpiresIn) * time.Second)
	cfg.Auth.Username = userInfo.Username

	// Save configuration
	if err := configManager.Save(cfg); err != nil {
		return fmt.Errorf("failed to save configuration: %w", err)
	}

	// Success message
	fmt.Println("✅ Authentication successful!")
	fmt.Printf("Logged in as: %s (%s)\n", userInfo.Username, userInfo.Email)
	fmt.Printf("Server: %s\n", serverArg)
	fmt.Printf("Token expires: %s\n", cfg.Auth.ExpiresAt.Format(time.RFC3339))

	return nil
}

func runAuthLogout(cmd *cobra.Command, args []string) error {
	// Load current config to show what we're logging out from
	cfg, err := configManager.Load()
	if err != nil {
		return fmt.Errorf("failed to load configuration: %w", err)
	}

	if cfg.Auth.Username == "" {
		fmt.Println("Not currently authenticated")
		return nil
	}

	// Clear configuration
	if err := configManager.Clear(); err != nil {
		return fmt.Errorf("failed to clear configuration: %w", err)
	}

	fmt.Printf("✅ Logged out from %s (user: %s)\n",
		fmt.Sprintf("%s:%d", cfg.Server.Hostname, cfg.Server.Port),
		cfg.Auth.Username)

	return nil
}

func runAuthStatus(cmd *cobra.Command, args []string) error {
	cfg, err := configManager.Load()
	if err != nil {
		return fmt.Errorf("failed to load configuration: %w", err)
	}

	if !configManager.IsAuthenticated() {
		fmt.Println("❌ Not authenticated")
		fmt.Println("Run 'airavata auth login <hostname:port>' to authenticate")
		return nil
	}

	// Show status
	fmt.Println("✅ Authenticated")
	fmt.Printf("User: %s\n", cfg.Auth.Username)
	fmt.Printf("Server: %s:%d\n", cfg.Server.Hostname, cfg.Server.Port)
	fmt.Printf("Keycloak: %s\n", cfg.Auth.KeycloakURL)
	fmt.Printf("Realm: %s\n", cfg.Auth.Realm)
	fmt.Printf("Token expires: %s\n", cfg.Auth.ExpiresAt.Format(time.RFC3339))

	// Check if token is close to expiry
	timeUntilExpiry := time.Until(cfg.Auth.ExpiresAt)
	if timeUntilExpiry < 5*time.Minute {
		fmt.Println("⚠️  Token expires soon, consider running 'airavata auth refresh'")
	}

	return nil
}

func runAuthRefresh(cmd *cobra.Command, args []string) error {
	cfg, err := configManager.Load()
	if err != nil {
		return fmt.Errorf("failed to load configuration: %w", err)
	}

	if !configManager.IsAuthenticated() {
		return fmt.Errorf("not authenticated")
	}

	if cfg.Auth.RefreshToken == "" {
		return fmt.Errorf("no refresh token available")
	}

	// Create auth manager
	authManager := auth.NewAuthManager(cfg.Auth.KeycloakURL, cfg.Auth.Realm, cfg.Auth.ClientID)

	// Refresh token
	fmt.Println("Refreshing authentication token...")
	tokenResp, err := authManager.RefreshToken(cfg.Auth.RefreshToken)
	if err != nil {
		return fmt.Errorf("failed to refresh token: %w", err)
	}

	// Update configuration
	cfg.Auth.AccessToken = tokenResp.AccessToken
	if tokenResp.RefreshToken != "" {
		cfg.Auth.RefreshToken = tokenResp.RefreshToken
	}
	cfg.Auth.ExpiresAt = time.Now().Add(time.Duration(tokenResp.ExpiresIn) * time.Second)

	// Save configuration
	if err := configManager.Save(cfg); err != nil {
		return fmt.Errorf("failed to save refreshed configuration: %w", err)
	}

	fmt.Println("✅ Token refreshed successfully")
	fmt.Printf("New expiry: %s\n", cfg.Auth.ExpiresAt.Format(time.RFC3339))

	return nil
}
