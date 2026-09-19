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
