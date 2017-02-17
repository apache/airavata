from django.shortcuts import render, redirect
from django.urls import reverse

from urllib.parse import urlencode

# Create your views here.

def login(request):
    # TODO redirect to WSO2 IS, passing the callback url
    # TODO get authorize URL, client_id from settings
    base_authorize_url = "https://localhost:9443/oauth2/authorize"
    authorize_query_params = {
        'client_id': 'fGwm3EW0EmaiV0jI6GBmmOiQ2Xca',
        'response_type': 'code',
        'scope': 'openid',
        'redirect_uri': request.build_absolute_uri(reverse('airavata_auth_callback'))
    }
    return redirect(base_authorize_url + '?' + urlencode(authorize_query_params))

def logout(request):
    # TODO clear out session information
    return HttpResponse("Logout")

def callback(request):
    # TODO authenticate user
    # TODO login user
    # TODO redirect to LOGIN_REDIRECT_URL
    pass
