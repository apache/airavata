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
	"errors"
	"testing"

	model "github.com/apache/airavata/api/process/model"
)

func TestParseBatchJobSubject(t *testing.T) {
	const jobName = "456dec76-12cc-4d90-a622-977a6715a89b"

	tests := []struct {
		name    string
		subject string
		jobID   string
		status  model.BatchJobStatusType
	}{
		{
			name:    "began",
			subject: "Slurm Job_id=127 Name=" + jobName + " Began, Queued time 00:00:00",
			jobID:   "127",
			status:  model.BatchJobStatusBegin,
		},
		{
			name:    "failed",
			subject: "Slurm Job_id=127 Name=" + jobName + " Failed, Run time 00:00:01, FAILED, ExitCode 2",
			jobID:   "127",
			status:  model.BatchJobStatusFailed,
		},
		{
			name:    "ended",
			subject: "Slurm Job_id=4242 Name=" + jobName + " Ended, Run time 00:01:05, COMPLETED, ExitCode 0",
			jobID:   "4242",
			status:  model.BatchJobStatusEnded,
		},
		{
			name:    "requeued",
			subject: "Slurm Job_id=4242 Name=" + jobName + " Requeued, Run time 00:00:12",
			jobID:   "4242",
			status:  model.BatchJobStatusRequeued,
		},
		{
			// Two words before the comma, which is why the event is not the first word.
			name:    "staged out",
			subject: "Slurm Job_id=4242 Name=" + jobName + " Staged Out, Run time 00:01:05, COMPLETED, ExitCode 0",
			jobID:   "4242",
			status:  model.BatchJobStatusStageOut,
		},
		{
			name:    "invalid dependency",
			subject: "Slurm Job_id=4242 Name=" + jobName + " Invalid Dependency",
			jobID:   "4242",
			status:  model.BatchJobStatusInvalidDepend,
		},
		{
			name:    "reached time limit",
			subject: "Slurm Job_id=4242 Name=" + jobName + " Reached time limit, Run time 01:00:00",
			jobID:   "4242",
			status:  model.BatchJobStatusTimeLimit,
		},
		{
			// The three warnings differ only in a number, so all three are checked.
			name:    "reached 90% of time limit",
			subject: "Slurm Job_id=4242 Name=" + jobName + " Reached 90% of time limit, Run time 00:54:00",
			jobID:   "4242",
			status:  model.BatchJobStatusTimeLimit90,
		},
		{
			name:    "reached 80% of time limit",
			subject: "Slurm Job_id=4242 Name=" + jobName + " Reached 80% of time limit, Run time 00:48:00",
			jobID:   "4242",
			status:  model.BatchJobStatusTimeLimit80,
		},
		{
			name:    "reached 50% of time limit",
			subject: "Slurm Job_id=4242 Name=" + jobName + " Reached 50% of time limit, Run time 00:30:00",
			jobID:   "4242",
			status:  model.BatchJobStatusTimeLimit50,
		},
		{
			// Older releases spell the product in capitals, and a cluster runs whichever
			// version it runs.
			name:    "older releases shout their own name",
			subject: "SLURM Job_id=127 Name=" + jobName + " BEGAN, Queued time 00:00:00",
			jobID:   "127",
			status:  model.BatchJobStatusBegin,
		},
		{
			// A header arrives unfolded but not necessarily trimmed.
			name:    "surrounding whitespace",
			subject: "  Slurm Job_id=127 Name=" + jobName + " Began, Queued time 00:00:00  ",
			jobID:   "127",
			status:  model.BatchJobStatusBegin,
		},
		{
			// An array task's id is not an integer, which is why the id is kept as text.
			name:    "array task id",
			subject: "Slurm Job_id=127_3 Name=" + jobName + " Ended, Run time 00:01:05, COMPLETED, ExitCode 0",
			jobID:   "127_3",
			status:  model.BatchJobStatusEnded,
		},
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			got, err := parseBatchJobSubject(tt.subject)
			if err != nil {
				t.Fatalf("parse: %v", err)
			}
			if got.JobID != tt.jobID {
				t.Errorf("JobID = %q, want %q", got.JobID, tt.jobID)
			}
			if got.JobName != jobName {
				t.Errorf("JobName = %q, want %q", got.JobName, jobName)
			}
			if got.Status != tt.status {
				t.Errorf("Status = %q, want %q", got.Status, tt.status)
			}
		})
	}
}

// The mailbox is shared with whatever else is sent to it, so a subject that is not a
// notification has to be told apart from a notification reporting something unmapped:
// the first is ordinary mail, the second is a transition that was missed.
func TestParseBatchJobSubjectRejects(t *testing.T) {
	tests := []struct {
		name            string
		subject         string
		notNotification bool
	}{
		{name: "empty", subject: "", notNotification: true},
		{name: "ordinary mail", subject: "Your cluster allocation expires soon", notNotification: true},
		{name: "no job name", subject: "Slurm Job_id=127 Began, Queued time 00:00:00", notNotification: true},
		{name: "no event", subject: "Slurm Job_id=127 Name=456dec76", notNotification: true},
		{
			// Shaped like a notification, but reporting something the job never asked
			// for: worth an error rather than a silent drop.
			name:    "unrequested mail type",
			subject: "Slurm Job_id=127 Name=456dec76 Reached 25% of time limit, Run time 00:15:00",
		},
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			got, err := parseBatchJobSubject(tt.subject)
			if err == nil {
				t.Fatalf("parsed %q as %+v, want an error", tt.subject, got)
			}
			if errors.Is(err, ErrNotBatchJobNotification) != tt.notNotification {
				t.Errorf("ErrNotBatchJobNotification = %v, want %v (err: %v)",
					!tt.notNotification, tt.notNotification, err)
			}
		})
	}
}
