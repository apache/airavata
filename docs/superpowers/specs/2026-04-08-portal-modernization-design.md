# Portal Full Modernization Roadmap

## Phase 1: Backend Modernization (this branch)

### Python 3.12+ minimum
- Drop Python 3.6-3.11 support
- Use match/case statements where appropriate
- Use `type` aliases (PEP 695)
- Use f-string improvements
- Leverage `tomllib` (stdlib)
- Update setup.cfg → pyproject.toml

### Django 5.1 (latest stable)
Migration path: 3.2 → 4.2 → 5.0 → 5.1

Key changes to apply:
- `django.conf.urls.url()` → `path()` / `re_path()`
- Remove deprecated middleware patterns
- Update `DEFAULT_AUTO_FIELD` setting
- Use `LoginRequiredMiddleware` (Django 5.1)
- Modern form rendering
- Update template tags

### Dependency upgrades

| Package | Current | Target | Notes |
|---------|---------|--------|-------|
| Django | 3.2.18 | 5.1.x | Latest stable |
| djangorestframework | 3.12.4 | 3.15.x | Compatible with Django 5.1 |
| requests | 2.25.1 | 2.32.x | Drop-in |
| requests-oauthlib | 0.7.0 | 1.4.x | Minor API changes |
| grpcio | 1.53.2 | 1.70.x | Drop-in |
| grpcio-tools | 1.51.1 | 1.70.x | Drop-in |
| google-api-python-client | 1.12.8 | 2.x | Major version, check API |
| papermill | 1.0.1 | 2.6.x | API mostly stable |
| django-webpack-loader | 0.6.0 | 3.x | Major rewrite |
| airavata-python-sdk | 2.2.7 | 3.0.0 | Our gRPC version |

### Remove
- `thrift` — replaced by gRPC
- `thrift_connector` — replaced by gRPC
- `airavata-django-portal-sdk` — inlined/replaced by SDK
- `airavata-django-portal-commons` — inlined
- `zipstream-new` — unused
- `jupyter` metapackage — replace with explicit deps

### Add
- `nbformat>=5.10`
- `nbconvert>=7.16`

### Convert to pyproject.toml
Replace `setup.cfg` with modern `pyproject.toml` using `setuptools` backend.

### Add Tiltfile

```python
# Portal dev stack:
# - MariaDB (from compose.yml)
# - Airavata server (gRPC on 9090)
# - Django dev server (8000)
# - Webpack dev server (HMR)
```

Dependencies:
- MariaDB via compose.yml (already exists)
- Airavata server on localhost:9090 (from airavata repo's tilt)
- Django `manage.py runserver`
- Frontend `yarn serve` per app

### Delete from portals repo
- `airavata-django-portal-sdk/` directory
- `airavata-django-portal-commons/` directory
- `airavata-cookiecutter-django-app/` directory
- `airavata-cookiecutter-django-output-view/` directory
- Move cookiecutter templates to `airavata-django-portal/templates/cookiecutters/`

## Phase 2: Frontend Modernization (separate branch)

### Vue 2 → 3 + Composition API
- Migrate all 8 frontend packages
- Replace Options API with Composition API
- Use `<script setup>` syntax
- Replace Vuex with Pinia
- Replace Vue Router 3 with 4

### Webpack → Vite
- Replace webpack.config.js with vite.config.js
- Replace django-webpack-loader with django-vite
- Faster HMR, better DX

### Bootstrap 4 → 5
- Remove jQuery dependency
- Update grid/utility classes
- Replace bootstrap-vue with bootstrap-vue-next (Vue 3)

### TypeScript
- Add TypeScript for new components
- Gradual migration of existing JS

## Phase 3: CMS Modernization (separate branch)

### Wagtail 2.13 → 6.x
Step through major versions:
- 2.13 → 3.0 (namespace changes: wagtail.core → wagtail)
- 3.0 → 4.0 (StreamField changes)
- 4.0 → 5.0 (universal listings)
- 5.0 → 6.0 (admin UI overhaul)

Each step requires DB migrations and template updates.
