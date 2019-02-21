import logging

from rest_framework import status
from rest_framework.response import Response
from rest_framework.views import exception_handler
from thrift.Thrift import TException

from airavata.api.error.ttypes import AuthorizationException

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

    # Default TException handler, should come after more specific subclasses of
    # TException
    if isinstance(exc, TException):
        log.error("TException", exc_info=exc)
        return Response(
            {'detail': str(exc)},
            status=status.HTTP_500_INTERNAL_SERVER_ERROR)

    # Generic handler
    if response is None:
        log.error("API exception", exc_info=exc)
        return Response(
            {'detail': str(exc)},
            status=status.HTTP_500_INTERNAL_SERVER_ERROR
        )

    return response
