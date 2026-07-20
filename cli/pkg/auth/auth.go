package auth

import (
	"context"
	"encoding/json"
	"fmt"
	"io"
	"net/http"
	"net/url"
	"strings"
	"time"
)

// DeviceAuthResponse represents the response from device authorization endpoint
type DeviceAuthResponse struct {
	DeviceCode              string `json:"device_code"`
	UserCode                string `json:"user_code"`
	VerificationURI         string `json:"verification_uri"`
	VerificationURIComplete string `json:"verification_uri_complete,omitempty"`
	ExpiresIn               int    `json:"expires_in"`
	Interval                int    `json:"interval"`
}

// TokenResponse represents the response from token endpoint
type TokenResponse struct {
	AccessToken  string `json:"access_token"`
	RefreshToken string `json:"refresh_token"`
	ExpiresIn    int    `json:"expires_in"`
	TokenType    string `json:"token_type"`
}

// UserInfo represents user information from token
type UserInfo struct {
	Username  string `json:"preferred_username"`
	Email     string `json:"email"`
	FirstName string `json:"given_name"`
	LastName  string `json:"family_name"`
}

// AuthManager handles OAuth2 device flow authentication
type AuthManager struct {
	keycloakURL string
	realm       string
	clientID    string
	httpClient  *http.Client
}

// NewAuthManager creates a new authentication manager
func NewAuthManager(keycloakURL, realm, clientID string) *AuthManager {
	return &AuthManager{
		keycloakURL: keycloakURL,
		realm:       realm,
		clientID:    clientID,
		httpClient:  &http.Client{Timeout: 30 * time.Second},
	}
}

// StartDeviceAuth initiates the device authorization flow
func (am *AuthManager) StartDeviceAuth() (*DeviceAuthResponse, error) {
	deviceURL := fmt.Sprintf("%s/realms/%s/protocol/openid-connect/auth/device",
		am.keycloakURL, am.realm)

	data := url.Values{}
	data.Set("client_id", am.clientID)

	req, err := http.NewRequest("POST", deviceURL, strings.NewReader(data.Encode()))
	if err != nil {
		return nil, fmt.Errorf("failed to create device auth request: %w", err)
	}

	req.Header.Set("Content-Type", "application/x-www-form-urlencoded")

	resp, err := am.httpClient.Do(req)
	if err != nil {
		return nil, fmt.Errorf("failed to make device auth request: %w", err)
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK {
		body, _ := io.ReadAll(resp.Body)
		return nil, fmt.Errorf("device auth failed with status %d: %s", resp.StatusCode, string(body))
	}

	var deviceResp DeviceAuthResponse
	if err := json.NewDecoder(resp.Body).Decode(&deviceResp); err != nil {
		return nil, fmt.Errorf("failed to decode device auth response: %w", err)
	}

	return &deviceResp, nil
}

// PollForToken polls the token endpoint until user completes authorization
func (am *AuthManager) PollForToken(deviceCode string, interval time.Duration) (*TokenResponse, error) {
	tokenURL := fmt.Sprintf("%s/realms/%s/protocol/openid-connect/token",
		am.keycloakURL, am.realm)

	data := url.Values{}
	data.Set("grant_type", "urn:ietf:params:oauth:grant-type:device_code")
	data.Set("client_id", am.clientID)
	data.Set("device_code", deviceCode)

	ctx, cancel := context.WithTimeout(context.Background(), 10*time.Minute)
	defer cancel()

	ticker := time.NewTicker(interval)
	defer ticker.Stop()

	for {
		select {
		case <-ctx.Done():
			return nil, fmt.Errorf("device auth timed out")
		case <-ticker.C:
			token, err := am.requestToken(tokenURL, data)
			if err == nil {
				return token, nil
			}

			// Check if it's a "slow_down" or "authorization_pending" error
			if strings.Contains(err.Error(), "slow_down") {
				// Increase polling interval
				interval = time.Duration(float64(interval) * 1.5)
				ticker.Reset(interval)
			} else if !strings.Contains(err.Error(), "authorization_pending") {
				return nil, err
			}
		}
	}
}

// requestToken makes a request to the token endpoint
func (am *AuthManager) requestToken(tokenURL string, data url.Values) (*TokenResponse, error) {
	req, err := http.NewRequest("POST", tokenURL, strings.NewReader(data.Encode()))
	if err != nil {
		return nil, fmt.Errorf("failed to create token request: %w", err)
	}

	req.Header.Set("Content-Type", "application/x-www-form-urlencoded")

	resp, err := am.httpClient.Do(req)
	if err != nil {
		return nil, fmt.Errorf("failed to make token request: %w", err)
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK {
		body, _ := io.ReadAll(resp.Body)
		return nil, fmt.Errorf("token request failed with status %d: %s", resp.StatusCode, string(body))
	}

	var tokenResp TokenResponse
	if err := json.NewDecoder(resp.Body).Decode(&tokenResp); err != nil {
		return nil, fmt.Errorf("failed to decode token response: %w", err)
	}

	return &tokenResp, nil
}

// GetUserInfo retrieves user information from the access token
func (am *AuthManager) GetUserInfo(accessToken string) (*UserInfo, error) {
	userInfoURL := fmt.Sprintf("%s/realms/%s/protocol/openid-connect/userinfo",
		am.keycloakURL, am.realm)

	req, err := http.NewRequest("GET", userInfoURL, nil)
	if err != nil {
		return nil, fmt.Errorf("failed to create userinfo request: %w", err)
	}

	req.Header.Set("Authorization", "Bearer "+accessToken)

	resp, err := am.httpClient.Do(req)
	if err != nil {
		return nil, fmt.Errorf("failed to make userinfo request: %w", err)
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK {
		body, _ := io.ReadAll(resp.Body)
		return nil, fmt.Errorf("userinfo request failed with status %d: %s", resp.StatusCode, string(body))
	}

	var userInfo UserInfo
	if err := json.NewDecoder(resp.Body).Decode(&userInfo); err != nil {
		return nil, fmt.Errorf("failed to decode userinfo response: %w", err)
	}

	return &userInfo, nil
}

// RefreshToken refreshes an access token using the refresh token
func (am *AuthManager) RefreshToken(refreshToken string) (*TokenResponse, error) {
	tokenURL := fmt.Sprintf("%s/realms/%s/protocol/openid-connect/token",
		am.keycloakURL, am.realm)

	data := url.Values{}
	data.Set("grant_type", "refresh_token")
	data.Set("client_id", am.clientID)
	data.Set("refresh_token", refreshToken)

	return am.requestToken(tokenURL, data)
}

// DiscoverKeycloakInfo discovers Keycloak configuration from server
func DiscoverKeycloakInfo(serverHostname string) (string, string, error) {
	// Try common Keycloak discovery patterns
	possibleURLs := []string{
		fmt.Sprintf("https://%s/auth", serverHostname),
		fmt.Sprintf("https://%s/realms/airavata", serverHostname),
		fmt.Sprintf("https://iam.%s", serverHostname),
		fmt.Sprintf("https://%s:8080/auth", serverHostname),
	}

	httpClient := &http.Client{Timeout: 10 * time.Second}

	for _, baseURL := range possibleURLs {
		// Try to find realm info
		realmURL := fmt.Sprintf("%s/realms/airavata", baseURL)
		resp, err := httpClient.Get(realmURL)
		if err == nil && resp.StatusCode == http.StatusOK {
			resp.Body.Close()
			return baseURL, "airavata", nil
		}
		if resp != nil {
			resp.Body.Close()
		}
	}

	// Default fallback
	return fmt.Sprintf("https://iam.%s", serverHostname), "airavata", nil
}
