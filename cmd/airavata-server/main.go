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

	iamrepo "github.com/apache/airavata/api/iam/repository"
	iamsvc "github.com/apache/airavata/api/iam/service"
	"github.com/apache/airavata/internal/app"
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

// run dispatches to the migrate subcommand when invoked as
// "airavata-migrate migrate <up|status>", or otherwise starts the server. Keeping
// migrations behind an explicit subcommand rather than a flag on the server itself
// matches INSTALL.md's production guidance to disable AIRAVATA_DB_AUTO_MIGRATE and
// apply schema changes as a reviewed, separate step.
func run() error {
	if len(os.Args) > 1 && os.Args[1] == "migrate" {
		return runMigrate(os.Args[2:])
	}
	return runServer()
}

// runMigrate applies or reports on versioned migrations (internal/db/migrations)
// against the configured database, then exits — it does not start the HTTP server.
func runMigrate(args []string) error {
	action := "up"
	if len(args) > 0 {
		action = args[0]
	}

	cfg, err := config.Load()
	if err != nil {
		return err
	}
	gdb, err := db.Open(db.DefaultConfig(cfg.DSN))
	if err != nil {
		return err
	}

	migrator := db.NewMigrator(gdb, db.Migrations())
	ctx := context.Background()

	switch action {
	case "up":
		applied, err := migrator.Up(ctx)
		if err != nil {
			return fmt.Errorf("migrate up: %w", err)
		}
		if len(applied) == 0 {
			slog.Info("no pending migrations")
			return nil
		}
		for _, m := range applied {
			slog.Info("applied migration", "version", m.Version, "description", m.Description)
		}
		return nil

	case "status":
		status, err := migrator.Status(ctx)
		if err != nil {
			return fmt.Errorf("migrate status: %w", err)
		}
		for _, s := range status {
			state := "pending"
			if s.Applied {
				state = fmt.Sprintf("applied at %s", s.AppliedAt.Format(time.RFC3339))
			}
			fmt.Printf("%04d  %-30s %s\n", s.Version, s.Description, state)
		}
		return nil

	default:
		return fmt.Errorf(`unknown migrate subcommand %q (want "up" or "status")`, action)
	}
}

func runServer() error {
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
	roles := iamrepo.DBRoleLookup{DB: gdb}

	var root *auth.RootTokenProvider
	if cfg.RootAccountEnabled {
		root = auth.NewRootTokenProvider(cfg.RootAccountToken)
		fmt.Print(root.Banner())
		if err := iamsvc.EnsureRootUser(context.Background(), gdb); err != nil {
			return fmt.Errorf("ensure root user: %w", err)
		}
	}
	introspector := auth.NewCILogonIntrospector(
		cfg.IntrospectionURI, cfg.UserInfoURI, cfg.ClientID, cfg.ClientSecret, roles, root)

	// The object graph, built once. The HTTP handler takes it, and so does the workflow
	// worker once it has a backend to run against — both act through the same services
	// rather than each assembling a set of their own.
	svcs := app.New(cfg, gdb)

	srv := &http.Server{
		Addr:              cfg.HTTPAddr,
		Handler:           server.New(cfg, svcs, introspector),
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
