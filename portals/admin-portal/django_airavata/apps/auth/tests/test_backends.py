from unittest.mock import MagicMock, patch

from django.contrib.auth.models import AnonymousUser
from django.core import mail
from django.test import RequestFactory, TestCase, override_settings

from django_airavata.apps.auth import backends

KEYCLOAK_CLIENT_ID = "kc-client"
KEYCLOAK_CLIENT_SECRET = "kc-secret"
KEYCLOAK_TOKEN_URL = "https://example.org/auth"
KEYCLOAK_USERINFO_URL = "https://example.org/userinfo"
KEYCLOAK_VERIFY_SSL = True
AUTHENTICATION_OPTIONS = {
    'external': [
        {
            'idp_alias': 'oidc',
            'name': 'Some OIDC compliant IDP',
        }
    ]
}
GATEWAY_ID = "gateway-id"


@override_settings(
    KEYCLOAK_CLIENT_ID=KEYCLOAK_CLIENT_ID,
    KEYCLOAK_CLIENT_SECRET=KEYCLOAK_CLIENT_SECRET,
    KEYCLOAK_TOKEN_URL=KEYCLOAK_TOKEN_URL,
    KEYCLOAK_USERINFO_URL=KEYCLOAK_USERINFO_URL,
    KEYCLOAK_VERIFY_SSL=KEYCLOAK_VERIFY_SSL,
    AUTHENTICATION_OPTIONS=AUTHENTICATION_OPTIONS,
    GATEWAY_ID=GATEWAY_ID,
    PORTAL_ADMINS=[('Admin Name', 'admin@example.org')],
)
class KeycloakBackendTestCase(TestCase):

    def setUp(self):
        self.factory = RequestFactory()

    @patch("django_airavata.apps.auth.backends.OAuth2Session")
    def test_username_initialized_with_email(self, MockOAuth2Session):
        """Test that username_initialized is set to True when username equals email address."""

        # Tests scenario that new user logs in via external IDP and when they
        # are assigned a username it is the same as their email address. This is
        # normally what happens and is a good outcome for the username so
        # username_initialized should be set to True and no alert email should
        # be sent to admins.

        # Mock out request for redirect flow, and OAuth2Session: token and userinfo
        request = self.factory.get("/callback?code=abc123", secure=True)
        request.user = AnonymousUser()
        request.session = {
            'OAUTH2_STATE': 'state',
            'OAUTH2_REDIRECT_URI': 'redirect-uri',
        }
        mock_oauth2_session = MagicMock()
        MockOAuth2Session.return_value = mock_oauth2_session
        mock_oauth2_session.fetch_token.return_value = {
            'access_token': 'the-access-token',
            'expires_in': 900,
            'refresh_token': 'the-refresh-token',
            'refresh_expires_in': 86400,
        }
        mock_userinfo = MagicMock()
        mock_oauth2_session.get.return_value = mock_userinfo
        email = 'testuser@example.org'
        mock_userinfo.json.return_value = {
            'sub': 'sub-123',
            'preferred_username': email,
            'email': email,
            'given_name': 'Test',
            'family_name': 'User',
        }

        # Mock out fetching IDP userinfo: AUTHENTICATION_OPTIONS: request to
        # idp_token_url and userinfo_url

        backend = backends.KeycloakBackend()
        idp_alias = "oidc"
        user = backend.authenticate(request, idp_alias=idp_alias)

        self.assertTrue(user.user_profile.username_initialized)
        self.assertEqual(0, len(mail.outbox))

    @patch("django_airavata.apps.auth.backends.OAuth2Session")
    def test_username_initialized_with_no_email(self, MockOAuth2Session):
        """Test that username_initialized is set to False when there is no email address."""

        # Tests scenario that new user logs in via external IDP and when they
        # are assigned a username but don't have an email address. This usually
        # means that they also have a randomly generated username and admins
        # need to be alerted.

        # Mock out request for redirect flow, and OAuth2Session: token and userinfo
        request = self.factory.get("/callback?code=abc123", secure=True)
        request.user = AnonymousUser()
        request.session = {
            'OAUTH2_STATE': 'state',
            'OAUTH2_REDIRECT_URI': 'redirect-uri',
        }
        mock_oauth2_session = MagicMock()
        MockOAuth2Session.return_value = mock_oauth2_session
        mock_oauth2_session.fetch_token.return_value = {
            'access_token': 'the-access-token',
            'expires_in': 900,
            'refresh_token': 'the-refresh-token',
            'refresh_expires_in': 86400,
        }
        mock_userinfo = MagicMock()
        mock_oauth2_session.get.return_value = mock_userinfo
        # email = 'testuser@example.org'
        mock_userinfo.json.return_value = {
            'sub': 'sub-123',
            'preferred_username': 'some-random-&invalid//-username',
            # 'email': email,
            'given_name': 'Test',
            'family_name': 'User',
        }

        # Mock out fetching IDP userinfo: AUTHENTICATION_OPTIONS: request to
        # idp_token_url and userinfo_url

        backend = backends.KeycloakBackend()
        idp_alias = "oidc"
        user = backend.authenticate(request, idp_alias=idp_alias)

        self.assertFalse(user.user_profile.userinfo_set.filter(claim='email').exists())
        self.assertFalse(user.user_profile.username_initialized)
        self.assertEqual(1, len(mail.outbox))
        self.assertTrue(mail.outbox[0].subject.startswith("Please fix username"))

    @patch("django_airavata.apps.auth.backends.OAuth2Session")
    def test_username_initialized_with_email_not_username(self, MockOAuth2Session):
        """Test that username_initialized is set to False when email address is different from username."""

        # Tests scenario that new user logs in via external IDP and when they
        # are assigned a username but it's different from their email address.
        # This usually means that they also have a randomly generated username
        # and admins need to be alerted.

        # Mock out request for redirect flow, and OAuth2Session: token and userinfo
        request = self.factory.get("/callback?code=abc123", secure=True)
        request.user = AnonymousUser()
        request.session = {
            'OAUTH2_STATE': 'state',
            'OAUTH2_REDIRECT_URI': 'redirect-uri',
        }
        mock_oauth2_session = MagicMock()
        MockOAuth2Session.return_value = mock_oauth2_session
        mock_oauth2_session.fetch_token.return_value = {
            'access_token': 'the-access-token',
            'expires_in': 900,
            'refresh_token': 'the-refresh-token',
            'refresh_expires_in': 86400,
        }
        mock_userinfo = MagicMock()
        mock_oauth2_session.get.return_value = mock_userinfo
        email = 'testuser@example.org'
        mock_userinfo.json.return_value = {
            'sub': 'sub-123',
            'preferred_username': 'some-random-&invalid//-username',
            'email': email,
            'given_name': 'Test',
            'family_name': 'User',
        }

        # Mock out fetching IDP userinfo: AUTHENTICATION_OPTIONS: request to
        # idp_token_url and userinfo_url

        backend = backends.KeycloakBackend()
        idp_alias = "oidc"
        user = backend.authenticate(request, idp_alias=idp_alias)

        self.assertTrue(user.user_profile.userinfo_set.filter(claim='email').exists())
        self.assertFalse(user.user_profile.username_initialized)
        self.assertEqual(1, len(mail.outbox))
        self.assertTrue(mail.outbox[0].subject.startswith("Please fix username"))
