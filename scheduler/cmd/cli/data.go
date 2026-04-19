package main

import (
	"archive/tar"
	"bytes"
	"compress/gzip"
	"context"
	"encoding/json"
	"fmt"
	"io"
	"mime/multipart"
	"net/http"
	"os"
	"path/filepath"
	"strings"
	"time"

	"github.com/spf13/cobra"
)

// FileInfo represents file information from storage
type FileInfo struct {
	Name         string    `json:"name"`
	Path         string    `json:"path"`
	Size         int64     `json:"size"`
	IsDirectory  bool      `json:"isDirectory"`
	Checksum     string    `json:"checksum,omitempty"`
	LastModified time.Time `json:"lastModified"`
}

// UploadResponse represents the response from upload API
type UploadResponse struct {
	Path     string `json:"path"`
	Size     int64  `json:"size"`
	Checksum string `json:"checksum"`
}

// createDataCommands creates data management commands
func createDataCommands() *cobra.Command {
	dataCmd := &cobra.Command{
		Use:   "data",
		Short: "Data management commands",
		Long:  "Commands for uploading, downloading, and managing data in storage resources",
	}

	// Upload commands
	uploadCmd := &cobra.Command{
		Use:   "upload <local-file> <storage-id>:<remote-path>",
		Short: "Upload a file to storage",
		Long: `Upload a local file to a storage resource.

Examples:
  airavata data upload input.dat minio-storage:/experiments/input.dat
  airavata data upload results.csv s3-bucket:/data/results.csv`,
		Args: cobra.ExactArgs(2),
		RunE: func(cmd *cobra.Command, args []string) error {
			localPath := args[0]
			storagePath := args[1]

			// Parse storage-id:remote-path
			parts := strings.SplitN(storagePath, ":", 2)
			if len(parts) != 2 {
				return fmt.Errorf("invalid storage path format. Use: storage-id:remote-path")
			}

			storageID := parts[0]
			remotePath := parts[1]

			return uploadFile(localPath, storageID, remotePath)
		},
	}

	uploadDirCmd := &cobra.Command{
		Use:   "upload-dir <local-dir> <storage-id>:<remote-path>",
		Short: "Upload a directory recursively to storage",
		Long: `Upload a local directory and all its contents recursively to a storage resource.

Examples:
  airavata data upload-dir ./data minio-storage:/experiments/data
  airavata data upload-dir ./results s3-bucket:/outputs/results`,
		Args: cobra.ExactArgs(2),
		RunE: func(cmd *cobra.Command, args []string) error {
			localPath := args[0]
			storagePath := args[1]

			// Parse storage-id:remote-path
			parts := strings.SplitN(storagePath, ":", 2)
			if len(parts) != 2 {
				return fmt.Errorf("invalid storage path format. Use: storage-id:remote-path")
			}

			storageID := parts[0]
			remotePath := parts[1]

			return uploadDirectory(localPath, storageID, remotePath)
		},
	}

	// Download commands
	downloadCmd := &cobra.Command{
		Use:   "download <storage-id>:<remote-path> <local-file>",
		Short: "Download a file from storage",
		Long: `Download a file from a storage resource to local filesystem.

Examples:
  airavata data download minio-storage:/experiments/input.dat ./input.dat
  airavata data download s3-bucket:/data/results.csv ./results.csv`,
		Args: cobra.ExactArgs(2),
		RunE: func(cmd *cobra.Command, args []string) error {
			storagePath := args[0]
			localPath := args[1]

			// Parse storage-id:remote-path
			parts := strings.SplitN(storagePath, ":", 2)
			if len(parts) != 2 {
				return fmt.Errorf("invalid storage path format. Use: storage-id:remote-path")
			}

			storageID := parts[0]
			remotePath := parts[1]

			return downloadFile(storageID, remotePath, localPath)
		},
	}

	downloadDirCmd := &cobra.Command{
		Use:   "download-dir <storage-id>:<remote-path> <local-dir>",
		Short: "Download a directory from storage",
		Long: `Download a directory and all its contents from a storage resource.

Examples:
  airavata data download-dir minio-storage:/experiments/data ./data
  airavata data download-dir s3-bucket:/outputs/results ./results`,
		Args: cobra.ExactArgs(2),
		RunE: func(cmd *cobra.Command, args []string) error {
			storagePath := args[0]
			localPath := args[1]

			// Parse storage-id:remote-path
			parts := strings.SplitN(storagePath, ":", 2)
			if len(parts) != 2 {
				return fmt.Errorf("invalid storage path format. Use: storage-id:remote-path")
			}

			storageID := parts[0]
			remotePath := parts[1]

			return downloadDirectory(storageID, remotePath, localPath)
		},
	}

	// List command
	listCmd := &cobra.Command{
		Use:   "list <storage-id>:<path>",
		Short: "List files in storage path",
		Long: `List files and directories in a storage resource path.

Examples:
  airavata data list minio-storage:/experiments/
  airavata data list s3-bucket:/data/
  airavata data list minio-storage:/experiments/exp-123/`,
		Args: cobra.ExactArgs(1),
		RunE: func(cmd *cobra.Command, args []string) error {
			storagePath := args[0]

			// Parse storage-id:remote-path
			parts := strings.SplitN(storagePath, ":", 2)
			if len(parts) != 2 {
				return fmt.Errorf("invalid storage path format. Use: storage-id:remote-path")
			}

			storageID := parts[0]
			path := parts[1]

			return listFiles(storageID, path)
		},
	}

	dataCmd.AddCommand(uploadCmd, uploadDirCmd, downloadCmd, downloadDirCmd, listCmd)
	return dataCmd
}

// uploadFile uploads a single file to storage
func uploadFile(localPath, storageID, remotePath string) error {
	configManager := NewConfigManager()
	if !configManager.IsAuthenticated() {
		return fmt.Errorf("not authenticated - run 'airavata auth login' first")
	}

	serverURL, err := configManager.GetServerURL()
	if err != nil {
		return fmt.Errorf("failed to get server URL: %w", err)
	}

	token, err := configManager.GetToken()
	if err != nil {
		return fmt.Errorf("failed to get token: %w", err)
	}

	// Check if local file exists
	if _, err := os.Stat(localPath); os.IsNotExist(err) {
		return fmt.Errorf("local file does not exist: %s", localPath)
	}

	// Open file for reading
	file, err := os.Open(localPath)
	if err != nil {
		return fmt.Errorf("failed to open file: %w", err)
	}
	defer file.Close()

	fmt.Printf("📤 Uploading %s to %s:%s...\n", localPath, storageID, remotePath)

	// Upload file
	response, err := uploadFileAPI(serverURL, token, storageID, remotePath, file)
	if err != nil {
		return fmt.Errorf("failed to upload file: %w", err)
	}

	fmt.Printf("✅ File uploaded successfully!\n")
	fmt.Printf("   Path: %s\n", response.Path)
	fmt.Printf("   Size: %d bytes\n", response.Size)
	if response.Checksum != "" {
		fmt.Printf("   Checksum: %s\n", response.Checksum)
	}

	return nil
}

// uploadDirectory uploads a directory recursively to storage
func uploadDirectory(localPath, storageID, remotePath string) error {
	configManager := NewConfigManager()
	if !configManager.IsAuthenticated() {
		return fmt.Errorf("not authenticated - run 'airavata auth login' first")
	}

	serverURL, err := configManager.GetServerURL()
	if err != nil {
		return fmt.Errorf("failed to get server URL: %w", err)
	}

	token, err := configManager.GetToken()
	if err != nil {
		return fmt.Errorf("failed to get token: %w", err)
	}

	// Check if local directory exists
	if _, err := os.Stat(localPath); os.IsNotExist(err) {
		return fmt.Errorf("local directory does not exist: %s", localPath)
	}

	fmt.Printf("📤 Uploading directory %s to %s:%s...\n", localPath, storageID, remotePath)

	// Create a tar.gz archive of the directory
	var buf bytes.Buffer
	if err := createTarGz(&buf, localPath); err != nil {
		return fmt.Errorf("failed to create archive: %w", err)
	}

	// Upload the archive
	archivePath := remotePath + ".tar.gz"
	response, err := uploadFileAPI(serverURL, token, storageID, archivePath, &buf)
	if err != nil {
		return fmt.Errorf("failed to upload directory: %w", err)
	}

	fmt.Printf("✅ Directory uploaded successfully!\n")
	fmt.Printf("   Archive: %s\n", response.Path)
	fmt.Printf("   Size: %d bytes\n", response.Size)
	if response.Checksum != "" {
		fmt.Printf("   Checksum: %s\n", response.Checksum)
	}

	return nil
}

// downloadFile downloads a single file from storage
func downloadFile(storageID, remotePath, localPath string) error {
	configManager := NewConfigManager()
	if !configManager.IsAuthenticated() {
		return fmt.Errorf("not authenticated - run 'airavata auth login' first")
	}

	serverURL, err := configManager.GetServerURL()
	if err != nil {
		return fmt.Errorf("failed to get server URL: %w", err)
	}

	token, err := configManager.GetToken()
	if err != nil {
		return fmt.Errorf("failed to get token: %w", err)
	}

	fmt.Printf("📥 Downloading %s:%s to %s...\n", storageID, remotePath, localPath)

	// Download file
	reader, err := downloadFileAPI(serverURL, token, storageID, remotePath)
	if err != nil {
		return fmt.Errorf("failed to download file: %w", err)
	}
	defer reader.Close()

	// Create local directory if needed
	if err := os.MkdirAll(filepath.Dir(localPath), 0755); err != nil {
		return fmt.Errorf("failed to create local directory: %w", err)
	}

	// Create local file
	file, err := os.Create(localPath)
	if err != nil {
		return fmt.Errorf("failed to create local file: %w", err)
	}
	defer file.Close()

	// Copy data
	bytesWritten, err := io.Copy(file, reader)
	if err != nil {
		return fmt.Errorf("failed to write file: %w", err)
	}

	fmt.Printf("✅ File downloaded successfully!\n")
	fmt.Printf("   Size: %d bytes\n", bytesWritten)

	return nil
}

// downloadDirectory downloads a directory from storage
func downloadDirectory(storageID, remotePath, localPath string) error {
	configManager := NewConfigManager()
	if !configManager.IsAuthenticated() {
		return fmt.Errorf("not authenticated - run 'airavata auth login' first")
	}

	serverURL, err := configManager.GetServerURL()
	if err != nil {
		return fmt.Errorf("failed to get server URL: %w", err)
	}

	token, err := configManager.GetToken()
	if err != nil {
		return fmt.Errorf("failed to get token: %w", err)
	}

	fmt.Printf("📥 Downloading directory %s:%s to %s...\n", storageID, remotePath, localPath)

	// Try to download as tar.gz archive first
	archivePath := remotePath + ".tar.gz"
	reader, err := downloadFileAPI(serverURL, token, storageID, archivePath)
	if err != nil {
		// If archive doesn't exist, try to download individual files
		return downloadDirectoryRecursive(serverURL, token, storageID, remotePath, localPath)
	}
	defer reader.Close()

	// Create local directory
	if err := os.MkdirAll(localPath, 0755); err != nil {
		return fmt.Errorf("failed to create local directory: %w", err)
	}

	// Extract tar.gz archive
	if err := extractTarGz(reader, localPath); err != nil {
		return fmt.Errorf("failed to extract archive: %w", err)
	}

	fmt.Printf("✅ Directory downloaded and extracted successfully!\n")

	return nil
}

// downloadDirectoryRecursive downloads directory contents recursively
func downloadDirectoryRecursive(serverURL, token, storageID, remotePath, localPath string) error {
	// List files in the directory
	files, err := listFilesAPI(serverURL, token, storageID, remotePath)
	if err != nil {
		return fmt.Errorf("failed to list directory contents: %w", err)
	}

	// Create local directory
	if err := os.MkdirAll(localPath, 0755); err != nil {
		return fmt.Errorf("failed to create local directory: %w", err)
	}

	// Download each file
	for _, file := range files {
		if file.IsDirectory {
			// Recursively download subdirectory
			subLocalPath := filepath.Join(localPath, file.Name)
			subRemotePath := file.Path
			if err := downloadDirectoryRecursive(serverURL, token, storageID, subRemotePath, subLocalPath); err != nil {
				return fmt.Errorf("failed to download subdirectory %s: %w", file.Name, err)
			}
		} else {
			// Download file
			localFilePath := filepath.Join(localPath, file.Name)
			reader, err := downloadFileAPI(serverURL, token, storageID, file.Path)
			if err != nil {
				return fmt.Errorf("failed to download file %s: %w", file.Name, err)
			}

			// Create local file
			file, err := os.Create(localFilePath)
			if err != nil {
				reader.Close()
				return fmt.Errorf("failed to create local file %s: %w", localFilePath, err)
			}

			// Copy data
			_, err = io.Copy(file, reader)
			file.Close()
			reader.Close()
			if err != nil {
				return fmt.Errorf("failed to write file %s: %w", localFilePath, err)
			}
		}
	}

	return nil
}

// listFiles lists files in a storage path
func listFiles(storageID, path string) error {
	configManager := NewConfigManager()
	if !configManager.IsAuthenticated() {
		return fmt.Errorf("not authenticated - run 'airavata auth login' first")
	}

	serverURL, err := configManager.GetServerURL()
	if err != nil {
		return fmt.Errorf("failed to get server URL: %w", err)
	}

	token, err := configManager.GetToken()
	if err != nil {
		return fmt.Errorf("failed to get token: %w", err)
	}

	files, err := listFilesAPI(serverURL, token, storageID, path)
	if err != nil {
		return fmt.Errorf("failed to list files: %w", err)
	}

	if len(files) == 0 {
		fmt.Printf("📁 No files found in %s:%s\n", storageID, path)
		return nil
	}

	fmt.Printf("📁 Files in %s:%s (%d items)\n", storageID, path, len(files))
	fmt.Println("==========================================")

	for _, file := range files {
		icon := "📄"
		if file.IsDirectory {
			icon = "📁"
		}

		fmt.Printf("%s %s", icon, file.Name)
		if !file.IsDirectory {
			fmt.Printf(" (%d bytes)", file.Size)
		}
		if file.Checksum != "" {
			fmt.Printf(" [%s]", file.Checksum[:8])
		}
		fmt.Printf(" %s\n", file.LastModified.Format("2006-01-02 15:04"))
	}

	return nil
}

// uploadFileAPI uploads a file via the API
func uploadFileAPI(serverURL, token, storageID, remotePath string, file io.Reader) (*UploadResponse, error) {
	// Create multipart form
	var buf bytes.Buffer
	writer := multipart.NewWriter(&buf)

	// Add file field
	fileWriter, err := writer.CreateFormFile("file", filepath.Base(remotePath))
	if err != nil {
		return nil, fmt.Errorf("failed to create form file: %w", err)
	}

	if _, err := io.Copy(fileWriter, file); err != nil {
		return nil, fmt.Errorf("failed to copy file data: %w", err)
	}

	// Add path field
	if err := writer.WriteField("path", remotePath); err != nil {
		return nil, fmt.Errorf("failed to write path field: %w", err)
	}

	writer.Close()

	// Create request
	ctx, cancel := context.WithTimeout(context.Background(), 5*time.Minute)
	defer cancel()

	url := fmt.Sprintf("%s/api/v1/storage/%s/upload", serverURL, storageID)
	req, err := http.NewRequestWithContext(ctx, "POST", url, &buf)
	if err != nil {
		return nil, fmt.Errorf("failed to create request: %w", err)
	}

	req.Header.Set("Content-Type", writer.FormDataContentType())
	req.Header.Set("Authorization", "Bearer "+token)

	// Send request
	client := &http.Client{Timeout: 5 * time.Minute}
	resp, err := client.Do(req)
	if err != nil {
		return nil, fmt.Errorf("failed to send request: %w", err)
	}
	defer resp.Body.Close()

	body, err := io.ReadAll(resp.Body)
	if err != nil {
		return nil, fmt.Errorf("failed to read response: %w", err)
	}

	if resp.StatusCode != http.StatusCreated {
		return nil, fmt.Errorf("upload failed: %s", string(body))
	}

	var response UploadResponse
	if err := json.Unmarshal(body, &response); err != nil {
		return nil, fmt.Errorf("failed to parse response: %w", err)
	}

	return &response, nil
}

// listFilesAPI lists files via the API
func listFilesAPI(serverURL, token, storageID, path string) ([]FileInfo, error) {
	ctx, cancel := context.WithTimeout(context.Background(), 30*time.Second)
	defer cancel()

	url := fmt.Sprintf("%s/api/v1/storage/%s/files?path=%s", serverURL, storageID, path)
	req, err := http.NewRequestWithContext(ctx, "GET", url, nil)
	if err != nil {
		return nil, fmt.Errorf("failed to create request: %w", err)
	}

	req.Header.Set("Authorization", "Bearer "+token)

	client := &http.Client{Timeout: 30 * time.Second}
	resp, err := client.Do(req)
	if err != nil {
		return nil, fmt.Errorf("failed to send request: %w", err)
	}
	defer resp.Body.Close()

	body, err := io.ReadAll(resp.Body)
	if err != nil {
		return nil, fmt.Errorf("failed to read response: %w", err)
	}

	if resp.StatusCode != http.StatusOK {
		return nil, fmt.Errorf("list files failed: %s", string(body))
	}

	var files []FileInfo
	if err := json.Unmarshal(body, &files); err != nil {
		return nil, fmt.Errorf("failed to parse response: %w", err)
	}

	return files, nil
}

// downloadFileAPI downloads a file via the API
func downloadFileAPI(serverURL, token, storageID, remotePath string) (io.ReadCloser, error) {
	ctx, cancel := context.WithTimeout(context.Background(), 5*time.Minute)
	defer cancel()

	url := fmt.Sprintf("%s/api/v1/storage/%s/download?path=%s", serverURL, storageID, remotePath)
	req, err := http.NewRequestWithContext(ctx, "GET", url, nil)
	if err != nil {
		return nil, fmt.Errorf("failed to create request: %w", err)
	}

	req.Header.Set("Authorization", "Bearer "+token)

	client := &http.Client{Timeout: 5 * time.Minute}
	resp, err := client.Do(req)
	if err != nil {
		return nil, fmt.Errorf("failed to send request: %w", err)
	}

	if resp.StatusCode != http.StatusOK {
		resp.Body.Close()
		body, _ := io.ReadAll(resp.Body)
		return nil, fmt.Errorf("download failed: %s", string(body))
	}

	return resp.Body, nil
}

// createTarGz creates a tar.gz archive from a directory
func createTarGz(w io.Writer, sourceDir string) error {
	gzWriter := gzip.NewWriter(w)
	defer gzWriter.Close()

	tarWriter := tar.NewWriter(gzWriter)
	defer tarWriter.Close()

	return filepath.Walk(sourceDir, func(path string, info os.FileInfo, err error) error {
		if err != nil {
			return err
		}

		// Create tar header
		header, err := tar.FileInfoHeader(info, info.Name())
		if err != nil {
			return err
		}

		// Update header name to be relative to source directory
		relPath, err := filepath.Rel(sourceDir, path)
		if err != nil {
			return err
		}
		header.Name = relPath

		// Write header
		if err := tarWriter.WriteHeader(header); err != nil {
			return err
		}

		// Write file content if it's a regular file
		if info.Mode().IsRegular() {
			file, err := os.Open(path)
			if err != nil {
				return err
			}
			defer file.Close()

			if _, err := io.Copy(tarWriter, file); err != nil {
				return err
			}
		}

		return nil
	})
}

// extractTarGz extracts a tar.gz archive to a directory
func extractTarGz(r io.Reader, destDir string) error {
	gzReader, err := gzip.NewReader(r)
	if err != nil {
		return err
	}
	defer gzReader.Close()

	tarReader := tar.NewReader(gzReader)

	for {
		header, err := tarReader.Next()
		if err == io.EOF {
			break
		}
		if err != nil {
			return err
		}

		// Create full path
		targetPath := filepath.Join(destDir, header.Name)

		// Create directory if needed
		if header.Typeflag == tar.TypeDir {
			if err := os.MkdirAll(targetPath, 0755); err != nil {
				return err
			}
			continue
		}

		// Create parent directories
		if err := os.MkdirAll(filepath.Dir(targetPath), 0755); err != nil {
			return err
		}

		// Create file
		file, err := os.Create(targetPath)
		if err != nil {
			return err
		}

		// Copy file content
		if _, err := io.Copy(file, tarReader); err != nil {
			file.Close()
			return err
		}

		file.Close()
	}

	return nil
}
