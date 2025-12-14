---
name: Spring Migration Plan
overview: "Migrate non-Spring dependencies to Spring equivalents: Guice → Spring DI, Dozer → MapStruct, Commons DBCP2 → HikariCP, Quartz → Spring Scheduling, Apache Shiro → Spring Security, and Gson → Jackson."
todos: []
---

# Spring Dependency Migration Plan

## Overview

Migrate dependencies in `airavata-api` to Spring equivalents to align with Spring Boot architecture and reduce external dependencies.

## Dependencies to Migrate

### 1. Google Guice → Spring Dependency Injection

**Current Usage:**

- `SecurityModule.java` - Uses Guice `AbstractModule` for AOP interceptor binding
- `@SecurityCheck` annotation - Uses Guice `@BindingAnnotation`
- Security interceptor registration via Guice

**Migration Strategy:**

- Replace `SecurityModule` with Spring `@Configuration` class
- Convert `@SecurityCheck` to Spring AOP annotation (e.g., custom `@SecurityCheck` with Spring AOP)
- Use Spring AOP `@Aspect` and `@Around` instead of Guice interceptors
- Update `SecurityInterceptor` to use Spring `MethodInterceptor` (already uses it, but needs Spring AOP setup)

**Files to Modify:**

- `airavata-api/src/main/java/org/apache/airavata/security/interceptor/SecurityModule.java` - Replace with Spring config
- `airavata-api/src/main/java/org/apache/airavata/security/interceptor/SecurityCheck.java` - Update annotation
- `airavata-api/src/main/java/org/apache/airavata/security/interceptor/SecurityInterceptor.java` - Already Spring-compatible
- Remove Guice dependency from `pom.xml`

---

### 2. Dozer → MapStruct (Recommended) or ModelMapper

**Current Usage:**

- 69+ service classes use `com.github.dozermapper.core.Mapper`
- `DozerMapperConfig.java` - Spring bean configuration
- `dozer_mapping.xml` - XML mapping configuration
- Custom converters: `StorageDateConverter`, `CsvStringConverter`
- Custom bean factory: `CustomBeanFactory`

**Migration Strategy:**

- **Option A (Recommended): MapStruct** - Compile-time mapping, type-safe, better performance
- Add `mapstruct` and `mapstruct-processor` dependencies
- Create mapper interfaces with `@Mapper` annotation
- MapStruct generates implementation at compile time
- Replace all `mapper.map()` calls with generated mapper methods
- Migrate custom converters to MapStruct `@AfterMapping` or custom methods

- **Option B: ModelMapper** - Runtime mapping, easier migration
- Replace Dozer with ModelMapper
- Similar API, less refactoring needed
- Lower performance than MapStruct

**Files to Modify:**

- All 69+ service classes using Dozer mapper
- `airavata-api/src/main/java/org/apache/airavata/config/DozerMapperConfig.java` - Replace with MapStruct/ModelMapper config
- `airavata-api/src/main/resources/dozer_mapping.xml` - Convert to MapStruct mappers or ModelMapper configuration
- `airavata-api/src/main/java/org/apache/airavata/registry/utils/DozerConverter/*.java` - Convert to MapStruct custom methods
- `airavata-api/src/main/java/org/apache/airavata/registry/utils/CustomBeanFactory.java` - May not be needed with MapStruct
- Remove Dozer dependency from `pom.xml`

---

### 3. Commons DBCP2 → HikariCP (Spring Boot Default)

**Current Usage:**

- `JpaConfig.java` - Creates `BasicDataSource` from Commons DBCP2
- `JPAUtils.java` - Uses DBCP2 driver name
- `DBUtil.java` - Uses DBCP2
- `MappingToolRunner.java` - Uses DBCP2

**Migration Strategy:**

- Replace `BasicDataSource` with HikariCP `HikariDataSource`
- HikariCP is already included in Spring Boot starter
- Update connection pool configuration
- HikariCP has better performance and is Spring Boot default

**Files to Modify:**

- `airavata-api/src/main/java/org/apache/airavata/config/JpaConfig.java` - Replace `BasicDataSource` with `HikariDataSource`
- `airavata-api/src/main/java/org/apache/airavata/common/utils/JPAUtils.java` - Update driver name
- `airavata-api/src/main/java/org/apache/airavata/common/utils/DBUtil.java` - Update to HikariCP
- `airavata-api/src/main/java/org/apache/airavata/registry/utils/migration/MappingToolRunner.java` - Update driver name
- Remove `commons-dbcp2` and `commons-pool2` dependencies from `pom.xml`

---

### 4. Quartz → Spring Scheduling

**Current Usage:**

- `ComputationalResourceMonitoringService.java` - Uses Quartz for compute resource monitoring
- `ClusterStatusMonitorJobScheduler.java` - Cluster status monitoring
- `ProcessReschedulingService.java` - Process scanning jobs
- `DataInterpreterService.java` - Data analysis jobs
- Multiple job classes: `MonitoringJob`, `ClusterStatusMonitorJob`, `ProcessScannerImpl`, `DataAnalyzerImpl`

**Migration Strategy:**

- **Option A: Spring @Scheduled** - For simple periodic tasks
- Use `@Scheduled` annotation on methods
- Enable with `@EnableScheduling`
- Convert job classes to Spring `@Component` with scheduled methods

- **Option B: Spring Quartz Integration** - For complex scheduling needs
- Use `spring-boot-starter-quartz`
- Keep Quartz but integrate with Spring
- Use Spring-managed job beans
- Better for dynamic job scheduling

**Recommendation:** Use Spring Quartz integration (Option B) since jobs are complex and need dynamic scheduling.

**Files to Modify:**

- `airavata-api/src/main/java/org/apache/airavata/monitor/compute/ComputationalResourceMonitoringService.java`
- `airavata-api/src/main/java/org/apache/airavata/monitor/cluster/ClusterStatusMonitorJobScheduler.java`
- `airavata-api/src/main/java/org/apache/airavata/metascheduler/process/scheduling/engine/rescheduler/ProcessReschedulingService.java`
- `airavata-api/src/main/java/org/apache/airavata/metascheduler/metadata/analyzer/DataInterpreterService.java`
- All job classes implementing `org.quartz.Job`
- Add `spring-boot-starter-quartz` dependency
- Keep `quartz` dependency but use Spring-managed version

---

### 5. Apache Shiro → Spring Security

**Current Usage:**

- `LDAPUserStore.java` - Uses Shiro `JndiLdapRealm` for LDAP authentication
- `JDBCUserStore.java` - Uses Shiro `JdbcRealm` for JDBC authentication
- Limited usage (only 2 classes)

**Migration Strategy:**

- Replace Shiro realms with Spring Security authentication providers
- Use Spring Security LDAP support (`spring-boot-starter-security` + `spring-ldap-core`)
- Use Spring Security JDBC authentication
- Create custom `AuthenticationProvider` implementations

**Files to Modify:**

- `airavata-api/src/main/java/org/apache/airavata/security/userstore/LDAPUserStore.java` - Replace with Spring Security LDAP
- `airavata-api/src/main/java/org/apache/airavata/security/userstore/JDBCUserStore.java` - Replace with Spring Security JDBC
- Add Spring Security dependencies
- Remove `shiro-core` dependency from `pom.xml`

**Note:** This is a lower priority migration since Shiro usage is minimal and isolated.

---

### 6. Gson → Jackson (Already Available)

**Current Usage:**

- Gson dependency exists but usage is minimal (only in `pom.xml`, no actual usage found)

**Migration Strategy:**

- Remove Gson dependency
- Use Jackson (already included via Spring Boot) for any JSON processing
- Search for any Gson usage and replace with Jackson `ObjectMapper`

**Files to Modify:**

- Remove `gson` dependency from `pom.xml`
- Search and replace any Gson usage with Jackson

---

## Migration Order (Recommended)

1. **Gson → Jackson** (Simplest, no code changes if unused)
2. **Commons DBCP2 → HikariCP** (Straightforward replacement)
3. **Google Guice → Spring DI** (Medium complexity, affects security)
4. **Quartz → Spring Quartz Integration** (Medium complexity, many files)
5. **Dozer → MapStruct** (Most complex, 69+ files, but high value)
6. **Apache Shiro → Spring Security** (Low priority, minimal usage)

## Testing Strategy

- Unit tests for each migrated component
- Integration tests for security interceptor
- Performance tests for mapper migration (MapStruct should be faster)
- Verify all scheduled jobs still work
- Test database connection pooling

## Dependencies to Remove

After migration, remove from `pom.xml`:

- `com.google.inject:guice`
- `com.github.dozermapper:dozer-core`
- `org.apache.commons:commons-dbcp2`
- `org.apache.commons:commons-pool2`
- `com.google.code.gson:gson`
- `org.apache.shiro:shiro-core` (after Shiro migration)

## Dependencies to Add

- `org.mapstruct:mapstruct` and `mapstruct-processor` (for Dozer replacement)
- `org.springframework.boot:spring-boot-starter-quartz` (for Quartz integration)
- `org.springframework.boot:spring-boot-starter-security` (for Shiro replacement)
- `org.springframework.ldap:spring-ldap-core` (for LDAP support)