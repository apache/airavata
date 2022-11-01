# Airavata Django Portal Commons

Utilities for working with dynamically loaded Django apps.

## Getting Started

Install this package with pip

```
pip install airavata-django-portal-commons
```

### Dynamically loaded Django apps

1. At the end of your Django server's settings.py file add

```python
import sys
from airavata_django_portal_commons import dynamic_apps

# Add any dynamic apps installed in the virtual environment
dynamic_apps.load(INSTALLED_APPS)

# (Optional) merge WEBPACK_LOADER settings from custom Django apps
settings_module = sys.modules[__name__]
dynamic_apps.merge_settings(settings_module)
```

2. Also add
   `'airavata_django_portal_commons.dynamic_apps.context_processors.custom_app_registry'`
   to the context_processors list:

```python
TEMPLATES = [
    {
        'BACKEND': 'django.template.backends.django.DjangoTemplates',
        'DIRS': ...
        'APP_DIRS': True,
        'OPTIONS': {
            'context_processors': [
                ...
                'airavata_django_portal_commons.dynamic_apps.context_processors.custom_app_registry',
            ],
        },
    },
]
```

3. In your urls.py file add the following to the urlpatterns

```python
urlpatterns = [
    # ...
    path('', include('airavata_django_portal_commons.dynamic_apps.urls')),
]
```

## Creating a dynamically loaded Django app

See
https://apache-airavata-django-portal.readthedocs.io/en/latest/dev/custom_django_app/
for the latest information.

Note that by default the
[cookiecutter template](https://github.com/machristie/cookiecutter-airavata-django-app)
registers Django apps under the entry_point group name of `airavata.djangoapp`,
but you can change this. Just make sure that when you call `dynamic_apps.load`
that you pass as the second argument the name of the entry_point group.
