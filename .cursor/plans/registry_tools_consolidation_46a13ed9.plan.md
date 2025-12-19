---
name: Registry Tools Consolidation
overview: Analyze and consolidate registry-db-migrator and registry-jpa-generator modules. Merge registry-db-migrator into airavata-api as a Spring CLI component, and evaluate whether registry-jpa-generator is still needed.
todos:
  - id: analyze-usage
    content: Verify registry-jpa-generator is not actively used
    status: completed
  - id: create-migrator-command
    content: Create Spring CLI command component for database migrator in airavata-api
    status: completed
    dependencies:
      - analyze-usage
  - id: move-migration-resources
    content: Move db-scripts resources from registry-db-migrator to airavata-api
    status: completed
    dependencies:
      - create-migrator-command
  - id: update-dependencies
    content: Add required dependencies to airavata-api pom.xml
    status: completed
    dependencies:
      - create-migrator-command
  - id: remove-db-migrator-module
    content: Remove registry-db-migrator module from root pom.xml and delete directory
    status: completed
    dependencies:
      - move-migration-resources
      - update-dependencies
  - id: remove-jpa-generator-module
    content: Remove registry-jpa-generator module from root pom.xml and delete directory
    status: completed
    dependencies:
      - analyze-usage
---

# Registry Tools Consolidation Plan

## Analysis Summary

### registry-db-migrator

- **Purpose**: Standalone CLI tool for running database migrations between Airavata versions
- **Current State**: 
- Contains `DBMigrator` class that executes SQL migration scripts
- Reads SQL scripts from `db-scripts/{version}/` resources
- Supports Derby and MySQL databases
- Has a shell script wrapper (`db-migrate.sh`)
- README shows usage for version 0.7->0.8 (very old)
- **Usage**: Not actively referenced in codebase, but migration scripts exist in `modules/release-migration-scripts/` (0.16-0.17, 0.17-0.18, etc.)
- **Decision**: **Merge into airavata-api** as a Spring Boot CLI command component

### registry-jpa-generator

- **Purpose**: Code generator for creating JPA entity classes from SQL schema definitions
- **Current State**:
- Contains generators for JPA classes, SQL, and resource classes
- Has test/main methods in each generator class
- Not referenced anywhere in the codebase except within its own module
- Not used in build process
- **Usage**: JPA entities are manually written (per `examples/registry-core-README.md`)
- **Decision**: **Remove** - appears to be legacy tool no longer used

## Implementation Plan

### Phase 1: Merge registry-db-migrator into airavata-api

1. **Create Spring CLI Command Component**

- File: `airavata-api/src/main/java/org/apache/airavata/registry/tool/DatabaseMigratorCommand.java`
- Implement `CommandLineRunner` or use Spring Shell
- Convert `DBMigrator` logic into a Spring-managed component
- Use Spring Boot's `@ConfigurationProperties` for database connection settings

2. **Move Resources**

- Copy `db-scripts/` from `registry-db-migrator/src/main/resources/` to `airavata-api/src/main/resources/db-migration/`
- Update resource paths in the migrated code

3. **Update Dependencies**

- Add `commons-cli` dependency to `airavata-api/pom.xml` if not already present
- Ensure Derby and MySQL JDBC drivers are available

4. **Create CLI Wrapper Script** (Optional)

- File: `airavata-api/src/main/resources/distribution/bin/db-migrate.sh`
- Wrapper script that calls the Spring Boot application with migration command

5. **Remove Module**

- Remove `modules/registry-db-migrator` from root `pom.xml`
- Delete the module directory

### Phase 2: Remove registry-jpa-generator

1. **Verify No Active Usage**

- Confirm with user that JPA entities are manually maintained
- Check if any documentation references this tool

2. **Remove Module**

- Remove `modules/registry-jpa-generator` from root `pom.xml`
- Delete the module directory

## Files to Modify

- `airavata/pom.xml` - Remove both modules from `<modules>` list
- `airavata-api/pom.xml` - Add `commons-cli` dependency if needed
- Create new files in `airavata-api/src/main/java/org/apache/airavata/registry/tool/`
- Move resources from `registry-db-migrator/src/main/resources/db-scripts/` to `airavata-api/src/main/resources/db-migration/`

## Questions for User