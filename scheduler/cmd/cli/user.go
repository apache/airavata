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

// Group represents a user group
type Group struct {
	ID          string `json:"id"`
	Name        string `json:"name"`
	Description string `json:"description"`
	OwnerID     string `json:"ownerId"`
	IsActive    bool   `json:"isActive"`
	CreatedAt   string `json:"createdAt"`
}

// Project represents a project
type Project struct {
	ID          string `json:"id"`
	Name        string `json:"name"`
	Description string `json:"description"`
	OwnerID     string `json:"ownerId"`
	IsActive    bool   `json:"isActive"`
	CreatedAt   string `json:"createdAt"`
}

// UpdateProfileRequest represents profile update request
type UpdateProfileRequest struct {
	FullName string `json:"fullName"`
	Email    string `json:"email"`
}

// ChangePasswordRequest represents password change request
type ChangePasswordRequest struct {
	OldPassword string `json:"oldPassword"`
	NewPassword string `json:"newPassword"`
}

// createUserCommands creates user-related commands
func createUserCommands() *cobra.Command {
	userCmd := &cobra.Command{
		Use:   "user",
		Short: "User management commands",
		Long:  "Commands for managing your user profile and account",
	}

	profileCmd := &cobra.Command{
		Use:   "profile",
		Short: "View your user profile",
		Long:  "Display your current user profile information",
		RunE: func(cmd *cobra.Command, args []string) error {
			return showUserProfile()
		},
	}

	updateCmd := &cobra.Command{
		Use:   "update",
		Short: "Update your user profile",
		Long: `Update your user profile information such as full name and email.
You will be prompted for the new values interactively.`,
		RunE: func(cmd *cobra.Command, args []string) error {
			return updateUserProfile()
		},
	}

	passwordCmd := &cobra.Command{
		Use:   "password",
		Short: "Change your password",
		Long:  "Change your account password. You will be prompted for the current and new passwords.",
		RunE: func(cmd *cobra.Command, args []string) error {
			return changePassword()
		},
	}

	groupsCmd := &cobra.Command{
		Use:   "groups",
		Short: "List your groups",
		Long:  "Display all groups that you are a member of",
		RunE: func(cmd *cobra.Command, args []string) error {
			return listUserGroups()
		},
	}

	projectsCmd := &cobra.Command{
		Use:   "projects",
		Short: "List your projects",
		Long:  "Display all projects that you own or have access to",
		RunE: func(cmd *cobra.Command, args []string) error {
			return listUserProjects()
		},
	}

	userCmd.AddCommand(profileCmd, updateCmd, passwordCmd, groupsCmd, projectsCmd)
	return userCmd
}

// showUserProfile displays the user's profile
func showUserProfile() error {
	configManager := NewConfigManager()

	if !configManager.IsAuthenticated() {
		return fmt.Errorf("not authenticated - run 'airavata auth login' first")
	}

	serverURL, err := configManager.GetServerURL()
	if err != nil {
		return fmt.Errorf("failed to get server URL: %w", err)
	}

	token, err := configManager.GetToken()
	if err != nil {
		return fmt.Errorf("failed to get token: %w", err)
	}

	user, err := getUserProfile(serverURL, token)
	if err != nil {
		return fmt.Errorf("failed to get user profile: %w", err)
	}

	fmt.Println("👤 User Profile")
	fmt.Println("===============")
	fmt.Printf("ID:         %s\n", user.ID)
	fmt.Printf("Username:   %s\n", user.Username)
	fmt.Printf("Full Name:  %s\n", user.FullName)
	fmt.Printf("Email:      %s\n", user.Email)
	fmt.Printf("Status:     %s\n", getStatusText(user.IsActive))

	return nil
}

// updateUserProfile updates the user's profile
func updateUserProfile() error {
	configManager := NewConfigManager()

	if !configManager.IsAuthenticated() {
		return fmt.Errorf("not authenticated - run 'airavata auth login' first")
	}

	serverURL, err := configManager.GetServerURL()
	if err != nil {
		return fmt.Errorf("failed to get server URL: %w", err)
	}

	token, err := configManager.GetToken()
	if err != nil {
		return fmt.Errorf("failed to get token: %w", err)
	}

	// Get current profile
	currentUser, err := getUserProfile(serverURL, token)
	if err != nil {
		return fmt.Errorf("failed to get current profile: %w", err)
	}

	fmt.Println("📝 Update User Profile")
	fmt.Println("======================")
	fmt.Printf("Current Full Name: %s\n", currentUser.FullName)
	fmt.Printf("Current Email:     %s\n", currentUser.Email)
	fmt.Println()

	// Prompt for new values
	reader := bufio.NewReader(os.Stdin)

	fmt.Print("New Full Name (press Enter to keep current): ")
	fullNameInput, _ := reader.ReadString('\n')
	fullName := strings.TrimSpace(fullNameInput)
	if fullName == "" {
		fullName = currentUser.FullName
	}

	fmt.Print("New Email (press Enter to keep current): ")
	emailInput, _ := reader.ReadString('\n')
	email := strings.TrimSpace(emailInput)
	if email == "" {
		email = currentUser.Email
	}

	// Update profile
	updateReq := UpdateProfileRequest{
		FullName: fullName,
		Email:    email,
	}

	updatedUser, err := updateUserProfileAPI(serverURL, token, updateReq)
	if err != nil {
		return fmt.Errorf("failed to update profile: %w", err)
	}

	fmt.Println("✅ Profile updated successfully!")
	fmt.Printf("Full Name: %s\n", updatedUser.FullName)
	fmt.Printf("Email:     %s\n", updatedUser.Email)

	return nil
}

// changePassword changes the user's password
func changePassword() error {
	configManager := NewConfigManager()

	if !configManager.IsAuthenticated() {
		return fmt.Errorf("not authenticated - run 'airavata auth login' first")
	}

	serverURL, err := configManager.GetServerURL()
	if err != nil {
		return fmt.Errorf("failed to get server URL: %w", err)
	}

	token, err := configManager.GetToken()
	if err != nil {
		return fmt.Errorf("failed to get token: %w", err)
	}

	fmt.Println("🔐 Change Password")
	fmt.Println("==================")

	// Prompt for current password
	fmt.Print("Current Password: ")
	currentPassword, err := term.ReadPassword(int(syscall.Stdin))
	if err != nil {
		return fmt.Errorf("failed to read current password: %w", err)
	}
	fmt.Println()

	// Prompt for new password
	fmt.Print("New Password: ")
	newPassword, err := term.ReadPassword(int(syscall.Stdin))
	if err != nil {
		return fmt.Errorf("failed to read new password: %w", err)
	}
	fmt.Println()

	// Prompt for password confirmation
	fmt.Print("Confirm New Password: ")
	confirmPassword, err := term.ReadPassword(int(syscall.Stdin))
	if err != nil {
		return fmt.Errorf("failed to read password confirmation: %w", err)
	}
	fmt.Println()

	// Validate passwords
	if string(newPassword) != string(confirmPassword) {
		return fmt.Errorf("passwords do not match")
	}

	if len(newPassword) < 8 {
		return fmt.Errorf("new password must be at least 8 characters long")
	}

	// Change password
	changeReq := ChangePasswordRequest{
		OldPassword: string(currentPassword),
		NewPassword: string(newPassword),
	}

	if err := changePasswordAPI(serverURL, token, changeReq); err != nil {
		return fmt.Errorf("failed to change password: %w", err)
	}

	fmt.Println("✅ Password changed successfully!")

	return nil
}

// listUserGroups lists the user's groups
func listUserGroups() error {
	configManager := NewConfigManager()

	if !configManager.IsAuthenticated() {
		return fmt.Errorf("not authenticated - run 'airavata auth login' first")
	}

	serverURL, err := configManager.GetServerURL()
	if err != nil {
		return fmt.Errorf("failed to get server URL: %w", err)
	}

	token, err := configManager.GetToken()
	if err != nil {
		return fmt.Errorf("failed to get token: %w", err)
	}

	groups, err := getUserGroupsAPI(serverURL, token)
	if err != nil {
		return fmt.Errorf("failed to get user groups: %w", err)
	}

	if len(groups) == 0 {
		fmt.Println("📋 You are not a member of any groups")
		return nil
	}

	fmt.Printf("📋 Your Groups (%d)\n", len(groups))
	fmt.Println("==================")

	for _, group := range groups {
		fmt.Printf("• %s", group.Name)
		if group.Description != "" {
			fmt.Printf(" - %s", group.Description)
		}
		fmt.Printf(" (%s)\n", getStatusText(group.IsActive))
	}

	return nil
}

// listUserProjects lists the user's projects
func listUserProjects() error {
	configManager := NewConfigManager()

	if !configManager.IsAuthenticated() {
		return fmt.Errorf("not authenticated - run 'airavata auth login' first")
	}

	serverURL, err := configManager.GetServerURL()
	if err != nil {
		return fmt.Errorf("failed to get server URL: %w", err)
	}

	token, err := configManager.GetToken()
	if err != nil {
		return fmt.Errorf("failed to get token: %w", err)
	}

	projects, err := getUserProjectsAPI(serverURL, token)
	if err != nil {
		return fmt.Errorf("failed to get user projects: %w", err)
	}

	if len(projects) == 0 {
		fmt.Println("📁 You don't have any projects")
		return nil
	}

	fmt.Printf("📁 Your Projects (%d)\n", len(projects))
	fmt.Println("=====================")

	for _, project := range projects {
		fmt.Printf("• %s", project.Name)
		if project.Description != "" {
			fmt.Printf(" - %s", project.Description)
		}
		fmt.Printf(" (%s)\n", getStatusText(project.IsActive))
	}

	return nil
}

// updateUserProfileAPI sends profile update request to the server
func updateUserProfileAPI(serverURL, token string, req UpdateProfileRequest) (*User, error) {
	jsonData, err := json.Marshal(req)
	if err != nil {
		return nil, fmt.Errorf("failed to marshal request: %w", err)
	}

	ctx, cancel := context.WithTimeout(context.Background(), 30*time.Second)
	defer cancel()

	httpReq, err := http.NewRequestWithContext(ctx, "PUT", serverURL+"/api/v2/user/profile", bytes.NewBuffer(jsonData))
	if err != nil {
		return nil, fmt.Errorf("failed to create request: %w", err)
	}

	httpReq.Header.Set("Content-Type", "application/json")
	httpReq.Header.Set("Authorization", "Bearer "+token)

	client := &http.Client{Timeout: 30 * time.Second}
	resp, err := client.Do(httpReq)
	if err != nil {
		return nil, fmt.Errorf("failed to send request: %w", err)
	}
	defer resp.Body.Close()

	body, err := io.ReadAll(resp.Body)
	if err != nil {
		return nil, fmt.Errorf("failed to read response: %w", err)
	}

	if resp.StatusCode != http.StatusOK {
		return nil, fmt.Errorf("update failed: %s", string(body))
	}

	var user User
	if err := json.Unmarshal(body, &user); err != nil {
		return nil, fmt.Errorf("failed to parse response: %w", err)
	}

	return &user, nil
}

// changePasswordAPI sends password change request to the server
func changePasswordAPI(serverURL, token string, req ChangePasswordRequest) error {
	jsonData, err := json.Marshal(req)
	if err != nil {
		return fmt.Errorf("failed to marshal request: %w", err)
	}

	ctx, cancel := context.WithTimeout(context.Background(), 30*time.Second)
	defer cancel()

	httpReq, err := http.NewRequestWithContext(ctx, "PUT", serverURL+"/api/v2/user/password", bytes.NewBuffer(jsonData))
	if err != nil {
		return fmt.Errorf("failed to create request: %w", err)
	}

	httpReq.Header.Set("Content-Type", "application/json")
	httpReq.Header.Set("Authorization", "Bearer "+token)

	client := &http.Client{Timeout: 30 * time.Second}
	resp, err := client.Do(httpReq)
	if err != nil {
		return fmt.Errorf("failed to send request: %w", err)
	}
	defer resp.Body.Close()

	body, err := io.ReadAll(resp.Body)
	if err != nil {
		return fmt.Errorf("failed to read response: %w", err)
	}

	if resp.StatusCode != http.StatusOK {
		return fmt.Errorf("password change failed: %s", string(body))
	}

	return nil
}

// getUserGroupsAPI gets user groups from the server
func getUserGroupsAPI(serverURL, token string) ([]Group, error) {
	ctx, cancel := context.WithTimeout(context.Background(), 10*time.Second)
	defer cancel()

	req, err := http.NewRequestWithContext(ctx, "GET", serverURL+"/api/v2/user/groups", nil)
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
		return nil, fmt.Errorf("failed to get groups: %s", string(body))
	}

	var groups []Group
	if err := json.Unmarshal(body, &groups); err != nil {
		return nil, fmt.Errorf("failed to parse groups: %w", err)
	}

	return groups, nil
}

// getUserProjectsAPI gets user projects from the server
func getUserProjectsAPI(serverURL, token string) ([]Project, error) {
	ctx, cancel := context.WithTimeout(context.Background(), 10*time.Second)
	defer cancel()

	req, err := http.NewRequestWithContext(ctx, "GET", serverURL+"/api/v2/user/projects", nil)
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
		return nil, fmt.Errorf("failed to get projects: %s", string(body))
	}

	var projects []Project
	if err := json.Unmarshal(body, &projects); err != nil {
		return nil, fmt.Errorf("failed to parse projects: %w", err)
	}

	return projects, nil
}
