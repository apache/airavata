package testutil

import (
	"crypto/rand"
	"crypto/rsa"
	"crypto/x509"
	"encoding/base64"
	"encoding/pem"
	"fmt"
	"os"
	"os/exec"
	"path/filepath"
	"strings"
	"time"

	"github.com/apache/airavata/scheduler/core/domain"
	"github.com/google/uuid"
)

// SSHKeyManager manages SSH key generation and injection for testing
type SSHKeyManager struct {
	privateKey []byte
	publicKey  []byte
	tempDir    string
}

// GenerateSSHKeys generates RSA key pair for testing
func GenerateSSHKeys() (*SSHKeyManager, error) {
	// Create temporary directory
	tempDir, err := os.MkdirTemp("", "ssh-keys-*")
	if err != nil {
		return nil, fmt.Errorf("failed to create temp directory: %w", err)
	}

	// Generate RSA private key
	privateKey, err := rsa.GenerateKey(rand.Reader, 2048)
	if err != nil {
		os.RemoveAll(tempDir)
		return nil, fmt.Errorf("failed to generate private key: %w", err)
	}

	// Encode private key to PEM
	privateKeyPEM := &pem.Block{
		Type:  "RSA PRIVATE KEY",
		Bytes: x509.MarshalPKCS1PrivateKey(privateKey),
	}

	privateKeyBytes := pem.EncodeToMemory(privateKeyPEM)

	// Generate public key in OpenSSH format
	publicKeyBytes, err := generateOpenSSHPublicKey(&privateKey.PublicKey)
	if err != nil {
		os.RemoveAll(tempDir)
		return nil, fmt.Errorf("failed to generate public key: %w", err)
	}

	// Write keys to files
	privateKeyPath := filepath.Join(tempDir, "id_rsa")
	publicKeyPath := filepath.Join(tempDir, "id_rsa.pub")

	if err := os.WriteFile(privateKeyPath, privateKeyBytes, 0600); err != nil {
		os.RemoveAll(tempDir)
		return nil, fmt.Errorf("failed to write private key: %w", err)
	}

	if err := os.WriteFile(publicKeyPath, publicKeyBytes, 0644); err != nil {
		os.RemoveAll(tempDir)
		return nil, fmt.Errorf("failed to write public key: %w", err)
	}

	return &SSHKeyManager{
		privateKey: privateKeyBytes,
		publicKey:  publicKeyBytes,
		tempDir:    tempDir,
	}, nil
}

// InjectIntoContainer copies public key into container's authorized_keys
func (m *SSHKeyManager) InjectIntoContainer(containerName string) error {
	// Get the public key content (without the newline)
	publicKeyContent := string(m.publicKey)
	if len(publicKeyContent) > 0 && publicKeyContent[len(publicKeyContent)-1] == '\n' {
		publicKeyContent = publicKeyContent[:len(publicKeyContent)-1]
	}

	// Create authorized_keys content
	authorizedKeysContent := publicKeyContent + "\n"

	// Check if this is a SLURM container
	isSlurmContainer := strings.Contains(containerName, "slurm")

	var cmd *exec.Cmd
	if isSlurmContainer {
		// For SLURM containers, use root to set up SSH keys
		cmd = exec.Command("docker", "exec", containerName, "bash", "-c",
			fmt.Sprintf("mkdir -p /home/testuser/.ssh && echo '%s' > /home/testuser/.ssh/authorized_keys && chmod 700 /home/testuser/.ssh && chmod 600 /home/testuser/.ssh/authorized_keys && chown -R testuser:testuser /home/testuser/.ssh && echo 'SSH key injected for testuser'",
				authorizedKeysContent))
	} else {
		// For other containers, use the standard approach
		cmd = exec.Command("docker", "exec", containerName, "bash", "-c",
			fmt.Sprintf("mkdir -p /home/testuser/.ssh && echo '%s' > /home/testuser/.ssh/authorized_keys && chmod 700 /home/testuser/.ssh && chmod 600 /home/testuser/.ssh/authorized_keys && chown -R testuser:testuser /home/testuser/.ssh",
				authorizedKeysContent))
	}

	if err := cmd.Run(); err != nil {
		return fmt.Errorf("failed to inject SSH key into container %s: %w", containerName, err)
	}

	return nil
}

// GetCredential returns domain.Credential for vault storage
func (m *SSHKeyManager) GetCredential(name string) *domain.Credential {
	return &domain.Credential{
		ID:        uuid.New().String(),
		Name:      name,
		Type:      domain.CredentialTypeSSHKey,
		OwnerID:   "test-user",
		CreatedAt: time.Now(),
		UpdatedAt: time.Now(),
	}
}

// GetPrivateKeyPath returns the path to the private key file
func (m *SSHKeyManager) GetPrivateKeyPath() string {
	return filepath.Join(m.tempDir, "id_rsa")
}

// GetPublicKeyPath returns the path to the public key file
func (m *SSHKeyManager) GetPublicKeyPath() string {
	return filepath.Join(m.tempDir, "id_rsa.pub")
}

// GetPrivateKey returns the private key bytes
func (m *SSHKeyManager) GetPrivateKey() []byte {
	return m.privateKey
}

// GetPublicKey returns the public key bytes
func (m *SSHKeyManager) GetPublicKey() []byte {
	return m.publicKey
}

// Cleanup removes temporary key files
func (m *SSHKeyManager) Cleanup() error {
	if m.tempDir != "" {
		return os.RemoveAll(m.tempDir)
	}
	return nil
}

// TestSSHConnection tests SSH connection to a container
func (m *SSHKeyManager) TestSSHConnection(host string, port int, username string) error {
	// Use ssh command to test connection
	cmd := exec.Command("ssh",
		"-i", m.GetPrivateKeyPath(),
		"-o", "StrictHostKeyChecking=no",
		"-o", "UserKnownHostsFile=/dev/null",
		"-o", "LogLevel=ERROR",
		"-p", fmt.Sprintf("%d", port),
		fmt.Sprintf("%s@%s", username, host),
		"echo 'SSH connection successful'")

	output, err := cmd.CombinedOutput()
	if err != nil {
		return fmt.Errorf("SSH connection failed: %w, output: %s", err, string(output))
	}

	return nil
}

// InjectMasterSSHKeyIntoContainer injects the master SSH key into a container
// DEPRECATED: SSH keys are now generated during resource registration, not pre-injected
func InjectMasterSSHKeyIntoContainer(containerName string) error {
	config := GetTestConfig()

	// Read the master SSH public key
	publicKeyBytes, err := os.ReadFile(config.MasterSSHPublicKey)
	if err != nil {
		return fmt.Errorf("failed to read master SSH public key: %w", err)
	}

	// Get the public key content (without the newline)
	publicKeyContent := string(publicKeyBytes)
	if len(publicKeyContent) > 0 && publicKeyContent[len(publicKeyContent)-1] == '\n' {
		publicKeyContent = publicKeyContent[:len(publicKeyContent)-1]
	}

	// Create authorized_keys content
	authorizedKeysContent := publicKeyContent + "\n"

	// Check if this is a SLURM container
	isSlurmContainer := strings.Contains(containerName, "slurm")

	// First check if the key is already present
	checkCmd := exec.Command("docker", "exec", containerName, "bash", "-c",
		fmt.Sprintf("grep -q '%s' /home/testuser/.ssh/authorized_keys 2>/dev/null", publicKeyContent))
	if err := checkCmd.Run(); err == nil {
		// Key already exists, no need to inject
		return nil
	}

	var cmd *exec.Cmd
	if isSlurmContainer {
		// For SLURM containers, use root to set up SSH keys
		cmd = exec.Command("docker", "exec", containerName, "bash", "-c",
			fmt.Sprintf("mkdir -p /home/testuser/.ssh && echo '%s' > /home/testuser/.ssh/authorized_keys && chmod 700 /home/testuser/.ssh && chmod 600 /home/testuser/.ssh/authorized_keys && chown -R testuser:testuser /home/testuser/.ssh && echo 'Master SSH key injected for testuser'",
				authorizedKeysContent))
	} else {
		// For other containers, use the standard approach
		cmd = exec.Command("docker", "exec", containerName, "bash", "-c",
			fmt.Sprintf("mkdir -p /home/testuser/.ssh && echo '%s' > /home/testuser/.ssh/authorized_keys && chmod 700 /home/testuser/.ssh && chmod 600 /home/testuser/.ssh/authorized_keys && chown -R testuser:testuser /home/testuser/.ssh",
				authorizedKeysContent))
	}

	if err := cmd.Run(); err != nil {
		return fmt.Errorf("failed to inject master SSH key into container %s: %w", containerName, err)
	}

	return nil
}

// generateOpenSSHPublicKey generates OpenSSH public key from RSA public key
func generateOpenSSHPublicKey(pub *rsa.PublicKey) ([]byte, error) {
	// Marshal the public key to DER format
	pubDER, err := x509.MarshalPKIXPublicKey(pub)
	if err != nil {
		return nil, err
	}

	// Encode to base64
	pubBase64 := base64.StdEncoding.EncodeToString(pubDER)

	// Create OpenSSH format
	opensshKey := fmt.Sprintf("ssh-rsa %s test-key\n", pubBase64)
	return []byte(opensshKey), nil
}
