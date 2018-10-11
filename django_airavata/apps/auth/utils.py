"""Auth utilities."""

import time

from django.conf import settings
from django.contrib.auth import authenticate
from oauthlib.oauth2 import BackendApplicationClient
from requests_oauthlib import OAuth2Session

from airavata.model.security.ttypes import AuthzToken


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


def get_service_account_authz_token():
    client_id = settings.KEYCLOAK_CLIENT_ID
    client_secret = settings.KEYCLOAK_CLIENT_SECRET
    token_url = settings.KEYCLOAK_TOKEN_URL
    verify_ssl = settings.KEYCLOAK_VERIFY_SSL

    client = BackendApplicationClient(client_id=client_id)
    oauth = OAuth2Session(client=client)
    if hasattr(settings, 'KEYCLOAK_CA_CERTFILE'):
        oauth.verify = settings.KEYCLOAK_CA_CERTFILE
    token = oauth.fetch_token(
        token_url=token_url,
        client_id=client_id,
        client_secret=client_secret,
        verify=verify_ssl)

    access_token = token.get('access_token')
    return AuthzToken(
        accessToken=access_token,
        claimsMap={
            'gatewayID': settings.GATEWAY_ID,
            # This is a service account, so leaving userName blank for now
            'userName': None})


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
