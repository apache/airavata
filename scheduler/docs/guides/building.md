# Building Guide

## Overview

This guide covers building the Airavata Scheduler system, including both the scheduler server and worker binaries, protocol buffer code generation, and deployment strategies.

## Prerequisites

### Required Software

- **Go 1.21+**: For building Go binaries
- **Protocol Buffers Compiler**: For generating gRPC code
- **Make**: For build automation (optional but recommended)

### Installation

#### Go
```bash
# Install Go 1.21+
wget https://go.dev/dl/go1.21.0.linux-amd64.tar.gz
sudo tar -C /usr/local -xzf go1.21.0.linux-amd64.tar.gz
export PATH=$PATH:/usr/local/go/bin
```

#### Protocol Buffers
```bash
# Install protoc
wget https://github.com/protocolbuffers/protobuf/releases/download/v21.12/protoc-21.12-linux-x86_64.zip
unzip protoc-21.12-linux-x86_64.zip -d /usr/local

# Install Go plugins
go install google.golang.org/protobuf/cmd/protoc-gen-go@latest
go install google.golang.org/grpc/cmd/protoc-gen-go-grpc@latest
```

## Build Process

### Quick Start

```bash
# Clone repository
git clone https://github.com/apache/airavata/scheduler.git
cd airavata-scheduler

# Install dependencies
go mod download

# Generate proto code
make proto

# Build both binaries
make build

# Verify build
ls -la build/
# Should show: scheduler, worker
```

### Build Targets

The Makefile provides several build targets:

```bash
# Build both binaries
make build

# Build scheduler only
make build-server

# Build worker only
make build-worker

# Generate proto code
make proto

# Clean build artifacts
make clean

# Run tests
make test

# Run all checks (lint, test, build)
make ci
```

### Manual Build

If you prefer to build manually:

```bash
# Create build directory
mkdir -p build

# Build scheduler
go build -o build/scheduler ./core/cmd

# Build worker
go build -o build/worker ./cmd/worker

# Verify binaries
./build/scheduler --help
./build/worker --help
```

## Protocol Buffer Generation

### Proto Files

The system uses Protocol Buffers for gRPC communication:

```
proto/
├── worker.proto         # Worker service definition
├── scheduler.proto      # Scheduler service definition
├── common.proto         # Common message types
├── data.proto          # Data transfer messages
├── experiment.proto    # Experiment messages
├── research.proto      # Research messages
└── resource.proto      # Resource messages
```

### Generation Process

```bash
# Generate all proto code
make proto

# Or manually
protoc --go_out=core/dto --go-grpc_out=core/dto \
    --go_opt=paths=source_relative \
    --go-grpc_opt=paths=source_relative \
    --proto_path=proto \
    proto/*.proto
```

### Generated Code

Proto generation creates Go code in the `core/dto/` directory:

```
core/dto/
├── worker.pb.go            # Generated message types
├── worker_grpc.pb.go       # Generated gRPC service
├── common.pb.go            # Common types
├── data.pb.go              # Data transfer types
├── experiment.pb.go        # Experiment types
├── research.pb.go          # Research workflow types
├── resource.pb.go          # Resource types
└── scheduler.pb.go         # Scheduler types
```

## Binary Details

### Scheduler Binary

**Location**: `build/scheduler`
**Source**: `core/cmd/main.go`
**Purpose**: Main scheduler server with HTTP API and gRPC services

```bash
# Run scheduler
./build/scheduler --mode=server

# Available flags
./build/scheduler --help
```

**Configuration**:
- HTTP server port (default: 8080)
- gRPC server port (default: 50051)
- Database connection string
- Worker binary configuration

### Worker Binary

**Location**: `build/worker`
**Source**: `cmd/worker/main.go`
**Purpose**: Standalone worker for task execution

```bash
# Run worker
./build/worker --server-address=localhost:50051

# Available flags
./build/worker --help
```

**Configuration**:
- Scheduler server address
- Worker ID
- Working directory
- Heartbeat interval
- Task timeout

## Development Builds

### Local Development

For local development, use the development build process:

```bash
# Install development dependencies
go mod download

# Generate proto code
make proto

# Build with debug symbols
go build -gcflags="all=-N -l" -o build/scheduler ./core/cmd
go build -gcflags="all=-N -l" -o build/worker ./cmd/worker

# Run with debug logging
./build/scheduler --log-level=debug --mode=server
./build/worker --log-level=debug --server-address=localhost:50051
```

### Hot Reloading

For rapid development, use hot reloading:

```bash
# Install air for hot reloading
go install github.com/cosmtrek/air@latest

# Run scheduler with hot reload
air -c .air.toml

# Or use go run for simple cases
go run ./core/cmd --mode=server
go run ./cmd/worker --server-address=localhost:50051
```

## Production Builds

### Optimized Builds

For production deployment, use optimized builds:

```bash
# Build with optimizations
CGO_ENABLED=0 GOOS=linux go build -a -installsuffix cgo -ldflags '-w -s' -o build/scheduler ./core/cmd
CGO_ENABLED=0 GOOS=linux go build -a -installsuffix cgo -ldflags '-w -s' -o build/worker ./cmd/worker

# Verify binary size
ls -lh build/
```

### Static Builds

For maximum portability, build static binaries:

```bash
# Build static binaries
CGO_ENABLED=0 go build -ldflags '-w -s -extldflags "-static"' -o build/scheduler ./core/cmd
CGO_ENABLED=0 go build -ldflags '-w -s -extldflags "-static"' -o build/worker ./cmd/worker

# Verify static linking
ldd build/scheduler  # Should show "not a dynamic executable"
ldd build/worker     # Should show "not a dynamic executable"
```

## Cross-Compilation

### Target Platforms

Build for different platforms:

```bash
# Linux AMD64
GOOS=linux GOARCH=amd64 go build -o build/scheduler-linux-amd64 ./core/cmd
GOOS=linux GOARCH=amd64 go build -o build/worker-linux-amd64 ./cmd/worker

# Linux ARM64
GOOS=linux GOARCH=arm64 go build -o build/scheduler-linux-arm64 ./core/cmd
GOOS=linux GOARCH=arm64 go build -o build/worker-linux-arm64 ./cmd/worker

# macOS AMD64
GOOS=darwin GOARCH=amd64 go build -o build/scheduler-darwin-amd64 ./core/cmd
GOOS=darwin GOARCH=amd64 go build -o build/worker-darwin-amd64 ./cmd/worker

# macOS ARM64 (Apple Silicon)
GOOS=darwin GOARCH=arm64 go build -o build/scheduler-darwin-arm64 ./core/cmd
GOOS=darwin GOARCH=arm64 go build -o build/worker-darwin-arm64 ./cmd/worker

# Windows AMD64
GOOS=windows GOARCH=amd64 go build -o build/scheduler-windows-amd64.exe ./core/cmd
GOOS=windows GOARCH=amd64 go build -o build/worker-windows-amd64.exe ./cmd/worker
```

### Build Script

Create a build script for multiple platforms:

```bash
#!/bin/bash
# build-all.sh

set -e

PLATFORMS=(
    "linux/amd64"
    "linux/arm64"
    "darwin/amd64"
    "darwin/arm64"
    "windows/amd64"
)

for platform in "${PLATFORMS[@]}"; do
    IFS='/' read -r os arch <<< "$platform"
    
    echo "Building for $os/$arch..."
    
    # Build scheduler
    GOOS=$os GOARCH=$arch go build -o "build/scheduler-$os-$arch" ./core/cmd
    
    # Build worker
    GOOS=$os GOARCH=$arch go build -o "build/worker-$os-$arch" ./cmd/worker
    
    # Add .exe extension for Windows
    if [ "$os" = "windows" ]; then
        mv "build/scheduler-$os-$arch" "build/scheduler-$os-$arch.exe"
        mv "build/worker-$os-$arch" "build/worker-$os-$arch.exe"
    fi
done

echo "Build complete!"
ls -la build/
```

## Docker Builds

### Multi-Stage Dockerfile

```dockerfile
# Build stage
FROM golang:1.21-alpine AS builder

# Install dependencies
RUN apk add --no-cache git make protoc

# Set working directory
WORKDIR /app

# Copy go mod files
COPY go.mod go.sum ./
RUN go mod download

# Copy source code
COPY . .

# Generate proto code
RUN make proto

# Build binaries
RUN make build

# Runtime stage
FROM alpine:latest

# Install runtime dependencies
RUN apk add --no-cache ca-certificates

# Copy binaries
COPY --from=builder /app/build/scheduler /usr/local/bin/
COPY --from=builder /app/build/worker /usr/local/bin/

# Set permissions
RUN chmod +x /usr/local/bin/scheduler /usr/local/bin/worker

# Expose ports
EXPOSE 8080 50051

# Default command
CMD ["scheduler", "--mode=server"]
```

### Build Docker Images

```bash
# Build scheduler image
docker build -t airavata-scheduler:latest .

# Build worker image
docker build -f Dockerfile.worker -t airavata-worker:latest .

# Build multi-arch images
docker buildx build --platform linux/amd64,linux/arm64 -t airavata-scheduler:latest .
```

## Binary Distribution

### HTTP Endpoint

The scheduler provides an HTTP endpoint for worker binary distribution:

```bash
# Download worker binary
curl -O http://scheduler:8080/api/worker-binary

# Or with authentication
curl -H "Authorization: Bearer $TOKEN" -O http://scheduler:8080/api/worker-binary
```

### Configuration

Configure worker binary distribution in the scheduler:

```go
// In core/app/bootstrap.go
config := &app.Config{
    Worker: struct {
        BinaryPath        string `json:"binary_path"`
        BinaryURL         string `json:"binary_url"`
        DefaultWorkingDir string `json:"default_working_dir"`
    }{
        BinaryPath:        "./build/worker",
        BinaryURL:         "http://localhost:8080/api/worker-binary",
        DefaultWorkingDir: "/tmp/worker",
    },
}
```

### Environment Variables

```bash
# Worker binary configuration
export WORKER_BINARY_PATH="./build/worker"
export WORKER_BINARY_URL="http://localhost:8080/api/worker-binary"
export WORKER_WORKING_DIR="/tmp/worker"
```

## Testing Builds

### Unit Tests

```bash
# Run unit tests
make test-unit

# Run with coverage
make test-coverage

# Run specific test
go test ./tests/unit/core -v -run TestSpecific
```

### Integration Tests

```bash
# Start test services
docker compose --profile test up -d

# Run integration tests
make test-integration

# Clean up
docker compose --profile test down
```

### Build Verification

```bash
# Verify binaries work
./build/scheduler --help
./build/worker --help

# Test gRPC connectivity
grpcurl -plaintext localhost:50051 list

# Test HTTP API
curl http://localhost:8080/health
```

## Troubleshooting

### Common Build Issues

#### Proto Generation Fails

**Error**: `protoc: command not found`
**Solution**: Install Protocol Buffers compiler

```bash
# Install protoc
wget https://github.com/protocolbuffers/protobuf/releases/download/v21.12/protoc-21.12-linux-x86_64.zip
unzip protoc-21.12-linux-x86_64.zip -d /usr/local
export PATH=$PATH:/usr/local/bin
```

#### Go Modules Issues

**Error**: `go: cannot find main module`
**Solution**: Initialize Go module

```bash
# Initialize module
go mod init github.com/apache/airavata/scheduler

# Download dependencies
go mod download

# Tidy dependencies
go mod tidy
```

#### Build Failures

**Error**: `undefined: grpc.Server`
**Solution**: Install gRPC dependencies

```bash
# Install gRPC dependencies
go get google.golang.org/grpc@latest
go get google.golang.org/grpc/codes@latest
go get google.golang.org/grpc/status@latest
```

### Debug Builds

#### Enable Debug Symbols

```bash
# Build with debug symbols
go build -gcflags="all=-N -l" -o build/scheduler ./core/cmd
go build -gcflags="all=-N -l" -o build/worker ./cmd/worker
```

#### Enable Race Detection

```bash
# Build with race detector
go build -race -o build/scheduler ./core/cmd
go build -race -o build/worker ./cmd/worker
```

#### Verbose Build Output

```bash
# Build with verbose output
go build -v -o build/scheduler ./core/cmd
go build -v -o build/worker ./cmd/worker
```

## Performance Optimization

### Build Performance

#### Parallel Builds

```bash
# Build with parallel jobs
go build -p 4 -o build/scheduler ./core/cmd
go build -p 4 -o build/worker ./cmd/worker
```

#### Build Cache

```bash
# Enable build cache
export GOCACHE=/tmp/go-cache
export GOMODCACHE=/tmp/go-mod-cache

# Build with cache
go build -o build/scheduler ./core/cmd
go build -o build/worker ./cmd/worker
```

### Binary Performance

#### Optimize for Size

```bash
# Build with size optimization
go build -ldflags '-w -s' -o build/scheduler ./core/cmd
go build -ldflags '-w -s' -o build/worker ./cmd/worker
```

#### Optimize for Speed

```bash
# Build with speed optimization
go build -ldflags '-w -s' -o build/scheduler ./core/cmd
go build -ldflags '-w -s' -o build/worker ./cmd/worker
```

## Best Practices

### Build Process

1. **Use Makefile**: Leverage build automation
2. **Generate Proto**: Always generate proto code before building
3. **Test Builds**: Verify binaries work after building
4. **Clean Builds**: Use clean build directories
5. **Version Control**: Tag releases with version numbers

### Binary Management

1. **Static Linking**: Use static builds for portability
2. **Size Optimization**: Strip debug symbols for production
3. **Cross-Compilation**: Build for target platforms
4. **Distribution**: Use HTTP endpoints for binary distribution
5. **Verification**: Verify binary integrity and functionality

### Development Workflow

1. **Hot Reloading**: Use air or similar tools for development
2. **Debug Builds**: Use debug symbols for debugging
3. **Test Coverage**: Maintain high test coverage
4. **CI/CD**: Automate builds in continuous integration
5. **Documentation**: Keep build documentation up to date

For more information, see the [Architecture Guide](architecture.md) and [Development Guide](development.md).
