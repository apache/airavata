"""Signal receivers for the workspace app."""

import logging

from django.conf import settings
from django.contrib.auth.signals import user_logged_in
from django.dispatch import receiver

from airavata.model.workspace.ttypes import Project
from django_airavata.apps.auth.utils import get_authz_token
from django_airavata.utils import get_airavata_client

log = logging.getLogger(__name__)


@receiver(user_logged_in)
def create_default_project_if_not_exists(sender, request, user, **kwargs):
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
