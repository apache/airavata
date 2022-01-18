"""Django Airavata Auth Backends: KeycloakBackend."""
import logging
import os
import time

import requests
from django.conf import settings
from django.contrib.auth.models import User
from django.views.decorators.debug import sensitive_variables
from oauthlib.oauth2 import InvalidGrantError, LegacyApplicationClient
from requests_oauthlib import OAuth2Session

from django_airavata.apps.auth.utils import get_authz_token

from . import models, utils

logger = logging.getLogger(__name__)


class KeycloakBackend(object):
    """Django authentication backend for Keycloak."""

    # mask all local variables from error emails since they contain the user's
    # password and/or client_secret. Note, we could selectively just hide
    # variables that are sensitive, but this decorator doesn't apply explicitly
    # listed variable masking to library function calls
    @sensitive_variables()
    def authenticate(self,
                     request=None,
                     username=None,
                     password=None,
                     refresh_token=None,
                     idp_alias=None):
        try:
            user = None
            access_token = None
            if username and password:
                token, userinfo = self._get_token_and_userinfo_password_flow(
                    username, password)
                if token is None:  # login failed
                    return None
                self._process_token(request, token)
                user = self._process_userinfo(request, userinfo)
                access_token = token['access_token']
            elif 'HTTP_AUTHORIZATION' in request.META:
                bearer, token = request.META.get('HTTP_AUTHORIZATION').split()
                if bearer != "Bearer":
                    raise Exception("Unexpected Authorization header")
                # implicitly validate token by using it to get userinfo
                userinfo = self._get_userinfo_from_token(request, token)
                user = self._process_userinfo(request, userinfo)
                access_token = token
            # user is already logged in and can use refresh token
            elif request.user.is_authenticated and not utils.is_refresh_token_expired(request):
                logger.debug("Refreshing token...")
                token, userinfo = \
                    self._get_token_and_userinfo_from_refresh_token(request)
                if token is None:  # refresh failed
                    return None
                self._process_token(request, token)
                # user is already logged in
                user = request.user
                access_token = token['access_token']
            elif refresh_token:
                logger.debug("Refreshing supplied token...")
                token, userinfo = \
                    self._get_token_and_userinfo_from_refresh_token(
                        request, refresh_token=refresh_token)
                if token is None:  # refresh failed
                    return None
                self._process_token(request, token)
                user = self._process_userinfo(request, userinfo)
                access_token = token['access_token']
            else:
                token, userinfo = self._get_token_and_userinfo_redirect_flow(
                    request)
                self._process_token(request, token)
                user = self._process_userinfo(request, userinfo)
                if idp_alias is not None:
                    self._store_idp_userinfo(user, token, idp_alias)
                    self._check_username_initialization(request, user)
                access_token = token['access_token']
            # authz_token_middleware has already run, so must manually add
            # the `request.authz_token` attribute
            if user is not None:
                request.authz_token = get_authz_token(
                    request, user=user, access_token=access_token)
            return user
        except Exception as e:
            logger.warning("login failed", exc_info=e)
            raise

    def get_user(self, user_id):
        try:
            return User.objects.get(pk=user_id)
        except User.DoesNotExist:
            return None

    def _get_token_and_userinfo_password_flow(self, username, password):
        try:
            client_id = settings.KEYCLOAK_CLIENT_ID
            client_secret = settings.KEYCLOAK_CLIENT_SECRET
            token_url = settings.KEYCLOAK_TOKEN_URL
            userinfo_url = settings.KEYCLOAK_USERINFO_URL
            verify_ssl = settings.KEYCLOAK_VERIFY_SSL
            oauth2_session = OAuth2Session(client=LegacyApplicationClient(
                client_id=client_id))
            verify = verify_ssl
            if verify_ssl and hasattr(settings, 'KEYCLOAK_CA_CERTFILE'):
                verify = settings.KEYCLOAK_CA_CERTFILE
            token = oauth2_session.fetch_token(token_url=token_url,
                                               username=username,
                                               password=password,
                                               client_id=client_id,
                                               client_secret=client_secret,
                                               verify=verify)
            userinfo = oauth2_session.get(userinfo_url).json()
            return token, userinfo
        except InvalidGrantError as e:
            # password wasn't valid, just log as a warning
            logger.warning(f"Failed to log in user {username} with "
                           f"password: {e}")
            return None, None

    def _get_token_and_userinfo_redirect_flow(self, request):
        authorization_code_url = request.build_absolute_uri()
        client_id = settings.KEYCLOAK_CLIENT_ID
        client_secret = settings.KEYCLOAK_CLIENT_SECRET
        token_url = settings.KEYCLOAK_TOKEN_URL
        userinfo_url = settings.KEYCLOAK_USERINFO_URL
        verify_ssl = settings.KEYCLOAK_VERIFY_SSL
        state = request.session['OAUTH2_STATE']
        redirect_uri = request.session['OAUTH2_REDIRECT_URI']
        logger.debug("state={}".format(state))
        oauth2_session = OAuth2Session(client_id,
                                       scope='openid',
                                       redirect_uri=redirect_uri,
                                       state=state)
        verify = verify_ssl
        if verify_ssl and hasattr(settings, 'KEYCLOAK_CA_CERTFILE'):
            verify = settings.KEYCLOAK_CA_CERTFILE
        if not request.is_secure() and settings.DEBUG and not os.environ.get('OAUTHLIB_INSECURE_TRANSPORT'):
            # For local development (DEBUG=True), allow insecure OAuth redirect flow
            # if OAUTHLIB_INSECURE_TRANSPORT isn't already set
            os.environ['OAUTHLIB_INSECURE_TRANSPORT'] = "1"
            logger.info("Adding env var OAUTHLIB_INSECURE_TRANSPORT=1 to allow "
                        "OAuth redirect flow even though request is not secure")
        token = oauth2_session.fetch_token(
            token_url, client_secret=client_secret,
            authorization_response=authorization_code_url, verify=verify)
        userinfo = oauth2_session.get(userinfo_url).json()
        return token, userinfo

    def _get_token_and_userinfo_from_refresh_token(self,
                                                   request,
                                                   refresh_token=None):
        client_id = settings.KEYCLOAK_CLIENT_ID
        client_secret = settings.KEYCLOAK_CLIENT_SECRET
        token_url = settings.KEYCLOAK_TOKEN_URL
        userinfo_url = settings.KEYCLOAK_USERINFO_URL
        verify_ssl = settings.KEYCLOAK_VERIFY_SSL
        oauth2_session = OAuth2Session(client_id, scope='openid')
        verify = verify_ssl
        if verify_ssl and hasattr(settings, 'KEYCLOAK_CA_CERTFILE'):
            verify = settings.KEYCLOAK_CA_CERTFILE
        refresh_token_ = (refresh_token
                          if refresh_token is not None
                          else request.session['REFRESH_TOKEN'])
        # refresh_token doesn't take client_secret kwarg, so create auth
        # explicitly
        auth = requests.auth.HTTPBasicAuth(client_id, client_secret)
        try:
            token = oauth2_session.refresh_token(token_url=token_url,
                                                 refresh_token=refresh_token_,
                                                 auth=auth,
                                                 verify=verify)
            userinfo = oauth2_session.get(userinfo_url).json()
            return token, userinfo
        except InvalidGrantError as e:
            # probably session was terminated by admin or by user logging out in another client
            logger.warning(f"Failed to refresh token for user {request.user.username} "
                           f": {e}")
            return None, None

    def _get_userinfo_from_token(self, request, token):
        client_id = settings.KEYCLOAK_CLIENT_ID
        userinfo_url = settings.KEYCLOAK_USERINFO_URL
        verify_ssl = settings.KEYCLOAK_VERIFY_SSL
        oauth2_session = OAuth2Session(
            client_id, token={'access_token': token})
        verify = verify_ssl
        if verify_ssl and hasattr(settings, 'KEYCLOAK_CA_CERTFILE'):
            verify = settings.KEYCLOAK_CA_CERTFILE
        userinfo = oauth2_session.get(
            userinfo_url, verify=verify).json()
        if 'error' in userinfo:
            msg = userinfo.get('error_description')
            if msg is None:
                msg = f"Error fetching userinfo: {userinfo['error']}"
            raise Exception(msg)
        return userinfo

    def _process_token(self, request, token):
        # TODO validate the JWS signature
        logger.debug("token: {}".format(token))
        now = time.time()
        # Put access_token into session to be used for authenticating with API
        # server
        sess = request.session
        sess['ACCESS_TOKEN'] = token['access_token']
        sess['ACCESS_TOKEN_EXPIRES_AT'] = now + token['expires_in']
        sess['REFRESH_TOKEN'] = token['refresh_token']
        sess['REFRESH_TOKEN_EXPIRES_AT'] = now + token['refresh_expires_in']

    def _process_userinfo(self, request, userinfo):
        logger.debug("userinfo: {}".format(userinfo))
        sub = userinfo['sub']
        username = userinfo['preferred_username']
        email = userinfo.get('email', '')
        first_name = userinfo.get('given_name', None)
        last_name = userinfo.get('family_name', None)

        user = self._get_or_create_user(sub, username)
        user_profile = user.user_profile

        # Save the user info claims
        for (claim, value) in userinfo.items():
            if user_profile.userinfo_set.filter(claim=claim).exists():
                userinfo_claim = user_profile.userinfo_set.get(claim=claim)
                userinfo_claim.value = value
                userinfo_claim.save()
            else:
                user_profile.userinfo_set.create(claim=claim, value=value)

        # Update User model fields
        user = user_profile.user
        user.username = username
        user.email = email
        user.first_name = first_name
        user.last_name = last_name
        user.save()

        return user

    def _get_or_create_user(self, sub, username):

        try:
            user_profile = models.UserProfile.objects.get(
                userinfo__claim='sub', userinfo__value=sub)
            return user_profile.user
        except models.UserProfile.DoesNotExist:
            try:
                # For backwards compatibility, lookup by username
                user = User.objects.get(username=username)
                # Make sure there is a user_profile with the sub claim, which
                # will be used to do the lookup next time
                if not hasattr(user, 'user_profile'):
                    user_profile = models.UserProfile(user=user)
                    user_profile.save()
                    user_profile.userinfo_set.create(
                        claim='sub', value=sub)
                else:
                    userinfo = user.user_profile.userinfo_set.get(claim='sub')
                    logger.warning(
                        f"User {username} exists but sub claims don't match: "
                        f"old={userinfo.value}, new={sub}. Updating to new "
                        "sub claim.")
                    userinfo.value = sub
                    userinfo.save()
                return user
            except User.DoesNotExist:
                user = User(username=username)
                user.save()
                user_profile = models.UserProfile(user=user)
                user_profile.save()
                user_profile.userinfo_set.create(claim='sub', value=sub)
                return user

    def _store_idp_userinfo(self, user, token, idp_alias):
        try:
            idp_token_url = None
            userinfo_url = None
            for auth_option in settings.AUTHENTICATION_OPTIONS['external']:
                if auth_option['idp_alias'] == idp_alias:
                    idp_token_url = auth_option.get('idp_token_url')
                    userinfo_url = auth_option.get('userinfo_url')
                    break
            if idp_token_url is None or userinfo_url is None:
                logger.debug(f"idp_token_url and/or userinfo_url not set for {idp_alias} "
                             "in AUTHENTICATION_OPTIONS, skipping retrieval of external IDP userinfo")
                return
            access_token = token['access_token']
            logger.debug(f"access_token={access_token} for idp_alias={idp_alias}")
            # fetch the idp's token
            headers = {'Authorization': f'Bearer {access_token}'}
            # For the following to work, in Keycloak the IDP should have 'Store
            # Tokens' and 'Stored Tokens Readable' enabled and the user needs
            # the broker/read-token role
            r = requests.get(idp_token_url, headers=headers)
            idp_token = r.json()
            idp_headers = {'Authorization': f"Bearer {idp_token['access_token']}"}
            r = requests.get(userinfo_url, headers=idp_headers)
            userinfo = r.json()
            logger.debug(f"userinfo={userinfo}")

            # Save the idp user info claims
            user_profile = user.user_profile
            for (claim, value) in userinfo.items():
                if user_profile.idp_userinfo.filter(idp_alias=idp_alias, claim=claim).exists():
                    userinfo_claim = user_profile.idp_userinfo.get(idp_alias=idp_alias, claim=claim)
                    userinfo_claim.value = value
                    userinfo_claim.save()
                else:
                    user_profile.idp_userinfo.create(idp_alias=idp_alias, claim=claim, value=value)
        except Exception:
            logger.exception(f"Failed to store IDP userinfo for {user.username} from IDP {idp_alias}")

    def _check_username_initialization(self, request, user):
        # Check if the username assigned to the user was based on the user's
        # email address or if it was assigned some random string (Keycloak's
        # sub). If the latter, we'll want to alert the admins so that they can
        # assign a proper username for the user.
        user_profile = user.user_profile
        if (not user_profile.username_initialized and
            user_profile.userinfo_set.filter(claim='email').exists() and
            user_profile.userinfo_set.filter(claim='preferred_username').exists() and
                user_profile.userinfo_set.get(claim='email').value == user_profile.userinfo_set.get(claim='preferred_username').value):
            user_profile.username_initialized = True
            user_profile.save()

        # TODO: also check idp_userinfo.preferred_username if it exists

        if not user_profile.username_initialized and not user_profile.is_username_valid:
            try:
                utils.send_admin_alert_about_uninitialized_username(
                    request, user.username, user.email, user.first_name, user.last_name)
            except Exception:
                logger.exception(f"Failed to send alert about username being uninitialized: {user.username}")
