package config

import (
	"fmt"
	"os"
	"path/filepath"
	"time"

	"gopkg.in/yaml.v3"
)

// Config represents the CLI configuration
type Config struct {
	Server  ServerConfig  `yaml:"server"`
	Auth    AuthConfig    `yaml:"auth"`
	Gateway GatewayConfig `yaml:"gateway"`
}

// ServerConfig holds server connection information
type ServerConfig struct {
	Hostname string `yaml:"hostname"`
	Port     int    `yaml:"port"`
	TLS      bool   `yaml:"tls"`
}

// AuthConfig holds authentication information
type AuthConfig struct {
	KeycloakURL  string    `yaml:"keycloak_url"`
	Realm        string    `yaml:"realm"`
	ClientID     string    `yaml:"client_id"`
	AccessToken  string    `yaml:"access_token"`
	RefreshToken string    `yaml:"refresh_token"`
	ExpiresAt    time.Time `yaml:"expires_at"`
	Username     string    `yaml:"username"`
}

// GatewayConfig holds default gateway information
type GatewayConfig struct {
	ID string `yaml:"id"`
}

// DefaultConfig returns a default configuration
func DefaultConfig() *Config {
	return &Config{
		Server: ServerConfig{
			Port: 9930,
			TLS:  true,
		},
		Auth: AuthConfig{
			ClientID: "airavata-cli",
		},
		Gateway: GatewayConfig{
			ID: "default-gateway",
		},
	}
}

// ConfigManager handles configuration file operations
type ConfigManager struct {
	configPath string
	config     *Config
}

// NewConfigManager creates a new configuration manager
func NewConfigManager() *ConfigManager {
	homeDir, err := os.UserHomeDir()
	if err != nil {
		panic(fmt.Sprintf("Failed to get user home directory: %v", err))
	}

	configDir := filepath.Join(homeDir, ".airavata-cli")
	configPath := filepath.Join(configDir, "config.yaml")

	return &ConfigManager{
		configPath: configPath,
		config:     nil,
	}
}

// Load loads configuration from file
func (cm *ConfigManager) Load() (*Config, error) {
	if cm.config != nil {
		return cm.config, nil
	}

	// Check if config file exists
	if _, err := os.Stat(cm.configPath); os.IsNotExist(err) {
		cm.config = DefaultConfig()
		return cm.config, nil
	}

	// Read config file
	data, err := os.ReadFile(cm.configPath)
	if err != nil {
		return nil, fmt.Errorf("failed to read config file: %w", err)
	}

	// Parse YAML
	config := DefaultConfig()
	if err := yaml.Unmarshal(data, config); err != nil {
		return nil, fmt.Errorf("failed to parse config file: %w", err)
	}

	cm.config = config
	return config, nil
}

// Save saves configuration to file
func (cm *ConfigManager) Save(config *Config) error {
	// Create config directory if it doesn't exist
	configDir := filepath.Dir(cm.configPath)
	if err := os.MkdirAll(configDir, 0755); err != nil {
		return fmt.Errorf("failed to create config directory: %w", err)
	}

	// Marshal to YAML
	data, err := yaml.Marshal(config)
	if err != nil {
		return fmt.Errorf("failed to marshal config: %w", err)
	}

	// Write to file
	if err := os.WriteFile(cm.configPath, data, 0600); err != nil {
		return fmt.Errorf("failed to write config file: %w", err)
	}

	cm.config = config
	return nil
}

// Clear clears the configuration (for logout)
func (cm *ConfigManager) Clear() error {
	if _, err := os.Stat(cm.configPath); os.IsNotExist(err) {
		return nil // Nothing to clear
	}

	if err := os.Remove(cm.configPath); err != nil {
		return fmt.Errorf("failed to remove config file: %w", err)
	}

	cm.config = nil
	return nil
}

// IsAuthenticated checks if the user is authenticated
func (cm *ConfigManager) IsAuthenticated() bool {
	config, err := cm.Load()
	if err != nil {
		return false
	}

	// Check if we have required auth fields
	if config.Auth.AccessToken == "" || config.Auth.Username == "" {
		return false
	}

	// Check if token is expired
	if !config.Auth.ExpiresAt.IsZero() && time.Now().After(config.Auth.ExpiresAt) {
		return false
	}

	return true
}

// GetServerAddress returns the server address
func (cm *ConfigManager) GetServerAddress() (string, error) {
	config, err := cm.Load()
	if err != nil {
		return "", err
	}

	if config.Server.Hostname == "" {
		return "", fmt.Errorf("server hostname not configured")
	}

	protocol := "thrift"
	if config.Server.TLS {
		protocol = "thrift+ssl"
	}

	return fmt.Sprintf("%s://%s:%d", protocol, config.Server.Hostname, config.Server.Port), nil
}

// GetAuthzToken returns the authorization token for Thrift calls
func (cm *ConfigManager) GetAuthzToken() (map[string]string, error) {
	config, err := cm.Load()
	if err != nil {
		return nil, err
	}

	if !cm.IsAuthenticated() {
		return nil, fmt.Errorf("not authenticated")
	}

	// Create claims map for AuthzToken
	claims := map[string]string{
		"accessToken": config.Auth.AccessToken,
		"userName":    config.Auth.Username,
		"gatewayID":   config.Gateway.ID,
	}

	return claims, nil
}
