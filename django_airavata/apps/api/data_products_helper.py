import copy
import logging
import mimetypes
import os
from urllib.parse import urlparse

from django.conf import settings
from django.core.exceptions import ObjectDoesNotExist

from airavata.model.data.replica.ttypes import (
    DataProductModel,
    DataProductType,
    DataReplicaLocationModel,
    ReplicaLocationCategory,
    ReplicaPersistentType
)

from . import datastore, models

logger = logging.getLogger(__name__)
TMP_INPUT_FILE_UPLOAD_DIR = "tmp"


def save(request, path, file, name=None, content_type=None):
    "Save file in path in the user's storage."
    username = request.user.username
    full_path = datastore.save(username, path, file, name=name)
    data_product = _save_data_product(request, full_path, name=name,
                                      content_type=content_type)
    return data_product


def move_from_filepath(request, source_path, target_path, name=None,
                       content_type=None):
    "Move a file from filesystem into user's storage."
    username = request.user.username
    file_name = name if name is not None else os.path.basename(source_path)
    full_path = datastore.move_external(
        source_path, username, target_path, file_name)
    data_product = _save_data_product(request, full_path, name=file_name,
                                      content_type=content_type)
    return data_product


def save_input_file_upload(request, file, name=None, content_type=None):
    """Save input file in staging area for input file uploads."""
    username = request.user.username
    file_name = name if name is not None else os.path.basename(file.name)
    full_path = datastore.save(username, TMP_INPUT_FILE_UPLOAD_DIR, file)
    data_product = _save_data_product(request, full_path, name=file_name,
                                      content_type=content_type)
    return data_product


def copy_input_file_upload(request, data_product):
    path = _get_replica_filepath(data_product)
    name = data_product.productName
    full_path = datastore.copy(data_product.ownerName,
                               path,
                               request.user.username,
                               TMP_INPUT_FILE_UPLOAD_DIR,
                               name=name)
    return _save_copy_of_data_product(request, full_path, data_product)


def is_input_file_upload(request, data_product):
    # Check if file is one of user's files and in TMP_INPUT_FILE_UPLOAD_DIR
    path = _get_replica_filepath(data_product)
    if datastore.exists(request.user.username, path):
        rel_path = datastore.rel_path(request.user.username, path)
        return os.path.dirname(rel_path) == TMP_INPUT_FILE_UPLOAD_DIR
    else:
        return False


def move_input_file_upload(request, data_product, path):
    source_path = _get_replica_filepath(data_product)
    file_name = data_product.productName
    full_path = datastore.move(
        data_product.ownerName,
        source_path,
        request.user.username,
        path,
        file_name)
    _delete_data_product(data_product.ownerName, source_path)
    data_product = _save_copy_of_data_product(request, full_path, data_product)
    return data_product


def move_input_file_upload_from_filepath(request, source_path, name=None,
                                         content_type=None):
    "Move a file from filesystem into user's input file staging area."
    username = request.user.username
    file_name = name if name is not None else os.path.basename(source_path)
    full_path = datastore.move_external(
        source_path, username, TMP_INPUT_FILE_UPLOAD_DIR, file_name)
    data_product = _save_data_product(request, full_path, name=file_name,
                                      content_type=content_type)
    return data_product


def open_file(request, data_product):
    "Return file object for replica if it exists in user storage."
    path = _get_replica_filepath(data_product)
    return datastore.open(data_product.ownerName, path)


def exists(request, data_product):
    "Return True if replica for data_product exists in user storage."
    path = _get_replica_filepath(data_product)
    return datastore.exists(data_product.ownerName, path)


def dir_exists(request, path):
    return datastore.dir_exists(request.user.username, path)


def user_file_exists(request, path):
    """If file exists, return data product URI, else None."""
    if datastore.exists(request.user.username, path):
        full_path = datastore.path(request.user.username, path)
        data_product_uri = _get_data_product_uri(request, full_path)
        return data_product_uri
    else:
        return None


def delete_dir(request, path):
    return datastore.delete_dir(request.user.username, path)


def delete(request, data_product):
    "Delete replica for data product in this data store."
    path = _get_replica_filepath(data_product)
    try:
        datastore.delete(data_product.ownerName, path)
        _delete_data_product(data_product.ownerName, path)
    except Exception as e:
        logger.exception("Unable to delete file {} for data product uri {}"
                         .format(path, data_product.productUri))
        raise


def listdir(request, path):
    if datastore.dir_exists(request.user.username, path):
        directories, files = datastore.list_user_dir(
            request.user.username, path)
        directories_data = []
        for d in directories:
            dpath = os.path.join(path, d)
            created_time = datastore.get_created_time(
                request.user.username, dpath)
            size = datastore.size(request.user.username, dpath)
            directories_data.append({
                'name': d,
                'path': dpath,
                'created_time': created_time,
                'size': size,
                'hidden': dpath == TMP_INPUT_FILE_UPLOAD_DIR})
        files_data = []
        for f in files:
            user_rel_path = os.path.join(path, f)
            created_time = datastore.get_created_time(
                request.user.username, user_rel_path)
            size = datastore.size(request.user.username, user_rel_path)
            full_path = datastore.path(request.user.username, user_rel_path)
            data_product_uri = _get_data_product_uri(request, full_path)
            files_data.append({'name': f,
                               'path': user_rel_path,
                               'data-product-uri': data_product_uri,
                               'created_time': created_time,
                               'size': size,
                               'hidden': False})
        return directories_data, files_data
    else:
        raise ObjectDoesNotExist("User storage path does not exist")


def get_experiment_dir(request,
                       project_name=None,
                       experiment_name=None,
                       path=None):
    return datastore.get_experiment_dir(
        request.user.username, project_name, experiment_name, path)


def create_user_dir(request, path):
    return datastore.create_user_dir(request.user.username, path)


def get_rel_path(request, path):
    return datastore.rel_path(request.user.username, path)


def _get_data_product_uri(request, full_path):

    user_file = models.User_Files.objects.filter(
        username=request.user.username, file_path=full_path)
    if user_file.exists():
        product_uri = user_file[0].file_dpu
    else:
        data_product = _save_data_product(request, full_path)
        product_uri = data_product.productUri
    return product_uri


def _save_data_product(request, full_path, name=None, content_type=None):
    "Create, register and record in DB a data product for full_path."
    data_product = _create_data_product(
        request.user.username, full_path, name=name, content_type=content_type)
    product_uri = _register_data_product(request, full_path, data_product)
    data_product.productUri = product_uri
    return data_product


def _register_data_product(request, full_path, data_product):
    product_uri = request.airavata_client.registerDataProduct(
        request.authz_token, data_product)
    user_file_instance = models.User_Files(
        username=request.user.username,
        file_path=full_path,
        file_dpu=product_uri)
    user_file_instance.save()
    return product_uri


def _save_copy_of_data_product(request, full_path, data_product):
    """Save copy of a data product with a different path."""
    data_product_copy = _copy_data_product(request, data_product, full_path)
    product_uri = _register_data_product(request, full_path, data_product_copy)
    data_product_copy.productUri = product_uri
    return data_product_copy


def _copy_data_product(request, data_product, full_path):
    """Create an unsaved copy of a data product with different path."""
    data_product_copy = copy.copy(data_product)
    data_product_copy.productUri = None
    data_product_copy.ownerName = request.user.username
    data_replica_location = _create_replica_location(
        full_path, data_product_copy.productName)
    data_product_copy.replicaLocations = [data_replica_location]
    return data_product_copy


def _delete_data_product(username, full_path):
    # TODO: call API to delete data product from replica catalog when it is
    # available (not currently implemented)
    user_file = models.User_Files.objects.filter(
        username=username, file_path=full_path)
    if user_file.exists():
        user_file.delete()


def _create_data_product(username, full_path, name=None,
                         content_type=None):
    data_product = DataProductModel()
    data_product.gatewayId = settings.GATEWAY_ID
    data_product.ownerName = username
    if name is not None:
        file_name = name
    else:
        file_name = os.path.basename(full_path)
    data_product.productName = file_name
    data_product.dataProductType = DataProductType.FILE
    final_content_type = _determine_content_type(full_path, content_type)
    if final_content_type is not None:
        data_product.productMetadata = {'mime-type': final_content_type}
    data_replica_location = _create_replica_location(full_path, file_name)
    data_product.replicaLocations = [data_replica_location]
    return data_product


def _determine_content_type(full_path, content_type=None):
    result = content_type
    if result is None:
        # Try to guess the content-type from file extension
        guessed_type, encoding = mimetypes.guess_type(full_path)
        result = guessed_type
    if result is None or result == 'application/octet-stream':
        # Check if file is Unicode text by trying to read some of it
        try:
            open(full_path, 'r').read(1024)
            result = 'text/plain'
        except UnicodeDecodeError:
            logger.debug(f"Failed to read as Unicode text: {full_path}")
    return result


def _create_replica_location(full_path, file_name):
    data_replica_location = DataReplicaLocationModel()
    data_replica_location.storageResourceId = \
        settings.GATEWAY_DATA_STORE_RESOURCE_ID
    data_replica_location.replicaName = \
        "{} gateway data store copy".format(file_name)
    data_replica_location.replicaLocationCategory = \
        ReplicaLocationCategory.GATEWAY_DATA_STORE
    data_replica_location.replicaPersistentType = \
        ReplicaPersistentType.TRANSIENT
    data_replica_location.filePath = \
        "file://{}:{}".format(settings.GATEWAY_DATA_STORE_HOSTNAME,
                              full_path)
    return data_replica_location


def _get_replica_filepath(data_product):
    replica_filepaths = [rep.filePath
                         for rep in data_product.replicaLocations
                         if rep.replicaLocationCategory ==
                         ReplicaLocationCategory.GATEWAY_DATA_STORE]
    replica_filepath = (replica_filepaths[0]
                        if len(replica_filepaths) > 0 else None)
    if replica_filepath:
        return urlparse(replica_filepath).path
    return None
