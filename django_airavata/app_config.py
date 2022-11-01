
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


def get_url_app_name(app_config):
    """Return the urls namespace for the given AppConfig instance."""
    urls = get_app_urls(app_config)
    return getattr(urls, 'app_name', None)


def get_app_urls(app_config):
    return import_module(".urls", app_config.name)
