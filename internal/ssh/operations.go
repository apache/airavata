package ssh

import (
	"context"
	"fmt"
	"log/slog"
	"os"
	"path/filepath"
	"strings"
)

// Copies a single remote file to localPath over SSH
func DownloadFileFromSCP(ctx context.Context, dialConfig SSHDialConfig, sshContext *GlobalSSHContext, remotePath string, localPath string) error {
	target := fmt.Sprintf("scp download %s@%s:%s", dialConfig.sshUsername, dialConfig.host, remotePath)

	t, err := intializeScpSession(ctx, dialConfig, sshContext, "-f", remotePath, target)
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

	slog.Info("Downloaded file over SCP", "host", dialConfig.host, "remotePath", remotePath, "localPath", localPath)
	return nil
}

// Uploads a single local file to a remote endpoint
func UploadFileToSCP(ctx context.Context, dialConfig SSHDialConfig, sshContext *GlobalSSHContext, localPath string, remotePath string) error {
	target := fmt.Sprintf("scp upload %s@%s:%s", dialConfig.sshUsername, dialConfig.host, remotePath)

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

	t, err := intializeScpSession(ctx, dialConfig, sshContext, "-t", remotePath, target)
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

	slog.Info("Uploaded file over SCP", "host", dialConfig.host, "localPath", localPath, "remotePath", remotePath)
	return nil
}

// Creates remotePath on the host, including any missing parent directories, and does
// nothing if it is already there.
func CreateDirectorySSH(ctx context.Context, dialConfig SSHDialConfig, sshContext *GlobalSSHContext, remotePath string) error {
	target := fmt.Sprintf("ssh mkdir %s@%s:%s", dialConfig.sshUsername, dialConfig.host, remotePath)

	if strings.TrimSpace(remotePath) == "" {
		return fmt.Errorf("%s: no remote directory was named", target)
	}

	if _, _, err := RunSSHCommand(ctx, dialConfig, sshContext, "mkdir -p "+ShellQuote(remotePath), target); err != nil {
		return err
	}

	slog.Info("Created directory over SSH", "host", dialConfig.host, "remotePath", remotePath)
	return nil
}

// Reads the mode, size and name a remote scp announces for a single remote file,
// without transferring its contents.
func GetSCPFileMetadata(ctx context.Context, dialConfig SSHDialConfig, sshContext *GlobalSSHContext, remotePath string) (*FileMetadata, error) {
	target := fmt.Sprintf("scp stat %s@%s:%s", dialConfig.sshUsername, dialConfig.host, remotePath)

	t, err := intializeScpSession(ctx, dialConfig, sshContext, "-f", remotePath, target)
	if err != nil {
		return nil, err
	}
	defer t.close()

	// The opening acknowledgement invites the header, and nothing beyond it.
	if err := scpAck(t.stdin); err != nil {
		return nil, t.fail(err)
	}
	msg, err := readSCPMessage(t.stdout)
	if err != nil {
		return nil, t.fail(err)
	}
	if msg[0] != 'C' {
		return nil, t.fail(fmt.Errorf("remote path is not a plain file: scp announced %q", msg))
	}
	mode, size, name, err := parseSCPFileHeader(msg)
	if err != nil {
		return nil, t.fail(err)
	}

	// Path carries the name scp announced, which is the remote basename rather than
	// the path asked about.
	return &FileMetadata{
		Path: name,
		Size: size,
		Mode: mode,
	}, nil
}
