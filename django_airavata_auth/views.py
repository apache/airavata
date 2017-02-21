from django.conf import settings
from django.contrib.auth import authenticate, login
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

def logout(request):
    # TODO clear out session information
    return HttpResponse("Logout")

def callback(request):
    # TODO handle authentication errors
    user = authenticate(authorization_code_url=request.build_absolute_uri(),
        redirect_url=request.build_absolute_uri(reverse('airavata_auth_callback')), request=request)
    if user is not None:
        login(request, user)
        return redirect(settings.LOGIN_REDIRECT_URL)
    else:
        return redirect('/')
