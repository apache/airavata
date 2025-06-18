
import logging
import warnings
from urllib.parse import quote

import requests
from django.conf import settings
from django.core.exceptions import ObjectDoesNotExist

logger = logging.getLogger(__name__)


def is_remote_api_configured():
    return getattr(settings, 'GATEWAY_DATA_STORE_REMOTE_API', None) is not None


def call(request,
         path,
         path_params=None,
         method="get",
         raise_for_status=True,
         base_url="/api",
         **kwargs):

    headers = {
        'Authorization': f'Bearer {request.authz_token.accessToken}'}
    encoded_path_params = {}
    if path_params is not None:
        for pk, pv in path_params.items():
            encoded_path_params[pk] = quote(pv)
    encoded_path = path.format(**encoded_path_params)
    logger.debug(f"encoded_path={encoded_path}")
    remote_api_url = settings.GATEWAY_DATA_STORE_REMOTE_API
    if remote_api_url.endswith("/api"):
        warnings.warn(f"Set GATEWAY_DATA_STORE_REMOTE_API to \"{remote_api_url}\". /api is no longer needed.", DeprecationWarning)
        remote_api_url = remote_api_url[0:remote_api_url.rfind("/api")]
    r = requests.request(
        method,
        f'{remote_api_url}{base_url}{encoded_path}',
        headers=headers,
        **kwargs,
    )
    if raise_for_status:
        r.raise_for_status()
    return r


def raise_if_404(response, msg, exception_class=ObjectDoesNotExist):
    if response.status_code == 404:
        raise exception_class(msg)
