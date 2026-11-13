/**
*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
 */

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
	"github.com/apache/airavata/internal/audit"
	"github.com/apache/airavata/internal/auth"
	"github.com/apache/airavata/internal/config"
	"github.com/apache/airavata/internal/db"
	"github.com/apache/airavata/internal/orchestration/tasks/monitoring"
	"github.com/apache/airavata/internal/server"
	"github.com/cschleiden/go-workflows/backend"
	"github.com/cschleiden/go-workflows/backend/sqlite"
)

func main() {
	if err := run(); err != nil {
		slog.Error("server failed", "error", err)
		os.Exit(1)
	}
}

// run dispatches to the migrate or seed subcommand when invoked as
// "airavata-server migrate <up|status>" or "airavata-server seed", or otherwise starts the server.
func run() error {
	if len(os.Args) > 1 {
		switch os.Args[1] {
		case "migrate":
			return runMigrate(os.Args[2:])
		case "seed":
			return runSeed(os.Args[2:])
		}
	}
	return runServer()
}

// runSeed populates the database with development and mock Slurm resources, then exits.
func runSeed(args []string) error {
	for _, arg := range args {
		if arg == "-h" || arg == "--help" || arg == "help" {
			fmt.Println(`airavata-server seed: populate the database with development and mock Slurm resources.

Usage:
  airavata-server seed [flags]

Flags:
  -h, --help    Show this help message

Environment variables:
  AIRAVATA_DB_DSN              PostgreSQL connection string
  MOCK_SLURM_HOST              Mock Slurm cluster host (default: localhost)
  MOCK_SLURM_PORT              Mock Slurm cluster SSH port (default: 12222)
  MOCK_SLURM_PRIVATE_KEY_PATH  Path to dev SSH private key (default: embedded/compose key)
  MOCK_SLURM_PUBLIC_KEY_PATH   Path to dev SSH public key (default: embedded/compose key)`)
			return nil
		}
	}

	cfg, err := config.FetchSystemConfigs()
	if err != nil {
		return err
	}
	gdb, err := db.Open(db.DefaultConfig(cfg.DSN))
	if err != nil {
		return err
	}

	if err := db.AutoMigrate(gdb); err != nil {
		return fmt.Errorf("auto-migrate schema before seeding: %w", err)
	}

	ctx := context.Background()
	result, err := db.Seed(ctx, gdb)
	if err != nil {
		return fmt.Errorf("seed database: %w", err)
	}

	fmt.Println("========================================")
	fmt.Println("AIRAVATA DATABASE SEEDING COMPLETED")
	fmt.Println("========================================")
	fmt.Println("Users:")
	for _, u := range result.Users {
		fmt.Printf("  ✓ %s\n", u)
	}
	fmt.Println("Service Accounts:")
	for _, sa := range result.ServiceAccounts {
		fmt.Printf("  ✓ %s\n", sa)
	}
	fmt.Println("Groups:")
	for _, g := range result.Groups {
		fmt.Printf("  ✓ %s\n", g)
	}
	fmt.Println("Credentials:")
	for _, c := range result.Credentials {
		fmt.Printf("  ✓ %s\n", c)
	}
	fmt.Println("Clusters:")
	for _, cl := range result.Clusters {
		fmt.Printf("  ✓ %s\n", cl)
	}
	fmt.Println("Partitions:")
	for _, p := range result.Partitions {
		fmt.Printf("  ✓ %s\n", p)
	}
	fmt.Println("Cluster Configs:")
	for _, cc := range result.Configs {
		fmt.Printf("  ✓ %s\n", cc)
	}
	fmt.Println("Application Templates:")
	for _, t := range result.Templates {
		fmt.Printf("  ✓ %s\n", t)
	}
	fmt.Println("Deployments:")
	for _, d := range result.Deployments {
		fmt.Printf("  ✓ %s\n", d)
	}
	fmt.Println("Data Storages:")
	for _, s := range result.Storages {
		fmt.Printf("  ✓ %s\n", s)
	}
	fmt.Println("========================================")
	return nil
}

// runMigrate applies or reports on versioned migrations (internal/db/migrations)
// against the configured database, then exits — it does not start the HTTP server.
func runMigrate(args []string) error {
	action := "up"
	if len(args) > 0 {
		action = args[0]
	}

	cfg, err := config.FetchSystemConfigs()
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

	cfg, err := config.FetchSystemConfigs()
	if err != nil {
		return err
	}

	gdb, err := db.Open(db.DefaultConfig(cfg.DSN))
	if err != nil {
		return err
	}
	// Before anything writes, including the migration below and the root user.
	if err := audit.Register(gdb); err != nil {
		return fmt.Errorf("register audit log: %w", err)
	}
	if cfg.AutoMigrate {
		if err := db.AutoMigrate(gdb); err != nil {
			return fmt.Errorf("migrate schema: %w", err)
		}
		slog.Info("schema is up to date")
	}

	if cfg.AutoSeed {
		if _, err := db.Seed(context.Background(), gdb); err != nil {
			return fmt.Errorf("auto-seed database: %w", err)
		}
		slog.Info("database seeded successfully")
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
	// Service account tokens are Airavata-issued, so they are checked against the
	// database and never forwarded to CILogon.
	introspector.ServiceAccounts = iamrepo.ServiceAccountTokenLookup{DB: gdb}
	// Signing in with CILogon is registration: the first request creates the user record.
	introspector.Users = &iamrepo.CILogonUserProvisioner{DB: gdb}

	repos := app.NewRepositories(gdb)
	// The object graph, built once. The HTTP handler takes it, and so does the workflow
	// worker once it has a backend to run against — both act through the same services
	// rather than each assembling a set of their own.

	var workflowBackend backend.Backend
	if cfg.WorkflowBackendType == "sqlite" {
		workflowBackend = sqlite.NewSqliteBackend(cfg.WorkflowBackendSqlitePath)
	}

	if workflowBackend == nil {
		return fmt.Errorf("no workflow backend configured")
	}

	svcs := app.NewServices(cfg, gdb, repos, workflowBackend)

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

	emailMonitor := monitoring.NewEmailMonitor(cfg.EmailMonitorAddress, cfg.EmailMonitorAppPassword, 20, svcs.ExecutionEngine.HandleBatchJobEmailResponse)
	go emailMonitor.MonitorBatchJobEmails(ctx)

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
