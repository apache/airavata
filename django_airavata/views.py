
from django_airavata_auth import utils

from django.http import HttpResponse

import logging

logger = logging.getLogger(__name__)

def home(request):
    authz_token = request.authz_token
    if request.authz_token:
        return HttpResponse("access token {}, username {}, gatewayId {}".format(authz_token.accessToken, authz_token.claimsMap['userName'], authz_token.claimsMap['gatewayID']))
    else:
        return HttpResponse("Not logged in!")