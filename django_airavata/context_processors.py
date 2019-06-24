import logging
import datetime

from django.urls import reverse
from django.apps import apps
from django.conf import settings
from django_airavata.apps.api.models import User_Notifications
from django_airavata.app_config import AiravataAppConfig
from django.core.exceptions import ObjectDoesNotExist


logger = logging.getLogger(__name__)


def create_notification_ui(notification):
    notification.level = {}
    notification.textColor = "text-info";

    if notification.priority == 0:
        notification.textColor = "text-primary";
        notification.level = "badge-info"
    elif( notification.priority == 1):
        notification.textColor = "text-warning";
        notification.level = "badge-warning"
    elif( notification.priority == 2):
        notification.textColor = "text-danger";
        notification.level = "badge-danger"

    if notification.is_read:
        notification.is_read_text = "read"
    else:
        notification.is_read_text = "unread"

    return notification


def get_notifications(request):
    #TODO: Check is user is authenticated, check if notifications is already set and don't fetch new notificaions ->> Or fetch them after some time instead of refreshing on each time
    if request.user.is_authenticated:
        notifications = request.airavata_client.getAllNotifications(
                    request.authz_token, settings.GATEWAY_ID)
        current_time = datetime.datetime.utcnow()
        valid_notifications = []
        for notification in notifications:

            notification.expirationTime = datetime.datetime.fromtimestamp(
                                            notification.expirationTime/1000)
            notification.publishedTime = datetime.datetime.fromtimestamp(
                                            notification.publishedTime/1000)

            if(notification.expirationTime > current_time and
                                    notification.publishedTime < current_time ):
                notification.url = request.build_absolute_uri( \
                            reverse('django_airavata_api:ack-notifications'))\
                            + "?id=" + str(notification.notificationId)

                try:
                    notification_status = User_Notifications.objects.get(
                                notification_id=notification.notificationId,
                                username=request.user.username)
                except ObjectDoesNotExist:
                    print("Either the entry or blog doesn't exist.")
                    notification_status = User_Notifications.objects.create(
                                username=request.user.username,
                                notification_id=notification.notificationId)
                notification.is_read = notification_status.is_read
                notification = create_notification_ui(notification)
                valid_notifications.append(notification)

        return {
            "notifications": valid_notifications
        }
    else:
        return {"notifications": ""}

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
