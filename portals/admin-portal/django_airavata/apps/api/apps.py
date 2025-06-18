from django.apps import AppConfig
from pkg_resources import iter_entry_points


class ApiConfig(AppConfig):
    name = 'django_airavata.apps.api'
    label = 'django_airavata_api'

    def ready(self):
        from . import signals  # noqa
        from . import output_views

        # Load and create instances of each output view provider
        for entry_point in iter_entry_points(group='airavata.output_view_providers'):
            output_views.OUTPUT_VIEW_PROVIDERS[entry_point.name] = entry_point.load()()
