"""Django Airavata Auth Middleware."""
import copy
import logging

from django.conf import settings
from django.contrib.auth import logout
from django.shortcuts import redirect
from django.urls import reverse

from . import utils

log = logging.getLogger(__name__)


def authz_token_middleware(get_response):
    """Automatically add the 'authz_token' to the request."""
    def middleware(request):

        authz_token = None
        if request.user.is_authenticated:
            authz_token = utils.get_authz_token(request)
            # If we can't construct an authz_token then need to re-login
            if authz_token is None:
                # logout user since no longer logged in with IAM server
                logout(request)

        request.authz_token = authz_token

        return get_response(request)

    return middleware


def gateway_groups_middleware(get_response):
    """Add 'is_gateway_admin' and 'is_read_only_gateway_admin' to request."""
    def middleware(request):

        request.is_gateway_admin = False
        request.is_read_only_gateway_admin = False

        if (not request.user.is_authenticated or
            not request.authz_token or
            (hasattr(request.user, "user_profile") and
                not request.user.user_profile.is_complete)):
            return get_response(request)

        try:
            # Load the GatewayGroups and check if user is in the Admins and/or
            # Read Only Admins groups
            if not request.session.get('GATEWAY_GROUPS'):
                gateway_groups = request.airavata_client.getGatewayGroups(
                    request.authz_token)
                gateway_groups_dict = copy.deepcopy(gateway_groups.__dict__)
                request.session['GATEWAY_GROUPS'] = gateway_groups_dict
            gateway_groups = request.session['GATEWAY_GROUPS']
            admins_group_id = gateway_groups['adminsGroupId']
            read_only_admins_group_id = gateway_groups['readOnlyAdminsGroupId']
            group_manager_client = request.profile_service[
                'group_manager']
            group_memberships = group_manager_client.getAllGroupsUserBelongs(
                request.authz_token, request.user.username + "@" + settings.GATEWAY_ID)
            group_ids = [group.id for group in group_memberships]
            request.is_gateway_admin = admins_group_id in group_ids
            request.is_read_only_gateway_admin = \
                read_only_admins_group_id in group_ids
            # Gateway Admins are made 'superuser' in Django so they can edit
            # pages in the CMS
            if request.is_gateway_admin and (
                not request.user.is_superuser or
                    not request.user.is_staff):
                request.user.is_superuser = True
                request.user.is_staff = True
                request.user.save()
        except Exception as e:
            log.warning("Failed to set is_gateway_admin, "
                        "is_read_only_gateway_admin for user", exc_info=e)

        return get_response(request)
    return middleware


def user_profile_completeness_check(get_response):
    """Check if user profile is complete and if not, redirect to user profile editor."""
    def middleware(request):

        if not request.user.is_authenticated:
            return get_response(request)

        allowed_paths = [
            reverse('django_airavata_auth:user_profile'),
            reverse('django_airavata_auth:logout'),
        ]
        incomplete_user_profile = (hasattr(request.user, "user_profile") and
                                   not request.user.user_profile.is_complete)
        # Exclude admin's from the ext user profile check since they will be
        # creating/editing the ext user profile fields
        invalid_ext_user_profile = (not getattr(request, "is_gateway_admin", False) and
                                    hasattr(request.user, "user_profile") and
                                    not request.user.user_profile.is_ext_user_profile_valid)
        if ((incomplete_user_profile or invalid_ext_user_profile) and
            request.path not in allowed_paths and
                'text/html' in request.META['HTTP_ACCEPT']):
            return redirect('django_airavata_auth:user_profile')
        else:
            return get_response(request)
    return middleware
