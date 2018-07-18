"""Django Airavata Auth Middleware."""
from django.contrib.auth import logout

from . import utils


def authz_token_middleware(get_response):
    """Automatically add the 'authz_token' to the request."""
    def middleware(request):

        authz_token = None
        if request.user.is_authenticated:
            authz_token = utils.get_authz_token(request)
            # If we can't construct an authz_token then need to re-login
            if authz_token is None:
                # logout user since no longer logged in with IAM server
                logout(request)

        request.authz_token = authz_token

        return get_response(request)

    return middleware
