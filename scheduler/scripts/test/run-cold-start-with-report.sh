#!/bin/bash

# Cold Start Test with CSV Report Generation
# Destroys all containers/volumes, performs cold start, runs all tests, and generates CSV report

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

log_header() {
    echo -e "${BLUE}==========================================${NC}"
    echo -e "${BLUE}$1${NC}"
    echo -e "${BLUE}==========================================${NC}"
}

# Configuration
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$(dirname "$SCRIPT_DIR")")"
LOGS_DIR="$PROJECT_ROOT/logs"
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")

# Test result files
UNIT_TEST_JSON="$LOGS_DIR/unit-tests-$TIMESTAMP.json"
INTEGRATION_TEST_JSON="$LOGS_DIR/integration-tests-$TIMESTAMP.json"
CSV_REPORT="$LOGS_DIR/cold-start-test-results-$TIMESTAMP.csv"
COLD_START_LOG="$LOGS_DIR/cold-start-setup-$TIMESTAMP.log"

# Timeouts
COLD_START_TIMEOUT="15m"
UNIT_TEST_TIMEOUT="30m"
INTEGRATION_TEST_TIMEOUT="60m"

# Function to show usage
show_usage() {
    echo "Usage: $0 [OPTIONS]"
    echo ""
    echo "Run cold start test with CSV report generation"
    echo ""
    echo "OPTIONS:"
    echo "  --skip-cleanup        Skip Docker cleanup (useful for debugging)"
    echo "  --skip-cold-start     Skip cold start setup (assume environment is ready)"
    echo "  --unit-only          Run only unit tests"
    echo "  --integration-only   Run only integration tests"
    echo "  --no-csv             Skip CSV report generation"
    echo "  -h, --help           Show this help"
    echo ""
    echo "EXAMPLES:"
    echo "  $0                           # Full cold start test with CSV report"
    echo "  $0 --unit-only              # Run only unit tests"
    echo "  $0 --skip-cleanup           # Skip Docker cleanup"
    echo "  $0 --integration-only       # Run only integration tests"
    echo ""
}

# Parse command line arguments
SKIP_CLEANUP=false
SKIP_COLD_START=false
UNIT_ONLY=false
INTEGRATION_ONLY=false
NO_CSV=false

while [[ $# -gt 0 ]]; do
    case $1 in
        --skip-cleanup)
            SKIP_CLEANUP=true
            shift
            ;;
        --skip-cold-start)
            SKIP_COLD_START=true
            shift
            ;;
        --unit-only)
            UNIT_ONLY=true
            shift
            ;;
        --integration-only)
            INTEGRATION_ONLY=true
            shift
            ;;
        --no-csv)
            NO_CSV=true
            shift
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

# Function to check prerequisites
check_prerequisites() {
    log_header "Checking Prerequisites"
    
    local errors=0
    
    # Check Go
    if ! command -v go &> /dev/null; then
        log_error "Go is not installed or not in PATH"
        errors=$((errors + 1))
    else
        log_success "Go is available: $(go version)"
    fi
    
    # Check Docker
    if ! command -v docker &> /dev/null; then
        log_error "Docker is not installed or not in PATH"
        errors=$((errors + 1))
    else
        log_success "Docker is available: $(docker --version)"
    fi
    
    # Check Docker Compose
    if ! command -v docker-compose &> /dev/null && ! docker compose version &> /dev/null; then
        log_error "Docker Compose is not available"
        errors=$((errors + 1))
    else
        log_success "Docker Compose is available"
    fi
    
    # Check jq for CSV generation
    if ! command -v jq &> /dev/null; then
        log_error "jq is required for CSV generation but not installed"
        errors=$((errors + 1))
    else
        log_success "jq is available: $(jq --version)"
    fi
    
    # Check bc for calculations
    if ! command -v bc &> /dev/null; then
        log_warning "bc not found, success rate calculation will be skipped"
    else
        log_success "bc is available"
    fi
    
    if [[ $errors -gt 0 ]]; then
        log_error "Prerequisites check failed with $errors error(s)"
        exit 1
    fi
    
    log_success "All prerequisites satisfied"
}

# Function to cleanup Docker environment
cleanup_docker() {
    if [[ "$SKIP_CLEANUP" == "true" ]]; then
        log_warning "Skipping Docker cleanup as requested"
        return 0
    fi
    
    log_header "Cleaning Up Docker Environment"
    
    # Get docker compose command
    local compose_cmd=""
    if command -v docker-compose &> /dev/null; then
        compose_cmd="docker-compose"
    elif docker compose version &> /dev/null; then
        compose_cmd="docker compose"
    else
        log_error "Docker Compose not available"
        return 1
    fi
    
    log_info "Stopping and removing all containers and volumes..."
    
    # Stop and remove containers with volumes
    if $compose_cmd --profile test down -v --remove-orphans 2>/dev/null || true; then
        log_success "Test profile containers stopped"
    fi
    
    if $compose_cmd down -v --remove-orphans 2>/dev/null || true; then
        log_success "Default profile containers stopped"
    fi
    
    # Remove any remaining volumes
    log_info "Removing unused volumes..."
    if docker volume prune -f 2>/dev/null || true; then
        log_success "Unused volumes removed"
    fi
    
    # Remove any dangling images
    log_info "Removing dangling images..."
    if docker image prune -f 2>/dev/null || true; then
        log_success "Dangling images removed"
    fi
    
    log_success "Docker cleanup completed"
}

# Function to perform cold start setup
perform_cold_start() {
    if [[ "$SKIP_COLD_START" == "true" ]]; then
        log_warning "Skipping cold start setup as requested"
        return 0
    fi
    
    log_header "Performing Cold Start Setup"
    
    cd "$PROJECT_ROOT"
    
    # Check if cold start script exists
    if [[ ! -f "scripts/setup-cold-start.sh" ]]; then
        log_error "Cold start script not found: scripts/setup-cold-start.sh"
        return 1
    fi
    
    # Make script executable
    chmod +x scripts/setup-cold-start.sh
    
    log_info "Running cold start setup with timeout: $COLD_START_TIMEOUT"
    
    # Run cold start setup (with timeout if available)
    if command -v timeout &> /dev/null; then
        log_info "Running with timeout: $COLD_START_TIMEOUT"
        if timeout "$COLD_START_TIMEOUT" ./scripts/setup-cold-start.sh 2>&1 | tee "$COLD_START_LOG"; then
            log_success "Cold start setup completed successfully"
            return 0
        else
            log_error "Cold start setup failed or timed out"
            log_error "Check log file: $COLD_START_LOG"
            return 1
        fi
    else
        log_warning "timeout command not available, running without timeout"
        if ./scripts/setup-cold-start.sh 2>&1 | tee "$COLD_START_LOG"; then
            log_success "Cold start setup completed successfully"
            return 0
        else
            log_error "Cold start setup failed"
            log_error "Check log file: $COLD_START_LOG"
            return 1
        fi
    fi
}

# Function to run unit tests
run_unit_tests() {
    if [[ "$INTEGRATION_ONLY" == "true" ]]; then
        log_warning "Skipping unit tests (integration-only mode)"
        return 0
    fi
    
    log_header "Running Unit Tests"
    
    cd "$PROJECT_ROOT"
    
    log_info "Running unit tests with timeout: $UNIT_TEST_TIMEOUT"
    log_info "Output will be saved to: $UNIT_TEST_JSON"
    
    # Run unit tests with JSON output
    if go test -v -json -timeout "$UNIT_TEST_TIMEOUT" ./tests/unit/... > "$UNIT_TEST_JSON" 2>&1; then
        log_success "Unit tests completed successfully"
        return 0
    else
        log_error "Unit tests failed"
        log_error "Check log file: $UNIT_TEST_JSON"
        return 1
    fi
}

# Function to run integration tests
run_integration_tests() {
    if [[ "$UNIT_ONLY" == "true" ]]; then
        log_warning "Skipping integration tests (unit-only mode)"
        return 0
    fi
    
    log_header "Running Integration Tests"
    
    cd "$PROJECT_ROOT"
    
    log_info "Running integration tests with timeout: $INTEGRATION_TEST_TIMEOUT"
    log_info "Output will be saved to: $INTEGRATION_TEST_JSON"
    
    # Run integration tests with JSON output
    if go test -v -json -timeout "$INTEGRATION_TEST_TIMEOUT" ./tests/integration/... > "$INTEGRATION_TEST_JSON" 2>&1; then
        log_success "Integration tests completed successfully"
        return 0
    else
        log_error "Integration tests failed"
        log_error "Check log file: $INTEGRATION_TEST_JSON"
        return 1
    fi
}

# Function to generate CSV report
generate_csv_report() {
    if [[ "$NO_CSV" == "true" ]]; then
        log_warning "Skipping CSV report generation as requested"
        return 0
    fi
    
    log_header "Generating CSV Report"
    
    # Check if CSV generator script exists
    if [[ ! -f "$SCRIPT_DIR/generate-test-csv.sh" ]]; then
        log_error "CSV generator script not found: $SCRIPT_DIR/generate-test-csv.sh"
        return 1
    fi
    
    # Make script executable
    chmod +x "$SCRIPT_DIR/generate-test-csv.sh"
    
    # Build command arguments
    local csv_args=""
    
    if [[ -f "$UNIT_TEST_JSON" ]]; then
        csv_args="$csv_args -u $UNIT_TEST_JSON"
    fi
    
    if [[ -f "$INTEGRATION_TEST_JSON" ]]; then
        csv_args="$csv_args -i $INTEGRATION_TEST_JSON"
    fi
    
    csv_args="$csv_args -o $CSV_REPORT"
    
    log_info "Generating CSV report: $CSV_REPORT"
    
    # Generate CSV report
    if "$SCRIPT_DIR/generate-test-csv.sh" $csv_args; then
        log_success "CSV report generated successfully: $CSV_REPORT"
        return 0
    else
        log_error "CSV report generation failed"
        return 1
    fi
}

# Function to print final summary
print_final_summary() {
    log_header "Final Summary"
    
    echo "Test execution completed at: $(date)"
    echo "Project root: $PROJECT_ROOT"
    echo ""
    
    # List generated files
    echo "Generated files:"
    if [[ -f "$COLD_START_LOG" ]]; then
        echo "  - Cold start log: $COLD_START_LOG"
    fi
    if [[ -f "$UNIT_TEST_JSON" ]]; then
        echo "  - Unit test results: $UNIT_TEST_JSON"
    fi
    if [[ -f "$INTEGRATION_TEST_JSON" ]]; then
        echo "  - Integration test results: $INTEGRATION_TEST_JSON"
    fi
    if [[ -f "$CSV_REPORT" ]]; then
        echo "  - CSV report: $CSV_REPORT"
    fi
    echo ""
    
    # Show CSV summary if available
    if [[ -f "$CSV_REPORT" ]]; then
        echo "CSV Report Summary:"
        echo "=================="
        if command -v tail &> /dev/null; then
            tail -n 10 "$CSV_REPORT" | grep -E "^(Total Tests|Passed|Failed|Skipped|Success Rate)" || true
        fi
        echo ""
    fi
    
    log_success "Cold start test with CSV report completed!"
}

# Cleanup function
cleanup() {
    log_info "Cleaning up temporary files..."
    # Keep log files for debugging, but clean up any temporary files if needed
}

# Set up trap for cleanup
trap cleanup EXIT

# Main execution function
main() {
    log_header "Cold Start Test with CSV Report"
    echo "Starting at: $(date)"
    echo "Project root: $PROJECT_ROOT"
    echo "Logs directory: $LOGS_DIR"
    echo ""
    
    # Ensure logs directory exists
    mkdir -p "$LOGS_DIR"
    
    local exit_code=0
    
    # Step 1: Check prerequisites
    if ! check_prerequisites; then
        exit_code=1
    fi
    
    # Step 2: Cleanup Docker environment
    if [[ $exit_code -eq 0 ]] && ! cleanup_docker; then
        exit_code=1
    fi
    
    # Step 3: Perform cold start setup
    if [[ $exit_code -eq 0 ]] && ! perform_cold_start; then
        exit_code=1
    fi
    
    # Step 4: Run unit tests
    if [[ $exit_code -eq 0 ]] && ! run_unit_tests; then
        exit_code=1
    fi
    
    # Step 5: Run integration tests
    if [[ $exit_code -eq 0 ]] && ! run_integration_tests; then
        exit_code=1
    fi
    
    # Step 6: Generate CSV report
    if [[ $exit_code -eq 0 ]] && ! generate_csv_report; then
        exit_code=1
    fi
    
    # Step 7: Print final summary
    print_final_summary
    
    if [[ $exit_code -eq 0 ]]; then
        log_success "All tests completed successfully!"
    else
        log_error "Some tests failed. Check the log files for details."
    fi
    
    exit $exit_code
}

# Run main function
main "$@"
