#!/bin/bash

#
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
#

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"
API_MODULE="${PROJECT_ROOT}/modules/airavata-api"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Default values
TEST_CLASS=""
VERBOSE=false
CLEANUP=true
REPORT_DIR="${PROJECT_ROOT}/target/test-reports"

# Function to print usage
usage() {
    cat << EOF
Usage: $0 [OPTIONS] [TEST_CLASS]

Run service startup tests systematically.

OPTIONS:
    -h, --help              Show this help message
    -v, --verbose           Enable verbose output
    -c, --no-cleanup        Don't clean up test artifacts
    -r, --report-dir DIR     Directory for test reports (default: target/test-reports)
    -a, --all               Run all service startup tests
    -b, --base              Run base test class only
    -k, --combination       Run combination tests only
    -d, --dependency        Run dependency tests only
    -t, --toggle            Run toggle tests only
    -e, --external          Run external service tests only
    -o, --docker            Run Docker tests only

TEST_CLASS:
    Specific test class to run (e.g., ServiceStartupCombinationTest)

Examples:
    $0 --all                    # Run all service startup tests
    $0 --combination             # Run only combination tests
    $0 ServiceStartupCombinationTest  # Run specific test class
EOF
}

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        -h|--help)
            usage
            exit 0
            ;;
        -v|--verbose)
            VERBOSE=true
            shift
            ;;
        -c|--no-cleanup)
            CLEANUP=false
            shift
            ;;
        -r|--report-dir)
            REPORT_DIR="$2"
            shift 2
            ;;
        -a|--all)
            TEST_CLASS="*ServiceStartup*Test"
            shift
            ;;
        -b|--base)
            TEST_CLASS="ServiceStartupTestBase"
            shift
            ;;
        -k|--combination)
            TEST_CLASS="ServiceStartupCombinationTest"
            shift
            ;;
        -d|--dependency)
            TEST_CLASS="ServiceDependencyTest"
            shift
            ;;
        -t|--toggle)
            TEST_CLASS="ServiceToggleTest"
            shift
            ;;
        -e|--external)
            TEST_CLASS="ExternalServiceStartupTest"
            shift
            ;;
        -o|--docker)
            TEST_CLASS="DockerServiceStartupTest"
            shift
            ;;
        *)
            TEST_CLASS="$1"
            shift
            ;;
    esac
done

# Default to all tests if no specific test class is provided
if [ -z "$TEST_CLASS" ]; then
    TEST_CLASS="*ServiceStartup*Test"
fi

echo "=========================================="
echo "Service Startup Test Runner"
echo "=========================================="
echo "Project Root: ${PROJECT_ROOT}"
echo "API Module: ${API_MODULE}"
echo "Test Class: ${TEST_CLASS}"
echo "Report Directory: ${REPORT_DIR}"
echo "Verbose: ${VERBOSE}"
echo "Cleanup: ${CLEANUP}"
echo "=========================================="
echo ""

# Check if Maven is available
if ! command -v mvn &> /dev/null; then
    echo -e "${RED}Error: Maven (mvn) is not available${NC}"
    exit 1
fi

# Check if we're in the right directory
if [ ! -f "${PROJECT_ROOT}/pom.xml" ]; then
    echo -e "${RED}Error: pom.xml not found in ${PROJECT_ROOT}${NC}"
    exit 1
fi

# Create report directory
mkdir -p "${REPORT_DIR}"

# Function to run tests
run_tests() {
    local test_class="$1"
    local mvn_args="-pl modules/airavata-api"
    
    if [ "$VERBOSE" = true ]; then
        mvn_args="${mvn_args} -X"
    fi
    
    echo -e "${YELLOW}Running tests: ${test_class}${NC}"
    echo ""
    
    # Run Maven test with specific test class
    if mvn test ${mvn_args} -Dtest="${test_class}" -DfailIfNoTests=false; then
        echo -e "${GREEN}✓ Tests passed: ${test_class}${NC}"
        return 0
    else
        echo -e "${RED}✗ Tests failed: ${test_class}${NC}"
        return 1
    fi
}

# Function to cleanup
cleanup() {
    if [ "$CLEANUP" = true ]; then
        echo ""
        echo -e "${YELLOW}Cleaning up test artifacts...${NC}"
        # Add cleanup commands here if needed
    fi
}

# Trap to ensure cleanup on exit
trap cleanup EXIT

# Change to project root
cd "${PROJECT_ROOT}"

# Run the tests
if run_tests "${TEST_CLASS}"; then
    echo ""
    echo -e "${GREEN}=========================================="
    echo "All tests completed successfully!"
    echo "==========================================${NC}"
    exit 0
else
    echo ""
    echo -e "${RED}=========================================="
    echo "Some tests failed!"
    echo "==========================================${NC}"
    exit 1
fi

