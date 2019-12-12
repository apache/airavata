from django.apps import AppConfig


class AuthConfig(AppConfig):
    name = 'django_airavata.apps.auth'
    label = 'django_airavata_auth'

    def ready(self):
        from . import signals  # noqa
