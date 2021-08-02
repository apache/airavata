import logging

from airavata.model.group.ttypes import ResourcePermissionType
from django.conf import settings
from django.core.exceptions import ObjectDoesNotExist

from . import models

logger = logging.getLogger(__name__)


class WorkspacePreferencesHelper:

    def get(self, request):
        try:
            workspace_preferences = models.WorkspacePreferences.objects.get(
                username=request.user.username)
            self._check(request, workspace_preferences)
        except ObjectDoesNotExist:
            workspace_preferences = self._create_default(request)
            workspace_preferences.save()
        return workspace_preferences

    def _create_default(self, request):
        workspace_preferences = models.WorkspacePreferences.create(
            request.user.username)
        most_recent_project = self._get_most_recent_project(request)
        workspace_preferences.most_recent_project_id = \
            most_recent_project.projectID
        first_grp = \
            self._get_first_group_resource_profile(request)
        workspace_preferences.most_recent_group_resource_profile_id = \
            first_grp.groupResourceProfileId if first_grp else None
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

    def _get_first_group_resource_profile(self, request):
        "Return first accessible group resource profile"

        group_resource_profiles = request.airavata_client.getGroupResourceList(
            request.authz_token, settings.GATEWAY_ID)
        if len(group_resource_profiles) > 0:
            return group_resource_profiles[0]
        else:
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
        group_resource_profiles = request.airavata_client.getGroupResourceList(
            request.authz_token, settings.GATEWAY_ID)
        group_resource_profile_ids = list(map(lambda g: g.groupResourceProfileId, group_resource_profiles))
        if (not prefs.most_recent_group_resource_profile_id or
                prefs.most_recent_group_resource_profile_id not in group_resource_profile_ids):
            first_grp_id = (group_resource_profile_ids[0]
                            if len(group_resource_profile_ids) > 0
                            else None)
            logger.warn(f"_check: updating "
                        f"most_recent_group_resource_profile_id to "
                        f"{first_grp_id}")
            prefs.most_recent_group_resource_profile_id = first_grp_id
            prefs.save()

    def _can_write(self, request, entity_id):
        return request.airavata_client.userHasAccess(
            request.authz_token,
            entity_id,
            ResourcePermissionType.WRITE)

    def _can_read(self, request, entity_id):
        return request.airavata_client.userHasAccess(
            request.authz_token,
            entity_id,
            ResourcePermissionType.READ)
