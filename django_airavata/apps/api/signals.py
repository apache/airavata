"""Signal and receivers for the api app."""

import logging

from airavata_django_portal_sdk import user_storage
from django.contrib.auth.signals import user_logged_in
from django.dispatch import Signal, receiver

log = logging.getLogger(__name__)


# Signals
user_added_to_group = Signal(providing_args=["user", "groups", "request"])


# Receivers
@receiver(user_logged_in)
def create_user_storage_dir(sender, request, user, **kwargs):
    """Create user's home direct in gateway storage."""
    path = ""
    if not user_storage.dir_exists(request, path):
        user_storage.create_user_dir(request, path)
        log.info("Created home directory for user {}".format(user.username))
