package activities

import (
	"bufio"
	"bytes"
	"context"
	"errors"
	"fmt"
	"io"
	"log/slog"
	"net"
	"os"
	"path/filepath"
	"strconv"
	"strings"
	"time"

	credmodel "github.com/apache/airavata/api/credentials/model"
	"golang.org/x/crypto/ssh"
)

// Note: Only use DownloadFileFromSCP and UploadFileToSCP for SCP transfers. Do not use
// any of the utility methods outside of this file. They are considered internal implementation details. Also,
// DO NOT change internal implementation logic unless you fully understand the implications.

// Copies a single remote file to localPath over SSH
func DownloadFileFromSCP(ctx context.Context, host string, port int, username string, key credmodel.SSHKey, remotePath string, localPath string) error {
	target := fmt.Sprintf("scp download %s@%s:%s", username, host, remotePath)

	t, err := intializeSession(ctx, host, port, username, key, "-f", remotePath, target)
	if err != nil {
		return err
	}
	defer t.close()

	if err := receiveSCPFile(t.stdout, t.stdin, localPath); err != nil {
		return t.fail(err)
	}
	if err := t.finish(); err != nil {
		return err
	}

	slog.Info("Downloaded file over SCP", "host", host, "remotePath", remotePath, "localPath", localPath)
	return nil
}

// Uploads a single local file to a remote endpoint
func UploadFileToSCP(ctx context.Context, host string, port int, username string, key credmodel.SSHKey, localPath string, remotePath string) error {
	target := fmt.Sprintf("scp upload %s@%s:%s", username, host, remotePath)

	// Validate the local file before attempting the SCP upload.
	file, err := os.Open(localPath)
	if err != nil {
		return fmt.Errorf("%s: %w", target, err)
	}
	defer file.Close()
	info, err := file.Stat()
	if err != nil {
		return fmt.Errorf("%s: %w", target, err)
	}
	if info.IsDir() {
		return fmt.Errorf("%s: %s is a directory: recursive scp upload is not supported", target, localPath)
	}
	if !info.Mode().IsRegular() {
		return fmt.Errorf("%s: %s is not a regular file, so the size it would announce cannot be trusted", target, localPath)
	}

	t, err := intializeSession(ctx, host, port, username, key, "-t", remotePath, target)
	if err != nil {
		return err
	}
	defer t.close()

	if err := sendSCPFile(t.stdout, t.stdin, file, filepath.Base(localPath), info.Mode().Perm(), info.Size()); err != nil {
		return t.fail(err)
	}
	if err := t.finish(); err != nil {
		return err
	}

	slog.Info("Uploaded file over SCP", "host", host, "localPath", localPath, "remotePath", remotePath)
	return nil
}

// ========================== Utility section =====================================

// scpOK is the byte each side of the SCP wire protocol writes to acknowledge the
// other's last message. A leading 0x01 or 0x02 instead introduces a one-line error,
// which is the only way a remote scp reports a missing or unreadable path.
const scpOK = 0x00

// scpDialTimeout bounds the TCP connect and the SSH handshake behind it. A staging
// activity is retried by the workflow, so a host that is merely down should fail the
// attempt quickly rather than hold a worker slot until the OS gives up on the socket.
const scpDialTimeout = 30 * time.Second

type scpTransfer struct {
	client  *ssh.Client
	session *ssh.Session
	stdin   io.WriteCloser
	stdout  *bufio.Reader
	stderr  *bytes.Buffer
	ctx     context.Context
	done    chan struct{}
	target  string
}

func intializeSession(ctx context.Context, host string, port int, username string, key credmodel.SSHKey, direction, remotePath, target string) (*scpTransfer, error) {
	client, err := dialSSH(ctx, host, port, username, key)
	if err != nil {
		return nil, fmt.Errorf("%s: %w", target, err)
	}
	session, err := client.NewSession()
	if err != nil {
		client.Close()
		return nil, fmt.Errorf("%s: opening session: %w", target, err)
	}
	t := &scpTransfer{client: client, session: session, ctx: ctx, target: target, done: make(chan struct{})}
	if t.stdin, err = session.StdinPipe(); err != nil {
		session.Close()
		client.Close()
		return nil, fmt.Errorf("%s: %w", target, err)
	}
	stdout, err := session.StdoutPipe()
	if err != nil {
		session.Close()
		client.Close()
		return nil, fmt.Errorf("%s: %w", target, err)
	}
	t.stdout = bufio.NewReader(stdout)
	t.stderr = &bytes.Buffer{}
	session.Stderr = t.stderr

	// A session read blocks on the network, so cancellation has to arrive by closing
	// the session under it. done stops the watcher when the transfer finishes first.
	go func() {
		select {
		case <-ctx.Done():
			session.Close()
		case <-t.done:
		}
	}()

	if err := session.Start("scp " + direction + " " + shellQuote(remotePath)); err != nil {
		t.close()
		return nil, fmt.Errorf("%s: starting remote scp: %w", target, err)
	}
	return t, nil
}

func (t *scpTransfer) close() {
	close(t.done)
	t.session.Close()
	t.client.Close()
}

// fail names the transfer in err. A cancelled context takes precedence, because every
// read and write fails once the session is closed under it and the resulting I/O error
// says nothing useful. Whatever the remote scp wrote to stderr is folded in.
func (t *scpTransfer) fail(err error) error {
	if ctxErr := t.ctx.Err(); ctxErr != nil {
		return fmt.Errorf("%s: %w", t.target, ctxErr)
	}
	if remote := strings.TrimSpace(t.stderr.String()); remote != "" {
		return fmt.Errorf("%s: %w (remote: %s)", t.target, err, remote)
	}
	return fmt.Errorf("%s: %w", t.target, err)
}

// Graceful completion of the scp transfer. It closes the stdin to signal the end of the transfer
// and waits for the remote scp process to exit, returning any errors encountered.
func (t *scpTransfer) finish() error {
	if err := t.stdin.Close(); err != nil {
		return t.fail(err)
	}
	if err := t.session.Wait(); err != nil {
		return t.fail(fmt.Errorf("remote scp exited: %w", err))
	}
	return nil
}

// sendSCPFile drives the source half of the protocol: wait to be invited, announce the
// file, write exactly the announced bytes, close it with a zero byte of our own, and
// read the remote's verdict.
//
// Every step waits for an acknowledgement before the next, so a refusal — no such
// directory, permission denied, disk full — surfaces as an error here rather than as a
// transfer that appears to succeed and leaves nothing on the host.
func sendSCPFile(r *bufio.Reader, w io.Writer, src io.Reader, name string, mode os.FileMode, size int64) error {
	// A name is interpolated into a header line the remote parses, so a newline would
	// inject a second header and a slash would move the file out of the directory the
	// caller named.
	if name == "" || strings.ContainsAny(name, "\n/") {
		return fmt.Errorf("unusable remote file name %q", name)
	}
	if err := readSCPAck(r); err != nil {
		return err
	}
	if _, err := fmt.Fprintf(w, "C%04o %d %s\n", mode.Perm(), size, name); err != nil {
		return fmt.Errorf("sending scp file header: %w", err)
	}
	if err := readSCPAck(r); err != nil {
		return err
	}
	// Exactly size bytes: the header already committed to that count, so a file that
	// grew under us must not spill into the next protocol message, and one that shrank
	// has to fail rather than leave the remote waiting.
	if _, err := io.CopyN(w, src, size); err != nil {
		return fmt.Errorf("sending %d bytes: %w", size, err)
	}
	if err := scpAck(w); err != nil {
		return err
	}
	return readSCPAck(r)
}

// readSCPAck reads the remote's verdict on what was just sent. A zero byte accepts it;
// 0x01 or 0x02 introduce a one-line description of the refusal.
func readSCPAck(r *bufio.Reader) error {
	b, err := r.ReadByte()
	if err != nil {
		if errors.Is(err, io.EOF) {
			return errors.New("remote closed the connection without acknowledging (is scp installed on the host?)")
		}
		return fmt.Errorf("reading scp acknowledgement: %w", err)
	}
	if b == scpOK {
		return nil
	}
	line, err := r.ReadString('\n')
	if err != nil && !errors.Is(err, io.EOF) {
		return fmt.Errorf("reading scp error message: %w", err)
	}
	if line = strings.TrimRight(line, "\n"); line == "" {
		line = "unspecified error"
	}
	return fmt.Errorf("remote scp: %s", line)
}

func receiveSCPFile(r *bufio.Reader, w io.Writer, localPath string) error {
	if err := scpAck(w); err != nil {
		return err
	}
	for {
		msg, err := readSCPMessage(r)
		if err != nil {
			return err
		}
		switch msg[0] {
		case 'T':
			// Modification times, sent only under -p, which is not requested here.
			// Accept and read on rather than fail if a remote sends them anyway.
			if err := scpAck(w); err != nil {
				return err
			}
		case 'C':
			mode, size, err := parseSCPFileHeader(msg)
			if err != nil {
				return err
			}
			if err := scpAck(w); err != nil {
				return err
			}
			if err := writeLocalFile(r, localPath, mode, size); err != nil {
				return err
			}
			// The remote closes the file with its own acknowledgement byte.
			b, err := r.ReadByte()
			if err != nil {
				return fmt.Errorf("reading end of file marker: %w", err)
			}
			if b != scpOK {
				return fmt.Errorf("remote reported a transfer error after sending %d bytes", size)
			}
			return scpAck(w)
		case 'D':
			return fmt.Errorf("remote path is a directory: recursive scp download is not supported")
		default:
			return fmt.Errorf("unexpected scp message %q", msg)
		}
	}
}

// readSCPMessage reads one control line, turning the remote's error messages into
// errors. The returned string keeps its leading type byte.
func readSCPMessage(r *bufio.Reader) (string, error) {
	kind, err := r.ReadByte()
	if err != nil {
		if errors.Is(err, io.EOF) {
			return "", errors.New("remote closed the connection without sending a file")
		}
		return "", fmt.Errorf("reading scp message: %w", err)
	}
	line, err := r.ReadString('\n')
	if err != nil && !errors.Is(err, io.EOF) {
		return "", fmt.Errorf("reading scp message: %w", err)
	}
	line = strings.TrimRight(line, "\n")

	switch kind {
	case 0x01, 0x02:
		if line == "" {
			line = "unspecified error"
		}
		return "", fmt.Errorf("remote scp: %s", line)
	case scpOK:
		return "", errors.New("unexpected scp acknowledgement while awaiting a file header")
	}
	return string(kind) + line, nil
}

// parseSCPFileHeader reads a "C<mode> <size> <name>" header. The name is the remote's
// basename and is deliberately ignored: the caller chose where the bytes land, and
// honouring a name the remote picked would let it write outside that path.
func parseSCPFileHeader(msg string) (os.FileMode, int64, error) {
	fields := strings.SplitN(msg[1:], " ", 3)
	if len(fields) != 3 {
		return 0, 0, fmt.Errorf("malformed scp file header %q", msg)
	}
	mode, err := strconv.ParseUint(fields[0], 8, 32)
	if err != nil {
		return 0, 0, fmt.Errorf("malformed mode in scp file header %q", msg)
	}
	size, err := strconv.ParseInt(fields[1], 10, 64)
	if err != nil || size < 0 {
		return 0, 0, fmt.Errorf("malformed size in scp file header %q", msg)
	}
	return os.FileMode(mode).Perm(), size, nil
}

// writeLocalFile stages size bytes into a sibling temporary file and renames it into
// place, so a transfer that fails part way through cannot leave a truncated file where
// a later task would read it as the staged input.
func writeLocalFile(r io.Reader, localPath string, mode os.FileMode, size int64) error {
	dir := filepath.Dir(localPath)
	if err := os.MkdirAll(dir, 0o755); err != nil {
		return fmt.Errorf("creating %s: %w", dir, err)
	}
	tmp, err := os.CreateTemp(dir, filepath.Base(localPath)+".part-*")
	if err != nil {
		return fmt.Errorf("creating staging file in %s: %w", dir, err)
	}
	tmpName := tmp.Name()
	// Both are no-ops once the transfer has closed and renamed the file.
	defer tmp.Close()
	defer os.Remove(tmpName)

	if _, err := io.CopyN(tmp, r, size); err != nil {
		return fmt.Errorf("copying %d bytes into %s: %w", size, tmpName, err)
	}
	if err := tmp.Chmod(mode); err != nil {
		return fmt.Errorf("setting mode on %s: %w", tmpName, err)
	}
	if err := tmp.Close(); err != nil {
		return fmt.Errorf("closing %s: %w", tmpName, err)
	}
	if err := os.Rename(tmpName, localPath); err != nil {
		return fmt.Errorf("moving %s into place at %s: %w", tmpName, localPath, err)
	}
	return nil
}

func scpAck(w io.Writer) error {
	if _, err := w.Write([]byte{scpOK}); err != nil {
		return fmt.Errorf("acknowledging scp message: %w", err)
	}
	return nil
}

func dialSSH(ctx context.Context, host string, port int, username string, key credmodel.SSHKey) (*ssh.Client, error) {
	signer, err := signerFor(key)
	if err != nil {
		return nil, err
	}
	hostKeys := ssh.InsecureIgnoreHostKey()
	if port == 0 {
		port = 22
	}
	addr := net.JoinHostPort(host, strconv.Itoa(port))

	// DialContext rather than ssh.Dial: the activity's context has to be able to
	// abandon a connect that is going nowhere.
	dialer := net.Dialer{Timeout: scpDialTimeout}
	conn, err := dialer.DialContext(ctx, "tcp", addr)
	if err != nil {
		return nil, fmt.Errorf("connecting to %s: %w", addr, err)
	}
	if deadline, ok := ctx.Deadline(); ok {
		_ = conn.SetDeadline(deadline)
	}
	sshConn, chans, reqs, err := ssh.NewClientConn(conn, addr, &ssh.ClientConfig{
		User:            username,
		Auth:            []ssh.AuthMethod{ssh.PublicKeys(signer)},
		HostKeyCallback: hostKeys,
		Timeout:         scpDialTimeout,
	})
	if err != nil {
		conn.Close()
		return nil, fmt.Errorf("ssh handshake with %s: %w", addr, err)
	}
	// The handshake deadline must not outlive the handshake: a transfer reads for as
	// long as the file takes.
	_ = conn.SetDeadline(time.Time{})
	return ssh.NewClient(sshConn, chans, reqs), nil
}

func signerFor(key credmodel.SSHKey) (ssh.Signer, error) {
	pem := []byte(key.PrivateKey)
	if key.Passphrase != nil && *key.Passphrase != "" {
		signer, err := ssh.ParsePrivateKeyWithPassphrase(pem, []byte(*key.Passphrase))
		if err != nil {
			return nil, fmt.Errorf("ssh key %s could not be decrypted with its registered passphrase", key.ID)
		}
		return signer, nil
	}
	signer, err := ssh.ParsePrivateKey(pem)
	if err != nil {
		return nil, fmt.Errorf("ssh key %s could not be parsed (is it passphrase protected without a registered passphrase?)", key.ID)
	}
	return signer, nil
}

// shellQuote wraps s for the remote login shell, which is what runs the scp command
// line. A path is user supplied, so it cannot be interpolated bare.
func shellQuote(s string) string {
	return "'" + strings.ReplaceAll(s, "'", `'\''`) + "'"
}
