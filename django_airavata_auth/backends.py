
from django.contrib.auth.models import User

from requests_oauthlib import OAuth2Session

import logging

logger = logging.getLogger(__name__)

class WSO2ISBackend(object):
    def authenticate(self, authorization_code_url=None, redirect_url=None):
        # TODO: get these values from settings
        client_id = 'fGwm3EW0EmaiV0jI6GBmmOiQ2Xca'
        token_url = 'https://localhost:9443/oauth2/token'
        userinfo_url = 'https://localhost:9443/oauth2/userinfo?schema=openid'
        client_secret = 'fMLLvWH6YEHwgl4Nb0hHu9AC5Jwa'
        # TODO: maybe store the OAuth2Session in session?
        wso2is = OAuth2Session(client_id, scope='openid', redirect_uri=redirect_url)
        token = wso2is.fetch_token(token_url, client_secret=client_secret,
            authorization_response=authorization_code_url, verify=False)
        userinfo = wso2is.get(userinfo_url).json()
        # TODO WSO2 IS userinfo only returns the 'sub' claim. Fixed in 5.3.0?
        # See also: http://stackoverflow.com/q/41281292
        # and: https://wso2.org/jira/browse/IDENTITY-4250
        sub_claim = userinfo['sub']
        # For WSO2 IS sometimes the sub claim is in the form of 'username@tenant-id'
        username = sub_claim.split('@')[0]
        logger.debug("userinfo: %s", userinfo)
        try:
            user = User.objects.get(username=username)
        except User.DoesNotExist:
            user = User(username=username)
            user.save()
        return user

    def get_user(self, user_id):
        # TODO: lookup user out of database
        pass