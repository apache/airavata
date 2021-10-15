import cgi
import copy
import importlib
import io
import logging
import mimetypes
import os
import warnings
from http import HTTPStatus
from urllib.parse import quote, unquote, urlencode, urlparse

import requests
from airavata.model.data.replica.ttypes import (
    DataProductModel,
    DataProductType,
    DataReplicaLocationModel,
    ReplicaLocationCategory,
    ReplicaPersistentType
)
from django.conf import settings
from django.core.exceptions import ObjectDoesNotExist
from django.urls import reverse

from airavata_django_portal_sdk.user_storage.backends.base import (
    ProvidesDownloadUrl
)

from ..util import convert_iso8601_to_datetime

logger = logging.getLogger(__name__)

TMP_INPUT_FILE_UPLOAD_DIR = "tmp"


def get_user_storage_provider(request, owner_username=None, storage_resource_id=None):
    # TODO: default the module_class_name to MFT provider
    module_class_name = None
    options = {}
    if storage_resource_id is None:
        if not hasattr(settings, 'USER_STORAGES'):
            # make this backward compatible with the older settings
            module_class_name = 'airavata_django_portal_sdk.user_storage.backends.DjangoFileSystemProvider'
            storage_resource_id = settings.GATEWAY_DATA_STORE_RESOURCE_ID
            options = dict(directory=settings.GATEWAY_DATA_STORE_DIR)
            logger.warning("Please add the USER_STORAGES setting. Using legacy GATEWAY_DATA_STORE_RESOURCE_ID and GATEWAY_DATA_STORE_DIR settings.")
        else:
            conf = settings.USER_STORAGES["default"]
            module_class_name = conf['BACKEND']
            storage_resource_id = conf['STORAGE_RESOURCE_ID']
            options = conf.get('OPTIONS', {})
    else:
        if not hasattr(settings, 'USER_STORAGES'):
            # make this backward compatible with the older settings
            module_class_name = 'airavata_django_portal_sdk.user_storage.backends.DjangoFileSystemProvider'
            storage_resource_id = settings.GATEWAY_DATA_STORE_RESOURCE_ID
            options = dict(directory=settings.GATEWAY_DATA_STORE_DIR)
            logger.warning("Please add the USER_STORAGES setting. Using legacy GATEWAY_DATA_STORE_RESOURCE_ID and GATEWAY_DATA_STORE_DIR settings.")
        else:
            for key in settings.USER_STORAGES:
                conf = settings.USER_STORAGES[key]
                if conf['STORAGE_RESOURCE_ID'] == storage_resource_id:
                    module_class_name = conf['BACKEND']
                    options = conf.get('OPTIONS', {})
                    break
    module_name, class_name = module_class_name.rsplit(".", 1)
    module = importlib.import_module(module_name)
    BackendClass = getattr(module, class_name)
    authz_token = request.authz_token
    context = {
        'request': request,
        'owner_username': owner_username,
    }
    instance = BackendClass(authz_token, storage_resource_id, context=context, **options)
    return instance


def get_default_storage_resource_id(request):
    backend = get_user_storage_provider(request)
    return backend.resource_id


def save(request, path, file, name=None, content_type=None, storage_resource_id=None, experiment_id=None):
    """
    Save file in path in the user's storage and return DataProduct. If
    `experiment_id` provided then the path will be relative to the experiment
    data directory.
    """
    if _is_remote_api():
        if name is None and hasattr(file, 'name'):
            name = os.path.basename(file.name)
        files = {'file': (name, file, content_type)
                 if content_type is not None else (name, file)}
        resp = _call_remote_api(request,
                                "/user-storage/~/{path}",
                                path_params={"path": path},
                                data={"experiment-id": experiment_id},
                                method="post",
                                files=files)
        data = resp.json()
        product_uri = data['uploaded']['productUri']
        data_product = request.airavata_client.getDataProduct(
            request.authz_token, product_uri)
        return data_product
    final_path = _get_final_path(request, path, experiment_id)
    backend = get_user_storage_provider(request, storage_resource_id=storage_resource_id)
    storage_resource_id, resource_path = backend.save(final_path, file, name=name, content_type=content_type)
    data_product = _save_data_product(
        request, resource_path, storage_resource_id, name=name, content_type=content_type, backend=backend
    )
    return data_product


def save_input_file(request, file, name=None, content_type=None, storage_resource_id=None):
    """Save input file in staging area for input files."""
    if _is_remote_api():
        if name is None and hasattr(file, 'name'):
            name = os.path.basename(file.name)
        files = {'file': (name, file, content_type)
                 if content_type is not None else (name, file)}
        resp = _call_remote_api(request,
                                "/upload",
                                method="post",
                                files=files)
        data = resp.json()
        product_uri = data['data-product']['productUri']
        data_product = request.airavata_client.getDataProduct(
            request.authz_token, product_uri)
        return data_product
    else:
        backend = get_user_storage_provider(request, storage_resource_id=storage_resource_id)
        file_name = name if name is not None else os.path.basename(file.name)
        storage_resource_id, resource_path = backend.save(
            TMP_INPUT_FILE_UPLOAD_DIR, file, name=file_name)
        data_product = _save_data_product(
            request, resource_path, storage_resource_id, name=name, content_type=content_type, backend=backend
        )
        return data_product


def copy_input_file(request, data_product=None, data_product_uri=None, storage_resource_id=None):
    if data_product is None:
        data_product = _get_data_product(request, data_product_uri)
    source_storage_resource_id, source_resource_path = _get_replica_resource_id_and_filepath(data_product)
    source_backend = get_user_storage_provider(request,
                                               owner_username=data_product.ownerName,
                                               storage_resource_id=source_storage_resource_id)
    file = source_backend.open(source_resource_path)
    name = data_product.productName
    target_backend = get_user_storage_provider(request, storage_resource_id=storage_resource_id)
    storage_resource_id, full_path = target_backend.save(TMP_INPUT_FILE_UPLOAD_DIR, file, name=name)
    return _save_copy_of_data_product(request, full_path, data_product, storage_resource_id)


def is_input_file(request, data_product=None, data_product_uri=None):
    if data_product is None:
        data_product = _get_data_product(request, data_product_uri)
    if _is_remote_api():
        resp = _call_remote_api(
            request,
            "/data-products/",
            params={'product-uri': data_product.productUri})
        data = resp.json()
        return data['isInputFileUpload']
    # Check if file is one of user's files and in TMP_INPUT_FILE_UPLOAD_DIR
    storage_resource_id, path = _get_replica_resource_id_and_filepath(data_product)
    backend = get_user_storage_provider(request,
                                        owner_username=data_product.ownerName,
                                        storage_resource_id=storage_resource_id)
    if backend.is_file(path):
        directories, files = backend.get_metadata(path)
        rel_path = files[0]['path']
        return os.path.dirname(rel_path) == TMP_INPUT_FILE_UPLOAD_DIR
    else:
        return False


def move(request, data_product=None, path=None, data_product_uri=None, storage_resource_id=None):
    if data_product is None:
        data_product = _get_data_product(request, data_product_uri)
    source_storage_resource_id, source_path = _get_replica_resource_id_and_filepath(data_product)
    source_backend = get_user_storage_provider(request,
                                               owner_username=data_product.ownerName,
                                               storage_resource_id=source_storage_resource_id)
    file = source_backend.open(source_path)
    file_name = data_product.productName
    target_backend = get_user_storage_provider(request, storage_resource_id=storage_resource_id)
    storage_resource_id, full_path = target_backend.save(path, file, name=file_name)
    data_product_copy = _save_copy_of_data_product(request, full_path, data_product, storage_resource_id)
    # Remove the source file and data product metadata
    source_backend.delete(source_path)
    _delete_data_product(data_product.ownerName, source_path, storage_resource_id=source_storage_resource_id)
    return data_product_copy


def move_input_file(request, data_product=None, path=None, data_product_uri=None, storage_resource_id=None):
    warnings.warn("Use 'move' instead.", DeprecationWarning)
    return move(request, data_product=data_product, path=path, data_product_uri=data_product_uri, storage_resource_id=storage_resource_id)


def get_download_url(request, data_product=None, data_product_uri=None, force_download=False, mime_type=None):
    "Return URL for downloading data product. One of `data_product` or `data_product_uri` is required."
    if data_product is None:
        data_product = _get_data_product(request, data_product_uri)
    if _is_remote_api():
        # Build a local /sdk/download-file URL that will stream the file from the remote server
        return _build_download_url(request, data_product, force_download=force_download, mime_type=mime_type)
    storage_resource_id, path = _get_replica_resource_id_and_filepath(data_product)
    backend = get_user_storage_provider(request,
                                        owner_username=data_product.ownerName,
                                        storage_resource_id=storage_resource_id)
    if isinstance(backend, ProvidesDownloadUrl):
        return backend.get_download_url(path)
    else:
        # if backend doesn't provide a download url, then use default one
        # that uses backend to read the file
        return _build_download_url(request, data_product, force_download=force_download, mime_type=mime_type)


def _build_download_url(request, data_product, force_download=False, mime_type=None):
    params = {"data-product-uri": data_product.productUri}
    if force_download:
        params['download'] = ''
    if mime_type is not None:
        params['mime-type'] = mime_type
    return request.build_absolute_uri(f"{reverse('airavata_django_portal_sdk:download_file')}?{urlencode(params)}")


def get_lazy_download_url(request, data_product=None, data_product_uri=None):
    if data_product is None:
        data_product = _get_data_product(request, data_product_uri)
    # /download will call get_download_url and redirect to it
    return request.build_absolute_uri(reverse("airavata_django_portal_sdk:download") + "?" +
                                      urlencode({"data-product-uri": data_product.productUri}))


def open_file(request, data_product=None, data_product_uri=None):
    """
    Return file object for replica if it exists in user storage. One of
    `data_product` or `data_product_uri` is required.
    """
    if data_product is None:
        data_product = _get_data_product(request, data_product_uri)
    if _is_remote_api():
        resp = _call_remote_api(
            request,
            "/download",
            params={'data-product-uri': data_product.productUri},
            base_url="/sdk",
            raise_for_status=False)
        _raise_404(resp, f"File does not exist for data product {data_product.productUri}")
        resp.raise_for_status()
        file = io.BytesIO(resp.content)
        disposition = resp.headers['Content-Disposition']
        disp_value, disp_params = cgi.parse_header(disposition)
        # Give the file object a name just like a real opened file object
        file.name = disp_params['filename']
        return file
    else:
        storage_resource_id, path = _get_replica_resource_id_and_filepath(data_product)
        backend = get_user_storage_provider(request,
                                            owner_username=data_product.ownerName,
                                            storage_resource_id=storage_resource_id)
        return backend.open(path)


def exists(request, data_product=None, data_product_uri=None):
    """
    Return True if replica for data_product exists in user storage. One of
    `data_product` or `data_product_uri` is required.
    """
    if data_product is None:
        data_product = _get_data_product(request, data_product_uri)
    if _is_remote_api():
        resp = _call_remote_api(
            request,
            "/data-products/",
            params={'product-uri': data_product.productUri},
            raise_for_status=False)
        if resp.status_code == HTTPStatus.NOT_FOUND:
            return False
        resp.raise_for_status()
        data = resp.json()
        return data['downloadURL'] is not None
    else:
        storage_resource_id, path = _get_replica_resource_id_and_filepath(data_product)
        backend = get_user_storage_provider(request,
                                            owner_username=data_product.ownerName,
                                            storage_resource_id=storage_resource_id)
        return backend.is_file(path)


def dir_exists(request, path, storage_resource_id=None, experiment_id=None):
    """
    Return True if path exists in user's data store. If `experiment_id` provided
    then the path will be relative to the experiment data directory.
    """
    if _is_remote_api():
        resp = _call_remote_api(request,
                                "/user-storage/~/",
                                params={"path": path, "experiment-id": experiment_id},
                                raise_for_status=False)
        if resp.status_code == HTTPStatus.NOT_FOUND:
            return False
        resp.raise_for_status()
        return resp.json()['isDir']
    else:
        final_path, owner_username = _get_final_path_and_owner_username(request, path, experiment_id)
        backend = get_user_storage_provider(
            request, storage_resource_id=storage_resource_id, owner_username=owner_username)
        return backend.is_dir(final_path)


def user_file_exists(request, path, storage_resource_id=None, experiment_id=None):
    """
    If file exists, return data product URI, else None. If `experiment_id`
    provided then the path will be relative to the experiment data directory.
    """
    if _is_remote_api():
        resp = _call_remote_api(request,
                                "/user-storage/~/{path}",
                                path_params={"path": path},
                                params={"experiment-id": experiment_id},
                                raise_for_status=False)
        if resp.status_code == HTTPStatus.NOT_FOUND:
            return None
        resp.raise_for_status()
        if resp.json()['isDir']:
            return None
        else:
            return resp.json()['files'][0]['dataProductURI']
    final_path, owner_username = _get_final_path_and_owner_username(request, path, experiment_id)
    backend = get_user_storage_provider(
        request, storage_resource_id=storage_resource_id, owner_username=owner_username)
    if backend.is_file(final_path):
        _, files = backend.get_metadata(final_path)
        full_path = files[0]['resource_path']
        data_product_uri = _get_data_product_uri(request, full_path,
                                                 backend.resource_id, owner=owner_username)
        return data_product_uri
    else:
        return None


def delete_dir(request, path, storage_resource_id=None, experiment_id=None):
    """
    Delete path in user's data store, if it exists. If `experiment_id` provided
    then the path will be relative to the experiment data directory.
    """
    if _is_remote_api():
        resp = _call_remote_api(request,
                                "/user-storage/~/{path}",
                                path_params={"path": path},
                                data={"experiment-id": experiment_id},
                                method="delete",
                                raise_for_status=False)
        _raise_404(resp, f"File path does not exist {path}")
        resp.raise_for_status()
        return
    backend = get_user_storage_provider(request, storage_resource_id=storage_resource_id)
    final_path = _get_final_path(request, path, experiment_id)
    backend.delete(final_path)


def delete_user_file(request, path, storage_resource_id=None, experiment_id=None):
    """Delete file in user's data store, if it exists."""
    if _is_remote_api():
        resp = _call_remote_api(request,
                                "/user-storage/~/{path}",
                                path_params={"path": path},
                                data={"experiment-id": experiment_id},
                                method="delete",
                                raise_for_status=False)
        _raise_404(resp, f"File path does not exist {path}")
        resp.raise_for_status()
        return
    backend = get_user_storage_provider(request, storage_resource_id=storage_resource_id)
    final_path = _get_final_path(request, path, experiment_id)
    backend.delete(final_path)


def update_file_content(request, path, fileContentText, storage_resource_id=None):
    if _is_remote_api():
        _call_remote_api(request,
                         "/user-storage/~/{path}",
                         path_params={"path": path},
                         method="put",
                         data={"fileContentText": fileContentText}
                         )
        return
    else:
        backend = get_user_storage_provider(request, storage_resource_id=storage_resource_id)
        file = io.StringIO(fileContentText)
        backend.update(path, file)


def update_data_product_content(request, data_product=None, fileContentText="", data_product_uri=None):
    if data_product is None:
        data_product = _get_data_product(request, data_product_uri)
    if _is_remote_api():
        _call_remote_api(request,
                         "/data-products/",
                         params={'product-uri': data_product.productUri},
                         method="put",
                         data={"fileContentText": fileContentText},
                         )
        return
    storage_resource_id, path = _get_replica_resource_id_and_filepath(data_product)
    update_file_content(request, path, fileContentText, storage_resource_id=storage_resource_id)


def get_file_metadata(request, path, storage_resource_id=None, experiment_id=None):
    if _is_remote_api():
        resp = _call_remote_api(request,
                                "/user-storage/~/{path}",
                                path_params={"path": path},
                                params={"experiment-id": experiment_id},
                                raise_for_status=False
                                )
        _raise_404(resp, "User storage file path does not exist")
        data = resp.json()
        if data["isDir"]:
            raise Exception("User storage path is a directory, not a file")
        file = data['files'][0]
        file['created_time'] = convert_iso8601_to_datetime(file['createdTime'])
        file['mime_type'] = file['mimeType']
        file['data-product-uri'] = file['dataProductURI']
        return file

    final_path, owner_username = _get_final_path_and_owner_username(request, path, experiment_id)
    backend = get_user_storage_provider(request, storage_resource_id=storage_resource_id, owner_username=owner_username)
    if backend.is_file(final_path):
        _, files = backend.get_metadata(final_path)
        file = files[0]
        data_product_uri = _get_data_product_uri(request, file['resource_path'],
                                                 storage_resource_id=backend.resource_id,
                                                 owner=owner_username)

        data_product = request.airavata_client.getDataProduct(
            request.authz_token, data_product_uri)
        mime_type = None
        if 'mime-type' in data_product.productMetadata:
            mime_type = data_product.productMetadata['mime-type']
        file['data-product-uri'] = data_product_uri
        file['mime_type'] = mime_type
        # TODO: remove this, there's no need for hidden files
        file['hidden'] = False
        return file
    else:
        raise ObjectDoesNotExist("File does not exist at that path.")


def get_data_product_metadata(request, data_product=None, data_product_uri=None):
    if data_product is None:
        data_product = _get_data_product(request, data_product_uri)
    storage_resource_id, path = _get_replica_resource_id_and_filepath(data_product)
    if _is_remote_api():
        resp = _call_remote_api(
            request,
            "/data-products/",
            params={'product-uri': data_product.productUri})
        data = resp.json()
        file = {
            "name": os.path.basename(path),
            # FIXME: since this isn't the true relative path, going to leave out for now
            # "path": path,
            "resource_path": path,
            "created_time": convert_iso8601_to_datetime(data['creationTime'], microseconds=False),
            "size": data['filesize']
        }
        mime_type = None
        if 'mime-type' in data_product.productMetadata:
            mime_type = data_product.productMetadata['mime-type']
        file['data-product-uri'] = data_product_uri
        file['mime_type'] = mime_type
        # TODO: remove this, there's no need for hidden files
        file['hidden'] = False
        return file
    backend = get_user_storage_provider(request,
                                        owner_username=data_product.ownerName,
                                        storage_resource_id=storage_resource_id)
    if backend.is_file(path):
        _, files = backend.get_metadata(path)
        file = files[0]
        mime_type = None
        if 'mime-type' in data_product.productMetadata:
            mime_type = data_product.productMetadata['mime-type']
        file['data-product-uri'] = data_product_uri
        file['mime_type'] = mime_type
        # TODO: remove this, there's no need for hidden files
        file['hidden'] = False
        return file
    else:
        raise ObjectDoesNotExist("File does not exist at that path.")


def get_file(request, path, storage_resource_id=None, experiment_id=None):
    warnings.warn("Use 'get_file_metadata' instead.", DeprecationWarning)
    return get_file_metadata(request, path, storage_resource_id, experiment_id=experiment_id)


def delete(request, data_product=None, data_product_uri=None):
    """
    Delete replica for data product in this data store. One of `data_product`
    or `data_product_uri` is required.
    """
    if data_product is None:
        data_product = _get_data_product(request, data_product_uri)
    if _is_remote_api():
        _call_remote_api(
            request,
            "/delete-file",
            params={'data-product-uri': data_product.productUri},
            method="delete")
        return
    else:
        storage_resource_id, path = _get_replica_resource_id_and_filepath(data_product)
        backend = get_user_storage_provider(request, storage_resource_id=storage_resource_id)
        try:
            backend.delete(path)
            _delete_data_product(data_product.ownerName, path, storage_resource_id)
        except Exception:
            logger.exception(
                "Unable to delete file {} for data product uri {}".format(
                    path, data_product.productUri
                )
            )
            raise


def listdir(request, path, storage_resource_id=None, experiment_id=None):
    """
    Return a tuple of two lists, one for directories, the second for files.  If
    `experiment_id` provided then the path will be relative to the experiment
    data directory.
    """

    if _is_remote_api():
        resp = _call_remote_api(request,
                                "/user-storage/~/",
                                params={"path": path, "experiment-id": experiment_id},
                                )
        data = resp.json()
        for directory in data['directories']:
            # Convert JSON ISO8601 timestamp to datetime instance
            directory['created_time'] = convert_iso8601_to_datetime(
                directory['createdTime'])
        for file in data['files']:
            # Convert JSON ISO8601 timestamp to datetime instance
            file['created_time'] = convert_iso8601_to_datetime(
                file['createdTime'])
            file['mime_type'] = file['mimeType']
            file['data-product-uri'] = file['dataProductURI']
        return data['directories'], data['files']

    final_path, owner_username = _get_final_path_and_owner_username(request, path, experiment_id)
    backend = get_user_storage_provider(request, storage_resource_id=storage_resource_id,
                                        owner_username=owner_username)
    directories, files = backend.get_metadata(final_path)
    # Mark the TMP_INPUT_FILE_UPLOAD_DIR directory as hidden in the UI
    for directory in directories:
        directory['hidden'] = directory['path'] == TMP_INPUT_FILE_UPLOAD_DIR
    # for each file, lookup or register a data product and enrich the file
    # metadata with data-product-uri and mime-type
    for file in files:
        data_product_uri = _get_data_product_uri(request, file['resource_path'],
                                                 storage_resource_id=backend.resource_id,
                                                 owner=owner_username,
                                                 backend=backend)

        data_product = request.airavata_client.getDataProduct(
            request.authz_token, data_product_uri)
        mime_type = None
        if 'mime-type' in data_product.productMetadata:
            mime_type = data_product.productMetadata['mime-type']
        file['data-product-uri'] = data_product_uri
        file['mime_type'] = mime_type
        # TODO: remove this, there's no need for hidden files
        file['hidden'] = False
    return directories, files


def list_experiment_dir(request, experiment_id, path="", storage_resource_id=None):
    """
    List files, directories in experiment data directory. Returns a tuple,
    see `listdir`.
    """
    if _is_remote_api():
        resp = _call_remote_api(request,
                                "/experiment-storage/{experiment_id}/{path}",
                                path_params={"path": path,
                                             "experiment_id": experiment_id},
                                )
        data = resp.json()
        for directory in data['directories']:
            # Convert JSON ISO8601 timestamp to datetime instance
            directory['created_time'] = convert_iso8601_to_datetime(
                directory['createdTime'])
        for file in data['files']:
            # Convert JSON ISO8601 timestamp to datetime instance
            file['created_time'] = convert_iso8601_to_datetime(
                file['createdTime'])
            file['mime_type'] = file['mimeType']
            file['data-product-uri'] = file['dataProductURI']
        return data['directories'], data['files']

    experiment = request.airavata_client.getExperiment(
        request.authz_token, experiment_id)
    backend = get_user_storage_provider(request,
                                        storage_resource_id=storage_resource_id,
                                        owner_username=experiment.userName)
    exp_data_dir = experiment.userConfigurationData.experimentDataDir
    exp_data_path = os.path.join(exp_data_dir, path)
    if backend.exists(exp_data_path):
        directories, files = backend.get_metadata(exp_data_path)
        for directory in directories:
            # construct the relative path of the directory within the experiment data dir
            directory['path'] = os.path.relpath(directory['resource_path'], exp_data_dir)
        # for each file, lookup or register a data product and enrich the file
        # metadata with data-product-uri and mime-type
        for file in files:
            data_product_uri = _get_data_product_uri(request, file['resource_path'],
                                                     storage_resource_id=backend.resource_id,
                                                     backend=backend, owner=experiment.userName)

            data_product = request.airavata_client.getDataProduct(
                request.authz_token, data_product_uri)
            mime_type = None
            if 'mime-type' in data_product.productMetadata:
                mime_type = data_product.productMetadata['mime-type']
            file['data-product-uri'] = data_product_uri
            file['mime_type'] = mime_type
            # construct the relative path of the file within the experiment data dir
            file['path'] = os.path.relpath(file['resource_path'], exp_data_dir)
            # TODO: remove this, there's no need for hidden files
            file['hidden'] = False
        return directories, files
    else:
        raise ObjectDoesNotExist("Experiment data directory does not exist")


def experiment_dir_exists(request, experiment_id, path="", storage_resource_id=None):
    "Returns True if the path exists in the given experiment's data directory."
    if _is_remote_api():
        resp = _call_remote_api(request,
                                "/experiment-storage/{experiment_id}/{path}",
                                path_params={"path": path,
                                             "experiment_id": experiment_id},
                                raise_for_status=False)
        if resp.status_code == HTTPStatus.NOT_FOUND:
            return False
        resp.raise_for_status()
        return resp.json()['isDir']

    experiment = request.airavata_client.getExperiment(
        request.authz_token, experiment_id)
    exp_data_path = experiment.userConfigurationData.experimentDataDir
    if exp_data_path is None:
        return False
    backend = get_user_storage_provider(request,
                                        storage_resource_id=storage_resource_id,
                                        owner_username=experiment.userName)
    return backend.exists(exp_data_path)


def get_experiment_dir(request, project_name=None, experiment_name=None, path=None, storage_resource_id=None):
    warnings.warn("Use 'create_user_dir' instead.", DeprecationWarning)
    storage_resource_id, resource_path = create_user_dir(request,
                                                         dir_names=[project_name, experiment_name],
                                                         create_unique=True,
                                                         storage_resource_id=storage_resource_id)
    return resource_path


def create_user_dir(request, path="", dir_names=(), create_unique=False, storage_resource_id=None, experiment_id=None):
    """
    Creates a directory, and intermediate directories if given, at the given
    path in the user's storage.  `dir_names` should be either a list or tuple of
    directories names to create at the given path.  If `create_unique` is True
    and the given `dir_names` results in an already existing directory, the
    `dir_names` will be modified (for example, random suffix added) until it
    results in a name for a directory that doesn't exist and that directory will
    get created.  If `create_unique` is False (the default) and the directory
    already exists, no directory will be created, but the directory resource
    information will be returned.  `dir_names` may also be modified to sanitize
    them for file paths, for example, converting spaces to underscores.  Returns
    a tuple of the storage_resource_id and resource_path of the directory
    resource.

    If `experiment_id` provided then the path will be relative to the experiment
    data directory.
    """
    if _is_remote_api():
        logger.debug(f"path={path}")
        resp = _call_remote_api(request,
                                "/user-storage/~/{path}",
                                path_params={"path": path},
                                data={"experiment-id": experiment_id},
                                method="post")
        json = resp.json()
        # 'path' is a new response attribute, for backwards compatibility check if it exists first
        if 'path' in json:
            path = json['path']
        # FIXME: should use the storage_resource_id returned from remote API call
        return storage_resource_id, path
    backend = get_user_storage_provider(request, storage_resource_id=storage_resource_id)
    # For backwards compatibility, manufacture the dir_names array as needed
    final_path = _get_final_path(request, path, experiment_id)
    if len(dir_names) == 0:
        dir_names = []
        while not backend.exists(final_path):
            final_path, dir_name = os.path.split(final_path)
            if dir_name == '':
                break
            dir_names.insert(0, dir_name)
    storage_resource_id, resource_path = backend.create_dirs(final_path, dir_names=dir_names, create_unique=create_unique)
    return storage_resource_id, resource_path


def create_symlink(request, src_path, dest_path, storage_resource_id=None):
    """Create link named dest_path pointing to src_path on storage resource."""
    if _is_remote_api():
        logger.warning("create_symlink isn't supported in Remote API mode")
        return
    backend = get_user_storage_provider(request, storage_resource_id=storage_resource_id)
    backend.create_symlink(src_path, dest_path)


def get_rel_experiment_dir(request, experiment_id, storage_resource_id=None):
    """Return experiment data dir path relative to user's directory."""
    warnings.warn("Use 'list_experiment_dir' instead.", DeprecationWarning)
    if _is_remote_api():
        resp = _call_remote_api(request,
                                "/experiments/{experimentId}/",
                                path_params={"experimentId": experiment_id})
        resp.raise_for_status()
        return resp.json()['relativeExperimentDataDir']

    experiment = request.airavata_client.getExperiment(
        request.authz_token, experiment_id)
    if (experiment.userConfigurationData and
            experiment.userConfigurationData.experimentDataDir):
        backend = get_user_storage_provider(request, storage_resource_id=storage_resource_id)
        data_dir = experiment.userConfigurationData.experimentDataDir
        if backend.exists(data_dir):
            directories, _ = backend.get_metadata(os.path.dirname(data_dir))
            for directory in directories:
                if directory['name'] == os.path.basename(data_dir):
                    return directory['path']
            raise Exception(f"Could not find relative path to experiment data dir {data_dir}")
        else:
            return None
    else:
        return None


def _get_data_product_uri(request, full_path, storage_resource_id, owner=None, backend=None):

    from airavata_django_portal_sdk import models
    if owner is None:
        owner = request.user.username
    user_file = models.UserFiles.objects.filter(
        username=owner, file_path=full_path, file_resource_id=storage_resource_id)
    if user_file.exists():
        product_uri = user_file[0].file_dpu
    else:
        data_product = _save_data_product(request, full_path, storage_resource_id, owner=owner, backend=backend)
        product_uri = data_product.productUri
    return product_uri


def _get_data_product(request, data_product_uri):
    return request.airavata_client.getDataProduct(
        request.authz_token, data_product_uri)


def _save_data_product(request, full_path, storage_resource_id, name=None, content_type=None, owner=None, backend=None):
    "Create, register and record in DB a data product for full_path."
    if owner is None:
        owner = request.user.username
    data_product = _create_data_product(
        owner, full_path, storage_resource_id, name=name, content_type=content_type, backend=backend
    )
    product_uri = _register_data_product(request, full_path, data_product, storage_resource_id, owner=owner)
    data_product.productUri = product_uri
    return data_product


def _register_data_product(request, full_path, data_product, storage_resource_id, owner=None):
    if owner is None:
        owner = request.user.username
    product_uri = request.airavata_client.registerDataProduct(
        request.authz_token, data_product
    )
    from airavata_django_portal_sdk import models
    user_file_instance = models.UserFiles(
        username=owner,
        file_path=full_path,
        file_dpu=product_uri,
        file_resource_id=storage_resource_id)
    user_file_instance.save()
    return product_uri


def _save_copy_of_data_product(request, full_path, data_product, storage_resource_id):
    """Save copy of a data product with a different path."""
    data_product_copy = _copy_data_product(request, data_product, full_path, storage_resource_id)
    product_uri = _register_data_product(request, full_path, data_product_copy, storage_resource_id)
    data_product_copy.productUri = product_uri
    return data_product_copy


def _copy_data_product(request, data_product, full_path, storage_resource_id):
    """Create an unsaved copy of a data product with different path."""
    data_product_copy = copy.copy(data_product)
    data_product_copy.productUri = None
    data_product_copy.ownerName = request.user.username
    data_replica_location = _create_replica_location(
        full_path, data_product_copy.productName, storage_resource_id
    )
    data_product_copy.replicaLocations = [data_replica_location]
    return data_product_copy


def _delete_data_product(username, full_path, storage_resource_id):
    # TODO: call API to delete data product from replica catalog when it is
    # available (not currently implemented)
    from airavata_django_portal_sdk import models
    user_file = models.UserFiles.objects.filter(
        username=username, file_path=full_path, file_resource_id=storage_resource_id)
    if user_file.exists():
        user_file.delete()


def _create_data_product(username, full_path, storage_resource_id, name=None, content_type=None, backend=None):
    data_product = DataProductModel()
    data_product.gatewayId = settings.GATEWAY_ID
    data_product.ownerName = username
    if name is not None:
        file_name = name
    else:
        file_name = os.path.basename(full_path)
    data_product.productName = file_name
    data_product.dataProductType = DataProductType.FILE
    final_content_type = _determine_content_type(full_path, content_type, backend=backend)
    if final_content_type is not None:
        data_product.productMetadata = {"mime-type": final_content_type}
    data_replica_location = _create_replica_location(full_path, file_name, storage_resource_id)
    data_product.replicaLocations = [data_replica_location]
    return data_product


def _determine_content_type(full_path, content_type=None, backend=None):
    result = content_type
    if result is None:
        # Try to guess the content-type from file extension
        guessed_type, encoding = mimetypes.guess_type(full_path)
        result = guessed_type
    if result is None or result == "application/octet-stream":
        # Check if file is Unicode text by trying to read some of it
        try:
            if backend is not None:
                with backend.open(full_path) as file:
                    # Try to decode the first kb as UTF8
                    file.read(1024).decode('utf-8')
                    result = "text/plain"
        except UnicodeDecodeError:
            logger.debug(f"Failed to read as Unicode text: {full_path}")
    return result


def _create_replica_location(full_path, file_name, storage_resource_id):
    data_replica_location = DataReplicaLocationModel()
    data_replica_location.storageResourceId = storage_resource_id
    data_replica_location.replicaName = "{} gateway data store copy".format(
        file_name)
    data_replica_location.replicaLocationCategory = (
        ReplicaLocationCategory.GATEWAY_DATA_STORE
    )
    data_replica_location.replicaPersistentType = ReplicaPersistentType.TRANSIENT
    data_replica_location.filePath = quote(full_path)
    return data_replica_location


def _get_replica_filepath(data_product):
    replica_filepaths = [
        rep.filePath
        for rep in data_product.replicaLocations
        if rep.replicaLocationCategory == ReplicaLocationCategory.GATEWAY_DATA_STORE
    ]
    replica_filepath = replica_filepaths[0] if len(
        replica_filepaths) > 0 else None
    if replica_filepath:
        return unquote(urlparse(replica_filepath).path)
    return None


def _get_replica_location(data_product, category=ReplicaLocationCategory.GATEWAY_DATA_STORE):
    replica_locations = [
        rep
        for rep in data_product.replicaLocations
        if rep.replicaLocationCategory == ReplicaLocationCategory.GATEWAY_DATA_STORE
    ]
    return replica_locations[0] if len(replica_locations) > 0 else None


def _get_replica_resource_id_and_filepath(data_product):
    replica_location = _get_replica_location(data_product)
    if replica_location is not None:
        return (replica_location.storageResourceId,
                unquote(urlparse(replica_location.filePath).path))
    else:
        return None, None


def _get_final_path(request, path, experiment_id):
    "If experiment_id is given, join the path to it's data directory"
    final_path = path
    if experiment_id is not None:
        experiment = request.airavata_client.getExperiment(
            request.authz_token, experiment_id)
        exp_data_dir = experiment.userConfigurationData.experimentDataDir
        final_path = os.path.join(exp_data_dir, path)
    return final_path


def _get_final_path_and_owner_username(request, path, experiment_id):
    "If experiment_id is given, join the path to it's data directory and also return owner username"
    final_path = path
    if experiment_id is not None:
        experiment = request.airavata_client.getExperiment(
            request.authz_token, experiment_id)
        exp_data_dir = experiment.userConfigurationData.experimentDataDir
        return os.path.join(exp_data_dir, path), experiment.userName
    return final_path, None


def _is_remote_api():
    return getattr(settings, 'GATEWAY_DATA_STORE_REMOTE_API', None) is not None


def _call_remote_api(
        request,
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


def _raise_404(response, msg, exception_class=ObjectDoesNotExist):
    if response.status_code == 404:
        raise exception_class(msg)
