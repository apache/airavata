#!/bin/bash

# CSV Test Report Generator
# Parses Go test JSON output and generates CSV report with test results

set -euo pipefail

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Logging functions
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

# Function to show usage
show_usage() {
    echo "Usage: $0 [OPTIONS]"
    echo ""
    echo "Generate CSV report from Go test JSON output"
    echo ""
    echo "OPTIONS:"
    echo "  -u, --unit FILE        Unit test JSON file"
    echo "  -i, --integration FILE Integration test JSON file"
    echo "  -o, --output FILE      Output CSV file (default: test-results.csv)"
    echo "  -h, --help             Show this help"
    echo ""
    echo "EXAMPLES:"
    echo "  $0 -u unit-tests.json -i integration-tests.json -o results.csv"
    echo "  $0 --unit unit.json --integration int.json"
    echo ""
}

# Default values
UNIT_JSON=""
INTEGRATION_JSON=""
OUTPUT_CSV="test-results.csv"

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        -u|--unit)
            UNIT_JSON="$2"
            shift 2
            ;;
        -i|--integration)
            INTEGRATION_JSON="$2"
            shift 2
            ;;
        -o|--output)
            OUTPUT_CSV="$2"
            shift 2
            ;;
        -h|--help)
            show_usage
            exit 0
            ;;
        *)
            log_error "Unknown option: $1"
            show_usage
            exit 1
            ;;
    esac
done

# Validate inputs
if [[ -z "$UNIT_JSON" && -z "$INTEGRATION_JSON" ]]; then
    log_error "At least one JSON file must be provided"
    show_usage
    exit 1
fi

# Check if jq is available
if ! command -v jq &> /dev/null; then
    log_error "jq is required but not installed. Please install jq to parse JSON."
    exit 1
fi

# Function to parse test JSON and extract results
parse_test_json() {
    local json_file="$1"
    local category="$2"
    
    if [[ ! -f "$json_file" ]]; then
        log_warning "JSON file not found: $json_file"
        return 0
    fi
    
    log_info "Parsing $category tests from: $json_file"
    
    # Parse JSON and extract test results
    jq -r '
        select(.Action == "run" or .Action == "pass" or .Action == "fail" or .Action == "skip") |
        select(.Test != null) |
        {
            test: .Test,
            action: .Action,
            elapsed: (.Elapsed // 0),
            output: (.Output // "")
        }
    ' "$json_file" | while IFS= read -r line; do
        if [[ -n "$line" && "$line" != "null" ]]; then
            echo "$line"
        fi
    done
}

# Function to determine test status and warnings
get_test_status() {
    local test_data="$1"
    local test_name=$(echo "$test_data" | jq -r '.test')
    local action=$(echo "$test_data" | jq -r '.action')
    local output=$(echo "$test_data" | jq -r '.output // ""')
    
    local status="UNKNOWN"
    local warnings=""
    
    case "$action" in
        "pass")
            status="PASS"
            # Check for warnings in output
            if echo "$output" | grep -qi "warning\|warn\|deprecated\|timeout"; then
                status="PASS_WITH_WARNING"
                warnings=$(echo "$output" | grep -i "warning\|warn\|deprecated\|timeout" | head -1 | tr -d '\n\r' | sed 's/"/""/g')
            fi
            ;;
        "fail")
            status="FAIL"
            # Extract error message
            if [[ -n "$output" ]]; then
                warnings=$(echo "$output" | head -1 | tr -d '\n\r' | sed 's/"/""/g')
            fi
            ;;
        "skip")
            status="SKIP"
            # Extract skip reason
            if [[ -n "$output" ]]; then
                warnings=$(echo "$output" | head -1 | tr -d '\n\r' | sed 's/"/""/g')
            fi
            ;;
        "run")
            # Test is starting, we'll get the result later
            return 0
            ;;
    esac
    
    echo "$status|$warnings"
}

# Function to generate CSV content
generate_csv() {
    local csv_file="$1"
    
    log_info "Generating CSV report: $csv_file"
    
    # Create CSV header
    echo "Category,Test Name,Status,Duration (s),Warnings/Notes" > "$csv_file"
    
    local total_tests=0
    local passed_tests=0
    local failed_tests=0
    local skipped_tests=0
    local warning_tests=0
    
    # Process unit tests
    if [[ -n "$UNIT_JSON" && -f "$UNIT_JSON" ]]; then
        log_info "Processing unit tests..."
        
        # Group test results by test name
        declare -A test_results
        
        while IFS= read -r line; do
            if [[ -n "$line" && "$line" != "null" ]]; then
                local test_name=$(echo "$line" | jq -r '.test')
                local action=$(echo "$line" | jq -r '.action')
                local elapsed=$(echo "$line" | jq -r '.elapsed')
                local output=$(echo "$line" | jq -r '.output // ""')
                
                # Store the latest result for each test
                test_results["$test_name"]="$action|$elapsed|$output"
            fi
        done < <(parse_test_json "$UNIT_JSON" "Unit")
        
        # Write unit test results to CSV
        for test_name in "${!test_results[@]}"; do
            local result_data="${test_results[$test_name]}"
            local action=$(echo "$result_data" | cut -d'|' -f1)
            local elapsed=$(echo "$result_data" | cut -d'|' -f2)
            local output=$(echo "$result_data" | cut -d'|' -f3-)
            
            local status_info=$(get_test_status "{\"test\":\"$test_name\",\"action\":\"$action\",\"output\":\"$output\"}")
            local status=$(echo "$status_info" | cut -d'|' -f1)
            local warnings=$(echo "$status_info" | cut -d'|' -f2-)
            
            # Escape CSV values
            test_name=$(echo "$test_name" | sed 's/"/""/g')
            warnings=$(echo "$warnings" | sed 's/"/""/g')
            
            echo "Unit,\"$test_name\",$status,$elapsed,\"$warnings\"" >> "$csv_file"
            
            # Update counters
            total_tests=$((total_tests + 1))
            case "$status" in
                "PASS") passed_tests=$((passed_tests + 1)) ;;
                "FAIL") failed_tests=$((failed_tests + 1)) ;;
                "SKIP") skipped_tests=$((skipped_tests + 1)) ;;
                "PASS_WITH_WARNING") 
                    passed_tests=$((passed_tests + 1))
                    warning_tests=$((warning_tests + 1))
                    ;;
            esac
        done
    fi
    
    # Process integration tests
    if [[ -n "$INTEGRATION_JSON" && -f "$INTEGRATION_JSON" ]]; then
        log_info "Processing integration tests..."
        
        # Group test results by test name
        declare -A test_results
        
        while IFS= read -r line; do
            if [[ -n "$line" && "$line" != "null" ]]; then
                local test_name=$(echo "$line" | jq -r '.test')
                local action=$(echo "$line" | jq -r '.action')
                local elapsed=$(echo "$line" | jq -r '.elapsed')
                local output=$(echo "$line" | jq -r '.output // ""')
                
                # Store the latest result for each test
                test_results["$test_name"]="$action|$elapsed|$output"
            fi
        done < <(parse_test_json "$INTEGRATION_JSON" "Integration")
        
        # Write integration test results to CSV
        for test_name in "${!test_results[@]}"; do
            local result_data="${test_results[$test_name]}"
            local action=$(echo "$result_data" | cut -d'|' -f1)
            local elapsed=$(echo "$result_data" | cut -d'|' -f2)
            local output=$(echo "$result_data" | cut -d'|' -f3-)
            
            local status_info=$(get_test_status "{\"test\":\"$test_name\",\"action\":\"$action\",\"output\":\"$output\"}")
            local status=$(echo "$status_info" | cut -d'|' -f1)
            local warnings=$(echo "$status_info" | cut -d'|' -f2-)
            
            # Escape CSV values
            test_name=$(echo "$test_name" | sed 's/"/""/g')
            warnings=$(echo "$warnings" | sed 's/"/""/g')
            
            echo "Integration,\"$test_name\",$status,$elapsed,\"$warnings\"" >> "$csv_file"
            
            # Update counters
            total_tests=$((total_tests + 1))
            case "$status" in
                "PASS") passed_tests=$((passed_tests + 1)) ;;
                "FAIL") failed_tests=$((failed_tests + 1)) ;;
                "SKIP") skipped_tests=$((skipped_tests + 1)) ;;
                "PASS_WITH_WARNING") 
                    passed_tests=$((passed_tests + 1))
                    warning_tests=$((warning_tests + 1))
                    ;;
            esac
        done
    fi
    
    # Add summary section
    echo "" >> "$csv_file"
    echo "SUMMARY" >> "$csv_file"
    echo "Total Tests,$total_tests" >> "$csv_file"
    echo "Passed,$passed_tests" >> "$csv_file"
    echo "Failed,$failed_tests" >> "$csv_file"
    echo "Skipped,$skipped_tests" >> "$csv_file"
    echo "Passed with Warnings,$warning_tests" >> "$csv_file"
    echo "Success Rate,$(echo "scale=2; $passed_tests * 100 / $total_tests" | bc -l)%" >> "$csv_file"
    
    # Print summary to console
    echo ""
    log_success "CSV report generated: $csv_file"
    echo "=========================================="
    echo "TEST SUMMARY"
    echo "=========================================="
    echo "Total Tests: $total_tests"
    echo "Passed: $passed_tests"
    echo "Failed: $failed_tests"
    echo "Skipped: $skipped_tests"
    echo "Passed with Warnings: $warning_tests"
    echo "Success Rate: $(echo "scale=2; $passed_tests * 100 / $total_tests" | bc -l)%"
    echo "=========================================="
    
    # Return exit code based on results
    if [[ $failed_tests -gt 0 ]]; then
        return 1
    else
        return 0
    fi
}

# Main execution
main() {
    log_info "Starting CSV report generation..."
    
    # Check if bc is available for calculations
    if ! command -v bc &> /dev/null; then
        log_warning "bc not found, success rate calculation will be skipped"
    fi
    
    # Generate CSV
    if generate_csv "$OUTPUT_CSV"; then
        log_success "CSV report generation completed successfully"
        exit 0
    else
        log_error "CSV report generation completed with failures"
        exit 1
    fi
}

# Run main function
main "$@"
