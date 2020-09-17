from django.apps import AppConfig


class ApiConfig(AppConfig):
    name = 'django_airavata.apps.api'
    label = 'django_airavata_api'

    def ready(self):
        from . import signals  # noqa
