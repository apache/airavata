#!/bin/bash

# Script to seed test data for Airavata Portal testing

API_BASE="http://localhost:8090/api/v1"

echo "Seeding test data..."

# 1. Create Storage Resource
echo "Creating storage resource..."
STORAGE_RESPONSE=$(curl -s -X POST "$API_BASE/storage-resources" \
  -H "Content-Type: application/json" \
  -d '{
    "hostName": "data.storage.edu",
    "storageResourceDescription": "Shared data storage for experiments",
    "dataMovementInterfaces": []
  }')
echo "Storage resource: $STORAGE_RESPONSE"

# 2. Create Compute Resource
echo "Creating compute resource..."
COMPUTE_RESPONSE=$(curl -s -X POST "$API_BASE/compute-resources" \
  -H "Content-Type: application/json" \
  -d '{
    "hostName": "cluster.hpc.edu",
    "resourceDescription": "Research HPC Cluster",
    "cpusPerNode": 32,
    "maxMemoryPerNode": 128,
    "defaultNodeCount": 1,
    "defaultCPUCount": 16,
    "defaultWalltime": 60,
    "batchQueues": [
      {
        "queueName": "normal",
        "maxRunTime": 1440,
        "maxNodes": 100,
        "maxProcessors": 3200,
        "maxMemory": 12800
      },
      {
        "queueName": "development",
        "maxRunTime": 120,
        "maxNodes": 10,
        "maxProcessors": 320,
        "maxMemory": 1280
      }
    ]
  }')
echo "Compute resource: $COMPUTE_RESPONSE"

# 3. Create Application Module
echo "Creating JupyterLab application module..."
APP_MODULE_RESPONSE=$(curl -s -X POST "$API_BASE/application-modules" \
  -H "Content-Type: application/json" \
  -d '{
    "appModuleName": "JupyterLab",
    "appModuleVersion": "3.0",
    "appModuleDescription": "Interactive computational environment for notebooks, code, and data"
  }')
APP_MODULE_ID=$(echo $APP_MODULE_RESPONSE | jq -r '.appModuleId')
echo "Application module ID: $APP_MODULE_ID"

# 4. Create Application Interface
echo "Creating JupyterLab application interface..."
APP_INTERFACE_RESPONSE=$(curl -s -X POST "$API_BASE/application-interfaces" \
  -H "Content-Type: application/json" \
  -d "{
    \"applicationName\": \"JupyterLab Session\",
    \"applicationDescription\": \"Launch an interactive JupyterLab session\",
    \"applicationModules\": [\"$APP_MODULE_ID\"],
    \"applicationInputs\": [
      {
        \"name\": \"Working_Directory\",
        \"type\": \"URI\",
        \"userFriendlyDescription\": \"Working directory for notebooks\",
        \"isRequired\": false,
        \"metaData\": \"Path to workspace\"
      },
      {
        \"name\": \"Session_Duration\",
        \"type\": \"INTEGER\",
        \"value\": \"240\",
        \"userFriendlyDescription\": \"Session duration in minutes\",
        \"isRequired\": false
      }
    ],
    \"applicationOutputs\": [
      {
        \"name\": \"Jupyter_URL\",
        \"type\": \"URI\",
        \"metaData\": \"URL to access JupyterLab session\"
      }
    ]
  }")
APP_INTERFACE_ID=$(echo $APP_INTERFACE_RESPONSE | jq -r '.applicationInterfaceId')
echo "Application interface ID: $APP_INTERFACE_ID"

echo ""
echo "Test data seeded successfully!"
echo "- Storage Resource: data.storage.edu"
echo "- Compute Resource: cluster.hpc.edu (with 2 queues)"
echo "- Application: JupyterLab ($APP_INTERFACE_ID)"
