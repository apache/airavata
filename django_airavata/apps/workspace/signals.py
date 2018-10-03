"""Signal receivers for the workspace app."""

import datetime
import logging

from django.conf import settings
from django.contrib.auth.signals import user_logged_in
from django.dispatch import receiver

from airavata.model.user.ttypes import Status, UserProfile
from airavata.model.workspace.ttypes import Project
from django_airavata.apps.auth.utils import get_authz_token
from django_airavata.utils import get_airavata_client, get_user_profile_client

log = logging.getLogger(__name__)


@receiver(user_logged_in)
def create_user_profile_for_new_user(sender, request, user, **kwargs):
    """Create basic User Profile for new user."""
    # auth middleware hasn't run yet so authz_token attribute is not available
    # on request, so need to create the authz_token manually
    authz_token = get_authz_token(request)
    with get_user_profile_client() as user_profile_client:
        user_profile_exists = user_profile_client.doesUserExist(
            authz_token, user.username, settings.GATEWAY_ID)
        if not user_profile_exists:
            log.debug("UserProfile doesn't exist for {username}, "
                      "creating...".format(username=user.username))
            new_user_profile = UserProfile()
            new_user_profile.airavataInternalUserId = (user.username + "@" +
                                                       settings.GATEWAY_ID)
            new_user_profile.userId = user.username
            new_user_profile.gatewayId = settings.GATEWAY_ID
            new_user_profile.emails = [user.email]
            new_user_profile.firstName = user.first_name
            new_user_profile.lastName = user.last_name
            unix_utcnow_ms = int(datetime.datetime.utcnow().timestamp() * 1000)
            new_user_profile.creationTime = unix_utcnow_ms
            new_user_profile.lastAccessTime = unix_utcnow_ms
            new_user_profile.validUntil = -1
            new_user_profile.State = Status.ACTIVE
            user_profile_client.addUserProfile(authz_token, new_user_profile)
            log.info("Created a new UserProfile for {username}".format(
                username=user.username))


@receiver(user_logged_in)
def create_default_project_if_not_exists(sender, request, user, **kwargs):
    """Create 'Default Project' for new user."""
    # auth middleware hasn't run yet so authz_token attribute is not available
    # on request, so need to create the authz_token manually
    authz_token = get_authz_token(request)
    with get_airavata_client() as airavata_client:
        # Just retrieve the first project
        projects = airavata_client.getUserProjects(
            authz_token, settings.GATEWAY_ID, request.user.username, 1, 0)
        if len(projects) == 0:
            log.info("Creating default project for user {}".format(
                user.username))
            default_project = Project()
            default_project.owner = request.user.username
            default_project.name = "Default Project"
            default_project.gatewayId = settings.GATEWAY_ID
            default_project.description = ("This is the default project for "
                                           "user {owner}".format(
                                               owner=default_project.owner))
            airavata_client.createProject(authz_token, settings.GATEWAY_ID,
                                          default_project)
