package adapters

import (
	"context"
	"crypto/rand"
	"encoding/hex"
	"fmt"
	"time"

	"github.com/apache/airavata/scheduler/core/domain"
	ports "github.com/apache/airavata/scheduler/core/port"
	"github.com/golang-jwt/jwt/v5"
	"github.com/google/uuid"
	"golang.org/x/crypto/bcrypt"
)

// JWTClaims represents JWT claims
type JWTClaims struct {
	UserID   string `json:"user_id"`
	Username string `json:"username"`
	Email    string `json:"email"`
	IsAdmin  bool   `json:"is_admin"`
	jwt.RegisteredClaims
}

// JWTAdapter implements ports.SecurityPort using JWT and bcrypt
type JWTAdapter struct {
	secretKey []byte
	issuer    string
	audience  string
}

// NewJWTAdapter creates a new JWT security adapter
func NewJWTAdapter(secretKey string, issuer, audience string) *JWTAdapter {
	if secretKey == "" {
		// Generate a random secret key if none provided
		secretKey = generateRandomKey()
	}

	return &JWTAdapter{
		secretKey: []byte(secretKey),
		issuer:    issuer,
		audience:  audience,
	}
}

// Encrypt encrypts data using the specified key
func (s *JWTAdapter) Encrypt(ctx context.Context, data []byte, keyID string) ([]byte, error) {
	// Simple XOR encryption for demo purposes
	// In production, use proper encryption like AES
	key := s.secretKey
	encrypted := make([]byte, len(data))
	for i := range data {
		encrypted[i] = data[i] ^ key[i%len(key)]
	}
	return encrypted, nil
}

// Decrypt decrypts data using the specified key
func (s *JWTAdapter) Decrypt(ctx context.Context, encryptedData []byte, keyID string) ([]byte, error) {
	// Simple XOR decryption for demo purposes
	// In production, use proper decryption like AES
	key := s.secretKey
	decrypted := make([]byte, len(encryptedData))
	for i := range encryptedData {
		decrypted[i] = encryptedData[i] ^ key[i%len(key)]
	}
	return decrypted, nil
}

// GenerateKey generates a new encryption key
func (s *JWTAdapter) GenerateKey(ctx context.Context, keyID string) error {
	// In production, this would generate and store a new key
	return nil
}

// RotateKey rotates an encryption key
func (s *JWTAdapter) RotateKey(ctx context.Context, keyID string) error {
	// In production, this would rotate the key
	return nil
}

// DeleteKey deletes an encryption key
func (s *JWTAdapter) DeleteKey(ctx context.Context, keyID string) error {
	// In production, this would delete the key
	return nil
}

// Hash hashes data using the specified algorithm
func (s *JWTAdapter) Hash(ctx context.Context, data []byte, algorithm string) ([]byte, error) {
	// Simple hash implementation for demo purposes
	// In production, use proper hashing algorithms
	return data, nil
}

// VerifyHash verifies data against its hash
func (s *JWTAdapter) VerifyHash(ctx context.Context, data, hash []byte, algorithm string) (bool, error) {
	// Simple verification for demo purposes
	return true, nil
}

// GenerateToken generates a JWT token with claims
func (s *JWTAdapter) GenerateToken(ctx context.Context, claims map[string]interface{}, ttl time.Duration) (string, error) {
	now := time.Now()
	jwtClaims := jwt.MapClaims{
		"iat": now.Unix(),
		"exp": now.Add(ttl).Unix(),
		"iss": s.issuer,
		"aud": s.audience,
	}

	// Add custom claims
	for k, v := range claims {
		jwtClaims[k] = v
	}

	token := jwt.NewWithClaims(jwt.SigningMethodHS256, jwtClaims)
	tokenString, err := token.SignedString(s.secretKey)
	if err != nil {
		return "", fmt.Errorf("failed to sign token: %w", err)
	}

	return tokenString, nil
}

// ValidateToken validates a JWT token and returns claims
func (s *JWTAdapter) ValidateToken(ctx context.Context, tokenString string) (map[string]interface{}, error) {
	token, err := jwt.Parse(tokenString, func(token *jwt.Token) (interface{}, error) {
		if _, ok := token.Method.(*jwt.SigningMethodHMAC); !ok {
			return nil, fmt.Errorf("unexpected signing method: %v", token.Header["alg"])
		}
		return s.secretKey, nil
	})

	if err != nil {
		return nil, fmt.Errorf("failed to parse token: %w", err)
	}

	claims, ok := token.Claims.(jwt.MapClaims)
	if !ok || !token.Valid {
		return nil, fmt.Errorf("invalid token")
	}

	// Convert to map[string]interface{}
	result := make(map[string]interface{})
	for k, v := range claims {
		result[k] = v
	}

	return result, nil
}

// RefreshToken refreshes a JWT token
func (s *JWTAdapter) RefreshToken(ctx context.Context, tokenString string, ttl time.Duration) (string, error) {
	claims, err := s.ValidateToken(ctx, tokenString)
	if err != nil {
		return "", err
	}

	return s.GenerateToken(ctx, claims, ttl)
}

// RevokeToken revokes a JWT token
func (s *JWTAdapter) RevokeToken(ctx context.Context, tokenString string) error {
	// In production, this would add the token to a blacklist
	return nil
}

// HashPassword hashes a password using bcrypt
func (s *JWTAdapter) HashPassword(ctx context.Context, password string) (string, error) {
	hash, err := bcrypt.GenerateFromPassword([]byte(password), bcrypt.DefaultCost)
	if err != nil {
		return "", fmt.Errorf("failed to hash password: %w", err)
	}
	return string(hash), nil
}

// VerifyPassword verifies a password against its hash
func (s *JWTAdapter) VerifyPassword(ctx context.Context, password, hash string) (bool, error) {
	err := bcrypt.CompareHashAndPassword([]byte(hash), []byte(password))
	return err == nil, nil
}

// GenerateRandomBytes generates random bytes
func (s *JWTAdapter) GenerateRandomBytes(ctx context.Context, length int) ([]byte, error) {
	bytes := make([]byte, length)
	_, err := rand.Read(bytes)
	if err != nil {
		return nil, fmt.Errorf("failed to generate random bytes: %w", err)
	}
	return bytes, nil
}

// GenerateRandomString generates a random string
func (s *JWTAdapter) GenerateRandomString(ctx context.Context, length int) (string, error) {
	const charset = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
	bytes := make([]byte, length)
	_, err := rand.Read(bytes)
	if err != nil {
		return "", fmt.Errorf("failed to generate random string: %w", err)
	}

	for i, b := range bytes {
		bytes[i] = charset[b%byte(len(charset))]
	}
	return string(bytes), nil
}

// GenerateUUID generates a UUID
func (s *JWTAdapter) GenerateUUID(ctx context.Context) (string, error) {
	uuid := uuid.New()
	return uuid.String(), nil
}

// IsTokenRevoked checks if a token is revoked
func (s *JWTAdapter) IsTokenRevoked(tokenString string) (bool, error) {
	// In a production system, this would check the blacklist
	return false, nil
}

// GetTokenClaims extracts claims from a token without validation
func (s *JWTAdapter) GetTokenClaims(tokenString string) (*JWTClaims, error) {
	token, err := jwt.ParseWithClaims(tokenString, &JWTClaims{}, func(token *jwt.Token) (interface{}, error) {
		return s.secretKey, nil
	})

	if err != nil {
		return nil, fmt.Errorf("failed to parse token: %w", err)
	}

	claims, ok := token.Claims.(*JWTClaims)
	if !ok {
		return nil, fmt.Errorf("invalid token claims")
	}

	return claims, nil
}

const userKey contextKey = "user"

// ExtractUserFromContext extracts user from context
func (s *JWTAdapter) ExtractUserFromContext(ctx context.Context) (*domain.User, error) {
	user, ok := ctx.Value(userKey).(*domain.User)
	if !ok {
		return nil, fmt.Errorf("user not found in context")
	}
	return user, nil
}

// SetUserInContext sets user in context
func (s *JWTAdapter) SetUserInContext(ctx context.Context, user *domain.User) context.Context {
	return context.WithValue(ctx, userKey, user)
}

// RequireAuth middleware function
func (s *JWTAdapter) RequireAuth() func(next func(context.Context) error) func(context.Context) error {
	return func(next func(context.Context) error) func(context.Context) error {
		return func(ctx context.Context) error {
			user, err := s.ExtractUserFromContext(ctx)
			if err != nil {
				return fmt.Errorf("authentication required: %w", err)
			}
			if user == nil {
				return fmt.Errorf("authentication required")
			}
			return next(ctx)
		}
	}
}

// RequireAdmin middleware function
func (s *JWTAdapter) RequireAdmin() func(next func(context.Context) error) func(context.Context) error {
	return func(next func(context.Context) error) func(context.Context) error {
		return func(ctx context.Context) error {
			user, err := s.ExtractUserFromContext(ctx)
			if err != nil {
				return fmt.Errorf("authentication required: %w", err)
			}
			if !s.isUserAdmin(user) {
				return fmt.Errorf("admin privileges required")
			}
			return next(ctx)
		}
	}
}

// RequirePermission middleware function
func (s *JWTAdapter) RequirePermission(resource, action string) func(next func(context.Context) error) func(context.Context) error {
	return func(next func(context.Context) error) func(context.Context) error {
		return func(ctx context.Context) error {
			user, err := s.ExtractUserFromContext(ctx)
			if err != nil {
				return fmt.Errorf("authentication required: %w", err)
			}

			authorized, err := s.Authorize(ctx, user.ID, resource, action)
			if err != nil {
				return fmt.Errorf("authorization check failed: %w", err)
			}
			if !authorized {
				return fmt.Errorf("insufficient permissions for %s on %s", action, resource)
			}

			return next(ctx)
		}
	}
}

// Authorize checks if a user is authorized to perform an action on a resource
func (s *JWTAdapter) Authorize(ctx context.Context, userID, action, resource string) (bool, error) {
	// Simple authorization logic - in production, this would be more sophisticated
	// For now, just check if user exists and is active
	// In a real implementation, this would check RBAC permissions

	// Extract user from context or database
	// For demo purposes, assume all authenticated users are authorized
	return true, nil
}

// isUserAdmin checks if a user is an admin
func (s *JWTAdapter) isUserAdmin(user *domain.User) bool {
	if user.Metadata != nil {
		if isAdmin, ok := user.Metadata["isAdmin"].(bool); ok {
			return isAdmin
		}
	}
	return false
}

// generateRandomKey generates a random secret key
func generateRandomKey() string {
	bytes := make([]byte, 32)
	rand.Read(bytes)
	return hex.EncodeToString(bytes)
}

// Compile-time interface verification
var _ ports.SecurityPort = (*JWTAdapter)(nil)
