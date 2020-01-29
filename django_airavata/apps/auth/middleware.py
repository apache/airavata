"""Django Airavata Auth Middleware."""
import copy
import logging

from django.conf import settings
from django.contrib.auth import logout

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

        if not request.user.is_authenticated or not request.authz_token:
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
            request.is_gateway_admin = False
            request.is_read_only_gateway_admin = False

        return get_response(request)
    return middleware
