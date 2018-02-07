"""Auth utilities."""

import time

from airavata.model.security.ttypes import AuthzToken

from django.conf import settings
from django.contrib.auth import authenticate


def get_authz_token(request):
    """Construct AuthzToken instance from session; refresh token if needed."""
    if not is_access_token_expired(request):
        return _create_authz_token(request)
    elif not is_refresh_token_expired(request):
        # Have backend reauthenticate the user with the refresh token
        user = authenticate(request)
        if user:
            return _create_authz_token(request)
    return None


def _create_authz_token(request):
    access_token = request.session['ACCESS_TOKEN']
    username = request.user.username
    gateway_id = settings.GATEWAY_ID
    return AuthzToken(accessToken=access_token,
                      claimsMap={'gatewayID': gateway_id,
                                 'userName': username})


def is_access_token_expired(request):
    """Return True if access_token is not available or is expired."""
    now = time.time()
    return not request.user.is_authenticated \
        or 'ACCESS_TOKEN' not in request.session \
        or 'ACCESS_TOKEN_EXPIRES_AT' not in request.session \
        or request.session['ACCESS_TOKEN_EXPIRES_AT'] < now


def is_refresh_token_expired(request):
    """Return True if refresh_token is not available or is expired."""
    now = time.time()
    return 'REFRESH_TOKEN' not in request.session \
        or 'REFRESH_TOKEN_EXPIRES_AT' not in request.session \
        or request.session['REFRESH_TOKEN_EXPIRES_AT'] < now
