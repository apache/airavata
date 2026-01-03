#!/bin/bash
#
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements. See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership. The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License. You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied. See the License for the
# specific language governing permissions and limitations
# under the License.
#

# Helper script to run Airavata with GraalVM tracing agent
# This generates native image configuration files automatically

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DISTRIBUTION_DIR="$(cd "$SCRIPT_DIR/../.." && pwd)"
PROJECT_ROOT="$(cd "$DISTRIBUTION_DIR/../.." && pwd)"

# Default output directory
CONFIG_OUTPUT_DIR="${DISTRIBUTION_DIR}/target/native-image-config"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}Airavata GraalVM Tracing Agent Helper${NC}"
echo "=========================================="
echo ""

# Check if JAR exists
JAR_FILE="${DISTRIBUTION_DIR}/target/airavata-*.jar"
if ! ls ${JAR_FILE} 1> /dev/null 2>&1; then
    echo -e "${YELLOW}Warning: JAR file not found. Building...${NC}"
    cd "${DISTRIBUTION_DIR}"
    mvn clean package -DskipTests
    echo ""
fi

# Find the JAR file
JAR_FILE=$(ls ${DISTRIBUTION_DIR}/target/airavata-*.jar | head -1)

if [ ! -f "$JAR_FILE" ]; then
    echo -e "${RED}Error: Could not find Airavata JAR file${NC}"
    echo "Expected: ${DISTRIBUTION_DIR}/target/airavata-*.jar"
    exit 1
fi

# Create output directory
mkdir -p "${CONFIG_OUTPUT_DIR}"

echo "JAR file: ${JAR_FILE}"
echo "Config output: ${CONFIG_OUTPUT_DIR}"
echo ""
echo -e "${YELLOW}Running with tracing agent...${NC}"
echo "Make sure to exercise all functionality you want in the native image!"
echo ""

# Run with tracing agent
java -agentlib:native-image-agent=config-output-dir="${CONFIG_OUTPUT_DIR}" \
     -jar "${JAR_FILE}" \
     "$@"

EXIT_CODE=$?

if [ $EXIT_CODE -eq 0 ]; then
    echo ""
    echo -e "${GREEN}✓ Configuration files generated successfully${NC}"
    echo ""
    echo "Generated files in: ${CONFIG_OUTPUT_DIR}"
    echo "  - reflect-config.json"
    echo "  - resource-config.json"
    echo "  - proxy-config.json"
    echo "  - jni-config.json"
    echo ""
    echo "Next steps:"
    echo "  1. Review generated configs in ${CONFIG_OUTPUT_DIR}"
    echo "  2. Merge relevant entries to:"
    echo "     - ${DISTRIBUTION_DIR}/src/main/resources/META-INF/native-image/reflect-config.json"
    echo "     - ${DISTRIBUTION_DIR}/src/main/resources/META-INF/native-image/resource-config.json"
    echo "  3. Build native image: mvn clean package -Pnative -DskipTests"
else
    echo ""
    echo -e "${RED}✗ Application exited with code: ${EXIT_CODE}${NC}"
    echo "Configuration files may still have been generated. Check: ${CONFIG_OUTPUT_DIR}"
fi

exit $EXIT_CODE

