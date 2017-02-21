
from apache.airavata.model.security.ttypes import AuthzToken

from django.conf import settings

def get_authz_token(request):
    access_token = request.session['ACCESS_TOKEN']
    username = request.user.username
    gateway_id = settings.GATEWAY_ID
    return AuthzToken(accessToken=access_token, claimsMap={'gatewayID': gateway_id, 'userName': username})
