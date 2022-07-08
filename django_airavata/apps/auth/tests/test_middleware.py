
from unittest.mock import MagicMock, sentinel

from django.contrib.auth import get_user_model
from django.contrib.auth.models import AnonymousUser
from django.http import HttpResponseRedirect
from django.test import RequestFactory, TestCase
from django.urls import reverse

from django_airavata.apps.auth import models
from django_airavata.apps.auth.middleware import (
    user_profile_completeness_check
)


class UserProfileCompletenessCheckTestCase(TestCase):

    def setUp(self):
        User = get_user_model()
        self.user: User = User.objects.create_user("testuser")
        self.user_profile: models.UserProfile = models.UserProfile.objects.create(user=self.user)
        self.factory = RequestFactory()

    def _middleware_passes_through(self, request):
        get_response = MagicMock(return_value=sentinel.response)
        response = user_profile_completeness_check(get_response)(request)
        get_response.assert_called()
        self.assertIs(response, sentinel.response)

    def _middleware_redirects_to_user_profile(self, request):
        get_response = MagicMock(return_value=sentinel.response)
        response = user_profile_completeness_check(get_response)(request)
        get_response.assert_not_called()
        self.assertIsInstance(response, HttpResponseRedirect)
        self.assertEqual(response.url, reverse('django_airavata_auth:user_profile'))

    def test_not_authenticated(self):
        """Test that completeness check is skipped when not authenticated."""
        request = self.factory.get(reverse('django_airavata_workspace:dashboard'))
        request.user = AnonymousUser()
        self.assertFalse(request.user.is_authenticated)
        self._middleware_passes_through(request)

    def test_user_profile_is_incomplete(self):
        """Test user profile incomplete, should redirect to user_profile view."""
        request = self.factory.get(reverse('django_airavata_workspace:dashboard'), HTTP_ACCEPT=['text/html'])
        request.user = self.user
        self.assertTrue(request.user.is_authenticated)
        self.assertFalse(self.user_profile.is_complete)
        self._middleware_redirects_to_user_profile(request)

    def test_user_profile_is_incomplete_but_allowed(self):
        """Test user profile incomplete, but should be able to access user_profile."""
        request = self.factory.get(reverse('django_airavata_auth:user_profile'), HTTP_ACCEPT=['text/html'])
        request.user = self.user
        self.assertTrue(request.user.is_authenticated)
        self.assertFalse(self.user_profile.is_complete)
        self._middleware_passes_through(request)

    def test_user_profile_is_complete(self):
        """Test user profile is complete, should pass through."""
        request = self.factory.get(reverse('django_airavata_workspace:dashboard'), HTTP_ACCEPT=['text/html'])
        request.user = self.user
        self.user.first_name = "Test"
        self.user.last_name = "User"
        self.user.email = "testuser@gateway.edu"
        self.assertTrue(request.user.is_authenticated)
        self.assertTrue(self.user_profile.is_complete)
        self.assertTrue(self.user_profile.is_ext_user_profile_valid)
        self._middleware_passes_through(request)

    def test_user_profile_is_complete_but_ext_up_is_invalid(self):
        """Test user profile is complete, but ext user prof is invalid."""
        request = self.factory.get(reverse('django_airavata_workspace:dashboard'), HTTP_ACCEPT=['text/html'])
        request.user = self.user
        self.user.first_name = "Test"
        self.user.last_name = "User"
        self.user.email = "testuser@gateway.edu"
        models.ExtendedUserProfileTextField.objects.create(name="test1", order=1, required=True)
        self.assertTrue(request.user.is_authenticated)
        self.assertTrue(self.user_profile.is_complete)
        self.assertFalse(self.user_profile.is_ext_user_profile_valid)
        self._middleware_redirects_to_user_profile(request)

    def test_user_profile_is_complete_and_ext_up_is_valid(self):
        """Test user profile is complete and ext user prof is valid."""
        request = self.factory.get(reverse('django_airavata_workspace:dashboard'), HTTP_ACCEPT=['text/html'])
        request.user = self.user
        self.user.first_name = "Test"
        self.user.last_name = "User"
        self.user.email = "testuser@gateway.edu"
        field1 = models.ExtendedUserProfileTextField.objects.create(name="test1", order=1, required=True)
        models.ExtendedUserProfileTextValue.objects.create(
            ext_user_profile_field=field1, user_profile=self.user_profile,
            text_value="Answer #1"
        )
        self.assertTrue(self.user_profile.is_ext_user_profile_valid)
        self.assertTrue(request.user.is_authenticated)
        self.assertTrue(self.user_profile.is_complete)
        self.assertEqual(1, len(self.user_profile.extended_profile_values.all()))
        self._middleware_passes_through(request)

    def test_user_profile_is_complete_ext_up_is_invalid_but_user_is_admin(self):
        """Test user profile is complete, ext user prof is invalid, but user is gateway admin."""
        request = self.factory.get(reverse('django_airavata_workspace:dashboard'), HTTP_ACCEPT=['text/html'])
        request.user = self.user
        request.is_gateway_admin = True
        self.user.first_name = "Admin"
        self.user.last_name = "User"
        self.user.email = "admin@gateway.edu"
        models.ExtendedUserProfileTextField.objects.create(name="test1", order=1, required=True)
        self.assertFalse(self.user_profile.is_ext_user_profile_valid)
        self.assertTrue(request.user.is_authenticated)
        self.assertTrue(self.user_profile.is_complete)
        self._middleware_passes_through(request)

    def test_user_profile_is_incomplete_but_logout_allowed(self):
        """Test user profile incomplete, but should be able to access logout."""
        request = self.factory.get(reverse('django_airavata_auth:logout'), HTTP_ACCEPT=['text/html'])
        request.user = self.user
        self.assertTrue(request.user.is_authenticated)
        self.assertFalse(self.user_profile.is_complete)
        self._middleware_passes_through(request)
