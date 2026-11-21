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

package batch

import (
	"context"
	"testing"

	"github.com/glebarez/sqlite"
	"gorm.io/gorm"
	"gorm.io/gorm/logger"

	computemodel "github.com/apache/airavata/api/compute/model"
	computerepo "github.com/apache/airavata/api/compute/repository"
	credmodel "github.com/apache/airavata/api/credentials/model"
	model "github.com/apache/airavata/api/process/model"
	processrepo "github.com/apache/airavata/api/process/repository"
	"github.com/apache/airavata/internal/db"
	ssh "github.com/apache/airavata/internal/ssh"
	"github.com/apache/airavata/internal/transport"
)

type revokedLinks struct{ revoked []string }

func (l *revokedLinks) Issue(context.Context, string) (*transport.IssuedLink, error) { return nil, nil }
func (l *revokedLinks) Revoke(_ context.Context, processID string) error {
	l.revoked = append(l.revoked, processID)
	return nil
}

// A stop must cancel every job by the process's name and remove the secrets' file it
// was handed, unless told to keep them, and must not log in for a process never launched.
func TestReleaseBatchResourcesRevokesCancelsAndRemoves(t *testing.T) {
	gdb, err := gorm.Open(sqlite.Open("file::memory:"), &gorm.Config{Logger: logger.Default.LogMode(logger.Silent)})
	if err != nil {
		t.Fatal(err)
	}
	if err := db.AutoMigrate(gdb); err != nil {
		t.Fatal(err)
	}
	for _, row := range []any{
		&credmodel.SSHCredential{ID: "cred", SSHCredentialName: "key", PublicKey: "pub", PrivateKey: "priv"},
		&computemodel.SlurmCluster{ID: "cluster", ClusterName: "c", HeadnodeHost: "login", HeadnodePort: 22, SlurmHome: ptr("/opt/slurm/bin")},
		&computemodel.SlurmClusterConfig{ID: "cfg", SlurmClusterID: "cluster", LoginUser: "u", WorkRoot: "/w", SSHCredentialID: ptr("cred")},
		&model.Process{ID: "p", OwnerID: ptr("alice"), BatchProcess: &model.BatchJobProcess{ID: "bp", SlurmClusterConfigID: "cfg", BatchJobConfigID: "bjc"}},
		&model.JobSubmissionTask{ProcessID: ptr("p"), WorkingDir: ptr("/w/p")},
	} {
		if err := gdb.Create(row).Error; err != nil {
			t.Fatalf("seed %T: %v", row, err)
		}
	}

	const scancel, rm = "'/opt/slurm/bin/scancel' --name='p'", "rm -f '/w/p/.airavata-link.env'"
	for _, tc := range []struct {
		name, processID, command string
	}{
		{"all released", "p", scancel + " && " + rm},
		{"never launched", "unlaunched", ""},
	} {
		t.Run(tc.name, func(t *testing.T) {
			links := &revokedLinks{}
			var ran []string
			tasks := &Tasks{
				processes:           processrepo.NewProcessRepository(gdb),
				jobSubmissionTasks:  processrepo.NewJobSubmissionTaskRepository(gdb),
				slurmClusterConfigs: computerepo.NewSlurmClusterConfigRepository(gdb),
				links:               links,
				run: func(_ context.Context, _ ssh.SSHDialConfig, command, _ string) (string, string, error) {
					ran = append(ran, command)
					return "", "", nil
				},
			}
			if err := tasks.ReleaseBatchResources(context.Background(), tc.processID); err != nil {
				t.Fatal(err)
			}
			if got := len(ran) == 1 && ran[0] == tc.command || len(ran) == 0 && tc.command == ""; !got {
				t.Errorf("ran %q, want %q", ran, tc.command)
			}
			if len(links.revoked) != 1 {
				t.Errorf("revoked = %v, want the process's link", links.revoked)
			}
		})
	}
}
