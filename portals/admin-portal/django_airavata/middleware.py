
import logging

import thrift
from django.shortcuts import render

from . import utils

logger = logging.getLogger(__name__)


# TODO: use the pooled clients in the airavata-python-sdk directly instead of
# these request attributes
class AiravataClientMiddleware:
    def __init__(self, get_response):
        self.get_response = get_response

    def __call__(self, request):
        request.airavata_client = utils.airavata_api_client_pool
        response = self.get_response(request)

        return response

    def process_exception(self, request, exception):
        if isinstance(exception,
                      thrift.transport.TTransport.TTransportException):
            return render(
                request,
                'django_airavata/error_page.html',
                status=500,
                context={
                    'title': 'Airavata is down',
                    'text': """The Airavata API server is not reachable. Please try again."""})
        else:
            return None


def profile_service_client(get_response):
    """Open and close Profile Service client for each request.

    Usage:
        request.profile_service['group_manager'].getGroup(
            request.authz_token, groupId)
    """
    def middleware(request):

        request.profile_service = {
            'group_manager': utils.group_manager_client_pool,
            'iam_admin': utils.iamadmin_client_pool,
            'tenant_profile': utils.tenant_profile_client_pool,
            'user_profile': utils.user_profile_client_pool,
        }
        response = get_response(request)

        return response

    return middleware
