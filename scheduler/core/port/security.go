package ports

import (
	"context"
	"time"
)

// SecurityPort defines the interface for security operations
// This abstracts security implementations from domain services
type SecurityPort interface {
	// Encryption/Decryption
	Encrypt(ctx context.Context, data []byte, keyID string) ([]byte, error)
	Decrypt(ctx context.Context, encryptedData []byte, keyID string) ([]byte, error)
	GenerateKey(ctx context.Context, keyID string) error
	RotateKey(ctx context.Context, keyID string) error
	DeleteKey(ctx context.Context, keyID string) error

	// Hashing
	Hash(ctx context.Context, data []byte, algorithm string) ([]byte, error)
	VerifyHash(ctx context.Context, data, hash []byte, algorithm string) (bool, error)

	// Token operations
	GenerateToken(ctx context.Context, claims map[string]interface{}, ttl time.Duration) (string, error)
	ValidateToken(ctx context.Context, token string) (map[string]interface{}, error)
	RefreshToken(ctx context.Context, token string, ttl time.Duration) (string, error)
	RevokeToken(ctx context.Context, token string) error

	// Password operations
	HashPassword(ctx context.Context, password string) (string, error)
	VerifyPassword(ctx context.Context, password, hash string) (bool, error)

	// Random generation
	GenerateRandomBytes(ctx context.Context, length int) ([]byte, error)
	GenerateRandomString(ctx context.Context, length int) (string, error)
	GenerateUUID(ctx context.Context) (string, error)
}

// AuthPort defines the interface for authentication operations
type AuthPort interface {
	// User authentication
	AuthenticateUser(ctx context.Context, username, password string) (*User, error)
	AuthenticateToken(ctx context.Context, token string) (*User, error)
	RefreshUserToken(ctx context.Context, refreshToken string) (*TokenPair, error)
	LogoutUser(ctx context.Context, userID string) error

	// Session management
	CreateSession(ctx context.Context, userID string, metadata map[string]interface{}) (*Session, error)
	GetSession(ctx context.Context, sessionID string) (*Session, error)
	UpdateSession(ctx context.Context, sessionID string, metadata map[string]interface{}) error
	DeleteSession(ctx context.Context, sessionID string) error
	DeleteUserSessions(ctx context.Context, userID string) error

	// Permission checking
	CheckPermission(ctx context.Context, userID, resource, action string) (bool, error)
	CheckResourceAccess(ctx context.Context, userID, resourceID, resourceType string) (bool, error)
	CheckGroupMembership(ctx context.Context, userID, groupID string) (bool, error)

	// User management
	CreateUser(ctx context.Context, user *User) error
	GetUser(ctx context.Context, userID string) (*User, error)
	GetUserByUsername(ctx context.Context, username string) (*User, error)
	GetUserByEmail(ctx context.Context, email string) (*User, error)
	UpdateUser(ctx context.Context, user *User) error
	DeleteUser(ctx context.Context, userID string) error
	ChangePassword(ctx context.Context, userID, oldPassword, newPassword string) error

	// Group management
	CreateGroup(ctx context.Context, group *Group) error
	GetGroup(ctx context.Context, groupID string) (*Group, error)
	UpdateGroup(ctx context.Context, group *Group) error
	DeleteGroup(ctx context.Context, groupID string) error
	AddUserToGroup(ctx context.Context, userID, groupID, role string) error
	RemoveUserFromGroup(ctx context.Context, userID, groupID string) error
	GetUserGroups(ctx context.Context, userID string) ([]*Group, error)
	GetGroupMembers(ctx context.Context, groupID string) ([]*User, error)
}

// User represents an authenticated user
type User struct {
	ID        string                 `json:"id"`
	Username  string                 `json:"username"`
	Email     string                 `json:"email"`
	FullName  string                 `json:"fullName"`
	IsActive  bool                   `json:"isActive"`
	Roles     []string               `json:"roles"`
	Groups    []string               `json:"groups"`
	Metadata  map[string]interface{} `json:"metadata"`
	CreatedAt time.Time              `json:"createdAt"`
	UpdatedAt time.Time              `json:"updatedAt"`
}

// Group represents a user group
type Group struct {
	ID          string                 `json:"id"`
	Name        string                 `json:"name"`
	Description string                 `json:"description"`
	OwnerID     string                 `json:"ownerId"`
	IsActive    bool                   `json:"isActive"`
	Metadata    map[string]interface{} `json:"metadata"`
	CreatedAt   time.Time              `json:"createdAt"`
	UpdatedAt   time.Time              `json:"updatedAt"`
}

// Session represents a user session
type Session struct {
	ID           string                 `json:"id"`
	UserID       string                 `json:"userId"`
	IPAddress    string                 `json:"ipAddress"`
	UserAgent    string                 `json:"userAgent"`
	Metadata     map[string]interface{} `json:"metadata"`
	CreatedAt    time.Time              `json:"createdAt"`
	ExpiresAt    time.Time              `json:"expiresAt"`
	LastActivity time.Time              `json:"lastActivity"`
}

// TokenPair represents a pair of access and refresh tokens
type TokenPair struct {
	AccessToken  string    `json:"accessToken"`
	RefreshToken string    `json:"refreshToken"`
	ExpiresAt    time.Time `json:"expiresAt"`
	TokenType    string    `json:"tokenType"`
}

// SecurityConfig represents security configuration
type SecurityConfig struct {
	// Encryption
	EncryptionKeyID     string        `json:"encryptionKeyId"`
	KeyRotationPeriod   time.Duration `json:"keyRotationPeriod"`
	EncryptionAlgorithm string        `json:"encryptionAlgorithm"`

	// Hashing
	HashAlgorithm string `json:"hashAlgorithm"`
	SaltLength    int    `json:"saltLength"`
	HashRounds    int    `json:"hashRounds"`

	// JWT
	JWTSecret     string        `json:"jwtSecret"`
	JWTAccessTTL  time.Duration `json:"jwtAccessTtl"`
	JWTRefreshTTL time.Duration `json:"jwtRefreshTtl"`
	JWTIssuer     string        `json:"jwtIssuer"`
	JWTAudience   string        `json:"jwtAudience"`

	// Session
	SessionTTL             time.Duration `json:"sessionTtl"`
	SessionCleanupInterval time.Duration `json:"sessionCleanupInterval"`
	MaxSessionsPerUser     int           `json:"maxSessionsPerUser"`

	// Password
	MinPasswordLength   int  `json:"minPasswordLength"`
	RequireUppercase    bool `json:"requireUppercase"`
	RequireLowercase    bool `json:"requireLowercase"`
	RequireNumbers      bool `json:"requireNumbers"`
	RequireSpecialChars bool `json:"requireSpecialChars"`

	// Rate limiting
	MaxLoginAttempts     int           `json:"maxLoginAttempts"`
	LockoutDuration      time.Duration `json:"lockoutDuration"`
	RateLimitWindow      time.Duration `json:"rateLimitWindow"`
	RateLimitMaxRequests int           `json:"rateLimitMaxRequests"`
}

// Permission represents a permission
type Permission struct {
	Resource string   `json:"resource"`
	Actions  []string `json:"actions"`
}

// Role represents a role with permissions
type Role struct {
	ID          string       `json:"id"`
	Name        string       `json:"name"`
	Description string       `json:"description"`
	Permissions []Permission `json:"permissions"`
	CreatedAt   time.Time    `json:"createdAt"`
	UpdatedAt   time.Time    `json:"updatedAt"`
}

// AuditEvent represents a security audit event
type AuditEvent struct {
	ID         string                 `json:"id"`
	UserID     string                 `json:"userId"`
	Action     string                 `json:"action"`
	Resource   string                 `json:"resource"`
	ResourceID string                 `json:"resourceId"`
	IPAddress  string                 `json:"ipAddress"`
	UserAgent  string                 `json:"userAgent"`
	Success    bool                   `json:"success"`
	Details    map[string]interface{} `json:"details"`
	Timestamp  time.Time              `json:"timestamp"`
}
