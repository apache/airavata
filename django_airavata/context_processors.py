import logging

from django.apps import apps
from django.conf import settings

from django_airavata.app_config import AiravataAppConfig

logger = logging.getLogger(__name__)


def airavata_app_registry(request):
    """Put airavata django apps into the context."""
    airavata_apps = [app for app in apps.get_app_configs()
                     if isinstance(app, AiravataAppConfig)]
    # Sort by app_order then by verbose_name (case-insensitive)
    airavata_apps.sort(
        key=lambda app: "{:09}-{}".format(app.app_order,
                                          app.verbose_name.lower()))
    return {
        'airavata_apps': airavata_apps,
        'current_airavata_app': _get_current_app(request, airavata_apps),
    }


def custom_app_registry(request):
    """Put custom Django apps into the context."""
    custom_apps = settings.CUSTOM_DJANGO_APPS.copy()
    custom_apps.sort(key=lambda app: app.verbose_name.lower())
    current_custom_app = _get_current_app(request, custom_apps)
    return {
        'custom_apps': list(map(_app_to_dict, custom_apps)),
        'current_custom_app': _app_to_dict(current_custom_app)
    }


def _app_to_dict(app):
    # For some reason adding the AppConfig instance directly to the context
    # doesn't allow its properties to be read. This code converts it into a
    # simple dict.
    if not app:
        return None
    return {
        'name': app.name,
        'label': app.label,
        'verbose_name': app.verbose_name,
        'url_home': app.url_home,
        'fa_icon_class': app.fa_icon_class,
        'app_description': app.app_description,
    }


def _get_current_app(request, apps):
    current_app = [
        app for app in apps
        if request.resolver_match and
        app.url_app_name == request.resolver_match.app_name]
    return current_app[0] if len(current_app) > 0 else None


def resolver_match(request):
    """Put resolver_match (ResolverMatch instance) into the context."""
    return {'resolver_match': request.resolver_match}
