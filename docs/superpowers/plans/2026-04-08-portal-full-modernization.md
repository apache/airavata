# Portal Full Modernization — Master Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Fully modernize the Django portal — Python 3.12+, Django 5.1, SDK facade, Vue 3, Vite, Wagtail 6, TypeScript, with dev tooling (Tiltfile) and zero legacy code.

**Architecture:** Backend-first modernization (Python/Django), then frontend (Vue 3/Vite), then CMS (Wagtail). Each phase produces a working, deployable portal. No big-bang rewrites — incremental, testable steps.

**Tech Stack:** Python 3.12+, Django 5.1, DRF 3.15, Vue 3 + Composition API, Vite, Bootstrap 5, Wagtail 6, TypeScript, gRPC via airavata-python-sdk 3.0

---

## Phase 1: Backend Modernization

### Task 1.1: Complete Thrift → SDK facade migration

The migration agents are already working on this. Complete any remaining work:
- All `request.airavata_client.thriftMethod(authz_token, ...)` → `request.airavata_client.<module>.sdk_method(...)`
- All `from airavata.model.*.ttypes import ...` → `from airavata_sdk.generated.org.apache.airavata.model.*.proto import ...`
- All `request.profile_service[...]` → `request.airavata_client.iam/sharing`
- Inline `dynamic_apps` from portal-commons into `django_airavata/utils/dynamic_apps.py`
- Inline `queue_settings_calculators` from portal-sdk into `django_airavata/apps/api/queue_settings.py`
- Remove all Thrift imports and references

**Files:** 19 Python files across `django_airavata/`

- [ ] **Step 1:** Verify migration agents completed all call sites
- [ ] **Step 2:** Fix any remaining Thrift references: `grep -rn "from airavata\.\|import airavata\.\|thrift\|ttypes\|authz_token" --include="*.py" django_airavata/ | grep -v __pycache__`
- [ ] **Step 3:** Verify no portal-sdk or portal-commons imports remain
- [ ] **Step 4:** Commit

```bash
git commit -m "refactor: complete Thrift → SDK facade migration"
```

---

### Task 1.2: Python 3.12+ and pyproject.toml

**Files:**
- Delete: `setup.cfg`, `setup.py` (if exists), `tox.ini`
- Create: `pyproject.toml`

- [ ] **Step 1:** Create `pyproject.toml` replacing setup.cfg

```toml
[build-system]
requires = ["setuptools>=69.0", "wheel"]
build-backend = "setuptools.build_meta"

[project]
name = "django-airavata-portal"
version = "2.0.0"
description = "Apache Airavata Django Portal"
readme = "README.md"
license = {text = "Apache-2.0"}
requires-python = ">=3.12"
dependencies = [
    "Django>=5.1,<5.2",
    "djangorestframework>=3.15,<4",
    "requests>=2.32,<3",
    "requests-oauthlib>=1.4,<2",
    "django-webpack-loader>=3.0,<4",
    "logging-formatter-anticrlf>=1.2",
    "wagtail>=6.3,<7",
    "wagtailfontawesome>=1.2",
    "wagtail-draftail-anchors>=0.5",
    "wagtailcodeblock>=1.28",
    "papermill>=2.6,<3",
    "nbformat>=5.10",
    "nbconvert>=7.16",
    "grpcio>=1.70",
    "airavata-python-sdk>=3.0,<4",
]

[project.optional-dependencies]
dev = [
    "pytest>=8.0",
    "pytest-django>=4.8",
    "pytest-cov>=5.0",
    "ruff>=0.8",
]
```

- [ ] **Step 2:** Delete `setup.cfg` and `tox.ini`
- [ ] **Step 3:** Update `requirements.txt` to match pyproject.toml deps (for Docker builds)

```
# Core
Django==5.1.7
djangorestframework==3.15.2
requests==2.32.3
requests-oauthlib==1.4.0
django-webpack-loader==3.1.1
logging-formatter-anticrlf==1.2

# CMS
wagtail==6.3.1
wagtailfontawesome==1.2.1
wagtail-draftail-anchors==0.5.0
wagtailcodeblock==1.28.0.0

# Notebooks
papermill==2.6.0
nbformat==5.10.4
nbconvert==7.16.6

# gRPC
grpcio==1.70.0

# Airavata
airavata-python-sdk==3.0.0

-e "."
```

- [ ] **Step 4:** Commit

```bash
git commit -m "build: migrate to pyproject.toml, Python 3.12+, upgrade all dependencies"
```

---

### Task 1.3: Django 3.2 → 5.1 migration

**Files:** All Python files with Django patterns

- [ ] **Step 1:** Update URL routing

Search and replace all `url()` with `path()` or `re_path()`:
```bash
grep -rn "from django.conf.urls import url" --include="*.py" django_airavata/
grep -rn "url(r'" --include="*.py" django_airavata/
```

Replace:
```python
# BEFORE:
from django.conf.urls import url
url(r'^api/', include('django_airavata.apps.api.urls'))

# AFTER:
from django.urls import path, re_path
path('api/', include('django_airavata.apps.api.urls'))
```

- [ ] **Step 2:** Add `DEFAULT_AUTO_FIELD` to settings.py

```python
DEFAULT_AUTO_FIELD = 'django.db.models.BigAutoField'
```

- [ ] **Step 3:** Update deprecated middleware patterns

Check for `MiddlewareMixin` usage — Django 5.1 has modern middleware.

- [ ] **Step 4:** Update deprecated template tags and filters

```bash
grep -rn "{% load staticfiles %}\|{% load admin_static %}" --include="*.html" django_airavata/
```

Replace `{% load staticfiles %}` → `{% load static %}`

- [ ] **Step 5:** Fix Wagtail compatibility for Django 5.1

Wagtail 6.3+ supports Django 5.1. Check for any breaking changes:
```bash
grep -rn "from wagtail.core\|from wagtail.admin" --include="*.py" django_airavata/
```

Replace `wagtail.core` → `wagtail` (namespace change in Wagtail 3.0+)

- [ ] **Step 6:** Run Django system checks

```bash
python manage.py check --deploy
python manage.py migrate --check
```

- [ ] **Step 7:** Commit

```bash
git commit -m "refactor: migrate Django 3.2 → 5.1, update URL routing, template tags, settings"
```

---

### Task 1.4: Wagtail 2.13 → 6.3

Must be done after Django 5.1 since Wagtail 6 requires Django 4.2+.

- [ ] **Step 1:** Update all Wagtail namespace imports

The biggest change: `wagtail.core` → `wagtail`

```bash
# Find all old-style imports
grep -rn "from wagtail\.core\|from wagtail\.contrib\|from wagtail\.admin\|from wagtail\.images\|from wagtail\.documents\|from wagtail\.snippets\|from wagtail\.search" --include="*.py" django_airavata/
```

Apply namespace migration:
- `wagtail.core.models` → `wagtail.models`
- `wagtail.core.fields` → `wagtail.fields`
- `wagtail.core.blocks` → `wagtail.blocks`
- `wagtail.admin.edit_handlers` → `wagtail.admin.panels`
- `FieldPanel`, `StreamFieldPanel` etc. → just `FieldPanel` (StreamFieldPanel removed)

- [ ] **Step 2:** Update StreamField usage

Wagtail 5+ changed StreamField:
```python
# BEFORE:
body = StreamField([...], blank=True)

# AFTER:
body = StreamField([...], blank=True, use_json_field=True)
```

- [ ] **Step 3:** Generate and apply Wagtail migrations

```bash
python manage.py makemigrations
python manage.py migrate
```

- [ ] **Step 4:** Test Wagtail admin loads

```bash
python manage.py runserver
# Visit /cms/ or wherever Wagtail admin is mounted
```

- [ ] **Step 5:** Commit

```bash
git commit -m "refactor: migrate Wagtail 2.13 → 6.3, update namespace and StreamField"
```

---

### Task 1.5: Add Tiltfile for portal development

**Files:**
- Create: `Tiltfile`
- Modify: `compose/docker-compose.yaml` (if needed)

- [ ] **Step 1:** Create Tiltfile

```python
# Tiltfile for Airavata Django Portal

# Infrastructure
docker_compose('./compose/docker-compose.yaml')

# Django backend
local_resource(
    'django-portal',
    serve_cmd='python manage.py runserver 0.0.0.0:8000',
    deps=['django_airavata/'],
    resource_deps=['mariadb'],
    readiness_probe=probe(
        http_get=http_get_action(port=8000, path='/api/health/'),
        period_secs=5,
    ),
    links=[
        link('http://localhost:8000', 'Portal'),
        link('http://localhost:8000/cms/', 'Wagtail Admin'),
    ],
)

# Frontend dev servers (one per app with HMR)
for app in ['api', 'common', 'admin', 'workspace']:
    app_dir = f'django_airavata/apps/{app}/static/django_airavata_{app}'
    if os.path.exists(app_dir + '/package.json'):
        local_resource(
            f'frontend-{app}',
            serve_cmd=f'cd {app_dir} && yarn serve',
            deps=[app_dir + '/src/'],
            resource_deps=['django-portal'],
        )

# Static file collection (for production-like testing)
local_resource(
    'collectstatic',
    cmd='python manage.py collectstatic --noinput',
    deps=['django_airavata/'],
    auto_init=False,
    trigger_mode=TRIGGER_MODE_MANUAL,
)
```

- [ ] **Step 2:** Add health check endpoint to Django

Create `django_airavata/apps/api/health.py`:
```python
from django.http import JsonResponse

def health_check(request):
    return JsonResponse({"status": "ok"})
```

Add to URL config.

- [ ] **Step 3:** Commit

```bash
git commit -m "build: add Tiltfile for portal development with HMR support"
```

---

### Task 1.6: Delete legacy packages + merge cookiecutters

**Files in portals repo (parent directory):**
- Delete: `airavata-django-portal-sdk/`
- Delete: `airavata-django-portal-commons/`
- Delete: `airavata-cookiecutter-django-app/`
- Delete: `airavata-cookiecutter-django-output-view/`
- Create: `airavata-django-portal/templates/cookiecutters/django-app/`
- Create: `airavata-django-portal/templates/cookiecutters/output-view/`

- [ ] **Step 1:** Copy cookiecutter templates into portal

```bash
cp -r ../airavata-cookiecutter-django-app/ django_airavata/templates/cookiecutters/django-app/
cp -r ../airavata-cookiecutter-django-output-view/ django_airavata/templates/cookiecutters/output-view/
```

Update template imports from portal-sdk to airavata-python-sdk.

- [ ] **Step 2:** Delete old packages (from the portals parent repo)

```bash
cd ..
rm -rf airavata-django-portal-sdk
rm -rf airavata-django-portal-commons
rm -rf airavata-cookiecutter-django-app
rm -rf airavata-cookiecutter-django-output-view
```

- [ ] **Step 3:** Commit

```bash
git commit -m "refactor: delete portal-sdk/commons/cookiecutters, merge templates into portal"
```

---

### Task 1.7: Add type hints and modern Python patterns

**Files:** All Python files in `django_airavata/`

- [ ] **Step 1:** Add type hints to all function signatures in core modules

Focus on:
- `middleware.py` — request/response types
- `utils.py` — function signatures
- `views.py` — view function/class signatures
- `serializers.py` — field types

Use Python 3.12 syntax:
```python
# Use built-in generics (no typing import needed)
def get_experiments(gateway_id: str, limit: int = 50) -> list[dict]:
    ...

# Use X | Y union syntax
def get_project(project_id: str) -> Project | None:
    ...
```

- [ ] **Step 2:** Add `ruff` configuration to pyproject.toml

```toml
[tool.ruff]
target-version = "py312"
line-length = 120

[tool.ruff.lint]
select = ["E", "F", "I", "UP", "B", "SIM"]
```

- [ ] **Step 3:** Run ruff and fix auto-fixable issues

```bash
ruff check --fix django_airavata/
ruff format django_airavata/
```

- [ ] **Step 4:** Commit

```bash
git commit -m "style: add type hints, ruff config, modern Python 3.12 patterns"
```

---

## Phase 2: Frontend Modernization

### Task 2.1: Webpack → Vite migration

**Files:** All `package.json`, `webpack.config.js`, `vue.config.js` across 8 frontend apps

- [ ] **Step 1:** Identify all frontend packages

```bash
find django_airavata -name "package.json" -not -path "*/node_modules/*" | sort
```

Each app has its own package.json with webpack config.

- [ ] **Step 2:** Create Vite config for each app

Replace `webpack.config.js` / `vue.config.js` with `vite.config.js`:

```javascript
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  build: {
    manifest: true,
    rollupOptions: {
      input: 'src/main.js',
    },
  },
  server: {
    origin: 'http://localhost:5173',
  },
})
```

- [ ] **Step 3:** Replace `django-webpack-loader` with `django-vite`

In Django settings:
```python
DJANGO_VITE = {
    "default": {
        "dev_mode": DEBUG,
        "dev_server_port": 5173,
    }
}
```

In templates:
```html
<!-- BEFORE -->
{% load render_bundle from webpack_loader %}
{% render_bundle 'app' %}

<!-- AFTER -->
{% load django_vite %}
{% vite_hmr_client %}
{% vite_asset 'src/main.js' %}
```

- [ ] **Step 4:** Update all package.json dependencies

Remove webpack-related deps, add Vite:
```json
{
  "devDependencies": {
    "vite": "^6.0",
    "@vitejs/plugin-vue": "^5.0"
  }
}
```

- [ ] **Step 5:** Test each app builds and serves with Vite
- [ ] **Step 6:** Commit

```bash
git commit -m "build: migrate from Webpack to Vite across all frontend apps"
```

---

### Task 2.2: Vue 2 → Vue 3 migration

**Files:** All `.vue` and `.js` files in frontend apps

- [ ] **Step 1:** Update Vue and router/store dependencies

```json
{
  "dependencies": {
    "vue": "^3.4",
    "vue-router": "^4.3",
    "pinia": "^2.2"
  }
}
```

Remove: `vuex`, `bootstrap-vue`

- [ ] **Step 2:** Update main.js entry points

```javascript
// BEFORE (Vue 2):
import Vue from 'vue'
new Vue({ render: h => h(App) }).$mount('#app')

// AFTER (Vue 3):
import { createApp } from 'vue'
const app = createApp(App)
app.mount('#app')
```

- [ ] **Step 3:** Migrate components to Composition API

For each `.vue` file, convert Options API to `<script setup>`:

```vue
<!-- BEFORE (Vue 2 Options API): -->
<script>
export default {
  data() { return { count: 0 } },
  methods: { increment() { this.count++ } }
}
</script>

<!-- AFTER (Vue 3 Composition API): -->
<script setup>
import { ref } from 'vue'
const count = ref(0)
const increment = () => count.value++
</script>
```

- [ ] **Step 4:** Replace Vuex with Pinia

```javascript
// BEFORE (Vuex):
const store = new Vuex.Store({ state: { user: null } })

// AFTER (Pinia):
export const useUserStore = defineStore('user', () => {
  const user = ref(null)
  return { user }
})
```

- [ ] **Step 5:** Replace bootstrap-vue with Bootstrap 5 native

```html
<!-- BEFORE (bootstrap-vue): -->
<b-modal v-model="showModal">...</b-modal>

<!-- AFTER (Bootstrap 5 + Vue 3): -->
<div class="modal" :class="{ show: showModal }">...</div>
```

Or use a Vue 3 Bootstrap library like `bootstrap-vue-next`.

- [ ] **Step 6:** Test all components render and function
- [ ] **Step 7:** Commit

```bash
git commit -m "refactor: migrate Vue 2 → 3 with Composition API, Pinia, Bootstrap 5"
```

---

### Task 2.3: Add TypeScript support

- [ ] **Step 1:** Add TypeScript to Vite config

```javascript
// vite.config.js already supports TS out of the box
```

- [ ] **Step 2:** Add `tsconfig.json` to each app

```json
{
  "compilerOptions": {
    "target": "ES2022",
    "module": "ESNext",
    "moduleResolution": "bundler",
    "strict": true,
    "jsx": "preserve",
    "types": ["vite/client"]
  },
  "include": ["src/**/*.ts", "src/**/*.vue"]
}
```

- [ ] **Step 3:** Rename new/modified `.js` files to `.ts`
- [ ] **Step 4:** Add types for API responses (from proto definitions)
- [ ] **Step 5:** Commit

```bash
git commit -m "build: add TypeScript support to all frontend apps"
```

---

## Phase 3: Final Cleanup

### Task 3.1: Update Dockerfile

- [ ] **Step 1:** Update base images

```dockerfile
# Build stage
FROM node:22-slim AS frontend-build

# Server stage
FROM python:3.12-slim AS server
```

- [ ] **Step 2:** Update build commands for Vite
- [ ] **Step 3:** Remove any Thrift/legacy references
- [ ] **Step 4:** Commit

```bash
git commit -m "build: update Dockerfile for Python 3.12, Node 22, Vite builds"
```

---

### Task 3.2: Update README and documentation

- [ ] **Step 1:** Update portal README with new:
  - Prerequisites (Python 3.12+, Node 22+)
  - Setup instructions (Tiltfile)
  - Architecture overview (SDK facade, no Thrift)
  - Development workflow
- [ ] **Step 2:** Commit

```bash
git commit -m "docs: update README for modernized portal stack"
```

---

### Task 3.3: Final verification sweep

- [ ] **Step 1:** Zero Thrift references: `grep -rn "thrift\|ttypes\|authz_token" --include="*.py" django_airavata/`
- [ ] **Step 2:** Zero portal-sdk references: `grep -rn "airavata_django_portal_sdk" --include="*.py" django_airavata/`
- [ ] **Step 3:** Zero portal-commons references: `grep -rn "airavata_django_portal_commons" --include="*.py" django_airavata/`
- [ ] **Step 4:** Django checks pass: `python manage.py check --deploy`
- [ ] **Step 5:** All frontend apps build: `for d in django_airavata/apps/*/static/*/; do (cd "$d" && yarn build); done`
- [ ] **Step 6:** Tilt works: `tilt up`

---

## Execution Order & Dependencies

```
Task 1.1 (SDK migration) ──┐
                            ├── Task 1.2 (pyproject.toml + deps) ──┐
                            │                                       ├── Task 1.3 (Django 5.1) ──┐
                            │                                       │                            ├── Task 1.4 (Wagtail 6.3)
                            │                                       │                            │
Task 1.5 (Tiltfile) ───────┤                                       │                            │
Task 1.6 (delete legacy) ──┘                                       │                            │
                                                                    │                            │
Task 1.7 (type hints) ─────────────────────────────────────────────┘                            │
                                                                                                 │
Task 2.1 (Webpack → Vite) ──── Task 2.2 (Vue 2 → 3) ──── Task 2.3 (TypeScript) ───────────────┘
                                                                                                 │
Task 3.1 (Dockerfile) ──── Task 3.2 (README) ──── Task 3.3 (final sweep) ──────────────────────┘
```

**Parallelizable:**
- Tasks 1.1, 1.5, 1.6 can run in parallel
- Task 1.7 can run after 1.2
- Task 2.1 can start after 1.2 (new build tool, doesn't need Django 5.1)
- Tasks 3.1, 3.2 can run in parallel after all others
