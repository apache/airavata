import logging
import time
from datetime import datetime, timedelta, timezone
from urllib.parse import quote, urlencode

from django.conf import settings
from django.contrib import messages
from django.contrib.auth import authenticate, login, logout
from django.core.exceptions import ObjectDoesNotExist
from django.forms import ValidationError
from django.http import HttpResponseBadRequest, JsonResponse
from django.shortcuts import redirect, render, resolve_url
from django.template import Context
from django.urls import reverse
from django.views.decorators.debug import sensitive_variables
from requests_oauthlib import OAuth2Session

from . import forms, iam_admin_client, models, utils

logger = logging.getLogger(__name__)


def start_login(request):
    return render(request, 'django_airavata_auth/login.html', {
        'next': request.GET.get('next', None),
        'options': settings.AUTHENTICATION_OPTIONS,
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
    if 'next' in request.GET:
        redirect_uri += "&next=" + quote(request.GET['next'])
    if 'login_desktop' in request.GET:
        redirect_uri += "&login_desktop=" + quote(request.GET['login_desktop'])
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
    username = request.POST['username']
    password = request.POST['password']
    login_type = request.POST.get('login_type', None)
    login_desktop = request.POST.get('login_desktop', "false") == "true"
    template = "django_airavata_auth/login.html"
    if login_type and login_type == 'password':
        template = "django_airavata_auth/login_username_password.html"
    user = authenticate(username=username, password=password, request=request)
    logger.debug("authenticated user: {}".format(user))
    try:
        if user is not None:
            login(request, user)
            if login_desktop:
                return _create_login_desktop_success_response(request)
            else:
                next_url = request.POST.get('next',
                                            settings.LOGIN_REDIRECT_URL)
                return redirect(next_url)
        else:
            messages.error(request, "Login failed. Please try again.")
    except Exception as err:
        logger.exception("Login failed for user {}".format(username))
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
        user = authenticate(request=request)
        login(request, user)
        if login_desktop:
            return _create_login_desktop_success_response(request)
        next_url = request.GET.get('next', settings.LOGIN_REDIRECT_URL)
        return redirect(next_url)
    except Exception as err:
        logger.exception("An error occurred while processing OAuth2 "
                         "callback: {}".format(request.build_absolute_uri()))
        messages.error(
            request,
            "Failed to process OAuth2 callback: {}".format(str(err)))
        idp_alias = request.GET.get('idp_alias')
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
                    _create_and_send_email_verification_link(
                        request, username, email, first_name, last_name)
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
                    "Failed to create account for user", exc_info=e)
                form.add_error(None, ValidationError(e.message))
    else:
        form = forms.CreateAccountForm()
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
        if iam_admin_client.is_user_enabled(username):
            logger.debug("User {} is already enabled".format(username))
            messages.success(
                request,
                "Your account has already been successfully created. "
                "Please log in now.")
            return redirect(reverse('django_airavata_auth:login'))
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
            return redirect(reverse('django_airavata_auth:login'))
    except ObjectDoesNotExist as e:
        # if doesn't exist, give user a form where they can enter their
        # username to resend verification code
        logger.exception("EmailVerification object doesn't exist for "
                         "code {}".format(code))
        messages.error(
            request,
            "Email verification failed. Please enter your username and we "
            "will send you another email verification link.")
        return redirect(reverse('django_airavata_auth:resend_email_link'))
    except Exception as e:
        logger.exception("Email verification processing failed!")
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
                    "Failed to resend email verification link", exc_info=e)
                form.add_error(None, ValidationError(str(e)))
    else:
        form = forms.ResendEmailVerificationLinkForm()
    return render(request, 'django_airavata_auth/verify_email.html', {
        'form': form
    })


def _create_and_send_email_verification_link(
        request, username, email, first_name, last_name):

    email_verification = models.EmailVerification(
        username=username)
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
                    exc_info=e)
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
    except ObjectDoesNotExist as e:
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
                    "Failed to reset password for user", exc_info=e)
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
    return render(request, 'django_airavata_auth/login-desktop.html', context)


def login_desktop_success(request):
    return render(request, 'django_airavata_auth/login-desktop-success.html')


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


def _create_login_desktop_success_response(request):
    valid_time = int(request.session['ACCESS_TOKEN_EXPIRES_AT'] - time.time())
    return redirect(
        reverse('django_airavata_auth:login_desktop_success') +
        "?" + urlencode({
            'status': 'ok',
            'code': request.session['ACCESS_TOKEN'],
            'refresh_code': request.session['REFRESH_TOKEN'],
            'valid_time': valid_time,
            'username': request.user.username
        }))


def _create_login_desktop_failed_response(request, idp_alias=None):
    params = {'status': 'failed'}
    if idp_alias is not None:
        return redirect(reverse('django_airavata_auth:callback-error',
                                args=(idp_alias,)) + "?" + urlencode(params))
    if 'username' in request.POST:
        params['username'] = request.POST['username']
    return redirect(reverse('django_airavata_auth:login_desktop') +
                    "?" + urlencode(params))
