# Dashboard Integration Guide

This guide provides comprehensive instructions for integrating a frontend dashboard with the Airavata Scheduler system. The system is designed to support dashboard development without requiring any backend code changes.

## Table of Contents

1. [Overview](#overview)
2. [Authentication](#authentication)
3. [REST API Integration](#rest-api-integration)
4. [WebSocket Integration](#websocket-integration)
5. [Real-time Updates](#real-time-updates)
6. [State Management](#state-management)
7. [Error Handling](#error-handling)
8. [Performance Considerations](#performance-considerations)
9. [Example Implementation](#example-implementation)

## Overview

The Airavata Scheduler provides a comprehensive API for building dashboards that can:

- Submit and manage experiments
- Track real-time progress
- Create derivative experiments
- Analyze results and performance
- Monitor system health

### Key Features for Dashboards

- **Real-time Updates**: WebSocket-based progress tracking
- **Advanced Querying**: Parameter-based experiment filtering
- **Derivative Experiments**: Create new experiments from past results
- **Comprehensive Analytics**: Task aggregation and timeline views
- **Audit Trail**: Complete action logging for compliance

## Authentication

All API endpoints require authentication using JWT tokens.

### Getting an Authentication Token

```javascript
// Login endpoint
const response = await fetch('/api/v1/auth/login', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
  },
  body: JSON.stringify({
    username: 'your-username',
    password: 'your-password',
  }),
});

const { token } = await response.json();
```

### Using the Token

```javascript
// Include token in all API requests
const apiCall = async (endpoint, options = {}) => {
  const token = localStorage.getItem('authToken');
  
  return fetch(endpoint, {
    ...options,
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json',
      ...options.headers,
    },
  });
};
```

## REST API Integration

### Experiment Management

#### Create Experiment

```javascript
const createExperiment = async (experimentData) => {
  const response = await apiCall('/api/v1/experiments', {
    method: 'POST',
    body: JSON.stringify(experimentData),
  });
  
  return response.json();
};

// Example usage
const experiment = await createExperiment({
  name: 'My Parameter Sweep',
  description: 'Testing different parameter values',
  projectId: 'project-123',
  commandTemplate: 'python script.py --param1 {{param1}} --param2 {{param2}}',
  outputPattern: 'output_{{param1}}_{{param2}}.txt',
  parameters: [
    { values: { param1: 'value1', param2: 100 } },
    { values: { param1: 'value2', param2: 200 } },
  ],
  computeRequirements: {
    cpu: 2,
    memory: '4GB',
  },
});
```

#### Submit Experiment

```javascript
const submitExperiment = async (experimentId) => {
  const response = await apiCall(`/api/v1/experiments/${experimentId}/submit`, {
    method: 'POST',
  });
  
  return response.json();
};
```

#### Get Experiment Details

```javascript
const getExperiment = async (experimentId) => {
  const response = await apiCall(`/api/v1/experiments/${experimentId}`);
  return response.json();
};
```

### Advanced Querying

#### Search Experiments

```javascript
const searchExperiments = async (filters = {}) => {
  const params = new URLSearchParams();
  
  // Add filters
  if (filters.projectId) params.append('project_id', filters.projectId);
  if (filters.ownerId) params.append('owner_id', filters.ownerId);
  if (filters.status) params.append('status', filters.status);
  if (filters.createdAfter) params.append('created_after', filters.createdAfter);
  if (filters.createdBefore) params.append('created_before', filters.createdBefore);
  if (filters.parameterFilter) params.append('parameter_filter', filters.parameterFilter);
  if (filters.tags) params.append('tags', filters.tags.join(','));
  
  // Pagination
  params.append('limit', filters.limit || 20);
  params.append('offset', filters.offset || 0);
  
  // Sorting
  params.append('sort_by', filters.sortBy || 'created_at');
  params.append('order', filters.order || 'desc');
  
  const response = await apiCall(`/api/v1/experiments/search?${params}`);
  return response.json();
};

// Example usage
const results = await searchExperiments({
  projectId: 'project-123',
  status: 'COMPLETED',
  parameterFilter: '{"param1": "value1"}',
  limit: 50,
});
```

#### Get Experiment Summary

```javascript
const getExperimentSummary = async (experimentId) => {
  const response = await apiCall(`/api/v1/experiments/${experimentId}/summary`);
  return response.json();
};
```

#### Get Failed Tasks

```javascript
const getFailedTasks = async (experimentId) => {
  const response = await apiCall(`/api/v1/experiments/${experimentId}/failed-tasks`);
  return response.json();
};
```

### Derivative Experiments

#### Create Derivative Experiment

```javascript
const createDerivativeExperiment = async (sourceExperimentId, options) => {
  const response = await apiCall(`/api/v1/experiments/${sourceExperimentId}/derive`, {
    method: 'POST',
    body: JSON.stringify({
      sourceExperimentId,
      newExperimentName: options.name,
      parameterModifications: options.parameterModifications,
      taskFilter: options.taskFilter, // 'only_successful', 'only_failed', 'all'
      preserveComputeResources: options.preserveComputeResources,
    }),
  });
  
  return response.json();
};

// Example usage
const derivative = await createDerivativeExperiment('exp-123', {
  name: 'Retry Failed Tasks',
  taskFilter: 'only_failed',
  parameterModifications: {
    param1: 'retry_value',
  },
});
```

### Task Aggregation

```javascript
const getTaskAggregation = async (experimentId, groupBy) => {
  const params = new URLSearchParams({
    experiment_id: experimentId,
    group_by: groupBy, // 'status', 'worker', 'compute_resource', 'parameter_value'
  });
  
  const response = await apiCall(`/api/v1/tasks/aggregate?${params}`);
  return response.json();
};
```

### Experiment Timeline

```javascript
const getExperimentTimeline = async (experimentId) => {
  const response = await apiCall(`/api/v1/experiments/${experimentId}/timeline`);
  return response.json();
};
```

## WebSocket Integration

### Connection Setup

```javascript
class WebSocketManager {
  constructor() {
    this.connections = new Map();
    this.reconnectAttempts = 0;
    this.maxReconnectAttempts = 5;
  }
  
  connect(endpoint, onMessage, onError) {
    const token = localStorage.getItem('authToken');
    const wsUrl = `ws://localhost:8080${endpoint}?token=${token}`;
    
    const ws = new WebSocket(wsUrl);
    
    ws.onopen = () => {
      console.log('WebSocket connected');
      this.reconnectAttempts = 0;
    };
    
    ws.onmessage = (event) => {
      const message = JSON.parse(event.data);
      onMessage(message);
    };
    
    ws.onerror = (error) => {
      console.error('WebSocket error:', error);
      onError(error);
    };
    
    ws.onclose = () => {
      console.log('WebSocket disconnected');
      this.handleReconnect(endpoint, onMessage, onError);
    };
    
    this.connections.set(endpoint, ws);
    return ws;
  }
  
  handleReconnect(endpoint, onMessage, onError) {
    if (this.reconnectAttempts < this.maxReconnectAttempts) {
      this.reconnectAttempts++;
      setTimeout(() => {
        this.connect(endpoint, onMessage, onError);
      }, 1000 * this.reconnectAttempts);
    }
  }
  
  disconnect(endpoint) {
    const ws = this.connections.get(endpoint);
    if (ws) {
      ws.close();
      this.connections.delete(endpoint);
    }
  }
  
  sendMessage(endpoint, message) {
    const ws = this.connections.get(endpoint);
    if (ws && ws.readyState === WebSocket.OPEN) {
      ws.send(JSON.stringify(message));
    }
  }
}
```

### Subscribing to Updates

```javascript
const wsManager = new WebSocketManager();

// Subscribe to experiment updates
const subscribeToExperiment = (experimentId) => {
  const endpoint = `/ws/experiments/${experimentId}`;
  
  wsManager.connect(endpoint, (message) => {
    switch (message.type) {
      case 'experiment_updated':
        updateExperimentDisplay(message.data);
        break;
      case 'experiment_progress':
        updateProgressBar(message.data);
        break;
      case 'task_updated':
        updateTaskDisplay(message.data);
        break;
      case 'task_progress':
        updateTaskProgress(message.data);
        break;
    }
  }, (error) => {
    console.error('WebSocket error:', error);
  });
  
  // Send subscription message
  wsManager.sendMessage(endpoint, {
    type: 'system_status',
    data: {
      action: 'subscribe',
      resourceType: 'experiment',
      resourceId: experimentId,
    },
  });
};

// Subscribe to user-wide updates
const subscribeToUserUpdates = (userId) => {
  const endpoint = '/ws/user';
  
  wsManager.connect(endpoint, (message) => {
    // Handle user-specific updates
    updateUserDashboard(message.data);
  }, (error) => {
    console.error('WebSocket error:', error);
  });
};
```

## Real-time Updates

### Progress Tracking

```javascript
class ProgressTracker {
  constructor(experimentId) {
    this.experimentId = experimentId;
    this.progress = {
      totalTasks: 0,
      completedTasks: 0,
      failedTasks: 0,
      runningTasks: 0,
      progressPercent: 0,
    };
  }
  
  updateProgress(data) {
    this.progress = { ...this.progress, ...data };
    this.renderProgress();
  }
  
  renderProgress() {
    const progressBar = document.getElementById('progress-bar');
    const progressText = document.getElementById('progress-text');
    
    progressBar.style.width = `${this.progress.progressPercent}%`;
    progressText.textContent = `${this.progress.completedTasks}/${this.progress.totalTasks} tasks completed`;
  }
  
  getETA() {
    if (this.progress.runningTasks === 0) return null;
    
    // Simple ETA calculation
    const avgTimePerTask = 300; // 5 minutes
    const remainingTasks = this.progress.totalTasks - this.progress.completedTasks - this.progress.failedTasks;
    return remainingTasks * avgTimePerTask;
  }
}
```

### Task Status Updates

```javascript
class TaskStatusManager {
  constructor() {
    this.tasks = new Map();
  }
  
  updateTask(taskData) {
    this.tasks.set(taskData.taskId, taskData);
    this.renderTask(taskData);
  }
  
  renderTask(taskData) {
    const taskElement = document.getElementById(`task-${taskData.taskId}`);
    if (taskElement) {
      taskElement.className = `task task-${taskData.status.toLowerCase()}`;
      taskElement.querySelector('.status').textContent = taskData.status;
      taskElement.querySelector('.progress').textContent = `${taskData.progressPercent}%`;
    }
  }
  
  getTasksByStatus(status) {
    return Array.from(this.tasks.values()).filter(task => task.status === status);
  }
}
```

## State Management

### Redux Store Structure

```javascript
const initialState = {
  experiments: {
    items: [],
    loading: false,
    error: null,
    filters: {
      projectId: null,
      status: null,
      dateRange: null,
    },
    pagination: {
      limit: 20,
      offset: 0,
      total: 0,
    },
  },
  currentExperiment: {
    data: null,
    summary: null,
    tasks: [],
    timeline: [],
    loading: false,
    error: null,
  },
  websocket: {
    connections: {},
    messages: [],
  },
  ui: {
    sidebarOpen: true,
    theme: 'light',
    notifications: [],
  },
};
```

### Actions

```javascript
// Experiment actions
export const fetchExperiments = (filters) => async (dispatch) => {
  dispatch({ type: 'FETCH_EXPERIMENTS_START' });
  
  try {
    const response = await searchExperiments(filters);
    dispatch({
      type: 'FETCH_EXPERIMENTS_SUCCESS',
      payload: response,
    });
  } catch (error) {
    dispatch({
      type: 'FETCH_EXPERIMENTS_ERROR',
      payload: error.message,
    });
  }
};

export const createExperiment = (experimentData) => async (dispatch) => {
  try {
    const response = await createExperiment(experimentData);
    dispatch({
      type: 'CREATE_EXPERIMENT_SUCCESS',
      payload: response,
    });
  } catch (error) {
    dispatch({
      type: 'CREATE_EXPERIMENT_ERROR',
      payload: error.message,
    });
  }
};

// WebSocket actions
export const connectWebSocket = (endpoint) => (dispatch) => {
  const ws = new WebSocket(`ws://localhost:8080${endpoint}`);
  
  ws.onmessage = (event) => {
    const message = JSON.parse(event.data);
    dispatch({
      type: 'WEBSOCKET_MESSAGE',
      payload: message,
    });
  };
  
  dispatch({
    type: 'WEBSOCKET_CONNECTED',
    payload: { endpoint, ws },
  });
};
```

## Error Handling

### API Error Handling

```javascript
const handleApiError = (error) => {
  if (error.status === 401) {
    // Unauthorized - redirect to login
    window.location.href = '/login';
  } else if (error.status === 403) {
    // Forbidden - show permission error
    showNotification('You do not have permission to perform this action', 'error');
  } else if (error.status === 429) {
    // Rate limited - show retry message
    showNotification('Rate limit exceeded. Please try again later.', 'warning');
  } else if (error.status >= 500) {
    // Server error - show generic error
    showNotification('Server error. Please try again later.', 'error');
  } else {
    // Other errors - show specific message
    showNotification(error.message || 'An error occurred', 'error');
  }
};

const apiCall = async (endpoint, options = {}) => {
  try {
    const response = await fetch(endpoint, options);
    
    if (!response.ok) {
      const error = await response.json();
      throw { status: response.status, message: error.message };
    }
    
    return response.json();
  } catch (error) {
    handleApiError(error);
    throw error;
  }
};
```

### WebSocket Error Handling

```javascript
const handleWebSocketError = (error) => {
  console.error('WebSocket error:', error);
  
  // Show user-friendly error message
  showNotification('Connection lost. Attempting to reconnect...', 'warning');
  
  // Implement reconnection logic
  setTimeout(() => {
    reconnectWebSocket();
  }, 5000);
};
```

## Performance Considerations

### Caching

```javascript
class ApiCache {
  constructor(ttl = 300000) { // 5 minutes default
    this.cache = new Map();
    this.ttl = ttl;
  }
  
  get(key) {
    const item = this.cache.get(key);
    if (!item) return null;
    
    if (Date.now() - item.timestamp > this.ttl) {
      this.cache.delete(key);
      return null;
    }
    
    return item.data;
  }
  
  set(key, data) {
    this.cache.set(key, {
      data,
      timestamp: Date.now(),
    });
  }
  
  clear() {
    this.cache.clear();
  }
}

const cache = new ApiCache();

const cachedApiCall = async (endpoint, options = {}) => {
  const cacheKey = `${endpoint}-${JSON.stringify(options)}`;
  
  // Check cache first
  const cached = cache.get(cacheKey);
  if (cached) return cached;
  
  // Make API call
  const data = await apiCall(endpoint, options);
  
  // Cache the result
  cache.set(cacheKey, data);
  
  return data;
};
```

### Debouncing

```javascript
const debounce = (func, wait) => {
  let timeout;
  return function executedFunction(...args) {
    const later = () => {
      clearTimeout(timeout);
      func(...args);
    };
    clearTimeout(timeout);
    timeout = setTimeout(later, wait);
  };
};

// Debounce search input
const debouncedSearch = debounce((query) => {
  searchExperiments({ query });
}, 300);
```

### Virtual Scrolling

```javascript
import { FixedSizeList as List } from 'react-window';

const ExperimentList = ({ experiments }) => (
  <List
    height={600}
    itemCount={experiments.length}
    itemSize={80}
    itemData={experiments}
  >
    {({ index, style, data }) => (
      <div style={style}>
        <ExperimentItem experiment={data[index]} />
      </div>
    )}
  </List>
);
```

## Example Implementation

### Complete Dashboard Component

```javascript
import React, { useState, useEffect, useCallback } from 'react';
import { WebSocketManager } from './websocket';
import { apiCall } from './api';

const Dashboard = () => {
  const [experiments, setExperiments] = useState([]);
  const [selectedExperiment, setSelectedExperiment] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  
  const wsManager = new WebSocketManager();
  
  // Load experiments on component mount
  useEffect(() => {
    loadExperiments();
  }, []);
  
  // Subscribe to WebSocket updates
  useEffect(() => {
    if (selectedExperiment) {
      subscribeToExperiment(selectedExperiment.id);
    }
  }, [selectedExperiment]);
  
  const loadExperiments = async () => {
    setLoading(true);
    try {
      const response = await apiCall('/api/v1/experiments/search');
      setExperiments(response.experiments);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };
  
  const subscribeToExperiment = (experimentId) => {
    const endpoint = `/ws/experiments/${experimentId}`;
    
    wsManager.connect(endpoint, (message) => {
      switch (message.type) {
        case 'experiment_updated':
          updateExperiment(message.data);
          break;
        case 'experiment_progress':
          updateProgress(message.data);
          break;
      }
    }, (error) => {
      console.error('WebSocket error:', error);
    });
  };
  
  const updateExperiment = (data) => {
    setExperiments(prev => 
      prev.map(exp => 
        exp.id === data.experimentId ? { ...exp, ...data } : exp
      )
    );
  };
  
  const updateProgress = (data) => {
    setSelectedExperiment(prev => 
      prev ? { ...prev, progress: data } : null
    );
  };
  
  const createDerivativeExperiment = async (sourceId, options) => {
    try {
      const response = await apiCall(`/api/v1/experiments/${sourceId}/derive`, {
        method: 'POST',
        body: JSON.stringify(options),
      });
      
      // Refresh experiments list
      loadExperiments();
      
      return response;
    } catch (err) {
      setError(err.message);
    }
  };
  
  return (
    <div className="dashboard">
      <div className="sidebar">
        <ExperimentList 
          experiments={experiments}
          onSelect={setSelectedExperiment}
          loading={loading}
        />
      </div>
      
      <div className="main-content">
        {selectedExperiment ? (
          <ExperimentDetail 
            experiment={selectedExperiment}
            onCreateDerivative={createDerivativeExperiment}
          />
        ) : (
          <div className="welcome">
            <h2>Welcome to Airavata Scheduler</h2>
            <p>Select an experiment to view details</p>
          </div>
        )}
      </div>
      
      {error && (
        <div className="error-banner">
          {error}
          <button onClick={() => setError(null)}>×</button>
        </div>
      )}
    </div>
  );
};

export default Dashboard;
```

### Experiment Detail Component

```javascript
const ExperimentDetail = ({ experiment, onCreateDerivative }) => {
  const [summary, setSummary] = useState(null);
  const [timeline, setTimeline] = useState([]);
  const [failedTasks, setFailedTasks] = useState([]);
  
  useEffect(() => {
    loadExperimentDetails();
  }, [experiment.id]);
  
  const loadExperimentDetails = async () => {
    try {
      const [summaryRes, timelineRes, failedTasksRes] = await Promise.all([
        apiCall(`/api/v1/experiments/${experiment.id}/summary`),
        apiCall(`/api/v1/experiments/${experiment.id}/timeline`),
        apiCall(`/api/v1/experiments/${experiment.id}/failed-tasks`),
      ]);
      
      setSummary(summaryRes);
      setTimeline(timelineRes.events);
      setFailedTasks(failedTasksRes);
    } catch (err) {
      console.error('Failed to load experiment details:', err);
    }
  };
  
  const handleCreateDerivative = async () => {
    const options = {
      name: `${experiment.name} - Derivative`,
      taskFilter: 'only_failed',
    };
    
    await onCreateDerivative(experiment.id, options);
  };
  
  return (
    <div className="experiment-detail">
      <div className="experiment-header">
        <h1>{experiment.name}</h1>
        <div className="experiment-actions">
          <button onClick={handleCreateDerivative}>
            Create Derivative
          </button>
        </div>
      </div>
      
      {summary && (
        <div className="experiment-summary">
          <div className="summary-stats">
            <div className="stat">
              <label>Total Tasks</label>
              <value>{summary.totalTasks}</value>
            </div>
            <div className="stat">
              <label>Completed</label>
              <value>{summary.completedTasks}</value>
            </div>
            <div className="stat">
              <label>Failed</label>
              <value>{summary.failedTasks}</value>
            </div>
            <div className="stat">
              <label>Success Rate</label>
              <value>{(summary.successRate * 100).toFixed(1)}%</value>
            </div>
          </div>
          
          <div className="progress-bar">
            <div 
              className="progress-fill"
              style={{ width: `${summary.progressPercent}%` }}
            />
          </div>
        </div>
      )}
      
      <div className="experiment-tabs">
        <div className="tab-content">
          <TimelineView events={timeline} />
        </div>
        
        {failedTasks.length > 0 && (
          <div className="tab-content">
            <FailedTasksView tasks={failedTasks} />
          </div>
        )}
      </div>
    </div>
  );
};
```

This comprehensive guide provides everything needed to build a production-ready dashboard for the Airavata Scheduler system. The system is designed to be frontend-agnostic and supports any modern web framework or technology stack.