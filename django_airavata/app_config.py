
import logging
from abc import ABC, abstractmethod
from importlib import import_module

from django.apps import AppConfig

logger = logging.getLogger(__name__)


class AiravataAppConfig(AppConfig, ABC):
    """Custom AppConfig for Django Airavata apps."""

    @property
    def url_app_name(self):
        """Return the urls application namespace."""
        return get_url_app_name(self)

    @property
    @abstractmethod
    def app_order(self):
        """Return positive int order of app in listings, lowest sorts first."""
        pass

    @property
    @abstractmethod
    def url_home(self):
        """Named route of home page for this application."""
        pass

    @property
    @abstractmethod
    def fa_icon_class(self):
        """Font Awesome icon class name."""
        pass

    @property
    @abstractmethod
    def app_description(self):
        """Some user friendly text to briefly describe the application."""
        pass


def enhance_custom_app_config(app):
    """As necessary add default values for properties to custom AppConfigs."""
    app.url_app_name = get_url_app_name(app)
    app.url_home = get_url_home(app)
    app.fa_icon_class = get_fa_icon_class(app)
    app.app_description = get_app_description(app)
    return app


def get_url_app_name(app_config):
    """Return the urls namespace for the given AppConfig instance."""
    urls = get_app_urls(app_config)
    return getattr(urls, 'app_name', None)


def get_url_home(app_config):
    """Get named URL of home page of app."""
    if hasattr(app_config, 'url_home'):
        return app_config.url_home
    else:
        return get_default_url_home(app_config)


def get_default_url_home(app_config):
    """Return first url pattern as a default."""
    urls = get_app_urls(app_config)
    app_name = get_url_app_name(app_config)
    logger.warning("Custom Django app {} has no URL namespace "
                   "defined".format(app_config.label))
    first_named_url = None
    for urlpattern in urls.urlpatterns:
        if hasattr(urlpattern, 'name'):
            first_named_url = urlpattern.name
            break
    if not first_named_url:
        raise Exception("{} has no named urls, "
                        "can't figure out default home URL")
    if app_name:
        return app_name + ":" + first_named_url
    else:
        return first_named_url


def get_fa_icon_class(app_config):
    """Return Font Awesome icon class to use for app."""
    if hasattr(app_config, "fa_icon_class"):
        return app_config.fa_icon_class
    else:
        return 'fa-circle'


def get_app_description(app_config):
    """Return brief description of app."""
    return getattr(app_config, 'app_description', None)


def get_app_urls(app_config):
    return import_module(".urls", app_config.name)
