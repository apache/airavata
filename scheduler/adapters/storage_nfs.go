package adapters

import (
	"context"
	"crypto/sha256"
	"encoding/json"
	"fmt"
	"io"
	"os"
	"path/filepath"
	"time"

	"github.com/apache/airavata/scheduler/core/domain"
	ports "github.com/apache/airavata/scheduler/core/port"
)

// NFSAdapter implements the StorageAdapter interface for NFS storage
type NFSAdapter struct {
	resource       domain.StorageResource
	vault          domain.CredentialVault
	mountPoint     string
	basePath       string
	connectionTime time.Time
}

// Compile-time interface verification
var _ ports.StoragePort = (*NFSAdapter)(nil)

// NewNFSAdapter creates a new NFS adapter
func NewNFSAdapter(resource domain.StorageResource, vault domain.CredentialVault) *NFSAdapter {
	return &NFSAdapter{
		resource:       resource,
		vault:          vault,
		connectionTime: time.Now(),
	}
}

// connect establishes NFS connection by validating mount point
func (n *NFSAdapter) connect(userID string) error {
	// Unmarshal metadata from JSON
	var metadata map[string]string
	metadataBytes, err := json.Marshal(n.resource.Metadata)
	if err != nil {
		return fmt.Errorf("failed to marshal metadata: %w", err)
	}
	if err := json.Unmarshal(metadataBytes, &metadata); err != nil {
		return fmt.Errorf("failed to unmarshal metadata: %w", err)
	}

	// Get mount point and base path from resource metadata
	if mountPoint, ok := metadata["mount_point"]; ok {
		n.mountPoint = mountPoint
	}
	if basePath, ok := metadata["base_path"]; ok {
		n.basePath = basePath
	}

	if n.mountPoint == "" {
		return fmt.Errorf("mount point not found in resource metadata")
	}

	// Check if mount point exists and is accessible
	if _, err := os.Stat(n.mountPoint); err != nil {
		return fmt.Errorf("mount point %s is not accessible: %w", n.mountPoint, err)
	}

	// Check if mount point is actually mounted (basic check)
	if !n.isMounted() {
		return fmt.Errorf("mount point %s does not appear to be mounted", n.mountPoint)
	}

	return nil
}

// isMounted checks if the mount point is actually mounted
func (n *NFSAdapter) isMounted() bool {
	// Try to read from the mount point
	_, err := os.ReadDir(n.mountPoint)
	return err == nil
}

// getFullPath returns the full path combining mount point and remote path
func (n *NFSAdapter) getFullPath(remotePath string) string {
	if n.basePath != "" {
		return filepath.Join(n.mountPoint, n.basePath, remotePath)
	}
	return filepath.Join(n.mountPoint, remotePath)
}

// Upload uploads a file to NFS storage
func (n *NFSAdapter) Upload(localPath, remotePath string, userID string) error {
	err := n.connect(userID)
	if err != nil {
		return err
	}

	// Open local file
	localFile, err := os.Open(localPath)
	if err != nil {
		return fmt.Errorf("failed to open local file: %w", err)
	}
	defer localFile.Close()

	// Get full remote path
	fullRemotePath := n.getFullPath(remotePath)

	// Create remote directory if needed
	remoteDir := filepath.Dir(fullRemotePath)
	err = os.MkdirAll(remoteDir, 0755)
	if err != nil {
		return fmt.Errorf("failed to create remote directory: %w", err)
	}

	// Create remote file
	remoteFile, err := os.Create(fullRemotePath)
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

// Download downloads a file from NFS storage
func (n *NFSAdapter) Download(remotePath, localPath string, userID string) error {
	err := n.connect(userID)
	if err != nil {
		return err
	}

	// Get full remote path
	fullRemotePath := n.getFullPath(remotePath)

	// Open remote file
	remoteFile, err := os.Open(fullRemotePath)
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
func (n *NFSAdapter) List(ctx context.Context, prefix string, recursive bool) ([]*ports.StorageObject, error) {
	userID := extractUserIDFromContext(ctx)
	err := n.connect(userID)
	if err != nil {
		return nil, err
	}

	// Get full remote path
	fullRemotePath := n.getFullPath(prefix)

	// List directory
	entries, err := os.ReadDir(fullRemotePath)
	if err != nil {
		return nil, fmt.Errorf("failed to list directory: %w", err)
	}

	// Convert to StorageObject
	var objects []*ports.StorageObject
	for _, entry := range entries {
		if !entry.IsDir() {
			info, err := entry.Info()
			if err != nil {
				continue // Skip files we can't get info for
			}

			objects = append(objects, &ports.StorageObject{
				Path:         filepath.Join(prefix, entry.Name()),
				Size:         info.Size(),
				Checksum:     "", // NFS doesn't have built-in checksums
				ContentType:  "", // NFS doesn't have content types
				LastModified: info.ModTime(),
				Metadata:     make(map[string]string),
			})
		}
	}

	return objects, nil
}

// Move moves a file from one location to another
func (n *NFSAdapter) Move(ctx context.Context, srcPath, dstPath string) error {
	userID := extractUserIDFromContext(ctx)
	err := n.connect(userID)
	if err != nil {
		return err
	}

	// Get full paths
	fullSrcPath := n.getFullPath(srcPath)
	fullDstPath := n.getFullPath(dstPath)

	// Rename the file
	err = os.Rename(fullSrcPath, fullDstPath)
	if err != nil {
		return fmt.Errorf("failed to move file: %w", err)
	}

	return nil
}

// Delete deletes a file from NFS storage
func (n *NFSAdapter) Delete(ctx context.Context, path string) error {
	// Extract userID from context
	userID := extractUserIDFromContext(ctx)

	err := n.connect(userID)
	if err != nil {
		return err
	}

	// Get full remote path
	fullRemotePath := n.getFullPath(path)

	err = os.Remove(fullRemotePath)
	if err != nil {
		return fmt.Errorf("failed to delete file: %w", err)
	}

	return nil
}

// DeleteDirectory deletes a directory from NFS storage
func (n *NFSAdapter) DeleteDirectory(ctx context.Context, path string) error {
	// Extract userID from context
	userID := extractUserIDFromContext(ctx)

	err := n.connect(userID)
	if err != nil {
		return err
	}

	// Get full remote path
	fullRemotePath := n.getFullPath(path)

	err = os.RemoveAll(fullRemotePath)
	if err != nil {
		return fmt.Errorf("failed to delete directory: %w", err)
	}

	return nil
}

// DeleteMultiple deletes multiple files from NFS storage
func (n *NFSAdapter) DeleteMultiple(ctx context.Context, paths []string) error {
	// Extract userID from context
	userID := extractUserIDFromContext(ctx)

	err := n.connect(userID)
	if err != nil {
		return err
	}

	// Delete each file
	for _, path := range paths {
		fullRemotePath := n.getFullPath(path)
		err = os.Remove(fullRemotePath)
		if err != nil {
			return fmt.Errorf("failed to delete file %s: %w", path, err)
		}
	}

	return nil
}

// Disconnect disconnects from NFS storage
func (n *NFSAdapter) Disconnect(ctx context.Context) error {
	// NFS doesn't require persistent connections
	return nil
}

// Exists checks if a file exists in NFS storage
func (n *NFSAdapter) Exists(ctx context.Context, path string) (bool, error) {
	// Extract userID from context
	userID := extractUserIDFromContext(ctx)

	err := n.connect(userID)
	if err != nil {
		return false, err
	}

	// Get full remote path
	fullRemotePath := n.getFullPath(path)

	_, err = os.Stat(fullRemotePath)
	if err != nil {
		return false, nil // File doesn't exist
	}

	return true, nil
}

// Get gets a file from NFS storage
func (n *NFSAdapter) Get(ctx context.Context, path string) (io.ReadCloser, error) {
	// Extract userID from context
	userID := extractUserIDFromContext(ctx)

	err := n.connect(userID)
	if err != nil {
		return nil, err
	}

	// Get full remote path
	fullRemotePath := n.getFullPath(path)

	file, err := os.Open(fullRemotePath)
	if err != nil {
		return nil, fmt.Errorf("failed to open file: %w", err)
	}

	return file, nil
}

// GetMetadata gets metadata for a file
func (n *NFSAdapter) GetMetadata(ctx context.Context, path string) (map[string]string, error) {
	userID := extractUserIDFromContext(ctx)
	err := n.connect(userID)
	if err != nil {
		return nil, err
	}

	// Get full remote path
	fullRemotePath := n.getFullPath(path)

	info, err := os.Stat(fullRemotePath)
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
func (n *NFSAdapter) GetMultiple(ctx context.Context, paths []string) (map[string]io.ReadCloser, error) {
	userID := extractUserIDFromContext(ctx)
	err := n.connect(userID)
	if err != nil {
		return nil, err
	}

	result := make(map[string]io.ReadCloser)
	for _, path := range paths {
		fullPath := n.getFullPath(path)
		file, err := os.Open(fullPath)
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
func (n *NFSAdapter) GetStats(ctx context.Context) (*ports.StorageStats, error) {
	userID := extractUserIDFromContext(ctx)
	err := n.connect(userID)
	if err != nil {
		return nil, err
	}

	// For NFS, we can't easily get all these stats
	// Return basic stats or implement based on your needs
	stats := &ports.StorageStats{
		TotalObjects:   0,                            // Would need to traverse directory tree
		TotalSize:      0,                            // Would need to sum all file sizes
		AvailableSpace: 0,                            // Would need to check disk usage
		Uptime:         time.Since(n.connectionTime), // Real connection uptime
		LastActivity:   time.Now(),
		ErrorRate:      0.0,
		Throughput:     0.0,
	}

	return stats, nil
}

// IsConnected checks if the adapter is connected
func (n *NFSAdapter) IsConnected() bool {
	// For NFS, we assume always connected if the mount point exists
	_, err := os.Stat(n.basePath)
	return err == nil
}

// GetType returns the storage resource type
func (n *NFSAdapter) GetType() string {
	return "nfs"
}

// Connect establishes connection to the storage resource
func (n *NFSAdapter) Connect(ctx context.Context) error {
	// Extract userID from context or use empty string
	userID := ""
	if userIDValue := ctx.Value("userID"); userIDValue != nil {
		if id, ok := userIDValue.(string); ok {
			userID = id
		}
	}
	return n.connect(userID)
}

// Copy copies a file from srcPath to dstPath
func (n *NFSAdapter) Copy(ctx context.Context, srcPath, dstPath string) error {
	// Extract userID from context or use empty string
	userID := ""
	if userIDValue := ctx.Value("userID"); userIDValue != nil {
		if id, ok := userIDValue.(string); ok {
			userID = id
		}
	}

	err := n.connect(userID)
	if err != nil {
		return err
	}

	// Get full paths
	srcFullPath := n.getFullPath(srcPath)
	dstFullPath := n.getFullPath(dstPath)

	// Open source file
	srcFile, err := os.Open(srcFullPath)
	if err != nil {
		return fmt.Errorf("failed to open source file: %w", err)
	}
	defer srcFile.Close()

	// Create destination directory if needed
	dstDir := filepath.Dir(dstFullPath)
	err = os.MkdirAll(dstDir, 0755)
	if err != nil {
		return fmt.Errorf("failed to create destination directory: %w", err)
	}

	// Create destination file
	dstFile, err := os.Create(dstFullPath)
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
func (n *NFSAdapter) CreateDirectory(ctx context.Context, path string) error {
	// Extract userID from context or use empty string
	userID := ""
	if userIDValue := ctx.Value("userID"); userIDValue != nil {
		if id, ok := userIDValue.(string); ok {
			userID = id
		}
	}

	err := n.connect(userID)
	if err != nil {
		return err
	}

	// Get full path
	fullPath := n.getFullPath(path)

	// Create directory
	err = os.MkdirAll(fullPath, 0755)
	if err != nil {
		return fmt.Errorf("failed to create directory: %w", err)
	}

	return nil
}

// Close closes the NFS adapter (no persistent connections to close)
func (n *NFSAdapter) Close() error {
	return nil
}

// Checksum computes SHA-256 checksum of remote file (interface method)
func (n *NFSAdapter) Checksum(ctx context.Context, path string) (string, error) {
	// Extract userID from context or use empty string
	userID := ""
	if userIDValue := ctx.Value("userID"); userIDValue != nil {
		if id, ok := userIDValue.(string); ok {
			userID = id
		}
	}
	return n.CalculateChecksum(path, userID)
}

// UploadWithProgress uploads a file with progress tracking
func (n *NFSAdapter) UploadWithProgress(localPath, remotePath string, progressCallback func(int64, int64), userID string) error {
	err := n.connect(userID)
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

	// Get full remote path
	fullRemotePath := n.getFullPath(remotePath)

	// Create remote directory if needed
	remoteDir := filepath.Dir(fullRemotePath)
	err = os.MkdirAll(remoteDir, 0755)
	if err != nil {
		return fmt.Errorf("failed to create remote directory: %w", err)
	}

	// Create remote file
	remoteFile, err := os.Create(fullRemotePath)
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
func (n *NFSAdapter) DownloadWithProgress(remotePath, localPath string, progressCallback func(int64, int64), userID string) error {
	err := n.connect(userID)
	if err != nil {
		return err
	}

	// Get full remote path
	fullRemotePath := n.getFullPath(remotePath)

	// Get remote file info for progress tracking
	remoteFileInfo, err := os.Stat(fullRemotePath)
	if err != nil {
		return fmt.Errorf("failed to get remote file info: %w", err)
	}
	totalSize := remoteFileInfo.Size()

	// Open remote file
	remoteFile, err := os.Open(fullRemotePath)
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
func (n *NFSAdapter) CalculateChecksum(remotePath string, userID string) (string, error) {
	err := n.connect(userID)
	if err != nil {
		return "", err
	}

	// Construct full path
	fullPath := filepath.Join(n.mountPoint, n.basePath, remotePath)

	// Open remote file
	remoteFile, err := os.Open(fullPath)
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
func (n *NFSAdapter) VerifyChecksum(remotePath string, expectedChecksum string, userID string) (bool, error) {
	actualChecksum, err := n.CalculateChecksum(remotePath, userID)
	if err != nil {
		return false, err
	}

	return actualChecksum == expectedChecksum, nil
}

// calculateLocalChecksum calculates SHA-256 checksum of a local file
func calculateLocalChecksumNFS(filePath string) (string, error) {
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
func (n *NFSAdapter) UploadWithVerification(localPath, remotePath string, userID string) (string, error) {
	// Calculate local file checksum first
	localChecksum, err := calculateLocalChecksumNFS(localPath)
	if err != nil {
		return "", fmt.Errorf("failed to calculate local checksum: %w", err)
	}

	// Upload file
	if err := n.Upload(localPath, remotePath, userID); err != nil {
		return "", fmt.Errorf("upload failed: %w", err)
	}

	// Verify uploaded file checksum
	remoteChecksum, err := n.CalculateChecksum(remotePath, userID)
	if err != nil {
		return "", fmt.Errorf("failed to calculate remote checksum: %w", err)
	}

	if localChecksum != remoteChecksum {
		return "", fmt.Errorf("checksum mismatch after upload: local=%s remote=%s", localChecksum, remoteChecksum)
	}

	return remoteChecksum, nil
}

// DownloadWithVerification downloads file and verifies checksum
func (n *NFSAdapter) DownloadWithVerification(remotePath, localPath string, expectedChecksum string, userID string) error {
	// Download file
	if err := n.Download(remotePath, localPath, userID); err != nil {
		return fmt.Errorf("download failed: %w", err)
	}

	// Calculate downloaded file checksum
	actualChecksum, err := calculateLocalChecksumNFS(localPath)
	if err != nil {
		return fmt.Errorf("failed to calculate downloaded file checksum: %w", err)
	}

	if actualChecksum != expectedChecksum {
		return fmt.Errorf("checksum mismatch after download: expected=%s actual=%s", expectedChecksum, actualChecksum)
	}

	return nil
}

// GetConfig returns the storage configuration
func (n *NFSAdapter) GetConfig() *ports.StorageConfig {
	return &ports.StorageConfig{
		Type:        "nfs",
		Endpoint:    n.resource.Endpoint,
		PathPrefix:  n.basePath,
		Credentials: make(map[string]string),
	}
}

// GetFileMetadata retrieves metadata for a file
func (n *NFSAdapter) GetFileMetadata(remotePath string, userID string) (*domain.FileMetadata, error) {
	err := n.connect(userID)
	if err != nil {
		return nil, err
	}

	// Get file info from NFS
	fileInfo, err := os.Stat(remotePath)
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
func (n *NFSAdapter) Ping(ctx context.Context) error {
	err := n.connect("")
	if err != nil {
		return err
	}

	// Try to stat the base path to verify it's accessible
	_, err = os.Stat(n.basePath)
	return err
}

// Put uploads data to the specified path
func (n *NFSAdapter) Put(ctx context.Context, path string, data io.Reader, metadata map[string]string) error {
	err := n.connect("")
	if err != nil {
		return err
	}

	// Create directory if it doesn't exist
	dir := filepath.Dir(path)
	if err := os.MkdirAll(dir, 0755); err != nil {
		return err
	}

	file, err := os.Create(path)
	if err != nil {
		return err
	}
	defer file.Close()

	_, err = io.Copy(file, data)
	return err
}

// PutMultiple uploads multiple objects
func (n *NFSAdapter) PutMultiple(ctx context.Context, objects []*ports.StorageObject) error {
	for _, obj := range objects {
		if err := n.Put(ctx, obj.Path, obj.Data, obj.Metadata); err != nil {
			return err
		}
	}
	return nil
}

// SetMetadata sets metadata for a file (NFS doesn't support metadata)
func (n *NFSAdapter) SetMetadata(ctx context.Context, path string, metadata map[string]string) error {
	// NFS doesn't support metadata, so this is a no-op
	return nil
}

// Size returns the size of a file
func (n *NFSAdapter) Size(ctx context.Context, path string) (int64, error) {
	err := n.connect("")
	if err != nil {
		return 0, err
	}

	fileInfo, err := os.Stat(path)
	if err != nil {
		return 0, err
	}
	return fileInfo.Size(), nil
}

// Transfer transfers a file from source storage to destination
func (n *NFSAdapter) Transfer(ctx context.Context, srcStorage ports.StoragePort, srcPath, dstPath string) error {
	// Get data from source storage
	data, err := srcStorage.Get(ctx, srcPath)
	if err != nil {
		return err
	}
	defer data.Close()

	// Put data to destination
	return n.Put(ctx, dstPath, data, nil)
}

// TransferWithProgress transfers a file with progress callback
func (n *NFSAdapter) TransferWithProgress(ctx context.Context, srcStorage ports.StoragePort, srcPath, dstPath string, progress ports.ProgressCallback) error {
	// For now, just call Transfer without progress tracking
	return n.Transfer(ctx, srcStorage, srcPath, dstPath)
}

// UpdateMetadata updates metadata for a file (NFS doesn't support metadata)
func (n *NFSAdapter) UpdateMetadata(ctx context.Context, path string, metadata map[string]string) error {
	// NFS doesn't support metadata, so this is a no-op
	return nil
}

// GenerateSignedURL generates a signed URL for NFS operations
func (n *NFSAdapter) GenerateSignedURL(ctx context.Context, path string, expiresIn time.Duration, method string) (string, error) {
	// NFS doesn't support signed URLs directly
	return "", fmt.Errorf("signed URLs are not supported for NFS storage")
}
