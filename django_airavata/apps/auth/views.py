import logging
from urllib.parse import quote

from django.conf import settings
from django.contrib.auth import authenticate, login, logout
from django.shortcuts import redirect, render, resolve_url
from django.urls import reverse
from requests_oauthlib import OAuth2Session

from . import forms

logger = logging.getLogger(__name__)


def start_login(request):
    return render(request, 'django_airavata_auth/login.html', {
        'next': request.GET.get('next', None),
        'options': settings.AUTHENTICATION_OPTIONS,
    })


def redirect_login(request, idp_alias):
    _validate_idp_alias(idp_alias)
    client_id = settings.KEYCLOAK_CLIENT_ID
    base_authorize_url = settings.KEYCLOAK_AUTHORIZE_URL
    redirect_uri = request.build_absolute_uri(
        reverse('django_airavata_auth:callback'))
    if 'next' in request.GET:
        redirect_uri += "?next=" + quote(request.GET['next'])
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


def handle_login(request):
    username = request.POST['username']
    password = request.POST['password']
    user = authenticate(username=username, password=password, request=request)
    logger.debug("authenticated user: {}".format(user))
    try:
        if user is not None:
            login(request, user)
            next_url = request.POST.get('next', settings.LOGIN_REDIRECT_URL)
            return redirect(next_url)
        else:
            # TODO: add error message that login failed
            return render(request, 'django_airavata_auth/login.html', {
                'username': username
            })
    except Exception as err:
        logger.exception("An error occurred while logging in with "
                         "username and password")
        return redirect(reverse('django_airavata_auth:error'))


def start_logout(request):
    logout(request)
    redirect_url = request.build_absolute_uri(
        resolve_url(settings.LOGOUT_REDIRECT_URL))
    return redirect(settings.KEYCLOAK_LOGOUT_URL +
                    "?redirect_uri=" + quote(redirect_url))


def callback(request):
    try:
        user = authenticate(request=request)
        login(request, user)
        next_url = request.GET.get('next', settings.LOGIN_REDIRECT_URL)
        return redirect(next_url)
    except Exception as err:
        logger.exception("An error occurred while processing OAuth2 "
                         "callback: {}".format(request.build_absolute_uri()))
        return redirect(reverse('django_airavata_auth:error'))


def auth_error(request):
    return render(request, 'django_airavata_auth/auth_error.html', {
        'login_url': settings.LOGIN_URL
    })


def create_account(request):
    if request.method == 'POST':
        form = forms.CreateAccountForm(request.POST)
        if form.is_valid():
            # TODO: IAM registerUser
            # TODO: send email account verification email
            # TODO: success message
            return redirect(reverse('django_airavata_auth:login'))
    else:
        form = forms.CreateAccountForm()
    return render(request, 'django_airavata_auth/create_account.html', {
        'options': settings.AUTHENTICATION_OPTIONS,
        'form': form
    })
