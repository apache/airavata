import logging
import os

from django.core.exceptions import ObjectDoesNotExist
from django.http import FileResponse, Http404
from django.shortcuts import redirect
from django.views.decorators.gzip import gzip_page
from rest_framework.decorators import api_view

from airavata_django_portal_sdk import user_storage

logger = logging.getLogger(__name__)


@api_view()
def download(request):
    data_product_uri = request.GET.get('data-product-uri', '')
    download_url = user_storage.get_download_url(request, data_product_uri=data_product_uri)
    return redirect(download_url)


@gzip_page
@api_view()
def download_file(request):
    data_product_uri = request.GET.get('data-product-uri', '')
    force_download = 'download' in request.GET
    data_product = None
    try:
        data_product = request.airavata_client.getDataProduct(
            request.authz_token, data_product_uri)
        mime_type = "application/octet-stream"  # default mime-type
        if (data_product.productMetadata and
                'mime-type' in data_product.productMetadata):
            mime_type = data_product.productMetadata['mime-type']
        # 'mime-type' url parameter overrides
        mime_type = request.GET.get('mime-type', mime_type)
    except Exception as e:
        logger.warning("Failed to load DataProduct for {}"
                       .format(data_product_uri), exc_info=True)
        raise Http404("data product does not exist") from e
    try:
        data_file = user_storage.open_file(request, data_product)
        response = FileResponse(data_file, content_type=mime_type)
        file_name = os.path.basename(data_file.name)
        if mime_type == 'application/octet-stream' or force_download:
            response['Content-Disposition'] = ('attachment; filename="{}"'
                                               .format(file_name))
        else:
            response['Content-Disposition'] = f'inline; filename="{file_name}"'
        return response
    except ObjectDoesNotExist as e:
        raise Http404(str(e)) from e
