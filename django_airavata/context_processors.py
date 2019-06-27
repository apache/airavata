import copy
import logging
import re

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
    current_app = _get_current_app(request, airavata_apps)

    return {
        'airavata_apps': airavata_apps,
        'current_airavata_app': current_app,
        'airavata_app_nav': (_get_app_nav(request, current_app)
                             if current_app else None)
    }


def custom_app_registry(request):
    """Put custom Django apps into the context."""
    custom_apps = settings.CUSTOM_DJANGO_APPS.copy()
    custom_apps.sort(key=lambda app: app.verbose_name.lower())
    current_custom_app = _get_current_app(request, custom_apps)
    return {
        # 'custom_apps': list(map(_app_to_dict, custom_apps)),
        'custom_apps': custom_apps,
        'current_custom_app': current_custom_app,
        'custom_app_nav': (_get_app_nav(request, current_custom_app)
                           if current_custom_app else None)
    }


def _get_current_app(request, apps):
    current_app = [
        app for app in apps
        if request.resolver_match and
        app.url_app_name == request.resolver_match.app_name]
    return current_app[0] if len(current_app) > 0 else None


def _get_app_nav(request, current_app):
    if hasattr(current_app, 'nav'):
        # Copy and filter current_app's nav items
        nav = [item
               for item in copy.copy(current_app.nav)
               if 'enabled' not in item or item['enabled'](request)]
        # convert "/djangoapp/path/in/app" to "path/in/app"
        app_path = "/".join(request.path.split("/")[2:])
        for nav_item in nav:
            if 'active_prefixes' in nav_item:
                if re.match("|".join(nav_item['active_prefixes']), app_path):
                    nav_item['active'] = True
                else:
                    nav_item['active'] = False
            else:
                # 'active_prefixes' is optional, and if not specified, assume
                # current item is active
                nav_item['active'] = True
    else:
        # Default to the home view in the app
        nav = [
            {
                'label': current_app.verbose_name,
                'icon': 'fa ' + current_app.fa_icon_class,
                'url': current_app.url_home
            }
        ]
    return nav


def resolver_match(request):
    """Put resolver_match (ResolverMatch instance) into the context."""
    return {'resolver_match': request.resolver_match}
