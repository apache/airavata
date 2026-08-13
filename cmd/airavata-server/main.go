// Command airavata-server runs the Airavata API.
package main

import (
	"context"
	"errors"
	"fmt"
	"log/slog"
	"net/http"
	"os"
	"os/signal"
	"syscall"
	"time"

	"github.com/apache/airavata/api/iam"
	"github.com/apache/airavata/internal/auth"
	"github.com/apache/airavata/internal/config"
	"github.com/apache/airavata/internal/db"
	"github.com/apache/airavata/internal/server"
)

func main() {
	if err := run(); err != nil {
		slog.Error("server failed", "error", err)
		os.Exit(1)
	}
}

func run() error {
	slog.Info("starting Airavata server")

	cfg, err := config.Load()
	if err != nil {
		return err
	}

	gdb, err := db.Open(db.DefaultConfig(cfg.DSN))
	if err != nil {
		return err
	}
	if cfg.AutoMigrate {
		if err := db.AutoMigrate(gdb); err != nil {
			return fmt.Errorf("migrate schema: %w", err)
		}
		slog.Info("schema is up to date")
	}

	// Roles come from the database rather than the hardcoded mock: the schema already
	// carries user_roles, and reading them is what makes an administrator an
	// administrator instead of a name in a switch statement.
	roles := iam.DBRoleLookup{DB: gdb}

	var root *auth.RootTokenProvider
	if cfg.RootAccountEnabled {
		root = auth.NewRootTokenProvider(cfg.RootAccountToken)
		fmt.Print(root.Banner())
		if err := iam.EnsureRootUser(context.Background(), gdb); err != nil {
			return fmt.Errorf("ensure root user: %w", err)
		}
	}
	introspector := auth.NewCILogonIntrospector(
		cfg.IntrospectionURI, cfg.UserInfoURI, cfg.ClientID, cfg.ClientSecret, roles, root)

	srv := &http.Server{
		Addr:              cfg.HTTPAddr,
		Handler:           server.New(cfg, gdb, introspector),
		ReadHeaderTimeout: 10 * time.Second,
		IdleTimeout:       120 * time.Second,
	}

	// Shut down on interrupt, giving in-flight requests a chance to finish rather
	// than dropping them mid-transaction.
	ctx, stop := signal.NotifyContext(context.Background(), os.Interrupt, syscall.SIGTERM)
	defer stop()

	errs := make(chan error, 1)
	go func() {
		slog.Info("listening", "addr", cfg.HTTPAddr)
		if err := srv.ListenAndServe(); err != nil && !errors.Is(err, http.ErrServerClosed) {
			errs <- err
		}
	}()

	select {
	case err := <-errs:
		return err
	case <-ctx.Done():
		slog.Info("shutting down")
	}

	shutdownCtx, cancel := context.WithTimeout(context.Background(), 20*time.Second)
	defer cancel()
	return srv.Shutdown(shutdownCtx)
}
