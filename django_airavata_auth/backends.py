
from django.contrib.auth.models import User

from requests_oauthlib import OAuth2Session

import logging

logger = logging.getLogger(__name__)

class WSO2ISBackend(object):
    def authenticate(self, authorization_code_url=None, redirect_url=None, request=None):
        # TODO: get these values from settings
        client_id = 'fGwm3EW0EmaiV0jI6GBmmOiQ2Xca'
        token_url = 'https://localhost:9443/oauth2/token'
        userinfo_url = 'https://localhost:9443/oauth2/userinfo?schema=openid'
        client_secret = 'fMLLvWH6YEHwgl4Nb0hHu9AC5Jwa'
        # TODO: maybe store the OAuth2Session in session?
        wso2is = OAuth2Session(client_id, scope='openid', redirect_uri=redirect_url)
        token = wso2is.fetch_token(token_url, client_secret=client_secret,
            authorization_response=authorization_code_url, verify=False)
        access_token = token['access_token']
        userinfo = wso2is.get(userinfo_url).json()
        logger.debug("userinfo: %s", userinfo)
        # TODO WSO2 IS userinfo only returns the 'sub' claim. Fixed in 5.3.0?
        # See also: http://stackoverflow.com/q/41281292
        # and: https://wso2.org/jira/browse/IDENTITY-4250
        sub_claim = userinfo['sub']
        # For WSO2 IS sometimes the sub claim is in the form of 'username@tenant-id'
        username = sub_claim.split('@')[0]
        try:
            user = User.objects.get(username=username)
        except User.DoesNotExist:
            user = User(username=username)
            user.save()
        # Put access_token into session to be used for authenticating with API server
        request.session['ACCESS_TOKEN'] = access_token
        return user

    def get_user(self, user_id):
        try:
            return User.objects.get(pk=user_id)
        except User.DoesNotExist:
            return None
