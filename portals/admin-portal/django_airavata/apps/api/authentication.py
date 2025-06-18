import logging

from django.contrib.auth import authenticate
from rest_framework import authentication, exceptions

from django_airavata.apps.auth import utils
from django_airavata.apps.auth.middleware import set_admin_group_attributes

logger = logging.getLogger(__name__)


class OAuthAuthentication(authentication.BaseAuthentication):
    def authenticate(self, request):

        if 'HTTP_AUTHORIZATION' in request.META:
            try:
                user = authenticate(request=request)
                if user is None:
                    raise exceptions.AuthenticationFailed(
                        "Token failed to authenticate")
                _, token = request.META.get('HTTP_AUTHORIZATION').split()

                logger.debug(f"OAuthAuthentication authenticated user {user}")
                # Set request attributes that are normally set by middleware
                request.authz_token = utils.get_authz_token(request, user=user, access_token=token)
                request.user = user
                set_admin_group_attributes(request)
                return (user, token)
            except Exception as e:
                raise exceptions.AuthenticationFailed(
                    "Token failed to authenticate") from e
        else:
            return None
