import copy
import datetime
import json
import logging
import re

from django.apps import apps
from django.conf import settings
from django.core.exceptions import ObjectDoesNotExist
from django.urls import reverse

from django_airavata.app_config import AiravataAppConfig
from django_airavata.apps.api.models import User_Notifications

logger = logging.getLogger(__name__)


def get_notifications(request):
    if request.user.is_authenticated and hasattr(request, 'airavata_client'):
        unread_notifications = 0
        try:
            notifications = request.airavata_client.getAllNotifications(
                request.authz_token, settings.GATEWAY_ID)
        except Exception:
            logger.warning("Failed to load notifications")
            notifications = []
        current_time = datetime.datetime.utcnow()
        valid_notifications = []
        for notification in notifications:

            notification_data = notification.__dict__
            expirationTime = datetime.datetime.fromtimestamp(
                notification.expirationTime / 1000)
            publishedTime = datetime.datetime.fromtimestamp(
                notification.publishedTime / 1000)

            if(expirationTime > current_time and publishedTime < current_time):
                notification_data['url'] = request.build_absolute_uri(
                    reverse('django_airavata_api:ack-notifications'))\
                    + "?id=" + str(notification.notificationId)

                try:
                    notification_status = User_Notifications.objects.get(
                        notification_id=notification.notificationId,
                        username=request.user.username)
                except ObjectDoesNotExist:
                    notification_status = User_Notifications.objects.create(
                        username=request.user.username,
                        notification_id=notification.notificationId)
                notification_data['is_read'] = notification_status.is_read
                if not notification_status.is_read:
                    unread_notifications += 1
                valid_notifications.append(notification_data)

        return {
            "notifications": json.dumps(valid_notifications),
            "unread_notifications": unread_notifications
        }
    else:
        return {"notifications": json.dumps([])}


def user_session_data(request):
    data = {}
    if request.user.is_authenticated:
        data["username"] = request.user.username
        data["airavataInternalUserId"] = (request.user.username +
                                          "@" +
                                          settings.GATEWAY_ID)
        # is_gateway_admin may not be set if a failure occurs during login
        data["isGatewayAdmin"] = getattr(request, "is_gateway_admin", False)
    return {
        "user_session_data": json.dumps(data)
    }


def airavata_app_registry(request):
    """Put airavata django apps into the context."""
    airavata_apps = [app for app in apps.get_app_configs()
                     if isinstance(app, AiravataAppConfig) and
                     (getattr(app, 'enabled', None) is None or
                      app.enabled(request)
                      ) and
                     app.label not in settings.HIDDEN_AIRAVATA_APPS]
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


def google_analytics_tracking_id(request):
    """Put the Google Analytics tracking id into context."""
    return {'ga_tracking_id':
            getattr(settings, 'GOOGLE_ANALYTICS_TRACKING_ID', None)}
