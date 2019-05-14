import os

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


def save(request, path, file):
    # return data_product
    # TODO
    pass


def open(request, data_product):
    # return file object
    # TODO
    pass


def exists(request, data_product):
    # return boolean
    # TODO
    pass


def delete(request, data_product):
    # TODO
    pass


def listdir(request, path):
    if datastore.user_file_exists(request.user.username, path):
        directories, files = datastore.list_user_dir(
            request.user.username, path)
        directories_data = []
        for d in directories:
            directories_data.append({'name': d,
                                     'path': os.path.join(path, d)})
        files_data = []
        for f in files:
            user_rel_path = os.path.join(path, f)
            full_path = datastore.path(request.user.username, user_rel_path)
            data_product_uri = _get_data_product_uri(request, full_path)
            files_data.append({'name': f,
                               'path': user_rel_path,
                               'data-product-uri': data_product_uri})
        return directories_data, files_data
    else:
        raise ObjectDoesNotExist("User storage path does not exist")


def _get_data_product_uri(request, full_path):

    user_file = models.User_Files.objects.filter(
        username=request.user.username, file_path=full_path)
    if user_file.exists():
        product_uri = user_file[0].file_dpu
    else:
        data_product = _create_data_product(request.user.username, full_path)
        product_uri = request.airavata_client.registerDataProduct(
            request.authz_token, data_product)
        user_file_instance = models.User_Files(
            username=request.user.username,
            file_path=full_path,
            file_dpu=product_uri)
        user_file_instance.save()
    return product_uri


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
