
from django.conf import settings
from django.contrib.auth.models import User
from django.urls import reverse

from requests_oauthlib import OAuth2Session

import logging

logger = logging.getLogger(__name__)

class KeycloakBackend(object):
    def authenticate(self, request=None):
        authorization_code_url=request.build_absolute_uri()
        redirect_url=request.build_absolute_uri(reverse('airavata_auth_callback'))
        client_id = settings.KEYCLOAK_CLIENT_ID
        token_url = settings.KEYCLOAK_TOKEN_URL
        userinfo_url = settings.KEYCLOAK_USERINFO_URL
        client_secret = settings.KEYCLOAK_CLIENT_SECRET
        verify_ssl = settings.KEYCLOAK_VERIFY_SSL
        state = request.session['OAUTH2_STATE']
        logger.debug("state={}".format(state))
        oauth2_session = OAuth2Session(client_id, scope='openid', redirect_uri=redirect_url, state=state)
        oauth2_session.verify = settings.KEYCLOAK_CA_CERTFILE
        token = oauth2_session.fetch_token(token_url, client_secret=client_secret,
            authorization_response=authorization_code_url, verify=verify_ssl)
        # TODO validate the JWS signature
        logger.debug("token: {}".format(token))
        access_token = token['access_token']
        userinfo = oauth2_session.get(userinfo_url).json()
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
