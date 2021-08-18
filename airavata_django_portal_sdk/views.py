import logging
import os
import tempfile
import zipfile

from django.core.exceptions import ObjectDoesNotExist
from django.http import FileResponse, Http404
from django.shortcuts import redirect
from django.utils.text import get_valid_filename
from django.views.decorators.gzip import gzip_page
from rest_framework.decorators import api_view

from airavata_django_portal_sdk import user_storage

logger = logging.getLogger(__name__)

MAX_DOWNLOAD_ZIPFILE_SIZE = 1 * 1024**3  # 1 GB


@api_view()
def download(request):
    data_product_uri = request.GET.get('data-product-uri', '')
    force_download = 'download' in request.GET
    mime_type = request.GET.get('mime-type')
    download_url = user_storage.get_download_url(request,
                                                 data_product_uri=data_product_uri,
                                                 force_download=force_download,
                                                 mime_type=mime_type)
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
        if user_storage.is_input_file(request, data_product):
            file_name = data_product.productName
        else:
            file_name = os.path.basename(data_file.name)
        if mime_type == 'application/octet-stream' or force_download:
            response['Content-Disposition'] = ('attachment; filename="{}"'
                                               .format(file_name))
        else:
            response['Content-Disposition'] = f'inline; filename="{file_name}"'
        return response
    except ObjectDoesNotExist as e:
        raise Http404(str(e)) from e


@api_view()
def download_dir(request):
    path = request.GET.get('path', "")
    fp = tempfile.TemporaryFile()
    with zipfile.ZipFile(fp, 'w', compression=zipfile.ZIP_DEFLATED) as zf:
        _add_directory_to_zipfile(request, zf, path)
    if os.path.basename(path) == "" or get_valid_filename(os.path.basename(path)) == "":
        filename = 'home.zip'
    else:
        filename = get_valid_filename(os.path.basename(path)) + ".zip"
    fp.seek(0)
    # FileResponse will automatically close the temporary file
    return FileResponse(fp, as_attachment=True, filename=filename)


@api_view()
def download_experiment_dir(request, experiment_id=None):
    path = request.GET.get('path', "")
    experiment = request.airavata_client.getExperiment(request.authz_token, experiment_id)
    fp = tempfile.TemporaryFile()
    with zipfile.ZipFile(fp, 'w', compression=zipfile.ZIP_DEFLATED) as zf:
        _add_experiment_directory_to_zipfile(request, zf, experiment_id, path)
    if os.path.basename(path) == "" or get_valid_filename(os.path.basename(path)) == "":
        filename = f'{get_valid_filename(experiment.experimentName)}.zip'
    else:
        filename = f'{get_valid_filename(experiment.experimentName)}_{get_valid_filename(os.path.basename(path))}.zip'
    fp.seek(0)
    # FileResponse will automatically close the temporary file
    return FileResponse(fp, as_attachment=True, filename=filename)


def _add_directory_to_zipfile(request, zf, path, directory=""):
    directories, files = user_storage.listdir(request, os.path.join(path, directory))
    for file in files:
        o = user_storage.open_file(request, data_product_uri=file['data-product-uri'])
        zf.writestr(os.path.join(directory, file['name']), o.read())
        if os.path.getsize(zf.filename) > MAX_DOWNLOAD_ZIPFILE_SIZE:
            raise Exception(f"Zip file size exceeds max of {MAX_DOWNLOAD_ZIPFILE_SIZE} bytes")
    for d in directories:
        _add_directory_to_zipfile(request, zf, path, os.path.join(directory, d['name']))


def _add_experiment_directory_to_zipfile(request, zf, experiment_id, path, directory=""):
    directories, files = user_storage.list_experiment_dir(request, experiment_id, os.path.join(path, directory))
    for file in files:
        o = user_storage.open_file(request, data_product_uri=file['data-product-uri'])
        zf.writestr(os.path.join(directory, file['name']), o.read())
        if os.path.getsize(zf.filename) > MAX_DOWNLOAD_ZIPFILE_SIZE:
            raise Exception(f"Zip file size exceeds max of {MAX_DOWNLOAD_ZIPFILE_SIZE} bytes")
    for d in directories:
        _add_experiment_directory_to_zipfile(request, zf, experiment_id, path, os.path.join(directory, d['name']))
