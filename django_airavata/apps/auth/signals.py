from django.conf import settings
from django.dispatch import receiver
from django.shortcuts import reverse
from django.template import Context

from django_airavata.apps.api.signals import user_added_to_group

from . import models, utils


@receiver(user_added_to_group, dispatch_uid="auth_email_user_added_to_group")
def email_user_added_to_group(sender, user, groups, request, **kwargs):
    context = Context({
        "email": user.emails[0],
        "first_name": user.firstName,
        "last_name": user.lastName,
        "username": user.userId,
        "portal_title": settings.PORTAL_TITLE,
        "dashboard_url": request.build_absolute_uri(
            reverse("django_airavata_workspace:dashboard")),
        "experiments_url": request.build_absolute_uri(
            reverse("django_airavata_workspace:experiments")),
        "group_names": [g.name for g in groups]
    })
    utils.send_email_to_user(models.USER_ADDED_TO_GROUP_TEMPLATE, context)
