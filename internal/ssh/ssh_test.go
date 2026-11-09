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

package ssh

import (
	"context"
	"crypto/ed25519"
	"crypto/rand"
	"errors"
	"net"
	"os"
	"testing"
	"time"

	credmodel "github.com/apache/airavata/api/credentials/model"
	gossh "golang.org/x/crypto/ssh"
)

// Test this individually
// go test -test.fullpath=true -timeout 30s -run ^TestSCPDownload$ github.com/apache/airavata/internal/orchestration/activities -count=1 -v
func TestSCPDownload(t *testing.T) {
	// This is an integration test to verify SCP download functionality using SSH credentials

	// Read from environment variable
	privateKeyPath := os.Getenv("AIRAVATA_SSH_PRIVATE_KEY_PATH")
	if privateKeyPath == "" {
		t.Skip("Skipping test because AIRAVATA_SSH_PRIVATE_KEY_PATH is not set")
		return
	}
	privateKey, err := os.ReadFile(privateKeyPath)
	if err != nil {
		t.Fatalf("Failed to read private key from %s: %v", privateKeyPath, err)
	}
	privateKeyStr := string(privateKey)
	passphrase := os.Getenv("AIRAVATA_SSH_PRIVATE_KEY_PASSPHRASE")
	host := os.Getenv("AIRAVATA_SSH_HOST")
	username := os.Getenv("AIRAVATA_SSH_USERNAME")

	if host == "" || username == "" || passphrase == "" {
		t.Skip("Skipping test because required environment variables are not set")
		return
	}

	startTime := time.Now()
	sshContext := NewGlobalSSHContext()

	err = DownloadFileFromSCP(t.Context(), SSHDialConfig{
		host:        host,
		port:        22,
		sshUsername: username,
		cred: credmodel.SSHCredential{
			PrivateKey: privateKeyStr,
			Passphrase: &passphrase,
		},
	}, sshContext, "/home/"+username+"/random.bin", "/tmp/random.bin")
	if err != nil {
		t.Fatalf("SCP download failed: %v", err)
	}
	t.Logf("SCP download completed in %v", time.Since(startTime))
}

// Test this individually
// go test -test.fullpath=true -timeout 30s -run ^TestSCPUpload$ github.com/apache/airavata/internal/orchestration/activities -count=1 -v
func TestSCPUpload(t *testing.T) {
	// This is an integration test to verify SCP upload functionality using SSH credentials

	// Read from environment variable
	privateKeyPath := os.Getenv("AIRAVATA_SSH_PRIVATE_KEY_PATH")
	if privateKeyPath == "" {
		t.Skip("Skipping test because AIRAVATA_SSH_PRIVATE_KEY_PATH is not set")
		return
	}
	privateKey, err := os.ReadFile(privateKeyPath)
	if err != nil {
		t.Fatalf("Failed to read private key from %s: %v", privateKeyPath, err)
	}
	privateKeyStr := string(privateKey)
	passphrase := os.Getenv("AIRAVATA_SSH_PRIVATE_KEY_PASSPHRASE")
	host := os.Getenv("AIRAVATA_SSH_HOST")
	username := os.Getenv("AIRAVATA_SSH_USERNAME")

	if host == "" || username == "" || passphrase == "" {
		t.Skip("Skipping test because required environment variables are not set")
		return
	}

	// Create a file in tmp
	f, err := os.Create("/tmp/random.bin")
	if err != nil {
		t.Fatalf("Failed to create temporary file: %v", err)
	}
	defer f.Close()
	// Write random data to the file
	_, err = f.Write([]byte("random data"))
	if err != nil {
		t.Fatalf("Failed to write to temporary file: %v", err)
	}

	sshContext := NewGlobalSSHContext()

	startTime := time.Now()

	err = UploadFileToSCP(t.Context(), SSHDialConfig{
		host:        host,
		port:        22,
		sshUsername: username,
		cred: credmodel.SSHCredential{
			PrivateKey: privateKeyStr,
			Passphrase: &passphrase,
		},
	}, sshContext, "/tmp/random.bin", "/home/"+username+"/randomupload.bin")
	if err != nil {
		t.Fatalf("SCP upload failed: %v", err)
	}
	t.Logf("SCP upload completed in %v", time.Since(startTime))

	metadata, err := GetSCPFileMetadata(t.Context(), SSHDialConfig{
		host:        host,
		port:        22,
		sshUsername: username,
		cred: credmodel.SSHCredential{
			PrivateKey: privateKeyStr,
			Passphrase: &passphrase,
		},
	}, sshContext, "/home/"+username+"/randomupload.bin")

	if err != nil {
		t.Fatalf("Failed to get SCP file metadata: %v", err)
	}

	if metadata == nil {
		t.Fatalf("SCP file metadata is nil")
	}

	if metadata.Size != int64(len("random data")) {
		t.Fatalf("SCP file size mismatch: expected %d, got %d", len("random data"), metadata.Size)
	}

	if !metadata.Mode.Perm().IsRegular() {
		t.Fatalf("SCP file mode mismatch: expected a regular file")
	}
	t.Logf("SCP file metadata: %+v", metadata)
}

// sshServer serves config and closes each connection after grace, like sshd's LoginGraceTime.
func sshServer(t *testing.T, config *gossh.ServerConfig, grace time.Duration) SSHDialConfig {
	_, private, _ := ed25519.GenerateKey(rand.Reader)
	signer, _ := gossh.NewSignerFromKey(private)
	config.AddHostKey(signer)
	listener, err := net.Listen("tcp", "127.0.0.1:0")
	if err != nil {
		t.Fatal(err)
	}
	t.Cleanup(func() { listener.Close() })
	go func() {
		for conn, err := listener.Accept(); err == nil; conn, err = listener.Accept() {
			time.AfterFunc(grace, func() { conn.Close() })
			go serveNoChannels(conn, config)
		}
	}()
	return NewSSHDialConfig("127.0.0.1", listener.Addr().(*net.TCPAddr).Port, "alice", credmodel.SSHCredential{ID: "c1", FullInteractive: true}, "alice")
}

func TestStoredPasswordLeavesOnlyFurtherPromptsToTheUser(t *testing.T) {
	password := "stored-pw"
	answers := map[string]string{"One-time password: ": "654321", "Password: ": password, "Passcode: ": "123456"}
	dial, sshContext := sshServer(t, &gossh.ServerConfig{KeyboardInteractiveCallback: func(_ gossh.ConnMetadata, ask gossh.KeyboardInteractiveChallenge) (*gossh.Permissions, error) {
		for _, prompt := range []string{"One-time password: ", "Password: ", "Passcode: "} {
			if got, err := ask("", "", []string{prompt}, []bool{false}); err != nil || got[0] != answers[prompt] {
				return nil, errors.New("refused")
			}
		}
		return nil, nil
	}}, time.Minute), NewGlobalSSHContext()
	dial.cred.Password = &password
	t.Cleanup(sshContext.ConnectionPool.Close)
	done := make(chan error, 1)
	go func() {
		_, err := dialSSH(context.Background(), dial, sshContext)
		done <- err
	}()

	var asked []string
	for pending, changed := sshContext.InteractiveAuthStore.NextAuthForUser("alice"); ; pending, changed = sshContext.InteractiveAuthStore.NextAuthForUser("alice") {
		if pending != nil {
			prompts, _ := pending.Read()
			asked = append(asked, prompts[0].Text)
			_ = pending.SetResponse([]string{answers[prompts[0].Text]}, nil)
			continue
		}
		select {
		case <-changed:
		case err := <-done:
			if err != nil || len(asked) != 2 || asked[0] != "One-time password: " || asked[1] != "Passcode: " {
				t.Fatalf("login: %v; the user was asked %q, want only the one-time password and the passcode", err, asked)
			}
			return
		case <-time.After(5 * time.Second):
			t.Fatal("the login neither prompted nor finished")
		}
	}
}
