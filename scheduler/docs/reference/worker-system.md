# Worker System Documentation

## Overview

The Airavata Scheduler uses a distributed worker architecture where standalone worker binaries communicate with the scheduler via gRPC. This design enables scalable, fault-tolerant task execution across multiple compute resources.

## Architecture

### System Components

```
┌─────────────────────────────────────────────────────────────────┐
│                    Scheduler Server                            │
├─────────────────────────────────────────────────────────────────┤
│  gRPC Server (Port 50051)                                      │
│  ├── WorkerService (generated from proto/worker.proto)        │
│  ├── Task Assignment                                          │
│  ├── Status Monitoring                                        │
│  └── Heartbeat Management                                     │
└─────────────────────────────────────────────────────────────────┘
                                │
                                │ gRPC
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│                    Worker Binary                               │
├─────────────────────────────────────────────────────────────────┤
│  gRPC Client                                                   │
│  ├── Task Polling                                             │
│  ├── Status Reporting                                         │
│  └── Heartbeat Sending                                        │
├─────────────────────────────────────────────────────────────────┤
│  Task Execution Engine                                        │
│  ├── Script Generation                                        │
│  ├── Data Staging                                             │
│  ├── Command Execution                                        │
│  └── Result Collection                                        │
└─────────────────────────────────────────────────────────────────┘
```

### Key Benefits

- **Scalability**: Workers can be deployed across multiple compute resources
- **Isolation**: Worker failures don't affect the scheduler
- **Flexibility**: Workers can be deployed on different platforms (SLURM, Kubernetes, Bare Metal)
- **Efficiency**: Direct binary deployment without container overhead
- **Fault Tolerance**: Automatic worker recovery and task reassignment

## Worker Lifecycle

### 1. Deployment

Workers are deployed to compute resources using runtime-specific scripts:

```bash
# SLURM deployment
sbatch worker_spawn_script.sh

# Kubernetes deployment
kubectl apply -f worker_job.yaml

# Bare metal deployment
ssh compute-node 'bash -s' < worker_script.sh
```

### 2. Registration

Upon startup, workers connect to the scheduler gRPC server:

```go
// Worker registration
conn, err := grpc.Dial("scheduler:50051", grpc.WithInsecure())
client := workerpb.NewWorkerServiceClient(conn)

// Register with scheduler
resp, err := client.RegisterWorker(ctx, &workerpb.RegisterWorkerRequest{
    WorkerId: workerID,
    Capabilities: capabilities,
    Status: workerpb.WorkerStatus_AVAILABLE,
})
```

### 3. Task Polling

Workers continuously poll for available tasks:

```go
// Poll for tasks
for {
    resp, err := client.PollForTask(ctx, &workerpb.PollForTaskRequest{
        WorkerId: workerID,
        Capabilities: capabilities,
    })
    
    if resp.Task != nil {
        // Execute task
        executeTask(resp.Task)
    }
    
    time.Sleep(pollInterval)
}
```

### 4. Task Execution

Workers execute assigned tasks with proper isolation:

```go
func executeTask(task *workerpb.Task) error {
    // Update status to running
    client.UpdateTaskStatus(ctx, &workerpb.UpdateTaskStatusRequest{
        TaskId: task.Id,
        Status: workerpb.TaskStatus_RUNNING,
    })
    
    // Stage input files
    for _, input := range task.InputFiles {
        stageFile(input)
    }
    
    // Execute command
    cmd := exec.Command("bash", "-c", task.Command)
    output, err := cmd.CombinedOutput()
    
    // Update status
    status := workerpb.TaskStatus_COMPLETED
    if err != nil {
        status = workerpb.TaskStatus_FAILED
    }
    
    client.UpdateTaskStatus(ctx, &workerpb.UpdateTaskStatusRequest{
        TaskId: task.Id,
        Status: status,
        Output: string(output),
    })
    
    return err
}
```

### 5. Status Reporting

Workers report progress and completion status:

```go
// Send heartbeat
client.SendHeartbeat(ctx, &workerpb.HeartbeatRequest{
    WorkerId: workerID,
    Status: workerpb.WorkerStatus_AVAILABLE,
    Metrics: &workerpb.WorkerMetrics{
        CpuUsage: cpuUsage,
        MemoryUsage: memoryUsage,
        ActiveTasks: activeTaskCount,
    },
})
```

### 6. Cleanup

Workers clean up resources and report final status:

```go
// Cleanup on shutdown
client.UpdateWorkerStatus(ctx, &workerpb.UpdateWorkerStatusRequest{
    WorkerId: workerID,
    Status: workerpb.WorkerStatus_TERMINATED,
})

conn.Close()
```

## Script Generation

The system generates runtime-specific scripts for deploying workers to different compute resources.

### SLURM Scripts

```bash
#!/bin/bash
#SBATCH --job-name=worker_${WORKER_ID}
#SBATCH --time=${WALLTIME}
#SBATCH --cpus-per-task=${CPU_CORES}
#SBATCH --mem=${MEMORY_MB}
#SBATCH --partition=${QUEUE}
#SBATCH --account=${ACCOUNT}

# Set up environment
export WORKER_ID="${WORKER_ID}"
export EXPERIMENT_ID="${EXPERIMENT_ID}"
export COMPUTE_RESOURCE_ID="${COMPUTE_RESOURCE_ID}"
export WORKING_DIR="${WORKING_DIR}"
export WORKER_BINARY_URL="${WORKER_BINARY_URL}"
export SERVER_ADDRESS="${SERVER_ADDRESS}"
export SERVER_PORT="${SERVER_PORT}"

# Create working directory
mkdir -p "${WORKING_DIR}"
cd "${WORKING_DIR}"

# Download worker binary
echo "Downloading worker binary from ${WORKER_BINARY_URL}"
curl -L "${WORKER_BINARY_URL}" -o worker
chmod +x worker

# Start worker
echo "Starting worker with ID: ${WORKER_ID}"
./worker \
    --server-address="${SERVER_ADDRESS}:${SERVER_PORT}" \
    --worker-id="${WORKER_ID}" \
    --working-dir="${WORKING_DIR}" \
    --experiment-id="${EXPERIMENT_ID}" \
    --compute-resource-id="${COMPUTE_RESOURCE_ID}"

echo "Worker ${WORKER_ID} completed"
```

### Kubernetes Manifests

```yaml
apiVersion: batch/v1
kind: Job
metadata:
  name: worker-${WORKER_ID}
  namespace: airavata
spec:
  template:
    spec:
      restartPolicy: Never
      containers:
      - name: worker
        image: worker-binary:latest
        command: ["./worker"]
        args:
        - "--server-address=${SERVER_ADDRESS}:${SERVER_PORT}"
        - "--worker-id=${WORKER_ID}"
        - "--working-dir=${WORKING_DIR}"
        - "--experiment-id=${EXPERIMENT_ID}"
        - "--compute-resource-id=${COMPUTE_RESOURCE_ID}"
        env:
        - name: WORKER_ID
          value: "${WORKER_ID}"
        - name: EXPERIMENT_ID
          value: "${EXPERIMENT_ID}"
        - name: COMPUTE_RESOURCE_ID
          value: "${COMPUTE_RESOURCE_ID}"
        - name: WORKING_DIR
          value: "${WORKING_DIR}"
        resources:
          requests:
            cpu: "${CPU_CORES}"
            memory: "${MEMORY_MB}Mi"
          limits:
            cpu: "${CPU_CORES}"
            memory: "${MEMORY_MB}Mi"
        volumeMounts:
        - name: worker-storage
          mountPath: "${WORKING_DIR}"
      volumes:
      - name: worker-storage
        emptyDir: {}
```

### Bare Metal Scripts

```bash
#!/bin/bash
set -euo pipefail

# Configuration
WORKER_ID="${WORKER_ID}"
EXPERIMENT_ID="${EXPERIMENT_ID}"
COMPUTE_RESOURCE_ID="${COMPUTE_RESOURCE_ID}"
WORKING_DIR="${WORKING_DIR}"
WORKER_BINARY_URL="${WORKER_BINARY_URL}"
SERVER_ADDRESS="${SERVER_ADDRESS}"
SERVER_PORT="${SERVER_PORT}"
WALLTIME_SECONDS="${WALLTIME_SECONDS}"

# Create working directory
mkdir -p "${WORKING_DIR}"
cd "${WORKING_DIR}"

# Download worker binary
echo "Downloading worker binary from ${WORKER_BINARY_URL}"
curl -L "${WORKER_BINARY_URL}" -o worker
chmod +x worker

# Set up signal handling for cleanup
cleanup() {
    echo "Cleaning up worker ${WORKER_ID}"
    # Kill any running processes
    pkill -f "worker.*${WORKER_ID}" || true
    # Clean up working directory
    rm -rf "${WORKING_DIR}" || true
}
trap cleanup EXIT INT TERM

# Start worker with timeout
echo "Starting worker with ID: ${WORKER_ID}"
timeout "${WALLTIME_SECONDS}" ./worker \
    --server-address="${SERVER_ADDRESS}:${SERVER_PORT}" \
    --worker-id="${WORKER_ID}" \
    --working-dir="${WORKING_DIR}" \
    --experiment-id="${EXPERIMENT_ID}" \
    --compute-resource-id="${COMPUTE_RESOURCE_ID}"

echo "Worker ${WORKER_ID} completed"
```

## Configuration

### Worker Configuration

Workers are configured through environment variables and command-line flags:

```bash
# Required configuration
--server-address=localhost:50051    # Scheduler gRPC server address
--worker-id=worker_12345           # Unique worker identifier
--working-dir=/tmp/worker          # Working directory for tasks

# Optional configuration
--heartbeat-interval=30s           # Heartbeat frequency
--task-timeout=1h                  # Maximum task execution time
--log-level=info                   # Logging level
--experiment-id=exp_123            # Associated experiment ID
--compute-resource-id=slurm_01     # Compute resource ID
```

### Environment Variables

```bash
# Worker configuration
export WORKER_ID="worker_$(date +%s)_$$"
export SERVER_ADDRESS="localhost:50051"
export WORKING_DIR="/tmp/worker"
export HEARTBEAT_INTERVAL="30s"
export TASK_TIMEOUT="1h"
export LOG_LEVEL="info"

# Experiment context
export EXPERIMENT_ID="exp_12345"
export COMPUTE_RESOURCE_ID="slurm_cluster_01"

# Network configuration
export GRPC_KEEPALIVE_TIME="30s"
export GRPC_KEEPALIVE_TIMEOUT="5s"
export GRPC_KEEPALIVE_PERMIT_WITHOUT_STREAMS=true
```

## Task Execution

### Task Assignment

Tasks are assigned to workers based on:

- **Capabilities**: CPU, memory, GPU requirements
- **Availability**: Worker status and current load
- **Affinity**: Data locality and resource preferences
- **Priority**: Task priority and deadline constraints

### Data Staging

Input files are staged to workers before task execution:

```go
func stageInputFiles(task *workerpb.Task) error {
    for _, input := range task.InputFiles {
        // Download from storage
        err := downloadFile(input.Source, input.Destination)
        if err != nil {
            return fmt.Errorf("failed to stage file %s: %w", input.Source, err)
        }
    }
    return nil
}
```

### Command Execution

Tasks are executed in isolated environments:

```go
func executeCommand(command string, workingDir string) (*exec.Cmd, error) {
    cmd := exec.Command("bash", "-c", command)
    cmd.Dir = workingDir
    
    // Set up environment
    cmd.Env = append(os.Environ(),
        "WORKING_DIR="+workingDir,
        "TASK_ID="+taskID,
    )
    
    // Set resource limits
    cmd.SysProcAttr = &syscall.SysProcAttr{
        Setpgid: true,
    }
    
    return cmd, nil
}
```

### Result Collection

Output files are collected after task completion:

```go
func collectOutputFiles(task *workerpb.Task) error {
    for _, output := range task.OutputFiles {
        // Upload to storage
        err := uploadFile(output.Source, output.Destination)
        if err != nil {
            return fmt.Errorf("failed to collect file %s: %w", output.Source, err)
        }
    }
    return nil
}
```

## Monitoring and Health Checks

### Heartbeat System

Workers send periodic heartbeats to the scheduler:

```go
func sendHeartbeat(client workerpb.WorkerServiceClient, workerID string) {
    ticker := time.NewTicker(heartbeatInterval)
    defer ticker.Stop()
    
    for range ticker.C {
        _, err := client.SendHeartbeat(ctx, &workerpb.HeartbeatRequest{
            WorkerId: workerID,
            Status: workerpb.WorkerStatus_AVAILABLE,
            Metrics: &workerpb.WorkerMetrics{
                CpuUsage: getCPUUsage(),
                MemoryUsage: getMemoryUsage(),
                ActiveTasks: getActiveTaskCount(),
                Timestamp: time.Now().Unix(),
            },
        })
        
        if err != nil {
            log.Printf("Failed to send heartbeat: %v", err)
        }
    }
}
```

### Health Monitoring

The scheduler monitors worker health and handles failures:

```go
func monitorWorkerHealth(workerID string) {
    ticker := time.NewTicker(healthCheckInterval)
    defer ticker.Stop()
    
    for range ticker.C {
        if !isWorkerHealthy(workerID) {
            // Mark worker as unhealthy
            markWorkerUnhealthy(workerID)
            
            // Reassign tasks to other workers
            reassignWorkerTasks(workerID)
        }
    }
}
```

## Error Handling

### Worker Failures

When workers fail, the scheduler:

1. **Detects Failure**: Via missed heartbeats or connection errors
2. **Marks Unhealthy**: Updates worker status in database
3. **Reassigns Tasks**: Moves pending tasks to other workers
4. **Cleans Up**: Removes worker from active pool

### Task Failures

Task failures are handled gracefully:

```go
func handleTaskFailure(taskID string, err error) {
    // Update task status
    updateTaskStatus(taskID, TaskStatusFailed, err.Error())
    
    // Log failure
    log.Printf("Task %s failed: %v", taskID, err)
    
    // Retry if appropriate
    if shouldRetry(taskID) {
        scheduleTaskRetry(taskID)
    }
}
```

### Network Failures

Network connectivity issues are handled with:

- **Retry Logic**: Exponential backoff for failed requests
- **Circuit Breaker**: Prevent cascading failures
- **Graceful Degradation**: Continue with available workers

## Security

### Authentication

Workers authenticate with the scheduler using:

- **TLS Certificates**: Mutual TLS for gRPC connections
- **API Keys**: Worker-specific authentication tokens
- **Network Policies**: Firewall rules and network segmentation

### Isolation

Task execution is isolated through:

- **Process Isolation**: Separate processes for each task
- **Resource Limits**: CPU, memory, and disk quotas
- **Network Isolation**: Restricted network access
- **File System Isolation**: Sandboxed working directories

## Performance Optimization

### Connection Pooling

gRPC connections are pooled for efficiency:

```go
type ConnectionPool struct {
    connections map[string]*grpc.ClientConn
    mutex       sync.RWMutex
}

func (p *ConnectionPool) GetConnection(address string) (*grpc.ClientConn, error) {
    p.mutex.RLock()
    conn, exists := p.connections[address]
    p.mutex.RUnlock()
    
    if exists {
        return conn, nil
    }
    
    // Create new connection
    conn, err := grpc.Dial(address, grpc.WithInsecure())
    if err != nil {
        return nil, err
    }
    
    p.mutex.Lock()
    p.connections[address] = conn
    p.mutex.Unlock()
    
    return conn, nil
}
```

### Batch Operations

Multiple operations are batched for efficiency:

```go
func batchUpdateTaskStatus(updates []TaskStatusUpdate) error {
    req := &workerpb.BatchUpdateTaskStatusRequest{
        Updates: make([]*workerpb.TaskStatusUpdate, len(updates)),
    }
    
    for i, update := range updates {
        req.Updates[i] = &workerpb.TaskStatusUpdate{
            TaskId: update.TaskID,
            Status: update.Status,
            Output: update.Output,
        }
    }
    
    _, err := client.BatchUpdateTaskStatus(ctx, req)
    return err
}
```

## Troubleshooting

### Common Issues

#### Worker Not Connecting

**Symptoms**: Worker fails to connect to scheduler
**Causes**: Network issues, incorrect server address, firewall blocking
**Solutions**:
- Verify network connectivity: `telnet scheduler-host 50051`
- Check firewall rules
- Verify server address and port configuration

#### Task Execution Failures

**Symptoms**: Tasks fail to execute or complete
**Causes**: Resource limits, permission issues, command errors
**Solutions**:
- Check worker logs for error messages
- Verify resource limits and permissions
- Test commands manually on worker node

#### High Memory Usage

**Symptoms**: Workers consuming excessive memory
**Causes**: Memory leaks, large input files, inefficient processing
**Solutions**:
- Monitor memory usage with `htop` or `ps`
- Implement memory limits for tasks
- Optimize data processing algorithms

### Debugging

#### Enable Debug Logging

```bash
# Set debug log level
export LOG_LEVEL=debug

# Start worker with verbose output
./worker --log-level=debug --server-address=localhost:50051
```

#### Monitor Worker Status

```bash
# Check worker processes
ps aux | grep worker

# Monitor network connections
netstat -an | grep 50051

# Check worker logs
tail -f /var/log/worker.log
```

#### Test gRPC Connectivity

```bash
# Test gRPC server connectivity
grpcurl -plaintext localhost:50051 list

# Test specific service
grpcurl -plaintext localhost:50051 worker.WorkerService/ListWorkers
```

## Best Practices

### Worker Deployment

1. **Use Resource Limits**: Set appropriate CPU and memory limits
2. **Monitor Health**: Implement comprehensive health checks
3. **Handle Failures**: Implement proper error handling and recovery
4. **Secure Communication**: Use TLS for gRPC connections
5. **Log Everything**: Comprehensive logging for debugging

### Task Execution

1. **Isolate Tasks**: Run each task in separate process
2. **Limit Resources**: Set appropriate resource quotas
3. **Clean Up**: Always clean up temporary files and processes
4. **Validate Inputs**: Verify input files and parameters
5. **Handle Timeouts**: Implement proper timeout handling

### Performance

1. **Connection Pooling**: Reuse gRPC connections
2. **Batch Operations**: Group multiple operations together
3. **Async Processing**: Use asynchronous operations where possible
4. **Resource Monitoring**: Monitor CPU, memory, and disk usage
5. **Load Balancing**: Distribute tasks evenly across workers

For more information, see the [Architecture Guide](architecture.md) and [Development Guide](development.md).
