from django.conf import settings
from django.contrib.auth import authenticate, login, logout
from django.http import HttpResponse
from django.shortcuts import render, redirect
from django.urls import reverse

from requests_oauthlib import OAuth2Session

import logging
from urllib.parse import urlencode

logger = logging.getLogger(__name__)

# Create your views here.

def start_login(request):
    client_id = settings.WSO2IS_CLIENT_ID
    base_authorize_url = settings.WSO2IS_AUTHORIZE_URL
    wso2is = OAuth2Session(client_id, scope='openid', redirect_uri=request.build_absolute_uri(reverse('airavata_auth_callback')))
    authorization_url, state = wso2is.authorization_url(base_authorize_url)
    logger.debug("authorization_url={}, state={}".format(authorization_url, state))
    # Store state in session for later validation
    request.session['OAUTH2_STATE'] = state
    return redirect(authorization_url)

def start_logout(request):
    logout(request)
    return redirect('/')

def callback(request):
    try:
        user = authenticate(request=request)
        login(request, user)
        return redirect(settings.LOGIN_REDIRECT_URL)
    except Exception as err:
        logger.exception("An error occurred while processing OAuth2 callback: {}".format(request.build_absolute_uri()))
        return redirect(reverse('airavata_auth_error'))

def auth_error(request):
    return render(request, 'django_airavata_auth/auth_error.html', {
        'login_url': settings.LOGIN_URL
    })