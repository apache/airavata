// Package config loads runtime settings, carrying over the properties the Java
// service read from application.properties.
package config

import (
	"fmt"
	"net/url"
	"os"
	"strconv"
	"strings"
)

// Config is the resolved server configuration.
type Config struct {
	// HTTPAddr is the listen address, from server.port.
	HTTPAddr string

	// Database
	DSN string

	// AutoMigrate mirrors spring.jpa.hibernate.ddl-auto: on, the schema is created or
	// updated from the entity model at startup.
	AutoMigrate bool

	// CORSAllowedOrigins is airavata.cors.allowed-origins.
	CORSAllowedOrigins []string

	// CILogon
	IntrospectionURI string
	UserInfoURI      string
	ClientID         string
	ClientSecret     string

	// Root account
	RootAccountEnabled bool
	RootAccountToken   string
}

// Load reads configuration from the environment, applying the same defaults the Java
// application.properties declared.
func Load() (Config, error) {
	cfg := Config{
		HTTPAddr:    ":" + env("SERVER_PORT", "9095"),
		DSN:         env("AIRAVATA_DB_DSN", defaultDSN()),
		AutoMigrate: envBool("AIRAVATA_DB_AUTO_MIGRATE", true),

		CORSAllowedOrigins: strings.Split(env("AIRAVATA_CORS_ALLOWED_ORIGINS", "*"), ","),

		IntrospectionURI: env("CILOGON_INTROSPECTION_URI", "https://cilogon.org/oauth2/introspect"),
		UserInfoURI:      env("CILOGON_USERINFO_URI", "https://cilogon.org/oauth2/userinfo"),
		ClientID:         env("CILOGON_CLIENT_ID", ""),
		ClientSecret:     env("CILOGON_CLIENT_SECRET", ""),

		RootAccountEnabled: envBool("AIRAVATA_ROOT_ACCOUNT_ENABLED", true),
		RootAccountToken:   env("AIRAVATA_ROOT_ACCOUNT_TOKEN", ""),
	}

	// Without a root account and without CILogon credentials there is no way to
	// authenticate at all, which would leave every guarded endpoint permanently
	// unreachable. Fail at startup rather than at the first request.
	if !cfg.RootAccountEnabled && cfg.ClientID == "" {
		return Config{}, fmt.Errorf(
			"no authentication configured: enable the root account or set CILOGON_CLIENT_ID")
	}

	return cfg, nil
}

// defaultDSN builds the PostgreSQL connection URL from the same host, port, database,
// user and password defaults the Java service used.
//
// sslmode is disabled because the default host is localhost — a development database
// reached over a loopback socket. A deployment talking to a real server should set
// AIRAVATA_DB_SSLMODE to require (or stricter), or supply AIRAVATA_DB_DSN outright.
func defaultDSN() string {
	return fmt.Sprintf("postgres://%s@%s:%s/%s?sslmode=%s",
		url.UserPassword(env("AIRAVATA_DB_USER", "airavata"), env("AIRAVATA_DB_PASSWORD", "123456")).String(),
		env("AIRAVATA_DB_HOST", "localhost"),
		env("AIRAVATA_DB_PORT", "15432"),
		env("AIRAVATA_DB_NAME", "airavata"),
		env("AIRAVATA_DB_SSLMODE", "disable"),
	)
}

func env(key, fallback string) string {
	if v, ok := os.LookupEnv(key); ok && v != "" {
		return v
	}
	return fallback
}

func envBool(key string, fallback bool) bool {
	v, ok := os.LookupEnv(key)
	if !ok || v == "" {
		return fallback
	}
	parsed, err := strconv.ParseBool(v)
	if err != nil {
		return fallback
	}
	return parsed
}
