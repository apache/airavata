
import logging

from . import utils

logger = logging.getLogger(__name__)


def airavata_client(get_response):
    "Open and close Airavata client for each request"

    def middleware(request):

        # If user is logged in create an airavata api client for the request
        if request.user.is_authenticated:
            try:
                with utils.airavata_client() as airavata_client:
                    request.airavata_client = airavata_client
                    response = get_response(request)
            except utils.ThriftConnectionException as e:
                logger.exception("Failed to open thrift connection to API server")
                # if request.airavata_client is None, this will indicate to view
                # code that the API server is down
                request.airavata_client = None
                response = get_response(request)
        else:
            response = get_response(request)

        return response

    return middleware


def sharing_client(get_response):
    "Open and close Sharing registry client for each request"

    def middleware(request):

        # If user is logged in create a sharing registry client for the request
        if request.user.is_authenticated:
            try:
                with utils.sharing_client() as sharing_client:
                    request.sharing_client = sharing_client
                    response = get_response(request)
            except utils.ThriftConnectionException as e:
                logger.exception("Failed to open thrift connection to"
                                 "Sharing Registry server")
                # if request.sharing_client is None, this will indicate to view
                # code that the Sharing server is down
                request.sharing_client = None
                response = get_response(request)
        else:
            response = get_response(request)

        return response

    return middleware


def profile_service_client(get_response):
    """Open and close Profile Service client for each request.

    Usage:
        request.profile_service['group_manager'].getGroup(
            request.authz_token, groupId)
    """
    def middleware(request):

        # If user is logged in create an profile service client for the request
        if request.user.is_authenticated:
            try:
                with utils.group_manager_client() as group_manager_client, \
                     utils.iam_admin_client() as iam_admin_client, \
                     utils.tenant_profile_client() as tenant_profile_client, \
                     utils.user_profile_client() as user_profile_client:
                    request.profile_service = {
                        'group_manager': group_manager_client,
                        'iam_admin': iam_admin_client,
                        'tenant_profile': tenant_profile_client,
                        'user_profile': user_profile_client,
                    }
                    response = get_response(request)
            except utils.ThriftConnectionException as e:
                logger.exception("Failed to open thrift connection to "
                                 "Profile Service server")
                # if request.profile_service is None, this will indicate to
                # view code that the Profile Service is down
                request.profile_service = None
                response = get_response(request)
        else:
            response = get_response(request)

        return response

    return middleware
