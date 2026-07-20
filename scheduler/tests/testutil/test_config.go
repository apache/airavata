package testutil

import (
	"os"
	"path/filepath"
	"strconv"
)

// TestConfig holds all centralized test configuration
type TestConfig struct {
	// Master credentials for binary deployment
	MasterSSHKeyPath    string
	MasterSSHPublicKey  string
	MasterSSHPrivateKey string

	// Database configuration
	DatabaseURL      string
	TestDatabaseURL  string
	PostgresUser     string
	PostgresPassword string
	PostgresDB       string

	// Service endpoints
	SpiceDBEndpoint string
	SpiceDBToken    string
	VaultEndpoint   string
	VaultToken      string
	MinIOEndpoint   string
	MinIOAccessKey  string
	MinIOSecretKey  string

	// Compute resource configuration
	SlurmCluster1Name  string
	SlurmCluster1Host  string
	SlurmCluster1Port  int
	SlurmCluster2Name  string
	SlurmCluster2Host  string
	SlurmCluster2Port  int
	BareMetalNode1Name string
	BareMetalNode1Host string
	BareMetalNode1Port int
	BareMetalNode2Name string
	BareMetalNode2Host string
	BareMetalNode2Port int

	// Storage resource configuration
	SFTPName string
	SFTPHost string
	SFTPPort int
	NFSName  string
	NFSHost  string
	NFSPort  int
	S3Name   string
	S3Host   string
	S3Port   int

	// Test user configuration
	TestUserName     string
	TestUserEmail    string
	TestUserPassword string

	// Kubernetes configuration
	KubernetesClusterName string
	KubernetesContext     string
	KubernetesNamespace   string
	KubernetesConfigPath  string

	// Test timeouts and retries
	DefaultTimeout     int
	DefaultRetries     int
	ResourceTimeout    int
	CleanupTimeout     int
	GRPCDialTimeout    int
	HTTPRequestTimeout int

	// Fixture paths
	FixturesDir         string
	MasterKeyPath       string
	MasterPublicKeyPath string
}

// GetTestConfig returns the test configuration with environment variable overrides
func GetTestConfig() *TestConfig {
	return &TestConfig{
		// Master credentials
		MasterSSHKeyPath:    getEnv("TEST_MASTER_SSH_KEY_PATH", "../fixtures/master_ssh_key"),
		MasterSSHPublicKey:  getEnv("TEST_MASTER_SSH_PUBLIC_KEY", "../fixtures/master_ssh_key.pub"),
		MasterSSHPrivateKey: getEnv("TEST_MASTER_SSH_PRIVATE_KEY", "../fixtures/master_ssh_key"),

		// Database configuration
		DatabaseURL:      getEnv("TEST_DATABASE_URL", "postgres://user:password@localhost:5432/airavata?sslmode=disable"),
		TestDatabaseURL:  getEnv("TEST_DATABASE_URL", "postgres://user:password@localhost:5432/airavata?sslmode=disable"),
		PostgresUser:     getEnv("POSTGRES_USER", "user"),
		PostgresPassword: getEnv("POSTGRES_PASSWORD", "password"),
		PostgresDB:       getEnv("POSTGRES_DB", "airavata"),

		// Service endpoints
		SpiceDBEndpoint: getEnv("SPICEDB_ENDPOINT", "localhost:50052"),
		SpiceDBToken:    getEnv("SPICEDB_TOKEN", "somerandomkeyhere"),
		VaultEndpoint:   getEnv("VAULT_ENDPOINT", "http://localhost:8200"),
		VaultToken:      getEnv("VAULT_TOKEN", "dev-token"),
		MinIOEndpoint:   getEnv("MINIO_ENDPOINT", "localhost:9000"),
		MinIOAccessKey:  getEnv("MINIO_ACCESS_KEY", "minioadmin"),
		MinIOSecretKey:  getEnv("MINIO_SECRET_KEY", "minioadmin"),

		// Compute resource configuration
		SlurmCluster1Name:  getEnv("SLURM_CLUSTER1_NAME", "SLURM Test Cluster 1"),
		SlurmCluster1Host:  getEnv("SLURM_CLUSTER1_HOST", "localhost"),
		SlurmCluster1Port:  getEnvInt("SLURM_CLUSTER1_PORT", 2223),
		SlurmCluster2Name:  getEnv("SLURM_CLUSTER2_NAME", "SLURM Test Cluster 2"),
		SlurmCluster2Host:  getEnv("SLURM_CLUSTER2_HOST", "localhost"),
		SlurmCluster2Port:  getEnvInt("SLURM_CLUSTER2_PORT", 2224),
		BareMetalNode1Name: getEnv("BAREMETAL_NODE1_NAME", "Bare Metal Test Node 1"),
		BareMetalNode1Host: getEnv("BAREMETAL_NODE1_HOST", "localhost"),
		BareMetalNode1Port: getEnvInt("BAREMETAL_NODE1_PORT", 2225),
		BareMetalNode2Name: getEnv("BAREMETAL_NODE2_NAME", "Bare Metal Test Node 2"),
		BareMetalNode2Host: getEnv("BAREMETAL_NODE2_HOST", "localhost"),
		BareMetalNode2Port: getEnvInt("BAREMETAL_NODE2_PORT", 2226),

		// Storage resource configuration
		SFTPName: getEnv("SFTP_NAME", "global-scratch"),
		SFTPHost: getEnv("SFTP_HOST", "localhost"),
		SFTPPort: getEnvInt("SFTP_PORT", 2222),
		NFSName:  getEnv("NFS_NAME", "nfs-storage"),
		NFSHost:  getEnv("NFS_HOST", "localhost"),
		NFSPort:  getEnvInt("NFS_PORT", 2049),
		S3Name:   getEnv("S3_NAME", "minio-storage"),
		S3Host:   getEnv("S3_HOST", "localhost"),
		S3Port:   getEnvInt("S3_PORT", 9000),

		// Test user configuration
		TestUserName:     getEnv("TEST_USER_NAME", "testuser"),
		TestUserEmail:    getEnv("TEST_USER_EMAIL", "test@example.com"),
		TestUserPassword: getEnv("TEST_USER_PASSWORD", "testpass123"),

		// Kubernetes configuration
		KubernetesClusterName: getEnv("KUBERNETES_CLUSTER_NAME", "docker-desktop"),
		KubernetesContext:     getEnv("KUBERNETES_CONTEXT", "docker-desktop"),
		KubernetesNamespace:   getEnv("KUBERNETES_NAMESPACE", "default"),
		KubernetesConfigPath:  getEnv("KUBECONFIG", filepath.Join(os.Getenv("HOME"), ".kube", "config")),

		// Test timeouts and retries
		DefaultTimeout:     getEnvInt("TEST_DEFAULT_TIMEOUT", 30),
		DefaultRetries:     getEnvInt("TEST_DEFAULT_RETRIES", 3),
		ResourceTimeout:    getEnvInt("TEST_RESOURCE_TIMEOUT", 60),
		CleanupTimeout:     getEnvInt("TEST_CLEANUP_TIMEOUT", 10),
		GRPCDialTimeout:    getEnvInt("TEST_GRPC_DIAL_TIMEOUT", 30),
		HTTPRequestTimeout: getEnvInt("TEST_HTTP_REQUEST_TIMEOUT", 30),

		// Fixture paths
		FixturesDir:         getEnv("TEST_FIXTURES_DIR", "tests/fixtures"),
		MasterKeyPath:       getEnv("TEST_MASTER_KEY_PATH", "tests/fixtures/master_ssh_key"),
		MasterPublicKeyPath: getEnv("TEST_MASTER_PUBLIC_KEY_PATH", "tests/fixtures/master_ssh_key.pub"),
	}
}

// Helper functions
func getEnv(key, defaultValue string) string {
	if value := os.Getenv(key); value != "" {
		return value
	}
	return defaultValue
}

func getEnvInt(key string, defaultValue int) int {
	if value := os.Getenv(key); value != "" {
		if intValue, err := strconv.Atoi(value); err == nil {
			return intValue
		}
	}
	return defaultValue
}

// GetSlurmCluster1Endpoint returns the full endpoint for SLURM cluster 1
func (c *TestConfig) GetSlurmCluster1Endpoint() string {
	return c.SlurmCluster1Host + ":" + strconv.Itoa(c.SlurmCluster1Port)
}

// GetSlurmCluster2Endpoint returns the full endpoint for SLURM cluster 2
func (c *TestConfig) GetSlurmCluster2Endpoint() string {
	return c.SlurmCluster2Host + ":" + strconv.Itoa(c.SlurmCluster2Port)
}

// GetBareMetalNode1Endpoint returns the full endpoint for bare metal node 1
func (c *TestConfig) GetBareMetalNode1Endpoint() string {
	return c.BareMetalNode1Host + ":" + strconv.Itoa(c.BareMetalNode1Port)
}

// GetBareMetalNode2Endpoint returns the full endpoint for bare metal node 2
func (c *TestConfig) GetBareMetalNode2Endpoint() string {
	return c.BareMetalNode2Host + ":" + strconv.Itoa(c.BareMetalNode2Port)
}

// GetSFTPEndpoint returns the full endpoint for SFTP storage
func (c *TestConfig) GetSFTPEndpoint() string {
	return c.SFTPHost + ":" + strconv.Itoa(c.SFTPPort)
}

// GetNFSEndpoint returns the full endpoint for NFS storage
func (c *TestConfig) GetNFSEndpoint() string {
	return c.NFSHost + ":" + strconv.Itoa(c.NFSPort)
}

// GetS3Endpoint returns the full endpoint for S3 storage
func (c *TestConfig) GetS3Endpoint() string {
	return c.S3Host + ":" + strconv.Itoa(c.S3Port)
}

// GetDefaultTimeout returns the default timeout in seconds
func (c *TestConfig) GetDefaultTimeout() int {
	return c.DefaultTimeout
}

// GetDefaultRetries returns the default number of retries
func (c *TestConfig) GetDefaultRetries() int {
	return c.DefaultRetries
}

// GetResourceTimeout returns the resource timeout in seconds
func (c *TestConfig) GetResourceTimeout() int {
	return c.ResourceTimeout
}

// GetCleanupTimeout returns the cleanup timeout in seconds
func (c *TestConfig) GetCleanupTimeout() int {
	return c.CleanupTimeout
}

// GetGRPCDialTimeout returns the gRPC dial timeout in seconds
func (c *TestConfig) GetGRPCDialTimeout() int {
	return c.GRPCDialTimeout
}

// GetHTTPRequestTimeout returns the HTTP request timeout in seconds
func (c *TestConfig) GetHTTPRequestTimeout() int {
	return c.HTTPRequestTimeout
}
