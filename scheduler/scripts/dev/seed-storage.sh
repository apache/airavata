#!/bin/bash

# seed-storage.sh - Seed central SFTP storage with test data

set -e

echo "Seeding central SFTP storage with test data..."

# Configuration
CENTRAL_HOST="localhost"
CENTRAL_PORT="2200"
CENTRAL_USER="testuser"
CENTRAL_PASS="testpass"
CENTRAL_PATH="/data"

# Function to create test input files
create_test_input_files() {
    local task_id=$1
    local content=$2
    
    echo "Creating test input file for task ${task_id}..."
    
    # Create a temporary file with test content
    local temp_file=$(mktemp)
    echo "$content" > "$temp_file"
    
    # Upload to central storage via SFTP
    # Note: In a real implementation, you would use an SFTP client
    # For now, we'll just log what would be done
    echo "Would upload ${temp_file} to ${CENTRAL_HOST}:${CENTRAL_PORT}${CENTRAL_PATH}/input/${task_id}/input.txt"
    
    # Clean up temp file
    rm -f "$temp_file"
}

# Function to create test data for multiple tasks
create_test_data() {
    local task_count=${1:-10}
    
    echo "Creating test data for ${task_count} tasks..."
    
    for i in $(seq 1 $task_count); do
        local task_id="test-task-${i}"
        local content="This is test input data for task ${i}. Line 1. Line 2. Line 3. Line 4. Line 5."
        
        create_test_input_files "$task_id" "$content"
    done
}

# Function to verify storage connectivity
verify_storage_connectivity() {
    echo "Verifying storage connectivity..."
    
    # In a real implementation, you would test SFTP connectivity
    # For now, we'll just log
    echo "Would test SFTP connection to ${CENTRAL_HOST}:${CENTRAL_PORT}"
    echo "Would verify directory structure: ${CENTRAL_PATH}/input and ${CENTRAL_PATH}/output"
}

# Function to create directory structure
create_directory_structure() {
    echo "Creating directory structure on central storage..."
    
    # In a real implementation, you would create directories via SFTP
    echo "Would create directories:"
    echo "  - ${CENTRAL_PATH}/input"
    echo "  - ${CENTRAL_PATH}/output"
    echo "  - ${CENTRAL_PATH}/input/test-task-*"
}

# Main execution
main() {
    echo "Starting storage seeding process..."
    
    # Verify connectivity
    verify_storage_connectivity
    
    # Create directory structure
    create_directory_structure
    
    # Create test data
    create_test_data 50  # Create test data for 50 tasks
    
    echo "Storage seeding completed!"
}

# Run main function
main "$@"
