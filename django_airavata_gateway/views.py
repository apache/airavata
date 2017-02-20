
from django_airavata_auth import utils

from django.http import HttpResponse

import logging

logger = logging.getLogger(__name__)

def home(request):
    authz_token = utils.get_authz_token(request)
    return HttpResponse("access token {}, username {}, gatewayId {}".format(authz_token.accessToken, authz_token.claimsMap['userName'], authz_token.claimsMap['gatewayID']))