package adapters

import (
	"context"
	"crypto/sha256"
	"encoding/json"
	"fmt"
	"io"
	"os"
	"path/filepath"
	"strings"
	"time"

	"github.com/apache/airavata/scheduler/core/domain"
	ports "github.com/apache/airavata/scheduler/core/port"
	"github.com/pkg/sftp"
	"golang.org/x/crypto/ssh"
)

// SFTPAdapter implements the StorageAdapter interface for SFTP
type SFTPAdapter struct {
	resource       domain.StorageResource
	sshClient      *ssh.Client
	sftpClient     *sftp.Client
	vault          domain.CredentialVault
	connectionTime time.Time
}

// Compile-time interface verification
var _ ports.StoragePort = (*SFTPAdapter)(nil)

// NewSFTPAdapter creates a new SFTP adapter
func NewSFTPAdapter(resource domain.StorageResource, vault domain.CredentialVault) *SFTPAdapter {
	return &SFTPAdapter{
		resource:       resource,
		vault:          vault,
		connectionTime: time.Now(),
	}
}

// connect establishes SSH and SFTP connections
func (s *SFTPAdapter) connect(userID string) error {
	if s.sftpClient != nil {
		return nil // Already connected
	}

	// Retrieve credentials from vault with user context
	ctx := context.Background()
	fmt.Printf("SFTP Storage: retrieving credentials for resource %s, user %s, type storage_resource\n", s.resource.ID, userID)
	credential, decryptedData, err := s.vault.GetUsableCredentialForResource(ctx, s.resource.ID, "storage_resource", userID, nil)
	if err != nil {
		fmt.Printf("SFTP Storage: failed to retrieve credentials: %v\n", err)
		return fmt.Errorf("failed to retrieve credentials: %w", err)
	}
	fmt.Printf("SFTP Storage: successfully retrieved credentials for resource %s\n", s.resource.ID)

	// Extract credential data
	var username, privateKeyPath, port string

	if credential.Type == domain.CredentialTypeSSHKey {
		// SSH key authentication - data contains the private key
		privateKeyPath = string(decryptedData)
		// Username should be in resource metadata
	} else {
		return fmt.Errorf("only SSH key authentication is supported, got credential type: %s", credential.Type)
	}

	// Unmarshal resource metadata
	var resourceMetadata map[string]string
	resourceMetadataBytes, err := json.Marshal(s.resource.Metadata)
	if err != nil {
		return fmt.Errorf("failed to marshal resource metadata: %w", err)
	}
	if err := json.Unmarshal(resourceMetadataBytes, &resourceMetadata); err != nil {
		return fmt.Errorf("failed to unmarshal resource metadata: %w", err)
	}

	// Get port from resource metadata or use default
	if portData, ok := resourceMetadata["port"]; ok {
		port = portData
	}
	if port == "" {
		port = "22"
	}

	// If username is not in credentials, try to get it from resource metadata
	if username == "" {
		if usernameData, ok := resourceMetadata["username"]; ok {
			username = usernameData
		}
	}

	if username == "" {
		return fmt.Errorf("username not found in credentials or resource metadata")
	}

	// Build SSH config
	config := &ssh.ClientConfig{
		User:            username,
		HostKeyCallback: ssh.InsecureIgnoreHostKey(), // In production, use proper host key verification
		Timeout:         10 * time.Second,
		Config: ssh.Config{
			Ciphers: []string{
				"aes128-ctr", "aes192-ctr", "aes256-ctr",
				"aes128-gcm@openssh.com", "aes256-gcm@openssh.com",
			},
		},
	}

	// Add authentication method
	if privateKeyPath != "" {
		// Use private key authentication
		// privateKeyPath contains the actual key data, not a file path
		signer, err := ssh.ParsePrivateKey([]byte(privateKeyPath))
		if err != nil {
			return fmt.Errorf("failed to parse private key: %w", err)
		}

		config.Auth = []ssh.AuthMethod{ssh.PublicKeys(signer)}
	} else {
		return fmt.Errorf("SSH private key is required for authentication")
	}

	// Extract host from resource
	host := s.resource.Endpoint
	if host == "" {
		// Fallback to metadata
		if hostData, ok := resourceMetadata["host"]; ok {
			host = hostData
		}
	}

	// Check if host already includes port
	var addr string
	if strings.Contains(host, ":") {
		// Host already includes port (e.g., "localhost:2222")
		addr = host
	} else {
		// Host doesn't include port, add it
		addr = fmt.Sprintf("%s:%s", host, port)
	}
	sshClient, err := ssh.Dial("tcp", addr, config)
	if err != nil {
		if strings.Contains(err.Error(), "unable to authenticate") ||
			strings.Contains(err.Error(), "handshake failed") {
			return fmt.Errorf("authentication failed: %w", err)
		}
		return fmt.Errorf("failed to connect to SSH server at %s: %w", addr, err)
	}

	s.sshClient = sshClient

	// Create SFTP client
	sftpClient, err := sftp.NewClient(sshClient)
	if err != nil {
		sshClient.Close()
		return fmt.Errorf("failed to create SFTP client: %w", err)
	}

	s.sftpClient = sftpClient
	return nil
}

// disconnect closes the SFTP and SSH connections
func (s *SFTPAdapter) disconnect() {
	if s.sftpClient != nil {
		s.sftpClient.Close()
		s.sftpClient = nil
	}
	if s.sshClient != nil {
		s.sshClient.Close()
		s.sshClient = nil
	}
}

// Upload uploads a file to SFTP storage
func (s *SFTPAdapter) Upload(localPath, remotePath string, userID string) error {
	err := s.connect(userID)
	if err != nil {
		return err
	}

	// Get base path from resource metadata
	var basePath string
	if pathData, ok := s.resource.Metadata["path"]; ok {
		if pathStr, ok := pathData.(string); ok {
			basePath = pathStr
		}
	}
	if basePath == "" {
		basePath = "/tmp" // Default fallback
	}

	// For atmoz/sftp container, the user is chrooted to /home/testuser
	// So /home/testuser/upload becomes /upload from the client's perspective
	var fullRemotePath string
	if strings.HasPrefix(basePath, "/home/testuser/") {
		// Convert /home/testuser/upload to /upload
		relativePath := strings.TrimPrefix(basePath, "/home/testuser")
		if relativePath == "" {
			relativePath = "/"
		}
		fullRemotePath = filepath.Join(relativePath, remotePath)
	} else {
		fullRemotePath = filepath.Join(basePath, remotePath)
	}

	// Debug logging removed

	// Open local file
	localFile, err := os.Open(localPath)
	if err != nil {
		return fmt.Errorf("failed to open local file: %w", err)
	}
	defer localFile.Close()

	// Create remote directory structure if needed
	remoteDir := filepath.Dir(fullRemotePath)
	if remoteDir != "." && remoteDir != "/" {
		err = s.sftpClient.MkdirAll(remoteDir)
		if err != nil {
			return fmt.Errorf("failed to create remote directory %s: %w", remoteDir, err)
		}
	}

	// Create remote file
	remoteFile, err := s.sftpClient.Create(fullRemotePath)
	if err != nil {
		return fmt.Errorf("failed to create remote file: %w", err)
	}
	defer remoteFile.Close()

	// Copy data
	_, err = io.Copy(remoteFile, localFile)
	if err != nil {
		return fmt.Errorf("failed to upload file: %w", err)
	}

	return nil
}

// Download downloads a file from SFTP storage
func (s *SFTPAdapter) Download(remotePath, localPath string, userID string) error {
	err := s.connect(userID)
	if err != nil {
		return err
	}

	// Get base path from resource metadata
	var basePath string
	if pathData, ok := s.resource.Metadata["path"]; ok {
		if pathStr, ok := pathData.(string); ok {
			basePath = pathStr
		}
	}
	if basePath == "" {
		basePath = "/tmp" // Default fallback
	}

	// For atmoz/sftp container, the user is chrooted to /home/testuser
	// So /home/testuser/upload becomes /upload from the client's perspective
	var fullRemotePath string
	if strings.HasPrefix(basePath, "/home/testuser/") {
		// Convert /home/testuser/upload to /upload
		relativePath := strings.TrimPrefix(basePath, "/home/testuser")
		if relativePath == "" {
			relativePath = "/"
		}
		fullRemotePath = filepath.Join(relativePath, remotePath)
	} else {
		fullRemotePath = filepath.Join(basePath, remotePath)
	}

	// Open remote file
	remoteFile, err := s.sftpClient.Open(fullRemotePath)
	if err != nil {
		return fmt.Errorf("failed to open remote file: %w", err)
	}
	defer remoteFile.Close()

	// Create local directory if needed
	localDir := filepath.Dir(localPath)
	err = os.MkdirAll(localDir, 0755)
	if err != nil {
		return fmt.Errorf("failed to create local directory: %w", err)
	}

	// Create local file
	localFile, err := os.Create(localPath)
	if err != nil {
		return fmt.Errorf("failed to create local file: %w", err)
	}
	defer localFile.Close()

	// Copy data
	_, err = io.Copy(localFile, remoteFile)
	if err != nil {
		return fmt.Errorf("failed to download file: %w", err)
	}

	return nil
}

// List lists files in a directory
func (s *SFTPAdapter) List(ctx context.Context, prefix string, recursive bool) ([]*ports.StorageObject, error) {
	userID := extractUserIDFromContext(ctx)
	err := s.connect(userID)
	if err != nil {
		return nil, err
	}

	// List directory
	entries, err := s.sftpClient.ReadDir(prefix)
	if err != nil {
		return nil, fmt.Errorf("failed to list directory: %w", err)
	}

	// Convert to StorageObject
	var objects []*ports.StorageObject
	for _, entry := range entries {
		if !entry.IsDir() {
			objects = append(objects, &ports.StorageObject{
				Path:         filepath.Join(prefix, entry.Name()),
				Size:         entry.Size(),
				Checksum:     "", // SFTP doesn't have built-in checksums
				ContentType:  "", // SFTP doesn't have content types
				LastModified: entry.ModTime(),
				Metadata:     make(map[string]string),
			})
		}
	}

	return objects, nil
}

// Move moves a file from one location to another
func (s *SFTPAdapter) Move(ctx context.Context, srcPath, dstPath string) error {
	userID := extractUserIDFromContext(ctx)
	err := s.connect(userID)
	if err != nil {
		return err
	}

	// Rename the file
	err = s.sftpClient.Rename(srcPath, dstPath)
	if err != nil {
		return fmt.Errorf("failed to move file: %w", err)
	}

	return nil
}

// Delete deletes a file from SFTP storage
func (s *SFTPAdapter) Delete(ctx context.Context, path string) error {
	// Extract userID from context
	userID := extractUserIDFromContext(ctx)

	err := s.connect(userID)
	if err != nil {
		return err
	}

	err = s.sftpClient.Remove(path)
	if err != nil {
		return fmt.Errorf("failed to delete file: %w", err)
	}

	return nil
}

// DeleteDirectory deletes a directory from SFTP storage
func (s *SFTPAdapter) DeleteDirectory(ctx context.Context, path string) error {
	// Extract userID from context
	userID := extractUserIDFromContext(ctx)

	err := s.connect(userID)
	if err != nil {
		return err
	}

	// Remove directory recursively
	err = s.sftpClient.RemoveDirectory(path)
	if err != nil {
		return fmt.Errorf("failed to delete directory: %w", err)
	}

	return nil
}

// DeleteMultiple deletes multiple files from SFTP storage
func (s *SFTPAdapter) DeleteMultiple(ctx context.Context, paths []string) error {
	// Extract userID from context
	userID := extractUserIDFromContext(ctx)

	err := s.connect(userID)
	if err != nil {
		return err
	}

	// Delete each file
	for _, path := range paths {
		err = s.sftpClient.Remove(path)
		if err != nil {
			return fmt.Errorf("failed to delete file %s: %w", path, err)
		}
	}

	return nil
}

// Disconnect disconnects from SFTP storage
func (s *SFTPAdapter) Disconnect(ctx context.Context) error {
	if s.sftpClient != nil {
		s.sftpClient.Close()
		s.sftpClient = nil
	}
	if s.sshClient != nil {
		s.sshClient.Close()
		s.sshClient = nil
	}
	return nil
}

// Exists checks if a file exists in SFTP storage
func (s *SFTPAdapter) Exists(ctx context.Context, path string) (bool, error) {
	// Extract userID from context
	userID := extractUserIDFromContext(ctx)

	err := s.connect(userID)
	if err != nil {
		return false, err
	}

	_, err = s.sftpClient.Stat(path)
	if err != nil {
		return false, nil // File doesn't exist
	}

	return true, nil
}

// Get gets a file from SFTP storage
func (s *SFTPAdapter) Get(ctx context.Context, path string) (io.ReadCloser, error) {
	// Extract userID from context
	userID := extractUserIDFromContext(ctx)

	err := s.connect(userID)
	if err != nil {
		return nil, err
	}

	file, err := s.sftpClient.Open(path)
	if err != nil {
		return nil, fmt.Errorf("failed to open file: %w", err)
	}

	return file, nil
}

// GetMetadata gets metadata for a file
func (s *SFTPAdapter) GetMetadata(ctx context.Context, path string) (map[string]string, error) {
	userID := extractUserIDFromContext(ctx)
	err := s.connect(userID)
	if err != nil {
		return nil, err
	}

	info, err := s.sftpClient.Stat(path)
	if err != nil {
		return nil, fmt.Errorf("failed to get file info: %w", err)
	}

	metadata := make(map[string]string)
	metadata["size"] = fmt.Sprintf("%d", info.Size())
	metadata["lastModified"] = info.ModTime().Format(time.RFC3339)
	metadata["mode"] = info.Mode().String()

	return metadata, nil
}

// GetMultiple retrieves multiple files
func (s *SFTPAdapter) GetMultiple(ctx context.Context, paths []string) (map[string]io.ReadCloser, error) {
	userID := extractUserIDFromContext(ctx)
	err := s.connect(userID)
	if err != nil {
		return nil, err
	}

	result := make(map[string]io.ReadCloser)
	for _, path := range paths {
		file, err := s.sftpClient.Open(path)
		if err != nil {
			// Close any already opened files
			for _, reader := range result {
				reader.Close()
			}
			return nil, fmt.Errorf("failed to open file %s: %w", path, err)
		}
		result[path] = file
	}

	return result, nil
}

// GetStats returns storage statistics
func (s *SFTPAdapter) GetStats(ctx context.Context) (*ports.StorageStats, error) {
	userID := extractUserIDFromContext(ctx)
	err := s.connect(userID)
	if err != nil {
		return nil, err
	}

	// For SFTP, we can't easily get all these stats
	// Return basic stats or implement based on your needs
	stats := &ports.StorageStats{
		TotalObjects:   0,                            // Would need to traverse directory tree
		TotalSize:      0,                            // Would need to sum all file sizes
		AvailableSpace: 0,                            // Would need to check disk usage
		Uptime:         time.Since(s.connectionTime), // Real connection uptime
		LastActivity:   time.Now(),
		ErrorRate:      0.0,
		Throughput:     0.0,
	}

	return stats, nil
}

// IsConnected checks if the adapter is connected
func (s *SFTPAdapter) IsConnected() bool {
	// For SFTP, we can check if the client is initialized
	return s.sftpClient != nil
}

// GetType returns the storage resource type
func (s *SFTPAdapter) GetType() string {
	return "sftp"
}

// Connect establishes connection to the storage resource
func (s *SFTPAdapter) Connect(ctx context.Context) error {
	// Extract userID from context or use empty string
	userID := ""
	if userIDValue := ctx.Value("userID"); userIDValue != nil {
		if id, ok := userIDValue.(string); ok {
			userID = id
		}
	}
	return s.connect(userID)
}

// Copy copies a file from srcPath to dstPath
func (s *SFTPAdapter) Copy(ctx context.Context, srcPath, dstPath string) error {
	// Extract userID from context or use empty string
	userID := ""
	if userIDValue := ctx.Value("userID"); userIDValue != nil {
		if id, ok := userIDValue.(string); ok {
			userID = id
		}
	}

	// Use SFTP client to copy file
	err := s.connect(userID)
	if err != nil {
		return err
	}
	defer s.disconnect()

	// Open source file
	srcFile, err := s.sftpClient.Open(srcPath)
	if err != nil {
		return fmt.Errorf("failed to open source file: %w", err)
	}
	defer srcFile.Close()

	// Create destination file
	dstFile, err := s.sftpClient.Create(dstPath)
	if err != nil {
		return fmt.Errorf("failed to create destination file: %w", err)
	}
	defer dstFile.Close()

	// Copy data
	_, err = io.Copy(dstFile, srcFile)
	if err != nil {
		return fmt.Errorf("failed to copy file: %w", err)
	}

	return nil
}

// CreateDirectory creates a directory in the storage resource
func (s *SFTPAdapter) CreateDirectory(ctx context.Context, path string) error {
	// Extract userID from context or use empty string
	userID := ""
	if userIDValue := ctx.Value("userID"); userIDValue != nil {
		if id, ok := userIDValue.(string); ok {
			userID = id
		}
	}

	err := s.connect(userID)
	if err != nil {
		return err
	}
	defer s.disconnect()

	// Create directory using SFTP client
	err = s.sftpClient.MkdirAll(path)
	if err != nil {
		return fmt.Errorf("failed to create directory: %w", err)
	}

	return nil
}

// Close closes the SFTP adapter connections
func (s *SFTPAdapter) Close() error {
	s.disconnect()
	return nil
}

// Checksum computes SHA-256 checksum of remote file (interface method)
func (s *SFTPAdapter) Checksum(ctx context.Context, path string) (string, error) {
	// Extract userID from context or use empty string
	userID := ""
	if userIDValue := ctx.Value("userID"); userIDValue != nil {
		if id, ok := userIDValue.(string); ok {
			userID = id
		}
	}
	return s.CalculateChecksum(path, userID)
}

// UploadWithProgress uploads a file with progress tracking
func (s *SFTPAdapter) UploadWithProgress(localPath, remotePath string, progressCallback func(int64, int64), userID string) error {
	err := s.connect(userID)
	if err != nil {
		return err
	}

	// Get file info for progress tracking
	localFileInfo, err := os.Stat(localPath)
	if err != nil {
		return fmt.Errorf("failed to get file info: %w", err)
	}
	totalSize := localFileInfo.Size()

	// Open local file
	localFile, err := os.Open(localPath)
	if err != nil {
		return fmt.Errorf("failed to open local file: %w", err)
	}
	defer localFile.Close()

	// Create remote directory if needed
	remoteDir := filepath.Dir(remotePath)
	err = s.sftpClient.MkdirAll(remoteDir)
	if err != nil {
		return fmt.Errorf("failed to create remote directory: %w", err)
	}

	// Create remote file
	remoteFile, err := s.sftpClient.Create(remotePath)
	if err != nil {
		return fmt.Errorf("failed to create remote file: %w", err)
	}
	defer remoteFile.Close()

	// Copy data with progress tracking
	buffer := make([]byte, 32*1024) // 32KB buffer
	var copied int64
	for {
		n, err := localFile.Read(buffer)
		if n > 0 {
			written, writeErr := remoteFile.Write(buffer[:n])
			if writeErr != nil {
				return fmt.Errorf("failed to write to remote file: %w", writeErr)
			}
			copied += int64(written)
			if progressCallback != nil {
				progressCallback(copied, totalSize)
			}
		}
		if err == io.EOF {
			break
		}
		if err != nil {
			return fmt.Errorf("failed to read from local file: %w", err)
		}
	}

	return nil
}

// DownloadWithProgress downloads a file with progress tracking
func (s *SFTPAdapter) DownloadWithProgress(remotePath, localPath string, progressCallback func(int64, int64), userID string) error {
	err := s.connect(userID)
	if err != nil {
		return err
	}

	// Get remote file info for progress tracking
	remoteFileInfo, err := s.sftpClient.Stat(remotePath)
	if err != nil {
		return fmt.Errorf("failed to get remote file info: %w", err)
	}
	totalSize := remoteFileInfo.Size()

	// Open remote file
	remoteFile, err := s.sftpClient.Open(remotePath)
	if err != nil {
		return fmt.Errorf("failed to open remote file: %w", err)
	}
	defer remoteFile.Close()

	// Create local directory if needed
	localDir := filepath.Dir(localPath)
	err = os.MkdirAll(localDir, 0755)
	if err != nil {
		return fmt.Errorf("failed to create local directory: %w", err)
	}

	// Create local file
	localFile, err := os.Create(localPath)
	if err != nil {
		return fmt.Errorf("failed to create local file: %w", err)
	}
	defer localFile.Close()

	// Copy data with progress tracking
	buffer := make([]byte, 32*1024) // 32KB buffer
	var copied int64
	for {
		n, err := remoteFile.Read(buffer)
		if n > 0 {
			written, writeErr := localFile.Write(buffer[:n])
			if writeErr != nil {
				return fmt.Errorf("failed to write to local file: %w", writeErr)
			}
			copied += int64(written)
			if progressCallback != nil {
				progressCallback(copied, totalSize)
			}
		}
		if err == io.EOF {
			break
		}
		if err != nil {
			return fmt.Errorf("failed to read from remote file: %w", err)
		}
	}

	return nil
}

// CalculateChecksum computes SHA-256 checksum of remote file
func (s *SFTPAdapter) CalculateChecksum(remotePath string, userID string) (string, error) {
	err := s.connect(userID)
	if err != nil {
		return "", err
	}

	// Open remote file
	remoteFile, err := s.sftpClient.Open(remotePath)
	if err != nil {
		return "", fmt.Errorf("failed to open remote file: %w", err)
	}
	defer remoteFile.Close()

	// Calculate SHA-256 while streaming
	hash := sha256.New()
	if _, err := io.Copy(hash, remoteFile); err != nil {
		return "", fmt.Errorf("failed to calculate checksum: %w", err)
	}

	return fmt.Sprintf("%x", hash.Sum(nil)), nil
}

// VerifyChecksum verifies file integrity against expected checksum
func (s *SFTPAdapter) VerifyChecksum(remotePath string, expectedChecksum string, userID string) (bool, error) {
	actualChecksum, err := s.CalculateChecksum(remotePath, userID)
	if err != nil {
		return false, err
	}

	return actualChecksum == expectedChecksum, nil
}

// calculateLocalChecksum calculates SHA-256 checksum of a local file
func calculateLocalChecksumSFTP(filePath string) (string, error) {
	file, err := os.Open(filePath)
	if err != nil {
		return "", fmt.Errorf("failed to open file: %w", err)
	}
	defer file.Close()

	hash := sha256.New()
	if _, err := io.Copy(hash, file); err != nil {
		return "", fmt.Errorf("failed to calculate local checksum: %w", err)
	}

	return fmt.Sprintf("%x", hash.Sum(nil)), nil
}

// UploadWithVerification uploads file and verifies checksum
func (s *SFTPAdapter) UploadWithVerification(localPath, remotePath string, userID string) (string, error) {
	// Calculate local file checksum first
	localChecksum, err := calculateLocalChecksumSFTP(localPath)
	if err != nil {
		return "", fmt.Errorf("failed to calculate local checksum: %w", err)
	}

	// Upload file
	if err := s.Upload(localPath, remotePath, userID); err != nil {
		return "", fmt.Errorf("upload failed: %w", err)
	}

	// Verify uploaded file checksum
	remoteChecksum, err := s.CalculateChecksum(remotePath, userID)
	if err != nil {
		return "", fmt.Errorf("failed to calculate remote checksum: %w", err)
	}

	if localChecksum != remoteChecksum {
		return "", fmt.Errorf("checksum mismatch after upload: local=%s remote=%s", localChecksum, remoteChecksum)
	}

	return remoteChecksum, nil
}

// DownloadWithVerification downloads file and verifies checksum
func (s *SFTPAdapter) DownloadWithVerification(remotePath, localPath string, expectedChecksum string, userID string) error {
	// Download file
	if err := s.Download(remotePath, localPath, userID); err != nil {
		return fmt.Errorf("download failed: %w", err)
	}

	// Calculate downloaded file checksum
	actualChecksum, err := calculateLocalChecksumSFTP(localPath)
	if err != nil {
		return fmt.Errorf("failed to calculate downloaded file checksum: %w", err)
	}

	if actualChecksum != expectedChecksum {
		return fmt.Errorf("checksum mismatch after download: expected=%s actual=%s", expectedChecksum, actualChecksum)
	}

	return nil
}

// GetConfig returns the storage configuration
func (s *SFTPAdapter) GetConfig() *ports.StorageConfig {
	return &ports.StorageConfig{
		Type:        "sftp",
		Endpoint:    s.resource.Endpoint,
		PathPrefix:  "",
		Credentials: make(map[string]string),
	}
}

// GetFileMetadata retrieves metadata for a file
func (s *SFTPAdapter) GetFileMetadata(remotePath string, userID string) (*domain.FileMetadata, error) {
	err := s.connect(userID)
	if err != nil {
		return nil, err
	}

	// Get file info from SFTP
	fileInfo, err := s.sftpClient.Stat(remotePath)
	if err != nil {
		return nil, fmt.Errorf("failed to get file metadata: %w", err)
	}

	metadata := &domain.FileMetadata{
		Path:     remotePath,
		Size:     fileInfo.Size(),
		Checksum: "", // Will be calculated separately if needed
		Type:     "", // Will be determined by context
	}

	return metadata, nil
}

// Ping checks if the storage is accessible
func (s *SFTPAdapter) Ping(ctx context.Context) error {
	err := s.connect("")
	if err != nil {
		return err
	}

	// Try to list the root directory to verify connection
	_, err = s.sftpClient.ReadDir(".")
	return err
}

// Put uploads data to the specified path
func (s *SFTPAdapter) Put(ctx context.Context, path string, data io.Reader, metadata map[string]string) error {
	err := s.connect("")
	if err != nil {
		return err
	}

	file, err := s.sftpClient.Create(path)
	if err != nil {
		return err
	}
	defer file.Close()

	_, err = io.Copy(file, data)
	return err
}

// PutMultiple uploads multiple objects
func (s *SFTPAdapter) PutMultiple(ctx context.Context, objects []*ports.StorageObject) error {
	for _, obj := range objects {
		if err := s.Put(ctx, obj.Path, obj.Data, obj.Metadata); err != nil {
			return err
		}
	}
	return nil
}

// SetMetadata sets metadata for a file (SFTP doesn't support metadata)
func (s *SFTPAdapter) SetMetadata(ctx context.Context, path string, metadata map[string]string) error {
	// SFTP doesn't support metadata, so this is a no-op
	return nil
}

// Size returns the size of a file
func (s *SFTPAdapter) Size(ctx context.Context, path string) (int64, error) {
	err := s.connect("")
	if err != nil {
		return 0, err
	}

	fileInfo, err := s.sftpClient.Stat(path)
	if err != nil {
		return 0, err
	}
	return fileInfo.Size(), nil
}

// Transfer transfers a file from source storage to destination
func (s *SFTPAdapter) Transfer(ctx context.Context, srcStorage ports.StoragePort, srcPath, dstPath string) error {
	// Get data from source storage
	data, err := srcStorage.Get(ctx, srcPath)
	if err != nil {
		return err
	}
	defer data.Close()

	// Put data to destination
	return s.Put(ctx, dstPath, data, nil)
}

// TransferWithProgress transfers a file with progress callback
func (s *SFTPAdapter) TransferWithProgress(ctx context.Context, srcStorage ports.StoragePort, srcPath, dstPath string, progress ports.ProgressCallback) error {
	// For now, just call Transfer without progress tracking
	return s.Transfer(ctx, srcStorage, srcPath, dstPath)
}

// UpdateMetadata updates metadata for a file (SFTP doesn't support metadata)
func (s *SFTPAdapter) UpdateMetadata(ctx context.Context, path string, metadata map[string]string) error {
	// SFTP doesn't support metadata, so this is a no-op
	return nil
}

// GenerateSignedURL generates a signed URL for SFTP operations
func (s *SFTPAdapter) GenerateSignedURL(ctx context.Context, path string, expiresIn time.Duration, method string) (string, error) {
	// SFTP doesn't support signed URLs directly
	return "", fmt.Errorf("signed URLs are not supported for SFTP storage")
}
