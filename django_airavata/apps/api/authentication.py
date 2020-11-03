import logging

from django.contrib.auth import authenticate
from rest_framework import authentication, exceptions

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
                return (user, token)
            except Exception as e:
                raise exceptions.AuthenticationFailed(
                    "Token failed to authenticate") from e
        else:
            return None
