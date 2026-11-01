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

// Command airavata-mcp is a Model Context Protocol server for Apache Airavata.
package main

import (
	"context"
	"errors"
	"flag"
	"fmt"
	"log/slog"
	"net"
	"net/http"
	"os"
	"os/signal"
	"strconv"
	"sync"
	"syscall"
	"time"

	"github.com/modelcontextprotocol/go-sdk/mcp"

	"github.com/apache/airavata/internal/mcp/auth"
	"github.com/apache/airavata/internal/mcp/policy"
	"github.com/apache/airavata/internal/mcp/server"
)

var version = "dev"

const usage = `airavata-mcp: a Model Context Protocol server for Apache Airavata

Usage:
  airavata-mcp [serve] [flags]   run the MCP server (stdio by default)
  airavata-mcp login [--device]  sign in with CILogon and store the token in the OS keychain
  airavata-mcp logout            remove the stored token
  airavata-mcp ssh-auth          answer pending interactive SSH logins (OTP, Duo, password) in this terminal
  airavata-mcp version

Every flag can also be set with the environment variable shown in its help.
The token can be given in AIRAVATA_MCP_TOKEN instead of logging in; it is never a flag.
`

func main() {
	args := os.Args[1:]
	cmd := "serve"
	if len(args) > 0 && len(args[0]) > 0 && args[0][0] != '-' {
		cmd, args = args[0], args[1:]
	}
	ctx, stop := signal.NotifyContext(context.Background(), os.Interrupt, syscall.SIGTERM)
	defer stop()

	var err error
	switch cmd {
	case "serve":
		err = serve(ctx, args)
	case "login":
		err = login(ctx, args)
	case "logout":
		err = logout(args)
	case "ssh-auth":
		err = sshAuth(ctx, args)
	case "version":
		fmt.Println(version)
	case "help", "-h", "--help":
		fmt.Print(usage)
	default:
		fmt.Fprint(os.Stderr, usage)
		err = fmt.Errorf("unknown command %q", cmd)
	}
	if err != nil {
		fmt.Fprintln(os.Stderr, "airavata-mcp:", err)
		os.Exit(1)
	}
}

// env returns the environment value for key, or def.
func env(key, def string) string {
	if v, ok := os.LookupEnv(key); ok {
		return v
	}
	return def
}

func envBool(key string, def bool) bool {
	if v, ok := os.LookupEnv(key); ok {
		if b, err := strconv.ParseBool(v); err == nil {
			return b
		}
	}
	return def
}

type authFlags struct {
	apiURL, issuer, clientID string
}

func (a *authFlags) register(fs *flag.FlagSet) {
	fs.StringVar(&a.apiURL, "api-url", env("AIRAVATA_MCP_API_URL", "http://localhost:9095"), "airavata-server base URL (AIRAVATA_MCP_API_URL)")
	fs.StringVar(&a.issuer, "oauth-issuer", env("AIRAVATA_MCP_OAUTH_ISSUER", auth.DefaultIssuer), "OpenID Connect issuer for sign-in (AIRAVATA_MCP_OAUTH_ISSUER)")
	fs.StringVar(&a.clientID, "oauth-client-id", env("AIRAVATA_MCP_OAUTH_CLIENT_ID", ""), "public OAuth client id registered for airavata-mcp (AIRAVATA_MCP_OAUTH_CLIENT_ID)")
}

func (a *authFlags) config() auth.Config {
	return auth.Config{Issuer: a.issuer, ClientID: a.clientID, APIURL: a.apiURL, Out: os.Stderr}
}

// lazySession resolves the token and identity on first use, so the server starts without network access.
type lazySession struct {
	cfg auth.Config
	mu  sync.Mutex
	ts  auth.TokenSource
	who *auth.Principal
}

func (l *lazySession) resolve(ctx context.Context) error {
	l.mu.Lock()
	defer l.mu.Unlock()
	if l.ts != nil {
		return nil
	}
	ts, who, err := auth.NewTokenSource(ctx, l.cfg)
	if err != nil {
		return err
	}
	l.ts, l.who = ts, who
	return nil
}

func (l *lazySession) Token(ctx context.Context) (string, error) {
	if err := l.resolve(ctx); err != nil {
		return "", err
	}
	return l.ts.Token(ctx)
}

func (l *lazySession) Principal(ctx context.Context) (string, error) {
	if err := l.resolve(ctx); err != nil {
		return "", err
	}
	return l.who.Subject, nil
}

func serve(ctx context.Context, args []string) error {
	fs := flag.NewFlagSet("serve", flag.ContinueOnError)
	var a authFlags
	a.register(fs)
	allowWrite := fs.Bool("allow-write", envBool("AIRAVATA_MCP_ALLOW_WRITE", false), "enable tools that change anything (AIRAVATA_MCP_ALLOW_WRITE)")
	readOnly := fs.Bool("read-only", false, "force read-only, overriding --allow-write")
	toolsets := fs.String("toolsets", env("AIRAVATA_MCP_TOOLSETS", "compute,applications,data,processes"), "comma-separated toolsets: compute, applications, data, processes, sharing (AIRAVATA_MCP_TOOLSETS)")
	lockdown := fs.Bool("lockdown", envBool("AIRAVATA_MCP_LOCKDOWN", false), "only surface descriptions written by you and members of your groups (AIRAVATA_MCP_LOCKDOWN)")
	admin := fs.Bool("admin", envBool("AIRAVATA_MCP_ADMIN", false), "enable the admin toolset; stdio only, and the signed-in user must be an Airavata ADMIN (AIRAVATA_MCP_ADMIN)")
	policyPath := fs.String("policy", env("AIRAVATA_MCP_POLICY", ""), "policy file with deny/confirm lists and local caps (AIRAVATA_MCP_POLICY; default "+policy.DefaultPath()+")")
	transport := fs.String("transport", env("AIRAVATA_MCP_TRANSPORT", "stdio"), "stdio, or http for a localhost-only Streamable HTTP endpoint used in testing (AIRAVATA_MCP_TRANSPORT)")
	httpAddr := fs.String("http-addr", env("AIRAVATA_MCP_HTTP_ADDR", "127.0.0.1:8095"), "listen address for --transport http; must be a loopback address (AIRAVATA_MCP_HTTP_ADDR)")
	skipConfirm := fs.Bool("unsafe-skip-confirmation", envBool("AIRAVATA_MCP_UNSAFE_SKIP_CONFIRMATION", false), "run writes without the confirmation form, leaving approval to the client's tool-approval prompt; only for clients that cannot show elicitation forms (AIRAVATA_MCP_UNSAFE_SKIP_CONFIRMATION)")
	if err := fs.Parse(args); err != nil {
		return err
	}

	explicit := *policyPath != ""
	path := *policyPath
	if !explicit {
		path = policy.DefaultPath()
	}
	file, err := policy.LoadFile(path, explicit)
	if err != nil {
		return err
	}
	pol := &policy.Policy{AllowWrite: *allowWrite && !*readOnly, Admin: *admin, Lockdown: *lockdown,
		Toolsets: policy.ParseList(*toolsets), SkipConfirmation: *skipConfirm, File: file}
	if err := pol.Validate(*transport); err != nil {
		return err
	}

	logger := slog.New(slog.NewTextHandler(os.Stderr, &slog.HandlerOptions{Level: slog.LevelWarn}))
	if pol.SkipConfirmation && pol.AllowWrite {
		logger.Warn("--unsafe-skip-confirmation: writes run without a confirmation form; only the client's tool-approval prompt stands in the way, and a tool set to always allow runs unconfirmed")
	}
	sess := &lazySession{cfg: a.config()}
	s, watcher, err := server.New(server.Options{Version: version, APIURL: a.apiURL, Token: sess.Token, Principal: sess.Principal,
		Policy: pol, Transport: *transport, Logger: logger, Audit: os.Stderr})
	if err != nil {
		return err
	}
	defer watcher.Close()

	switch *transport {
	case "stdio":
		return s.Run(ctx, &mcp.StdioTransport{})
	case "http":
		return serveHTTP(ctx, s, *httpAddr)
	}
	return fmt.Errorf("unknown transport %q (stdio or http)", *transport)
}

// serveHTTP serves stateless Streamable HTTP on a loopback address only.
func serveHTTP(ctx context.Context, s *mcp.Server, addr string) error {
	host, _, err := net.SplitHostPort(addr)
	if err != nil {
		return err
	}
	if ip := net.ParseIP(host); host != "localhost" && (ip == nil || !ip.IsLoopback()) {
		return fmt.Errorf("--http-addr %s is not a loopback address; the hosted HTTP mode is not available yet", addr)
	}
	handler := mcp.NewStreamableHTTPHandler(func(*http.Request) *mcp.Server { return s }, &mcp.StreamableHTTPOptions{Stateless: true})
	mux := http.NewServeMux()
	mux.Handle("/mcp", http.NewCrossOriginProtection().Handler(handler))
	srv := &http.Server{Addr: addr, Handler: mux, ReadHeaderTimeout: 10 * time.Second}
	go func() {
		<-ctx.Done()
		shutdown, cancel := context.WithTimeout(context.Background(), 5*time.Second)
		defer cancel()
		_ = srv.Shutdown(shutdown)
	}()
	fmt.Fprintf(os.Stderr, "airavata-mcp listening on http://%s/mcp\n", addr)
	if err := srv.ListenAndServe(); err != nil && !errors.Is(err, http.ErrServerClosed) {
		return err
	}
	return nil
}

func login(ctx context.Context, args []string) error {
	fs := flag.NewFlagSet("login", flag.ContinueOnError)
	var a authFlags
	a.register(fs)
	device := fs.Bool("device", false, "use the device authorization grant (for SSH sessions and machines without a browser)")
	if err := fs.Parse(args); err != nil {
		return err
	}
	who, err := auth.Login(ctx, a.config(), *device)
	if err != nil {
		return err
	}
	name := who.Name
	if name == "" {
		name = who.Subject
	}
	fmt.Fprintf(os.Stderr, "Signed in as %s (%s). The token is stored in the OS keychain.\n", name, who.Subject)
	return nil
}

func logout(args []string) error {
	fs := flag.NewFlagSet("logout", flag.ContinueOnError)
	var a authFlags
	a.register(fs)
	if err := fs.Parse(args); err != nil {
		return err
	}
	if err := auth.Logout(a.config()); err != nil {
		return err
	}
	fmt.Fprintln(os.Stderr, "Signed out.")
	return nil
}
