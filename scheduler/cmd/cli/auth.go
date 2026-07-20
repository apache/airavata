package main

import (
	"bufio"
	"bytes"
	"context"
	"encoding/json"
	"fmt"
	"io"
	"net/http"
	"os"
	"strings"
	"syscall"
	"time"

	"github.com/spf13/cobra"
	"golang.org/x/term"
)

// LoginRequest represents the login request
type LoginRequest struct {
	Username string `json:"username"`
	Password string `json:"password"`
}

// LoginResponse represents the login response
type LoginResponse struct {
	Token     string `json:"token"`
	User      User   `json:"user"`
	ExpiresIn int    `json:"expiresIn"`
}

// User represents a user from the API
type User struct {
	ID       string `json:"id"`
	Username string `json:"username"`
	Email    string `json:"email"`
	FullName string `json:"fullName"`
	IsActive bool   `json:"isActive"`
}

// createAuthCommands creates authentication-related commands
func createAuthCommands() *cobra.Command {
	authCmd := &cobra.Command{
		Use:   "auth",
		Short: "Authentication commands",
		Long:  "Commands for user authentication and session management",
	}

	loginCmd := &cobra.Command{
		Use:   "login [username]",
		Short: "Login to the Airavata scheduler",
		Long: `Login to the Airavata scheduler with your username and password.
If username is not provided, you will be prompted for it.

Examples:
  airavata auth login
  airavata auth login admin
  airavata auth login --admin`,
		Args: cobra.MaximumNArgs(1),
		RunE: func(cmd *cobra.Command, args []string) error {
			username := ""
			if len(args) > 0 {
				username = args[0]
			}

			useAdmin, _ := cmd.Flags().GetBool("admin")
			if useAdmin {
				username = "admin"
			}

			return loginCommand(username)
		},
	}

	loginCmd.Flags().Bool("admin", false, "Use default admin credentials")

	logoutCmd := &cobra.Command{
		Use:   "logout",
		Short: "Logout from the Airavata scheduler",
		Long:  "Logout from the Airavata scheduler and clear stored credentials",
		RunE: func(cmd *cobra.Command, args []string) error {
			return logoutCommand()
		},
	}

	statusCmd := &cobra.Command{
		Use:   "status",
		Short: "Check authentication status",
		Long:  "Check if you are currently authenticated and show user information",
		RunE: func(cmd *cobra.Command, args []string) error {
			return statusCommand()
		},
	}

	authCmd.AddCommand(loginCmd, logoutCmd, statusCmd)
	return authCmd
}

// loginCommand handles the login process
func loginCommand(username string) error {
	configManager := NewConfigManager()

	// Get server URL
	serverURL, err := configManager.GetServerURL()
	if err != nil {
		return fmt.Errorf("failed to get server URL: %w", err)
	}

	// Prompt for username if not provided
	if username == "" {
		username, err = promptForUsername()
		if err != nil {
			return fmt.Errorf("failed to get username: %w", err)
		}
	}

	// Prompt for password
	password, err := promptForPassword()
	if err != nil {
		return fmt.Errorf("failed to get password: %w", err)
	}

	// Perform login
	loginResp, err := performLogin(serverURL, username, password)
	if err != nil {
		return fmt.Errorf("login failed: %w", err)
	}

	// Save credentials
	if err := configManager.SetToken(loginResp.Token, loginResp.User.Username); err != nil {
		return fmt.Errorf("failed to save credentials: %w", err)
	}

	fmt.Printf("✅ Successfully logged in as %s (%s)\n", loginResp.User.Username, loginResp.User.FullName)
	fmt.Printf("Token expires in %d seconds\n", loginResp.ExpiresIn)

	return nil
}

// logoutCommand handles the logout process
func logoutCommand() error {
	configManager := NewConfigManager()

	// Check if user is authenticated
	if !configManager.IsAuthenticated() {
		fmt.Println("ℹ️  You are not currently logged in")
		return nil
	}

	// Get current username
	username, err := configManager.GetUsername()
	if err != nil {
		return fmt.Errorf("failed to get username: %w", err)
	}

	// Get server URL and token for logout request
	serverURL, err := configManager.GetServerURL()
	if err != nil {
		return fmt.Errorf("failed to get server URL: %w", err)
	}

	token, err := configManager.GetToken()
	if err != nil {
		return fmt.Errorf("failed to get token: %w", err)
	}

	// Perform logout request
	if err := performLogout(serverURL, token); err != nil {
		fmt.Printf("⚠️  Warning: Logout request failed: %v\n", err)
	}

	// Clear local credentials
	if err := configManager.ClearConfig(); err != nil {
		return fmt.Errorf("failed to clear credentials: %w", err)
	}

	fmt.Printf("✅ Successfully logged out user: %s\n", username)
	return nil
}

// statusCommand shows authentication status
func statusCommand() error {
	configManager := NewConfigManager()

	if !configManager.IsAuthenticated() {
		fmt.Println("❌ Not authenticated")
		fmt.Println("Run 'airavata auth login' to authenticate")
		return nil
	}

	// Get user info
	_, err := configManager.GetUsername()
	if err != nil {
		return fmt.Errorf("failed to get username: %w", err)
	}

	serverURL, err := configManager.GetServerURL()
	if err != nil {
		return fmt.Errorf("failed to get server URL: %w", err)
	}

	token, err := configManager.GetToken()
	if err != nil {
		return fmt.Errorf("failed to get token: %w", err)
	}

	// Get user profile to verify token is still valid
	user, err := getUserProfile(serverURL, token)
	if err != nil {
		fmt.Printf("❌ Authentication expired or invalid\n")
		fmt.Printf("Run 'airavata auth login' to re-authenticate\n")

		// Clear invalid credentials
		configManager.ClearConfig()
		return nil
	}

	fmt.Println("✅ Authenticated")
	fmt.Printf("Username: %s\n", user.Username)
	fmt.Printf("Full Name: %s\n", user.FullName)
	fmt.Printf("Email: %s\n", user.Email)
	fmt.Printf("Server: %s\n", serverURL)
	fmt.Printf("Status: %s\n", getStatusText(user.IsActive))

	return nil
}

// performLogin sends login request to the server
func performLogin(serverURL, username, password string) (*LoginResponse, error) {
	loginReq := LoginRequest{
		Username: username,
		Password: password,
	}

	jsonData, err := json.Marshal(loginReq)
	if err != nil {
		return nil, fmt.Errorf("failed to marshal login request: %w", err)
	}

	ctx, cancel := context.WithTimeout(context.Background(), 30*time.Second)
	defer cancel()

	req, err := http.NewRequestWithContext(ctx, "POST", serverURL+"/api/v2/auth/login", bytes.NewBuffer(jsonData))
	if err != nil {
		return nil, fmt.Errorf("failed to create request: %w", err)
	}

	req.Header.Set("Content-Type", "application/json")

	client := &http.Client{Timeout: 30 * time.Second}
	resp, err := client.Do(req)
	if err != nil {
		return nil, fmt.Errorf("failed to send login request: %w", err)
	}
	defer resp.Body.Close()

	body, err := io.ReadAll(resp.Body)
	if err != nil {
		return nil, fmt.Errorf("failed to read response: %w", err)
	}

	if resp.StatusCode != http.StatusOK {
		return nil, fmt.Errorf("login failed: %s", string(body))
	}

	var loginResp LoginResponse
	if err := json.Unmarshal(body, &loginResp); err != nil {
		return nil, fmt.Errorf("failed to parse login response: %w", err)
	}

	return &loginResp, nil
}

// performLogout sends logout request to the server
func performLogout(serverURL, token string) error {
	ctx, cancel := context.WithTimeout(context.Background(), 10*time.Second)
	defer cancel()

	req, err := http.NewRequestWithContext(ctx, "POST", serverURL+"/api/v2/auth/logout", nil)
	if err != nil {
		return fmt.Errorf("failed to create request: %w", err)
	}

	req.Header.Set("Authorization", "Bearer "+token)

	client := &http.Client{Timeout: 10 * time.Second}
	resp, err := client.Do(req)
	if err != nil {
		return fmt.Errorf("failed to send logout request: %w", err)
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK {
		body, _ := io.ReadAll(resp.Body)
		return fmt.Errorf("logout failed: %s", string(body))
	}

	return nil
}

// getUserProfile gets user profile information
func getUserProfile(serverURL, token string) (*User, error) {
	ctx, cancel := context.WithTimeout(context.Background(), 10*time.Second)
	defer cancel()

	req, err := http.NewRequestWithContext(ctx, "GET", serverURL+"/api/v2/user/profile", nil)
	if err != nil {
		return nil, fmt.Errorf("failed to create request: %w", err)
	}

	req.Header.Set("Authorization", "Bearer "+token)

	client := &http.Client{Timeout: 10 * time.Second}
	resp, err := client.Do(req)
	if err != nil {
		return nil, fmt.Errorf("failed to send request: %w", err)
	}
	defer resp.Body.Close()

	body, err := io.ReadAll(resp.Body)
	if err != nil {
		return nil, fmt.Errorf("failed to read response: %w", err)
	}

	if resp.StatusCode != http.StatusOK {
		return nil, fmt.Errorf("failed to get user profile: %s", string(body))
	}

	var user User
	if err := json.Unmarshal(body, &user); err != nil {
		return nil, fmt.Errorf("failed to parse user profile: %w", err)
	}

	return &user, nil
}

// promptForUsername prompts the user for username
func promptForUsername() (string, error) {
	fmt.Print("Username: ")
	reader := bufio.NewReader(os.Stdin)
	username, err := reader.ReadString('\n')
	if err != nil {
		return "", err
	}
	return strings.TrimSpace(username), nil
}

// promptForPassword prompts the user for password (hidden input)
func promptForPassword() (string, error) {
	fmt.Print("Password: ")
	password, err := term.ReadPassword(int(syscall.Stdin))
	if err != nil {
		return "", err
	}
	fmt.Println() // Add newline after hidden input
	return string(password), nil
}

// getStatusText returns a human-readable status text
func getStatusText(isActive bool) string {
	if isActive {
		return "Active"
	}
	return "Inactive"
}
