package config_test

import (
	"strings"
	"testing"

	"github.com/apache/airavata/internal/config"
)

// isolate clears every variable Load reads.
//
// Setting a variable to the empty string is equivalent to unsetting it, because Load
// treats an empty value as absent. Clearing them explicitly keeps the tests honest on
// a developer machine that already exports CILOGON_CLIENT_ID — otherwise the
// authentication guard silently reads as configured and the test proves nothing.
func isolate(t *testing.T) {
	t.Helper()
	for _, key := range []string{
		"SERVER_PORT",
		"AIRAVATA_DB_DSN", "AIRAVATA_DB_AUTO_MIGRATE",
		"AIRAVATA_DB_HOST", "AIRAVATA_DB_PORT", "AIRAVATA_DB_NAME",
		"AIRAVATA_DB_USER", "AIRAVATA_DB_PASSWORD",
		"AIRAVATA_CORS_ALLOWED_ORIGINS",
		"CILOGON_CLIENT_ID", "CILOGON_CLIENT_SECRET",
		"CILOGON_INTROSPECTION_URI", "CILOGON_USERINFO_URI",
		"AIRAVATA_ROOT_ACCOUNT_ENABLED", "AIRAVATA_ROOT_ACCOUNT_TOKEN",
	} {
		t.Setenv(key, "")
	}
}

func TestDefaults(t *testing.T) {
	isolate(t)

	cfg, err := config.Load()
	if err != nil {
		t.Fatalf("Load: %v", err)
	}

	if cfg.HTTPAddr != ":9095" {
		t.Errorf("HTTPAddr = %q, want :9095", cfg.HTTPAddr)
	}
	if !cfg.AutoMigrate {
		t.Error("AutoMigrate = false, want true by default")
	}
	if !cfg.RootAccountEnabled {
		t.Error("RootAccountEnabled = false, want true by default")
	}
	for _, want := range []string{"postgres://airavata:123456@localhost:15432/airavata", "sslmode=disable"} {
		if !strings.Contains(cfg.DSN, want) {
			t.Errorf("DSN = %q, want it to contain %q", cfg.DSN, want)
		}
	}
}

// Root account disabled with no identity provider leaves no way to authenticate, so
// every guarded endpoint would be permanently unreachable. That must fail at startup,
// not at the first request.
func TestRefusesToStartWithNoAuthentication(t *testing.T) {
	isolate(t)
	t.Setenv("AIRAVATA_ROOT_ACCOUNT_ENABLED", "false")

	if _, err := config.Load(); err == nil {
		t.Fatal("Load succeeded with no root account and no CILogon client, want an error")
	} else if !strings.Contains(err.Error(), "no authentication configured") {
		t.Errorf("error = %v, want it to name the missing authentication", err)
	}
}

// Disabling the root account is legitimate once CILogon is configured — that is the
// intended production posture.
func TestCILogonAloneIsEnough(t *testing.T) {
	isolate(t)
	t.Setenv("AIRAVATA_ROOT_ACCOUNT_ENABLED", "false")
	t.Setenv("CILOGON_CLIENT_ID", "some-client-id")

	cfg, err := config.Load()
	if err != nil {
		t.Fatalf("Load: %v", err)
	}
	if cfg.RootAccountEnabled {
		t.Error("RootAccountEnabled = true, want the explicit false to be honoured")
	}
}

// The root account alone is enough for a fresh deployment with no identity provider.
func TestRootAccountAloneIsEnough(t *testing.T) {
	isolate(t)

	if _, err := config.Load(); err != nil {
		t.Fatalf("Load: %v", err)
	}
}

func TestEnvironmentOverrides(t *testing.T) {
	isolate(t)
	t.Setenv("SERVER_PORT", "8080")
	t.Setenv("AIRAVATA_DB_AUTO_MIGRATE", "false")
	t.Setenv("AIRAVATA_CORS_ALLOWED_ORIGINS", "https://a.example.edu,https://b.example.edu")
	t.Setenv("AIRAVATA_ROOT_ACCOUNT_TOKEN", "fixed-token")

	cfg, err := config.Load()
	if err != nil {
		t.Fatalf("Load: %v", err)
	}
	if cfg.HTTPAddr != ":8080" {
		t.Errorf("HTTPAddr = %q, want :8080", cfg.HTTPAddr)
	}
	if cfg.AutoMigrate {
		t.Error("AutoMigrate = true, want the override to disable it")
	}
	if len(cfg.CORSAllowedOrigins) != 2 {
		t.Errorf("CORSAllowedOrigins = %v, want two entries", cfg.CORSAllowedOrigins)
	}
	if cfg.RootAccountToken != "fixed-token" {
		t.Errorf("RootAccountToken = %q, want the pinned value", cfg.RootAccountToken)
	}
}

// A full DSN replaces the assembled one, which is how a managed database or TLS
// options get configured.
func TestDSNOverrideWins(t *testing.T) {
	isolate(t)
	t.Setenv("AIRAVATA_DB_HOST", "ignored.example.edu")
	t.Setenv("AIRAVATA_DB_DSN", "user:pw@tcp(db.example.edu:3306)/airavata?tls=true")

	cfg, err := config.Load()
	if err != nil {
		t.Fatalf("Load: %v", err)
	}
	if !strings.Contains(cfg.DSN, "db.example.edu") || strings.Contains(cfg.DSN, "ignored") {
		t.Errorf("DSN = %q, want the explicit DSN to win", cfg.DSN)
	}
}

// A malformed boolean falls back to the default rather than being read as false,
// so a typo cannot quietly disable migrations or the root account.
func TestUnparseableBooleanFallsBackToDefault(t *testing.T) {
	isolate(t)
	t.Setenv("AIRAVATA_ROOT_ACCOUNT_ENABLED", "yes-please")

	cfg, err := config.Load()
	if err != nil {
		t.Fatalf("Load: %v", err)
	}
	if !cfg.RootAccountEnabled {
		t.Error("RootAccountEnabled = false, want the default to survive an unparseable value")
	}
}
