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

package orchestration

import (
	"os"
	"testing"
	"time"

	credmodel "github.com/apache/airavata/api/credentials/model"
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

	err = DownloadFileFromSCP(t.Context(), host, 22, username, credmodel.SSHKey{
		PrivateKey: privateKeyStr,
		Passphrase: &passphrase,
	}, "/home/"+username+"/random.bin", "/tmp/random.bin")
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

	startTime := time.Now()

	err = UploadFileToSCP(t.Context(), host, 22, username, credmodel.SSHKey{
		PrivateKey: privateKeyStr,
		Passphrase: &passphrase,
	}, "/tmp/random.bin", "/home/"+username+"/randomupload.bin")
	if err != nil {
		t.Fatalf("SCP upload failed: %v", err)
	}
	t.Logf("SCP upload completed in %v", time.Since(startTime))

	metadata, err := GetSCPFileMetadata(t.Context(), host, 22, username, credmodel.SSHKey{
		PrivateKey: privateKeyStr,
		Passphrase: &passphrase,
	}, "/home/"+username+"/randomupload.bin")

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
