from django.apps import apps

from django_airavata.app_config import AiravataAppConfig


def airavata_app_registry(request):
    """Put airavata django apps into the context."""
    airavata_apps = [app for app in apps.get_app_configs()
                     if isinstance(app, AiravataAppConfig)]
    # Sort by app_order then by verbose_name (case-insensitive)
    airavata_apps.sort(
        key=lambda app: "{:09}-{}".format(app.app_order,
                                          app.verbose_name.lower()))
    return {'airavata_apps': airavata_apps}


def resolver_match(request):
    """Put resolver_match (ResolverMatch instance) into the context."""
    return {'resolver_match': request.resolver_match}
