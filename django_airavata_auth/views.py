from django.contrib.auth import authenticate, login
from django.http import HttpResponse
from django.shortcuts import render, redirect
from django.urls import reverse

from requests_oauthlib import OAuth2Session

from urllib.parse import urlencode

# Create your views here.

def start_login(request):
    # TODO get authorize URL, client_id from settings
    client_id = 'fGwm3EW0EmaiV0jI6GBmmOiQ2Xca'
    base_authorize_url = "https://localhost:9443/oauth2/authorize"
    wso2is = OAuth2Session(client_id, scope='openid', redirect_uri=request.build_absolute_uri(reverse('airavata_auth_callback')))
    authorization_url, state = wso2is.authorization_url(base_authorize_url)
    # TODO store state in session
    return redirect(authorization_url)

def logout(request):
    # TODO clear out session information
    return HttpResponse("Logout")

def callback(request):
    # TODO handle authentication errors
    user = authenticate(authorization_code_url=request.build_absolute_uri(),
        redirect_url=request.build_absolute_uri(reverse('airavata_auth_callback')))
    if user is not None:
        login(request, user)
        # TODO redirect to LOGIN_REDIRECT_URL
        return HttpResponse("Logged in!")
    else:
        redirect('/')
