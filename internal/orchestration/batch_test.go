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

import "testing"

// The id has to survive whatever a login shell prints around it, and a submission that
// announced no id at all has to be a failure rather than an empty job id recorded
// against the run.
func TestParseSbatchJobID(t *testing.T) {
	cases := []struct {
		name    string
		out     string
		want    string
		wantErr bool
	}{
		{name: "plain", out: "Submitted batch job 4242\n", want: "4242"},
		{name: "no trailing newline", out: "Submitted batch job 4242", want: "4242"},
		{name: "federated cluster", out: "Submitted batch job 4242 on cluster delta\n", want: "4242"},
		{
			name: "after shell noise",
			out:  "Lmod is automatically replacing intel with gcc.\nSubmitted batch job 987654\n",
			want: "987654",
		},
		{name: "empty", out: "", wantErr: true},
		{name: "unrecognised", out: "sbatch: error: Batch job submission failed: Invalid account\n", wantErr: true},
		{name: "no digits", out: "Submitted batch job\n", wantErr: true},
	}

	for _, tc := range cases {
		t.Run(tc.name, func(t *testing.T) {
			got, err := parseSbatchJobID(tc.out)
			if tc.wantErr {
				if err == nil {
					t.Fatalf("parseSbatchJobID(%q) = %q, want an error", tc.out, got)
				}
				return
			}
			if err != nil {
				t.Fatalf("parseSbatchJobID(%q) returned %v", tc.out, err)
			}
			if got != tc.want {
				t.Errorf("parseSbatchJobID(%q) = %q, want %q", tc.out, got, tc.want)
			}
		})
	}
}
