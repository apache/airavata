"""Signal receivers for the workspace app."""

import logging

from django.contrib.auth.signals import user_logged_in
from django.dispatch import receiver

from django_airavata.apps.auth.utils import get_authz_token
from django_airavata.utils import get_user_profile_client

log = logging.getLogger(__name__)


@receiver(user_logged_in)
def initialize_user_profile(sender, request, user, **kwargs):
    """Initialize user profile in Airavata in case this is a new user."""
    authz_token = get_authz_token(request)
    with get_user_profile_client() as user_profile_client:
        user_profile_client.initializeUserProfile(authz_token)
        log.debug("initialized user profile for {}".format(user.username))
