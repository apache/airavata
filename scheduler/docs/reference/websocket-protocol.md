# WebSocket Protocol Documentation

This document describes the WebSocket protocol used by the Airavata Scheduler for real-time communication between the server and client applications.

## Table of Contents

1. [Overview](#overview)
2. [Connection Setup](#connection-setup)
3. [Authentication](#authentication)
4. [Message Format](#message-format)
5. [Message Types](#message-types)
6. [Subscription Management](#subscription-management)
7. [Event Broadcasting](#event-broadcasting)
8. [Error Handling](#error-handling)
9. [Connection Management](#connection-management)
10. [Examples](#examples)

## Overview

The Airavata Scheduler WebSocket protocol provides real-time updates for:

- Experiment status changes
- Task progress updates
- Worker status changes
- System health updates
- User-specific notifications

### Key Features

- **Real-time Updates**: Instant notification of status changes
- **Selective Subscriptions**: Subscribe to specific resources or events
- **Authentication**: JWT-based authentication for secure connections
- **Automatic Reconnection**: Built-in reconnection logic for reliability
- **Message Acknowledgment**: Ping/pong mechanism for connection health

## Connection Setup

### WebSocket Endpoints

The system provides several WebSocket endpoints for different types of subscriptions:

```
ws://localhost:8080/ws/experiments/{experimentId}
ws://localhost:8080/ws/tasks/{taskId}
ws://localhost:8080/ws/projects/{projectId}
ws://localhost:8080/ws/user
```

### Connection URL Format

```
ws://host:port/ws/{resourceType}/{resourceId}?token={jwt_token}
```

**Parameters:**
- `host`: Server hostname or IP address
- `port`: Server port (default: 8080)
- `resourceType`: Type of resource to subscribe to
- `resourceId`: ID of the specific resource
- `token`: JWT authentication token

### Example Connections

```javascript
// Connect to experiment updates
const experimentWs = new WebSocket('ws://localhost:8080/ws/experiments/exp-123?token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...');

// Connect to user-wide updates
const userWs = new WebSocket('ws://localhost:8080/ws/user?token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...');

// Connect to project updates
const projectWs = new WebSocket('ws://localhost:8080/ws/projects/proj-456?token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...');
```

## Authentication

### JWT Token Authentication

All WebSocket connections require a valid JWT token for authentication. The token can be provided in two ways:

1. **Query Parameter** (Recommended):
   ```
   ws://localhost:8080/ws/experiments/exp-123?token=your_jwt_token
   ```

2. **Authorization Header** (Alternative):
   ```javascript
   const ws = new WebSocket('ws://localhost:8080/ws/experiments/exp-123', {
     headers: {
       'Authorization': 'Bearer your_jwt_token'
     }
   });
   ```

### Token Validation

The server validates the JWT token on connection and:

- **Valid Token**: Connection established successfully
- **Invalid Token**: Connection closed with error message
- **Expired Token**: Connection closed with authentication error
- **Missing Token**: Connection closed with authentication required error

### Error Responses

```json
{
  "type": "error",
  "id": "error-123",
  "timestamp": "2024-01-15T10:30:00Z",
  "error": "Authentication required"
}
```

## Message Format

### Standard Message Structure

All WebSocket messages follow a consistent JSON format:

```json
{
  "type": "message_type",
  "id": "unique_message_id",
  "timestamp": "2024-01-15T10:30:00Z",
  "data": {
    // Message-specific data
  },
  "error": "error_message_if_applicable"
}
```

### Message Fields

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `type` | string | Yes | Message type identifier |
| `id` | string | Yes | Unique message identifier |
| `timestamp` | string | Yes | ISO 8601 timestamp |
| `data` | object | No | Message payload |
| `error` | string | No | Error message (for error types) |

### Message ID Format

Message IDs follow a specific format for easy identification:

- **Experiment Events**: `exp_{experimentId}_{timestamp}`
- **Task Events**: `task_{taskId}_{timestamp}`
- **Worker Events**: `worker_{workerId}_{timestamp}`
- **System Events**: `system_{timestamp}`
- **User Events**: `user_{userId}_{timestamp}`

## Message Types

### System Messages

#### Ping Message
```json
{
  "type": "ping",
  "id": "ping-123",
  "timestamp": "2024-01-15T10:30:00Z"
}
```

#### Pong Message
```json
{
  "type": "pong",
  "id": "pong-123",
  "timestamp": "2024-01-15T10:30:00Z"
}
```

#### System Status
```json
{
  "type": "system_status",
  "id": "system-123",
  "timestamp": "2024-01-15T10:30:00Z",
  "data": {
    "totalConnections": 150,
    "activeConnections": 120,
    "totalMessages": 15420,
    "messagesPerSecond": 25.5,
    "averageLatency": "0.05s",
    "uptime": "2h 30m 15s"
  }
}
```

#### Error Message
```json
{
  "type": "error",
  "id": "error-123",
  "timestamp": "2024-01-15T10:30:00Z",
  "error": "Invalid message format"
}
```

### Experiment Messages

#### Experiment Created
```json
{
  "type": "experiment_created",
  "id": "exp_exp-123_1642248600",
  "timestamp": "2024-01-15T10:30:00Z",
  "data": {
    "experiment": {
      "id": "exp-123",
      "name": "Parameter Sweep",
      "status": "CREATED",
      "ownerId": "user-456",
      "projectId": "proj-789"
    },
    "summary": {
      "id": "exp-123",
      "name": "Parameter Sweep",
      "status": "CREATED",
      "ownerId": "user-456"
    }
  }
}
```

#### Experiment Updated
```json
{
  "type": "experiment_updated",
  "id": "exp_exp-123_1642248660",
  "timestamp": "2024-01-15T10:31:00Z",
  "data": {
    "experiment": {
      "id": "exp-123",
      "name": "Parameter Sweep",
      "status": "RUNNING",
      "ownerId": "user-456",
      "projectId": "proj-789"
    },
    "summary": {
      "id": "exp-123",
      "name": "Parameter Sweep",
      "status": "RUNNING",
      "ownerId": "user-456"
    }
  }
}
```

#### Experiment Progress
```json
{
  "type": "experiment_progress",
  "id": "exp_exp-123_1642248720",
  "timestamp": "2024-01-15T10:32:00Z",
  "data": {
    "experimentId": "exp-123",
    "totalTasks": 100,
    "completedTasks": 45,
    "failedTasks": 5,
    "runningTasks": 10,
    "progressPercent": 45.0,
    "estimatedTimeRemaining": "1h 30m",
    "lastUpdated": "2024-01-15T10:32:00Z"
  }
}
```

#### Experiment Completed
```json
{
  "type": "experiment_completed",
  "id": "exp_exp-123_1642249200",
  "timestamp": "2024-01-15T10:40:00Z",
  "data": {
    "experiment": {
      "id": "exp-123",
      "name": "Parameter Sweep",
      "status": "COMPLETED",
      "ownerId": "user-456",
      "projectId": "proj-789"
    },
    "summary": {
      "id": "exp-123",
      "name": "Parameter Sweep",
      "status": "COMPLETED",
      "ownerId": "user-456"
    }
  }
}
```

#### Experiment Failed
```json
{
  "type": "experiment_failed",
  "id": "exp_exp-123_1642249260",
  "timestamp": "2024-01-15T10:41:00Z",
  "data": {
    "experiment": {
      "id": "exp-123",
      "name": "Parameter Sweep",
      "status": "FAILED",
      "ownerId": "user-456",
      "projectId": "proj-789"
    },
    "summary": {
      "id": "exp-123",
      "name": "Parameter Sweep",
      "status": "FAILED",
      "ownerId": "user-456"
    }
  }
}
```

### Task Messages

#### Task Created
```json
{
  "type": "task_created",
  "id": "task_task-456_1642248600",
  "timestamp": "2024-01-15T10:30:00Z",
  "data": {
    "task": {
      "id": "task-456",
      "experimentId": "exp-123",
      "status": "CREATED",
      "command": "python script.py --param1 value1",
      "workerId": null
    },
    "summary": {
      "id": "task-456",
      "experimentId": "exp-123",
      "status": "CREATED",
      "workerId": null
    }
  }
}
```

#### Task Updated
```json
{
  "type": "task_updated",
  "id": "task_task-456_1642248660",
  "timestamp": "2024-01-15T10:31:00Z",
  "data": {
    "task": {
      "id": "task-456",
      "experimentId": "exp-123",
      "status": "RUNNING",
      "command": "python script.py --param1 value1",
      "workerId": "worker-789"
    },
    "summary": {
      "id": "task-456",
      "experimentId": "exp-123",
      "status": "RUNNING",
      "workerId": "worker-789"
    }
  }
}
```

#### Task Progress
```json
{
  "type": "task_progress",
  "id": "task_task-456_1642248720",
  "timestamp": "2024-01-15T10:32:00Z",
  "data": {
    "taskId": "task-456",
    "experimentId": "exp-123",
    "status": "RUNNING",
    "progressPercent": 75.0,
    "currentStage": "RUNNING",
    "workerId": "worker-789",
    "startedAt": "2024-01-15T10:31:00Z",
    "estimatedCompletion": "2024-01-15T10:35:00Z",
    "lastUpdated": "2024-01-15T10:32:00Z"
  }
}
```

#### Task Completed
```json
{
  "type": "task_completed",
  "id": "task_task-456_1642249200",
  "timestamp": "2024-01-15T10:40:00Z",
  "data": {
    "task": {
      "id": "task-456",
      "experimentId": "exp-123",
      "status": "COMPLETED",
      "command": "python script.py --param1 value1",
      "workerId": "worker-789"
    },
    "summary": {
      "id": "task-456",
      "experimentId": "exp-123",
      "status": "COMPLETED",
      "workerId": "worker-789"
    }
  }
}
```

#### Task Failed
```json
{
  "type": "task_failed",
  "id": "task_task-456_1642249260",
  "timestamp": "2024-01-15T10:41:00Z",
  "data": {
    "task": {
      "id": "task-456",
      "experimentId": "exp-123",
      "status": "FAILED",
      "command": "python script.py --param1 value1",
      "workerId": "worker-789",
      "error": "Script execution failed"
    },
    "summary": {
      "id": "task-456",
      "experimentId": "exp-123",
      "status": "FAILED",
      "workerId": "worker-789",
      "error": "Script execution failed"
    }
  }
}
```

### Worker Messages

#### Worker Registered
```json
{
  "type": "worker_registered",
  "id": "worker_worker-789_1642248600",
  "timestamp": "2024-01-15T10:30:00Z",
  "data": {
    "worker": {
      "id": "worker-789",
      "computeResourceId": "compute-123",
      "experimentId": "exp-123",
      "status": "RUNNING",
      "currentTaskId": null
    },
    "summary": {
      "id": "worker-789",
      "computeResourceId": "compute-123",
      "experimentId": "exp-123",
      "status": "RUNNING"
    }
  }
}
```

#### Worker Updated
```json
{
  "type": "worker_updated",
  "id": "worker_worker-789_1642248660",
  "timestamp": "2024-01-15T10:31:00Z",
  "data": {
    "worker": {
      "id": "worker-789",
      "computeResourceId": "compute-123",
      "experimentId": "exp-123",
      "status": "RUNNING",
      "currentTaskId": "task-456"
    },
    "summary": {
      "id": "worker-789",
      "computeResourceId": "compute-123",
      "experimentId": "exp-123",
      "status": "RUNNING"
    }
  }
}
```

#### Worker Offline
```json
{
  "type": "worker_offline",
  "id": "worker_worker-789_1642249200",
  "timestamp": "2024-01-15T10:40:00Z",
  "data": {
    "worker": {
      "id": "worker-789",
      "computeResourceId": "compute-123",
      "experimentId": "exp-123",
      "status": "OFFLINE",
      "currentTaskId": null
    },
    "summary": {
      "id": "worker-789",
      "computeResourceId": "compute-123",
      "experimentId": "exp-123",
      "status": "OFFLINE"
    }
  }
}
```

## Subscription Management

### Subscription Request

To subscribe to specific resources or events, send a subscription message:

```json
{
  "type": "system_status",
  "data": {
    "action": "subscribe",
    "resourceType": "experiment",
    "resourceId": "exp-123"
  }
}
```

### Unsubscription Request

To unsubscribe from resources or events:

```json
{
  "type": "system_status",
  "data": {
    "action": "unsubscribe",
    "resourceType": "experiment",
    "resourceId": "exp-123"
  }
}
```

### Subscription Response

The server responds to subscription requests:

```json
{
  "type": "system_status",
  "id": "sub-response-123",
  "timestamp": "2024-01-15T10:30:00Z",
  "data": {
    "action": "subscribed",
    "resourceType": "experiment",
    "resourceId": "exp-123",
    "status": "success"
  }
}
```

### Supported Resource Types

| Resource Type | Description | Events |
|---------------|-------------|---------|
| `experiment` | Experiment-specific events | All experiment and task events |
| `task` | Task-specific events | Task events only |
| `project` | Project-wide events | All experiments in project |
| `user` | User-specific events | All user's experiments |
| `system` | System-wide events | System status and health |

## Event Broadcasting

### Broadcast Scope

Events are broadcast to clients based on their subscriptions:

1. **Resource-specific**: Events are sent to clients subscribed to the specific resource
2. **User-specific**: Events are sent to clients subscribed to the user
3. **Project-specific**: Events are sent to clients subscribed to the project
4. **System-wide**: Events are sent to all connected clients

### Event Routing

```
Experiment Event → Experiment Subscribers + Project Subscribers + User Subscribers
Task Event → Task Subscribers + Experiment Subscribers + Project Subscribers + User Subscribers
Worker Event → Worker Subscribers + System Subscribers
System Event → All Subscribers
```

### Event Ordering

Events are delivered in the order they occur, with timestamps to ensure proper sequencing:

```json
{
  "type": "experiment_updated",
  "id": "exp_exp-123_1642248600",
  "timestamp": "2024-01-15T10:30:00Z",
  "data": { ... }
}
```

## Error Handling

### Connection Errors

#### Authentication Error
```json
{
  "type": "error",
  "id": "error-123",
  "timestamp": "2024-01-15T10:30:00Z",
  "error": "Authentication required"
}
```

#### Invalid Token
```json
{
  "type": "error",
  "id": "error-124",
  "timestamp": "2024-01-15T10:30:00Z",
  "error": "Invalid or expired token"
}
```

#### Rate Limit Exceeded
```json
{
  "type": "error",
  "id": "error-125",
  "timestamp": "2024-01-15T10:30:00Z",
  "error": "Rate limit exceeded"
}
```

### Message Errors

#### Invalid Message Format
```json
{
  "type": "error",
  "id": "error-126",
  "timestamp": "2024-01-15T10:30:00Z",
  "error": "Invalid message format"
}
```

#### Unknown Message Type
```json
{
  "type": "error",
  "id": "error-127",
  "timestamp": "2024-01-15T10:30:00Z",
  "error": "Unknown message type"
}
```

### Client Error Handling

```javascript
const ws = new WebSocket('ws://localhost:8080/ws/experiments/exp-123?token=your_token');

ws.onerror = (error) => {
  console.error('WebSocket error:', error);
  // Handle connection errors
};

ws.onmessage = (event) => {
  const message = JSON.parse(event.data);
  
  if (message.type === 'error') {
    console.error('Server error:', message.error);
    // Handle server errors
  } else {
    // Handle normal messages
    handleMessage(message);
  }
};
```

## Connection Management

### Connection Lifecycle

1. **Connect**: Client establishes WebSocket connection
2. **Authenticate**: Server validates JWT token
3. **Subscribe**: Client subscribes to desired resources
4. **Receive**: Client receives real-time updates
5. **Disconnect**: Connection closed by client or server

### Heartbeat Mechanism

The server sends periodic ping messages to maintain connection health:

```json
{
  "type": "ping",
  "id": "ping-123",
  "timestamp": "2024-01-15T10:30:00Z"
}
```

Clients should respond with pong messages:

```json
{
  "type": "pong",
  "id": "pong-123",
  "timestamp": "2024-01-15T10:30:00Z"
}
```

### Reconnection Strategy

Clients should implement automatic reconnection:

```javascript
class WebSocketManager {
  constructor() {
    this.reconnectAttempts = 0;
    this.maxReconnectAttempts = 5;
    this.reconnectDelay = 1000; // 1 second
  }
  
  connect(url) {
    this.ws = new WebSocket(url);
    
    this.ws.onopen = () => {
      console.log('Connected');
      this.reconnectAttempts = 0;
    };
    
    this.ws.onclose = () => {
      console.log('Disconnected');
      this.handleReconnect(url);
    };
    
    this.ws.onerror = (error) => {
      console.error('WebSocket error:', error);
    };
  }
  
  handleReconnect(url) {
    if (this.reconnectAttempts < this.maxReconnectAttempts) {
      this.reconnectAttempts++;
      const delay = this.reconnectDelay * Math.pow(2, this.reconnectAttempts - 1);
      
      setTimeout(() => {
        console.log(`Reconnecting... (attempt ${this.reconnectAttempts})`);
        this.connect(url);
      }, delay);
    } else {
      console.error('Max reconnection attempts reached');
    }
  }
}
```

### Connection Limits

- **Per User**: 10 concurrent connections
- **Per IP**: 50 concurrent connections
- **Global**: 1000 concurrent connections

## Examples

### Complete Client Implementation

```javascript
class AiravataWebSocketClient {
  constructor(baseUrl, token) {
    this.baseUrl = baseUrl;
    this.token = token;
    this.connections = new Map();
    this.subscriptions = new Map();
  }
  
  connectToExperiment(experimentId, onMessage) {
    const url = `${this.baseUrl}/ws/experiments/${experimentId}?token=${this.token}`;
    const ws = new WebSocket(url);
    
    ws.onopen = () => {
      console.log(`Connected to experiment ${experimentId}`);
      this.subscribe(ws, 'experiment', experimentId);
    };
    
    ws.onmessage = (event) => {
      const message = JSON.parse(event.data);
      onMessage(message);
    };
    
    ws.onclose = () => {
      console.log(`Disconnected from experiment ${experimentId}`);
      this.connections.delete(experimentId);
    };
    
    this.connections.set(experimentId, ws);
    return ws;
  }
  
  subscribe(ws, resourceType, resourceId) {
    const message = {
      type: 'system_status',
      data: {
        action: 'subscribe',
        resourceType: resourceType,
        resourceId: resourceId
      }
    };
    
    ws.send(JSON.stringify(message));
  }
  
  disconnect(experimentId) {
    const ws = this.connections.get(experimentId);
    if (ws) {
      ws.close();
      this.connections.delete(experimentId);
    }
  }
  
  sendPing(experimentId) {
    const ws = this.connections.get(experimentId);
    if (ws && ws.readyState === WebSocket.OPEN) {
      const message = {
        type: 'ping',
        id: `ping-${Date.now()}`,
        timestamp: new Date().toISOString()
      };
      
      ws.send(JSON.stringify(message));
    }
  }
}

// Usage
const client = new AiravataWebSocketClient('ws://localhost:8080', 'your_jwt_token');

client.connectToExperiment('exp-123', (message) => {
  switch (message.type) {
    case 'experiment_updated':
      console.log('Experiment updated:', message.data);
      break;
    case 'experiment_progress':
      console.log('Progress update:', message.data);
      break;
    case 'task_updated':
      console.log('Task updated:', message.data);
      break;
    case 'pong':
      console.log('Pong received');
      break;
  }
});
```

### React Hook Example

```javascript
import { useState, useEffect, useRef } from 'react';

const useWebSocket = (url, token) => {
  const [socket, setSocket] = useState(null);
  const [lastMessage, setLastMessage] = useState(null);
  const [connectionStatus, setConnectionStatus] = useState('Connecting');
  const reconnectTimeoutRef = useRef(null);
  
  useEffect(() => {
    const ws = new WebSocket(`${url}?token=${token}`);
    
    ws.onopen = () => {
      setConnectionStatus('Connected');
      setSocket(ws);
    };
    
    ws.onmessage = (event) => {
      const message = JSON.parse(event.data);
      setLastMessage(message);
    };
    
    ws.onclose = () => {
      setConnectionStatus('Disconnected');
      setSocket(null);
      
      // Auto-reconnect after 5 seconds
      reconnectTimeoutRef.current = setTimeout(() => {
        setConnectionStatus('Reconnecting');
      }, 5000);
    };
    
    ws.onerror = (error) => {
      console.error('WebSocket error:', error);
      setConnectionStatus('Error');
    };
    
    return () => {
      if (reconnectTimeoutRef.current) {
        clearTimeout(reconnectTimeoutRef.current);
      }
      ws.close();
    };
  }, [url, token]);
  
  const sendMessage = (message) => {
    if (socket && socket.readyState === WebSocket.OPEN) {
      socket.send(JSON.stringify(message));
    }
  };
  
  return { socket, lastMessage, connectionStatus, sendMessage };
};

// Usage in component
const ExperimentDashboard = ({ experimentId }) => {
  const { lastMessage, connectionStatus, sendMessage } = useWebSocket(
    `ws://localhost:8080/ws/experiments/${experimentId}`,
    'your_jwt_token'
  );
  
  useEffect(() => {
    if (lastMessage) {
      switch (lastMessage.type) {
        case 'experiment_progress':
          // Update progress bar
          break;
        case 'task_updated':
          // Update task list
          break;
      }
    }
  }, [lastMessage]);
  
  return (
    <div>
      <div>Status: {connectionStatus}</div>
      {/* Dashboard content */}
    </div>
  );
};
```

This comprehensive WebSocket protocol documentation provides everything needed to implement real-time communication with the Airavata Scheduler system. The protocol is designed to be reliable, scalable, and easy to integrate with any modern web application.