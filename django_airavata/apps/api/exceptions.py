import logging

from airavata.api.error.ttypes import AuthorizationException
from django.core.exceptions import ObjectDoesNotExist
from django.http import JsonResponse
from rest_framework import status
from rest_framework.exceptions import NotAuthenticated
from rest_framework.response import Response
from rest_framework.views import exception_handler
from thrift.Thrift import TException
from thrift.transport import TTransport

log = logging.getLogger(__name__)


def custom_exception_handler(exc, context):
    # Call REST framework's default exception handler first,
    # to get the standard error response.
    response = exception_handler(exc, context)

    if isinstance(exc, AuthorizationException):
        log.warning("AuthorizationException", exc_info=exc)
        return Response(
            {'detail': str(exc)},
            status=status.HTTP_403_FORBIDDEN)

    if isinstance(exc, TTransport.TTransportException):
        log.warning("TTransportException", exc_info=exc)
        return Response(
            {'detail': str(exc), 'apiServerDown': True},
            status=status.HTTP_500_INTERNAL_SERVER_ERROR)

    # Default TException handler, should come after more specific subclasses of
    # TException
    if isinstance(exc, TException):
        log.error("TException", exc_info=exc, extra={'request': context['request']})
        return Response(
            {'detail': str(exc)},
            status=status.HTTP_500_INTERNAL_SERVER_ERROR)

    if isinstance(exc, ObjectDoesNotExist):
        log.warning("ObjectDoesNotExist", exc_info=exc)
        return Response(
            {'detail': str(exc)},
            status=status.HTTP_404_NOT_FOUND)

    if isinstance(exc, NotAuthenticated):
        log.debug("NotAuthenticated", exc_info=exc)
        if response is not None:
            response.data['is_authenticated'] = False

    # Generic handler
    if response is None:
        log.error("API exception", exc_info=exc, extra={'request': context['request']})
        return Response(
            {'detail': str(exc)},
            status=status.HTTP_500_INTERNAL_SERVER_ERROR
        )

    return response


# For non-Django REST Framework error responses
def generic_json_exception_response(
        exc, status=status.HTTP_500_INTERNAL_SERVER_ERROR):
    return JsonResponse({'detail': str(exc)}, status=status)
