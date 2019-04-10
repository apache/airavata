import logging

from django.conf import settings
from django.core.exceptions import ObjectDoesNotExist

from airavata.model.group.ttypes import ResourcePermissionType

from . import models

logger = logging.getLogger(__name__)


class WorkspacePreferencesHelper:

    def get(self, request):
        try:
            workspace_preferences = models.WorkspacePreferences.objects.get(
                username=request.user.username)
            self._check(request, workspace_preferences)
        except ObjectDoesNotExist as e:
            workspace_preferences = self._create_default(request)
            workspace_preferences.save()
        return workspace_preferences

    def _create_default(self, request):
        workspace_preferences = models.WorkspacePreferences.create(
            request.user.username)
        most_recent_project = self._get_most_recent_project(request)
        workspace_preferences.most_recent_project_id = \
            most_recent_project.projectID
        return workspace_preferences

    def _get_most_recent_project(self, request):
        "Return most recent writeable project."
        projects = request.airavata_client.getUserProjects(
            request.authz_token, settings.GATEWAY_ID, request.user.username,
            -1, 0)
        for project in projects:
            if self._can_write(request, project.projectID):
                return project
        return None

    def _check(self, request, prefs):
        "Validate preference values and update as needed."
        if (not prefs.most_recent_project_id or
                not self._can_write(request, prefs.most_recent_project_id)):
            most_recent_project = self._get_most_recent_project(request)
            logger.warn(
                "_check: updating most_recent_project_id to {}".format(
                    most_recent_project.projectID))
            prefs.most_recent_project_id = most_recent_project.projectID
            prefs.save()

    def _can_write(self, request, entity_id):
        return request.airavata_client.userHasAccess(
            request.authz_token,
            entity_id,
            ResourcePermissionType.WRITE)
