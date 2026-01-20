# Service Startup Testing Framework

This directory contains comprehensive integration tests for verifying Airavata service startup with different configurations.

## Overview

The test framework systematically verifies that:
- Services start correctly when enabled
- Services don't start when disabled
- Service dependencies are handled correctly
- The system handles missing optional services gracefully
- Configuration changes take effect correctly

## Test Structure

### Base Classes

- **`ServiceStartupTestBase`**: Base class for all service startup tests
  - Provides Testcontainers setup for infrastructure (MySQL, Redis for Dapr)
  - Utility methods for service status checking
  - Configuration property management

- **`ServiceConfigurationBuilder`**: Builder pattern for creating test configurations
  - Programmatically enable/disable services
  - Generate properties for Spring Boot tests
  - Predefined configurations (minimal, all enabled, etc.)

- **`ServiceStatusVerifier`**: Utility for verifying service status
  - Check if services are running
  - Verify port availability
  - Wait for services to start with timeout/retry logic

### Test Classes

- **`ServiceStartupCombinationTest`**: Comprehensive tests for all service combinations
  - Parameterized tests for systematic coverage
  - Tests all services enabled, minimal configuration, individual services, etc.

- **`ServiceDependencyTest`**: Tests for service dependencies and startup order
  - Verifies Dapr Controller starts before Participant
  - Tests workflow managers with/without Dapr
  - Verifies graceful handling of missing dependencies

- **`ServiceToggleTest`**: Tests for enabling/disabling services via properties
  - Verifies services start when enabled
  - Verifies services don't start when disabled
  - Tests property precedence

- **`ExternalServiceStartupTest`**: Tests for external services (Agent, Research, File)
  - Verifies services start when available
  - Handles missing services gracefully
  - Checks port availability

- **`DockerServiceStartupTest`**: Tests for Docker-based service startup
  - Verifies Docker startup script configuration
  - Tests with different service combinations via environment variables

## Test Configuration Files

Test property files are located in `src/test/resources/service-startup-tests/`:
- `all-services-enabled.properties`: All services enabled
- `minimal-services.properties`: Only core services
- `thrift-only.properties`: Thrift API only
- `rest-only.properties`: REST API only
- `background-services-only.properties`: Background services only

## Running Tests

### Using Maven

Run all service startup tests:
```bash
mvn test -pl airavata-api -Dtest="*ServiceStartup*Test"
```

Run specific test class:
```bash
mvn test -pl airavata-api -Dtest="ServiceStartupCombinationTest"
```

### Using the Test Runner Script

Run all tests:
```bash
./dev-tools/test-service-startup.sh --all
```

Run specific test category:
```bash
./dev-tools/test-service-startup.sh --combination
./dev-tools/test-service-startup.sh --dependency
./dev-tools/test-service-startup.sh --toggle
```

Run specific test class:
```bash
./dev-tools/test-service-startup.sh ServiceStartupCombinationTest
```

## Test Scenarios

### Core Scenarios
1. **All Services Enabled**: Verify all services start successfully
2. **Minimal Configuration**: Only core services, verify optional services don't start
3. **Thrift API Only**: Verify Thrift API starts, REST API doesn't
4. **REST API Only**: Verify REST API starts, Thrift API doesn't
5. **Both APIs**: Verify both Thrift and REST can run simultaneously

### Background Service Scenarios
6. **Dapr Only**: Controller + Participant, no workflow managers
7. **Workflow Managers Only**: Pre/Post/Parser without Dapr (should fail gracefully)
8. **Monitors Only**: Realtime/Email monitors without dependencies
9. **Each Service Individually**: Test each service in isolation

### Dependency Scenarios
10. **Missing Dependencies**: Test behavior when required services are disabled
11. **Startup Order**: Verify services start in correct order
12. **Graceful Degradation**: Verify system handles missing optional services

### External Service Scenarios
13. **Agent Service Available**: Test when Agent Service JAR is present
14. **Agent Service Missing**: Test when Agent Service is not available
15. **Research Service Available/Missing**: Similar to Agent Service
16. **File Service Available/Missing**: Similar to Agent Service

## Notes

- Tests use the `test` profile, which excludes services marked with `@Profile("!test")`
- In test profile, background services may not actually start, but configuration validity is verified
- ServiceHandler may not be available in test context (it's optional with `@Autowired(required = false)`)
- Tests verify configuration correctness rather than actual runtime behavior in test profile

## Long-term Maintainability

- All test scenarios are documented in test class javadoc
- Clear test method names describe the scenario being tested
- Assertions include descriptive failure messages
- Test utilities are reusable across test classes
- Test coverage can be reported using standard Maven tools

