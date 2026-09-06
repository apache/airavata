package adapters

import (
	"fmt"
	"strings"

	"github.com/apache/airavata/scheduler/core/domain"
)

// SSHCredentials holds extracted SSH authentication information
type SSHCredentials struct {
	Username       string
	Password       string
	PrivateKeyPath string
	Port           string
}

// ExtractSSHCredentials standardizes credential extraction from vault
func ExtractSSHCredentials(credential *domain.Credential, credentialData []byte, resourceMetadata map[string]any) (*SSHCredentials, error) {
	// Extract credential metadata
	credMetadata := make(map[string]string)
	if credential.Metadata != nil {
		for k, v := range credential.Metadata {
			credMetadata[k] = fmt.Sprintf("%v", v)
		}
	}

	// Extract credential data
	var username, password, privateKeyPath, port string

	if credential.Type == domain.CredentialTypeSSHKey {
		// SSH key authentication
		if keyData, ok := credMetadata["private_key"]; ok {
			privateKeyPath = keyData
		} else {
			// Fallback: use credential data as private key
			privateKeyPath = string(credentialData)
		}
		if user, ok := credMetadata["username"]; ok {
			username = user
		}
	} else if credential.Type == domain.CredentialTypePassword {
		// Password authentication
		if user, ok := credMetadata["username"]; ok {
			username = user
		}
		if pass, ok := credMetadata["password"]; ok {
			password = pass
		}
	}

	// Extract resource metadata
	resourceMetadataStr := make(map[string]string)
	for k, v := range resourceMetadata {
		resourceMetadataStr[k] = fmt.Sprintf("%v", v)
	}

	// Get port from resource metadata or extract from endpoint
	if portData, ok := resourceMetadataStr["port"]; ok {
		port = portData
	}
	if port == "" {
		// Try to extract port from endpoint
		if endpoint, ok := resourceMetadataStr["endpoint"]; ok {
			if strings.Contains(endpoint, ":") {
				parts := strings.Split(endpoint, ":")
				if len(parts) == 2 {
					port = parts[1]
				}
			}
		}
	}
	if port == "" {
		port = "22" // Default SSH port
	}

	// If username is not in credentials, try to parse it from credential data
	if username == "" {
		// Try to parse credential data in format "username:password"
		credData := string(credentialData)
		if strings.Contains(credData, ":") {
			parts := strings.SplitN(credData, ":", 2)
			if len(parts) == 2 {
				username = parts[0]
				if password == "" {
					password = parts[1]
				}
			}
		}
	}

	// If still no username, try to get it from resource metadata
	if username == "" {
		if usernameData, ok := resourceMetadataStr["username"]; ok {
			username = usernameData
		}
	}

	if username == "" {
		return nil, fmt.Errorf("username not found in credentials or resource metadata")
	}

	// Password or private key must be provided in credentials
	if password == "" && privateKeyPath == "" {
		return nil, fmt.Errorf("no authentication method provided (password or private key required)")
	}

	return &SSHCredentials{
		Username:       username,
		Password:       password,
		PrivateKeyPath: privateKeyPath,
		Port:           port,
	}, nil
}
