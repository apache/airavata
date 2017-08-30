
from django.conf import settings
from django.contrib.auth.models import User
from django.urls import reverse

from oauthlib.oauth2 import LegacyApplicationClient
from requests_oauthlib import OAuth2Session

import logging

logger = logging.getLogger(__name__)

class KeycloakBackend(object):
    def authenticate(self, username=None, password=None, request=None):
        token = None
        userinfo = None
        if username and password:
            token, userinfo = self._get_token_and_userinfo_password_flow(username, password)
        else:
            token, userinfo = self._get_token_and_userinfo_redirect_flow(request)
        # TODO validate the JWS signature
        logger.debug("token: {}".format(token))
        access_token = token['access_token']
        logger.debug("userinfo: {}".format(userinfo))
        username = userinfo['preferred_username']
        # TODO load user roles too
        try:
            user = User.objects.get(username=username)
        except User.DoesNotExist:
            user = User(username=username)
            user.save()
        # Put access_token into session to be used for authenticating with API server
        request.session['ACCESS_TOKEN'] = access_token
        request.session['USERINFO'] = userinfo
        return user

    def get_user(self, user_id):
        try:
            return User.objects.get(pk=user_id)
        except User.DoesNotExist:
            return None

    def _get_token_and_userinfo_password_flow(self, username, password):
        client_id = settings.KEYCLOAK_CLIENT_ID
        client_secret = settings.KEYCLOAK_CLIENT_SECRET
        token_url = settings.KEYCLOAK_TOKEN_URL
        userinfo_url = settings.KEYCLOAK_USERINFO_URL
        verify_ssl = settings.KEYCLOAK_VERIFY_SSL
        oauth2_session = OAuth2Session(client=LegacyApplicationClient(client_id=client_id))
        if hasattr(settings, 'KEYCLOAK_CA_CERTFILE'):
            oauth2_session.verify = settings.KEYCLOAK_CA_CERTFILE
        token = oauth2_session.fetch_token(token_url=token_url, username=username,
            password=password, client_id=client_id, client_secret=client_secret, verify=verify_ssl)
        userinfo = oauth2_session.get(userinfo_url).json()
        return token, userinfo

    def _get_token_and_userinfo_redirect_flow(self, request):
        authorization_code_url=request.build_absolute_uri()
        redirect_url=request.build_absolute_uri(reverse('django_airavata_auth:callback'))
        client_id = settings.KEYCLOAK_CLIENT_ID
        client_secret = settings.KEYCLOAK_CLIENT_SECRET
        token_url = settings.KEYCLOAK_TOKEN_URL
        userinfo_url = settings.KEYCLOAK_USERINFO_URL
        verify_ssl = settings.KEYCLOAK_VERIFY_SSL
        state = request.session['OAUTH2_STATE']
        logger.debug("state={}".format(state))
        oauth2_session = OAuth2Session(client_id, scope='openid', redirect_uri=redirect_url, state=state)
        if hasattr(settings, 'KEYCLOAK_CA_CERTFILE'):
            oauth2_session.verify = settings.KEYCLOAK_CA_CERTFILE
        token = oauth2_session.fetch_token(token_url, client_secret=client_secret,
            authorization_response=authorization_code_url, verify=verify_ssl)
        userinfo = oauth2_session.get(userinfo_url).json()
        return token, userinfo
