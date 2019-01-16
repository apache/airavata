
import logging

from . import utils

logger = logging.getLogger(__name__)


def airavata_client(get_response):
    "Open and close Airavata client for each request"

    def middleware(request):

        # If user is logged in create an airavata api client for the request
        if request.user.is_authenticated:
            try:
                request.airavata_client = utils.airavata_api_client_pool
                response = get_response(request)
            except utils.ThriftConnectionException as e:
                logger.exception(
                    "Failed to open thrift connection to API server")
                # if request.airavata_client is None, this will indicate to
                # view code that the API server is down
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
                request.sharing_client = utils.sharing_api_client_pool
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
                request.profile_service = {
                    'group_manager': utils.group_manager_client_pool,
                    'iam_admin': utils.iamadmin_client_pool,
                    'tenant_profile': utils.tenant_profile_client_pool,
                    'user_profile': utils.user_profile_client_pool,
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
