
from abc import ABC, abstractmethod

from django.apps import AppConfig


class AiravataAppConfig(AppConfig, ABC):
    """Custom AppConfig for Django Airavata apps."""

    @property
    @abstractmethod
    def url_app_name(self):
        """Return the urls application namespace (typically, same as label)."""
        pass

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
