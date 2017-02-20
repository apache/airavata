
from apache.airavata.model.security.ttypes import AuthzToken

def get_authz_token(request):
    access_token = request.session['ACCESS_TOKEN']
    username = request.user.username
    # TODO: get this from settings?
    gateway_id = 'php_reference_gateway'
    return AuthzToken(accessToken=access_token, claimsMap={'gatewayID': gateway_id, 'userName': username})
