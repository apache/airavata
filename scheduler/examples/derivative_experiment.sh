#!/bin/bash

# Airavata Scheduler - Derivative Experiment Creation Example
# This script demonstrates how to create derivative experiments based on successful results

set -e

# Configuration
API_BASE_URL="http://localhost:8080/api/v2"
USER_ID="demo-user"
PROJECT_ID="research-project"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Helper functions
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# API helper function
api_call() {
    local method=$1
    local endpoint=$2
    local data=$3
    
    if [ -n "$data" ]; then
        curl -s -X "$method" \
            -H "Content-Type: application/json" \
            -H "X-User-ID: $USER_ID" \
            -d "$data" \
            "$API_BASE_URL$endpoint"
    else
        curl -s -X "$method" \
            -H "X-User-ID: $USER_ID" \
            "$API_BASE_URL$endpoint"
    fi
}

# Check if API is available
check_api() {
    log_info "Checking API availability..."
    if ! curl -s -f "$API_BASE_URL/health" > /dev/null; then
        log_error "API is not available at $API_BASE_URL"
        log_error "Please ensure the Airavata Scheduler is running"
        exit 1
    fi
    log_success "API is available"
}

# Create a parameter sweep experiment
create_parameter_sweep_experiment() {
    local experiment_name=$1
    local param1_values=$2
    local param2_values=$3
    
    log_info "Creating parameter sweep experiment: $experiment_name"
    
    # Generate parameter sets
    local parameters="["
    local first=true
    
    for param1 in $param1_values; do
        for param2 in $param2_values; do
            if [ "$first" = true ]; then
                first=false
            else
                parameters+=","
            fi
            parameters+="{\"id\":\"param_${param1}_${param2}\",\"values\":{\"param1\":\"$param1\",\"param2\":\"$param2\"}}"
        done
    done
    parameters+="]"
    
    local experiment_data=$(cat <<EOF
{
    "name": "$experiment_name",
    "description": "Parameter sweep experiment for derivative creation demo",
    "project_id": "$PROJECT_ID",
    "command_template": "echo 'Processing parameters: {{.param1}} {{.param2}}' && sleep 2",
    "output_pattern": "output_{{.param1}}_{{.param2}}.txt",
    "parameters": $parameters,
    "compute_requirements": {
        "cpu_cores": 1,
        "memory_gb": 2,
        "walltime_minutes": 10
    }
}
EOF
)
    
    local response=$(api_call "POST" "/experiments" "$experiment_data")
    local experiment_id=$(echo "$response" | jq -r '.id')
    
    if [ "$experiment_id" = "null" ] || [ -z "$experiment_id" ]; then
        log_error "Failed to create experiment"
        echo "$response" | jq '.'
        exit 1
    fi
    
    log_success "Created experiment: $experiment_id"
    echo "$experiment_id"
}

# Submit experiment for execution
submit_experiment() {
    local experiment_id=$1
    
    log_info "Submitting experiment: $experiment_id"
    
    local response=$(api_call "POST" "/experiments/$experiment_id/submit")
    local status=$(echo "$response" | jq -r '.status')
    
    if [ "$status" != "SUBMITTED" ]; then
        log_error "Failed to submit experiment"
        echo "$response" | jq '.'
        exit 1
    fi
    
    log_success "Experiment submitted successfully"
}

# Wait for experiment completion
wait_for_experiment_completion() {
    local experiment_id=$1
    local max_wait_time=${2:-300} # 5 minutes default
    
    log_info "Waiting for experiment completion: $experiment_id"
    log_info "Maximum wait time: ${max_wait_time}s"
    
    local start_time=$(date +%s)
    local status=""
    
    while [ $(($(date +%s) - start_time)) -lt $max_wait_time ]; do
        local response=$(api_call "GET" "/experiments/$experiment_id")
        status=$(echo "$response" | jq -r '.status')
        
        case "$status" in
            "COMPLETED")
                log_success "Experiment completed successfully"
                return 0
                ;;
            "FAILED")
                log_error "Experiment failed"
                echo "$response" | jq '.'
                return 1
                ;;
            "CANCELLED")
                log_warning "Experiment was cancelled"
                return 1
                ;;
            "CREATED"|"SUBMITTED"|"RUNNING")
                log_info "Experiment status: $status"
                sleep 10
                ;;
            *)
                log_error "Unknown experiment status: $status"
                return 1
                ;;
        esac
    done
    
    log_error "Experiment did not complete within ${max_wait_time}s"
    return 1
}

# Get experiment summary
get_experiment_summary() {
    local experiment_id=$1
    
    log_info "Getting experiment summary: $experiment_id"
    
    local response=$(api_call "GET" "/experiments/$experiment_id/summary")
    
    if [ $? -eq 0 ]; then
        echo "$response" | jq '.'
    else
        log_error "Failed to get experiment summary"
        return 1
    fi
}

# Get failed tasks
get_failed_tasks() {
    local experiment_id=$1
    
    log_info "Getting failed tasks for experiment: $experiment_id"
    
    local response=$(api_call "GET" "/experiments/$experiment_id/failed-tasks")
    
    if [ $? -eq 0 ]; then
        local failed_count=$(echo "$response" | jq 'length')
        log_info "Found $failed_count failed tasks"
        echo "$response" | jq '.'
    else
        log_error "Failed to get failed tasks"
        return 1
    fi
}

# Create derivative experiment
create_derivative_experiment() {
    local source_experiment_id=$1
    local derivative_name=$2
    local task_filter=$3
    local parameter_modifications=$4
    
    log_info "Creating derivative experiment from: $source_experiment_id"
    log_info "Derivative name: $derivative_name"
    log_info "Task filter: $task_filter"
    
    local derivative_data=$(cat <<EOF
{
    "new_experiment_name": "$derivative_name",
    "task_filter": "$task_filter",
    "parameter_modifications": $parameter_modifications,
    "options": {
        "preserve_compute_resources": true,
        "preserve_data_requirements": true
    }
}
EOF
)
    
    local response=$(api_call "POST" "/experiments/$source_experiment_id/derive" "$derivative_data")
    local new_experiment_id=$(echo "$response" | jq -r '.new_experiment_id')
    local task_count=$(echo "$response" | jq -r '.task_count')
    
    if [ "$new_experiment_id" = "null" ] || [ -z "$new_experiment_id" ]; then
        log_error "Failed to create derivative experiment"
        echo "$response" | jq '.'
        exit 1
    fi
    
    log_success "Created derivative experiment: $new_experiment_id"
    log_info "Task count: $task_count"
    echo "$new_experiment_id"
}

# Search experiments
search_experiments() {
    local project_id=$1
    local status=$2
    
    log_info "Searching experiments (project: $project_id, status: $status)"
    
    local url="/experiments/search?limit=20"
    if [ -n "$project_id" ]; then
        url+="&project_id=$project_id"
    fi
    if [ -n "$status" ]; then
        url+="&status=$status"
    fi
    
    local response=$(api_call "GET" "$url")
    
    if [ $? -eq 0 ]; then
        local count=$(echo "$response" | jq '.experiments | length')
        log_info "Found $count experiments"
        echo "$response" | jq '.experiments[] | {id: .id, name: .name, status: .status, created_at: .created_at}'
    else
        log_error "Failed to search experiments"
        return 1
    fi
}

# Main demonstration function
demonstrate_derivative_experiments() {
    log_info "Starting derivative experiment demonstration"
    echo "=================================================="
    
    # Step 1: Create a parameter sweep experiment
    log_info "Step 1: Creating parameter sweep experiment"
    local source_experiment_id=$(create_parameter_sweep_experiment \
        "Parameter Sweep Demo" \
        "0.1 0.5 0.9" \
        "A B C")
    
    # Step 2: Submit the experiment
    log_info "Step 2: Submitting experiment for execution"
    submit_experiment "$source_experiment_id"
    
    # Step 3: Wait for completion (in a real scenario, this would be much longer)
    log_info "Step 3: Waiting for experiment completion"
    if ! wait_for_experiment_completion "$source_experiment_id" 60; then
        log_warning "Experiment did not complete in time, continuing with demo..."
    fi
    
    # Step 4: Get experiment summary
    log_info "Step 4: Getting experiment summary"
    get_experiment_summary "$source_experiment_id"
    
    # Step 5: Get failed tasks (if any)
    log_info "Step 5: Checking for failed tasks"
    get_failed_tasks "$source_experiment_id"
    
    # Step 6: Create derivative experiment with only successful tasks
    log_info "Step 6: Creating derivative experiment (successful tasks only)"
    local derivative1_id=$(create_derivative_experiment \
        "$source_experiment_id" \
        "Derivative - Successful Only" \
        "only_successful" \
        '{"param1": "0.7", "param2": "D"}')
    
    # Step 7: Create derivative experiment with parameter modifications
    log_info "Step 7: Creating derivative experiment (with parameter modifications)"
    local derivative2_id=$(create_derivative_experiment \
        "$source_experiment_id" \
        "Derivative - Modified Parameters" \
        "all" \
        '{"param1": "1.0", "param2": "E", "new_param": "test"}')
    
    # Step 8: Create derivative experiment from failed tasks only
    log_info "Step 8: Creating derivative experiment (failed tasks only)"
    local derivative3_id=$(create_derivative_experiment \
        "$source_experiment_id" \
        "Derivative - Retry Failed" \
        "only_failed" \
        '{"param1": "0.2", "param2": "F"}')
    
    # Step 9: Search for all experiments in the project
    log_info "Step 9: Searching for all experiments in project"
    search_experiments "$PROJECT_ID" ""
    
    # Step 10: Show final summary
    log_info "Step 10: Final summary"
    echo "=================================================="
    log_success "Demonstration completed successfully!"
    echo ""
    echo "Created experiments:"
    echo "  Source: $source_experiment_id"
    echo "  Derivative 1 (successful): $derivative1_id"
    echo "  Derivative 2 (modified): $derivative2_id"
    echo "  Derivative 3 (retry failed): $derivative3_id"
    echo ""
    log_info "You can now:"
    log_info "  - Submit the derivative experiments for execution"
    log_info "  - Monitor their progress via the dashboard"
    log_info "  - Create further derivatives based on their results"
}

# Advanced derivative creation example
advanced_derivative_example() {
    log_info "Advanced derivative experiment example"
    echo "=========================================="
    
    # Create a complex parameter sweep
    local source_id=$(create_parameter_sweep_experiment \
        "Advanced Parameter Sweep" \
        "0.1 0.3 0.5 0.7 0.9" \
        "A B C D E")
    
    submit_experiment "$source_id"
    
    # Wait a bit for some tasks to complete
    sleep 30
    
    # Create derivative with specific parameter filtering
    log_info "Creating derivative with specific parameter range"
    local derivative_data=$(cat <<EOF
{
    "new_experiment_name": "Focused Parameter Range",
    "task_filter": "only_successful",
    "parameter_modifications": {
        "param1": "0.6",
        "param2": "F",
        "optimization_level": "high"
    },
    "options": {
        "preserve_compute_resources": true,
        "preserve_data_requirements": true
    }
}
EOF
)
    
    local response=$(api_call "POST" "/experiments/$source_id/derive" "$derivative_data")
    local new_id=$(echo "$response" | jq -r '.new_experiment_id')
    
    log_success "Created advanced derivative: $new_id"
    
    # Get validation results
    local validation=$(echo "$response" | jq '.validation')
    log_info "Validation results:"
    echo "$validation" | jq '.'
}

# Cleanup function
cleanup() {
    log_info "Cleaning up demonstration experiments..."
    
    # Search for demo experiments
    local response=$(api_call "GET" "/experiments/search?project_id=$PROJECT_ID&limit=100")
    local experiments=$(echo "$response" | jq -r '.experiments[] | select(.name | contains("Demo") or contains("Derivative")) | .id')
    
    for exp_id in $experiments; do
        log_info "Deleting experiment: $exp_id"
        api_call "DELETE" "/experiments/$exp_id" > /dev/null
    done
    
    log_success "Cleanup completed"
}

# Main script logic
main() {
    case "${1:-demo}" in
        "demo")
            check_api
            demonstrate_derivative_experiments
            ;;
        "advanced")
            check_api
            advanced_derivative_example
            ;;
        "cleanup")
            check_api
            cleanup
            ;;
        "search")
            check_api
            search_experiments "$PROJECT_ID" "${2:-}"
            ;;
        "help"|"-h"|"--help")
            echo "Airavata Scheduler - Derivative Experiment Demo"
            echo ""
            echo "Usage: $0 [command]"
            echo ""
            echo "Commands:"
            echo "  demo      Run the basic derivative experiment demonstration (default)"
            echo "  advanced  Run advanced derivative experiment example"
            echo "  cleanup   Clean up demonstration experiments"
            echo "  search    Search for experiments (optionally filter by status)"
            echo "  help      Show this help message"
            echo ""
            echo "Examples:"
            echo "  $0 demo"
            echo "  $0 advanced"
            echo "  $0 cleanup"
            echo "  $0 search COMPLETED"
            ;;
        *)
            log_error "Unknown command: $1"
            echo "Use '$0 help' for usage information"
            exit 1
            ;;
    esac
}

# Check dependencies
check_dependencies() {
    local missing_deps=()
    
    if ! command -v curl &> /dev/null; then
        missing_deps+=("curl")
    fi
    
    if ! command -v jq &> /dev/null; then
        missing_deps+=("jq")
    fi
    
    if [ ${#missing_deps[@]} -ne 0 ]; then
        log_error "Missing required dependencies: ${missing_deps[*]}"
        log_error "Please install them and try again"
        exit 1
    fi
}

# Run main function
check_dependencies
main "$@"
