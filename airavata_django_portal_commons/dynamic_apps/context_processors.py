import copy
from importlib import import_module
import logging
import re

from airavata_django_portal_commons import dynamic_apps

logger = logging.getLogger(__name__)


def custom_app_registry(request):
    """Put custom Django apps into the context."""
    custom_apps = dynamic_apps.CUSTOM_DJANGO_APPS.copy()
    custom_apps = [
        _enhance_custom_app_config(app)
        for app in custom_apps
        if (getattr(app, "enabled", None) is None or app.enabled(request))
    ]
    custom_apps.sort(key=lambda app: app.verbose_name.lower())
    current_custom_app = _get_current_app(request, custom_apps)
    return {
        # 'custom_apps': list(map(_app_to_dict, custom_apps)),
        "custom_apps": custom_apps,
        "current_custom_app": current_custom_app,
        "custom_app_nav": (
            _get_app_nav(request, current_custom_app) if current_custom_app else None
        ),
    }


def _enhance_custom_app_config(app):
    """As necessary add default values for properties to custom AppConfigs."""
    app.url_app_name = _get_url_app_name(app)
    app.url_home = _get_url_home(app)
    app.fa_icon_class = _get_fa_icon_class(app)
    app.app_description = _get_app_description(app)
    return app


def _get_url_app_name(app_config):
    """Return the urls namespace for the given AppConfig instance."""
    urls = _get_app_urls(app_config)
    return getattr(urls, "app_name", None)


def _get_url_home(app_config):
    """Get named URL of home page of app."""
    if hasattr(app_config, "url_home"):
        return app_config.url_home
    else:
        return _get_default_url_home(app_config)


def _get_default_url_home(app_config):
    """Return first url pattern as a default."""
    urls = _get_app_urls(app_config)
    app_name = _get_url_app_name(app_config)
    logger.warning(
        "Custom Django app {} has no URL namespace " "defined".format(app_config.label)
    )
    first_named_url = None
    for urlpattern in urls.urlpatterns:
        if hasattr(urlpattern, "name"):
            first_named_url = urlpattern.name
            break
    if not first_named_url:
        raise Exception(f"{urls} has no named urls, can't figure out default home URL")
    if app_name:
        return app_name + ":" + first_named_url
    else:
        return first_named_url


def _get_fa_icon_class(app_config):
    """Return Font Awesome icon class to use for app."""
    if hasattr(app_config, "fa_icon_class"):
        return app_config.fa_icon_class
    else:
        return "fa-circle"


def _get_app_description(app_config):
    """Return brief description of app."""
    return getattr(app_config, "app_description", None)


def _get_app_urls(app_config):
    return import_module(".urls", app_config.name)


def _get_current_app(request, apps):
    current_app = [
        app
        for app in apps
        if request.resolver_match
        and app.url_app_name == request.resolver_match.app_name
    ]
    return current_app[0] if len(current_app) > 0 else None


def _get_app_nav(request, current_app):
    if hasattr(current_app, "nav"):
        # Copy and filter current_app's nav items
        nav = [
            item
            for item in copy.copy(current_app.nav)
            if "enabled" not in item or item["enabled"](request)
        ]
        # convert "/djangoapp/path/in/app" to "path/in/app"
        app_path = "/".join(request.path.split("/")[2:])
        for nav_item in nav:
            if "active_prefixes" in nav_item:
                if re.match("|".join(nav_item["active_prefixes"]), app_path):
                    nav_item["active"] = True
                else:
                    nav_item["active"] = False
            else:
                # 'active_prefixes' is optional, and if not specified, assume
                # current item is active
                nav_item["active"] = True
    else:
        # Default to the home view in the app
        nav = [
            {
                "label": current_app.verbose_name,
                "icon": "fa " + current_app.fa_icon_class,
                "url": current_app.url_home,
            }
        ]
    return nav
