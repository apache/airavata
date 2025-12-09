#!/bin/bash

# Run end-to-end tests

set -e

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

echo "=== Running End-to-End Tests ==="

# E2E tests are part of integration tests
./scripts/test-integration.sh

echo "✓ End-to-end tests complete"

