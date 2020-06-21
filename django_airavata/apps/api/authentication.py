import logging

from django.contrib.auth import authenticate
from rest_framework import authentication, exceptions

from django_airavata.apps.auth.utils import get_authz_token

logger = logging.getLogger(__name__)


class OAuthAuthentication(authentication.BaseAuthentication):
    def authenticate(self, request):

        if 'HTTP_AUTHORIZATION' in request.META:
            try:
                user = authenticate(request=request)
                _, token = request.META.get('HTTP_AUTHORIZATION').split()

                # authz_token_middleware has already run, so must manually add
                # the `request.authz_token` attribute

                # Must pass user directly since `request.user` access will
                # trigger this Authentication being called again, resulting in
                # an infinite loop
                request.authz_token = get_authz_token(request, user=user, access_token=token)
                logger.debug(f"OAuthAuthentication authenticated user {user}")
                return (user, token)
            except Exception as e:
                raise exceptions.AuthenticationFailed(
                    "Token failed to authenticate") from e
        else:
            return None
