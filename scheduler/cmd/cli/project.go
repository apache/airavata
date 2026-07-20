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
	"time"

	"github.com/spf13/cobra"
)

// ProjectMember represents a project member
type ProjectMember struct {
	UserID   string `json:"user_id"`
	Username string `json:"username"`
	Email    string `json:"email"`
	Role     string `json:"role"`
	JoinedAt string `json:"joined_at"`
	IsActive bool   `json:"is_active"`
}

// CreateProjectRequest represents project creation request
type CreateProjectRequest struct {
	Name        string `json:"name"`
	Description string `json:"description"`
}

// UpdateProjectRequest represents project update request
type UpdateProjectRequest struct {
	Name        string `json:"name"`
	Description string `json:"description"`
}

// AddMemberRequest represents add member request
type AddMemberRequest struct {
	UserID string `json:"user_id"`
	Role   string `json:"role"`
}

// createProjectCommands creates project management commands
func createProjectCommands() *cobra.Command {
	projectCmd := &cobra.Command{
		Use:   "project",
		Short: "Project management commands",
		Long:  "Commands for managing projects and project members",
	}

	// Create command
	createCmd := &cobra.Command{
		Use:   "create",
		Short: "Create a new project",
		Long: `Create a new project with interactive prompts.

Examples:
  airavata project create`,
		RunE: func(cmd *cobra.Command, args []string) error {
			return createProject()
		},
	}

	// List command (reuse existing user projects command)
	listCmd := &cobra.Command{
		Use:   "list",
		Short: "List your projects",
		Long: `List all projects that you own or have access to.

Examples:
  airavata project list`,
		RunE: func(cmd *cobra.Command, args []string) error {
			return listUserProjects()
		},
	}

	// Get command
	getCmd := &cobra.Command{
		Use:   "get <project-id>",
		Short: "Get project details",
		Long: `Get detailed information about a specific project.

Examples:
  airavata project get proj-123`,
		Args: cobra.ExactArgs(1),
		RunE: func(cmd *cobra.Command, args []string) error {
			return getProject(args[0])
		},
	}

	// Update command
	updateCmd := &cobra.Command{
		Use:   "update <project-id>",
		Short: "Update a project",
		Long: `Update project information with interactive prompts.

Examples:
  airavata project update proj-123`,
		Args: cobra.ExactArgs(1),
		RunE: func(cmd *cobra.Command, args []string) error {
			return updateProject(args[0])
		},
	}

	// Delete command
	deleteCmd := &cobra.Command{
		Use:   "delete <project-id>",
		Short: "Delete a project",
		Long: `Delete a project and all its associated data.

Examples:
  airavata project delete proj-123`,
		Args: cobra.ExactArgs(1),
		RunE: func(cmd *cobra.Command, args []string) error {
			return deleteProject(args[0])
		},
	}

	// Members command
	membersCmd := &cobra.Command{
		Use:   "members <project-id>",
		Short: "List project members",
		Long: `List all members of a project.

Examples:
  airavata project members proj-123`,
		Args: cobra.ExactArgs(1),
		RunE: func(cmd *cobra.Command, args []string) error {
			return listProjectMembers(args[0])
		},
	}

	// Add member command
	addMemberCmd := &cobra.Command{
		Use:   "add-member <project-id> <user-id>",
		Short: "Add a member to a project",
		Long: `Add a user as a member to a project.

Examples:
  airavata project add-member proj-123 user-456
  airavata project add-member proj-123 user-456 --role admin`,
		Args: cobra.ExactArgs(2),
		RunE: func(cmd *cobra.Command, args []string) error {
			role, _ := cmd.Flags().GetString("role")
			return addProjectMember(args[0], args[1], role)
		},
	}

	addMemberCmd.Flags().String("role", "member", "Role for the new member (admin, member)")

	// Remove member command
	removeMemberCmd := &cobra.Command{
		Use:   "remove-member <project-id> <user-id>",
		Short: "Remove a member from a project",
		Long: `Remove a user from a project.

Examples:
  airavata project remove-member proj-123 user-456`,
		Args: cobra.ExactArgs(2),
		RunE: func(cmd *cobra.Command, args []string) error {
			return removeProjectMember(args[0], args[1])
		},
	}

	projectCmd.AddCommand(createCmd, listCmd, getCmd, updateCmd, deleteCmd, membersCmd, addMemberCmd, removeMemberCmd)
	return projectCmd
}

// createProject creates a new project
func createProject() error {
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

	reader := bufio.NewReader(os.Stdin)

	fmt.Println("🆕 Create Project")
	fmt.Println("=================")

	fmt.Print("Project Name: ")
	name, _ := reader.ReadString('\n')
	name = strings.TrimSpace(name)

	fmt.Print("Description: ")
	description, _ := reader.ReadString('\n')
	description = strings.TrimSpace(description)

	createReq := CreateProjectRequest{
		Name:        name,
		Description: description,
	}

	project, err := createProjectAPI(serverURL, token, createReq)
	if err != nil {
		return fmt.Errorf("failed to create project: %w", err)
	}

	fmt.Printf("✅ Project created successfully!\n")
	fmt.Printf("ID:          %s\n", project.ID)
	fmt.Printf("Name:        %s\n", project.Name)
	fmt.Printf("Description: %s\n", project.Description)
	fmt.Printf("Owner:       %s\n", project.OwnerID)
	fmt.Printf("Created:     %s\n", project.CreatedAt)

	return nil
}

// getProject gets detailed information about a project
func getProject(projectID string) error {
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

	project, err := getProjectAPI(serverURL, token, projectID)
	if err != nil {
		return fmt.Errorf("failed to get project: %w", err)
	}

	fmt.Printf("📁 Project Details: %s\n", project.Name)
	fmt.Println("================================")
	fmt.Printf("ID:          %s\n", project.ID)
	fmt.Printf("Name:        %s\n", project.Name)
	fmt.Printf("Description: %s\n", project.Description)
	fmt.Printf("Owner:       %s\n", project.OwnerID)
	fmt.Printf("Status:      %s\n", getStatusText(project.IsActive))
	fmt.Printf("Created:     %s\n", project.CreatedAt)

	return nil
}

// updateProject updates project information
func updateProject(projectID string) error {
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

	// Get current project details
	currentProject, err := getProjectAPI(serverURL, token, projectID)
	if err != nil {
		return fmt.Errorf("failed to get current project: %w", err)
	}

	fmt.Println("📝 Update Project")
	fmt.Println("==================")
	fmt.Printf("Current Name:        %s\n", currentProject.Name)
	fmt.Printf("Current Description: %s\n", currentProject.Description)
	fmt.Println()

	reader := bufio.NewReader(os.Stdin)

	fmt.Print("New Name (press Enter to keep current): ")
	nameInput, _ := reader.ReadString('\n')
	name := strings.TrimSpace(nameInput)
	if name == "" {
		name = currentProject.Name
	}

	fmt.Print("New Description (press Enter to keep current): ")
	descriptionInput, _ := reader.ReadString('\n')
	description := strings.TrimSpace(descriptionInput)
	if description == "" {
		description = currentProject.Description
	}

	updateReq := UpdateProjectRequest{
		Name:        name,
		Description: description,
	}

	updatedProject, err := updateProjectAPI(serverURL, token, projectID, updateReq)
	if err != nil {
		return fmt.Errorf("failed to update project: %w", err)
	}

	fmt.Println("✅ Project updated successfully!")
	fmt.Printf("Name:        %s\n", updatedProject.Name)
	fmt.Printf("Description: %s\n", updatedProject.Description)

	return nil
}

// deleteProject deletes a project
func deleteProject(projectID string) error {
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

	// Get project details for confirmation
	project, err := getProjectAPI(serverURL, token, projectID)
	if err != nil {
		return fmt.Errorf("failed to get project details: %w", err)
	}

	fmt.Printf("⚠️  Are you sure you want to delete project '%s'?\n", project.Name)
	fmt.Printf("   This will permanently delete the project and all associated data.\n")
	fmt.Print("   Type 'yes' to confirm: ")

	reader := bufio.NewReader(os.Stdin)
	confirm, _ := reader.ReadString('\n')
	confirm = strings.TrimSpace(strings.ToLower(confirm))

	if confirm != "yes" {
		fmt.Println("❌ Deletion cancelled")
		return nil
	}

	if err := deleteProjectAPI(serverURL, token, projectID); err != nil {
		return fmt.Errorf("failed to delete project: %w", err)
	}

	fmt.Printf("✅ Project '%s' deleted successfully\n", project.Name)
	return nil
}

// listProjectMembers lists all members of a project
func listProjectMembers(projectID string) error {
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

	members, err := getProjectMembersAPI(serverURL, token, projectID)
	if err != nil {
		return fmt.Errorf("failed to get project members: %w", err)
	}

	if len(members) == 0 {
		fmt.Printf("👥 No members found for project %s\n", projectID)
		return nil
	}

	fmt.Printf("👥 Project Members (%d)\n", len(members))
	fmt.Println("========================")

	for _, member := range members {
		statusIcon := "✅"
		if !member.IsActive {
			statusIcon = "❌"
		}
		fmt.Printf("%s %s (%s) - %s\n", statusIcon, member.Username, member.Email, member.Role)
		fmt.Printf("   Joined: %s\n", member.JoinedAt)
		fmt.Println()
	}

	return nil
}

// addProjectMember adds a user to a project
func addProjectMember(projectID, userID, role string) error {
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

	addReq := AddMemberRequest{
		UserID: userID,
		Role:   role,
	}

	if err := addProjectMemberAPI(serverURL, token, projectID, addReq); err != nil {
		return fmt.Errorf("failed to add project member: %w", err)
	}

	fmt.Printf("✅ User %s added to project %s as %s\n", userID, projectID, role)
	return nil
}

// removeProjectMember removes a user from a project
func removeProjectMember(projectID, userID string) error {
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

	fmt.Printf("⚠️  Are you sure you want to remove user %s from project %s? (y/N): ", userID, projectID)
	reader := bufio.NewReader(os.Stdin)
	confirm, _ := reader.ReadString('\n')
	confirm = strings.TrimSpace(strings.ToLower(confirm))

	if confirm != "y" && confirm != "yes" {
		fmt.Println("❌ Removal cancelled")
		return nil
	}

	if err := removeProjectMemberAPI(serverURL, token, projectID, userID); err != nil {
		return fmt.Errorf("failed to remove project member: %w", err)
	}

	fmt.Printf("✅ User %s removed from project %s\n", userID, projectID)
	return nil
}

// API functions

// createProjectAPI creates a project via the API
func createProjectAPI(serverURL, token string, req CreateProjectRequest) (*Project, error) {
	jsonData, err := json.Marshal(req)
	if err != nil {
		return nil, fmt.Errorf("failed to marshal request: %w", err)
	}

	ctx, cancel := context.WithTimeout(context.Background(), 30*time.Second)
	defer cancel()

	httpReq, err := http.NewRequestWithContext(ctx, "POST", serverURL+"/api/v1/projects", bytes.NewBuffer(jsonData))
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

	if resp.StatusCode != http.StatusCreated {
		return nil, fmt.Errorf("failed to create project: %s", string(body))
	}

	var project Project
	if err := json.Unmarshal(body, &project); err != nil {
		return nil, fmt.Errorf("failed to parse response: %w", err)
	}

	return &project, nil
}

// getProjectAPI gets a project via the API
func getProjectAPI(serverURL, token, projectID string) (*Project, error) {
	ctx, cancel := context.WithTimeout(context.Background(), 10*time.Second)
	defer cancel()

	url := serverURL + "/api/v1/projects/" + projectID
	req, err := http.NewRequestWithContext(ctx, "GET", url, nil)
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
		return nil, fmt.Errorf("failed to get project: %s", string(body))
	}

	var project Project
	if err := json.Unmarshal(body, &project); err != nil {
		return nil, fmt.Errorf("failed to parse response: %w", err)
	}

	return &project, nil
}

// updateProjectAPI updates a project via the API
func updateProjectAPI(serverURL, token, projectID string, req UpdateProjectRequest) (*Project, error) {
	jsonData, err := json.Marshal(req)
	if err != nil {
		return nil, fmt.Errorf("failed to marshal request: %w", err)
	}

	ctx, cancel := context.WithTimeout(context.Background(), 30*time.Second)
	defer cancel()

	url := serverURL + "/api/v1/projects/" + projectID
	httpReq, err := http.NewRequestWithContext(ctx, "PUT", url, bytes.NewBuffer(jsonData))
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
		return nil, fmt.Errorf("failed to update project: %s", string(body))
	}

	var project Project
	if err := json.Unmarshal(body, &project); err != nil {
		return nil, fmt.Errorf("failed to parse response: %w", err)
	}

	return &project, nil
}

// deleteProjectAPI deletes a project via the API
func deleteProjectAPI(serverURL, token, projectID string) error {
	ctx, cancel := context.WithTimeout(context.Background(), 10*time.Second)
	defer cancel()

	url := serverURL + "/api/v1/projects/" + projectID
	req, err := http.NewRequestWithContext(ctx, "DELETE", url, nil)
	if err != nil {
		return fmt.Errorf("failed to create request: %w", err)
	}

	req.Header.Set("Authorization", "Bearer "+token)

	client := &http.Client{Timeout: 10 * time.Second}
	resp, err := client.Do(req)
	if err != nil {
		return fmt.Errorf("failed to send request: %w", err)
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusNoContent && resp.StatusCode != http.StatusOK {
		body, _ := io.ReadAll(resp.Body)
		return fmt.Errorf("failed to delete project: %s", string(body))
	}

	return nil
}

// getProjectMembersAPI gets project members via the API
func getProjectMembersAPI(serverURL, token, projectID string) ([]ProjectMember, error) {
	ctx, cancel := context.WithTimeout(context.Background(), 10*time.Second)
	defer cancel()

	url := serverURL + "/api/v1/projects/" + projectID + "/members"
	req, err := http.NewRequestWithContext(ctx, "GET", url, nil)
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
		return nil, fmt.Errorf("failed to get project members: %s", string(body))
	}

	var members []ProjectMember
	if err := json.Unmarshal(body, &members); err != nil {
		return nil, fmt.Errorf("failed to parse response: %w", err)
	}

	return members, nil
}

// addProjectMemberAPI adds a project member via the API
func addProjectMemberAPI(serverURL, token, projectID string, req AddMemberRequest) error {
	jsonData, err := json.Marshal(req)
	if err != nil {
		return fmt.Errorf("failed to marshal request: %w", err)
	}

	ctx, cancel := context.WithTimeout(context.Background(), 30*time.Second)
	defer cancel()

	url := serverURL + "/api/v1/projects/" + projectID + "/members"
	httpReq, err := http.NewRequestWithContext(ctx, "POST", url, bytes.NewBuffer(jsonData))
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

	if resp.StatusCode != http.StatusCreated && resp.StatusCode != http.StatusOK {
		return fmt.Errorf("failed to add project member: %s", string(body))
	}

	return nil
}

// removeProjectMemberAPI removes a project member via the API
func removeProjectMemberAPI(serverURL, token, projectID, userID string) error {
	ctx, cancel := context.WithTimeout(context.Background(), 10*time.Second)
	defer cancel()

	url := serverURL + "/api/v1/projects/" + projectID + "/members/" + userID
	req, err := http.NewRequestWithContext(ctx, "DELETE", url, nil)
	if err != nil {
		return fmt.Errorf("failed to create request: %w", err)
	}

	req.Header.Set("Authorization", "Bearer "+token)

	client := &http.Client{Timeout: 10 * time.Second}
	resp, err := client.Do(req)
	if err != nil {
		return fmt.Errorf("failed to send request: %w", err)
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusNoContent && resp.StatusCode != http.StatusOK {
		body, _ := io.ReadAll(resp.Body)
		return fmt.Errorf("failed to remove project member: %s", string(body))
	}

	return nil
}
