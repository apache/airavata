package orchestration

import (
	"context"
	"os"
	"strings"
	"testing"
	"time"
)

// rawMessage joins lines with CRLF, which is what a message read off the wire uses and
// what the MIME parser expects; a bare newline changes how a quoted-printable soft break
// or a multipart boundary is read.
func rawMessage(lines ...string) []byte {
	return []byte(strings.Join(lines, "\r\n"))
}

func TestMessageText(t *testing.T) {
	tests := []struct {
		name string
		raw  []byte
		want string
	}{
		{
			name: "plain text",
			raw: rawMessage(
				"From: slurm@cluster.example.org",
				"Subject: Slurm Job_id=4242 Name=solvate Ended, COMPLETED, ExitCode 0",
				"Content-Type: text/plain; charset=us-ascii",
				"",
				"Job 4242 finished.",
			),
			want: "Job 4242 finished.",
		},
		{
			name: "no content type defaults to text/plain",
			raw: rawMessage(
				"From: slurm@cluster.example.org",
				"Subject: Slurm Job_id=4242 Began",
				"",
				"Job 4242 started.",
			),
			want: "Job 4242 started.",
		},
		{
			name: "quoted-printable is decoded",
			raw: rawMessage(
				"Content-Type: text/plain; charset=utf-8",
				"Content-Transfer-Encoding: quoted-printable",
				"",
				"Run time =3D 00:12:31",
			),
			want: "Run time = 00:12:31",
		},
		{
			name: "base64 is decoded",
			raw: rawMessage(
				"Content-Type: text/plain; charset=utf-8",
				"Content-Transfer-Encoding: base64",
				"",
				"Sm9iIDQyNDIgZmFpbGVkLg==",
			),
			want: "Job 4242 failed.",
		},
		{
			name: "multipart prefers the plain text part",
			raw: rawMessage(
				`Content-Type: multipart/alternative; boundary="BOUND"`,
				"",
				"--BOUND",
				"Content-Type: text/html; charset=utf-8",
				"",
				"<p>Job 4242 finished.</p>",
				"--BOUND",
				"Content-Type: text/plain; charset=utf-8",
				"",
				"Job 4242 finished.",
				"--BOUND--",
				"",
			),
			want: "Job 4242 finished.",
		},
		{
			name: "html only falls back to the html part",
			raw: rawMessage(
				`Content-Type: multipart/alternative; boundary="BOUND"`,
				"",
				"--BOUND",
				"Content-Type: text/html; charset=utf-8",
				"",
				"<p>Job 4242 finished.</p>",
				"--BOUND--",
				"",
			),
			want: "<p>Job 4242 finished.</p>",
		},
		{
			name: "attachments are skipped",
			raw: rawMessage(
				`Content-Type: multipart/mixed; boundary="BOUND"`,
				"",
				"--BOUND",
				"Content-Type: text/plain; charset=utf-8",
				"",
				"See the attached log.",
				"--BOUND",
				"Content-Type: text/plain; charset=utf-8",
				`Content-Disposition: attachment; filename="slurm-4242.out"`,
				"",
				"lots of job output",
				"--BOUND--",
				"",
			),
			want: "See the attached log.",
		},
	}

	for _, test := range tests {
		t.Run(test.name, func(t *testing.T) {
			got, err := messageText(test.raw)
			if err != nil {
				t.Fatalf("messageText returned an error: %v", err)
			}
			if strings.TrimSpace(got) != test.want {
				t.Errorf("messageText = %q, want %q", strings.TrimSpace(got), test.want)
			}
		})
	}
}

// TestNewEmailMonitorStripsAppPasswordSpaces covers the paste Google's own UI invites:
// it shows an app password as four space-separated groups.
func TestNewEmailMonitorStripsAppPasswordSpaces(t *testing.T) {
	monitor := NewEmailMonitor(" monitor@gmail.com ", "abcd efgh ijkl mnop", 30, nil)

	if monitor.emailAddress != "monitor@gmail.com" {
		t.Errorf("emailAddress = %q, want %q", monitor.emailAddress, "monitor@gmail.com")
	}
	if monitor.appPassword != "abcdefghijklmnop" {
		t.Errorf("appPassword = %q, want %q", monitor.appPassword, "abcdefghijklmnop")
	}
	if got := monitor.serverAddr(); got != gmailIMAPAddr {
		t.Errorf("serverAddr = %q, want %q", got, gmailIMAPAddr)
	}
	if got := monitor.mailboxName(); got != defaultMailbox {
		t.Errorf("mailboxName = %q, want %q", got, defaultMailbox)
	}
}

// TestEmailMonitorValidate checks that a monitor missing a setting says which one,
// before it opens a connection that could only fail obscurely.
func TestEmailMonitorValidate(t *testing.T) {
	if err := NewEmailMonitor("", "secret", 30, nil).validate(); err == nil {
		t.Error("validate accepted a monitor with no address")
	}
	if err := NewEmailMonitor("monitor@gmail.com", "", 30, nil).validate(); err == nil {
		t.Error("validate accepted a monitor with no app password")
	}
	if err := NewEmailMonitor("monitor@gmail.com", "secret", 30, nil).validate(); err != nil {
		t.Errorf("validate rejected a complete monitor: %v", err)
	}
}

// Test this individually against a real mailbox:
//
//	AIRAVATA_EMAIL_MONITOR_ADDRESS=you@gmail.com \
//	AIRAVATA_EMAIL_MONITOR_APP_PASSWORD='abcd efgh ijkl mnop' \
//	go test -timeout 120s -run ^TestReadUnreadEmails$ github.com/apache/airavata/internal/orchestration -count=1 -v
//
// It reads every unread message in the mailbox and marks each one read, so point it at a
// mailbox whose unread mail you are willing to consume.
func TestReadUnreadEmails(t *testing.T) {
	address := os.Getenv("AIRAVATA_EMAIL_MONITOR_ADDRESS")
	appPassword := os.Getenv("AIRAVATA_EMAIL_MONITOR_APP_PASSWORD")
	if address == "" || appPassword == "" {
		t.Skip("Skipping test because AIRAVATA_EMAIL_MONITOR_ADDRESS and AIRAVATA_EMAIL_MONITOR_APP_PASSWORD are not set")
		return
	}

	var read []Email
	monitor := NewEmailMonitor(address, appPassword, 0, func(_ context.Context, email Email) error {
		read = append(read, email)
		return nil
	})
	if mailbox := os.Getenv("AIRAVATA_EMAIL_MONITOR_MAILBOX"); mailbox != "" {
		monitor.mailbox = mailbox
	}

	ctx, cancel := context.WithTimeout(t.Context(), 90*time.Second)
	defer cancel()

	startTime := time.Now()
	count, err := monitor.readUnreadEmails(ctx)
	if err != nil {
		t.Fatalf("Reading unread emails failed: %v", err)
	}
	if count != len(read) {
		t.Errorf("readUnreadEmails reported %d messages but the handler saw %d", count, len(read))
	}
	for _, email := range read {
		t.Logf("uid=%d from=%q date=%s subject=%q body=%q",
			email.UID, email.From, email.Date.Format(time.RFC3339), email.Subject, email.Body)
	}
	t.Logf("Read %d unread emails in %v", count, time.Since(startTime))

	// A second pass must come back empty: the first marked everything it handled read.
	count, err = monitor.readUnreadEmails(ctx)
	if err != nil {
		t.Fatalf("Second pass failed: %v", err)
	}
	if count != 0 {
		t.Errorf("Second pass read %d emails, want 0: the first pass did not mark them read", count)
	}
}
