---
name: Migrate Manual Instantiations to Spring DI
overview: Refactor manual instantiation of services (specifically ComputationalResourceMonitoringService) and repositories to use Spring Dependency Injection, and identify legacy JPA usage.
todos:
  - id: refactor-jobs
    content: Refactor ComputeResourceMonitor and MonitoringJob to support DI
    status: completed
  - id: refactor-monitor-service
    content: Refactor ComputationalResourceMonitoringService to be a Spring Bean
    status: completed
  - id: update-orch-server
    content: Update OrchestratorServiceServer to inject MonitoringService
    status: completed
    dependencies:
      - refactor-monitor-service
---

# Migrate Manual Instantiations to Spring DI

This plan addresses the legacy manual instantiation of services and repositories in the Airavata codebase, specifically focusing on `OrchestratorServiceServer` and the `ComputationalResourceMonitoringService`. It also identifies legacy `JPAUtils` usage in abstract repositories.

## Identified Issues

1.  **OrchestratorServiceServer**: Manually instantiates `ComputationalResourceMonitoringService` using `new`.
2.  **ComputationalResourceMonitoringService**: Manually creates `SchedulerFactoryBean` and fails to support Spring injection for Quartz jobs (`MonitoringJob`), leading to potential runtime errors or hacky workarounds.
3.  **AbstractRepository (Sharing/Profile)**: Uses static `JPAUtils.getEntityManagerFactory()`, a legacy pattern. While most leaf repositories are now `JpaRepository` interfaces, the base classes remain and should be deprecated or refactored if still in use.

## Proposed Changes

### 1. Refactor `ComputeResourceMonitor` and `MonitoringJob`

Enable Spring DI for Quartz jobs.

-   **[airavata-api/src/main/java/org/apache/airavata/monitor/compute/job/ComputeResourceMonitor.java](airavata-api/src/main/java/org/apache/airavata/monitor/compute/job/ComputeResourceMonitor.java)**:
    -   Add no-arg constructor.
    -   Annotate `registryService` with `@Autowired` (or use setter injection) to allow Spring to inject it when instantiated via a Spring-aware JobFactory.
-   **[airavata-api/src/main/java/org/apache/airavata/monitor/compute/job/MonitoringJob.java](airavata-api/src/main/java/org/apache/airavata/monitor/compute/job/MonitoringJob.java)**:
    -   Add no-arg constructor.

### 2. Refactor `ComputationalResourceMonitoringService`

Convert to a proper Spring Service.

-   **[airavata-api/src/main/java/org/apache/airavata/monitor/compute/ComputationalResourceMonitoringService.java](airavata-api/src/main/java/org/apache/airavata/monitor/compute/ComputationalResourceMonitoringService.java)**:
    -   Annotate with `@Service`.
    -   Inject `RegistryService` via constructor.
    -   Inject `Scheduler` (or `SchedulerFactoryBean`) instead of manually instantiating `SchedulerFactoryBean`.
    -   Configure the Scheduler to use `SpringBeanJobFactory` (or ensure the injected one does) so `MonitoringJob` gets its dependencies.

### 3. Update `OrchestratorServiceServer`

Remove manual instantiation.

-   **[modules/thrift-api/src/main/java/org/apache/airavata/thriftapi/server/OrchestratorServiceServer.java](modules/thrift-api/src/main/java/org/apache/airavata/thriftapi/server/OrchestratorServiceServer.java)**:
    -   Inject `ComputationalResourceMonitoringService` via constructor.
    -   Remove `new ComputationalResourceMonitoringService(...)` call.
    -   Remove static `monitoringService` field and use the injected instance.

### 4. Cleanup/Deprecation (Optional but Recommended)

-   Identify and mark `org.apache.airavata.sharing.repositories.AbstractRepository` and `org.apache.airavata.profile.repositories.AbstractRepository` as deprecated if they are no longer used by active repositories (which appear to be `JpaRepository` interfaces now).

## Verification

-   Verify `OrchestratorServiceServer` starts up correctly.
-   Verify `MonitoringJob` can be triggered and has `registryService` injected.