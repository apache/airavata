import io
import logging
import time
from datetime import datetime, timedelta, timezone
from urllib.parse import quote, urlencode, urlparse

import requests
from django.conf import settings
from django.contrib import messages
from django.contrib.auth import authenticate, get_user_model, login, logout
from django.contrib.auth.decorators import login_required
from django.core.exceptions import ObjectDoesNotExist, PermissionDenied
from django.db.transaction import atomic
from django.forms import ValidationError
from django.http import (
    FileResponse,
    HttpResponseBadRequest,
    HttpResponseForbidden,
    JsonResponse
)
from django.shortcuts import redirect, render, resolve_url
from django.template import Context
from django.template.loader import render_to_string
from django.urls import reverse
from django.views.decorators.debug import sensitive_variables
from requests_oauthlib import OAuth2Session
from rest_framework import mixins, permissions, viewsets
from rest_framework.decorators import action
from rest_framework.response import Response

from django_airavata.apps.api.view_utils import (
    IsInAdminsGroupPermission,
    ReadOnly
)
from django_airavata.apps.auth import serializers

from . import forms, iam_admin_client, models, utils

logger = logging.getLogger(__name__)


def start_login(request):
    next_url = request.GET.get('next', None)
    if next_url is not None:
        create_account_url = (reverse('django_airavata_auth:create_account') +
                              "?" + urlencode({'next': next_url}))
    else:
        create_account_url = reverse('django_airavata_auth:create_account')
    return render(request, 'django_airavata_auth/login.html', {
        'next': request.GET.get('next', None),
        'options': settings.AUTHENTICATION_OPTIONS,
        'create_account_url': create_account_url
    })


def start_username_password_login(request):
    # return bad request if password isn't a configured option
    if 'password' not in settings.AUTHENTICATION_OPTIONS:
        return HttpResponseBadRequest("Username/password login is not enabled")
    return render(request,
                  'django_airavata_auth/login_username_password.html',
                  {
                      'next': request.GET.get('next', None),
                      'options': settings.AUTHENTICATION_OPTIONS,
                      'login_type': 'password'
                  })


def redirect_login(request, idp_alias):
    _validate_idp_alias(idp_alias)
    client_id = settings.KEYCLOAK_CLIENT_ID
    base_authorize_url = settings.KEYCLOAK_AUTHORIZE_URL
    redirect_uri = request.build_absolute_uri(
        reverse('django_airavata_auth:callback'))
    redirect_uri += '?idp_alias=' + quote(idp_alias)
    passthrough_query_params = ('next', 'login_desktop', 'download-code', 'show-code')
    for passthrough_query_param in passthrough_query_params:
        if passthrough_query_param in request.GET:
            redirect_uri += f"&{passthrough_query_param}={quote(request.GET[passthrough_query_param])}"
    oauth2_session = OAuth2Session(
        client_id, scope='openid', redirect_uri=redirect_uri)
    authorization_url, state = oauth2_session.authorization_url(
        base_authorize_url)
    authorization_url += '&kc_idp_hint=' + quote(idp_alias)
    # Store state in session for later validation (see backends.py)
    request.session['OAUTH2_STATE'] = state
    request.session['OAUTH2_REDIRECT_URI'] = redirect_uri
    return redirect(authorization_url)


def _validate_idp_alias(idp_alias):
    external_auth_options = settings.AUTHENTICATION_OPTIONS['external']
    valid_idp_aliases = [ext['idp_alias'] for ext in external_auth_options]
    if idp_alias not in valid_idp_aliases:
        raise Exception("idp_alias is not valid")


@sensitive_variables('password')
def handle_login(request):
    # This view handles a POST of the login form. If the request is a GET, just
    # redirect to the login page.
    if request.method == 'GET':
        return redirect(reverse('django_airavata_auth:login'))
    username = request.POST['username']
    password = request.POST['password']
    login_type = request.POST.get('login_type', None)
    login_desktop = request.POST.get('login_desktop', "false") == "true"
    download_code = request.POST.get('download-code', 'false') == "true"
    show_code = request.POST.get('show-code', 'false') == "true"
    template = "django_airavata_auth/login.html"
    if login_type and login_type == 'password':
        template = "django_airavata_auth/login_username_password.html"
    user = authenticate(username=username, password=password, request=request)
    logger.debug("authenticated user: {}".format(user))
    try:
        if user is not None:
            # Middleware will add authz_token attr to request, but since user
            # just authenticated, authz_token won't be added yet. Login signals
            # need the authz_token so adding it to the request now.
            request.authz_token = utils.get_authz_token(request, user=user)
            login(request, user)
            if login_desktop:
                return _create_login_desktop_success_response(request,
                                                              download_code=download_code,
                                                              show_code=show_code)
            else:
                next_url = request.POST.get('next',
                                            settings.LOGIN_REDIRECT_URL)
                return redirect(next_url)
        else:
            messages.error(request, "Login failed. Please try again.")
    except Exception as err:
        logger.exception("Login failed for user {}".format(username), extra={'request': request})
        messages.error(request,
                       "Login failed: {}. Please try again.".format(str(err)))
    if login_desktop:
        return _create_login_desktop_failed_response(request)
    return render(request, template, {
        'username': username,
        'next': request.POST.get('next', None),
        'options': settings.AUTHENTICATION_OPTIONS,
        'login_type': login_type,
    })


def start_logout(request):
    logout(request)
    redirect_url = request.build_absolute_uri(
        resolve_url(settings.LOGOUT_REDIRECT_URL))
    return redirect(settings.KEYCLOAK_LOGOUT_URL +
                    "?redirect_uri=" + quote(redirect_url))


def callback(request):
    try:
        login_desktop = request.GET.get('login_desktop', "false") == "true"
        idp_alias = request.GET.get('idp_alias')
        user = authenticate(request=request, idp_alias=idp_alias)

        if user is not None:
            login(request, user)
            if login_desktop:
                download_code = request.GET.get('download-code', 'false') == "true"
                show_code = request.GET.get('show-code', 'false') == "true"
                return _create_login_desktop_success_response(request, download_code=download_code, show_code=show_code)
            next_url = request.GET.get('next', settings.LOGIN_REDIRECT_URL)
            return redirect(next_url)
        else:
            raise Exception("Failed to authenticate user")
    except Exception as err:
        logger.exception("An error occurred while processing OAuth2 "
                         "callback: {}".format(request.build_absolute_uri()),
                         extra={'request': request})
        messages.error(
            request,
            "Failed to process OAuth2 callback: {}".format(str(err)))
        if login_desktop:
            return _create_login_desktop_failed_response(
                request, idp_alias=idp_alias)
        return redirect(reverse('django_airavata_auth:callback-error',
                                args=(idp_alias,)))


def callback_error(request, idp_alias):
    _validate_idp_alias(idp_alias)
    # Create a filtered options object with just the given idp_alias
    options = {
        'external': []
    }
    for ext in settings.AUTHENTICATION_OPTIONS['external']:
        if ext['idp_alias'] == idp_alias:
            options['external'].append(ext.copy())

    return render(request, 'django_airavata_auth/callback-error.html', {
        'idp_alias': idp_alias,
        'options': options,
    })


@sensitive_variables('password')
def create_account(request):
    if request.method == 'POST':
        form = forms.CreateAccountForm(request.POST)
        if form.is_valid():
            try:
                username = form.cleaned_data['username']
                email = form.cleaned_data['email']
                first_name = form.cleaned_data['first_name']
                last_name = form.cleaned_data['last_name']
                password = form.cleaned_data['password']
                success = iam_admin_client.register_user(
                    username, email, first_name, last_name, password)
                if not success:
                    form.add_error(None, ValidationError(
                        "Failed to register user with IAM service"))
                else:
                    next = form.cleaned_data['next']
                    _create_and_send_email_verification_link(
                        request, username, email, first_name, last_name, next)
                    messages.success(
                        request,
                        "Account request processed successfully. Before you "
                        "can login you need to confirm your email address. "
                        "We've sent you an email with a link that you should "
                        "click on to complete the account creation process.")
                    return redirect(
                        reverse('django_airavata_auth:create_account'))
            except Exception as e:
                logger.exception(
                    "Failed to create account for user", exc_info=e, extra={'request', request})
                form.add_error(None, ValidationError(e.message))
    else:
        form = forms.CreateAccountForm(initial=request.GET)
    return render(request, 'django_airavata_auth/create_account.html', {
        'options': settings.AUTHENTICATION_OPTIONS,
        'form': form
    })


def verify_email(request, code):

    try:
        email_verification = models.EmailVerification.objects.get(
            verification_code=code)
        email_verification.verified = True
        email_verification.save()
        # Check if user is enabled, if so redirect to login page
        username = email_verification.username
        logger.debug("Email address verified for {}".format(username))
        login_url = reverse('django_airavata_auth:login')
        if email_verification.next:
            login_url += "?" + urlencode({'next': email_verification.next})
        if iam_admin_client.is_user_enabled(username):
            logger.debug("User {} is already enabled".format(username))
            messages.success(
                request,
                "Your account has already been successfully created. "
                "Please log in now.")
            return redirect(login_url)
        else:
            logger.debug("Enabling user {}".format(username))
            # enable user and inform admins
            iam_admin_client.enable_user(username)
            user_profile = iam_admin_client.get_user(username)
            email_address = user_profile.emails[0]
            first_name = user_profile.firstName
            last_name = user_profile.lastName
            utils.send_new_user_email(request,
                                      username,
                                      email_address,
                                      first_name,
                                      last_name)
            messages.success(
                request,
                "Your account has been successfully created. "
                "Please log in now.")
            return redirect(login_url)
    except ObjectDoesNotExist:
        # if doesn't exist, give user a form where they can enter their
        # username to resend verification code
        logger.exception("EmailVerification object doesn't exist for "
                         "code {}".format(code), extra={'request': request})
        messages.error(
            request,
            "Email verification failed. Please enter your username and we "
            "will send you another email verification link.")
        return redirect(reverse('django_airavata_auth:resend_email_link'))
    except Exception:
        logger.exception("Email verification processing failed!", extra={'request': request})
        messages.error(
            request,
            "Email verification failed. Please try clicking the email "
            "verification link again later.")
        return redirect(reverse('django_airavata_auth:create_account'))


def resend_email_link(request):

    if request.method == 'POST':
        form = forms.ResendEmailVerificationLinkForm(request.POST)
        if form.is_valid():
            try:
                username = form.cleaned_data['username']
                if iam_admin_client.is_user_exist(username):
                    user_profile = iam_admin_client.get_user(username)
                    email_address = user_profile.emails[0]
                    _create_and_send_email_verification_link(
                        request,
                        username,
                        email_address,
                        user_profile.firstName,
                        user_profile.lastName)
                    messages.success(
                        request,
                        "Email verification link sent successfully. Please "
                        "click on the link in the email that we sent "
                        "to your email address.")
                else:
                    messages.error(
                        request,
                        "Unable to resend email verification link. Please "
                        "contact the website administrator for further "
                        "assistance.")
                return redirect(
                    reverse('django_airavata_auth:resend_email_link'))
            except Exception as e:
                logger.exception(
                    "Failed to resend email verification link", exc_info=e, extra={'request': request})
                form.add_error(None, ValidationError(str(e)))
    else:
        form = forms.ResendEmailVerificationLinkForm()
    return render(request, 'django_airavata_auth/verify_email.html', {
        'form': form
    })


def _create_and_send_email_verification_link(
        request, username, email, first_name, last_name, next=None):

    email_verification = models.EmailVerification(
        username=username, next=next)
    email_verification.save()

    verification_uri = request.build_absolute_uri(
        reverse(
            'django_airavata_auth:verify_email', kwargs={
                'code': email_verification.verification_code}))
    logger.debug(
        "verification_uri={}".format(verification_uri))

    context = Context({
        "username": username,
        "email": email,
        "first_name": first_name,
        "last_name": last_name,
        "portal_title": settings.PORTAL_TITLE,
        "url": verification_uri,
    })
    utils.send_email_to_user(models.VERIFY_EMAIL_TEMPLATE, context)


def forgot_password(request):
    if request.method == 'POST':
        form = forms.ForgotPasswordForm(request.POST)
        if form.is_valid():
            try:
                username = form.cleaned_data['username']
                user_exists = iam_admin_client.is_user_exist(username)
                if user_exists:
                    user_enabled = iam_admin_client.is_user_enabled(username)
                    if not user_enabled:
                        messages.error(
                            request,
                            "Please finish creating your account before "
                            "resetting your password. Provide your username "
                            "below and we will send you another email "
                            "verification link.")
                        return redirect(
                            reverse('django_airavata_auth:resend_email_link'))
                    _create_and_send_password_reset_request_link(
                        request, username)
                # Always display this message even if you doesn't exist. Don't
                # reveal whether a user with that username exists.
                messages.success(
                    request,
                    "Reset password request processed successfully. We've "
                    "sent an email with a password reset link to the email "
                    "address associated with the username you provided. You "
                    "can use that link within the next 24 hours to set a new "
                    "password.")
                return redirect(
                    reverse('django_airavata_auth:forgot_password'))
            except Exception as e:
                logger.exception(
                    "Failed to generate password reset request for user",
                    exc_info=e, extra={'request': request})
                form.add_error(None, ValidationError(str(e)))
    else:
        form = forms.ForgotPasswordForm()
    return render(request, 'django_airavata_auth/forgot_password.html', {
        'form': form
    })


def _create_and_send_password_reset_request_link(request, username):
    password_reset_request = models.PasswordResetRequest(username=username)
    password_reset_request.save()

    verification_uri = request.build_absolute_uri(
        reverse(
            'django_airavata_auth:reset_password', kwargs={
                'code': password_reset_request.reset_code}))
    logger.debug(
        "password reset verification_uri={}".format(verification_uri))

    user = iam_admin_client.get_user(username)
    context = Context({
        "username": username,
        "email": user.emails[0],
        "first_name": user.firstName,
        "last_name": user.lastName,
        "portal_title": settings.PORTAL_TITLE,
        "url": verification_uri,
    })
    utils.send_email_to_user(models.PASSWORD_RESET_EMAIL_TEMPLATE, context)


@sensitive_variables('password')
def reset_password(request, code):
    try:
        password_reset_request = models.PasswordResetRequest.objects.get(
            reset_code=code)
    except ObjectDoesNotExist:
        messages.error(
            request,
            "Reset password link is invalid. Please try again.")
        return redirect(reverse('django_airavata_auth:forgot_password'))

    now = datetime.now(timezone.utc)
    if now - password_reset_request.created_date > timedelta(days=1):
        password_reset_request.delete()
        messages.error(
            request,
            "Reset password link has expired. Please try again.")
        return redirect(reverse('django_airavata_auth:forgot_password'))

    if request.method == "POST":
        form = forms.ResetPasswordForm(request.POST)
        if form.is_valid():
            try:
                password = form.cleaned_data['password']
                success = iam_admin_client.reset_user_password(
                    password_reset_request.username, password)
                if not success:
                    messages.error(
                        request, "Failed to reset password. Please try again.")
                    return redirect(
                        reverse('django_airavata_auth:forgot_password'))
                else:
                    password_reset_request.delete()
                    messages.success(
                        request,
                        "You may now log in with your new password.")
                    return redirect(
                        reverse('django_airavata_auth:login_with_password'))
            except Exception as e:
                logger.exception(
                    "Failed to reset password for user", exc_info=e, extra={'request': request})
                form.add_error(None, ValidationError(str(e)))
    else:
        form = forms.ResetPasswordForm()
    return render(request, 'django_airavata_auth/reset_password.html', {
        'form': form,
        'code': code
    })


def login_desktop(request):
    context = {
        'options': settings.AUTHENTICATION_OPTIONS,
        'login_desktop': True
    }
    if 'username' in request.GET:
        context['username'] = request.GET['username']
    download_code = request.GET.get('download-code', "false") == "true"
    show_code = request.GET.get('show-code', "false") == "true"
    context['download_code'] = download_code
    context['show_code'] = show_code
    return render(request, 'django_airavata_auth/login-desktop.html', context)


def login_desktop_success(request):
    download_code = request.GET.get('download-code', "false") == "true"
    show_code = request.GET.get('show-code', "false") == "true"

    access_token = request.session['ACCESS_TOKEN']
    if download_code:
        access_token_bytesio = io.BytesIO(access_token.encode())
        return FileResponse(access_token_bytesio, as_attachment=True, filename="access_token.txt")
    else:
        context = {
            'show_code': show_code,
            'code': access_token,
        } if (show_code) else {}
        return render(request, 'django_airavata_auth/login-desktop-success.html', context)


def refreshed_token_desktop(request):
    refresh_code = request.GET['refresh_code']
    user = authenticate(refresh_token=refresh_code, request=request)
    if user is not None:
        valid_time = int(request.session['ACCESS_TOKEN_EXPIRES_AT'] -
                         time.time())
        return JsonResponse({
            'status': 'ok',
            'code': request.session['ACCESS_TOKEN'],
            'refresh_code': request.session['REFRESH_TOKEN'],
            'valid_time': valid_time,
        })
    else:
        return JsonResponse({
            'status': 'failed',
        })


def _create_login_desktop_success_response(request, download_code=False, show_code=False):
    valid_time = int(request.session['ACCESS_TOKEN_EXPIRES_AT'] - time.time())
    query_params = {
        'status': 'ok',
        'code': request.session['ACCESS_TOKEN'],
        'refresh_code': request.session['REFRESH_TOKEN'],
        'valid_time': valid_time,
        'username': request.user.username,
    }
    if download_code:
        query_params['download-code'] = "true"
    if show_code:
        query_params['show-code'] = "true"
    return redirect(
        reverse('django_airavata_auth:login_desktop_success') + "?" + urlencode(query_params))


def _create_login_desktop_failed_response(request, idp_alias=None):
    params = {'status': 'failed'}
    if idp_alias is not None:
        return redirect(reverse('django_airavata_auth:callback-error',
                                args=(idp_alias,)) + "?" + urlencode(params))
    if 'username' in request.POST:
        params['username'] = request.POST['username']
    return redirect(reverse('django_airavata_auth:login_desktop') +
                    "?" + urlencode(params))


@login_required
def access_token_redirect(request):
    redirect_uri = request.GET['redirect_uri']
    config = next(filter(lambda d: d.get('URI') == redirect_uri,
                         settings.ACCESS_TOKEN_REDIRECT_ALLOWED_URIS), None)
    if config is None:
        logger.warning(f"redirect_uri value '{redirect_uri}' is not configured "
                       "in ACCESS_TOKEN_REDIRECT_ALLOWED_URIS setting")
        return HttpResponseForbidden("Invalid redirect_uri value")
    return redirect(redirect_uri + f"{'&' if '?' in redirect_uri else '?'}{config.get('PARAM_NAME', 'access_token')}="
                    f"{quote(request.authz_token.accessToken)}")


@login_required
def user_profile(request):
    return render(request, "django_airavata_auth/base.html", {
        'bundle_name': "user-profile"
    })


class IsUserOrReadOnlyForAdmins(permissions.BasePermission):
    def has_permission(self, request, view):
        return request.user.is_authenticated

    def has_object_permission(self, request, view, obj):
        if (request.method in permissions.SAFE_METHODS and
                request.is_gateway_admin):
            return True
        return obj == request.user


# TODO: disable deleting and creating?
class UserViewSet(viewsets.ModelViewSet):
    serializer_class = serializers.UserSerializer
    queryset = get_user_model().objects.all()
    permission_classes = [IsUserOrReadOnlyForAdmins]

    def get_queryset(self):
        user = self.request.user
        if self.request.is_gateway_admin:
            return get_user_model().objects.all()
        else:
            return get_user_model().objects.filter(pk=user.pk)

    @action(detail=False)
    def current(self, request):
        return redirect(reverse('django_airavata_auth:user-detail', kwargs={'pk': request.user.id}))

    @action(methods=['post'], detail=True)
    def resend_email_verification(self, request, pk=None):
        pending_email_change = models.PendingEmailChange.objects.get(user=request.user, verified=False)
        if pending_email_change is not None:
            serializer = serializers.UserSerializer()
            serializer._send_email_verification_link(request, pending_email_change)
        return JsonResponse({})

    @action(methods=['post'], detail=True)
    @atomic
    def verify_email_change(self, request, pk=None):
        user = self.get_object()
        code = request.data['code']

        try:
            pending_email_change = models.PendingEmailChange.objects.get(user=user, verification_code=code)
        except models.PendingEmailChange.DoesNotExist:
            raise Exception('Verification code is invalid. Please try again.')
        pending_email_change.verified = True
        pending_email_change.save()
        user.email = pending_email_change.email_address
        user.save()
        user.refresh_from_db()

        try:
            # only update the airavata profile if it exists
            user_profile_client = request.profile_service['user_profile']
            if user_profile_client.doesUserExist(request.authz_token,
                                                 request.user.username,
                                                 settings.GATEWAY_ID):
                airavata_user_profile = user_profile_client.getUserProfileById(
                    request.authz_token, user.username, settings.GATEWAY_ID)
                airavata_user_profile.emails = [pending_email_change.email_address]
                user_profile_client.updateUserProfile(request.authz_token, airavata_user_profile)
            # otherwise, update the user's email in the Keycloak user store
            else:
                iam_admin_client.update_user(request.user.username,
                                             email=pending_email_change.email_address)
        except Exception as e:
            raise Exception(f"Failed to update Airavata User Profile with new email address: {e}") from e
        serializer = self.get_serializer(user)
        return Response(serializer.data)


@login_required
def download_settings_local(request):

    if not (request.is_gateway_admin or request.is_read_only_gateway_admin):
        raise PermissionDenied()

    if settings.DEBUG:
        raise Exception("Downloading a settings_local.py file isn't allowed in DEBUG mode.")

    development_client_id = f"local-django-{request.user.username}"
    access_token = utils.get_service_account_authz_token().accessToken
    clients_endpoint = get_clients_endpoint()
    development_client = get_client(access_token, clients_endpoint, development_client_id)
    if development_client is None:
        development_client_endpoint = create_client(access_token, clients_endpoint, development_client_id)
    else:
        development_client_endpoint = get_client_endpoint(development_client)
    development_client_secret = get_client_secret(access_token, development_client_endpoint)

    context = {}
    context['AUTHENTICATION_OPTIONS'] = settings.AUTHENTICATION_OPTIONS
    context['keycloak_client_id'] = development_client_id
    context['keycloak_client_secret'] = development_client_secret
    context['KEYCLOAK_AUTHORIZE_URL'] = settings.KEYCLOAK_AUTHORIZE_URL
    context['KEYCLOAK_TOKEN_URL'] = settings.KEYCLOAK_TOKEN_URL
    context['KEYCLOAK_USERINFO_URL'] = settings.KEYCLOAK_USERINFO_URL
    context['KEYCLOAK_LOGOUT_URL'] = settings.KEYCLOAK_LOGOUT_URL
    context['GATEWAY_ID'] = settings.GATEWAY_ID
    context['AIRAVATA_API_HOST'] = settings.AIRAVATA_API_HOST
    context['AIRAVATA_API_PORT'] = settings.AIRAVATA_API_PORT
    context['AIRAVATA_API_SECURE'] = settings.AIRAVATA_API_SECURE
    if hasattr(settings, 'GATEWAY_DATA_STORE_REMOTE_API'):
        context['GATEWAY_DATA_STORE_REMOTE_API'] = settings.GATEWAY_DATA_STORE_REMOTE_API
    else:
        context['GATEWAY_DATA_STORE_REMOTE_API'] = request.build_absolute_uri("/")
    context['PROFILE_SERVICE_HOST'] = settings.PROFILE_SERVICE_HOST
    context['PROFILE_SERVICE_PORT'] = settings.PROFILE_SERVICE_PORT
    context['PROFILE_SERVICE_SECURE'] = settings.PROFILE_SERVICE_SECURE
    context['PORTAL_TITLE'] = settings.PORTAL_TITLE
    settings_local_str = render_to_string("django_airavata_auth/settings_local.py.template", context)
    settings_local_bytesio = io.BytesIO(settings_local_str.encode())
    return FileResponse(settings_local_bytesio, as_attachment=True, filename="settings_local.py")


def get_client(access_token, clients_endpoint, client_id):
    headers = {'Authorization': f'Bearer {access_token}', 'Content-Type': 'application/json'}
    r = requests.get(clients_endpoint, {'clientId': client_id}, headers=headers)
    r.raise_for_status()
    clients = r.json()
    if len(clients) == 0:
        return None
    else:
        return clients[0]


def get_clients_endpoint():
    realm = settings.GATEWAY_ID
    parse_result = urlparse(settings.KEYCLOAK_AUTHORIZE_URL)
    clients_endpoint = f"{parse_result.scheme}://{parse_result.netloc}/auth/admin/realms/{realm}/clients"
    return clients_endpoint


def get_client_endpoint(client):
    return f"{get_clients_endpoint()}/{client['id']}"


def create_client(access_token, clients_endpoint, client_id):
    client = {
        'clientId': client_id,
        "redirectUris": [
            "http://localhost:8000/",
            "http://localhost:8000/auth/callback*",
            "http://127.0.0.1:8000/",
            "http://127.0.0.1:8000/auth/callback*"
        ],
        "directAccessGrantsEnabled": True
    }
    headers = {'Authorization': f'Bearer {access_token}', 'Content-Type': 'application/json'}
    r = requests.post(clients_endpoint, json=client, headers=headers)
    r.raise_for_status()
    return r.headers['Location']


def get_client_secret(access_token, client_endpoint):

    headers = {'Authorization': f'Bearer {access_token}'}
    r = requests.get(client_endpoint + "/client-secret", headers=headers)
    r.raise_for_status()
    return r.json()['value']


class ExtendedUserProfileFieldViewset(viewsets.ModelViewSet):
    serializer_class = serializers.ExtendedUserProfileFieldSerializer
    queryset = models.ExtendedUserProfileField.objects.all().order_by('order')
    permission_classes = [permissions.IsAuthenticated, IsInAdminsGroupPermission | ReadOnly]

    def get_queryset(self):
        queryset = super().get_queryset()
        if self.action == 'list':
            queryset = queryset.filter(deleted=False)
        return queryset

    def perform_destroy(self, instance):
        instance.deleted = True
        instance.save()


class IsExtendedUserProfileOwnerOrReadOnlyForAdmins(permissions.BasePermission):
    def has_permission(self, request, view):
        return request.user.is_authenticated

    def has_object_permission(self, request, view, obj):
        if (request.method in permissions.SAFE_METHODS and
                request.is_gateway_admin):
            return True
        return obj.user_profile.user == request.user


class ExtendedUserProfileValueViewset(mixins.CreateModelMixin,
                                      mixins.RetrieveModelMixin,
                                      mixins.UpdateModelMixin,
                                      mixins.ListModelMixin,
                                      viewsets.GenericViewSet):
    serializer_class = serializers.ExtendedUserProfileValueSerializer
    permission_classes = [IsExtendedUserProfileOwnerOrReadOnlyForAdmins]

    def get_queryset(self):
        user = self.request.user
        if self.request.is_gateway_admin and self.request.query_params.get('username'):
            queryset = models.ExtendedUserProfileValue.objects.all()
            username = self.request.query_params.get('username')
            queryset = queryset.filter(user_profile__user__username=username)
        else:
            queryset = user.user_profile.extended_profile_values.all()
        return queryset

    @action(methods=['POST'], detail=False, url_path="save-all")
    @atomic
    def save_all(self, request, format=None):
        user = request.user
        user_profile: models.UserProfile = user.user_profile
        old_valid = user_profile.is_ext_user_profile_valid
        serializer: serializers.ExtendedUserProfileValueSerializer = self.get_serializer(data=request.data, many=True)
        serializer.is_valid(raise_exception=True)
        values = serializer.save()

        new_valid = user_profile.is_ext_user_profile_valid
        if not old_valid and new_valid:
            utils.send_admin_user_completed_profile(request, user_profile)

        serializer = self.get_serializer(values, many=True)
        return Response(serializer.data)
