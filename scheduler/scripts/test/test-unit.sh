#!/bin/bash

# Run unit tests only (no Docker required)

set -e

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

echo "=== Running Unit Tests ==="
cd "${PROJECT_ROOT}"

# Run unit tests excluding integration tests
go test -v -short -race -coverprofile=coverage-unit.out \
    $(go list ./... | grep -v /tests/integration | grep -v /tests/load)

echo "✓ Unit tests complete"

