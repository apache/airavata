# Airavata Native Binary

Build and use the GraalVM native binary for Airavata.

## Prerequisites

- **GraalVM JDK 21+** with native-image: `gu install native-image`
- **Maven 3.8+**
- **Build tools**: `gcc` (Linux) or Xcode Command Line Tools (macOS)

## Build

```bash
cd modules/distribution
mvn clean package -Pnative -DskipTests
```

Binary: `target/airavata` (Linux/macOS) or `target/airavata.exe` (Windows)

## Usage

### CLI Commands

```bash
./airavata init --clean
./airavata account init --username=admin --password=pass --gateway=my-gateway
./airavata serve --config-dir /path/to/config
```

### Configuration

The `serve` command requires `--config-dir` with:
- `airavata.properties` - Main config
- `log4j2.xml` - Logging
- `application.properties` or `application.yml` - Optional
- `META-INF/persistence.xml` - JPA config

### Service Control

In `airavata.properties`:
```properties
services.thrift.enabled=true   # Default: true
services.rest.enabled=false    # Default: false
```

Both can run in parallel if both are `true`.

## Native Image Configuration

Configuration files are in `src/main/resources/META-INF/native-image/`:
- `reflect-config.json` - Reflection metadata
- `resource-config.json` - Resource patterns

### Generating Configs

Use GraalVM tracing agent to auto-discover requirements:

```bash
# Build JAR first
mvn clean package -DskipTests

# Run with agent
java -agentlib:native-image-agent=config-output-dir=target/native-image-config \
  -jar target/airavata-*.jar serve --config-dir /path/to/config

# Exercise all functionality, then merge generated configs
```

Or use the helper script:
```bash
./modules/distribution/src/main/scripts/run-with-agent.sh serve --config-dir /path/to/config
```

### Updating Configs

1. Run agent and exercise all functionality
2. Review `target/native-image-config/` output
3. Merge relevant entries to `src/main/resources/META-INF/native-image/`
4. Rebuild native image

## Troubleshooting

**Missing reflection**: Add class to `reflect-config.json` and rebuild  
**Missing resource**: Add pattern to `resource-config.json` and rebuild  
**Build fails**: Check GraalVM version (`java -version` should show GraalVM)

## Notes

- Build time: 5-15 minutes (first build), 2-5 minutes (incremental)
- Configs are maintained manually from agent output
- Platform-specific: build separately for Linux/macOS/Windows
