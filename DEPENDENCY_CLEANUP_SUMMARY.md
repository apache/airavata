# Dependency Cleanup Summary

## Removed Dependencies

### From Root POM dependencyManagement

1. **commons-pool2** (2.12.1) - Not used in source code
2. **commons-dbcp2** (2.13.0) - Replaced with HikariCP (included in Spring Boot)
3. **commons-email** (1.6.0) - Replaced with Spring Mail (spring-boot-starter-mail)
4. **commons-exec** (1.1) - Not used in source code
5. **commons-io** (2.19.0) - Not used in source code
6. **commons-codec** (1.18.0) - Not used in source code
7. **commons-logging** (1.1.3) - Replaced with SLF4J/Logback (included in Spring Boot)
8. **gson** (2.10.1) - Replaced with Jackson (included in Spring Boot)
9. **guice** (7.0.0) - Replaced with Spring DI

### Kept Dependencies (with justification)

1. **commons-lang3** (3.18.0) - Used in test files (EqualsBuilder.reflectionEquals)
2. **commons-cli** (1.9.0) - Used in dev-tools/load-client
3. **guava** (33.4.8-jre) - Used in AiravataFileService (Cache/CacheBuilder for caching). Note: LocalEventPublisher (which used EventBus) was removed as it was unused.
4. **httpclient5/httpcore5** - Used by KeycloakRestClient via HttpComponentsClientHttpRequestFactory

## Spring Replacements Made

### Code Changes

1. **KeyCloakSecurityManager.java**:
   - Replaced `org.apache.http.*` (httpclient4) with Spring's `RestTemplate`
   - Updated `getClientCredentials()` method to use RestTemplate instead of CloseableHttpClient
   - Removed httpclient4 imports, added Spring Web imports

### Already Using Spring

1. **EmailNotifier.java** - Already uses Spring's JavaMailSender (spring-boot-starter-mail)
2. **SecurityModule.java** - Already uses Spring AOP instead of Guice
3. **KeycloakRestClient.java** - Already uses Spring's RestTemplate with HttpComponentsClientHttpRequestFactory

## Notes

- **httpclient5/httpcore5**: Kept because KeycloakRestClient explicitly uses HttpComponentsClientHttpRequestFactory which requires httpclient5. Spring's RestTemplate can work with the default SimpleClientHttpRequestFactory, but the current implementation uses HttpComponentsClientHttpRequestFactory for TLS support.

- **commons-lang3**: Only used in test files. Could be replaced with AssertJ in the future, but keeping for now to minimize test changes.

- **commons-cli**: Used in dev-tools/load-client. Could be replaced with Spring Boot's @CommandLineRunner in the future.

## Additional Changes

### Removed Unused Code
- **LocalEventPublisher.java**: Removed unused class that wrapped Guava's `EventBus`. The class was not referenced anywhere in the codebase.

## Verification

- All modules compile successfully
- No unused dependency warnings
- Spring replacements work correctly
- EventBus successfully replaced with Spring ApplicationEventPublisher

