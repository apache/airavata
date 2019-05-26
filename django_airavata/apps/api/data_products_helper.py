import logging
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


def save(request, path, file):
    "Save file in path in the user's storage."
    username = request.user.username
    full_path = datastore.save(username, path, file)
    data_product = _save_data_product(request, full_path)
    return data_product


def open(request, data_product):
    "Return file object for replica if it exists in user storage."
    path = _get_replica_filepath(data_product)
    return datastore.open(request.user.username, path)


def exists(request, data_product):
    "Return True if replica for data_product exists in user storage."
    path = _get_replica_filepath(data_product)
    return datastore.exists(request.user.username, path)


def dir_exists(request, path):
    return datastore.exists(request.user.username, path)


def user_file_exists(request, path):
    """If file exists, return data product URI, else None."""
    if datastore.user_file_exists(request.user.username, path):
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
        datastore.delete(request.user.username, path)
        _delete_data_product(request, path)
    except Exception as e:
        logger.exception("Unable to delete file {} for data product uri {}"
                         .format(path, data_product.productUri))
        raise


def listdir(request, path):
    if datastore.user_file_exists(request.user.username, path):
        directories, files = datastore.list_user_dir(
            request.user.username, path)
        directories_data = []
        for d in directories:
            dpath = os.path.join(path, d)
            created_time = datastore.get_created_time(
                request.user.username, dpath)
            size = datastore.size(request.user.username, dpath)
            directories_data.append({'name': d,
                                     'path': dpath,
                                     'created_time': created_time,
                                     'size': size})
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
                               'size': size})
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


def _get_data_product_uri(request, full_path):

    user_file = models.User_Files.objects.filter(
        username=request.user.username, file_path=full_path)
    if user_file.exists():
        product_uri = user_file[0].file_dpu
    else:
        data_product = _save_data_product(request, full_path)
        product_uri = data_product.productUri
    return product_uri


def _save_data_product(request, full_path):
    "Create, register and record in DB a data product for full_path."
    data_product = _create_data_product(request.user.username, full_path)
    product_uri = request.airavata_client.registerDataProduct(
        request.authz_token, data_product)
    data_product.productUri = product_uri
    user_file_instance = models.User_Files(
        username=request.user.username,
        file_path=full_path,
        file_dpu=product_uri)
    user_file_instance.save()
    return data_product


def _delete_data_product(request, full_path):
    # TODO: call API to delete data product from replica catalog when it is
    # available (not currently implemented)
    user_file = models.User_Files.objects.filter(
        username=request.user.username, file_path=full_path)
    if user_file.exists():
        user_file.delete()


def _create_data_product(username, full_path):
    data_product = DataProductModel()
    data_product.gatewayId = settings.GATEWAY_ID
    data_product.ownerName = username
    file_name = os.path.basename(full_path)
    data_product.productName = file_name
    data_product.dataProductType = DataProductType.FILE
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
    data_product.replicaLocations = [data_replica_location]
    return data_product


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
