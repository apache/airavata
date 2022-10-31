import logging
from importlib import import_module

from pkg_resources import iter_entry_points

# AppConfig instances from custom Django apps
CUSTOM_DJANGO_APPS = []

logger = logging.getLogger(__name__)


def load(installed_apps, entry_point_group="airavata.djangoapp"):
    for entry_point in iter_entry_points(group=entry_point_group):
        custom_app_class = entry_point.load()
        custom_app_instance = custom_app_class(
            entry_point.name, import_module(entry_point.module_name)
        )
        CUSTOM_DJANGO_APPS.append(custom_app_instance)
        # Create path to AppConfig class (otherwise the ready() method doesn't get
        # called)
        logger.info(f"adding dynamic Django app {entry_point.name}")
        installed_apps.append(
            "{}.{}".format(entry_point.module_name, entry_point.attrs[0])
        )


def merge_setting_dict(default, custom_setting):
    # FIXME: only handles dict settings, doesn't handle lists
    if isinstance(custom_setting, dict):
        for k in custom_setting.keys():
            if k not in default:
                default[k] = custom_setting[k]
            else:
                raise Exception(
                    "Custom django app setting conflicts with "
                    "key {} in {}".format(k, default)
                )


def merge_setting(settings_module, setting_name):
    # Merge settings from custom Django apps
    # FIXME: only handles WEBPACK_LOADER additions
    for custom_django_app in CUSTOM_DJANGO_APPS:
        if hasattr(custom_django_app, "settings"):
            s = custom_django_app.settings
            merge_setting_dict(
                getattr(settings_module, setting_name), getattr(s, setting_name, {})
            )


def merge_webpack_loader(settings_module):
    merge_setting(settings_module, "WEBPACK_LOADER")
