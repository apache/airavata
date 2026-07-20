package config

import (
	"fmt"
	"os"
	"strconv"
	"time"

	"gopkg.in/yaml.v3"
)

// Config represents the complete application configuration
type Config struct {
	Database DatabaseConfig `yaml:"database"`
	Server   ServerConfig   `yaml:"server"`
	GRPC     GRPCConfig     `yaml:"grpc"`
	Worker   WorkerConfig   `yaml:"worker"`
	SpiceDB  SpiceDBConfig  `yaml:"spicedb"`
	OpenBao  OpenBaoConfig  `yaml:"openbao"`
	Services ServicesConfig `yaml:"services"`
	JWT      JWTConfig      `yaml:"jwt"`
	Compute  ComputeConfig  `yaml:"compute"`
	Storage  StorageConfig  `yaml:"storage"`
	Cache    CacheConfig    `yaml:"cache"`
	Metrics  MetricsConfig  `yaml:"metrics"`
	Logging  LoggingConfig  `yaml:"logging"`
	Test     TestConfig     `yaml:"test"`
}

type DatabaseConfig struct {
	DSN string `yaml:"dsn"`
}

type ServerConfig struct {
	Host         string        `yaml:"host"`
	Port         int           `yaml:"port"`
	ReadTimeout  time.Duration `yaml:"read_timeout"`
	WriteTimeout time.Duration `yaml:"write_timeout"`
	IdleTimeout  time.Duration `yaml:"idl_timeout"`
}

type GRPCConfig struct {
	Host string `yaml:"host"`
	Port int    `yaml:"port"`
}

type WorkerConfig struct {
	BinaryPath        string        `yaml:"binary_path"`
	BinaryURL         string        `yaml:"binary_url"`
	DefaultWorkingDir string        `yaml:"default_working_dir"`
	HeartbeatInterval time.Duration `yaml:"heartbeat_interval"`
	DialTimeout       time.Duration `yaml:"dial_timeout"`
	RequestTimeout    time.Duration `yaml:"request_timeout"`
}

type SpiceDBConfig struct {
	Endpoint     string        `yaml:"endpoint"`
	PresharedKey string        `yaml:"preshared_key"`
	DialTimeout  time.Duration `yaml:"dial_timeout"`
}

type OpenBaoConfig struct {
	Address     string        `yaml:"address"`
	Token       string        `yaml:"token"`
	MountPath   string        `yaml:"mount_path"`
	DialTimeout time.Duration `yaml:"dial_timeout"`
}

type ServicesConfig struct {
	Postgres PostgresConfig `yaml:"postgres"`
	MinIO    MinIOConfig    `yaml:"minio"`
	SFTP     SFTPConfig     `yaml:"sftp"`
	NFS      NFSConfig      `yaml:"nfs"`
}

type PostgresConfig struct {
	Host     string `yaml:"host"`
	Port     int    `yaml:"port"`
	Database string `yaml:"database"`
	User     string `yaml:"user"`
	Password string `yaml:"password"`
	SSLMode  string `yaml:"ssl_mode"`
}

type MinIOConfig struct {
	Host      string `yaml:"host"`
	Port      int    `yaml:"port"`
	AccessKey string `yaml:"access_key"`
	SecretKey string `yaml:"secret_key"`
	UseSSL    bool   `yaml:"use_ssl"`
}

type SFTPConfig struct {
	Host     string `yaml:"host"`
	Port     int    `yaml:"port"`
	Username string `yaml:"username"`
}

type NFSConfig struct {
	Host      string `yaml:"host"`
	Port      int    `yaml:"port"`
	MountPath string `yaml:"mount_path"`
}

type JWTConfig struct {
	SecretKey  string        `yaml:"secret_key"`
	Algorithm  string        `yaml:"algorithm"`
	Issuer     string        `yaml:"issuer"`
	Audience   string        `yaml:"audience"`
	Expiration time.Duration `yaml:"expiration"`
}

type ComputeConfig struct {
	SLURM      SLURMConfig      `yaml:"slurm"`
	BareMetal  BareMetalConfig  `yaml:"baremetal"`
	Kubernetes KubernetesConfig `yaml:"kubernetes"`
	Docker     DockerConfig     `yaml:"docker"`
}

type SLURMConfig struct {
	DefaultPartition string        `yaml:"default_partition"`
	DefaultAccount   string        `yaml:"default_account"`
	DefaultQoS       string        `yaml:"default_qos"`
	JobTimeout       time.Duration `yaml:"job_timeout"`
	SSHTimeout       time.Duration `yaml:"ssh_timeout"`
}

type BareMetalConfig struct {
	SSHTimeout        string `yaml:"ssh_timeout"`
	DefaultWorkingDir string `yaml:"default_working_dir"`
}

type KubernetesConfig struct {
	DefaultNamespace      string        `yaml:"default_namespace"`
	DefaultServiceAccount string        `yaml:"default_service_account"`
	PodTimeout            time.Duration `yaml:"pod_timeout"`
	JobTimeout            time.Duration `yaml:"job_timeout"`
}

type DockerConfig struct {
	DefaultImage     string        `yaml:"default_image"`
	ContainerTimeout time.Duration `yaml:"container_timeout"`
	NetworkMode      string        `yaml:"network_mode"`
}

type StorageConfig struct {
	S3   S3Config          `yaml:"s3"`
	SFTP SFTPStorageConfig `yaml:"sftp"`
	NFS  NFSStorageConfig  `yaml:"nfs"`
}

type S3Config struct {
	Region     string        `yaml:"region"`
	Timeout    time.Duration `yaml:"timeout"`
	MaxRetries int           `yaml:"max_retries"`
}

type SFTPStorageConfig struct {
	Timeout    time.Duration `yaml:"timeout"`
	MaxRetries int           `yaml:"max_retries"`
}

type NFSStorageConfig struct {
	Timeout    time.Duration `yaml:"timeout"`
	MaxRetries int           `yaml:"max_retries"`
}

type CacheConfig struct {
	DefaultTTL      time.Duration `yaml:"default_ttl"`
	MaxSize         string        `yaml:"max_size"`
	CleanupInterval time.Duration `yaml:"cleanup_interval"`
}

type MetricsConfig struct {
	Enabled bool   `yaml:"enabled"`
	Port    int    `yaml:"port"`
	Path    string `yaml:"path"`
}

type LoggingConfig struct {
	Level  string `yaml:"level"`
	Format string `yaml:"format"`
	Output string `yaml:"output"`
}

type TestConfig struct {
	Timeout         time.Duration `yaml:"timeout"`
	Retries         int           `yaml:"retries"`
	CleanupTimeout  time.Duration `yaml:"cleanup_timeout"`
	ResourceTimeout time.Duration `yaml:"resource_timeout"`
}

// Load loads configuration from file and environment variables
func Load(configPath string) (*Config, error) {
	config := &Config{}

	// Load default config if no path specified
	if configPath == "" {
		configPath = "config/default.yaml"
	}

	// Load YAML file
	if err := loadYAML(config, configPath); err != nil {
		return nil, fmt.Errorf("failed to load config file %s: %w", configPath, err)
	}

	// Override with environment variables
	overrideWithEnv(config)

	return config, nil
}

// loadYAML loads configuration from YAML file
func loadYAML(config *Config, path string) error {
	// Check if file exists
	if _, err := os.Stat(path); os.IsNotExist(err) {
		return fmt.Errorf("config file %s does not exist", path)
	}

	data, err := os.ReadFile(path)
	if err != nil {
		return fmt.Errorf("failed to read config file: %w", err)
	}

	if err := yaml.Unmarshal(data, config); err != nil {
		return fmt.Errorf("failed to parse YAML: %w", err)
	}

	return nil
}

// overrideWithEnv overrides config values with environment variables
func overrideWithEnv(config *Config) {
	// Database
	if dsn := os.Getenv("DATABASE_URL"); dsn != "" {
		config.Database.DSN = dsn
	}

	// Server
	if host := os.Getenv("HOST"); host != "" {
		config.Server.Host = host
	}
	if port := os.Getenv("PORT"); port != "" {
		if p, err := strconv.Atoi(port); err == nil {
			config.Server.Port = p
		}
	}

	// GRPC
	if grpcPort := os.Getenv("GRPC_PORT"); grpcPort != "" {
		if p, err := strconv.Atoi(grpcPort); err == nil {
			config.GRPC.Port = p
		}
	}

	// Worker
	if binaryPath := os.Getenv("WORKER_BINARY_PATH"); binaryPath != "" {
		config.Worker.BinaryPath = binaryPath
	}
	if binaryURL := os.Getenv("WORKER_BINARY_URL"); binaryURL != "" {
		config.Worker.BinaryURL = binaryURL
	}
	if workingDir := os.Getenv("WORKER_WORKING_DIR"); workingDir != "" {
		config.Worker.DefaultWorkingDir = workingDir
	}

	// SpiceDB
	if endpoint := os.Getenv("SPICEDB_ENDPOINT"); endpoint != "" {
		config.SpiceDB.Endpoint = endpoint
	}
	if token := os.Getenv("SPICEDB_PRESHARED_KEY"); token != "" {
		config.SpiceDB.PresharedKey = token
	}

	// OpenBao
	if address := os.Getenv("VAULT_ENDPOINT"); address != "" {
		config.OpenBao.Address = address
	}
	if token := os.Getenv("VAULT_TOKEN"); token != "" {
		config.OpenBao.Token = token
	}

	// Services
	if host := os.Getenv("POSTGRES_HOST"); host != "" {
		config.Services.Postgres.Host = host
	}
	if port := os.Getenv("POSTGRES_PORT"); port != "" {
		if p, err := strconv.Atoi(port); err == nil {
			config.Services.Postgres.Port = p
		}
	}
	if user := os.Getenv("POSTGRES_USER"); user != "" {
		config.Services.Postgres.User = user
	}
	if password := os.Getenv("POSTGRES_PASSWORD"); password != "" {
		config.Services.Postgres.Password = password
	}
	if db := os.Getenv("POSTGRES_DB"); db != "" {
		config.Services.Postgres.Database = db
	}

	if host := os.Getenv("MINIO_HOST"); host != "" {
		config.Services.MinIO.Host = host
	}
	if port := os.Getenv("MINIO_PORT"); port != "" {
		if p, err := strconv.Atoi(port); err == nil {
			config.Services.MinIO.Port = p
		}
	}
	if accessKey := os.Getenv("MINIO_ACCESS_KEY"); accessKey != "" {
		config.Services.MinIO.AccessKey = accessKey
	}
	if secretKey := os.Getenv("MINIO_SECRET_KEY"); secretKey != "" {
		config.Services.MinIO.SecretKey = secretKey
	}

	if host := os.Getenv("SFTP_HOST"); host != "" {
		config.Services.SFTP.Host = host
	}
	if port := os.Getenv("SFTP_PORT"); port != "" {
		if p, err := strconv.Atoi(port); err == nil {
			config.Services.SFTP.Port = p
		}
	}

	if host := os.Getenv("NFS_HOST"); host != "" {
		config.Services.NFS.Host = host
	}
	if port := os.Getenv("NFS_PORT"); port != "" {
		if p, err := strconv.Atoi(port); err == nil {
			config.Services.NFS.Port = p
		}
	}
}

// GetDSN returns the database DSN, building it from components if needed
func (c *Config) GetDSN() string {
	if c.Database.DSN != "" {
		return c.Database.DSN
	}

	// Build DSN from components
	return fmt.Sprintf("postgres://%s:%s@%s:%d/%s?sslmode=%s",
		c.Services.Postgres.User,
		c.Services.Postgres.Password,
		c.Services.Postgres.Host,
		c.Services.Postgres.Port,
		c.Services.Postgres.Database,
		c.Services.Postgres.SSLMode,
	)
}

// GetMinIOEndpoint returns the MinIO endpoint URL
func (c *Config) GetMinIOEndpoint() string {
	protocol := "http"
	if c.Services.MinIO.UseSSL {
		protocol = "https"
	}
	return fmt.Sprintf("%s://%s:%d", protocol, c.Services.MinIO.Host, c.Services.MinIO.Port)
}

// GetSFTPEndpoint returns the SFTP endpoint
func (c *Config) GetSFTPEndpoint() string {
	return fmt.Sprintf("%s:%d", c.Services.SFTP.Host, c.Services.SFTP.Port)
}

// GetNFSEndpoint returns the NFS endpoint
func (c *Config) GetNFSEndpoint() string {
	return fmt.Sprintf("%s:%d", c.Services.NFS.Host, c.Services.NFS.Port)
}
