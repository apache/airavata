package main

import (
	"crypto/aes"
	"crypto/cipher"
	"crypto/rand"
	"encoding/base64"
	"encoding/json"
	"fmt"
	"io"
	"os"
	"path/filepath"
	"runtime"
)

// CLIConfig holds configuration for the CLI
type CLIConfig struct {
	ServerURL string `json:"server_url"`
	Token     string `json:"token,omitempty"`
	Username  string `json:"username,omitempty"`
	Encrypted bool   `json:"encrypted,omitempty"`
}

// ConfigManager handles CLI configuration
type ConfigManager struct {
	configPath string
	key        []byte
}

// NewConfigManager creates a new config manager
func NewConfigManager() *ConfigManager {
	homeDir, err := os.UserHomeDir()
	if err != nil {
		homeDir = "."
	}

	configDir := filepath.Join(homeDir, ".airavata")
	configPath := filepath.Join(configDir, "config.json")

	// Generate a simple key based on user's home directory
	// In production, this should be more secure
	key := generateKey(homeDir)

	return &ConfigManager{
		configPath: configPath,
		key:        key,
	}
}

// LoadConfig loads configuration from file
func (cm *ConfigManager) LoadConfig() (*CLIConfig, error) {
	// Create config directory if it doesn't exist
	configDir := filepath.Dir(cm.configPath)
	if err := os.MkdirAll(configDir, 0700); err != nil {
		return nil, fmt.Errorf("failed to create config directory: %w", err)
	}

	// Check if config file exists
	if _, err := os.Stat(cm.configPath); os.IsNotExist(err) {
		// Return default config
		return &CLIConfig{
			ServerURL: getEnvOrDefault("AIRAVATA_SERVER", "http://localhost:8080"),
		}, nil
	}

	// Read config file
	data, err := os.ReadFile(cm.configPath)
	if err != nil {
		return nil, fmt.Errorf("failed to read config file: %w", err)
	}

	var config CLIConfig
	if err := json.Unmarshal(data, &config); err != nil {
		return nil, fmt.Errorf("failed to parse config file: %w", err)
	}

	// Decrypt token if encrypted
	if config.Encrypted && config.Token != "" {
		decryptedToken, err := cm.decrypt(config.Token)
		if err != nil {
			return nil, fmt.Errorf("failed to decrypt token: %w", err)
		}
		config.Token = decryptedToken
		config.Encrypted = false
	}

	return &config, nil
}

// SaveConfig saves configuration to file
func (cm *ConfigManager) SaveConfig(config *CLIConfig) error {
	// Create config directory if it doesn't exist
	configDir := filepath.Dir(cm.configPath)
	if err := os.MkdirAll(configDir, 0700); err != nil {
		return fmt.Errorf("failed to create config directory: %w", err)
	}

	// Create a copy to avoid modifying the original
	saveConfig := *config

	// Encrypt token if present
	if saveConfig.Token != "" {
		encryptedToken, err := cm.encrypt(saveConfig.Token)
		if err != nil {
			return fmt.Errorf("failed to encrypt token: %w", err)
		}
		saveConfig.Token = encryptedToken
		saveConfig.Encrypted = true
	}

	// Marshal to JSON
	data, err := json.MarshalIndent(&saveConfig, "", "  ")
	if err != nil {
		return fmt.Errorf("failed to marshal config: %w", err)
	}

	// Write to file with restricted permissions
	if err := os.WriteFile(cm.configPath, data, 0600); err != nil {
		return fmt.Errorf("failed to write config file: %w", err)
	}

	return nil
}

// ClearConfig clears the configuration (removes token)
func (cm *ConfigManager) ClearConfig() error {
	config, err := cm.LoadConfig()
	if err != nil {
		return err
	}

	config.Token = ""
	config.Username = ""
	config.Encrypted = false

	return cm.SaveConfig(config)
}

// SetServerURL sets the server URL in config
func (cm *ConfigManager) SetServerURL(serverURL string) error {
	config, err := cm.LoadConfig()
	if err != nil {
		return err
	}

	config.ServerURL = serverURL
	return cm.SaveConfig(config)
}

// SetToken sets the authentication token in config
func (cm *ConfigManager) SetToken(token, username string) error {
	config, err := cm.LoadConfig()
	if err != nil {
		return err
	}

	config.Token = token
	config.Username = username
	return cm.SaveConfig(config)
}

// GetToken returns the current authentication token
func (cm *ConfigManager) GetToken() (string, error) {
	config, err := cm.LoadConfig()
	if err != nil {
		return "", err
	}

	return config.Token, nil
}

// GetServerURL returns the current server URL
func (cm *ConfigManager) GetServerURL() (string, error) {
	config, err := cm.LoadConfig()
	if err != nil {
		return "", err
	}

	return config.ServerURL, nil
}

// IsAuthenticated checks if the user is authenticated
func (cm *ConfigManager) IsAuthenticated() bool {
	config, err := cm.LoadConfig()
	if err != nil {
		return false
	}

	return config.Token != ""
}

// GetUsername returns the current username
func (cm *ConfigManager) GetUsername() (string, error) {
	config, err := cm.LoadConfig()
	if err != nil {
		return "", err
	}

	return config.Username, nil
}

// encrypt encrypts a string using AES
func (cm *ConfigManager) encrypt(plaintext string) (string, error) {
	block, err := aes.NewCipher(cm.key)
	if err != nil {
		return "", err
	}

	gcm, err := cipher.NewGCM(block)
	if err != nil {
		return "", err
	}

	nonce := make([]byte, gcm.NonceSize())
	if _, err := io.ReadFull(rand.Reader, nonce); err != nil {
		return "", err
	}

	ciphertext := gcm.Seal(nonce, nonce, []byte(plaintext), nil)
	return base64.StdEncoding.EncodeToString(ciphertext), nil
}

// decrypt decrypts a string using AES
func (cm *ConfigManager) decrypt(ciphertext string) (string, error) {
	data, err := base64.StdEncoding.DecodeString(ciphertext)
	if err != nil {
		return "", err
	}

	block, err := aes.NewCipher(cm.key)
	if err != nil {
		return "", err
	}

	gcm, err := cipher.NewGCM(block)
	if err != nil {
		return "", err
	}

	nonceSize := gcm.NonceSize()
	if len(data) < nonceSize {
		return "", fmt.Errorf("ciphertext too short")
	}

	nonce, ciphertextBytes := data[:nonceSize], data[nonceSize:]
	plaintext, err := gcm.Open(nil, nonce, ciphertextBytes, nil)
	if err != nil {
		return "", err
	}

	return string(plaintext), nil
}

// generateKey generates a key based on the user's home directory
// This is a simple implementation - in production, use a more secure method
func generateKey(homeDir string) []byte {
	// Use a combination of home directory and OS info to generate a key
	key := fmt.Sprintf("%s-%s-%s", homeDir, runtime.GOOS, runtime.GOARCH)

	// Hash the key to get 32 bytes for AES-256
	hash := make([]byte, 32)
	for i := 0; i < len(key) && i < 32; i++ {
		hash[i] = key[i%len(key)]
	}

	// Pad with zeros if needed
	for i := len(key); i < 32; i++ {
		hash[i] = byte(i)
	}

	return hash
}

// getEnvOrDefault returns environment variable value or default
func getEnvOrDefault(key, defaultValue string) string {
	if value := os.Getenv(key); value != "" {
		return value
	}
	return defaultValue
}
