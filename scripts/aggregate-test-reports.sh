#!/bin/zsh
#
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements. See the NOTICE file
# for additional information regarding copyright ownership.
#
# Aggregates JUnit XML test reports from all modules into a single directory
# and generates a summary report.
#

set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
OUTPUT_DIR="$PROJECT_ROOT/target/aggregated-test-reports"
SUMMARY_FILE="$OUTPUT_DIR/test-summary.txt"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo "=========================================="
echo "Airavata Test Report Aggregator"
echo "=========================================="
echo ""

# Create output directory
mkdir -p "$OUTPUT_DIR"

# Find all surefire report directories
REPORT_DIRS=($(find "$PROJECT_ROOT/modules" -type d -name "surefire-reports" 2>/dev/null))

if [[ ${#REPORT_DIRS[@]} -eq 0 ]]; then
    echo "${YELLOW}No test reports found. Run 'mvn test' first.${NC}"
    exit 1
fi

# Initialize counters
TOTAL_TESTS=0
TOTAL_FAILURES=0
TOTAL_ERRORS=0
TOTAL_SKIPPED=0
TOTAL_TIME=0

# Copy all XML files and collect stats
echo "Collecting reports from ${#REPORT_DIRS[@]} module(s)..."
echo ""

for dir in "${REPORT_DIRS[@]}"; do
    # Get module name from path
    MODULE_PATH="${dir#$PROJECT_ROOT/modules/}"
    MODULE_NAME="${MODULE_PATH%%/target*}"
    
    # Count XML files
    XML_COUNT=$(find "$dir" -name "TEST-*.xml" 2>/dev/null | wc -l | tr -d ' ')
    
    if [[ $XML_COUNT -gt 0 ]]; then
        echo "  ${GREEN}✓${NC} $MODULE_NAME: $XML_COUNT test file(s)"
        
        # Copy XML files with module prefix to avoid conflicts
        for xml_file in "$dir"/TEST-*.xml; do
            if [[ -f "$xml_file" ]]; then
                BASENAME=$(basename "$xml_file")
                cp "$xml_file" "$OUTPUT_DIR/${MODULE_NAME//\//_}_$BASENAME"
                
                # Parse test counts from XML
                TESTS=$(grep -o 'tests="[0-9]*"' "$xml_file" | head -1 | grep -o '[0-9]*' || echo "0")
                FAILURES=$(grep -o 'failures="[0-9]*"' "$xml_file" | head -1 | grep -o '[0-9]*' || echo "0")
                ERRORS=$(grep -o 'errors="[0-9]*"' "$xml_file" | head -1 | grep -o '[0-9]*' || echo "0")
                SKIPPED=$(grep -o 'skipped="[0-9]*"' "$xml_file" | head -1 | grep -o '[0-9]*' || echo "0")
                TIME=$(grep -o 'time="[0-9.]*"' "$xml_file" | head -1 | grep -o '[0-9.]*' || echo "0")
                
                TOTAL_TESTS=$((TOTAL_TESTS + TESTS))
                TOTAL_FAILURES=$((TOTAL_FAILURES + FAILURES))
                TOTAL_ERRORS=$((TOTAL_ERRORS + ERRORS))
                TOTAL_SKIPPED=$((TOTAL_SKIPPED + SKIPPED))
                # Use awk for floating point addition
                TOTAL_TIME=$(echo "$TOTAL_TIME $TIME" | awk '{printf "%.3f", $1 + $2}')
            fi
        done
    fi
done

# Calculate passed tests
TOTAL_PASSED=$((TOTAL_TESTS - TOTAL_FAILURES - TOTAL_ERRORS - TOTAL_SKIPPED))

# Generate summary
echo ""
echo "=========================================="
echo "Test Summary"
echo "=========================================="

# Determine overall status
if [[ $TOTAL_FAILURES -gt 0 || $TOTAL_ERRORS -gt 0 ]]; then
    STATUS="${RED}FAILED${NC}"
    EXIT_CODE=1
else
    STATUS="${GREEN}PASSED${NC}"
    EXIT_CODE=0
fi

echo ""
echo "Status: $STATUS"
echo ""
echo "  Total Tests:  $TOTAL_TESTS"
echo "  ${GREEN}Passed:${NC}       $TOTAL_PASSED"
echo "  ${RED}Failed:${NC}       $TOTAL_FAILURES"
echo "  ${RED}Errors:${NC}       $TOTAL_ERRORS"
echo "  ${YELLOW}Skipped:${NC}      $TOTAL_SKIPPED"
echo "  Time:         ${TOTAL_TIME}s"
echo ""
echo "Reports saved to: $OUTPUT_DIR"
echo ""

# Write summary file
cat > "$SUMMARY_FILE" << EOF
Airavata Test Report Summary
Generated: $(date)

Status: $(echo -e "$STATUS" | sed 's/\x1b\[[0-9;]*m//g')

Total Tests:  $TOTAL_TESTS
Passed:       $TOTAL_PASSED
Failed:       $TOTAL_FAILURES
Errors:       $TOTAL_ERRORS
Skipped:      $TOTAL_SKIPPED
Time:         ${TOTAL_TIME}s

Reports Directory: $OUTPUT_DIR
EOF

# List failed tests if any
if [[ $TOTAL_FAILURES -gt 0 || $TOTAL_ERRORS -gt 0 ]]; then
    echo "Failed/Error Tests:"
    echo "-------------------"
    for xml_file in "$OUTPUT_DIR"/*.xml; do
        if [[ -f "$xml_file" ]]; then
            # Check for failure or error elements
            if grep -q '<failure\|<error' "$xml_file" 2>/dev/null; then
                TEST_CLASS=$(grep -o 'classname="[^"]*"' "$xml_file" | head -1 | sed 's/classname="//;s/"//')
                echo "  - $TEST_CLASS"
            fi
        fi
    done
    echo ""
fi

exit $EXIT_CODE
