package testutil

import (
	"crypto/rand"
	"crypto/rsa"
	"crypto/x509"
	"encoding/pem"
	"fmt"
	"os"
	"path/filepath"

	"golang.org/x/crypto/ssh"
)

// SSHConfig represents SSH connection configuration
type SSHConfig struct {
	Host     string
	Port     int
	Username string
	KeyPath  string
}

// ComputeResourceConfig represents compute resource configuration
type ComputeResourceConfig struct {
	Name     string
	Host     string
	Port     int
	Username string
	Type     string
}

// StorageResourceConfig represents storage resource configuration
type StorageResourceConfig struct {
	Name     string
	Host     string
	Port     int
	Username string
	Type     string
	BasePath string
}

// SSHKeyPair represents an SSH key pair
type SSHKeyPair struct {
	PrivateKeyPath string
	PublicKeyPath  string
	PrivateKey     *rsa.PrivateKey
	PublicKey      ssh.PublicKey
}

// SSHSetupManager manages SSH credentials and connections for testing
type SSHSetupManager struct {
	keyDir string
}

// NewSSHSetupManager creates a new SSH setup manager
func NewSSHSetupManager() (*SSHSetupManager, error) {
	keyDir := "/tmp/airavata-test-ssh"
	if err := os.MkdirAll(keyDir, 0700); err != nil {
		return nil, fmt.Errorf("failed to create SSH key directory: %w", err)
	}

	return &SSHSetupManager{
		keyDir: keyDir,
	}, nil
}

// GenerateSSHKeyPair generates a new SSH key pair for testing
func (ssm *SSHSetupManager) GenerateSSHKeyPair() (*SSHKeyPair, error) {
	// Generate private key
	privateKey, err := rsa.GenerateKey(rand.Reader, 2048)
	if err != nil {
		return nil, fmt.Errorf("failed to generate private key: %w", err)
	}

	// Encode private key to PEM format
	privateKeyPEM := &pem.Block{
		Type:  "RSA PRIVATE KEY",
		Bytes: x509.MarshalPKCS1PrivateKey(privateKey),
	}

	privateKeyPath := filepath.Join(ssm.keyDir, "test_rsa")
	privateKeyFile, err := os.Create(privateKeyPath)
	if err != nil {
		return nil, fmt.Errorf("failed to create private key file: %w", err)
	}
	defer privateKeyFile.Close()

	if err := pem.Encode(privateKeyFile, privateKeyPEM); err != nil {
		return nil, fmt.Errorf("failed to encode private key: %w", err)
	}

	// Set proper permissions
	if err := os.Chmod(privateKeyPath, 0600); err != nil {
		return nil, fmt.Errorf("failed to set private key permissions: %w", err)
	}

	// Generate public key
	publicKey, err := ssh.NewPublicKey(&privateKey.PublicKey)
	if err != nil {
		return nil, fmt.Errorf("failed to generate public key: %w", err)
	}

	publicKeyPath := filepath.Join(ssm.keyDir, "test_rsa.pub")
	publicKeyFile, err := os.Create(publicKeyPath)
	if err != nil {
		return nil, fmt.Errorf("failed to create public key file: %w", err)
	}
	defer publicKeyFile.Close()

	if _, err := publicKeyFile.Write(ssh.MarshalAuthorizedKey(publicKey)); err != nil {
		return nil, fmt.Errorf("failed to write public key: %w", err)
	}

	return &SSHKeyPair{
		PrivateKeyPath: privateKeyPath,
		PublicKeyPath:  publicKeyPath,
		PrivateKey:     privateKey,
		PublicKey:      publicKey,
	}, nil
}

// SetupSSHCredentials sets up SSH credentials for compute resources
func (ssm *SSHSetupManager) SetupSSHCredentials(computeResources []ComputeResourceConfig) error {
	for _, resource := range computeResources {
		// SSH credentials are now managed via the registration workflow
		// which generates SSH keys and stores them in the vault
		fmt.Printf("SSH credentials for %s at %s:%d will be managed via registration workflow\n",
			resource.Name, resource.Host, resource.Port)
	}
	return nil
}

// SetupSFTPCredentials sets up SFTP credentials for storage resources
func (ssm *SSHSetupManager) SetupSFTPCredentials(storageResources []StorageResourceConfig) error {
	for _, resource := range storageResources {
		// SFTP credentials are now managed via the registration workflow
		// which generates SSH keys and stores them in the vault
		fmt.Printf("SFTP credentials for %s at %s:%d will be managed via registration workflow\n",
			resource.Name, resource.Host, resource.Port)
	}
	return nil
}

// CreateSSHConfig creates an SSH configuration from a compute resource
func CreateSSHConfig(computeConfig *ComputeResourceConfig) *SSHConfig {
	return &SSHConfig{
		Host:     computeConfig.Host,
		Port:     computeConfig.Port,
		Username: computeConfig.Username,
		KeyPath:  "", // Key path will be set during registration
	}
}

// CreateSFTPConfig creates an SSH configuration from a storage resource
func CreateSFTPConfig(storageConfig *StorageResourceConfig) *SSHConfig {
	return &SSHConfig{
		Host:     storageConfig.Host,
		Port:     storageConfig.Port,
		Username: storageConfig.Username,
		KeyPath:  "", // Key path will be set during registration
	}
}

// GetDefaultComputeConfigs returns default compute resource configurations for testing
func GetDefaultComputeConfigs() []ComputeResourceConfig {
	return []ComputeResourceConfig{
		{
			Name:     "SLURM Test Cluster",
			Host:     "localhost",
			Port:     2223,
			Username: "testuser",
			Type:     "slurm",
		},
		{
			Name:     "Bare Metal Test Cluster",
			Host:     "localhost",
			Port:     2225,
			Username: "testuser",
			Type:     "baremetal",
		},
	}
}

// GetDefaultStorageConfigs returns default storage resource configurations for testing
func GetDefaultStorageConfigs() []StorageResourceConfig {
	return []StorageResourceConfig{
		{
			Name:     "global-scratch",
			Host:     "localhost",
			Port:     2222,
			Username: "testuser",
			Type:     "sftp",
			BasePath: "/home/testuser/upload",
		},
	}
}

// CleanupSSHKeys removes generated SSH keys
func (ssm *SSHSetupManager) CleanupSSHKeys() error {
	return os.RemoveAll(ssm.keyDir)
}
