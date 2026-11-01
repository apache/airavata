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

package auth

import (
	"context"
	"crypto/rand"
	"crypto/subtle"
	"encoding/base64"
	"errors"
	"fmt"
	"net"
	"net/http"
	"os/exec"
	"runtime"
	"sync"
	"time"

	"golang.org/x/oauth2"
)

func Login(ctx context.Context, cfg Config, device bool) (*Principal, error) {
	cfg, err := cfg.withDefaults()
	if err != nil {
		return nil, err
	}
	ctx, cancel := context.WithTimeout(ctx, cfg.LoginTimeout)
	defer cancel()
	d, err := discover(ctx, cfg)
	if err != nil {
		return nil, err
	}
	var tok *oauth2.Token
	if device {
		tok, err = deviceLogin(ctx, cfg, d)
	} else {
		tok, err = browserLogin(ctx, cfg, d)
	}
	if err != nil {
		return nil, err
	}
	p, err := identify(ctx, cfg, d, tok.AccessToken)
	if err != nil {
		return nil, err
	}
	if err := saveEntry(cfg, entryFromToken(cfg, tok, p.RawSubject)); err != nil {
		return nil, err
	}
	return p, nil
}

func Logout(cfg Config) error {
	cfg, err := cfg.withDefaults()
	if err != nil {
		return err
	}
	return deleteEntry(cfg)
}

func deviceLogin(ctx context.Context, cfg Config, d *discovery) (*oauth2.Token, error) {
	if d.DeviceAuthorizationEndpoint == "" {
		return nil, fmt.Errorf("auth: %s does not advertise a device_authorization_endpoint; use browser login", cfg.Issuer)
	}
	oc := d.oauth(cfg, "")
	da, err := oc.DeviceAuth(cfg.ctx(ctx))
	if err != nil {
		return nil, fmt.Errorf("auth: device authorization: %w", err)
	}
	if da.VerificationURIComplete != "" {
		fmt.Fprintf(cfg.Out, "To sign in, visit:\n\n  %s\n\nand confirm the code: %s\n", da.VerificationURIComplete, da.UserCode)
	} else {
		fmt.Fprintf(cfg.Out, "To sign in, visit:\n\n  %s\n\nand enter the code: %s\n", da.VerificationURI, da.UserCode)
	}
	tok, err := oc.DeviceAccessToken(cfg.ctx(ctx), da)
	if err != nil {
		return nil, fmt.Errorf("auth: device sign-in: %w", err)
	}
	return tok, nil
}

type callbackResult struct {
	code string
	err  error
}

func browserLogin(ctx context.Context, cfg Config, d *discovery) (*oauth2.Token, error) {
	if d.AuthorizationEndpoint == "" {
		return nil, fmt.Errorf("auth: %s does not advertise an authorization_endpoint", cfg.Issuer)
	}
	ln, err := net.Listen("tcp", "127.0.0.1:0")
	if err != nil {
		return nil, fmt.Errorf("auth: starting loopback listener: %w", err)
	}
	redirect := fmt.Sprintf("http://127.0.0.1:%d/callback", ln.Addr().(*net.TCPAddr).Port)
	oc := d.oauth(cfg, redirect)
	state, err := randomString()
	if err != nil {
		ln.Close()
		return nil, err
	}
	verifier := oauth2.GenerateVerifier()
	authURL := oc.AuthCodeURL(state, oauth2.S256ChallengeOption(verifier))

	results := make(chan callbackResult, 1)
	var once sync.Once
	mux := http.NewServeMux()
	mux.HandleFunc("GET /callback", func(w http.ResponseWriter, r *http.Request) {
		res := readCallback(r, state)
		handled := false
		once.Do(func() {
			handled = true
			results <- res
		})
		w.Header().Set("Content-Type", "text/plain; charset=utf-8")
		switch {
		case !handled:
			w.WriteHeader(http.StatusConflict)
			fmt.Fprintln(w, "This sign-in request was already handled. You can close this window.")
		case res.err != nil:
			w.WriteHeader(http.StatusBadRequest)
			fmt.Fprintln(w, "Sign-in failed. Return to the terminal for details.")
		default:
			fmt.Fprintln(w, "Signed in to airavata-mcp. You can close this window.")
		}
	})
	srv := &http.Server{Handler: mux, ReadHeaderTimeout: 10 * time.Second}
	go srv.Serve(ln)
	defer func() {
		sctx, scancel := context.WithTimeout(context.Background(), 2*time.Second)
		defer scancel()
		srv.Shutdown(sctx)
	}()

	fmt.Fprintf(cfg.Out, "Opening your browser to sign in. If it does not open, visit:\n\n  %s\n\n", authURL)
	go func() { _ = cfg.OpenBrowser(authURL) }()

	var res callbackResult
	select {
	case <-ctx.Done():
		return nil, fmt.Errorf("auth: timed out waiting for browser sign-in: %w", ctx.Err())
	case res = <-results:
	}
	if res.err != nil {
		return nil, res.err
	}
	tok, err := oc.Exchange(cfg.ctx(ctx), res.code, oauth2.VerifierOption(verifier))
	if err != nil {
		return nil, fmt.Errorf("auth: exchanging authorization code: %w", err)
	}
	return tok, nil
}

func readCallback(r *http.Request, state string) callbackResult {
	q := r.URL.Query()
	if subtle.ConstantTimeCompare([]byte(q.Get("state")), []byte(state)) != 1 {
		return callbackResult{err: errors.New("auth: sign-in callback state mismatch; login aborted")}
	}
	if e := q.Get("error"); e != "" {
		if desc := q.Get("error_description"); desc != "" {
			e += ": " + desc
		}
		return callbackResult{err: fmt.Errorf("auth: authorization denied: %s", e)}
	}
	code := q.Get("code")
	if code == "" {
		return callbackResult{err: errors.New("auth: sign-in callback has no authorization code")}
	}
	return callbackResult{code: code}
}

func randomString() (string, error) {
	b := make([]byte, 32)
	if _, err := rand.Read(b); err != nil {
		return "", err
	}
	return base64.RawURLEncoding.EncodeToString(b), nil
}

func openBrowser(url string) error {
	var cmd *exec.Cmd
	switch runtime.GOOS {
	case "darwin":
		cmd = exec.Command("open", url)
	case "windows":
		cmd = exec.Command("rundll32", "url.dll,FileProtocolHandler", url)
	default:
		cmd = exec.Command("xdg-open", url)
	}
	if err := cmd.Start(); err != nil {
		return err
	}
	go cmd.Wait()
	return nil
}
