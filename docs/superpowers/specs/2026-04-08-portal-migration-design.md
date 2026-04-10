# Portal Migration: Thrift → Python SDK Facade

## Problem

The Django portal (`airavata-django-portal`) depends on 3 intermediary packages (portal-sdk, portal-commons, 2 cookiecutters) and uses Thrift clients directly. After the gRPC/Armeria migration, the portal should use the Python SDK's transport-agnostic `AiravataClient` facade exclusively.

## Goal

- Replace all Thrift client usage with `AiravataClient` facade
- Replace all Thrift type imports with proto types from SDK
- Inline `dynamic_apps` from portal-commons into portal
- Inline `queue_settings_calculators` from portal-sdk into portal
- Merge cookiecutter templates into portal's `templates/` directory
- Delete portal-sdk, portal-commons, and both cookiecutter directories

## Scope: 19 files across the portal

### Core infrastructure (3 files)
- `django_airavata/middleware.py` — replace Thrift client pools with AiravataClient
- `django_airavata/utils.py` — remove Thrift connection/pool management
- `django_airavata/settings.py` — inline dynamic_apps, update dependencies

### API layer (8 files)
- `apps/api/views.py` — heaviest usage (~60+ request.airavata_client calls)
- `apps/api/serializers.py` — 40+ Thrift type imports → proto types
- `apps/api/output_views.py` — replace client + type usage
- `apps/api/view_utils.py` — replace client usage
- `apps/api/helpers.py` — replace client usage
- `apps/api/signals.py` — replace user_storage imports
- `apps/api/exceptions.py` — replace Thrift error types
- `apps/api/thrift_utils.py` — replace/delete entirely (Thrift-specific)

### Auth (3 files)
- `apps/auth/middleware.py` — replace client + profile_service usage
- `apps/auth/utils.py` — replace AuthzToken
- `apps/auth/tests/` — update mocks

### Workspace/Views (1 file)
- `apps/workspace/views.py` — replace client + type usage

### Context & Config (1 file)
- `context_processors.py` — replace client access

### Tests (2 files)
- `apps/api/tests/test_views.py` — update mocks
- `apps/auth/tests/` — update mocks

### requirements.txt
- Remove: `thrift`, `thrift_connector`, `airavata-django-portal-sdk`, `airavata-django-portal-commons`
- Update: `airavata-python-sdk` to 3.0.0 (the gRPC version)
- Keep: `grpcio`, `grpcio-tools` (used by SDK)

## Migration pattern

### Before (Thrift)
```python
# middleware.py creates Thrift client pool
request.airavata_client = thrift_pool.get_client()

# views.py uses Thrift client directly
from airavata.model.experiment.ttypes import ExperimentModel
experiment = request.airavata_client.getExperiment(authz_token, exp_id)
```

### After (SDK facade)
```python
# middleware.py creates AiravataClient
from airavata_sdk import AiravataClient
request.airavata_client = AiravataClient(host, port, token, gateway_id)

# views.py uses facade sub-clients
from airavata_sdk.generated.org.apache.airavata.model.experiment.proto import ExperimentModel
experiment = request.airavata_client.research.get_experiment(exp_id)
```

Key changes:
1. `request.airavata_client` becomes an `AiravataClient` instance (not Thrift)
2. Method calls change: `request.airavata_client.someThriftMethod(authz_token, ...)` → `request.airavata_client.<module>.<method>(...)`
3. No more `authz_token` parameter — SDK handles auth via metadata
4. Type imports change from `airavata.model.*.ttypes` to `airavata_sdk.generated.org.apache.airavata.model.*.proto`
5. `request.profile_service['group_manager']` → `request.airavata_client.sharing`

## Inlining portal-commons

The `dynamic_apps` module (~50 lines) loads custom Django apps via entry points. Inline directly into `settings.py` or a `utils/dynamic_apps.py` within the portal.

## Inlining queue_settings_calculators

The `queue_settings_calculators` module is a Django-specific decorator/registry for pluggable queue calculators. Inline into `apps/api/queue_settings.py` within the portal.

## Cookiecutter templates

Move `airavata-cookiecutter-django-app/` and `airavata-cookiecutter-django-output-view/` contents into `airavata-django-portal/templates/cookiecutters/` with updated imports.

## Decisions

| Decision | Choice | Reason |
|----------|--------|--------|
| Client initialization | Middleware creates AiravataClient per request | Matches existing pattern, auth token is per-request |
| Type imports | From SDK generated namespace | Transport-agnostic, types defined by proto |
| user_storage operations | Use client.storage facade | Server-side UserStorageService handles all ops |
| experiment_util | Use client.research facade | Server handles launch/clone orchestration |
| portal-sdk file storage backends | Not needed | Server handles MFT/filesystem selection |
| dynamic_apps | Inline in portal | ~50 lines, purely Django-specific |
| queue_settings_calculators | Inline in portal | Django decorator pattern, portal-specific |
