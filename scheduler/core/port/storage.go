package ports

import (
	"context"
	"io"
	"time"

	"github.com/apache/airavata/scheduler/core/domain"
)

// StoragePort defines the interface for storage operations
// This abstracts storage implementations from domain services
type StoragePort interface {
	// File operations
	Put(ctx context.Context, path string, data io.Reader, metadata map[string]string) error
	Get(ctx context.Context, path string) (io.ReadCloser, error)
	Delete(ctx context.Context, path string) error
	Exists(ctx context.Context, path string) (bool, error)
	Size(ctx context.Context, path string) (int64, error)
	Checksum(ctx context.Context, path string) (string, error)

	// Directory operations
	List(ctx context.Context, prefix string, recursive bool) ([]*StorageObject, error)
	CreateDirectory(ctx context.Context, path string) error
	DeleteDirectory(ctx context.Context, path string) error
	Copy(ctx context.Context, srcPath, dstPath string) error
	Move(ctx context.Context, srcPath, dstPath string) error

	// Metadata operations
	GetMetadata(ctx context.Context, path string) (map[string]string, error)
	SetMetadata(ctx context.Context, path string, metadata map[string]string) error
	UpdateMetadata(ctx context.Context, path string, metadata map[string]string) error

	// Batch operations
	PutMultiple(ctx context.Context, objects []*StorageObject) error
	GetMultiple(ctx context.Context, paths []string) (map[string]io.ReadCloser, error)
	DeleteMultiple(ctx context.Context, paths []string) error

	// Transfer operations
	Transfer(ctx context.Context, srcStorage StoragePort, srcPath, dstPath string) error
	TransferWithProgress(ctx context.Context, srcStorage StoragePort, srcPath, dstPath string, progress ProgressCallback) error

	// Signed URL operations
	GenerateSignedURL(ctx context.Context, path string, duration time.Duration, method string) (string, error)

	// Adapter-specific operations (merged from StorageAdapter)
	Upload(localPath, remotePath string, userID string) error
	Download(remotePath, localPath string, userID string) error
	GetFileMetadata(remotePath string, userID string) (*domain.FileMetadata, error)
	CalculateChecksum(remotePath string, userID string) (string, error)
	VerifyChecksum(remotePath string, expectedChecksum string, userID string) (bool, error)
	UploadWithVerification(localPath, remotePath string, userID string) (string, error)
	DownloadWithVerification(remotePath, localPath string, expectedChecksum string, userID string) error

	// Connection management
	Connect(ctx context.Context) error
	Disconnect(ctx context.Context) error
	IsConnected() bool
	Ping(ctx context.Context) error

	// Configuration
	GetConfig() *StorageConfig
	GetStats(ctx context.Context) (*StorageStats, error)
	GetType() string
}

// StorageObject represents a storage object
type StorageObject struct {
	Path         string            `json:"path"`
	Size         int64             `json:"size"`
	Checksum     string            `json:"checksum"`
	ContentType  string            `json:"contentType"`
	LastModified time.Time         `json:"lastModified"`
	Metadata     map[string]string `json:"metadata"`
	Data         io.Reader         `json:"-"`
}

// StorageConfig represents storage configuration
type StorageConfig struct {
	Type        string            `json:"type"`
	Endpoint    string            `json:"endpoint"`
	Credentials map[string]string `json:"credentials"`
	Bucket      string            `json:"bucket,omitempty"`
	Region      string            `json:"region,omitempty"`
	PathPrefix  string            `json:"pathPrefix,omitempty"`
	MaxRetries  int               `json:"maxRetries"`
	Timeout     time.Duration     `json:"timeout"`
	ChunkSize   int64             `json:"chunkSize"`
	Concurrency int               `json:"concurrency"`
	Compression bool              `json:"compression"`
	Encryption  bool              `json:"encryption"`
}

// StorageStats represents storage statistics
type StorageStats struct {
	TotalObjects   int64         `json:"totalObjects"`
	TotalSize      int64         `json:"totalSize"`
	AvailableSpace int64         `json:"availableSpace"`
	Uptime         time.Duration `json:"uptime"`
	LastActivity   time.Time     `json:"lastActivity"`
	ErrorRate      float64       `json:"errorRate"`
	Throughput     float64       `json:"throughput"`
}

// ProgressCallback defines the interface for transfer progress callbacks
type ProgressCallback interface {
	OnProgress(bytesTransferred, totalBytes int64, speed float64)
	OnComplete(bytesTransferred int64, duration time.Duration)
	OnError(err error)
}

// StorageFactory defines the interface for creating storage instances
type StorageFactory interface {
	CreateStorage(ctx context.Context, config *StorageConfig) (StoragePort, error)
	GetSupportedTypes() []string
	ValidateConfig(config *StorageConfig) error
}

// StorageValidator defines the interface for storage validation
type StorageValidator interface {
	ValidateConnection(ctx context.Context, storage StoragePort) error
	ValidatePermissions(ctx context.Context, storage StoragePort) error
	ValidatePerformance(ctx context.Context, storage StoragePort) error
}

// StorageMonitor defines the interface for storage monitoring
type StorageMonitor interface {
	StartMonitoring(ctx context.Context, storage StoragePort) error
	StopMonitoring(ctx context.Context, storage StoragePort) error
	GetMetrics(ctx context.Context, storage StoragePort) (*StorageMetrics, error)
}

// StorageMetrics represents detailed storage metrics
type StorageMetrics struct {
	ReadOperations    int64         `json:"readOperations"`
	WriteOperations   int64         `json:"writeOperations"`
	DeleteOperations  int64         `json:"deleteOperations"`
	BytesRead         int64         `json:"bytesRead"`
	BytesWritten      int64         `json:"bytesWritten"`
	BytesDeleted      int64         `json:"bytesDeleted"`
	AverageReadTime   time.Duration `json:"averageReadTime"`
	AverageWriteTime  time.Duration `json:"averageWriteTime"`
	AverageDeleteTime time.Duration `json:"averageDeleteTime"`
	ErrorCount        int64         `json:"errorCount"`
	LastError         time.Time     `json:"lastError"`
	LastErrorMsg      string        `json:"lastErrorMsg"`
}

// StorageCache defines the interface for storage caching
type StorageCache interface {
	Get(ctx context.Context, key string) ([]byte, error)
	Set(ctx context.Context, key string, data []byte, ttl time.Duration) error
	Delete(ctx context.Context, key string) error
	Clear(ctx context.Context) error
	GetStats(ctx context.Context) (*StorageCacheStats, error)
}

// StorageCacheStats represents cache statistics
type StorageCacheStats struct {
	Hits       int64         `json:"hits"`
	Misses     int64         `json:"misses"`
	Size       int64         `json:"size"`
	MaxSize    int64         `json:"maxSize"`
	HitRate    float64       `json:"hitRate"`
	Uptime     time.Duration `json:"uptime"`
	LastAccess time.Time     `json:"lastAccess"`
}
