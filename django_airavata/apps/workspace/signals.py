"""Signal receivers for the workspace app."""

import logging

from django.contrib.auth.signals import user_logged_in
from django.dispatch import receiver

from django_airavata.apps.auth.utils import get_authz_token
from django_airavata.utils import user_profile_client_pool

log = logging.getLogger(__name__)


@receiver(user_logged_in)
def initialize_user_profile(sender, request, user, **kwargs):
    """Initialize user profile in Airavata in case this is a new user."""
    # NOTE: if the user verified their email address then they should already
    # have an Airavata user profile (See IAMAdminServices.enableUser). The
    # following is necessary for users coming from federated login who don't
    # need to verify their email.
    authz_token = get_authz_token(request)
    if authz_token is not None:
        user_profile_client_pool.initializeUserProfile(authz_token)
        log.debug("initialized user profile for {}".format(user.username))
    else:
        log.warning(f"Logged in user {user.username} has no access token")
