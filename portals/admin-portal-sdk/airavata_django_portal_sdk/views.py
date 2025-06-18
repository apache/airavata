import fnmatch
import io
import logging
import os
import uuid
from string import Template

import zipstream
from django.core.exceptions import ObjectDoesNotExist
from django.http import FileResponse, Http404, StreamingHttpResponse
from django.shortcuts import redirect
from django.urls import reverse
from django.utils.text import get_valid_filename
from django.views.decorators.gzip import gzip_page
from rest_framework import status
from rest_framework.decorators import api_view
from rest_framework.response import Response

from airavata_django_portal_sdk import serializers, user_storage

logger = logging.getLogger(__name__)


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
        as_attachment = (mime_type == 'application/octet-stream' or force_download)
        if user_storage.is_input_file(request, data_product):
            file_name = data_product.productName
        else:
            file_name = os.path.basename(data_file.name)
        return FileResponse(data_file, content_type=mime_type, as_attachment=as_attachment, filename=file_name)
    except ObjectDoesNotExist as e:
        raise Http404(str(e)) from e


@api_view()
def download_dir(request):
    path = request.GET.get('path', "")
    if os.path.basename(path) == "" or get_valid_filename(os.path.basename(path)) == "":
        filename = 'home.zip'
    else:
        filename = get_valid_filename(os.path.basename(path)) + ".zip"
    zipfile_entries = _get_directory_zipfile_entries(request, path)
    return _create_zip_response(request, filename, zipfile_entries)


@api_view()
def download_experiment_dir(request, experiment_id=None):
    path = request.GET.get('path', "")
    experiment = request.airavata_client.getExperiment(request.authz_token, experiment_id)
    if os.path.basename(path) == "" or get_valid_filename(os.path.basename(path)) == "":
        filename = f'{get_valid_filename(experiment.experimentName)}.zip'
    else:
        filename = f'{get_valid_filename(experiment.experimentName)}_{get_valid_filename(os.path.basename(path))}.zip'
    zipfile_entries = _get_experiment_directory_zipfile_entries(request, experiment_id, path)
    return _create_zip_response(request, filename, zipfile_entries)


@api_view(['GET', 'POST'])
def download_experiments(request, download_id=None):
    if request.method == 'POST':
        serializer = serializers.MultiExperimentDownloadSerializer(data=request.data)
        if serializer.is_valid():
            download_id = str(uuid.uuid4())
            request.session["download_experiments:" + download_id] = serializer.validated_data
            download_url = reverse('airavata_django_portal_sdk:download_experiments',
                                   args=[download_id])
            return Response({"download_url": download_url})
        else:
            return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)
    elif request.method == 'GET' and download_id is not None:
        download_key = f"download_experiments:{download_id}"
        if download_key in request.session:
            download_spec = request.session[download_key]
            experiments = download_spec['experiments']
            filename = download_spec.get("filename")
            if filename is None:
                filename = "experiments.zip"
            filename = get_valid_filename(filename)
            zipfile_entries = _get_experiment_directories_zipfile_entries(request, experiments)
            return _create_zip_response(request, filename, zipfile_entries)
        else:
            return Response({"detail": "Not found."}, status=status.HTTP_404_NOT_FOUND)
    else:
        return Response({"detail": "Bad request"}, status=status.HTTP_400_BAD_REQUEST)


def _get_directory_zipfile_entries(request, path, directory=""):
    directories, files = user_storage.listdir(request, os.path.join(path, directory))
    for file in files:
        yield os.path.join(directory, file['name']), file["data-product-uri"], file["size"]
    for d in directories:
        yield from _get_directory_zipfile_entries(request, path, os.path.join(directory, d['name']))


def _create_zip_response(request, filename, zipfile_entries):
    zf = zipstream.ZipFile(compression=zipstream.ZIP_DEFLATED, allowZip64=True)
    for archive_name, data_product_uri, size in zipfile_entries:
        zf.write_iter(archive_name, _read_file(request, data_product_uri), buffer_size=size)
    response = StreamingHttpResponse(zf, content_type='application/zip')
    response['Content-Disposition'] = f'attachment; filename={filename}'
    return response


def _read_file(request, data_product_uri: str):
    """Return generator that reads in data product."""
    with user_storage.open_file(request, data_product_uri=data_product_uri) as f:
        chunk = f.read(io.DEFAULT_BUFFER_SIZE)
        logger.debug(f"read first chunk of {len(chunk)} bytes of {f.name}")
        while chunk != b'':
            yield chunk
            chunk = f.read(io.DEFAULT_BUFFER_SIZE)
            logger.debug(f"read next chunk of {len(chunk)} bytes of {f.name}")


def _get_experiment_directory_zipfile_entries(request, experiment_id, path, directory="", zipfile_prefix="", includes=None, excludes=None):
    directories, files = user_storage.list_experiment_dir(request, experiment_id, os.path.join(path, directory))
    for file in files:
        matches, rename = _matches_filters(file['name'], includes=includes, excludes=excludes)
        if matches:
            archive_name = os.path.join(zipfile_prefix, directory, rename if rename is not None else file['name'])
            yield archive_name, file["data-product-uri"], file["size"]
    for d in directories:
        yield from _get_experiment_directory_zipfile_entries(
            request, experiment_id, path, directory=os.path.join(directory, d['name']),
            zipfile_prefix=zipfile_prefix)


def _get_experiment_directories_zipfile_entries(request, experiments):
    for experiment in experiments:
        experiment_id = experiment['experiment_id']
        # Load experiment to make sure user has access to experiment
        experiment_model = request.airavata_client.getExperiment(request.authz_token, experiment_id)
        path = experiment['path']
        yield from _get_experiment_directory_zipfile_entries(
            request, experiment_id, path,
            zipfile_prefix=os.path.join(get_valid_filename(experiment_model.experimentName), path),
            includes=experiment['includes'], excludes=experiment['excludes'])


def _matches_filters(filename, includes=None, excludes=None):
    """Return as a tuple True if matching and a new name for the file if renamed."""
    # excludes take precedence
    if excludes is not None and len(excludes) > 0:
        for exclude in excludes:
            if fnmatch.fnmatch(filename, exclude['pattern']):
                return False, None
    # if there are no include patterns, default to include all
    if includes is None or len(includes) == 0:
        return True, None
    for include in includes:
        if fnmatch.fnmatch(filename, include['pattern']):
            rename = include.get('rename')
            if rename:
                root, ext = os.path.splitext(filename)
                template = Template(rename)
                new_filename = template.safe_substitute(root=root, ext=ext)
                new_filename = get_valid_filename(new_filename)
                return True, new_filename
            else:
                return True, None
    return False, None
