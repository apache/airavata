from . import utils

def authz_token_middleware(get_response):
    "Automatically add the 'authz_token' to the request"

    def middleware(request):

        authz_token = None
        if request.user.is_authenticated:
            authz_token = utils.get_authz_token(request)

        request.authz_token = authz_token

        return get_response(request)

    return middleware
