# Research Service Application

This Spring Boot application supports different profiles for running in production vs development mode. In production mode, a security filter enforces authentication. In development mode, the security filter is bypassed for easier local testing.

## Running in Development Mode

### Using Maven

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Using IntelliJ IDEA

1. Go to Run > Edit Configurations.
2. Select your Spring Boot run configuration
3. In the Program arguments field, add:

```bash
--spring.profiles.active=dev
```
