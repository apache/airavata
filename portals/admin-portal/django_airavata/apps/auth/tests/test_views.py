from unittest.mock import patch
from urllib.parse import urlencode

from airavata.model.user.ttypes import UserProfile
from django.contrib import messages
from django.contrib.auth.models import AnonymousUser
from django.contrib.messages.middleware import MessageMiddleware
from django.contrib.sessions.middleware import SessionMiddleware
from django.core import mail
from django.http import HttpResponseRedirect
from django.shortcuts import reverse
from django.test import RequestFactory, TestCase, override_settings

from django_airavata.apps.auth import models, views


class LoginViewTestCase(TestCase):

    def test_login_with_next_param(self):

        response = self.client.get('/auth/login', data={'next': '/some/url'})
        self.assertTrue('next' in response.context)
        self.assertEqual(response.context['next'], '/some/url')
        self.assertContains(
            response,
            '<input type="hidden" name="next" value="/some/url"/>')
        # Create account url should pass along the next param
        create_account_url = (reverse('django_airavata_auth:create_account') +
                              "?" +
                              urlencode({'next': '/some/url'}))
        self.assertContains(response, f'<a href="{create_account_url}">')


class HandleLoginViewTestCase(TestCase):

    def test_with_get_request(self):
        """Verify GET request redirects to login page."""
        response = self.client.get(
            reverse('django_airavata_auth:handle_login'))
        self.assertEqual(response.status_code, 302)
        self.assertEqual(
            response['Location'],
            reverse('django_airavata_auth:login'))


class CreateAccountViewTestCase(TestCase):

    def setUp(self):
        self.factory = RequestFactory()

    def test_create_account_with_next_param(self):
        create_account_url = (reverse('django_airavata_auth:create_account') +
                              "?" +
                              urlencode({'next': '/some/url'}))
        response = self.client.get(create_account_url)
        self.assertTrue('form' in response.context)
        form = response.context['form']
        self.assertEqual(form.initial['next'], "/some/url")

    # Need to mock the iam_admin_client twice because it is imported in .forms
    # and in .views
    @patch('django_airavata.apps.auth.forms.iam_admin_client')
    @patch('django_airavata.apps.auth.views.iam_admin_client')
    def test_submit_create_account_with_next(self, forms_iam_admin_client,
                                             views_iam_admin_client):

        # make sure there are no EmailVerification records at the beginning
        self.assertEqual(0, models.EmailVerification.objects.all().count())
        create_account_url = reverse('django_airavata_auth:create_account')
        data = {
            'username': 'testuser',
            'email': 'test@example.com',
            'email_again': 'test@example.com',
            'first_name': 'Test',
            'last_name': 'User',
            'password': 'passworD1#',
            'password_again': 'passworD1#',
            'next': '/next/path'
        }
        request = self.factory.post(create_account_url, data)
        request.user = AnonymousUser()
        forms_iam_admin_client.is_username_available.return_value = True
        views_iam_admin_client.register_user.return_value = True
        # RequestFactory doesn't load middleware so have to manually call
        # SessionMiddleware and MessageMiddleware since create_account uses
        # 'messages' framework
        response = SessionMiddleware(
            MessageMiddleware(
                views.create_account))(request)
        self.assertEqual(302, response.status_code)
        self.assertIsInstance(response, HttpResponseRedirect)
        self.assertEqual(reverse('django_airavata_auth:create_account'),
                         response.url)
        self.assertEqual(len(mail.outbox), 1)
        email_verification = models.EmailVerification.objects.get(
            username=data['username'])
        self.assertIsNotNone(email_verification)
        self.assertEqual(data['next'], email_verification.next)

    @patch('django_airavata.apps.auth.forms.iam_admin_client')
    def test_submit_invalid_create_account_with_next(
            self, forms_iam_admin_client):

        # make sure there are no EmailVerification records at the beginning
        self.assertEqual(0, models.EmailVerification.objects.all().count())
        create_account_url = reverse('django_airavata_auth:create_account')
        data = {
            'username': 'testuser',
            'email': 'test@example.com',
            'email_again': 'test@example.com',
            'first_name': 'Test',
            'last_name': 'User',
            'password': 'passworD1#',
            # make invalid by having the password_again not match
            'password_again': 'DOESNT-MATCH',
            'next': '/next/path'
        }
        request = self.factory.post(create_account_url, data)
        request.user = AnonymousUser()
        forms_iam_admin_client.is_username_available.return_value = True
        # RequestFactory doesn't load middleware so have to manually call
        # SessionMiddleware and MessageMiddleware since create_account uses
        # 'messages' framework
        response = SessionMiddleware(
            MessageMiddleware(
                views.create_account))(request)
        self.assertEqual(200, response.status_code)
        self.assertNotIsInstance(response, HttpResponseRedirect)
        # No email is sent
        self.assertEqual(len(mail.outbox), 0)
        # Make sure that the 'next' field was put back into the form
        self.assertRegex(
            response.content.decode("utf-8"),
            '(?s)type="hidden"[^>]+name="next"[^>]+value="/next/path"')


GATEWAY_ID = "gateway-id"
PORTAL_TITLE = "Airavata Django Portal"
SERVER_EMAIL = "admin@gateway.org"


@override_settings(
    GATEWAY_ID=GATEWAY_ID,
    PORTAL_TITLE=PORTAL_TITLE,
    SERVER_EMAIL=SERVER_EMAIL,
    PORTAL_ADMINS=[('Gateway Admin', 'admin@gateway.org')]
)
class VerifyEmailViewTestCase(TestCase):

    def setUp(self):
        self.factory = RequestFactory()

    @patch('django_airavata.apps.auth.views.iam_admin_client')
    def test_verify_email(self, views_iam_admin_client):

        # create an EmailVerification record with a next url
        email_verification = models.EmailVerification(username='testuser')
        email_verification.save()

        verify_email_url = reverse(
            'django_airavata_auth:verify_email', kwargs={
                'code': email_verification.verification_code})
        request = self.factory.get(verify_email_url)
        request.user = AnonymousUser()
        # Skip enabling the user
        views_iam_admin_client.is_user_enabled.return_value = True
        # RequestFactory doesn't load middleware so have to manually call
        # SessionMiddleware and MessageMiddleware since create_account uses
        # 'messages' framework
        response = SessionMiddleware(MessageMiddleware(
            lambda r: views.verify_email(r,
                                         email_verification.verification_code)
        ))(request)
        self.assertIsInstance(response, HttpResponseRedirect)
        self.assertEqual(reverse('django_airavata_auth:login'), response.url)
        email_verification = models.EmailVerification.objects.get(
            username="testuser")
        self.assertTrue(email_verification.verified)

    @patch('django_airavata.apps.auth.views.iam_admin_client')
    def test_verify_email_with_next(self, views_iam_admin_client):

        # create an EmailVerification record with a next url
        email_verification = models.EmailVerification(
            username='testuser', next='/next/path')
        email_verification.save()

        verify_email_url = reverse(
            'django_airavata_auth:verify_email', kwargs={
                'code': email_verification.verification_code})
        request = self.factory.get(verify_email_url)
        request.user = AnonymousUser()
        # Skip enabling the user
        views_iam_admin_client.is_user_enabled.return_value = True
        # RequestFactory doesn't load middleware so have to manually call
        # SessionMiddleware and MessageMiddleware since create_account uses
        # 'messages' framework
        response = SessionMiddleware(MessageMiddleware(
            lambda r: views.verify_email(r,
                                         email_verification.verification_code)
        ))(request)
        self.assertIsInstance(response, HttpResponseRedirect)
        self.assertEqual(reverse('django_airavata_auth:login') + "?" +
                         urlencode({'next': email_verification.next}),
                         response.url)
        email_verification = models.EmailVerification.objects.get(
            username="testuser")
        self.assertTrue(email_verification.verified)

    @patch('django_airavata.apps.auth.views.iam_admin_client')
    def test_verify_email_with_new_user_email(self, views_iam_admin_client):

        # create an EmailVerification record
        email_verification = models.EmailVerification(username='testuser')
        email_verification.save()

        verify_email_url = reverse(
            'django_airavata_auth:verify_email', kwargs={
                'code': email_verification.verification_code})
        request = self.factory.get(verify_email_url)
        request.user = AnonymousUser()
        # Mock using the iam_admin_client to enable the user
        views_iam_admin_client.is_user_enabled.return_value = False
        views_iam_admin_client.enable_user.return_value = True
        user_profile = UserProfile(
            airavataInternalUserId=f"testuser@{GATEWAY_ID}",
            userId="testuser",
            firstName="Test",
            lastName="User1",
            emails=["testuser1@example.com"]
        )
        views_iam_admin_client.get_user.return_value = user_profile
        # RequestFactory doesn't load middleware so have to manually call
        # SessionMiddleware and MessageMiddleware since create_account uses
        # 'messages' framework
        response = SessionMiddleware(MessageMiddleware(
            lambda r: views.verify_email(r,
                                         email_verification.verification_code)
        ))(request)
        self.assertIsInstance(response, HttpResponseRedirect)
        self.assertEqual(reverse('django_airavata_auth:login'), response.url)
        email_verification = models.EmailVerification.objects.get(
            username="testuser")
        self.assertTrue(email_verification.verified)
        self.assertEqual(len(mail.outbox), 1)
        # Make sure from email address is formatted correctly
        self.assertEqual(mail.outbox[0].from_email, f'"{PORTAL_TITLE}" <{SERVER_EMAIL}>')
        self.assertEqual(len(mail.outbox[0].to), 1)
        self.assertEqual(mail.outbox[0].to[0], '"Gateway Admin" <admin@gateway.org>')


@override_settings(
    GATEWAY_ID=GATEWAY_ID,
    PORTAL_TITLE=PORTAL_TITLE,
    SERVER_EMAIL=SERVER_EMAIL,
    PORTAL_ADMINS=[('Gateway Admin', 'admin@gateway.org')]
)
class ResendEmailLinkTestCase(TestCase):

    def setUp(self):
        self.factory = RequestFactory()

    @patch('django_airavata.apps.auth.views.iam_admin_client')
    def test_resend_email_link(self, views_iam_admin_client):
        data = {
            'username': 'testuser',
        }
        request = self.factory.post(reverse('django_airavata_auth:resend_email_link'), data)
        request.user = AnonymousUser()

        views_iam_admin_client.is_user_exist.return_value = True
        user_profile = UserProfile(
            airavataInternalUserId=f"testuser@{GATEWAY_ID}",
            userId="testuser",
            firstName="Test",
            lastName="User1",
            emails=["testuser1@example.com"]
        )
        views_iam_admin_client.get_user.return_value = user_profile

        # RequestFactory doesn't load middleware so have to manually call
        # SessionMiddleware and MessageMiddleware since create_account uses
        # 'messages' framework
        response = SessionMiddleware(MessageMiddleware(
            lambda r: views.resend_email_link(r)
        ))(request)

        email_verification = models.EmailVerification.objects.get(
            username="testuser")
        self.assertFalse(email_verification.verified)
        self.assertEqual(len(mail.outbox), 1)

        self.assertIsInstance(response, HttpResponseRedirect)
        self.assertEqual(reverse('django_airavata_auth:resend_email_link'), response.url)

        self.assertEqual(len(messages.get_messages(request)), 1)
        # get the first/only message
        for message in messages.get_messages(request):
            pass
        self.assertIn('Email verification link sent successfully', str(message))
