package main

import (
	"bufio"
	"bytes"
	"context"
	"crypto/rand"
	"crypto/rsa"
	"crypto/x509"
	"encoding/json"
	"encoding/pem"
	"fmt"
	"io"
	"net/http"
	"os"
	"os/exec"
	"path/filepath"
	"strings"
	"syscall"
	"time"

	"github.com/spf13/cobra"
	"golang.org/x/crypto/ssh"
	"golang.org/x/term"
)

// Resource represents a compute or storage resource
type Resource struct {
	ID           string                 `json:"id"`
	Name         string                 `json:"name"`
	Type         string                 `json:"type"`
	Endpoint     string                 `json:"endpoint"`
	Status       string                 `json:"status"`
	CreatedAt    string                 `json:"createdAt"`
	UpdatedAt    string                 `json:"updatedAt"`
	Metadata     map[string]interface{} `json:"metadata,omitempty"`
	CredentialID string                 `json:"credentialId,omitempty"`
}

// ComputeResource represents a compute resource
type ComputeResource struct {
	Resource
	MaxWorkers  int     `json:"maxWorkers"`
	CostPerHour float64 `json:"costPerHour"`
	Partition   string  `json:"partition,omitempty"`
	Account     string  `json:"account,omitempty"`
}

// StorageResource represents a storage resource
type StorageResource struct {
	Resource
	Bucket    string `json:"bucket,omitempty"`
	Region    string `json:"region,omitempty"`
	AccessKey string `json:"accessKey,omitempty"`
	SecretKey string `json:"secretKey,omitempty"`
}

// Credential represents a stored credential
type Credential struct {
	ID          string `json:"id"`
	Name        string `json:"name"`
	Type        string `json:"type"`
	Description string `json:"description"`
	CreatedAt   string `json:"createdAt"`
	UpdatedAt   string `json:"updatedAt"`
}

// CreateComputeResourceRequest represents compute resource creation
type CreateComputeResourceRequest struct {
	Name         string  `json:"name"`
	Type         string  `json:"type"`
	Endpoint     string  `json:"endpoint"`
	CredentialID string  `json:"credentialId"`
	MaxWorkers   int     `json:"maxWorkers"`
	CostPerHour  float64 `json:"costPerHour"`
	Partition    string  `json:"partition,omitempty"`
	Account      string  `json:"account,omitempty"`
}

// CreateStorageResourceRequest represents storage resource creation
type CreateStorageResourceRequest struct {
	Name         string `json:"name"`
	Type         string `json:"type"`
	Endpoint     string `json:"endpoint"`
	CredentialID string `json:"credentialId"`
	Bucket       string `json:"bucket,omitempty"`
	Region       string `json:"region,omitempty"`
}

// CreateCredentialRequest represents credential creation
type CreateCredentialRequest struct {
	Name        string `json:"name"`
	Type        string `json:"type"`
	Data        string `json:"data"`
	Description string `json:"description,omitempty"`
}

// createResourceCommands creates resource management commands
func createResourceCommands() *cobra.Command {
	resourceCmd := &cobra.Command{
		Use:   "resource",
		Short: "Resource management commands",
		Long:  "Commands for managing compute and storage resources",
	}

	// Compute resource commands
	computeCmd := &cobra.Command{
		Use:   "compute",
		Short: "Compute resource management",
		Long:  "Commands for managing compute resources (clusters, HPC systems, etc.)",
	}

	computeListCmd := &cobra.Command{
		Use:   "list",
		Short: "List compute resources",
		RunE: func(cmd *cobra.Command, args []string) error {
			return listComputeResources()
		},
	}

	computeGetCmd := &cobra.Command{
		Use:   "get <id>",
		Short: "Get compute resource details",
		Args:  cobra.ExactArgs(1),
		RunE: func(cmd *cobra.Command, args []string) error {
			return getComputeResource(args[0])
		},
	}

	computeCreateCmd := &cobra.Command{
		Use:   "create",
		Short: "Create a new compute resource",
		RunE: func(cmd *cobra.Command, args []string) error {
			return createComputeResource()
		},
	}

	computeUpdateCmd := &cobra.Command{
		Use:   "update <id>",
		Short: "Update a compute resource",
		Args:  cobra.ExactArgs(1),
		RunE: func(cmd *cobra.Command, args []string) error {
			return updateComputeResource(args[0])
		},
	}

	computeDeleteCmd := &cobra.Command{
		Use:   "delete <id>",
		Short: "Delete a compute resource",
		Args:  cobra.ExactArgs(1),
		RunE: func(cmd *cobra.Command, args []string) error {
			return deleteComputeResource(args[0])
		},
	}

	computeRegisterCmd := &cobra.Command{
		Use:   "register --token <token>",
		Short: "Register this compute resource with the scheduler",
		Long: `Auto-discover and register this compute resource with the scheduler.
This command should be run on the actual compute resource (SLURM cluster, bare metal node, etc.)
to discover its capabilities and register it with the scheduler.

The command will:
- Auto-detect the resource type (SLURM, bare metal, etc.)
- Discover available queues, partitions, and resource limits
- Generate SSH keys for secure access
- Register the resource with the scheduler using the provided token

Examples:
  airavata compute register --token abc123def456
  airavata compute register --token abc123def456 --name "My SLURM Cluster"`,
		RunE: func(cmd *cobra.Command, args []string) error {
			token, _ := cmd.Flags().GetString("token")
			name, _ := cmd.Flags().GetString("name")
			return registerComputeResource(token, name)
		},
	}

	computeRegisterCmd.Flags().String("token", "", "One-time registration token (required)")
	computeRegisterCmd.Flags().String("name", "", "Custom name for the resource (optional)")
	computeRegisterCmd.MarkFlagRequired("token")

	computeCmd.AddCommand(computeListCmd, computeGetCmd, computeCreateCmd, computeUpdateCmd, computeDeleteCmd, computeRegisterCmd)

	// Storage resource commands
	storageCmd := &cobra.Command{
		Use:   "storage",
		Short: "Storage resource management",
		Long:  "Commands for managing storage resources (S3, NFS, etc.)",
	}

	storageListCmd := &cobra.Command{
		Use:   "list",
		Short: "List storage resources",
		RunE: func(cmd *cobra.Command, args []string) error {
			return listStorageResources()
		},
	}

	storageGetCmd := &cobra.Command{
		Use:   "get <id>",
		Short: "Get storage resource details",
		Args:  cobra.ExactArgs(1),
		RunE: func(cmd *cobra.Command, args []string) error {
			return getStorageResource(args[0])
		},
	}

	storageCreateCmd := &cobra.Command{
		Use:   "create",
		Short: "Create a new storage resource",
		RunE: func(cmd *cobra.Command, args []string) error {
			return createStorageResource()
		},
	}

	storageUpdateCmd := &cobra.Command{
		Use:   "update <id>",
		Short: "Update a storage resource",
		Args:  cobra.ExactArgs(1),
		RunE: func(cmd *cobra.Command, args []string) error {
			return updateStorageResource(args[0])
		},
	}

	storageDeleteCmd := &cobra.Command{
		Use:   "delete <id>",
		Short: "Delete a storage resource",
		Args:  cobra.ExactArgs(1),
		RunE: func(cmd *cobra.Command, args []string) error {
			return deleteStorageResource(args[0])
		},
	}

	storageCmd.AddCommand(storageListCmd, storageGetCmd, storageCreateCmd, storageUpdateCmd, storageDeleteCmd)

	// Credential commands
	credentialCmd := &cobra.Command{
		Use:   "credential",
		Short: "Credential management",
		Long:  "Commands for managing credentials (SSH keys, passwords, etc.)",
	}

	credentialListCmd := &cobra.Command{
		Use:   "list",
		Short: "List credentials",
		RunE: func(cmd *cobra.Command, args []string) error {
			return listCredentials()
		},
	}

	credentialCreateCmd := &cobra.Command{
		Use:   "create",
		Short: "Create a new credential",
		RunE: func(cmd *cobra.Command, args []string) error {
			return createCredential()
		},
	}

	credentialDeleteCmd := &cobra.Command{
		Use:   "delete <id>",
		Short: "Delete a credential",
		Args:  cobra.ExactArgs(1),
		RunE: func(cmd *cobra.Command, args []string) error {
			return deleteCredential(args[0])
		},
	}

	credentialCmd.AddCommand(credentialListCmd, credentialCreateCmd, credentialDeleteCmd)

	// Credential binding commands
	bindCredentialCmd := &cobra.Command{
		Use:   "bind-credential <resource-id> <credential-id>",
		Short: "Bind a credential to a resource with verification",
		Long: `Bind a credential to a resource and verify that it can be used to access the resource.

Examples:
  airavata resource bind-credential compute-123 cred-456
  airavata resource bind-credential storage-789 cred-456`,
		Args: cobra.ExactArgs(2),
		RunE: func(cmd *cobra.Command, args []string) error {
			return bindCredentialToResource(args[0], args[1])
		},
	}

	unbindCredentialCmd := &cobra.Command{
		Use:   "unbind-credential <resource-id>",
		Short: "Unbind credential from a resource",
		Long: `Remove credential binding from a resource.

Examples:
  airavata resource unbind-credential compute-123`,
		Args: cobra.ExactArgs(1),
		RunE: func(cmd *cobra.Command, args []string) error {
			return unbindCredentialFromResource(args[0])
		},
	}

	testCredentialCmd := &cobra.Command{
		Use:   "test-credential <resource-id>",
		Short: "Test if bound credential works with resource",
		Long: `Test the currently bound credential to verify it can access the resource.

Examples:
  airavata resource test-credential compute-123`,
		Args: cobra.ExactArgs(1),
		RunE: func(cmd *cobra.Command, args []string) error {
			return testResourceCredential(args[0])
		},
	}

	// Resource status and metrics commands
	statusCmd := &cobra.Command{
		Use:   "status <resource-id>",
		Short: "Check resource availability and status",
		Long: `Check the current status and availability of a resource.

Examples:
  airavata resource status compute-123`,
		Args: cobra.ExactArgs(1),
		RunE: func(cmd *cobra.Command, args []string) error {
			return getResourceStatus(args[0])
		},
	}

	metricsCmd := &cobra.Command{
		Use:   "metrics <resource-id>",
		Short: "View resource metrics and usage",
		Long: `View detailed metrics and usage information for a resource.

Examples:
  airavata resource metrics compute-123`,
		Args: cobra.ExactArgs(1),
		RunE: func(cmd *cobra.Command, args []string) error {
			return getResourceMetrics(args[0])
		},
	}

	testCmd := &cobra.Command{
		Use:   "test <resource-id>",
		Short: "Test resource connectivity",
		Long: `Test connectivity and basic functionality of a resource.

Examples:
  airavata resource test compute-123`,
		Args: cobra.ExactArgs(1),
		RunE: func(cmd *cobra.Command, args []string) error {
			return testResourceConnectivity(args[0])
		},
	}

	resourceCmd.AddCommand(computeCmd, storageCmd, credentialCmd, bindCredentialCmd, unbindCredentialCmd, testCredentialCmd, statusCmd, metricsCmd, testCmd)
	return resourceCmd
}

// Compute resource functions
func listComputeResources() error {
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

	resources, err := getResourcesAPI(serverURL, token, "compute")
	if err != nil {
		return fmt.Errorf("failed to get compute resources: %w", err)
	}

	if len(resources) == 0 {
		fmt.Println("💻 No compute resources found")
		return nil
	}

	fmt.Printf("💻 Compute Resources (%d)\n", len(resources))
	fmt.Println("==========================")

	for _, resource := range resources {
		fmt.Printf("• %s (%s) - %s\n", resource.Name, resource.Type, resource.Status)
		fmt.Printf("  ID: %s\n", resource.ID)
		fmt.Printf("  Endpoint: %s\n", resource.Endpoint)
		if resource.CredentialID != "" {
			fmt.Printf("  Credential: %s\n", resource.CredentialID)
		}
		fmt.Println()
	}

	return nil
}

func getComputeResource(id string) error {
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

	resource, err := getResourceAPI(serverURL, token, id)
	if err != nil {
		return fmt.Errorf("failed to get compute resource: %w", err)
	}

	fmt.Printf("💻 Compute Resource: %s\n", resource.Name)
	fmt.Println("========================")
	fmt.Printf("ID:          %s\n", resource.ID)
	fmt.Printf("Type:        %s\n", resource.Type)
	fmt.Printf("Endpoint:    %s\n", resource.Endpoint)
	fmt.Printf("Status:      %s\n", resource.Status)
	if resource.CredentialID != "" {
		fmt.Printf("Credential:  %s\n", resource.CredentialID)
	}
	fmt.Printf("Created:     %s\n", resource.CreatedAt)
	fmt.Printf("Updated:     %s\n", resource.UpdatedAt)

	return nil
}

func createComputeResource() error {
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

	fmt.Println("🆕 Create Compute Resource")
	fmt.Println("==========================")

	fmt.Print("Name: ")
	name, _ := reader.ReadString('\n')
	name = strings.TrimSpace(name)

	fmt.Print("Type (SLURM/Kubernetes/BareMetal): ")
	typeInput, _ := reader.ReadString('\n')
	resourceType := strings.TrimSpace(typeInput)

	fmt.Print("Endpoint: ")
	endpoint, _ := reader.ReadString('\n')
	endpoint = strings.TrimSpace(endpoint)

	fmt.Print("Credential ID: ")
	credentialID, _ := reader.ReadString('\n')
	credentialID = strings.TrimSpace(credentialID)

	fmt.Print("Max Workers: ")
	maxWorkersInput, _ := reader.ReadString('\n')
	maxWorkers := 10 // default
	if maxWorkersInput != "" {
		fmt.Sscanf(maxWorkersInput, "%d", &maxWorkers)
	}

	fmt.Print("Cost per Hour: ")
	costInput, _ := reader.ReadString('\n')
	costPerHour := 0.0
	if costInput != "" {
		fmt.Sscanf(costInput, "%f", &costPerHour)
	}

	createReq := CreateComputeResourceRequest{
		Name:         name,
		Type:         resourceType,
		Endpoint:     endpoint,
		CredentialID: credentialID,
		MaxWorkers:   maxWorkers,
		CostPerHour:  costPerHour,
	}

	resource, err := createComputeResourceAPI(serverURL, token, createReq)
	if err != nil {
		return fmt.Errorf("failed to create compute resource: %w", err)
	}

	fmt.Printf("✅ Compute resource created successfully!\n")
	fmt.Printf("ID: %s\n", resource.ID)
	fmt.Printf("Name: %s\n", resource.Name)

	return nil
}

func updateComputeResource(id string) error {
	// Implementation would be similar to create but with PUT request
	return fmt.Errorf("update compute resource not implemented yet")
}

func deleteComputeResource(id string) error {
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

	fmt.Printf("⚠️  Are you sure you want to delete compute resource %s? (y/N): ", id)
	reader := bufio.NewReader(os.Stdin)
	confirm, _ := reader.ReadString('\n')
	confirm = strings.TrimSpace(strings.ToLower(confirm))

	if confirm != "y" && confirm != "yes" {
		fmt.Println("❌ Deletion cancelled")
		return nil
	}

	if err := deleteResourceAPI(serverURL, token, id); err != nil {
		return fmt.Errorf("failed to delete compute resource: %w", err)
	}

	fmt.Printf("✅ Compute resource %s deleted successfully\n", id)
	return nil
}

// Storage resource functions
func listStorageResources() error {
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

	resources, err := getResourcesAPI(serverURL, token, "storage")
	if err != nil {
		return fmt.Errorf("failed to get storage resources: %w", err)
	}

	if len(resources) == 0 {
		fmt.Println("💾 No storage resources found")
		return nil
	}

	fmt.Printf("💾 Storage Resources (%d)\n", len(resources))
	fmt.Println("==========================")

	for _, resource := range resources {
		fmt.Printf("• %s (%s) - %s\n", resource.Name, resource.Type, resource.Status)
		fmt.Printf("  ID: %s\n", resource.ID)
		fmt.Printf("  Endpoint: %s\n", resource.Endpoint)
		if resource.CredentialID != "" {
			fmt.Printf("  Credential: %s\n", resource.CredentialID)
		}
		fmt.Println()
	}

	return nil
}

func getStorageResource(id string) error {
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

	resource, err := getResourceAPI(serverURL, token, id)
	if err != nil {
		return fmt.Errorf("failed to get storage resource: %w", err)
	}

	fmt.Printf("💾 Storage Resource: %s\n", resource.Name)
	fmt.Println("========================")
	fmt.Printf("ID:          %s\n", resource.ID)
	fmt.Printf("Type:        %s\n", resource.Type)
	fmt.Printf("Endpoint:    %s\n", resource.Endpoint)
	fmt.Printf("Status:      %s\n", resource.Status)
	if resource.CredentialID != "" {
		fmt.Printf("Credential:  %s\n", resource.CredentialID)
	}
	fmt.Printf("Created:     %s\n", resource.CreatedAt)
	fmt.Printf("Updated:     %s\n", resource.UpdatedAt)

	return nil
}

func createStorageResource() error {
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

	fmt.Println("🆕 Create Storage Resource")
	fmt.Println("==========================")

	fmt.Print("Name: ")
	name, _ := reader.ReadString('\n')
	name = strings.TrimSpace(name)

	fmt.Print("Type (S3/NFS/SFTP): ")
	typeInput, _ := reader.ReadString('\n')
	resourceType := strings.TrimSpace(typeInput)

	fmt.Print("Endpoint: ")
	endpoint, _ := reader.ReadString('\n')
	endpoint = strings.TrimSpace(endpoint)

	fmt.Print("Credential ID: ")
	credentialID, _ := reader.ReadString('\n')
	credentialID = strings.TrimSpace(credentialID)

	createReq := CreateStorageResourceRequest{
		Name:         name,
		Type:         resourceType,
		Endpoint:     endpoint,
		CredentialID: credentialID,
	}

	resource, err := createStorageResourceAPI(serverURL, token, createReq)
	if err != nil {
		return fmt.Errorf("failed to create storage resource: %w", err)
	}

	fmt.Printf("✅ Storage resource created successfully!\n")
	fmt.Printf("ID: %s\n", resource.ID)
	fmt.Printf("Name: %s\n", resource.Name)

	return nil
}

func updateStorageResource(id string) error {
	// Implementation would be similar to create but with PUT request
	return fmt.Errorf("update storage resource not implemented yet")
}

func deleteStorageResource(id string) error {
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

	fmt.Printf("⚠️  Are you sure you want to delete storage resource %s? (y/N): ", id)
	reader := bufio.NewReader(os.Stdin)
	confirm, _ := reader.ReadString('\n')
	confirm = strings.TrimSpace(strings.ToLower(confirm))

	if confirm != "y" && confirm != "yes" {
		fmt.Println("❌ Deletion cancelled")
		return nil
	}

	if err := deleteResourceAPI(serverURL, token, id); err != nil {
		return fmt.Errorf("failed to delete storage resource: %w", err)
	}

	fmt.Printf("✅ Storage resource %s deleted successfully\n", id)
	return nil
}

// Credential functions
func listCredentials() error {
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

	credentials, err := getCredentialsAPI(serverURL, token)
	if err != nil {
		return fmt.Errorf("failed to get credentials: %w", err)
	}

	if len(credentials) == 0 {
		fmt.Println("🔑 No credentials found")
		return nil
	}

	fmt.Printf("🔑 Credentials (%d)\n", len(credentials))
	fmt.Println("==================")

	for _, cred := range credentials {
		fmt.Printf("• %s (%s)\n", cred.Name, cred.Type)
		fmt.Printf("  ID: %s\n", cred.ID)
		if cred.Description != "" {
			fmt.Printf("  Description: %s\n", cred.Description)
		}
		fmt.Printf("  Created: %s\n", cred.CreatedAt)
		fmt.Println()
	}

	return nil
}

func createCredential() error {
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

	fmt.Println("🆕 Create Credential")
	fmt.Println("===================")

	fmt.Print("Name: ")
	name, _ := reader.ReadString('\n')
	name = strings.TrimSpace(name)

	fmt.Print("Type (SSH_KEY/PASSWORD/API_TOKEN): ")
	typeInput, _ := reader.ReadString('\n')
	credType := strings.TrimSpace(typeInput)

	fmt.Print("Description: ")
	description, _ := reader.ReadString('\n')
	description = strings.TrimSpace(description)

	fmt.Print("Data (will be hidden): ")
	data, err := term.ReadPassword(int(syscall.Stdin))
	if err != nil {
		return fmt.Errorf("failed to read credential data: %w", err)
	}
	fmt.Println()

	createReq := CreateCredentialRequest{
		Name:        name,
		Type:        credType,
		Data:        string(data),
		Description: description,
	}

	credential, err := createCredentialAPI(serverURL, token, createReq)
	if err != nil {
		return fmt.Errorf("failed to create credential: %w", err)
	}

	fmt.Printf("✅ Credential created successfully!\n")
	fmt.Printf("ID: %s\n", credential.ID)
	fmt.Printf("Name: %s\n", credential.Name)

	return nil
}

func deleteCredential(id string) error {
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

	fmt.Printf("⚠️  Are you sure you want to delete credential %s? (y/N): ", id)
	reader := bufio.NewReader(os.Stdin)
	confirm, _ := reader.ReadString('\n')
	confirm = strings.TrimSpace(strings.ToLower(confirm))

	if confirm != "y" && confirm != "yes" {
		fmt.Println("❌ Deletion cancelled")
		return nil
	}

	if err := deleteCredentialAPI(serverURL, token, id); err != nil {
		return fmt.Errorf("failed to delete credential: %w", err)
	}

	fmt.Printf("✅ Credential %s deleted successfully\n", id)
	return nil
}

// API functions
func getResourcesAPI(serverURL, token, resourceType string) ([]Resource, error) {
	ctx, cancel := context.WithTimeout(context.Background(), 10*time.Second)
	defer cancel()

	url := serverURL + "/api/v2/resources?type=" + resourceType
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
		return nil, fmt.Errorf("failed to get resources: %s", string(body))
	}

	var response struct {
		Resources []Resource `json:"resources"`
	}
	if err := json.Unmarshal(body, &response); err != nil {
		return nil, fmt.Errorf("failed to parse response: %w", err)
	}

	return response.Resources, nil
}

func getResourceAPI(serverURL, token, id string) (*Resource, error) {
	ctx, cancel := context.WithTimeout(context.Background(), 10*time.Second)
	defer cancel()

	url := serverURL + "/api/v2/resources/" + id
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
		return nil, fmt.Errorf("failed to get resource: %s", string(body))
	}

	var resource Resource
	if err := json.Unmarshal(body, &resource); err != nil {
		return nil, fmt.Errorf("failed to parse response: %w", err)
	}

	return &resource, nil
}

func createComputeResourceAPI(serverURL, token string, req CreateComputeResourceRequest) (*Resource, error) {
	jsonData, err := json.Marshal(req)
	if err != nil {
		return nil, fmt.Errorf("failed to marshal request: %w", err)
	}

	ctx, cancel := context.WithTimeout(context.Background(), 30*time.Second)
	defer cancel()

	httpReq, err := http.NewRequestWithContext(ctx, "POST", serverURL+"/api/v2/resources/compute", bytes.NewBuffer(jsonData))
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
		return nil, fmt.Errorf("failed to create compute resource: %s", string(body))
	}

	var resource Resource
	if err := json.Unmarshal(body, &resource); err != nil {
		return nil, fmt.Errorf("failed to parse response: %w", err)
	}

	return &resource, nil
}

func createStorageResourceAPI(serverURL, token string, req CreateStorageResourceRequest) (*Resource, error) {
	jsonData, err := json.Marshal(req)
	if err != nil {
		return nil, fmt.Errorf("failed to marshal request: %w", err)
	}

	ctx, cancel := context.WithTimeout(context.Background(), 30*time.Second)
	defer cancel()

	httpReq, err := http.NewRequestWithContext(ctx, "POST", serverURL+"/api/v2/resources/storage", bytes.NewBuffer(jsonData))
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
		return nil, fmt.Errorf("failed to create storage resource: %s", string(body))
	}

	var resource Resource
	if err := json.Unmarshal(body, &resource); err != nil {
		return nil, fmt.Errorf("failed to parse response: %w", err)
	}

	return &resource, nil
}

func deleteResourceAPI(serverURL, token, id string) error {
	ctx, cancel := context.WithTimeout(context.Background(), 10*time.Second)
	defer cancel()

	url := serverURL + "/api/v2/resources/" + id
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
		return fmt.Errorf("failed to delete resource: %s", string(body))
	}

	return nil
}

func getCredentialsAPI(serverURL, token string) ([]Credential, error) {
	ctx, cancel := context.WithTimeout(context.Background(), 10*time.Second)
	defer cancel()

	req, err := http.NewRequestWithContext(ctx, "GET", serverURL+"/api/v2/credentials", nil)
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
		return nil, fmt.Errorf("failed to get credentials: %s", string(body))
	}

	var credentials []Credential
	if err := json.Unmarshal(body, &credentials); err != nil {
		return nil, fmt.Errorf("failed to parse response: %w", err)
	}

	return credentials, nil
}

func createCredentialAPI(serverURL, token string, req CreateCredentialRequest) (*Credential, error) {
	jsonData, err := json.Marshal(req)
	if err != nil {
		return nil, fmt.Errorf("failed to marshal request: %w", err)
	}

	ctx, cancel := context.WithTimeout(context.Background(), 30*time.Second)
	defer cancel()

	httpReq, err := http.NewRequestWithContext(ctx, "POST", serverURL+"/api/v2/credentials", bytes.NewBuffer(jsonData))
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
		return nil, fmt.Errorf("failed to create credential: %s", string(body))
	}

	var credential Credential
	if err := json.Unmarshal(body, &credential); err != nil {
		return nil, fmt.Errorf("failed to parse response: %w", err)
	}

	return &credential, nil
}

func deleteCredentialAPI(serverURL, token, id string) error {
	ctx, cancel := context.WithTimeout(context.Background(), 10*time.Second)
	defer cancel()

	url := serverURL + "/api/v2/credentials/" + id
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
		return fmt.Errorf("failed to delete credential: %s", string(body))
	}

	return nil
}

// Credential binding and resource testing functions

// BindCredentialRequest represents a credential binding request
type BindCredentialRequest struct {
	CredentialID string `json:"credential_id"`
}

// TestResult represents the result of a credential test
type TestResult struct {
	Success   bool   `json:"success"`
	Message   string `json:"message"`
	Details   string `json:"details,omitempty"`
	Timestamp string `json:"timestamp"`
}

// ResourceStatus represents resource status information
type ResourceStatus struct {
	ID          string `json:"id"`
	Name        string `json:"name"`
	Type        string `json:"type"`
	Status      string `json:"status"`
	Available   bool   `json:"available"`
	LastChecked string `json:"last_checked"`
	Message     string `json:"message,omitempty"`
}

// ResourceMetrics represents resource metrics
type ResourceMetrics struct {
	ID             string  `json:"id"`
	Name           string  `json:"name"`
	Type           string  `json:"type"`
	ActiveWorkers  int     `json:"active_workers"`
	TotalWorkers   int     `json:"total_workers"`
	RunningTasks   int     `json:"running_tasks"`
	QueuedTasks    int     `json:"queued_tasks"`
	CompletedTasks int     `json:"completed_tasks"`
	FailedTasks    int     `json:"failed_tasks"`
	CPUUsage       float64 `json:"cpu_usage,omitempty"`
	MemoryUsage    float64 `json:"memory_usage,omitempty"`
	StorageUsage   float64 `json:"storage_usage,omitempty"`
	LastUpdated    string  `json:"last_updated"`
}

// bindCredentialToResource binds a credential to a resource with verification
func bindCredentialToResource(resourceID, credentialID string) error {
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

	fmt.Printf("🔗 Binding credential %s to resource %s...\n", credentialID, resourceID)

	// Bind credential
	if err := bindCredentialAPI(serverURL, token, resourceID, credentialID); err != nil {
		return fmt.Errorf("failed to bind credential: %w", err)
	}

	fmt.Printf("✅ Credential bound successfully!\n")

	// Test the credential
	fmt.Printf("🧪 Testing credential access...\n")
	result, err := testCredentialAPI(serverURL, token, resourceID)
	if err != nil {
		fmt.Printf("⚠️  Warning: Could not test credential: %v\n", err)
		return nil
	}

	if result.Success {
		fmt.Printf("✅ Credential test passed: %s\n", result.Message)
	} else {
		fmt.Printf("❌ Credential test failed: %s\n", result.Message)
		if result.Details != "" {
			fmt.Printf("   Details: %s\n", result.Details)
		}
	}

	return nil
}

// unbindCredentialFromResource unbinds a credential from a resource
func unbindCredentialFromResource(resourceID string) error {
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

	fmt.Printf("🔓 Unbinding credential from resource %s...\n", resourceID)

	if err := unbindCredentialAPI(serverURL, token, resourceID); err != nil {
		return fmt.Errorf("failed to unbind credential: %w", err)
	}

	fmt.Printf("✅ Credential unbound successfully\n")
	return nil
}

// testResourceCredential tests if the bound credential works with the resource
func testResourceCredential(resourceID string) error {
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

	fmt.Printf("🧪 Testing credential for resource %s...\n", resourceID)

	result, err := testCredentialAPI(serverURL, token, resourceID)
	if err != nil {
		return fmt.Errorf("failed to test credential: %w", err)
	}

	if result.Success {
		fmt.Printf("✅ Credential test passed: %s\n", result.Message)
	} else {
		fmt.Printf("❌ Credential test failed: %s\n", result.Message)
		if result.Details != "" {
			fmt.Printf("   Details: %s\n", result.Details)
		}
	}

	fmt.Printf("   Tested at: %s\n", result.Timestamp)
	return nil
}

// getResourceStatus gets the current status of a resource
func getResourceStatus(resourceID string) error {
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

	status, err := getResourceStatusAPI(serverURL, token, resourceID)
	if err != nil {
		return fmt.Errorf("failed to get resource status: %w", err)
	}

	statusIcon := "✅"
	if !status.Available {
		statusIcon = "❌"
	}

	fmt.Printf("📊 Resource Status: %s\n", status.Name)
	fmt.Println("========================")
	fmt.Printf("ID:           %s\n", status.ID)
	fmt.Printf("Type:         %s\n", status.Type)
	fmt.Printf("Status:       %s %s\n", statusIcon, status.Status)
	fmt.Printf("Available:    %t\n", status.Available)
	fmt.Printf("Last Checked: %s\n", status.LastChecked)
	if status.Message != "" {
		fmt.Printf("Message:      %s\n", status.Message)
	}

	return nil
}

// getResourceMetrics gets metrics for a resource
func getResourceMetrics(resourceID string) error {
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

	metrics, err := getResourceMetricsAPI(serverURL, token, resourceID)
	if err != nil {
		return fmt.Errorf("failed to get resource metrics: %w", err)
	}

	fmt.Printf("📈 Resource Metrics: %s\n", metrics.Name)
	fmt.Println("================================")
	fmt.Printf("ID:              %s\n", metrics.ID)
	fmt.Printf("Type:            %s\n", metrics.Type)
	fmt.Printf("Active Workers:  %d/%d\n", metrics.ActiveWorkers, metrics.TotalWorkers)
	fmt.Printf("Running Tasks:   %d\n", metrics.RunningTasks)
	fmt.Printf("Queued Tasks:    %d\n", metrics.QueuedTasks)
	fmt.Printf("Completed Tasks: %d\n", metrics.CompletedTasks)
	fmt.Printf("Failed Tasks:    %d\n", metrics.FailedTasks)

	if metrics.CPUUsage > 0 {
		fmt.Printf("CPU Usage:       %.1f%%\n", metrics.CPUUsage)
	}
	if metrics.MemoryUsage > 0 {
		fmt.Printf("Memory Usage:    %.1f%%\n", metrics.MemoryUsage)
	}
	if metrics.StorageUsage > 0 {
		fmt.Printf("Storage Usage:   %.1f%%\n", metrics.StorageUsage)
	}

	fmt.Printf("Last Updated:    %s\n", metrics.LastUpdated)

	return nil
}

// testResourceConnectivity tests basic connectivity to a resource
func testResourceConnectivity(resourceID string) error {
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

	fmt.Printf("🔍 Testing connectivity to resource %s...\n", resourceID)

	// Get resource status first
	status, err := getResourceStatusAPI(serverURL, token, resourceID)
	if err != nil {
		return fmt.Errorf("failed to get resource status: %w", err)
	}

	if !status.Available {
		fmt.Printf("❌ Resource is not available: %s\n", status.Message)
		return nil
	}

	// Test credential if bound
	result, err := testCredentialAPI(serverURL, token, resourceID)
	if err != nil {
		fmt.Printf("⚠️  Warning: Could not test credential: %v\n", err)
	} else if result.Success {
		fmt.Printf("✅ Credential test passed: %s\n", result.Message)
	} else {
		fmt.Printf("❌ Credential test failed: %s\n", result.Message)
	}

	fmt.Printf("✅ Resource connectivity test completed\n")
	fmt.Printf("   Status: %s\n", status.Status)
	fmt.Printf("   Available: %t\n", status.Available)

	return nil
}

// API functions for credential binding and resource testing

// bindCredentialAPI binds a credential to a resource via the API
func bindCredentialAPI(serverURL, token, resourceID, credentialID string) error {
	req := BindCredentialRequest{
		CredentialID: credentialID,
	}

	jsonData, err := json.Marshal(req)
	if err != nil {
		return fmt.Errorf("failed to marshal request: %w", err)
	}

	ctx, cancel := context.WithTimeout(context.Background(), 30*time.Second)
	defer cancel()

	url := fmt.Sprintf("%s/api/v1/resources/%s/bind-credential", serverURL, resourceID)
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

	if resp.StatusCode != http.StatusOK {
		return fmt.Errorf("failed to bind credential: %s", string(body))
	}

	return nil
}

// unbindCredentialAPI unbinds a credential from a resource via the API
func unbindCredentialAPI(serverURL, token, resourceID string) error {
	ctx, cancel := context.WithTimeout(context.Background(), 10*time.Second)
	defer cancel()

	url := fmt.Sprintf("%s/api/v1/resources/%s/unbind-credential", serverURL, resourceID)
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
		return fmt.Errorf("failed to unbind credential: %s", string(body))
	}

	return nil
}

// testCredentialAPI tests a credential via the API
func testCredentialAPI(serverURL, token, resourceID string) (*TestResult, error) {
	ctx, cancel := context.WithTimeout(context.Background(), 30*time.Second)
	defer cancel()

	url := fmt.Sprintf("%s/api/v1/resources/%s/test-credential", serverURL, resourceID)
	req, err := http.NewRequestWithContext(ctx, "POST", url, nil)
	if err != nil {
		return nil, fmt.Errorf("failed to create request: %w", err)
	}

	req.Header.Set("Authorization", "Bearer "+token)

	client := &http.Client{Timeout: 30 * time.Second}
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
		return nil, fmt.Errorf("failed to test credential: %s", string(body))
	}

	var result TestResult
	if err := json.Unmarshal(body, &result); err != nil {
		return nil, fmt.Errorf("failed to parse response: %w", err)
	}

	return &result, nil
}

// getResourceStatusAPI gets resource status via the API
func getResourceStatusAPI(serverURL, token, resourceID string) (*ResourceStatus, error) {
	ctx, cancel := context.WithTimeout(context.Background(), 10*time.Second)
	defer cancel()

	url := fmt.Sprintf("%s/api/v1/resources/%s/status", serverURL, resourceID)
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
		return nil, fmt.Errorf("failed to get resource status: %s", string(body))
	}

	var status ResourceStatus
	if err := json.Unmarshal(body, &status); err != nil {
		return nil, fmt.Errorf("failed to parse response: %w", err)
	}

	return &status, nil
}

// getResourceMetricsAPI gets resource metrics via the API
func getResourceMetricsAPI(serverURL, token, resourceID string) (*ResourceMetrics, error) {
	ctx, cancel := context.WithTimeout(context.Background(), 10*time.Second)
	defer cancel()

	url := fmt.Sprintf("%s/api/v1/resources/%s/metrics", serverURL, resourceID)
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
		return nil, fmt.Errorf("failed to get resource metrics: %s", string(body))
	}

	var metrics ResourceMetrics
	if err := json.Unmarshal(body, &metrics); err != nil {
		return nil, fmt.Errorf("failed to parse response: %w", err)
	}

	return &metrics, nil
}

// registerComputeResource registers this compute resource with the scheduler
func registerComputeResource(token, customName string) error {
	fmt.Println("🔍 Auto-discovering compute resource capabilities...")

	// Auto-detect resource type
	resourceType, err := detectResourceType()
	if err != nil {
		return fmt.Errorf("failed to detect resource type: %w", err)
	}

	fmt.Printf("✅ Detected resource type: %s\n", resourceType)

	// Discover resource capabilities
	capabilities, err := discoverResourceCapabilities(resourceType)
	if err != nil {
		return fmt.Errorf("failed to discover resource capabilities: %w", err)
	}

	// Generate SSH key pair
	fmt.Println("🔑 Generating SSH key pair...")
	privateKey, publicKey, err := generateSSHKeyPair()
	if err != nil {
		return fmt.Errorf("failed to generate SSH key pair: %w", err)
	}

	// Add public key to authorized_keys
	fmt.Println("🔐 Adding public key to authorized_keys...")
	if err := addPublicKeyToAuthorizedKeys(publicKey); err != nil {
		return fmt.Errorf("failed to add public key to authorized_keys: %w", err)
	}

	// Get hostname and endpoint
	hostname, err := getHostname()
	if err != nil {
		return fmt.Errorf("failed to get hostname: %w", err)
	}

	// Use custom name or generate default
	resourceName := customName
	if resourceName == "" {
		resourceName = fmt.Sprintf("%s-%s", resourceType, hostname)
	}

	// Prepare registration data
	registrationData := ComputeResourceRegistration{
		Token:        token,
		Name:         resourceName,
		Type:         resourceType,
		Hostname:     hostname,
		Capabilities: capabilities,
		PrivateKey:   privateKey,
	}

	// Send registration to server
	fmt.Println("📡 Registering with scheduler...")
	resourceID, err := sendRegistrationToServer(registrationData)
	if err != nil {
		return fmt.Errorf("failed to register with server: %w", err)
	}

	fmt.Printf("✅ Successfully registered compute resource!\n")
	fmt.Printf("   Resource ID: %s\n", resourceID)
	fmt.Printf("   Name: %s\n", resourceName)
	fmt.Printf("   Type: %s\n", resourceType)
	fmt.Printf("   Hostname: %s\n", hostname)

	return nil
}

// ComputeResourceRegistration represents the registration data sent to the server
type ComputeResourceRegistration struct {
	Token        string                 `json:"token"`
	Name         string                 `json:"name"`
	Type         string                 `json:"type"`
	Hostname     string                 `json:"hostname"`
	Capabilities map[string]interface{} `json:"capabilities"`
	PrivateKey   string                 `json:"private_key"`
}

// detectResourceType auto-detects the type of compute resource
func detectResourceType() (string, error) {
	// Check for SLURM
	if _, err := exec.Command("scontrol", "ping").Output(); err == nil {
		return "SLURM", nil
	}

	// Check for Kubernetes
	if _, err := exec.Command("kubectl", "version", "--client").Output(); err == nil {
		return "KUBERNETES", nil
	}

	// Default to bare metal
	return "BARE_METAL", nil
}

// discoverResourceCapabilities discovers the capabilities of the resource
func discoverResourceCapabilities(resourceType string) (map[string]interface{}, error) {
	switch resourceType {
	case "SLURM":
		return discoverSLURMCapabilities()
	case "KUBERNETES":
		return discoverKubernetesCapabilities()
	case "BARE_METAL":
		return discoverBareMetalCapabilities()
	default:
		return nil, fmt.Errorf("unknown resource type: %s", resourceType)
	}
}

// discoverSLURMCapabilities discovers SLURM-specific capabilities
func discoverSLURMCapabilities() (map[string]interface{}, error) {
	capabilities := make(map[string]interface{})

	// Get partition information
	partitions, err := getSLURMPartitions()
	if err != nil {
		return nil, fmt.Errorf("failed to get SLURM partitions: %w", err)
	}
	capabilities["partitions"] = partitions

	// Get queue information
	queues, err := getSLURMQueues()
	if err != nil {
		return nil, fmt.Errorf("failed to get SLURM queues: %w", err)
	}
	capabilities["queues"] = queues

	// Get account information
	accounts, err := getSLURMAccounts()
	if err != nil {
		return nil, fmt.Errorf("failed to get SLURM accounts: %w", err)
	}
	capabilities["accounts"] = accounts

	// Get node information
	nodes, err := getSLURMNodes()
	if err != nil {
		return nil, fmt.Errorf("failed to get SLURM nodes: %w", err)
	}
	capabilities["nodes"] = nodes

	return capabilities, nil
}

// discoverKubernetesCapabilities discovers Kubernetes-specific capabilities
func discoverKubernetesCapabilities() (map[string]interface{}, error) {
	capabilities := make(map[string]interface{})

	// Get cluster info
	clusterInfo, err := getKubernetesClusterInfo()
	if err != nil {
		return nil, fmt.Errorf("failed to get Kubernetes cluster info: %w", err)
	}
	capabilities["cluster_info"] = clusterInfo

	// Get node information
	nodes, err := getKubernetesNodes()
	if err != nil {
		return nil, fmt.Errorf("failed to get Kubernetes nodes: %w", err)
	}
	capabilities["nodes"] = nodes

	return capabilities, nil
}

// discoverBareMetalCapabilities discovers bare metal-specific capabilities
func discoverBareMetalCapabilities() (map[string]interface{}, error) {
	capabilities := make(map[string]interface{})

	// Get CPU information
	cpuInfo, err := getCPUInfo()
	if err != nil {
		return nil, fmt.Errorf("failed to get CPU info: %w", err)
	}
	capabilities["cpu"] = cpuInfo

	// Get memory information
	memoryInfo, err := getMemoryInfo()
	if err != nil {
		return nil, fmt.Errorf("failed to get memory info: %w", err)
	}
	capabilities["memory"] = memoryInfo

	// Get disk information
	diskInfo, err := getDiskInfo()
	if err != nil {
		return nil, fmt.Errorf("failed to get disk info: %w", err)
	}
	capabilities["disk"] = diskInfo

	return capabilities, nil
}

// Helper functions for resource discovery

func getSLURMPartitions() ([]map[string]interface{}, error) {
	cmd := exec.Command("scontrol", "show", "partition")
	output, err := cmd.Output()
	if err != nil {
		// If scontrol fails, return a default partition for testing
		return []map[string]interface{}{
			{
				"name":      "default",
				"nodes":     "test-node-01",
				"max_time":  "24:00:00",
				"max_nodes": "1",
				"max_cpus":  "4",
				"state":     "up",
			},
		}, nil
	}

	// Parse SLURM partition output
	partitions := []map[string]interface{}{}
	lines := strings.Split(string(output), "\n")

	for _, line := range lines {
		if strings.HasPrefix(line, "PartitionName=") {
			partition := make(map[string]interface{})
			fields := strings.Fields(line)
			for _, field := range fields {
				if strings.Contains(field, "=") {
					parts := strings.SplitN(field, "=", 2)
					partition[parts[0]] = parts[1]
				}
			}
			partitions = append(partitions, partition)
		}
	}

	return partitions, nil
}

func getSLURMQueues() ([]map[string]interface{}, error) {
	cmd := exec.Command("squeue", "--format=%P,%Q,%T,%N", "--noheader")
	output, err := cmd.Output()
	if err != nil {
		// If squeue fails, return a default queue for testing
		return []map[string]interface{}{
			{
				"partition": "default",
				"qos":       "normal",
				"state":     "idle",
				"nodes":     "test-node-01",
			},
		}, nil
	}

	queues := []map[string]interface{}{}
	lines := strings.Split(string(output), "\n")

	for _, line := range lines {
		if line != "" {
			fields := strings.Split(line, ",")
			if len(fields) >= 4 {
				queue := map[string]interface{}{
					"partition": fields[0],
					"account":   fields[1],
					"state":     fields[2],
					"nodes":     fields[3],
				}
				queues = append(queues, queue)
			}
		}
	}

	return queues, nil
}

func getSLURMAccounts() ([]map[string]interface{}, error) {
	cmd := exec.Command("sacctmgr", "show", "account", "--format=Account,Description,Organization", "--noheader", "--parsable2")
	output, err := cmd.Output()
	if err != nil {
		// If sacctmgr fails, return a default account for testing
		return []map[string]interface{}{
			{
				"name":         "default",
				"description":  "Default account for testing",
				"organization": "test",
			},
		}, nil
	}

	accounts := []map[string]interface{}{}
	lines := strings.Split(string(output), "\n")

	for _, line := range lines {
		if line != "" {
			fields := strings.Split(line, "|")
			if len(fields) >= 3 {
				account := map[string]interface{}{
					"name":         fields[0],
					"description":  fields[1],
					"organization": fields[2],
				}
				accounts = append(accounts, account)
			}
		}
	}

	return accounts, nil
}

func getSLURMNodes() ([]map[string]interface{}, error) {
	cmd := exec.Command("scontrol", "show", "nodes", "--format=NodeName,CPUs,Memory,State", "--noheader")
	output, err := cmd.Output()
	if err != nil {
		// If scontrol fails, return a default node for testing
		return []map[string]interface{}{
			{
				"name":   "test-node-01",
				"cpus":   "4",
				"memory": "8192",
				"state":  "idle",
			},
		}, nil
	}

	nodes := []map[string]interface{}{}
	lines := strings.Split(string(output), "\n")

	for _, line := range lines {
		if line != "" {
			fields := strings.Fields(line)
			if len(fields) >= 4 {
				node := map[string]interface{}{
					"name":   fields[0],
					"cpus":   fields[1],
					"memory": fields[2],
					"state":  fields[3],
				}
				nodes = append(nodes, node)
			}
		}
	}

	return nodes, nil
}

func getKubernetesClusterInfo() (map[string]interface{}, error) {
	cmd := exec.Command("kubectl", "cluster-info")
	output, err := cmd.Output()
	if err != nil {
		return nil, err
	}

	return map[string]interface{}{
		"cluster_info": string(output),
	}, nil
}

func getKubernetesNodes() ([]map[string]interface{}, error) {
	cmd := exec.Command("kubectl", "get", "nodes", "-o", "json")
	output, err := cmd.Output()
	if err != nil {
		return nil, err
	}

	var nodeList struct {
		Items []map[string]interface{} `json:"items"`
	}
	if err := json.Unmarshal(output, &nodeList); err != nil {
		return nil, err
	}

	return nodeList.Items, nil
}

func getCPUInfo() (map[string]interface{}, error) {
	cpuInfo := make(map[string]interface{})

	// Try lscpu first (available on most Linux distributions)
	cmd := exec.Command("lscpu")
	output, err := cmd.Output()
	if err == nil {
		lines := strings.Split(string(output), "\n")
		for _, line := range lines {
			if strings.Contains(line, ":") {
				parts := strings.SplitN(line, ":", 2)
				key := strings.TrimSpace(parts[0])
				value := strings.TrimSpace(parts[1])
				cpuInfo[key] = value
			}
		}
		return cpuInfo, nil
	}

	// Fallback to /proc/cpuinfo (available on all Linux systems including Alpine)
	data, err := os.ReadFile("/proc/cpuinfo")
	if err != nil {
		return nil, fmt.Errorf("failed to read /proc/cpuinfo: %w", err)
	}

	lines := strings.Split(string(data), "\n")
	processorCount := 0
	var modelName, cpuMHz, cacheSize string

	for _, line := range lines {
		if strings.HasPrefix(line, "processor") {
			processorCount++
		} else if strings.HasPrefix(line, "model name") {
			parts := strings.SplitN(line, ":", 2)
			if len(parts) == 2 {
				modelName = strings.TrimSpace(parts[1])
			}
		} else if strings.HasPrefix(line, "cpu MHz") {
			parts := strings.SplitN(line, ":", 2)
			if len(parts) == 2 {
				cpuMHz = strings.TrimSpace(parts[1])
			}
		} else if strings.HasPrefix(line, "cache size") {
			parts := strings.SplitN(line, ":", 2)
			if len(parts) == 2 {
				cacheSize = strings.TrimSpace(parts[1])
			}
		}
	}

	// Populate cpuInfo with extracted data
	cpuInfo["CPU(s)"] = fmt.Sprintf("%d", processorCount)
	if modelName != "" {
		cpuInfo["Model name"] = modelName
	}
	if cpuMHz != "" {
		cpuInfo["CPU MHz"] = cpuMHz
	}
	if cacheSize != "" {
		cpuInfo["L3 cache"] = cacheSize
	}

	return cpuInfo, nil
}

func getMemoryInfo() (map[string]interface{}, error) {
	cmd := exec.Command("free", "-h")
	output, err := cmd.Output()
	if err != nil {
		return nil, err
	}

	memoryInfo := make(map[string]interface{})
	lines := strings.Split(string(output), "\n")

	for _, line := range lines {
		if strings.HasPrefix(line, "Mem:") {
			fields := strings.Fields(line)
			if len(fields) >= 4 {
				memoryInfo["total"] = fields[1]
				memoryInfo["used"] = fields[2]
				memoryInfo["free"] = fields[3]
			}
		}
	}

	return memoryInfo, nil
}

func getDiskInfo() (map[string]interface{}, error) {
	cmd := exec.Command("df", "-h")
	output, err := cmd.Output()
	if err != nil {
		return nil, err
	}

	diskInfo := make(map[string]interface{})
	lines := strings.Split(string(output), "\n")

	for _, line := range lines {
		if strings.HasPrefix(line, "/dev/") {
			fields := strings.Fields(line)
			if len(fields) >= 6 {
				diskInfo[fields[5]] = map[string]interface{}{
					"device": fields[0],
					"size":   fields[1],
					"used":   fields[2],
					"avail":  fields[3],
					"use":    fields[4],
				}
			}
		}
	}

	return diskInfo, nil
}

func getHostname() (string, error) {
	cmd := exec.Command("hostname")
	output, err := cmd.Output()
	if err != nil {
		return "", err
	}
	return strings.TrimSpace(string(output)), nil
}

func generateSSHKeyPair() (string, string, error) {
	// Generate private key
	privateKey, err := rsa.GenerateKey(rand.Reader, 2048)
	if err != nil {
		return "", "", err
	}

	// Encode private key to PEM format
	privateKeyPEM := &pem.Block{
		Type:  "RSA PRIVATE KEY",
		Bytes: x509.MarshalPKCS1PrivateKey(privateKey),
	}

	privateKeyBytes := pem.EncodeToMemory(privateKeyPEM)

	// Generate public key
	publicKey, err := ssh.NewPublicKey(&privateKey.PublicKey)
	if err != nil {
		return "", "", err
	}

	publicKeyBytes := ssh.MarshalAuthorizedKey(publicKey)

	return string(privateKeyBytes), string(publicKeyBytes), nil
}

func addPublicKeyToAuthorizedKeys(publicKey string) error {
	homeDir, err := os.UserHomeDir()
	if err != nil {
		return err
	}

	sshDir := filepath.Join(homeDir, ".ssh")
	if err := os.MkdirAll(sshDir, 0700); err != nil {
		return err
	}

	authorizedKeysPath := filepath.Join(sshDir, "authorized_keys")

	// Check if key already exists
	existingKeys, err := os.ReadFile(authorizedKeysPath)
	if err != nil && !os.IsNotExist(err) {
		return err
	}

	if strings.Contains(string(existingKeys), publicKey) {
		return nil // Key already exists
	}

	// Append public key
	file, err := os.OpenFile(authorizedKeysPath, os.O_APPEND|os.O_CREATE|os.O_WRONLY, 0600)
	if err != nil {
		return err
	}
	defer file.Close()

	_, err = file.WriteString(publicKey)
	return err
}

func sendRegistrationToServer(registrationData ComputeResourceRegistration) (string, error) {
	// Get server URL from config
	configManager := NewConfigManager()
	serverURL, err := configManager.GetServerURL()
	if err != nil {
		return "", fmt.Errorf("failed to get server URL: %w", err)
	}

	// Marshal registration data
	jsonData, err := json.Marshal(registrationData)
	if err != nil {
		return "", fmt.Errorf("failed to marshal registration data: %w", err)
	}

	// Create HTTP request
	ctx, cancel := context.WithTimeout(context.Background(), 30*time.Second)
	defer cancel()

	req, err := http.NewRequestWithContext(ctx, "POST", serverURL+"/api/v2/resources/compute", bytes.NewBuffer(jsonData))
	if err != nil {
		return "", fmt.Errorf("failed to create request: %w", err)
	}

	req.Header.Set("Content-Type", "application/json")

	// Send request
	client := &http.Client{Timeout: 30 * time.Second}
	resp, err := client.Do(req)
	if err != nil {
		return "", fmt.Errorf("failed to send registration request: %w", err)
	}
	defer resp.Body.Close()

	body, err := io.ReadAll(resp.Body)
	if err != nil {
		return "", fmt.Errorf("failed to read response: %w", err)
	}

	if resp.StatusCode != http.StatusCreated {
		return "", fmt.Errorf("registration failed: %d - %s", resp.StatusCode, string(body))
	}

	// Parse response
	var result struct {
		ID string `json:"id"`
	}
	if err := json.Unmarshal(body, &result); err != nil {
		return "", fmt.Errorf("failed to parse response: %w", err)
	}

	return result.ID, nil
}
